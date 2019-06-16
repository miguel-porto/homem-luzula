package pt.flora_on.homemluzula.geo;

import android.app.Application;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pt.flora_on.homemluzula.HomemLuzulaApp;
import pt.flora_on.homemluzula.MainMap;

public class Tracklog implements Iterable<Tracklog.Segment>, Serializable {
    private List<Segment> tracklog = new ArrayList<>();
    private transient FolderOverlay folder;
    private transient Segment selectedSegment;
    private transient View.OnClickListener clickListener;
    private transient Map<Segment, Polyline> map;
    private transient Integer TRACKLOGWIDTH;

    public Tracklog() {
        this.TRACKLOGWIDTH =
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext()).getString("pref_track_width", "4"));
    }

    public Tracklog(FolderOverlay folder) {
        this();
        this.folder = folder;
        this.map = new IdentityHashMap<>();
    }

    public void clear() {
        tracklog = new ArrayList<>();
    }

    @NonNull
    @Override
    public Iterator<Segment> iterator() {
        return tracklog.iterator();
    }

    public int size() {
        return tracklog.size();
    }

    public void setOverlay(FolderOverlay ovr) {
        this.folder = ovr;
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public GeoTimePoint getLastLocation() {
        if(tracklog == null || tracklog.size() == 0) return null;
        List<GeoTimePoint> tmp = tracklog.get(tracklog.size() - 1);
        if(tmp.size() == 0 && tracklog.size() == 1) return null;
        if(tmp.size() == 0)
            tmp = tracklog.get(tracklog.size() - 2);
        if(tmp.size() == 0) return null;
        return tmp.get(tmp.size() - 1);
    }

    public void add(GeoTimePoint point, boolean breakPath) {
        Segment tmp;
        Polyline pl;
        if(tracklog == null)
            tracklog = new ArrayList<>();
        if(tracklog.size() == 0 || breakPath || folder.getItems().size() == 0) {
            tracklog.add(tmp = new Segment());
            folder.add(pl = createPolylineFromPoints(null));
            map.put(tmp, pl);
        } else {
            tmp = tracklog.get(tracklog.size() - 1);
            pl = (Polyline) folder.getItems().get(folder.getItems().size() - 1);
        }
        pl.addPoint(point);
        tmp.add(point);
    }

    public void add(Location point, boolean breakPath) {
        this.add(new GeoTimePoint(point), breakPath);
    }

    public void refresh() {
        if(folder == null) return;
        Polyline pl;
        if(this.map == null)
             this.map = new IdentityHashMap<>();

        if(this.TRACKLOGWIDTH == null)
            this.TRACKLOGWIDTH = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext()).getString("pref_track_width", "4"));

        for(Segment plist : tracklog) {
            folder.add(pl = createPolylineFromPoints(plist));
            this.map.put(plist, pl);
        }
    }

    private Polyline createPolylineFromPoints(Segment plist) {
        Polyline pl = new SensitivePolyline();
        pl.setOnClickListener(new clickPolyline());
//            pl.getPaint().setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0f));

        if(plist == null || plist.getColor() == null)
            pl.setColor(Color.YELLOW);
        else
            pl.setColor(plist.getColor());

        pl.setWidth(TRACKLOGWIDTH);
        if(plist != null) {
            for (GeoTimePoint gtp : plist)
                pl.addPoint(gtp);
        }
        return pl;
    }

    public double getLength(int i) {
        Segment s = tracklog.get(i);
        double length = 0;
        for (int j = 0; j < s.size() - 1; j++) {
            length += s.get(j).distanceToAsDouble(s.get(j + 1));
        }
        return length;
    }

    public void deleteTrack(int i) {
        if(i >= tracklog.size() || i < 0) return;
        Polyline pl = map.get(tracklog.get(i));
        if(pl == null) return;
        map.remove(tracklog.get(i));
        folder.remove(pl);
        tracklog.remove(i);
    }

    public void setSelectedTrack(Integer toSelect) {
        if(toSelect != null && toSelect >= 0 && toSelect < this.tracklog.size()) {
            setSelectedSegment(this.tracklog.get(toSelect));
        } else {
            setSelectedSegment(null);
        }
    }

    public Integer getSelectedTrack() {
        if(selectedSegment == null) return null;
        return tracklog.indexOf(selectedSegment);
    }

    public Segment getSelectedSegment() {
        return selectedSegment;
    }

    public Polyline getSelectedPolyline() {
        if(selectedSegment == null) return null;
        return this.map.get(selectedSegment);
    }

    public void setSelectedSegment(Segment segment) {
        if (selectedSegment != null) {
            Polyline selectedPolyline = map.get(selectedSegment);
            if(selectedPolyline != null) {
                Segment seg = null;
                for(Map.Entry<Segment, Polyline> e : map.entrySet()) {
                    if(selectedPolyline == e.getValue()) {
                        seg = e.getKey();
                        break;
                    }
                }

                if(seg == null || seg.getColor() == null)
                    selectedPolyline.setColor(Color.YELLOW);
                else
                    selectedPolyline.setColor(seg.getColor());
                selectedPolyline.setWidth(TRACKLOGWIDTH);
            }
        }
        if(segment != null) {
            this.selectedSegment = segment;
            Polyline pl = map.get(selectedSegment);
            if(pl != null) {
                pl.setColor(Color.RED);
                pl.setWidth(TRACKLOGWIDTH + 4);
            } else
                Log.w("tracklog", "setSelectedSegment: some error fetching track");
        } else {
            this.selectedSegment = null;
        }
    }

    public void setLabel(int i, String label) {
        if(i < 0 || i >= this.tracklog.size()) return;
        this.tracklog.get(i).setTitle(label);
    }

    public String getLabel(int i) {
        if(i < 0 || i >= this.tracklog.size()) return null;
        return this.tracklog.get(i).getTitle();
    }

    public void setColor(int i, int color) {
        if(i < 0 || i >= this.tracklog.size()) return;
        this.map.get(this.tracklog.get(i)).setColor(color);
        this.tracklog.get(i).setColor(color);
    }

    public Integer getColor(int i) {
        if(i < 0 || i >= this.tracklog.size()) return null;
        return this.tracklog.get(i).getColor();
    }

    public boolean cutNearestSegmentAt(IGeoPoint nearPoint, boolean onlySelected) {
        double hyp, minHyp = 10000000;
        try {
            Segment whichSegment = null;
            GeoTimePoint whichPoint = null;
            List<Segment> available;
            if(onlySelected)
                available = Collections.singletonList(selectedSegment);
            else
                available = this.tracklog;
            for (Segment seg : available) {
                for (GeoTimePoint p : seg) {
                    hyp = Math.pow(p.getLatitude() - nearPoint.getLatitude(), 2) + Math.pow(p.getLongitude() - nearPoint.getLongitude(), 2);
                    if (hyp < minHyp) {
                        minHyp = hyp;
                        whichPoint = p;
                        whichSegment = seg;
                    }
                }
            }
            if (whichSegment == null) return false;

            int breakAt = whichSegment.indexOf(whichPoint);
            Segment part1 = new Segment();
            Segment part2 = new Segment();
            Polyline pl;
            part1.addAll(whichSegment.subList(0, breakAt + 1));
            part2.addAll(whichSegment.subList(breakAt, whichSegment.size()));

            deleteTrack(this.tracklog.indexOf(whichSegment));
            this.tracklog.add(part1);
            this.tracklog.add(part2);

            folder.add(pl = createPolylineFromPoints(part1));
            map.put(part1, pl);

            folder.add(pl = createPolylineFromPoints(part2));
            map.put(part2, pl);

            this.setSelectedTrack(null);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    class clickPolyline implements Polyline.OnClickListener {

        @Override
        public boolean onClick(Polyline polyline, MapView mapView, GeoPoint geoPoint) {
            Segment toSelect = null;
            // TODO use a BiMap
            for(Map.Entry<Segment, Polyline> e : map.entrySet()) {
                if(polyline == e.getValue()) {
                    toSelect = e.getKey();
                    break;
                }
            }
            setSelectedSegment(toSelect);
            if(clickListener != null)
                clickListener.onClick(null);
            return true;
        }
    }

    public class Segment extends ArrayList<GeoTimePoint> {
        private String title;
        private Integer color;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

}
