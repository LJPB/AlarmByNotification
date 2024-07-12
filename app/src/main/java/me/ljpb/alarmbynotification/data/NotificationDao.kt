package me.ljpb.alarmbynotification.data

import androidx.room.Dao
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
 
    @Query("delete from notifications where notifyId = :notifyId")
    fun delete(notifyId: Int)
    
    @Query("select * from notifications where notifyId = :notifyId")
    fun getItem(notifyId: Int): Flow<NotificationEntity>
    
    @Query("select * from notifications order by triggerTime asc")
    fun getAllItem(): Flow<List<NotificationEntity>?>
    
    @Query("select count(*) from notifications")
    fun count(): Flow<Int>
}