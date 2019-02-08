package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hitasoft.app.customclass.TouchImageView;
import com.hitasoft.app.helper.NetworkReceiver;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by hitasoft on 7/3/17.
 */

public class DownloadImage extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {
    static final String TAG ="DownloadImage";

    TouchImageView imageView;

    String imgPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_image);
        imageView = findViewById(R.id.imageView);

        imgPath = getIntent().getExtras().getString("image");

        File file = new File(imgPath);

        Log.v(TAG, "imgPath=" + imgPath);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        if (!file.exists()) {
            dialog();
        }

        ViewCompat.setTransitionName(imageView, imgPath);

        Picasso.with(this).load(file).into(imageView);
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

    private void dialog() {
        final Dialog dialog = new Dialog(DownloadImage.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title =  dialog.findViewById(R.id.headerTxt);
        TextView subTxt =  dialog.findViewById(R.id.subTxt);
        TextView yes =  dialog.findViewById(R.id.yes);
        TextView no =  dialog.findViewById(R.id.no);

        subTxt.setText(getString(R.string.sorry_media_file_doesnt_exit));
        no.setText(getString(R.string.ok));

        title.setVisibility(View.GONE);
        yes.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }
}
