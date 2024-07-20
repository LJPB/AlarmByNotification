package me.ljpb.alarmbynotification.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarmInfo: AlarmInfoEntity): Long

    @Update
    suspend fun update(alarmInfo: AlarmInfoEntity)

    @Query("delete from alarms where id = :id")
    suspend fun delete(id: Long)

    @Query("select * from alarms where id = :id")
    fun getItem(id: Long): Flow<AlarmInfoEntity?>

    @Query("select * from alarms")
    fun getAllItem(): Flow<List<AlarmInfoEntity>?>
}