package pt.flora_on.homemluzula.geo;

import android.graphics.Paint;
import android.location.Location;
import android.view.View;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class PointLayer extends Layer implements Serializable {
    private ArrayList<GeoPoint> points = new ArrayList<>();
    private transient Paint style = new Paint();

    public PointLayer(FolderOverlay folder) {
        super(folder);
    }

    @Override
    public void add(GeoTimePoint point, boolean breakPath) {
        points.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
    }

    @Override
    public void add(Location point, boolean breakPath) {
        this.add(new GeoTimePoint(point), breakPath);
    }

    @Override
    public void setWidth(float width) {
        this.width = width;
        if(folder != null && folder.getItems().size() > 0)
            ((SimpleFastPointOverlay) folder.getItems().get(0)).getStyle().setRadius(width * 1.5f);
    }

    @Override
    public void refresh() {
        if(folder == null) return;
        folder.getItems().clear();
        if(this.style == null)
            this.style = new Paint();

        style.setStyle(Paint.Style.FILL_AND_STROKE);
        style.setColor(color);
        SimplePointTheme theme = new SimplePointTheme();
        for(GeoPoint p : this.points) {
            StyledLabelledGeoPoint sgp = new StyledLabelledGeoPoint(p);
//            sgp.setPointStyle(style);
            theme.add(sgp);
        }
        folder.add(new SimpleFastPointOverlay(theme, new SimpleFastPointOverlayOptions()
                .setPointStyle(this.style).setRadius(this.width * 1.5f).setIsClickable(true)
                .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)));

    }

    @Override
    public void setOnClickListener(View.OnClickListener clickListener) {
        super.setOnClickListener(clickListener);
        ((SimpleFastPointOverlay) folder.getItems().get(0)).setOnClickListener(new clickLayer());
    }

    class clickLayer implements SimpleFastPointOverlay.OnClickListener {
        @Override
        public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
            if(clickListener != null)
                clickListener.onClick(null);
        }
    }


    @Override
    public void setColor(Integer color) {
        this.color = color;
        if(this.style == null)
            this.style = new Paint();

        style.setStyle(Paint.Style.FILL_AND_STROKE);
        style.setColor(color);
    }

}
