package br.edu.iftm.workouttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CorridaDao {

    @Insert
    suspend fun inserir(corrida: Corrida): Long

    @Delete
    suspend fun excluir(corrida: Corrida)

    @Query("SELECT * FROM corrida ORDER BY dataHora DESC")
    fun observarTodas(): Flow<List<Corrida>>
}
