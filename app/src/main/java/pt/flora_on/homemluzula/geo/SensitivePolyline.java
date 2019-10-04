package pt.flora_on.homemluzula.geo;

import android.view.MotionEvent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polyline;

/**
 * More sensitive to touch than the original.
 * Created by miguel on 29-04-2018.
 */

public class SensitivePolyline extends Polyline {
    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint)pj.fromPixels((int)event.getX(), (int)event.getY());

        double tolerance = (double)(6 * mapView.getContext().getResources().getDisplayMetrics().density) * 6;
        boolean touched = this.isCloseTo(eventPos, tolerance, mapView);
        return touched?(this.mOnClickListener == null?this.onClickDefault(this, mapView, eventPos):this.mOnClickListener.onClick(this, mapView, eventPos)):touched;
    }

}
