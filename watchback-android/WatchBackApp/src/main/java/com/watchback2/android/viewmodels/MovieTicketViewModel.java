package com.watchback2.android.viewmodels;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.models.movietickets.VideoHistoryModel;
import com.watchback2.android.models.movietickets.VideoHistoryResponseModel;
import com.watchback2.android.models.movietickets.VideoHistoryWapper;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.WrapResponseModels;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MovieTicketViewModel {

    private static final String LOG_TAG = "MovieTicketVM";
    private ObservableField<String> monthName = new ObservableField<>("");
    private ObservableField<String> descriptionText = new ObservableField<>("");
    private ObservableField<String> watchedEpisodes = new ObservableField<>("Watched 0 Episodes");
    private ObservableField<String> remainingEpisodes = new ObservableField<>("5 Episodes remaining");
    private ObservableField<List<VideoHistoryModel>> videoList = new ObservableField<>();
    private ObservableInt imageResourceID = new ObservableInt(R.drawable.ic_progress_0);
    private ObservableInt recyclerViewVisbility = new ObservableInt(View.GONE);
    private ObservableInt startWatchingVisbility = new ObservableInt(View.VISIBLE);
    private ObservableInt progressLayoutVisbility = new ObservableInt(View.GONE);
    private ObservableInt noTicketsPaddingVisbility = new ObservableInt(View.VISIBLE);
    private ObservableInt progressbarVisibility = new ObservableInt(View.VISIBLE);

    private IMovieTicketClickListener clickListener;

    public interface IMovieTicketClickListener {
        void onStartWatchingButtonClick();

        void onVideoItemClick(long videoID);
    }

    public void setClickListener(IMovieTicketClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void makeVideoHistoryCall(Context context) {
        progressbarVisibility.set(View.VISIBLE);
        WatchbackAPIController.INSTANCE.getVideoHistory(context, new OnRequestFinishedListener<VideoHistoryResponseModel>() {
            @Override
            public void onSuccess(@NonNull VideoHistoryResponseModel videoHistoryResponseModel, @Nullable String s) {
                progressbarVisibility.set(View.GONE);
                processData(WrapResponseModels.wrapVideoHistoryResponse(videoHistoryResponseModel));
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<VideoHistoryResponseModel> perkResponse) {
                progressbarVisibility.set(View.GONE);
                PerkLogger.e(LOG_TAG,
                        "Loading rewards failed:" + errorType + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

            }
        });

    }

    private void processData(VideoHistoryWapper videoHistory) {
        PerkLogger.d(LOG_TAG, "videos remaining: " + videoHistory.getmVideosRemaining());
        int videosRemaining = videoHistory.getmVideosRemaining();
        int videosWatched = 5 - videosRemaining;

        if (videoHistory.ismRewarded()) {
            // User rewarded
            descriptionText.set("Congrats, you won a FREE Fandango Promo code. Your Promo code will be delivered within the first 7 days of the next month. Check your email for more details.");
            progressLayoutVisbility.set(View.VISIBLE);
            noTicketsPaddingVisbility.set(View.GONE);

        } else if (!videoHistory.ismTotalRewardsRemaining()) {
            // no more tickets left
            descriptionText.set("NO MORE PROMO CODES. PLEASE CHECK BACK NEXT MONTH.");
            progressLayoutVisbility.set(View.GONE);
            noTicketsPaddingVisbility.set(View.VISIBLE);

        } else {
            descriptionText.set("Watch 5 shows this month and get a FREE Fandango Promo Code*\n\n *While Supplies Last. Up to $12 value. Ends 12/31/19");

            progressLayoutVisbility.set(View.VISIBLE);
            noTicketsPaddingVisbility.set(View.GONE);
        }

        switch (videosWatched) {

            case 1:
                imageResourceID.set(R.drawable.ic_progress_1);
                break;
            case 2:
                imageResourceID.set(R.drawable.ic_progress_2);
                break;
            case 3:
                imageResourceID.set(R.drawable.ic_progress_3);
                break;
            case 4:
                imageResourceID.set(R.drawable.ic_progress_4);
                break;
            case 5:
                imageResourceID.set(R.drawable.ic_progress_5);
                break;
            case 0:
            default:
                imageResourceID.set(R.drawable.ic_progress_0);
                break;

        }

        String watched;
        String remaining;

        if (videosWatched == 1) {
            watched = "Watched " + videosWatched + " Episode";
        } else {
            watched = "Watched " + videosWatched + " Episodes";

        }

        if (videosRemaining == 1) {
            remaining = videosRemaining + " Episode remaining";
        } else {
            remaining = videosRemaining + " Episodes remaining";
        }

        watchedEpisodes.set(watched);
        remainingEpisodes.set(remaining);
        videoList.set(videoHistory.getmVideos());

        if (videoList.get() != null && videoList.get().size() > 0) {
            for (VideoHistoryModel wrapper : videoList.get()) {
                wrapper.setItemClickListener(new VideoHistoryModel.OnVideoHistoryItemClicked() {
                    @Override
                    public void onItemClick(long videoID) {
                        PerkLogger.d(LOG_TAG, "onItemClick");
                        if (clickListener != null) {
                            clickListener.onVideoItemClick(videoID);
                        }
                    }
                });
            }
            recyclerViewVisbility.set(View.VISIBLE);
            startWatchingVisbility.set(View.GONE);
        }
    }

    public void onStartWatchingClick(View view) {
        PerkLogger.d(LOG_TAG, "onStartWatchingClick");
        if (clickListener != null) {
            clickListener.onStartWatchingButtonClick();
        }
    }

    public ObservableField<String> getCurrentMonthAndYr() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        String month = new SimpleDateFormat("MMMM", Locale.US).format(c.getTime());
        monthName.set(month + " " + year);
        return monthName;
    }

    public ObservableField<String> getWatchedEpisodes() {
        return watchedEpisodes;
    }

    public ObservableField<String> getRemainingEpisodes() {
        return remainingEpisodes;
    }

    public ObservableField<List<VideoHistoryModel>> getVideoList() {
        return videoList;
    }

    public ObservableInt getImageResourceID() {
        return imageResourceID;
    }

    public ObservableInt getRecyclerViewVisbility() {

        if (videoList.get() != null && videoList.get().size() > 0) {
            recyclerViewVisbility.set(View.VISIBLE);
        } else {
            recyclerViewVisbility.set(View.GONE);

        }
        return recyclerViewVisbility;
    }

    public ObservableInt getStartWatchingVisbility() {

        if (videoList.get() != null && videoList.get().size() > 0) {
            startWatchingVisbility.set(View.GONE);
        } else {
            startWatchingVisbility.set(View.VISIBLE);
        }
        return startWatchingVisbility;
    }

    public ObservableInt getProgressLayoutVisbility() {
        return progressLayoutVisbility;
    }

    public ObservableField<String> getDescriptionText() {
        return descriptionText;
    }

    public ObservableInt getNoTicketsPaddingVisbility() {
        return noTicketsPaddingVisbility;
    }

    public ObservableInt getProgressbarVisibility() {
        return progressbarVisibility;
    }
}
