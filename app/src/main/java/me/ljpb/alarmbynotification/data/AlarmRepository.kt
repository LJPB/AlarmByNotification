package me.ljpb.alarmbynotification.data

import kotlinx.coroutines.flow.Flow
import me.ljpb.alarmbynotification.data.room.AlarmDao
import me.ljpb.alarmbynotification.data.room.AlarmInfoEntity

interface AlarmRepositoryInterface {
    suspend fun insert(alarmInfo: AlarmInfoEntity): Long
    suspend fun update(alarmInfo: AlarmInfoEntity)
    suspend fun delete(id: Long)
    fun getItem(id: Long): Flow<AlarmInfoEntity?>
    fun getAllItemOrderByTimeAsc(): Flow<List<AlarmInfoEntity>?>
}

class AlarmRepository(private val dao: AlarmDao) : AlarmRepositoryInterface {
    override suspend fun insert(alarmInfo: AlarmInfoEntity): Long {
        return dao.insert(alarmInfo)
    }

    override suspend fun update(alarmInfo: AlarmInfoEntity) {
        dao.update(alarmInfo)
    }

    override suspend fun delete(id: Long) {
        dao.delete(id)
    }

    override fun getItem(id: Long): Flow<AlarmInfoEntity?> {
        return dao.getItem(id)
    }

    override fun getAllItemOrderByTimeAsc(): Flow<List<AlarmInfoEntity>?> {
        return dao.getAllItemOrderByTimeAsc()
    }
}