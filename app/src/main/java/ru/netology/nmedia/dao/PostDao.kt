package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    suspend fun getSimpleList(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + 1,
        likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes - 1,
        likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun removeLike(id: Long)


    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}

//--------------------------------------------------------------------------------------------------
//                                       - Old code -
// @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
//    fun updateContentById(id: Long, content: String)
//
//    fun save(post: PostEntity) =
//        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)
//
//--------------------------------------------------------------------------------------------------

// @Query(
//        """
//        UPDATE PostEntity SET
//        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
//        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
//        WHERE id = :id
//        """
//    )
//    suspend fun likeById(id: Long)