package com.hitasoft.app.customclass;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.hitasoft.app.howzu.R;

/**
 * Created by hitasoft on 12/2/16.
 */
public class CustomEditText extends AppCompatEditText {

    private Context context;
    private AttributeSet attrs;
    private int defStyle;

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
        init();
    }

//    private void init() {
//        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
//        this.setTypeface(font);
//    }
//
//    @Override
//    public void setTypeface(Typeface tf, int style) {
//        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
//        super.setTypeface(tf, style);
//    }
//
//    @Override
//    public void setTypeface(Typeface tf) {
//        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
//        super.setTypeface(tf);
//    }

    private void init() {
        Typeface font = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            font = getResources().getFont(R.font.font_regular);
        } else {
            font = ResourcesCompat.getFont(getContext(),R.font.font_regular);
        }
        this.setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            tf = getResources().getFont(R.font.font_regular);
        } else {
            tf = ResourcesCompat.getFont(getContext(),R.font.font_regular);
        }
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            tf = getResources().getFont(R.font.font_regular);
        } else {
            tf = ResourcesCompat.getFont(getContext(),R.font.font_regular);
        }
        super.setTypeface(tf);
    }
}