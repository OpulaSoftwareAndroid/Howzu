package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.customclass.RoundedImageView;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener, View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();
    private Toolbar toolbar;
    private CustomTextView toolbarTitle, bio, info, location, membershipValid, becomePremium, txtBasic,
            txtName, txtLocate, txtLocation;
    private ScrollView scroll;
    ImageView setting, backbtn;
    private RoundedImageView profilePic;
    private RelativeLayout detailLay, photoLay, btnPhoto, mainLay;
    private LinearLayout premiumLay;
    private RelativeLayout btnPremium, premiumBanner;
    private LinearLayout basicInfoLay, locationLay, interestLay;
    private CustomTextView interest, txtInterests;
    private ProgressWheel progress;
    public static AppCompatActivity activity;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String from = "", userId = "", scrollState = "expanded", sendMatch = "", userImage = "";
    ProgressDialog dialog;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    ArrayList<String> interestsAry = new ArrayList<>(), imagesAry = new ArrayList<String>();
    private static boolean imageUploading;
    public static ProgressDialog pd;
    private String profileImage;
    private String peopleForId;
    ArrayList<HashMap<String, String>> interestList = new ArrayList<HashMap<String, String>>();
    private List<String> selectedList = new ArrayList<>();
    private String strInterets;
    private String imagesJson = "";
    Dialog dialogInterest;
    private InterestAdapter interestAdapter;
    TextView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        activity = this;

        findViews();

        from = getIntent().getExtras().getString(Constants.TAG_INTENT_FROM);
        userId = getIntent().getExtras().getString(Constants.TAG_INTENT_FRIEND_ID_PROFILE_PAGE);

        mainLay.setVisibility(View.GONE);
        toolbarTitle.setText(R.string.profile);
        setting.setVisibility(View.GONE);
        premiumLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.spin();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        getProfile();
//        setProfile();
        backbtn.setOnClickListener(this);
        btnPremium.setOnClickListener(this);
        setting.setOnClickListener(this);
        photoLay.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        basicInfoLay.setOnClickListener(this);
        interestLay.setOnClickListener(this);
        locationLay.setOnClickListener(this);

        dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);
    }

    private void findViews() {

        pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        scroll = (ScrollView) findViewById(R.id.scroll);
        profilePic = (RoundedImageView) findViewById(R.id.profile_pic);
        btnPhoto = (RelativeLayout) findViewById(R.id.btnPhoto);
        detailLay = (RelativeLayout) findViewById(R.id.detail_lay);
        photoLay = (RelativeLayout) findViewById(R.id.photoLay);
        info = (CustomTextView) findViewById(R.id.info);
        bio = (CustomTextView) findViewById(R.id.textViewProfileLocation);
        location = (CustomTextView) findViewById(R.id.location);
        premiumLay = (LinearLayout) findViewById(R.id.premiumLay);
        membershipValid = (CustomTextView) findViewById(R.id.membership_valid);
        btnPremium = (RelativeLayout) findViewById(R.id.btnPremium);
        premiumBanner = (RelativeLayout) findViewById(R.id.premiumBanner);
        becomePremium = (CustomTextView) findViewById(R.id.become_premium);
        basicInfoLay = (LinearLayout) findViewById(R.id.basicInfoLay);
        txtBasic = (CustomTextView) findViewById(R.id.txtBasic);
        txtName = (CustomTextView) findViewById(R.id.txtName);
        interestLay = (LinearLayout) findViewById(R.id.interestLay);
        interest = (CustomTextView) findViewById(R.id.interest);
        locationLay = (LinearLayout) findViewById(R.id.locationLay);
        txtLocate = (CustomTextView) findViewById(R.id.txtLocate);
        txtLocation = (CustomTextView) findViewById(R.id.txtLocation);
        mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        progress = (ProgressWheel) findViewById(R.id.progress);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        setting = (ImageView) findViewById(R.id.setting);
        toolbarTitle = (CustomTextView) findViewById(R.id.toolbarTitle);
        txtInterests = (CustomTextView) findViewById(R.id.txtInterests);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.main_content), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void checkUser() {
        if (from.equals("myprofile") || GetSet.getUserId().equals(userId)) {
            userId = GetSet.getUserId();
            setting.setVisibility(View.VISIBLE);
        }
    }

    private void getProfile() {
        progress.setVisibility(View.VISIBLE);
//        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE,
                StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_USER_DETAILS_BY_ID,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "jigar the user profile details we have =" + res);
                            JSONObject json = new JSONObject(res);
                            String strStatus = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (strStatus.equalsIgnoreCase("1")) {
                                JSONObject values = json.getJSONObject(Constants.TAG_INFO);
                         //       sendMatch = DefensiveClass.optString(values, Constants.TAG_SEND_MATCH);

                                profileMap.put(Constants.TAG_USERID, DefensiveClass.optString(values, Constants.TAG_USERID));
                                profileMap.put(Constants.TAG_NEW_USERNAME, DefensiveClass.optString(values, Constants.TAG_NEW_USERNAME));
                                profileMap.put(Constants.TAG_GENDER, DefensiveClass.optString(values, Constants.TAG_GENDER));
                                profileMap.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                profileMap.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                profileMap.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LATITUDE));
                                profileMap.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LONGITUDE));
                                profileMap.put(Constants.TAG_ONLINE, DefensiveClass.optString(values, Constants.TAG_ONLINE));
                                profileMap.put(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
                        //        profileMap.put(Constants.TAG_INFO, DefensiveClass.optString(values, Constants.TAG_INFO));
                                profileMap.put(Constants.TAG_INTEREST, DefensiveClass.optString(values, Constants.TAG_INTEREST));
                                profileMap.put(Constants.TAG_IMAGE, DefensiveClass.optString(values, Constants.TAG_IMAGE));
                     //           profileMap.put(Constants.TAG_IMAGES, DefensiveClass.optString(values, Constants.TAG_IMAGES));
             //                   profileMap.put(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
               //                 profileMap.put(Constants.TAG_SHOW_LOCATION, DefensiveClass.optString(values, Constants.TAG_SHOW_LOCATION));
                 //               profileMap.put(Constants.TAG_INVISIBLE, DefensiveClass.optString(values, Constants.TAG_INVISIBLE));
                   //             profileMap.put(Constants.TAG_REPORT, DefensiveClass.optString(values, Constants.TAG_REPORT));
                     //           profileMap.put(Constants.TAG_PREMIUM_MEMBER, DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER));
                       //         profileMap.put(Constants.TAG_MEMBERSHIP_VALID, DefensiveClass.optString(values, Constants.TAG_MEMBERSHIP_VALID));
                        //        profileMap.put(Constants.TAG_SEND_MATCH, sendMatch);
                                profileMap.put(Constants.TAG_PEOPLE_FOR, DefensiveClass.optString(values, Constants.TAG_PEOPLE_FOR));

                                Log.v(TAG, "jigar the json profile mpa have  in user profile details we have =" + profileMap);

                                progress.setVisibility(View.GONE);
                                progress.stopSpinning();
                                mainLay.setVisibility(View.VISIBLE);
                               // checkUser();
                                setProfile();

                            } else if (strStatus.equalsIgnoreCase("0")) {
                                CommonFunctions.disabledialog(ProfileActivity.this, "Error", json.getString("message"));
                            }
                        } catch (JSONException e) {
                            Log.v(TAG, "jigar the json error  user profile details we have =" + e);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.v(TAG, "jigar the null pointer user profile details we have =" + res);
                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.v(TAG, "jigar the exception user profile details we have =" + res);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
        Log.v(TAG, "jigar the volley exception user profile details we have =" + error);
                VolleyLog.d(TAG, "jigar the volley error in user profile details we have =" + error);
                error.printStackTrace();

            }

        }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
          //      if (GetSet.getUserId().equals(userId)) {

                    map.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
           //         map.put(Constants.TAG_FRIEND_ID, userId);
            //    }
//                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "jigar the user profile parameters " + map);

                return map;
            }

        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void setProfile() {
        try {
            peopleForId = profileMap.get(Constants.TAG_PEOPLE_FOR);
            info.setVisibility(View.VISIBLE);

            info.setText(profileMap.get(Constants.TAG_NEW_USERNAME) + ", " + profileMap.get(Constants.TAG_AGE));

//        info.setText(pref.getString(Constants.TAG_USERNAME,"")+ ", " + pref.getString(Constants.TAG_AGE,""));
            // Removed for testing
            //        info.setVisibility(View.GONE);
            txtName.setText(profileMap.get(Constants.TAG_NEW_USERNAME));
            //      txtName.setText(pref.getString(Constants.TAG_USERNAME,""));

            if (profileMap.get(Constants.TAG_BIO).equals("")) {
                bio.setVisibility(View.GONE);
            } else {
                bio.setText(Html.fromHtml(profileMap.get(Constants.TAG_BIO)));
                bio.setVisibility(View.GONE);
            }
            Log.d(TAG,"jigar the profile image url is "+profileMap.get(Constants.TAG_IMAGE));
            Picasso.with(getApplicationContext())
                    .load(profileMap.get(Constants.TAG_IMAGE))
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .into(profilePic);

//            if (profileMap.get(Constants.TAG_SHOW_LOCATION).equals("true") && !profileMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())) {
//                location.setVisibility(View.GONE);
//            } else {
//                location.setText("Lives in " + profileMap.get(Constants.TAG_LOCATION));
//                txtLocation.setText("Lives in " + profileMap.get(Constants.TAG_LOCATION));
//                location.setVisibility(View.GONE);
//            }

//            if (!profileMap.get(Constants.TAG_INTEREST).equals("")) {
//                try {
//                    JSONArray ints = new JSONArray(profileMap.get(Constants.TAG_INTEREST));
//                    selectedList = new ArrayList<>();
//                    for (int i = 0; i < ints.length(); i++) {
//                        selectedList.add(ints.optString(i, ""));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (NullPointerException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

//            interest.setText(selectedList.size() + " interest");
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (int i = 0; i < selectedList.size(); i++) {
//                stringBuilder.append(selectedList.get(i));
//                stringBuilder.append(",");
//            }
//            if (stringBuilder.length() > 1)
//                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//            txtInterests.setText("" + stringBuilder);
//
//            if (!profileMap.get(Constants.TAG_IMAGE).equals("")) {
//                try {
//                    JSONArray imgs = new JSONArray(profileMap.get(Constants.TAG_IMAGE));
//                    for (int i = 0; i < imgs.length(); i++) {
//                        imagesAry.add(imgs.optString(i, ""));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (NullPointerException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (imagesAry.size() == 0) {
//                imagesAry.add(profileMap.get(Constants.TAG_IMAGE));
//            }
//
//
//
//            premiumLay.setVisibility(View.VISIBLE);
//            if (profileMap.get(Constants.TAG_PREMIUM_MEMBER).equals("true") && profileMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())) {
//                btnPremium.setVisibility(View.GONE);
//                premiumBanner.setVisibility(View.VISIBLE);
//                membershipValid.setVisibility(View.VISIBLE);
//                try {
//                    if (!profileMap.get(Constants.TAG_MEMBERSHIP_VALID).equals(null) && !profileMap.get(Constants.TAG_MEMBERSHIP_VALID).equals("")) {
//                        long date = Long.parseLong(profileMap.get(Constants.TAG_MEMBERSHIP_VALID)) * 1000;
//                        membershipValid.setText(getString(R.string.your_premium_end_on) + " " + getDate(date));
//                    }
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                btnPremium.setVisibility(View.VISIBLE);
//                premiumBanner.setVisibility(View.GONE);
//                membershipValid.setVisibility(View.GONE);
//            }
        }catch (Exception ex)
        {
            Log.d(TAG,"jigar the error in setting profile is "+ex);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting:
                Intent j = new Intent(ProfileActivity.this, Settings.class);
                startActivity(j);
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.btnPremium:
                Intent m = new Intent(ProfileActivity.this, PremiumDialog.class);
                startActivity(m);
                break;
            case R.id.btnPhoto:
            case R.id.photoLay:
                if (from.equals("myprofile") || GetSet.getUserId().equals(userId)) {
                    Intent i = new Intent(ProfileActivity.this, EditPhoto.class);
                    i.putExtra("data", profileMap);
                    i.putExtra("images", imagesAry);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(ProfileActivity.this, ViewImage.class);
                    intent.putExtra("images", imagesAry);
                    intent.putExtra("position", 0);
                    Pair<View, String> bodyPair = Pair.create(view, "fromProfile");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this, bodyPair);
                    ActivityCompat.startActivity(ProfileActivity.this, intent, options.toBundle());
                }
                break;
            case R.id.basicInfoLay:
//                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
//                intent.putExtra("strFriendID", userId);
//                startActivity(intent);
                break;
            case R.id.interestLay:
//                openInterestDialog();
                break;
            case R.id.locationLay:
//                if (profileMap.get(Constants.TAG_PREMIUM_MEMBER).equals("true") && profileMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())) {
//                    Intent i = new Intent(ProfileActivity.this, LocationActivity.class);
//                    i.putExtra("isFrom", "myProfile");
//                    startActivity(i);
//                } else {
//                    Intent p = new Intent(ProfileActivity.this, PremiumDialog.class);
//                    startActivity(p);
//                }
                break;
        }
    }

    private void visitProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_VISIT_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "visitProfileRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(ProfileActivity.this, "Error", json.getString("message"));
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
                map.put(Constants.TAG_VISIT_USER_ID, userId);
                Log.v(TAG, "visitProfileParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    private void openInterestDialog() {

        ImageView btnSearch, btnCancel;
        ProgressWheel progress;
        Display display;
        RecyclerView recyclerView;
        EditText edtSearch;

        dialogInterest = new Dialog(ProfileActivity.this);
        dialogInterest.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogInterest.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogInterest.setContentView(R.layout.dialog_interest);
        dialogInterest.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogInterest.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        dialogInterest.setCancelable(true);


        recyclerView = dialogInterest.findViewById(R.id.recyclerView);
        edtSearch = dialogInterest.findViewById(R.id.edtSearch);
        btnNext = dialogInterest.findViewById(R.id.btnNext);
        btnCancel = dialogInterest.findViewById(R.id.btnCancel);
        btnSearch = dialogInterest.findViewById(R.id.btnSearch);
        progress = dialogInterest.findViewById(R.id.interestProgress);


    //    btnNext.setText(R.string.save);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
                if (HowzuApplication.isNetworkAvailable(ProfileActivity.this)) {
                    if (selectedList.size() > 0)
                        updateProfile();
                    else
                        Toast.makeText(getApplicationContext(), "Select anyone interest", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        progress.setVisibility(View.VISIBLE);
        progress.spin();
        interestAdapter = new InterestAdapter(getApplicationContext(), interestList);
        recyclerView.setAdapter(interestAdapter);
        interestAdapter.notifyDataSetChanged();

//
//        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String res) {
//                        try {
//                            interestList = new ArrayList<>();
//                            Log.v(TAG, "getSettingsRes=" + res);
//                            JSONObject response = new JSONObject(res);
//                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
//                                JSONObject result = response.optJSONObject(Constants.TAG_RESULT);
//                                JSONArray interest = result.getJSONArray(Constants.TAG_INTERESTS);
//
//                                for (int i = 0; i < interest.length(); i++) {
//                                    JSONObject temp = interest.getJSONObject(i);
//                                    HashMap<String, String> map = new HashMap<String, String>();
//                                    map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
//                                    map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
//                                    interestList.add(map);
//                                }
//
//                                interestAdapter = new InterestAdapter(getApplicationContext(), interestList);
//                                recyclerView.setAdapter(interestAdapter);
//                                interestAdapter.notifyDataSetChanged();
//                                progress.setVisibility(View.GONE);
//                                progress.stopSpinning();
//                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
//                                CommonFunctions.disabledialog(getApplicationContext(), "Error", response.getString("message"));
//                            } else {
//                                progress.setVisibility(View.GONE);
//                                progress.stopSpinning();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//            }
//
//        }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, GetSet.getUserId());
//                Log.v(TAG, "getSettingsParams=" + map);
//                return map;
//            }
//        };
//        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        //dialogInterest.show();

    }

    public void updateProfile() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        try {
                            Log.v(TAG, "saveSettingsRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                editor.putString(Constants.TAG_PEOPLE_FOR, peopleForId);
                                editor.putString(Constants.TAG_INTEREST, strInterets);
                                editor.commit();
                                if (dialogInterest != null) {
                                    dialogInterest.dismiss();
                                    getProfile();
                                }
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getApplicationContext(), "Error", json.getString("message"));
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
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
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
                JSONArray intjson = new JSONArray(selectedList);
                strInterets = String.valueOf(intjson);
                map.put(Constants.TAG_INTERESTS, strInterets);
                Log.v(TAG, "saveSettingsParams=" + map);
                return map;
            }
        };

        if (!pd.isShowing()) {
            pd.show();
        }
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
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
                holder.txtInterest.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            } else {
                holder.btnInterest.setChecked(false);
                holder.txtInterest.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
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
                txtInterest.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            } else {
                selectedList.remove(Items.get(position).get(Constants.TAG_NAME));
                txtInterest.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
            }
            if (btnNext != null) {
                btnNext.setVisibility(View.VISIBLE);
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

}
