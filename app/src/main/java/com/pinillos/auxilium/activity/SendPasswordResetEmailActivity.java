package com.pinillos.auxilium.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.util.MessageUtil;

public class SendPasswordResetEmailActivity extends AppCompatActivity {

    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_password_reset_email);
        email = findViewById(R.id.resetEmail_editText);
    }

    public void sendPasswordResetEmail(View view)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String emailAddress = email.getText().toString();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            MessageUtil.toastMessage(getApplicationContext(),"Email enviado a "+emailAddress+" revise su bandeja de entrada por favor.");
                    }
                });
    }
}
