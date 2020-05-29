package pt.flora_on.homemluzula;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.StyledLabelledGeoPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.flora_on.homemluzula.geo.Layer;
import pt.flora_on.homemluzula.geo.LineLayer;
import pt.flora_on.homemluzula.geo.SensitivePolyline;
import pt.flora_on.homemluzula.geo.SimplePointTheme;
import pt.flora_on.homemluzula.geo.Tracklog;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;

public class DataManager extends AppCompatActivity {
    public static SimplePointTheme POIPointTheme;
    public static Inventories allData;
    public static Tracklog tracklog;
    public static List<Layer> layers;
    private static Integer selectedLayer = null;

    public static Integer getSelectedLayer() {
        return selectedLayer;
    }

    public static void setSelectedLayer(Integer selectedLayer) {
        if(DataManager.selectedLayer != null) {
            Overlay ovr = DataManager.layers.get(DataManager.selectedLayer).getOverlay().getItems().get(0);
            if(ovr != null) {
                if(ovr instanceof SimpleFastPointOverlay)
                    ((SimpleFastPointOverlay) ovr).setSelectedPoint(null);
            }
        }
        DataManager.selectedLayer = selectedLayer;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_data_saver);

//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        if(getIntent().hasExtra("POIs")) {
        if(POIPointTheme != null && POIPointTheme.getPointsList().size() > 0) {
            //ArrayList<LabelledGeoPoint> POIs = getIntent().getParcelableArrayListExtra("POIs");
/*            POItheme = new SimplePointTheme();
            POItheme.setPointsList(POIPointTheme.getPointsList());*/
            POIPointTheme.setChanged(true);
        }

//        if(getIntent().hasExtra("specieslists")) {
        if(allData != null && allData.getSpeciesLists().size() > 0) {
//            ArrayList<SpeciesList> sll = getIntent().getParcelableArrayListExtra("specieslists");
/*            allData = new Inventories();
            allData.setSpeciesLists(allData.getSpeciesLists());*/
            allData.setChanged(getIntent().getBooleanExtra("changed", true));
        }

//        Toast.makeText(this, "Tracklog desactivado." + (allData==null) +","+(POItheme==null), Toast.LENGTH_SHORT).show();
        if(allData != null || POIPointTheme != null || tracklog != null) {
            new SaveAndExport(DataManager.this).execute();
        } else
            finish();
//            finishAffinity();
    }

    public void saveEverythingSilently() {
        StringBuilder sb = new StringBuilder();
        int nErrors = 0;
        final Gson gs = new GsonBuilder().setPrettyPrinting().create();
        boolean setUnchanged = false;
/*
        // export main data file
        if(allData != null && allData.isChanged()) {
            try {
                final File tmp = new File(System.getenv("EXTERNAL_STORAGE") + "/flora-on.json");
//                if (!tmp.canWrite()) throw new IOException("Can't write to file");

                FileWriter fw = new FileWriter(tmp, false);
                gs.toJson(allData, fw);
                fw.close();
            } catch (IOException e) {
                sb.append("flora-on.json: ").append(e.getMessage()).append("\n");
                nErrors++;
            }
            allData.setChanged(false);
            setUnchanged = true;
        }
*/
        // export POI
        if(POIPointTheme != null && POIPointTheme.isChanged()) {
            try {
                final File tmp = new File(System.getenv("EXTERNAL_STORAGE") + "/POI.txt");
                CSVPrinter csvp = new CSVPrinter(new FileWriter(tmp, false), CSVFormat.DEFAULT);

                for(StyledLabelledGeoPoint gp : POIPointTheme.getPointsList()) {
                    csvp.printRecord(gp.getLatitude(), gp.getLongitude()
                    , gp.getLabel() == null ? "" : gp.getLabel()
                    , gp.getPointStyle() == null ? "" : gp.getPointStyle().getColor());
                }
                csvp.close();
            } catch (IOException e) {
                sb.append("POI.json: ").append(e.getMessage()).append("\n");
                nErrors++;
            }
            POIPointTheme.setChanged(false);
            //Toast.makeText(getApplicationContext(), "Saved POI", Toast.LENGTH_SHORT).show();
        }

        // remove changed notification
/*
        if(setUnchanged) {
            SharedPreferences prefs = getSharedPreferences("datastate", MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("changed", false);
            edit.commit();

//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.cancel(MainMap.UNSAVED_NOTIFICATION);
        }
*/

//        String extStore = System.getenv("EXTERNAL_STORAGE");
        File extStoreDir = Environment.getExternalStorageDirectory();
        File invdir = new File(extStoreDir, "homemluzula");
        // save tracklog
        if(tracklog != null) {
            File file = new File(invdir, "tracklog.bin");
            Log.i("PATH", file.getAbsolutePath());
            if (file.exists()) file.delete();
            try {
                file.createNewFile();
                FileOutputStream fout = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(tracklog);
                oos.close();
            } catch (IOException e) {
                sb.append("tracklog.bin: ").append(e.getMessage()).append("\n");
                nErrors++;
            }
        }

        if(layers != null && layers.size() > 0) {
            File file = new File(invdir, "layers.bin");
            if (file.exists()) file.delete();
            try {
                file.createNewFile();
                FileOutputStream fout = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(layers);
                oos.close();
            } catch (IOException e) {
                sb.append("layers.bin: ").append(e.getMessage()).append("\n");
                nErrors++;
            }
        }

        // export Flora-On text file
        //File chk = new File(extStore + "/dados-floraon.txt");
        File chk = new File(extStoreDir, "dados-floraon.txt");
        PrintWriter bw;
        try {
            bw = new PrintWriter(new FileWriter(chk, false));
            for(SpeciesList sList : allData.getSpeciesLists()) {
                sList.toCSV(bw, "floraon");
            }
            bw.close();
        } catch (IOException e) {
            sb.append("dados.txt: ").append(e.getMessage()).append("\n");
            nErrors++;
        }

//        chk = new File(extStore + "/dados-lvf.txt");
        chk = new File(extStoreDir, "/dados-lvf.txt");
        try {
            bw = new PrintWriter(new FileWriter(chk, false));
            bw.println("code\tlatitude\tlongitude\tdate\tdateYMD\ttaxa\tphenostate\tconfidence\tabundance\ttypeofestimate\tcomment");
            for(SpeciesList sList : allData.getSpeciesLists()) {
                sList.toCSV(bw, "lvf");
            }
            bw.close();
        } catch (IOException e) {
            sb.append("dados.txt: ").append(e.getMessage()).append("\n");
            nErrors++;
        }

//        tit = "Dados gravados com sucesso";
//        msg = "Ficheiros gravados:\n" + System.getenv("EXTERNAL_STORAGE") + "/flora-on.json\n" + System.getenv("EXTERNAL_STORAGE") + "/dados.txt";

        String tit, msg;
        if(nErrors > 0) {
            tit = "Erro ao gravar dados";
            msg = sb.toString();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_folha)
                    .setContentTitle(tit)
                    .setContentText(msg).setAutoCancel(true);
            Intent resultIntent = new Intent(this, MainMap.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainMap.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }

    }

    /**
     * Save everything and quit, with UI waiting
     */
    private class SaveAndExport extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private WeakReference<DataManager> dataSaverWeakReference;

        SaveAndExport(DataManager context) {
            dataSaverWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DataManager activity = dataSaverWeakReference.get();
            if (activity == null || activity.isFinishing()) return;
            dialog = ProgressDialog.show(activity, "Gravando", "Um momento...", true);
        }

        protected Void doInBackground(Void... voids) {
            saveEverythingSilently();
            return null;
        }

        @Override
        protected void onPostExecute(Void r) {
            if(dialog != null) dialog.dismiss();
/*
            DataSaver.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
*/
            finish(); //finishAffinity();
        }
    }

}
