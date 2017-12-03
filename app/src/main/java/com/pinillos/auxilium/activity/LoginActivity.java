package com.pinillos.auxilium.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pinillos.auxilium.Others.SendPasswordResetEmail;
import com.pinillos.auxilium.Others.SignUpNewUserActivity;
import com.pinillos.auxilium.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //Firebase authe
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // UI references
    private EditText mEmail, mPassword;
    private TextView signUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);



        //Get auth instance
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    //Go to main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        //Reference UI items
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        signUpText = findViewById(R.id.singUpText);
}

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        //Remove AuthListerner
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void goToSignUpNewUserActivity(View view)
    {
        //Go to signUpNewUser activity
        Intent intent = new Intent(LoginActivity.this, SignUpNewUserActivity.class);
        startActivity(intent);
    }

    public void goToSendPasswordResetEmailActivity(View view)
    {
        //Go to main activity
        Intent intent = new Intent(LoginActivity.this, SendPasswordResetEmail.class);
        startActivity(intent);
    }



    public void logIn(View view)
    {
        String email = mEmail.getText().toString();
        String pass = mPassword.getText().toString();



        if(!email.equals("") && !pass.equals(""))
        {
            //ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "Espere por favor", "Conectandose...", false);
            final ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "Conectando", "Espere por favor");

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                dialog.dismiss();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());

                                String excetionType = task.getException().toString().split(":")[0];
                                switch (excetionType)
                                {
                                    //wrong password
                                    case "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException":
                                        toastMessage("La contraseña es incorrecta");
                                    break;

                                    //user does not exist
                                    case "com.google.firebase.auth.FirebaseAuthInvalidUserException":
                                        toastMessage("El usuario no existe");
                                    break;
                                }
                                //updateUI(null);
                            }

                            // ...
                        }
                    });

        //check if email or pass are empty
        }else if (email.equals("")){
            toastMessage("Introduzca su correo electrónico");
        }else if (pass.equals("")){
            toastMessage("Introduzca su contraseña");
        }
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
