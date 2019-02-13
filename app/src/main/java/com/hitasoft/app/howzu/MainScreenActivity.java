package com.hitasoft.app.howzu;// Created by Hitasoft on 2/17/2015.

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;
//import com.baidu.location.LocationClient;
//import com.baidu.location.LocationClientOption;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.hitasoft.app.customclass.BadgeView;
import com.hitasoft.app.customclass.CircleTransform;
import com.hitasoft.app.customclass.RateThisApp;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SharedPrefManager;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainScreenActivity extends AppCompatActivity implements View.OnClickListener
        , NetworkReceiver.ConnectivityReceiverListener
//    , BDLocationListener
{
    String TAG = "MainScreenActivity";

    public static ImageView userImage, menubtn, filterbtn, logo, searchbtn, setting, batch, visitorsCount, requestCount,
            messageCount,friendCount;
    public static TextView title, spinText, profileName, viewProfile;
    public static RelativeLayout spinLay, bannerLay, searchLay, profileLay, emptyLay;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    Dialog dialog;
    LinearLayout peopleNear, findPeople, visitors, message, friends, request, liked, menuItemMatchMaker,linearLayoutMenuItemNotification;
    BadgeView chatBadge, visitBadge, requestBadge,friendsBadge;
    AdView mAdView;
    Fragment mContent;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    public static boolean resumeHome = false, resumeMessage = false;
    public static String currentFragment = "";
    boolean premiumClicked = false;

    String strUserID;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Log.i(TAG, "MainScreenActivity: onCreate()");
//
//        mLocationClient = new LocationClient (this);
//        LocationClientOption Option = new LocationClientOption ();
//        Option.setIsNeedAddress (true);
//        Option.setAddrType ("all");
//        mLocationClient.setLocOption (Option);
//        mLocationClient.registerLocationListener (this);
//        mLocationClient.start ();
        setUpNavigationDrawer();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();
        Constants.pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);
        pref.getString(Constants.TAG_USERID,null);
        strUserID=pref.getString(Constants.TAG_USERID,null);
        dialog = new Dialog(this);
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
       // CommonFunctions.showProgressDialog(MainScreenActivity.this);
//        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {
//            CommonFunctions.showProgressDialog(this);
//        }
            GetSet.setUserId(strUserID);
        if(strUserID!=null)
        {
            getUserDetailByID();
        }
//        getUserDetailByID();

        System.out.println("jigar the main screen user id is "+strUserID);

        if (pref.getBoolean(Constants.ISLOGGED, false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString(Constants.TAG_USERID, null));
            GetSet.setUserName(pref.getString(Constants.TAG_USERNAME, null));
            Log.i(TAG, "MainScreenActivity the user id is: "+GetSet.getUserId());
            GetSet.setImageUrl(pref.getString(Constants.TAG_USERIMAGE, null));
            //GetSet.setLocation(pref.getString("location", null));
            if (Constants.pref.getBoolean(Constants.TAG_PREMIUM_MEMBER, false)) {
                GetSet.setPremium(true);
            } else {
                GetSet.setPremium(false);
            }
            if (pref.getBoolean(Constants.TAG_ADMIN_ENABLE_ADS, false)) {
                GetSet.setBannerEnable(true);
                GetSet.setAdUnitId(pref.getString(Constants.TAG_AD_UNIT_ID, null));
            } else {
                GetSet.setBannerEnable(false);
            }
            if (pref.getBoolean(Constants.TAG_HIDE_ADS, false)) {
                GetSet.setHideAds(true);
            } else {
                GetSet.setHideAds(false);
            }
            if (pref.getBoolean(Constants.TAG_LIKE_NOTIFICATION, false)) {
                GetSet.setLikeNotification(true);
            } else {
                GetSet.setLikeNotification(false);
            }
            if (pref.getBoolean(Constants.TAG_MESSAGE_NOTIFICATION, false)) {
                GetSet.setMsgNotification(true);
            } else {
                GetSet.setMsgNotification(false);
            }
        }


        userImage = findViewById(R.id.profile_pic);
        profileLay = findViewById(R.id.profile_lay);
        emptyLay = findViewById(R.id.empty_lay);
        profileName = findViewById(R.id.profile_name);
        viewProfile = findViewById(R.id.view_profile);
        menubtn = findViewById(R.id.menubtn);
        peopleNear = findViewById(R.id.people_near);
        findPeople = findViewById(R.id.find_people);
        visitors = findViewById(R.id.visitors);
        message = findViewById(R.id.message);
        friends = findViewById(R.id.friends);
        request = findViewById(R.id.request);
        liked = findViewById(R.id.liked);
        menuItemMatchMaker = findViewById(R.id.menuItemMatchmakerFeature);
        filterbtn = findViewById(R.id.filterbtn);
        searchbtn = findViewById(R.id.searchbtn);
        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);
        spinText = findViewById(R.id.spinText);
        spinLay = findViewById(R.id.spinLay);
//        becomePremium = findViewById(R.id.become_premium);
        setting = findViewById(R.id.setting);
        linearLayoutMenuItemNotification = findViewById(R.id.linearLayoutMenuItemNotification);

        batch = findViewById(R.id.premium_batch);
        requestCount = findViewById(R.id.request_count);
        messageCount = findViewById(R.id.message_count);
        friendCount = findViewById(R.id.friends_count);

        visitorsCount = findViewById(R.id.visitors_count);
        bannerLay = findViewById(R.id.banner_lay);
        searchLay = findViewById(R.id.search_lay);

        //mAdView = (AdView) findViewById(R.id.adView);

        Picasso.with(this).load(GetSet.getImageUrl())
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .transform(new CircleTransform())
                .into(userImage);
        profileName.setText(GetSet.getUserName());
        String strIntentType=getIntent().getStringExtra(Constants.TAG_INTENT_FROM);
        System.out.println("jigar the intent info we have is "+strIntentType);

        if (getIntent().getStringExtra("filterLocation") != null) {
            viewProfile.setText(getIntent().getStringExtra("filterLocation"));
        } else {
            viewProfile.setText(GetSet.getLocation());
        }
        if (GetSet.isPremium()) {
            batch.setVisibility(View.VISIBLE);
//            becomePremium.setVisibility(View.GONE);
        } else {
            batch.setVisibility(View.GONE);
//            becomePremium.setVisibility(View.VISIBLE);
        }

        profileLay.setOnClickListener(this);
        emptyLay.setOnClickListener(this);
        menubtn.setOnClickListener(this);
        filterbtn.setOnClickListener(this);
        peopleNear.setOnClickListener(this);
        findPeople.setOnClickListener(this);
        visitors.setOnClickListener(this);
        message.setOnClickListener(this);
        friends.setOnClickListener(this);
        request.setOnClickListener(this);
        liked.setOnClickListener(this);
        menuItemMatchMaker.setOnClickListener(this);
//        becomePremium.setOnClickListener(this);
        setting.setOnClickListener(this);
        linearLayoutMenuItemNotification.setOnClickListener(this);

        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(
                    savedInstanceState, "mContent");

        if(getIntent().getStringExtra(Constants.TAG_INTENT_FROM)!=null)
        {
//            String strIntentType=getIntent().getStringExtra(Constants.TAG_INTENT_FROM);

            if(strIntentType.equals(Constants.TAG_INTENT_PROFILE_PAGE))
            {
                liked.performClick();
                currentFragment = "Liked";
                switchContent(new LikedFragment());
                return;
            }
        }
        if (mContent == null) {
            switchContent(new HomeFragment());
            //getUserDetailByID();

        }
//        switchContent(new DemoFragment());

        friendsBadge = new BadgeView(MainScreenActivity.this, friendCount);
        friendsBadge.setBadgePosition(BadgeView.POSITION_CENTER);
        friendsBadge.setBadgeMargin(7);
        friendsBadge.setTextSize(13);
        friendsBadge.setBackgroundDrawable(getResources().getDrawable(R.drawable.request_bg));
        friendsBadge.setGravity(Gravity.CENTER);

        chatBadge = new BadgeView(MainScreenActivity.this, messageCount);
        chatBadge.setBadgePosition(BadgeView.POSITION_CENTER);
        chatBadge.setBadgeMargin(7);
        chatBadge.setTextSize(13);
        chatBadge.setBackgroundDrawable(getResources().getDrawable(R.drawable.message_bg));
        chatBadge.setGravity(Gravity.CENTER);


        visitBadge = new BadgeView(MainScreenActivity.this, visitorsCount);
        visitBadge.setBadgePosition(BadgeView.POSITION_CENTER);
        visitBadge.setBadgeMargin(7);
        visitBadge.setTextSize(13);
        visitBadge.setBackgroundDrawable(getResources().getDrawable(R.drawable.visitor_bg));
        visitBadge.setGravity(Gravity.CENTER);

        requestBadge = new BadgeView(MainScreenActivity.this, requestCount);
        requestBadge.setBadgePosition(BadgeView.POSITION_CENTER);
        requestBadge.setBadgeMargin(7);
        requestBadge.setTextSize(13);
        requestBadge.setBackgroundDrawable(getResources().getDrawable(R.drawable.request_bg));
        requestBadge.setGravity(Gravity.CENTER);
        CommonFunctions.showProgressDialog(this);
        if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
            Log.v(TAG, "enableAds=" + GetSet.getAdUnitId());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);

            mAdView = new AdView(this);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId(GetSet.getAdUnitId());
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("08B93995F13C6F3085CA08590A0A056A")
                    .build();
            if (mAdView.getAdSize() != null || mAdView.getAdUnitId() != null) {
                mAdView.loadAd(adRequest);
                mAdView.setBackgroundColor(Color.TRANSPARENT);
            }
            // else Log state of adsize/adunit
            mAdView.setLayoutParams(params);
            bannerLay.addView(mAdView, params);
        } else {
            bannerLay.setVisibility(View.GONE);
            Log.v(TAG, "hideAds");
        }
//FOR TESTING
      //  switchContent(new FindPeople());
    //    switchContent(new HomeFragment());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            dialog();
        }
    }

    @Override
    protected void onResume() {
        premiumClicked = false;
        if (GetSet.isLogged()) {
//            getCounts();
        }
        if (resumeHome) {
            resumeHome = false;
            switchContent(new HomeFragment());
        } else if (resumeMessage) {
            resumeMessage = false;
            switchContent(new Message());
        }
        HowzuApplication.activityResumed();

        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, mDrawerLayout, true);
        } else {
            HowzuApplication.showSnack(this, mDrawerLayout, false);
        }

        if (mAdView != null) {
            mAdView.resume();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, mDrawerLayout, true);
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);

//        ImageView imageViewMatchMakerProfile=findViewById(R.id.imageViewMatchMakerProfile);
//
//        imageViewMatchMakerProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                switchContent(new MatchMakerFragment());
//                GetSet.setFriendId("96639683");
//                Bundle bundle=new Bundle();
//// STATIC UNTIL USER BY DETAIL RESPONSE DOES NOT HAVE MATCH MAKER ID
//                bundle.putString(Constants.TAG_FRIEND_ID, "96639683");  //match maker id
//                bundle.putString(Constants.TAG_REGISTERED_ID, GetSet.getUserId());  // register id
//
////                bundle.putString(Constants.TAG_FRIEND_ID, "96639683");
//                MatchMakerFragment matchMakerFragment=new MatchMakerFragment();
//                matchMakerFragment.setArguments(bundle);
//
//            }
//        });

//
//        ImageView imageViewLikeMatchMaker=findViewById(R.id.imageViewLikeMatchMaker);
//
//        imageViewLikeMatchMaker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                switchContent(new MatchMakerFragment());
//            }
//        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.v(TAG, "isConnected=" + isConnected);
        HowzuApplication.showSnack(this, mDrawerLayout, isConnected);
    }

    public static void setToolBar(Context context, String from) {
        switch (from) {
            case "home":
                logo.setVisibility(View.VISIBLE);
                filterbtn.setVisibility(View.VISIBLE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.GONE);
                spinLay.setVisibility(View.GONE);
                bannerLay.setVisibility(View.GONE);
                currentFragment = "HomeFragment";
                searchLay.setVisibility(View.GONE);
                break;
            case "findpeople":
                logo.setVisibility(View.VISIBLE);
                filterbtn.setVisibility(View.VISIBLE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.GONE);
                spinLay.setVisibility(View.GONE);
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "FindPeople";
                searchLay.setVisibility(View.GONE);
                break;
            case "visitors":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(context.getResources().getString(R.string.visitors));
                spinLay.setVisibility(View.GONE);
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "Visitors";
                searchLay.setVisibility(View.GONE);
                break;
            case "message":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                spinLay.setVisibility(View.VISIBLE);
                spinText.setText(context.getString(R.string.all_message));
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "Message";
                searchLay.setVisibility(View.GONE);
                break;
            case "friends":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                spinLay.setVisibility(View.VISIBLE);
                spinText.setText(context.getString(R.string.all_friends));
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "Friends";
                searchLay.setVisibility(View.GONE);
                break;
            case "requests":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(context.getResources().getString(R.string.friend_request));
                spinLay.setVisibility(View.GONE);
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "FriendRequests";
                searchLay.setVisibility(View.GONE);
                break;
            case "liked":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(context.getResources().getString(R.string.liked));
                spinLay.setVisibility(View.GONE);
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "Liked";
                searchLay.setVisibility(View.GONE);
                break;
            case "menuItemMatchMaker":
                logo.setVisibility(View.GONE);
                filterbtn.setVisibility(View.GONE);
                searchbtn.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(context.getResources().getString(R.string.matchmaker_feature));
                spinLay.setVisibility(View.GONE);
                if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
                    bannerLay.setVisibility(View.VISIBLE);
                } else {
                    bannerLay.setVisibility(View.GONE);
                }
                currentFragment = "Matchmaker Feature";
                searchLay.setVisibility(View.GONE);
                break;
        }
    }

    private void setUpNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //    mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d(TAG, "Drawer Opened");
                if (GetSet.isLogged()) {
      //              getCounts();
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "Drawer Closed");
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public void switchContent(Fragment fragment) {
        try {
            mContent = fragment;
            getSupportFragmentManager().beginTransaction().addToBackStack("back")
                    .replace(R.id.content_frame, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            System.out.println("jigar the error in switch content is "+e);
            e.printStackTrace();
        }
    }

    private void dialog() {
        final Dialog dialog = new Dialog(MainScreenActivity.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.headerTxt);
        TextView subTxt = dialog.findViewById(R.id.subTxt);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);

        title.setVisibility(View.GONE);

        subTxt.setText(getString(R.string.really_exit));

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                MainScreenActivity.this.finish();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finishAffinity();
                }

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

    /*API Implementation*/

    private void getCounts() {
//        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
//                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_COUNTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getCountsRes=" + res);
                            Log.v(TAG, "jigar the getCountsRes=" + res);

                            JSONObject jsonObjectResponseMain = new JSONObject(res);
                            String strStatus = jsonObjectResponseMain.getString(Constants.TAG_STATUS);
                        //    String strMsg = jsonObjectResponseMain.getString(Constants.TAG_MSG);

                            if (strStatus.equals("true")) {

                                JSONObject jsonObjectResult=jsonObjectResponseMain.getJSONObject(Constants.TAG_RESULT);
                                String strVisitor=jsonObjectResult.getString(Constants.TAG_VISITORS);
                                String strFriendRequestCount=jsonObjectResult.getString(Constants.TAG_FRIEND_REQUEST);
                                String strMessageCount=jsonObjectResult.getString(Constants.TAG_MESSAGE);
                                String strFriends=jsonObjectResult.getString(Constants.TAG_FRIENDS);
                                strVisitor="1";
                                strFriends="20";
                                strMessageCount="4";

//                                messageCount.setVisibility(View.GONE);
//                                visitorsCount.setVisibility(View.GONE);
//                                requestCount.setVisibility(View.GONE);

                                if(!strVisitor.equals("0"))
                                {
                                    visitBadge.setText(strVisitor);
                                    visitBadge.show();
                                } else {
                                    visitBadge.hide();
                                }

                                if(!strFriendRequestCount.equals("0"))
                                {
                                    requestBadge.setText(strFriendRequestCount);
                                    requestBadge.show();
                                } else {
                                    requestBadge.hide();
                                }
                                if(!strFriends.equals("0"))
                                {
                                    friendsBadge.setText(strFriends);
                                    friendsBadge.show();
                                } else {
                                    friendsBadge.hide();
                                }

                                if(!strMessageCount.equals("0"))
                                {
                                    chatBadge.setText(strMessageCount);
                                    chatBadge.show();
                                } else {
                                    chatBadge.hide();
                                }

                            }


//                                if(!strMessageCount.equals("0"))
//                                {
//                                    message.setText(strVisitor);
//                                    messageCount.show();
//                                } else {
//                                    messageCount.hide();
//                                }
//                            if (DefensiveClass.optString(jsonObjectResponseMain, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
//                                JSONObject result = jsonObjectResponseMain.getJSONObject(Constants.TAG_RESULT);
//                                messageCount.setVisibility(View.GONE);
//                                visitorsCount.setVisibility(View.GONE);
//                                requestCount.setVisibility(View.GONE);
//
//
//
//                                if (GetSet.isLogged() && !DefensiveClass.optInt(result, Constants.TAG_MESSAGE).equals("0") && !DefensiveClass.optInt(result, Constants.TAG_MESSAGE).equals("")) {
//                                    chatBadge.setText(DefensiveClass.optInt(result, Constants.TAG_MESSAGE));
//                                    chatBadge.show();
//                                } else {
//                                    chatBadge.hide();
//                                }
//
//                                if (GetSet.isLogged() && !DefensiveClass.optInt(result, Constants.TAG_VISITORS).equals("0") && !DefensiveClass.optInt(result, Constants.TAG_VISITORS).equals("")) {
//                                    visitBadge.setText(DefensiveClass.optInt(result, Constants.TAG_VISITORS));
//                                    visitBadge.show();
//                                } else {
//                                    visitBadge.hide();
//                                }
//
//                                if (GetSet.isLogged() && !DefensiveClass.optInt(result, Constants.TAG_FRIEND_REQUEST).equals("0") && !DefensiveClass.optInt(result, Constants.TAG_FRIEND_REQUEST).equals("")) {
//                                    requestBadge.setText(DefensiveClass.optInt(result, Constants.TAG_FRIEND_REQUEST));
//                                    requestBadge.show();
//                                } else {
//                                    requestBadge.hide();
//                                }

                                /*if (DefensiveClass.optInt(result, Constants.TAG_DEVICE_REGISTERED).equals("false")) {
                                    addDeviceId();
                                }*/
//
//                            } else if (DefensiveClass.optString(jsonObjectResponseMain, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
//                                CommonFunctions.disabledialog(MainScreenActivity.this, "Error", jsonObjectResponseMain.getString(Constants.TAG_MESSAGE));
//                            }


                        } catch (JSONException e) {
                            Log.d(TAG,"jigar the error in count badge json is "+e);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.d(TAG,"jigar the error in null pointer count badge is "+e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.d(TAG,"jigar the error in main exception is  count badge is "+e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "jigar the get count Error: " + error.getMessage());
            }

        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                String strTokenLikeUserID=GetSet.getUseridLikeToken();
                map.put(Constants.TAG_USERID, strTokenLikeUserID);
        //        map.put(Constants.TAG_DEVICE_ID, deviceId);
                Log.v(TAG, "jigar the params in getCountsParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_DEVICE_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            if (!DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(MainScreenActivity.this, json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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

    /*Onclick Event*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_lay:
                mDrawerLayout.closeDrawers();
                Intent p = new Intent(MainScreenActivity.this, MainViewProfileDetailActivity.class);
                p.putExtra("from", "myprofile");
                p.putExtra("strFriendID", GetSet.getUserId());
                startActivity(p);
                break;

            case R.id.empty_lay:
                break;
            case R.id.menubtn:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.people_near:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("HomeFragment")) {
                    switchContent(new HomeFragment());
                }
                break;
            case R.id.find_people:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("FindPeople")) {
                    switchContent(new FindPeople());
                }
                break;
            case R.id.visitors:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("Visitors")) {
                    switchContent(new Visitors());
                }
                break;
            case R.id.message:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("Message")) {
                    switchContent(new Message());
                }
                break;
            case R.id.friends:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("Friends")) {
                    switchContent(new Friends());
                }
                break;
            case R.id.request:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("FriendRequests")) {
                    switchContent(new FriendRequests());
                }
                break;
            case R.id.liked:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("Liked")) {
                    switchContent(new LikedFragment());
                }
                break;
            case R.id.menuItemMatchmakerFeature:
                mDrawerLayout.closeDrawers();
                if (!currentFragment.equals("Matchmaker Feature")) {
                    Bundle bundle=new Bundle();
                    // STATIC UNTIL USER BY DETAIL RESPONSE DOES NOT HAVE MATCH MAKER ID
                    bundle.putString(Constants.TAG_FRIEND_ID, pref.getString(Constants.TAG_LOGGED_USER_SPONSOR_ID,""));  //match maker id
                    bundle.putString(Constants.TAG_REGISTERED_ID, GetSet.getUserId());  // register id
                    switchContent(new MatchMakerFragment());
//                    GetSet.setFriendId("96639683");
//                    GetSet.setFriendId("96639683");

//                    strFriendID=GetSet.getFriendId();

                }
                break;
            case R.id.filterbtn:
                Intent i = new Intent(MainScreenActivity.this, Filter.class);
                startActivity(i);
                break;
            case R.id.become_premium:
                mDrawerLayout.closeDrawers();
                if (!premiumClicked) {
                    premiumClicked = true;
                    Intent m = new Intent(MainScreenActivity.this, PremiumDialog.class);
                    startActivity(m);
                }
                break;
            case R.id.setting:
                mDrawerLayout.closeDrawers();
                Intent j = new Intent(MainScreenActivity.this, Settings.class);
                startActivity(j);
                break;

            case R.id.linearLayoutMenuItemNotification:
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(MainScreenActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
        }
    }


    private void getUserDetailByID() {

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            if (CommonFunctions.isNetwork((MainScreenActivity.this))) {

                CommonFunctions.showProgressDialog(MainScreenActivity.this);

                StringRequest requestGetUserDetails = new StringRequest(Request.Method.POST, Constants.API_GET_USER_DETAILS_BY_ID,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String res) {
                                try {
                                    System.out.println("jigar the user details response we have by id is " + res);

                                    JSONObject results = new JSONObject(res);

                                    String strStatus = results.getString(Constants.TAG_STATUS);
                                    String strMsg = results.getString(Constants.TAG_MSG);
                                    JSONObject jsonObjectInfo = results.getJSONObject(Constants.TAG_INFO);

                                    Log.e(TAG, "getAccessTokenResult: " + results);
                                    //     System.out.println("jigar the response on user by id is " + res);
                                    if (strMsg.equals(Constants.TAG_SUCCESS)
                                            && strStatus.equals("1")) {

                                        String strAccessToken = jsonObjectInfo.getString(Constants.TAG_ACCESS_TOKEN_NEW);
                                        String strUserLikeTokenID = jsonObjectInfo.getString(Constants.TAG_ID);
                                        String strSponsorMatchMakerID = jsonObjectInfo.getString(Constants.TAG_SPONSOR_ID);

                                        editor.putString(Constants.TAG_AUTHORIZATION, strAccessToken);
                                        editor.commit();
                                        if(strSponsorMatchMakerID.equals(0))
                                        {
                                            strSponsorMatchMakerID="96302890";
                                        }
                                        editor.putString(Constants.TAG_LOGGED_USER_SPONSOR_ID, strSponsorMatchMakerID);
                                        editor.commit();

                                        editor.putString(Constants.TAG_TOKEN_LIKE_USER_ID, strUserLikeTokenID);
                                        editor.commit();

                                        GetSet.setLogged(true);
                                        GetSet.setUserId(pref.getString(Constants.TAG_USERID, null));
                                        GetSet.setUserName(pref.getString(Constants.TAG_USERNAME, null));
                                        GetSet.setImageUrl(pref.getString(Constants.TAG_USERIMAGE, null));
                                        GetSet.setUseridLikeToken(pref.getString(Constants.TAG_TOKEN_LIKE_USER_ID, null));
                                        if (pref.getBoolean(Constants.TAG_PREMIUM_MEMBER, false)) {
                                            GetSet.setPremium(true);
                                        } else {
                                            GetSet.setPremium(false);
                                        }

                                        if (!pref.getString(Constants.TAG_LOGGED_USER_SPONSOR_ID, "").equals("0")) {
                                            menuItemMatchMaker.setVisibility(View.VISIBLE);

                                        } else {
                                            menuItemMatchMaker.setVisibility(View.GONE);

                                        }
                                        GetSet.setLocation(pref.getString(Constants.TAG_LOCATION, ""));
                                        Log.v(TAG, "show home");
                                    }
//                                overridePendingTransition(R.anim.fade_in,
//                                        R.anim.fade_out);
//                                //switchContent(new FindPeople());
                                    //switchContent(new FindPeople());
                                    dialog.dismiss();

                                    getCounts();
                                    switchContent(new HomeFragment());

                                    CommonFunctions.hideProgressDialog(MainScreenActivity.this);

                                } catch (JSONException e) {
                                    CommonFunctions.hideProgressDialog(MainScreenActivity.this);

                                    System.out.println("jigar the error user details json response on user by id is " + e);
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    System.out.println("jigar the error user details null pointer response on user by id is " + e);
                                    CommonFunctions.hideProgressDialog(MainScreenActivity.this);

                                    e.printStackTrace();
                                } catch (Exception e) {
                                    System.out.println("jigar the error user details exception main  on user by id is " + e);
                                    CommonFunctions.hideProgressDialog(MainScreenActivity.this);

                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("jigar the user details volley error response on user by id is " + error);
                        CommonFunctions.hideProgressDialog(MainScreenActivity.this);
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        dialog.dismiss();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put(Constants.TAG_REGISTERED_ID, strUserID);
                        Log.v(TAG, "jigar the getAccessTokenParams=" + map);
                        return map;
                    }
                };
                queue.add(requestGetUserDetails);
//            HowzuApplication.getInstance().addToRequestQueue(req, TAG);
                //}
            }else
            {
                Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                CommonFunctions.hideProgressDialog(MainScreenActivity.this);


            }
            }catch(Exception e)
            {
                dialog.dismiss();

                Log.d(TAG, "jigar the error in main exception is " + e);
            }
        //    dialog.dismiss();


//        CommonFunctions.hideProgressDialog(this);

    }


//    @Override
//    public void onReceiveLocation(BDLocation bdLocation) {
//        try {
//            String Province = bdLocation.getProvince();
//            double doubleLatitude=bdLocation.getLatitude();
//            double doubleLongitude=bdLocation.getLongitude();
//
//            System.out.println("jigar the location we have is " + doubleLatitude +" and "+doubleLongitude);
//        }catch (Exception e)
//        {
//            System.out.println("jigar the error in location we have is " + e);
//        }
//    }
}