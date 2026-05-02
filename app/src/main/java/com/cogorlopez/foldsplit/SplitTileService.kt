package com.cogorlopez.foldsplit

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplitTileService : TileService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var listeningJob: Job? = null

    private lateinit var runner: ShizukuCommandRunner
    private lateinit var stateManager: SplitStateManager

    override fun onCreate() {
        super.onCreate()
        runner = ShizukuCommandRunner()
        runner.init()
        stateManager = SplitStateManager(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        runner.destroy()
        job.cancel()
    }

    override fun onStartListening() {
        super.onStartListening()
        listeningJob = scope.launch {
            combine(
                runner.state,
                stateManager.isLandscapeFlow,
            ) { runnerState, isLandscape -> runnerState to isLandscape }
                .collect { (runnerState, isLandscape) ->
                    updateTile(runnerState, isLandscape)
                }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val runnerState = runner.state.first()
            if (runnerState == RunnerState.PERMISSION_NEEDED) {
                runner.requestPermission()
                return@launch
            }
            if (runnerState != RunnerState.READY) return@launch

            stateManager.toggle()
            val isLandscape = stateManager.isLandscapeFlow.first()
            runner.setLandscape(isLandscape)
        }
    }

    private fun updateTile(runnerState: RunnerState, isLandscape: Boolean) {
        val tile = qsTile ?: return
        when (runnerState) {
            RunnerState.UNAVAILABLE -> {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = "Shizuku not running"
            }
            RunnerState.PERMISSION_NEEDED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = "Tap to grant permission"
            }
            RunnerState.READY -> {
                tile.state = Tile.STATE_ACTIVE
                if (isLandscape) {
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_split_lr)
                    tile.subtitle = "Left-Right"
                } else {
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_split_tb)
                    tile.subtitle = "Top-Bottom"
                }
            }
        }
        tile.updateTile()
    }
}
