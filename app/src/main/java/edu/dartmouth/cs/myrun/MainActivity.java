package edu.dartmouth.cs.myrun;


import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity.lifecycle";

    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private BottomNavAdapter myViewPageAdapter;
    BottomNavigationView navigation;
    StartFragment mStartFragment;
    HistoryFragment mHistoryFragment;
    BoardFragment mBoardFragment;
    MenuItem mSyncBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.main_viewpager);
        navigation = findViewById(R.id.bottom_navigation);

        // create a fragment list in order.
        fragments = new ArrayList();
        mStartFragment = StartFragment.newInstance();
        mHistoryFragment = HistoryFragment.newInstance();
        mBoardFragment = BoardFragment.newInstance();
        fragments.add(mStartFragment);
        fragments.add(mHistoryFragment);
        fragments.add(mBoardFragment);

        // use FragmentPagerAdapter to bind the TabLayout (tabs with different titles)
        // and ViewPager (different pages of fragment) together.
        myViewPageAdapter = new BottomNavAdapter(getSupportFragmentManager(), fragments);

        // add the PagerAdapter to the viewPager
        viewPager.setAdapter(myViewPageAdapter);

        // 底部navigation bar切换
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_start:
                        // switch to start fragment
                        viewPager.setCurrentItem(0);
                        mSyncBtn.setVisible(false);
                        return true;
                    case R.id.navigation_history:
                        // switch to history fragment
                        viewPager.setCurrentItem(1);
                        mSyncBtn.setVisible(true);
                        return true;
                    case R.id.navigation_board:
                        // switch to history fragment
                        viewPager.setCurrentItem(2);
                        mSyncBtn.setVisible(true);
                        return true;
                }
                return false;
            }
        } );


        Log.d(TAG, "onCreate()");
        Log.d(TAG, MyPreferences.getInstance(this).getCurrentLoggedInUserEmail());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MyPermissionChecker.getInstance().checkMapPermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    // create an action bar button
    // The onCreate method is called first, and before it finishes onCreateOptionsMenu is called.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSyncBtn = menu.findItem(R.id.sync);
        if (navigation.getSelectedItemId() == R.id.navigation_start) {
            // start fragment
            mSyncBtn.setVisible(false);
        }
        else {
            mSyncBtn.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }


    public void menuSettingsClicked(MenuItem item) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void menuProfileClicked(MenuItem item) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void menuSyncClicked(MenuItem item) {
        switch (navigation.getSelectedItemId()) {
            case R.id.navigation_history:
                // 同步firebase
                mHistoryFragment.syncWithFirebase();
                break;
            case R.id.navigation_board:
                // 同步board
                mBoardFragment.syncWithSocialServer();
                break;
        }
    }


    // 内部类swti
    class BottomNavAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;

        public BottomNavAdapter(FragmentManager mgr, ArrayList<Fragment> fragments) {
            super(mgr);
            this.fragments = fragments;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
    }
}
