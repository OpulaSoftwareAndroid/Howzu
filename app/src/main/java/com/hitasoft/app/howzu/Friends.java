package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.hitasoft.app.model.POJOFriendsListDetails;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 27/10/16.
 */

public class Friends extends Fragment implements View.OnClickListener, TextWatcher {
    String TAG = "Friends";

    RecyclerView recyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerViewAdapter itemAdapter;
//    FriendRequestAdapter friendRequestAdapter;
    Display display;
    RelativeLayout main, searchLay;
    LinearLayout nullLay;
    ProgressWheel progress;
    EditText searchEdit;
    ImageView cancelBtn, backbtn;
    TextView nullText;

    public static Context context;
    int currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount, visibleThreshold = 5, previousTotal = 0;
    InputMethodManager imm;
    ArrayList<HashMap<String, String>> peoplesAry = new ArrayList<HashMap<String, String>>();
//    ArrayList<POJOFriendsListDetails> peoplesAry = new ArrayList<>();

    boolean loading = true, loadmore = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends, container, false);
        Log.i("Friends", "Friends: onCreateView");

        progress = view.findViewById(R.id.progress);
        recyclerView = view.findViewById(R.id.recyclerView);
        main = view.findViewById(R.id.main);
        nullLay = view.findViewById(R.id.nullLay);
        nullText =  view.findViewById(R.id.nullText);

        searchLay = getActivity().findViewById(R.id.search_lay);
        searchEdit = getActivity().findViewById(R.id.searchbar);
        cancelBtn = getActivity().findViewById(R.id.cancelbtn);
        backbtn = getActivity().findViewById(R.id.backbtn);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainScreenActivity.setToolBar(getActivity(), "friends");

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        context = getActivity();
        display = getActivity().getWindowManager().getDefaultDisplay();
        currentPage = 0;
        previousTotal = 0;
        peoplesAry.clear();
        setAdapter();

        MainScreenActivity.searchbtn.setOnClickListener(this);
        MainScreenActivity.spinLay.setOnClickListener(this);

        cancelBtn.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        searchEdit.addTextChangedListener(this);

        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        CommonFunctions.setupUI(getActivity(), getView());

        getFriendsList();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (searchLay.getVisibility() == View.VISIBLE) {
                            imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                            searchLay.setVisibility(View.GONE);
                            searchEdit.setText("");
                            nullLay.setVisibility(View.GONE);
                            itemAdapter = new RecyclerViewAdapter(peoplesAry);
                            recyclerView.setAdapter(itemAdapter);
//                            friendRequestAdapter = new FriendRequestAdapter(getContext(),peoplesAry);
//                            recyclerView.setAdapter(friendRequestAdapter);

                            if (peoplesAry.size() == 0) {
                                currentPage = 0;
                                previousTotal = 0;
                                loadmore = false;
                                getFriendsList();
                            }
                            return true;
                        } else {
                            return false;
                        }

                    }
                }
                return false;
            }
        });

        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchEdit.clearFocus();
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            cancelBtn.setVisibility(View.VISIBLE);
        } else {
            cancelBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
//        filter(editable.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        //setOnlineStatus();
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(mLayoutManager);

        itemAdapter = new RecyclerViewAdapter(peoplesAry);
        recyclerView.setAdapter(itemAdapter);
//


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
                        Log.v("END Reached", "END Reached=" + currentPage);
                        getFriendsList();
                        loading = true;
                    }
                }
            }
        });

//        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return friendRequestAdapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
//            }
//        });
    }

    /**
     * function for showing the popup window
     **/
    public void viewOptions(View v) {
        final String[] values;
        if (MainScreenActivity.spinText.getText().toString().equals(getString(R.string.all_friends))) {
            values = new String[]{getString(R.string.online)};
        } else {
            values = new String[]{getString(R.string.all_friends)};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.options_item, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.options, null);
        // layout.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.linear_interpolator));
        final PopupWindow popup = new PopupWindow(getActivity());
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 50 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.LEFT, HowzuApplication.dpToPx(getActivity(), 60), HowzuApplication.dpToPx(getActivity(), 70));

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        popup.dismiss();
                        MainScreenActivity.spinText.setText(values[position]);
                        peoplesAry.clear();
                        itemAdapter.notifyDataSetChanged();
//                        friendRequestAdapter.notifyDataSetChanged();
                        currentPage = 0;
                        previousTotal = 0;
                        loadmore = false;
                        nullLay.setVisibility(View.GONE);
                        getFriendsList();
                        break;
                }
            }
        });
    }

//    void filter(String text) {
//        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
//        for (HashMap<String, String> name : peoplesAry) {
//            //or use .contains(text)
//            if (name.get(Constants.TAG_USERNAME).toLowerCase().startsWith(text.toLowerCase())) {
//                temp.add(name);
//            }
//        }
//        //update recyclerview
//        itemAdapter.updateList(temp);
//    }


    class RecyclerViewAdapter extends RecyclerView.Adapter {
        private static final int TYPE_ITEM = 1,TYPE_FOOTER = 2;
        ArrayList<HashMap<String, String>> Items;
        private boolean showLoader;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
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
            if (itemType == TYPE_ITEM)
            {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                Log.d(TAG,"jigar the friend list user name is "+tempMap.get(Constants.TAG_USERNAME));

                viewHolder.userName.setText(tempMap.get(Constants.TAG_USERNAME));
//                String img = Constants.RESIZE_URL + CommonFunctions.getImageName(tempMap.get(Constants.TAG_USERIMAGE)) + Constants.IMAGE_RES;
                String img = CommonFunctions.getImageName(tempMap.get(Constants.TAG_USERIMAGE)) + Constants.IMAGE_RES;

                Picasso.with(context)
                        .load(tempMap.get(Constants.TAG_USERIMAGE))
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
            }
            else {
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

        public void updateList(ArrayList<HashMap<String, String>> data) {
            Items = data;
            notifyDataSetChanged();
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
            return position == Items.size();
        }

        @Override
        public int getItemCount() {
            if (Items.size() == 0)
                return 0;
            else
                return Items.size() + 1;
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
                        p.putExtra("from", "friends");
                        p.putExtra("strFriendID", Items.get(getAdapterPosition()).get(Constants.TAG_USERID));
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

    public void getFriendsList() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (peoplesAry.size() == 0) {
                    progress.setVisibility(View.VISIBLE);
                    progress.spin();
                    itemAdapter.showLoading(false);
//                    friendRequestAdapter.show
                } else if (peoplesAry.size() >= 20) {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    itemAdapter.showLoading(false);
                    itemAdapter.notifyDataSetChanged();
//                    friendRequestAdapter.showLoading(true);
   //                 friendRequestAdapter.notifyDataSetChanged();
                } else {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    itemAdapter.showLoading(false);
                }
            }
        });

        StringRequest getFriendList = new StringRequest(Request.Method.POST,
                Constants.API_NEW_FRIEND_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
  //                      Log.d(TAG, "getPeopleRes="+res);
                        try {
//                            Log.d(TAG, "getVisitorRes=" + res);
                            Log.d(TAG, "jigar the response in friend list  is : " + res);

                                JSONObject response = new JSONObject(res);

//                            String status = DefensiveClass.optString(response, Constants.TAG_STATUS);

                                String strStatus=response.getString(Constants.TAG_STATUS);
                                   String strMessage=response.getString(Constants.TAG_MSG);

                                if(strStatus.equals("1")) {

                                    JSONArray jsonArrayMainInfo = response.getJSONArray(Constants.TAG_INFO);
                                    for (int i = 0; i < jsonArrayMainInfo.length(); i++) {
                                        HashMap<String,String> map = new HashMap<String, String>();
                                        JSONObject jsonObjectSubMainResponse = jsonArrayMainInfo.getJSONObject(i);
                                        String strFriendName = jsonObjectSubMainResponse.getString(Constants.TAG_NAME);
                                        String strFriendLocation = jsonObjectSubMainResponse.getString(Constants.TAG_LOCATION);
                                        String strFriendUserID = jsonObjectSubMainResponse.getString(Constants.TAG_ID);
                                        String strFriendImageUrl = jsonObjectSubMainResponse.getString(Constants.TAG_IMAGE);
                                        map.put(Constants.TAG_USERID, strFriendUserID);
                                        map.put(Constants.TAG_USERIMAGE, strFriendImageUrl);
                                        map.put(Constants.TAG_LOCATION, strFriendLocation);
                                        map.put(Constants.TAG_USERNAME, strFriendName);
                                        map.put(Constants.TAG_ONLINE, "0");

                                        peoplesAry.add(map);
                                      //  peoplesAry.add(map);

//                                    map.put(Constants.TAG_USERNAME, DefensiveClass.optString(values, Constants.TAG_USERNAME));
//                                    map.put(Constants.TAG_SEND_MATCH, DefensiveClass.optString(values, Constants.TAG_SEND_MATCH));
//                                    map.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
//                                    map.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
//                                    map.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
//                                    map.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
//                                    map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
//                                    map.put(Constants.TAG_ONLINE, DefensiveClass.optInt(values, Constants.TAG_ONLINE));
//                                    peoplesAry.add(map);
                                    }

                                }



                            Log.d(TAG,"jigar the friend list people array "+peoplesAry.size());



//                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true"))
//                            {
//                                JSONArray peoples = response.optJSONArray(Constants.TAG_PEOPLES);
//                                for (int i = 0; i < peoples.length(); i++) {
//                                    JSONObject values = peoples.optJSONObject(i);
//                                    HashMap<String, String> map = new HashMap<String, String>();
//                                    map.put(Constants.TAG_USERID, DefensiveClass.optInt(values, Constants.TAG_USERID));
//                                    map.put(Constants.TAG_USERNAME, DefensiveClass.optString(values, Constants.TAG_USERNAME));
//                                    map.put(Constants.TAG_SEND_MATCH, DefensiveClass.optString(values, Constants.TAG_SEND_MATCH));
//                                    map.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
//                                    map.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
//                                    map.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
//                                    map.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
//                                    map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
//                                    map.put(Constants.TAG_ONLINE, DefensiveClass.optInt(values, Constants.TAG_ONLINE));
//                                    peoplesAry.add(map);
//                                }
//                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
//                                CommonFunctions.disabledialog(getActivity(), "Error", response.getString("message"));
//                            } else {
//
//                            }

                            if (peoplesAry.size() >= 20) {
                                loadmore = true;
                            } else {
                                loadmore = false;
                            }

                            itemAdapter.notifyDataSetChanged();
                        //    itemAdapter.showLoading(false);
//                            friendRequestAdapter.notifyDataSetChanged();
                            if (peoplesAry.size() == 0) {
                                //nullLay.setVisibility(View.VISIBLE);
                                String spinTxt = MainScreenActivity.spinText.getText().toString();
                                if (spinTxt.equals(getString(R.string.all_friends))) {
                                    nullText.setText(getString(R.string.no_friends));
                                } else {
                                    nullText.setText(getString(R.string.no_online));
                                }
                            } else {
                                nullLay.setVisibility(View.GONE);
                            }
                            progress.setVisibility(View.GONE);
                            progress.stopSpinning();

                        } catch (JSONException e) {
                            Log.d(TAG,"jigar the error in json is "+e);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.d(TAG,"jigar the error in null pointer  json is "+e);
                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.d(TAG,"jigar the error in main exception   json is "+e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "jigar the friend list Error: " + error.getMessage());
                progress.setVisibility(View.GONE);
                progress.stopSpinning();
      //          itemAdapter.showLoading(false);
//                friendRequestAdapter.notifyDataSetChanged();
                if (peoplesAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    String spinTxt = MainScreenActivity.spinText.getText().toString();
                    if (spinTxt.equals(getString(R.string.all_friends))) {
                        nullText.setText(getString(R.string.no_friends));
                    } else {
                        nullText.setText(getString(R.string.no_online));
                    }
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
//                params.put(Constants.TAG_OFFSET, String.valueOf(currentPage * 20));
//                params.put(Constants.TAG_LIMIT, "20");
//                params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
//                if (MainScreenActivity.spinText.getText().toString().equals(getString(R.string.all_friends))) {
//                    params.put(Constants.TAG_SORT, "all");
//                } else {
//                    params.put(Constants.TAG_SORT, "online");
//                }
                Log.v(TAG, "jigar get friend list params=" + params);
                return params;
            }
        };

//        HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
        HowzuApplication.getInstance().addToRequestQueue(getFriendList, TAG);
    }

    private void setOnlineStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "setOnlineStatusRes=" + res);
                        }catch (NullPointerException e) {
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
                map.put(Constants.TAG_STATUS, "1");
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "setOnlineStatusParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelbtn:
                searchEdit.setText("");
                cancelBtn.setVisibility(View.GONE);
                break;
            case R.id.searchbtn:
                searchLay.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
                itemAdapter = new RecyclerViewAdapter(peoplesAry);
                recyclerView.setAdapter(itemAdapter);

//                friendRequestAdapter = new FriendRequestAdapter(getContext(),peoplesAry);
//                recyclerView.setAdapter(friendRequestAdapter);

                if (peoplesAry.size() == 0) {
                    currentPage = 0;
                    previousTotal = 0;
                    loadmore = false;
                    getFriendsList();
                }
                break;
            case R.id.spinLay:
                viewOptions(v);
                break;
            case R.id.backbtn:
                imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                searchLay.setVisibility(View.GONE);
                searchEdit.setText("");
                nullLay.setVisibility(View.GONE);
                itemAdapter = new RecyclerViewAdapter(peoplesAry);
                recyclerView.setAdapter(itemAdapter);
//
//                friendRequestAdapter = new FriendRequestAdapter(getContext(),peoplesAry);
//                recyclerView.setAdapter(friendRequestAdapter);


                if (peoplesAry.size() == 0) {
                    currentPage = 0;
                    previousTotal = 0;
                    loadmore = false;
                    getFriendsList();
                }
                break;
        }
    }

    class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.MyViewHolder> {

        private Context mContext;
        private List<POJOFriendsListDetails> notificationDetailsList;

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

        public FriendRequestAdapter(Context mContext, List<POJOFriendsListDetails> notificationDetailsList) {
            this.mContext = mContext;
            this.notificationDetailsList = notificationDetailsList;
        }

        @Override
        public FriendRequestAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friends_list_adapter_item, parent, false);

            return new FriendRequestAdapter.MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            POJOFriendsListDetails album = notificationDetailsList.get(position);
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




        }


        @Override
        public int getItemCount() {
            return notificationDetailsList.size();
        }


    }
}