package com.chronos.clock

import android.app.Application
import com.chronos.clock.data.ChronosRepository
import com.chronos.clock.service.ChronosNotifications

class ChronosApplication : Application() {

    lateinit var repository: ChronosRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ChronosRepository(this)
        ChronosNotifications.ensureChannels(this)
    }
}
