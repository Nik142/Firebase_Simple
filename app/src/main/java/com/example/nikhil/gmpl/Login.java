package com.example.nikhil.gmpl;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//This is the activity that controls login process.
public class Login extends AppCompatActivity {

    TextInputEditText mail,password;
    TextInputLayout mails,passwords;
    Button log,logout,verify;
    String email,pass,name;
    TextView stat;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mail = findViewById(R.id.LoginIdtext);
        password = findViewById(R.id.loginPassText);
        log = findViewById(R.id.loginBtn);
        stat = findViewById(R.id.loginStat);
        logout = findViewById(R.id.logout);
        verify = findViewById(R.id.loginVerify);

        mails = findViewById(R.id.loginId);
        passwords = findViewById(R.id.loginPass);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users");
    }

    public void signin(View view){

        email = mail.getText().toString();
        pass = password.getText().toString();

        mails.setVisibility(View.INVISIBLE);
        passwords.setVisibility(View.INVISIBLE);
        log.setVisibility(View.INVISIBLE);

        //Login into user account with id and password entered.
        mAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            user = mAuth.getCurrentUser();

                            //Checks if email id is verified or not.
                            //If email is not verified then user name is not displayed.
                            if(user.isEmailVerified()){
                                ref.child(user.getUid()).child("Name").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        name = dataSnapshot.getValue(String.class);
                                        stat.setText("Hello " + name);
                                        logout.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else{

                                stat.setText("Your Email Id is not verified.\nPlease try again.");
                                verify.setVisibility(View.VISIBLE);
                                logout.setVisibility(View.VISIBLE);
                            }
                        }
                        else{
                            stat.setText("Login process failed.");
                        }
                    }
                });

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    }

    public void signout(View view){

        //Logout of user account.
        mAuth.signOut();
        startActivity(new Intent(Login.this,Startup.class));
        Login.this.finish();
    }

    public void verify(final View view){

        email = mail.getText().toString();
        pass = password.getText().toString();

        user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                verify.setEnabled(false);
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Verification email sent to " + user.getEmail(),Toast.LENGTH_LONG).show();
                    Snackbar snackbar = Snackbar.make(view,"Please verify your account and login to view your account.",Snackbar.LENGTH_LONG);
                    snackbar.show();

                    startActivity(new Intent(Login.this,Startup.class));
                } else{
                    verify.setEnabled(true);
                    Toast.makeText(getApplicationContext(),"Verification email cannot be sent",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
