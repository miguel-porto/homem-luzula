package pt.flora_on.homemluzula;

/**
 * Created by miguel on 09-10-2016.
 * This code results from a merge between code taken from here
 * http://www.c-sharpcorner.com/uploadfile/88b6e5/multi-touch-panning-pinch-zoom-image-view-in-android-using/
 * and here
 * http://android-developers.blogspot.pt/2010/06/making-sense-of-multitouch.html
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

public class PanZoomView extends View {
    private ScaleGestureDetector mScaleDetector;
    private int viewWidth = 0, viewHeight = 0;
    private float saveScale = 1.f;
    private Matrix mMatrix = new Matrix();
    protected Float maxScale = null, minScale = null;
    PointF last = new PointF();
    PointF start = new PointF();
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int CLICK = 3;
    int mode = NONE;
    private DrawCallback callback;
    private OnClickListener clickListener;

    public PanZoomView(Context context) {
        this(context, null, 0);
    }

    public PanZoomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public interface DrawCallback {
        void DrawCallback(Canvas canvas, float scale);
    }

    public interface OnClickListener {
        void clickListener(float x, float y);
    }

    public void setDrawCallback(DrawCallback callback) {
        this.callback = callback;
    }

    private void immediateFitBounds(final float minX, final float maxX, final float minY, final float maxY) {
        int width  = PanZoomView.this.getMeasuredWidth();
        int height = PanZoomView.this.getMeasuredHeight();

        float scale;

        float bmWidth = maxX - minX;
        float bmHeight = maxY - minY;

        float scaleX = (float) width / bmWidth;
        float scaleY = (float) height / bmHeight;

        scale = Math.min(scaleX, scaleY);

        // Center the image
        float redundantYSpace = (float) height - (scale * bmHeight);
        float redundantXSpace = (float) width - (scale * bmWidth);
        redundantYSpace /= (float) 2;
        redundantXSpace /= (float) 2;
        //MainMap.status.setText(String.format("%d %d -> %.1f %.1f", width, height, redundantXSpace, redundantYSpace));

        mMatrix.setScale(scale, scale);
        mMatrix.postScale(1, -1, width / 2, height / 2);
        mMatrix.postTranslate(redundantXSpace , redundantYSpace );
        saveScale = scale;
        invalidate();
    }
    public void fitBounds(final float minX, final float maxX, final float minY, final float maxY) {
        ViewTreeObserver vto = this.getViewTreeObserver();
        if(this.viewHeight == 0 || this.viewWidth == 0) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    PanZoomView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    immediateFitBounds(minX, maxX, minY, maxY);
                }
            });
        } else immediateFitBounds(minX, maxX, minY, maxY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last.set(curr);
                start.set(last);
                mode = DRAG;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    float deltaX = curr.x - last.x;
                    float deltaY = curr.y - last.y;
                    mMatrix.postTranslate(deltaX, deltaY);
                    last.set(curr.x, curr.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                if(this.clickListener == null) break;
                mode = NONE;
                int xDiff = (int) Math.abs(curr.x - start.x);
                int yDiff = (int) Math.abs(curr.y - start.y);
                if (xDiff < CLICK && yDiff < CLICK) {
                    Matrix invMat = new Matrix();
                    mMatrix.invert(invMat);
                    float[] p = new float[] {curr.x, curr.y};
                    float[] pt = new float[2];
                    invMat.mapPoints(pt, p);
                    this.clickListener.clickListener(pt[0], pt[1]);
                    //Toast.makeText(this.getContext(), String.format("%.1f %.1f -> %.1f %.1f", curr.x, curr.y, pt[0], pt[1]), Toast.LENGTH_SHORT).show();
                    performClick();
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

        }
        invalidate();
        return true; // indicate event was handled
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.clickListener = onClickListener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(this.callback == null) return;
        //MainMap.status.setText(String.format("Scale %.4f", saveScale));

        canvas.save();
        canvas.concat(mMatrix);

        this.callback.DrawCallback(canvas, saveScale);

        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;

            if (maxScale != null && saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (minScale != null && saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

    }
}