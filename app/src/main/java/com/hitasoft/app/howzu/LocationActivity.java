package com.hitasoft.app.howzu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.PlacesAutoCompleteAdapter;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 25/11/16.
 */

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "LocationActivity";
    AutoCompleteTextView location;
    TextInputLayout input_layout_location;
    TextView locationTxt, join;
    ProgressDialog dialog;

    HashMap<String, String> fbdata = new HashMap<String, String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private boolean isFromMyProfile = false, isFromFindPeople = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        location = findViewById(R.id.location);
        input_layout_location = findViewById(R.id.input_layout_location);
        locationTxt = findViewById(R.id.locationTxt);
        join = findViewById(R.id.join);

        join.setOnClickListener(this);

        if ((HashMap<String, String>) getIntent().getExtras().get("data") != null) {
            fbdata = (HashMap<String, String>) getIntent().getExtras().get("data");
        }

        if (getIntent().getStringExtra("isFrom") != null && !getIntent().getStringExtra("isFrom").equalsIgnoreCase("")) {
            if (getIntent().getStringExtra("isFrom").equalsIgnoreCase("myProfile")) {
                isFromMyProfile = true;
            } else if (getIntent().getStringExtra("isFrom").equalsIgnoreCase("findPeople")) {
                isFromFindPeople = true;
            }
        }
        location.setAdapter(new PlacesAutoCompleteAdapter(LocationActivity.this, R.layout.dropdown_layout));

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    locationTxt.setVisibility(View.GONE);
                }
            }
        });

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        dialog = new ProgressDialog(LocationActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
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


    /**
     * API Implementation
     */

    /*private void getLocationFromString(final String params) {
        String url = "";
        try {
            url = "http://maps.google.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(params, "UTF-8") + "&ka&sensor=false&key=" + PlacesAutoCompleteAdapter.API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");

                            if (jsonArray.length() == 0) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.valid_location), Toast.LENGTH_LONG).show();
                            } else {
                                double lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lng");

                                double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lat");
                                Log.v("lat & lon", " lat = " + lat + " &lon = " + lon);

                                fbdata.put(Constants.TAG_LAT, Double.toString(lat));
                                fbdata.put(Constants.TAG_LON, Double.toString(lon));
                                fbdata.put(Constants.TAG_LOCATION, params);

                                Log.v("fbdata", "" + fbdata);

                                sendData(fbdata);
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
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }*/
    private void getAccessToken() {


        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_USER_DETAILS_BY_ID,
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

                                //addDeviceId();
                                //setOnlineStatus();

                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }

                                LocationActivity.this.overridePendingTransition(R.anim.fade_in,
                                        R.anim.fade_out);
                            } else {
                                Toast.makeText(LocationActivity.this, results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LocationActivity.this, "Network Error. Please login", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                Long timeStamp = System.currentTimeMillis() / 1000L;
                String hashString = pref.getString(Constants.TAG_TOKEN, "").trim() + timeStamp.toString().trim();
                hashString = new String(android.util.Base64.encode(hashString.trim().getBytes(), android.util.Base64.DEFAULT));
                map.put(Constants.TAG_REGISTERED_ID, pref.getString(Constants.TAG_USERID, ""));
//                map.put(Constants.TAG_HASH, hashString.trim());
//                map.put(Constants.TAG_TIMESTAMP, "" + timeStamp);
                Log.v(TAG, "getAccessTokenParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, "");
    }
    private void sendData(final HashMap<String, String> datas) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "sendDataRes=" + res);
                            JSONObject results = new JSONObject(res);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                                GetSet.setLocation(fbdata.get(Constants.TAG_LOCATION));

                                editor.putString(Constants.TAG_LAT, fbdata.get(Constants.TAG_LAT));
                                editor.putString(Constants.TAG_LON, fbdata.get(Constants.TAG_LON));
                                editor.putString(Constants.TAG_LOCATION, fbdata.get(Constants.TAG_LOCATION));

                                editor.commit();

                                dialog.dismiss();

                                if (isFromMyProfile) {
                                    finish();
                                    ProfileActivity.activity.finish();
                                    Intent e = new Intent(LocationActivity.this, ProfileActivity.class);
                                    e.putExtra("from", "myprofile");
                                    e.putExtra("strFriendID", GetSet.getUserId());
                                    e.putExtra("sendMatch", "");
                                    startActivity(e);
                                } else if (isFromFindPeople) {
                                    LoginActivity.lat = Double.parseDouble(fbdata.get(Constants.TAG_LAT));
                                    LoginActivity.longit = Double.parseDouble(fbdata.get(Constants.TAG_LON));
                                    LoginActivity.loc = fbdata.get(Constants.TAG_LOCATION);
                                    GetSet.setLatitude(LoginActivity.lat);
                                    GetSet.setLongitude(LoginActivity.longit);
                                    GetSet.setLocation(LoginActivity.loc);
                                    finish();
                                } else {
                                    LoginActivity.lat = Double.parseDouble(fbdata.get(Constants.TAG_LAT));
                                    LoginActivity.longit = Double.parseDouble(fbdata.get(Constants.TAG_LON));
                                    LoginActivity.loc = fbdata.get(Constants.TAG_LOCATION);
                                    GetSet.setLatitude(LoginActivity.lat);
                                    GetSet.setLongitude(LoginActivity.longit);
                                    GetSet.setLocation(LoginActivity.loc);
                                    finish();
                                    Intent i = new Intent(LocationActivity.this, MainScreenActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }
                                overridePendingTransition(R.anim.fade_in,
                                        R.anim.fade_out);
                            } else if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                dialog.dismiss();
                                CommonFunctions.dialog(LocationActivity.this, "Error", results.getString("message"));
                            } else {
                                dialog.dismiss();
                                Toast.makeText(LocationActivity.this, results.getString("message"), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_LAT, datas.get("lat"));
                map.put(Constants.TAG_LON, datas.get("lon"));
                map.put(Constants.TAG_LOCATION, datas.get("location"));
                Log.v(TAG, "sendDataParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /*OnClick Event*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.join:
                if (location.getText().toString().length() == 0) {
                    Toast.makeText(LocationActivity.this, getString(R.string.please_enter_your_location), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
//                    getLocationFromString(location.getText().toString());
                    getGeocodeLocation(location.getText().toString());
                }
                break;
        }
    }

    private void getGeocodeLocation(String params) {
        Geocoder gc = new Geocoder(getApplicationContext());
        if (Geocoder.isPresent()) {
            List<Address> list = new ArrayList<>();
            try {
                list = gc.getFromLocationName(params, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (list.size() > 0) {
                Address address = list.get(0);
                double lat = address.getLatitude();
                double lon = address.getLongitude();
                fbdata.put(Constants.TAG_LAT, Double.toString(lat));
                fbdata.put(Constants.TAG_LON, Double.toString(lon));
                fbdata.put(Constants.TAG_LOCATION, params);
            }

            Log.v("fbdata", "" + fbdata);

            sendData(fbdata);
        } else {
            Toast.makeText(getApplicationContext(), "No Location found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFromMyProfile || isFromFindPeople) {
            finish();
        } else {
            if (location.getText().toString().length() == 0) {
                Toast.makeText(LocationActivity.this, getString(R.string.please_enter_your_location), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
