package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

/**
 * Created by hitasoft on 15/11/16.
 */

public class PremiumSuccess {

    Context context;
    Dialog dialog;
    ImageView userImage;
    TextView continu;

    public PremiumSuccess(Context context) {
        this.context = context;
    }

    public void show() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.premium_success);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        userImage = dialog.findViewById(R.id.profile_pic);
        continu =  dialog.findViewById(R.id.continu);

        continu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent p = new Intent(context, MainScreenActivity.class);
                p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                p.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(p);
            }
        });

        Picasso.with(context).load(GetSet.getImageUrl()).into(userImage);

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
