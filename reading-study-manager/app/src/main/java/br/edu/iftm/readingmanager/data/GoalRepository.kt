package br.edu.iftm.readingmanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.goalStore: DataStore<Preferences> by preferencesDataStore(name = "reading_goal")

class GoalRepository(context: Context) {

    private val store = context.applicationContext.goalStore
    private val monthlyPagesKey = intPreferencesKey("monthly_pages")

    /**
     * Observa a meta de páginas por mês. Uma falha de leitura do arquivo é tratada como meta vazia.
     *
     * @return fluxo com a meta, ou null enquanto nenhuma foi definida.
     */
    fun observeMonthlyPages(): Flow<Int?> = store.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[monthlyPagesKey] }

    /**
     * Grava a meta de páginas por mês.
     *
     * @param pages quantidade de páginas, sempre maior que zero.
     */
    suspend fun setMonthlyPages(pages: Int) {
        store.edit { preferences -> preferences[monthlyPagesKey] = pages }
    }
}
