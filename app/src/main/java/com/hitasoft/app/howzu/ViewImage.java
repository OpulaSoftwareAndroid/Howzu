package com.hitasoft.app.howzu;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hitasoft.app.helper.NetworkReceiver;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hitasoft on 28/4/17.
 */

public class ViewImage extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {

    ViewPager viewPager;

    ViewPagerAdapter viewPagerAdapter;
    ArrayList<String> imagesAry = new ArrayList<String>();
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);

        viewPager = findViewById(R.id.view_pager);
        imagesAry = (ArrayList<String>) getIntent().getExtras().get("images");
        position = (int) getIntent().getExtras().get("position");

        ViewCompat.setTransitionName(viewPager, "fromProfile");

        viewPagerAdapter = new ViewPagerAdapter(ViewImage.this, imagesAry);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(position);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        super.onPause();
    }

    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        @Override
        public int getCount() {
            return temp.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int posi) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.image_aspect,
                    collection, false);

            ImageView image = (ImageView) itemView.findViewById(R.id.imageView);
            if (!temp.get(posi).equals("")) {
                Picasso.with(context).load(temp.get(posi)).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder).into(image);
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
