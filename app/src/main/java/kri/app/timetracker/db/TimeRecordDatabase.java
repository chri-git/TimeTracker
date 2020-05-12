package kri.app.timetracker.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import kri.app.timetracker.model.TimeRecord;

/**
 * Local database to store time records
 */
@Database(entities = {TimeRecord.class}, version = 1)
@TypeConverters({LocalDateConverter.class, LocalTimeConverter.class})
public abstract class TimeRecordDatabase extends RoomDatabase {
    public abstract TimeRecordDao timeRecordDao();
}
