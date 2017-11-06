package com.pinillos.auxilium;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class SignUpNewUserActivity extends AppCompatActivity {


    private static final String TAG = "SignUpNewUserActivity";
    private EditText firstName, lastName1, lastName2, dni, email, password1, password2;
    private Spinner spinnerGroup;
    private Spinner spinnerCategory;
    private String group;
    private String category;

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapterGroups;
    ArrayAdapter<CharSequence> adapterCategories;

    private FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up_new_user);

        //Reference ui
        firstName = findViewById(R.id.first_name_editText);
        lastName1 = findViewById(R.id.last_name1_editText);
        lastName2 = findViewById(R.id.last_name2_editText);
        dni = findViewById(R.id.dni_editText);
        email = findViewById(R.id.email_editText);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        password1 = findViewById(R.id.password1_editText);
        password2 = findViewById(R.id.password2_editText);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();


        adapterGroups = ArrayAdapter.createFromResource(this, R.array.groups_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerGroup.setAdapter(adapterGroups);

        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                group = spinnerGroup.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterCategories = ArrayAdapter.createFromResource(this, R.array.categories_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerCategory.setAdapter(adapterCategories);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spinnerCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void singUpNewUser(View view)
    {
        final String emailText = email.getText().toString();
        final String passwordText1 = password1.getText().toString();
        final String passwordText2 = password1.getText().toString();



        if(!emailText.equals("") && !passwordText1.equals("") && passwordText1.equals(passwordText2))
        {
            mAuth.createUserWithEmailAndPassword(emailText, passwordText1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                myRef = FirebaseDatabase.getInstance().getReference();
                                String userID = user.getUid();

                                UserInformation userInformation = new UserInformation(category, dni.getText().toString(), email.getText().toString(),
                                        firstName.getText().toString(), group, lastName1.getText().toString(), lastName2.getText().toString(), user.getUid());

                                myRef.child("users").child(userID).child("information").setValue(userInformation);

                                toastMessage("Usuario creado correctamente");

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpNewUserActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });

            mAuth.signInWithEmailAndPassword(emailText, passwordText1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                Intent intent = new Intent(SignUpNewUserActivity.this, MainActivity.class);
                                startActivity(intent);

                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());

                                Toast.makeText(SignUpNewUserActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
