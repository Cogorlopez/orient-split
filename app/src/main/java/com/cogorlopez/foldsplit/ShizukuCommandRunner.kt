package com.cogorlopez.foldsplit

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

enum class RunnerState { UNAVAILABLE, PERMISSION_NEEDED, READY }

class ShizukuCommandRunner {

    private val _state = MutableStateFlow(RunnerState.UNAVAILABLE)
    val state: StateFlow<RunnerState> = _state

    private var userService: IUserService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            userService = IUserService.Stub.asInterface(binder)
            _state.value = RunnerState.READY
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            _state.value = RunnerState.UNAVAILABLE
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshState()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        userService = null
        _state.value = RunnerState.UNAVAILABLE
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                bindService()
            } else {
                _state.value = RunnerState.PERMISSION_NEEDED
            }
        }

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        if (userService != null) {
            Shizuku.unbindUserService(userServiceArgs, serviceConnection, false)
            userService = null
        }
    }

    fun refreshState() {
        if (!Shizuku.pingBinder()) {
            _state.value = RunnerState.UNAVAILABLE
            return
        }
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _state.value = RunnerState.PERMISSION_NEEDED
            return
        }
        if (userService == null) {
            bindService()
        }
    }

    fun requestPermission() {
        Shizuku.requestPermission(REQUEST_CODE)
    }

    private fun bindService() {
        Shizuku.bindUserService(userServiceArgs, serviceConnection)
    }

    fun setLandscape(landscape: Boolean) {
        // rotation-override 1 = landscape (left-right split)
        // rotation-override 0 = portrait (top-bottom split)
        val rotation = if (landscape) 1 else 0
        runCommand("wm rotation-override $rotation")
    }

    fun clearOverride() {
        runCommand("wm rotation-override -1")
    }

    private fun runCommand(command: String) {
        userService?.runCommand(command)
    }

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            "com.cogorlopez.foldsplit",
            "com.cogorlopez.foldsplit.ShizukuUserService"
        )
    )
        .daemon(false)
        .processNameSuffix("user_service")
        .debuggable(false)
        .version(1)

    companion object {
        private const val REQUEST_CODE = 1001
    }
}
