package br.edu.iftm.readingmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Book::class, Note::class, Session::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Acesso às consultas do catálogo de livros.
     *
     * @return DAO gerado pelo Room.
     */
    abstract fun bookDao(): BookDao

    /**
     * Acesso às notas do diário de leitura.
     *
     * @return DAO gerado pelo Room.
     */
    abstract fun noteDao(): NoteDao

    /**
     * Acesso às sessões de leitura e ao progresso dos livros.
     *
     * @return DAO gerado pelo Room.
     */
    abstract fun sessionDao(): SessionDao

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
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
