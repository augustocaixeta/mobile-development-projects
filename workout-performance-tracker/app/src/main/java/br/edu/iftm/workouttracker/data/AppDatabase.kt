package br.edu.iftm.workouttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Rotina::class,
        Exercicio::class,
        RegistroCarga::class,
        Corrida::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rotinaDao(): RotinaDao
    abstract fun exercicioDao(): ExercicioDao
    abstract fun registroCargaDao(): RegistroCargaDao
    abstract fun corridaDao(): CorridaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obterInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val nova = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_tracker.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                instancia = nova
                nova
            }
        }
    }
}
