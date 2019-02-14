package com.hitasoft.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.howzu.ChatActivity;
import com.hitasoft.app.howzu.HowzuApplication;
import com.hitasoft.app.howzu.LoginActivity;
import com.hitasoft.app.howzu.MainScreenActivity;
import com.hitasoft.app.howzu.R;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 26/5/15.
 */
public class CommonFunctions {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final String TAG = CommonFunctions.class.getSimpleName();

    public static Dialog dialog;
    public static boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static void showProgressDialog(Context context) {
        try {
            if (context != null) {

                if (dialog != null && dialog.isShowing()) {

                } else {
                    dialog = new Dialog(context);
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    dialog.setCancelable(false);

                    dialog.setContentView(R.layout.dialog_progress);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = dialog.getWindow();

                    lp.copyFrom(window.getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setAttributes(lp);

                    RotateLoading pDialog = (RotateLoading) dialog.findViewById(R.id.progressBar_Dialog);
                    pDialog.start();

                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void hideProgressDialog(Context context) {
        // progressDialog.dismiss();
        try {
            if (context != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showProgressDialog2(Context context) {
        try {
            if (context != null) {

                if (dialog != null && dialog.isShowing()) {

                } else {
                    dialog = new Dialog(context);
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(false);

                    dialog.setContentView(R.layout.dialog_progress);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = dialog.getWindow();

                    lp.copyFrom(window.getAttributes());
                    lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setAttributes(lp);

                    RotateLoading pDialog = (RotateLoading) dialog.findViewById(R.id.progressBar_Dialog);
                    pDialog.start();

                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void hideProgressDialog2(Context context) {
        // progressDialog.dismiss();
        try {
            if (context != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadJSONFromAsset(Context context, String name) {
        String json = null;
        try {

            InputStream is = context.getResources().getAssets().open(name);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = getCurrentTime(ctx);
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    private static long getCurrentTime(Context ctx) {
        long dtMili = System.currentTimeMillis();
        return dtMili;
    }

    public static String getImageName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    public static String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static void disabledialog(final Context ctx, final String title, final String content) {
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Dialog dialog = new Dialog(ctx);
                    Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.logout_dialog);
                    dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    TextView titleTxt =  dialog.findViewById(R.id.headerTxt);
                    TextView subTxt =  dialog.findViewById(R.id.subTxt);
                    TextView yes =  dialog.findViewById(R.id.yes);
                    TextView no =  dialog.findViewById(R.id.no);

                    no.setVisibility(View.VISIBLE);
                    yes.setVisibility(View.INVISIBLE);

                    no.setText(ctx.getString(R.string.ok));
                    subTxt.setText(content);

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (title.equalsIgnoreCase("Blocked User")) {
                                    MainScreenActivity.resumeMessage = true;
                                    Intent i = new Intent(ctx, MainScreenActivity.class);
                                    ctx.startActivity(i);
                                    ((Activity) ctx).finish();
                                } else {
                                    removeToken(ctx);
                                    dialog.dismiss();
                                    Constants.editor.clear();
                                    Constants.editor.commit();
                                    GetSet.reset();
                                    Intent i = new Intent(ctx, LoginActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ctx.startActivity(i);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void removeToken(Context context) {
        final String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        final SharedPreferences pref = context.getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_SIGNOUT,
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
                VolleyLog.d("Remove", "Error: " + error.getMessage());
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
                map.put(Constants.TAG_DEVICE_ID, deviceId);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                Log.v("parameters", "parameters=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, "Remove");
    }

    public static void dialog(final Context ctx, final String title, final String content) {
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Dialog dialog = new Dialog(ctx);
                    Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.logout_dialog);
                    dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(false);

                    TextView titleTxt =  dialog.findViewById(R.id.headerTxt);
                    TextView subTxt =  dialog.findViewById(R.id.subTxt);
                    TextView yes =  dialog.findViewById(R.id.yes);
                    TextView no =  dialog.findViewById(R.id.no);


                    yes.setVisibility(View.GONE);
                    no.setText("Ok");
                    subTxt.setText(content);

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static int dpToPx(Context context, float dp) {
// Took from http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    public static void setupUI(Context context, View view) {
        final Activity act = (Activity) context;
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(act);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(act, innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context
                    .getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException npe) {

        } catch (Exception e) {

        }
    }
}
