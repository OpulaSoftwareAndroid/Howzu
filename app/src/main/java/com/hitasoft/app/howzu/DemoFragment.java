package com.hitasoft.app.howzu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static com.facebook.FacebookSdk.getApplicationContext;

public class DemoFragment extends Fragment {

    ImageView open, close;
    LinearLayout myView;
    LinearLayout lin_open;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_main_layout, container, false);

        myView = view.findViewById(R.id.my_view);
        open = view.findViewById(R.id.open);
        close = view.findViewById(R.id.close);
        lin_open = view.findViewById(R.id.lin_open);
        myView.setVisibility(View.INVISIBLE);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_swipe);
                myView.setVisibility(View.VISIBLE);
                lin_open.setVisibility(View.INVISIBLE);
                myView.startAnimation(slideUp);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rigt_swipe);
                lin_open.setVisibility(View.VISIBLE);
                myView.setVisibility(View.INVISIBLE);
                myView.startAnimation(slideDown);
            }
        });

        return view;
    }

}
