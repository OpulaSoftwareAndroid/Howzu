package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.customclass.RippleBackground;
import com.hitasoft.app.model.LikedPeopleModel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.webservice.RestClient;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//import okhttp3.Headers;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 27/10/16.
 */

public class FindPeople extends Fragment implements View.OnClickListener {
    String TAG = "FindPeople";

    public static ImageView userImage;
    RecyclerView recyclerView;
    RippleBackground rippleBackground;
    NestedScrollView scrollview;

    RelativeLayout premiumLay;
    LinearLayout  locationLay; //nullLay,
    TextView changeLocation, becomePremium;
    Display display;

    public static Context context;
    public static Handler handler = new Handler();
    public static ArrayList<HashMap<String, String>> peoplesAry = new ArrayList<HashMap<String, String>>();
    StaggeredGridLayoutManager mLayoutManager;
    RecyclerViewAdapter itemAdapter;
    int topPadding = 0, scrollHeight, currentPage = 0, scrollPadding = 15;
    boolean loading = false, premiumClicked = false, loadmore = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "find people: onCreateView");
        return inflater.inflate(R.layout.find_people, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

       // MainScreenActivity.setToolBar(getActivity(), "findpeople");

        becomePremium = getView().findViewById(R.id.become_premium);
        changeLocation = getView().findViewById(R.id.change_location);
        recyclerView = getView().findViewById(R.id.recyclerView);
        userImage = getView().findViewById(R.id.user_image);
        rippleBackground = getView().findViewById(R.id.ripple_background);
        premiumLay = getView().findViewById(R.id.premium_lay);
       // nullLay = getView().findViewById(R.id.nullLay);
        locationLay = getView().findViewById(R.id.locationLay);
        scrollview = getView().findViewById(R.id.scrollview);

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        Date date= new Date();
        long time = date.getTime();
        System.out.println("jigar the time in Milliseconds: " + time);
        changeLocation.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        becomePremium.setOnClickListener(this);

        context = getActivity();
        topPadding = HowzuApplication.dpToPx(context, 60);
        scrollPadding = HowzuApplication.dpToPx(context, 15);
        rippleBackground.startRippleAnimation();
        rippleBackground.setVisibility(View.VISIBLE);
        premiumLay.setVisibility(View.GONE);
    //    nullLay.setVisibility(View.GONE);

        currentPage = 0;
        peoplesAry.clear();
        loading = false;
        loadmore = false;

        display = getActivity().getWindowManager().getDefaultDisplay();
        if (GetSet.isBannerEnable() && !GetSet.isHideAds()) {
            scrollHeight = display.getHeight() - HowzuApplication.dpToPx(context, 145);
        } else {
            scrollHeight = display.getHeight() - HowzuApplication.dpToPx(context, 95);
        }

        Log.d(TAG,"jigar the user image for find people is "+Constants.pref.getString("user_image", null));
        Picasso.with(getActivity())
//                .load(GetSet.getImageUrl())
                .load(Constants.pref.getString("user_image", null))
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .into(userImage);
        setAdapter();

        recyclerView.setNestedScrollingEnabled(false);
        //getPeople1();
    }

    @Override
    public void onResume() {
        super.onResume();
        premiumClicked = false;
        if (itemAdapter != null) {
            peoplesAry.clear();
            itemAdapter.notifyDataSetChanged();
            rippleBackground.startRippleAnimation();
            rippleBackground.setVisibility(View.VISIBLE);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPeople();
//                getFindPeopleList();
                //getPeople1();
            }
        }, 3000);
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(false);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);

        itemAdapter = new RecyclerViewAdapter(peoplesAry);
        recyclerView.setAdapter(itemAdapter);
        scrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    //  Log.i(TAG, "Scroll DOWN");
                }
                if (scrollY < oldScrollY) {
                    //  Log.i(TAG, "Scroll UP");
                }

                if (scrollY == 0) {
                    //  Log.i(TAG, "TOP SCROLL");
                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    if (loadmore && !loading) {
                        loading = true;
                        currentPage++;
                        getPeople();
//                        getPeople1();
                        //getFindPeopleList();
                    }
                    Log.v(TAG, "END Reached=" + currentPage);
                }
            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private static final int TYPE_ITEM = 1;
        private static final int TYPE_FOOTER = 2;
        ArrayList<HashMap<String, String>> peopleList;
        private boolean showLoader;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> peopleList) {
            this.peopleList = peopleList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.people_item_layout, parent, false);
                return new MyViewHolder(itemView);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loader, parent, false);
                return new FooterViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);
            if (itemType == TYPE_ITEM) {
                HashMap<String, String> tempMap = peopleList.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

                if (position == 0 || position == 2) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.singleImage.getLayoutParams();
                    params.setMargins(0, topPadding, 0, 0);
                    viewHolder.singleImage.setLayoutParams(params);
                }

                viewHolder.userName.setText(tempMap.get(Constants.TAG_USERNAME));
                String img = Constants.RESIZE_URL + CommonFunctions.getImageName(tempMap.get(Constants.TAG_USERIMAGE)) + Constants.IMAGE_RES;
                Picasso.with(context)
                        .load(img)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                        .centerCrop()
                        .into(viewHolder.singleImage);

                if (tempMap.get(Constants.TAG_ONLINE).equals("1")) {
                    viewHolder.online.setVisibility(View.VISIBLE);
                    viewHolder.online.setImageResource(R.drawable.online);
                } else if (tempMap.get(Constants.TAG_ONLINE).equals("2")) {
                    viewHolder.online.setVisibility(View.VISIBLE);
                    viewHolder.online.setImageResource(R.drawable.away);
                } else {
                    viewHolder.online.setVisibility(View.GONE);
                }
            } else {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) footerHolder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
                layoutParams.setMargins(0, scrollPadding, 0, scrollPadding);
                if (showLoader) {
                    footerHolder.progress.setVisibility(View.VISIBLE);
                    footerHolder.progress.spin();
                } else {
                    footerHolder.progress.setVisibility(View.GONE);
                    footerHolder.progress.stopSpinning();
                }
            }
        }

        public void showLoading(boolean status) {
            showLoader = status;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionFooter(position)) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        private boolean isPositionFooter(int position) {
            return position == peopleList.size();
        }

        @Override
        public int getItemCount() {
            if (peopleList.size() == 0)
                return 0;
            else
                return peopleList.size() + 1;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage, online;
            TextView userName;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.user_image);
                userName = view.findViewById(R.id.user_name);
                online = (ImageView) view.findViewById(R.id.online);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.user_image:
                        Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                        p.putExtra("from", "home");
                        // here friend id means user own id
                        // and register id means friend id whos profile we are visiting
                        p.putExtra(Constants.TAG_FRIEND_ID, GetSet.getUseridLikeToken());
                        p.putExtra(Constants.TAG_PROFILE_VISITOR_ID_LIKE_TOKEN,  peopleList.get(getAdapterPosition()).get(Constants.TAG_USERID));
                        p.putExtra(Constants.TAG_REGISTERED_ID, peopleList.get(getAdapterPosition()).get(Constants.TAG_REGISTERED_ID));

                        p.putExtra("from", "other");
                        p.putExtra("strFriendID", peopleList.get(getAdapterPosition()).get(Constants.TAG_USERID));
                        startActivity(p);
                        break;
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            ProgressWheel progress;

            public FooterViewHolder(View parent) {
                super(parent);
                progress = (ProgressWheel) parent.findViewById(R.id.footer_progress);
            }

        }
    }


//    public void getFindPeopleList() {
//        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
//            CommonFunctions.showProgressDialog(getContext());
//            RequestBody aregId = RequestBody.create(MediaType.parse("text/plain"), GetSet.getUserId());
//            new RestClient(getContext()).getInstance().get().getLikedPerson(aregId).enqueue(new Callback<LikedPeopleModel>() {
//                @Override
//                public void onResponse(Call<LikedPeopleModel> call, retrofit2.Response<LikedPeopleModel> response) {
//                    CommonFunctions.hideProgressDialog(getContext());
//                    if (response.body() != null) {
//                        if (response.body().getStatus() == 1) {
////                            peopleList = (ArrayList<LikedPeopleModel.Info>) response.body().getInfo();
////                            infiniteAdapter = InfiniteScrollAdapter.wrap(new LikedFragment.LikedPeopleAdapter(peopleList));
////                            itemPicker.setAdapter(infiniteAdapter);
//                        } else {
//                            Toast.makeText(getContext(), response.body().getMsg(), Toast.LENGTH_LONG).show();
//                        }
//                    } else {
//                        Toast.makeText(getContext(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<LikedPeopleModel> call, Throwable t) {
//                    try {
//                        CommonFunctions.hideProgressDialog(getContext());
//                        Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//
//            });
//        } else {
//            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//        }
//    }

    public void getPeople() {
        loading = true;
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (peoplesAry.size() >= 20) {
                    itemAdapter.showLoading(true);
                    itemAdapter.notifyDataSetChanged();
                } else {
                    itemAdapter.showLoading(false);
                }
            }
        });

        StringRequest getPeople = new StringRequest(Request.Method.POST, Constants.API_GET_PEOPLE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, res);
                        System.out.println("jigar the response in find people is have is " + res);

                        try {
                            JSONObject response = new JSONObject(res);
                            String status = DefensiveClass.optString(response, Constants.TAG_STATUS);
                            if (status.equalsIgnoreCase("true")) {
                                JSONArray peoples = response.optJSONArray(Constants.TAG_PEOPLES);
                                for (int i = 0; i < peoples.length(); i++) {
                                    JSONObject values = peoples.optJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(Constants.TAG_USERID, DefensiveClass.optInt(values, Constants.TAG_USERID));
                                    map.put(Constants.TAG_REGISTERED_ID, DefensiveClass.optInt(values, Constants.TAG_REGISTERED_ID));
                                    map.put(Constants.TAG_USERNAME, DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                    map.put(Constants.TAG_SEND_MATCH, DefensiveClass.optString(values, Constants.TAG_SEND_MATCH));
                                    map.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                    map.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                    map.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
                                    map.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
                                    map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                    map.put(Constants.TAG_ONLINE, DefensiveClass.optInt(values, Constants.TAG_ONLINE));
                                    peoplesAry.add(map);
                                }

                            } else if (status.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", response.getString("message"));
                            } else {

                            }

                            Log.v(TAG, "peoplesArySize=" + peoplesAry.size());

                            if (peoplesAry.size() > 0) {
                                itemAdapter.showLoading(false);
                                itemAdapter.notifyDataSetChanged();
                                rippleBackground.stopRippleAnimation();
                                rippleBackground.setVisibility(View.GONE);
                                //    nullLay.setVisibility(View.GONE);
                                scrollview.setVisibility(View.VISIBLE);

                                if (GetSet.isPremium()) {
                                    premiumLay.setVisibility(View.GONE);
                                    if (peoplesAry.size() >= 20 && status.equalsIgnoreCase("true")) {
                                        loadmore = true;
                                    } else {
                                        loadmore = false;
                                    }
                                } else {
                                    loadmore = false;
                                    if (peoplesAry.size() < 20) {
                                        premiumLay.setVisibility(View.VISIBLE);
                                        recyclerView.getLayoutParams().height = scrollHeight;
                                    } else {
                                        premiumLay.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                loadmore = false;
                                //         nullLay.setVisibility(View.VISIBLE);
                                scrollview.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        loading = false;
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                System.out.println("jigar the error in find people is have is " + error.getMessage());

                itemAdapter.showLoading(false);
                itemAdapter.notifyDataSetChanged();
                if (peoplesAry.size() > 0) {
                    //    line.setVisibility(View.GONE);
                    scrollview.setVisibility(View.VISIBLE);
                } else {
                    //        nullLay.setVisibility(View.VISIBLE);
                    scrollview.setVisibility(View.GONE);
                }
                loadmore = false;
                loading = false;
            }
        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.v(TAG, "getPeopleParams auth =" + map);
//                System.out.println("jigar the find people auth have is " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_USERID, pref.getString(Constants.TAG_TOKEN_LIKE_USER_ID,""));
                params.put(Constants.TAG_USERID, "4dc61be0-2dca-11e9-9304-2900178afb99");

                //      params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
//                params.put(Constants.TAG_OFFSET, String.valueOf(currentPage * 20));
//                params.put(Constants.TAG_LIMIT, "20");
                params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "getPeopleParams=" + params);
                System.out.println("jigar the find people param have is " + params);

                return params;
            }

        };

        HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
        HowzuApplication.getInstance().addToRequestQueue(getPeople, TAG);

    }

//
//        Interceptor interceptor = new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                okhttp3.Request newRequest = chain.request().newBuilder().addHeader("User-Agent", "Retrofit-Sample-App").build();
//                return chain.proceed(newRequest);
//            }
//        };
//
//// Add the interceptor to OkHttpClient
//       // OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//
//// Can be Level.BASIC, Level.HEADERS, or Level.BODY
//// See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
////        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
////        builder.networkInterceptors().add(httpLoggingInterceptor);
////        builder.build();
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.interceptors().add(interceptor);
//        OkHttpClient client = builder.build();
//
//// Set the custom client when building adapter
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.github.com")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build();//    public void getPeople1()
//    {
//        loading = true;
//        new Handler().post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (peoplesAry.size() >= 20) {
//                    itemAdapter.showLoading(true);
//                    itemAdapter.notifyDataSetChanged();
//                } else {
//                    itemAdapter.showLoading(false);
//                }
//            }
//        });
//        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
//
//            CommonFunctions.showProgressDialog(getContext());
//
//            OkHttpClient client = new OkHttpClient();
//
//
//            System.out.println("jigar the before response user id is " + GetSet.getUserId());
//            System.out.println("jigar the before response token is " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
//
//
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart(Constants.TAG_REGISTERED_ID, GetSet.getUserId())
//                    .build();
//            Request request = new Request.Builder()
//                    //.header("Authorization", pref.getString(Constants.TAG_AUTHORIZATION, ""))
//                    .post(requestBody)
//                    .url(Constants.API_GET_PROFILE)
//                    .build();
//
//
//            // Response response = client.newCall(request).execute();
//            client.newCall(request).enqueue(new okhttp3.Callback() {
//                @Override
//                public void onFailure(okhttp3.Call call, IOException e) {
//
//                    System.out.println("jigar the error failure in response we have is " + e);
//
//
//                }
//
//                @Override
//                public void onResponse(okhttp3.Call call, Response response) throws IOException {
//                    final String responseData = response.body().string();
//                    Headers responseHeaders = response.headers();
//                    for (int i = 0; i < responseHeaders.size(); i++) {
//                        Log.d("DEBUG", responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }
//                    if (!response.isSuccessful()) {
//                        throw new IOException("jigar Unexpected code " + response);
////                        System.out.println("jigar the response we have is " + response.body().toString());
//                    }
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            CommonFunctions.hideProgressDialog(getContext());
//                            System.out.println("jigar the response we have is " + responseData);
//                            setFindPeoplePageData(responseData);
//
//
//                        }
//                    });
//                }
//            });
//        }else {
//            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//        }
//        CommonFunctions.hideProgressDialog(getContext());
//
//    }
//


    public void setFindPeoplePageData(String strResponse)
    {
        try {
            JSONObject jsonObject=new JSONObject(strResponse);
            String strStatus=jsonObject.getString(Constants.TAG_STATUS);
            String strMessage=jsonObject.getString(Constants.TAG_MSG);

            if(strStatus.equals("1") || strMessage.equals(Constants.TAG_STATUS)) {


                JSONObject jsonObjectInfo=jsonObject.getJSONObject(Constants.TAG_INFO);
                JSONObject jsonObjectMember=jsonObjectInfo.getJSONObject(Constants.TAG_MEMBER);

                for(int i=0;i<jsonObjectMember.length();i++)
                {
                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject jsonObjectMemberMainInfo=jsonObjectMember.getJSONObject(""+i+"");

                    String strMemberName =jsonObjectMemberMainInfo.getString(Constants.TAG_NAME);
                    String strMemberRegisterID =jsonObjectMemberMainInfo.getString(Constants.TAG_REGISTERED_ID);
                    String strMemberAge =jsonObjectMemberMainInfo.getString(Constants.TAG_AGE);
                    System.out.println("jigar the member name is "+strMemberName);
                    map.put(Constants.TAG_REGISTERED_ID, strMemberRegisterID);
                    map.put(Constants.TAG_NAME, strMemberName);
                    map.put(Constants.TAG_AGE, strMemberAge);
                    map.put(Constants.TAG_IMAGE, strMemberAge);
                    peoplesAry.add(map);
                }

//                for (int i = 0; i < jsonObject)
//                    HashMap<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, DefensiveClass.optInt(values, Constants.TAG_USERID));
            }
        }catch (JSONException ex)
        {
            System.out.println("jigar the error in json is "+ex);
        }

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.locationLay:
            case R.id.change_location:
                if (GetSet.isPremium()) {
                    HashMap<String, String> fbdata = new HashMap<>();
                    Intent i = new Intent(getActivity(), LocationActivity.class);
                    i.putExtra("data", fbdata);
                    i.putExtra("isFrom","findPeople");
                    startActivity(i);
                } else {
                    if (!premiumClicked) {
                        premiumClicked = true;
                        Intent m = new Intent(getActivity(), PremiumDialog.class);
                        startActivity(m);
                    }
                }
                break;
            case R.id.become_premium:
                if (!premiumClicked) {
                    premiumClicked = true;
                    Intent m = new Intent(getActivity(), PremiumDialog.class);
                    startActivity(m);
                }
                break;
        }
    }
}
