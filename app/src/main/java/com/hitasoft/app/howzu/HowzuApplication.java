package com.hitasoft.app.howzu;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
//import com.baidu.mapapi.SDKInitializer;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.hitasoft.app.customclass.FontsOverride;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.OnlineManager;
import com.hitasoft.app.helper.PhoneStateReceiver;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by hitasoft on 29/6/15.
 */

@AcraCore(buildConfigClass = BuildConfig.class)
public class HowzuApplication extends Application {

    public static final String TAG = HowzuApplication.class
            .getSimpleName();
    private static HowzuApplication mInstance;
    private static boolean activityVisible;
    private static Snackbar snackbar = null;
    OnlineManager onlineManager;
    private RequestQueue mRequestQueue;

    /*Socket Declaration*/

    private Socket mSocket;

    {
        try {

            mSocket = IO.socket(Constants.SOCKET_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    /*other Functions Implementation*/

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static synchronized HowzuApplication getInstance() {
        return mInstance;
    }

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(view, context.getString(R.string.network_failure), Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView =  sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
        }

        if (!isConnected && !snackbar.isShown()) {
            snackbar.show();
        } else {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * To convert the given pixel value to dp
     **/
    public static float pxToDp(Context context, float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
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

    /*public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }*/

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED || info[i].getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static String getLoc(Context context, double latitude, double longitude) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        String location = null;
        try {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                //for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
                location = addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + "," + addresses.get(0).getCountryName();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        GetSet.setLocation(location);
        return location;
    }

    @Override
    public void onCreate() {
        super.onCreate();
     //   MultiDex.install(this);
        mInstance = this;
// Baidu maps
//        SDKInitializer.initialize(this);
        Log.v("Application oncreate", "Application oncreate");

        // init facebook //
        FacebookSdk.setApplicationId(Constants.App_ID);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
        FontsOverride.setDefaultFont(this, "MONOSPACE", "font_regular.ttf");

        Constants.pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        if (Constants.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(Constants.pref.getString("user_id", null));
            GetSet.setUserName(Constants.pref.getString("user_name", null));
            GetSet.setImageUrl(Constants.pref.getString("user_image", null));
            GetSet.setLocation(Constants.pref.getString("location", null));
            Log.d("glocationchk", "From Appln-->" + Constants.pref.getString("location", null));
            if (Constants.pref.getBoolean(Constants.TAG_PREMIUM_MEMBER, false)) {
                GetSet.setPremium(true);
            } else {
                GetSet.setPremium(false);
            }
            if (Constants.pref.getBoolean(Constants.TAG_ADMIN_ENABLE_ADS, false)) {
                GetSet.setBannerEnable(true);
                GetSet.setAdUnitId(Constants.pref.getString(Constants.TAG_AD_UNIT_ID, null));
            } else {
                GetSet.setBannerEnable(false);
            }
            if (Constants.pref.getBoolean("hide_ads", false)) {
                GetSet.setHideAds(true);
            } else {
                GetSet.setHideAds(false);
            }
            if (Constants.pref.getBoolean("like_notification", false)) {
                GetSet.setLikeNotification(true);
            } else {
                GetSet.setLikeNotification(false);
            }
            if (Constants.pref.getBoolean("message_notification", false)) {
                GetSet.setMsgNotification(true);
            } else {
                GetSet.setMsgNotification(false);
            }
        }

        onlineManager = new OnlineManager();
        if (GetSet.isLogged()) {
            onlineManager.setAlarm(getApplicationContext());
        }
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    public void setPhoneStateListener(PhoneStateReceiver.PhoneState listener) {
        PhoneStateReceiver.phoneState = listener;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        ACRA.init(this);
        MultiDex.install(this);
    }
}
