package edu.dartmouth.cs.myrun;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.dartmouth.cs.myrun.dblayer.ExerciseEntry;
import edu.dartmouth.cs.myrun.dblayer.ExerciseEntryDataSource;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    // intent extra key
    public static final String INTENT_EXTRA_KEY_ACTIVITY_USE_CASE = "INTENT_EXTRA_KEY_ACTIVITY_USE_CASE";
    public static final String INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE = "INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE";
    public static final String INTENT_EXTRA_KEY_ENTRY_OBJECT = "INTENT_EXTRA_KEY_ENTRY_OBJECT";
    public static final String INTENT_EXTRA_KEY_INPUT_TYPE = "INTENT_EXTRA_KEY_INPUT_TYPE";


    // activity_use_case possible values
    // 规定是用于显示（不产生新数据，提供删除功能） 还是用于新建（产生新tracking数据，提供插入功能）
    public static final String ACTIVITY_USE_CASE_NEW_ENTRY_INPUT = "ACTIVITY_USE_CASE_NEW_ENTRY_INPUT";
    public static final String ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY = "ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY";

    // default for new input
    private String activity_use_case = ACTIVITY_USE_CASE_NEW_ENTRY_INPUT;

    private String inputType;

    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_CODE = 1;

    TrackingServiceReceiver trackingServiceReceiver;
    boolean listeningToTrackingService;

    DetectActivityServiceReceiver detectActivityServiceReceiver;
    boolean listeningToDetectActivityService;

    public Marker firstAt; // green
    public Marker lastAt; // red
    PolylineOptions rectOptions;
    Polyline polyline;


    long entryID;
    boolean entryForDisplayIsSyncedWithFirebase;
    String cloudKey;

    String activityType;
    double curSpeed;
    double avgSpeed;
    double duration; // duration is in min in db
    double distance; // always in metric. Please use changeDistancePreferredToMetric and changeDistanceInMetricToPreferred
    double climbed;
    ArrayList<LatLng> locationList;

    String unitPreference;
    private int menu_rid;
    private ExerciseEntryDataSource dataSource;

    private static final String DATE_PATTERN = "yyyy/MM/dd";
    private static final String TIME_PATTERN = "HH:mm";

    TextView mActivityType;
    TextView mCurSpeed;
    TextView mAvgSpeed;
    TextView mClimbed;
    TextView mCalorie;
    TextView mDistance;

    Location preLocation;

    Long trackingStartTS;

    ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingDectectActivityIntent;

    public static final String CHANNEL_ID = "notification channel";
    private NotificationManager mNotificationManager;

    private TrackingService mTrackingService;
    private boolean mTrackingServiceBound;
    private ServiceConnection mConnectionToTrackingService = new ServiceConnection() {
        // 绑定service后的操作
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTrackingService = ((TrackingService.LocalBinder)service).getService();
            mTrackingService.startBroadcasting();
            mTrackingServiceBound = true;
        }
        // 如果service意外失联的操作
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTrackingServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // deal with extras 获取是显示history还是新建
        Intent intent = getIntent();
        String activity_use_case = intent.getStringExtra(INTENT_EXTRA_KEY_ACTIVITY_USE_CASE);
        if (activity_use_case != null) {
            if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                menu_rid = R.menu.manual_entry_save;
                this.activity_use_case = activity_use_case;
            }
            else if (activity_use_case.equals(ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY)) {
                menu_rid = R.menu.manual_entry_delete;
                this.activity_use_case = activity_use_case;
            }
        }

        String inputType = intent.getStringExtra(INTENT_EXTRA_KEY_INPUT_TYPE); // 必须提供
        if (inputType == null) {
            inputType = getString(R.string.start_input_type_automatic);
        }
        if (inputType.equals(getString(R.string.start_input_type_automatic))) {
            // 属于automatic
            // 如果是新建
            if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                //连接ActivityRecognition服务
                //创建ActivityRecognition的client
                mActivityRecognitionClient = new ActivityRecognitionClient(this);
                Intent mIntentService = new Intent(this, DetectActivityIntentService.class);
                mPendingDectectActivityIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
                if(mActivityRecognitionClient != null) {
                    // 通知栏显示
                    showNotification();
                    // 如有update，交给一个pendingIntentService 去处理. 用requestActivityUpdates委托。//在ondestroy里removeActivityUpdates
                    mActivityRecognitionClient.requestActivityUpdates(2000, mPendingDectectActivityIntent);
                    // 监听广播
                    IntentFilter intentFilter = new IntentFilter(DetectActivityIntentService.BROADCAST_ACTION_KEY_DETECTED_ACTIVITY_UPDATE);
                    detectActivityServiceReceiver = new DetectActivityServiceReceiver();
                    registerReceiver(detectActivityServiceReceiver, intentFilter);
                    listeningToDetectActivityService = true;
                }

                activityType = "unknown";
                locationList = new ArrayList<>();
            }
        }
        else if (inputType.equals(getString(R.string.start_input_type_gps))) {
            if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                activityType = intent.getStringExtra(INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE);
                locationList = new ArrayList<>();
                // 通知栏显示
                showNotification();
            }
        }
        this.inputType = inputType;


        // 获取Extra 的entry 如果是for display 那就有，不然就是null
        ExerciseEntry entry = intent.getParcelableExtra(INTENT_EXTRA_KEY_ENTRY_OBJECT);
        if (entry != null) {
            // 展示之前保存的entry
            entryID = entry.getId();
            entryForDisplayIsSyncedWithFirebase = entry.isSynced();
            if (entryForDisplayIsSyncedWithFirebase) {
                cloudKey = entry.getCloudKey();
            }
            activityType = entry.getActivityType();
            duration = entry.getDuration();
            distance = entry.getDistance();
            locationList = entry.getmLocationList();
        }


        setContentView(R.layout.activity_map);

        mActivityType = findViewById(R.id.text_activity_type);
        mCurSpeed = findViewById(R.id.text_cur_speed);
        mAvgSpeed = findViewById(R.id.text_avg_speed);
        mClimbed = findViewById(R.id.text_climbed);
        mCalorie = findViewById(R.id.text_calorie);
        mDistance = findViewById(R.id.text_distance);

        refreshView();

        // 绑定dataSource， 用于之后的save 或 delete
        dataSource = new ExerciseEntryDataSource(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.act_map_bar_title);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // 加载map， 完成后onMapReady会被调用，在里面启动tracking service
        mapFragment.getMapAsync(this);
    }





    private void refreshView() {
        unitPreference = MyPreferences.getInstance(this).getUnitPreference();
        String unit;
        if (unitPreference.equals("miles")) {
            unit = "feet";
        }
        else {
            unit = "meter";
        }

        mActivityType.setText("Activity: " + activityType);
        mCurSpeed.setText("Speed: " + String.format("%.2f", changeDistanceInKmToMOrFeet(curSpeed)) + " " + unit + "/s");
        mAvgSpeed.setText("Avg Speed: " + String.format("%.2f",changeDistanceInKmToMOrFeet(avgSpeed)) + " " + unit + "/s");
        mClimbed.setText("Climbed: " + String.format("%.2f",changeDistanceInKmToMOrFeet(climbed)) + " " + unit);
        mCalorie.setText("Calorie: " + String.format("%.2f",distance * 1000 * 0.06) + " cal" );
        mDistance.setText("Distance: " + String.format("%.2f",changeDistanceInKmToMOrFeet(distance)) + " " + unit);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menu_rid, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle 右上角 SAVE button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 点击了save或delete
        int id = item.getItemId();
        if (id == R.id.manual_entry_save || id == R.id.manual_entry_delete) {
            MapActivity.DataModifyTask task = new MapActivity.DataModifyTask();
            task.execute();
        }

        return super.onOptionsItemSelected(item);
    }


    public class TrackingServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 传过来的是 ArrayList<Location>
            ArrayList<Location> locations = intent.getParcelableArrayListExtra(TrackingService.BROADCAST_EXTRA_KEY_LOCATION);
            for (Location location: locations) {
                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                locationList.add(latlng);

                if (locationList.size() == 1) {
                    firstAt = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));//set position and icon for the marker
                    lastAt = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));//set position and icon for the marker
                    rectOptions = new PolylineOptions();
                    rectOptions.add(lastAt.getPosition());
                    rectOptions.color(Color.BLACK);
                    // Zoom in
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17)); //17: the desired zoom level, in the range of 2.0 to 21.0
                    // 使用第一个location获取的时间作为trackingStartTS
                    trackingStartTS = location.getTime() / 1000;
                }
                else {
                    distance += location.distanceTo(preLocation) / 1000;
                    climbed += (location.getAltitude() - preLocation.getAltitude()) / 1000;
                    duration = (location.getTime() / 1000 - trackingStartTS) / 60.0;
                    avgSpeed = distance / (duration * 60);
                    curSpeed = (location.getSpeed()) / 1000;

                    lastAt.remove();
                    lastAt = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));//set position and icon for the marker
                    // rectOptions 用于存放所有经过的position
                    rectOptions.add(lastAt.getPosition());
                }
                preLocation = location;
            }
            // 清除上次画的所有路径
            if(polyline != null){
                polyline.remove();
                polyline = null;
            }
            // 重新画
            polyline = mMap.addPolyline(rectOptions);

            refreshView();
        }
    }


    public class DetectActivityServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            activityType = intent.getStringExtra(DetectActivityIntentService.BROADCAST_EXTRA_KEY_DETECTED_ACTIVITY_TYPE);
            refreshView();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!MyPermissionChecker.getInstance().checkMapPermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 地图初始化好后，启动 Tracking Service (仅在new时，而不是在display时)
        if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
            //startService(new Intent(this, TrackingService.class)); --- 我们这次不用startService了，而是用bind去create
            //BIND_AUTO_CREATE： 如果service还没创建就自动创建
            bindService(new Intent(this, TrackingService.class), mConnectionToTrackingService, Context.BIND_AUTO_CREATE);

            // 一旦绑定了service,注册广播事件
            IntentFilter intentFilter = new IntentFilter(TrackingService.BROADCAST_ACTION_KEY_LOCATION_UPDATE);
            trackingServiceReceiver = new TrackingServiceReceiver();
            registerReceiver(trackingServiceReceiver, intentFilter);
            listeningToTrackingService = true;
        }
        else {
            // 把历史location显示在地图上
            if (locationList.size() >= 1) {
                firstAt = mMap.addMarker(new MarkerOptions().position(locationList.get(0)).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN)));//set position and icon for the marker
                lastAt = mMap.addMarker(new MarkerOptions().position(locationList.get(locationList.size()-1)).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED)));//set position and icon for the marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationList.get(locationList.size()-1), 17)); //17: the desired zoom level, in the range of 2.0 to 21.0

                rectOptions = new PolylineOptions();
                for (LatLng pos: locationList) {
                    rectOptions.add(pos);
                }
                rectOptions.color(Color.BLACK);
                // 清除上次画的所有路径
                if(polyline != null){
                    polyline.remove();
                    polyline = null;
                }
                polyline = mMap.addPolyline(rectOptions);
            }


        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止监听广播事件
        if (listeningToTrackingService) {
            unregisterReceiver(trackingServiceReceiver);
        }
        if (listeningToDetectActivityService) {
            unregisterReceiver(detectActivityServiceReceiver);
        }
        // 停止接收ActivityRecognition
        if(mActivityRecognitionClient != null){
            mActivityRecognitionClient.removeActivityUpdates(mPendingDectectActivityIntent);
        }

        // 停止service -- 我们这次用unbind
        // stopService(new Intent(this, TrackingService.class));
        if (mConnectionToTrackingService != null && mTrackingServiceBound) {
            unbindService(mConnectionToTrackingService);
        }

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll(); // Cancel the persistent notification.
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 启动service的broadcast
        if (mTrackingServiceBound) {
            mTrackingService.startBroadcasting();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //停止service的broadcast，这样location会滞留在service里
        if (mTrackingServiceBound) {
            mTrackingService.stopBroadcasting();
        }
    }

    // 内部类，定义插入及删除操作AsyncTask
    private class DataModifyTask extends AsyncTask<Void, Integer, Void> {

        // A callback method executed on UI thread on starting the task
        // 回调函数， 在UI线程上执行， 可以取得UI上的值
        @Override
        protected void onPreExecute() {
            // nothing
        }

        // A callback method executed on non UI thread, invoked after onPreExecute method if exists
        // Takes a set of parameters of the type defined in your class implementation. This method will be
        // executed on the background thread, so it must not attempt to interact with UI objects.
        // 回调函数，onPreExecute之后唤起，在非UI线程后台执行，不能取得UI的值
        @Override
        protected Void doInBackground(Void... params) {
            switch (activity_use_case) {
                case ACTIVITY_USE_CASE_NEW_ENTRY_INPUT:
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
                    String dateStr = sdf.format(Calendar.getInstance().getTime());
                    sdf = new SimpleDateFormat(TIME_PATTERN, Locale.US);
                    String timeStr = sdf.format(Calendar.getInstance().getTime());
                    // distance always be saved as kms
                    dataSource.createExerciseEntry(
                            MyPreferences.getInstance(MapActivity.this).getCurrentLoggedInUserEmail(),
                            inputType,activityType,dateStr,timeStr,
                            duration,distance,climbed,0,0,null,locationList);
                    break;
                case ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY:
                    // 如果已经sync过，删除时需要连firebase记录一起删除，本地的删除交给firebase成功后的回调
                    if (entryForDisplayIsSyncedWithFirebase) {
                        FirebaseDatabase.getInstance().getReference().child(
                                MyPreferences.getInstance(MapActivity.this).getCurrentLoggedUserCloudId()).
                                child("exercise_entries").child(cloudKey).removeValue();
                    }
                    else {
                        // 如果没有sync过, 手动调用本地的删除
                        dataSource.deleteExerciseEntry(entryID);
                    }
                    break;
                default:
                    break;
            }

            return null;
        }

        // A callback method executed on UI thread, invoked by the publishProgress() from doInBackground() method
        // Overrider this handler to post interim updates to the UI thread. This handler receives the set of parameters
        // passed in publishProgress from within doInbackground.
        // 回调函数，如果在doInBackground有publishProgress()，在UI线程后台执行，更新界面
        // 这个例子中没有用到
        @Override
        protected void onProgressUpdate(Integer... values) {
            // pass, nothing
        }

        // A callback method executed on UI thread, invoked after the completion of the task
        // When doInbackground has completed, the return value from that method is passed into this event handler.
        // 回调函数，doInbackground全部结束之后唤起，在UI线程后台执行，可以取得UI的值
        @Override
        protected void onPostExecute(Void result) {
            // 存好后/删除后退出
            onSupportNavigateUp();
        }
    }


    private double changeDistanceInKmToMOrFeet(double dInMetric) {
        if (unitPreference.equals("miles")) {
            return dInMetric / 1.6 * 5280;
        }
        else {
            return dInMetric * 1000;
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void showNotification() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                "channel name", NotificationManager.IMPORTANCE_DEFAULT);

        Intent intent = new Intent(this, MapActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // this is the main app page it will show by clicking the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking...")
                .setContentText("MyRun Tracking started.")
                .setSmallIcon(R.drawable.ic_directions_run_white_24dp)
                .setContentIntent(contentIntent);
        Notification notification = notificationBuilder.build();
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(notificationChannel);

        mNotificationManager.notify(0, notification);
    }
}
