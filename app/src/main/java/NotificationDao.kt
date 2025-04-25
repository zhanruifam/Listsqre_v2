package com.example.listsqre_revamped

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY notificationTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications WHERE uniqueId = :uniqueId")
    suspend fun deleteByUniqueId(uniqueId: Int)
}

//class NotificationRepository(private val dao: NotificationDao) {
//    fun getAllNotifications() = dao.getAllNotifications()
//
//    suspend fun insert(notification: NotificationEntity) = withContext(Dispatchers.IO) {
//        dao.insert(notification)
//    }
//
//    suspend fun delete(notification: NotificationEntity) = withContext(Dispatchers.IO) {
//        dao.delete(notification)
//    }
//
//    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
//        dao.deleteById(id)
//    }
//}
