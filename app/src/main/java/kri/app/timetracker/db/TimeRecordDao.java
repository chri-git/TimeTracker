package kri.app.timetracker.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import kri.app.timetracker.model.TimeRecord;

/**
 * DAO for interacting with the DB in  {@link TimeRecordDatabase}
 */
@Dao
public interface TimeRecordDao {
    
    @Query("SELECT * from record where date = :date")
    TimeRecord getByDate(LocalDate date);
    
    @Insert
    void insertRecord(TimeRecord record);
    
    @Update
    void updateRecord(TimeRecord record);
    
    @Delete
    void deleteRecord(TimeRecord record);
    
    @Query("SELECT * from record where date >= :start and date <= :end order by date asc")
    List<TimeRecord> getAllForPeriod(LocalDate start, LocalDate end);
}
