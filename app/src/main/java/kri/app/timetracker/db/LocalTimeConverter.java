package kri.app.timetracker.db;

import androidx.room.TypeConverter;

import java.time.LocalTime;

/**
 * Since there is no fitting data type in sqlite, we store times as String
 */
class LocalTimeConverter {

    @TypeConverter
    public static LocalTime fromString(Long value) {
        return value == null ? null : LocalTime.ofSecondOfDay(value);
    }

    @TypeConverter
    public static Long timeToInt(LocalTime value) { 
        return value == null ? null : (long) value.toSecondOfDay();
    }
}
