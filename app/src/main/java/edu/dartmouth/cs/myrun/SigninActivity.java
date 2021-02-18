package edu.dartmouth.cs.myrun;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninAct.lifecycle";

    Button mSignInButton;
    Button mRegisterButton;
    EditText mEmail;
    EditText mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Log.d(TAG, "onCreate()");

        mSignInButton = findViewById(R.id.sign_in_button);
        mRegisterButton = findViewById(R.id.register_button);
        mEmail = findViewById(R.id.edit_email);
        mPassword = findViewById(R.id.edit_password);

        getSupportActionBar().setTitle(R.string.sign_in);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start the AccountManagerActivity
                Intent intent = new Intent(SigninActivity.this, RegisterActivity.class);

                startActivity(intent);
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if match
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                // 如果使用firebase auth
                if (MyPreferences.FIREBASE_AUTH_ON) {
                    // entry point of the Firebase Authentication
                    final FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmailAndPassword:success");
                                        MyPreferences.getInstance(SigninActivity.this).setCurrentLoggedInUserEmail(email);
                                        MyPreferences.getInstance(SigninActivity.this).setCurrentLoggedInCloudUserID(mAuth.getCurrentUser().getUid());
                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                        Toast.makeText(SigninActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                // 如果不使用 firebase
                else {
                    if (email.equals(MyPreferences.getInstance(getApplicationContext()).getPrefsEmail()) &&
                            password.equals(MyPreferences.getInstance(getApplicationContext()).getPrefsPassword())){
                        MyPreferences.getInstance(SigninActivity.this).setCurrentLoggedInUserEmail(email);
                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Authentication failed." ,
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }


    @Override
    public void onBackPressed() {
        // do nothing
    }

}

