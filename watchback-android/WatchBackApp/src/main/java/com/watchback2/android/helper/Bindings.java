package com.watchback2.android.helper;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.perk.util.PerkLogger;
import com.perk.util.PixelConverter;
import com.watchback2.android.R;
import com.watchback2.android.activities.WatchbackWebViewActivity;
import com.watchback2.android.adapters.ChannelsListAdapter;
import com.watchback2.android.adapters.HomeFragmentParentListAdapter;
import com.watchback2.android.adapters.InterestsListAdapter;
import com.watchback2.android.adapters.PlayerVideoAdapter;
import com.watchback2.android.adapters.VideoHistoryAdapter;
import com.watchback2.android.adapters.VideosRecyclerViewAdapter;
import com.watchback2.android.adapters.channelsfragment.ChannelsGenreListItemAdapter;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.Interest;
import com.watchback2.android.models.movietickets.VideoHistoryModel;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.views.SmoothScrollEnabledRecyclerView;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Contains {@link BindingAdapter}s used in the app
 */
public final class Bindings {

    private static final String TAG = "Bindings";

    private Bindings() {
        // No instances
    }

    @BindingAdapter("homeItemsList")
    public static void setHomeItems(RecyclerView recyclerView, List<BrightcovePlaylistData.BrightcoveVideo> items) {
        int position = 0;

        if (recyclerView.getLayoutManager() != null) {
            position = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        HomeFragmentParentListAdapter adapter = (HomeFragmentParentListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }

        // Scroll to the top of the RecyclerView:
        int finalPosition = position;
        recyclerView.postDelayed(() -> {
            if (recyclerView != null) {
                recyclerView.scrollToPosition(finalPosition);
            }
        }, 300);
    }

    @BindingAdapter("videoItemsList")
    public static void setVideoItems(RecyclerView recyclerView, List<BrightcovePlaylistData.BrightcoveVideo> items) {
        if (recyclerView == null || !(recyclerView.getAdapter() instanceof VideosRecyclerViewAdapter)) {
            return;
        }
        VideosRecyclerViewAdapter adapter = (VideosRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    /**
     * Parses provided duration value in hh:mm:ss, as appropriate
     * Assumes that provided duration is in milliseconds
     */
    @BindingAdapter("videoDuration")
    public static void setVideoDuration(TextView textView, long durationMillis) {
        if (durationMillis > 0) {
            try {
                textView.setText(DateUtils.formatElapsedTime(durationMillis / 1000));
                textView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                PerkLogger.e(TAG, "setVideoDuration: Encountered exception, hiding view: ", e);
                textView.setVisibility(View.GONE);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("videoDuration")
    public static void setVideoDuration(TextView textView, String durationMillis) {
        try {
            setVideoDuration(textView, Integer.parseInt(durationMillis));
        } catch (Exception e) {
            PerkLogger.e(TAG, "setVideoDuration: Encountered exception, hiding view: ", e);
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        if (imageView == null) {
            return;
        }

        if (url == null || TextUtils.isEmpty(url)) {
            // clear the ImageView in this case
            imageView.setImageDrawable(null);
            return;
        }

        try {
            Glide.with(imageView).load(url).into(imageView);
        } catch (IllegalArgumentException e) {
            PerkLogger.e("Glide", "You cannot start a load for a destroyed activity");
        }
    }

    @BindingAdapter({"roundedCornerImageUrl", "cornerRadius"})
    public static void setRoundedCornerImageUrl(ImageView imageView, String url, float cornerRadius) {
        if (imageView == null) {
            return;
        }

        if (url == null || TextUtils.isEmpty(url)) {
            // clear the ImageView in this case
            imageView.setImageDrawable(null);
            return;
        }

        if (cornerRadius == 0f) {
            setImageUrl(imageView, url);
        } else {
            Glide.with(imageView).load(url).apply(RequestOptions.bitmapTransform(
                    new RoundedCornersTransformation(Math.round(cornerRadius), 0))).into(imageView);
        }
    }

    @BindingAdapter("channelRoundImageUrl")
    public static void setChannelRoundImageUrl(ImageView imageView, String url) {
        if (imageView == null) {
            return;
        }

        if (TextUtils.equals("add_channel_img_source", url)) {
            // set the 'add-channel' round image in this case
            imageView.setImageResource(R.drawable.ic_add);
        } else {
            setImageUrl(imageView, url);
        }
    }

    @BindingAdapter({"goldBorderEnabled", "isTypeUrl"})
    public static void toggleGoldBorder(ImageView imageView, boolean enabled, boolean isUrl) {
        if (imageView == null) {
            return;
        }

        Drawable normalGoldFrame = imageView.getContext().getResources().getDrawable(
                R.drawable.gold_frame_updated);

        Drawable silverFrame = DrawableCompat.wrap(
                imageView.getContext().getResources().getDrawable(R.drawable.silver_frame));

        // Enable if tinting is needed:
        /*Drawable tintedFrame = DrawableCompat.wrap(
                imageView.getContext().getResources().getDrawable(R.drawable.gold_frame_for_tint));

        if (!enabled && !isUrl) {
            if (PerkPreferencesManager.INSTANCE.isNightMode()) {
                // clear the tint:
                DrawableCompat.setTintList(tintedFrame, null);
            } else {
                TypedArray a = imageView.getContext().getTheme().obtainStyledAttributes(
                        new int[]{R.attr.viewBackgroundInverse});

                int color = a.getColor(0, 0);
                if (color != 0) {
                    DrawableCompat.setTint(tintedFrame, color);
                }

                a.recycle();
            }
        }*/

        imageView.setBackground(isUrl ? null : (enabled ? normalGoldFrame : silverFrame));

        int padding = isUrl ? 0 : Math.round(
                PixelConverter.convertDpToPixels(imageView.getContext(), 3L));
        imageView.setPadding(padding, padding, padding, padding);
    }

    @BindingAdapter("channelCarrotImage")
    public static void setChannelImage(ImageView imageView, boolean hide) {
        if (imageView == null) {
            return;
        }

        if (hide) {
            // clear the ImageView in this case
            imageView.setImageDrawable(null);
            return;
        }

        imageView.setImageResource(R.drawable.ic_carrot_settings);
    }

    @BindingAdapter("channelExploreText")
    public static void setChannelExploreText(TextView view, boolean hide) {
        if (view == null) {
            return;
        }

        if (hide) {
            view.setText(R.string.empty_string);
            return;
        }

        view.setText(R.string.explore);
    }

    @BindingAdapter("videoContainerBgTint")
    public static void setVideoContainerBgTint(ImageView imageView, String tintColor) {
        if (imageView == null) {
            return;
        }

        if (TextUtils.isEmpty(tintColor)) {
            // Use default - bluish background
            tintColor = "#0089cc";
        } else if (!tintColor.startsWith("#")) {
            tintColor = "#" + tintColor;
        }

        final Drawable wrapDrawable = DrawableCompat.wrap(
                imageView.getContext().getResources().getDrawable(R.drawable.rounded_bg_donate));

        DrawableCompat.setTint(wrapDrawable, Color.parseColor(tintColor));

        imageView.setBackground(wrapDrawable);
    }

    @BindingAdapter("htmlText")
    public static void setText(TextView textView, String source) {
        setHtmlText(textView, source, false);
    }

    @BindingAdapter("htmlTextUnderlined")
    public static void setUnderlinedHtmlText(TextView textView, String source) {
        setHtmlText(textView, source, true);
    }

    public static void setHtmlText(final TextView textView, final String source, final boolean underlined) {
        if (source == null || TextUtils.isEmpty(source)) {
            return;
        }

        textView.setText(Html.fromHtml(source));

        // Enable links to work on TextView, if any:
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // Get color to use as per theme:
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = textView.getContext().getTheme();
        theme.resolveAttribute(android.R.attr.textColor, typedValue, true);
        @ColorInt int color = typedValue.data;

        // Remove underlined hyperlinks
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpan(span.getURL()) {
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(underlined);

                    // special handling for red-colored error text on login screen:
                    /*if (source.equalsIgnoreCase(
                            textView.getContext().getResources().getString(R.string.usage_terms_message_red))) {
                        ds.setColor(Color.parseColor("#EC415A"));
                    } else {
                        ds.setColor(color);
                    }*/
                    ds.setColor(color);
                }
            };
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(s);
    }

    /**
     * Used for making EditText read-only (For use as spinner)
     *
     * @param editText
     * @param inputType
     */
    @BindingAdapter("inputType")
    public static void setInputType(EditText editText, String inputType) {
        if (inputType == null || TextUtils.isEmpty(inputType)) {
            return;
        }

        if (TextUtils.equals(inputType, "spinner")) {
            editText.setInputType(InputType.TYPE_NULL);
            editText.setKeyListener(null);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setLongClickable(false);
            editText.setCursorVisible(false);
        } else {
            PerkLogger.e(TAG, "setInputType: Specified an unsupported inputType: " + inputType);
        }
    }

    @BindingAdapter("tintCompoundDrawables")
    public static void setTintCompoundDrawables(TextView view, boolean tint) {
        if (!tint) {
            return;
        }

        Drawable[] drawables = view.getCompoundDrawables();
        for (Drawable drawable : drawables) {
            if (drawable == null) {
                continue;
            }
            Drawable wrapDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrapDrawable, view.getContext().getResources().getColor(
                    PerkPreferencesManager.INSTANCE.isNightMode() ? android.R.color.white
                            : android.R.color.black));
        }
    }

    @BindingAdapter("vectorDrawableLeft")
    public static void setVectorDrawableLeft(TextView view, int res) {
        Drawable drawable = (res == -1) ? null : AppCompatResources.getDrawable(view.getContext(), res);
        view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    @BindingAdapter("vectorDrawableRight")
    public static void setVectorDrawableRight(TextView view, int res) {
        Drawable drawable = (res == -1) ? null : AppCompatResources.getDrawable(view.getContext(), res);
        view.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    @BindingAdapter({"vectorDrawableLeft", "vectorDrawableRight"})
    public static void setVectorDrawableLeftAndRight(TextView view, int resLeft, int resRight) {
        Drawable drawableLeft = (resLeft == -1) ? null : AppCompatResources.getDrawable(view.getContext(), resLeft);
        Drawable drawableRight = (resRight == -1) ? null : AppCompatResources.getDrawable(view.getContext(), resRight);
        view.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, drawableRight, null);
    }

    @BindingAdapter("interestsItems")
    public static void setInterestListItems(RecyclerView recyclerView, final List<Interest> items) {
        InterestsListAdapter adapter = (InterestsListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("channelItems")
    public static void setChannelListItems(RecyclerView recyclerView, final List<Channel> items) {
        ChannelsListAdapter adapter = (ChannelsListAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("searchItems")
    public static void setSearchedVideosListItems(RecyclerView recyclerView, final List<BrightcovePlaylistData.BrightcoveVideo> items) {
        PlayerVideoAdapter adapter = (PlayerVideoAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }

        if (items == null || items.isEmpty()) {
            recyclerView.setBackground(null);
        } else {
            TypedArray a = recyclerView.getContext().getTheme().obtainStyledAttributes(
                    new int[]{R.attr.viewBackground});

            int color = a.getColor(0, 0);
            if (color != 0) {
                recyclerView.setBackgroundColor(color);
            } else {
                recyclerView.setBackground(null);
            }

            a.recycle();
        }
    }

    @BindingAdapter({"channelItemsList"})
    public static void setAllChannelsListItems(RecyclerView recyclerView, final List<Channel> items) {
        ChannelsGenreListItemAdapter adapter = (ChannelsGenreListItemAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }

        // Removed scroll to top as we now have pagination on Channels-screen
        //scrollToTop(recyclerView);
    }

    public static void scrollToTop(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }

        // Scroll to the top of the RecyclerView:
        recyclerView.postDelayed(() -> {
            if (recyclerView != null && recyclerView.isShown()) {
                recyclerView.scrollToPosition(0);
            }
        }, 300);
    }

    public static void smoothScrollToTop(SmoothScrollEnabledRecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }

        // Smooth scroll to top / first position:
        recyclerView.postDelayed(() -> {
            if (recyclerView != null && recyclerView.isShown()) {
                if (recyclerView.getChildCount() > 0) {
                    recyclerView.smoothScrollToTop();
                } else {
                    recyclerView.scrollToPosition(0);
                }
            }
        }, 300);
    }

    @BindingAdapter("userHistoryItems")
    public static void setUserVideoHistoryListItems(RecyclerView recyclerView, final List<BrightcovePlaylistData.BrightcoveVideo> items) {
        PlayerVideoAdapter adapter = (PlayerVideoAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("brandVideoItems")
    public static void setBrandVideoListItems(RecyclerView recyclerView, final List<BrightcovePlaylistData.BrightcoveVideo> items) {
        // same as setUserVideoHistoryListItems:
        setUserVideoHistoryListItems(recyclerView, items);

        // Disabled since we now have pagination as endless-scrolling list:
        // Smooth scroll to first position:
        /*if (recyclerView.getChildCount() > 0
                && recyclerView instanceof SmoothScrollEnabledRecyclerView) {
            ((SmoothScrollEnabledRecyclerView) recyclerView).smoothScrollToTop();
        }*/
    }

    @BindingAdapter({"customLayoutBehavior"})
    public static void setcustomLayoutBehavior(RecyclerView view, String behavior) {
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        if (!TextUtils.isEmpty(behavior)) {
            params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        } else {
            params.setBehavior(null);
        }
        view.setLayoutParams(params);
    }

    @BindingAdapter("triggerExpand")
    public static void triggerAppBarExpand(AppBarLayout view, boolean loadingData) {
        // expand the action bar to original/expanded state only when data is not loading. When
        // data is loading, the view is hidden anyway
        if (!loadingData) {
            view.setExpanded(true, true);
        }
    }

    @BindingAdapter("onLoadStateChange")
    public static void onLoadStateChanged(SwipeRefreshLayout view, boolean loadingData) {
        if (!loadingData) {
            view.setRefreshing(false);
        }
    }

    @BindingAdapter("onClickUrl")
    public static void setOnClickUrl(TextView textView, final String source) {
        if (TextUtils.isEmpty(source) || textView == null) {
            return;
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(AppUtility.replaceAccessTokenMacro(source)), view.getContext(),
                        WatchbackWebViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });
    }

    @BindingAdapter({"onClickUrl", "pageTitle"})
    public static void setOnClickUrlWithTitle(TextView textView, final String source, final String pageTitle) {
        if (TextUtils.isEmpty(source) || textView == null) {
            return;
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(AppUtility.replaceAccessTokenMacro(source)), view.getContext(),
                        WatchbackWebViewActivity.class);
                intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, pageTitle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });
    }

    @BindingAdapter("layout_margins")
    public static void setRecyclerViewMargins(View view, float margin) {
        if (view == null || !(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }

        PerkLogger.d(TAG, "setRecyclerViewMargins: margin=" + margin);

        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.topMargin = (int) margin;
        layoutParams.bottomMargin = (int) (margin / 2);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter({"shimmer_base_alpha_nightMode", "shimmer_base_alpha_dayMode"})
    public static void setShimmerBaseAlpha(ShimmerFrameLayout view, float baseAlphaNightMode, float baseAlphaDayMode) {
        boolean nightMode = PerkPreferencesManager.INSTANCE.isNightMode();

        Shimmer.Builder shimmer = new Shimmer.AlphaHighlightBuilder()
                .setClipToChildren(true)
                .setAutoStart(true)
                // TODO: Remove this if we are always going to use the day-mode value --since that looks best currently:
                //.setBaseAlpha(nightMode ? baseAlphaDayMode : baseAlphaDayMode)
                .setBaseAlpha(baseAlphaDayMode)
                .setHighlightAlpha(1f)
                .setWidthRatio(0.75f)
                .setDuration(1250)
                .setRepeatCount(ValueAnimator.INFINITE)
                .setRepeatMode(ValueAnimator.RESTART);

        view.setShimmer(shimmer.build());
    }

    @BindingAdapter("videoHistoryList")
    public static void setVideoHistory(RecyclerView recyclerView, List<VideoHistoryModel> items) {
        VideoHistoryAdapter adapter = (VideoHistoryAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setList(items);
        }

        // Scroll to the top of the RecyclerView:
        recyclerView.postDelayed(() -> {
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        }, 300);
    }

    @BindingAdapter("imageResource")
    public static void setImageSrc(ImageView view, int id) {
        view.setImageResource(id);
    }

    @BindingAdapter("updateCollapsableToolbarLockState")
    public static void updateCollapsableToolbarLockState(CollapsingToolbarLayout toolbar, boolean shouldScroll) {

        if(shouldScroll){
            AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            p.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            toolbar.setLayoutParams(p);
        }/*else{
          // list is empty, no need to set scroll flags.
        }*/

    }
}
