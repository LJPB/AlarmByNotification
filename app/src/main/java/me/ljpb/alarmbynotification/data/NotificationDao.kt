package me.ljpb.alarmbynotification.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)
 
    @Delete
    suspend fun delete(notification: NotificationEntity)
    
    @Query("select * from notifications where id = :id")
    fun getItem(id: Int): Flow<NotificationEntity>
    
    @Query("select * from notifications order by triggerTime asc")
    fun getAllItem(): Flow<List<NotificationEntity>>
}