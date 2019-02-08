package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.ExpandableHeightGridView;
import com.hitasoft.app.customclass.ListAsGridBaseAdapter;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 8/6/15.
 */
public class Settings extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "Settings";

    TextView logout, nullText, title, deleteAccount, save,changePassword;
    View switchView;
    ImageView backbtn;
    ProgressWheel progress;
    LinearLayout main, adsLay;
    Display display;
    SwitchCompat ageSwitch, distanceSwitch, invisibleSwitch, adsSwitch, messageSwitch, likesSwitch;

    int gridWidth;
    String peopleForId = "";
    PeopleAdapter peopleAdapter;
    ExpandableHeightGridView peopleGrid;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> peopleFor = new ArrayList<HashMap<String, String>>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        logout =  findViewById(R.id.logout);
        progress = findViewById(R.id.progress);
        main =  findViewById(R.id.main);
        nullText =  findViewById(R.id.nullText);
        title =  findViewById(R.id.title);
        backbtn = findViewById(R.id.backbtn);
        peopleGrid = findViewById(R.id.people_grid);
        ageSwitch =  findViewById(R.id.age_switch);
        distanceSwitch =  findViewById(R.id.distance_switch);
        invisibleSwitch =  findViewById(R.id.invisible_switch);
        adsSwitch =  findViewById(R.id.ads_switch);
        messageSwitch =  findViewById(R.id.message_switch);
        likesSwitch =  findViewById(R.id.likes_switch);
        deleteAccount =  findViewById(R.id.delete_account);
        save =  findViewById(R.id.save);
        switchView = findViewById(R.id.switch_view);
        adsLay =  findViewById(R.id.adsLay);
        changePassword =  findViewById(R.id.changePassword);

        title.setText(getString(R.string.setting));
        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        peopleGrid.setExpanded(true);

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        gridWidth = width * 47 / 100;

        backbtn.setOnClickListener(this);
        logout.setOnClickListener(this);
        save.setOnClickListener(this);
        switchView.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        changePassword.setOnClickListener(this);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        if (!GetSet.isPremium()) {
            ageSwitch.setClickable(false);
            invisibleSwitch.setClickable(false);
            distanceSwitch.setClickable(false);
            adsSwitch.setClickable(false);
        } else {
            switchView.setVisibility(View.GONE);
        }

        getSettings();

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

    private void setSettings() {
        peopleForId = profileMap.get(Constants.TAG_PEOPLE_FOR);
        peopleAdapter.notifyDataSetChanged();

        if (profileMap.get(Constants.TAG_ADMIN_ENABLE_ADS).equals("true")) {
            adsLay.setVisibility(View.VISIBLE);
        } else {
            adsLay.setVisibility(View.GONE);
        }

        if (profileMap.get(Constants.TAG_SHOW_AGE).equals("true")) {
            ageSwitch.setChecked(true);
        } else {
            ageSwitch.setChecked(false);
        }

        if (profileMap.get(Constants.TAG_SHOW_LOCATION).equals("true")) {
            distanceSwitch.setChecked(true);
        } else {
            distanceSwitch.setChecked(false);
        }

        if (profileMap.get(Constants.TAG_INVISIBLE).equals("true")) {
            invisibleSwitch.setChecked(true);
        } else {
            invisibleSwitch.setChecked(false);
        }

        if (profileMap.get(Constants.TAG_MESSAGE_NOTIFICATION).equals("true")) {
            messageSwitch.setChecked(true);
        } else {
            messageSwitch.setChecked(false);
        }

        if (profileMap.get(Constants.TAG_LIKE_NOTIFICATION).equals("true")) {
            likesSwitch.setChecked(true);
        } else {
            likesSwitch.setChecked(false);
        }

        if (profileMap.get(Constants.TAG_HIDE_ADS).equals("true")) {
            adsSwitch.setChecked(true);
        } else {
            adsSwitch.setChecked(false);
        }
    }

    private void dialog(final String from) {
        final Dialog dialog = new Dialog(Settings.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title =  dialog.findViewById(R.id.headerTxt);
        TextView subTxt =  dialog.findViewById(R.id.subTxt);
        TextView yes =  dialog.findViewById(R.id.yes);
        TextView no =  dialog.findViewById(R.id.no);

        title.setVisibility(View.GONE);

        if (from.equals("delete")) {
            subTxt.setText(getString(R.string.really_deactivate_account));
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from.equals("delete")) {
                    deleteAccount();
                }
                removeToken();
                dialog.dismiss();
                Constants.editor.clear();
                Constants.editor.commit();
                GetSet.reset();
                finishAffinity();
                Intent i = new Intent(Settings.this, LoginActivity.class);
                startActivity(i);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public class PeopleAdapter extends ListAsGridBaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        ViewHolder holder = null;
        private Context mContext;

        public PeopleAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            super(ctx);
            mContext = ctx;
            Items = data;
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
            return Items.size();
        }

        @Override
        protected View getItemView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.filter_people_item, parent, false);//layout
                holder = new ViewHolder();

                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.icon_bg = (ImageView) convertView.findViewById(R.id.icon_bg);
                holder.name =  convertView.findViewById(R.id.name);
                holder.detail_lay = (RelativeLayout) convertView.findViewById(R.id.detail_lay);

                holder.detail_lay.setLayoutParams(new LinearLayout.LayoutParams(gridWidth, gridWidth));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
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
     * */

    private void deleteAccount() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELETE_ACCOUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "deleteAccountRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Settings.this, "Error", json.getString("message"));
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
                Log.v(TAG, "deleteAccountParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void removeToken() {
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_SIGNOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "removeTokenRes=" + res);
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
                map.put(Constants.TAG_DEVICE_ID, deviceId);
                Log.v(TAG, "removeTokenParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void getSettings() {
        main.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        progress.spin();
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

                                getProfile();

                                if (peopleFor.size() % 2 != 0) {
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    peopleFor.add(map);
                                }

                                peopleAdapter = new PeopleAdapter(Settings.this, peopleFor);
                                peopleGrid.setAdapter(peopleAdapter);

                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Settings.this, "Error", response.getString("message"));
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
                Log.v(TAG,"getSettingsParams="+map);
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
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                JSONObject values = json.getJSONObject(Constants.TAG_RESULT);
                                profileMap.put(Constants.TAG_PEOPLE_FOR, DefensiveClass.optString(values, Constants.TAG_PEOPLE_FOR));
                                profileMap.put(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
                                profileMap.put(Constants.TAG_SHOW_LOCATION, DefensiveClass.optString(values, Constants.TAG_SHOW_LOCATION));
                                profileMap.put(Constants.TAG_INVISIBLE, DefensiveClass.optString(values, Constants.TAG_INVISIBLE));
                                profileMap.put(Constants.TAG_MESSAGE_NOTIFICATION, DefensiveClass.optString(values, Constants.TAG_MESSAGE_NOTIFICATION));
                                profileMap.put(Constants.TAG_LIKE_NOTIFICATION, DefensiveClass.optString(values, Constants.TAG_LIKE_NOTIFICATION));
                                profileMap.put(Constants.TAG_HIDE_ADS, DefensiveClass.optString(values, Constants.TAG_HIDE_ADS));
                                profileMap.put(Constants.TAG_ADMIN_ENABLE_ADS, DefensiveClass.optString(values, Constants.TAG_ADMIN_ENABLE_ADS));

                                setSettings();

                                main.setVisibility(View.VISIBLE);
                                progress.setVisibility(View.GONE);
                                progress.stopSpinning();

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Settings.this, "Error", json.getString("message"));
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
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "getProfileParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void saveSettings() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG,"saveSettingsRes="+res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setHideAds(adsSwitch.isChecked());
                                editor.putBoolean(Constants.TAG_HIDE_ADS, adsSwitch.isChecked());

                                GetSet.setLikeNotification(likesSwitch.isChecked());
                                editor.putBoolean(Constants.TAG_LIKE_NOTIFICATION, likesSwitch.isChecked());

                                GetSet.setMsgNotification(messageSwitch.isChecked());
                                editor.putBoolean(Constants.TAG_MESSAGE_NOTIFICATION, messageSwitch.isChecked());
                                editor.commit();
                                finish();
                                Intent p = new Intent(Settings.this, MainScreenActivity.class);
                                p.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(p);
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(Settings.this, "Error", json.getString("message"));
                                save.setClickable(true);
                                save.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            save.setClickable(true);
                            save.setEnabled(true);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            save.setClickable(true);
                            save.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            save.setClickable(true);
                            save.setEnabled(true);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                save.setClickable(true);
                save.setEnabled(true);
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
                map.put(Constants.TAG_PEOPLE_FOR, peopleForId);
                map.put(Constants.TAG_SHOW_AGE, String.valueOf(ageSwitch.isChecked()));
                map.put(Constants.TAG_SHOW_LOCATION, String.valueOf(distanceSwitch.isChecked()));
                map.put(Constants.TAG_INVISIBLE, String.valueOf(invisibleSwitch.isChecked()));
                map.put(Constants.TAG_HIDE_ADS, String.valueOf(adsSwitch.isChecked()));
                map.put(Constants.TAG_MESSAGE_NOTIFICATION, String.valueOf(messageSwitch.isChecked()));
                map.put(Constants.TAG_LIKE_NOTIFICATION, String.valueOf(likesSwitch.isChecked()));
                Log.v(TAG,"saveSettingsParams="+map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /*OnClick Event*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                dialog("logout");
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.save:
                save.setClickable(false);
                save.setEnabled(false);
                saveSettings();
                break;
            case R.id.switch_view:
                if (!GetSet.isPremium()) {
                    Intent m = new Intent(Settings.this, PremiumDialog.class);
                    startActivity(m);
                }
                break;
            case R.id.delete_account:
                dialog("delete");
                break;
            case R.id.changePassword:
                Intent pass= new Intent(getApplicationContext(),ChangePasswordActivity.class);
                startActivity(pass);
                break;
        }
    }
}
