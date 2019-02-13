package com.hitasoft.app.howzu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.utils.Constants;

public class SetPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    CustomTextView btnSetPassword;
    ImageView back;
    String strCountryCode,strMobileNumber,strName,strDateOfBirth,strLatitude,strLongitude,strLocation,strGender;
    EditText editTextPassword,editTextConfirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
// get data via the key
        strCountryCode = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE);
        strMobileNumber = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER);
        strName = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_NAME);
        strDateOfBirth = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE);
        strLatitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE);
        strLongitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE);
        strLocation = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION);
        strGender = extras.getString(Constants.TAG_LOGIN_INTENT_GENDER);

        initViews();
        btnSetPassword.setOnClickListener(this);
        editTextPassword=findViewById(R.id.editTextPassword);
        editTextConfirmPassword=findViewById(R.id.editTextConfirmPassword);
        back.setOnClickListener(this);
    }

    private void initViews() {
        btnSetPassword = findViewById(R.id.btnSetPassword);
        back = findViewById(R.id.backbtn);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSetPassword:
                String strPassword=editTextPassword.getText().toString();
                String strConfirmPassword=editTextConfirmPassword.getText().toString();

                if(strConfirmPassword.equals("") )
                {
                    editTextPassword.setError("Please enter valid password");
                } else if(strPassword.equals(""))
                {
                    editTextConfirmPassword.setError("Please enter valid confirm password");
                }else {
                    if (strPassword.equals(strConfirmPassword)) {
                        Intent intent = new Intent(getApplicationContext(), SetDialogsActivity.class);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_NAME,strName);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE,strCountryCode);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER,strMobileNumber);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE,strDateOfBirth);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PASSWORD,strPassword);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE, strLatitude);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE, strLongitude);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION, strLocation);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_GENDER, strGender);


                        startActivity(intent);
                    } else {
                        editTextPassword.setError("Password and confirm password should be same");
                    }
                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
