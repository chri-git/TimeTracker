package kri.app.timetracker.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A TimeRecord represents the arrival and leave time of a particular work day
 */
@Entity(tableName = "record")
public class TimeRecord {

    @PrimaryKey
    @NonNull
    private final LocalDate date;
    @ColumnInfo(name = "start")
    private LocalTime start;
    @ColumnInfo(name = "end")
    private LocalTime end;

    public TimeRecord(@NonNull LocalDate date, LocalTime start, LocalTime end) {
        this.date = date;
        this.start = start;
        this.end = end;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}
