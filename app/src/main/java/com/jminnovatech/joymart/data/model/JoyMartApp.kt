package com.jminnovatech.joymart

import android.app.Application
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.remote.api.RetrofitClient

class JoyMartApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ INIT ONCE — BEFORE ANY API / VIEWMODEL
        RetrofitClient.init(SessionManager(this))
    }
}
