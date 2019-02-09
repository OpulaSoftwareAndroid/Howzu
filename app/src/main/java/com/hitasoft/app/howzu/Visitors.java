package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.BlurBuilder;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 27/10/16.
 */

public class Visitors extends Fragment implements View.OnClickListener {
    String TAG = "Visitors";

    View premiumView;
    LinearLayout nullLay;
    RelativeLayout premiumLay;
    ProgressWheel progress;
    TextView becomePremium;
    RecyclerView recyclerView;
    String strUserID;
    public static Context context;
    CustomStaggeredGridLayoutManager mStagLayoutManager;
    GridLayoutManager mLayoutManager;
    RecyclerViewAdapter itemAdapter;
    int topPadding = 0;
    int currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount, visibleThreshold = 5, previousTotal = 0;
    ArrayList<HashMap<String, String>> peoplesAry = new ArrayList<HashMap<String, String>>();
    boolean loading = true, loadmore = false, premiumClicked = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visitors, container, false);
        Log.i(TAG, "Visitors: onCreateView");

        recyclerView = view.findViewById(R.id.recyclerView);
        nullLay = view.findViewById(R.id.nullLay);
        progress = view.findViewById(R.id.progress);
        premiumLay = view.findViewById(R.id.premium_lay);
        premiumView = view.findViewById(R.id.view);
        becomePremium = view.findViewById(R.id.become_premium);
        strUserID="84c515a0-1b01-11e9-9b55-6b6474cbadea";
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainScreenActivity.setToolBar(getActivity(), "visitors");

        context = getActivity();
        topPadding = HowzuApplication.dpToPx(context, 60);
        currentPage = 0;
        previousTotal = 0;
        peoplesAry.clear();

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        setAdapter();

        premiumLay.setVisibility(View.GONE);
        premiumView.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        becomePremium.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        premiumClicked = false;
        if (itemAdapter != null) {
            peoplesAry.clear();
            itemAdapter.notifyDataSetChanged();
            currentPage = 0;
        }
        getPeople();
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(false);
        if (GetSet.isPremium()) {
            mLayoutManager = new GridLayoutManager(context, 3);
            recyclerView.setLayoutManager(mLayoutManager);

            itemAdapter = new RecyclerViewAdapter(getActivity(), peoplesAry);
            recyclerView.setAdapter(itemAdapter);
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
                            getPeople();
                            loading = true;
                        }
                    }
                }
            });

            mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return itemAdapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
                }
            });
        } else {
            mStagLayoutManager = new CustomStaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(mStagLayoutManager);

            itemAdapter = new RecyclerViewAdapter(getActivity(), peoplesAry);
            recyclerView.setAdapter(itemAdapter);

            recyclerView.stopScroll();
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter {
        private static final int TYPE_ITEM = 1, TYPE_FOOTER = 2;
        ArrayList<HashMap<String, String>> visitorList;
        Context context;
        boolean showLoader;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> visitorList) {
            this.visitorList = visitorList;
            this.context = context;
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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);
            if (itemType == TYPE_ITEM) {
                HashMap<String, String> tempMap = visitorList.get(position);
                final MyViewHolder viewHolder = (MyViewHolder) holder;

                if (premiumLay.getVisibility() == View.VISIBLE) {
                    if (position == 0 || position == 2) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.singleImage.getLayoutParams();
                        params.setMargins(0, topPadding, 0, 0);
                        viewHolder.singleImage.setLayoutParams(params);
                    }
                }

                viewHolder.userName.setText(tempMap.get(Constants.TAG_USERNAME));
                String img = Constants.RESIZE_URL + CommonFunctions.getImageName(tempMap.get(Constants.TAG_USERIMAGE)) + Constants.IMAGE_RES;
                Picasso.with(context)
                        .load(img)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                        .centerCrop()
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                if (GetSet.isPremium()) {
                                    viewHolder.singleImage.setImageBitmap(bitmap);
                                } else {
                                    Bitmap blurredBitmap = BlurBuilder.blur(context, bitmap);
                                    viewHolder.singleImage.setImageBitmap(blurredBitmap);
                                }
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                            }
                        });

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
            return position == visitorList.size();
        }

        @Override
        public int getItemCount() {
            if (visitorList.size() == 0)
                return 0;
            else
                return visitorList.size() + 1;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage, online;
            TextView userName;

            public MyViewHolder(View view) {
                super(view);
                singleImage = view.findViewById(R.id.user_image);
                userName = view.findViewById(R.id.user_name);
                online = view.findViewById(R.id.online);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.user_image:
                        if (GetSet.isPremium()) {
                            Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                            p.putExtra("from", "visitors");
                            p.putExtra("strFriendID", visitorList.get(getAdapterPosition()).get(Constants.TAG_USERID));
                            startActivity(p);
                        } else {
                            if (!premiumClicked) {
                                premiumClicked = true;
                                Intent p = new Intent(getActivity(), PremiumDialog.class);
                                startActivity(p);
                            }
                        }
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

    public class CustomStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

        public CustomStaggeredGridLayoutManager(int span, int orientation) {
            super(span, orientation);

        }

        // it will always pass false to RecyclerView when calling "canScrollVertically()" method.
        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }

    /**
     * API Implementation
     */

    public void getPeople() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (peoplesAry.size() == 0) {
                    progress.setVisibility(View.VISIBLE);
                    progress.spin();
                    itemAdapter.showLoading(false);
                } else if (peoplesAry.size() >= 20) {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    itemAdapter.showLoading(true);
                    itemAdapter.notifyDataSetChanged();
                } else {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    itemAdapter.showLoading(false);
                }
            }
        });

        StringRequest getPeople = new StringRequest(Request.Method.POST,
                Constants.API_VISITORS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "getVisitorRes=" + res);
                        Log.d(TAG, "jigar the response in null pointer exception  is : " + res);

                        try {
                            JSONObject response = new JSONObject(res);
//                            String status = DefensiveClass.optString(response, Constants.TAG_STATUS);

                            String strStatus=response.getString(Constants.TAG_STATUS);
                            String strMessage=response.getString(Constants.TAG_MESSAGE);

//                            if (status.equalsIgnoreCase("true")) {
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
//
//                            } else if (status.equalsIgnoreCase("error")) {
//                                CommonFunctions.disabledialog(getActivity(), "Error", response.getString("message"));
//                            } else {
//
//                            }

                            if (peoplesAry.size() >= 20) {
                                loadmore = true;
                            } else {
                                loadmore = false;
                            }

                            if (peoplesAry.size() == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                            } else {
                                nullLay.setVisibility(View.GONE);
                                if (GetSet.isPremium()) {
                                    premiumLay.setVisibility(View.GONE);
                                    premiumView.setVisibility(View.GONE);
                                } else {
                                    premiumLay.setVisibility(View.VISIBLE);
                                    premiumView.setVisibility(View.VISIBLE);
                                    becomePremium.setVisibility(View.VISIBLE);
                                }
                            }
                            progress.setVisibility(View.GONE);
                            progress.stopSpinning();
                            itemAdapter.showLoading(false);
                            itemAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Log.d(TAG, "jigar the error in json is : " + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.d(TAG, "jigar the error in null pointer exception  is : " + e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.d(TAG, "jigar the error in exception  is : " + e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                VolleyLog.d(TAG, "jigar the error in volley is : " + error.getMessage());

                progress.setVisibility(View.GONE);
                progress.stopSpinning();
                itemAdapter.showLoading(false);
                itemAdapter.notifyDataSetChanged();
                if (peoplesAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                    if (GetSet.isPremium()) {
                        premiumLay.setVisibility(View.GONE);
                        premiumView.setVisibility(View.GONE);
                    } else {
                        premiumLay.setVisibility(View.VISIBLE);
                        premiumView.setVisibility(View.VISIBLE);
                        becomePremium.setVisibility(View.VISIBLE);
                    }
                }
                loadmore = false;
            }
        }) {

            //
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
////                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                map.put(Constants.TAG_AUTHORIZATION, "84c515a0-1b01-11e9-bfc0-177318cdc83d");
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_USERID, GetSet.getUserId());
                params.put(Constants.TAG_NEW_USERID, strUserID);
//                params.put(Constants.TAG_OFFSET, String.valueOf(currentPage * 20));
//                params.put(Constants.TAG_LIMIT, "20");
//                params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "getVisitorParams=" + params);
                return params;
            }
        };

        HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
        HowzuApplication.getInstance().addToRequestQueue(getPeople, TAG);
    }

    /*OnClick Event*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
