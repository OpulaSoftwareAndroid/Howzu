package com.hitasoft.app.howzu;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.FontCache;
import com.hitasoft.app.customclass.LaybelLayout;
import com.hitasoft.app.customclass.RoundedImageView;
import com.hitasoft.app.model.MatchMackerDetailModel;
import com.hitasoft.app.model.MyMembersModel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.webservice.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

import static com.hitasoft.app.utils.Constants.TAG_IMAGE;

public class MatchMakerFragment extends Fragment implements View.OnClickListener {
    String TAG = "MatchMakerFragment";
    ViewPager viewPager;
    LinearLayout sliderDotspanel, lin_without_matchmaker, lin_one, lin_two, lin_three, lin_matchmaker_comment, lin_dating, lin_comments;
    private int dotscount;
    private ImageView[] dots;
    LaybelLayout laybelLayout;
    boolean loading = false;
    ArrayList<String> interestsAry = new ArrayList<>();
    RecyclerView recycler_my_members, recycler_without_matchmaker;
    List<MatchMackerDetailModel.Matchmakermember> myMembersList;
    List<MatchMackerDetailModel.Nomatchmakermember> matchmakerMembersList;
    ArrayList<String> imgList;
    MyMemberAdapter myMemberAdapter;
    NotMatchMakerMemberAdapter matchmakerMemberAdapter;
    ViewPagerAdapter viewPagerAdapter;
    HashMap<String, String> haspMapProfileDetails = new HashMap<String, String>();

    CustomTextView help_more;
    TextView txtDinner, txtVideo, txtAsk, txtname, txtStudy, txtAddress;
    ImageView imgAsk, imgVideo, imgDinner,imageViewMatchMakerProfile;
    BottomSheetDialog dialogMenu;
    String selectedPosition = "";
    GridView grid_view;
    String strFriendID,strUserID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.home_matchmaker_layout, container, false);

        initViews(view);


        if(getArguments()!=null) {
           strFriendID = getArguments().getString(Constants.TAG_FRIEND_ID);
           strUserID = getArguments().getString(Constants.TAG_REGISTERED_ID);
        }

        strUserID=GetSet.getUserId();
        strFriendID=GetSet.getFriendId();

        System.out.println("jigar the friend id when enter to matchmaker fragment "+strFriendID);
        System.out.println("jigar the user id when enter to matchmaker fragment "+strUserID);

        //Interests
        interestsAry.add("gaming");
        interestsAry.add("Foodie");
        interestsAry.add("Bike Riding");
        interestsAry.add("Books");
        for (int i = 0; i < interestsAry.size(); i++) {
            TextView tv = new TextView(getContext());
            tv.setText(interestsAry.get(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTypeface(FontCache.get("font_regular.ttf", getContext()));
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            lp.leftMargin = HowzuApplication.dpToPx(getContext(), 5);
            lp.rightMargin = HowzuApplication.dpToPx(getContext(), 5);
            lp.bottomMargin = HowzuApplication.dpToPx(getContext(), 5);
            lp.topMargin = HowzuApplication.dpToPx(getContext(), 5);
            tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_rounded_stroke_corner));
            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
            laybelLayout.addView(tv, lp);
        }

        imgList = new ArrayList<>();

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recycler_my_members.setLayoutManager(horizontalLayoutManagaer);

        LinearLayoutManager horizontalLayoutManagaer1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recycler_without_matchmaker.setLayoutManager(horizontalLayoutManagaer1);


        getMatchMakerDetails();
//        getMatchMakerProfile
        help_more.setOnClickListener(this);
        lin_one.setOnClickListener(this);
        lin_two.setOnClickListener(this);
        lin_three.setOnClickListener(this);

        return view;
    }
    public ImageView getImageViewMatchMaker(ImageView imageViewMatchMaker){

        return imageViewMatchMaker;
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        sliderDotspanel = view.findViewById(R.id.SliderDots);
        laybelLayout = view.findViewById(R.id.laybel);
        recycler_my_members = view.findViewById(R.id.recycler_my_members);
        recycler_without_matchmaker = view.findViewById(R.id.recycler_without_matchmaker);
        help_more = view.findViewById(R.id.help_more);
        imageViewMatchMakerProfile=view.findViewById(R.id.imageViewMatchMakerProfile);

                lin_without_matchmaker = view.findViewById(R.id.lin_without_matchmaker);
        lin_one = view.findViewById(R.id.lin_one);
        lin_two = view.findViewById(R.id.lin_two);
        lin_three = view.findViewById(R.id.lin_three);
        lin_matchmaker_comment = view.findViewById(R.id.lin_matchmaker_comment);

        txtDinner = view.findViewById(R.id.txtDinner);
        txtVideo = view.findViewById(R.id.txtVideo);
        txtAsk = view.findViewById(R.id.txtAsk);
        imgAsk = view.findViewById(R.id.imgAsk);
        imgVideo = view.findViewById(R.id.imgVideo);
        imgDinner = view.findViewById(R.id.imgDinner);

        txtname = view.findViewById(R.id.txtname);
        txtStudy = view.findViewById(R.id.txtStudy);
        txtAddress = view.findViewById(R.id.txtAddress);
        lin_dating = view.findViewById(R.id.lin_dating);
        lin_comments = view.findViewById(R.id.lin_comments);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainScreenActivity.setToolBar(getActivity(), "menuItemMatchMaker");
    }

    public void getMatchMakerDetails() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
            CommonFunctions.showProgressDialog(getContext());
            System.out.println("jigar the received fragment friend id  is "+GetSet.getFriendId());
            RequestBody registerId = RequestBody.create(MediaType.parse("text/plain"), strUserID);
            RequestBody friendID = RequestBody.create(MediaType.parse("text/plain"),strFriendID);

            new RestClient(getContext()).getInstance().get().getgirlprofiledetail(registerId, friendID).enqueue(new Callback<MatchMackerDetailModel>() {
                @Override
                public void onResponse(Call<MatchMackerDetailModel> call, retrofit2.Response<MatchMackerDetailModel> response) {
                    CommonFunctions.hideProgressDialog(getContext());
                    if (response.body() != null) {
                        if (response.body().getStatus() == 1) {
                            //Mymember
                            myMembersList = response.body().getInfo().getMatchmakermember();
                            myMemberAdapter = new MyMemberAdapter(getActivity(), myMembersList);
                            recycler_my_members.setAdapter(myMemberAdapter);

                            //Mot Match maker member
                            matchmakerMembersList = response.body().getInfo().getNomatchmakermember();

                            imgList.add(response.body().getInfo().getUser().getImage());
                            viewPagerAdapter = new ViewPagerAdapter(getContext(), imgList);

                            viewPager.setAdapter(viewPagerAdapter);

                            txtname.setText(response.body().getInfo().getUser().getFirstname() + " , " + response.body().getInfo().getUser().getAge());
                            txtAddress.setText(response.body().getInfo().getUser().getBio());

//                            if (response.body().getInfo().getMatchmaker() != null) {
//                                lin_matchmaker_comment.setVisibility(View.VISIBLE);
//                            } else {
//                                lin_matchmaker_comment.setVisibility(View.GONE);
//                            }

                        } else {
                            Toast.makeText(getContext(), response.body().getMsg(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MatchMackerDetailModel> call, Throwable t) {
                    try {
                        CommonFunctions.hideProgressDialog(getContext());
                        Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        System.out.println("jigar the error on failure fragment friend id  is "+t.getMessage());

                    } catch (Exception e) {
                        System.out.println("jigar the error exception on failure fragment friend id  is "+t.getMessage());

                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void getMatchMakerProfile() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(getContext()))) {
            CommonFunctions.showProgressDialog(getContext());

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
                                    //   haspMapProfileDetails.put(Constants.TAG_MEMBERSHIP_VALID, DefensiveClass.optString(jsonObject, Constants.TAG_MEMBERSHIP_VALID));
                                    //    haspMapProfileDetails.put(Constants.TAG_SEND_MATCH, sendMatch);
                                    haspMapProfileDetails.put(TAG_IMAGE, jsonObjectMainUserInfo.getString(TAG_IMAGE));

                                    CommonFunctions.hideProgressDialog(getContext());

                                    System.out.println("jigar the response on images we have view profile is " + haspMapProfileDetails.get(TAG_IMAGE));

//                                    checkUser();
//                                    setProfile();

                                } else if (strStatus.equals("0")) {
                                    System.out.println("jigar the error in status json response on view profile is " + json.getString(Constants.TAG_MSG));

                                    CommonFunctions.disabledialog(getContext(), "Error", json.getString("message"));
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
                    Toast.makeText(getContext(),R.string.something_went_wrong,Toast.LENGTH_LONG).show();
                    CommonFunctions.hideProgressDialog(getContext());

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
                        map.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());

                        map.put(Constants.TAG_FRIEND_ID, strFriendID);
                    }
                    // else
                    //                    {
                    //                    map.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                    //                    map.put(Constants.TAG_FRIEND_ID, strFriendID);
                    //                }
                    // map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                    Log.v("params", "getProfileParams=" + map);
                    return map;
                }

            };
            HowzuApplication.getInstance().addToRequestQueue(req, TAG);
        }else {
            CommonFunctions.hideProgressDialog(getContext());

            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.help_more:
                help_more.setVisibility(View.GONE);
                lin_without_matchmaker.setVisibility(View.VISIBLE);
                matchmakerMemberAdapter = new NotMatchMakerMemberAdapter(getActivity(), matchmakerMembersList);
                recycler_without_matchmaker.setAdapter(matchmakerMemberAdapter);
                break;

            case R.id.lin_one:

                lin_one.setBackgroundResource(R.drawable.gray_bg);
                lin_two.setBackgroundResource(R.drawable.gray_bg_border);
                lin_three.setBackgroundResource(R.drawable.gray_bg_border);

                imgAsk.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                txtAsk.setTextColor(getResources().getColor(R.color.white));


                imgVideo.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtVideo.setTextColor(getResources().getColor(R.color.colorGray));

                imgDinner.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtDinner.setTextColor(getResources().getColor(R.color.colorGray));


                break;

            case R.id.lin_two:

                lin_two.setBackgroundResource(R.drawable.gray_bg);
                lin_one.setBackgroundResource(R.drawable.gray_bg_border);
                lin_three.setBackgroundResource(R.drawable.gray_bg_border);

                imgAsk.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtAsk.setTextColor(getResources().getColor(R.color.colorGray));


                imgVideo.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                txtVideo.setTextColor(getResources().getColor(R.color.white));

                imgDinner.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtDinner.setTextColor(getResources().getColor(R.color.colorGray));

                View v1 = getLayoutInflater().inflate(R.layout.gift_dialog, null);
                dialogMenu = new BottomSheetDialog(getContext());
                dialogMenu.setContentView(v1);
                dialogMenu.setCancelable(false);

                grid_view = dialogMenu.findViewById(R.id.grid_view);
                ImageView imgClose = dialogMenu.findViewById(R.id.imgClose);
//                GiftViewPagerAdapter giftAdapter = new GiftViewPagerAdapter(getContext(), notificationDetailList);
                assert grid_view != null;
//                grid_view.setAdapter(giftAdapter);

                grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(getActivity(), i + "/", Toast.LENGTH_SHORT).show();
                    }
                });

                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogMenu.dismiss();
                    }
                });

                dialogMenu.show();

                break;

            case R.id.lin_three:

                lin_three.setBackgroundResource(R.drawable.gray_bg);
                lin_two.setBackgroundResource(R.drawable.gray_bg_border);
                lin_one.setBackgroundResource(R.drawable.gray_bg_border);

                imgAsk.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtAsk.setTextColor(getResources().getColor(R.color.colorGray));

                imgVideo.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray));
                txtVideo.setTextColor(getResources().getColor(R.color.colorGray));

                imgDinner.setColorFilter(ContextCompat.getColor(getContext(), R.color.white));
                txtDinner.setTextColor(getResources().getColor(R.color.white));

                break;
        }
    }

    class MyMemberAdapter extends RecyclerView.Adapter<MyMemberAdapter.MyViewHolder> {

        private Context mContext;
        private List<MatchMackerDetailModel.Matchmakermember> albumList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public RoundedImageView profile_pic;


            private boolean fabExpanded = false;
            private Button fabSettings;
            private LinearLayout layoutFabSave;
            private LinearLayout layoutFabEdit;
            private LinearLayout layoutFabPhoto;

            public MyViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                profile_pic = view.findViewById(R.id.profile_pic);


                fabSettings = (Button) view.findViewById(R.id.fabSetting);

                layoutFabSave = (LinearLayout) view.findViewById(R.id.layoutFabSave);
                layoutFabEdit = (LinearLayout) view.findViewById(R.id.layoutFabEdit);
                layoutFabPhoto = (LinearLayout) view.findViewById(R.id.layoutFabPhoto);
                //layoutFabSettings = (LinearLayout) this.findViewById(R.id.layoutFabSettings);

                //When main Fab (Settings) is clicked, it expands if not expanded already.
                //Collapses if main FAB was open already.
                //This gives FAB (Settings) open/close behavior
                fabSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fabExpanded == true) {
                            closeSubMenusFab();
                        } else {
                            openSubMenusFab();
                        }
                    }
                });

                closeSubMenusFab();
            }

            //closes FAB submenus
            private void closeSubMenusFab() {
                layoutFabSave.setVisibility(View.INVISIBLE);
                layoutFabEdit.setVisibility(View.INVISIBLE);
                layoutFabPhoto.setVisibility(View.INVISIBLE);
                fabExpanded = false;
            }

            //Opens FAB submenus
            private void openSubMenusFab() {
                layoutFabSave.setVisibility(View.VISIBLE);
                layoutFabEdit.setVisibility(View.VISIBLE);
                layoutFabPhoto.setVisibility(View.VISIBLE);
                fabExpanded = true;
            }

        }


        public MyMemberAdapter(Context mContext, List<MatchMackerDetailModel.Matchmakermember> albumList) {
            this.mContext = mContext;
            this.albumList = albumList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_member_adapter_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            MatchMackerDetailModel.Matchmakermember album = albumList.get(position);
            try {
                if (album.getName() != null) {
                    holder.title.setText(album.getName());

                }

                if (album.getImage().equalsIgnoreCase("http://www.ilovemisskey.com/uploads/user/")) {
                    Glide.with(getActivity()).load(album.getImage())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            }).placeholder(R.drawable.user_placeholder)
                            .into(holder.profile_pic);
                } else {
                    Glide.with(getActivity()).load(album.getImage())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(holder.profile_pic);
                }

                holder.profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
//                    PopupWindow pw;
//                    try {
//                        //We need to get the instance of the LayoutInflater, use the context of this activity
//                        LayoutInflater inflater = (LayoutInflater) getActivity()
//                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                        //Inflate the view from a predefined XML layout
//                        View layout = inflater.inflate(R.layout.popup,
//                                (ViewGroup) view.findViewById(R.id.popup_element));
//                        // create a 300px width and 470px height PopupWindow
//                        pw = new PopupWindow(layout, 300, 470, true);
//                        // display the popup in the center
////                        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
//
//                        TextView mResultText = (TextView) layout.findViewById(R.id.server_status_text);
//                        Button cancelButton = (Button) layout.findViewById(R.id.end_data_send_button);
////                        cancelButton.setOnClickListener(cancel_button_click_listener);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }


                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getContext(), holder.profile_pic);
                        //inflating menu from xml resource
                        popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());
                        //adding click listener
//                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.menu1:
//                                    //handle menu1 click
//                                    break;
//                                case R.id.menu2:
//                                    //handle menu2 click
//                                    break;
//                                case R.id.menu3:
//                                    //handle menu3 click
//                                    break;
//                            }
//                            return false;
//                        }
//                    });
                        //displaying the popup
                        popup.show();
                        return false;
                    }
                });
            } catch (NullPointerException ex)
            {

            }
        }


        @Override
        public int getItemCount() {
            return albumList.size();
        }


    }

    class NotMatchMakerMemberAdapter extends RecyclerView.Adapter<NotMatchMakerMemberAdapter.MyViewHolder> {

        private Context mContext;
        private List<MatchMackerDetailModel.Nomatchmakermember> albumList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public RoundedImageView profile_pic;


            public MyViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                profile_pic = view.findViewById(R.id.profile_pic);
            }
        }


        public NotMatchMakerMemberAdapter(Context mContext, List<MatchMackerDetailModel.Nomatchmakermember> albumList) {
            this.mContext = mContext;
            this.albumList = albumList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.not_matchmaker_member_adapter_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            MatchMackerDetailModel.Nomatchmakermember album = albumList.get(position);
            holder.title.setText(album.getName());

            if (album.getImage().equalsIgnoreCase("http://www.ilovemisskey.com/uploads/user/")) {
                Glide.with(getActivity()).load(album.getImage())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        }).placeholder(R.drawable.user_placeholder)
                        .into(holder.profile_pic);
            } else {
                Glide.with(getActivity()).load(album.getImage())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.profile_pic);
            }

        }


        @Override
        public int getItemCount() {
            return albumList.size();
        }


    }

    class GiftViewPagerAdapter extends BaseAdapter {
        private Context mContext;
        private List<MyMembersModel> albumList;

        // Constructor
        public GiftViewPagerAdapter(Context c, List<MyMembersModel> albumList) {
            mContext = c;
            this.albumList = albumList;
        }

        @Override
        public int getCount() {
            return albumList.size();
        }

        @Override
        public Object getItem(int position) {
            return albumList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                grid = inflater.inflate(R.layout.gift_adapter_item, null);
                TextView txtprice = grid.findViewById(R.id.txtprice);
                TextView txtName = grid.findViewById(R.id.txtName);
                ImageView giftImg = (ImageView) grid.findViewById(R.id.giftImg);
                RelativeLayout main_lin = grid.findViewById(R.id.main_lin);
                AppCompatCheckBox btnInterest = grid.findViewById(R.id.btnInterest);

                txtName.setText(albumList.get(position).getMemberName());
                main_lin.setTag(new Integer(position));
                main_lin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        if (selectedPosition.equals(view.getTag())) {
//                            main_lin.setBackgroundResource(R.drawable.pink_bg_border);
//                            btnInterest.setVisibility(View.VISIBLE);
//                        } else {
//                            main_lin.setBackgroundColor(Color.parseColor("#fff"));
//                            btnInterest.setVisibility(View.INVISIBLE);
//                        }
                    }
                });
            } else {
                grid = (View) convertView;
            }

            return grid;
        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        private Context context;
        private LayoutInflater layoutInflater;
        private ArrayList<String> img_list;

        public ViewPagerAdapter(Context context, ArrayList<String> img_list) {
            this.context = context;
            this.img_list = img_list;
        }

        @Override
        public int getCount() {
            return img_list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.matchmaker_profile_image_layout, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            if (img_list.get(position).equalsIgnoreCase("http://www.ilovemisskey.com/uploads/user/")) {
                Glide.with(getActivity()).load(img_list.get(position))
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        }).placeholder(R.drawable.user_placeholder)
                        .into(imageView);
            } else {
                Glide.with(getActivity()).load(img_list.get(position))
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imageView);
            }
            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);

            dotscount = viewPagerAdapter.getCount();
            dots = new ImageView[dotscount];

            for (int i = 0; i < dotscount; i++) {

                dots[i] = new ImageView(getContext());
                dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nonactive_dot));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(0, 5, 0, 5);

                sliderDotspanel.addView(dots[i], params);
            }

            dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    for (int i = 0; i < dotscount; i++) {
                        dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nonactive_dot));
                    }
                    dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            ViewPager vp = (ViewPager) container;
            View view = (View) object;
            vp.removeView(view);

        }
    }

    private void getMyMembers() {
        loading = true;
        StringRequest req = new StringRequest(Request.Method.GET, Constants.API_GET_MY_MEMBERS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getChatRes=" + res);
                            System.out.println("jigar the response of matchmaker feature is " + res);


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
                System.out.println("jigar the response of matchmaker feature is " + error);


            }

        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, GetSet.getUserId());
//                map.put(Constants.TAG_CHAT_ID, chatId);
//                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
//                map.put(Constants.TAG_LIMIT, "20");
//                Log.v(TAG, "getChatParams=" + map);
//                return map;
//            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

}
