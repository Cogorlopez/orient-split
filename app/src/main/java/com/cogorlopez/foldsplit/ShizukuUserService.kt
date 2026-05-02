package com.cogorlopez.foldsplit

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ShizukuUserService : Service() {

    private val binder = object : IUserService.Stub() {
        override fun runCommand(command: String) {
            Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        }

        override fun destroy() {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
