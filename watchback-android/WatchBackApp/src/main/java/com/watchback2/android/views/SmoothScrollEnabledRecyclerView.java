package com.watchback2.android.views;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

import com.perk.util.PerkLogger;

/**
 * Created by perk on 16/04/18.
 */
public class SmoothScrollEnabledRecyclerView extends RecyclerView {

    private static final String LOG_TAG = "SmoothScrollRV";

    private RecyclerView.SmoothScroller mSmoothScroller;

    public SmoothScrollEnabledRecyclerView(Context context) {
        super(context);
    }

    public SmoothScrollEnabledRecyclerView(Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothScrollEnabledRecyclerView(Context context, @Nullable AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initSmoothScroller() {
        mSmoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    /**
     * Performs a smooth scroll to the top-most child in the list
     */
    public void smoothScrollToTop() {
        smoothScrollToPosition(0);
    }

    /**
     * Performs a smooth scroll to the specified child-position in the list
     */
    public void smoothScrollToPosition(int position) {
        if (mSmoothScroller == null) {
            initSmoothScroller();
        }

        if (getAdapter() == null) {
            PerkLogger.e(LOG_TAG, "smoothScrollToPosition(): Adapter not set!");
            return;
        }

        if (position < 0 || position >= getAdapter().getItemCount()) {
            PerkLogger.e(LOG_TAG,
                    "smoothScrollToPosition(): Invalid position " + position
                            + " supplied! Item-count: " + getAdapter().getItemCount());
            return;
        }

        mSmoothScroller.setTargetPosition(position);

        getLayoutManager().startSmoothScroll(mSmoothScroller);
    }
}
