package edu.dartmouth.cs.myrun;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StartFragment extends Fragment {
    FloatingActionButton mStartRecordingBtn;
    Spinner mInputTypeSpinner;
    Spinner mActivityTypeSpinner;

    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartRecordingBtn = view.findViewById(R.id.start_frag_start_recording_btn);
        mInputTypeSpinner = view.findViewById(R.id.start_frag_input_spinner);
        mActivityTypeSpinner = view.findViewById(R.id.start_frag_activity_spinner);


        mInputTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mInputTypeSpinner.getItemAtPosition(position).toString().equals("Automatic")) {
                    mActivityTypeSpinner.setEnabled(false);
                }
                else {
                    mActivityTypeSpinner.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mActivityTypeSpinner.setEnabled(true);
            }
        });

        mStartRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputTypeSpinner.getSelectedItem().toString()
                        .equals(getString(R.string.start_input_type_manual))) {
                    // 打开Manual Entry activity
                    Intent intent = new Intent(getActivity(), ManualEntryActivity.class);
                    intent.putExtra(ManualEntryActivity.INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE, mActivityTypeSpinner.getSelectedItem().toString());
                    intent.putExtra(ManualEntryActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE, ManualEntryActivity.ACTIVITY_USE_CASE_NEW_ENTRY_INPUT);
                    startActivity(intent);
                }
                else if (mInputTypeSpinner.getSelectedItem().toString().
                        equals(getString(R.string.start_input_type_gps))){
                    // 打开Map
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    // activity type 还是自己选的
                    intent.putExtra(MapActivity.INTENT_EXTRA_KEY_INPUT_TYPE, getString(R.string.start_input_type_gps));
                    intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ENTRY_ACTIVITY_TYPE, mActivityTypeSpinner.getSelectedItem().toString());
                    intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE, MapActivity.ACTIVITY_USE_CASE_NEW_ENTRY_INPUT);
                    startActivity(intent);
                }
                else {
                    // 打开Map
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(MapActivity.INTENT_EXTRA_KEY_INPUT_TYPE, getString(R.string.start_input_type_automatic));
                    intent.putExtra(MapActivity.INTENT_EXTRA_KEY_ACTIVITY_USE_CASE, MapActivity.ACTIVITY_USE_CASE_NEW_ENTRY_INPUT);
                    startActivity(intent);
                }
            }
        });
    }
}
