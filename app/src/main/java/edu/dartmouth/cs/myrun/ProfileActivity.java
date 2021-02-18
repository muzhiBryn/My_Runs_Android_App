package edu.dartmouth.cs.myrun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AccountManagerActivity {
    String originalPassword;

    private static String TAG = "REGISTER_USER_ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.act_profile_bar_title);


        MyPreferences myPref = MyPreferences.getInstance(getApplicationContext());
        mEmail.setText(myPref.getPrefsEmail());
        mEmail.setEnabled(false);
        int genderIdx = myPref.getPrefsGender();
        if (genderIdx >= 0) {
            ((RadioButton)mRadioGroup.getChildAt(genderIdx)).setChecked(true);
        }
        mName.setText(myPref.getPrefsName());
        originalPassword = myPref.getPrefsPassword();
        mPassword.setText(myPref.getPrefsPassword());
        mPhone.setText(myPref.getPrefsPhone());
        mMajor.setText(myPref.getPrefsMajor());
        mDartmouthClass.setText(myPref.getPrefsDartmouthClass());
        Bitmap bitmap = BitmapFactory.decodeFile(myPref.getPrefsAvatarPath());
        if (myPref.getPrefsAvatarPath() != null) {
            mAvatar.setImageBitmap(imageOreintationValidator(bitmap, myPref.getPrefsAvatarPath()));
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // handle 右上角 SAVE button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.profile_menu_save) {
            if (checkInputValidation()) {
                mAuth.getCurrentUser().updatePassword(mPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyPreferences myPref = MyPreferences.getInstance(ProfileActivity.this);
                            myPref.saveUserData(
                                    mEmail.getText().toString(),
                                    mRadioGroup.indexOfChild(findViewById(mRadioGroup
                                            .getCheckedRadioButtonId())),
                                    mName.getText().toString(),
                                    mPassword.getText().toString(),
                                    mPhone.getText().toString(),
                                    mMajor.getText().toString(),
                                    mDartmouthClass.getText().toString(),
                                    avatarUri != null ? avatarUri.getPath() : null);

                            Toast.makeText(ProfileActivity.this, "saved", Toast.LENGTH_SHORT).show();
                            onSupportNavigateUp();
                        } else {
                            Log.w(TAG, "updateUser:failure", task.getException());
                            Toast.makeText(ProfileActivity.this, "Update failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!originalPassword.equals(mPassword.getText().toString())) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
        }
        return super.onSupportNavigateUp();
    }
}
