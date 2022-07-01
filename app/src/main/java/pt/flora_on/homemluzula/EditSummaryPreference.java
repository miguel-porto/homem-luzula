package pt.flora_on.homemluzula;

import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

public class EditSummaryPreference extends EditTextPreference {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditSummaryPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        return "Prefixo: " + getText();
    }
}
