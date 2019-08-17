package com.watchback2.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.watchback2.android.R;

/**
 * Created by perk on 15/03/18.
 */

/**
 * Custom ImageView that allows center top cropping also.
 */
public class WatchBackImageView extends AppCompatImageView {

    // ----------------------------------------------------------------------
    // Public enums
    // ----------------------------------------------------------------------

    /**
     * Extended options for scaling the bounds of an image to the bounds of this view.
     *
     * Basically, we added one more setting top center crop
     */
    public enum ScaleType {
        /**
         * Scale using the image matrix when drawing. The image matrix can be set using
         * {@link ImageView#setImageMatrix(Matrix)}. From XML, use this syntax:
         * <code>android:scaleType="matrix"</code>.
         */
        MATRIX(0),
        /**
         * Scale the image using {@link Matrix.ScaleToFit#FILL}.
         * From XML, use this syntax: <code>android:scaleType="fitXY"</code>.
         */
        FIT_XY(1),
        /**
         * Scale the image using {@link Matrix.ScaleToFit#START}.
         * From XML, use this syntax: <code>android:scaleType="fitStart"</code>.
         */
        FIT_START(2),
        /**
         * Scale the image using {@link Matrix.ScaleToFit#CENTER}.
         * From XML, use this syntax:
         * <code>android:scaleType="fitCenter"</code>.
         */
        FIT_CENTER(3),
        /**
         * Scale the image using {@link Matrix.ScaleToFit#END}.
         * From XML, use this syntax: <code>android:scaleType="fitEnd"</code>.
         */
        FIT_END(4),
        /**
         * Center the image in the view, but perform no scaling.
         * From XML, use this syntax: <code>android:scaleType="center"</code>.
         */
        CENTER(5),
        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that both dimensions (width and height) of the image will be equal
         * to or larger than the corresponding dimension of the view
         * (minus padding). The image is then centered in the view.
         * From XML, use this syntax: <code>android:scaleType="centerCrop"</code>.
         */
        CENTER_CROP(6),
        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that both dimensions (width and height) of the image will be equal
         * to or less than the corresponding dimension of the view
         * (minus padding). The image is then centered in the view.
         * From XML, use this syntax: <code>android:scaleType="centerInside"</code>.
         */
        CENTER_INSIDE(7),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that both dimensions (width and height) of the image will be equal
         * to or larger than the corresponding dimension of the view
         * (minus padding). The image is then top centered in the view.
         * From XML, use this syntax: <code>app:scaleType="topCenterCrop"</code>.
         */
        TOP_CENTER_CROP(8),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that width of the image will be equal to or larger than the width of
         * the view (minus padding). The image is then top centered in the view.
         * From XML, use this syntax: <code>app:scaleType="fillXTopCenter"</code>.
         */
        FILL_X_TOP_CENTER(9),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that height of the image will be equal to or larger than the width of
         * the view (minus padding). The image is then centered in the view.
         * From XML, use this syntax: <code>app:scaleType="fillYCenter"</code>.
         */
        FILL_Y_CENTER(10),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so
         * that width of the image will be equal to or larger than the height of
         * the view (minus padding). The image is then centered in the view.
         * From XML, use this syntax: <code>app:scaleType="fillXCenter"</code>.
         */
        FILL_X_CENTER(11);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;

    }

    // ----------------------------------------------------------------------
    // Private variables
    // ----------------------------------------------------------------------

    /** Keeps track whether the first frame of the image view is drawn or not. */
    private boolean mHaveFrame = false;

    /** Keeps track which type of scaling is used for scaling image drawable. */
    private ScaleType mScaleType = ScaleType.TOP_CENTER_CROP;

    /** Minimum width of the image view. */
    private int mMinHeight = 0;

    /** Minimum height of the image view. */
    private int mMinWidth = 0;

    /** AdjustViewBounds behavior will be in compatibility mode for older apps. */
    private boolean mAdjustViewBoundsCompat = false;

    /** Specifies if the image view needs to be a square i.e. height will be made same as width */
    private boolean mSquareImageViewMode = false;

    // ----------------------------------------------------------------------
    // ImageButton overridden methods
    // ----------------------------------------------------------------------

    /**
     * Constructor.
     */
    public WatchBackImageView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor.
     */
    public WatchBackImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor.
     */
    public WatchBackImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            // Read default settings for image rendering
            final TypedArray watchBackImageViewStyleable = context.obtainStyledAttributes(
                    attrs, R.styleable.WatchBackImageView);

            // Set the scale type for the image view
            final int index = watchBackImageViewStyleable.getInt(
                    R.styleable.WatchBackImageView_scaleType, -1);
            if (index >= 0) {
                // Set the scale type if it is defined in the range of ScaleType
                final ScaleType[] scaleTypeValues = ScaleType.values();
                if (index < scaleTypeValues.length) {
                    setScaleType(scaleTypeValues[index]);
                }
            }

            // Set the alpha settings of the image
            final int alpha = watchBackImageViewStyleable.getInt(
                    R.styleable.WatchBackImageView_drawableAlpha, 255);
            if (alpha != 255) {
                //noinspection deprecation
                setAlpha(alpha);
            }

            // Set the square mode, if set
            mSquareImageViewMode = watchBackImageViewStyleable.getBoolean(
                    R.styleable.WatchBackImageView_squareImageView, false);

            // Recycle the styleable
            watchBackImageViewStyleable.recycle();
        }

        initImageView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // NOTE: Code of this method is mostly coped from the original implementation
        //       of ImageView class, except the following modifications:
        //       1. We resize the height if scale type is FILL_X_AUTO_Y_TOP_CENTER.
        //       2. ImageView can have the minimum width and height. And the view size will be
        //          calculated considering that in mind.

        int w;
        int h;

        // Desired aspect ratio of the view's contents (not including padding)
        float desiredAspect = 0.0f;

        // We are allowed to change the view's width
        boolean resizeWidth = false;

        // We are allowed to change the view's height
        boolean resizeHeight = false;

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        // Check if we have a valid drawable and at least one frame for the
        // image view is already drawn.
        final Drawable drawable = getDrawable();

        if (drawable == null) {
            // If no drawable, its intrinsic size is 0.
            w = h = 0;
        } else {
            w = drawable.getIntrinsicWidth();
            h = drawable.getIntrinsicHeight();
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;

            // We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            if (getAdjustViewBounds()) {
                resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;

                // Or the result with the previously saved result
                resizeHeight |= heightSpecMode != MeasureSpec.EXACTLY;
                desiredAspect = (float) w / (float) h;
            }
        }

        int pleft = getPaddingLeft();
        int pright = getPaddingRight();
        int ptop = getPaddingTop();
        int pbottom = getPaddingBottom();

        int widthSize;
        int heightSize;

        if (resizeWidth || resizeHeight) {

        	/* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension. 
            */

            // Get the max possible width given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright, getMaxWidth(), mMinWidth,
                    widthMeasureSpec);

            // Get the max possible height given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom, getMaxHeight(), mMinHeight,
                    heightMeasureSpec);

            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                float actualAspect = (float) (widthSize - pleft - pright) /
                        (heightSize - ptop - pbottom);

                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                    boolean done = false;

                    // Try adjusting width to be proportional to height
                    if (resizeWidth) {
                        int newWidth = (int) (desiredAspect * (heightSize - ptop - pbottom)) +
                                pleft + pright;

                        // Allow the width to outgrow its original estimate if height is fixed.
                        if (!resizeHeight && !mAdjustViewBoundsCompat) {
                            widthSize = resolveAdjustedSize(newWidth, getMaxWidth(), mMinWidth,
                                    widthMeasureSpec);
                        }

                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        }
                    }

                    // Try adjusting height to be proportional to width
                    if (!done && resizeHeight) {
                        int newHeight = (int) ((widthSize - pleft - pright) / desiredAspect) +
                                ptop + pbottom;

                        // Allow the height to outgrow its original estimate if width is fixed.
                        if (!resizeWidth && !mAdjustViewBoundsCompat) {
                            heightSize = resolveAdjustedSize(newHeight, getMaxHeight(), mMinHeight,
                                    heightMeasureSpec);
                        }

                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        }
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright;
            h += ptop + pbottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
        }

        setMeasuredDimension(widthSize, mSquareImageViewMode ? widthSize : heightSize);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        mHaveFrame = true;
        configureBounds();
        invalidate();
        return changed;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        configureBounds();
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        configureBounds();
        invalidate();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Sets the minimum height of the view.
     */
    public void setMinimumHeight(int minHeight) {
        mMinHeight = minHeight;
    }

    /**
     * Sets the minimum width of the view.
     */
    public void setMinimumWidth(int minWidth) {
        mMinWidth = minWidth;
    }

    /**
     * Sets the defined scale type. These scale type includes
     * ImageView scale types as well as the custom defined scale types
     * for image drawables.
     */
    public void setScaleType(ScaleType scaleType) {

        // save the scale type
        mScaleType = scaleType;

        // set the scale type on the ImageButton as matrix. We will set the
        // matrix whenever the image drawable is available or the frame of
        // the image view changes.
        switch (mScaleType) {
            case TOP_CENTER_CROP:
            case FILL_X_TOP_CENTER:
            case FILL_X_CENTER:
            case FILL_Y_CENTER:
                setScaleType(ImageView.ScaleType.MATRIX);
                break;

            default:
                break;
        }
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Configure the bounds for
     */
    private void configureBounds() {

        // Configure bonds only if the scale type is top center crop. Other cases are handled by the
        // image view by itself.
        if (ScaleType.TOP_CENTER_CROP != mScaleType && ScaleType.FILL_X_TOP_CENTER != mScaleType &&
                ScaleType.FILL_Y_CENTER != mScaleType && ScaleType.FILL_X_CENTER != mScaleType) {
            return;
        }

        // Check if we have a valid drawable and at least one frame for the
        // image view is already drawn.
        final Drawable drawable = getDrawable();
        if (drawable == null || !mHaveFrame) {
            return;
        }

        // Get the current matrix for the image
        final Matrix drawMatrix = new Matrix();

        // Get the height and width of the drawable
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        // Get the height and width of the view in which drawable is
        // going to be drawn.
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        // Define default scale and offset by which image should be moved after scaling
        float scale = 0.0f;
        float dx = 0.0f;
        float dy = 0.0f;

        // Determine by which scale we should scale down/up the image
        // NOTE: This code is borrowed from the ImageView implementation of
        //       CENTER_CROP except we do not center the image vertically.
        //       I have no specific details how they determine that image
        //       drawable should be shrinked or expanded using the width
        //       or height ratio. But my best guess is, they check if
        //       which ratio will shrink or expand the image more and based
        //       on that they perform their calculations.
        if (ScaleType.TOP_CENTER_CROP == mScaleType) {
            if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
                scale = (float) viewHeight / (float) drawableHeight;

                // Calculation of offset is as follows. If we need to center
                // the image, first they calculate what would be shrinked or
                // expanded width of the image. If the drawable
                // width is still bigger than the view width, then they calculate
                // what is the difference. And then the difference is halved,
                // because offset is set from the left corner. And we want to
                // crop the same amount of left and right portions of the drawable
                // if it cannot be fit in an image.
                dx = (int) ((viewWidth - drawableWidth * scale) * 0.5f + 0.5f);
            } else {
                scale = (float) viewWidth / (float) drawableWidth;
            }

        } else if (ScaleType.FILL_X_TOP_CENTER == mScaleType) {

            scale = (float) viewWidth / (float) drawableWidth;

            // Calculation of offset is as follows. If we need to center
            // the image, first they calculate what would be shrinked or 
            // expanded width of the image. If the drawable
            // width is still bigger than the view width, then they calculate
            // what is the difference. And then the difference is halved, 
            // because offset is set from the left corner. And we want to
            // crop the same amount of left and right portions of the drawable
            // if it cannot be fit in an image.
            // dx = (int) ((viewWidth - drawableWidth * scale) * 0.5f + 0.5f);

        } else if (ScaleType.FILL_X_CENTER == mScaleType) {

            scale = (float) viewWidth / (float) drawableWidth;

            // Calculation of offset is as follows. If we need to center
            // the image, first they calculate what would be shrinked or
            // expanded width of the image. If the drawable
            // width is still bigger than the view width, then they calculate
            // what is the difference. And then the difference is halved,
            // because offset is set from the left corner. And we want to
            // crop the same amount of left and right portions of the drawable
            // if it cannot be fit in an image.
            dy = (int) ((viewHeight - drawableHeight * scale) * 0.5f + 0.5f);

        } else if (ScaleType.FILL_Y_CENTER == mScaleType) {

            scale = (float) viewHeight / (float) drawableHeight;

            // Calculation of offset is as follows. If we need to center
            // the image, first they calculate what would be shrinked or
            // expanded width of the image. If the drawable
            // width is still bigger than the view width, then they calculate
            // what is the difference. And then the difference is halved,
            // because offset is set from the left corner. And we want to
            // crop the same amount of left and right portions of the drawable
            // if it cannot be fit in an image.
            dx = (int) ((viewWidth - drawableWidth * scale) * 0.5f + 0.5f);
        }

        // First scale the drawable, and then crop the image by centering
        // it horizontally.
        drawMatrix.setScale(scale, scale);
        drawMatrix.postTranslate(dx, dy);
        setImageMatrix(drawMatrix);
    }

    /**
     * Initializes image view.
     */
    private void initImageView() {
        // In ICS, hardware acceleration was turned on by default.
        // Until 4.0, the default was that hardware acceleration was off. Hardware acceleration does
        // not support clipPath. So for that we need to off the hardware acceleration.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mAdjustViewBoundsCompat =
                getContext().getApplicationInfo().targetSdkVersion <=
                        Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * Calculates the adjusted view size
     */
    private int resolveAdjustedSize(int desiredSize, int maxSize, int minSize, int measureSpec) {

        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Parent says we can be as big as we want. Just don't be larger
                // than max size imposed on ourselves.
                result = Math.min(Math.max(minSize, desiredSize), maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(Math.max(minSize, desiredSize), specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }
}