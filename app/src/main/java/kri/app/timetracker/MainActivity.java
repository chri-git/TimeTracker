package kri.app.timetracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import kri.app.timetracker.db.TimeRecordService;
import kri.app.timetracker.util.BalanceTextUtil;
import kri.app.timetracker.util.TaskRunner;
import kri.app.timetracker.view.TimeRecordListAdapter;

/**
 * The main (and only) screen of the application, shows the selected month, the monthly overall
 * balance and a list of work days with times and balance for each of them.
 */
public class MainActivity extends Activity {

    /**
     * The month which is currently shown on the screen
     */
    private LocalDate mCurrentMonth;

    private TimeRecordListAdapter mTimeRecordListAdapter;

    private final TaskRunner mTaskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load the current (real-life) month
        mCurrentMonth = LocalDate.now();
        loadView();
    }

    // todo lifecycle considerations. if we get resumed, may want to reload the last selected month

    /**
     * Asynchronously loads the data for the currently selected month and updates the view when
     * done. While loading, displays a spinner.
     */
    private void loadView() {
        showSpinner();
        mTaskRunner.executeAsync(() ->
                        TimeRecordService.getInstance(getApplicationContext()).getRecordsForMonth(mCurrentMonth)
                , (result) -> {
                    mTimeRecordListAdapter = new TimeRecordListAdapter(this, result,
                            this::loadView);
                    showContent();
                });
    }

    private void updateMonthlyBalance() {
        final TextView monthName = findViewById(R.id.txt_current_month);
        monthName.setText(getString(R.string.txt_current_month,
                mCurrentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), mCurrentMonth.getYear()));
        LocalDate now = LocalDate.now();
        // by clicking the month name, we can choose a specific month to skip to, 
        // rather than only going one month back or forward
        monthName.setOnClickListener((v) -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (picker, year, month, day) -> {
                Log.d("DatePicker", "Selected " + year + "-" + month + "-" + day);
                mCurrentMonth = LocalDate.of(year, month + 1, day);
                loadView();
            }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth());
            datePicker.setTitle(R.string.txt_month_picker);
            datePicker.show();
        });
        // display the balance for the selected month
        int balanceMinutes = mTimeRecordListAdapter.getMonthlyBalanceInMinutes();
        final TextView currentMonthBalance = findViewById(R.id.txt_current_balance);
        BalanceTextUtil.setBalance(currentMonthBalance, balanceMinutes, this);
    }

    private void showSpinner() {
        setContentView(R.layout.spinner);
    }

    private void showContent() {
        setContentView(R.layout.activity_main);
        final ListView listView = findViewById(R.id.list_times);
        listView.setAdapter(mTimeRecordListAdapter);
        updateMonthlyBalance();

        final Button nextMonth = findViewById(R.id.btn_next_month);
        final Button prevMonth = findViewById(R.id.btn_last_month);

        nextMonth.setOnClickListener((v) -> loadNextMonth());

        prevMonth.setOnClickListener((v) -> loadPreviousMonth());
    }

    private void loadNextMonth() {
        mCurrentMonth = mCurrentMonth.plusMonths(1);
        loadView();
    }

    private void loadPreviousMonth() {
        mCurrentMonth = mCurrentMonth.minusMonths(1);
        loadView();
    }
}
