package pt.flora_on.homemluzula;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 09-10-2016.
 */

public class VectorMapView extends PanZoomView {
    private final List<PointF> mPoints = new ArrayList<>()
        , originalMPoints = new ArrayList<>();
    private final List<PointF> mTracklog = new ArrayList<>();
    private final List<Path> tracklogPath = new ArrayList<>();
    private PointF currentLocation;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Float originalMinX, originalMaxX, originalMinY, originalMaxY;
    private float factor;
    private VectorMapView.OnClickListener clickListener;
    private Integer selectedPoint;

    public interface OnClickListener {
        void clickListener(Integer point);
    }

    public VectorMapView(Context context) {
        super(context);
        initVectorMapView();
    }

    public VectorMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVectorMapView();
    }

    public VectorMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVectorMapView();
    }

    public void setSelectedPoint(Integer toSelect) {
        if(toSelect < 0 || toSelect >= mPoints.size())
            selectedPoint = null;
        else
            selectedPoint = toSelect;
        invalidate();
    }

    public Integer getSelectedPoint() {
        return selectedPoint;
    }

    public void setCurrentLocation(PointF point) {
        currentLocation = (point == null) ? null : getPointScaled(point);
        invalidate();
    }

    public void initVectorMapView() {
        setDrawCallback(new PanZoomView.DrawCallback() {
            @Override
            public void DrawCallback(Canvas canvas, float scale) {
                if(mPoints != null) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.parseColor("#ff7700"));
                    for (PointF pt : mPoints) {
                        if (pt == null) continue;
                        canvas.drawCircle(pt.x, pt.y, 10 / scale, paint);
                        //canvas.drawPoint(pt.x, pt.y, paint);
                    }
                }

                if(selectedPoint != null) {
                    paint.setStrokeWidth(5 / scale);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.YELLOW);
                    canvas.drawCircle(mPoints.get(selectedPoint).x, mPoints.get(selectedPoint).y, 18 / scale, paint);
                }

                if(currentLocation != null) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.parseColor("#00ff00"));
                    canvas.drawPoint(currentLocation.x, currentLocation.y, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(currentLocation.x, currentLocation.y, 18 / scale, paint);
                }

                if(mTracklog.size() > 1) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.YELLOW);
                    paint.setPathEffect(null);
                    paint.setStrokeWidth(2 / scale);
                    //Toast.makeText(MainMap.this, String.format("%d",tracklogPath.), Toast.LENGTH_SHORT).show();
                    //canvas.drawPath(tracklogPath, paint);
                }
            }
        });

        super.setOnClickListener(new PanZoomView.OnClickListener() {
            @Override
            public void clickListener(float x, float y) {
                //Toast.makeText(VectorMapView.this.getContext(), String.format("Clique %f %f", x,y), Toast.LENGTH_SHORT).show();
                if(VectorMapView.this.clickListener == null) return;
                float hyp;
                Float minHyp = null;
                int closest = -1;
                PointF tmp;
                for(int i = 0; i < mPoints.size(); i++) {
                    tmp = mPoints.get(i);
                    if(tmp == null) continue;
                    hyp = (x - tmp.x) * (x - tmp.x) + (y - tmp.y) * (y - tmp.y);
                    if(minHyp == null || hyp < minHyp) {
                        minHyp = hyp;
                        closest = i;
                    }
                }
                selectedPoint = closest;
                invalidate();
                VectorMapView.this.clickListener.clickListener(closest);
            }
        });
    }

    public void setOnClickListener(VectorMapView.OnClickListener listener) {
        this.clickListener = listener;
    }

    public void removePoint(int index) {
        mPoints.remove(index);
        originalMPoints.remove(index);
        selectedPoint = mPoints.size() - 1;
        fitToScreen();
    }

    public PointF getPointScaled(PointF point) {
        return new PointF(
                (point.x - originalMinX) * factor
                , (point.y - originalMinY) * factor);
    }

    public void addPoint(PointF point) {
        if(point == null) {
            mPoints.add(null);
            originalMPoints.add(null);
        } else {
            originalMPoints.add(new PointF(point.x, point.y));
            mPoints.add(getPointScaled(point));
        }
        fitToScreen();
    }

    public void addToTracklog(PointF point) {
        PointF tmp = getPointScaled(point);
        Path path;  // TODO multipart paths
        mTracklog.add(tmp);
    /*    if(mTracklog.size() == 1) {
            path = new Path();
            path.moveTo(tmp.x, tmp.y);
            tracklogPath.add(path);
        } else {
            tracklogPath.lineTo(tmp.x, tmp.y);
        }*/
        invalidate();
    }

    /**
     * Sets a list of points to be displayed in the map
     * @param points
     */
    public void setPoints(List<PointF> points) {
        for(PointF p : points) {
            if(p == null)
                originalMPoints.add(null);
            else
                originalMPoints.add(new PointF(p.x, p.y));
        }

        originalMinX = null;
        originalMinY = null;
        originalMaxX = null;
        originalMaxY = null;

        for(PointF p : originalMPoints) {
            if(p == null) continue;
            if(originalMinX == null || p.x < originalMinX) originalMinX = p.x;
            if(originalMinY == null || p.y < originalMinY) originalMinY = p.y;
            if(originalMaxX == null || p.x > originalMaxX) originalMaxX = p.x;
            if(originalMaxY == null || p.y > originalMaxY) originalMaxY = p.y;
        }

        // rescale point coordinates to a 0-1000 range, in order to avoid loss of precision in
        // matrix calculations
        float fx = 1000 / (originalMaxX - originalMinX);
        float fy = 1000 / (originalMaxY - originalMinY);

        factor = Math.min(fx, fy);
        for(PointF p : originalMPoints)
            if (p == null)
                mPoints.add(null);
            else
                mPoints.add(new PointF((p.x - originalMinX) * factor, (p.y - originalMinY) * factor));
    }

    public RectF getBoundingBox() {
        Float minx = null, maxx = null, miny = null, maxy = null;

        for(PointF p : originalMPoints) {
            if(p == null) continue;
            if(minx == null || p.x < minx) minx = p.x;
            if(miny == null || p.y < miny) miny = p.y;
            if(maxx == null || p.x > maxx) maxx = p.x;
            if(maxy == null || p.y > maxy) maxy = p.y;
        }

        return new RectF(minx, maxy, maxx, miny);
    }

    public void fitToScreen() {
        RectF bbox = getBoundingBox();
        fitBounds(
                (bbox.left - originalMinX) * factor
                , (bbox.right - originalMinX) * factor
                , (bbox.bottom - originalMinY) * factor
                , (bbox.top - originalMinY) * factor);
    }

    public void centerOnCurrentLocation() {
        // FIXME
        fitBounds(currentLocation.x - 10f, currentLocation.x + 10f, currentLocation.y - 10f, currentLocation.y + 10f);
    }
}
