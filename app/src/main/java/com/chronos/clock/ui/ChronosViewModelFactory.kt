package com.chronos.clock.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chronos.clock.ChronosApplication
import com.chronos.clock.ui.alarm.AlarmViewModel
import com.chronos.clock.ui.clock.ClockViewModel
import com.chronos.clock.ui.stopwatch.StopwatchViewModel
import com.chronos.clock.ui.timer.TimerViewModel

object ChronosViewModelFactory {

    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as ChronosApplication
            ClockViewModel(app.repository)
        }
        initializer {
            AlarmViewModel(this[APPLICATION_KEY] as ChronosApplication)
        }
        initializer {
            TimerViewModel(this[APPLICATION_KEY] as ChronosApplication)
        }
        initializer {
            StopwatchViewModel(this[APPLICATION_KEY] as ChronosApplication)
        }
    }
}
