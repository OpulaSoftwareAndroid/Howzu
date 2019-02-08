package com.hitasoft.app.howzu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hitasoft.app.customclass.FontCache;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ViewProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewProfileFragment newInstance(String param1, String param2) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

//    private void checkUser() {
//        if (from.equals("myprofile") || GetSet.getUserId().equals(strFriendID)) {
//            strFriendID = GetSet.getUserId();
//            setting.setVisibility(View.VISIBLE);
//            fab.setImageResource(R.drawable.pen);
//            fab2.setImageResource(R.drawable.pen);
//            showFab = true;
//            fab.show(fabListener);
//            optionbtn.setVisibility(View.GONE);
//            //iconsLay.setVisibility(View.GONE);
//            gradientFrame.setVisibility(View.GONE);
//        } else {
//            setting.setVisibility(View.GONE);
//            fab.setImageResource(R.drawable.msg);
//            fab2.setImageResource(R.drawable.msg);
//            optionbtn.setVisibility(View.VISIBLE);
//            switch (sendMatch) {
//                case "4":
//                    showFab = false;
//                    //      iconsLay.setVisibility(View.VISIBLE);
//                    gradientFrame.setVisibility(View.GONE);
//                    break;
//                case "3":
//                    showFab = false;
//                    //    iconsLay.setVisibility(View.GONE);
//                    gradientFrame.setVisibility(View.GONE);
//                    break;
//                case "5":
//                    showFab = false;
//                    //  iconsLay.setVisibility(View.VISIBLE);
//                    gradientFrame.setVisibility(View.GONE);
//                    break;
//                case "1":
//                    showFab = true;
//                    fab.show(fabListener);
//                    //   iconsLay.setVisibility(View.GONE);
//                    gradientFrame.setVisibility(View.GONE);
//                    break;
//                case "0":
//                    showFab = false;
//                    //  iconsLay.setVisibility(View.GONE);
//                    gradientFrame.setVisibility(View.GONE);
//                    break;
//            }
//            //visitProfile();
//        }
//    }
//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//        HowzuApplication.showSnack(this, findViewById(R.id.main_content), isConnected);
//    }
//
//
//    public void viewOptions(View v) {
//        Rect location = locateView(v);
//        String[] values;
////        if (haspMapProfileDetails.get(Constants.TAG_REPORT).equals("true")) {
////            values = new String[]{getString(R.string.undo_report), getString(R.string.unfriend_user)};
////        } else
//
//        if (from.equals("other") || from.equals("home")) {
//            values = new String[]{getString(R.string.report_user)};
//        } else if (sendMatch.equals("1")) {
//            values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
//        } else if (from.equals("visitors")) {
//            if (sendMatch.equals("1")) {
//                values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
//            } else {
//                values = new String[]{getString(R.string.report_user)};
//            }
//        } else {
//            values = new String[]{getString(R.string.report_user), getString(R.string.unfriend_user)};
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainViewProfileDetailActivity.this,
//                R.layout.options_item, android.R.id.text1, values);
//        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View layout = layoutInflater.inflate(R.layout.options, null);
//        // layout.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.linear_interpolator));
//        final PopupWindow popup = new PopupWindow(MainViewProfileDetailActivity.this);
//        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        popup.setContentView(layout);
//        popup.setWidth(display.getWidth() * 50 / 100);
//        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popup.setFocusable(true);
//        popup.showAtLocation(coordinatorLayout, Gravity.TOP | Gravity.LEFT, location.left, location.bottom);
//
//        final ListView lv = layout.findViewById(R.id.lv);
//        lv.setAdapter(adapter);
//        popup.showAsDropDown(v);
//
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                switch (position) {
//                    case 0:
//                        popup.dismiss();
//                        dialog("report");
//                        break;
//                    case 1:
//                        popup.dismiss();
//                        dialog("unfriend");
//                        break;
//                }
//            }
//        });
//    }
//    private void setProfile() {
//        if (haspMapProfileDetails.get(Constants.TAG_AGE_STATUS).equals("true") && !haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
//            String strUserName = haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(1);
//            collapsingToolbar.setTitle(strUserName);
//            collapsingToolbar.setCollapsedTitleTypeface(FontCache.get("font_regular.ttf", this));
//
//        } else {
//            collapsingToolbar.setTitle(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME) + ", " + haspMapProfileDetails.get(Constants.TAG_AGE));
//            String strUserName = haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME).substring(1)
//                    + ", " + haspMapProfileDetails.get(Constants.TAG_AGE);
//            collapsingToolbar.setTitle(strUserName);
//            collapsingToolbar.setCollapsedTitleTypeface(FontCache.get("font_regular.ttf", this));
//
//        }
//
//        if (haspMapProfileDetails.get(Constants.TAG_INFO).equals("")) {
//            info.setVisibility(View.GONE);
//        } else {
//            info.setText(haspMapProfileDetails.get(Constants.TAG_INFO));
//        }
//
//        if (haspMapProfileDetails.get(Constants.TAG_BIO).equals("")) {
//            bio.setVisibility(View.GONE);
//        } else {
//            String strBio = haspMapProfileDetails.get(Constants.TAG_BIO).substring(0,1).toUpperCase() + haspMapProfileDetails.get(Constants.TAG_BIO).substring(1);
//            bio.setText(Html.fromHtml(strBio));
//        }
//
//        if (!haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
//            if (haspMapProfileDetails.get(Constants.TAG_DISTANCE_STATUS).equals("true")) {
//                location.setVisibility(View.GONE);
//            } else {
//                location.setText("Lives in " + haspMapProfileDetails.get(Constants.TAG_LOCATION));
//            }
//        }
//
//        setAppbarListener();
//
////        if (!haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).equals(null)||
//        if(       !haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).equals("")) {
//            try {
//                List<String> aListInterest = Arrays.asList(haspMapProfileDetails.get(Constants.TAG_INTEREST_PLAN).split("\\s*,\\s*"));
//                System.out.println("jigar the array arrayListComments  in tab listner is "+aListInterest.toString());
//                System.out.println("jigar the array arrayListComments  size is tab listner is "+aListInterest.size());
//
//                JSONArray ints = new JSONArray(aListInterest);
//                for (int i = 0; i < ints.length(); i++) {
//                    interestsAry.add(arrayListInterestStatic.get(Integer.parseInt(ints.optString(i, ""))));
//                }
//            }
//
//            //            catch (JSONException e) {
////                System.out.println("jigar the error json exception in tablistner is "+e);
////                e.printStackTrace();
////            }
//            catch (NullPointerException e) {
//                System.out.println("jigar the error null pointer exception in tablistner is "+e);
//
//                e.printStackTrace();
//            } catch (Exception e) {
//                System.out.println("jigar the error main exception in tablistner is "+e);
//
//                e.printStackTrace();
//            }
//        }
//
//        if (interestsAry.size() == 0) {
//            interestLay.setVisibility(View.GONE);
//        } else {
//            interest.setText(interestsAry.size() + " interest");
//        }
//
//        for (int i = 0; i < interestsAry.size(); i++) {
//            TextView tv = new TextView(this);
//            tv.setText(interestsAry.get(i));
//            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//            tv.setTypeface(FontCache.get("font_regular.ttf", this));
//            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
//                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
//                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
//            lp.leftMargin = HowzuApplication.dpToPx(this, 7);
//            lp.rightMargin = HowzuApplication.dpToPx(this, 7);
//            tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_rounded_stroke_corner));
//            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
//            laybelLayout.addView(tv, lp);
//        }
//
//        if (!haspMapProfileDetails.get(Constants.TAG_IMAGES).equals(null) && !haspMapProfileDetails.get(Constants.TAG_IMAGES).equals("")) {
//            try {
//                // change this for combined array of images -----------------------------------------------------------------------------------------------
//                List<String> aListImage = Arrays.asList(haspMapProfileDetails.get(Constants.TAG_IMAGE).split("\\s*,\\s*"));
//                JSONArray arrayProfileImages = new JSONArray(aListImage);
//
////                JSONArray imgs = new JSONArray(haspMapProfileDetails.get(Constants.TAG_IMAGE));
//                for (int i = 0; i < arrayProfileImages.length(); i++) {
//                    imagesAry.add(arrayProfileImages.optString(i, ""));
//                }
//            }
////            catch (JSONException e) {
////                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);
////
////                e.printStackTrace();
////            }
//            catch (NullPointerException e) {
//                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);
//
//                e.printStackTrace();
//            } catch (Exception e) {
//                System.out.println("jigar the error null pointer exception in image arrayListComments is "+e);
//
//                e.printStackTrace();
//            }
//        }
//
//        if (imagesAry.size() == 0) {
//            imagesAry.add(haspMapProfileDetails.get(Constants.TAG_PROFILE_IMAGE));
//        }
//        pageIndicator.setVisibility(imagesAry.size() > 1 ? View.VISIBLE : View.GONE);
//
//        viewPagerAdapter = new MainViewProfileDetailActivity.ViewPagerAdapter(MainViewProfileDetailActivity.this, imagesAry);
//        viewPager.setAdapter(viewPagerAdapter);
//        pageIndicator.setViewPager(viewPager);
//
//        if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0") && haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
//            premiumLay.setVisibility(View.VISIBLE);
//            becomePremium.setVisibility(View.GONE);
//            membershipValid.setVisibility(View.VISIBLE);
//            batch.setVisibility(View.VISIBLE);
//            batch.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!fab.isShown()) {
//                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) batch.getLayoutParams();
//                        params.setMargins(0, HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 80), HowzuApplication.dpToPx(MainViewProfileDetailActivity.this, 15), 0);
//                        batch.setLayoutParams(params);
//                    }
//                    batch.show();
//                }
//            }, 50);
//            try {
//                if (!haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID).equals(null) && !haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID).equals("")) {
//                    long date = Long.parseLong(haspMapProfileDetails.get(Constants.TAG_MEMBERSHIP_VALID)) * 1000;
//                    membershipValid.setText("Membership valid upto " + getDate(date));
//                }
//            } catch (NumberFormatException e) {
//                System.out.println("jigar the error number format exception is "+e);
//                e.printStackTrace();
//            }
//        } else {
//            premiumLay.setVisibility(View.GONE);
//            batch.setVisibility(View.INVISIBLE);
//        }
//    }
//
//    private void setAppbarListener() {
//        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//                if (verticalOffset == 0) {
//                    backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryText));
//                    Log.v("expanded", "expanded");
//                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0"))
//                    {
//                        batch.show();
//                    }
//                    if (showFab) {
//                        fab.show(fabListener);
//                        fab2.hide(fabListener);
//                    } else {
//                        fab.hide(fabListener);
//                        fab2.hide(fabListener);
//                    }
//
//                    scrollState = "expanded";
//
//                    /*if (collapsed){
//                        collapsed = false;
//                        *//*fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animation) {
//                                CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
//                                p.setBehavior(new AppBarLayout.ScrollingViewBehavior());
//                                view.setLayoutParams(p);
//                                view.requestLayout();
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animation) {
//
//                            }
//                        }).start();*//*
//                     *//*fab.setY(coordinatorLayout.getHeight() - (fab.getHeight() * 3));
//                        fab.invalidate();*//*
//                    }*/
//                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
//                    Log.v("collapsed", "collapsed");
//                    scrollState = "collapsed";
//                    backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white));
//                    toolbarTitle.setVisibility(View.VISIBLE);
//                    if (haspMapProfileDetails.get(Constants.TAG_AGE_STATUS).equals("0") && !haspMapProfileDetails.get(Constants.TAG_REGISTERED_ID).equals(GetSet.getUserId())) {
//                        toolbarTitle.setText(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME));
//                    } else {
//                        toolbarTitle.setText(haspMapProfileDetails.get(Constants.TAG_NEW_USERNAME) + ", " + haspMapProfileDetails.get(Constants.TAG_AGE));
//                    }
//                    if (haspMapProfileDetails.get(Constants.TAG_PREMIUM_FROM).equals("0")) {
//                        batch.hide();
//                    }
//                    if (showFab) {
//                        fab.hide(fabListener);
//                        fab2.show(fabListener);
//                    } else {
//                        fab.hide(fabListener);
//                        fab2.hide(fabListener);
//                    }
//                    collapsed = true;
//                    /*CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
//                    p.setBehavior(null);
//                    view.setLayoutParams(p);
//                    view.requestLayout();
//                    fab.animate().translationY(coordinatorLayout.getHeight() - (fab.getHeight() * 3)).setInterpolator(new AccelerateInterpolator(2)).start();*/
//                    //   fab.animate().translationY(coordinatorLayout.getHeight() - (fab.getHeight() * 3)).setInterpolator(new AccelerateInterpolator(1)).start();
//                } else {
//                    if (toolbarTitle.getVisibility() == View.VISIBLE) {
//                        toolbarTitle.setVisibility(View.GONE);
//                    }
//                    Log.v("collapse", "collapse" + verticalOffset);
//                }
//            }
//        });
//    }
//
//    /**
//     * To convert timestamp to Date
//     **/
//
//    private String getDate(long timeStamp) {
//
//        try {
//            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
//            Date netDate = (new Date(timeStamp));
//            return sdf.format(netDate);
//        } catch (Exception ex) {
//            return "xx";
//        }
//    }
//
//    class ViewPagerAdapter extends PagerAdapter {
//        Context context;
//        LayoutInflater inflater;
//        ArrayList<String> temp;
//
//        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
//            this.temp = newary;
//            this.context = act;
//        }
//
//        public int getCount() {
//            return temp.size();
//        }
//
//        public Object instantiateItem(ViewGroup collection, int posi) {
//            inflater = (LayoutInflater) context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View itemView = inflater.inflate(R.layout.profile_image_viewer,
//                    collection, false);
//
//            ImageView image = itemView.findViewById(R.id.imgDisplay);
//            if (!temp.get(posi).equals("")) {
//                //  Picasso.with(context).load(temp.get(posi)).into(image);
//
//                Picasso.with(context)
//                        .load(temp.get(posi))
//                        .placeholder(R.drawable.user_placeholder)
//                        .error(R.drawable.user_placeholder)
//                        .fit()
//                        .into(image);
//
//            }
//
//            image.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(MainViewProfileDetailActivity.this, ViewImage.class);
//                    intent.putExtra("images", imagesAry);
//                    intent.putExtra("position", viewPager.getCurrentItem());
//                    Pair<View, String> bodyPair = Pair.create(view, "fromProfile");
//                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainViewProfileDetailActivity.this, bodyPair);
//                    ActivityCompat.startActivity(MainViewProfileDetailActivity.this, intent, options.toBundle());
//                }
//            });
//
//            collection.addView(itemView, 0);
//
//            return itemView;
//
//        }
//
//        @Override
//        public void destroyItem(View arg0, int arg1, Object arg2) {
//            ((ViewPager) arg0).removeView((View) arg2);
//        }
//
//        @Override
//        public boolean isViewFromObject(View arg0, Object arg1) {
//            return arg0 == (arg1);
//
//        }
//
//        @Override
//        public Parcelable saveState() {
//            return null;
//        }
//    }

}
