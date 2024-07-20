package me.ljpb.alarmbynotification.data

import me.ljpb.alarmbynotification.data.room.AlarmInfoInterface

data class AlarmInfo(
    override val id: Long = 0,
    override val hour: Int,
    override val min: Int,
    override val zoneId: String,
    override val name: String = "",
) : AlarmInfoInterface