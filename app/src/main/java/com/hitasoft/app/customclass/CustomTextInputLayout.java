package com.hitasoft.app.customclass;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import com.hitasoft.app.howzu.R;

import java.util.HashMap;

/**
 * Customizing AutoCompleteTextView to return Country Name
 * corresponding to the selected item
 */
public class CustomTextInputLayout extends TextInputLayout {

    public CustomTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

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
    public void setTypeface(@Nullable Typeface typeface) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(getContext(),R.font.font_regular);
        }
        super.setTypeface(typeface);
    }
}
