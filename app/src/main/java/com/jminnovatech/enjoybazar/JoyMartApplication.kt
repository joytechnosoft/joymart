package com.jminnovatech.joymart

import android.app.Application
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.remote.api.RetrofitClient

class JoyMartApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)
    }
}
