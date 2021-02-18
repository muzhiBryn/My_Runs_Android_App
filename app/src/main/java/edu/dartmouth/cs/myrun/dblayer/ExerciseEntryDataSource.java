package edu.dartmouth.cs.myrun.dblayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ExerciseEntryDataSource {
    // Database fields
    private MySQLiteHelper dbHelper;

    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_USER_EMAIL,
            MySQLiteHelper.COLUMN_CLOUD_KEY,
            MySQLiteHelper.COLUMN_BOARDED,
            MySQLiteHelper.COLUMN_INPUT_TYPE,
            MySQLiteHelper.COLUMN_ACTIVITY_TYPE,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_TIME,
            MySQLiteHelper.COLUMN_DURATION,
            MySQLiteHelper.COLUMN_DISTANCE,
            MySQLiteHelper.COLUMN_CLIMBED,
            MySQLiteHelper.COLUMN_CALORIE,
            MySQLiteHelper.COLUMN_HEARTBEAT,
            MySQLiteHelper.COLUMN_COMMENT,
            MySQLiteHelper.COLUMN_PATH,
    };

    private static final String TAG = "DB_MYRUN";

    public ExerciseEntryDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public ExerciseEntry createExerciseEntry(String userEmail, String inputType, String activityType, String dateStr, String timeStr,
                                             double duration, double distance, double climbed, int calorie, int heartbeat,
                                             String comment, ArrayList<LatLng> locationList) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_EMAIL, userEmail);
        values.put(MySQLiteHelper.COLUMN_CLOUD_KEY, ExerciseEntry.DEFAULT_NOT_SYNCED_CLOUD_KEY);
        values.put(MySQLiteHelper.COLUMN_BOARDED, false);
        values.put(MySQLiteHelper.COLUMN_INPUT_TYPE, inputType);
        values.put(MySQLiteHelper.COLUMN_ACTIVITY_TYPE, activityType);
        values.put(MySQLiteHelper.COLUMN_DATE, dateStr);
        values.put(MySQLiteHelper.COLUMN_TIME, timeStr);
        values.put(MySQLiteHelper.COLUMN_DURATION, duration);
        values.put(MySQLiteHelper.COLUMN_DISTANCE, distance);
        values.put(MySQLiteHelper.COLUMN_CALORIE, calorie);
        values.put(MySQLiteHelper.COLUMN_CLIMBED, climbed);
        values.put(MySQLiteHelper.COLUMN_HEARTBEAT, heartbeat);
        values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
        // use json to convert a locationList to string
        Gson gson = new Gson();
        String locationJsonStr = gson.toJson(locationList);
        values.put(MySQLiteHelper.COLUMN_PATH, locationJsonStr);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long insertId = database.insert(MySQLiteHelper.TABLE_EXERCISE_ENTRY, null, values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXERCISE_ENTRY,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ExerciseEntry exerciseEntry = cursorToExerciseEntry(cursor);


        // Log the comment stored
        Log.d(TAG, "newExerciseEntry = " + cursorToExerciseEntry(cursor).toString()
                + " insert ID = " + insertId);

        cursor.close();
        database.close();
        dbHelper.close();
        return exerciseEntry;
    }


    public ExerciseEntry createExerciseEntry(ExerciseEntry entry) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_EMAIL, entry.getUserEmail());
        values.put(MySQLiteHelper.COLUMN_CLOUD_KEY, entry.getCloudKey());
        values.put(MySQLiteHelper.COLUMN_BOARDED, entry.isBoarded());
        values.put(MySQLiteHelper.COLUMN_INPUT_TYPE, entry.getInputType());
        values.put(MySQLiteHelper.COLUMN_ACTIVITY_TYPE, entry.getActivityType());
        values.put(MySQLiteHelper.COLUMN_DATE, entry.getDateStr());
        values.put(MySQLiteHelper.COLUMN_TIME, entry.getTimeStr());
        values.put(MySQLiteHelper.COLUMN_DURATION, entry.getDuration());
        values.put(MySQLiteHelper.COLUMN_DISTANCE, entry.getDistance());
        values.put(MySQLiteHelper.COLUMN_CALORIE, entry.getCalorie());
        values.put(MySQLiteHelper.COLUMN_CLIMBED, entry.getClimbed());
        values.put(MySQLiteHelper.COLUMN_HEARTBEAT, entry.getHeartbeat());
        values.put(MySQLiteHelper.COLUMN_COMMENT, entry.getComment());
        // use json to convert a locationList to string
        Gson gson = new Gson();
        String locationJsonStr = gson.toJson(entry.getmLocationList());
        values.put(MySQLiteHelper.COLUMN_PATH, locationJsonStr);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long insertId = database.insert(MySQLiteHelper.TABLE_EXERCISE_ENTRY, null, values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXERCISE_ENTRY,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ExerciseEntry exerciseEntry = cursorToExerciseEntry(cursor);

        database.close();
        dbHelper.close();
        return exerciseEntry;
    }

    public void updateExerciseEntry(ExerciseEntry entry) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USER_EMAIL, entry.getUserEmail());
        values.put(MySQLiteHelper.COLUMN_CLOUD_KEY, entry.getCloudKey());
        values.put(MySQLiteHelper.COLUMN_BOARDED, entry.isBoarded());
        values.put(MySQLiteHelper.COLUMN_INPUT_TYPE, entry.getInputType());
        values.put(MySQLiteHelper.COLUMN_ACTIVITY_TYPE, entry.getActivityType());
        values.put(MySQLiteHelper.COLUMN_DATE, entry.getDateStr());
        values.put(MySQLiteHelper.COLUMN_TIME, entry.getTimeStr());
        values.put(MySQLiteHelper.COLUMN_DURATION, entry.getDuration());
        values.put(MySQLiteHelper.COLUMN_DISTANCE, entry.getDistance());
        values.put(MySQLiteHelper.COLUMN_CALORIE, entry.getCalorie());
        values.put(MySQLiteHelper.COLUMN_CLIMBED, entry.getClimbed());
        values.put(MySQLiteHelper.COLUMN_HEARTBEAT, entry.getHeartbeat());
        values.put(MySQLiteHelper.COLUMN_COMMENT, entry.getComment());
        // use json to convert a locationList to string
        Gson gson = new Gson();
        String locationJsonStr = gson.toJson(entry.getmLocationList());
        values.put(MySQLiteHelper.COLUMN_PATH, locationJsonStr);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_EXERCISE_ENTRY,  values, MySQLiteHelper.COLUMN_ID + "=" + entry.getId(), null);

        database.close();
        dbHelper.close();
    }

    public void deleteExerciseEntry(ExerciseEntry exerciseEntry) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = exerciseEntry.getId();
        Log.d(TAG, "delete exerciseEntry = " + id);
        database.delete(MySQLiteHelper.TABLE_EXERCISE_ENTRY, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        database.close();
        dbHelper.close();
    }

    public void deleteExerciseEntry(long id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(TAG, "delete exerciseEntry = " + id);
        database.delete(MySQLiteHelper.TABLE_EXERCISE_ENTRY, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        database.close();
        dbHelper.close();
    }

    public void deleteAllExerciseEntries() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        System.out.println("ExerciseEntry deleted all");
        Log.d(TAG, "delete all = ");
        database.delete(MySQLiteHelper.TABLE_EXERCISE_ENTRY, null, null);
        database.close();
        dbHelper.close();
    }


    public List<ExerciseEntry> getAllExerciseEntries() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<ExerciseEntry> exerciseEntries = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXERCISE_ENTRY,
                allColumns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            ExerciseEntry exerciseEntry = cursorToExerciseEntry(cursor);
            Log.d(TAG, "get exerciseEntry = " + cursorToExerciseEntry(cursor).toString());
            exerciseEntries.add(exerciseEntry);
        }
        // Make sure to close the cursor
        cursor.close();
        database.close();
        dbHelper.close();
        return exerciseEntries;
    }


    public List<ExerciseEntry> getAllExerciseEntriesOfUser(String userEmail) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<ExerciseEntry> exerciseEntries = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXERCISE_ENTRY,
                allColumns, MySQLiteHelper.COLUMN_USER_EMAIL + "= ?",
                new String[] {userEmail}, null, null, null);

        while (cursor.moveToNext()) {
            ExerciseEntry exerciseEntry = cursorToExerciseEntry(cursor);
            Log.d(TAG, "get exerciseEntry = " + cursorToExerciseEntry(cursor).toString());
            exerciseEntries.add(exerciseEntry);
        }
        // Make sure to close the cursor
        cursor.close();
        database.close();
        dbHelper.close();
        return exerciseEntries;
    }


    private ExerciseEntry cursorToExerciseEntry(Cursor cursor) {
        ExerciseEntry exerciseEntry = new ExerciseEntry();
        exerciseEntry.setId(cursor.getLong(0));
        exerciseEntry.setUserEmail(cursor.getString(1));
        exerciseEntry.setCloudKey(cursor.getString(2));
        exerciseEntry.setBoarded(cursor.getInt(3) != 0);
        exerciseEntry.setInputType(cursor.getString(4));
        exerciseEntry.setActivityType(cursor.getString(5));
        exerciseEntry.setDateStr(cursor.getString(6));
        exerciseEntry.setTimeStr(cursor.getString(7));
        exerciseEntry.setDuration(cursor.getDouble(8));
        exerciseEntry.setDistance(cursor.getDouble(9));
        exerciseEntry.setClimbed(cursor.getDouble(10));
        exerciseEntry.setCalorie(cursor.getInt(11));
        exerciseEntry.setHeartbeat(cursor.getInt(12));
        exerciseEntry.setComment(cursor.getString(13));
        String path = cursor.getString(14);
        if (path != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<LatLng>>(){}.getType();
            ArrayList<LatLng> locationList = gson.fromJson(path, listType);
            exerciseEntry.setmLocationList(locationList);
        }
        else {
            exerciseEntry.setmLocationList(new ArrayList<LatLng>());
        }
        return exerciseEntry;
    }
}
