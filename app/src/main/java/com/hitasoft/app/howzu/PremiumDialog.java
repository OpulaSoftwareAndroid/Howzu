package com.hitasoft.app.howzu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.inapp.IabHelper;
import com.hitasoft.app.inapp.IabResult;
import com.hitasoft.app.inapp.Inventory;
import com.hitasoft.app.inapp.Purchase;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 14/11/16.
 */

public class PremiumDialog extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "PremiumDialog";

    //    ViewPager topPager;
    RecyclerView bottomPager;
    LinearLayoutManager layoutManager;
    TopPagerAdapter topPagerAdapter;
    RecyclerViewAdapter bottomPagerAdapter;
    //    CirclePageIndicator pageIndicator;
    ProgressWheel progress;
    TextView cancel, continu;

    private static final int DROP_IN_REQUEST = 100;
    int height, width, selectedHeight;
    String selectedPosition = "", premiumId = "", premiumPrice = "", currencyCode = "", payment = "", base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArR0dSr8yHmtBE/jN3fzuuhl8Y+vZ3qKb2P9NBHoB3ZZ4bc4Xi2NkfhiLFmBmMcR3b96DogrqS1EEKr8fDL23ydwuji4OdDc/w8OCyMFA+/dpU3cvNkA+ZqtgG/296+0l4uS2FVzsvN8ARs+nTWlRAWnV9zKcd+se96B/dtcLceIH8vXbul/8ALg1maX2EIQ4JHC4wbN7EV/A4EstCruVL0ZUBt2kulySGJg+XpCq2LGNPMyPb7Pi87ZIU8KYhO4uLpDoicsSyTbkvcL63aFsWGBwe7yRHjouHXmqYHj+IKtFwepRlKMwt8ZAJbAD4v9y0NzR3FQcqDQbCDh2irG2OwIDAQAB";
    ArrayList<HashMap<String, String>> premiumAry = new ArrayList<HashMap<String, String>>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    boolean payClicked = false;
    IabHelper mHelper;
    ArrayList<String> skuList = new ArrayList<String>();
    String currencySym = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.premium_dialog);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        topPager = findViewById(R.id.top_pager);
        bottomPager = findViewById(R.id.bottom_pager);
//        pageIndicator = findViewById(R.id.pager_indicator);
        cancel = findViewById(R.id.cancel);
        continu = findViewById(R.id.continu);
        progress = findViewById(R.id.progress);

//        pageIndicator.setFillColor(getResources().getColor(R.color.colorPrimary));

        ArrayList<HashMap<String, String>> premiumList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("image", "sub_1");
        map1.put("title", getString(R.string.change_your_location));
        map1.put("subtext", getString(R.string.change_loc_des));
        premiumList.add(map1);
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("image", "sub_2");
        map2.put("title", getString(R.string.send_more_likes));
        map2.put("subtext", getString(R.string.send_likes_des));
        premiumList.add(map2);
        HashMap<String, String> map3 = new HashMap<String, String>();
        map3.put("image", "sub_3");
        map3.put("title", getString(R.string.rewind_lastswipe));
        map3.put("subtext", getString(R.string.rewind_swipe_des));
        premiumList.add(map3);

        topPagerAdapter = new TopPagerAdapter(this, premiumList);
//        topPager.setAdapter(topPagerAdapter);
//        pageIndicator.setViewPager(topPager);

        height = HowzuApplication.dpToPx(this, 130);
        width = HowzuApplication.dpToPx(this, 100);
        selectedHeight = HowzuApplication.dpToPx(this, 140);

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

//        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(manager);
        bottomPager.setLayoutManager(manager);
        bottomPagerAdapter = new RecyclerViewAdapter(premiumAry);
        bottomPager.setAdapter(bottomPagerAdapter);
        bottomPager.setHasFixedSize(true);

        premiumList();

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition.equals("")) {
                    Toast.makeText(PremiumDialog.this, "Please choose your plan", Toast.LENGTH_SHORT).show();
                } else {
                    if (payment.equals("inapp")) {
                        try {
                            if (mHelper != null) {
                                mHelper.launchPurchaseFlow(PremiumDialog.this, premiumId, 10001,
                                        new IabHelper.OnIabPurchaseFinishedListener() {
                                            public void onIabPurchaseFinished(IabResult result,
                                                                              Purchase purchase) {
                                                if (result.isFailure()) {
                                                    // Handle error
                                                    return;
                                                } else if (purchase.getSku().equals(premiumId)) {
                                                    consumeItem();
                                                }

                                            }
                                        }, "purchasetoken");
                            }
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    } else if (payment.equals("braintree")) {
                        if (!payClicked) {
                            payClicked = true;
                            generateToken();
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                payBraintree(paymentMethodNonce);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // canceled
                Toast.makeText(PremiumDialog.this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // an error occurred, checked the returned exception
                Exception exception = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                CommonFunctions.dialog(PremiumDialog.this, "Failed", "Sorry, Payment gateway server is not working now, please try again later!");
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (mHelper != null && !mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
    }

    public static Bitmap getAssetImage(Context context, String filename) throws IOException {
        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open(filename + ".png")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return bitmap;
    }

    private void initInApp() {
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " +
                            result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                    List subsku = new ArrayList();
                    try {
                        mHelper.queryInventoryAsync(true, skuList, subsku, new IabHelper.QueryInventoryFinishedListener() {
                            public void onQueryInventoryFinished(IabResult result,
                                                                 Inventory inventory) {
                                if (result.isFailure()) {
                                    // handle error here
                                } else {
                                    for (int i = 0; i < skuList.size(); i++) {
                                        Log.v(TAG, "skudetails=" + inventory.getSkuDetails(skuList.get(i)));
                                        if (inventory.getSkuDetails(skuList.get(i)) != null) {
                                            String price = inventory.getSkuDetails(skuList.get(i)).getPrice();
                                            premiumAry.get(i).put(Constants.TAG_PRICE, price);
                                        }
                                    }

                                    Log.v(TAG, "updated premium-premiumAry=" + premiumAry);

                                    progress.stopSpinning();
                                    progress.setVisibility(View.GONE);
                                    bottomPager.setVisibility(View.VISIBLE);
                                    bottomPagerAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void consumeItem() {
        try {
            mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result,
                                                     Inventory inventory) {
                    if (result.isFailure()) {
                        // Handle failure
                    } else {
                        try {
                            mHelper.consumeAsync(inventory.getPurchase(premiumId),
                                    new IabHelper.OnConsumeFinishedListener() {
                                        public void onConsumeFinished(Purchase purchase,
                                                                      IabResult result) {
                                            Log.v(TAG, "result=" + result.toString());
                                            if (result.isSuccess()) {
                                                Log.v(TAG, "Success=");
                                                payInapp();
                                            } else {
                                                // handle error
                                                Log.v(TAG, "error");
                                            }
                                        }
                                    });
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    class TopPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> data;

        public TopPagerAdapter(Context act, ArrayList<HashMap<String, String>> newary) {
            this.data = newary;
            this.context = act;
        }

        public int getCount() {
            return data.size();
        }

        public Object instantiateItem(ViewGroup collection, int posi) {
            inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.premium_top_pager,
                    collection, false);

            HashMap<String, String> tempMap = data.get(posi);

            TextView title = itemView.findViewById(R.id.title);
            TextView subText = itemView.findViewById(R.id.sub_text);

            title.setText(tempMap.get("title"));
            subText.setText(tempMap.get("subtext"));

            try {
                ImageView image = itemView.findViewById(R.id.imageViewProfileImage);
                image.setImageBitmap(getAssetImage(context, tempMap.get("image")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            collection.addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
        ArrayList<HashMap<String, String>> lists;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> lists) {
            this.lists = lists;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.premium_bottom_pager, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {

            if (position == 0 || position == 1) {
                GridLayoutManager.LayoutParams layoutParams= (GridLayoutManager.LayoutParams) holder.item_layout.getLayoutParams();
                layoutParams.setMargins(0, -2, 0, 0);
                holder.item_layout.setLayoutParams(layoutParams);
            }else if(position==(lists.size()-1)){
                GridLayoutManager.LayoutParams layoutParams= (GridLayoutManager.LayoutParams) holder.item_layout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, -2);
                holder.item_layout.setLayoutParams(layoutParams);
            }

            HashMap<String, String> tempMap = lists.get(position);
//            holder.count.setText(tempMap.get(Constants.TAG_DAYS));
//            holder.duration.setText("Days");
//            holder.price.setText(currencySym + tempMap.get(Constants.TAG_PRICE));
            holder.priceBottom.setText(currencySym + tempMap.get(Constants.TAG_PRICE));

            if (selectedPosition.equals("")) {
                holder.main.getLayoutParams().height = height;
                holder.content.setBackgroundDrawable(getResources().getDrawable(R.drawable.gary_rounded));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                holder.content.setLayoutParams(layoutParams);
                holder.price.setVisibility(View.VISIBLE);
            } else {
                if (selectedPosition.equals(String.valueOf(position))) {
//                    holder.main.setAnimation(AnimationUtils.loadAnimation(PremiumDialog.this, R.anim.bounce));
                    holder.main.getLayoutParams().height = height;
                    holder.content.setBackgroundDrawable(getResources().getDrawable(R.drawable.pink_rounded));
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                    holder.content.setLayoutParams(layoutParams);
                } else {
                    holder.main.getLayoutParams().height = height;
                    holder.content.setBackgroundDrawable(getResources().getDrawable(R.drawable.gary_rounded));
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                    holder.content.setLayoutParams(layoutParams);
                    holder.price.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return lists.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView duration, price, priceBottom;
            RelativeLayout main;
            LinearLayout content, item_layout;

            public MyViewHolder(View view) {
                super(view);
                duration = view.findViewById(R.id.duration);
                price = view.findViewById(R.id.price);
                main = view.findViewById(R.id.main);
                priceBottom = view.findViewById(R.id.price_bottom);
                content = view.findViewById(R.id.content);
                item_layout = view.findViewById(R.id.item_layout);

                main.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.main:
                        int pos = getAdapterPosition();
                        selectedPosition = String.valueOf(pos);
                        premiumId = premiumAry.get(pos).get(Constants.TAG_ID);
                        String price = premiumAry.get(pos).get(Constants.TAG_PRICE);
//                        String[] value = price.split("\\s+");
//                        premiumPrice = value[1];
                        premiumPrice = price;
                        bottomPagerAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    /**
     * API Implementation
     */

    private void premiumList() {
        progress.setVisibility(View.VISIBLE);
        progress.spin();
        bottomPager.setVisibility(View.INVISIBLE);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PREMIUM_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "premiumListRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_CURRENCY).contains("-")) {
                                String[] curn = DefensiveClass.optString(json, Constants.TAG_CURRENCY).split("-");
                                currencyCode = curn[0];
                                currencySym = curn[1];
                            }
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                payment = DefensiveClass.optString(json, Constants.TAG_PAYMENT);
                                base64EncodedPublicKey = DefensiveClass.optString(json, Constants.TAG_LICENSE_KEY);

                                JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject temp = result.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                                    map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                    map.put(Constants.TAG_PRICE, DefensiveClass.optString(temp, Constants.TAG_PRICE));
                                    map.put(Constants.TAG_DAYS, DefensiveClass.optString(temp, Constants.TAG_DAYS));
                                    skuList.add(DefensiveClass.optString(temp, Constants.TAG_ID));
                                    premiumAry.add(map);
                                }

                                if (payment.equals("inapp")) {
                                    initInApp();
                                } else {
                                    progress.stopSpinning();
                                    progress.setVisibility(View.GONE);
                                    bottomPager.setVisibility(View.VISIBLE);
                                    bottomPagerAdapter.notifyDataSetChanged();
                                }

                                /*Bundle querySkus = new Bundle();
                                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                                Bundle skuDetails = mHelper.getSkuDetails(3, getPackageName(), “inapp”, querySkus);

                                int response = skuDetails.getInt("RESPONSE_CODE");
                                if (response == 0) {
                                    ArrayList responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                                    for (String thisResponse : responseList) {
                                        JSONObject object = new JSONObject(thisResponse);
                                        String sku = object.getString("productId");
                                        String price = object.getString("price");
                                        if (sku.equals(“premiumUpgrade”)) {
                                            mPremiumUpgradePrice = price;
                                        } else if (sku.equals(“gas”)) {
                                            mGasPrice = price;
                                        }
                                    }
                                }*/

                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(PremiumDialog.this, "Error", json.getString("message"));
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
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("platform", "android");
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void generateToken() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GENERATE_CLIENT_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        payClicked = false;
                        try {
                            Log.v(TAG, "generateTokenRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Cart cart = Cart.newBuilder()
                                        .setCurrencyCode(currencyCode)
                                        .setTotalPrice(premiumPrice)
                                        .addLineItem(LineItem.newBuilder()
                                                .setCurrencyCode(currencyCode)
                                                .setDescription("Description")
                                                .setQuantity("1")
                                                .setUnitPrice(premiumPrice)
                                                .setTotalPrice(premiumPrice)
                                                .build())
                                        .build();

                                DropInRequest dropInRequest = new DropInRequest()
                                        .clientToken(DefensiveClass.optString(json, Constants.TAG_TOKEN))
                                        .amount(premiumPrice)
                                        .androidPayCart(cart);
                                startActivityForResult(dropInRequest.getIntent(PremiumDialog.this), DROP_IN_REQUEST);
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(PremiumDialog.this, "Error", json.getString("message"));
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
                payClicked = false;
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void payBraintree(final String nonce) {
        final ProgressDialog pd = new ProgressDialog(PremiumDialog.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PAY_PREMIUM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        pd.dismiss();
                        try {
                            Log.v(TAG, "payBraintreeRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setPremium(true);
                                editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, true);
                                editor.commit();
                                MainScreenActivity.batch.setVisibility(View.VISIBLE);
                                PremiumSuccess premiumSuccess = new PremiumSuccess(PremiumDialog.this);
                                premiumSuccess.show();
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(PremiumDialog.this, "Error", json.getString("message"));
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
                pd.dismiss();
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
                map.put(Constants.TAG_PREMIUM_ID, premiumId);
                map.put(Constants.TAG_PRICE, premiumPrice);
                map.put(Constants.TAG_PAY_NONCE, nonce);
                map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
                Log.v(TAG, "payBraintreeParams=" + map);
                return map;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(20000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void payInapp() {
        final ProgressDialog pd = new ProgressDialog(PremiumDialog.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MANUAL_PAY_PREMIUM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        pd.dismiss();
                        try {
                            Log.v(TAG, "payInappRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                GetSet.setPremium(true);
                                editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, true);
                                editor.commit();
                                MainScreenActivity.batch.setVisibility(View.VISIBLE);
                                PremiumSuccess premiumSuccess = new PremiumSuccess(PremiumDialog.this);
                                premiumSuccess.show();
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(PremiumDialog.this, "Error", json.getString("message"));
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
                pd.dismiss();
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
                map.put(Constants.TAG_PREMIUM_ID, premiumId);
                Log.v(TAG, "payInappParams=" + map);
                return map;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(20000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }
}
