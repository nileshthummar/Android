package com.watchback2.android.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.annotation.PerkAccessToken;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.ChannelUpdateRequestBody;
import com.watchback2.android.models.NullableDataModel;
import com.watchback2.android.utils.PerkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PostFavChannels {

    private static final String LOG_TAG = "PostFavChannels";

    private WeakReference<Context> contextWeakReference;

    private PostChannelCallback postChannelCallback;

    public interface PostChannelCallback {
        void onCompleted();
    }

    public PostFavChannels(Context context) {
        contextWeakReference = new WeakReference<>(context);

    }

    public void setPostChannelCallback(PostChannelCallback postChannelCallback) {
        this.postChannelCallback = postChannelCallback;
    }

    public void makeCall(List<Channel> channelList) {
        Context context = contextWeakReference.get();
        if (context == null) {
            if (postChannelCallback != null) {
                postChannelCallback.onCompleted();
            }
            return;
        }
        List<String> channelUuidList = new ArrayList<>(0);

        if (channelList != null) {
            for (Channel channel : channelList) {
                if (channel != null) {
                    channelUuidList.add(channel.getUuid());
                }
            }
        }

        ChannelUpdateRequestBody channelUpdateRequestBody = new ChannelUpdateRequestBody();
        channelUpdateRequestBody.setmAccessToken(PerkAccessToken.ACCESS_TOKEN);
        channelUpdateRequestBody.setmChannels(channelUuidList);

        PerkLogger.d(LOG_TAG, "Updating favorite channels: " + channelUuidList.toString());

        WatchbackAPIController.INSTANCE.postUserFavorites(context, channelUpdateRequestBody, new OnRequestFinishedListener<NullableDataModel>() {
            @Override
            public void onSuccess(@NonNull NullableDataModel nullableDataModel, @Nullable String s) {
                PerkLogger.d(LOG_TAG, "Updating favorite channels successful!");
                if (postChannelCallback != null) {
                    postChannelCallback.onCompleted();
                }
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<NullableDataModel> perkResponse) {
                PerkLogger.d(LOG_TAG, "Updating favorite channels failed! " +  (perkResponse != null ? perkResponse.getMessage()
                        : ""));
                if (postChannelCallback != null) {
                    postChannelCallback.onCompleted();
                }
                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
            }
        });

    }
}
