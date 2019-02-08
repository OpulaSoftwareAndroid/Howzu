package com.hitasoft.app.howzu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 21/3/17.
 */

public class TOS extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "tos";

    ImageView backbtn;
    TextView title, terms, header;
    ProgressWheel progress;
    LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tos);

        title = findViewById(R.id.title);
        backbtn = findViewById(R.id.backbtn);
        terms = findViewById(R.id.terms_txt);
        progress = findViewById(R.id.progress);
        main = findViewById(R.id.main);
        header = findViewById(R.id.title_txt);

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.spin();

        title.setText(getString(R.string.terms_conditions));

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        getTos();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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

    /**
     * API Implementation
     **/

    private void getTos() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_TOS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                                main.setVisibility(View.VISIBLE);
                                progress.setVisibility(View.GONE);

                                header.setText(DefensiveClass.optString(result, Constants.TAG_TITLE));
                                terms.setText(DefensiveClass.optString(result, Constants.TAG_DESCRIPTION));
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
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

}
