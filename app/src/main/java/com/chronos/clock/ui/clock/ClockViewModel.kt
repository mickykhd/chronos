package com.chronos.clock.ui.clock

import androidx.lifecycle.ViewModel
import com.chronos.clock.core.SunTimes
import com.chronos.clock.core.describeZone
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.data.WorldCity
import java.time.ZoneId

class ClockViewModel(private val repository: ChronosRepository) : ViewModel() {

    val cities = repository.cities

    val localZone: ZoneId = ZoneId.systemDefault()

    val zoneDescription = describeZone(localZone)

    val sunTimes: SunTimes.Result = SunTimes.calculate(localZone)

    fun addCity(city: WorldCity) = repository.addCity(city)

    fun removeCity(id: String) = repository.removeCity(id)
}
