package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

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

public class Message extends Fragment implements AdapterView.OnItemClickListener, TextWatcher, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static Context context;
    String TAG = "Message";
    EditText searchEdit;
    ImageView cancelBtn, backbtn, nullImage;
    TextView nullText;
    RelativeLayout searchLay, main, spinLay;
    ProgressWheel progress;
    LinearLayout nullLay;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeLayout;
    Display display;
    LinearLayoutManager mLayoutManager;
    InputMethodManager imm;
    ArrayList<HashMap<String, String>> matchesAry = new ArrayList<HashMap<String, String>>()
            , searchAry = new ArrayList<HashMap<String, String>>();
    boolean loading = true, loadmore = false, pulldown = false;
    String searchKey = "";
    MessageAdapter messageAdapter;
    int currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount, visibleThreshold = 5, previousTotal = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private boolean appNotClicked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_main_layout, container, false);
        Log.i(TAG, "message_main_layout: onCreateView");

        progress = view.findViewById(R.id.progress);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        main = view.findViewById(R.id.main);
        nullLay = view.findViewById(R.id.nullLay);
        nullImage = view.findViewById(R.id.nullImage);
        nullText = view.findViewById(R.id.nullText);
        swipeLayout = view.findViewById(R.id.swipeLayout);

        searchLay = getActivity().findViewById(R.id.search_lay);
        spinLay = getActivity().findViewById(R.id.spinLay);
        searchEdit = getActivity().findViewById(R.id.searchbar);
        cancelBtn = getActivity().findViewById(R.id.cancelbtn);
        backbtn = getActivity().findViewById(R.id.backbtn);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainScreenActivity.setToolBar(getActivity(), "message");
        matchesAry.clear();
        searchAry.clear();
        progress.setVisibility(View.VISIBLE);
        progress.spin();
        currentPage = 0;
        previousTotal = 0;

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        context = getActivity();
        display = getActivity().getWindowManager().getDefaultDisplay();
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        MainScreenActivity.searchbtn.setOnClickListener(this);
        MainScreenActivity.spinLay.setOnClickListener(this);
        setUpRecyclerView();

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        getChat("message", currentPage);

        CommonFunctions.setupUI(getActivity(), getView());

        cancelBtn.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        searchEdit.addTextChangedListener(this);
        swipeLayout.setOnRefreshListener(this);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.v("search1", "search1");
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Log.v("search2", "search2");
                        if (searchLay.getVisibility() == View.VISIBLE) {
                            spinLay.setVisibility(View.GONE);
                            imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                            searchLay.setVisibility(View.GONE);
                            searchEdit.setText("");
                            nullLay.setVisibility(View.GONE);
                            messageAdapter = new MessageAdapter(matchesAry);
                            mRecyclerView.setAdapter(messageAdapter);
                            if (matchesAry.size() == 0) {
                                progress.setVisibility(View.VISIBLE);
                                currentPage = 0;
                                previousTotal = 0;
                                getChat("message", currentPage);
                            } else {
                                progress.setVisibility(View.GONE);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            cancelBtn.setVisibility(View.VISIBLE);
        } else {
            cancelBtn.setVisibility(View.GONE);
        }
    }


    @Override
    public void afterTextChanged(Editable s) {
        filter(s.toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (searchKey.equals("")) {
                if (matchesAry.size() > 0) {
                    HashMap<String, String> tempMap = matchesAry.get(position);
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("data", tempMap);
                    i.putExtra("position", position);
                    context.startActivity(i);
                }
            } else {
                if (searchAry.size() > 0) {
                    HashMap<String, String> tempMap = searchAry.get(position);
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("data", tempMap);
                    i.putExtra("position", position);
                    context.startActivity(i);
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /*private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.de);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                MessageAdapter messageAdapter = (MessageAdapter) recyclerView.getAdapter();
                if (messageAdapter.isPositionFooter(position) || messageAdapter.isPositionHeader(position)) {
                    Log.v("getSwipeHeads", "if");
                    return 0;
                }
                if (messageAdapter.isUndoOn() && messageAdapter.isPendingRemoval(position - 1)) {
                    Log.v("getSwipeDirs", "if");
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                MessageAdapter adapter = (MessageAdapter) mRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    Log.v("undo", "undo");
                    adapter.pendingRemoval(swipedPosition - 1);
                } else {
                    adapter.remove(swipedPosition - 1);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if ((viewHolder.getAdapterPosition() - 1) == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicHeight();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;

                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }*/


    /*private void setUpAnimationDecoratorHelper() {
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one strVisitingIdLikeToken the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }*/


    @Override
    public void onResume() {
        super.onResume();
        setOnlineStatus();
        Log.v(TAG, "resume");
        if (ChatActivity.fromChat) {
            ChatActivity.fromChat = false;
            searchLay.setVisibility(View.GONE);
            searchEdit.setText("");
            MainScreenActivity.spinText.setText(getString(R.string.all_message));
            nullLay.setVisibility(View.GONE);
            matchesAry.clear();
            searchAry.clear();
            currentPage = 0;
            previousTotal = 0;
            getChat("message", currentPage);
            messageAdapter = new MessageAdapter(matchesAry);
            mRecyclerView.setAdapter(messageAdapter);
        }
    }

    @Override
    public void onRefresh() {
        Log.v(TAG, "onRefresh=" + swipeLayout.isRefreshing());
        if (!pulldown) {
            if (searchLay.getVisibility() == View.VISIBLE) {
                spinLay.setVisibility(View.GONE);
                pulldown = true;
                getChat("search", 0);
            } else {
                currentPage = 0;
                previousTotal = 0;
                pulldown = true;
                nullLay.setVisibility(View.GONE);
                getChat("message", currentPage);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private String getSort() {
        String sort = "all";
        String spinTxt = MainScreenActivity.spinText.getText().toString();
        if (spinTxt.equals(getString(R.string.all_message))) {
            sort = "all";
        } else if (spinTxt.equals(getString(R.string.unread))) {
            sort = "unread";
        } else if (spinTxt.equals(getString(R.string.favorite))) {
            sort = "favourite";
        } else {
            sort = "online";
        }
        return sort;
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        messageAdapter = new MessageAdapter(matchesAry);
        messageAdapter.setUndoOn(false);
        mRecyclerView.setAdapter(messageAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = mRecyclerView.getChildCount();
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
                        getChat("message", currentPage);
                        loading = true;
                    }
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);

    }

    private void setNullText() {
        String spinTxt = MainScreenActivity.spinText.getText().toString();
        if (spinTxt.equals(getString(R.string.all_message)) || spinTxt.equals(getString(R.string.unread))) {
            nullImage.setImageResource(R.drawable.no_message);
            nullText.setText(getString(R.string.no_conversation));
        } else if (spinTxt.equals(getString(R.string.favorite))) {
            nullImage.setImageResource(R.drawable.no_friends);
            nullText.setText(getString(R.string.no_favorite));
        } else {
            nullImage.setImageResource(R.drawable.no_friends);
            nullText.setText(getString(R.string.no_online));
        }
    }

    void filter(String text) {
        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
        for (HashMap<String, String> name : searchAry) {
            //or use .contains(text)
            if (name.get(Constants.TAG_USERNAME).toLowerCase().startsWith(text.toLowerCase())) {
                temp.add(name);
            }
        }
        //update recyclerview
        messageAdapter.updateList(temp);
    }

    public void Appdialog(ArrayList<String> imagesAry, String title, String des, final String url) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.app_of_day_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);

        TextView appName = dialog.findViewById(R.id.app_name);
        TextView appDes = dialog.findViewById(R.id.app_des);
        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView download = dialog.findViewById(R.id.download);
        ViewPager viewPager = dialog.findViewById(R.id.view_pager);
        CirclePageIndicator pageIndicator = dialog.findViewById(R.id.pager_indicator);
        pageIndicator.setFillColor(getResources().getColor(R.color.colorPrimary));

        appName.setText(title);
        appDes.setText(des);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getActivity(), imagesAry);
        viewPager.setAdapter(pagerAdapter);
        pageIndicator.setViewPager(viewPager);

        download.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(b);
                } else {
                    Toast.makeText(context, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
            appNotClicked = false;
        }

    }

    /**
     * function for showing the popup window
     **/

    public void viewOptions(View v) {
        final ArrayList<String> values = new ArrayList<>();
        values.add(getString(R.string.unread));
        values.add(getString(R.string.online));
        values.add(getString(R.string.favorite));

        if (!MainScreenActivity.spinText.getText().toString().equals(getString(R.string.all_message))) {
            int index = values.indexOf(MainScreenActivity.spinText.getText().toString());
            values.remove(index);
            values.add(0, getString(R.string.all_message));
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

        final ListView lv = layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MainScreenActivity.spinText.setText(values.get(position));
                popup.dismiss();
                matchesAry.clear();
                messageAdapter.notifyDataSetChanged();
                currentPage = 0;
                previousTotal = 0;
                nullLay.setVisibility(View.GONE);
                getChat("message", currentPage);
            }
        });
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

    /**
     * API Implementation
     **/

    private void favorite(final String userId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FAVORITE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "favoriteRes=" + res);
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
//                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());

                map.put(Constants.TAG_FAVOURITE_USER_ID, userId);
                Log.v(TAG, "favoriteParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void getAppOfDay() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final StringRequest req = new StringRequest(Request.Method.POST, Constants.API_APP_OF_DAY,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        dialog.dismiss();
                        try {
                            Log.v(TAG, "getAppOfDayRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                                JSONArray images = result.getJSONArray(Constants.TAG_IMAGES);
                                ArrayList<String> imagesAry = new ArrayList<String>();
                                for (int i = 0; i < images.length(); i++) {
                                    String imag = images.getString(i);
                                    imagesAry.add(imag);
                                }
                                Appdialog(imagesAry, DefensiveClass.optString(result, Constants.TAG_TITLE)
                                        , DefensiveClass.optString(result, Constants.TAG_DESCRIPTION)
                                        , DefensiveClass.optString(result, Constants.TAG_VIEW_URL));
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString("message"));
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
                appNotClicked = false;
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

    private void setOnlineStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "setOnlineStatusRes=" + res);
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
 //               map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());

                map.put(Constants.TAG_STATUS, "1");
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "setOnlineStatusParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    public void getChat(final String from, final int offset) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (from.equals("message") && matchesAry.size() == 0) {
                    if (pulldown) {
                        swipeRefresh(true);
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        progress.spin();
                        messageAdapter.showLoading(false);
                    }
                } else if (from.equals("search") && searchAry.size() == 0) {
                    if (pulldown) {
                        swipeRefresh(true);
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        progress.spin();
                        messageAdapter.showLoading(false);
                    }
                } else if (from.equals("message") && matchesAry.size() >= 20) {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    messageAdapter.showLoading(true);
                    messageAdapter.notifyDataSetChanged();
                } else {
                    progress.stopSpinning();
                    progress.setVisibility(View.GONE);
                    messageAdapter.showLoading(false);
                }
            }
        });
        StringRequest getChat = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "jigar the chat list response we have getChatRes=" + res);
                        try {
                            if (pulldown) {
                                pulldown = false;
                                if (from.equals("message")) {
                                    matchesAry.clear();
                                } else {
                                    searchAry.clear();
                                }
                            }
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                JSONArray result = json.optJSONArray(Constants.TAG_MATCHES);
                                if (result != null) {
                                    for (int i = 0; i < result.length(); i++) {
                                        HashMap<String, String> map = new HashMap<String, String>();
                                        JSONObject temp = result.getJSONObject(i);
                                        map.put(Constants.TAG_CHAT_ID, DefensiveClass.optInt(temp, Constants.TAG_CHAT_ID));
                                        map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(temp, Constants.TAG_USERIMAGE));
                                        map.put(Constants.TAG_USERNAME, DefensiveClass.optString(temp, Constants.TAG_USERNAME));
                                        map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(temp, Constants.TAG_MESSAGE));
                                        map.put(Constants.TAG_USERID, DefensiveClass.optInt(temp, Constants.TAG_USERID));
                                        map.put(Constants.TAG_CHAT_TIME, DefensiveClass.optInt(temp, Constants.TAG_CHAT_TIME));
                                        map.put(Constants.TAG_FAVORITE, DefensiveClass.optInt(temp, Constants.TAG_FAVORITE));
                                        map.put(Constants.TAG_ONLINE, DefensiveClass.optInt(temp, Constants.TAG_ONLINE));
                                        map.put(Constants.TAG_LAST_ONLINE, DefensiveClass.optInt(temp, Constants.TAG_LAST_ONLINE));
                                        map.put(Constants.TAG_LAST_REPLIED, DefensiveClass.optInt(temp, Constants.TAG_LAST_REPLIED));
                                        map.put(Constants.TAG_BLOCK, DefensiveClass.optInt(temp, Constants.TAG_BLOCK));
                                        map.put(Constants.TAG_BLOCKED_BY_ME, DefensiveClass.optInt(temp, Constants.TAG_BLOCKED_BY_ME));
                                        map.put(Constants.TAG_LAST_TO_READ, DefensiveClass.optInt(temp, Constants.TAG_LAST_TO_READ));
                                        map.put(Constants.TAG_USER_STATUS, DefensiveClass.optInt(temp, Constants.TAG_USER_STATUS));
                                        map.put(Constants.TAG_CLEAR_CHAT, DefensiveClass.optInt(temp, Constants.TAG_CLEAR_CHAT));
                                        map.put(Constants.TAG_TYPE, DefensiveClass.optInt(temp, Constants.TAG_TYPE));

                                        if (from.equals("message")) {
                                            matchesAry.add(map);
                                        } else {
                                            searchAry.add(map);
                                        }
                                    }
                                }
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(context, "Error", json.getString("message"));
                            } else {

                            }

                            if (from.equals("message") && matchesAry.size() >= 20) {
                                loadmore = true;
                            } else {
                                loadmore = false;
                            }

                            if (from.equals("message") && matchesAry.size() == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                                setNullText();
                            } else if (from.equals("search") && searchAry.size() == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                                setNullText();
                            } else {
                                nullLay.setVisibility(View.GONE);
                            }

                            progress.stopSpinning();
                            progress.setVisibility(View.GONE);
                            swipeRefresh(false);
                            messageAdapter.showLoading(false);
                            messageAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Log.d(TAG, "jigar the error in json chat list response we have getChatRes=" + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.d(TAG, "jigar the error in null pointer chat list we have " + e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.d(TAG, "jigar the error main exception chat list we have " + e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.d(TAG, "jigar the volley error chat list we have " + error.getMessage());

                pulldown = false;
                progress.stopSpinning();
                progress.setVisibility(View.GONE);
                messageAdapter.showLoading(false);
                swipeLayout.setRefreshing(false);
                messageAdapter.notifyDataSetChanged();
                if (from.equals("message") && matchesAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    setNullText();
                } else if (from.equals("search") && searchAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    setNullText();
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
//                params.put(Constants.TAG_USERID, GetSet.getUserId());
                params.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());

                params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                if (from.equals("search")) {
                    params.put(Constants.TAG_SEARCH_KEY, "1");
                    params.put(Constants.TAG_SORT, "all");
                } else {
                    params.put(Constants.TAG_SEARCH_KEY, "");
                    params.put(Constants.TAG_SORT, getSort());
                    params.put(Constants.TAG_OFFSET, String.valueOf(offset * 20));
                    params.put(Constants.TAG_LIMIT, "20");
                }
                Log.v(TAG, "getChatParams=" + params);
                Log.d(TAG, "jigar the chat list params have list " + params);
                return params;
            }
        };
        HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
        HowzuApplication.getInstance().addToRequestQueue(getChat, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelbtn:
                searchKey = "";
                searchEdit.setText("");
                cancelBtn.setVisibility(View.GONE);
                break;
            case R.id.searchbtn:
                searchLay.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
                swipeRefresh(false);
                messageAdapter = new MessageAdapter(searchAry);
                mRecyclerView.setAdapter(messageAdapter);
                if (searchAry.size() == 0) {
                    getChat("search", 0);
                } else {
                    progress.setVisibility(View.GONE);
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
                swipeRefresh(false);
                messageAdapter = new MessageAdapter(matchesAry);
                mRecyclerView.setAdapter(messageAdapter);
                if (matchesAry.size() == 0) {
                    currentPage = 0;
                    previousTotal = 0;
                    getChat("message", currentPage);
                } else {
                    progress.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * ViewHolder capable of presenting two states: "normal" and "undo" state.
     */

    static class AdapViewHolder extends RecyclerView.ViewHolder {

        TextView deleted, comment, username;
        Button undoButton;
        ImageView userimg, favorite, online;
        RelativeLayout dataLay;

        public AdapViewHolder(View itemView) {
            super(itemView);

            userimg = itemView.findViewById(R.id.user_image);
            username = itemView.findViewById(R.id.user_name);
            comment = itemView.findViewById(R.id.comment);
            favorite = itemView.findViewById(R.id.favorite);
            deleted = itemView.findViewById(R.id.deleted);
            undoButton = itemView.findViewById(R.id.undo_button);
            dataLay = itemView.findViewById(R.id.data_lay);
            online = itemView.findViewById(R.id.online);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout appLay;

        public HeaderViewHolder(View parent) {
            super(parent);

            appLay = parent.findViewById(R.id.app_lay);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        ProgressWheel progress;

        public FooterViewHolder(View parent) {
            super(parent);

            progress = parent.findViewById(R.id.footer_progress);
        }
    }

    /**
     * RecyclerView adapter enabling undo on a swiped away item.
     */

    class MessageAdapter extends RecyclerView.Adapter {
        // 3000=3sec
        private static final int PENDING_REMOVAL_TIMEOUT = 3000, TYPE_HEADER = 0, TYPE_ITEM = 1, TYPE_FOOTER = 2;
        protected boolean showLoader;
        ArrayList<HashMap<String, String>> messageList;
        List<String> itemsPendingRemoval = new ArrayList<>();
        boolean undoOn; // is undo on, you can turn it on strVisitingIdLikeToken the toolbar menu
        HashMap<String, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
        private Handler handler = new Handler(); // hanlder for running delayed runnables

        public MessageAdapter(ArrayList<HashMap<String, String>> data) {
            messageList = data;
            itemsPendingRemoval = new ArrayList<>();
        }

        public void updateList(ArrayList<HashMap<String, String>> data) {
            messageList = data;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_header, parent, false);
                return new HeaderViewHolder(v);
            } else if (viewType == TYPE_FOOTER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loader, parent, false);
                return new FooterViewHolder(v);
            } else if (viewType == TYPE_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
                return new AdapViewHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                headerHolder.appLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!appNotClicked){
                            appNotClicked = true;
                            getAppOfDay();
                        }
                    }
                });
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                if (showLoader) {
                    footerHolder.progress.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.progress.setVisibility(View.GONE);
                }
            } else if (holder instanceof AdapViewHolder) {
                final AdapViewHolder viewHolder = (AdapViewHolder) holder;
                final HashMap<String, String> tempMap = messageList.get(position - 1);
                final String item = tempMap.get(Constants.TAG_CHAT_ID);

                if (itemsPendingRemoval.contains(item)) {
                    // we need to show the "undo" state of the row
                    viewHolder.itemView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    viewHolder.dataLay.setVisibility(View.INVISIBLE);
                    viewHolder.undoButton.setVisibility(View.VISIBLE);
                    viewHolder.deleted.setVisibility(View.VISIBLE);
                    viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // user wants to undo the removal, let's cancel the pending task
                            Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                            pendingRunnables.remove(item);
                            if (pendingRemovalRunnable != null)
                                handler.removeCallbacks(pendingRemovalRunnable);
                            itemsPendingRemoval.remove(item);
                            // this will rebind the row in "normal" state
                            notifyItemChanged(position);
                        }
                    });
                } else {
                    // we need to show the "normal" state
                    viewHolder.itemView.setBackgroundColor(Color.WHITE);
                    viewHolder.dataLay.setVisibility(View.VISIBLE);
                    viewHolder.deleted.setVisibility(View.GONE);
                    viewHolder.undoButton.setVisibility(View.GONE);
                    viewHolder.undoButton.setOnClickListener(null);

//                    String img = Constants.RESIZE_URL + CommonFunctions.getImageName(tempMap.get(Constants.TAG_USERIMAGE)) + Constants.IMAGE_RES;
                    String img =tempMap.get(Constants.TAG_USERIMAGE) ;
                    Log.d(TAG,"jigar the user image we before resolution is have is "+tempMap.get(Constants.TAG_USERIMAGE));

                    Log.d(TAG,"jigar the user image we have is "+img);
                    if (tempMap.get(Constants.TAG_USER_STATUS).equals("1")) {
                        Picasso.with(getActivity()).load(img)
                                .placeholder(R.drawable.user_placeholder)
                                .error(R.drawable.user_placeholder)
                                .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                                .centerCrop()
                                .into(viewHolder.userimg);
                    } else {
                        Picasso.with(getActivity()).load(R.drawable.user_placeholder).into(viewHolder.userimg);
                    }

                    String str =tempMap.get(Constants.TAG_USERNAME);
                    String strUserName = str.substring(0, 1).toUpperCase() + str.substring(1);
                    viewHolder.username.setText(strUserName);


                    if (tempMap.get(Constants.TAG_LAST_TO_READ).equals(tempMap.get(Constants.TAG_USERID)) || tempMap.get(Constants.TAG_LAST_TO_READ).equals("0")) {
                        viewHolder.username.setTextColor(getResources().getColor(R.color.primaryText));
                        viewHolder.comment.setTypeface(viewHolder.comment.getTypeface(), Typeface.NORMAL);
                    } else {
                        viewHolder.username.setTextColor(getResources().getColor(R.color.colorPrimary));
                        viewHolder.comment.setTypeface(viewHolder.comment.getTypeface(), Typeface.BOLD);
                    }



                    if(tempMap.get(Constants.TAG_CLEAR_CHAT).equals("0")){
                        if (tempMap.get(Constants.TAG_MESSAGE).equals("") ) {
                            if (tempMap.get(Constants.TAG_LAST_REPLIED).equals(tempMap.get(Constants.TAG_USERID))) {
                                viewHolder.comment.setText("Sent a photo");
                            } else if (!tempMap.get(Constants.TAG_LAST_REPLIED).equals(tempMap.get(Constants.TAG_USERID))) {
                                viewHolder.comment.setText("You sent a photo");
                            }
                        }
                        else {
                            if(tempMap.get(Constants.TAG_TYPE).equals("missed")){
                                viewHolder.comment.setText("Missed "+tempMap.get(Constants.TAG_MESSAGE)+" call");
                            }else {
                                viewHolder.comment.setText(tempMap.get(Constants.TAG_MESSAGE));
                            }
                        }
                    }else {
                        viewHolder.comment.setText("");
                    }

                    if (tempMap.get(Constants.TAG_ONLINE).equals("1")) {
                        viewHolder.online.setVisibility(View.VISIBLE);
                        viewHolder.online.setImageResource(R.drawable.online);
                    } else if (tempMap.get(Constants.TAG_ONLINE).equals("2")) {
                        viewHolder.online.setVisibility(View.VISIBLE);
                        viewHolder.online.setImageResource(R.drawable.away);
                    } else {
                        viewHolder.online.setVisibility(View.GONE);
                    }

                    if (tempMap.get(Constants.TAG_FAVORITE).equals("true")) {
                        viewHolder.favorite.setImageResource(R.drawable.fav);
                    } else {
                        viewHolder.favorite.setImageResource(R.drawable.not_fav);
                    }

                    viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (tempMap.get(Constants.TAG_FAVORITE).equals("true")) {
                                viewHolder.favorite.setImageResource(R.drawable.not_fav);
                                tempMap.put(Constants.TAG_FAVORITE, "false");
                            } else {
                                viewHolder.favorite.setImageResource(R.drawable.fav);
                                tempMap.put(Constants.TAG_FAVORITE, "true");
                            }
                            favorite(tempMap.get(Constants.TAG_USERID));
                        }
                    });

                    viewHolder.dataLay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("data", messageList.get(position - 1));
                            i.putExtra("position", position - 1);
                            i.putExtra("from", "message");
                            startActivity(i);
                        }
                    });

                    viewHolder.userimg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (tempMap.get(Constants.TAG_USER_STATUS).equals("1")) {
                                Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                                p.putExtra("from", "friend");
                                p.putExtra("strFriendID", tempMap.get(Constants.TAG_USERID));
                                startActivity(p);
                            } else {
                                Toast.makeText(context, tempMap.get(Constants.TAG_USERNAME) + " " + getString(R.string.account_deactivated), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position)) {
                return TYPE_HEADER;
            } else if (isPositionFooter(position)) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        public void showLoading(boolean status) {
            showLoader = status;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        private boolean isPositionFooter(int position) {
            return position == messageList.size() + 1;
        }

        @Override
        public int getItemCount() {
            return messageList.size() + 2;
        }

        public boolean isUndoOn() {
            return undoOn;
        }

        public void setUndoOn(boolean undoOn) {
            this.undoOn = undoOn;
        }

        public void pendingRemoval(final int position) {
            final String item = messageList.get(position).get(Constants.TAG_CHAT_ID);
            if (!itemsPendingRemoval.contains(item)) {
                itemsPendingRemoval.add(item);
                // this will redraw row in "undo" state
                Log.v("position", "=" + position);
                notifyItemChanged(position + 1);
                // let's create, store and post a runnable to remove the item
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        remove(position);
                    }
                };
                handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
                pendingRunnables.put(item, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {
            final String item = messageList.get(position).get(Constants.TAG_CHAT_ID);
            if (itemsPendingRemoval.contains(item)) {
                itemsPendingRemoval.remove(item);
            }
            if (messageList.contains(messageList.get(position))) {
                messageList.remove(position);
                notifyItemRemoved(position + 1);
            }
        }

        public boolean isPendingRemoval(int position) {
            String item = messageList.get(position).get(Constants.TAG_CHAT_ID);
            return itemsPendingRemoval.contains(item);
        }
    }

    /*Onclick Event*/

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
            View itemView = inflater.inflate(R.layout.image_viewpager,
                    collection, false);

            try {
                ImageView image = (ImageView) itemView.findViewById(R.id.imgDisplay);
                if (!temp.get(posi).equals("")) {
                    int pixels = HowzuApplication.dpToPx(getContext(),150);
                    Picasso.with(context).load(temp.get(posi)).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder).resize(pixels,pixels).into(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((ViewPager) collection).addView(itemView, 0);

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
}
