package com.bluecats.app.jobservicesample;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BCBeaconRegion;
import com.bluecats.sdk.BCLogManager;
import com.bluecats.sdk.BlueCatsSDK;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SdkStarterService extends JobService {

    private static final String TAG = "SdkStarterService";

    private BCBeaconManager mBeaconManager;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        logToFile("onStartJob");
        Map<String, String> options = new HashMap<>();

        options.put(BlueCatsSDK.BC_OPTION_DEEP_SLEEP_WAKEUP_TIME_INTERVAL_IN_MINUTES, "1");

        BlueCatsSDK.setOptions(options);

        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), MainActivity.APP_TOKEN);
        BlueCatsSDK.didEnterForeground(); //immediately force foreground although the SDK running in background.

        mBeaconManager = new BCBeaconManager();
        mBeaconManager.registerCallback(mCallback);

        new AsyncTask<Void, Void, Void>() {
            Object lock = new Object();

            @Override
            protected Void doInBackground(Void... voids) {
                //do something or just wait for fun
                //and then stop SDK
                synchronized (lock) {
                    try {
                        lock.wait(MainActivity.SCAN_WINDOW_IN_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "time out");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters,false);

                BlueCatsSDK.stopPurring();

                //re-schedule the job
                logToFile("re-schedule job");
                MainActivity.scheduleJob(getApplicationContext());
            }
        }.execute();

        Log.d(TAG, "onStartJob");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    BCBeaconManagerCallback mCallback = new BCBeaconManagerCallback() {
        @Override
        public void didRangeBeacons(List<BCBeacon> beacons) {
            Log.d(TAG, "didRangeBeacons with "+beacons.size()+" beacons.");

            logToFile(new Date().toString() + ": didRangeBeacons");
        }
    };

    private static void logToFile(String message) {
        String filename = "beacon_ranging.txt";

        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, filename);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.append(message + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
