package com.hitasoft.app.howzu;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;
//import com.baidu.location.LocationClient;
//import com.baidu.location.LocationClientOption;
//import com.baidu.mapapi.model.LatLng;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsResult;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SharedPrefManager;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baidu.mapapi.map.MyLocationData;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
/**
 * Created by hitasoft on 9/6/15.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener
        , GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
//        , BDLocationListener
//        , LocationListener
{
    double doubleLatitude=0,doubleLongitude=0;

    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1, PERMISSION_ACCESS_LOCATION = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000, FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static boolean isLocationEnabled = true;
    public static double lat = 0, longit = 0;
    public static String loc = "";
    private static int SPLASH_TIME_OUT = 2000;
    String strLocationAddress="";
    String TAG = "LoginActivity", loginFrom = "";
    ViewPager viewPager;
    CirclePageIndicator pageIndicator;
    TextView textViewLoginWithMobile, fb, terms;
    LinearLayout loginLay;
    ImageView logo;
    ProgressDialog dialog;
    ViewPagerAdapter pagerAdapter;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    HashMap<String, String> fbdata = new HashMap<String, String>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+", albumId = "";
    String[] header = new String[]{}, content = new String[]{};
    boolean splashLoading = false;
    CallbackManager callbackManager;
    ArrayList<String> profileImgs = new ArrayList<>(), interests = new ArrayList<>();
    Geocoder geocoder;
    List<Address> addresses;
    public static Location mylocation;
    GoogleApiClient googleApiClient;
    boolean updateAvailable = false;
    private String androidVersion, androidUpdate, versionName;
    private int versionCode;
    LocationClient mLocationClient ;
    Dialog updateDialog, permissionDialog;
    boolean fromPlayStore = false;
    private static String MAX_AGE = "70";
    private boolean forceUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        viewPager = findViewById(R.id.view_pager);
        pageIndicator = findViewById(R.id.pager_indicator);
        fb = findViewById(R.id.fb_txt);
        textViewLoginWithMobile = findViewById(R.id.textViewLoginWithMobile);
        loginLay = findViewById(R.id.loginLay);
        terms = findViewById(R.id.terms_txt);
        logo = findViewById(R.id.logo);

//
//        mLocationClient = new LocationClient(this);
//        LocationClientOption Option = new LocationClientOption ();
//        Option.setIsNeedAddress (true);
//        Option.setAddrType ("all");
//        mLocationClient.setLocOption (Option);
//        mLocationClient.registerLocationListener (this);
//        mLocationClient.start ();


        fb.setOnClickListener(this);
        textViewLoginWithMobile.setOnClickListener(this);
        terms.setOnClickListener(this);
//        setUpGClient();
        checkLocation();
//        checkForUpdates();

        if (!checkPermissions()) {
            requestPermission();
        }

        header = new String[]{getString(R.string.find_people_near_you), getString(R.string.swipe_find_matches), getString(R.string.chat_with_your_matches)};
        content = new String[]{getString(R.string.lorem_ipsum), getString(R.string.lorem_ipsum), getString(R.string.lorem_ipsum)};
        // mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pagerAdapter = new ViewPagerAdapter(LoginActivity.this, header);
        viewPager.setAdapter(pagerAdapter);
        pageIndicator.setViewPager(viewPager);

        pref = getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
//        editor = pref.edit();
//        editor.putString(Constants.TAG_USERID,"76235890");
//        editor.apply();
    //    editor.commit();
        String strUserID=pref.getString(Constants.TAG_USERID,null);

        if(strUserID!=null)
        {
            Intent intent=new Intent(LoginActivity.this,MainScreenActivity.class);
            intent.putExtra(Constants.TAG_USERID,strUserID);
            startActivity(intent);
            finish();
        }
       System.out.println("jigar the user id at login is "+strUserID);

        Constants.pref = getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        Constants.editor = pref.edit();
        if (pref.getBoolean("isLogged", false)) {
            splashLoading = true;
            loginLay.setVisibility(View.GONE);
        } else {
            splashLoading = false;
            loginLay.setVisibility(View.VISIBLE);
        }

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.hitasoft.app.howzu", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e(TAG, "hash key=" + something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(TAG, "name not found=" + e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "no such an algorithm=" + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "exception=" + e.toString());
        }

        //loginToFacebook();
    }

    private void checkForUpdates() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject jsonResponse = new JSONObject(res);
                            Log.v(TAG, "checkForUpdatesRes=" + jsonResponse);

                            PackageInfo packageInfo = null;
                            try {
                                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                versionName = packageInfo.versionName;
                                versionCode = packageInfo.versionCode;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (jsonResponse.getString(Constants.TAG_STATUS).equals("true")) {
                                JSONObject result = jsonResponse.optJSONObject(Constants.TAG_RESULT);
                                MAX_AGE = result.getString(Constants.TAG_MAX_AGE);
                                GetSet.setMaxAge(MAX_AGE);
                                androidVersion = result.getString(Constants.TAG_ANDROID_VERSION);
                                androidUpdate = result.getString(Constants.TAG_ANDROID_UPDATE);
                                if (Double.parseDouble(androidVersion) > Double.parseDouble(versionName)) {
                                    updateAvailable = true;
                                    checkLocation();
                                    //updateConfirmDialog(result);
                                } else {
                                    checkLocation();
                                }
                            } else {
                                checkLocation();
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
                checkLocation();
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Log.v(TAG, "checkForUpdatesParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void setLocation() {
        if (mylocation != null && GetSet.isLogged()) {
            lat = mylocation.getLatitude();
            longit = mylocation.getLongitude();
            Log.d(TAG, "getlocationLat=" + lat);
            Log.d(TAG, "getlocationLon=" + longit);
            GetSet.setLatitude(lat);
            GetSet.setLongitude(longit);
            loc = HowzuApplication.getLoc(LoginActivity.this, lat, longit);
            GetSet.setLocation(loc);
            updateUserLocation();
            Log.d(TAG, "getlocationName=" + loc);
        } else if (mylocation != null && !GetSet.isLogged()) {
            lat = mylocation.getLatitude();
            longit = mylocation.getLongitude();
            loc = HowzuApplication.getLoc(LoginActivity.this, lat, longit);
            moveToNextActivity();
        } else if (mylocation == null) {
            checkLocation();
        }
    }

    private void checkLocation() {
        Log.v("checkLocation", "checkLocation");
        if (googleApiClient == null) {
         //   setUpGClient();
        } else if (mylocation == null) {
       //     getMyLocation();
        } else {
            setLocation();
        }
    }

//    private synchronized void setUpGClient() {
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, 0, this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();
//    }

//    private void getMyLocation() {
//        if (googleApiClient != null) {
//            if (!googleApiClient.isConnected())
//                googleApiClient.connect();
//            /*Time delay for to get location after googleApiClient connected*/
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (googleApiClient.isConnected()) {
//                        int permissionLocation = ContextCompat.checkSelfPermission(LoginActivity.this,
//                                ACCESS_FINE_LOCATION);
//                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//                            mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//                            LocationRequest locationRequest = new LocationRequest();
//                            locationRequest.setInterval(3000);
//                            locationRequest.setFastestInterval(3000);
//                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                                    .addLocationRequest(locationRequest);
//                            builder.setAlwaysShow(true);
//                            LocationServices.FusedLocationApi
//                                    .requestLocationUpdates(googleApiClient, locationRequest, LoginActivity.this);
//                            PendingResult result =
//                                    LocationServices.SettingsApi
//                                            .checkLocationSettings(googleApiClient, builder.build());
//                            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//
//                                @Override
//                                public void onResult(LocationSettingsResult result) {
//                                    final Status status = result.getStatus();
//                                    switch (status.getStatusCode()) {
//                                        case LocationSettingsStatusCodes.SUCCESS:
//                                            // All location settings are satisfied.
//                                            // You can initialize location requests here.
//                                            int permissionLocation = ContextCompat
//                                                    .checkSelfPermission(LoginActivity.this,
//                                                            ACCESS_FINE_LOCATION);
//                                            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//                                                mylocation = LocationServices.FusedLocationApi
//                                                        .getLastLocation(googleApiClient);
//                                                Log.v(TAG, "mylocation=" + mylocation);
//                                                setLocation();
//                                            }
//                                            break;
//                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                            // LocationActivity settings are not satisfied.
//                                            // But could be fixed by showing the user a dialog.
//                                            try {
//                                                // Show the dialog by calling startResolutionForResult(),
//                                                // and check the result in onActivityResult().
//                                                // Ask to turn on GPS automatically
//                                                status.startResolutionForResult(LoginActivity.this,
//                                                        REQUEST_CHECK_SETTINGS_GPS);
//                                            } catch (IntentSender.SendIntentException e) {
//                                                // Ignore the error.
//                                            }
//                                            break;
//                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                            // LocationActivity settings are not satisfied. However, we have no way to fix the
//                                            // settings so we won't show the dialog.
//                                            //finish();
//                                            break;
//                                    }
//                                }
//                            });
//                        }
//                    }
//                }
//            }, 1000);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        checkLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        checkLocation();
                        break;
                }
                break;
        }
    }


    private boolean checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(LoginActivity.this, ACCESS_FINE_LOCATION);
        return permissionLocation == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        if (requestCode == 100) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this,
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                checkLocation();
            } else {
//                recreate();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        requestPermission();
                    } else {
                        openPermissionDialog();
                    }
                }
            }
        }
    }

    private void openPermissionDialog() {
        permissionDialog = new Dialog(LoginActivity.this);
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
        subTxt.setText(R.string.location_permission_description);
        yes.setText(R.string.grant);
        no.setText(R.string.nope);

        if (GetSet.isLogged()) {
            no.setVisibility(View.GONE);
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    private void updateConfirmDialog(JSONObject jsonResponse) {
        updateDialog = new Dialog(LoginActivity.this);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        updateDialog.setContentView(R.layout.logout_dialog);
        updateDialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        updateDialog.setCancelable(false);
        updateDialog.setCanceledOnTouchOutside(false);

        TextView title = updateDialog.findViewById(R.id.headerTxt);
        TextView subTxt = updateDialog.findViewById(R.id.subTxt);
        TextView yes = updateDialog.findViewById(R.id.yes);
        TextView no = updateDialog.findViewById(R.id.no);
        title.setVisibility(View.GONE);
        subTxt.setText(R.string.update_description);
        yes.setText(R.string.okay);
        no.setText(R.string.nope);

        try {
            if (jsonResponse.getString(Constants.TAG_ANDROID_UPDATE).equals("1")) {
                no.setVisibility(View.GONE);
                forceUpdate = true;
            } else {
                no.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromPlayStore = true;
                updateDialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateDialog.isShowing())
                    updateDialog.dismiss();
                checkLocation();
            }
        });

        updateDialog.show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

//    @Override
//    public void onLocationChanged(Location location) {
//        Log.v("onLocationChanged", "onLocationChanged");
//        mylocation = location;
//        lat = mylocation.getLatitude();
//        longit = mylocation.getLongitude();
//        Log.d(TAG, "onLocationChangedLat=" + lat);
//        Log.d(TAG, "onLocationChangedLon=" + longit);
//        loc = HowzuApplication.getLoc(LoginActivity.this, lat, longit);
//        moveToNextActivity();
//    }

    private void moveToNextActivity() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        //if (!updateAvailable)

        {
            if (loginFrom.equals("email")) {
                Intent i = new Intent(LoginActivity.this, EmailLogin.class);
                startActivity(i);
            } else if (loginFrom.equals("fb")) {
                loginToFacebook();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "user_location", "public_profile", "user_hometown", "user_about_me", "user_birthday", "user_education_history", "user_work_history", "user_likes", "user_photos"));
            }
        }

        if (googleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
        if (fromPlayStore && forceUpdate) {
            fromPlayStore = false;
            forceUpdate = false;
            checkLocation();
//            checkForUpdates();
        } else if (fromPlayStore) {
            fromPlayStore = false;
            checkLocation();
        }
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.stopAutoManage(this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !splashLoading) {
            moveTaskToBack(true);
            finish();
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }

    private void showHome() {
        splashLoading = true;
        loginLay.setVisibility(View.GONE);
        getAccessToken();
    }

    private void getAccessToken() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ACCESS_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject results = new JSONObject(res);
                            Log.e(TAG, "getAccessTokenResult: " + results);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                String accessToken = DefensiveClass.optString(results, Constants.TAG_ACCESS_TOKEN);
                                editor.putString(Constants.TAG_AUTHORIZATION, accessToken);
                                editor.putString(Constants.TAG_EXPIRY, DefensiveClass.optString(results, Constants.TAG_EXPIRY));
                                editor.commit();

                                if ((updateAvailable && updateDialog != null && !updateDialog.isShowing()) || !updateAvailable) {
                                    GetSet.setLogged(true);
                                    GetSet.setUserId(pref.getString(Constants.TAG_USERID, null));
                                    GetSet.setUserName(pref.getString(Constants.TAG_USERNAME, null));
                                    GetSet.setImageUrl(pref.getString(Constants.TAG_USERIMAGE, null));
                                    if (pref.getBoolean(Constants.TAG_PREMIUM_MEMBER, false)) {
                                        GetSet.setPremium(true);
                                    } else {
                                        GetSet.setPremium(false);
                                    }
                                    GetSet.setLocation(pref.getString(Constants.TAG_LOCATION, ""));
                                    Log.v(TAG, "show home");
                                    if (!LoginActivity.this.isDestroyed()) {
                                        Intent i = new Intent(LoginActivity.this,
                                                MainScreenActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        LoginActivity.this.finish();
                                    }
                                    overridePendingTransition(R.anim.fade_in,
                                            R.anim.fade_out);
                                }
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
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /**
     * Function to login into facebook
     */
    @SuppressWarnings("deprecation")
    public void loginToFacebook() {
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(final JSONObject profile, GraphResponse response) {
                                Log.v(TAG + "Mode", "completeJsonobject" + profile);
                                // Application code

                                LoginActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        terms.setOnClickListener(LoginActivity.this);
                                        try {
                                            dialog.show();
                                            if (profile.has("email")) {
                                                // getting name of the user
                                                String name = DefensiveClass.optString(profile, "name");

                                                // getting email of the user
                                                String email = DefensiveClass.optString(profile, "email");

                                                // getting strFriendID of the user
                                                String userId = DefensiveClass.optString(profile, "id");

//                                                String textViewProfileLocation = DefensiveClass.optString(profile, "about");
//                                                String age = DefensiveClass.optString(profile, "birthday");
//
//                                                getFBInterest(profile.optJSONObject("likes"));
//                                                new getFacebookAlbums().execute().get();
//                                                Log.v(TAG + "Mode", "albumId=" + albumId);
//                                                new getFacebookImages().execute(albumId).get();
//                                                String info = "";
//                                                String work = getFacebookWork(profile.optJSONArray("work"));
//                                                String education = getFacebookEducation(profile.optJSONArray("education"));
//                                                if (!work.equals("")) {
//                                                    info = work;
//                                                } else if (!education.equals("")) {
//                                                    info = education;
//                                                } else {
//                                                    info = "";
//                                                }

                                                if (profileImgs.size() > 0) {
                                                    fbdata.put(Constants.TAG_IMAGE_URL, profileImgs.get(0));
                                                } else {
                                                    String img = "https://graph.facebook.com/" + userId + "/picture?width=9999";
                                                    profileImgs.add(img);
                                                    fbdata.put(Constants.TAG_IMAGE_URL, img);
                                                }

                                                String interest = "", images = "";
                                                try {
                                                    JSONArray intjson = new JSONArray(interests);
                                                    interest = String.valueOf(intjson);
                                                    JSONArray imgjson = new JSONArray(profileImgs);
                                                    images = String.valueOf(imgjson);
                                                } catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

//                                                if (age.equals("")) {
//                                                    age = "21";
//                                                } else {
//                                                    age = Integer.toString(getAge(age));
//                                                }

                                                fbdata.put(Constants.TAG_TYPE, "facebook");
                                                fbdata.put(Constants.TAG_ID, userId);
                                                fbdata.put(Constants.TAG_USERNAME, name);
                                                fbdata.put(Constants.TAG_EMAIL, email);
//                                                fbdata.put(Constants.TAG_BIO, textViewProfileLocation);
//                                                fbdata.put(Constants.TAG_AGE, age);
//                                                fbdata.put(Constants.TAG_GENDER, DefensiveClass.optString(profile, "gender"));
                                                fbdata.put(Constants.TAG_DEVICE_ID, Settings.Secure.getString(getContentResolver(),
                                                        Settings.Secure.ANDROID_ID));
//                                                fbdata.put(Constants.TAG_INTEREST, interest);
                                                fbdata.put(Constants.TAG_IMAGES, images);
//                                                fbdata.put(Constants.TAG_INFO, info);

                                                fbdata.put(Constants.TAG_LAT, Double.toString(lat));
                                                fbdata.put(Constants.TAG_LON, Double.toString(longit));
                                                fbdata.put(Constants.TAG_LOCATION, loc);
                                                Log.v(TAG + "Mode", "fbdata" + fbdata);


                                                sendData(fbdata);
                                            } else {
                                                if (dialog != null && dialog.isShowing()) {
                                                    dialog.dismiss();
                                                }
                                                Toast.makeText(LoginActivity.this, "Your facebook account doesn't contain Email, Please add it and try again", Toast.LENGTH_LONG).show();
                                            }

                                        } catch (Exception e) {
                                            Log.e(TAG, "FaceBookLogIn " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        });
                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,email,birthday,gender,location,hometown,about,likes,work,education");
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }


            @Override
            public void onCancel() {
                terms.setOnClickListener(LoginActivity.this);
                Toast.makeText(LoginActivity.this, "Login - Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                terms.setOnClickListener(LoginActivity.this);
                Toast.makeText(LoginActivity.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                if (exception instanceof FacebookAuthorizationException) {
                    if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });
    }

    private void getFBInterest(JSONObject likes) {
        try {
            JSONArray data = likes.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject temp = data.getJSONObject(i);
                String name = DefensiveClass.optString(temp, "name");

                interests.add(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFacebookWork(JSONArray work) {
        String fbwork = "";
        try {
            if (work != null && work.length() > 0) {
                JSONObject temp = work.getJSONObject(0);
                String employer = "";
                JSONObject empl = temp.optJSONObject("employer");
                if (empl != null) {
                    employer = DefensiveClass.optString(empl, "name");
                }
                String position = "";
                JSONObject pos = temp.optJSONObject("position");
                if (empl != null) {
                    position = DefensiveClass.optString(pos, "name");
                }
                fbwork = position + " at " + employer;
            }
        } catch (JSONException e) {
            fbwork = "";
            e.printStackTrace();
        } catch (NullPointerException e) {
            fbwork = "";
            e.printStackTrace();
        } catch (Exception e) {
            fbwork = "";
            e.printStackTrace();
        }
        return fbwork;
    }

    private String getFacebookEducation(JSONArray educ) {
        String fbedu = "";
        try {
            if (educ != null && educ.length() > 0) {
                JSONObject temp = educ.getJSONObject(0);
                String school = "";
                JSONObject scl = temp.optJSONObject("school");
                if (scl != null) {
                    school = DefensiveClass.optString(scl, "name");
                }
                fbedu = "Studied at " + school;
            }
        } catch (JSONException e) {
            fbedu = "";
            e.printStackTrace();
        } catch (NullPointerException e) {
            fbedu = "";
            e.printStackTrace();
        } catch (Exception e) {
            fbedu = "";
            e.printStackTrace();
        }
        return fbedu;
    }

    private int getAge(String date) {
        int[] dat = new int[3];
        if (date.contains("/")) {
            String[] tmp = date.split("/");
            dat[0] = Integer.parseInt(tmp[0]);
            dat[1] = Integer.parseInt(tmp[1]);
            dat[2] = Integer.parseInt(tmp[2]);
        }
        Calendar dob = Calendar.getInstance();
        dob.set(dat[2], dat[0], dat[1]);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
            age--;
        return age;
    }

    /**
     * API Implementation
     **/
    private void updateUserLocation() {
        final String mylocation = HowzuApplication.getLoc(this, GetSet.getLatitude(), GetSet.getLongitude());
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject results = new JSONObject(res);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setLocation(mylocation);
                                getProfile();
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
                getProfile();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
                Log.v(TAG, "updateUserLocationHeaders=" + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, pref.getString(Constants.TAG_USERID, ""));
                map.put(Constants.TAG_LAT, Double.toString(lat));
                map.put(Constants.TAG_LON, Double.toString(longit));
                if (mylocation.equals("")) {
                    map.put(Constants.TAG_LOCATION, "");
                } else {
                    map.put(Constants.TAG_LOCATION, mylocation);
                }
                Log.v(TAG, "updateUserLocationParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void sendData(final HashMap<String, String> datas) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "sendDataRes=" + res);
                            JSONObject results = new JSONObject(res);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                if (updateDialog != null && updateDialog.isShowing()) {
                                    updateDialog.dismiss();
                                }
                                Intent i = new Intent(LoginActivity.this, EmailLogin.class);
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put(Constants.TAG_ID, datas.get(Constants.TAG_ID));
                                hashMap.put(Constants.TAG_TYPE, datas.get(Constants.TAG_TYPE));
                                hashMap.put(Constants.TAG_EMAIL, datas.get(Constants.TAG_EMAIL));
                                hashMap.put(Constants.TAG_DEVICE_ID, datas.get(Constants.TAG_DEVICE_ID));
                                hashMap.put(Constants.TAG_USERNAME, datas.get(Constants.TAG_USERNAME));
                                hashMap.put(Constants.TAG_IMAGES, datas.get(Constants.TAG_IMAGES));
                                hashMap.put(Constants.TAG_IMAGE_URL, datas.get(Constants.TAG_IMAGE_URL));
                                hashMap.put(Constants.TAG_LAT, datas.get(Constants.TAG_LAT));
                                hashMap.put(Constants.TAG_LON, datas.get(Constants.TAG_LON));
                                hashMap.put(Constants.TAG_LOCATION, datas.get(Constants.TAG_LOCATION));
                                hashMap.put(Constants.TAG_AVAILABILITY, results.optString(Constants.TAG_AVAILABILITY));
                                i.putExtra("hashMap", hashMap);
                                startActivity(i);
                                    /*JSONObject values = results.getJSONObject(Constants.TAG_PEOPLES);

                                    GetSet.setLogged(true);
                                    GetSet.setUserId(DefensiveClass.optString(values, Constants.TAG_USERID));
                                    GetSet.setUserName(DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                    GetSet.setImageUrl(DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
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

                                    addDeviceId();

                                    setOnlineStatus();

                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }

                                    if (DefensiveClass.optString(values, Constants.TAG_LAT).equals("0")
                                            || DefensiveClass.optString(values, Constants.TAG_LON).equals("0")
                                            || DefensiveClass.optString(values, Constants.TAG_LOCATION).equals("")) {
                                        finish();
                                        Intent i = new Intent(LoginActivity.this,
                                                LocationActivity.class);
                                        i.putExtra("data", fbdata);
                                        startActivity(i);
                                    } else {
                                        GetSet.setLocation(DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                        finish();
                                        Log.v("LOCC send data", "send data");
                                        Intent i = new Intent(LoginActivity.this, MainScreenActivity.class);
                                        startActivity(i);
                                    }

                                    overridePendingTransition(R.anim.fade_in,
                                            R.anim.fade_out);*/

                            } else if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                CommonFunctions.dialog(LoginActivity.this, "Error", results.getString(Constants.TAG_MESSAGE));
                            } else {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(LoginActivity.this, results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_TYPE, datas.get(Constants.TAG_TYPE));
                map.put(Constants.TAG_ID, datas.get(Constants.TAG_ID));
                map.put(Constants.TAG_USERNAME, datas.get(Constants.TAG_USERNAME));
                map.put(Constants.TAG_EMAIL, datas.get(Constants.TAG_EMAIL));
                map.put(Constants.TAG_IMAGE_URL, datas.get(Constants.TAG_IMAGE_URL));
                map.put(Constants.TAG_LAT, datas.get(Constants.TAG_LAT));
                map.put(Constants.TAG_LON, datas.get(Constants.TAG_LON));
                map.put(Constants.TAG_DEVICE_ID, datas.get(Constants.TAG_DEVICE_ID));
                map.put(Constants.TAG_LOCATION, datas.get(Constants.TAG_LOCATION));
                map.put(Constants.TAG_IMAGES, datas.get(Constants.TAG_IMAGES));

                Log.v(TAG, "sendDataParams=" + map);
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
                        Log.v(TAG, "setOnlineStatusRes=" + res);
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
                Log.i(TAG, "getHeaders: " + map);
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

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_DEVICE_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            if (!DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(LoginActivity.this, json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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

    private void getProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getProfileRes=" + res);

                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject values = json.getJSONObject(Constants.TAG_RESULT);

                                GetSet.setLocation(DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                editor.putString(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));

                                GetSet.setImageUrl(DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                editor.putString(Constants.TAG_USERIMAGE, GetSet.getImageUrl());

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

                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(LoginActivity.this, "Error", json.getString(Constants.TAG_MESSAGE));
                            }

                            showHome();

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
                showHome();
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
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "getProfileParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewLoginWithMobile:
                if (ContextCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                } else {
                    if (mylocation == null) {
                        loginFrom = "email";
                       // dialog.show();
                        Intent intent=new Intent(LoginActivity.this,EmailLogin.class);
                        startActivity(intent);
//                        checkLocation();
                    } else {
                        Intent i = new Intent(LoginActivity.this, EmailLogin.class);
                        if (updateDialog != null && updateDialog.isShowing()) {
                            updateDialog.dismiss();
                        }
                        startActivity(i);
                    }
                }
                break;
            case R.id.fb_txt:
                if (ContextCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{ACCESS_FINE_LOCATION}, 100);
                } else {
                    if (mylocation == null) {
                        loginFrom = "fb";
                        dialog.show();
                    } else {
                        terms.setOnClickListener(null);
                        loginToFacebook();
                        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "user_location", "public_profile", "user_hometown", "user_about_me", "user_birthday", "user_education_history", "user_work_history", "user_likes", "user_photos"));
                    }
                }
                break;
            case R.id.terms_txt:
                Intent e = new Intent(LoginActivity.this, TOS.class);
                if (updateDialog != null && updateDialog.isShowing()) {
                    updateDialog.dismiss();
                }
                startActivity(e);
                break;
        }
    }


    private class getFacebookAlbums extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + AccessToken.getCurrentAccessToken().getUserId() + "/albums", null, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.d(TAG + "Mode", "Facebook Albums: " + response.toString());
                                try {
                                    if (response.getError() == null) {
                                        JSONObject joMain = response.getJSONObject();
                                        if (joMain.has("data")) {
                                            JSONArray jaData = joMain.optJSONArray("data");
                                            for (int i = 0; i < jaData.length(); i++) {
                                                JSONObject joAlbum = jaData.getJSONObject(i);
                                                String name = DefensiveClass.optString(joAlbum, "name");
                                                if (name.equalsIgnoreCase("MainViewProfileDetailActivity Pictures")) {
                                                    String id = DefensiveClass.optString(joAlbum, "id");
                                                    albumId = id;
                                                    Log.v(TAG + "Mode", "id=" + albumId);
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d(TAG + "Mode", response.getError().toString());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class getFacebookImages extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Bundle parameters = new Bundle();
            parameters.putString("fields", "images");
            Log.v(TAG + "Mode", "getFacebookImages");
            try {
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + params[0] + "/photos", parameters, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.v(TAG + "Mode", "Facebook Photos response: " + response.getJSONObject());
                                try {
                                    if (response.getError() == null) {
                                        JSONObject joMain = response.getJSONObject();
                                        if (joMain.has("data")) {
                                            JSONArray jaData = joMain.optJSONArray("data");
                                            for (int i = 0; i < jaData.length(); i++) {
                                                JSONObject joAlbum = jaData.getJSONObject(i);
                                                JSONArray jaImages = joAlbum.getJSONArray("images");
                                                if (i < 5) {
                                                    if (jaImages.length() > 0) {
                                                        String source = jaImages.getJSONObject(0).getString("source");
                                                        profileImgs.add(source);
                                                    }
                                                } else {
                                                    break;
                                                }
                                            }
                                            //set your adapter here
                                            Log.v(TAG + "Mode", "profileImgs=" + profileImgs);
                                        }
                                    } else {
                                        Log.v(TAG + "Mode", response.getError().toString());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*OnClick Event*/

    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        String[] temp;

        public ViewPagerAdapter(Context act, String[] newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.length;

        }

        public Object instantiateItem(ViewGroup collection, int posi) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.login_pager_items,
                    collection, false);

            TextView headerTxt = itemView.findViewById(R.id.header);
            TextView contentTxt = itemView.findViewById(R.id.content);

            headerTxt.setText(header[posi]);
            contentTxt.setText(content[posi]);

            collection.addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

//    @Override
//    public void onReceiveLocation(BDLocation bdLocation) {
//        try {
//            String Province = bdLocation.getProvince();
//
//            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
//            System.out.println("jigar the location we have is " + bdLocation.getLatitude());
//            System.out.println("jigar the location we have is " + Province);
//
//            doubleLatitude=bdLocation.getLatitude();
//            doubleLongitude=bdLocation.getLongitude();
////            if(bdLocation.getLatitude()==4.9E-324) {
////                doubleLatitude = 21.189838;
////                doubleLongitude = 72.812643;
////            }
////            if(strBuildingName!=null)
////            {
////                strLocationAddress=strBuildingName;
////            }
//            strLocationAddress= bdLocation.getCity()+","+bdLocation.getProvince()+","+bdLocation.getCountry();
//            if(bdLocation.getCity().equals("null") || bdLocation.getCity()==null)
//            {
//                strLocationAddress="Surat,Gujarat,India";
//            }
//
//            System.out.println("jigar the location we have is " + strLocationAddress);
//        }catch (Exception e)
//        {
//            System.out.println("jigar the error in location we have is " + e);
//
//        }
//
//
//    }
}
