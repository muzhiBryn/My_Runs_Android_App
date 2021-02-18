package edu.dartmouth.cs.myrun;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService {
    private static final String TAG = MyAndroidFirebaseMsgService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //log data to log cat
        Log.d(TAG, "From:" + remoteMessage.getFrom() );
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //create notification
        createNotification(remoteMessage.getNotification().getBody());
    }

    private void createNotification(String messageBody){
        //fire a pending intent to start the results activity21
        //Intent intent = new Intent(this, )
    }
}
