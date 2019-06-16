package pt.flora_on.homemluzula.geo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import org.metalev.multitouch.controller.MultiTouchController;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;

import java.util.List;

import pt.flora_on.homemluzula.MainMap;

/**
 * Created by Miguel Porto on 12-10-2016.
 */

public class SimplePointOverlayWithTracklog extends SimpleFastPointOverlay {
//    private Tracklog mTracklog = new Tracklog();
    private Location currentLocation;
    private Drawable youAreHere;
    private final Paint mTracklogPaint = new Paint();
    private final Paint mDistanceLinePaint = new Paint();
    private final Paint mDistanceLineText = new Paint();
    private final Paint mDistanceLineTextOutline = new Paint();
    private final Paint mDistanceLineMarkers = new Paint();
    private GeoPoint mPt1, mPt2;

    public SimplePointOverlayWithTracklog(PointAdapter pointList, SimpleFastPointOverlayOptions style) {
        super(pointList, style);
        mDistanceLinePaint.setStrokeWidth(12);
        mDistanceLinePaint.setColor(Color.CYAN);
        mDistanceLinePaint.setPathEffect(new DashPathEffect(new float[] {64f, 64f}, 0f));
        mDistanceLinePaint.setStyle(Paint.Style.STROKE);

        mDistanceLineMarkers.setColor(Color.CYAN);
        mDistanceLineMarkers.setStyle(Paint.Style.FILL);

        mDistanceLineText.setStyle(Paint.Style.FILL);
        mDistanceLineText.setColor(Color.YELLOW);
        mDistanceLineText.setTextAlign(Paint.Align.CENTER);
        mDistanceLineText.setFakeBoldText(true);
        mDistanceLineText.setTextSize(64);

        mDistanceLineTextOutline.setStyle(Paint.Style.STROKE);
        mDistanceLineTextOutline.setColor(Color.BLACK);
        mDistanceLineTextOutline.setStrokeWidth(3);
        mDistanceLineTextOutline.setTextAlign(Paint.Align.CENTER);
        mDistanceLineTextOutline.setFakeBoldText(true);
        mDistanceLineTextOutline.setTextSize(64);
    }

//    public void setTracklogObject(Tracklog tracklog) {
//        mTracklog = tracklog;
//    }

    public void setYouAreHereDrawable(Drawable d) {
        youAreHere = d;
    }

/*
    public void setCurrentLocation(IGeoPoint point) {
        currentLocation = (point == null) ? null : point;
    }
*/

    public void setCurrentLocation(Location point) {
//        currentLocation = (point == null) ? null : new GeoPoint(point.getLatitude(), point.getLongitude());
        currentLocation = point;
    }


    public void setDistanceLine(GeoPoint pt1, GeoPoint pt2) {
        mPt1 = pt1;
        mPt2 = pt2;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        if(mPt1 != null && mPt2 != null) {
            float mm5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 8, mapView.getContext().getResources().getDisplayMetrics());
            mm5 = mm5 * mm5;
            final Point p1px = new Point();
            final Point p2px = new Point();
            final Projection pj = mapView.getProjection();
            pj.toPixels(mPt1, p1px);
            pj.toPixels(mPt2, p2px);
            float hyp1 = (event.getX(0) - p1px.x) * (event.getX(0) - p1px.x) + (event.getY(0) - p1px.y) * (event.getY(0) - p1px.y);
            float hyp2 = (event.getX(0) - p2px.x) * (event.getX(0) - p2px.x) + (event.getY(0) - p2px.y) * (event.getY(0) - p2px.y);
            if(hyp1 < mm5 || hyp2 < mm5) {
                setDistanceLine(null, null);
                mapView.invalidate();
            }
        }

        return super.onSingleTapConfirmed(event, mapView);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        final Point mPositionPixels = new Point();
        final Projection pj = mapView.getProjection();

        // draw tracklog
/*
        if(mTracklog != null) {
            Path path;
            IGeoPoint pt;
            mTracklogPaint.setStyle(Paint.Style.STROKE);
            mTracklogPaint.setColor(Color.YELLOW);
            mTracklogPaint.setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0f));
            mTracklogPaint.setStrokeWidth(3);
            for (List<GeoTimePoint> pl : mTracklog) {
                if (pl.size() < 2) continue;
                pt = pl.get(0);
                path = new Path();
                pj.toPixels(pt, mPositionPixels);
                path.moveTo((float) mPositionPixels.x, (float) mPositionPixels.y);
                for (int i = 1; i < pl.size(); i++) {
                    pj.toPixels(pl.get(i), mPositionPixels);
                    path.lineTo((float) mPositionPixels.x, (float) mPositionPixels.y);
                }
                canvas.drawPath(path, mTracklogPaint);
            }
            mTracklogPaint.setPathEffect(null);

            // draw tracklog's start and end markers
            mTracklogPaint.setStyle(Paint.Style.FILL);
            for (List<GeoTimePoint> pl : mTracklog) {
                if (pl.size() < 2) continue;
                pj.toPixels(pl.get(0), mPositionPixels);
                mTracklogPaint.setColor(Color.parseColor("#0000ff"));
                canvas.drawCircle((float) mPositionPixels.x, (float) mPositionPixels.y, 7, mTracklogPaint);
                mTracklogPaint.setColor(Color.YELLOW);
                pj.toPixels(pl.get(pl.size() - 1), mPositionPixels);
                canvas.drawCircle((float) mPositionPixels.x, (float) mPositionPixels.y, 7, mTracklogPaint);
            }
        }
*/

        if(currentLocation != null) {
            GeoPoint gpt = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            pj.toPixels(gpt, mPositionPixels);
            if(youAreHere == null) {
                mTracklogPaint.setStyle(Paint.Style.FILL);
                mTracklogPaint.setColor(Color.parseColor("#00ff00"));
                canvas.drawPoint(mPositionPixels.x, mPositionPixels.y, mTracklogPaint);
                mTracklogPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(mPositionPixels.x, mPositionPixels.y, 18, mTracklogPaint);
            } else {
                Rect rect = new Rect(0, 0, 100, 100);
                rect.offset(-(int)(0.5f * 100) , -(int)(0.5f * 100) );
                youAreHere.setBounds(rect);
                //youAreHere.setBounds(mPositionPixels.x - 10, mPositionPixels.y - 10, mPositionPixels.x + 10, mPositionPixels.y + 10);
                drawAt(canvas, youAreHere, mPositionPixels.x, mPositionPixels.y, false
                        , currentLocation.hasBearing() ? -currentLocation.getBearing() : 0);
                //youAreHere.draw(canvas);
                /*canvas.save();
                float mapRotation = 45;
                if(mapRotation >= 360.0F) {
                    mapRotation -= 360.0F;
                }

                canvas.rotate(mapRotation, (float)mPositionPixels.x, mPositionPixels.y);
                canvas.drawBitmap(youAreHere, mPositionPixels.x, mPositionPixels.y, null);
                canvas.restore();*/

            }
        }

        super.draw(canvas, mapView, b);

        if(mPt1 != null) {
            final Point pt2px = new Point();
            pj.toPixels(mPt1, mPositionPixels);
            pj.toPixels(mPt2, pt2px);
            canvas.drawLine(mPositionPixels.x, mPositionPixels.y, pt2px.x, pt2px.y, mDistanceLinePaint);
            canvas.drawCircle(mPositionPixels.x, mPositionPixels.y, 18, mDistanceLineMarkers);
            canvas.drawCircle(pt2px.x, pt2px.y, 18, mDistanceLineMarkers);
            canvas.drawText(MainMap.formatDistance(mPt1.distanceToAsDouble(mPt2)), (mPositionPixels.x +  pt2px.x) / 2, (mPositionPixels.y +  pt2px.y) / 2, mDistanceLineText);
            canvas.drawText(MainMap.formatDistance(mPt1.distanceToAsDouble(mPt2)), (mPositionPixels.x +  pt2px.x) / 2, (mPositionPixels.y +  pt2px.y) / 2, mDistanceLineTextOutline);
        }
    }
}
