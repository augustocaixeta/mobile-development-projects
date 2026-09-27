package br.edu.iftm.deadlinetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Obligation::class, Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Acesso às consultas de obrigações e lembretes.
     *
     * @return DAO gerado pelo Room.
     */
    abstract fun obligationDao(): ObligationDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Devolve a instância única do banco, criando o arquivo na primeira chamada.
         *
         * @param context qualquer contexto do app.
         * @return banco de dados compartilhado.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deadline_tracker.db"
                ).build().also { instance = it }
            }
        }
    }
}
