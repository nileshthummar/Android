package com.watchback2.android.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.perk.util.PerkLogger;

/**
 * Created by perk on 09/08/18.
 * Custom SwipeRefreshLayout that will hide the loading icon after pulling to refresh the view
 */
public class WatchBackSwipeRefreshLayout extends SwipeRefreshLayout implements
        SwipeRefreshLayout.OnRefreshListener {

    @Nullable
    private OnRefreshListener mOnRefreshListener;

    public WatchBackSwipeRefreshLayout(
            @NonNull Context context) {
        super(context);
    }

    public WatchBackSwipeRefreshLayout(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        super.setOnRefreshListener(this);
    }

    @Override
    public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    @Override
    public void onRefresh() {
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
        postDelayed(() -> {
            if (isRefreshing()) {
                PerkLogger.d("SwipeRefresh", "Setting refreshing to false!");
                setRefreshing(false);
            }
        }, 99);
    }
}
