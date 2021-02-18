package edu.dartmouth.cs.myrun;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class TrackingService extends Service {
    private static final String TAG = "TrackingService";
    public static final String BROADCAST_ACTION_KEY_LOCATION_UPDATE = "BROADCAST_ACTION_KEY_LOCATION_UPDATE";
    public static final String BROADCAST_EXTRA_KEY_LOCATION = "BROADCAST_EXTRA_KEY_LOCATION";

    LocationManager locationManager;

    ArrayList<Location> locationBuffer;

    private boolean isBroadcasting;
    String provider;

    long lastLocationUpdateTimestamp;

    private Timer timer;

    // 会被绑定的activity调用
    public void startBroadcasting() {
        isBroadcasting = true;
        Log.d(TAG, "start broadcasting!!!");
        broadcastLocation(null);
    }

    public void stopBroadcasting() {
        isBroadcasting = false;
    }

    class LocalBinder extends Binder {
        TrackingService getService() {
            return TrackingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    // onCreate() is called when the Service object is instantiated (ie: when the service is created).
    @Override
    public void onCreate() {
        super.onCreate();
        locationBuffer = new ArrayList<>();
        registerGoogleLocationService();
    }


    private void registerGoogleLocationService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        provider = locationManager.getBestProvider(criteria, true);

        //再次检查权限
        if (
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // update once every 2 second, min distance 0 therefore not considered
        // 注册location服务，每2秒更新一次，最小移动为0，相应事件为locationListener (下方定义）
        Location l = locationManager.getLastKnownLocation(provider);
        // 放入locationBuffer
        broadcastLocation(l);
        locationManager.requestLocationUpdates(provider, 2000, 0, locationListener);

//        if (timer != null) {
//           timer.cancel();
//        }
//               timer = new Timer();
//        timer.scheduleAtFixedRate(new ManuallyUpdateLocationTask(), 3000, 10000);
    }


    // synchronized 方法， 保证线程安全）
    //一次只能一个thread进入
    private synchronized void broadcastLocation(Location location) {
        Log.d(TAG, "get a new location!!!");
        lastLocationUpdateTimestamp = System.currentTimeMillis() / 1000;
        if (location != null) {
            locationBuffer.add(location);
        }

        // 如果允许广播
        if (isBroadcasting && locationBuffer.size() > 0) {
            Log.d(TAG, "broadcast all the locations!!!");
            Intent intent = new Intent();
            // 广播的key是BROADCAST_KEY_LOCATION_UPDATE
            intent.setAction(BROADCAST_ACTION_KEY_LOCATION_UPDATE);
            intent.putParcelableArrayListExtra(BROADCAST_EXTRA_KEY_LOCATION, locationBuffer);
            sendBroadcast(intent);
            locationBuffer.clear();
        }
        // 如果不允许广播，滞留在locationBuffer里
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
            //  广播
            broadcastLocation(location);
        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged");
        }
    };


    // onStartCommand() 只要有人 using startService(Intent intent) 就会被调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "S:onStartCommand(): Received start id " + startId + ": " + intent);
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "S:onDestroy():Service Stopped");
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }



    private class ManuallyUpdateLocationTask extends TimerTask {
        public void run() {
            Log.d(TAG, "ManuallyUpdateLocationTask");
            if (System.currentTimeMillis() / 1000 - lastLocationUpdateTimestamp > 10) {
                Log.d(TAG, "Long enough to run ManuallyUpdateLocationTask");
                if (ActivityCompat.checkSelfPermission(TrackingService.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (TrackingService.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Log.d(TAG, "try to manually get a location");
                Location l = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, "manually got a location" + l.toString());
                broadcastLocation(l);
            }
        }
    }

}
