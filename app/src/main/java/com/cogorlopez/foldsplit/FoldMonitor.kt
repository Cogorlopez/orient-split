package com.cogorlopez.foldsplit

import android.app.Activity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class FoldState { FLAT, FOLDED }

class FoldMonitor(private val activity: Activity) {

    fun foldStateFlow(): Flow<FoldState> =
        WindowInfoTracker.getOrCreate(activity)
            .windowLayoutInfo(activity)
            .map { layoutInfo ->
                val fold = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
                when (fold?.state) {
                    FoldingFeature.State.FLAT -> FoldState.FLAT
                    else -> FoldState.FOLDED
                }
            }
}
