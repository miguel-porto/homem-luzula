package pt.flora_on.homemluzula;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.api.IGeoPoint;

/**
 * Created by miguel on 21-10-2016.
 */

public class LabelPoint implements Parcelable {
    private double latitude, longitude;
    private String label;

    public LabelPoint() {
    }

    public LabelPoint(double lat, double lng, String label) {
        this.latitude = lat;
        this.longitude = lng;
        this.label = label;
    }

    public LabelPoint(IGeoPoint point, String label) {
        this(point.getLatitude(), point.getLongitude(), label);
    }

    protected LabelPoint(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        label = in.readString();
    }

    public static final Creator<LabelPoint> CREATOR = new Creator<LabelPoint>() {
        @Override
        public LabelPoint createFromParcel(Parcel in) {
            return new LabelPoint(in);
        }

        @Override
        public LabelPoint[] newArray(int size) {
            return new LabelPoint[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(label);
    }
}
