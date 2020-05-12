package kri.app.timetracker.db;

import androidx.room.TypeConverter;

import java.time.LocalDate;

/**
 * Since there is no fitting data type in sqlite, we store dates as String
 */
class LocalDateConverter {

    @TypeConverter
    public static LocalDate fromString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String dateToTimestamp(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
