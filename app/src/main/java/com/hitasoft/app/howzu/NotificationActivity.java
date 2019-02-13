package com.hitasoft.app.howzu;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitasoft.app.customclass.RoundedImageView;
import com.hitasoft.app.model.POJONotificationDetails;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationActivity extends AppCompatActivity {
    RecyclerView recycler_notification;
    POJONotificationDetails pojoNotificationDetails;
    List<POJONotificationDetails> notificationDetailList;
    NotificationAdapter notificationAdapter;

    LinearLayout linearLayoutEmptyNotification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noitification);
        initView();


        //My members
        notificationDetailList = new ArrayList<>();
        linearLayoutEmptyNotification=findViewById(R.id.linearLayoutEmptyNotification);
        linearLayoutEmptyNotification.setVisibility(View.GONE);
        //        MyMembersModel myMembersModel = new MyMembersModel("Member 1", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 2", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 3", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 4", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 5", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 6", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 7", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
//        myMembersModel = new MyMembersModel("Member 8", R.drawable.user_placeholder);
//        notificationDetailList.add(myMembersModel);
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recycler_notification.setLayoutManager(horizontalLayoutManagaer);
        notificationAdapter = new NotificationAdapter(getApplicationContext(), notificationDetailList);
        recycler_notification.setAdapter(notificationAdapter);
        getProfileNotificationList();

    }

    private void initView() {
        recycler_notification = findViewById(R.id.recycler_notification);
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

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

        public NotificationAdapter(Context mContext, List<POJONotificationDetails> notificationDetailsList) {
            this.mContext = mContext;
            this.notificationDetailsList = notificationDetailsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_adapter_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
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
                    .load(album.getNotificationImageMainURL()+"/"+album.getNotificationImageProfileUrl()).listener
//                    .load(album.getNotificationImageMainURL()+"/male.png").listener

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
                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_VIDEO_CHAT))
                    {
                        setSentVideoChatRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_ACCEPTED);
                    }
                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_SEND_INVITATION))
                    {
                        setSentInvitationRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_ACCEPTED);
                    }


                }
            });
            holder.imageViewDeclineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(album.getNotificationType().equals(Constants.TAG_TYPE_FRIEND_REQUEST))
                    {
                        setFriendRequestAccepted(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
                    }
                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_VIDEO_CHAT))
                    {
                        setSentVideoChatRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
                    }
                    else if(album.getNotificationType().equals(Constants.TAG_TYPE_SEND_INVITATION))
                    {
                        setSentInvitationRequestAcceptedDecline(String.valueOf(album.getNotificationID()),Constants.TAG_REQUEST_DECLINE);
                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return notificationDetailsList.size();
        }


    }

    public void setFriendRequestAccepted(String strNotificationID,String strAcceptDeclineStatus) {


        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {

            CommonFunctions.showProgressDialog(this);

            StringRequest getNotificationList = new StringRequest(Request.Method.POST, Constants.API_NEW_ACCEPT_FRIEND_REQUEST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {

                            System.out.println("jigar the friend request response  in json is " + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    finish();
                                    startActivity(getIntent());
//                                    setNotificationData(res);
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(NotificationActivity.this, StrMessage, Toast.LENGTH_LONG).show();
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                }
                            } catch (JSONException e) {
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                    System.out.println("jigar the error volley in json is " + error.getMessage());
                    linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
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
            CommonFunctions.hideProgressDialog(this);
        }
    }

    public void setSentVideoChatRequestAcceptedDecline(String strNotificationID,String strAcceptDeclineStatus) {


        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {

            CommonFunctions.showProgressDialog(this);


            StringRequest getNotificationList = new StringRequest(Request.Method.POST, Constants.API_NEW_ACCEPT_DECLINE_VIDEO_CHAT_REQUEST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {

                            System.out.println("jigar the video request response  in json is " + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    finish();
                                    startActivity(getIntent());
//                                    setNotificationData(res);
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(NotificationActivity.this, StrMessage, Toast.LENGTH_LONG).show();
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                }
                            } catch (JSONException e) {
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                    System.out.println("jigar the error volley in json is " + error.getMessage());
                    linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
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
                    params.put(Constants.TAG_TYPE,strAcceptDeclineStatus);

                    System.out.println("jigar the parameter we have in json is " + params);

//                    Log.v(TAG, "commentParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getNotificationList, "");
        }else
        {
            CommonFunctions.hideProgressDialog(this);
        }
    }

    public void setSentInvitationRequestAcceptedDecline(String strNotificationID,String strAcceptDeclineStatus) {


        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {

            CommonFunctions.showProgressDialog(this);


            StringRequest getNotificationList = new StringRequest(Request.Method.POST, Constants.API_NEW_ACCEPT_DECLINE_INVITATION_REQUEST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {

                            System.out.println("jigar the invitation request response  in json is " + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    finish();
                                    startActivity(getIntent());
//                                    setNotificationData(res);
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(NotificationActivity.this, StrMessage, Toast.LENGTH_LONG).show();
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                }
                            } catch (JSONException e) {
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                    System.out.println("jigar the error volley in json is " + error.getMessage());
                    linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
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
                    params.put(Constants.TAG_TYPE,strAcceptDeclineStatus);

                    System.out.println("jigar the parameter we have in json is " + params);

//                    Log.v(TAG, "commentParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getNotificationList, "");
        }else
        {
            CommonFunctions.hideProgressDialog(this);
        }
    }

    public void getProfileNotificationList() {

        if (CommonFunctions.isNetwork(Objects.requireNonNull(this))) {

            CommonFunctions.showProgressDialog(this);


            StringRequest getNotificationList = new StringRequest(Request.Method.POST, Constants.API_GET_USER_NOTIFICATION,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {

                            System.out.println("jigar the response  in json is " + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    setNotificationData(res);
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(NotificationActivity.this, StrMessage, Toast.LENGTH_LONG).show();
                                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                                }
                            } catch (JSONException e) {
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);
                                linearLayoutEmptyNotification.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonFunctions.hideProgressDialog(NotificationActivity.this);
                    System.out.println("jigar the error volley in json is " + error.getMessage());
                    linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
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
                    params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                    System.out.println("jigar the parameter we have in json is " + params);

//                    Log.v(TAG, "commentParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getNotificationList, "");
        }else
        {
            CommonFunctions.hideProgressDialog(this);
        }
    }

    public void setNotificationData(String strResponse)
    {

        try {
            JSONObject jsonObject = new JSONObject(strResponse);
            String strStatus = jsonObject.getString(Constants.TAG_STATUS);
            String strMessage = jsonObject.getString(Constants.TAG_MSG);
            System.out.println("jigar the commenter response is " + strResponse);

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
                    notificationDetailList.add(pojoNotificationDetails);
                    System.out.println("jigar the commenter name is " + strNotificationID);
                    recycler_notification.setAdapter(notificationAdapter);


                    //       peoplesAry.add(map);
                }
                //                System.out.println("jigar the commenter list have is " + arrayListComments.toString());
//                System.out.println("jigar the commenter list size is " + arrayListComments.size());
            }else
                {
                    linearLayoutEmptyNotification.setVisibility(View.VISIBLE);
                }
        }catch (JSONException ex)
        {
            System.out.println("jigar the error in json is "+ex);
        }
    }


}
