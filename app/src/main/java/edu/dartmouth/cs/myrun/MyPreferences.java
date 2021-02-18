package edu.dartmouth.cs.myrun;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class MyPreferences {

    public static final String SHARED_ROOT = "MyPrefs";

    public static final String PREFS_EMAIL = "prefs_email";
    public static final String PREFS_GENDER = "prefs_gender";
    public static final String PREFS_NAME = "prefs_name";
    public static final String PREFS_PASSWORD = "prefs_password";
    public static final String PREFS_PHONE = "prefs_phone";
    public static final String PREFS_MAJOR = "prefs_major";
    public static final String PREFS_DARTMOUTH_CLASS = "prefs_dartmouth_class";
    public static final String PREFS_AVATAR_PATH = "prefs_avatar_path";

    public static final boolean FIREBASE_AUTH_ON = true;

    private static MyPreferences myPrefs;

    private Context appCtx;
    private SharedPreferences sp;

    public static final String CURR_LOGGED_USER_EMAIL = "current_logged_in_user_email";
    public static final String CURR_LOGGED_USER_CLOUD_ID = "current_logged_in_user_cloud_id";

    // 在调用setCurrentLoggedInUserEmail前， getInstance返回SHARED_ROOT的配置
    // 如果调用了setCurrentLoggedInUserEmail， myPrefs.sp会被替换，所以会切换到不同人的配置
    public static MyPreferences getInstance(Context ctx) {
        if (myPrefs != null) {
            return myPrefs;
        }
        else {
            myPrefs = new MyPreferences();
            myPrefs.appCtx = ctx.getApplicationContext();
            myPrefs.sp = myPrefs.appCtx.getSharedPreferences(SHARED_ROOT, Activity.MODE_PRIVATE);
            return myPrefs;
        }
    }


    // load the user data from shared preferences if there is no data make sure
    // that we set it to something reasonable
    public void saveUserData(String email, int gender, String name, String password, String phone,
                             String major, String dartmouthClass, String avatarPath) {
        SharedPreferences.Editor mEditor = sp.edit();

        // Save information
        mEditor.putString(PREFS_EMAIL, email);
        mEditor.putInt(PREFS_GENDER, gender);
        mEditor.putString(PREFS_NAME, name);
        mEditor.putString(PREFS_PASSWORD, password);
        mEditor.putString(PREFS_PHONE, phone);
        mEditor.putString(PREFS_MAJOR, major);
        mEditor.putString(PREFS_DARTMOUTH_CLASS, dartmouthClass);
        mEditor.putString(PREFS_AVATAR_PATH, avatarPath);

        // Commit all the changes into the shared preference
        mEditor.commit();

    }

//    private String getRealPathFromURI(Uri contentUri) {
//        String[] proj = new String[] { android.provider.MediaStore.Images.ImageColumns.DATA };
//
//        Cursor cursor = appCtx.getContentResolver().query(contentUri, proj, null,
//                null, null);
//        int column_index = cursor
//                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//
//        String filename = cursor.getString(column_index);
//        cursor.close();
//
//        return filename;
//    }


    public String getPrefsEmail() {
        return sp.getString(PREFS_EMAIL, null);
    }

    public String getPrefsName() {
        return sp.getString(PREFS_NAME, null);
    }

    public String getPrefsPhone() {
        return sp.getString(PREFS_PHONE, null);
    }

    public String getPrefsMajor() {
        return sp.getString(PREFS_MAJOR, null);
    }

    public String getPrefsPassword() {
        return sp.getString(PREFS_PASSWORD, null);
    }

    public int getPrefsGender() {
        return sp.getInt(PREFS_GENDER, -1);
    }

    public String getPrefsDartmouthClass() {
        return sp.getString(PREFS_DARTMOUTH_CLASS, null);
    }

    public String getPrefsAvatarPath() {
        return sp.getString(PREFS_AVATAR_PATH, null);
    }

    public String getUnitPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appCtx);
        return prefs.getString(appCtx.getString(R.string.pref_setting_key_unit), "kms");
    }

    public boolean isAnonymousPost() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appCtx);
        return prefs.getBoolean(appCtx.getString(R.string.pref_setting_key_privacy), false);
    }

    public void setCurrentLoggedInUserEmail(String email) {
        myPrefs.sp = appCtx.getSharedPreferences(SHARED_ROOT + "_" + email, Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(CURR_LOGGED_USER_EMAIL, email);
        mEditor.commit();
    }

    public void setCurrentLoggedInCloudUserID(String cloudUserID) {
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(CURR_LOGGED_USER_CLOUD_ID, cloudUserID);
        mEditor.commit();
    }

    public String getCurrentLoggedUserCloudId() {
        return sp.getString(CURR_LOGGED_USER_CLOUD_ID, null);
    }


    public String getCurrentLoggedInUserEmail() {
        return sp.getString(CURR_LOGGED_USER_EMAIL, null);
    }

}
