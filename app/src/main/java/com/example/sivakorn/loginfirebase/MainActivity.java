package com.example.sivakorn.loginfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private Button btnLogin, btnRegister, btnGmailLogin, btnFacebookLogin;
    private  LoginButton loginButton;
    private EditText etUsername, etPassword;
    private CallbackManager mCallbackManager;
    private TextInputLayout inputLayoutUser, inputLayoutPassword;
    private RequestQueue queue;
    private static final String TAG = "FacebookLogin";
    private ProgressDialog loadingDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG_Gmail = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        //..button..
        btnLogin = findViewById(R.id.bt_login_login_local);
        btnRegister = findViewById(R.id.bt_login_signup);
        btnGmailLogin = findViewById(R.id.bt_login_login_gmail);
        btnFacebookLogin = findViewById(R.id.bt_login_login_facebook);
        //..inputLayout
        inputLayoutUser = (TextInputLayout) findViewById(R.id.ti_login_username);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.ti_login_password);
        //..edittext..
        etUsername = findViewById(R.id.et_login_username);
        etPassword = findViewById(R.id.et_login_password);

        //gmail
        // Set the dimensions of the sign-in button.
        //SignInButton signInButton = findViewById(R.id.sign_in_button);
        // signInButton.setSize(SignInButton.SIZE_STANDARD);

        //.. onclick
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnGmailLogin.setOnClickListener(this);
        btnFacebookLogin.setOnClickListener(this);
        //findViewById(R.id.sign_in_button).setOnClickListener(this);

        queue = Volley.newRequestQueue(this);
        loadingDialog = new ProgressDialog(this);
        GenerateHashKey();
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "facebook:onError", error);
                // ...
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i(TAG_Gmail, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG_Gmail, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                CheckUserIsExist(user.getUid().toString());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG_Gmail, "signInWithCredential:failure", task.getException());

                        }


                    }
                });
    }

    private void signInGmial() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.i(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            if (user != null) {
                                CheckUserIsExist(user.getUid().toString());
//                                for (UserInfo profile : user.getProviderData()) {
//                                    // Id of the provider (ex: google.com)
//                                    String providerId = profile.getProviderId();
//                                    String uid = profile.getUid();
//                                    String name = profile.getDisplayName();
//                                    String email = profile.getEmail();
//                                    Uri photoUrl = profile.getPhotoUrl();
//                                };
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                    }
                });
    }

    private void GenerateHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.sivakorn.loginfirebase", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private void singIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("signIn", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            if (user != null) {
                                CheckUserIsExist(user.getUid().toString());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("signIn", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void CheckUserIsExist(String id) {
        String url = "http://128.199.215.183:4000/account/checkuser";
        loadingDialog.setMessage("Loading...");
        loadingDialog.show();
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", id);
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("CheckUserIsExist", response.toString());
                            try {
                                String status = response.getString("checkuser");
                                if (status.equals("success")) {
                                    //มีแล้ว
                                    loadingDialog.dismiss();
                                    gotoHome();
                                } else {
                                    //ยังไม่มี
                                    loadingDialog.dismiss();
                                    gotoPersonProfile();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.i("CheckUserIsExist", error.toString());
                        }
                    }
            );
            queue.add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signInMt() {
        inputLayoutUser.setError(null); // hide error
        inputLayoutPassword.setError(null); // hide error

        String email = etUsername.getText().toString();
        String password = etPassword.getText().toString();


        if (TextUtils.isEmpty(email)) {
            inputLayoutUser.setError("Username is required"); // show error

            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError("Password is required"); // show error
            return;
        }

        singIn(email, password);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //updateUI(currentUser);
            gotoHome();

        }

    }

    private void gotoRegister() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        finish();

    }

    private void gotoHome() {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    private void gotoPersonProfile() {
        startActivity(new Intent(MainActivity.this, PersonalProfileActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_login_login_local:
                signInMt();
                break;
            case R.id.bt_login_signup:
                gotoRegister();
                break;
            case R.id.bt_login_login_gmail:
                signInGmial();
            case R.id.bt_login_login_facebook:
                loginButton.performClick();
            default:
                break;
        }
    }
}
