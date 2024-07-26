package me.ljpb.alarmbynotification.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

interface AlarmInfoInterface {
    val id: Long
    val hour: Int
    val min: Int
    val zoneId: String
    val name: String
}

@Entity("alarms")
data class AlarmInfoEntity(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    override val hour: Int,
    override val min: Int,
    override val zoneId: String,
    override val name: String = "",
) : AlarmInfoInterface