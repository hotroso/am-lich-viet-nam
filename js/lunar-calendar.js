/**
 * Vietnamese Lunar Calendar Algorithm
 * Ported from VietCalendar.kt (Ho Ngoc Duc's algorithm)
 * Reference: https://www.informatik.uni-leipzig.de/~duc/amlich/
 */

const VietCalendar = (() => {
    const PI = Math.PI;
    const TIMEZONE = 7.0; // Vietnam GMT+7
    const JULIAN_EPOCH = 2415021.076998695;
    const SYNODIC_MONTH = 29.530588853;

    /**
     * Compute Julian Day Number from Gregorian date.
     */
    function jdFromDate(day, month, year) {
        const a = Math.floor((14 - month) / 12);
        const y = year + 4800 - a;
        const m = month + 12 * a - 3;
        let jd = day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045;
        if (jd < 2299161) {
            jd = day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - 32083;
        }
        return jd;
    }

    /**
     * Convert Julian Day Number to Gregorian date.
     */
    function jdToDate(jd) {
        let a, b, c;
        if (jd > 2299160) {
            const aa = jd + 32044;
            const bb = Math.floor((4 * aa + 3) / 146097);
            c = aa - Math.floor((146097 * bb) / 4);
            a = bb;
        } else {
            c = jd + 32082;
            a = 0;
        }
        const d = Math.floor((4 * c + 3) / 1461);
        const e = c - Math.floor((1461 * d) / 4);
        const m = Math.floor((5 * e + 2) / 153);
        const day = e - Math.floor((153 * m + 2) / 5) + 1;
        const month = m + 3 - 12 * Math.floor(m / 10);
        const year = 100 * a + d - 4800 + Math.floor(m / 10);
        return { day, month, year };
    }

    /**
     * Compute new moon day (k-th new moon after 1/1/1900).
     */
    function getNewMoonDay(k) {
        const t = k / 1236.85;
        const t2 = t * t;
        const t3 = t2 * t;
        const dr = PI / 180.0;

        let jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
        jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr);

        const m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
        const mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
        const f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;

        let c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr);
        c1 -= 0.4068 * Math.sin(mpr * dr);
        c1 += 0.0161 * Math.sin(2.0 * mpr * dr);
        c1 += 0.0104 * Math.sin(2.0 * f * dr);
        c1 -= 0.0074 * Math.sin((m - mpr) * dr);
        c1 -= 0.0051 * Math.sin((m + mpr) * dr);
        c1 += 0.0021 * Math.sin(2.0 * m * dr);
        c1 += 0.0010 * Math.sin((2.0 * f - mpr) * dr);
        c1 += 0.0005 * Math.sin((2.0 * mpr + m) * dr);
        c1 -= 0.0004 * Math.sin((2.0 * f + m) * dr);
        c1 -= 0.0006 * Math.sin((2.0 * mpr + m) * dr);
        c1 += 0.0004 * Math.sin((2.0 * f + m) * dr);
        c1 -= 0.0004 * Math.sin((2.0 * f - m) * dr);

        let deltaT;
        if (t < -11) {
            deltaT = 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3;
        } else {
            deltaT = -0.000278 + 0.000265 * t + 0.000262 * t2;
        }

        return Math.floor(jd1 + c1 - deltaT + 0.5 + TIMEZONE / 24.0);
    }

    /**
     * Compute Sun's longitude at JDN (degrees 0-360).
     */
    function getSunLongitude(jdn) {
        const t = (jdn - 0.5 - TIMEZONE / 24.0 - 2451545.0) / 36525.0;
        const t2 = t * t;
        const dr = PI / 180.0;

        const m = 357.5291 + 35999.0503 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
        const l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2;

        let dl = (1.9146 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m);
        dl += (0.019993 - 0.000101 * t) * Math.sin(dr * 2.0 * m);
        dl += 0.00029 * Math.sin(dr * 3.0 * m);

        let l = l0 + dl;
        l -= 0.00569;
        l -= 0.00478 * Math.sin((125.04 - 1934.136 * t) * dr);
        l -= 360.0 * Math.floor(l / 360.0);
        return l;
    }

    function getSunLongitudeIndex(jdn) {
        return Math.floor(getSunLongitude(jdn) / 30.0);
    }

    function getLunarMonth11(year) {
        const off = jdFromDate(31, 12, year) - JULIAN_EPOCH;
        const k = Math.floor(off / SYNODIC_MONTH);
        let nm = getNewMoonDay(k);
        const sunLong = getSunLongitudeIndex(nm);
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1);
        }
        return nm;
    }

    function getLeapMonthOffset(a11) {
        const k = Math.floor((a11 - JULIAN_EPOCH) / SYNODIC_MONTH + 0.5);
        let last = 0;
        let i = 1;
        let arc = Math.floor(getSunLongitude(getNewMoonDay(k + i)) / 30.0);
        last = arc;
        while (i < 14) {
            i++;
            arc = Math.floor(getSunLongitude(getNewMoonDay(k + i)) / 30.0);
            if (arc === last) break;
            last = arc;
        }
        return i - 1;
    }

    /**
     * Convert Solar date to Vietnamese Lunar date.
     * Returns { day, month, year, isLeapMonth, julianDay }
     */
    function solarToLunar(day, month, year) {
        const dayNumber = jdFromDate(day, month, year);
        const k = Math.floor((dayNumber - JULIAN_EPOCH) / SYNODIC_MONTH);

        let monthStart = getNewMoonDay(k + 1);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k);
        }

        let a11 = getLunarMonth11(year);
        let b11 = a11;
        let lunarYear;
        if (a11 >= monthStart) {
            lunarYear = year;
            a11 = getLunarMonth11(year - 1);
        } else {
            lunarYear = year + 1;
            b11 = getLunarMonth11(year + 1);
        }

        const lunarDay = dayNumber - monthStart + 1;
        const diff = Math.floor((monthStart - a11) / 29.0);
        let lunarMonth = diff + 11;
        let lunarLeap = 0;

        if (b11 - a11 > 365) {
            const leapMonthDiff = getLeapMonthOffset(a11);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff === leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }

        if (lunarMonth > 12) {
            lunarMonth -= 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            return {
                day: lunarDay,
                month: lunarMonth,
                year: lunarYear - 1,
                isLeapMonth: lunarLeap,
                julianDay: dayNumber
            };
        }

        return {
            day: lunarDay,
            month: lunarMonth,
            year: lunarYear,
            isLeapMonth: lunarLeap,
            julianDay: dayNumber
        };
    }

    /**
     * Convert Lunar date to Solar date.
     * Returns { day, month, year }
     */
    function lunarToSolar(lunarDay, lunarMonth, lunarYear, lunarLeap = 0) {
        let a11, b11;
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1);
            b11 = getLunarMonth11(lunarYear);
        } else {
            a11 = getLunarMonth11(lunarYear);
            b11 = getLunarMonth11(lunarYear + 1);
        }

        const k = Math.floor((a11 - JULIAN_EPOCH) / SYNODIC_MONTH + 0.5);
        let off = lunarMonth - 11;
        if (off < 0) {
            off += 12;
        }

        if (b11 - a11 > 365) {
            const leapOff = getLeapMonthOffset(a11);
            let leapMonth = leapOff - 2;
            if (leapMonth < 0) {
                leapMonth += 12;
            }
            if (lunarLeap !== 0 && lunarMonth !== leapMonth) {
                // Invalid
            } else if (lunarLeap !== 0 || off >= leapOff) {
                off += 1;
            }
        }

        const monthStart = getNewMoonDay(k + off);
        return jdToDate(monthStart + lunarDay - 1);
    }

    /**
     * Get leap month of a lunar year. Returns 0 if none.
     */
    function getLeapMonthOfYear(lunarYear) {
        const a11 = getLunarMonth11(lunarYear - 1);
        const b11 = getLunarMonth11(lunarYear);
        if (b11 - a11 <= 365) return 0;
        const leapOff = getLeapMonthOffset(a11);
        let leapMonth = leapOff - 2;
        if (leapMonth < 0) leapMonth += 12;
        if (leapMonth === 0) leapMonth = 12;
        return leapMonth;
    }

    /**
     * Get day of week. 0=Monday, 6=Sunday.
     */
    function getDayOfWeek(day, month, year) {
        const jd = jdFromDate(day, month, year);
        return (jd + 1) % 7;
    }

    return {
        jdFromDate,
        jdToDate,
        getNewMoonDay,
        getSunLongitude,
        getSunLongitudeIndex,
        getLunarMonth11,
        getLeapMonthOffset,
        solarToLunar,
        lunarToSolar,
        getLeapMonthOfYear,
        getDayOfWeek
    };
})();
