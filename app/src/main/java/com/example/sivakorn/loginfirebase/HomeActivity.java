package com.example.sivakorn.loginfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSignOut;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //...by
        btnSignOut = findViewById(R.id.signout_bt);


        //..onclick
        btnSignOut.setOnClickListener(this);

        //..firebase
        mAuth = FirebaseAuth.getInstance();

        initGmail();


        FirebaseUser user = mAuth.getCurrentUser();
        user.getUid();
    }
    private void initGmail(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void SignOut() {
        LoginManager.getInstance().logOut();
        mAuth.signOut();
        if(mGoogleSignInClient!=null){
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //updateUI(null);
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                            finish();
                        }
                    });
        }else{
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signout_bt:
                SignOut();
                break;
            default:
                break;
        }
    }
}
