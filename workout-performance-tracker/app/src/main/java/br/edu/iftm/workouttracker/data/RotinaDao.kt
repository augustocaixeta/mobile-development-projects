package br.edu.iftm.workouttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RotinaDao {

    @Insert
    abstract suspend fun inserir(rotina: Rotina): Long

    @Update
    abstract suspend fun atualizar(rotina: Rotina)

    @Delete
    abstract suspend fun excluir(rotina: Rotina)

    @Insert
    abstract suspend fun inserirRegistros(registros: List<RegistroCarga>)

    @Query("DELETE FROM registro_carga WHERE rotinaId = :rotinaId")
    abstract suspend fun removerRegistros(rotinaId: Long)

    @Transaction
    open suspend fun salvarComRegistros(rotina: Rotina, registros: List<RegistroCarga>): Long {
        val id = if (rotina.id == 0L) {
            inserir(rotina)
        } else {
            atualizar(rotina)
            rotina.id
        }
        removerRegistros(id)
        inserirRegistros(registros.map { it.copy(id = 0, rotinaId = id, dataHora = rotina.realizadoEm) })
        return id
    }

    @Transaction
    @Query("SELECT * FROM rotina ORDER BY realizadoEm DESC")
    abstract fun observarTodasComExercicios(): Flow<List<RotinaComExercicios>>

    @Transaction
    @Query("SELECT * FROM rotina WHERE id = :rotinaId")
    abstract fun observarComExercicios(rotinaId: Long): Flow<RotinaComExercicios?>

    @Transaction
    @Query("SELECT * FROM rotina WHERE id = :rotinaId")
    abstract suspend fun obterComExercicios(rotinaId: Long): RotinaComExercicios?
}
