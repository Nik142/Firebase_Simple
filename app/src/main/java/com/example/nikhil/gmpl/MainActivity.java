package com.example.nikhil.gmpl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

//This is the activity that controls Sign Up process.
public class MainActivity extends AppCompatActivity {

    TextInputEditText name,mail,password;
    Button signUp,verify;
    String userName,userMail,userPassword;
    TextView t,t1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.edit_name);
        mail = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        signUp = findViewById(R.id.signUp);
        verify = findViewById(R.id.verifyBtn);
        t = findViewById(R.id.statusText);
        t1 = findViewById(R.id.note);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void create(View view){

        userName = name.getText().toString();
        userMail = mail.getText().toString();
        userPassword = password.getText().toString();

        //Checks if all fields all filled by the user.
        if(!validateForm()){
            return;
        }

        //Creates a account with entered email and password.
        mAuth.createUserWithEmailAndPassword(userMail,userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            user = mAuth.getCurrentUser();
                            t.setText("Trying to Sign in with: " + userMail + "\n\nVerification Status: " + user.isEmailVerified());

                            //Stores user name in database with key as user id.
                            ref.child(user.getUid()).child("Name").setValue(userName);

                            verify.setVisibility(View.VISIBLE);
                            t1.setText("The verification email may take some time to reflect. Kindly check your Spam folder as well.");
                            t1.setVisibility(View.VISIBLE);
                        } else {
                            t.setText("Sign Up failed.\nPlease try again.");
                        }
                    }
                });

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    }

    public void Verify(final View view){

        userName = name.getText().toString();
        userMail = mail.getText().toString();
        userPassword = password.getText().toString();

        user = mAuth.getCurrentUser();
        //Sends verification email to the user.
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                verify.setEnabled(false);
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Verification email sent to " + user.getEmail(),Toast.LENGTH_LONG).show();

                    startActivity(new Intent(MainActivity.this,Startup.class));
                    MainActivity.this.finish();
                } else{
                    verify.setEnabled(true);
                    Toast.makeText(getApplicationContext(),"Verification email cannot be sent",Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    //Checks if all fields have valid data.
    private boolean validateForm() {
        boolean valid = true;

        userName = name.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            name.setError("Required.");
            valid = false;
        } else {
            name.setError(null);
        }

        userMail = mail.getText().toString();
        if (TextUtils.isEmpty(userMail)) {
            mail.setError("Required.");
            valid = false;
        } else {
            mail.setError(null);
        }

        userPassword = password.getText().toString();
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }
}
