package com.watchback2.android.views;

import android.content.Context;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;

import java.lang.ref.WeakReference;

/**
 * Created by perk on 03/04/18.
 */

public class AccountRadioGroup extends RadioGroup implements RadioGroup.OnCheckedChangeListener {

    private static final String LOG_TAG = "AccountRadioGroup";

    private WeakReference<AccountRadioButtonSelectedListener> mListenerReference;

    private AppCompatRadioButton mUsageRadioButton;

    private AppCompatRadioButton mHistoryRadioButton;

    public interface AccountRadioButtonSelectedListener {
        void onUsageSelected();

        void onHistorySelected();
    }

    public AccountRadioGroup(Context context) {
        super(context);
    }

    public AccountRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mUsageRadioButton = findViewById(R.id.id_usage);
        mHistoryRadioButton = findViewById(R.id.id_history);

        mHistoryRadioButton.setChecked(true);

        setOnCheckedChangeListener(this);
    }

    public void selectHistoryButton() {
        mHistoryRadioButton.setChecked(true);
        notifySelection();
    }

    public void setAccountRadioButtonSelectedListener(AccountRadioButtonSelectedListener listener) {
        if (mListenerReference != null) {
            mListenerReference.clear();
            mListenerReference = null;
        }
        mListenerReference = new WeakReference<>(listener);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        PerkLogger.d(LOG_TAG,
                "onCheckedChanged(): checkedId : " + checkedId + " R.id.id_usage: " + R.id.id_usage
                        + " R.id.id_history: " + R.id.id_history);
        if (checkedId == R.id.id_usage) {
            mHistoryRadioButton.setChecked(false);
        } else {
            mUsageRadioButton.setChecked(false);
        }
        notifySelection();
    }

    private void notifySelection() {
        if (mListenerReference == null) {
            PerkLogger.d(LOG_TAG, "Listener not set!");
            return;
        }

        AccountRadioButtonSelectedListener listener = mListenerReference.get();
        if (listener == null) {
            PerkLogger.d(LOG_TAG, "Cannot proceed as listener is null");
            return;
        }

        if (getCheckedRadioButtonId() == R.id.id_usage) {
            listener.onUsageSelected();
        } else {
            listener.onHistorySelected();
        }
    }
}
