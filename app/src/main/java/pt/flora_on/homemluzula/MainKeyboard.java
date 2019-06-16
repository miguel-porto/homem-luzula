package pt.flora_on.homemluzula;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Locale;
import java.util.Random;

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
                        , UPDATE_OBSERVATIONS = 215;
    private Integer replace = null;
    private SharedPreferences preferences;
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
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
    private static final int numberNonLetterKeys = 2;   // how many keys are non-letter, in the keyboard

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_keyboard, menu);
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
                locationManager.removeUpdates(locationListener);
                if(ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
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

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if(ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions( MainKeyboard.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 23 );
                }
                MainKeyboard.this.setTitle(String.format(Locale.getDefault(), "Fixando GPS %.1fm", location.getAccuracy()));

                coordinates[0] = (float) location.getLatitude();
                coordinates[1] = (float) location.getLongitude();
                speciesList.setLocation(coordinates[0], coordinates[1]);
                if(location.getAccuracy() < 6) {
                    locationManager.removeUpdates(this);

                    setTitle();
                    ((TextView) findViewById(R.id.coordinates)).setText(
                            String.format(Locale.getDefault(), "%.5fº %.5fº", coordinates[0], coordinates[1]));
                    Toast.makeText(MainKeyboard.this, "Ponto fixado!", Toast.LENGTH_SHORT).show();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        String ip = preferences.getString("inventory_prefix", "");
        int zp = preferences.getInt("inventory_zeropad", 3);
        boolean autonumber = preferences.getBoolean("pref_inventorylabels", true);

        if(intent.hasExtra("specieslist")) {
            speciesList = intent.getParcelableExtra("specieslist");
            replace = intent.getIntExtra("index", -1);
            TextView tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
            //tv.setText(String.format(Locale.getDefault(), "%d espécies", speciesList.getNumberOfSpecies()));
            tv.setText(speciesList.concatSpecies(true, 2000));
            coordinates[0] = speciesList.getLatitude();
            coordinates[1] = speciesList.getLongitude();
            if( coordinates[0] == null || coordinates[1] == null || coordinates[0] == 0 || coordinates[1] == 0) coordinates[0] = null;
        } else if(intent.hasExtra("latitude")) {    // coordinates are given
            coordinates[0] = (float) intent.getDoubleExtra("latitude", 0f);
            coordinates[1] = (float) intent.getDoubleExtra("longitude", 0f);
            speciesList.setNow();
            speciesList.setLocation(coordinates[0], coordinates[1]);
            if(autonumber)
                speciesList.setSerialNumber(DataSaver.allData.getNextSerial(), ip, zp);
        } else {    // fetch GPS location
            if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }

            MainKeyboard.this.setTitle("Adquirindo coordenadas...");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
            speciesList.setNow();
            if(autonumber)
                speciesList.setSerialNumber(DataSaver.allData.getNextSerial(), ip, zp);
        }

        toolbar.setOnClickListener(view -> {    // change inventory code
            final StringBuilder titleText = new StringBuilder();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nome do ponto");

// Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    speciesList.setGpsCode(input.getText().toString());
                    setTitle();
                    mHidePart2Runnable.run();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    mHidePart2Runnable.run();
                }
            });

            final AlertDialog dialog = builder.show();
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus && dialog.getWindow() != null) {
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });
            input.requestFocus();
        });

        setTitle();

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
                        case '<':
                            if (inputBuffer.length() == 0) break;
                            inputBuffer.setLength(inputBuffer.length() - 1);
                            inputBuffer.trimToSize();
                            break;
                        case '_':
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, 345);
                            }
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

        /**
         * Delete current inventory
         */
        findViewById(R.id.delete_inventario).setVisibility(replace != null && replace > -1 && !intent.getBooleanExtra("onlyadd", false) ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.delete_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replace == null) return;
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainKeyboard.this);
                builder.setMessage("Tem a certeza que quer apagar este inventário?")
                        .setCancelable(true)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                Intent data = new Intent();
                                if(locationManager != null && locationListener != null && ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                                    locationManager.removeUpdates(locationListener);
                                }
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
         * Discard changes in current inventory
         */
        findViewById(R.id.cancel_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                if(locationManager != null && locationListener != null && ContextCompat.checkSelfPermission(MainKeyboard.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
                    locationManager.removeUpdates(locationListener);
                finish();
            }
        });

        /**
         * Save current inventory
         */
        findViewById(R.id.save_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                if(speciesList.getLatitude() == null || speciesList.getLongitude() == null) {
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
                TextView tv;
                TaxonObservation tObs = new TaxonObservation(((TextView) findViewById(R.id.freedescriptionedit)).getText().toString(), null);
                speciesList.addObservation(tObs);
                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000));
                tv.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.freedescriptionedit)).setText("");
                findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                findViewById(R.id.freedescription).setVisibility(View.GONE);
                ((RadioButton) findViewById(R.id.inputmode_fast)).setChecked(true);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tv;
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case GET_OBSERVATION:
                TaxonObservation tObs = data.getParcelableExtra("observation");
                speciesList.addObservation(tObs);
                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000));
                tv.setVisibility(View.VISIBLE);
//                Toast.makeText(MainKeyboard.this, tObs.getTaxon()+": "+tObs.getPhenoState().toString()+ " " + tObs.getComment(), Toast.LENGTH_SHORT).show();
                break;

            case UPDATE_OBSERVATIONS:
//                ArrayList<TaxonObservation> updated = data.getParcelableArrayListExtra("specieslist");
//                speciesList.replaceTaxa(updated);
                speciesList = data.getParcelableExtra("specieslist");
                tv = (TextView) MainKeyboard.this.findViewById(R.id.showspecies);
                //tv.setText(speciesList.getNumberOfSpecies()+" espécies");
                tv.setText(speciesList.concatSpecies(true, 2000));
                break;
        }
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
