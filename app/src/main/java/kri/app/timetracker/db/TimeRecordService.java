package kri.app.timetracker.db;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kri.app.timetracker.model.TimeRecord;

/**
 * Singleton service for interacting with {@link TimeRecordDao_Impl}
 */
public class TimeRecordService {

    private static TimeRecordService mInstance = null;
    private final TimeRecordDatabase mDb;

    private TimeRecordService(final Context ctx) {
        mDb = Room.databaseBuilder(ctx, TimeRecordDatabase.class, "record-db").build();
    }

    public static TimeRecordService getInstance(final Context ctx) {
        if (mInstance == null) {
            mInstance = new TimeRecordService(ctx);
        }
        return mInstance;
    }

    /**
     * Upserts a record in the DB
     *
     * @param timeRecord the record to insert or update
     */
    public void save(TimeRecord timeRecord) {
        TimeRecord presentRecord = mDb.timeRecordDao().getByDate(timeRecord.getDate());
        if (presentRecord != null) {
            mDb.timeRecordDao().updateRecord(timeRecord);
        } else {
            mDb.timeRecordDao().insertRecord(timeRecord);
        }
        Log.d("DB", "Record for " + timeRecord.getDate() + " has been saved to DB");
    }

    /**
     * Gets all records for a given month. If no records are available in the DB, fills with blanks.
     * Workdays only.
     *
     * @param month the month for which to get the records
     * @return a list with records, as many as there are work days in the given month
     */
    public List<TimeRecord> getRecordsForMonth(LocalDate month) {
        // determine all workdays of that month
        final Map<LocalDate, TimeRecord> dayMap = new LinkedHashMap<>();
        LocalDate current = month.withDayOfMonth(1).minusDays(1);
        while (current.isBefore(month.withDayOfMonth(month.lengthOfMonth()))) {
            current = current.plusDays(1);
            if (current.getDayOfWeek().ordinal() < 5) {
                dayMap.put(current, new TimeRecord(current, null, null));
            }
        } 

        // query from the first to the last of the month we were given
        final List<TimeRecord> recordsInDb = mDb.timeRecordDao().getAllForPeriod(month.withDayOfMonth(1), month.withDayOfMonth(month.lengthOfMonth()));
        Log.d("DB", "Found " + recordsInDb.size() + " records for current month " + month.getMonth() + " " + month.getYear() + " in database");
        for (TimeRecord record : recordsInDb) {
            dayMap.put(record.getDate(), record);
        }
        return new ArrayList<>(dayMap.values());
    }
}
