package com.hitasoft.app.howzu;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ImagePicker;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.ItemOffsetDecoration;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SharedPrefManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 7/3/18.
 */

public class SignupFragment extends Fragment implements View.OnClickListener {
    String TAG = "SignupFragment";

    EditText name, email, password, age, work, about;
    //TextInputLayout nameLay, emailLay, passwordLay, ageLay, workLay, aboutLay;
    TextView signup;
    ProgressDialog dialog;
    RadioButton male, female;
    ArrayList<HashMap<String, String>> peopleFor = new ArrayList<HashMap<String, String>>();

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 100;
    public static final int LOCATION_REQUEST_CODE = 105;
    private static String MAX_AGE = "70";
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+", gender = "";
    Context context;
    Activity activity;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private ImageView userImageView;
    private TextView btnAddPhoto, btnNext;
    private String profileImage = "";
    private String peopleForId = "";
    ArrayList<HashMap<String, String>> interestList = new ArrayList<HashMap<String, String>>();
    private List<String> selectedList = new ArrayList<>();
    HashMap<String, String> profileMap = new HashMap<String, String>();
    private String sendMatch;
    private static boolean imageUploading;
    public static ProgressDialog pd;
    private String imagesJson = "";
    private List<String> imagesAry = new ArrayList<>();
    private String interest = "", deviceId, facebookId, imageURL, images;
    private HashMap<String, String> hashMap;
    Dialog dialogPeopleFor, dialogAddPhotos, dialogInterest;
    ProgressWheel peopleProgress, photoProgress, interestProgress;
    private PeopleAdapter peopleAdapter;
    private InterestAdapter interestAdapter;
    RelativeLayout mainLay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_lay, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Log.v(TAG, "income");
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        name = getView().findViewById(R.id.name);
        email = getView().findViewById(R.id.email);
        password = getView().findViewById(R.id.password);
        age = getView().findViewById(R.id.age);
        work = getView().findViewById(R.id.work);
        about = getView().findViewById(R.id.about);
        signup = getView().findViewById(R.id.signup);
        male = getView().findViewById(R.id.male);
        female = getView().findViewById(R.id.female);
        /*nameLay = getView().findViewById(R.id.nameLay);
        emailLay = getView().findViewById(R.id.emailLay);
        passwordLay = getView().findViewById(R.id.passwordLay);
        ageLay = getView().findViewById(R.id.ageLay);
        workLay = getView().findViewById(R.id.workLay);
        aboutLay = getView().findViewById(R.id.aboutLay);*/
        mainLay = getView().findViewById(R.id.mainLay);

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        editor = pref.edit();

        Constants.pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        Constants.editor = pref.edit();
        context = getActivity();
        activity = (Activity) context;
        signup.setOnClickListener(this);

        getAdminData();

        if (hashMap != null && hashMap.size() > 0) {
            name.setText(hashMap.get(Constants.TAG_USERNAME));
            email.setText(hashMap.get(Constants.TAG_EMAIL));
            deviceId = hashMap.get(Constants.TAG_DEVICE_ID);
            facebookId = hashMap.get(Constants.TAG_ID);
            profileImage = hashMap.get(Constants.TAG_IMAGE_URL);
        }

        if (male.isChecked()) {
            gender = "male";
        } else {
            gender = "female";
        }
        male.setOnClickListener(this);
        female.setOnClickListener(this);


        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            // setLocationSettings("strVisitingIdLikeToken start");
        }

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

    }

    private void getAdminData() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getSettingsRes=" + res);
                            JSONObject response = new JSONObject(res);
                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject result = response.optJSONObject(Constants.TAG_RESULT);
                                MAX_AGE = result.getString(Constants.TAG_MAX_AGE);
                                GetSet.setMaxAge(MAX_AGE);
                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(context, "Error", response.getString("message"));
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
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * API Implementation
     */

    public void signup() {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_SIGNUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "signupRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONObject results = new JSONObject(res);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject values = results.getJSONObject(Constants.TAG_PEOPLES);

                                GetSet.setLogged(true);
                                GetSet.setUserId(DefensiveClass.optString(values, Constants.TAG_USERID));
                                GetSet.setUserName(DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                GetSet.setToken(DefensiveClass.optString(values, Constants.TAG_TOKEN));

                                editor.putBoolean(Constants.ISLOGGED, true);
                                editor.putString(Constants.TAG_USERID, GetSet.getUserId());
                                editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                                editor.putString(Constants.TAG_GENDER, DefensiveClass.optString(values, Constants.TAG_GENDER));
                                editor.putString(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                editor.putString(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                editor.putString(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
                                editor.putString(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
                                editor.putString(Constants.TAG_ONLINE, DefensiveClass.optString(values, Constants.TAG_ONLINE));
                                editor.putString(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                editor.putString(Constants.TAG_INFO, DefensiveClass.optString(values, Constants.TAG_INFO));
                                editor.putString(Constants.TAG_INTEREST, DefensiveClass.optString(values, Constants.TAG_INTEREST));
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
                                    GetSet.setPremium(false);
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

                                if (LoginActivity.lat != 0 && LoginActivity.longit != 0 && !LoginActivity.loc.equals("")) {
                                    GetSet.setLocation(DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                }

                                getAccessToken();

                                getActivity().overridePendingTransition(R.anim.fade_in,
                                        R.anim.fade_out);
                            } else if (res.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString(Constants.TAG_MESSAGE));
                            } else {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(getActivity(), results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_TYPE, "email");
                params.put(Constants.TAG_NAME, name.getText().toString());
                params.put(Constants.TAG_EMAIL, email.getText().toString());
                params.put(Constants.TAG_PASSWORD, password.getText().toString());
                params.put(Constants.TAG_AGE, age.getText().toString());
                params.put(Constants.TAG_GENDER, gender);
                String info = "";
                String Work = work.getText().toString();
                if (!Work.equals("")) {
                    info = Work;
                } else {
                    info = "";
                }
                params.put(Constants.TAG_INFO, info);
                params.put(Constants.TAG_LAT, Double.toString(LoginActivity.lat));
                params.put(Constants.TAG_LON, Double.toString(LoginActivity.longit));
                params.put(Constants.TAG_LOCATION, LoginActivity.loc);
                params.put(Constants.TAG_DEVICE_ID, android.provider.Settings.Secure.getString(getActivity().getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID));
                Log.v(TAG, "signupParams=" + params);
                return params;
            }

        };

        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");
    }

    private void getAccessToken() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ACCESS_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject results = new JSONObject(res);
                            Log.i(TAG, "getAccessTokenResult: " + results);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                String accessToken = DefensiveClass.optString(results, Constants.TAG_ACCESS_TOKEN);
                                editor.putString(Constants.TAG_AUTHORIZATION, accessToken);
                                editor.putString(Constants.TAG_EXPIRY, DefensiveClass.optString(results, Constants.TAG_EXPIRY));
                                editor.commit();
                                Log.i(TAG, "getAccessToken: " + pref.getString(Constants.TAG_AUTHORIZATION, ""));

                                addDeviceId();
                                setOnlineStatus();

                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                name.setText("");
                                email.setText("");
                                password.setText("");
                                age.setText("");
                                openPeopleForDialog();

                                getActivity().overridePendingTransition(R.anim.fade_in,
                                        R.anim.fade_out);
                            } else {
                                Toast.makeText(getContext(), results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                VolleyLog.e(TAG, "getAccessToken Error: " + error.getMessage());
                Toast.makeText(getContext(), "Network Error. Please login", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Long timeStamp = System.currentTimeMillis() / 1000L;
                String hashString = pref.getString(Constants.TAG_TOKEN, "").trim() + timeStamp.toString().trim();
                hashString = new String(android.util.Base64.encode(hashString.trim().getBytes(), android.util.Base64.DEFAULT));
                map.put(Constants.TAG_ID, pref.getString(Constants.TAG_USERID, ""));
                map.put(Constants.TAG_HASH, hashString.trim());
                map.put(Constants.TAG_TIMESTAMP, "" + timeStamp);
                Log.v(TAG, "getAccessTokenParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, "");
    }

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(getActivity()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_DEVICE_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            if (!DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(getActivity(), json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                Log.i(TAG, "getHeaders: " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
                Log.i(TAG, "getHeaders: " + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_DEVICE_TOKEN, token);
                map.put(Constants.TAG_DEVICE_TYPE, "1");
                map.put(Constants.TAG_DEVICE_ID, deviceId);
                map.put(Constants.TAG_DEVICE_MODE, "1");
                Log.v(TAG, "addDeviceIdParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void setOnlineStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "setOnlineStatusRes=" + res);
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
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_STATUS, "1");
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "setOnlineStatusParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /*Onclick Event*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup:
                if (name.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please fill valid name", Toast.LENGTH_SHORT).show();
                    //nameLay.setError("Please fill valid name");
                    requestFocus(name);
                } else if (name.getText().toString().length() < 3) {
                    Toast.makeText(context, "Please fill at least 3 characters", Toast.LENGTH_SHORT).show();
                    //nameLay.setError("Please fill at least 3 characters");
                    requestFocus(name);
                } else if (!email.getText().toString().matches(emailPattern) && !Constants.EMAIL_ADDRESS_PATTERN.matcher("" + email.getText()).matches()) {
                    //nameLay.setError(null);
                    Toast.makeText(context, "Please fill valid email", Toast.LENGTH_SHORT).show();
                    //emailLay.setError("Please fill valid email");
                    requestFocus(email);
                } else if (password.getText().equals("") || password.getText().toString().length() < 6) {
                    //nameLay.setError(null);
                    Toast.makeText(context, "Please fill valid password", Toast.LENGTH_SHORT).show();
                    email.setError(null);
                    //passwordLay.setError("Please fill valid password");
                    requestFocus(password);
                } else if (gender.isEmpty()) {
                    //nameLay.setError(null);
                    //emailLay.setError(null);
                    //asswordLay.setError(null);
                    Toast.makeText(getActivity(), "Please select your gender", Toast.LENGTH_SHORT).show();
                } else if (age.getText().toString().isEmpty()) {
                    //nameLay.setError(null);
                    //emailLay.setError(null);
                    Toast.makeText(context, "Age must not be empty", Toast.LENGTH_SHORT).show();
                    //passwordLay.setError(null);
                    //ageLay.setError("Age must not be empty");
                    requestFocus(age);
                } else if (Integer.parseInt(age.getText().toString()) < 18) {
                    //nameLay.setError(null);
                    //emailLay.setError(null);
                    Toast.makeText(context, "Please fill valid age", Toast.LENGTH_SHORT).show();
                    //passwordLay.setError(null);
                    //ageLay.setError("Please fill valid age");
                    requestFocus(age);
                } else if (Integer.parseInt(age.getText().toString()) > Integer.parseInt(MAX_AGE)) {
                    //nameLay.setError(null);
                    //emailLay.setError(null);
                    //passwordLay.setError(null);
                    Toast.makeText(context, "Please enter age between 18 and " + MAX_AGE , Toast.LENGTH_SHORT).show();
                    //ageLay.setError("Please enter age between 18 and " + MAX_AGE);
                    requestFocus(age);
                } else {
                    //nameLay.setError(null);
                    //emailLay.setError(null);
                    //passwordLay.setError(null);
                    //ageLay.setError(null);
                    if (HowzuApplication.isNetworkAvailable(getActivity())) {
                        requestFocus(name);
                        dialog.show();
                        signup();
                    } else {
                        HowzuApplication.showSnack(getActivity(), mainLay, false);
                    }
                }
                break;
            case R.id.male:
                male.setChecked(true);
                female.setChecked(false);
                gender = "male";
                break;
            case R.id.female:
                female.setChecked(true);
                male.setChecked(false);
                gender = "female";
                break;
        }
    }

    private void openPeopleForDialog() {
        dialogPeopleFor = new Dialog(getActivity());
        dialogPeopleFor.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPeopleFor.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogPeopleFor.setContentView(R.layout.dialog_people_for);
        dialogPeopleFor.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogPeopleFor.setCancelable(false);

        RecyclerView recyclerView = dialogPeopleFor.findViewById(R.id.recyclerView);
        CustomTextView btnNext = dialogPeopleFor.findViewById(R.id.btnNext);
        peopleProgress = dialogPeopleFor.findViewById(R.id.peopleProgress);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(context, R.dimen.grid_space);
        recyclerView.addItemDecoration(itemDecoration);
//        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        recyclerView.setLayoutManager(gridLayoutManager);
        peopleFor = new ArrayList<>();
        peopleProgress.setVisibility(View.VISIBLE);
        peopleProgress.spin();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getSettingsRes=" + res);
                            JSONObject response = new JSONObject(res);
                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject result = response.optJSONObject(Constants.TAG_RESULT);
                                JSONArray people_for = result.getJSONArray(Constants.TAG_PEOPLEFOR);

                                for (int i = 0; i < people_for.length(); i++) {
                                    JSONObject temp = people_for.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                                    map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                    map.put(Constants.TAG_ICON, DefensiveClass.optString(temp, Constants.TAG_ICON));
                                    peopleFor.add(map);
                                }

                                peopleAdapter = new PeopleAdapter(context, peopleFor);
                                recyclerView.setAdapter(peopleAdapter);
                                peopleAdapter.notifyDataSetChanged();
                                peopleProgress.setVisibility(View.GONE);
                                peopleProgress.stopSpinning();
                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(context, "Error", response.getString("message"));
                            } else {
                                peopleProgress.setVisibility(View.GONE);
                                peopleProgress.stopSpinning();
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
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HowzuApplication.isNetworkAvailable(getActivity())) {
                    if (peopleForId != null && !peopleForId.equalsIgnoreCase("")) {
                        setProfile("updatePeopleFor");
                    } else {
                        Toast.makeText(getActivity(), "Select anyone", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!dialogPeopleFor.isShowing()) {
            dialogPeopleFor.show();
        }
    }

    class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;

        public PeopleAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
        }

        @NonNull
        @Override
        public PeopleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.item_people_for, parent, false);//layout
            return new PeopleAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull PeopleAdapter.ViewHolder holder, int position) {
            try {
                final HashMap<String, String> map = Items.get(position);
                if (map.isEmpty()) {
                    holder.detail_lay.setVisibility(View.INVISIBLE);
                } else {
                    holder.detail_lay.setVisibility(View.VISIBLE);
                    holder.name.setText(map.get(Constants.TAG_NAME));
                    Picasso.with(mContext).load(map.get(Constants.TAG_ICON)).into(holder.icon);
                    if (peopleForId.equals("")) {
                        holder.icon_bg.setSelected(false);
                    } else {
                        if (peopleForId.equals(map.get(Constants.TAG_ID))) {
                            holder.icon_bg.setSelected(true);
                        } else {
                            holder.icon_bg.setSelected(false);
                        }
                    }
                }


                holder.detail_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        peopleForId = String.valueOf(map.get(Constants.TAG_ID));
                        notifyDataSetChanged();
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon, icon_bg;
            TextView name;
            RelativeLayout detail_lay;

            public ViewHolder(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon_bg = (ImageView) itemView.findViewById(R.id.icon_bg);
                name = itemView.findViewById(R.id.name);
                detail_lay = (RelativeLayout) itemView.findViewById(R.id.detail_lay);
//                int width = itemView.getWidth();
//                gridWidth = width * 47 / 100;
//                detail_lay.setLayoutParams(new LinearLayout.LayoutParams(gridWidth, gridWidth));
            }
        }
    }

    private void openPhotoDialog() {
        dialogAddPhotos = new Dialog(getActivity());
        dialogAddPhotos.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAddPhotos.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogAddPhotos.setContentView(R.layout.dialog_add_photos);
        dialogAddPhotos.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogAddPhotos.setCancelable(false);

        TextView btnSkip, btnAddPhoto, btnNext;
        ImageView userImageView;
        RelativeLayout imageLayout;
        btnSkip = dialogAddPhotos.findViewById(R.id.btnSkip);
        btnAddPhoto = dialogAddPhotos.findViewById(R.id.btnAddPhoto);
        btnNext = dialogAddPhotos.findViewById(R.id.btnNext);
        userImageView = dialogAddPhotos.findViewById(R.id.userImage);
        imageLayout = dialogAddPhotos.findViewById(R.id.imageLayout);
        photoProgress = dialogAddPhotos.findViewById(R.id.photoProgress);

        this.btnAddPhoto = btnAddPhoto;
        this.btnNext = btnNext;
        this.userImageView = userImageView;

        ViewGroup.LayoutParams params = userImageView.getLayoutParams();
        params.width = HowzuApplication.dpToPx(getActivity(), 150);
        params.height = HowzuApplication.dpToPx(getActivity(), 200);

        userImageView.setLayoutParams(params);
        Picasso.with(context).load(GetSet.getImageUrl()).resize(params.width, params.height)
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .centerCrop().into(userImageView);

        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (profileImage == null || profileImage.equalsIgnoreCase("")) {
                    dialogAddPhotos.dismiss();
                    if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                        dialogPeopleFor.dismiss();
                    }
                    openInterestDialog();
                } else {
                    if (HowzuApplication.isNetworkAvailable(getActivity())) {
                        imageUploading = true;
                        new uploadImage().execute(profileImage);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogAddPhotos.dismiss();
                if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                    dialogPeopleFor.dismiss();
                }
                openInterestDialog();
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(SignupFragment.this, "Select your image:");
                }
            }
        });

        userImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                } else {
                    ImagePicker.pickImage(SignupFragment.this, "Select your image:");
                }
            }
        });

        if (dialogAddPhotos != null) {
            dialogAddPhotos.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {

            boolean isPermissionEnabled = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = false;
                    break;
                } else {
                    isPermissionEnabled = true;
                }
            }

            if (!isPermissionEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                    } else {
                        openPermissionDialog("Camera and Write External Storage");
                    }
                }
            }
        }
    }

    private void openPermissionDialog(String permissionList) {
        Dialog permissionDialog = new Dialog(getActivity());
        permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        permissionDialog.setContentView(R.layout.logout_dialog);
        permissionDialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        permissionDialog.setCancelable(false);
        permissionDialog.setCanceledOnTouchOutside(false);

        TextView title = permissionDialog.findViewById(R.id.headerTxt);
        TextView subTxt = permissionDialog.findViewById(R.id.subTxt);
        TextView yes = permissionDialog.findViewById(R.id.yes);
        TextView no = permissionDialog.findViewById(R.id.no);
        title.setVisibility(View.GONE);
        subTxt.setText("This app requires " + permissionList + " permissions to access the features. Please turn on");
        yes.setText(R.string.grant);
        no.setText(R.string.nope);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
//                finish();
            }
        });

        no.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    private void requestPermission(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(getContext(), requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(getContext());
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "profile", timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage("profile", timestamp + ".jpg");
                profileImage = file.getAbsolutePath();
                //   String filepath = ImagePicker.getImageFilePath(this, requestCode, resultCode, data);
                Log.i(TAG, "selectedImageFile: " + file);
                Picasso.with(getContext()).load(file).resize(180, 250)
                        .centerCrop().into(userImageView);
                btnAddPhoto.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void openInterestDialog() {
        ImageView btnSearch, btnCancel;
        Display display;
        RecyclerView recyclerView;
        EditText edtSearch;
        CustomTextView btnNext;

        dialogInterest = new Dialog(getActivity());
        dialogInterest.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogInterest.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogInterest.setContentView(R.layout.dialog_interest);
        dialogInterest.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogInterest.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        dialogInterest.setCancelable(false);

        recyclerView = dialogInterest.findViewById(R.id.recyclerView);
        edtSearch = dialogInterest.findViewById(R.id.edtSearch);
        btnNext = dialogInterest.findViewById(R.id.btnNext);
        btnCancel = dialogInterest.findViewById(R.id.btnCancel);
        btnSearch = dialogInterest.findViewById(R.id.btnSearch);
        btnNext = dialogInterest.findViewById(R.id.btnNext);
        interestProgress = dialogInterest.findViewById(R.id.interestProgress);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnCancel.setVisibility(View.VISIBLE);
                } else {
                    btnCancel.setVisibility(View.GONE);
                }
                interestAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtSearch.setText("");
                btnCancel.setVisibility(View.GONE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HowzuApplication.isNetworkAvailable(getActivity())) {
                    if (selectedList.size() > 0)
                        setProfile("");
                    else
                        Toast.makeText(getActivity(), "Select anyone interest", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        interestProgress.setVisibility(View.VISIBLE);
        interestProgress.spin();

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getSettingsRes=" + res);
                            JSONObject response = new JSONObject(res);
                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject result = response.optJSONObject(Constants.TAG_RESULT);
                                JSONArray interest = result.getJSONArray(Constants.TAG_INTERESTS);

                                for (int i = 0; i < interest.length(); i++) {
                                    JSONObject temp = interest.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                                    map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                    interestList.add(map);
                                }

                                interestAdapter = new InterestAdapter(context, interestList);
                                recyclerView.setAdapter(interestAdapter);
                                interestAdapter.notifyDataSetChanged();
                                interestProgress.setVisibility(View.GONE);
                                interestProgress.stopSpinning();
                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(context, "Error", response.getString("message"));
                            } else {
                                interestProgress.setVisibility(View.GONE);
                                interestProgress.stopSpinning();
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
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        dialogInterest.show();
    }

    class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> implements Filterable {

        ArrayList<HashMap<String, String>> Items;
        ArrayList<HashMap<String, String>> interestsList;
        InterestAdapter.ViewHolder holder = null;
        private Context mContext;

        public InterestAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
            interestsList = data;
        }

        @NonNull
        @Override
        public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_interest, parent, false);//layout
            return new InterestAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InterestAdapter.ViewHolder holder, int position) {
            final HashMap<String, String> interest = Items.get(position);

            holder.txtInterest.setText(interest.get(Constants.TAG_NAME));
            if (selectedList.contains(interest.get(Constants.TAG_NAME))) {
                holder.btnInterest.setChecked(true);
                holder.txtInterest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                holder.btnInterest.setChecked(false);
                holder.txtInterest.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            }

            holder.btnInterest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });

            holder.txtInterest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.btnInterest.setChecked(!holder.btnInterest.isChecked());
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });

            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.btnInterest.setChecked(!holder.btnInterest.isChecked());
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });
        }

        private void setUI(CheckBox btnInterest, TextView txtInterest, boolean checked, int position) {
            btnInterest.setChecked(checked);
            if (checked) {
                selectedList.add(Items.get(position).get(Constants.TAG_NAME));
                txtInterest.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                selectedList.remove(Items.get(position).get(Constants.TAG_NAME));
                txtInterest.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public void setInterests(ArrayList<HashMap<String, String>> filteredInterests) {
            this.Items = filteredInterests;
        }


        @Override
        public android.widget.Filter getFilter() {
            return FilterHelper.newInstance(interestsList, this);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatCheckBox btnInterest;
            CustomTextView txtInterest;
            RelativeLayout itemLay;

            public ViewHolder(View itemView) {
                super(itemView);
                btnInterest = itemView.findViewById(R.id.btnInterest);
                txtInterest = itemView.findViewById(R.id.txtInterest);
                itemLay = itemView.findViewById(R.id.itemLay);
            }
        }
    }

    public static class FilterHelper extends Filter {
        static ArrayList<HashMap<String, String>> interestsList;
        static InterestAdapter adapter;

        public static FilterHelper newInstance(ArrayList<HashMap<String, String>> interestsList, InterestAdapter adapter) {
            FilterHelper.adapter = adapter;
            FilterHelper.interestsList = interestsList;
            return new FilterHelper();
        }

        /*
        - Perform actual filtering.
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                //HOLD FILTERS WE FIND
                ArrayList<HashMap<String, String>> foundFilters = new ArrayList<>();

                //ITERATE CURRENT LIST
                for (HashMap<String, String> hashMap : interestsList) {

                    //SEARCH
                    if (hashMap.get(Constants.TAG_NAME).toLowerCase().contains(constraint.toString().toLowerCase())) {
                        //ADD IF FOUND
                        foundFilters.add(hashMap);
                    }
                }

                //SET RESULTS TO FILTER LIST
                filterResults.count = foundFilters.size();
                filterResults.values = foundFilters;
            } else {
                //NO ITEM FOUND.LIST REMAINS INTACT
                filterResults.count = interestsList.size();
                filterResults.values = interestsList;
            }

            //RETURN RESULTS
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            adapter.setInterests((ArrayList<HashMap<String, String>>) filterResults.values);
            adapter.notifyDataSetChanged();
        }
    }

    private void setProfile(String dialogType) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
//                        if (pd != null && pd.isShowing()) {
//                            pd.dismiss();
//                        }

                        try {
                            Log.v(TAG, "saveSettingsRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                editor.putString(Constants.TAG_PEOPLE_FOR, peopleForId);
                                editor.putString(Constants.TAG_INTEREST, interest);
                                editor.commit();

                                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
                                    /*Set User Image strVisitingIdLikeToken FACEBOOK*/
                                    if (facebookId != null && !facebookId.equalsIgnoreCase("")) {
                                        GetSet.setImageUrl(profileImage);
                                        editor.putString(Constants.TAG_USERIMAGE, profileImage);
                                        editor.commit();
                                        imagesAry = new ArrayList<>();
                                        imagesAry.add(profileImage);
                                        try {
                                            JSONArray jsonArray = new JSONArray(imagesAry);
                                            imagesJson = String.valueOf(jsonArray);
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        peopleProgress.setVisibility(View.GONE);
                                        peopleProgress.stopSpinning();
                                        setProfile("updateImage");
                                    } else {
                                        if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                                            peopleProgress.setVisibility(View.GONE);
                                            peopleProgress.stopSpinning();
                                            dialogPeopleFor.dismiss();
                                        }
                                        openPhotoDialog();
                                    }
                                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                                    if (dialogAddPhotos != null && dialogAddPhotos.isShowing()) {
                                        photoProgress.setVisibility(View.GONE);
                                        photoProgress.stopSpinning();
                                        dialogAddPhotos.dismiss();
                                    }
                                    if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                                        dialogPeopleFor.dismiss();
                                    }
                                    openInterestDialog();
                                } else {
                                    interestProgress.setVisibility(View.GONE);
                                    interestProgress.stopSpinning();
                                    if (dialogInterest != null && dialogInterest.isShowing()) {
                                        dialogInterest.dismiss();
                                    }

                                    if (LoginActivity.lat == 0 || LoginActivity.longit == 0 || LoginActivity.loc.equals("")) {
                                        HashMap<String, String> fbdata = new HashMap<>();
                                        Intent i = new Intent(getActivity(),
                                                LocationActivity.class);
                                        i.putExtra("data", fbdata);
                                        startActivity(i);
                                    } else {
                                        getActivity().finish();
                                        Intent i = new Intent(getActivity(), MainScreenActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);

                                    }
                                }
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString("message"));
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
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }

                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
                    if (peopleProgress != null) {
                        peopleProgress.setVisibility(View.GONE);
                        peopleProgress.stopSpinning();
                    }
                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                    if (photoProgress != null) {
                        photoProgress.setVisibility(View.GONE);
                        photoProgress.stopSpinning();
                    }
                } else {
                    if (interestProgress != null) {
                        interestProgress.setVisibility(View.GONE);
                        interestProgress.stopSpinning();
                    }
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
                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
                    map.put(Constants.TAG_PEOPLE_FOR, peopleForId);
                    map.put(Constants.TAG_SHOW_AGE, pref.getString(Constants.TAG_SHOW_AGE, ""));
                    map.put(Constants.TAG_SHOW_LOCATION, pref.getString(Constants.TAG_SHOW_LOCATION, ""));
                    map.put(Constants.TAG_INVISIBLE, "" + pref.getString(Constants.TAG_INVISIBLE, ""));
                    map.put(Constants.TAG_HIDE_ADS, "" + pref.getBoolean(Constants.TAG_HIDE_ADS, true));
                    map.put(Constants.TAG_MESSAGE_NOTIFICATION, "" + pref.getBoolean(Constants.TAG_MESSAGE_NOTIFICATION, true));
                    map.put(Constants.TAG_LIKE_NOTIFICATION, "" + pref.getBoolean(Constants.TAG_LIKE_NOTIFICATION, true));
                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                    map.put(Constants.TAG_USERIMAGE, profileImage);
                    map.put(Constants.TAG_IMAGES, imagesJson);
                } else {
                    JSONArray intjson = new JSONArray(selectedList);
                    interest = String.valueOf(intjson);
                    map.put(Constants.TAG_INTERESTS, interest);
                }
                Log.v(TAG, "saveSettingsParams=" + map);
                return map;
            }
        };

        if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
            if (peopleProgress != null) {
                peopleProgress.setVisibility(View.VISIBLE);
                peopleProgress.spin();
            }
        } else if (dialogType.equalsIgnoreCase("updateImage")) {
            if (photoProgress != null) {
                photoProgress.setVisibility(View.VISIBLE);
                photoProgress.spin();
            }
        } else {
            if (interestProgress != null) {
                interestProgress.setVisibility(View.VISIBLE);
                interestProgress.spin();
            }
        }

//        if (!pd.isShowing()) {
//            pd.show();
//        }
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    public void setHashMap(HashMap<String, String> hashMap) {
        this.hashMap = hashMap;
    }

    class uploadImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "", status, existingFileName = "";

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_IMAGE;
            try {
                existingFileName = imgpath[0];
                Log.v(TAG, " existingFileName=" + existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("user");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e(TAG, "MediaPlayer-Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v(TAG, "buffer=" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v(TAG, "bytesRead=" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e(TAG, "MediaPlayer-File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e(TAG, "MediaPlayer-error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e(TAG, "MediaPlayer-error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e(TAG, "MediaPlayer-Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e(TAG, "MediaPlayer-error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v(TAG, "json=" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String name = DefensiveClass.optString(image, "Name");
                    String viewUrl = DefensiveClass.optString(image, "View_url");

                    File dir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "Images/MainViewProfileDetailActivity");

                    if (dir.exists()) {
                        File from = new File(existingFileName);
                        File to = new File(dir + "/" + name);
                        if (from.exists())
                            from.renameTo(to);
                    }

                    imagesAry = new ArrayList<>();
                    imagesAry.add(viewUrl);
                    try {
                        JSONArray jsonArray = new JSONArray(imagesAry);
                        imagesJson = String.valueOf(jsonArray);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!getActivity().isFinishing()) {
//                pd.show();
                photoProgress.setVisibility(View.VISIBLE);
                photoProgress.spin();
            }
        }

        @Override
        protected void onPostExecute(Integer unused) {
            try {
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }
                photoProgress.setVisibility(View.GONE);
                photoProgress.stopSpinning();
                profileImage = imagesAry.get(0);
                imageUploading = false;
                GetSet.setImageUrl(profileImage);
                editor.putString(Constants.TAG_USERIMAGE, profileImage);
                editor.commit();
                setProfile("updateImage");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}