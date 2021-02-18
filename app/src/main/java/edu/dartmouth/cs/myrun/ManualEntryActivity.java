package edu.dartmouth.cs.myrun;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.dartmouth.cs.myrun.dblayer.ExerciseEntry;
import edu.dartmouth.cs.myrun.dblayer.ExerciseEntryDataSource;

//教程 https://guides.codepath.com/android/using-the-recyclerview

public class ManualEntryActivity extends AppCompatActivity {
    // intent extra key
    public static final String INTENT_EXTRA_KEY_ACTIVITY_USE_CASE = "INTENT_EXTRA_KEY_ACTIVITY_USE_CASE";
    public static final String INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE = "INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE";
    public static final String INTENT_EXTRA_KEY_ENTRY_OBJECT = "INTENT_EXTRA_KEY_ENTRY_OBJECT";

    // activity_use_case possible values
    public static final String ACTIVITY_USE_CASE_NEW_ENTRY_INPUT = "ACTIVITY_USE_CASE_NEW_ENTRY_INPUT";
    public static final String ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY = "ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY";

    // default for new input
    private String activity_use_case = ACTIVITY_USE_CASE_NEW_ENTRY_INPUT;

    private static String INPUT_TYPE = "Manual";
    private static final String[] entryTitles = {
            "Activity", "Date", "Time",
            "Duration", "Distance",
            "Calorie", "Heartbeat", "Comment"};
    private static final String DATE_PATTERN = "yyyy/MM/dd";
    private static final String TIME_PATTERN = "HH:mm";

    RecyclerView mRecyclerView;
    EntryAdapter mEntryAdapter;

    long entryID;
    boolean entryForDisplayIsSyncedWithFirebase;
    String cloudKey;

    String activityType;
    String dateStr;
    String timeStr;
    double duration;
    double distance; // always in metric. Please use changeDistancePreferredToMetric and changeDistanceInMetricToPreferred
    int calorie;
    int heartbeat;
    String comment;

    String unitPreference;
    private int menu_rid;
    private ExerciseEntryDataSource dataSource;


    // define listener: 点击date显示选择界面 (DatePickerDialog)
    View.OnClickListener mDateEntryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(ManualEntryActivity.this, dateSelectedCallback, cal
                    .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        }
    };
    // 定义选择了date后的回调方法
    DatePickerDialog.OnDateSetListener dateSelectedCallback = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            dateStr = sdf.format(cal.getTime());
            mEntryAdapter.notifyDataSetChanged();
        }
    };


    // define listener: 点击time显示选择界面 (DatePickerDialog)
    View.OnClickListener mTimeEntryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(ManualEntryActivity.this, timeSelectedCallback, cal
                    .get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }
    };
    // 定义选择了date后的回调方法
    TimePickerDialog.OnTimeSetListener timeSelectedCallback = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_PATTERN, Locale.US);
            timeStr = sdf.format(cal.getTime());
            mEntryAdapter.notifyDataSetChanged();
        }
    };


    // define listener: 点击其他显示对话框
    View.OnClickListener mModificationTextInputListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManualEntryActivity.this);
            final String title = ((TextView)(((LinearLayout) v).getChildAt(0))).getText().toString();
            builder.setTitle(title);
            // Set up the input
            final EditText input = new EditText(ManualEntryActivity.this);
            LinearLayout ll = new LinearLayout(ManualEntryActivity.this);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(40, 0, 40, 0);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            if (title.equals("Comment")) {
                input.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            else if (title.equals("Duration") || title.equals("Distance")) {
                // 可以输入小数
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            else {
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            ll.addView(input, layoutParams);

            builder.setView(ll);
            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputStr = input.getText().toString();
                    if (!inputStr.equals("")) {
                        switch (title) {
                            case "Duration":
                                duration = Double.parseDouble(input.getText().toString());
                                break;
                            case "Distance":
                                distance = changeDistanceInPreferredToMetric(Double.parseDouble(input.getText().toString()));
                                break;
                            case "Calorie":
                                calorie = Integer.parseInt(input.getText().toString());
                                break;
                            case "Heartbeat":
                                heartbeat = Integer.parseInt(input.getText().toString());
                                break;
                            case "Comment":
                                comment = input.getText().toString();
                                break;
                        }
                    }

                    mEntryAdapter.notifyDataSetChanged();
                }
            });

            builder.show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // deal with extras
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

        ExerciseEntry entry = intent.getParcelableExtra(INTENT_EXTRA_KEY_ENTRY_OBJECT);
        if (entry == null) {
            activityType = intent.getStringExtra(INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            dateStr = sdf.format(Calendar.getInstance().getTime());
            sdf = new SimpleDateFormat(TIME_PATTERN, Locale.US);
            timeStr = sdf.format(Calendar.getInstance().getTime());
            comment = "";
        }
        else {
            entryID = entry.getId();
            entryForDisplayIsSyncedWithFirebase = entry.isSynced();
            if (entryForDisplayIsSyncedWithFirebase) {
                cloudKey = entry.getCloudKey();
            }
            activityType = entry.getActivityType();
            dateStr = entry.getDateStr();
            timeStr = entry.getTimeStr();
            duration = entry.getDuration();
            distance = entry.getDistance();
            calorie = entry.getCalorie();
            heartbeat = entry.getHeartbeat();
            comment = entry.getComment();
        }


        unitPreference = MyPreferences.getInstance(this).getUnitPreference();
        setContentView(R.layout.activity_manual_entry);
        mRecyclerView = findViewById(R.id.act_man_entry_rcl);
        //RecyclerView提供的布局管理器：
        //LinearLayoutManager 以垂直或水平滚动列表方式显示项目。
        //GridLayoutManager 在网格中显示项目。
        //StaggeredGridLayoutManager 在分散对齐网格中显示项目。
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this)); //这里用线性显示 类似于listview
        mEntryAdapter = new EntryAdapter();
        mRecyclerView.setAdapter(mEntryAdapter);
        // 添加行间横线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 绑定dataSource， 用于之后的save 或 delete
        dataSource = new ExerciseEntryDataSource(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.act_manual_entry_bar_title);
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
            DataModifyTask task = new DataModifyTask();
            task.execute();
        }

        return super.onOptionsItemSelected(item);
    }


    // 内部类，定义adapter (所有原始数据到界面显示的中间层)
    class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {
        // Adapter 需要提供以下方法
        //getItemCount() 获取总的条目数
        //onCreateViewHolder() 创建ViewHolder(用于一条记录的展现),定义一条记录的布局
        //onBindViewHolder() 将数据绑定至ViewHolder， 实现一条记录在布局中的显示

        // Usually involves inflating a layout from XML and returning the holder
        // 我们创建的ViewHolder必须继承RecyclerView.ViewHolder，
        // 这个RecyclerView.ViewHolder构造时必须传入一个View，
        // 这个View相当于我们ListView getView中的convertView
        // （即：inflate的item布局需要传入）。
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return (new ViewHolder(getLayoutInflater().inflate(
                    R.layout.row_manual_entry, parent, false)));
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Set item views based on your views and data model
            String title = entryTitles[position];
            String contentText;
            switch (title) {
                case "Activity":
                    contentText = activityType;
                    break;
                case "Date":
                    contentText = dateStr;
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mDateEntryListener);
                    }
                    break;
                case "Time":
                    contentText = timeStr;
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mTimeEntryListener);
                    }
                    break;
                case "Duration":
                    contentText = duration + " mins";
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mModificationTextInputListener);
                    }
                    break;
                case "Distance":
                    contentText = changeDistanceInMetricToPreferred(distance) + " " + unitPreference;
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mModificationTextInputListener);
                    }
                    break;
                case "Calorie":
                    contentText = calorie + " cals";
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mModificationTextInputListener);
                    }
                    break;
                case "Heartbeat":
                    contentText = heartbeat + " bpm";
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mModificationTextInputListener);
                    }
                    break;
                case "Comment":
                    contentText = comment;
                    if (activity_use_case.equals(ACTIVITY_USE_CASE_NEW_ENTRY_INPUT)) {
                        holder.itemView.setOnClickListener(mModificationTextInputListener);
                    }
                    break;
                default:
                    contentText = "";
            }
            holder.mEntryTitle.setText(title);
            holder.mEntryContent.setText(contentText);
        }


        @Override
        public int getItemCount() {
            return (entryTitles.length);
        }

        // 内部类，定义viewholder (一条原始数据的界面)
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mEntryTitle;
            TextView mEntryContent;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            ViewHolder(View row) {
                super(row);
                mEntryTitle = row.findViewById(R.id.entry_title);
                mEntryContent = row.findViewById(R.id.entry_content);
            }
        }

    }

    private double changeDistanceInMetricToPreferred(double dInMetric) {
        if (unitPreference.equals("miles")) {
            return dInMetric / 1.6;
        }
        else {
            return dInMetric;
        }
    }

    private double changeDistanceInPreferredToMetric(double dInPreferred) {
        if (unitPreference.equals("miles")) {
            return dInPreferred * 1.6;
        }
        else {
            return dInPreferred;
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
                    // distance always be saved as kms
                    dataSource.createExerciseEntry(
                            MyPreferences.getInstance(ManualEntryActivity.this).getCurrentLoggedInUserEmail(),
                            INPUT_TYPE,activityType,dateStr,timeStr,
                            duration,distance,0,calorie,heartbeat,comment, null);
                    break;
                case ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY:
                    // 如果已经sync过，删除时需要连firebase记录一起删除，本地的删除交给firebase成功后的回调
                    if (entryForDisplayIsSyncedWithFirebase) {
                        FirebaseDatabase.getInstance().getReference().child(
                                MyPreferences.getInstance(ManualEntryActivity.this).getCurrentLoggedUserCloudId()).
                                child("exercise_entries").child(cloudKey).removeValue();
                    }
                    else {
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
}

