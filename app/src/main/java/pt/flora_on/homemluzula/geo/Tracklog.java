package pt.flora_on.homemluzula.geo;

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pt.flora_on.homemluzula.HomemLuzulaApp;

public class Tracklog extends LineLayer implements Iterable<Tracklog.Segment>, Serializable {
    private transient Segment selectedSegment;
    private transient Integer TRACKLOGWIDTH;

    public Tracklog() {
        super();
        this.TRACKLOGWIDTH =
                Integer.parseInt(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext()).getString("pref_track_width", "4")));
    }

    public Tracklog(FolderOverlay folder) {
        super(folder);
        this.TRACKLOGWIDTH =
                Integer.parseInt(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext()).getString("pref_track_width", "4")));
    }

    public void clear() {
        tracklog = new ArrayList<>();
    }


    public int size() {
        return tracklog.size();
    }

    @Override
    public String getLayerName() {
        return "Tracklog";
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

    public void refresh() {
        if(this.TRACKLOGWIDTH == null) {
            this.TRACKLOGWIDTH = Integer.parseInt(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext()).getString("pref_track_width", "4")));
        }
        super.refresh();
    }

    @Override
    public void setColor(Integer color) {
        for(int i=0; i < this.tracklog.size(); i++) {
            this.setColor(color, i);
        }
    }

    Polyline createPolylineFromPoints(Segment plist) {
        Polyline pl = new SensitivePolyline();
        pl.setOnClickListener(new clickPolyline());
//            pl.getPaint().setPathEffect(new DashPathEffect(new float[]{6f, 6f}, 0f));

        if(plist == null || plist.getColor() == null)
            pl.getOutlinePaint().setColor(Color.YELLOW);
        else
            pl.getOutlinePaint().setColor(plist.getColor());

        pl.getOutlinePaint().setStrokeWidth(TRACKLOGWIDTH);
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

    void setSelectedSegment(Segment segment) {
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
                    selectedPolyline.getOutlinePaint().setColor(Color.YELLOW);
                else
                    selectedPolyline.getOutlinePaint().setColor(seg.getColor());
                selectedPolyline.getOutlinePaint().setStrokeWidth(TRACKLOGWIDTH);
            }
        }
        if(segment != null) {
            this.selectedSegment = segment;
            Polyline pl = map.get(selectedSegment);
            if(pl != null) {
                pl.getOutlinePaint().setColor(Color.RED);
                pl.getOutlinePaint().setStrokeWidth(TRACKLOGWIDTH + 4);
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
        Polyline pl = this.map.get(this.tracklog.get(i));
        if(pl != null) pl.getOutlinePaint().setColor(color);
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

    public static class Segment extends ArrayList<GeoTimePoint> {
        private String title;
        private Integer color;

        void setTitle(String title) {
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
