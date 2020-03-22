package pt.flora_on.homemluzula.geo;

import android.graphics.Color;
import android.graphics.Paint;

import com.google.gson.Gson;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by miguel on 21-10-2016.
 */

public class SimplePointTheme implements SimpleFastPointOverlay.PointAdapter {
    private ArrayList<StyledLabelledGeoPoint> points = new ArrayList<>();
    private transient boolean changed = false;
    private boolean isStyled = false;

    public static SimplePointTheme fromJSON(InputStream inputStream, boolean styled) throws IOException {
        Gson gs = new Gson();
        InputStreamReader ir = new InputStreamReader(inputStream);
        SimplePointTheme data = gs.fromJson(ir, SimplePointTheme.class);
        ir.close();
        data.isStyled = styled;
        return data == null ? new SimplePointTheme() : data;
    }

    public static SimplePointTheme fromXY(InputStream inputStream, boolean styled) throws IOException {
        SimplePointTheme out = new SimplePointTheme();
        BufferedReader ir = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<StyledLabelledGeoPoint> pts = new ArrayList<>();
        String tmp;
        String[] line;
        while((tmp = ir.readLine()) != null) {
            line = tmp.split(",");
            double lat, lng;
            if((lat = Double.parseDouble(line[0])) > -85 && lat < 85 && (lng = Double.parseDouble(line[1])) > -180 && lng < 180)
                pts.add(new StyledLabelledGeoPoint(lat, lng));
        }
        ir.close();
        out.setPointsList(pts);
        out.isStyled = styled;
        return out;
    }

    public static SimplePointTheme fromXYLabelColor(InputStream inputStream) throws IOException {
        SimplePointTheme out = new SimplePointTheme();
        CSVParser ir = CSVFormat.DEFAULT.parse(new InputStreamReader(inputStream));
//        BufferedReader ir = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<StyledLabelledGeoPoint> pts = new ArrayList<>();
        String tmp;
        Integer tmpcol;
        String[] line;
        Paint tmp1 = null, tmp2 = null;
        for(CSVRecord rec : ir) {
            tmp = (rec.size() < 3 || rec.get(2).equals("")) ? null : rec.get(2);
            tmpcol = (rec.size() < 4 || rec.get(3).equals("")) ? null : Integer.parseInt(rec.get(3));

            if(tmpcol != null && tmpcol != Color.parseColor("#ff0000")) {
                tmp2 = new Paint();
                tmp2.setStyle(Paint.Style.FILL);
                tmp2.setTextSize(32);
                tmp2.setTextAlign(Paint.Align.CENTER);
                tmp2.setColor(tmpcol);
            } else tmp2 = null;

            if(tmpcol != null) {
                tmp1 = new Paint();
                tmp1.setStyle(Paint.Style.FILL_AND_STROKE);
                tmp1.setColor(tmpcol);
            } else tmp1 = null;
            pts.add(new StyledLabelledGeoPoint(Double.parseDouble(rec.get(0)), Double.parseDouble(rec.get(1))
                    , tmp, tmp1, tmp2));

        }

/*
        while((tmp = ir.readLine()) != null) {
            line = tmp.split(",", -1);
            if(line.length < 3 || line[2].equals(""))
                pts.add(new StyledLabelledGeoPoint(Double.parseDouble(line[0]), Double.parseDouble(line[1])));
            else if(line.length < 4 || line[3].equals(""))
                pts.add(new StyledLabelledGeoPoint(Double.parseDouble(line[0]), Double.parseDouble(line[1]), line[2]));
            else {
                Paint tmp2 = null;
                if(Integer.parseInt(line[3]) != Color.parseColor("#ff0000")) {
                    tmp2 = new Paint();
                    tmp2.setStyle(Paint.Style.FILL);
                    tmp2.setTextSize(32);
                    tmp2.setTextAlign(Paint.Align.CENTER);
                    tmp2.setColor(Integer.parseInt(line[3]));
                }

                tmp1 = new Paint();
                tmp1.setStyle(Paint.Style.FILL_AND_STROKE);
                tmp1.setColor(Integer.parseInt(line[3]));
                pts.add(new StyledLabelledGeoPoint(Double.parseDouble(line[0]), Double.parseDouble(line[1]), line[2], tmp1, tmp2));
            }
        }
*/
        ir.close();
        out.setPointsList(pts);
        out.isStyled = true;
        return out;
    }

    public static SimplePointTheme randomPoints(int n) {
        SimplePointTheme out = new SimplePointTheme();
        Paint tmp1;
        Paint tmp2;

        for (int i = 0; i < n; i++) {
            tmp1 = new Paint();
            tmp1.setStyle(Paint.Style.FILL);
            tmp1.setColor(Color.rgb((int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255)));

            tmp2 = new Paint();
            tmp2.setTextSize((int) (10 + Math.random() * 30));
            tmp2.setTextAlign(Paint.Align.CENTER);
            tmp2.setColor(Color.rgb((int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255)));
            tmp2.setStyle(Paint.Style.FILL);

            out.points.add(new StyledLabelledGeoPoint(35 + Math.random()*5, -9 + Math.random()*5, "Point", tmp1, tmp2));
//            out.points.add(new LabelledGeoPoint(35 + Math.random()*5, -9 + Math.random()*5, "Point"));
        }
        out.isStyled = true;

//        out.syncronizeGeoPoints();
        return out;
    }

    public void add(StyledLabelledGeoPoint point) {
        points.add(point);
        changed = true;
    }

    public void remove(int i) {
        points.remove(i);
        changed = true;
    }

/*
    public void syncronizeGeoPoints() {
        geoPoints.clear();
        for(LabelledGeoPoint sl : points) {
            geoPoints.add(new LabelledGeoPoint(sl));
        }
    }
*/

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public ArrayList<StyledLabelledGeoPoint> getPointsList() {
        return points;
    }

    public void setPointsList(final ArrayList<StyledLabelledGeoPoint> pointsList) {
        this.points = pointsList;
//        syncronizeGeoPoints();
        changed = true;
    }

    @Override
    public int size() {
        return points.size();
    }

    @Override
    public IGeoPoint get(int i) {
        return points.get(i);
    }

    @Override
    public boolean isLabelled() {
        return true;
    }

//    @Override
    public boolean isStyled() {
        return this.isStyled;
    }

    @Override
    public Iterator<IGeoPoint> iterator() {
        return new IGeoPointIterator(points);
    }

    public class IGeoPointIterator implements Iterator<IGeoPoint> {
        private int i;
        private List<? extends IGeoPoint> mIterable;

        public IGeoPointIterator(List<? extends IGeoPoint> list) {
            this.mIterable = list;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < this.mIterable.size();
        }

        @Override
        public IGeoPoint next() {
            return mIterable.get(i++);
        }

        @Override
        public void remove() {

        }
    }
}
