package edu.dartmouth.cs.myrun.dblayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_EXERCISE_ENTRY = "myrun_activity";
    public static final String TABLE_EXERCISE_ENTRY_CLOUD_KEY_INDEX = "myrun_activity_index_cloud_key";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_CLOUD_KEY = "cloud_key";
    public static final String COLUMN_BOARDED = "boarded";
    public static final String COLUMN_INPUT_TYPE = "input_type";
    public static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    public static final String COLUMN_DATE = "date_str";
    public static final String COLUMN_TIME = "time_str";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_CLIMBED = "climbed";
    public static final String COLUMN_CALORIE = "calorie";
    public static final String COLUMN_HEARTBEAT = "heartbeat";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_PATH = "path";


    private static final String DATABASE_NAME = "myrun.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
            "create table " + TABLE_EXERCISE_ENTRY +
                "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_USER_EMAIL + " text not null," +
                    COLUMN_CLOUD_KEY + " text not null," +
                    COLUMN_BOARDED + " integer not null," +
                    COLUMN_INPUT_TYPE + " text not null, " +
                    COLUMN_ACTIVITY_TYPE + " text not null, " +
                    COLUMN_DATE + " text not null, " +
                    COLUMN_TIME + " text not null, " +
                    COLUMN_DURATION + " real not null, " +
                    COLUMN_DISTANCE + " real not null, " +
                    COLUMN_CLIMBED + " real, " +
                    COLUMN_CALORIE + " integer not null, " +
                    COLUMN_HEARTBEAT + " integer, " +
                    COLUMN_COMMENT + " text, " +
                    COLUMN_PATH + " text" +
                ");";

    private static final String DATABASE_INDEX =
            "create index " + TABLE_EXERCISE_ENTRY_CLOUD_KEY_INDEX + " on " + TABLE_EXERCISE_ENTRY + " (" + COLUMN_CLOUD_KEY + ");";


    MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        database.execSQL(DATABASE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_ENTRY);
        onCreate(db);
    }
}
