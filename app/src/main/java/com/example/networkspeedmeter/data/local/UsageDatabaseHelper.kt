package com.example.networkspeedmeter.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.networkspeedmeter.data.model.DailyUsage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsageDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _todayUsage = MutableStateFlow(DailyUsage(todayDateString()))
    val todayUsage: StateFlow<DailyUsage> = _todayUsage.asStateFlow()

    init {
        // Load today's initial usage
        _todayUsage.value = getUsageForDate(todayDateString())
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_DATE TEXT PRIMARY KEY,
                $COLUMN_MOBILE_BYTES INTEGER NOT NULL DEFAULT 0,
                $COLUMN_WIFI_BYTES INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TOTAL_BYTES INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations
    }

    fun todayDateString(): String = dateFormat.format(Date())

    @Synchronized
    fun addUsage(date: String, mobileBytesDelta: Long, wifiBytesDelta: Long): DailyUsage {
        if (mobileBytesDelta <= 0 && wifiBytesDelta <= 0) {
            return _todayUsage.value
        }

        val totalDelta = mobileBytesDelta + wifiBytesDelta
        val db = writableDatabase

        // SQLite Upsert supported in modern Android (API 26+)
        val sql = """
            INSERT INTO $TABLE_NAME ($COLUMN_DATE, $COLUMN_MOBILE_BYTES, $COLUMN_WIFI_BYTES, $COLUMN_TOTAL_BYTES)
            VALUES (?, ?, ?, ?)
            ON CONFLICT($COLUMN_DATE) DO UPDATE SET
                $COLUMN_MOBILE_BYTES = $COLUMN_MOBILE_BYTES + excluded.$COLUMN_MOBILE_BYTES,
                $COLUMN_WIFI_BYTES = $COLUMN_WIFI_BYTES + excluded.$COLUMN_WIFI_BYTES,
                $COLUMN_TOTAL_BYTES = $COLUMN_TOTAL_BYTES + excluded.$COLUMN_TOTAL_BYTES
        """.trimIndent()

        db.execSQL(sql, arrayOf<Any>(date, mobileBytesDelta, wifiBytesDelta, totalDelta))

        val updated = getUsageForDate(date)
        if (date == todayDateString()) {
            _todayUsage.value = updated
        }
        return updated
    }

    @Synchronized
    fun getUsageForDate(date: String): DailyUsage {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_DATE, COLUMN_MOBILE_BYTES, COLUMN_WIFI_BYTES),
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val mobile = it.getLong(it.getColumnIndexOrThrow(COLUMN_MOBILE_BYTES))
                val wifi = it.getLong(it.getColumnIndexOrThrow(COLUMN_WIFI_BYTES))
                return DailyUsage(date, mobile, wifi)
            }
        }
        return DailyUsage(date, 0L, 0L)
    }

    @Synchronized
    fun getLast7DaysUsage(): List<DailyUsage> {
        val list = mutableListOf<DailyUsage>()
        val cal = Calendar.getInstance()

        // Get past 7 calendar days starting from 6 days ago up to today
        val dateStrings = mutableListOf<String>()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            dateStrings.add(dateFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        for (d in dateStrings) {
            list.add(getUsageForDate(d))
        }
        return list
    }

    @Synchronized
    fun getLast30DaysUsage(): List<DailyUsage> {
        val list = mutableListOf<DailyUsage>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_DATE, COLUMN_MOBILE_BYTES, COLUMN_WIFI_BYTES),
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC",
            "30"
        )

        cursor.use {
            while (it.moveToNext()) {
                val date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val mobile = it.getLong(it.getColumnIndexOrThrow(COLUMN_MOBILE_BYTES))
                val wifi = it.getLong(it.getColumnIndexOrThrow(COLUMN_WIFI_BYTES))
                list.add(DailyUsage(date, mobile, wifi))
            }
        }
        return list
    }

    @Synchronized
    fun clearAllHistory() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        _todayUsage.value = DailyUsage(todayDateString(), 0L, 0L)
    }

    companion object {
        const val DATABASE_NAME = "network_usage.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "daily_usage"
        const val COLUMN_DATE = "date"
        const val COLUMN_MOBILE_BYTES = "mobile_bytes"
        const val COLUMN_WIFI_BYTES = "wifi_bytes"
        const val COLUMN_TOTAL_BYTES = "total_bytes"

        @Volatile
        private var INSTANCE: UsageDatabaseHelper? = null

        fun getInstance(context: Context): UsageDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UsageDatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
