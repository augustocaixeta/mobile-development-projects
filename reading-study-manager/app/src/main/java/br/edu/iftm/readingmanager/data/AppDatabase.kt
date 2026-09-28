package br.edu.iftm.readingmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Acesso às consultas do catálogo de livros.
     *
     * @return DAO gerado pelo Room.
     */
    abstract fun bookDao(): BookDao

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
                    "reading_manager.db"
                ).build().also { instance = it }
            }
        }
    }
}
