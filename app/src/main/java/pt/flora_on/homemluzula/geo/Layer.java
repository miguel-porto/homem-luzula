package pt.flora_on.homemluzula.geo;

import android.graphics.Color;
import android.location.Location;
import android.view.View;

import org.osmdroid.views.overlay.FolderOverlay;

import java.io.Serializable;

public abstract class Layer implements Serializable {
    transient protected View.OnClickListener clickListener;
    protected Integer color;
    protected String layerName;
    protected float width;
    transient FolderOverlay folder;
    private boolean visible = true;

    public abstract void add(GeoTimePoint point, boolean breakPath);
    public abstract void add(Location point, boolean breakPath);
    public abstract void refresh();
    public abstract void setWidth(float width);
    public abstract void setColor(Integer color);

    public Layer() {
        if(this.color == null)
            color = Color.YELLOW;
        this.width = 2;
    }

    public Layer(FolderOverlay folder) {
        this();
        this.folder = folder;
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public float getWidth() {
        return this.width;
    };

    public FolderOverlay getOverlay() {
        return this.folder;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.getOverlay().setEnabled(visible);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setOverlay(FolderOverlay ovr) {
        this.folder = ovr;
        ovr.setEnabled(this.visible);
    }

    public Integer getColor() {
        return this.color;
    }

/*
    void setSelectedSegment(Tracklog.Segment segment);
    Polyline createPolylineFromPoints(Tracklog.Segment plist);
*/
}
