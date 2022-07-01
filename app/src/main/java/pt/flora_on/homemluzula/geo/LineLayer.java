package pt.flora_on.homemluzula.geo;

import android.location.Location;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LineLayer extends Layer implements Iterable<Tracklog.Segment>, Serializable {
    protected List<Tracklog.Segment> tracklog = new ArrayList<>();
    public transient Map<Tracklog.Segment, Polyline> map;

    public LineLayer() {
        super();
    }

    public LineLayer(FolderOverlay folder) {
        super(folder);
        this.map = new IdentityHashMap<>();
    }

    @Override
    public void add(GeoTimePoint point, boolean breakPath) {
        Tracklog.Segment tmp;
        Polyline pl;
        if(tracklog == null)
            tracklog = new ArrayList<>();
        if(tracklog.size() == 0 || breakPath || folder.getItems().size() == 0) {
            tracklog.add(tmp = new Tracklog.Segment());
            folder.add(pl = createPolylineFromPoints(null));
            map.put(tmp, pl);
        } else {
            tmp = tracklog.get(tracklog.size() - 1);
            pl = (Polyline) folder.getItems().get(folder.getItems().size() - 1);
        }
        pl.addPoint(point);
        tmp.add(point);
    }

    @Override
    public void add(Location point, boolean breakPath) {
        this.add(new GeoTimePoint(point), breakPath);
    }

    public void refresh() {
        if(folder == null) return;
        folder.getItems().clear();
        Polyline pl;
        if(this.map == null)
            this.map = new IdentityHashMap<>();

        for(Tracklog.Segment plist : tracklog) {
            folder.add(pl = createPolylineFromPoints(plist));
            this.map.put(plist, pl);
        }
    }

    public void setWidth(float width) {
        this.width = width;
        for(Polyline pl : this.map.values()) {
            if(pl != null) pl.getOutlinePaint().setStrokeWidth(width);
        }
    }

    Polyline createPolylineFromPoints(Tracklog.Segment plist) {
        Polyline pl = new SensitivePolyline();
        pl.setOnClickListener(new clickLayer());
//            pl.getPaint().setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0f));

        if(plist == null || plist.getColor() == null)
            pl.getOutlinePaint().setColor(this.color);
        else
            pl.getOutlinePaint().setColor(plist.getColor());

        pl.getOutlinePaint().setStrokeWidth(this.width);
//        pl.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
//        pl.getOutlinePaint().setShadowLayer(30, 0, 0, Color.BLACK);
        if(plist != null) {
            for (GeoTimePoint gtp : plist)
                pl.addPoint(gtp);
        }
        return pl;
    }

    @Override
    public void setColor(Integer color) {
        this.color = color;
        for(Polyline pl : this.map.values()) {
            if(pl != null) pl.getOutlinePaint().setColor(color);
        }
    }

    @NonNull
    @Override
    public Iterator<Tracklog.Segment> iterator() {
        return tracklog.iterator();
    }

    class clickLayer implements Polyline.OnClickListener {
        @Override
        public boolean onClick(Polyline polyline, MapView mapView, GeoPoint geoPoint) {
            if(clickListener != null)
                clickListener.onClick(null);
            return true;
        }
    }
}
