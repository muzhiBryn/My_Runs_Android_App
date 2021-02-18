package edu.dartmouth.cs.myrun;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AccountManagerActivity {

    // entry point of the Firebase Authentication

    private static String TAG = "REGISTER_USER_ACCOUNT";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.sign_up);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // handle 右上角 REGISTER button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            if (checkInputValidation()) {

                mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    MyPreferences myPref = MyPreferences.getInstance(getApplicationContext());
                                    //一定要先调用setCurrentLoggedInUserEmail
                                    myPref.setCurrentLoggedInUserEmail(mEmail.getText().toString());
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

                                    Toast.makeText(RegisterActivity.this, "saved", Toast.LENGTH_SHORT).show();
                                    onSupportNavigateUp();

                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
        }

        return super.onOptionsItemSelected(item);
    }

}
