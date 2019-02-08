package com.hitasoft.app.howzu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SendOTPActivity extends AppCompatActivity implements View.OnClickListener {
    CustomTextView btn_verify;
    ImageView back;
  //  ProgressWheel progressWheel;
    static String TAG = "SendOTPActivity";
    String strServerOTP,strCountryCode,strMobileNumber,strName,strDateOfBirth,strLocation,strLatitude,strLongitude;
    EditText editTextVerificationCode;
    CustomTextView customTextViewResendCode,customTextViewPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);
        editTextVerificationCode=findViewById(R.id.editTextVerificationCode);
        customTextViewResendCode=findViewById(R.id.customTextViewResendCode);
        customTextViewPhoneNumber=findViewById(R.id.customTextViewPhoneNumber);
        Bundle extras = getIntent().getExtras();

        if (extras == null) {
            //            strCountryCode="+91";
//            strMobileNumber="9662290050";
            return;
        }else {
            strCountryCode = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE);
            strMobileNumber = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER);
            strName = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_NAME);
            strDateOfBirth = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE);
            strLatitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE);
            strLongitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE);
            strLocation = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION);

            System.out.println("jigar the lat is "+strLatitude);
        }

        //        strCountryCode="+91";
//        strMobileNumber="9662290050";
        customTextViewPhoneNumber.setText(strCountryCode+"-"+strMobileNumber);
        customTextViewResendCode.setOnClickListener(view -> {
            getOTPVerificationCode();
        });

        initViews();
        getOTPVerificationCode();
        btn_verify.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initViews() {
        back = findViewById(R.id.backbtn);
        btn_verify = findViewById(R.id.btnVerifyNow);
//        progressWheel = findViewById(R.id.interestProgress);
//        progressWheel.setVisibility(View.VISIBLE);
//        progressWheel.spin();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnVerifyNow:

                String strUserVerification=editTextVerificationCode.getText().toString();
                if(strUserVerification.equals(""))
                {
                    editTextVerificationCode.setError("Please Enter Valid OTP");
                }else
                {
                    System.out.println("jigar the server otp is "+strServerOTP);
                    Toast.makeText(SendOTPActivity.this,"OTP is  "+strServerOTP,Toast.LENGTH_LONG).show();
                    if(strUserVerification.equals(strServerOTP))


                    {
                        Intent intent = new Intent(getApplicationContext(), SetPasswordActivity.class);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_NAME,strName);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE,strCountryCode);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER,strMobileNumber);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE,strDateOfBirth);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE, strLatitude);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE, strLongitude);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION, strLocation);

                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(SendOTPActivity.this,"OTP Mismatch,Please Enter Valid OTP ",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }
    }
    public void getOTPVerificationCode() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(SendOTPActivity.this))) {
            CommonFunctions.showProgressDialog(this);

            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_OTP_VERIFICATION,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            try {
                                Log.v(TAG, "getSettingsRes=" + res);

                                System.out.println("jigar the response on otp code is " + res);

                                JSONObject jsonObjectResponseMain = new JSONObject(res);
                                String strStatus = jsonObjectResponseMain.getString(Constants.TAG_STATUS);
                                String strMsg = jsonObjectResponseMain.getString(Constants.TAG_MSG);

                                if (strStatus.equals("1") && strMsg.equals(Constants.TAG_SUCCESS_OTP)) {
                                    strServerOTP = jsonObjectResponseMain.getString(Constants.TAG_INFO);
                                } else {
                                    Toast.makeText(SendOTPActivity.this, R.string.response_failure, Toast.LENGTH_LONG).show();
                                }
                                CommonFunctions.hideProgressDialog(SendOTPActivity.this);

                            } catch (Exception e) {
                                System.out.println("jigar the error in response is " + e);
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    System.out.println("jigar the volley error in response is " + error.getMessage());
                    CommonFunctions.hideProgressDialog(SendOTPActivity.this);
                }

            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(Constants.TAG_MOBILE_NUMBER, strMobileNumber);
                    map.put(Constants.TAG_COUNTRY_CODE, strCountryCode);
                    Log.v(TAG, "getSettingsParams=" + map);
                    return map;
                }
            };
            HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        }else
        {
            CommonFunctions.hideProgressDialog(SendOTPActivity.this);

            Toast.makeText(SendOTPActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();

        }
    }


}
