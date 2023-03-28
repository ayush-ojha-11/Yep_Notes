package com.example.yepnotes;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int REQ_ONE_TAP = 1 ;
    private FirebaseAuth mAuth;
    private TextInputEditText emailInputEditText, passwordInputEditText;
    private String emailPattern;
    private ConstraintLayout parentLayout;

    //for google signIn
    private SignInClient oneTapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailInputEditText = findViewById(R.id.email);
        passwordInputEditText = findViewById(R.id.password);
        emailPattern = getString(R.string.emailPattern);
        parentLayout = findViewById(R.id.parentNewUser);

        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton signUpButton = findViewById(R.id.signUpButton);
        MaterialButton googleButton = findViewById(R.id.googleBtn);

        //Handling click on signUp button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(emailInputEditText.getText()).toString().trim();
                String password = Objects.requireNonNull(passwordInputEditText.getText()).toString().trim();

                // checking if email pattern is matched by input that user gives
                if (!email.matches(emailPattern)) {
                    emailInputEditText.setError("Enter a valid email!");
                }
                //checking whether password field is not left empty
                if (password.isEmpty()) {
                    passwordInputEditText.setError("Enter the password");
                }
                //create user only when both email and password are provided
                if (!email.isEmpty() && !password.isEmpty() && email.matches(emailPattern)) {
                    signUpNewUser(email, password);
                }
            }
        });

        //Handling click on login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(emailInputEditText.getText()).toString().trim();
                String password = Objects.requireNonNull(passwordInputEditText.getText()).toString().trim();

                // checking if email pattern is matched by input that user gives
                if (!email.matches(emailPattern)) {
                    emailInputEditText.setError("Enter a valid email!");
                }
                //checking whether password field is not left empty
                if (password.isEmpty()) {
                    passwordInputEditText.setError("Enter the password");
                }
                //create user only when both email and password are provided
                if (!email.isEmpty() && !password.isEmpty() && email.matches(emailPattern)) {
                    loginExistingUser(email, password);
                }
            }
        });

        //for google sign in

        oneTapClient = Identity.getSignInClient(this);
        // Show all accounts on the device.
        BeginSignInRequest signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.webClientId))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build()).build();

        oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {
                            startIntentSenderForResult(beginSignInResult.getPendingIntent()
                                    .getIntentSender(),REQ_ONE_TAP,null,0,0,0);
                        } catch (IntentSender.SendIntentException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //No saved account found
                    }
                });
    }
    private void signUpNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    updateUI(FirebaseAuth.getInstance().getCurrentUser());
                }
                else{
                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Snackbar.make(parentLayout,"Email is already registered!",Snackbar.LENGTH_SHORT).setAction("Login instead",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
    }

    private void loginExistingUser(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            updateUI(FirebaseAuth.getInstance().getCurrentUser());
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this,"Error in logging in!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            updateUI(FirebaseAuth.getInstance().getCurrentUser());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case REQ_ONE_TAP:

                try {
                   SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
                   String idToken = googleCredential.getGoogleIdToken();
                   if(idToken!=null){
                       AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                       mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(this,
                               new OnCompleteListener<AuthResult>() {
                                   @Override
                                   public void onComplete(@NonNull Task<AuthResult> task) {
                                       if(task.isSuccessful()){
                                           //successfully signedIn
                                           startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                       }
                                       else {
                                           Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });
                   }
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }


        }
    }
}