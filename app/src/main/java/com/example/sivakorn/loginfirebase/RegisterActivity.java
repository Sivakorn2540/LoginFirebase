package com.example.sivakorn.loginfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private Button btnSign;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword, inputLayoutConfirm;
    private EditText etEmail, etPassword, etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        //..button
        btnSign = findViewById(R.id.signup_bt);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputUsername);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputUsername);
        inputLayoutConfirm = (TextInputLayout) findViewById(R.id.inputLayoutConfirm);
        //..edittext
        etEmail = findViewById(R.id.email_et);
        etPassword = findViewById(R.id.password_et);
        etConfirm = findViewById(R.id.confirm_et);

        //..onclick
        btnSign.setOnClickListener(this);

    }

    private void singUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("singUp", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            gotoPersonal();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("singUp", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                    }
                });
    }

    private void registerMt() {
        inputLayoutEmail.setError(null); // hide error
        inputLayoutPassword.setError(null); // hide error
        inputLayoutConfirm.setError(null); // hide error
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (TextUtils.isEmpty(email)) {
            inputLayoutEmail.setError("email is required"); // show error

            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("Password is required"); // show error
            return;
        }
        if (!password.equals(confirm)) {
            inputLayoutConfirm.setError("Password is not compare"); // show error
            return;
        }
        singUp(email, password);
    }

    private void gotoPersonal() {
        startActivity(new Intent(RegisterActivity.this, PersonalProfileActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_bt:
                registerMt();
                break;
            default:
                break;
        }
    }
}
