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

    // private GestureDetectorCompat mGestureDetector;

    private TimeRecordListAdapter mTimeRecordListAdapter;

    private final TaskRunner mTaskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mGestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
        // load the current (real-life) month
        mCurrentMonth = LocalDate.now();
        loadView();
    }

/*    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }*/

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
                            this::updateMonthlyBalance);
                    showContent();
                });
    }

    private void updateMonthlyBalance() {
        final TextView monthName = findViewById(R.id.txt_current_month);
        monthName.setText(getString(R.string.txt_current_month,
                mCurrentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), mCurrentMonth.getYear()));
        LocalDate now = LocalDate.now();
        monthName.setOnClickListener((v) -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (picker, year, month, day) -> {
                Log.d("DatePicker", "Selected " + year + "-" + month + "-" + day);
                mCurrentMonth = LocalDate.of(year, month + 1, day);
                loadView();
            }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth());
            datePicker.setTitle(R.string.txt_month_picker);
            datePicker.show();
        });
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
        // listView.setOnTouchListener((view, motionEvent) -> mGestureDetector.onTouchEvent(motionEvent));
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


    
/*    class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

      // Detects swipe to the right and left and will switch the month accordingly
     

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("Gestures", "onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if (event1 != null && event2 != null) {
                float diffY = event2.getY() - event1.getY();
                float diffX = event2.getX() - event1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // swipe right
                            Log.d("Gestures", "swipe right: " + event1.toString() + event2.toString());
                            //loadPreviousMonth();
                        } else {
                            Log.d("Gestures", "swipe left: " + event1.toString() + event2.toString());
                            //loadNextMonth();
                        }
                        return true;
                    }
                    Log.d("Gestures", "onFling: " + event1.toString() + event2.toString());
                }
            }
            return false;
        }
    }*/
}
