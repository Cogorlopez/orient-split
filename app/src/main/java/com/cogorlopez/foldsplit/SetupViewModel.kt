package com.cogorlopez.foldsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SetupViewModel(
    val runner: ShizukuCommandRunner,
    foldMonitor: FoldMonitor,
) : ViewModel() {

    val shizukuState: StateFlow<RunnerState> = runner.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, RunnerState.UNAVAILABLE)

    val foldState: StateFlow<FoldState> = foldMonitor.foldStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, FoldState.FOLDED)

    val setupComplete: StateFlow<Boolean> =
        combine(shizukuState, foldState) { shizuku, fold ->
            shizuku == RunnerState.READY && fold == FoldState.FLAT
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun onCleared() {
        super.onCleared()
        runner.destroy()
    }
}
