package com.financeapp.data.dao

import androidx.room.*
import com.financeapp.data.entities.DailySnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: DailySnapshot)

    @Query("SELECT * FROM daily_snapshots WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSnapshotsInRange(startDate: Long, endDate: Long): Flow<List<DailySnapshot>>

    @Query("SELECT * FROM daily_snapshots ORDER BY date DESC LIMIT :days")
    fun getRecentSnapshots(days: Int): Flow<List<DailySnapshot>>

    @Query("SELECT * FROM daily_snapshots WHERE date = :date LIMIT 1")
    suspend fun getSnapshotForDate(date: Long): DailySnapshot?
}
