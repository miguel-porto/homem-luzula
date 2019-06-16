package pt.flora_on.homemluzula.geo;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

/**
 * Created by miguel on 18-10-2016.
 */

public class GeoTimePoint extends GeoPoint implements Parcelable {
    private long mTime;

    public GeoTimePoint(Double latitude, Double longitude, long time) {
        super(latitude, longitude);
        mTime = time;
    }

    public GeoTimePoint(Location location) {
        super(location);
        mTime = location.getTime();
    }

    public long getTime() {
        return mTime;
    }

    protected GeoTimePoint(Parcel in) {
        super(in.readDouble(), in.readDouble());
        setAltitude(in.readDouble());
        this.mTime = in.readLong();
    }

    public static final Creator<GeoTimePoint> CREATOR = new Creator<GeoTimePoint>() {
        @Override
        public GeoTimePoint createFromParcel(Parcel in) {
            return new GeoTimePoint(in);
        }

        @Override
        public GeoTimePoint[] newArray(int size) {
            return new GeoTimePoint[size];
        }
    };

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
        dest.writeDouble(getAltitude());
        dest.writeLong(mTime);
    }
}
