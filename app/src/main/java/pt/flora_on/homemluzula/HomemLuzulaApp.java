package pt.flora_on.homemluzula;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDexApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by miguel on 24-10-2016.
 */

public class HomemLuzulaApp extends MultiDexApplication {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        HomemLuzulaApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return HomemLuzulaApp.context;
    }

/*
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                String extStore = System.getenv("EXTERNAL_STORAGE");
                File chk = new File(extStore + "/log.txt");
                PrintWriter bw;
                try {
                    bw = new PrintWriter(new FileWriter(chk, true));
                    e.printStackTrace(bw);
                    bw.close();
                } catch (IOException e1) {

                }
                System.exit(1);
            }
        });
*/
}
