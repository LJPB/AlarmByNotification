package me.ljpb.alarmbynotification

import me.ljpb.alarmbynotification.Utility.getHowManyLater
import me.ljpb.alarmbynotification.Utility.getMilliSecondsOfNextTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class UtilityTest {
    private val testCurrentYear = 2024
    private val testCurrentMonth = 2
    private val testCurrentDayOfMonth = 29
    private val testCurrentHour = 10
    private val testCurrentMinute = 10
    private val testCurrentSecond = 10
    private val testCurrentNanoOfSecond = 10
    private val testCurrentZoneId = "UTC"

    private val testCurrentTime = ZonedDateTime.of(
        testCurrentYear,
        testCurrentMonth,
        testCurrentDayOfMonth,
        testCurrentHour,
        testCurrentMinute,
        testCurrentSecond,
        testCurrentNanoOfSecond,
        ZoneId.of(testCurrentZoneId)
    )

    /**
     * 現在時刻とトリガー時刻が同じ場合のテスト
     * 翌日の同じ時間で，秒以下は0となることを期待
     */
    @Test
    fun getMilliSecondsOfNextTime_SameTime() {
        val nextTime =
            getMilliSecondsOfNextTime(testCurrentHour, testCurrentMinute, testCurrentTime)
        // currentTimeの次の日
        val expectMilliSeconds = ZonedDateTime.of(
            testCurrentYear,
            testCurrentMonth,
            testCurrentDayOfMonth,
            testCurrentHour,
            testCurrentMinute,
            0,
            0,
            ZoneId.of(testCurrentZoneId)
        ).plusDays(1)
            .toEpochSecond() * 1000
        
        assertEquals(expectMilliSeconds, nextTime)
    }
    
    /**
     * トリガー時刻が(00:00~23:59の範囲内で)現在時刻以前に設定されている場合
     * 日付は翌日で時刻はトリガー時刻，秒以下は0となることを期待
     */
    @Test
    fun getMilliSecondsOfNextTime_BeforeTime() {
        val localTime = LocalTime.of(testCurrentHour, testCurrentMinute).minusMinutes(1)
        val triggerHour = localTime.hour
        val triggerMinutes = localTime.minute
        val nextTime =
            getMilliSecondsOfNextTime(triggerHour, triggerMinutes, testCurrentTime)
        
        val expectMilliSeconds = ZonedDateTime.of(
            testCurrentYear,
            testCurrentMonth,
            testCurrentDayOfMonth,
            triggerHour,
            triggerMinutes,
            0,
            0,
            ZoneId.of(testCurrentZoneId)
        ).plusDays(1)
            .toEpochSecond() * 1000
        
        assertEquals(expectMilliSeconds, nextTime)
    }
    
    /**
     * トリガー時刻が(00:00~23:59の範囲内で)現在時刻以降に設定されている場合
     * 日付は同じで時刻はトリガー時刻，秒以下は0となることを期待
     */
    @Test
    fun getMilliSecondsOfNextTime_AfterTime() {
        val localTime = LocalTime.of(testCurrentHour, testCurrentMinute).plusMinutes(1)
        val triggerHour = localTime.hour
        val triggerMinutes = localTime.minute
        
        val nextTime =
            getMilliSecondsOfNextTime(triggerHour, triggerMinutes, testCurrentTime)
        
        val expectMilliSeconds = ZonedDateTime.of(
            testCurrentYear,
            testCurrentMonth,
            testCurrentDayOfMonth,
            triggerHour,
            triggerMinutes,
            0,
            0,
            ZoneId.of(testCurrentZoneId)
        ).toEpochSecond() * 1000
        
        assertEquals(expectMilliSeconds, nextTime)
    }


    /**
     * 現在の時刻とトリガー時刻が同じ場合
     * 24時間後を期待
     */
    @Test
    fun getHowManyLater_SameTime() {
        val later = getHowManyLater(testCurrentHour, testCurrentMinute, testCurrentTime)
        val expect = Pair(24L, 0L)
        assertEquals(expect, later)
    }
    
    /**
     * トリガー時刻が(00:00~23:59の範囲内で)現在の時刻よりも前の場合
     * 翌日の時刻までの時間を期待((24時間-差分)時間)
     */
    @Test
    fun getHowManyLater_BeforeTime() {
        val localTime = LocalTime.of(testCurrentHour, testCurrentMinute).minusMinutes(1)
        val triggerHour = localTime.hour
        val triggerMinutes = localTime.minute
        val later = getHowManyLater(triggerHour, triggerMinutes, testCurrentTime)
        val expect = Pair(23L, 59L)
        assertEquals(expect, later)
    }
    
    /**
     * トリガー時刻が(00:00~23:59の範囲内で)現在の時刻以降の場合
     * 当日の時間を期待
     */
    @Test
    fun getHowManyLater_AfterTime() {
        val plusHour = 0L
        val plusMinutes = 1L
        val localTime = LocalTime.of(testCurrentHour, testCurrentMinute).plusHours(plusHour).plusMinutes(plusMinutes)
        val triggerHour = localTime.hour
        val triggerMinutes = localTime.minute
        val later = getHowManyLater(triggerHour, triggerMinutes, testCurrentTime)
        val expect = Pair(plusHour, plusMinutes)
        assertEquals(expect, later)
    }
}