# Android JobService Sample Project for Android 8+

This project demonstrates how to use Android's Job Service to implement background scanning as per https://developer.android.com/about/versions/oreo/background. To run this sample replace "APP_TOKEN_HERE" in MainActivity.java with your BlueCats app token. 

Change the constants below to adjust the scanning frequency:

SCAN_WINDOW_IN_MS: how long to scan for beacons in milliseconds 

SCAN_TIMEOUT_IN_MS: how long between scans in milliseconds

As your app enters the background this scanning frequency will be reduced by the Android OS as it enters deeper sleep. Once the app is back in the foreground the initial scan frequency will be resumed.
