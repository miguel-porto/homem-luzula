package pt.flora_on.homemluzula.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.positions.AreaPositions;
import com.github.filosganga.geogson.model.positions.LinearPositions;
import com.github.filosganga.geogson.model.positions.MultiDimensionalPositions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import pt.flora_on.homemluzula.DataManager;
import pt.flora_on.homemluzula.HomemLuzulaApp;
import pt.flora_on.homemluzula.InventoryShow;
import pt.flora_on.homemluzula.R;
import pt.flora_on.homemluzula.SettingsActivity;
import pt.flora_on.homemluzula.geo.GeoTimePoint;
import pt.flora_on.homemluzula.geo.Layer;
import pt.flora_on.homemluzula.geo.LineLayer;
import pt.flora_on.homemluzula.geo.PointLayer;
import pt.flora_on.homemluzula.geo.Tracklog;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;

import static pt.flora_on.homemluzula.activities.MainMap.CLEAR_ALLLAYERS;
import static pt.flora_on.homemluzula.activities.MainMap.CLEAR_TRACKLOG;
import static pt.flora_on.homemluzula.activities.MainMap.mainActivity;

/**
 * Created by miguel on 22-04-2018.
 */

public class Activity_dashboard extends AppCompatActivity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SharedPreferences prefs;
    private static final int OPEN_GEOJSON = 42;
    private static final int OPEN_POINTLIST = 43;
    private static final int OPEN_GEOJSONASLAYER = 44;
    private static final int OPEN_CHECKLIST = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.dash_toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());

/*
        String ip = prefs.getString("inventory_prefix", "");
        int zp = prefs.getInt("inventory_zeropad", 3);
*/

        findViewById(R.id.exporttracks).setOnClickListener(this);
        findViewById(R.id.deletetracks).setOnClickListener(this);
        findViewById(R.id.importtracks).setOnClickListener(this);
        findViewById(R.id.importchecklist).setOnClickListener(this);
        findViewById(R.id.clear_checklist).setOnClickListener(this);
        findViewById(R.id.importinventories).setOnClickListener(this);
        findViewById(R.id.importlayer).setOnClickListener(this);
        findViewById(R.id.removelayers).setOnClickListener(this);
        findViewById(R.id.clear_pinned_species).setOnClickListener(this);

/*
        ((EditText) findViewById(R.id.inventory_prefix)).setText(ip);
        ((EditText) findViewById(R.id.inventory_zeropad)).setText(((Integer) zp).toString());
*/

        /*
         * Show list of inventories
         */
        findViewById(R.id.statustext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Activity_dashboard.this, InventoryShow.class));
            }
        });

        ((TextView) findViewById(R.id.statustext)).setText(String.format(Locale.getDefault(), getString(R.string.n_inventoryies),
                DataManager.allData.getSpeciesLists().size()));
        refreshLayerList();
    }

    private void refreshLayerList() {
        LinearLayout layerManager = ((LinearLayout) findViewById(R.id.layer_manager));
        layerManager.removeAllViews();
        for(Layer ll : DataManager.layers) {
            final CheckBox cb = new CheckBox(this);
            cb.setText(ll.getLayerName());
            cb.setChecked(ll.isVisible());
            cb.setTextSize(18);
            cb.setOnCheckedChangeListener(this);
            cb.setTag(ll);
            layerManager.addView(cb);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

/*
        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("inventory_prefix", ((EditText) findViewById(R.id.inventory_prefix)).getText().toString());
        edt.putString("inventory_zeropad", "3");
        edt.apply();
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(Activity_dashboard.this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()) {
            case R.id.importinventories:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, OPEN_POINTLIST);
                break;

            case R.id.importtracks:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_GEOJSON);
                break;

            case R.id.importlayer:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_GEOJSONASLAYER);
                break;

            case R.id.importchecklist:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_CHECKLIST);
                break;

            case R.id.clear_checklist:
                File extStoreDir = Environment.getExternalStorageDirectory();
                File invdir = new File(extStoreDir, "homemluzula");
                File chk1 = new File(invdir, "checklist.txt");
                if(chk1.exists())
                    chk1.delete();
                ((MainMap) mainActivity).readChecklist();
                finish();
                break;

            case R.id.clear_pinned_species:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());
                preferences.edit().putStringSet("pinnedTaxa", new HashSet<>()).apply();
                Toast.makeText(this, "Removed species shortcuts from map", Toast.LENGTH_SHORT).show();
                break;

            case R.id.removelayers:
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage("Quer apagar todas as layers?")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Sim, apagar tudo", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                setResult(CLEAR_ALLLAYERS);
                                finish();
                            }
                        });
                final androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
                break;

            case R.id.exporttracks:
                String extStore = System.getenv("EXTERNAL_STORAGE");
                File chk = new File(extStore + "/tracklogs.geojson");

                Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(new GeometryAdapterFactory())
                        .create();

                FileWriter fwr = null;
                try {
                    fwr = new FileWriter(chk, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Could not write file", Toast.LENGTH_SHORT).show();
                    return;
                }

                Feature.Builder ftr;
                List<Feature> ftrs = new ArrayList<>();
                LineString lstr;
                LinearPositions.Builder lposi;
                for(Tracklog.Segment gtpl : DataManager.tracklog) {
                    if(gtpl.size() < 2) continue;
                    lposi = LinearPositions.builder();

                    for(GeoTimePoint gtp : gtpl) {
                        lposi.addSinglePosition(new SinglePosition(gtp.getLongitude(), gtp.getLatitude(), 0));
                    }
                    lstr = new LineString(lposi.build());
                    ftr = Feature.builder();
                    ftr.withGeometry(lstr);
                    if(gtpl.getTitle() != null)
                        ftr.withProperty("label", new JsonPrimitive(gtpl.getTitle()));
                    if(gtpl.getColor() != null)
                        ftr.withProperty("color", new JsonPrimitive(String.format("#%06X", (0xFFFFFF & gtpl.getColor()))));
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                    long st = gtpl.get(0).getTime();
                    if(st != 0) ftr.withProperty("starttime", new JsonPrimitive(df.format(new Date(st))));
                    long et = gtpl.get(gtpl.size() - 1).getTime();
                    if(et != 0) ftr.withProperty("endtime", new JsonPrimitive(df.format(new Date(et))));
                    ftrs.add(ftr.build());
                }
                FeatureCollection fcol = new FeatureCollection(ftrs);
                try {
                    fwr.write(gson.toJson(fcol));
                    fwr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Could not write file", Toast.LENGTH_SHORT).show();
                    return;
                }

/*
                CSVPrinter bw;
                try {
                    bw = new CSVPrinter(new FileWriter(chk, false), CSVFormat.DEFAULT);
                } catch (IOException e) {
                    Toast.makeText(this, "Could not write file", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(List<GeoTimePoint> gtpl : DataSaver.tracklog) {
                    sb.append("LINESTRING(");
                    for(GeoTimePoint gtp : gtpl) {
                        sb.append(gtp.getLongitude()).append(" ").append(gtp.getLatitude()).append(", ");
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append(")");
                    try {
                        bw.printRecord(sb.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    sb.setLength(0);
                }
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
*/
                Toast.makeText(this, "Tracklogs exported", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case R.id.deletetracks:
                final androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder2.setMessage("Quer apagar todos os tracklogs?")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Sim, apagar tudo", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                setResult(CLEAR_TRACKLOG);
                                finish();
                            }
                        });
                final androidx.appcompat.app.AlertDialog alert2 = builder2.create();
                alert2.show();

                break;

        }
    }

    private void savefile(Uri sourceuri, File file) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(getContentResolver().openInputStream(sourceuri));
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri uri;
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case OPEN_CHECKLIST:
                    if (resultData != null) {
                        File extStoreDir = Environment.getExternalStorageDirectory();
                        File invdir = new File(extStoreDir, "homemluzula");
                        uri = resultData.getData();
                        if (uri == null) return;
                        File chk = new File(invdir, "checklist.txt");
                        try {
                            chk.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        savefile(uri, chk);
                    }
                    ((MainMap) mainActivity).readChecklist();
                    finish();
                    break;

                case OPEN_POINTLIST:
                    if (resultData != null) {
                        uri = resultData.getData();
                        if(uri == null) return;
                        InputStream inputStream = null;
                        try {
                            inputStream = getContentResolver().openInputStream(uri);
                        } catch(FileNotFoundException e) {
                            Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(inputStream == null) return;

                        InputStreamReader chk = new InputStreamReader(inputStream);
                        CSVParser ir;
                        try {
                            ir = CSVFormat.DEFAULT.withDelimiter('\t').parse(chk);
                        } catch (IOException e) {
                            Toast.makeText(this, "Could not parse file.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            return;
                        }
                        int counter = 0;
                        for(CSVRecord rec : ir) {
                            if(rec.size() != 3) continue;
                            SpeciesList sl = new SpeciesList();
                            sl.setGpsCode(rec.get(0));
                            sl.setLocation(Float.parseFloat(rec.get(1)), Float.parseFloat(rec.get(2)));
                            DataManager.allData.addSpeciesList(sl);
                            if(Inventories.saveInventoryToDisk(sl, sl.getUuid().toString()))
                                counter ++;
                        }
                        Toast.makeText(this, String.format("Added %d inventories.", counter), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case OPEN_GEOJSON:
                case OPEN_GEOJSONASLAYER:
                    if (resultData == null || resultData.getData() == null) break;

                    Layer newLayer;

                    uri = resultData.getData();
                    if(uri == null) return;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(uri);
                    } catch(FileNotFoundException e) {
                        Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(inputStream == null) return;
                    InputStreamReader geoJsonFile = new InputStreamReader(inputStream);

                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapterFactory(new GeometryAdapterFactory())
                            .create();
                    FeatureCollection fc;
                    try {
                        fc = gson.fromJson(geoJsonFile, FeatureCollection.class);
                        geoJsonFile.close();
                    } catch (IOException | IllegalArgumentException e) {
                        Toast.makeText(this, "Ficheiro geojson inválido.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }

                    if(requestCode == OPEN_GEOJSONASLAYER) {
                        FolderOverlay fo = new FolderOverlay();
                        ((MainMap) mainActivity).getLayersOverlay().add(fo);
                        String layerType = fc.features().get(0).geometry().type().getValue().toUpperCase();
                        Layer tmp = null;
                        switch(layerType) {
                            case "MULTIPOLYGON":
                            case "POLYGON":
                            case "MULTILINESTRING":
                            case "LINESTRING":
                                tmp = new LineLayer(fo);
                                break;

                            case "POINT":
                                tmp = new PointLayer(fo);
                                tmp.setColor(Color.BLUE);
                                tmp.setWidth(7);
                                break;

                            default:
                                new AlertDialog.Builder(this)
                                        .setCancelable(true)
                                        .setMessage(String.format("Geometry type %s not supported.", layerType))
                                        .create().show();
                                break;
                        }

                        if(tmp == null)
                            break;

                        DataManager.layers.add(tmp);
                        String result = resultData.getData().getPath();
                        int cut = result.lastIndexOf('/');
                        if (cut != -1) {
                            result = result.substring(cut + 1);
                        }
                        tmp.setLayerName(result);
                        newLayer = tmp;
                    } else {
                        newLayer = DataManager.tracklog;
                    }

                    int counter = 0;
                    if (fc != null) {
                        for (Feature f : fc.features()) {
                            Date st = null, et = null;
                            try {
                                if (f.properties().get("starttime") != null && !f.properties().get("starttime").isJsonNull())
                                    st = df.parse(f.properties().get("starttime").getAsString());
                                if (f.properties().get("endtime") != null && !f.properties().get("endtime").isJsonNull())
                                    et = df.parse(f.properties().get("endtime").getAsString());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            switch(f.geometry().type().getValue().toUpperCase()) {
                                case "POINT":
                                    SinglePosition sp = (SinglePosition) f.geometry().positions();
                                    newLayer.add(new GeoTimePoint(sp.lat(), sp.lon(), 0), false);
                                    break;

                                case "MULTIPOLYGON":
                                    for(AreaPositions ap : ((MultiDimensionalPositions) f.geometry().positions()).children()) {
                                        for(LinearPositions lp : ap.children()) {
                                            addLineStringToTracklog(lp, newLayer, st, et);
                                        }
                                    }
                                    break;

                                case "POLYGON":
                                case "MULTILINESTRING":
                                    for(LinearPositions lp : ((AreaPositions) f.geometry().positions()).children()) {
                                        addLineStringToTracklog(lp, newLayer, st, et);
                                    }
                                    break;

                                case "LINESTRING":
                                    addLineStringToTracklog((LinearPositions) f.geometry().positions(),
                                            newLayer, st, et);
                                    break;
                            }
                            counter++;
                        }
                    }
                    Toast.makeText(this, counter + " linhas importados do ficheiro.", Toast.LENGTH_SHORT).show();
                    refreshLayerList();
                    newLayer.refresh();
                    break;
            }
        }
    }

    private void addLineStringToTracklog(LinearPositions lps, Layer tracklog, Date startTime, Date endTime) {
        List<SinglePosition> sps = lps.children();
        for (int i = 0; i < sps.size(); i++) {
            SinglePosition sp = sps.get(i);
            tracklog.add(new GeoTimePoint(sp.lat(), sp.lon()
                    , (i == 0 && startTime != null) ? startTime.getTime()
                    : ((i == sps.size() - 1 && endTime != null) ? endTime.getTime() : 0)), i == 0);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        final Layer ll = (Layer) compoundButton.getTag();
        ll.setVisible(compoundButton.isChecked());
    }
}
