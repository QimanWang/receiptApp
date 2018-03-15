package com.next.groupmeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    //Declare the variable
    private EditText mEmail;
    private EditText mPassword;
    private Button mSignUpBtn;
    private TextView mLoginTextView;
    private static final String TAG = "GroupMeal";
    private FirebaseAuth mAuth;
    private TextView ErrorTextView;
    private ImageView mFaceBookSignupBtn;
    private CallbackManager callbackManager;/*CallBack for handling login*/
    private static final String EMAIL = "email";
    private LoginButton loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       // FacebookSdk.sdkInitialize(getApplicationContext());
        //FacebookSdk.sdkInitialize(this.getApplicationContext());


        callbackManager = CallbackManager.Factory.create();


        setContentView(R.layout.activity_sign_up); //  <-- ERROR

       // AppEventsLogger.activateApp(this);

        ErrorTextView= (TextView)findViewById(R.id.info);
        //EDIT
        loginButton = (LoginButton) findViewById(R.id.fblogin);


        loginButton.setReadPermissions(Arrays.asList(EMAIL));//  <-- ERROR

        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        ErrorTextView = (TextView) findViewById(R.id.errorTextView);



        findViewById(R.id.signupBtn).setOnClickListener(this);
       // findViewById(R.id.facebookSigUpButton).setOnClickListener(this);
        findViewById(R.id.fblogin).setOnClickListener(this);


    }

    //Create a user Account

    private void CreateUserAccount()
    {
        //Collect the input from user

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        //Check and validate the input

        if(email.isEmpty()){

            mEmail.setError("This field is required");
            mEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Invalid email format");
            mEmail.requestFocus();
            return;
        }

        //Validate the password

        if(password.isEmpty()){
            mPassword.setError("This field is required");
            mPassword.requestFocus();
            return;
        }
        if (password.length() <6){
            mPassword.setError("The password should have a least six characters");
            mPassword.requestFocus();
            return;
        }

        //Register the authentication input from the user

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    //if the user successfully sign up then launch the login page

                    startActivity(new Intent(SignUpActivity.this, loginPage.class));



                    Log.d(TAG, "Successfully Sign Up");
                }else {

                    ErrorTextView.setText("Sorry!! they was an error. Please try again");

                    if(task.getException() instanceof FirebaseAuthUserCollisionException){

                        ErrorTextView.setText("Sorry it looks like you have already sign up");

                        //Toast.makeText(getApplicationContext(), "You have already sign up", Toast.LENGTH_SHORT).show();
                    }else {

                        ErrorTextView.setText("Sorry!! they was an error. Please try again");

                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    //Signup with FaceBook

    @SuppressLint("SetTextI18n")
    private void SignUpWithFaceBook(){

        ErrorTextView.setText("This action is not yet available");

    }

    //End CreateUserAccount()

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.fblogin:
                startActivity(new Intent(SignUpActivity.this, loginPage.class));
                break;

            case R.id.signupBtn:
                CreateUserAccount();
                break;
            /*case R.id.facebookSigUpButton:
                SignUpWithFaceBook();
                break;*/
        }

    }
}
