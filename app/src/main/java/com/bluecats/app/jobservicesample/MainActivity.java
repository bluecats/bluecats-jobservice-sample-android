package com.bluecats.app.jobservicesample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bluecats.sdk.BlueCatsSDK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1001;
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 1002;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1003;

    public static final int MY_JOB_ID = 1111;
    public static final long SCAN_WINDOW_IN_MS = 10*1000;
    public static final long SCAN_TIMEOUT_IN_MS = 5*1000;

    public static final String APP_TOKEN = "APP_TOKEN_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        verifyPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scheduleJob(getApplicationContext());
    }

    public void verifyPermissions() {
        if (!BlueCatsSDK.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else if (!locationPermissionsEnabled()) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSIONS);
        } else if (!BlueCatsSDK.isLocationAuthorized(MainActivity.this)) {
            showLocationServicesAlert();
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    private boolean locationPermissionsEnabled() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showLocationServicesAlert() {
        new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setMessage("This app requires Location Services to run. Would you like to enable Location Services now?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton("No", cancelClickListener)
                .create()
                .show();
    }

    private DialogInterface.OnClickListener cancelClickListener =  new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                verifyPermissions();
            }
        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verifyPermissions();
            }
        }
    }

    public static JobInfo getJobInfo(Context ctx) {
        JobInfo jobInfo = new JobInfo.Builder(MY_JOB_ID, new ComponentName(ctx, SdkStarterService.class))
                .setMinimumLatency(SCAN_TIMEOUT_IN_MS)
                .build();
        return jobInfo;
    }

    public static void scheduleJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(getJobInfo(context));
        Log.d(TAG, "Job is scheduled.");
    }
}
