package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.positions.AreaPositions;
import com.github.filosganga.geogson.model.positions.LinearPositions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pt.flora_on.homemluzula.geo.GeoTimePoint;
import pt.flora_on.homemluzula.geo.Layer;
import pt.flora_on.homemluzula.geo.LineLayer;
import pt.flora_on.homemluzula.geo.Tracklog;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;

import static pt.flora_on.homemluzula.MainMap.CLEAR_ALLLAYERS;
import static pt.flora_on.homemluzula.MainMap.CLEAR_TRACKLOG;
import static pt.flora_on.homemluzula.MainMap.mainActivity;

/**
 * Created by miguel on 22-04-2018.
 */

public class Activity_dashboard extends AppCompatActivity implements Button.OnClickListener {
    private SharedPreferences prefs;
    private static final int OPEN_GEOJSON = 42;
    private static final int OPEN_POINTLIST = 43;
    private static final int OPEN_GEOJSONASLAYER = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.dash_toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());

        String ip = prefs.getString("inventory_prefix", "");
        int zp = prefs.getInt("inventory_zeropad", 3);

        findViewById(R.id.exporttracks).setOnClickListener(this);
        findViewById(R.id.deletetracks).setOnClickListener(this);
        findViewById(R.id.importtracks).setOnClickListener(this);
        findViewById(R.id.importinventories).setOnClickListener(this);
        findViewById(R.id.importlayer).setOnClickListener(this);
        findViewById(R.id.removelayers).setOnClickListener(this);
        ((EditText) findViewById(R.id.inventory_prefix)).setText(ip);
        ((EditText) findViewById(R.id.inventory_zeropad)).setText(((Integer) zp).toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("inventory_prefix", ((EditText) findViewById(R.id.inventory_prefix)).getText().toString());
        edt.putInt("inventory_zeropad", Integer.parseInt(((EditText) findViewById(R.id.inventory_zeropad)).getText().toString()));
        edt.apply();
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

            case R.id.removelayers:
                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
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
                final android.support.v7.app.AlertDialog alert = builder.create();
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
                final android.support.v7.app.AlertDialog.Builder builder2 = new android.support.v7.app.AlertDialog.Builder(this);
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
                final android.support.v7.app.AlertDialog alert2 = builder2.create();
                alert2.show();

                break;

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

                    Layer tl;
                    if(requestCode == OPEN_GEOJSONASLAYER) {
                        LineLayer tmp = new LineLayer(((MainMap) mainActivity).getLayersOverlay());
                        DataManager.layers.add(tmp);
                        tmp.setSolidLayer(true);
                        String result = resultData.getData().getPath();
                        int cut = result.lastIndexOf('/');
                        if (cut != -1) {
                            result = result.substring(cut + 1);
                        }
                        tmp.setLayerName(result);
                        tl = tmp;
                    } else {
                        tl = DataManager.tracklog;
                    }

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

                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapterFactory(new GeometryAdapterFactory())
                            .create();
                    FeatureCollection fc;
                    try {
                        fc = gson.fromJson(chk, FeatureCollection.class);
                        chk.close();
                    } catch (IOException | IllegalArgumentException e) {
                        Toast.makeText(this, "Ficheiro geojson inválido.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
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
                                case "POLYGON":
                                case "MULTILINESTRING":
                                    for(LinearPositions lp : ((AreaPositions) f.geometry().positions()).children()) {
                                        addLineStringToTracklog(lp, tl, st, et);
                                    }
                                    break;

                                case "LINESTRING":
                                    addLineStringToTracklog((LinearPositions) f.geometry().positions(),
                                            tl, st, et);
                                    break;
                            }
                            counter++;
                        }
                    }
                    Toast.makeText(this, counter + " linhas importados do ficheiro.", Toast.LENGTH_SHORT).show();

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
}
