package edu.dartmouth.cs.myrun;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


public class DetectActivityIntentService extends IntentService {
    public static final String BROADCAST_ACTION_KEY_DETECTED_ACTIVITY_UPDATE= "BROADCAST_ACTION_KEY_DETECTED_ACTIVITY_UPDATE";
    public static final String BROADCAST_EXTRA_KEY_DETECTED_ACTIVITY_TYPE = "BROADCAST_EXTRA_KEY_DETECTED_ACTIVITY_TYPE";

    private static final int CONFIDENCE_THRESHOLD = 70;

    protected static final String TAG = DetectActivityIntentService.class.getSimpleName();

    public DetectActivityIntentService() {
        super(TAG);
        // Log.d(TAG,TAG + "DetectedActivityIntentService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Log.d(TAG,TAG + "onCreate()");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,TAG + "onHandleIntent()");
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.

        List<DetectedActivity> detectedActivities = result.getProbableActivities();

        for (DetectedActivity activity : detectedActivities) {
            //Log.d(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            if (activity.getConfidence() >= CONFIDENCE_THRESHOLD) {
                broadcastActivity(activity);
            }
        }
    }

    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(BROADCAST_ACTION_KEY_DETECTED_ACTIVITY_UPDATE);
        String label = "unknown";
        switch (activity.getType()) {
            case DetectedActivity.IN_VEHICLE: {
                label = "In_Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = "On_Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "On_Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                label = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                label = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                label = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                break;
            }
        }
        intent.putExtra(BROADCAST_EXTRA_KEY_DETECTED_ACTIVITY_TYPE, label);
        sendBroadcast(intent);
    }


}
