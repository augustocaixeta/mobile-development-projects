package br.edu.iftm.workouttracker.repository

import br.edu.iftm.workouttracker.data.Corrida
import br.edu.iftm.workouttracker.data.CorridaDao
import kotlinx.coroutines.flow.Flow

class CorridaRepository(private val corridaDao: CorridaDao) {

    fun observarCorridas(): Flow<List<Corrida>> = corridaDao.observarTodas()

    suspend fun registrarCorrida(distanciaKm: Double, tempoSegundos: Long): Long =
        corridaDao.inserir(Corrida(distanciaKm = distanciaKm, tempoSegundos = tempoSegundos))
}
