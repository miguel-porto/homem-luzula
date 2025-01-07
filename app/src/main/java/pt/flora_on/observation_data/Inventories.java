package pt.flora_on.observation_data;

import android.widget.Toast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by miguel on 07-10-2016.
 */

public class Inventories implements SimpleFastPointOverlay.PointAdapter {
    transient boolean changed = false;
    protected ArrayList<SpeciesList> speciesLists = new ArrayList<SpeciesList>();
    protected transient final List<IGeoPoint> points = new ArrayList<IGeoPoint>();
    protected transient final List<IGeoPoint> pointsBuffer = new ArrayList<IGeoPoint>();
    private transient OnChangedListener changedListener;
    private transient int maxInventorySerial = 0;

    public interface OnChangedListener {
        void onChange();
    }

    public Inventories() {
    }

    public Inventories(Inventories inv) {
        for(SpeciesList sl : inv.getSpeciesLists()) {
            this.speciesLists.add(new SpeciesList(sl));
        }
    }

    public void setOnChangedListener(OnChangedListener listener) {
        changedListener = listener;
    }

    public int getNextSerial() {
        return maxInventorySerial + 1;
    }

    public void addSpeciesList(SpeciesList sl) {
        changed = true;
        speciesLists.add(sl);
        if(sl.getSerialNumber() != null) {
            if(sl.getSerialNumber() > maxInventorySerial)
                maxInventorySerial = sl.getSerialNumber();
        }
        if (sl.getLatitude() == null || sl.getLongitude() == null)
            points.add(null);
        else {
//            points.add(new GeoPoint(sl.getLatitude(), sl.getLongitude()));
            points.add(new LabelledGeoPoint(sl.getLatitude(), sl.getLongitude(), "".equals(sl.getGpsCode()) ? null : sl.getGpsCode()));
        }
        if(changedListener != null) changedListener.onChange();
    }

    public void addSpeciesListAsync(SpeciesList sl) {
        speciesLists.add(sl);
        if(sl.getSerialNumber() != null) {
            if(sl.getSerialNumber() > maxInventorySerial)
                maxInventorySerial = sl.getSerialNumber();
        }
        if (sl.getLatitude() == null || sl.getLongitude() == null)
            pointsBuffer.add(null);
        else {
//            points.add(new GeoPoint(sl.getLatitude(), sl.getLongitude()));
            pointsBuffer.add(new LabelledGeoPoint(sl.getLatitude(), sl.getLongitude(), "".equals(sl.getGpsCode()) ? null : sl.getGpsCode()));
        }
    }

    public void flush() {
        changed = true;
        points.addAll(pointsBuffer);
        pointsBuffer.clear();
        if(changedListener != null) changedListener.onChange();
    }

    public void replaceSpeciesList(SpeciesList sl, int index) {
        changed = true;
        speciesLists.set(index, sl);
        if(sl.getLatitude() == null || sl.getLongitude() == null)
            points.set(index, null);
        else
//            points.set(index, new GeoPoint(sl.getLatitude(), sl.getLongitude()));
            points.set(index, new LabelledGeoPoint(sl.getLatitude(), sl.getLongitude(), "".equals(sl.getGpsCode()) ? null : sl.getGpsCode()));
        if(changedListener != null) changedListener.onChange();
    }

    public Multimap<Date, SpeciesList> getInventoriesByDate() {
        Multimap<Date, SpeciesList> out = ArrayListMultimap.create();
        GregorianCalendar gc = new GregorianCalendar();
        for(SpeciesList sl : this.speciesLists) {
            gc.set(sl.getYear(), sl.getMonth(), sl.getDay());
            out.put(gc.getTime(), sl);
        }
        return out;
    }
/*
    public void syncronizeSpeciesListsAndPoints() {
        points.clear();
        for(SpeciesList sl : speciesLists) {
            if(sl.getLatitude() == null || sl.getLongitude() == null)
                points.add(null);
            else
//                points.add(new GeoPoint(sl.getLatitude(), sl.getLongitude()));
                points.add(new LabelledGeoPoint(sl.getLatitude(), sl.getLongitude(), "a"));
        }
    }
*/

    public ArrayList<SpeciesList> getSpeciesLists() {
        return speciesLists;
    }

    public void setSpeciesLists(final ArrayList<SpeciesList> list) {
        this.speciesLists = list;
        changed = true;
        if(changedListener != null) changedListener.onChange();
    }

    public SpeciesList getSpeciesList(int i) { return speciesLists.get(i); }

    public void deleteSpeciesList(int index) {
        changed = true;
        speciesLists.remove(index);
        points.remove(index);
        if(changedListener != null) changedListener.onChange();
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
        if(changed && changedListener != null) changedListener.onChange();
    }

    @Override
    public Iterator<IGeoPoint> iterator() {
        return points.iterator();
        //return new InventoriesIterator();
    }

    @Override
    public int size() {
        return speciesLists.size();
    }

    @Override
    public IGeoPoint get(int i) {
        return !points.isEmpty() ? points.get(i) : null;
    }

    @Override
    public boolean isLabelled() {
        return true;
    }

//    @Override
    public boolean isStyled() {
        return false;
    }

    public static boolean saveInventoryToDisk(SpeciesList sList, String uuid) {
        File invdir = new File(System.getenv("EXTERNAL_STORAGE"), "homemluzula");
        if(!invdir.exists()) invdir.mkdir();
        File newinv = new File(invdir, uuid + ".json");
        if(newinv.exists()) newinv.delete();

        try {
            newinv.createNewFile();
            FileWriter fw = new FileWriter(newinv);
            fw.append(new Gson().toJson(sList));
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
