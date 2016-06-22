package com.sccomponents.widgets;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Draw a pointer on the path at a certain distance from the path start.
 *
 * @author Samuele Carassai
 * @version 1.0.0
 * @since 2016-05-26
 */
public class ScPointer extends ScFeature {

    /****************************************************************************************
     * Constants
     */

    public static final float DEFAULT_HALO_WIDTH = 10.0f;
    public static final int DEFAULT_HALO_ALPHA = 128;


    /****************************************************************************************
     * Enumerators
     */

    /**
     * Define the pointer status
     */
    @SuppressWarnings("unused")
    public enum PointerStatus {
        RELEASED,
        PRESSED
    }


    /****************************************************************************************
     * Private variables
     */

    private float mPointerRadius;
    private float mPointerPosition;
    private float mHaloWidth;
    private int mHaloAlpha;
    private PointerStatus mPointerStatus;

    private OnDrawListener mOnDrawListener;


    /****************************************************************************************
     * Constructor
     */

    @SuppressWarnings("unused")
    public ScPointer(Path path) {
        // Super
        super(path);

        // Init
        this.mPointerRadius = 0.0f;
        this.mHaloWidth = ScPointer.DEFAULT_HALO_WIDTH;
        this.mHaloAlpha = ScPointer.DEFAULT_HALO_ALPHA;
        this.mPointerStatus = PointerStatus.RELEASED;

        // Paint
        this.mPaint.setStrokeWidth(1.0f);
        this.mPaint.setStyle(Paint.Style.FILL);
    }


    /****************************************************************************************
     * Draw methods
     * <p>
     * ATTENTION!
     * In these methods I used to instantiate new objects and is preferable NOT do it for improve
     * the performance of the component drawing.
     * In case of low performance the first solution must be to move the new object creation in
     * the global scope for do it once.
     */

    /**
     * Default drawing the circles that representing the pointer.
     * Note when draw a circle the angle not take effect on the final drawing.
     *
     * @param canvas where to draw
     * @param point  the point on path
     * @param info   the pointer info
     */
    private void drawCircles(Canvas canvas, PointF point, PointerInfo info) {
        // Adjust the pointer offset
        ScPointer.translatePoint(point, info.offset, info.angle);

        // Check for null values and for the pointer radius
        if (canvas != null && this.mPointerRadius > 0.0f) {
            // Draw the halo and the pointer
            canvas.drawCircle(point.x, point.y, this.mPointerRadius, info.haloPainter);
            canvas.drawCircle(point.x, point.y, this.mPointerRadius, this.mPaint);
        }
    }

    /**
     * Draw on canvas a bitmap centered in the passed point.
     *
     * @param canvas where to draw
     * @param point  the point on path
     * @param info   the pointer info
     */
    private void drawBitmap(Canvas canvas, PointF point, PointerInfo info) {
        // Calculate the bitmap center
        int centerX = (int) ((info.bitmap.getWidth() + point.x) / 2);
        int centerY = (int) ((info.bitmap.getHeight() + point.y) / 2);

        // Save the current canvas state
        canvas.save();

        // Translate and rotate the canvas
        canvas.rotate(info.angle, centerX, centerY);
        canvas.translate(info.offset.x, info.offset.y);

        // Draw the bitmap and restore the canvas state
        canvas.drawBitmap(info.bitmap, centerX, centerY, this.mPaint);
        canvas.restore();
    }

    /**
     * Draw the pointer on the canvas
     *
     * @param canvas where draw
     */
    private void drawPointer(Canvas canvas) {
        // Set the pointer painter
        boolean isPressed = this.mPointerStatus == PointerStatus.PRESSED;
        this.mPaint.setAlpha(isPressed ? this.mHaloAlpha : 255);

        // Set the halo painter
        Paint haloPaint = new Paint(this.mPaint);
        haloPaint.setAlpha(isPressed ? 255 : this.mHaloAlpha);
        haloPaint.setStyle(Paint.Style.STROKE);
        haloPaint.setStrokeWidth(this.mHaloWidth);

        // Refresh the measurer and convert the position in a distance
        float distance = (this.mPathLength * this.mPointerPosition) / 100;

        // Find the point on the path and check the result
        float[] point = this.mPathMeasure.getContoursPosTan(distance);
        if (point == null) return;

        // Create the pointer info holder
        PointerInfo info = new PointerInfo();
        info.source = this;
        info.offset = new PointF();
        info.haloPainter = haloPaint;
        info.angle = (float) Math.toDegrees(point[3]);

        // Check the listener
        if (this.mOnDrawListener != null) {
            this.mOnDrawListener.onBeforeDrawPointer(info);
        }

        // Check if the bitmap is not null
        if (info.bitmap != null) {
            // Draw a bitmap
            this.drawBitmap(canvas, ScPointer.toPoint(point), info);

        } else {
            // Draw the circles
            this.drawCircles(canvas, ScPointer.toPoint(point), info);
        }
    }


    /****************************************************************************************
     * Overrides
     */

    /**
     * Draw the pointer on the canvas.
     *
     * @param canvas the canvas where draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for null values
        if (this.mPath == null)
            return;

        // Draw the pointer
        this.drawPointer(canvas);
    }


    /****************************************************************************************
     * Public classes and methods
     */

    /**
     * This is a structure to hold the notch information before draw it.
     */
    @SuppressWarnings("unused")
    public class PointerInfo {

        public ScPointer source;
        public Bitmap bitmap;
        public Paint haloPainter;
        public PointF offset;
        public float angle;

    }


    /****************************************************************************************
     * Public properties
     */

    /**
     * Return the position of pointer in percentage respect to the path length.
     *
     * @return the position in percentage
     */
    @SuppressWarnings("unused")
    public float getPosition() {
        return this.mPointerPosition;
    }

    /**
     * Set the position of pointer in percentage respect to the path length.
     *
     * @param value the position in percentage
     */
    @SuppressWarnings("unused")
    public void setPosition(float value) {
        // Check the limits
        if (value < 0.0f) value = 0.0f;
        if (value > 100.0f) value = 100.0f;

        // Store the value
        this.mPointerPosition = value;
    }

    /**
     * Return the pointer radius in pixel.
     *
     * @return the radius
     */
    @SuppressWarnings("unused")
    public float getRadius() {
        return this.mPointerRadius;
    }

    /**
     * Set the pointer radius in pixel.
     *
     * @param value the radius
     */
    @SuppressWarnings("unused")
    public void setRadius(float value) {
        this.mPointerRadius = value < 0.0f ? 0.0f : value;
    }

    /**
     * Return the halo width in pixel
     *
     * @return the width
     */
    @SuppressWarnings("unused")
    public float getHaloWidth() {
        return this.mHaloWidth;
    }

    /**
     * Set the halo width in pixel
     *
     * @param value the width
     */
    @SuppressWarnings("unused")
    public void setHaloWidth(float value) {
        this.mHaloWidth = value < 0.0f ? 0.0f : value;
    }

    /**
     * Return the halo alpha
     *
     * @return the alpha
     */
    @SuppressWarnings("unused")
    public int getHaloAlpha() {
        return this.mHaloAlpha;
    }

    /**
     * Set the halo alpha
     *
     * @param value the new alpha value
     */
    @SuppressWarnings("unused")
    public void setHaloAlpha(int value) {
        // Check the limits
        if (value < 0) value = 0;
        if (value > 255) value = 255;

        // Store the value
        this.mHaloAlpha = value;
    }

    /**
     * Return the pointer status
     *
     * @return the current status
     */
    @SuppressWarnings("unused")
    public PointerStatus getStatus() {
        return this.mPointerStatus;
    }

    /**
     * Set the pointer status
     *
     * @param value the new status
     */
    @SuppressWarnings("unused")
    public void setStatus(PointerStatus value) {
        this.mPointerStatus = value;
    }


    /****************************************************************************************
     * Listeners and Interfaces
     */

    /**
     * Define the draw listener interface
     */
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        /**
         * Called before draw the pointer.
         * If the method set the bitmap inside the info object the default drawing will be bypassed
         * and the new bitmap will be draw on the canvas following the other setting.
         *
         * @param info the pointer info
         */
        void onBeforeDrawPointer(PointerInfo info);

    }

    /**
     * Set the draw listener to call
     *
     * @param listener the linked method to call
     */
    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}