package com.example.sivakorn.loginfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonalProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private RequestQueue queue;
    private ProgressDialog loadingDialog;
    private RadioGroup radioGenderGroup;
    private RadioButton radioGenderButton;
    private EditText etFirstname, etLastname, etAge ,etTel;
    private Button type1,type2;
    private TextInputLayout textInputFirstname, textInputLastname,textInputAge,textInputTel;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);
        mAuth = FirebaseAuth.getInstance();
        //...by
        radioGenderGroup = findViewById(R.id.radioSex);
        etFirstname = findViewById(R.id.firstname_et);
        etLastname = findViewById(R.id.lastname_et);
        etTel = findViewById(R.id.tel_et);
        etAge = findViewById(R.id.age_et);
        type1 = findViewById(R.id.type1);
        type2 = findViewById(R.id.type2);

        //..onclick
        type1.setOnClickListener(this);
        type2.setOnClickListener(this);

        //..inputLayout
        textInputFirstname = (TextInputLayout) findViewById(R.id.textInputFirstname);
        textInputLastname = (TextInputLayout) findViewById(R.id.textInputLastname);
        textInputAge = (TextInputLayout) findViewById(R.id.textInputAge);
        textInputTel= (TextInputLayout) findViewById(R.id.textInputTel);


        queue = Volley.newRequestQueue(this);
        loadingDialog = new ProgressDialog(this);
    }

    private void createUser(String id, String firstname, String lastname, String tel, String email, int age, String type, String gender) {
        String url = "http://128.199.215.183:4000/account/add";
        loadingDialog.setMessage("กำลังสร้าง...");
        loadingDialog.show();
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", id);
            jsonBody.put("username", "");
            jsonBody.put("password", "");
            jsonBody.put("tel", tel);
            jsonBody.put("firstname", firstname);
            jsonBody.put("email", email);
            jsonBody.put("lastname", lastname);
            jsonBody.put("age", age);
            jsonBody.put("gender", gender);
            jsonBody.put("type", type);
            jsonBody.put("qrcode", "");

            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("createUser", response.toString());
                            try {
                                String status = response.getString("add");
                                if (status.equals("success")) {
                                    //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                                    loadingDialog.dismiss();
                                    gotoHome();
                                } else {
                                    //ยังไม่มี
                                    loadingDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
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
                            Log.d("createUser", error.toString());
                            Toast.makeText(getApplicationContext(), "Error.Response", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                        }
                    }
            );
            queue.add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void create(String type) {
        textInputFirstname.setError(null); // hide error
        textInputLastname.setError(null); // hide error
        textInputAge.setError(null); // hide error
        textInputTel.setError(null); // hide error
        String firstname = etFirstname.getText().toString();
        String lastname = etLastname.getText().toString();
        String age = etAge.getText().toString();
        String tel = etTel.getText().toString();
        String gender = "";
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            if (TextUtils.isEmpty(firstname)) {
                textInputFirstname.setError("firstname is required"); // show error

                return;
            }
            if (TextUtils.isEmpty(lastname)) {
                textInputLastname.setError("lastname is required"); // show error
                return;
            }
            if (TextUtils.isEmpty(age)) {
                textInputLastname.setError("age is required"); // show error
                return;
            }
            if (TextUtils.isEmpty(tel)) {
                textInputTel.setError("tel is required"); // show error
                return;
            }
            // get selected radio button from radioGroup
            int selectedId = radioGenderGroup.getCheckedRadioButtonId();
            // find the radiobutton by returned id
            radioGenderButton = (RadioButton) findViewById(selectedId);
            if(radioGenderButton.getText().toString().equals("ชาย")){
                gender = "male";
                //Toast.makeText(getApplicationContext(), "Radio : "+radioGenderButton.getText()+"Type : " +type, Toast.LENGTH_LONG).show();
            }
            if(radioGenderButton.getText().toString().equals("หญิง")){
                gender = "female";
                // /Toast.makeText(getApplicationContext(), "Radio : "+radioGenderButton.getText()+"Type : " +type, Toast.LENGTH_LONG).show();
            }
            createUser( user.getUid(), firstname,  lastname,  tel,  user.getEmail(), Integer.parseInt( age),  type,  gender);
        }else {
            Toast.makeText(getApplicationContext(),"เรียกข้อมูลผู้ใช้ไม่ได้",Toast.LENGTH_SHORT).show();
        }


    }

    private void gotoHome() {
        startActivity(new Intent(PersonalProfileActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SignOut();
    }
    private void SignOut() {
        LoginManager.getInstance().logOut();
        mAuth.signOut();
        startActivity(new Intent(PersonalProfileActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.type1:
                create("1");
                break;
            case R.id.type2:
                create("2");
                break;
            default:
                break;
        }
    }
}
