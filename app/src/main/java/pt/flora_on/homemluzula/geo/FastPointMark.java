package pt.flora_on.homemluzula.geo;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import pt.flora_on.homemluzula.LocationFixedCallback;

/**
 * A LocationListener that waits for some accuracy then calls back with the coordinates.
 */
public class FastPointMark implements LocationListener {
    private LocationFixedCallback callback = null;
    private int minPrecision;

    public FastPointMark(int minPrecision) {
        this.minPrecision = minPrecision;
    }

    public void setCallback(LocationFixedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy() < minPrecision) {
            if(this.callback != null)
                this.callback.finished((float) location.getLatitude(), (float) location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
