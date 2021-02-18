package edu.dartmouth.cs.myrun;



import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;


import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.dartmouth.cs.myrun.dblayer.ExerciseEntry;
import edu.dartmouth.cs.myrun.dblayer.ExerciseEntryDataSource;


public class BoardFragment extends Fragment {
    RecyclerView mRecyclerView;
    private BoardItemAdapter mAdapter;
    private List<BoardItem> allBoardData;

    public static BoardFragment newInstance() {
        BoardFragment fragment = new BoardFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BoardItemAdapter();
        allBoardData = new LinkedList<>();
        startLoadSocialData();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 定义fragment 的 xml, 然后才能有view
        return inflater.inflate(R.layout.fragment_board, container, false);
    }


    //已经有view了
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.fra_board_rcl);
        //RecyclerView提供的布局管理器：
        //LinearLayoutManager 以垂直或水平滚动列表方式显示项目。
        //GridLayoutManager 在网格中显示项目。
        //StaggeredGridLayoutManager 在分散对齐网格中显示项目。
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity())); //这里用线性显示 类似于listview
        mAdapter = new BoardItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        // 添加行间横线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }


    class BoardItem {
        private String userEmail;
        private String inputType;
        private String activityType;
        private String dateStr;
        private String duration;
        private String distance;

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public String getActivityType() {
            return activityType;
        }

        public void setActivityType(String activityType) {
            this.activityType = activityType;
        }

        public String getDateStr() {
            return dateStr;
        }

        public void setDateStr(String dateStr) {
            this.dateStr = dateStr;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }
    }

    // 内部类，定义adapter (所有原始数据到界面显示的中间层)
    class BoardItemAdapter extends RecyclerView.Adapter<BoardItemAdapter.ViewHolder> {
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
        public BoardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return (new BoardItemAdapter.ViewHolder(getLayoutInflater().inflate(
                    R.layout.row_board_entry, parent, false)));
        }

        // 一条数据怎么和一个viewHolder绑定
        @Override
        public void onBindViewHolder(BoardItemAdapter.ViewHolder holder, int position) {
            // Set item views based on your views and data model
            final BoardItem item = allBoardData.get(position);
            holder.mEntryTitle.setText(item.getInputType() + ": " + item.getActivityType());
            holder.mEntryDateTime.setText(item.getDateStr());
            String unitPref = MyPreferences.getInstance(BoardFragment.this.getActivity()).getUnitPreference();
            double distance = Double.parseDouble(item.getDistance()); //always in kms
            if (unitPref.equals("miles")) {
                distance /= 1.6;
            }
            holder.mEntryContent.setText(//保留小数点后两位
                    String.format("%.2f", distance) + " " + unitPref + ", " +
                            String.format("%.2f", Double.parseDouble(item.getDuration())) + " mins");
            holder.mUserEmail.setText(item.getUserEmail());
        }

        @Override
        public int getItemCount() {
            return (allBoardData.size());
        }

        // 内部类，定义viewholder (一条原始数据的界面)
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mEntryTitle;
            TextView mEntryDateTime;
            TextView mEntryContent;
            TextView mUserEmail;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            ViewHolder(View row) {
                super(row);
                mEntryTitle = row.findViewById(R.id.entry_title);
                mEntryDateTime = row.findViewById(R.id.entry_datetime);
                mEntryContent = row.findViewById(R.id.entry_content);
                mUserEmail = row.findViewById(R.id.user_email);
            }
        }
    }


    private void startLoadSocialData() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, "http://129.170.214.246:5000/get_exercises", null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Parse the JSON array and each JSON objects inside it
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jo = response.getJSONObject(i);
                                BoardItem item = new BoardItem();
                                item.setInputType(jo.getString("input_type"));
                                item.setActivityType(jo.getString("activity_type"));
                                item.setDateStr(jo.getString("activity_date"));
                                item.setDistance(jo.getString("distance"));
                                item.setDuration(jo.getString("duration"));
                                item.setUserEmail(jo.getString("email"));
                                allBoardData.add(0, item);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        // 刷新界面
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.getMessage() != null)
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonArrayRequest);
    }


    public final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void syncWithSocialServer() {
        // Simple snippet for inserting data
        List<ExerciseEntry> allEntriesData = ((MainActivity)getActivity()).mHistoryFragment.getAllEntriesData();
        for (int i = 0; i < allEntriesData.size(); i++) {
            final ExerciseEntry entry = allEntriesData.get(i);
            if (!entry.isBoarded()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    if(MyPreferences.getInstance(getActivity()).isAnonymousPost())
                        jsonObject.put("email", md5(entry.getUserEmail()));
                    else
                        jsonObject.put("email", entry.getUserEmail());

                    jsonObject.put("activity_type", entry.getActivityType());
                    jsonObject.put("activity_date", entry.getDateStr());
                    jsonObject.put("input_type", entry.getInputType());
                    jsonObject.put("duration", entry.getDuration() + "");
                    jsonObject.put("distance", entry.getDistance() + "");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, "http://129.170.214.246:5000/upload_exercise", jsonObject, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if(response.has("result") && response.getString("result").equalsIgnoreCase("success")){
                                        // Server operation is successful.
                                        // 成功上传后，如果已经同步过过firebase，则要把isBoarded=true提交到firebase，否则只需要更改本地
                                        if (entry.isSynced()) {
                                            FirebaseDatabase.getInstance().getReference().
                                                    child(MyPreferences.getInstance(getActivity()).
                                                            getCurrentLoggedUserCloudId()).
                                                    child("exercise_entries").
                                                    child(entry.getCloudKey()).
                                                    child("boarded").setValue(true);
                                            // 在firebase sync成功后，onChildChanged 事件会被触发，那里面会写入本地sql，所以我们没必要在这里调了
                                        }
                                        else {
                                            entry.setBoarded(true);
                                            HistoryFragment.DataModifyTaskParameters params = new HistoryFragment.DataModifyTaskParameters(
                                                    HistoryFragment.DataModifyTaskParameters.TASK_UPDATE_ENTRY,
                                                    entry
                                            );
                                            HistoryFragment.DataModifyTask myTask = ((MainActivity)getActivity()).mHistoryFragment.new DataModifyTask();
                                            myTask.execute(params);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                if(error.getMessage() != null)
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }){
                };

                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(jsonObjectRequest);


            }
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLoadSocialData();
            }
        }, 2000);
    }
}
