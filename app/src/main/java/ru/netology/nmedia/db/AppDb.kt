package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 7, exportSchema = false)
// При изменении таблицы (добавление новых колонок)
// нужно изменять версию и устанавливать миграцию (**)
// либо заходить в приложение и всё чистить. Иначе получим exception при запуске.
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

}










//    companion object {
//        @Volatile
//        private var instance: AppDb? = null
//
//        fun getInstance(context: Context): AppDb {
//            return instance ?: synchronized(this) {
//                instance ?: buildDatabase(context).also { instance = it }
//            }
//        }
//
//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(context, AppDb::class.java, "app.db")
//                .fallbackToDestructiveMigration(true) // для миграции (**)
//                // .allowMainThreadQueries() - чтобы можно было работать с Room
//                //  на главном потоке (больше не нужно).
//                .build()
//    }