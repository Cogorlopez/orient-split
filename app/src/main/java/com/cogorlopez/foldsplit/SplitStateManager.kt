package com.cogorlopez.foldsplit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "split_prefs")

class SplitStateManager(private val context: Context) {

    companion object {
        private val KEY_IS_LANDSCAPE = booleanPreferencesKey("is_landscape")
    }

    // true = landscape / left-right split, false = portrait / top-bottom split
    val isLandscapeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_IS_LANDSCAPE] ?: true }

    suspend fun toggle() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LANDSCAPE] = !(prefs[KEY_IS_LANDSCAPE] ?: true)
        }
    }

    suspend fun set(landscape: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LANDSCAPE] = landscape
        }
    }
}
