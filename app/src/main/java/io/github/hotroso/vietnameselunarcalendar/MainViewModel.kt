package io.github.hotroso.vietnameselunarcalendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.LunarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar

class MainViewModel : ViewModel() {

    private val _selectedDate = MutableLiveData(SolarDate.today())
    val selectedDate: LiveData<SolarDate> = _selectedDate

    private val _lunarDate = MutableLiveData<LunarDate>()
    val lunarDate: LiveData<LunarDate> = _lunarDate

    private val _canChiInfo = MutableLiveData<CanChi.CanChiInfo>()
    val canChiInfo: LiveData<CanChi.CanChiInfo> = _canChiInfo

    init {
        updateDate(SolarDate.today())
    }

    fun updateDate(solar: SolarDate) {
        _selectedDate.value = solar
        val lunar = VietCalendar.solarToLunar(solar)
        _lunarDate.value = lunar
        _canChiInfo.value = CanChi.getFullInfo(lunar)
    }

    fun selectDate(day: Int, month: Int, year: Int) {
        updateDate(SolarDate(day, month, year))
    }

    fun goToToday() {
        updateDate(SolarDate.today())
    }
}
