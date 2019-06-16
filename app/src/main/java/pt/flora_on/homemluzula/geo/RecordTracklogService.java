package pt.flora_on.homemluzula.geo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import pt.flora_on.homemluzula.DataSaver;
import pt.flora_on.homemluzula.MainMap;
import pt.flora_on.homemluzula.R;

public class RecordTracklogService extends Service {
    private Boolean tracklogPrecisionFilter;
    private Integer tracklogMinDist, precisionFilter;
    private int interval;
    private LocationManager locationManager;

    private final LocationListener tracklogListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (MainMap.theMap == null || MainMap.inventoryLayer == null) return;

            //IGeoPoint tmp = new GeoPoint(location.getLatitude()+ (float) Math.random()*0.1f, location.getLongitude() + (float) Math.random()*0.1f);
            MainMap.inventoryLayer.setCurrentLocation(location);
            if(MainMap.recordTracklog) {
                GeoPoint pt2;
                Double dist;
                GeoTimePoint lastRecordedLocation = DataSaver.tracklog.getLastLocation();
                if (lastRecordedLocation != null) {
                    pt2 = new GeoPoint(location);
                    dist = lastRecordedLocation.distanceToAsDouble(pt2);
                    if(dist >= tracklogMinDist && (!tracklogPrecisionFilter || location.getAccuracy() <= precisionFilter)) {
                        DataSaver.tracklog.add(location, dist > 100 | MainMap.breakTrackAtNextFix);
                        if(MainMap.breakTrackAtNextFix)
                            MainMap.breakTrackAtNextFix = false;
                    }
                } else {
                    DataSaver.tracklog.add(location, true);
                }
                //location.getElapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos() > (long) getTracklogInterval() * 6 * 1000000000L

                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            }


            if(MainMap.lockOnCurrentLocation) {
                MainMap.theMap.getController().setCenter(new GeoPoint(location));
                MainMap.mainActivity.findViewById(R.id.mira).setVisibility(View.GONE);
                MainMap.mainActivity.findViewById(R.id.add_location).setVisibility(View.GONE);
                MainMap.mainActivity.findViewById(R.id.view_distance).setVisibility(View.GONE);
            }

            MainMap.lastLocation = new GeoTimePoint(location);
            MainMap.theMap.invalidate();

//            r.play();
/*
            counter++;
            if(counter>10) {
                stopSelf();
            }
*/
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public RecordTracklogService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel androidChannel = new NotificationChannel("pt.floraon.homemluzula",
                "Homem Luzula", NotificationManager.IMPORTANCE_DEFAULT);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(false);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.BLUE);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm != null)
            nm.createNotificationChannel(androidChannel);
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
/*
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            Intent notificationIntent = new Intent(this, MainMap.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = null;
            notification = new Notification.Builder(this, "pt.floraon.homemluzula")
                    .setContentTitle("Recording tracklog")
                    .setSmallIcon(R.drawable.ic_folha)
                    .build();
            startForeground(34, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if(extras != null) {
            interval = extras.getInt("interval");
            tracklogPrecisionFilter = extras.getBoolean("tracklogPrecisionFilter");
            precisionFilter = extras.getInt("precisionFilter");
            tracklogMinDist = extras.getInt("tracklogMinDist");
        }
        if(MainMap.recordTracklog)
            Toast.makeText(this, "Tracklog activado.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "GPS activado.", Toast.LENGTH_SHORT).show();

        // The service is starting, due to a call to startService()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return (int) 0;
        }
        locationManager.removeUpdates(tracklogListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval * 1000, 0, tracklogListener);
        return (int) 1;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(tracklogListener);
        if(MainMap.recordTracklog)
            Toast.makeText(this, "Tracklog desactivado.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "GPS desactivado.", Toast.LENGTH_SHORT).show();
    }

}
