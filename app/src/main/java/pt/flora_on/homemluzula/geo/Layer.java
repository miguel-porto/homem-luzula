package pt.flora_on.homemluzula.geo;

import android.location.Location;

import org.osmdroid.views.overlay.Polyline;

public interface Layer extends Iterable<Tracklog.Segment> {
    void add(GeoTimePoint point, boolean breakPath);
    void add(Location point, boolean breakPath);
/*
    void setSelectedSegment(Tracklog.Segment segment);
    Polyline createPolylineFromPoints(Tracklog.Segment plist);
*/
}
