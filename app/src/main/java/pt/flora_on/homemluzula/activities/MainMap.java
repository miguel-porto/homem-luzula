package pt.flora_on.homemluzula.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import pt.flora_on.homemluzula.Checklist;
import pt.flora_on.homemluzula.DataManager;
import pt.flora_on.homemluzula.EditTextBackEvent;
import pt.flora_on.homemluzula.HomemLuzulaApp;
import pt.flora_on.homemluzula.LocationFixedCallback;
import pt.flora_on.homemluzula.R;
import pt.flora_on.homemluzula.geo.FastPointMark;
import pt.flora_on.homemluzula.geo.GeoTimePoint;
import pt.flora_on.homemluzula.geo.Layer;
import pt.flora_on.homemluzula.geo.RecordTracklogService;
import pt.flora_on.homemluzula.geo.SimplePointOverlayWithCurrentLocation;
import pt.flora_on.homemluzula.geo.SimplePointTheme;
import pt.flora_on.homemluzula.geo.Tracklog;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.TaxonObservation;

public class MainMap extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int UI_ANIMATION_DELAY = 300;
    public static final int GET_SPECIESLIST = 190;
    public static final int REPLACE_SPECIESLIST = 191;
    public static final int CLEAR_TRACKLOG = 192;
    public static final int EDIT_OBSERVATION = 193;
    public static final int CLEAR_ALLLAYERS = 194;
    public static final int SELECT_SPECIES = 195;
    public static final int UNSAVED_NOTIFICATION = 6774;
    public static final int DASHBOARD = 1;
    public static Checklist checklist;
    public static SimplePointTheme basePointTheme, otherPointTheme, observationTheme;
    public static final Map<String, Integer> frequencies = new HashMap<>();
    public static int phenoFlower, phenoVegetative, phenoResting, phenoFruit, phenoDispersion, phenoBud;
    private final Handler mHideHandler = new Handler();
    private final Handler mHideGPSHandler = new Handler();
    private final Handler mRecordingTracklog = new Handler();
    private final Handler saveTracklogTimer = new Handler();
    private Timer waitLayersLoadedTimer;
    private Integer tracklogMinDist, precisionFilter;
    private Boolean tracklogPrecisionFilter;
    private View mContentView;
    private AlertDialog downloadPrompt;
    private View downloadPromptView;
    static public GeoTimePoint lastLocation;
    private LocationManager locationManager;
    //private VectorMapView map;

    static public MapView theMap;
    private SimpleFastPointOverlay basePointLayer, otherPointLayer;  // this is a static point layer to display underneath
    private SimpleFastPointOverlay POIPointLayer;   // this is an interactive simple point layer (only coordinates and title)
    private SimpleFastPointOverlay searchResultsLayer, searchResultsLayerFill;
    static public SimpleFastPointOverlay inventoryLayer, observationLayer;  // this is the real inventory layer plus observations
    static public SimplePointOverlayWithCurrentLocation currentLocationLayer;  // this is the current position
    protected FolderOverlay trackLogOverlay = new FolderOverlay();
    protected FolderOverlay layersOverlay = new FolderOverlay();

    static public boolean lockOnCurrentLocation = true;
    static public boolean recordTracklog = false;
    static public boolean breakTrackAtNextFix = false;
    static public boolean layersLoaded = false, tracklogsLoaded = false, inventoriesLoaded = false;
    private ExecutorService executor;
    private Future<?> loadDataTask;
    private boolean isGPSOn = false;
    private boolean showCutTrackButton = false;
    static public AppCompatActivity mainActivity;
    private GeoPoint finger1, finger2;
    private long finger1Time;
    private enum BUTTONLAYOUT {CONTINUE_LAST, EDIT_INVENTORY, DELETE_POI, DELETE_TRACK, DELETE_LAYER};
    private SharedPreferences preferences;
    private Intent GPSIntent;
    private final int quickMarkId = 54654;
    private final int quickMarkToolbar = 54653;
    /**
     * Set to true when starting internal activity, so the onStop method does not save anything.
     */
    private boolean internalNavigation;
    private StyledLabelledGeoPoint prevSelectedPoint = null;
    private final Map<BUTTONLAYOUT, String[]> buttonLayouts = new HashMap<>();

    private final LocationListener curPosListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (MainMap.theMap == null || MainMap.inventoryLayer == null) return;
            MainMap.currentLocationLayer.setCurrentLocation(location);
            if(MainMap.lockOnCurrentLocation) {
                MainMap.theMap.getController().setCenter(new GeoPoint(location));
                MainMap.mainActivity.findViewById(R.id.mira).setVisibility(View.GONE);
                MainMap.mainActivity.findViewById(R.id.view_distance).setVisibility(View.GONE);
                MainMap.mainActivity.findViewById(R.id.view_distance).setVisibility(View.GONE);
            }

            MainMap.lastLocation = new GeoTimePoint(location);
            ((TextView) MainMap.mainActivity.findViewById(R.id.view_what)).setText(String.format(Locale.getDefault(), "Alt %.0fm", location.getAltitude()));
            ((MainMap) MainMap.mainActivity).updateDistanceToCenter();
            MainMap.theMap.invalidate();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    /**
     * This is a listener called on onResume, to enable GPS as soon as the user switches the screen
     * on. It only lives for a short time, until the location is precise.
     *//*

    private final LocationListener fastWaypointListener = new LocationListener() {
        private int counter = 0;

        public void onLocationChanged(Location location) {
            if (theMap == null || !checkGPSPermission() || inventoryLayer == null) return;

            if(location.getAccuracy() < 5) counter++;

            inventoryLayer.setCurrentLocation(location);

            if(counter == 1) {
                myZoomToBoundingBox(new BoundingBox(location.getLatitude() + 0.001, location.getLongitude() + 0.001, location.getLatitude() - 0.001, location.getLongitude() - 0.001));
                theMap.invalidate();
            }

            if(counter > 5) {
                locationManager.removeUpdates(this);
                counter = 0;
            }
//            Toast.makeText(MainMap.this, String.format("%f %f: prec %f",location.getLongitude(), location.getLatitude(), location.getAccuracy()), Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if(isGPSEnabled() && checkGPSPermission()) {
                    locationManager.removeUpdates(fastWaypointListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, fastWaypointListener);
                }
            }
        }
    };
*/

/*
    public class DismissUnsavedNotification extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Toast.makeText(context, "AAAA", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent(context, MainMap.class);
            resultIntent.putExtras(intent);
            context.startActivity(resultIntent);
        }
    }
*/

    static public void beep() {
        beep(0);
    }

    static public void beep(int tone) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        switch(tone) {
            case 0:
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,200);
                break;

            case 1:
                toneGen1.startTone(ToneGenerator.TONE_SUP_RINGTONE,100);
                break;
            case 2:
                toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,100);
                break;
        }
    }

    public FolderOverlay getLayersOverlay() {
        return this.layersOverlay;
    }

    private final Runnable mHideGPSRunnable = new Runnable() {
        @Override
        public void run() {
            setGPSVisibility(false);
        }
    };

    private final Runnable hideCutTrackButton = new Runnable() {
        @Override
        public void run() {
            showCutTrackButton = false;
            setButtonLayout(null);
        }
    };

    private final Runnable mHideToolbars = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            if(mContentView != null)
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

/*
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
*/
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mToggleTracklogIcon = new Runnable() {
        @Override
        public void run() {
            ImageButton tb = (ImageButton) findViewById(R.id.show_tracklog);
            if(tb.getTag() == null)
                tb.setTag(1);
            try {
                tb.setTag(((int) tb.getTag()) ^ 2);
                tb.setImageResource(getTracklogIcon());
            } finally {
                mRecordingTracklog.postDelayed(mToggleTracklogIcon, 500);
            }
        }
    };

    private final Runnable saveTracklogRunnable = new Runnable() {
        @Override
        public void run() {
            DataManager.saveTrackLog(null, null);
            int asInt = Integer.parseInt(preferences.getString("pref_autosave_interval", "300000"));
            if(asInt > 0)
                saveTracklogTimer.postDelayed(saveTracklogRunnable, asInt);
        }
    };

    /**
     * Show or hide the tracklog interval buttons
     * @param expand
     */
    private void setGPSVisibility(final boolean expand) {
        final View v1 = findViewById(R.id.gps_seconds);
        if(v1.getAnimation() != null) return;
        //final boolean expanding = v1.getVisibility() != VISIBLE;
        v1.animate()
                .translationY(expand ? 0 : v1.getHeight()).alpha(expand ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if(expand) v1.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if(!expand) v1.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void colorButtonClick(View v) {
        int color = ((ColorDrawable) ((Button) v).getBackground()).getColor();
        if(POIPointLayer.getSelectedPoint() != null) {
            GeoPoint sgp = DataManager.POIPointTheme.getPointsList().get(POIPointLayer.getSelectedPoint());
            int colTxt = (color == Color.parseColor("#ff0000") ? Color.WHITE : color);
            Paint tmp = new Paint();
            Paint tmp1 = new Paint();
            tmp.setStyle(Paint.Style.FILL_AND_STROKE);
            tmp1.setStyle(Paint.Style.FILL);
            tmp1.setTextSize(32);
            tmp1.setTextAlign(Paint.Align.CENTER);

            tmp.setColor(color);
            tmp1.setColor(colTxt);

            if(sgp instanceof StyledLabelledGeoPoint) {
                ((StyledLabelledGeoPoint) sgp).setPointStyle(tmp);
                ((StyledLabelledGeoPoint) sgp).setTextStyle(tmp1);
            }
            theMap.invalidate();
        }

        if(DataManager.tracklog.getSelectedTrack() != null) {
            DataManager.tracklog.setColor(DataManager.tracklog.getSelectedTrack(), color);
            theMap.invalidate();
        }

        if(DataManager.getSelectedLayer() != null && DataManager.layers.get(DataManager.getSelectedLayer()) != null) {
            DataManager.layers.get(DataManager.getSelectedLayer()).setColor(color);
            theMap.invalidate();
        }
    }

    public void openSpeciesList(int index) {
        SpeciesList sl = DataManager.allData.getSpeciesLists().get(index);
        if(sl.isSingleSpecies()) {
            Intent intent = new Intent(this, ObservationDetails.class);
            intent.putExtra("taxon", sl.getTaxa().get(0));
            intent.putExtra("fill_fields", true);
            intent.putExtra("showDelete", true);
            intent.putExtra("index", index);
            startActivityForResult(intent, EDIT_OBSERVATION);
        } else {
            Intent intent = new Intent(MainMap.this, MainKeyboard.class);
            intent.putExtra("specieslist", sl);
            intent.putExtra("index", index);
            internalNavigation = true;
            startActivityForResult(intent, REPLACE_SPECIESLIST);
        }
    }

    private void setRecordTracklog(Boolean value) {
        ImageButton tb1 = (ImageButton) findViewById(R.id.show_tracklog);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        boolean hardOff = false;    // set to true when the user really presses the Off button

        if(tb1.getTag() == null)
            tb1.setTag(1);

        if(value == null) {
            value = !recordTracklog;
/*
            if(tb1.getTag() == null) {
                tb1.setTag(1);
                value = true;
            } else if((((int) tb1.getTag()) & 2) != 0)
                value = false;
            else
                value = true;
*/
        } else hardOff = true;

        if(!value) {    // switched recording off
            recordTracklog = false;
            mRecordingTracklog.removeCallbacks(mToggleTracklogIcon);
            saveTracklogTimer.removeCallbacks(saveTracklogRunnable);
            stopService(GPSIntent);

            tb1.setTag(((int) tb1.getTag()) & 1);

            tb1.setImageResource(getTracklogIcon());
//            Toast.makeText(MainMap.this, "Tracklog desactivado.", Toast.LENGTH_SHORT).show();
            showCutTrackButton = false;
            setButtonLayout(null);
            DataManager.saveTrackLog(null, null);
            Toast.makeText(this.getApplicationContext(), "Saved tracklog", Toast.LENGTH_SHORT).show();
            if(!hardOff) setGPSEnabled(true);
        } else {
            recordTracklog = true;
            mRecordingTracklog.removeCallbacks(mToggleTracklogIcon);
            tb1.setTag(((int) tb1.getTag()) & 3);
            mToggleTracklogIcon.run();
//            Toast.makeText(MainMap.this, "Tracklog activado.", Toast.LENGTH_SHORT).show();
            showCutTrackButton = true;
            inventoryLayer.setSelectedPoint(DataManager.allData.size() - 1);
            DataManager.tracklog.setSelectedTrack(null);
            setButtonLayout(BUTTONLAYOUT.CONTINUE_LAST);
            mRecordingTracklog.postDelayed(hideCutTrackButton, 10000);

            int asInt = Integer.parseInt(preferences.getString("pref_autosave_interval", "300000"));
            if(asInt > 0) saveTracklogTimer.postDelayed(saveTracklogRunnable, asInt);

        }

        if(v != null) {
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(recordTracklog)
                    v.vibrate(VibrationEffect.createWaveform(new long[]{0, 80, 50, 80, 50, 80}, -1));
                else
                    v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(200);
            }
        }
    }

    private void setGPSEnabled(boolean value) {
        final ImageButton tb = (ImageButton) findViewById(R.id.toggleGPS);

        if(value) {
//            if(isGPSOn) return;
            tracklogMinDist = Integer.parseInt(preferences.getString("pref_gps_mindist", "4"));
            tracklogPrecisionFilter = preferences.getBoolean("pref_gps_filter", true);
            precisionFilter = Integer.parseInt(preferences.getString("pref_minprecision", "10"));

            if (getTracklogInterval() == 0)
                ((RadioButton) findViewById(R.id.log1s)).setChecked(true);

            if (!isGPSEnabled()) {
                internalNavigation = true;
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            switchOnLocationUpdates(getTracklogInterval());
            tb.setImageResource(R.drawable.ic_gps_fixed_green_24dp);
            tb.setTag(true);
        } else {
            stopService(GPSIntent);
            locationManager.removeUpdates(curPosListener);
            isGPSOn = false;
            saveTracklogTimer.removeCallbacks(saveTracklogRunnable);
            tb.setImageResource(R.drawable.ic_gps_fixed_white_24dp);
            tb.setTag(false);
        }
    }

    private void switchOnLocationUpdates(int interval) {
        if(checkGPSPermission()) {
            if(recordTracklog) {
                GPSIntent.putExtra("interval", interval);
                GPSIntent.putExtra("tracklogPrecisionFilter", tracklogPrecisionFilter);
                GPSIntent.putExtra("precisionFilter", precisionFilter);
                GPSIntent.putExtra("tracklogMinDist", tracklogMinDist);

                stopService(GPSIntent);
                locationManager.removeUpdates(curPosListener);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(GPSIntent);
                } else {
                    startService(GPSIntent);
                }
            } else {
                locationManager.removeUpdates(curPosListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval * 1000L, 0, curPosListener);
            }
            isGPSOn = true;
        }
    }

    public boolean checkGPSPermission () {
        return
            locationManager != null && ContextCompat.checkSelfPermission(MainMap.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Count the frequency of each species in the data
     */
    public void refreshFrequencies() {
        if(!inventoriesLoaded) return;
        int count;
        String tName;
        frequencies.clear();

        for(SpeciesList sl : DataManager.allData.getSpeciesLists()) {
            for(TaxonObservation t : sl.getTaxa()) {
                tName = t.getTaxon().toLowerCase();
                count = frequencies.containsKey(tName) ? frequencies.get(tName) : 0;
                frequencies.put(tName, count + 1);
            }
        }

        checklist.resetSpeciesFrequencies();
        checklist.setSpeciesFrequencies(frequencies);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                theMap.getController().zoomOut();
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                theMap.getController().zoomIn();
                return true;

            case KeyEvent.KEYCODE_BACK:     // quit app!
                if(checkGPSPermission()) {
                    isGPSOn = false;
                    stopService(GPSIntent);
                    locationManager.removeUpdates(curPosListener);
                    saveTracklogTimer.removeCallbacks(saveTracklogRunnable);
//                    locationManager.removeUpdates(tracklogListener);
//                    locationManager.removeUpdates(fastWaypointListener);
                }
                Intent saveIntent = new Intent(MainMap.this, DataManager.class);
                if(DataManager.POIPointTheme != null) {
                    saveIntent.putExtra("changed", DataManager.POIPointTheme.isChanged());
                    startActivity(saveIntent);
                }
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void displayLayers() {
        for(Layer tl : DataManager.layers) {
            FolderOverlay fo = new FolderOverlay();
            layersOverlay.add(fo);

            tl.setOverlay(fo);
            tl.refresh();

            tl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                            Toast.makeText(MainMap.this, "layers", Toast.LENGTH_SHORT).show();
                    POIPointLayer.setSelectedPoint(null);
                    inventoryLayer.setSelectedPoint(null);
                    DataManager.tracklog.setSelectedTrack(null);
                    DataManager.setSelectedLayer(DataManager.layers.indexOf(tl));
                    theMap.invalidate();
                    ((EditText) findViewById(R.id.POILabel)).setText(
                            DataManager.layers.get(DataManager.getSelectedLayer()).getLayerName()
                    );
                    ((SeekBar) findViewById(R.id.trackWidth)).setProgress((int) (DataManager.layers.get(DataManager.getSelectedLayer()).getWidth() * 10));
                    showLayerEditBox();
                }
            });

        }
        theMap.invalidate();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initializeApp() {
        DataManager.allData = new Inventories();
        observationTheme = new SimplePointTheme();
        File extStoreDir = Environment.getExternalStorageDirectory();
        File invDir = new File(extStoreDir, "homemluzula");
        Gson gs = new Gson();

        ((TextView) findViewById(R.id.zoomlevel)).setText(R.string.tracklog);
        // READ ALL DATA ASYNC
        class DaemonThreadFactory implements ThreadFactory {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        }
        executor = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
//        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        loadDataTask = executor.submit(new Runnable() {
            @Override
            public void run() {
                // Read tracklog
                FileInputStream fin;
                File file = new File(invDir, "tracklog.bin");
                if(!file.exists())
                    DataManager.tracklog = new Tracklog(trackLogOverlay);
                else {
                    tracklogsLoaded = false;
                    try {
                        fin = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fin);
                        DataManager.tracklog = (Tracklog) ois.readObject();
                        fin.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(Thread.currentThread().isInterrupted()) return;

                    if(DataManager.tracklog == null) DataManager.tracklog = new Tracklog(trackLogOverlay);
                    DataManager.tracklog.setOverlay(trackLogOverlay);
                    DataManager.tracklog.refresh();

                    DataManager.tracklog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            POIPointLayer.setSelectedPoint(null);
                            inventoryLayer.setSelectedPoint(null);
                            DataManager.setSelectedLayer(null);
                            theMap.invalidate();
                            ((EditText) findViewById(R.id.POILabel)).setText(
                                    DataManager.tracklog.getLabel(DataManager.tracklog.getSelectedTrack())
                            );
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                            long t = DataManager.tracklog.getSelectedSegment().get(0).getTime();
                            ((TextView) findViewById(R.id.tr_start)).setText(t == 0 ? "-" : df.format(t));
                            t = DataManager.tracklog.getSelectedSegment().get(DataManager.tracklog.getSelectedSegment().size() - 1).getTime();
                            ((TextView) findViewById(R.id.tr_end)).setText(t == 0 ? "-" : df.format(t));
                            ((TextView) findViewById(R.id.tracklog_length)).setText(formatDistance(
                                    DataManager.tracklog.getLength(DataManager.tracklog.getSelectedTrack())));
                            showTrackEditBox();
                        }
                    });
                }
                tracklogsLoaded = true;
                if(Thread.currentThread().isInterrupted()) return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Loaded tracklogs.", Toast.LENGTH_SHORT).show();
                        ((TextView) findViewById(R.id.zoomlevel)).setText(String.format(getString(R.string.inventories_counter), 0));
                    }
                });

                // READ ALL INVENTORIES async
                // Read from directory
                int counter = 0;
                FileReader data;
                SpeciesList sltmp;
                final int numberOfFiles = invDir.list().length;
                if(invDir.exists()) {
                    inventoriesLoaded = false;
                    DataManager.allData.setSpeciesLists(new ArrayList<SpeciesList>());
                    for (File inv : Objects.requireNonNull(invDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                            return s.endsWith(".json");
                        }
                    }))) {
                        if(Thread.currentThread().isInterrupted()) return;
                        try {
                            data = new FileReader(inv);
                            sltmp = gs.fromJson(data, SpeciesList.class);
                            if(sltmp == null) {
                                Log.e("HZ", "Inventory error: " + inv.getName());
                            } else {
                                DataManager.allData.addSpeciesListAsync(sltmp);
                                for(TaxonObservation to : sltmp.getTaxa()) {
                                    if(to.hasObservationCoordinates()) {
                                        observationTheme.add(new StyledLabelledGeoPoint(to.getObservationLatitude(), to.getObservationLongitude()));

                                    }
                                }
                            }
                            data.close();
                            counter++;
                            if(counter % 100 == 0) {
                                int finalCounter = counter;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) findViewById(R.id.zoomlevel)).setText(String.format(getString(R.string.inventories_counter), (int)((float) finalCounter / numberOfFiles * 100)));
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    DataManager.allData.flush();
                    inventoriesLoaded = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Loaded all inventories.", Toast.LENGTH_SHORT).show();
                            ((TextView) findViewById(R.id.zoomlevel)).setText(R.string.layers);
                        }
                    });
                }
                if(Thread.currentThread().isInterrupted()) return;
                theMap.invalidate();

                // Read layers
                file = new File(invDir, "layers.bin");
                if(!file.exists())
                    DataManager.layers = new ArrayList<>();
                else {
                    layersLoaded = false;
                    try {
                        fin = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fin);
                        DataManager.layers = (ArrayList<Layer>) ois.readObject();
                        fin.close();
                    } catch (IOException | ClassNotFoundException e) {
                        Log.e("LOADLAYERS", e.getMessage());
                        e.printStackTrace();
                    }
                    if(Thread.currentThread().isInterrupted()) return;

                    if(DataManager.layers == null) {
                        DataManager.layers = new ArrayList<>();
                    }

//                Toast.makeText(MainMap.this, DataSaver.layers.size() + " layers", Toast.LENGTH_SHORT).show();
                    layersLoaded = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Loaded all data.", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.loading_status).setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.zoomlevel)).setText("");
                        }
                    });

                }
                MainMap.beep(2);
            }
        });


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        GPSIntent = new Intent(this, RecordTracklogService.class);
        mainActivity = MainMap.this;

        // set default options if not set
        checklist.setNFirst(Integer.parseInt(preferences.getString("pref_ngenusletters", "1")));
        checklist.setNLast(Integer.parseInt(preferences.getString("pref_nspeciesletters", "3")));

        View bar = findViewById(R.id.edit_label_box);
        if(bar == null) {
            getLayoutInflater().inflate(R.layout.edit_properties, findViewById(R.id.topbar));

        }
        createQuickAccessButtons();
        updateStatusBar();
        refreshFrequencies();

        /**
         * New inventory in the target location (selected POI)
         */
        View.OnClickListener fastMark = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(POIPointLayer.getSelectedPoint() == null) return;
                // fetch selected POI coordinates
                IGeoPoint center = DataManager.POIPointTheme.get(POIPointLayer.getSelectedPoint());

                // delete selected POI to replace with inventory
                ((Button) findViewById(R.id.bottombutton_1)).performClick();

                POIPointLayer.setSelectedPoint(null);

                // fast mark the point without species
//                IGeoPoint center = theMap.getProjection().fromPixels(theMap.getWidth() / 2, theMap.getHeight() / 2);
                Intent intent = new Intent(MainMap.this, MainKeyboard.class);
                intent.putExtra("latitude", center.getLatitude());
                intent.putExtra("longitude", center.getLongitude());
                intent.putExtra("dontMarkTaxa", true);
                startActivityForResult(intent, GET_SPECIESLIST);

/*
                SpeciesList sList = new SpeciesList();
                sList.setLocation((float) center.getLatitude(), (float) center.getLongitude());
                sList.setNow();
                allData.addSpeciesList(sList);
                inventoryLayer.setSelectedPoint(allData.getSpeciesLists().size() - 1);
                theMap.invalidate();
                ((Button) findViewById(R.id.edit_point)).setText("Continuar\nponto anterior");
                updateStatusBar();
*/
            }
        };

        View.OnClickListener fastPOI = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fast mark the point
                IGeoPoint center = theMap.getProjection().fromPixels(theMap.getWidth() / 2, theMap.getHeight() / 2);
                DataManager.POIPointTheme.add(new StyledLabelledGeoPoint(center.getLatitude(), center.getLongitude(), null));
                DataManager.POIPointTheme.setChanged(true);
                DataManager.savePOIs();
                POIPointLayer.setSelectedPoint(DataManager.POIPointTheme.size() > 0 ? DataManager.POIPointTheme.size() - 1 : null);
                inventoryLayer.setSelectedPoint(null);
                DataManager.tracklog.setSelectedTrack(null);
                DataManager.setSelectedLayer(null);
                showPOIEditBox();
                ((EditText) findViewById(R.id.POILabel)).setText("");
                theMap.invalidate();
            }
        };

//        findViewById(R.id.add_location).setOnClickListener(fastMark);
        findViewById(R.id.button_make_inventory).setOnClickListener(fastMark);
        findViewById(R.id.mira).setOnClickListener(fastPOI);
//        findViewById(R.id.download_tiles).setOnClickListener(this);

        // New inventory from GPS
        findViewById(R.id.bottombutton_3).setOnClickListener(this);
        findViewById(R.id.show_dashboard).setOnClickListener(this);
        findViewById(R.id.bottombutton_2).setOnClickListener(this);
        // Edit selected point (or delete selected POI)
        findViewById(R.id.bottombutton_1).setOnClickListener(this);

/*
        // Toggle base waypoint layers visibility
        findViewById(R.id.show_layers).setOnClickListener(this);
*/

        // Toggle inventory layer visibility
        findViewById(R.id.show_inventories).setOnClickListener(this);
        findViewById(R.id.show_POI).setOnClickListener(this);
        findViewById(R.id.search_taxon).setOnClickListener(this);
        findViewById(R.id.show_tracklog).setOnClickListener(this);
        findViewById(R.id.show_veclayers).setOnClickListener(this);

        // Toggle GPS
        findViewById(R.id.toggleGPS).setOnClickListener(this);

        // Toggle record tracklog
        findViewById(R.id.show_tracklog).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setRecordTracklog(null);
                if(recordTracklog)
                    setGPSEnabled(true);

                return true;
            }
        });

        // Tracklog interval buttons
        View.OnClickListener logtimelist = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getTracklogInterval() == 0) {    // switch it off
                    if(checkGPSPermission()) {
                        if(recordTracklog)
                            setRecordTracklog(false);

                        setGPSEnabled(false);
                        if (currentLocationLayer != null)
                            currentLocationLayer.setCurrentLocation((Location) null);
                        theMap.invalidate();
//                        final ImageButton tb = (ImageButton) findViewById(R.id.toggleGPS);
                    }
                } else
                    switchOnLocationUpdates(getTracklogInterval());
            }
        };

        ViewGroup gps = (ViewGroup) findViewById(R.id.gps_seconds_group);
        for (int i = 0; i < gps.getChildCount(); i++) {
            gps.getChildAt(i).setOnClickListener(logtimelist);
        }

        // go fullscreen when the keyboard is hidden
        ((EditTextBackEvent) findViewById(R.id.POILabel)).setOnEditTextImeBackListener((ctrl, text) -> {
            findViewById(R.id.edit_label_box).setVisibility(View.GONE);
            mHideToolbars.run();
        });

        // update POI label when keyboard done
        ((EditText) findViewById(R.id.POILabel)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    String label = ((EditText) findViewById(R.id.POILabel)).getText().toString();
                    if(POIPointLayer.getSelectedPoint() != null) {
                        ((LabelledGeoPoint) DataManager.POIPointTheme.get(POIPointLayer.getSelectedPoint()))
                                .setLabel(label);
                        DataManager.POIPointTheme.setChanged(true);
                        DataManager.savePOIs();
                    } else if(DataManager.tracklog.getSelectedTrack() != null) {
                        DataManager.tracklog.setLabel(DataManager.tracklog.getSelectedTrack(), label);
                    } else if(DataManager.getSelectedLayer() != null && DataManager.layers.get(DataManager.getSelectedLayer()) != null) {
                        DataManager.layers.get(DataManager.getSelectedLayer()).setLayerName(label);
                    }
                    findViewById(R.id.edit_label_box).setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null)
                        imm.hideSoftInputFromWindow(findViewById(R.id.POILabel).getWindowToken(), 0);
                    mHideToolbars.run();
                    theMap.invalidate();
                    return true;
                }
                return false;
            }
        });

        ((SeekBar) findViewById(R.id.trackWidth)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                DataManager.layers.get(DataManager.getSelectedLayer()).setWidth((float) i / 10);
                theMap.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if(!MainMap.checklist.getErrors().isEmpty()) {
            new AlertDialog.Builder(MainMap.this)
                    .setCancelable(false)
                    .setTitle("Errors reading the checklist:")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setItems(MainMap.checklist.getErrors().toArray(new String[0]), null)
                    .setPositiveButton("Ok, no problem", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mHideToolbars.run();
                        }
                    })
                    .show();
        }

/*
        map = (VectorMapView) findViewById(R.id.themap);
        List<PointF> points = new ArrayList<>();
        for (SpeciesList sl : allData.getSpeciesLists()) {
            if (sl.getLongitude() != null && sl.getLatitude() != null)
                points.add(new PointF(sl.getLongitude(), sl.getLatitude()));
        }

        map.setPoints(points);
        map.setSelectedPoint(points.size() - 1);
        map.fitToScreen();
*/
        /**
         * Create the map. NOTE: this has to be dynamic because if the permissions are not set for
         * the first time, the tiles don't appear
         */
//        theMap = new MapView(this);
//        theMap.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.setBingKey("ApA5M01feDGVQr602GErbpwgwdlw3yvslROMtFrhPVyKab84vo2YKudRNNJgOl_v");
        org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource bing = new org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource(null);
        bing.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);

        theMap = findViewById(R.id.map);
        theMap.setMultiTouchControls(true);
        theMap.setFlingEnabled(false);
/*
        theMap.setTilesScaledToDpi(true);
        theMap.setTilesScaleFactor(0.5f);
*/
        theMap.setTileSource(bing);

        theMap.setMaxZoomLevel(19.9);
        theMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 100);

/*
        MyLocationNewOverlay mloc = new MyLocationNewOverlay(theMap);
        mloc.setDirectionArrow(null
                , Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.seta2), 100, 100, false));
        mloc.enableMyLocation();
        theMap.getOverlays().add(mloc);
*/

/*
    final List<MapTileListComputer> computers = theMap.getTileProvider().getTileCache().getProtectedTileComputers();
    computers.clear();
    computers.add(new MapTileListZoomComputer(1)); // equivalent of displayed tiles at zoom +1
    computers.add(new MapTileListZoomComputer(-1)); // equivalent of displayed tiles at zoom -1
        computers.add(new MapTileListZoomComputer(2)); // equivalent of displayed tiles at zoom +1
        computers.add(new MapTileListZoomComputer(3)); // equivalent of displayed tiles at zoom +1
    computers.add(new MapTileListBorderComputer(1, false)); // 1-tile-wide border of displayed tiles
*/
/*
    final MapTileList protect = new MapTileList(); // fixed list of tiles to protect. "fixed" meaning "not linked to displayed tiles"
    protect.put(0); // whole world in zoom 0
    protect.put(1); // whole world in zoom 1
    protect.put(2); // whole world in zoom 2
    protect.put(3, 4, 7, 4, 7); // north-east side of the world in zoom 3
    computers.add(new MapTileListComputer() {
        @Override
        public MapTileList computeFromSource(MapTileList pSource, MapTileList pReuse) {
            return protect;
        }
    });
*/
        //this.getResources().getDisplayMetrics().density = 2;

        //this.getResources().getDisplayMetrics().setToDefaults();

        theMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    finger1 = (GeoPoint) theMap.getProjection().fromPixels((int) event.getX(0), (int) event.getY(0));
                    finger1Time = event.getEventTime();
                }

                if(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN && event.getEventTime() - finger1Time > 150) {
                    finger2 = (GeoPoint) theMap.getProjection().fromPixels((int) event.getX(1), (int) event.getY(1));
                    currentLocationLayer.setDistanceLine(finger1, finger2);
                    theMap.invalidate();
                    theMap.requestLayout();
//                    Log.i("PC", "onTouch: "+event.getPointerCount()+" "+event.getPointerId(0));
                }


/*
                if(event.getPointerCount() == 2) {
                    // measure distance between the two fingers
                    ((TextView) findViewById(R.id.view_distance)).setText(formatDistance(
                            finger1.distanceTo(finger2)
                    ));
                }
*/
                if(lockOnCurrentLocation) {
                    lockOnCurrentLocation = false;
                    findViewById(R.id.mira).setVisibility(View.VISIBLE);
//                    findViewById(R.id.add_location).setVisibility(View.VISIBLE);
                    findViewById(R.id.view_distance).setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        theMap.addMapListener(new MapListener() {
            private void updateCoordinates() {
                GeoPoint center = updateDistanceToCenter();
                ((TextView) findViewById(R.id.view_latitude)).setText(String.format(Locale.getDefault(), "%.5f", center.getLatitude()));
                ((TextView) findViewById(R.id.view_longitude)).setText(String.format(Locale.getDefault(), "%.5f", center.getLongitude()));
//                ((TextView) findViewById(R.id.zoomlevel)).setText(String.format(Locale.getDefault(), "%.1f", theMap.getZoomLevelDouble()));
                findViewById(R.id.edit_label_box).setVisibility(View.GONE);
            }

            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                updateCoordinates();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                updateCoordinates();
                return true;
            }
        });

//        ((FrameLayout) findViewById(R.id.mainmap)).addView(theMap, 0);

        Paint tmp1 = new Paint();
        Paint tmp2 = new Paint();
        // add base theme
        tmp1.setStyle(Paint.Style.FILL);
        tmp1.setColor(Color.parseColor("#ff7700"));

        theMap.getOverlays().add(trackLogOverlay);
        theMap.getOverlays().add(layersOverlay);
        // Now we can wait until all layers are loaded
        waitLayersLoadedTimer = new Timer(false);
        waitLayersLoadedTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(layersLoaded) {
                    waitLayersLoadedTimer.cancel();
                    displayLayers();
                } else {
//                    beep(1);
                }

            }
        }, 1000, 1000);

        SimpleFastPointOverlayOptions opt;
        opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setPointStyle(tmp1)
                .setRadius(5f)
                .setCellSize(15)
                .setIsClickable(false)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION);
        basePointLayer = new SimpleFastPointOverlay(basePointTheme, opt);

        tmp1 = new Paint();
        tmp1.setStyle(Paint.Style.FILL);
        tmp1.setColor(Color.argb(255,255, 0, 255));
        opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setPointStyle(tmp1)
                .setRadius(3f)
                .setCellSize(15)
                .setIsClickable(false)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION);
        opt.setPointStyle(tmp1);
        otherPointLayer = new SimpleFastPointOverlay(otherPointTheme, opt);

        theMap.getOverlays().add(otherPointLayer);
        theMap.getOverlays().add(basePointLayer);

        // Add POI theme
        tmp1 = new Paint();
        tmp1.setColor(Color.parseColor("#ff0000"));

        tmp2 = new Paint();
        tmp2.setTextSize(32);
        tmp2.setTextAlign(Paint.Align.CENTER);
        tmp2.setColor(Color.WHITE);
        tmp2.setStyle(Paint.Style.FILL);

        Float rad = Float.parseFloat(preferences.getString("pref_poi_size", "8"));
        opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setPointStyle(tmp1)
                .setRadius(rad).setSelectedRadius(rad + 2)
                .setCellSize(10).setTextStyle(tmp2)
                .setSymbol(SimpleFastPointOverlayOptions.Shape.SQUARE)
                .setMinZoomShowLabels(6)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MEDIUM_OPTIMIZATION);
        POIPointLayer = new SimpleFastPointOverlay(DataManager.POIPointTheme, opt);
        POIPointLayer.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer integer) {
                StyledLabelledGeoPoint sgp = (StyledLabelledGeoPoint) points.get(integer);
                inventoryLayer.setSelectedPoint(null);
                DataManager.tracklog.setSelectedTrack(null);
                DataManager.setSelectedLayer(null);
                showPOIEditBox();
                ((EditText) findViewById(R.id.POILabel)).setText(((LabelledGeoPoint) points.get(integer)).getLabel());
                prevSelectedPoint = sgp;
            }
        });
        theMap.getOverlays().add(POIPointLayer);

        // add inventories
        tmp1 = new Paint();
        tmp1.setStyle(Paint.Style.FILL_AND_STROKE);
        tmp1.setColor(Color.parseColor("#00ff00"));

        tmp2 = new Paint();
        tmp2.setTextSize(32);
        tmp2.setTextAlign(Paint.Align.CENTER);
        tmp2.setColor(Color.WHITE);
        tmp2.setStyle(Paint.Style.FILL);

        opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setPointStyle(tmp1).setTextStyle(tmp2)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MEDIUM_OPTIMIZATION)
                .setRadius(8f);
        inventoryLayer = new SimpleFastPointOverlay(DataManager.allData, opt);
        currentLocationLayer = new SimplePointOverlayWithCurrentLocation(new Inventories(), opt);
        observationLayer = new SimpleFastPointOverlay(observationTheme,
                new SimpleFastPointOverlayOptions().setRadius(3).setSymbol(SimpleFastPointOverlayOptions.Shape.SQUARE)
                        .setPointStyle(tmp1).setIsClickable(false));

        if(DataManager.allData.size() > 0) inventoryLayer.setSelectedPoint(DataManager.allData.size() - 1);
//        inventoryLayer.setTracklogObject(DataSaver.tracklog);
        currentLocationLayer.setYouAreHereDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.seta2, null));
        inventoryLayer.setOnClickListener(new SimplePointOverlayWithCurrentLocation.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                if(!inventoryLayer.isEnabled()) return;
                POIPointLayer.setSelectedPoint(null);
                DataManager.tracklog.setSelectedTrack(null);
                DataManager.setSelectedLayer(null);
                findViewById(R.id.edit_label_box).setVisibility(View.GONE);
                setButtonLayout(BUTTONLAYOUT.EDIT_INVENTORY, new Float[] {DataManager.allData.getSpeciesLists().get(point).getLatitude()
                   , DataManager.allData.getSpeciesLists().get(point).getLongitude()});
            }
        });
        theMap.getOverlays().add(inventoryLayer);
        theMap.getOverlays().add(currentLocationLayer);
        theMap.getOverlays().add(observationLayer);

/*
final Gson gs1 = new GsonBuilder().setPrettyPrinting().create();
try {
    final File tmp2 = new File(extStore + "/basetheme.json");
    FileWriter fw = new FileWriter(tmp2, false);
    gs.toJson(basePointTheme, fw);
    fw.close();
} catch (IOException e) {
    e.printStackTrace();
}
*/


        theMap.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View view, int i, int i1, int i2, int i3) {
//                theMap.zoomToBoundingBox(new BoundingBox(42.2f, -6f, 36.8f, -10f), false);
                myZoomToBoundingBox(new BoundingBox(42.2f, -6f, 36.8f, -10f));
/*
                if(sfpo.getBoundingBox() != null)
                    theMap.zoomToBoundingBox(sfpo.getBoundingBox(), false);
                else
                    theMap.zoomToBoundingBox(new BoundingBox(42.2, -6, 36.8, -10), false);
*/
            }
        });
        theMap.setVisibility(View.VISIBLE);

        findViewById(R.id.loader).setVisibility(View.GONE);
        findViewById(R.id.main_map_interface).setVisibility(View.VISIBLE);

    }

    public GeoPoint updateDistanceToCenter() {
        GeoPoint center = (GeoPoint) theMap.getProjection().fromPixels(theMap.getWidth() / 2, theMap.getHeight() / 2);
        updateDistanceToPoint(center);
        return center;
    }

    private void updateDistanceToPoint(GeoPoint center) {
        if(lastLocation != null) {
            ((TextView) findViewById(R.id.view_distance)).setText(formatDistance(
                    center.distanceToAsDouble(new GeoPoint(lastLocation))
            ));
        }
    }

    private int getTracklogIcon() {
        ImageButton tb = (ImageButton) findViewById(R.id.show_tracklog);

        if((((int) tb.getTag()) & 1) != 0) {
            if((((int) tb.getTag()) & 2) != 0)
                return R.drawable.ic_tracklog_rec;
            else
                return R.drawable.ic_tracklog1;
        } else {
            if((((int) tb.getTag()) & 2) != 0)
                return R.drawable.ic_tracklog0_rec;
            else
                return R.drawable.ic_tracklog0;
        }

    }

    private void showPOIEditBox() {
        setButtonLayout(BUTTONLAYOUT.DELETE_POI);
        ((EditText) findViewById(R.id.POILabel)).setHint("título do ponto");
        findViewById(R.id.edit_label_box).setVisibility(View.VISIBLE);
        findViewById(R.id.button_make_inventory).setVisibility(View.VISIBLE);
        findViewById(R.id.tracklog_times).setVisibility(View.GONE);
        findViewById(R.id.trackWidthWidget).setVisibility(View.GONE);
    }

    private void showTrackEditBox() {
        setButtonLayout(BUTTONLAYOUT.DELETE_TRACK);
        ((EditText) findViewById(R.id.POILabel)).setHint("título da track");
        findViewById(R.id.tracklog_times).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_label_box).setVisibility(View.VISIBLE);
        findViewById(R.id.button_make_inventory).setVisibility(View.GONE);
        findViewById(R.id.trackWidthWidget).setVisibility(View.GONE);
    }

    private void showLayerEditBox() {
        setButtonLayout(BUTTONLAYOUT.DELETE_LAYER);
        ((EditText) findViewById(R.id.POILabel)).setHint("título da layer");
        findViewById(R.id.tracklog_times).setVisibility(View.INVISIBLE);
        findViewById(R.id.edit_label_box).setVisibility(View.VISIBLE);
        findViewById(R.id.button_make_inventory).setVisibility(View.GONE);
        findViewById(R.id.trackWidthWidget).setVisibility(View.VISIBLE);
    }

    private void setButtonLayout(BUTTONLAYOUT layout) {
        setButtonLayout(layout, null);
    }

    private void setButtonLayout(BUTTONLAYOUT layout, Object o) {
        if(layout != null) {
            String[] buttonNames = buttonLayouts.get(layout);
            if (layout == BUTTONLAYOUT.EDIT_INVENTORY) {
                Float[] tmp = (Float[]) o;
                ((Button) findViewById(R.id.bottombutton_1)).setText(
                        String.format(Locale.getDefault(), buttonNames[0], tmp[0], tmp[1]));
            } else
                ((Button) findViewById(R.id.bottombutton_1)).setText(buttonNames[0]);

            if(buttonNames.length > 1)
                ((Button) findViewById(R.id.bottombutton_2)).setText(buttonNames[1]);
            if(buttonNames.length > 2)
                ((Button) findViewById(R.id.bottombutton_3)).setText(buttonNames[2]);
            findViewById(R.id.bottombuttons).setTag(layout);
        }
//        if(buttonLayouts.get(layout)[1] == null)
        if(showCutTrackButton || layout == BUTTONLAYOUT.DELETE_TRACK)
            findViewById(R.id.bottombutton_2).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.bottombutton_2).setVisibility(View.GONE);

    }

    private BUTTONLAYOUT getButtonLayout() {
        if(findViewById(R.id.bottombuttons).getTag() == null)
            return BUTTONLAYOUT.CONTINUE_LAST;
        else
            return (BUTTONLAYOUT) findViewById(R.id.bottombuttons).getTag();
    }

    /**
     * I need permissions man!
     */
    private void noPermissionsMan() {
        findViewById(R.id.loader).setVisibility(View.GONE);
        findViewById(R.id.toggleGPS).setVisibility(View.INVISIBLE);
        findViewById(R.id.bottombutton_1).setVisibility(View.INVISIBLE);
        findViewById(R.id.bottombutton_2).setVisibility(View.INVISIBLE);
        findViewById(R.id.bottombutton_3).setVisibility(View.INVISIBLE);
        findViewById(R.id.show_dashboard).setVisibility(View.INVISIBLE);
        findViewById(R.id.gps_seconds).setVisibility(View.INVISIBLE);
        findViewById(R.id.nopermissions).setVisibility(View.VISIBLE);
        findViewById(R.id.main_map_interface).setVisibility(View.VISIBLE);
    }

    /**
     * Code from http://stackoverflow.com/questions/34577385/osmdroid5-cant-get-zoomtoboundingbox-working-correctly
     */
    private void zoomTo(MapView mapView, IGeoPoint min, IGeoPoint max) {
        MapTileProviderBase tileProvider = mapView.getTileProvider();
        IMapController controller = mapView.getController();
        IGeoPoint center = new GeoPoint((max.getLatitude() + min.getLatitude()) / 2, (max.getLongitude() + min.getLongitude()) / 2);

        // diagonale in pixels
        double pixels = Math.sqrt((mapView.getWidth() * mapView.getWidth()) + (mapView.getHeight() * mapView.getHeight()));
        final double requiredMinimalGroundResolutionInMetersPerPixel = (new GeoPoint(min.getLatitude(), min.getLongitude()).distanceToAsDouble(max)) / pixels;
        int zoom = calculateZoom(center.getLatitude(), requiredMinimalGroundResolutionInMetersPerPixel, tileProvider.getMaximumZoomLevel(), tileProvider.getMinimumZoomLevel());
        controller.setZoom(zoom);
        controller.setCenter(center);
    }

    private int calculateZoom(double latitude, double requiredMinimalGroundResolutionInMetersPerPixel, int maximumZoomLevel, int minimumZoomLevel) {
        for (int zoom = maximumZoomLevel; zoom >= minimumZoomLevel; zoom--) {
            if (TileSystem.GroundResolution(latitude, zoom) > requiredMinimalGroundResolutionInMetersPerPixel)
                return zoom;
        }

        return 0;
    }

    private void myZoomToBoundingBox(BoundingBox boundingBox) {
        GeoPoint min = new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest());

        GeoPoint max = new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast());
        zoomTo(theMap, min, max);
        // this.mMapView.zoomToBoundingBox(boundingBox); this is to inexact
    }

    public static String formatDistance(Double distance) {
        if(distance < 10000)
            return String.format(Locale.getDefault(), "%.0fm", distance);
        else if(distance < 100000)
            return String.format(Locale.getDefault(), "%.1fkm", distance / 1000);
        else
            return String.format(Locale.getDefault(), "%.0fkm", distance / 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonLayouts.put(BUTTONLAYOUT.CONTINUE_LAST, new String[] {getString(R.string.continuar_nponto_anterior), getString(R.string.new_track), getString(R.string.novo_ponto)});
        buttonLayouts.put(BUTTONLAYOUT.EDIT_INVENTORY, new String[] {getString(R.string.edit_inventory), getString(R.string.new_track), getString(R.string.novo_ponto)});
        buttonLayouts.put(BUTTONLAYOUT.DELETE_POI, new String[] {getString(R.string.apagar_poi), getString(R.string.new_track), getString(R.string.novo_ponto)});
        buttonLayouts.put(BUTTONLAYOUT.DELETE_TRACK, new String[] {getString(R.string.delete_track), getString(R.string.cut_track), getString(R.string.novo_ponto)});
        buttonLayouts.put(BUTTONLAYOUT.DELETE_LAYER, new String[] {getString(R.string.delete_layer), getString(R.string.novo_ponto)});

        phenoFlower = getResources().getColor(R.color.phenoFlower);
        phenoVegetative = getResources().getColor(R.color.phenoVegetative);
        phenoResting = getResources().getColor(R.color.phenoResting);
        phenoFruit = getResources().getColor(R.color.phenoFruit);
        phenoDispersion = getResources().getColor(R.color.phenoDispersion);
        phenoBud = getResources().getColor(R.color.phenoBud);

        preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());
        IConfigurationProvider config = Configuration.getInstance();
        config.setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        // set cache expiry to one year
        config.setExpirationOverrideDuration(365L * 24L * 3600L * 1000L);
        config.setTileFileSystemCacheMaxBytes(6000L * 1024L * 1024L);
        config.setTileFileSystemCacheTrimBytes(5900L * 1024L * 1024L);

        setContentView(R.layout.main_map);
        mContentView = findViewById(R.id.entrance_page);
        findViewById(R.id.main_map_interface).setVisibility(View.INVISIBLE);
        mHideToolbars.run();

        ViewTreeObserver vto = mContentView.getViewTreeObserver();
        // hide expandable GPS menu
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View v1 = findViewById(R.id.gps_seconds);
                v1.setTranslationY(v1.getHeight());
                v1.setVisibility(View.INVISIBLE);
            }
        });
        findViewById(R.id.toggleGPS).setTag(false);
        checkAllPermissions();
    }

    private void checkAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_WIFI_STATE
                    , Manifest.permission.ACCESS_NETWORK_STATE
                    , Manifest.permission.INTERNET
                    , Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.VIBRATE
            }, 1);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 1);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_storage)
                        .setMessage(R.string.permission_storage_text)
                        .setCancelable(false)
                        .setPositiveButton("I'm ok with that", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                Uri uri = Uri.fromParts("package", MainMap.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 8937);
                            }
                        }).create().show();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(ContextCompat.checkSelfPermission(MainMap.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_location)
                        .setMessage(R.string.permission_location_text)
                        .setCancelable(false)
                        .setPositiveButton("I'm ok with that", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // this request will take user to Application's Setting page
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                            }
                        }).create().show();
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(MainMap.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainMap.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                return;
            }
        }

        new InitialLoad().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                checkAllPermissions();
                break;

            case 2:
                if(ContextCompat.checkSelfPermission(MainMap.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    noPermissionsMan();
                else
                    checkAllPermissions();
                break;

            case 23: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    boolean anynot = true;
                    for(int p : grantResults)
                        anynot &= (p == PackageManager.PERMISSION_GRANTED);
                    if(anynot) {
                        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                        dlgAlert.setMessage("Por favor reinicie a aplicação");
                        dlgAlert.setTitle("Homem Luzula");
                        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
//                        new InitialLoad().execute();
                    } else
                        noPermissionsMan();
                } else
                    noPermissionsMan();
            }
        }
    }

    private void updateStatusBar() {
//        ((TextView) findViewById(R.id.statustext)).setText(String.format(Locale.getDefault(), "%d inv.", DataManager.allData.getSpeciesLists().size()));
    }

    private int getTracklogInterval() {
        return Integer.parseInt(findViewById( ((RadioGroup) findViewById(R.id.gps_seconds_group)).getCheckedRadioButtonId()).getTag().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 8937) {
            checkAllPermissions();
            return;
        }

        SpeciesList sList = null;
        int index;
        if(data != null)
            sList = data.getParcelableExtra("specieslist");
        File extStoreDir = Environment.getExternalStorageDirectory();
        File invdir = new File(extStoreDir, "homemluzula");

        switch (requestCode) {
            case GET_SPECIESLIST:
                if (resultCode != RESULT_OK) return;
                DataManager.allData.addSpeciesList(sList);

                for(TaxonObservation to : sList.getTaxa())
                    if(to.hasObservationCoordinates())
                        MainMap.observationTheme.add(new StyledLabelledGeoPoint(to.getObservationLatitude(), to.getObservationLongitude()));

                if(Inventories.saveInventoryToDisk(sList, sList.getUuid().toString()))
                    Toast.makeText(getApplicationContext(), "Saved inventory.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Some error saving inventory.", Toast.LENGTH_SHORT).show();

                //sfpo.addPoint(new GeoPoint(sList.getLatitude(), sList.getLongitude()));
                if(inventoryLayer != null) inventoryLayer.setSelectedPoint(DataManager.allData.getSpeciesLists().size() - 1);
                theMap.invalidate();
                setButtonLayout(BUTTONLAYOUT.CONTINUE_LAST);
                break;

            case EDIT_OBSERVATION:      // edit an existing single-species inventory (i.e. observation)
                if (resultCode != RESULT_OK) return;
                index = data == null ? -1 : data.getIntExtra("index", -1);
                if(index < 0) new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setPositiveButton(android.R.string.yes, null).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Erro substituindo o inventário").show();
                else {
                    String uuid = DataManager.allData.getSpeciesList(index).getUuid().toString();
                    if(data.hasExtra("delete")) {
                        File repinv = new File(invdir, uuid + ".json");
                        repinv.delete();
                        DataManager.allData.deleteSpeciesList(index);
                        if(inventoryLayer != null && inventoryLayer.getSelectedPoint() >= DataManager.allData.size())
                            inventoryLayer.setSelectedPoint(DataManager.allData.size() - 1);
                        theMap.invalidate();
                    } else {
                        TaxonObservation obs = data.getParcelableExtra("observation");
                        sList = DataManager.allData.getSpeciesList(index);
                        sList.getTaxa().set(0, obs);
                        if (Inventories.saveInventoryToDisk(sList, uuid))
                            Toast.makeText(getApplicationContext(), "Saved inventory.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Some error saving inventory.", Toast.LENGTH_SHORT).show();

                        DataManager.allData.replaceSpeciesList(sList, index);
                    }
                }
                break;

            case REPLACE_SPECIESLIST:       // edit an existing inventory
                if (resultCode != RESULT_OK) return;
                index = data == null ? -1 : data.getIntExtra("index", -1);
                if(index < 0) new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setPositiveButton(android.R.string.yes, null).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Erro substituindo o inventário").show();
                else {
                    String uuid = DataManager.allData.getSpeciesList(index).getUuid().toString();
                    if(data.hasExtra("delete")) {
                        File repinv = new File(invdir, uuid + ".json");
                        repinv.delete();
                        DataManager.allData.deleteSpeciesList(index);
                        if(inventoryLayer != null && inventoryLayer.getSelectedPoint() >= DataManager.allData.size())
                            inventoryLayer.setSelectedPoint(DataManager.allData.size() - 1);
                        //sfpo.removePoint(index);
                        theMap.invalidate();
                    } else {
                        if(Inventories.saveInventoryToDisk(sList, uuid))
                            Toast.makeText(getApplicationContext(), "Saved inventory.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Some error saving inventory.", Toast.LENGTH_SHORT).show();

                        DataManager.allData.replaceSpeciesList(sList, index);
                    }
                }
                break;

            case SELECT_SPECIES:
                if (resultCode != RESULT_OK || data == null) return;
                SpeciesList sl = data.getParcelableExtra("specieslist");
                String searchTaxon = sl.getTaxa().get(0).getTaxon().toLowerCase(Locale.ROOT);
                Paint srPaint = new Paint();
                Paint srPaintFill = new Paint();
                Paint srText = new Paint();
                srPaint.setStyle(Paint.Style.STROKE);
                srPaint.setStrokeWidth(10);
                srPaint.setColor(Color.rgb(255, 255, 0));

                srPaintFill.setStyle(Paint.Style.FILL);
                srPaintFill.setColor(Color.rgb(0, 0, 0));

                srText.setStyle(Paint.Style.FILL_AND_STROKE);
                srText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                srText.setTextSize(36);
                srText.setColor(Color.parseColor("#ef6c00"));
                srText.setTextAlign(Paint.Align.LEFT);

                SimplePointTheme searchResults = new SimplePointTheme();
                Set<String> resultNames = new HashSet<>();
                for(SpeciesList sl1 : DataManager.allData.getSpeciesLists()) {
                    for(TaxonObservation to : sl1.getTaxa()) {
                        if(to.getTaxon().toLowerCase(Locale.ROOT).contains(searchTaxon)) {
                            resultNames.add(to.getTaxon().toLowerCase(Locale.ROOT));
                            if(to.hasObservationCoordinates())
                                searchResults.add(new StyledLabelledGeoPoint(to.getObservationLatitude(), to.getObservationLongitude(), to.getTaxonCapital()));
                            else if(sl1.getLatitude() != null && sl1.getLongitude() != null)
                                searchResults.add(new StyledLabelledGeoPoint(sl1.getLatitude(), sl1.getLongitude(), to.getTaxonCapital()));
                        }
                    }
                }

                searchResultsLayer = new SimpleFastPointOverlay(searchResults,
                        new SimpleFastPointOverlayOptions().setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
                                .setPointStyle(srPaint).setRadius(22).setIsClickable(false).setLabelPolicy(SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD).setMaxNShownLabels(0));
                searchResultsLayerFill = new SimpleFastPointOverlay(searchResults,
                        new SimpleFastPointOverlayOptions().setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
                                .setPointStyle(srPaintFill).setRadius(18).setIsClickable(false).setTextStyle(srText).setLabelPolicy(SimpleFastPointOverlayOptions.LabelPolicy.DENSITY_THRESHOLD).setMaxNShownLabels(resultNames.size() > 1 ? 30 : 0));

                theMap.getOverlays().add(searchResultsLayer);
                theMap.getOverlays().add(searchResultsLayerFill);

                ImageButton ib = (ImageButton) findViewById(R.id.search_taxon);
                ib.setImageResource(R.drawable.magnifier_crossed);
                ib.setTag(!((boolean) ib.getTag()));
                break;

            case DASHBOARD:
                switch(resultCode) {
                    case CLEAR_TRACKLOG:
                        theMap.getOverlays().remove(trackLogOverlay);
                        DataManager.tracklog.clear();
                        lastLocation = null;
                        trackLogOverlay = new FolderOverlay();
                        theMap.getOverlays().add(trackLogOverlay);
                        DataManager.tracklog.setOverlay(trackLogOverlay);
                        File file = new File(invdir, "tracklog.bin");
                        if (file.exists()) file.delete();
                        theMap.invalidate();
                        break;

                    case CLEAR_ALLLAYERS:
                        theMap.getOverlays().remove(layersOverlay);
                        DataManager.layers.clear();
                        layersOverlay = new FolderOverlay();
                        theMap.getOverlays().add(layersOverlay);
                        File file1 = new File(invdir, "layers.bin");
                        if (file1.exists()) file1.delete();
                        theMap.invalidate();
                        break;
                }
                break;
        }
        refreshFrequencies();

        updateStatusBar();
        //Toast.makeText(MainMap.this, gs.toJson(sList), Toast.LENGTH_SHORT).show();
    }

/*
    @Override
    public void onUserInteraction(){
        autoSaveHandler.removeCallbacksAndMessages(null);
        autoSaveHandler.postDelayed(autoSave, AUTOSAVE_DELAY);
    }
*/

    @Override
    protected void onPause() {
        super.onPause();
        if(!recordTracklog && isGPSOn) {
            if(checkGPSPermission()) {
//                stopService(GPSIntent);
                locationManager.removeUpdates(curPosListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHideToolbars.run();
        createQuickAccessButtons();
        if(!recordTracklog && isGPSOn) {
            switchOnLocationUpdates(getTracklogInterval());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHideToolbars.run();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // this is just an internal activity change, don't do anything.
        if(internalNavigation) {
            internalNavigation = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * When screen is switched on, switch GPS on for some time, as it may be an emergency waypoint!
         */
//        registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadDataTask.cancel(true);
        lockOnCurrentLocation = true;
        recordTracklog = false;
        breakTrackAtNextFix = false;
        layersLoaded = false;
        inventoriesLoaded = false;
        tracklogsLoaded = false;
        mRecordingTracklog.removeCallbacks(mToggleTracklogIcon);
        stopService(GPSIntent);
        saveTracklogTimer.removeCallbacks(saveTracklogRunnable);
        waitLayersLoadedTimer.cancel();
        executor.shutdownNow();
/*
        try {
            unregisterReceiver(screenOnReceiver);
        } catch (IllegalArgumentException e) {
            // just ignore, it was not registered
        }
*/
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideToolbars.run();
//        mHideHandler.postDelayed(mHideToolbars, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    protected boolean isGPSEnabled() {
        return locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(seekBar.getId() == R.id.maxZoom) {
            if(seekBar.getProgress() < ((SeekBar) downloadPromptView.findViewById(R.id.minZoom)).getProgress())
                seekBar.setProgress(((SeekBar) downloadPromptView.findViewById(R.id.minZoom)).getProgress());
            ((TextView) downloadPromptView.findViewById(R.id.maxzoomvalue)).setText(seekBar.getProgress() + "");
        }

        if(seekBar.getId() == R.id.minZoom) {
            if(seekBar.getProgress() > ((SeekBar) downloadPromptView.findViewById(R.id.maxZoom)).getProgress())
                seekBar.setProgress(((SeekBar) downloadPromptView.findViewById(R.id.maxZoom)).getProgress());
            ((TextView) downloadPromptView.findViewById(R.id.minzoomvalue)).setText(seekBar.getProgress() + "");
        }

        updateEstimate(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void readChecklist() {
        // Read species checklist
//            String extStore = Environment.getExternalStorageDirectory().getAbsolutePath();// System.getenv("EXTERNAL_STORAGE");
        File extStoreDir = Environment.getExternalStorageDirectory();
        File invDir = new File(extStoreDir, "homemluzula");
        File chk = new File(invDir,"checklist.txt");
        try {
            if(!chk.exists() || !chk.canRead())
                MainMap.checklist = new Checklist(getResources().openRawResource(R.raw.checklist2));
            else
                MainMap.checklist = new Checklist(new FileInputStream(chk));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InitialLoad extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ((TextView) findViewById(R.id.loadstatus)).setText("checklist");
            readChecklist();

            File extStoreDir = Environment.getExternalStorageDirectory();
            File invDir = new File(extStoreDir, "homemluzula");

/*
            // Read all inventory data
            Gson gs = new Gson();
            FileReader data;

            // LOAD INVENTORIES
            // Read from directory
            ((TextView) findViewById(R.id.loadstatus)).setText(R.string.inventories);
            DataManager.allData = new Inventories();
            int counter = 0;
            SpeciesList sltmp;
            observationTheme = new SimplePointTheme();
            if(invDir.exists()) {
                for (File inv : invDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return s.endsWith(".json");
                    }
                })) {

                    try {
                        data = new FileReader(inv);
                        sltmp = gs.fromJson(data, SpeciesList.class);
                        if(sltmp == null) {
                            Log.e("HZ", "Inventory error: " + inv.getName());
                        } else {
                            DataManager.allData.addSpeciesList(sltmp);
                            for(TaxonObservation to : sltmp.getTaxa()) {
                                if(to.hasObservationCoordinates())
                                    observationTheme.add(new StyledLabelledGeoPoint(to.getObservationLatitude(), to.getObservationLongitude()));
                            }
                        }
                        data.close();
                        counter++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(counter % 50 == 0)
                        publishProgress(counter);
                }
            }
*/

            // this is just to build an array of GeoPoints in order to make rendering faster
//            DataSaver.allData.syncronizeSpeciesListsAndPoints();

            // Read base point theme
            ((TextView) findViewById(R.id.loadstatus)).setText(R.string.base_points);
            try {
                basePointTheme = SimplePointTheme.fromJSON(new FileInputStream(new File(extStoreDir, "basetheme.json")), false);
            } catch (IOException e) {
                basePointTheme = new SimplePointTheme();
            }

            otherPointTheme = new SimplePointTheme();
            try {
                otherPointTheme = SimplePointTheme.fromXY(new FileInputStream(new File(extStoreDir, "/allregs.txt")), false);
            } catch (IOException e) {
                otherPointTheme = new SimplePointTheme();
            }

//            basePointTheme.syncronizeGeoPoints();

            // Read POI theme
            ((TextView) findViewById(R.id.loadstatus)).setText(R.string.POIs);
            try {
//                DataSaver.POIPointTheme = SimplePointTheme.fromJSON(new FileInputStream(extStore + "/POI.json"), false);
                DataManager.POIPointTheme = SimplePointTheme.fromXYLabelColor(new FileInputStream(new File(extStoreDir, "/POI.txt")));
            } catch (IOException e) {
                e.printStackTrace();
                DataManager.POIPointTheme = new SimplePointTheme();
            }
            DataManager.POIPointTheme.setChanged(false);
//            POIPointTheme.syncronizeGeoPoints();
            return null;
        }

        @Override
        protected void onPostExecute(Void r) {
            super.onPostExecute(r);
            MainMap.this.initializeApp();
        }
    }

    /**
     * if true, start the job
     * if false, just update the dialog boxpoi.txt
     */
    private void updateEstimate(boolean startJob) {
        CacheManager mgr = new CacheManager(theMap);
        try {
            int zoommax = ((SeekBar) downloadPromptView.findViewById(R.id.maxZoom)).getProgress();
            int zoommin = ((SeekBar) downloadPromptView.findViewById(R.id.minZoom)).getProgress();

            int tilecount = mgr.possibleTilesInArea(theMap.getBoundingBox(), zoommin, zoommax);
            ((TextView) downloadPromptView.findViewById(R.id.tile_number)).setText(tilecount + " tiles estimados");

            if (startJob) {
                if(tilecount > 10000) {
                    Toast.makeText(this, "Download demasiado grande, reduzir p.f.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(downloadPrompt != null) {
                    downloadPrompt.dismiss();
                    downloadPrompt=null;
                }

                mgr.downloadAreaAsync(MainMap.this, theMap.getBoundingBox(), zoommin, zoommax, new CacheManager.CacheManagerCallback() {
                    @Override
                    public void onTaskComplete() {
                        Toast.makeText(MainMap.this, "Download complete!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onTaskFailed(int errors) {
                        Toast.makeText(MainMap.this, "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
//                        Toast.makeText(MainMap.this, "Progress: " + progress + ", current zoom: " + currentZoomLevel, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void downloadStarted() {
                        //NOOP since we are using the build in UI
                    }

                    @Override
                    public void setPossibleTilesInArea(int total) {
                        //NOOP since we are using the build in UI
                    }
                });
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void createQuickAccessButtons() {
        LinearLayout v = null;
        LinearLayout pinnedToolbar = findViewById(quickMarkToolbar);
        if(pinnedToolbar == null) {
            v = findViewById(R.id.main_map_interface);
            pinnedToolbar = new LinearLayout(this);
            pinnedToolbar.setId(quickMarkToolbar);
            pinnedToolbar.setOrientation(LinearLayout.HORIZONTAL);
        } else
            pinnedToolbar.removeAllViews();

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
//        buttonParams.setMargins(0, 0,0,0);

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Create LinearLayout
//        linearParams.setMargins(0, 0, 0, 0);
//        ll.setLayoutParams(linearParams);
//        ll.setPadding(0, 0, 0, 0);

/*
        TaxonObservation[] spp = new TaxonObservation[] {
                new TaxonObservation("Linaria algarviana", null),
                new TaxonObservation("Linaria munbyana", null)
//                new TaxonObservation("Linaria algarviana", Constants.PhenologicalState.DISPERSION)
        };
*/
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());
        Set<String> pinned = preferences.getStringSet("pinnedTaxa", new HashSet<String>());
        int counter = 0;
        for (String spname : pinned) {
            TaxonObservation sp = new TaxonObservation(TaxonObservation.capitalize(spname), null);
            final Button btn = new Button(this);
            // Give button an ID
            btn.setId(quickMarkId + counter);
            btn.setText(sp.getTaxon().replace(" ", " "));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setTag(sp);

            // set the layoutParams on the button
//            btn.setLayoutParams(buttonParams);
//            btn.setPadding(0, 0, 0, 0);
            counter++;

            // Set click listener for button
            btn.setOnClickListener(this);
            pinnedToolbar.addView(btn, buttonParams);
        }

        if(v != null)
            v.addView(pinnedToolbar, 1, linearParams);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.toggleGPS) {
            final ImageButton tb = (ImageButton) v;
            tb.setTag(!((boolean) tb.getTag()));

            if((boolean) tb.getTag()) {     // if it was off, switch on
                setGPSEnabled(true);
            } else {        // if it was on, just center
                tb.setTag(true);
                if(lastLocation != null) {
                    theMap.getController().animateTo(new GeoPoint(lastLocation));
                    findViewById(R.id.mira).setVisibility(View.GONE);
//                        findViewById(R.id.add_location).setVisibility(View.GONE);
                    findViewById(R.id.view_distance).setVisibility(View.GONE);
                }
            }

            // show interval buttons
            mHideGPSHandler.removeCallbacks(mHideGPSRunnable);
            mHideGPSHandler.postDelayed(mHideGPSRunnable, 2500);
            setGPSVisibility(true);
            lockOnCurrentLocation = true;
        }

        if(v.getId() == R.id.show_veclayers) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null) tb.setTag(true);
            tb.setTag(!((boolean) tb.getTag()));

            if((boolean) tb.getTag()) {
                tb.setImageResource(R.drawable.ic_layers);
                layersOverlay.setEnabled(true);
                otherPointLayer.setEnabled(true);
                basePointLayer.setEnabled(true);
            } else {
                tb.setImageResource(R.drawable.ic_layers_no);
                layersOverlay.setEnabled(false);
                otherPointLayer.setEnabled(false);
                basePointLayer.setEnabled(false);
            }
            theMap.invalidate();
        }

        if(v.getId() == R.id.show_tracklog) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null)
                tb.setTag(1);

            tb.setTag(((int) tb.getTag()) ^ 1);
            tb.setImageResource(getTracklogIcon());

            trackLogOverlay.setEnabled((((int) tb.getTag()) & 1) != 0);
            theMap.invalidate();
        }

        if(v.getId() == R.id.search_taxon) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null) tb.setTag(true);

            if((boolean) tb.getTag()) {
                Intent intent = new Intent(MainMap.this, MainKeyboard.class);
                internalNavigation = true;
                intent.putExtra("selectSpecies", true);
                startActivityForResult(intent, SELECT_SPECIES);
            } else {
                tb.setImageResource(R.drawable.magnifier);
                theMap.getOverlays().remove(searchResultsLayer);
                theMap.getOverlays().remove(searchResultsLayerFill);
                theMap.invalidate();
                tb.setTag(!((boolean) tb.getTag()));
            }

        }

        if(v.getId() == R.id.show_POI) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null) tb.setTag(true);
            tb.setTag(!((boolean) tb.getTag()));

            if((boolean) tb.getTag()) {
                tb.setImageResource(R.drawable.ic_redsquare);
                POIPointLayer.setEnabled(true);
            } else {
                tb.setImageResource(R.drawable.ic_redsquare_no);
                POIPointLayer.setEnabled(false);
            }
            theMap.invalidate();
        }
/*
        if(v.getId() == R.id.show_layers) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null) tb.setTag(true);
            tb.setTag(!((boolean) tb.getTag()));

            if((boolean) tb.getTag()) {
                tb.setImageResource(R.drawable.ic_point);
                otherPointLayer.setEnabled(true);
                basePointLayer.setEnabled(true);
                theMap.invalidate();
            } else {
                tb.setImageResource(R.drawable.ic_nopoint);
                otherPointLayer.setEnabled(false);
                basePointLayer.setEnabled(false);
                theMap.invalidate();
            }
        }
*/
        if(v.getId() == R.id.start_download) {
            updateEstimate(true);
        }

        if(v.getId() == R.id.bottombutton_3) {
            Intent intent = new Intent(MainMap.this, MainKeyboard.class);
            internalNavigation = true;
            startActivityForResult(intent, GET_SPECIESLIST);
        }

        if(v.getId() == R.id.show_dashboard) {
            Intent dash = new Intent(MainMap.this, Activity_dashboard.class);
            startActivityForResult(dash, DASHBOARD);
        }

        if(v.getId() == R.id.bottombutton_2) {
            if(getButtonLayout() == BUTTONLAYOUT.DELETE_TRACK) {
                if(DataManager.tracklog.cutNearestSegmentAt(theMap.getMapCenter(), true)) {
                    Toast.makeText(MainMap.this, "Track cortado no vértice mais próximo do centro.", Toast.LENGTH_SHORT).show();
                    theMap.invalidate();
                }
            } else {
                breakTrackAtNextFix = true;
                showCutTrackButton = false;
                setButtonLayout(null);
                Toast.makeText(MainMap.this, "Tracklog interrompido nesta posição.", Toast.LENGTH_SHORT).show();
            }
        }

        if(v.getId() == R.id.bottombutton_1) {
            boolean noInvSel = inventoryLayer == null || inventoryLayer.getSelectedPoint() == null;
            boolean noPOISel = POIPointLayer == null || POIPointLayer.getSelectedPoint() == null;
            boolean noTrackSel = DataManager.tracklog == null || DataManager.tracklog.getSelectedTrack() == null;

            switch(getButtonLayout()) {
                case EDIT_INVENTORY:
                case CONTINUE_LAST:
                    if(!noInvSel) {
                        openSpeciesList(inventoryLayer.getSelectedPoint());
                        return;
                    }
                    break;

                case DELETE_POI:
                    if(!noPOISel) {
                        DataManager.POIPointTheme.remove(POIPointLayer.getSelectedPoint());
                        DataManager.POIPointTheme.setChanged(true);
                        DataManager.savePOIs();
                        POIPointLayer.setSelectedPoint(DataManager.POIPointTheme.size() > 0 ? DataManager.POIPointTheme.size() - 1 : null);
                        findViewById(R.id.edit_label_box).setVisibility(View.GONE);
                        theMap.invalidate();
                    }
                    break;

                case DELETE_TRACK:
                    if(!noTrackSel) {
                        theMap.zoomToBoundingBox(BoundingBox.fromGeoPoints(DataManager.tracklog.getSelectedPolyline().getPoints()), true);
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainMap.this);
                        builder.setMessage("Quer apagar este segmento?")
                                .setCancelable(true)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        dialogInterface.dismiss();
                                        mHideToolbars.run();
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        mHideToolbars.run();
                                    }
                                })
                                .setPositiveButton("Sim, apagar track", new DialogInterface.OnClickListener() {
                                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                        DataManager.tracklog.deleteTrack(DataManager.tracklog.getSelectedTrack());
                                        DataManager.tracklog.setSelectedTrack(DataManager.tracklog.size() > 0 ? DataManager.tracklog.size() - 1 : null);
                                        findViewById(R.id.edit_label_box).setVisibility(View.GONE);
                                        theMap.invalidate();
                                        mHideToolbars.run();
                                    }
                                });
                        final androidx.appcompat.app.AlertDialog alert = builder.create();
                        alert.show();
                    }
                    break;

                case DELETE_LAYER:
                    if(DataManager.getSelectedLayer() == null) break;
                    Layer layer = DataManager.layers.get(DataManager.getSelectedLayer());
                    if(layer == null) break;
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainMap.this);
                    builder.setMessage(R.string.delete_this_layer)
                            .setCancelable(true)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    dialogInterface.dismiss();
                                    mHideToolbars.run();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    mHideToolbars.run();
                                }
                            })
                            .setPositiveButton(R.string.yes_delete_layer, new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    DataManager.layers.remove(layer);
                                    layersOverlay.remove(layer.getOverlay());
/*
                                    for(Polyline pl : layer.map.values()) {
                                        layer.getOverlay().remove(pl);
                                    }
*/

                                    DataManager.setSelectedLayer(null);
                                    findViewById(R.id.edit_label_box).setVisibility(View.GONE);
                                    theMap.invalidate();
                                    mHideToolbars.run();
                                }
                            });
                    final androidx.appcompat.app.AlertDialog alert = builder.create();
                    alert.show();

                    break;
            }
        }

        if(v.getId() == R.id.show_inventories) {
            final ImageButton tb = (ImageButton) v;
            if(tb.getTag() == null) tb.setTag(true);
            tb.setTag(!((boolean) tb.getTag()));

            if((boolean) tb.getTag()) {
                tb.setImageResource(R.drawable.ic_square);
                inventoryLayer.setEnabled(true);
                inventoryLayer.getStyle().setIsClickable(true);
                observationLayer.setEnabled(true);
            } else {
                tb.setImageResource(R.drawable.ic_square_no);
                inventoryLayer.setEnabled(false);
                inventoryLayer.getStyle().setIsClickable(false);
                observationLayer.setEnabled(false);
            }
            theMap.invalidate();
        }

        // fast mark pinned species
        if(v.getId() == quickMarkId || v.getId() == quickMarkId + 1 || v.getId() == quickMarkId + 2 || v.getId() == quickMarkId + 3) {
            if(ContextCompat.checkSelfPermission(MainMap.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
                return;

            int minGPSPrecision = Integer.parseInt(Objects.requireNonNull(preferences.getString("pref_gps_minprecision", "6")));
            final FastPointMark locationListener = new FastPointMark(minGPSPrecision);

            LocationFixedCallback cb = new LocationFixedCallback() {
                @Override
                public void finished(float latitude, float longitude) {
                    beep();
                    locationManager.removeUpdates(locationListener);
                    Intent data = new Intent();
                    SpeciesList sl = new SpeciesList();
                    sl.setSingleSpecies(true);
                    sl.setNow();
                    sl.setLocation(latitude, longitude);
                    TaxonObservation tObs = (TaxonObservation) v.getTag();
                    sl.addObservation(tObs);
                    ((Button) v).setText(tObs.getTaxonCapital().replace(" ", "\n"));

                    data.putExtra("specieslist", sl);
                    MainMap.this.onActivityResult(GET_SPECIESLIST, RESULT_OK, data);
                }
            };
            locationListener.setCallback(cb);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
            ((Button) v).setText("waiting\nGPS...");
        }

/*
        switch(view.getId()) {

            case R.id.download_tiles:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainMap.this);

                downloadPromptView = View.inflate(MainMap.this, R.layout.download_tiles, null);
                SeekBar zoom_max = (SeekBar) downloadPromptView.findViewById(R.id.maxZoom);
                SeekBar zoom_min = (SeekBar) downloadPromptView.findViewById(R.id.minZoom);
                zoom_max.setMax((int) theMap.getMaxZoomLevel());
                zoom_min.setMax((int) theMap.getMaxZoomLevel());
                zoom_min.setProgress((int) theMap.getZoomLevel());
                zoom_max.setProgress((int) theMap.getZoomLevel());
                */
/*zoom_min.setProgress((int) theMap.getZoomLevelDouble());
                zoom_max.setProgress((int) theMap.getZoomLevelDouble());*//*

                zoom_min.setOnSeekBarChangeListener(this);
                zoom_max.setOnSeekBarChangeListener(this);

                ((TextView) downloadPromptView.findViewById(R.id.maxzoomvalue)).setText(theMap.getZoomLevel() + "");
                ((TextView) downloadPromptView.findViewById(R.id.minzoomvalue)).setText(theMap.getZoomLevel() + "");
*/
/*
                ((TextView) downloadPromptView.findViewById(R.id.maxzoomvalue)).setText(theMap.getZoomLevelDouble() + "");
                ((TextView) downloadPromptView.findViewById(R.id.minzoomvalue)).setText(theMap.getZoomLevelDouble() + "");
*//*


                downloadPromptView.findViewById(R.id.start_download).setOnClickListener(this);
                builder.setView(downloadPromptView);
                builder.setCancelable(true);

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        hide();
                    }
                });
                builder.setTitle("Descarregar para navegar offline");
                downloadPrompt = builder.create();
                downloadPrompt.setCanceledOnTouchOutside(true);

                downloadPrompt.show();
                updateEstimate(false);
                break;

        }*/

    }
}
