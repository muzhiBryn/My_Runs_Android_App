package edu.dartmouth.cs.myrun;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class MyPermissionChecker {

    private static MyPermissionChecker checker;

    public static MyPermissionChecker getInstance() {
        if (checker != null) {
            return checker;
        }
        else {
            checker = new MyPermissionChecker();
            return checker;
        }
    }

    //******** Check run time permission for locationManager. This is for v23+  ********
    public boolean checkMapPermission(Context ctx){
        int result = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
}
