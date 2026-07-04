package io.github.hotroso.vietnameselunarcalendar.lunar

import kotlin.math.floor
import kotlin.math.sin

/**
 * Vietnamese Lunar Calendar Algorithm - based on Ho Ngoc Duc's algorithm.
 * Implements astronomical calculations using Jean Meeus formulas.
 *
 * Reference: https://www.informatik.uni-leipzig.de/~duc/amlich/
 */
object VietCalendar {

    private const val PI = Math.PI
    private const val TIMEZONE = 7.0 // Vietnam GMT+7

    // Julian Day epoch for 1/1/1900 at Vietnam timezone
    private const val JULIAN_EPOCH = 2415021.076998695

    // Synodic month (average lunar month length)
    private const val SYNODIC_MONTH = 29.530588853

    /**
     * Compute Julian Day Number from a Gregorian date.
     * Supports both Julian calendar (before 15/10/1582) and Gregorian calendar.
     */
    fun jdFromDate(day: Int, month: Int, year: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        var jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    /**
     * Convert Julian Day Number to Gregorian date.
     */
    fun jdToDate(jd: Int): SolarDate {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) { // After 15/10/1582
            val aa = jd + 32044
            val bb = (4 * aa + 3) / 146097
            c = aa - (146097 * bb) / 4
            a = bb
        } else {
            c = jd + 32082
            a = 0
        }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * a + d - 4800 + m / 10
        return SolarDate(day, month, year)
    }

    /**
     * Compute the time of the k-th new moon after the new moon of 1/1/1900.
     * Returns Julian Day Number of the new moon.
     */
    fun getNewMoonDay(k: Int): Int {
        val t = k.toDouble() / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180.0

        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)

        // Sun's mean anomaly
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        // Moon's mean anomaly
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        // Moon's argument of latitude
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3

        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr)
        c1 -= 0.4068 * sin(mpr * dr)
        c1 += 0.0161 * sin(2.0 * mpr * dr)
        c1 += 0.0104 * sin(2.0 * f * dr)
        c1 -= 0.0074 * sin((m - mpr) * dr)
        c1 -= 0.0051 * sin((m + mpr) * dr)
        c1 += 0.0021 * sin(2.0 * m * dr)
        c1 += 0.0010 * sin((2.0 * f - mpr) * dr)
        c1 += 0.0005 * sin((2.0 * mpr + m) * dr)
        c1 -= 0.0004 * sin((2.0 * f + m) * dr)
        c1 -= 0.0006 * sin((2.0 * mpr + m) * dr)  // correction
        c1 += 0.0004 * sin((2.0 * f + m) * dr)     // correction
        c1 -= 0.0004 * sin((2.0 * f - m) * dr)

        val deltaT: Double = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 -
                    0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }

        return floor(jd1 + c1 - deltaT + 0.5 + TIMEZONE / 24.0).toInt()
    }

    /**
     * Compute the Sun's longitude at a given Julian Day Number.
     * Returns the longitude in degrees (0-360).
     */
    fun getSunLongitude(jdn: Int): Double {
        val t = (jdn.toDouble() - 0.5 - TIMEZONE / 24.0 - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = PI / 180.0

        // Mean anomaly
        val m = 357.5291 + 35999.0503 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        // Sun's mean longitude
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        // Equation of center
        var dl = (1.9146 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2.0 * m)
        dl += 0.00029 * sin(dr * 3.0 * m)

        // Sun's true longitude
        var l = l0 + dl
        // Correction
        l -= 0.00569
        l -= 0.00478 * sin((125.04 - 1934.136 * t) * dr)

        // Normalize to 0-360
        l -= 360.0 * floor(l / 360.0)
        return l
    }

    /**
     * Get the solar term index (0-23) from Sun's longitude.
     * Each solar term is 15 degrees of solar longitude.
     * Returns the major solar term number (0-11) where each represents 30 degrees.
     */
    fun getSunLongitudeIndex(jdn: Int): Int {
        return floor(getSunLongitude(jdn) / 30.0).toInt()
    }

    /**
     * Find the Julian Day of the 11th lunar month (the month containing Winter Solstice)
     * of the given year.
     */
    fun getLunarMonth11(year: Int): Int {
        val off = jdFromDate(31, 12, year) - JULIAN_EPOCH
        val k = floor(off / SYNODIC_MONTH).toInt()
        var nm = getNewMoonDay(k)
        val sunLong = getSunLongitudeIndex(nm)
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1)
        }
        return nm
    }

    /**
     * Find the index of the leap month after month 11 of the given lunar year.
     * Returns 0 if no leap month.
     */
    fun getLeapMonthOffset(a11: Int): Int {
        val k = floor((a11.toDouble() - JULIAN_EPOCH) / SYNODIC_MONTH + 0.5).toInt()
        var last = 0
        var i = 1
        var arc = floor(getSunLongitude(getNewMoonDay(k + i)) / 30.0).toInt()
        last = arc
        while (i < 14) {
            i++
            arc = floor(getSunLongitude(getNewMoonDay(k + i)) / 30.0).toInt()
            if (arc == last) break
            last = arc
        }
        return i - 1
    }

    /**
     * Convert a Solar date to Vietnamese Lunar date.
     */
    fun solarToLunar(day: Int, month: Int, year: Int): LunarDate {
        val dayNumber = jdFromDate(day, month, year)
        val k = floor((dayNumber.toDouble() - JULIAN_EPOCH) / SYNODIC_MONTH).toInt()

        var monthStart = getNewMoonDay(k + 1)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k)
        }

        var a11 = getLunarMonth11(year)
        var b11 = a11
        val lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = year
            a11 = getLunarMonth11(year - 1)
        } else {
            lunarYear = year + 1
            b11 = getLunarMonth11(year + 1)
        }

        val lunarDay = dayNumber - monthStart + 1
        val diff = floor(((monthStart - a11).toDouble()) / 29.0).toInt()
        var lunarMonth = diff + 11

        var lunarLeap = 0
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = 1
                }
            }
        }

        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            return LunarDate(
                day = lunarDay,
                month = lunarMonth,
                year = lunarYear - 1,
                isLeapMonth = lunarLeap,
                julianDay = dayNumber
            )
        }

        return LunarDate(
            day = lunarDay,
            month = lunarMonth,
            year = lunarYear,
            isLeapMonth = lunarLeap,
            julianDay = dayNumber
        )
    }

    /**
     * Convert a Lunar date to Solar date.
     */
    fun lunarToSolar(
        lunarDay: Int, lunarMonth: Int, lunarYear: Int, lunarLeap: Int
    ): SolarDate {
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1)
            b11 = getLunarMonth11(lunarYear)
        } else {
            a11 = getLunarMonth11(lunarYear)
            b11 = getLunarMonth11(lunarYear + 1)
        }

        val k = floor((a11.toDouble() - JULIAN_EPOCH) / SYNODIC_MONTH + 0.5).toInt()
        var off = lunarMonth - 11
        if (off < 0) {
            off += 12
        }

        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) {
                leapMonth += 12
            }
            if (lunarLeap != 0 && lunarMonth != leapMonth) {
                // Invalid input
            } else if (lunarLeap != 0 || off >= leapOff) {
                off += 1
            }
        }

        val monthStart = getNewMoonDay(k + off)
        return jdToDate(monthStart + lunarDay - 1)
    }

    /**
     * Convenience wrapper: Solar date object to Lunar date.
     */
    fun solarToLunar(solar: SolarDate): LunarDate {
        return solarToLunar(solar.day, solar.month, solar.year)
    }

    /**
     * Lấy tháng nhuận của năm âm lịch.
     * @return số tháng nhuận (1-12) hoặc 0 nếu năm đó không có tháng nhuận.
     */
    fun getLeapMonthOfYear(lunarYear: Int): Int {
        val a11 = getLunarMonth11(lunarYear - 1)
        val b11 = getLunarMonth11(lunarYear)
        if (b11 - a11 <= 365) return 0

        val leapOff = getLeapMonthOffset(a11)
        var leapMonth = leapOff - 2
        if (leapMonth < 0) leapMonth += 12
        if (leapMonth == 0) leapMonth = 12
        return leapMonth
    }

    /**
     * Check if a solar date is a "good" day (Đại lợi).
     * Simplified check based on lunar day and solar term.
     */
    fun isGoodDay(solarDay: Int, solarMonth: Int, solarYear: Int): Boolean {
        val jd = jdFromDate(solarDay, solarMonth, solarYear)
        val sunIndex = getSunLongitudeIndex(jd)
        // Simplified: even solar term indices tend to be "Đại lợi"
        return sunIndex % 2 == 0
    }

    /**
     * Get day of week from JDN. 0=Monday, 6=Sunday.
     */
    fun getDayOfWeek(day: Int, month: Int, year: Int): Int {
        val jd = jdFromDate(day, month, year)
        return (jd + 1) % 7  // 0=Mon, 1=Tue, ... 6=Sun
    }
}
