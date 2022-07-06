package pt.flora_on.homemluzula.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.flora_on.homemluzula.Checklist;
import pt.flora_on.homemluzula.DataManager;
import pt.flora_on.homemluzula.EditTextBackEvent;
import pt.flora_on.homemluzula.HomemLuzulaApp;
import pt.flora_on.homemluzula.InventoryProperties;
import pt.flora_on.homemluzula.R;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.Taxon;
import pt.flora_on.observation_data.TaxonObservation;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainKeyboard extends AppCompatActivity {
    public static int screenWidth, screenHeight;
    private SpeciesList speciesList = new SpeciesList();
    private final StringBuilder inputBuffer = new StringBuilder();
    public static final int GET_OBSERVATION = 214
                        , UPDATE_OBSERVATIONS = 215, EDIT_INVENTORY_PROPERTIES = 216, TAKE_PHOTO = 345;
    private Integer replace = null;
    private boolean selectSpecies = false, changed = false, recordTaxonCoordinates;
    private SharedPreferences preferences;
    private Uri imageUri;
    private static final Pattern imageName = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(-[\\w-]+)?-([0-9]{2})\\.jpg", Pattern.CASE_INSENSITIVE);
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(//View.SYSTEM_UI_FLAG_LOW_PROFILE
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };
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
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private LocationManager locationManager;
    private LocationListener locationListener;
    private List<LocationListener> observationLocationListener = new ArrayList<LocationListener>();
    private static final int numberNonLetterKeys = 2;   // how many keys are non-letter, in the keyboard

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_keyboard, menu);
        if(selectSpecies) {
            MenuItem item = menu.findItem(R.id.refreshgps);
            item.setVisible(false);
            item = menu.findItem(R.id.take_photo);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshgps:
                if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                MainKeyboard.this.setTitle("Actualizando coordenadas...");
                speciesList.setNow();

                if (ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainKeyboard.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.removeUpdates(locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                }
                return true;

            case R.id.take_photo:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, TAKE_PHOTO);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Display the help hint
     */
    private void displayHelp() {
        ((TextView) findViewById(R.id.comousar)).setText(getString(R.string.digite_a_letra_do_genero_e_as_da_especie_ou_infrataxon, MainMap.checklist.getNLast()));
        Taxon example = MainMap.checklist.getTaxon(new Random().nextInt(MainMap.checklist.size()));
        String abbrev = Checklist.abbreviateTaxon(example, MainMap.checklist.getNFirst(), MainMap.checklist.getNLast());
        for (int i = 0; i < abbrev.length(); i++) {
            Button bt = new Button(this);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
            bt.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            bt.setText(((Character) abbrev.charAt(i)).toString());
            bt.setTextSize(36);
            bt.setPadding(0, 0, 0, 0);
            bt.setTextColor(getResources().getColor(R.color.colorAccent));
            if (i < MainMap.checklist.getNFirst())
                ((LinearLayout) findViewById(R.id.help_row1)).addView(bt, i);
            else
                ((LinearLayout) findViewById(R.id.help_row2)).addView(bt, i - MainMap.checklist.getNFirst());
        }
        ((TextView) findViewById(R.id.genus_tail)).setText(example.getGenus().substring(MainMap.checklist.getNFirst()));
        ((TextView) findViewById(R.id.spec_tail)).setText(example.getLastInfraname().substring(MainMap.checklist.getNLast()));

        findViewById(R.id.gotit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.help_keyboard).setVisibility(View.GONE);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putBoolean("helpkeyboard", true);
                edit.commit();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.help_keyboard).setAlpha(0);
                findViewById(R.id.help_keyboard).setVisibility(View.VISIBLE);
                ((FrameLayout) findViewById(R.id.help_keyboard)).animate().alpha(1);
            }
        }, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_keyboard);
        mContentView = findViewById(R.id.keyboard_page);
        mHidePart2Runnable.run();

        preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.keyboard_toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        if(!preferences.getBoolean("helpkeyboard", false)) displayHelp();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = getIntent();
        final Float[] coordinates = new Float[2];
        int minGPSPrecision = Integer.parseInt(Objects.requireNonNull(preferences.getString("pref_gps_minprecision", "6")));

        recordTaxonCoordinates = preferences.getBoolean("pref_markSpeciesCoords", true);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                MainKeyboard.this.setTitle(String.format(Locale.getDefault(), "Fixando GPS %.1fm", location.getAccuracy()));

                coordinates[0] = (float) location.getLatitude();
                coordinates[1] = (float) location.getLongitude();
                speciesList.setLocation(coordinates[0], coordinates[1]);
                if(location.getAccuracy() < minGPSPrecision) {
                    locationManager.removeUpdates(this);

                    setTitle();
                    ((TextView) findViewById(R.id.coordinates)).setText(
                            String.format(Locale.getDefault(), "%.5fº %.5fº", coordinates[0], coordinates[1]));
                    MainMap.beep();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        String ip = preferences.getString("inventory_prefix", "");
        int zp = Integer.parseInt(Objects.requireNonNull(preferences.getString("inventory_zeropad", "3")));
        boolean autonumber = preferences.getBoolean("pref_inventorylabels", true);

        if(intent.hasExtra("specieslist")) {
            speciesList = intent.getParcelableExtra("specieslist");
            replace = intent.getIntExtra("index", -1);
            recordTaxonCoordinates = speciesList.isHasTaxonCoordinates();
            TextView tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
            //tv.setText(String.format(Locale.getDefault(), "%d espécies", speciesList.getNumberOfSpecies()));
            tv.setText(speciesList.concatSpecies(true, 2000), TextView.BufferType.SPANNABLE);
            coordinates[0] = speciesList.getLatitude();
            coordinates[1] = speciesList.getLongitude();
            if( coordinates[0] == null || coordinates[1] == null || coordinates[0] == 0 || coordinates[1] == 0) coordinates[0] = null;
            setTitle();
        } else if(intent.hasExtra("latitude")) {    // coordinates are given
            coordinates[0] = (float) intent.getDoubleExtra("latitude", 0f);
            coordinates[1] = (float) intent.getDoubleExtra("longitude", 0f);
            speciesList.setNow();
            speciesList.setLocation(coordinates[0], coordinates[1]);
            if(autonumber)
                speciesList.setSerialNumber(DataManager.allData.getNextSerial(), ip, zp);
            setTitle();
        } else if (intent.hasExtra("selectSpecies")) {
            setTitle("Pesquisar por espécie ou texto livre");
            findViewById(R.id.save_inventario).setVisibility(View.GONE);
            findViewById(R.id.showspecies).setVisibility(View.GONE);
            findViewById(R.id.coordinates).setVisibility(View.GONE);
//            findViewById(R.id.take_photo).setVisibility(View.GONE);
            ((EditTextBackEvent) findViewById(R.id.freedescriptionedit)).setHint("pesquise por texto livre");
            ((RadioButton) findViewById(R.id.inputmode_doubt)).setText("TEXTO LIVRE");
            selectSpecies = true;
        } else {    // fetch GPS location
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }

            MainKeyboard.this.setTitle("Adquirindo coordenadas...");
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            speciesList.setNow();
            if(autonumber)
                speciesList.setSerialNumber(DataManager.allData.getNextSerial(), ip, zp);
            setTitle();
        }

        if(intent.hasExtra("dontMarkTaxa"))     // this is an inventory clicked on the map, not GPS
            recordTaxonCoordinates = false;

        speciesList.setHasTaxonCoordinates(recordTaxonCoordinates);


        if(!selectSpecies && speciesList.getUuid() != null) {
            refreshPhotos();
        }
        
        if(!selectSpecies) {
            toolbar.setOnClickListener(view -> {    // change inventory code
                Intent inv_prop = new Intent(this, InventoryProperties.class);
                inv_prop.putExtra("speciesList", speciesList);
                startActivityForResult(inv_prop, EDIT_INVENTORY_PROPERTIES);
            });
        }

        if(coordinates[0] == null) {
            ((TextView) findViewById(R.id.coordinates)).setText("Sem coordenadas");
        } else {
            ((TextView) findViewById(R.id.coordinates)).setText(
                    String.format(Locale.getDefault(), "%.5fº %.5fº", coordinates[0], coordinates[1]));
        }

        GridLayout keys = (GridLayout) findViewById(R.id.keyboard);
        View tmp;
        View.OnClickListener keyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.help_keyboard).getVisibility() != View.GONE)
                    findViewById(R.id.gotit).callOnClick();

                Character btn = ((TextView) v).getText().charAt(0);

                //Toast.makeText(MainKeyboard.this, , Toast.LENGTH_SHORT).show();
                if (!v.isEnabled()) return;
                if (btn >= 'A' && btn <= 'Z') {
                    inputBuffer.append(btn);
                } else {    // not a letter
                    switch (btn) {
                        case '⌫':
                            if (inputBuffer.length() == 0) break;
                            inputBuffer.setLength(inputBuffer.length() - 1);
                            inputBuffer.trimToSize();
                            break;
                    }
                }
                refreshKeyboard();
            }
        };

        for(int i = 0; i < keys.getChildCount(); i++) {
            tmp = keys.getChildAt(i);
            tmp.setOnClickListener(keyListener);

        }

        if(MainMap.checklist == null) {
            Toast.makeText(MainKeyboard.this.getApplicationContext(), "ERRO", Toast.LENGTH_LONG).show();
            return;
        }

        Taxon[] allTaxa = MainMap.checklist.getTaxonFromAbbreviation("");
        dimKeys(keys, Checklist.getPossibleLetters(allTaxa, MainMap.checklist.getNFirst(), MainMap.checklist.getNLast(), ""));
        setDimKey(findViewById(R.id.key_backspace), true);

        if(!intent.getBooleanExtra("onlyadd", false)) {
            // show current species list
            MainKeyboard.this.findViewById(R.id.showspecies).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainKeyboard.this, ShowObservations.class);
                    //intent.putParcelableArrayListExtra("specieslist", speciesList.getTaxa());
                    intent.putExtra("specieslist", speciesList);
                    startActivityForResult(intent, UPDATE_OBSERVATIONS);
                }
            });
        }

/*
        findViewById(R.id.take_photo).setOnClickListener(view -> {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        });
*/

        /**
         * Delete current inventory
         */
        findViewById(R.id.delete_inventario).setVisibility(replace != null && replace > -1 && !intent.getBooleanExtra("onlyadd", false) ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.delete_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replace == null) return;
                File[] imgs = getPhotosOfInventory(speciesList.getUuid().toString());
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainKeyboard.this);
                builder.setMessage("Tem a certeza que quer apagar este inventário?" + ((imgs.length > 0) ? " Todas as fotos associadas ao inventário e às espécies serão apagadas!" : ""))
                        .setCancelable(true)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                Intent data = new Intent();
                                if(locationManager != null && ContextCompat.checkSelfPermission(MainKeyboard.this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                                    if(locationListener != null)
                                        locationManager.removeUpdates(locationListener);
                                    if(recordTaxonCoordinates) {
                                        for (LocationListener ll : observationLocationListener)
                                            locationManager.removeUpdates(ll);
                                    }
                                }

                                for(File f : imgs)
                                    f.delete();

                                data.putExtra("delete", true);
                                data.putExtra("index", replace);
                                setResult(Activity.RESULT_OK, data);
                                finish();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                                hide();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        });

        /**
         * Discard changes in current inventoryTools
         */
        findViewById(R.id.cancel_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changed) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainKeyboard.this);
                    builder.setMessage("Tem a certeza que quer descartar as alterações?")
                            .setCancelable(true)
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    if (locationManager != null && ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        if (locationListener != null)
                                            locationManager.removeUpdates(locationListener);
                                        if (recordTaxonCoordinates) {
                                            for (LocationListener ll : observationLocationListener)
                                                locationManager.removeUpdates(ll);
                                        }
                                    }

                                    File[] imgs = getPhotosOfInventory(speciesList.getUuid().toString());
                                    for(File f : imgs)
                                        f.delete();

                                    finish();
                                }
                            })
                            .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                    hide();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    if (locationManager != null && ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (locationListener != null)
                            locationManager.removeUpdates(locationListener);
                        if (recordTaxonCoordinates) {
                            for (LocationListener ll : observationLocationListener)
                                locationManager.removeUpdates(ll);
                        }
                    }
                    finish();
                }
            }
        });


        /**
         * Save current inventory
         */
        findViewById(R.id.save_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                if((speciesList.getLatitude() == null || speciesList.getLongitude() == null) && !selectSpecies) {
                    if(ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
//                        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                            Toast.makeText(MainKeyboard.this.getApplicationContext(), "Ponto gravado sem coordenadas", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainKeyboard.this.getApplicationContext(), String.format(Locale.getDefault(), "O ponto GPS ainda não foi fixo, irá ficar com precisão de %.1f metros."
                                    , locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy()), Toast.LENGTH_LONG).show();
                            coordinates[0] = (float) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                            coordinates[1] = (float) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                            speciesList.setLocation(coordinates[0], coordinates[1]);
                        }
                        locationManager.removeUpdates(locationListener);
                        if(recordTaxonCoordinates) {
                            for (LocationListener ll : observationLocationListener)
                                locationManager.removeUpdates(ll);
                        }
                    }
                }
                data.putExtra("specieslist", speciesList);
                if(replace != null) data.putExtra("index", replace);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        findViewById(R.id.inputmode_fast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                findViewById(R.id.freedescription).setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.hideSoftInputFromWindow(findViewById(R.id.freedescriptionedit).getWindowToken(), 0);
            }
        });
        findViewById(R.id.inputmode_genus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                findViewById(R.id.freedescription).setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.hideSoftInputFromWindow(findViewById(R.id.freedescriptionedit).getWindowToken(), 0);
            }
        });

        findViewById(R.id.inputmode_doubt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.keyboard).setVisibility(View.GONE);
                findViewById(R.id.freedescription).setVisibility(View.VISIBLE);
                findViewById(R.id.freedescriptionedit).requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null)
                    imm.showSoftInput(findViewById(R.id.freedescriptionedit), InputMethodManager.SHOW_IMPLICIT);
            }
        });

        ((EditText) findViewById(R.id.freedescriptionedit)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        hide();
                        return false;
                    }
                }
                return false; // pass on to other listeners.
            }
        });

        ((EditTextBackEvent) findViewById(R.id.freedescriptionedit)).setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
            @Override
            public void onImeBack(EditTextBackEvent ctrl, String text) {
                hide();
            }
        });
        findViewById(R.id.add_freedoubt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                TextView tv;
                TaxonObservation tObs = new TaxonObservation(((TextView) findViewById(R.id.freedescriptionedit)).getText().toString(), null);

                speciesList.addObservation(tObs);
                if(selectSpecies) {  // this was called to replace one taxon, so confirm it immediately
                    findViewById(R.id.save_inventario).callOnClick();
                    return;
                }
                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000), TextView.BufferType.SPANNABLE);
                tv.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.freedescriptionedit)).setText("");
                findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                findViewById(R.id.freedescription).setVisibility(View.GONE);
                ((RadioButton) findViewById(R.id.inputmode_fast)).setChecked(true);

                if(recordTaxonCoordinates) recordObservationCoordinates(tObs);
                changed = true;
            }
        });
    }

    static private File[] getPhotosOfInventory(String uuid) {
        File extStoreDir = Environment.getExternalStorageDirectory();
        File imgDir = new File(extStoreDir, "homemluzula/photos");
        if(!imgDir.exists()) {
            File invDir = new File(extStoreDir, "homemluzula");
            if(!invDir.exists()) invDir.mkdir();
            imgDir = new File(invDir, "photos");
            if(!imgDir.exists()) imgDir.mkdir();
        }
        return imgDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(uuid);
            }
        });
    }

    private void refreshPhotos() {
//        Toast.makeText(this, "REFRESH", Toast.LENGTH_SHORT).show();
        ((LinearLayout) findViewById(R.id.photo_holder)).removeAllViews();
        File[] imgs = getPhotosOfInventory(speciesList.getUuid().toString());

        if(imgs != null) {
            Arrays.sort(imgs);
            for(File img : imgs) {
                Bitmap bmp = BitmapFactory.decodeFile(img.getAbsolutePath());
                Matcher m = imageName.matcher(img.getName());
                if(m.find()) {
                    String gr1 = m.group(1);
                    addImage(bmp, gr1 != null ? gr1.substring(1) : null);
                } else
                    addImage(bmp, null);
            }
        }
    }
    
    private void setTitle() {
        if(speciesList.getSerialNumber() == null && "".equals(speciesList.getGpsCode()))
            MainKeyboard.this.setTitle(String.format(Locale.getDefault(), "%d/%d/%d"
                    , speciesList.getDay(), speciesList.getMonth(), speciesList.getYear()));
        else
            MainKeyboard.this.setTitle(String.format(Locale.getDefault(), "Inv. %s: %d/%d/%d", speciesList.getGpsCode()
                    , speciesList.getDay(), speciesList.getMonth(), speciesList.getYear()));
    }
    /**
     * Depending on the entered letters, either dims the keyboard keys or sends user to the species
     * list
     */
    private void dimKeys(GridLayout egv, List<Integer> keys) {
        TextView key;
        // dim the keys which have no possibilities
        for (int i = 0; i < egv.getChildCount() - numberNonLetterKeys; i++) {
            key = (TextView) egv.getChildAt(i);
            if(key != null)
                setDimKey(key, !keys.contains(i + 65));
        }
    }

    private void setDimKey(View v, boolean dimmed) {
        if(dimmed) {
            v.animate().alpha(0.5f).scaleX(0.75f).scaleY(0.75f).setDuration(150);
//            v.setAlpha(0.2f);
            v.setEnabled(false);
        } else {
            v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(150);
//            v.setAlpha(1f);
            v.setEnabled(true);
        }
    }

    private void refreshKeyboard() {
        TextView tv = (TextView) findViewById(R.id.inputbuffer);
        tv.setText(inputBuffer.toString());
        GridLayout egv = (GridLayout) findViewById(R.id.keyboard);

        if(((RadioButton) findViewById(R.id.inputmode_genus)).isChecked()) {
            // genus input mode
            String[] genera = MainMap.checklist.getGeneraFromPrefix(inputBuffer.toString().toLowerCase());

            if (genera.length == 1) {
                Taxon[] possibleTaxa = MainMap.checklist.getTaxaFromGenus(genera[0]);
                String[] taxa = Checklist.getTaxonNameFromTaxonList(possibleTaxa);
                Intent intent = new Intent(MainKeyboard.this, SpeciesChooser.class);
                intent.putExtra("taxa", taxa);
                intent.putExtra("uuid", speciesList.getUuid().toString());
                // this avoids the flickering
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                startActivityForResult(intent, GET_OBSERVATION);

                inputBuffer.delete(0, inputBuffer.length());
                // reset key visibility
                possibleTaxa = MainMap.checklist.getTaxaFromGenus("");
                List<Integer> pl = Checklist.getPossibleLetters(possibleTaxa, "");
                dimKeys(egv, pl);
                tv.setText("");
                setDimKey(findViewById(R.id.key_backspace), true);
            } else {
                Taxon[] possibleTaxa = MainMap.checklist.getTaxaFromGenus(inputBuffer.toString().toLowerCase());
                List<Integer> pl = Checklist.getPossibleLetters(possibleTaxa, inputBuffer.toString().toLowerCase());
                dimKeys(egv, pl);
                setDimKey(findViewById(R.id.key_backspace), inputBuffer.length() == 0);
            }
        }
        if(((RadioButton) findViewById(R.id.inputmode_fast)).isChecked()) {    // fast input mode (abbreviation)
            Taxon[] possibleTaxa = MainMap.checklist.getTaxonFromAbbreviation(inputBuffer.toString().toLowerCase());
            List<Integer> pl = Checklist.getPossibleLetters(possibleTaxa, MainMap.checklist.getNFirst(), MainMap.checklist.getNLast(), inputBuffer.toString().toLowerCase());

            if (inputBuffer.length() < MainMap.checklist.getNFilterCharacters()
                    && possibleTaxa.length > Integer.parseInt(preferences.getString("pref_maxnrspecies", "5")) && pl.size() > 1) {
                dimKeys(egv, pl);
                setDimKey(findViewById(R.id.key_backspace), inputBuffer.length() == 0);
            } else if (possibleTaxa.length > 0) {
                // show species list
                String[] taxa = Checklist.getTaxonNameFromTaxonList(possibleTaxa);

                inputBuffer.delete(0, inputBuffer.length());
/*
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < taxa.length; i++) {
                sb.append(taxa[i]).append("\n");
            }
*/
                Intent intent = new Intent(MainKeyboard.this, SpeciesChooser.class);
                intent.putExtra("taxa", taxa);
                intent.putExtra("uuid", speciesList.getUuid().toString());
                // this avoids the flickering
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                startActivityForResult(intent, GET_OBSERVATION);

                // reset key visibility
                possibleTaxa = MainMap.checklist.getTaxonFromAbbreviation("");
                pl = Checklist.getPossibleLetters(possibleTaxa, MainMap.checklist.getNFirst(), MainMap.checklist.getNLast(), "");
                dimKeys(egv, pl);
                tv.setText("");
                setDimKey(findViewById(R.id.key_backspace), true);
                //Toast.makeText(MainKeyboard.this, sb.toString(), Toast.LENGTH_SHORT).show();
            } else Toast.makeText(MainKeyboard.this, "No species found", Toast.LENGTH_SHORT).show();
        }

        if(((RadioButton) findViewById(R.id.inputmode_doubt)).isChecked()) {    // fast input mode (abbreviation)

        }
    }

    class observationLocationListener implements LocationListener {
        private final TaxonObservation taxonObservation;
        private final int minGPSPrecision = Integer.parseInt(Objects.requireNonNull(preferences.getString("pref_gps_minprecision", "6")));

        observationLocationListener(TaxonObservation taxonObservation) {
            this.taxonObservation = taxonObservation;
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            taxonObservation.setLocation((float) location.getLatitude(), (float) location.getLongitude());
            if(location.getAccuracy() < minGPSPrecision) {
                locationManager.removeUpdates(this);
                taxonObservation.isAcquiringGPS = 0;
//                Inventories.saveInventoryToDisk(speciesList, speciesList.getUuid().toString());
                MainMap.beep();
            }
            ((TextView) MainKeyboard.this.findViewById(R.id.showspecies)).setText(speciesList.concatSpecies(true, 2000), TextView.BufferType.SPANNABLE);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    }

    private void recordObservationCoordinates(TaxonObservation taxonObservation) {
        LocationListener tmp;
        if(speciesList.getNumberOfSpecies() == 1) {     // if it has only one species, set the same location of inventory
            taxonObservation.setLocation(speciesList.getLatitude(), speciesList.getLongitude());
//            Inventories.saveInventoryToDisk(speciesList, speciesList.getUuid().toString());
        } else if(speciesList.getNumberOfSpecies() > 1) {
            taxonObservation.isAcquiringGPS = 1;
            observationLocationListener.add(tmp = new observationLocationListener(taxonObservation));
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, tmp);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tv;
        if (resultCode != RESULT_OK) return;
        changed = true;
        findViewById(R.id.save_inventario).setVisibility(View.VISIBLE);
        switch(requestCode) {
            case GET_OBSERVATION:
                final TaxonObservation tObs = data.getParcelableExtra("observation");
                speciesList.addObservation(tObs);
                if(selectSpecies) {  // this was called to replace one taxon, so confirm it immediately
                    findViewById(R.id.save_inventario).callOnClick();
                    break;
                }

//                Inventories.saveInventoryToDisk(speciesList, speciesList.getUuid().toString());
                if(recordTaxonCoordinates)
                    recordObservationCoordinates(tObs);

                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000), TextView.BufferType.SPANNABLE);
                tv.setVisibility(View.VISIBLE);
                if(data.getBooleanExtra("hasPhoto", false))
                    refreshPhotos();
//                Toast.makeText(MainKeyboard.this, tObs.getTaxon()+": "+tObs.getPhenoState().toString()+ " " + tObs.getComment(), Toast.LENGTH_SHORT).show();
                break;

            case UPDATE_OBSERVATIONS:
//                ArrayList<TaxonObservation> updated = data.getParcelableArrayListExtra("specieslist");
//                speciesList.replaceTaxa(updated);
                speciesList = data.getParcelableExtra("specieslist");
                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000), TextView.BufferType.SPANNABLE);
//                Inventories.saveInventoryToDisk(speciesList, speciesList.getUuid().toString());
                break;

            case EDIT_INVENTORY_PROPERTIES:
                speciesList = data.getParcelableExtra("specieslist");
//                Inventories.saveInventoryToDisk(speciesList, speciesList.getUuid().toString());
                setTitle();
                break;

            case TAKE_PHOTO:
                addImage(savePhoto(imageUri, this, speciesList.getUuid().toString(), null), null);
//                from.renameTo(chk);
/*
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ((ImageView) findViewById(R.id.foto)).setImageBitmap(photo);
                try (FileOutputStream out = new FileOutputStream(chk)) {
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static Bitmap savePhoto(Uri imageUri, Context context, final String filenamePrefix, final String imageTitle) {
        Bitmap thumbnail = null;
        ExifInterface ei = null;
        Bitmap rotatedBitmap;

        try {
            thumbnail = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), imageUri);
            ei = new ExifInterface(getRealPathFromURI(imageUri, context));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(ei != null) {
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(thumbnail, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(thumbnail, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(thumbnail, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = thumbnail;
            }
        } else rotatedBitmap = thumbnail;
//                ((ImageView) findViewById(R.id.foto)).setImageBitmap(rotatedBitmap);
//                Toast.makeText(this, getRealPathFromURI(imageUri), Toast.LENGTH_SHORT).show();
        File[] imgs = getPhotosOfInventory(filenamePrefix);

        int imageNumber = 0;
        if(imgs != null) {
            for(File img : imgs) {
                Matcher m = imageName.matcher(img.getName());
                if(m.find()) {
                    String gr, name;
                    if((gr = m.group(2)) != null) {
                        if (Integer.parseInt(gr) > imageNumber) {
                            name = m.group(1);
                            imageNumber = Integer.parseInt(gr);
                        }
                    }
                }
            }
        }
        String filename;
        if(imageTitle != null)
            filename = filenamePrefix + "-" + imageTitle.replaceAll("[\\W]", "-");
        else
            filename = filenamePrefix;

        File imgDir = new File(Environment.getExternalStorageDirectory(), "homemluzula/photos");
        File dest = new File(imgDir, filename + "-" +
                String.format(Locale.getDefault(), "%02d", imageNumber + 1) + ".jpg");

        try (FileOutputStream out = new FileOutputStream(dest)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File from = new File(getRealPathFromURI(imageUri, context));
        from.delete();
        return rotatedBitmap;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private void addImage(Bitmap bmp, String title) {
        ViewGroup v = (ViewGroup) LayoutInflater.from(this)
                .inflate(R.layout.photo_thumbnail, null);
        View v1 = null;
        for(int i=0; i<v.getChildCount(); i++) {
            v1 = v.getChildAt(i);
            if(v1 instanceof PhotoView) break;
        }
        if (!(v1 instanceof PhotoView)) return;
        PhotoView pv = (PhotoView) v1;

        if(title != null)
            ((TextView) v.findViewById(R.id.photo_label)).setText(title);
        else
            v.findViewById(R.id.photo_label).setVisibility(View.GONE);
/*
    PhotoView pv = new PhotoView(this);
    pv.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    pv.setAdjustViewBounds(true);
    setMargins(pv, 0, 30, 0, 30);
    pv.setScaleType(ImageView.ScaleType.FIT_CENTER);
*/
        pv.setImageBitmap(getResizedBitmap(bmp, 1000));
        pv.setMaximumScale(10);
        pv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = findViewById(R.id.keyboard_layout);
                if (v.getVisibility() == View.GONE)
                    findViewById(R.id.keyboard_layout).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.keyboard_layout).setVisibility(View.GONE);
            }
        });

        ((LinearLayout) findViewById(R.id.photo_holder)).addView(v);
    }

    static public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        hide();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void hide() {
        // Hide UI first
/*        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }*/
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        if(recordTaxonCoordinates) {
            for (LocationListener ll : observationLocationListener)
                locationManager.removeUpdates(ll);
        }
    }
    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
