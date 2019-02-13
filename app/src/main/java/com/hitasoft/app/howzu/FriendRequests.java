package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.customclass.RoundedImageView;
import com.hitasoft.app.model.POJONotificationDetails;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 27/10/16.
 */

public class FriendRequests extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String TAG = "FriendRequests";

    RecyclerView recyclerView;
    GridLayoutManager mLayoutManager;
    LinearLayout nullLay;
    ProgressWheel progress;
    SwipeRefreshLayout swipeLayout;

    public static Context context;
//    RecyclerViewAdapter itemAdapter;
    FriendRequestAdapter friendRequestAdapter;
    int currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount, visibleThreshold = 5, previousTotal = 0;
//    ArrayList<HashMap<String, String>> peoplesAry = new ArrayList<HashMap<String, String>>();
    ArrayList<POJONotificationDetails> peoplesAry = new ArrayList<POJONotificationDetails>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    boolean loading = true, loadmore = false, pulldown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request, container, false);
        Log.i(TAG, "FriendRequests: onCreateView");

        progress = view.findViewById(R.id.progress);
        nullLay = view.findViewById(R.id.nullLay);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeLayout = view.findViewById(R.id.swipeLayout);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainScreenActivity.setToolBar(getActivity(), "requests");

        context = getActivity();
        currentPage = 0;
        previousTotal = 0;
        peoplesAry.clear();
        setAdapter();
        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(this);
        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        getFriendRequest();
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(context, 1);
        recyclerView.setLayoutManager(mLayoutManager);

//        itemAdapter = new RecyclerViewAdapter(peoplesAry);
//        recyclerView.setAdapter(itemAdapter);
        friendRequestAdapter = new FriendRequestAdapter(getContext(),peoplesAry);
        recyclerView.setAdapter(friendRequestAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loadmore) {
                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        Log.v(TAG, "END Reached=" + currentPage);
                        getFriendRequest();
                        loading = true;
                    }
                }
            }
        });

//        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return itemAdapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
//            }
//        });
    }

    private void swipeRefresh(final boolean refresh) {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                if (refresh) {
                    swipeLayout.setRefreshing(true);
                } else {
                    swipeLayout.setRefreshing(false);
                }

            }
        });
    }

    @Override
    public void onRefresh() {
        Log.v(TAG, "onRefresh=" + swipeLayout.isRefreshing());
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            nullLay.setVisibility(View.GONE);
            getFriendRequest();
        }
    }

    class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.MyViewHolder> {

        private Context mContext;
        private List<POJONotificationDetails> notificationDetailsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewNotificationSenderName,textViewNotificationMessage,textViewNotificationTime;
            LinearLayout linearLayoutAcceptDecline;
            public RoundedImageView imageViewSenderProfileImage;
            ImageView imageViewAcceptRequest,imageViewDeclineRequest;
            public MyViewHolder(View view) {
                super(view);
                textViewNotificationSenderName = view.findViewById(R.id.textViewNotificationSenderName);
                imageViewSenderProfileImage = view.findViewById(R.id.imageViewSenderProfileImage);
                textViewNotificationMessage = view.findViewById(R.id.textViewNotificationMessage);
                textViewNotificationTime = view.findViewById(R.id.textViewNotificationTime);
                linearLayoutAcceptDecline=view.findViewById(R.id.linearLayoutAcceptDecline);
                imageViewDeclineRequest=view.findViewById(R.id.imageViewDeclineRequest);
                imageViewAcceptRequest=view.findViewById(R.id.imageViewAcceptRequest);

            }
        }

        public FriendRequestAdapter(Context mContext, List<POJONotificationDetails> notificationDetailsList) {
            this.mContext = mContext;
            this.notificationDetailsList = notificationDetailsList;
        }

        @Override
        public FriendRequestAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_request_adapter_item, parent, false);

            return new FriendRequestAdapter.MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            POJONotificationDetails album = notificationDetailsList.get(position);
            holder.textViewNotificationSenderName.setText(album.getNotificationName());
            holder.textViewNotificationMessage.setText(album.getNotificationMessage());
            holder.textViewNotificationTime.setText(album.getNotificationTime());

            if(album.getNotificationType().equals(Constants.TAG_TYPE_FRIEND_REQUEST) ||
                    album.getNotificationType().equals(Constants.TAG_TYPE_VIDEO_CHAT) ||
                    album.getNotificationType().equals(Constants.TAG_TYPE_SEND_INVITATION))
            {
                holder.linearLayoutAcceptDecline.setVisibility(View.VISIBLE);
            }else
            {
                holder.linearLayoutAcceptDecline.setVisibility(View.GONE);
            }

            Glide.with(mContext)
            //                    .load(album.getNotificationImageMainURL()+"/"+album.getNotificationImageProfileUrl()).listener
//                    .load(album.getNotificationImageMainURL()+"/male.png").listener
                    .load(album.getNotificationImageMainURL())
                    .listener

        (new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                Toast.makeText(mContext, "No Image Found!" + model + "/" + e, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        }).into(holder.imageViewSenderProfileImage);


            holder.imageViewAcceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(album.getNotificationType().equals(Constants.TAG_TYPE_FRIEND_REQUEST))
                    {
                        setFriendRequestAccepted(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_ACCEPTED);
                    }
//                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_VIDEO_CHAT))
//                    {
//                        setSentVideoChatRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_ACCEPTED);
//                    }
//                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_SEND_INVITATION))
//                    {
//                        setSentInvitationRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_ACCEPTED);
//                    }


                }
            });
            holder.imageViewDeclineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(album.getNotificationType().equals(Constants.TAG_TYPE_FRIEND_REQUEST))
                    {
                        setFriendRequestAccepted(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
                    }
//                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_VIDEO_CHAT))
//                    {
//                        setSentVideoChatRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
//                    }
//                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_SEND_INVITATION))
//                    {
//                        setSentInvitationRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
//                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return notificationDetailsList.size();
        }


    }
    public void setFriendRequestAccepted(String strNotificationID,String strAcceptDeclineStatus) {


        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {

            CommonFunctions.showProgressDialog(getContext());


            StringRequest getNotificationList = new StringRequest(Request.Method.POST, Constants.API_NEW_ACCEPT_FRIEND_REQUEST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {

                            System.out.println("jigar the friend request response  in json is " + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
//                                    finish();
                                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_frame, new FriendRequests(), TAG);
                                    ft.commit();

//                                    Fragment frg = null;
//
////                                    frg = getActivity().getSupportFragmentManager().findFragmentByTag(TAG);
//                                    frg = getChildFragmentManager().findFragmentByTag(TAG);
////                                    final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                                    final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//                                    ft.detach(frg);
//                                    ft.attach(frg);
//                                    ft.commit();

//                                    startActivity(getIntent());
//                                    setNotificationData(res);
                                    CommonFunctions.hideProgressDialog(getContext());
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(getContext(), StrMessage, Toast.LENGTH_LONG).show();
                                    CommonFunctions.hideProgressDialog(getContext());
                                }
                            } catch (JSONException e) {
//                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);
                           //     linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);
                    //            linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonFunctions.hideProgressDialog(getContext());
                    System.out.println("jigar the error volley in json is " + error.getMessage());
           //         linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                }
            })
            {

                //            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_FRIEND_ID, strFriendId);
                    params.put(Constants.TAG_ID,strNotificationID );
                    params.put(Constants.TAG_STATUS,strAcceptDeclineStatus);

                    System.out.println("jigar the parameter we have in json is " + params);

//                    Log.v(TAG, "commentParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getNotificationList, "");
        }else
        {
            CommonFunctions.hideProgressDialog(getContext());
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter {
        private static final int TYPE_ITEM = 1,TYPE_FOOTER = 2;
        ArrayList<HashMap<String, String>> friendReqlist;
        private boolean showLoader;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> friendReqlist) {
            this.friendReqlist = friendReqlist;
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
                HashMap<String, String> tempMap = friendReqlist.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

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
            return position == friendReqlist.size();
        }

        @Override
        public int getItemCount() {
            if (friendReqlist.size() == 0)
                return 0;
            else
                return friendReqlist.size() + 1;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage, online;
            TextView userName;

            public MyViewHolder(View view) {
                super(view);
                singleImage = view.findViewById(R.id.user_image);
                userName =  view.findViewById(R.id.user_name);
                online = view.findViewById(R.id.online);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.user_image:
                        Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                        p.putExtra("from", "other");
                        p.putExtra("strFriendID", friendReqlist.get(getAdapterPosition()).get(Constants.TAG_USERID));
                        startActivity(p);
                        break;
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            ProgressWheel progress;

            public FooterViewHolder(View parent) {
                super(parent);
                progress = parent.findViewById(R.id.footer_progress);
            }

        }
    }

    /**
     * API Implementation
     * */

    public void getFriendRequest() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (peoplesAry.size() == 0) {
                    if (pulldown) {
                        swipeRefresh(true);
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        progress.spin();
//                        itemAdapter.showLoading(false);
                    }
                } else if (peoplesAry.size() >= 20) {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
  ///                  itemAdapter.showLoading(true);
     //               itemAdapter.notifyDataSetChanged();
                } else {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
       //             itemAdapter.showLoading(false);
                }
            }
        });
        StringRequest getPeople = new StringRequest(Request.Method.POST,
                Constants.API_NEW_FRIEND_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "getPeopleRes="+res);
                        try {
                            if (pulldown) {
                                pulldown = false;
                                peoplesAry.clear();
                            }
                            setFriendRequestData(res);


//                            JSONObject jsonObjectResponseMain = new JSONObject(res);
//                            String strStatus = jsonObjectResponseMain.getString(Constants.TAG_STATUS);
//                            String strMsg = jsonObjectResponseMain.getString(Constants.TAG_MSG);
//
//
//                            JSONArray jsonArrayInfo = jsonObjectResponseMain.getJSONArray(Constants.TAG_INFO);
//                            for (int i = 0; i < jsonArrayInfo.length(); i++) {
//                                JSONObject jsonObjectCountryCodeInfo = jsonArrayInfo.getJSONObject(i);
//                                String strFriendRequestProfileImage = jsonObjectCountryCodeInfo.getString(Constants.TAG_NOTIFICATION_SENDER_PROFILE_IMAGE);
//                                String strFriendRequestImageUrl = jsonObjectCountryCodeInfo.getString(Constants.TAG_NOTIFICATION_SENDER_URL);
//                                String strFriendID = jsonObjectCountryCodeInfo.getString(Constants.TAG_NOTIFICATION_FRIEND_ID);
//                                String strFriendMessageTime = jsonObjectCountryCodeInfo.getString(Constants.TAG_NOTIFICATION_TIME);
//
//
//                            }

//                            CommonFunctions.hideProgressDialog(EmailLogin.this);
//                            itemAdapter.showLoading(false);
//                            itemAdapter.notifyDataSetChanged();
                            swipeRefresh(false);
                            if (peoplesAry.size() == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                            } else {
                                nullLay.setVisibility(View.GONE);
                            }
                            progress.setVisibility(View.GONE);
                            progress.stopSpinning();




                            if (peoplesAry.size() >= 20) {
                                loadmore = true;
                            } else {
                                loadmore = false;
                            }


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
                System.out.println("jigar the volly error is " + error.getMessage());

                pulldown = false;
                swipeRefresh(false);
                progress.setVisibility(View.GONE);
                progress.stopSpinning();
//                itemAdapter.showLoading(false);
//                itemAdapter.notifyDataSetChanged();
                if (peoplesAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
                loadmore = false;
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
//                params.put(Constants.TAG_USERID, GetSet.getUserId());
//                params.put(Constants.TAG_OFFSET, String.valueOf(currentPage * 20));
//                params.put(Constants.TAG_LIMIT, "20");
//                params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "getPeopleParams=" + params);
                System.out.println("jigar the friend request params is " + params);

                return params;
            }

        };

        HowzuApplication.getInstance().addToRequestQueue(getPeople, TAG);
    }

    public void setFriendRequestData(String strResponse)
    {
        System.out.println("jigar the friend request response is " + strResponse);

        try {
            JSONObject jsonObject = new JSONObject(strResponse);
            String strStatus = jsonObject.getString(Constants.TAG_STATUS);
            String strMessage = jsonObject.getString(Constants.TAG_MSG);

            if (strStatus.equals("1") && strMessage.equals(Constants.TAG_SUCCESS)) {


                JSONArray jsonArrayProfileInfo = jsonObject.getJSONArray(Constants.TAG_INFO);
                //     JSONObject jsonObjectMember=jsonObjectInfo.getJSONObject(Constants.TAG_MEMBER);


                for (int i = 0; i < jsonArrayProfileInfo.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject jsonObjectMemberMainInfo = jsonArrayProfileInfo.getJSONObject(i);

                    String strNotificationID = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_ID);
                    String strNotificationFriendID = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_ID);

                    String strNotificationSenderName = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_SENDER_NAME);
                    String strNotificationMessage = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_MESSAGE);
                    String strNotificationType = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_TYPE);
                    String strNotificationImageProfileURL = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_SENDER_PROFILE_IMAGE);
                    String strNotificationImageMainURL = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_SENDER_URL);
                    String strNotificationTime = jsonObjectMemberMainInfo.getString(Constants.TAG_NOTIFICATION_TIME);
                    strNotificationSenderName = strNotificationSenderName.substring(0,1).toUpperCase() + strNotificationSenderName.substring(1);
                    int intNotificationID=Integer.parseInt(strNotificationID);
                    POJONotificationDetails  pojoNotificationDetails=new POJONotificationDetails(intNotificationID,strNotificationSenderName,strNotificationMessage,strNotificationTime
                            ,strNotificationType,strNotificationImageMainURL,strNotificationImageProfileURL,strNotificationFriendID);
                    peoplesAry.add(pojoNotificationDetails);
                    System.out.println("jigar the friend request  name is " + strNotificationID);
//                    recyclerView.setAdapter(friendRequestAdapter);

                    friendRequestAdapter.notifyDataSetChanged();

                    //       peoplesAry.add(map);
                }
                //                System.out.println("jigar the commenter list have is " + arrayListComments.toString());
//                System.out.println("jigar the commenter list size is " + arrayListComments.size());
            }else
            {
             //   linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
            }
        }catch (JSONException ex)
        {
            System.out.println("jigar the error in json is "+ex);
        }
    }


}
