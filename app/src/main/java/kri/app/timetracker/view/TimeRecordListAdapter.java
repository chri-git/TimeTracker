package kri.app.timetracker.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import kri.app.timetracker.R;
import kri.app.timetracker.db.TimeRecordService;
import kri.app.timetracker.model.TimeRecord;
import kri.app.timetracker.util.BalanceTextUtil;
import kri.app.timetracker.util.TaskRunner;

/**
 * A list adapter that handles a single entry in the list of time records displayed on the main screen
 */
public class TimeRecordListAdapter extends ArrayAdapter<TimeRecord> {

    private final DateTimeFormatter mTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final TaskRunner mTaskRunner = new TaskRunner();

    private final List<TimeRecord> mRecords;
    private final Runnable mUpdateCallback;

    public TimeRecordListAdapter(@NonNull Activity context, @NonNull List<TimeRecord> records,
                                 @NonNull Runnable updateCallback) {
        super(context, R.layout.list_item_record, records);
        this.mRecords = records;
        this.mUpdateCallback = updateCallback;

    }

    public int getMonthlyBalanceInMinutes() {
        int balance = 0;
        for (TimeRecord record : mRecords) {
            balance = balance + getRecordBalance(record);
        }
        return balance;
    }

    private void deleteRecord(TimeRecord record) {
        mTaskRunner.executeAsyncVoid(() -> recordService().deleteRecord(record), this::notifyDataSetChanged);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // trigger callback to parent to make sure that the balance gets updated there
        mUpdateCallback.run();
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        final TimeRecord rec = mRecords.get(position);

        ViewHolder viewHolder;

        if (view == null) {
            // no view to reuse, inflate a new one for the current row
            viewHolder = new ViewHolder();
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.list_item_record, parent, false);
            viewHolder.balance = view.findViewById(R.id.txt_balance);
            viewHolder.dayOfMonth = view.findViewById(R.id.txt_day_of_month);
            viewHolder.dayOfWeek = view.findViewById(R.id.txt_day_of_week);
            viewHolder.endTime = view.findViewById(R.id.txt_end);
            viewHolder.startTime = view.findViewById(R.id.txt_start);
            viewHolder.listItem = view.findViewById(R.id.list_item);
            // cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }

        // long click listener to delete entries
        viewHolder.listItem.setOnLongClickListener(v -> {
            if (rec.getStart() != null || rec.getEnd() != null) {
                // only show the popup if there's actually something to delete
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getContext()).setCancelable(true).setMessage(getContext().getString(R.string.pp_delete_text,
                                rec.getDate()))
                                .setPositiveButton(R.string.pp_delete_confirm,
                                        (dialog, which) -> deleteRecord(rec))
                                .setNegativeButton(R.string.pp_delete_cancel, 
                                        (dialog, which) -> {});
                builder.create().show();
            }
            return true;
        });

        // show the day of the month
        viewHolder.dayOfMonth.setText(getContext().getString(R.string.txt_day_of_month_number,
                rec.getDate().getDayOfMonth()));
        // show the weekday
        viewHolder.dayOfWeek.setText(rec.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()).substring(0, 2));
        // show start
        // prepare a time picker, starting at either the previously set time or the current real time
        final LocalTime timePickerDefaultStart = rec.getStart() == null ? LocalTime.now() : rec.getStart();
        viewHolder.startTime.setOnClickListener((v) -> {
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                LocalTime newStartTime = LocalTime.of(selectedHour, selectedMinute);
                if (rec.getEnd() != null && rec.getEnd().isBefore(newStartTime)) {
                    // don't accept the change
                    Toast.makeText(getContext(), R.string.txt_time_mismatch_error, Toast.LENGTH_LONG).show();
                }
                rec.setStart(newStartTime);
                updateRecord(rec);
            }, timePickerDefaultStart.getHour(), timePickerDefaultStart.getMinute(), true);
            mTimePicker.setTitle(R.string.txt_arrive);
            mTimePicker.show();
        });
        viewHolder.startTime.setLongClickable(true);
        viewHolder.startTime.setOnLongClickListener(v -> viewHolder.listItem.performLongClick());

        if (rec.getStart() != null) {
            viewHolder.startTime.setText(rec.getStart().format(mTimeFormatter));
            viewHolder.startTime.setTextColor(getContext().getColor(R.color.defaultTextColor));
        } else {
            viewHolder.startTime.setText(R.string.txt_arrive);
            viewHolder.startTime.setTextColor(getContext().getColor(R.color.fadedTextColor));
        }
        // show end
        // prepare a time picker, starting at either the previously set time or the current real time
        // todo this is a bit buggy, the default time will only update when the list changes
        final LocalTime timePickerDefaultEnd = rec.getEnd() == null ? LocalTime.now() : rec.getEnd();
        viewHolder.endTime.setOnClickListener((v) -> {
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                LocalTime newEndTime = LocalTime.of(selectedHour, selectedMinute);
                if (rec.getStart() != null && newEndTime.isBefore(rec.getStart())) {
                    // don't accept the change
                    Toast.makeText(getContext(), R.string.txt_time_mismatch_error, Toast.LENGTH_LONG).show();
                } else {
                    rec.setEnd(newEndTime);
                    updateRecord(rec);
                }
            }, timePickerDefaultEnd.getHour(), timePickerDefaultEnd.getMinute(), true);
            mTimePicker.setTitle(R.string.txt_leave);
            mTimePicker.show();
        });
        viewHolder.endTime.setLongClickable(true);
        viewHolder.endTime.setOnLongClickListener(v -> viewHolder.listItem.performLongClick());

        if (rec.getEnd() != null) {
            viewHolder.endTime.setText(rec.getEnd().format(mTimeFormatter));
            viewHolder.endTime.setTextColor(getContext().getColor(R.color.defaultTextColor));
        } else {
            viewHolder.endTime.setText(R.string.txt_leave);
            viewHolder.endTime.setTextColor(getContext().getColor(R.color.fadedTextColor));
        }
        // compute balance
        int balanceMinutes = getRecordBalance(rec);
        BalanceTextUtil.setBalance(viewHolder.balance, balanceMinutes, getContext());

        return view;
    }

    private int getRecordBalance(TimeRecord record) {
        // fast path, if the record is incomplete, count as 0
        if (record.getStart() == null || record.getEnd() == null) {
            return 0;
        }
        int nominalTime = 8 * 60 + 15;
        if (record.getDate().getDayOfWeek().ordinal() == 4) {
            // Friday
            nominalTime = 5 * 60 + 30;
        }
        int workTime =
                (record.getEnd().getHour() * 60 + record.getEnd().getMinute()) - (record.getStart().getHour() * 60 + record.getStart().getMinute());

        if (record.getEnd().isAfter(LocalTime.of(12, 30))) {
            // deduct 30 min lunch break
            workTime = workTime - 30;
        }
        return workTime - nominalTime;
    }

    private void updateRecord(TimeRecord record) {
        mTaskRunner.executeAsyncVoid(() -> recordService().save(record), this::notifyDataSetChanged);
    }

    private TimeRecordService recordService() {
        return TimeRecordService.getInstance(getContext());
    }

    private static class ViewHolder {
        LinearLayout listItem;
        TextView dayOfMonth;
        TextView dayOfWeek;
        TextView startTime;
        TextView endTime;
        TextView balance;
    }
}
