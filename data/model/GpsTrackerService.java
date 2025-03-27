package com.example.running_app.data.model;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.running_app.R;


public class GpsTrackerService extends Service implements LocationListener {
    protected LocationManager locationManager;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000;
    Location location;
    private updateMap mListener;
    private NotificationManager notificationManager;
    int notificationId = 0;

    private final IBinder mBinder = new LocalBinder();

    private final PermissionManager permissionManager = new PermissionManager();


    public class LocalBinder extends Binder {
        public GpsTrackerService getService() {
            return GpsTrackerService.this;
        }
    }

    public void startLocationUpdate() {
        getLocation();
    }

    @SuppressLint({"ServiceCast", "MissingPermission"})
    public void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d("GPS@@", "isGPSEnabled : " + isGPSEnabled + " isNetworkEnabled : " + isNetworkEnabled);

//                int hasFineLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.ACCESS_FINE_LOCATION);
//                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permissionManager.haveLocationPermissions(getApplicationContext())) {

                    if (isNetworkEnabled) {
                        if (locationManager != null) {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            if (location == null) {
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }

                        }
                    }

                    if (isGPSEnabled) {

                        if (locationManager != null) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            if (location == null) {
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }

                        }

                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {

        Toast.makeText(getApplicationContext(), "현재위치 " + location.getProvider() + " \n위도 " + location.getLatitude() + "\n경도 " + location.getLongitude(), Toast.LENGTH_LONG).show();
        if (mListener != null) mListener.drawMap(location);
    }



    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GpsTrackerService.this);
        }
    }

    public void setListener(updateMap listener) {
        mListener = listener;
    }

    public interface updateMap {
        void drawMap(Location location);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("HSR", "onStartCommand");
        return START_STICKY;
    }

    public void startForeground() {
        startForeground(1, createNotification());
    }

    @SuppressLint("MissingPermission")
    private Notification createNotification() {

        Log.d("HSR", "createNotification");

        String channelId = "gps_tacker_channel";



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "GPS Tracker Channel", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("RUNNING APP")
                    .setContentText("Tracking your location...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        else {
            //noinspection deprecation
            builder = new NotificationCompat.Builder(this)
                    .setContentTitle("RUNNING APP")
                    .setContentText("Tracking your location...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        return builder.build();
    }

    @Override
    public boolean stopService(Intent name) {
        Toast.makeText(getApplicationContext(), "서비스가 중지 되었습니다.", Toast.LENGTH_SHORT).show();

        return super.stopService(name);
    }

    public void stopNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
            stopForeground(true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
