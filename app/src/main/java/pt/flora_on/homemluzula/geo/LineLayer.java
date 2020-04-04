package pt.flora_on.homemluzula.geo;

import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.Serializable;

public class LineLayer extends Tracklog implements Iterable<Tracklog.Segment>, Serializable, Layer {
    private Integer color = Color.YELLOW;
    private float width = 2;
    private String layerName;
    private boolean solidLayer = true;     // true: don't select individual polylines but the whole layer
    private boolean visible = true;

    public LineLayer(FolderOverlay folder) {
        super(folder);
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        for(Polyline pl : this.map.values()) {
            if(pl != null) pl.getOutlinePaint().setStrokeWidth(width);
        }
    }

    public void setOverlay(FolderOverlay ovr) {
        this.folder = ovr;
        ovr.setEnabled(this.visible);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.getOverlay().setEnabled(visible);
    }

    public void setSolidLayer(boolean solidLayer) {
        this.solidLayer = solidLayer;
    }

    @Override
    void setSelectedSegment(Segment segment) {
    }

    @Override
    Polyline createPolylineFromPoints(Segment plist) {
        Polyline pl = new SensitivePolyline();
        pl.setOnClickListener(this.solidLayer ? new clickLayer() : new clickPolyline());
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

    public Integer getColor() {
        return this.color;
    }

    public void setColor(Integer color) {
        this.color = color;
        for(Polyline pl : this.map.values()) {
            if(pl != null) pl.getOutlinePaint().setColor(color);
        }
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
