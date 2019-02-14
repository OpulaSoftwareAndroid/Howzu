package com.hitasoft.app.howzu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainNewLoginActivity extends AppCompatActivity implements View.OnClickListener {
    CustomTextView btnLogin;
    ImageView back;
    String strCountryCode,strMobileNumber,strName,strDateOfBirth,strLatitude,strLongitude,strLocation,strGender;
    EditText editTextPassword, editTextUserName;
    String TAG = "MainNewLoginActivity";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String strUserID="";
    CustomTextView customTextViewNewUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        pref = this.getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        editor = pref.edit();

        Constants.pref = this.getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        Constants.editor = pref.edit();

        if (pref.contains(Constants.TAG_USERID)) {
        String strUserID=pref.getString(Constants.TAG_USERID,null);

        if(strUserID!=null)
        {
            Intent intent=new Intent(MainNewLoginActivity.this,MainScreenActivity.class);
            intent.putExtra(Constants.TAG_USERID,strUserID);
            startActivity(intent);
            finish();
        }
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
        btnLogin.setOnClickListener(this);
        editTextPassword=findViewById(R.id.editTextPassword);
        editTextUserName =findViewById(R.id.editTextMobileNumber);
        editTextUserName.setText("9852412008");
        editTextPassword.setText("123456");
        back.setOnClickListener(this);

        customTextViewNewUser=findViewById(R.id.textViewNewUser);
        customTextViewNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainNewLoginActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        back = findViewById(R.id.backbtn);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                String strPassword=editTextPassword.getText().toString();
                String strUsername= editTextUserName.getText().toString();

                if(strUsername.equals("") )
                {
                    editTextUserName.setError("Please enter valid username");
                } else if(strPassword.equals(""))
                {
                    editTextUserName.setError("Please enter valid password");
                }else
                {
                    userLogin();
                }
//                else {
//                    if (strPassword.equals(strConfirmPassword)) {
//                        Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_NAME,strName);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE,strCountryCode);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER,strMobileNumber);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE,strDateOfBirth);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PASSWORD,strPassword);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE, strLatitude);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE, strLongitude);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION, strLocation);
//                        intent.putExtra(Constants.TAG_LOGIN_INTENT_GENDER, strGender);
//
//
//                        startActivity(intent);
//                    } else {
//                        editTextPassword.setError("Password and confirm password should be same");
//                    }
//                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }
    }


    public void userLogin() {
        CommonFunctions.showProgressDialog2(MainNewLoginActivity.this);
        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {
            CommonFunctions.showProgressDialog2(this);
            StringRequest loginReq = new StringRequest(Request.Method.POST, Constants.API_SIGNIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            Log.d(TAG, "jigar the login response =" + res);
                            try {
                                JSONObject json = new JSONObject(res);
                                JSONObject results = new JSONObject(res);
                                String status = results.getString("status");
                                if (status.equalsIgnoreCase("true")) {

                                    CommonFunctions.hideProgressDialog2(MainNewLoginActivity.this);
                                    JSONObject values = results.getJSONObject(Constants.TAG_PEOPLES);


                                    GetSet.setLogged(true);
                                    GetSet.setUseridLikeToken(DefensiveClass.optString(values, Constants.TAG_USERID));
                                    GetSet.setUserName(DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                    GetSet.setImageUrl(DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                    GetSet.setToken(DefensiveClass.optString(values, Constants.TAG_TOKEN));
                                    GetSet.setUserId(DefensiveClass.optString(values, Constants.TAG_REGISTERED_ID));

                                    editor.putBoolean(Constants.ISLOGGED, true);
                                    editor.putString(Constants.TAG_USERID, GetSet.getUserId());
                                    editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                                    editor.putString(Constants.TAG_GENDER, DefensiveClass.optString(values, Constants.TAG_GENDER));
                                    editor.putString(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                    editor.putString(Constants.TAG_REGISTERED_ID, DefensiveClass.optString(values, Constants.TAG_REGISTERED_ID));
                                    editor.putString(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                    editor.putString(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
                                    editor.putString(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
                                    editor.putString(Constants.TAG_ONLINE, DefensiveClass.optString(values, Constants.TAG_ONLINE));
                                    editor.putString(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                    editor.putString(Constants.TAG_INFO, DefensiveClass.optString(values, Constants.TAG_INFO));
                                    editor.putString(Constants.TAG_INTEREST, DefensiveClass.optString(values, Constants.TAG_INTEREST));
                                    editor.putString(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                    editor.putString(Constants.TAG_IMAGES, DefensiveClass.optString(values, Constants.TAG_IMAGES));
                                    editor.putString(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
                                    editor.putString(Constants.TAG_SHOW_DISTANCE, DefensiveClass.optString(values, Constants.TAG_SHOW_DISTANCE));
                                    editor.putString(Constants.TAG_INVISIBLE, DefensiveClass.optString(values, Constants.TAG_INVISIBLE));
                                    editor.putString(Constants.TAG_REPORT, DefensiveClass.optString(values, Constants.TAG_REPORT));
                                    editor.putString(Constants.TAG_TOKEN, DefensiveClass.optString(values, Constants.TAG_TOKEN));

                                      if (DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER).equals("true") || DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER).equals("1")) {
                                        GetSet.setPremium(true);
                                        editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, true);
                                    } else {
                                        GetSet.setPremium(true);
                                        editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, false);
                                    }

                                    editor.putString(Constants.TAG_AD_UNIT_ID, DefensiveClass.optString(values, Constants.TAG_AD_UNIT_ID));
                                    GetSet.setAdUnitId(DefensiveClass.optString(values, Constants.TAG_AD_UNIT_ID));
                                    if (DefensiveClass.optString(values, Constants.TAG_ADMIN_ENABLE_ADS).equals("true")) {
                                        GetSet.setBannerEnable(true);
                                        editor.putBoolean(Constants.TAG_ADMIN_ENABLE_ADS, true);
                                    } else {
                                        GetSet.setBannerEnable(false);
                                        editor.putBoolean(Constants.TAG_ADMIN_ENABLE_ADS, false);
                                    }

                                    if (DefensiveClass.optString(values, Constants.TAG_HIDE_ADS).equals("true")) {
                                        GetSet.setHideAds(true);
                                        editor.putBoolean(Constants.TAG_HIDE_ADS, true);
                                    } else {
                                        GetSet.setHideAds(false);
                                        editor.putBoolean(Constants.TAG_HIDE_ADS, false);
                                    }

                                    if (DefensiveClass.optString(values, Constants.TAG_LIKE_NOTIFICATION).equals("true")) {
                                        GetSet.setLikeNotification(true);
                                        editor.putBoolean(Constants.TAG_LIKE_NOTIFICATION, true);
                                    } else {
                                        GetSet.setLikeNotification(false);
                                        editor.putBoolean(Constants.TAG_LIKE_NOTIFICATION, false);
                                    }

                                    if (DefensiveClass.optString(values, Constants.TAG_MESSAGE_NOTIFICATION).equals("true")) {
                                        GetSet.setMsgNotification(true);
                                        editor.putBoolean(Constants.TAG_MESSAGE_NOTIFICATION, true);
                                    } else {
                                        GetSet.setMsgNotification(false);
                                        editor.putBoolean(Constants.TAG_MESSAGE_NOTIFICATION, false);
                                    }
                                    editor.commit();

                                    GetSet.setLocation(DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                    //                    getAccessToken();

                                    strUserID = pref.getString(Constants.TAG_REGISTERED_ID, "");
                                    Log.d(TAG, "jigar the login user id is   " + strUserID);


                                    Log.d(TAG, "jigar the login data we have is in the..  " + pref.getString(Constants.TAG_REGISTERED_ID, ""));

                                    if (!strUserID.equals("")) {
                                        Intent intent = new Intent(MainNewLoginActivity.this, MainScreenActivity.class);
                                        intent.putExtra(Constants.TAG_USERID, strUserID);
                                        startActivity(intent);
                                        finish();
                                    }

                                }



//                            else if (res.equalsIgnoreCase("error")) {
//                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString(Constants.TAG_MESSAGE));
//                            } else {
//                                if (dialog != null && dialog.isShowing()) {
//                                    dialog.dismiss();
//                                }
//                                Toast.makeText(getActivity(), results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
//                            }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.v(TAG, "jigar the error in login json is " + e);
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
                                Toast.makeText(MainNewLoginActivity.this, "something went to be wrong", Toast.LENGTH_SHORT).show();
                            } catch (NullPointerException e) {
                                Log.v(TAG, "jigar the error in main login null pointer is " + e);

                                e.printStackTrace();
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
                                Toast.makeText(MainNewLoginActivity.this, "something went to be wrong", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
                                Log.v(TAG, "jigar the error in main login exception is " + e);

                                Toast.makeText(MainNewLoginActivity.this, "something went to be wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "jigar the volley error: " + error.getMessage());

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Constants.TAG_MOBILE_NUMBER, editTextUserName.getText().toString());
                    params.put(Constants.TAG_PASSWORD, editTextPassword.getText().toString());
//                params.put(Constants.TAG_LAT, Double.toString(LoginActivity.lat));
//                params.put(Constants.TAG_LON, Double.toString(LoginActivity.longit));
////                params.put(Constants.TAG_LOCATION, LoginActivity.loc);
//                params.put(Constants.TAG_DEVICE_ID, android.provider.Settings.Secure.getString
//                        (MainNewLoginActivity.this.getContentResolver(),
//                        android.provider.Settings.Secure.ANDROID_ID));
                    Log.v(TAG, "jigar the login Params=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(loginReq, "");
        } else {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            CommonFunctions.hideProgressDialog2(MainNewLoginActivity.this);


        }

    }

}
