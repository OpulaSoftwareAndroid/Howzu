package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.ExpandableHeightGridView;
import com.hitasoft.app.customclass.ListAsGridBaseAdapter;
import com.hitasoft.app.customclass.PlacesAutoCompleteAdapter;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.customclass.RangeSeekBar;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

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
 * Created by hitasoft on 8/6/15.
 */
public class Filter extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "Filter";

    LinearLayout ageLay, main, locationLay, ageSeekLay, distanceSeekLay;
    TextView ageTxt, nullText, title, save, distance;
    RangeSeekBar<Integer> seekBar;
    SeekBar distanceBar;
    ProgressWheel progress;
    ImageView backbtn, guyBg, guy, girlBg, girl;
    Display display;
    ExpandableHeightGridView peopleGrid;
    AutoCompleteTextView location;

    public static String loc = "";
    int gridWidth;
    String peopleForId = "", selectedAge = "", selectedDistance = "", selectedGender = "";
    int maxAge = 55, maxDistance = 100;
    PeopleAdapter peopleAdapter;
    HashMap<String, String> fbdata = new HashMap<String, String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ArrayList<HashMap<String, String>> peopleFor = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);
        ageLay = findViewById(R.id.age_lay);
        ageTxt = findViewById(R.id.age_txt);
        progress = findViewById(R.id.progress);
        main = findViewById(R.id.main);
        nullText = findViewById(R.id.nullText);
        title = findViewById(R.id.title);
        save = findViewById(R.id.save);
        backbtn = findViewById(R.id.backbtn);
        guyBg = findViewById(R.id.guy_bg);
        guy = findViewById(R.id.guy);
        girlBg = findViewById(R.id.girl_bg);
        girl = findViewById(R.id.girl);
        peopleGrid = findViewById(R.id.people_grid);
        location = findViewById(R.id.location);
        locationLay = findViewById(R.id.locationLay);
        distanceBar = findViewById(R.id.distance_bar);
        distance = findViewById(R.id.distance);
        ageSeekLay = findViewById(R.id.ageSeekLay);
        distanceSeekLay = findViewById(R.id.distanceSeekLay);
        title.setText(getString(R.string.filter));
        save.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        peopleGrid.setExpanded(true);

        backbtn.setOnClickListener(this);
        guyBg.setOnClickListener(this);
        girlBg.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        save.setOnClickListener(this);
        location.setOnClickListener(this);

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        location.setAdapter(new PlacesAutoCompleteAdapter(Filter.this, R.layout.dropdown_layout));

        display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        gridWidth = width * 47 / 100;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        if (!GetSet.isPremium()) {
            location.setKeyListener(null);
        }


        setAgeRange();
        setDistanceRange();

//        getSettings();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        super.onPause();
    }

    private void setAgeRange() {
        seekBar = new RangeSeekBar<Integer>(18, maxAge, Filter.this);
        seekBar.setSelectedMinValue(18);
        seekBar.setSelectedMaxValue(maxAge);
        //seekBar.setDefaultColor(getResources().getColor(R.color.colorPrimary));
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                Log.i(TAG, "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
                ageTxt.setText(minValue + " - " + maxValue);
            }
        });

        ageSeekLay.addView(seekBar);
    }

    private void setDistanceRange() {
        seekBar = new RangeSeekBar<Integer>(18, 100, Filter.this);
        seekBar.setSelectedMinValue(18);
        seekBar.setSelectedMaxValue(100);
        //seekBar.setDefaultColor(getResources().getColor(R.color.colorPrimary));
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                Log.i(TAG, "User selected new distance values: MIN=" + minValue + ", MAX=" + maxValue);
                distance.setText(minValue + " - " + maxValue + "Km.");
            }
        });

        distanceSeekLay.addView(seekBar);
    }

    public class PeopleAdapter extends ListAsGridBaseAdapter {
        ArrayList<HashMap<String, String>> filterList;
        ViewHolder holder = null;
        Context mContext;

        public PeopleAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            super(ctx);
            mContext = ctx;
            filterList = data;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        @Override
        protected View getItemView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.filter_people_item, parent, false);//layout
                holder = new ViewHolder();

                holder.icon = convertView.findViewById(R.id.icon);
                holder.icon_bg = convertView.findViewById(R.id.icon_bg);
                holder.name = convertView.findViewById(R.id.name);
                holder.detail_lay = (RelativeLayout) convertView.findViewById(R.id.detail_lay);

                holder.detail_lay.setLayoutParams(new LinearLayout.LayoutParams(gridWidth, gridWidth));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                final HashMap<String, String> map = filterList.get(position);

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
                        peopleAdapter.notifyDataSetChanged();
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView icon, icon_bg;
            TextView name;
            RelativeLayout detail_lay;
        }

    }

    /**
     * API Implementation
     */

    private void getSettings() {
        main.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        progress.spin();
        save.setOnClickListener(null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            save.setOnClickListener(Filter.this);
                            Log.v(TAG, "getSettingsRes=" + res);
                            JSONObject response = new JSONObject(res);
                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                                getProfile();

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

                                if (peopleFor.size() % 2 != 0) {
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    peopleFor.add(map);
                                }

                                peopleAdapter = new PeopleAdapter(Filter.this, peopleFor);
                                peopleGrid.setAdapter(peopleAdapter);

                                try {
                                    maxAge = Integer.valueOf(DefensiveClass.optInt(result, Constants.TAG_MAX_AGE));
                                    maxDistance = Integer.valueOf(DefensiveClass.optInt(result, Constants.TAG_MAX_DISTANCE));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

                                setAgeRange();
                                setDistanceRange();
                                distanceBar.setMax(maxDistance);

                                ageSeekLay.addView(seekBar);

                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Filter.this, "Error", response.getString("message"));
                            } else {
                                nullText.setVisibility(View.VISIBLE);
                                main.setVisibility(View.INVISIBLE);
                                progress.setVisibility(View.GONE);
                                progress.stopSpinning();
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
                save.setOnClickListener(Filter.this);
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
                Log.v(TAG, "getSettingsParams=" + map);
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
                        save.setOnClickListener(Filter.this);
                        try {
                            Log.v(TAG, "getProfileRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                JSONObject values = json.getJSONObject(Constants.TAG_RESULT);
                                loc = DefensiveClass.optString(values, Constants.TAG_LOCATION);
                                String filter = DefensiveClass.optString(values, Constants.TAG_FILTER);

                                location.setText(loc);

                                if (!filter.equals(null) && !filter.equals("")) {
                                    JSONObject temp = new JSONObject(filter);
                                    String age = DefensiveClass.optString(temp, Constants.TAG_AGE);

                                    if (DefensiveClass.optString(temp, Constants.TAG_GENDER).equals("both")) {
                                        girlBg.setSelected(true);
                                        girl.setSelected(true);
                                        guyBg.setSelected(true);
                                        guy.setSelected(true);
                                    } else if (DefensiveClass.optString(temp, Constants.TAG_GENDER).equals("men")) {
                                        girlBg.setSelected(false);
                                        girl.setSelected(false);
                                        guyBg.setSelected(true);
                                        guy.setSelected(true);
                                    } else if (DefensiveClass.optString(temp, Constants.TAG_GENDER).equals("women")) {
                                        girlBg.setSelected(true);
                                        girl.setSelected(true);
                                        guyBg.setSelected(false);
                                        guy.setSelected(false);
                                    } else {
                                        girlBg.setSelected(false);
                                        girl.setSelected(false);
                                        guyBg.setSelected(false);
                                        guy.setSelected(false);
                                    }

                                    peopleForId = DefensiveClass.optString(temp, Constants.TAG_PEOPLE_FOR);
                                    Log.v(TAG, "peopleForId=" + peopleForId);
                                    peopleAdapter.notifyDataSetChanged();
                                    try {
                                        if (age.contains("-")) {
                                            age = age.replaceAll(" ", "");
                                            String[] ages = age.split("-");
                                            int selectedMaxAge = Integer.parseInt(ages[1]);
                                            if (selectedMaxAge > maxAge) {
                                                selectedMaxAge = maxAge;
                                                Log.v(TAG, "selectedMaxAge=" + selectedMaxAge);
                                            }
                                            seekBar.setSelectedMinValue(Integer.parseInt(ages[0]));
                                            seekBar.setSelectedMaxValue(selectedMaxAge);
                                            ageTxt.setText(ages[0] + " - " + selectedMaxAge);
                                        } else {
                                            ageTxt.setText("18 - " + maxAge);
                                        }
                                        distanceBar.setProgress(Integer.parseInt(DefensiveClass.optString(temp, Constants.TAG_DISTANCE)));
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }

                                main.setVisibility(View.VISIBLE);
                                progress.setVisibility(View.GONE);
                                progress.stopSpinning();
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Filter.this, "Error", json.getString("message"));
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
                save.setOnClickListener(Filter.this);
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

    private void setFilter() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_FILTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        save.setOnClickListener(Filter.this);
                        try {
                            Log.v(TAG, "setFilterRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setLocation(location.getText().toString());
                                finish();
                                MainScreenActivity.resumeHome = true;
                                Intent i = new Intent(Filter.this, MainScreenActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Filter.this, "Error", json.getString("message"));
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
                save.setOnClickListener(Filter.this);
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
                map.put(Constants.TAG_DISTANCE, selectedDistance);
                map.put(Constants.TAG_AGE, selectedAge);
                map.put(Constants.TAG_GENDER, selectedGender);
                map.put(Constants.TAG_PEOPLE_FOR, peopleForId);
                Log.v(TAG, "setFilterParams=" + map);
                return map;
            }
        };

        //HowzuApplication.getInstance().addToRequestQueue(req, TAG);
        RequestQueue queue = HowzuApplication.getInstance().getRequestQueue();
        queue.add(req);
        queue.start();
    }

    private void getLocationFromString(final String params) {
        String url = "";
        try {
            url = "http://maps.google.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(params, "UTF-8") + "&ka&sensor=false";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject jsonObject = new JSONObject(res);

                            double lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");

                            double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");
                            Log.v(TAG, " lat = " + lat + " &lon = " + lon);

                            fbdata.put(Constants.TAG_LAT, Double.toString(lat));
                            fbdata.put(Constants.TAG_LON, Double.toString(lon));
                            fbdata.put(Constants.TAG_LOCATION, params);

                            sendData(fbdata);

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

                            } else if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Filter.this, "Error", results.getString(Constants.TAG_MESSAGE));
                            } else {
                                Toast.makeText(Filter.this, results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_LAT, datas.get(Constants.TAG_LAT));
                map.put(Constants.TAG_LON, datas.get(Constants.TAG_LON));
                map.put(Constants.TAG_LOCATION, datas.get(Constants.TAG_LOCATION));

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
            case R.id.backbtn:
                if (HomeFragment.peopleAdapter != null && HomeFragment.peoplesAry != null) {
                    HomeFragment.peoplesAry.clear();
                    HomeFragment.peopleAdapter.notifyDataSetChanged();
                }
                finish();
                break;
            case R.id.guy_bg:
                if (guyBg.isSelected()) {
                    guyBg.setSelected(false);
                    guy.setSelected(false);
                } else {
                    guyBg.setSelected(true);
                    guy.setSelected(true);
                }
                break;
            case R.id.girl_bg:
                if (girlBg.isSelected()) {
                    girlBg.setSelected(false);
                    girl.setSelected(false);
                } else {
                    girlBg.setSelected(true);
                    girl.setSelected(true);
                }
                break;
            case R.id.locationLay:
            case R.id.location:
                if (!GetSet.isPremium()) {
                    Intent m = new Intent(Filter.this, PremiumDialog.class);
                    startActivity(m);
                }
                break;
            case R.id.save:
                save.setOnClickListener(null);
                if (!location.getText().toString().trim().equals(loc)) {
//                    getLocationFromString(location.getText().toString());
                    getGeocodeLocation(location.getText().toString());
                }
                selectedAge = seekBar.getSelectedMinValue() + "-" + seekBar.getSelectedMaxValue();
                selectedDistance = String.valueOf(distanceBar.getProgress());
                if (girlBg.isSelected() && guyBg.isSelected()) {
                    selectedGender = "both";
                } else if (guyBg.isSelected()) {
                    selectedGender = "men";
                } else if (girlBg.isSelected()) {
                    selectedGender = "women";
                } else {
                    selectedGender = "";
                }
                setFilter();
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
}
