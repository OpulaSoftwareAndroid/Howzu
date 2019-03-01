package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.FontCache;
import com.hitasoft.app.customclass.LaybelLayout;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hitasoft.app.utils.Constants.TAG_IMAGE;

/**
 * Created by hitasoft on 23/11/16.
 */

public class  MainViewProfileDetailActivity extends AppCompatActivity implements View.OnClickListener
        , NetworkReceiver.ConnectivityReceiverListener
{
    String TAG = "MainViewProfileDetailActivity";

    FloatingActionButton fab, fab2, floatingButtonStartChat;
    AppBarLayout appbar;
    Display display;
    Toolbar toolbar;
    ArrayList<HashMap<String, String>> matchesAry = new ArrayList<HashMap<String, String>>(), searchAry = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> data = new HashMap<String, String>();

    CollapsingToolbarLayout collapsingToolbar;
    CoordinatorLayout coordinatorLayout;
    NestedScrollView scrollView;
    LaybelLayout laybelLayout;
    LinearLayout linearLayoutSendFriendRequest,linearLayoutSendVideoDate,linearLayoutSendDinnerRequest;
    ViewPager viewPager;
    CirclePageIndicator pageIndicator;
    ViewPagerAdapter viewPagerAdapter;
    ImageView backbtn, optionbtn;
            //, match, unmatch;
    ImageView imageViewStartChat;
    TextView setting, userName, bio, location, interest, info, membershipValid, becomePremium, toolbarTitle;
    LinearLayout  interestLay, premiumLay;
    FrameLayout gradientFrame;
    ProgressDialog dialog;
    ArrayList<String> arrayListInterestStatic;
    public static AppCompatActivity activity;
    boolean collapsed = false, showFab = false;
    String strVisitingIdLikeToken = "", strFriendID = "", strVisitorFriendID = "", scrollState = "expanded", sendMatch = "", userImage = "";
    HashMap<String, String> haspMapProfileDetails = new HashMap<String, String>();
    ArrayList<String> interestsAry = new ArrayList<>(), imagesAry = new ArrayList<String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    FloatingActionButton.OnVisibilityChangedListener fabListener = new FloatingActionButton.OnVisibilityChangedListener() {
        @Override
        public void onShown(FloatingActionButton fab) {
            super.onShown(fab);
            Log.v("onShown", "onShown=" + fab.getVisibility());
            fab.show();
            /*if(!fabShouldBeShown){
                fab.hide();
            }*/
        }

        @Override
        public void onHidden(FloatingActionButton fab) {
            super.onHidden(fab);
            Log.v("onHidden", "onHidden=" + fab.getVisibility());
            fab.hide();
            /*if(fabShouldBeShown){
                fab.show();
            }*/
        }
    };
    public Bitmap bitMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        activity = this;

        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setCollapsedTitleTypeface(FontCache.get("font_regular.ttf", this));
        collapsingToolbar.setExpandedTitleTypeface(FontCache.get("font_regular.ttf", this));
        collapsingToolbar.setExpandedTitleTextColor(ColorStateList.valueOf(Color.BLACK));
        appbar = findViewById(R.id.appbar);
        fab = findViewById(R.id.fab);
        fab2 = findViewById(R.id.fab2);
        coordinatorLayout = findViewById(R.id.main_content);
        backbtn = findViewById(R.id.backbtn);
        setting = findViewById(R.id.setting);
        floatingButtonStartChat = findViewById(R.id.floatingButtonStartChat);
        optionbtn = findViewById(R.id.optionbtn);
        userName = findViewById(R.id.userName);
        bio = findViewById(R.id.textViewProfileLocation);
        info = findViewById(R.id.info);
        location = findViewById(R.id.location);
        interest = findViewById(R.id.interest);
        //iconsLay = findViewById(R.id.iconsLay);
        gradientFrame = findViewById(R.id.gradientFrame);
        interestLay = findViewById(R.id.interestLay);
        premiumLay = findViewById(R.id.premiumLay);
        membershipValid = findViewById(R.id.membership_valid);
        becomePremium = findViewById(R.id.become_premium);
        scrollView = findViewById(R.id.scroll);
//        progress = findViewById(R.id.progress);
//        progress.setVisibility(View.GONE);
//        match = findViewById(R.id.match);
//        unmatch = findViewById(R.id.unmatch);
        laybelLayout = findViewById(R.id.laybel);
        viewPager = findViewById(R.id.view_pager);
        pageIndicator = findViewById(R.id.pager_indicator);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        linearLayoutSendFriendRequest=findViewById(R.id.linearLayoutSendFriendRequest);
        linearLayoutSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFriendRequest();
            }
        });
        linearLayoutSendVideoDate=findViewById(R.id.linearLayoutSendVideoDate);
        linearLayoutSendVideoDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendVideoDateRequest();
            }
        });

        linearLayoutSendDinnerRequest=findViewById(R.id.linearLayoutSendDinnerRequest);
        linearLayoutSendDinnerRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendDinnerDateRequest();
            }
        });

        arrayListInterestStatic=new ArrayList<String>();
        arrayListInterestStatic.add("Foodie");
        arrayListInterestStatic.add("Gaming");
        arrayListInterestStatic.add("Books");
        arrayListInterestStatic.add("Movies");
        arrayListInterestStatic.add("Bike Riding");

        display = getWindowManager().getDefaultDisplay();
        pageIndicator.setFillColor(getResources().getColor(R.color.colorPrimary));
        pageIndicator.setPageColor(getResources().getColor(R.color.colorAccent));
        pageIndicator.setStrokeColor(0);

        // FOR TESTING Disable INTENT---------------------------------------------------------------------------------
//        strVisitingIdLikeToken = getIntent().getExtras().getString(Constants.TAG_INTENT_FROM);
//        strFriendID = getIntent().getExtras().getString(Constants.TAG_FRIEND_ID);
//        p.putExtra(Constants.TAG_FRIEND_ID, GetSet.getUseridLikeToken());
//        p.putExtra(Constants.TAG_PROFILE_VISITOR_ID, peoplesAry.get(itemPosition).get(Constants.TAG_ID));
        // here friend id means user own id
        // and register id means friend id whos profile we are visiting
        strVisitingIdLikeToken = getIntent().getExtras().getString(Constants.TAG_PROFILE_VISITOR_ID_LIKE_TOKEN);
        strVisitorFriendID = getIntent().getExtras().getString(Constants.TAG_FRIEND_ID);
        strFriendID = getIntent().getExtras().getString(Constants.TAG_REGISTERED_ID);
        //        p.putExtra(Constants.TAG_FRIEND_ID, GetSet.getUseridLikeToken());
//        p.putExtra(Constants.TAG_PROFILE_VISITOR_ID, peoplesAry.get(itemPosition).get(Constants.TAG_ID));

        //userImage = getIntent().getExtras().getString("userImage");
        System.out.println("jigar the intent strfriend id is "+strVisitorFriendID);
        System.out.println("jigar the intent visitor who visit id is "+ strVisitingIdLikeToken);

        floatingButtonStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createNewChat();
//                Intent intent=new Intent(MainViewProfileDetailActivity.this,ChatActivity.class);
//                startActivity(intent);

            }
        });
//        if(strFriendID==null)
//        {
//           strFriendID=GetSet.getUserId();
//        }

        //System.out.println("jigar the intent user id is "+GetSet.getUserId());

//        strFriendID="18327071";
//        GetSet.setUserId("96519710");

//        strFriendID="46346028";
//        GetSet.setUserId("39100911");

        setting.setVisibility(View.GONE);
        scrollView.setVisibility(View.INVISIBLE);
        appbar.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab.hide();
        fab2.hide();
        floatingButtonStartChat.setVisibility(View.INVISIBLE);
        gradientFrame.setVisibility(View.GONE);
        //iconsLay.setVisibility(View.GONE);
        premiumLay.setVisibility(View.GONE);
//        progress.setVisibility(View.VISIBLE);
//        progress.spin();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        getProfile();
        visitProfile();
        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        backbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        becomePremium.setOnClickListener(this);
        setting.setOnClickListener(this);
        fab.setOnClickListener(this);
        fab2.setOnClickListener(this);
    //    match.setOnClickListener(this);
      //  unmatch.setOnClickListener(this);

        dialog = new ProgressDialog(MainViewProfileDetailActivity.this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void sendFriendRequest()
    {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_NEW_MATCH_AND_FRIEND_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "matchRes=" + res);
                        System.out.println("jigar the on like profile in json is "+res);

                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                            } else {

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
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                params.put(Constants.TAG_FRIEND_ID, strFriendID);
                Log.v(TAG, "matchParams=" + params);
                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");

    }

    public void sendVideoDateRequest()
    {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_SEND_VIDEO_CHAT_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "matchRes=" + res);
                        System.out.println("jigar the send video chat request response is  "+res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("0")) {
                                String strMessage = json.getString(Constants.TAG_MSG);
                                Toast.makeText(MainViewProfileDetailActivity.this,strMessage,Toast.LENGTH_LONG).show();
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            System.out.println("jigar the error in json send video chat request response is  "+e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            System.out.println("jigar the error in null pointer send video chat request is  "+e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("jigar the error in main exception send video chat request is  "+e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                VolleyLog.d(TAG, "jigar the volley error in send video date params " + error.getMessage());

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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                params.put(Constants.TAG_FRIEND_ID, strFriendID);
                Log.v(TAG, "jigar the send video date params " + params);
                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");

    }
    public void sendDinnerDateRequest()
    {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_SEND_DINNER_DATE_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "matchRes=" + res);
                        System.out.println("jigar the send dinner request response is  "+res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("0")) {
                                String strMessage = json.getString(Constants.TAG_MSG);
                                Toast.makeText(MainViewProfileDetailActivity.this,strMessage,Toast.LENGTH_LONG).show();
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            System.out.println("jigar the error in json dinner chat request response is  "+e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            System.out.println("jigar the error in null pointer dinner chat request is  "+e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("jigar the error in main exception dinner chat request is  "+e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                VolleyLog.d(TAG, "jigar the volley error in send dinner date params " + error.getMessage());

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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                params.put(Constants.TAG_FRIEND_ID, strFriendID);
                Log.v(TAG, "jigar the send dinner date params " + params);
                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");

    }

    private void createNewChat() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "jigar the create Chat Response " + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

//                                data.put(Constants.TAG_CHAT_ID, DefensiveClass.optString(json, Constants.TAG_CHAT_ID));
                                data.put(Constants.TAG_CHAT_ID, "3");
                                data.put(Constants.TAG_BLOCKED_BY_ME, DefensiveClass.optInt(json, Constants.TAG_BLOCKED_BY_ME));
                                data.put(Constants.TAG_BLOCK, DefensiveClass.optInt(json, Constants.TAG_BLOCK));
                                data.put(Constants.TAG_USER_STATUS, DefensiveClass.optInt(json, Constants.TAG_USER_STATUS));
                                data.put(Constants.TAG_ONLINE, DefensiveClass.optInt(json, Constants.TAG_ONLINE));
                                data.put(Constants.TAG_LAST_ONLINE, DefensiveClass.optInt(json, Constants.TAG_LAST_ONLINE));
                                Log.v(TAG, "jigar the json chat before image is " + imagesAry.get(0));
//                                Log.v(TAG, "jigar the json chat profile image is " +  haspMapProfileDetails.get(Constants.TAG_PROFILE_IMAGE));
                                data.put(Constants.TAG_USERIMAGE, imagesAry.get(0));
                                data.put(Constants.TAG_USERNAME, haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME));
                                data.put(Constants.TAG_USERID, strVisitingIdLikeToken);

                                Log.v(TAG, "jigar the json response is map data is " + data);

                                Intent intent=new Intent(MainViewProfileDetailActivity.this,ChatActivity.class);
                                intent.putExtra("data", data);
                                intent.putExtra("position", 0);
                                intent.putExtra("strVisitingIdLikeToken", "profile");
                                startActivity(intent);

//                                if (data.get(Constants.TAG_BLOCKED_BY_ME).equals("true")) {
//                                    chatStatus.setVisibility(View.VISIBLE);
//                                    chatStatus.setText(getString(R.string.you_blocked));
//                                    editText.setEnabled(false);
//                                    send.setOnClickListener(null);
//                                    attachbtn.setOnClickListener(null);
//                                } else if (data.get(Constants.TAG_USER_STATUS).equals("0")) {
//                                    Picasso.with(ChatActivity.this).load(R.drawable.user_placeholder).transform(new CircleTransform()).into(userimage);
//                                    chatStatus.setVisibility(View.VISIBLE);
//                                    chatStatus.setText(getString(R.string.account_deactivated));
//                                    editText.setEnabled(false);
//                                    send.setOnClickListener(null);
//                                    attachbtn.setOnClickListener(null);
//                                    optionbtn.setOnClickListener(null);
//                                } else {
//                                    chatStatus.setVisibility(View.GONE);
//                                }
//
//                                try {
//                                    if (data.get(Constants.TAG_ONLINE).equals("1")) {
//                                        online.setText("Online");
//                                        online.setSelected(false);
//                                    } else {
//                                        online.setText("Last seen at " + getTime(Long.parseLong(data.get(Constants.TAG_LAST_ONLINE)), Constants.TAG_LAST_SEEN));
//                                        online.setSelected(true);
//                                    }
//                                } catch (NumberFormatException e) {
//                                    e.printStackTrace();
//                                }

                            }
                        } catch (JSONException e) {
                            Log.v(TAG, "jigar the json exception create Chat" + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.v(TAG, "jigar the null pointer exception create Chat" + e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.v(TAG, "jigar the main exception create Chat" + e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "jigar the volley Error in chat create is " + error.getMessage());
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
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_FRIEND_ID, strVisitingIdLikeToken);
                Log.v(TAG, "jigar the create chat params are " + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.main_content), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageIndicator.setVisibility(imagesAry.size() > 1 ? View.VISIBLE : View.GONE);
        setFabClickable(true);
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

    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    private void checkUser() {
        if (strVisitingIdLikeToken.equals("myprofile") || GetSet.getUserId().equals(strFriendID)) {
            strFriendID = GetSet.getUserId();
            setting.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.pen);
            fab2.setImageResource(R.drawable.pen);
            showFab = true;
            fab.show(fabListener);
            optionbtn.setVisibility(View.GONE);
            //iconsLay.setVisibility(View.GONE);
            gradientFrame.setVisibility(View.GONE);
        } else {
            setting.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.msg);
            fab2.setImageResource(R.drawable.msg);
            optionbtn.setVisibility(View.VISIBLE);
            switch (sendMatch) {
                case "4":
                    showFab = false;
              //      iconsLay.setVisibility(View.VISIBLE);
                    gradientFrame.setVisibility(View.GONE);
                    break;
                case "3":
                    showFab = false;
                //    iconsLay.setVisibility(View.GONE);
                    gradientFrame.setVisibility(View.GONE);
                    break;
                case "5":
                    showFab = false;
                  //  iconsLay.setVisibility(View.VISIBLE);
                    gradientFrame.setVisibility(View.GONE);
                    break;
                case "1":
                    showFab = true;
                    fab.show(fabListener);
                 //   iconsLay.setVisibility(View.GONE);
                    gradientFrame.setVisibility(View.GONE);
                    break;
                case "0":
                    showFab = false;
                  //  iconsLay.setVisibility(View.GONE);
                    gradientFrame.setVisibility(View.GONE);
                    break;
            }
            //visitProfile();
        }
    }

    private void setProfile() {
        if (haspMapProfileDetails.get(Constants.TAG_AGE_STATUS).equals("true") && !haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
            String strUserName = haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(1);
            collapsingToolbar.setTitle(strUserName);
            collapsingToolbar.setCollapsedTitleTypeface(FontCache.get("font_regular.ttf", this));

        } else {
            collapsingToolbar.setTitle(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME) + ", " + haspMapProfileDetails.get(Constants.TAG_AGE));
            String strUserName = haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(1)
                    + ", " + haspMapProfileDetails.get(Constants.TAG_AGE);
            collapsingToolbar.setTitle(strUserName);
            collapsingToolbar.setCollapsedTitleTypeface(FontCache.get("font_regular.ttf", this));

        }

        if (haspMapProfileDetails.get(Constants.TAG_INFO).equals("")) {
            info.setVisibility(View.GONE);
        } else {
            info.setText(haspMapProfileDetails.get(Constants.TAG_INFO));
        }

        if (haspMapProfileDetails.get(Constants.TAG_BIO).equals("")) {
            bio.setVisibility(View.GONE);
        } else {
            String strBio = haspMapProfileDetails.get(Constants.TAG_BIO).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_BIO).substring(1);
            bio.setText(Html.fromHtml(strBio));
        }

        if (!haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
            if (haspMapProfileDetails.get(Constants.TAG_DISTANCE_STATUS).equals("true")) {
                location.setVisibility(View.GONE);
            } else {
                location.setText("Lives in " + haspMapProfileDetails.get(Constants.TAG_LOCATION));
            }
        }

        setAppbarListener();

//        if (!haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).equals(null)||
         if(       !haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).equals("")) {
            try {
                List<String> aListInterest = Arrays.asList(haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).split("\\s*,\\s*"));
                System.out.println("jigar the array arrayListComments  in tab listner is "+aListInterest.toString());
                System.out.println("jigar the array arrayListComments  size is tab listner is "+aListInterest.size());

                JSONArray ints = new JSONArray(aListInterest);
                for (int i = 0; i < ints.length(); i++) {
                    interestsAry.add(arrayListInterestStatic.get(Integer.parseInt(ints.optString(i, ""))));
                }
            }

            //            catch (JSONException e) {
//                System.out.println("jigar the error json exception in tablistner is "+e);
//                e.printStackTrace();
//            }
            catch (NullPointerException e) {
                System.out.println("jigar the error null pointer exception in tablistner is "+e);

                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("jigar the error main exception in tablistner is "+e);

                e.printStackTrace();
            }
        }

        if (interestsAry.size() == 0) {
            interestLay.setVisibility(View.GONE);
        } else {
            interest.setText(interestsAry.size() + " interest");
        }

        for (int i = 0; i < interestsAry.size(); i++) {
            TextView tv = new TextView(this);
            tv.setText(interestsAry.get(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTypeface(FontCache.get("font_regular.ttf", this));
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            lp.leftMargin = HowzuApplication.dpToPx(this, 7);
            lp.rightMargin = HowzuApplication.dpToPx(this, 7);
            tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_rounded_stroke_corner));
            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
            laybelLayout.addView(tv, lp);
        }

        if (!haspMapProfileDetails.get(Constants.TAG_IMAGES).equals(null) && !haspMapProfileDetails.get(Constants.TAG_IMAGES).equals("")) {
            try {
                // change this for combined array of images -----------------------------------------------------------------------------------------------
                List<String> aListImage = Arrays.asList(haspMapProfileDetails.get(Constants.TAG_IMAGE).split("\\s*,\\s*"));
                JSONArray arrayProfileImages = new JSONArray(aListImage);

//                JSONArray imgs = new JSONArray(haspMapProfileDetails.get(Constants.TAG_IMAGE));
                for (int i = 0; i < arrayProfileImages.length(); i++) {
                    imagesAry.add(arrayProfileImages.optString(i, ""));
                }
            }
//            catch (JSONException e) {
//                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);
//
//                e.printStackTrace();
//            }
            catch (NullPointerException e) {
                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);

                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);

                e.printStackTrace();
            }
        }

        if (imagesAry.size() == 0) {
            imagesAry.add(haspMapProfileDetails.get(Constants.TAG_PROFILE_IMAGE));
        }
        pageIndicator.setVisibility(imagesAry.size() > 1 ? View.VISIBLE : View.GONE);

        viewPagerAdapter = new ViewPagerAdapter(MainViewProfileDetailActivity.this, imagesAry);
        viewPager.setAdapter(viewPagerAdapter);
        pageIndicator.setViewPager(viewPager);

        if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0") && haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
            premiumLay.setVisibility(View.VISIBLE);
            becomePremium.setVisibility(View.GONE);
            membershipValid.setVisibility(View.VISIBLE);
            floatingButtonStartChat.setVisibility(View.VISIBLE);
            floatingButtonStartChat.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!fab.isShown()) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) floatingButtonStartChat.getLayoutParams();
                        params.setMargins(0, HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 80), HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 15), 0);
                        floatingButtonStartChat.setLayoutParams(params);
                    }
                    floatingButtonStartChat.show();
                }
            }, 50);
            try {
                if (!haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID).equals(null) && !haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID).equals("")) {
                    long date = Long.parseLong(haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID)) * 1000;
                    membershipValid.setText("Membership valid upto " + getDate(date));
                }
            } catch (NumberFormatException e) {
                System.out.println("jigar the error number format exception is "+e);
                e.printStackTrace();
            }
        } else {
            premiumLay.setVisibility(View.GONE);
            floatingButtonStartChat.setVisibility(View.INVISIBLE);
        }
    }

    private void setAppbarListener() {
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (verticalOffset == 0) {
                    backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryText));
                    Log.v("expanded", "expanded");
                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0"))
                    {
                        floatingButtonStartChat.show();
                    }
                    if (showFab) {
                        fab.show(fabListener);
                        fab2.hide(fabListener);
                    } else {
                        fab.hide(fabListener);
                        fab2.hide(fabListener);
                    }

                    scrollState = "expanded";

                    /*if (collapsed){
                        collapsed = false;
                        *//*fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                                p.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                                view.setLayoutParams(p);
                                view.requestLayout();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();*//*
                     *//*fab.setY(coordinatorLayout.getHeight() - (fab.getHeight() * 3));
                        fab.invalidate();*//*
                    }*/
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    Log.v("collapsed", "collapsed");
                    scrollState = "collapsed";
                    backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    toolbarTitle.setVisibility(View.VISIBLE);
                    if (haspMapProfileDetails.get(Constants.TAG_AGE_STATUS).equals("0") && !haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
                        toolbarTitle.setText(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME));
                    } else {
                        toolbarTitle.setText(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME) + ", " + haspMapProfileDetails.get(Constants.TAG_AGE));
                    }
                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0")) {
                        floatingButtonStartChat.hide();
                    }
                    if (showFab) {
                        fab.hide(fabListener);
                        fab2.show(fabListener);
                    } else {
                        fab.hide(fabListener);
                        fab2.hide(fabListener);
                    }
                    collapsed = true;
                    /*CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                    p.setBehavior(null);
                    view.setLayoutParams(p);
                    view.requestLayout();
                    fab.animate().translationY(coordinatorLayout.getHeight() - (fab.getHeight() * 3)).setInterpolator(new AccelerateInterpolator(2)).start();*/
                    //   fab.animate().translationY(coordinatorLayout.getHeight() - (fab.getHeight() * 3)).setInterpolator(new AccelerateInterpolator(1)).start();
                } else {
                    if (toolbarTitle.getVisibility() == View.VISIBLE) {
                        toolbarTitle.setVisibility(View.GONE);
                    }
                    Log.v("collapse", "collapse" + verticalOffset);
                }
            }
        });
    }

    /**
     * To convert timestamp to Date
     **/

    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    private void dialog(final String type) {
        final Dialog dialog = new Dialog(MainViewProfileDetailActivity.this);
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
        if (type.equals("report")) {
            if (haspMapProfileDetails.get(Constants.TAG_REPORT).equals("true")) {
                subTxt.setText(getString(R.string.really_undo_report));
            } else {
                subTxt.setText(getString(R.string.really_report));
            }
        } else {
            subTxt.setText(getString(R.string.really_unfriend));
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (type.equals("report"))
                    reportUser();
                else
                    unfriendUser();
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

    /**
     * function for showing the popup window
     **/

    public void viewOptions(View v) {
        Rect location = locateView(v);
        String[] values;
//        if (haspMapProfileDetails.get(Constants.TAG_REPORT).equals("true")) {
//            values = new String[]{getString(R.string.undo_report), getString(R.string.unfriend_user)};
//        } else

            if (strVisitingIdLikeToken.equals("other") || strVisitingIdLikeToken.equals("home")) {
            values = new String[]{getString(R.string.report_user)};
        } else if (sendMatch.equals("1")) {
            values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
        } else if (strVisitingIdLikeToken.equals("visitors")) {
            if (sendMatch.equals("1")) {
                values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
            } else {
                values = new String[]{getString(R.string.report_user)};
            }
        } else {
            values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainViewProfileDetailActivity.this,
                R.layout.options_item, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.options, null);
        // layout.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.linear_interpolator));
        final PopupWindow popup = new PopupWindow(MainViewProfileDetailActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 50 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(coordinatorLayout, Gravity.TOP | Gravity.LEFT, location.left, location.bottom);

        final ListView lv = layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        popup.dismiss();
                        dialog("report");
                        break;
                    case 1:
                        popup.dismiss();
                        dialog("unfriend");
                        break;
                }
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, int posi) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.profile_image_viewer,
                    collection, false);

            ImageView image = itemView.findViewById(R.id.imgDisplay);
            if (!temp.get(posi).equals("")) {
                //  Picasso.with(context).load(temp.get(posi)).into(image);

                Picasso.with(context)
                        .load(temp.get(posi))
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .fit()
                        .into(image);

            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainViewProfileDetailActivity.this, ViewImage.class);
                    intent.putExtra("images", imagesAry);
                    intent.putExtra("position", viewPager.getCurrentItem());
                    Pair<View, String> bodyPair = Pair.create(view, "fromProfile");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainViewProfileDetailActivity.this, bodyPair);
                    ActivityCompat.startActivity(MainViewProfileDetailActivity.this, intent, options.toBundle());
                }
            });

            collection.addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    /**
     * API Implementation
     **/

    private void getProfile() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(MainViewProfileDetailActivity.this))) {
            CommonFunctions.showProgressDialog(this);

            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_NEW_VIEW_PROFILE_DETAIL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            try {
                                Log.v(TAG, "getProfileRes=" + res);
                                System.out.println("jigar the response on view profile is " + res);
                                JSONObject json = new JSONObject(res);
                                String strStatus = DefensiveClass.optString(json, Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    JSONObject jsonObjectMainInfo = json.getJSONObject(Constants.TAG_INFO);
                                    JSONObject jsonObjectMainUserInfo = jsonObjectMainInfo.getJSONObject(Constants.TAG_USER_INFO);

                                    //    sendMatch = DefensiveClass.optString(jsonObject, Constants.TAG_SEND_MATCH);

                                    haspMapProfileDetails.put(Constants.TAG_REGISTERED_ID, jsonObjectMainUserInfo.getString(Constants.TAG_REGISTERED_ID));
                                    haspMapProfileDetails.put(Constants.TAG_NEW_USERNAME, jsonObjectMainUserInfo.getString(Constants.TAG_NEW_USERNAME));
                                    //          haspMapProfileDetails.put(Constants.TAG_GENDER, DefensiveClass.optString(jsonObject, Constants.TAG_GENDER));
                                    haspMapProfileDetails.put(Constants.TAG_AGE, jsonObjectMainUserInfo.getString(Constants.TAG_AGE));
                                    haspMapProfileDetails.put(Constants.TAG_BIO, jsonObjectMainUserInfo.getString(Constants.TAG_BIO));
                                    haspMapProfileDetails.put(Constants.TAG_LATITUDE, jsonObjectMainUserInfo.getString(Constants.TAG_LATITUDE));
                                    haspMapProfileDetails.put(Constants.TAG_LONGITUDE, jsonObjectMainUserInfo.getString(Constants.TAG_LONGITUDE));
                                    haspMapProfileDetails.put(Constants.TAG_ONLINE_STATUS, jsonObjectMainUserInfo.getString(Constants.TAG_ONLINE_STATUS));
                                    haspMapProfileDetails.put(Constants.TAG_LOCATION, jsonObjectMainUserInfo.getString(Constants.TAG_LOCATION));
                                    haspMapProfileDetails.put(Constants.TAG_INFO, jsonObjectMainUserInfo.getString(Constants.TAG_INFO));
                                    haspMapProfileDetails.put(Constants.TAG_INTEREST, jsonObjectMainUserInfo.getString(Constants.TAG_INTEREST));
                                    haspMapProfileDetails.put(Constants.TAG_INTEREST_PLAN, jsonObjectMainUserInfo.getString(Constants.TAG_INTEREST_PLAN));
                                    haspMapProfileDetails.put(Constants.TAG_PROFILE_IMAGE, jsonObjectMainUserInfo.getString(Constants.TAG_PROFILE_IMAGE));
                                    haspMapProfileDetails.put(Constants.TAG_IMAGES, jsonObjectMainUserInfo.getString(Constants.TAG_IMAGES));
                                    haspMapProfileDetails.put(Constants.TAG_AGE_STATUS, jsonObjectMainUserInfo.getString(Constants.TAG_AGE_STATUS));
                                    haspMapProfileDetails.put(Constants.TAG_DISTANCE_STATUS, jsonObjectMainUserInfo.getString(Constants.TAG_DISTANCE_STATUS));
                                    haspMapProfileDetails.put(Constants.TAG_INVISIBLE_STATUS, jsonObjectMainUserInfo.getString(Constants.TAG_INVISIBLE_STATUS));
                                    //  haspMapProfileDetails.put(Constants.TAG_REPORT, DefensiveClass.optString(jsonObject, Constants.TAG_REPORT));
                                    haspMapProfileDetails.put(Constants.TAG_PREMIUM_FROM, jsonObjectMainUserInfo.getString(Constants.TAG_PREMIUM_FROM));
                                    haspMapProfileDetails.put(Constants.TAG_VIDEO_DATE, jsonObjectMainUserInfo.getString(Constants.TAG_VIDEO_DATE));
                                    haspMapProfileDetails.put(Constants.TAG_DINNER, jsonObjectMainUserInfo.getString(Constants.TAG_DINNER));
//                                    haspMapProfileDetails.put(Constants.TAG_REPORT, jsonObjectMainUserInfo.getString(Constants.TAG_REPORT));
                                    haspMapProfileDetails.put(Constants.TAG_REPORT, "0");

                                    //                                    haspMapProfileDetails.put(Constants.TAG_VIDEO_DATE, "0");

                                    if(haspMapProfileDetails.get(Constants.TAG_VIDEO_DATE).equals("1"))
                                    {
                                        linearLayoutSendVideoDate.setClickable(true);
                                    }else
                                    {
                                        linearLayoutSendVideoDate.setClickable(false);

                                    }

                                    if(haspMapProfileDetails.get(Constants.TAG_DINNER).equals("1"))
                                    {
                                        linearLayoutSendDinnerRequest.setClickable(true);
                                    }else
                                    {
                                        linearLayoutSendDinnerRequest.setClickable(false);
                                    }
                                    //   haspMapProfileDetails.put(Constants.TAG_MEMBERSHIP_VALID, DefensiveClass.optString(jsonObject, Constants.TAG_MEMBERSHIP_VALID));
                                    //    haspMapProfileDetails.put(Constants.TAG_SEND_MATCH, sendMatch);
                                    haspMapProfileDetails.put(TAG_IMAGE, jsonObjectMainUserInfo.getString(TAG_IMAGE));

                                    scrollView.setVisibility(View.VISIBLE);
                                    appbar.setVisibility(View.VISIBLE);
                                    CommonFunctions.hideProgressDialog(MainViewProfileDetailActivity.this);

                                    System.out.println("jigar the response on images we have view profile is " + haspMapProfileDetails.get(TAG_IMAGE));

                                    checkUser();
                                    setProfile();

                                } else if (strStatus.equals("0")) {
                                    System.out.println("jigar the error in status json response on view profile is " + json.getString(Constants.TAG_MSG));

                                    CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                                }
                            } catch (JSONException e) {
                                System.out.println("jigar the error in json response on view profile is " + e);
                                e.printStackTrace();
                            }
                            //                        catch (NullPointerException e) {
                            //                            System.out.println("jigar the error in null pointeron view profile is "+e);
                            //
                            //                            e.printStackTrace();
                            //                        }
                            catch (Exception e) {
                                System.out.println("jigar the error main exception view profile is " + e);
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    System.out.println("jigar the error in volley response view profile is " + error.getMessage());
                    Toast.makeText(MainViewProfileDetailActivity.this,R.string.something_went_wrong,Toast.LENGTH_LONG).show();
                    CommonFunctions.hideProgressDialog(MainViewProfileDetailActivity.this);

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
                    //if (GetSet.getUserId().equals(strFriendID))
                    {

                        //                    GetSet.setUserId("96519710");
                        //                    map.put(Constants.TAG_REGISTERED_ID,"96519710");
//                        map.put(Constants.TAG_REGISTERED_ID, strFriendID);
//                        map.put(Constants.TAG_FRIEND_ID, pref.getString(Constants.TAG_USERID,""));
                        map.put(Constants.TAG_FRIEND_ID, strFriendID);
                        map.put(Constants.TAG_REGISTERED_ID, pref.getString(Constants.TAG_USERID,""));

                    }
                    // else
                    //                    {
                    //                    map.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                    //                    map.put(Constants.TAG_FRIEND_ID, strFriendID);
                    //                }
                    // map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                    Log.v(TAG, "jigar the profile visitor params =" + map);
                    return map;
                }

            };
            HowzuApplication.getInstance().addToRequestQueue(req, TAG);
        }else {
            CommonFunctions.hideProgressDialog(MainViewProfileDetailActivity.this);

            Toast.makeText(MainViewProfileDetailActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }


    }

    private void visitProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_NEW_VISIT_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "visitProfileRes=" + res);
                            System.out.println("jigar the response in visitor is "+ res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
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
                System.out.println("jigar the volley error in response  is "+ error.getMessage());
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
                map.put(Constants.TAG_NEW_USERID, strVisitorFriendID);
//                map.put(Constants.TAG_VISIT_USER_ID, strFriendID);
                map.put(Constants.TAG_NEW_VISIT_USER_ID, strVisitingIdLikeToken);

                System.out.println("jigar the visitor is "+ map);

                Log.v(TAG, "visitProfileParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    public void match(final String followId) {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_MATCH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        //match.setOnClickListener(MainViewProfileDetailActivity.this);
                        Log.d(TAG, "matchRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            if (json.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                          //      iconsLay.setVisibility(View.GONE);
                                gradientFrame.setVisibility(View.GONE);
                                if (sendMatch.equals("4")) {
                                    Toast.makeText(MainViewProfileDetailActivity.this, "Request accepted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainViewProfileDetailActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                                    HomeFragment.peoplesAry.remove(0);
                                    HomeFragment.flingContainer.removeViewsInLayout(0, 1);
                                }
                                if (scrollState.equals("expanded") && sendMatch.equals("4")) {
                                    Log.v(TAG, "if");
                                    sendMatch = "1";
                                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0")) {
                                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) floatingButtonStartChat.getLayoutParams();
                                        params.setMargins(0, HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 80), HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 75), 0);
                                        floatingButtonStartChat.setLayoutParams(params);
                                    }
                                    showFab = true;
                                    fab.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fab.show();
                                        }
                                    }, 50);
                                } else if (scrollState.equals("collapsed") && sendMatch.equals("4")) {
                                    Log.v(TAG, "else if");
                                    sendMatch = "1";
                                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0")) {
                                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) floatingButtonStartChat.getLayoutParams();
                                        params.setMargins(0, HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 80), HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 75), 0);
                                        floatingButtonStartChat.setLayoutParams(params);
                                    }
                                    showFab = true;
                                    fab2.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fab2.show();
                                        }
                                    }, 50);
                                } else {
                                    Log.v(TAG, "else");
                                }
                            } else if (json.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
   //             match.setOnClickListener(MainViewProfileDetailActivity.this);
                dialog.dismiss();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_USERID, GetSet.getUserId());
                params.put(Constants.TAG_FOLLOW_ID, followId);
                Log.v(TAG, "matchParams=" + params);
                return params;
            }

        };
        dialog.show();
        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");

    }

    private void reportUser() {
        dialog.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_REPORT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "jigar the report User Response we have=" + res);
                            JSONObject json = new JSONObject(res);
                            dialog.dismiss();
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(MainViewProfileDetailActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                                if (haspMapProfileDetails.get(Constants.TAG_REPORT).equals("true")) {
                                    haspMapProfileDetails.put(Constants.TAG_REPORT, "false");
                                } else {
                                    haspMapProfileDetails.put(Constants.TAG_REPORT, "true");
                                }
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString("message"));
                            }
                        } catch (JSONException e) {
                            Log.v(TAG, "jigar the error json report User Response we have" + e);

                            e.printStackTrace();
                            dialog.dismiss();
                        } catch (NullPointerException e) {
                            Log.v(TAG, "jigar the error null pointer report User Response we have" + e);

                            e.printStackTrace();
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            dialog.dismiss();
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
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_REPORT_USER_ID, strFriendID);
                Log.v(TAG, "reportUserParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void unfriendUser() {
        dialog.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UNFRIEND_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "unfriendUserRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(MainViewProfileDetailActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                finish();
                                Intent intent = new Intent(MainViewProfileDetailActivity.this, MainScreenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_UNFRIEND_USER_ID, strVisitingIdLikeToken);
                Log.v(TAG, "unfriendUserParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void createChat() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "createChatRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                haspMapProfileDetails.put(Constants.TAG_CHAT_ID, DefensiveClass.optString(json, Constants.TAG_CHAT_ID));
                                haspMapProfileDetails.put(Constants.TAG_BLOCKED_BY_ME, DefensiveClass.optInt(json, Constants.TAG_BLOCKED_BY_ME));
                                haspMapProfileDetails.put(Constants.TAG_BLOCK, DefensiveClass.optInt(json, Constants.TAG_BLOCK));
                                haspMapProfileDetails.put(Constants.TAG_USERID, strVisitingIdLikeToken);
                                haspMapProfileDetails.put(Constants.TAG_USER_STATUS, DefensiveClass.optInt(json, Constants.TAG_USER_STATUS));
                                haspMapProfileDetails.put(Constants.TAG_ONLINE, DefensiveClass.optInt(json, Constants.TAG_ONLINE));
                                haspMapProfileDetails.put(Constants.TAG_LAST_ONLINE, DefensiveClass.optInt(json, Constants.TAG_LAST_ONLINE));
                                Intent i = new Intent(MainViewProfileDetailActivity.this, ChatActivity.class);
                                i.putExtra("data", haspMapProfileDetails);
                                Log.d(TAG, "haspMapProfileDetails: " + haspMapProfileDetails);
                                i.putExtra("from", "profile");
                                i.putExtra("position", 0);
                                startActivity(i);
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(MainViewProfileDetailActivity.this, "Error", json.getString(Constants.TAG_MESSAGE));
                                setFabClickable(true);
                            }
                        } catch (JSONException e) {
                            setFabClickable(true);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            setFabClickable(true);
                            e.printStackTrace();
                        } catch (Exception e) {
                            setFabClickable(true);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                setFabClickable(true);
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
                map.put(Constants.TAG_FRIEND_ID, strFriendID);
                Log.v(TAG, "createChatParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        LikedFragment mFragment = null;
        mFragment = new LikedFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mFragment).commit();
//        Intent intent=new Intent(this,MainScreenActivity.class);
//        intent.putExtra(Constants.TAG_INTENT_FROM,Constants.TAG_INTENT_PROFILE_PAGE);
//        startActivity(intent);
//        finish();
    }

    public void unmatch(final String followId) {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_UNMATCH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                //        unmatch.setOnClickListener(MainViewProfileDetailActivity.this);
                        Log.d(TAG, "unmatchRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            //iconsLay.setVisibility(View.GONE);
                            gradientFrame.setVisibility(View.GONE);
                            if (json.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(MainViewProfileDetailActivity.this, "Request declined", Toast.LENGTH_SHORT).show();
                                if(strVisitingIdLikeToken.equals("visitors")){
                                    finish();
                                } else if(strVisitingIdLikeToken.equalsIgnoreCase("home")) {
                                    HomeFragment.decline();
                                } else {
                                    finish();
                                }
                                /*Intent i = new Intent(MainViewProfileDetailActivity.this, EditPhoto.class);
                                i.putExtra("strVisitingIdLikeToken", "myprofile");
                                i.putExtra("data", haspMapProfileDetails);
                                i.putExtra("images", imagesAry);
                                startActivity(i);*/
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //unmatch.setOnClickListener(MainViewProfileDetailActivity.this);
                dialog.dismiss();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_USERID, GetSet.getUserId());
                params.put(Constants.TAG_FOLLOW_ID, followId);
                Log.v(TAG, "unmatchParams=" + params);
                return params;
            }
        };
        dialog.show();
        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");
    }

    /*OnClick Event*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.optionbtn:
                viewOptions(v);
                break;
            case R.id.become_premium:
                Intent m = new Intent(MainViewProfileDetailActivity.this, PremiumDialog.class);
                startActivity(m);
                break;
            case R.id.setting:
                Intent j = new Intent(MainViewProfileDetailActivity.this, Settings.class);
                startActivity(j);
                break;
            case R.id.fab:
            case R.id.fab2:
                if (strVisitingIdLikeToken.equals("myprofile") || GetSet.getUseridLikeToken().equals(strFriendID)) {
                    Intent i = new Intent(MainViewProfileDetailActivity.this, EditPhoto.class);
                    i.putExtra("data", haspMapProfileDetails);
                    i.putExtra("images", imagesAry);
                    startActivity(i);
                } else {
                    if (HowzuApplication.isNetworkAvailable(getApplicationContext())) {
                        if (strVisitingIdLikeToken.equalsIgnoreCase("chat")) {
                            finish();
                        } else {
                            setFabClickable(false);
                            createChat();
                        }
                    } else {
                        HowzuApplication.showSnack(this, findViewById(R.id.main_content), false);
                    }
                }
                break;
            case R.id.match:
//                match.setOnClickListener(null);
//                match.startAnimation(AnimationUtils.loadAnimation(MainViewProfileDetailActivity.this, R.anim.button_bounce));
                match(strFriendID);
                break;
            case R.id.unmatch:
//                unmatch.setOnClickListener(null);
//                unmatch.startAnimation(AnimationUtils.loadAnimation(MainViewProfileDetailActivity.this, R.anim.button_bounce));
                unmatch(strFriendID);
                break;
        }
    }

    void setFabClickable(boolean clickable) {
        fab.setClickable(clickable);
        fab.setEnabled(clickable);
        fab2.setClickable(clickable);
        fab2.setEnabled(clickable);
    }
}
