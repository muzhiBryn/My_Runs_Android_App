package edu.dartmouth.cs.myrun;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import edu.dartmouth.cs.myrun.dblayer.ExerciseEntry;
import edu.dartmouth.cs.myrun.dblayer.ExerciseEntryDataSource;
import edu.dartmouth.cs.myrun.dblayer.ExerciseEntryListLoader;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ExerciseEntry>>{
    private static final String TAG = "HistoryFragment";

    private static final int ALL_EXERCISE_ENTRY_LOADER_FROM_SQLITE_ID = 1;


    RecyclerView mRecyclerView;
    private HistoryItemAdapter mAdapter;

    public List<ExerciseEntry> getAllEntriesData() {
        return allEntriesData;
    }

    private List<ExerciseEntry> allEntriesData;
    private HashMap<String, Long> allDataCloudKeys; //用来存放本地的有的cloudkey -> sqlite id

    private DatabaseReference mFirebaseDatabase;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allEntriesData = new ArrayList<>();
        allDataCloudKeys = new HashMap<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }


//    addListenerForSingleValueEvent(ValueEventListener listener)
//    addValueEventListener(ValueEventListener listener)
//    addChildEventListener(ChildEventListener listener)
//    addListenerForSingleValueEvent 主要用于一次性获取当前节点下数据的场景，触发一次后就会失效。
//    addValueEventListener 和 addChildEventListener 都会为当前节点绑定监听事件，持续的监听当前节点数据的变化情况，
//    ValueListener会将当前节点下的数据一次性返回;
//    ChildListener将当前节点数据按子节点一个一个返回。

    private void registerFirebaseListener(DatabaseReference firebaseDatabase) {
        firebaseDatabase.child(MyPreferences.getInstance(getActivity()).getCurrentLoggedUserCloudId()).
                child("exercise_entries").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // 如果本地没有这个cloud记录：存入sqlite
                if (!allDataCloudKeys.containsKey(dataSnapshot.getKey())) {
                    ExerciseEntry entry = new ExerciseEntry(dataSnapshot);
                    // 用asynctask 存
                    DataModifyTaskParameters params = new DataModifyTaskParameters(
                            DataModifyTaskParameters.TASK_INSERT_ENTRY,
                            entry
                    );
                    DataModifyTask myTask = new DataModifyTask();
                    myTask.execute(params);
                    // 对于insert，因为还咩有id, 通知adpter 刷新界面 放在task里
                }
            }

            @Override

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ExerciseEntry entry = new ExerciseEntry(dataSnapshot);
                if (allDataCloudKeys.containsKey(dataSnapshot.getKey())) {
                    long entryId = allDataCloudKeys.get(entry.getCloudKey());
                    entry.setId(entryId);
                    // 用asynctask 改
                    DataModifyTaskParameters params = new DataModifyTaskParameters(
                            DataModifyTaskParameters.TASK_UPDATE_ENTRY,
                            entry
                    );
                    DataModifyTask myTask = new DataModifyTask();
                    myTask.execute(params);
                    for (ExerciseEntry entry2: allEntriesData) {
                        if (entry2.getId() == entryId) {
                            entry2.setExerciseEntry(entry);
                            break;
                        }
                    }
                    // 通知adpter 刷新界面
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                ExerciseEntry entry = new ExerciseEntry(dataSnapshot);
                if (allDataCloudKeys.containsKey(dataSnapshot.getKey())) {
                    long entryId = allDataCloudKeys.get(entry.getCloudKey());
                    entry.setId(entryId);
                    // 用asynctask 删
                    DataModifyTaskParameters params = new DataModifyTaskParameters(
                            DataModifyTaskParameters.TASK_DELETE_ENTRY,
                            entry
                    );
                    DataModifyTask myTask = new DataModifyTask();
                    myTask.execute(params);
                    for (ExerciseEntry entry2: allEntriesData) {
                        if (entry2.getId() == entryId) {
                            allEntriesData.remove(entry2);
                            break;
                        }
                    }
                    // 通知adpter 刷新界面
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //???
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 定义fragment 的 xml, 然后才能有view
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    //已经有view了
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.fra_history_rcl);
        //RecyclerView提供的布局管理器：
        //LinearLayoutManager 以垂直或水平滚动列表方式显示项目。
        //GridLayoutManager 在网格中显示项目。
        //StaggeredGridLayoutManager 在分散对齐网格中显示项目。
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity())); //这里用线性显示 类似于listview
        mAdapter = new HistoryItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        // 添加行间横线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    // 为什么要放在resume里？如果你去startFragment 插入了一条记录，我希望在返回historyFrag 的时候能看到这条记录
    // 这时候onCreate是不会被调用的，所以我要在onResume里面重新调用load.
    @Override
    public void onResume() {
        super.onResume();
        LoaderManager mLoader = getActivity().getSupportLoaderManager();
        // 发出一个load指令，callback对象为自己。
        // load 开始时，调用自己的 onCreateLoader
        // 当load结束后，会调用自己的onLoadFinished
        mLoader.initLoader(ALL_EXERCISE_ENTRY_LOADER_FROM_SQLITE_ID, null, this).forceLoad();
    }

    // Async task loader callback functions
    // this method is for creating different loaders.
    // 取得data 的loader
    @NonNull
    @Override
    public Loader<List<ExerciseEntry>> onCreateLoader(int id, @Nullable Bundle args) {
        if(id == ALL_EXERCISE_ENTRY_LOADER_FROM_SQLITE_ID){
            // create an instance of the loader in our case AsyncTaskLoader
            // which loads a List of Entry List<ExerciseEntry>
            return new ExerciseEntryListLoader(getActivity(), MyPreferences.getInstance(getActivity()).getCurrentLoggedInUserEmail());
        }
        return null;
    }

    // this method will be called when loader finishes its task.
    // 用data 的loader 取得data后，data传入此方法
    @Override
    public void onLoadFinished(@NonNull Loader<List<ExerciseEntry>> loader, List<ExerciseEntry> data) {
        if(loader.getId() == ALL_EXERCISE_ENTRY_LOADER_FROM_SQLITE_ID){
            // returns the List<Comment> from queried from the db
            // Use the UI with the adapter to show the elements in a ListView
            if(data.size() > 0){
                // 更新数据
                allEntriesData = data;
                allDataCloudKeys.clear();
                for (ExerciseEntry entry: allEntriesData) {
                    // 如果sync过
                    if (entry.isSynced()) {
                        allDataCloudKeys.put(entry.getCloudKey(), entry.getId());
                    }
                }
                // force notification -- tell the adapter to display
                // 通知adpter 刷新界面
                mAdapter.notifyDataSetChanged();
            }
            // load 完sqlite 在链接firebase 把在cloud上但不在本地的数据附加上来(listener的onChildAdded方法)
            registerFirebaseListener(mFirebaseDatabase);
        }

    }
    // this method will be called after invoking loader.restart()
    @Override
    public void onLoaderReset(@NonNull Loader<List<ExerciseEntry>> loader) {
        if(loader.getId() == ALL_EXERCISE_ENTRY_LOADER_FROM_SQLITE_ID){
            allEntriesData.clear();
            mAdapter.notifyDataSetChanged();
        }
    }



    // 内部类，定义adapter (所有原始数据到界面显示的中间层)
    class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.ViewHolder> {
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
        public HistoryItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return (new HistoryItemAdapter.ViewHolder(getLayoutInflater().inflate(
                    R.layout.row_history_entry, parent, false)));
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(HistoryItemAdapter.ViewHolder holder, int position) {
            // Set item views based on your views and data model
            final ExerciseEntry entry = allEntriesData.get(position);
            holder.mEntryTitle.setText(entry.getInputType() + ": " + entry.getActivityType());
            holder.mEntryDateTime.setText(entry.getDateStr() + " " + entry.getTimeStr());
            String unitPref = MyPreferences.getInstance(HistoryFragment.this.getActivity()).getUnitPreference();
            double distance = entry.getDistance(); //always in kms
            if (unitPref.equals("miles")) {
                distance /= 1.6;
            }
            holder.mEntryContent.setText(//保留小数点后两位
                    String.format("%.2f", distance) + " " + unitPref + ", " +
                            String.format("%.2f", entry.getDuration()) + " mins");
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (entry.getInputType().equals(getString(R.string.start_input_type_manual))) {
                                // 打开 ManualEntryActivity
                                Intent intent = new Intent(getActivity(), ManualEntryActivity.class);
                                intent.putExtra(ManualEntryActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE,
                                        ManualEntryActivity.ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY);
                                intent.putExtra(ManualEntryActivity.INTENT_EXTRA_KEY_ENTRY_OBJECT, entry);
                                startActivity(intent);
                            }
                            else if (entry.getInputType().equals(getString(R.string.start_input_type_gps))) {
                                // 打开Map (GPS)
                                Intent intent = new Intent(getActivity(), MapActivity.class);
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE,
                                        MapActivity.ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY);
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_INPUT_TYPE, getString(R.string.start_input_type_gps));
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ENTRY_OBJECT, entry);
                                startActivity(intent);
                            }
                            else if (entry.getInputType().equals(getString(R.string.start_input_type_automatic))) {
                                // 打开Map (automatic)
                                Intent intent = new Intent(getActivity(), MapActivity.class);
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE,
                                        MapActivity.ACTIVITY_USE_CASE_EXISTING_ENTRY_DISPLAY);
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_INPUT_TYPE, getString(R.string.start_input_type_automatic));
                                intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ENTRY_OBJECT, entry);
                                startActivity(intent);
                            }
                        }
                    }
            );
        }


        @Override
        public int getItemCount() {
            return (allEntriesData.size());
        }

        // 内部类，定义viewholder (一条原始数据的界面)
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mEntryTitle;
            TextView mEntryDateTime;
            TextView mEntryContent;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            ViewHolder(View row) {
                super(row);
                mEntryTitle = row.findViewById(R.id.entry_title);
                mEntryDateTime = row.findViewById(R.id.entry_datetime);
                mEntryContent = row.findViewById(R.id.entry_content);
            }
        }

    }


    public void syncWithFirebase() {
        // Simple snippet for inserting data
        for (int i = 0; i < allEntriesData.size(); i++) {
            final ExerciseEntry entry = allEntriesData.get(i);
            if (!entry.isSynced()) {
                syncOneEntryWithFirebase(entry);
            }
        }
    }


    public void syncOneEntryWithFirebase (final ExerciseEntry entry) {
        // 向firebase insert新纪录，调用push后，firebase会生成一个random的key
        //
        DatabaseReference pushRef = mFirebaseDatabase.child(
                MyPreferences.getInstance(getActivity()).getCurrentLoggedUserCloudId()).child("exercise_entries").push();
        // 拿到push生成的key
        final String cloudKey = pushRef.getKey();
        // 本地保存push 的key
        entry.setCloudKey(cloudKey);
        allDataCloudKeys.put(cloudKey, entry.getId());
        // 上传到firebase
        pushRef.setValue(entry).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Insert is done!
                    DataModifyTaskParameters params = new DataModifyTaskParameters(
                            DataModifyTaskParameters.TASK_UPDATE_ENTRY,
                            entry
                    );
                    DataModifyTask myTask = new DataModifyTask();
                    myTask.execute(params);
                }else{
                    // Failed
                    // 改回来
                    entry.setCloudKey(ExerciseEntry.DEFAULT_NOT_SYNCED_CLOUD_KEY);
                    allDataCloudKeys.remove(cloudKey);
                    if(task.getException() != null)
                        Log.w(TAG, task.getException().getMessage());
                }
            }
        });
    }


    public static class DataModifyTaskParameters {
        public static final String TASK_UPDATE_ENTRY = "TASK_UPDATE_ENTRY";
        public static final String TASK_INSERT_ENTRY = "TASK_INSERT_ENTRY";
        public static final String TASK_DELETE_ENTRY = "TASK_DELETE_ENTRY";

        String operation;
        ExerciseEntry entry;

        public DataModifyTaskParameters(String operation, ExerciseEntry entry) {
            this.operation = operation;
            this.entry = entry;
        }
    }


    // 内部类，定义Sqlite操作AsyncTask
    public class DataModifyTask extends AsyncTask<DataModifyTaskParameters, Integer, Void> {
        ExerciseEntry entry;
        String operation;

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
        protected Void doInBackground(DataModifyTaskParameters... params) {
            ExerciseEntryDataSource mExerciseEntrySqliteDataSource = new ExerciseEntryDataSource(getActivity());
            DataModifyTaskParameters myParams = params[0];
            entry = myParams.entry;
            operation = myParams.operation;

            if (operation.equals(DataModifyTaskParameters.TASK_UPDATE_ENTRY)) {
                mExerciseEntrySqliteDataSource.updateExerciseEntry(entry);
            }
            else if (operation.equals(DataModifyTaskParameters.TASK_INSERT_ENTRY)) {
                // insert to sqlite
                long insertId = mExerciseEntrySqliteDataSource.createExerciseEntry(entry).getId();
                entry.setId(insertId);
            }
            else if (operation.equals(DataModifyTaskParameters.TASK_DELETE_ENTRY)) {
                // delete from sqlite
                mExerciseEntrySqliteDataSource.deleteExerciseEntry(entry.getId());
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
            if (operation.equals(DataModifyTaskParameters.TASK_UPDATE_ENTRY)) {

            }
            else if (operation.equals(DataModifyTaskParameters.TASK_INSERT_ENTRY)) {
                if (entry.isSynced()) {
                    allDataCloudKeys.put(entry.getCloudKey(), entry.getId());
                }
                allEntriesData.add(entry);
                // 通知adpter 刷新界面
                mAdapter.notifyDataSetChanged();
            }
            else if (operation.equals(DataModifyTaskParameters.TASK_DELETE_ENTRY)) {
            }

        }
    }
}
