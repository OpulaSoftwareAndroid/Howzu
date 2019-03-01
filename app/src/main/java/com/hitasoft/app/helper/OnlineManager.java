package com.hitasoft.app.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.howzu.HowzuApplication;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 4/3/17.
 */

public class OnlineManager extends BroadcastReceiver {

    private String TAG = "MainScreenActivity";
    private String appVisibility = "0";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("onReceive", "onReceive");
        //Toast.makeText(context, "Online Manager", Toast.LENGTH_LONG).show();

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.v("BOOT_COMPLETED", "BOOT_COMPLETED");
            setAlarm(context);
        }

        pref = context.getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        if (GetSet.isLogged() && NetworkReceiver.isConnected()) {
            if (HowzuApplication.isActivityVisible()) {
                appVisibility = "1";
            } else {
                appVisibility = "2";
            }
            setOnlineStatus();

            //SignallingClient.getInstance();
        }
    }

    public void setAlarm(Context context) {
        Log.v("setAlarm", "setAlarm");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, OnlineManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //After 15 minutes
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 15 * 60 * 1000, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, OnlineManager.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void setOnlineStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v("res", "res=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

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

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUseridLikeToken());
                map.put("status", appVisibility);
                map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v("map", "map=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }
}
