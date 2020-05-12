package kri.app.timetracker.util;

import android.content.Context;
import android.widget.TextView;

import kri.app.timetracker.R;

/**
 * Utility to format and display balance texts
 */
public class BalanceTextUtil {

    public static void setBalance(final TextView textView, final int balanceInMinutes, final Context context) {
        int hours = Math.abs(balanceInMinutes) / 60;
        int minutes = Math.abs(balanceInMinutes) % 60;
        if (balanceInMinutes < 0) {
            textView.setText(context.getString(R.string.txt_balance_negative, hours, minutes));
            textView.setTextColor(textView.getContext().getColor(R.color.balance_negative));
        } else if (balanceInMinutes > 0) {
            textView.setText(context.getString(R.string.txt_balance_positive, hours, minutes));
            textView.setTextColor(textView.getContext().getColor(R.color.balance_positive));
        } else {
            // balance is exactly 0
            textView.setText(context.getString(R.string.txt_balance_neutral, hours, minutes));
            textView.setTextColor(textView.getContext().getColor(R.color.fadedTextColor));
        }
    }
}
