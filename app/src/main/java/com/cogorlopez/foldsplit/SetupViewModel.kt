package com.cogorlopez.foldsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SetupViewModel(val runner: ShizukuCommandRunner) : ViewModel() {

    val shizukuState: StateFlow<RunnerState> = runner.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, RunnerState.UNAVAILABLE)

    val setupComplete: StateFlow<Boolean> = shizukuState
        .map { it == RunnerState.READY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun onCleared() {
        super.onCleared()
        runner.destroy()
    }
}
