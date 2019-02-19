package com.hitasoft.app.howzu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomEditText;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    private String TAG = this.getClass().getSimpleName();
    private Toolbar toolbar;
    private RelativeLayout actionbar;
    private ImageView backbtn;
    private CustomTextView toolbarTitle;
    private CustomEditText edtName;
    private CustomTextView txtEmail, btnSave;
    private CustomEditText edtAge;
    private AppCompatRadioButton btnMale, btnFemale;
    private TextInputLayout aboutLay;
    private CustomEditText edtAbout;
    public static ProgressDialog pd;
    private ProgressWheel progress;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    String userId;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViews();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();
        userId = getIntent().getExtras().getString("strFriendID");

        getProfile();
    }

    private void findViews() {

        pd = new ProgressDialog(EditProfileActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        progress = (ProgressWheel) findViewById(R.id.progress);
        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        toolbarTitle = (CustomTextView) findViewById(R.id.toolbarTitle);
        edtName = (CustomEditText) findViewById(R.id.edtName);
        txtEmail = (CustomTextView) findViewById(R.id.txtEmail);
        btnSave = (CustomTextView) findViewById(R.id.btnSave);
        edtAge = (CustomEditText) findViewById(R.id.edtAge);
        btnMale = (AppCompatRadioButton) findViewById(R.id.btnMale);
        btnFemale = (AppCompatRadioButton) findViewById(R.id.btnFemale);
        aboutLay = (TextInputLayout) findViewById(R.id.aboutLay);
        edtAbout = (CustomEditText) findViewById(R.id.edtAbout);
        requestFocus(edtName);
        edtName.setSelection(0);
        toolbarTitle.setText(R.string.edit_profile);
        btnMale.setOnClickListener(this);
        btnFemale.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);
    }

    private void getProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getProfileRes=" + res);

                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                JSONObject values = json.getJSONObject(Constants.TAG_RESULT);

//                                sendMatch = DefensiveClass.optString(values, Constants.TAG_SEND_MATCH);

                                profileMap.put(Constants.TAG_USERID, DefensiveClass.optString(values, Constants.TAG_USERID));
                                profileMap.put(Constants.TAG_USERNAME, DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                profileMap.put(Constants.TAG_GENDER, DefensiveClass.optString(values, Constants.TAG_GENDER));
                                profileMap.put(Constants.TAG_EMAIL, DefensiveClass.optString(values, Constants.TAG_EMAIL));
                                profileMap.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                profileMap.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                profileMap.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
                                profileMap.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
                                profileMap.put(Constants.TAG_ONLINE, DefensiveClass.optString(values, Constants.TAG_ONLINE));
                                profileMap.put(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                profileMap.put(Constants.TAG_INFO, DefensiveClass.optString(values, Constants.TAG_INFO));
                                profileMap.put(Constants.TAG_INTEREST, DefensiveClass.optString(values, Constants.TAG_INTEREST));
                                profileMap.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                profileMap.put(Constants.TAG_IMAGES, DefensiveClass.optString(values, Constants.TAG_IMAGES));
                                profileMap.put(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
                                profileMap.put(Constants.TAG_SHOW_LOCATION, DefensiveClass.optString(values, Constants.TAG_SHOW_LOCATION));
                                profileMap.put(Constants.TAG_INVISIBLE, DefensiveClass.optString(values, Constants.TAG_INVISIBLE));
                                profileMap.put(Constants.TAG_REPORT, DefensiveClass.optString(values, Constants.TAG_REPORT));
                                profileMap.put(Constants.TAG_PREMIUM_MEMBER, DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER));
                                profileMap.put(Constants.TAG_MEMBERSHIP_VALID, DefensiveClass.optString(values, Constants.TAG_MEMBERSHIP_VALID));
//                                haspMapProfileDetails.put(Constants.TAG_SEND_MATCH, sendMatch);
                                profileMap.put(Constants.TAG_PEOPLE_FOR, DefensiveClass.optString(values, Constants.TAG_PEOPLE_FOR));

                                progress.setVisibility(View.GONE);
                                progress.stopSpinning();
                                setProfile();

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(EditProfileActivity.this, "Error", json.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.getUserId().equals(userId)) {
                    map.put(Constants.TAG_USERID, GetSet.getUserId());
                    map.put(Constants.TAG_FRIEND_ID, userId);
                }
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v("params", "getProfileParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void setProfile() {

        edtName.setText(profileMap.get(Constants.TAG_USERNAME));
        txtEmail.setText(profileMap.get(Constants.TAG_EMAIL));
        edtAge.setText(profileMap.get(Constants.TAG_AGE));
        gender = profileMap.get(Constants.TAG_GENDER);
        edtAbout.setText("" + profileMap.get(Constants.TAG_BIO));
        requestFocus(edtName);
        edtName.setSelection(("" + edtName.getText()).length());

        if (gender.equalsIgnoreCase("male")) {
            btnMale.setChecked(true);
            btnFemale.setChecked(false);
        } else {
            btnMale.setChecked(false);
            btnFemale.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMale:
                btnMale.setChecked(true);
                btnFemale.setChecked(false);
                gender = "male";
                break;
            case R.id.btnFemale:
                btnMale.setChecked(false);
                btnFemale.setChecked(true);
                gender = "female";
                break;
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.btnSave:
                Log.e(TAG, "onClick: " + GetSet.getMaxAge());
                if (TextUtils.isEmpty("" + edtName.getText())) {
                    Toast.makeText(getApplicationContext(), R.string.enter_name, Toast.LENGTH_SHORT).show();
                } else if (edtName.getText().toString().length() < 3) {
                    edtName.setError("Please fill at least 3 characters");
                    requestFocus(edtName);
                } else if (TextUtils.isEmpty("" + edtAge.getText())) {
                    Toast.makeText(getApplicationContext(), R.string.enter_age, Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(edtAge.getText().toString()) < 18) {
                    edtAge.setError("Please fill valid age");
                    requestFocus(edtAge);
                } else if (Integer.parseInt(edtAge.getText().toString()) > Integer.parseInt(GetSet.getMaxAge())) {
                    edtAge.setError("Please enter age between 18 and " + GetSet.getMaxAge());
                    requestFocus(edtAge);
                } else {
                    saveProfile();
                }
                break;
        }
    }

    private void saveProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        try {
                            Log.v(TAG, "saveSettingsRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setUserName(edtName.getText().toString());
                                editor.putString(Constants.TAG_USERID, GetSet.getUserId());
                                editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                                editor.putString(Constants.TAG_GENDER, gender);
                                editor.putString(Constants.TAG_AGE, edtAge.getText().toString());
                                editor.putString(Constants.TAG_BIO, !TextUtils.isEmpty("" + edtAbout.getText()) ? "" + edtAbout.getText() : "");
                                editor.commit();
                                onBackPressed();
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(EditProfileActivity.this, "Error", json.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_USERNAME, edtName.getText().toString());
                map.put(Constants.TAG_AGE, edtAge.getText().toString());
                map.put(Constants.TAG_GENDER, gender);
                map.put(Constants.TAG_BIO, !TextUtils.isEmpty("" + edtAbout.getText()) ? "" + edtAbout.getText() : "");
                Log.v(TAG, "saveSettingsParams=" + map);
                return map;
            }
        };

        if (!pd.isShowing()) {
            pd.show();
        }
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.main_content), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.main_content), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.main_content), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.main_content), true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        ProfileActivity.activity.finish();
        Intent e = new Intent(EditProfileActivity.this, ProfileActivity.class);
        e.putExtra("from", "myprofile");
        e.putExtra("strFriendID", GetSet.getUserId());
        e.putExtra("sendMatch", "");
        startActivity(e);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
