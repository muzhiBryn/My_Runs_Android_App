package edu.dartmouth.cs.myrun;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = MyAndroidFirebaseInstanceIdService.class.getName();

    public void onTokenRefresh(){
        //get hold of the registration token
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "CloudMessage: Refreshed token: " + refreshToken);
        sendRegistrationToServer(refreshToken);
    }
    private void sendRegistrationToServer(String token){
        //Implement this method if you wantto store the token on your server
        Log.d(TAG, "CloudMessage: sendRegistrationToServer token: " + token);

    }
}
