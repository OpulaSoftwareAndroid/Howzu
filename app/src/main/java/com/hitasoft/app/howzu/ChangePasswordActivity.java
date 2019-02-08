package com.hitasoft.app.howzu;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {
    EditText oldPass, newPass, reNewPass;
    //TextInputLayout oldPassLay, newPassLay, reNewPassLay;
    TextView save/*title*/;
    ImageView backbtn;
    ProgressDialog dialog;
    String TAG = "ChangePasswordActivity";
    SharedPreferences.Editor editor;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ChangePasswordActivity.this.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
        oldPass = findViewById(R.id.oldPass);
        newPass = findViewById(R.id.newPass);
        reNewPass = findViewById(R.id.reNewPass);
        /*oldPassLay = findViewById(R.id.oldPassLay);
        newPassLay = findViewById(R.id.newPassLay);
        reNewPassLay = findViewById(R.id.reNewPassLay);*/
        save = findViewById(R.id.savebtn);
        backbtn = findViewById(R.id.backbtn);
        //title = findViewById(R.id.title);

//        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
//        oldPassLay.setTypeface(typeface);
//        newPassLay.setTypeface(typeface);
//        reNewPassLay.setTypeface(typeface);

        pref = getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        editor = pref.edit();

        //title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        //title.setText(getResources().getString(R.string.change_password));

        save.setOnClickListener(this);
        backbtn.setOnClickListener(this);


        dialog = new ProgressDialog(ChangePasswordActivity.this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


    }

    private boolean validatePassword() {
        if (oldPass.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill valid password", Toast.LENGTH_SHORT).show();
            requestFocus(oldPass);
            return false;
        } else if (oldPass.getText().toString().length() < 6) {
            Toast.makeText(this, "Password should atleast 6 characters", Toast.LENGTH_SHORT).show();
            requestFocus(oldPass);
            return false;
        }
        if (newPass.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill valid password", Toast.LENGTH_SHORT).show();
            requestFocus(newPass);
            return false;
        } else if (newPass.getText().toString().length() < 6) {
            Toast.makeText(this, "Password should atleast 6 characters", Toast.LENGTH_SHORT).show();
            requestFocus(newPass);
            return false;
        }
        if (reNewPass.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill valid password", Toast.LENGTH_SHORT).show();
            requestFocus(reNewPass);
            return false;
        } else if (reNewPass.getText().toString().length() < 6) {
            Toast.makeText(this, "Password should atleast 6 characters", Toast.LENGTH_SHORT).show();
            requestFocus(reNewPass);
            return false;
        } else if (!newPass.getText().toString().equals(reNewPass.getText().toString())) {
            Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
            requestFocus(reNewPass);
            return false;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void changePassword() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_PASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            dialog.dismiss();

                            if (response.equalsIgnoreCase("true")) {
                                finish();
                                Toast.makeText(getApplicationContext(), json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_OLD_PASSWORD, oldPass.getText().toString());
                map.put(Constants.TAG_NEW_PASSWORD, newPass.getText().toString());
                Log.v(TAG, "addDeviceIdParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.savebtn:
                if (!validatePassword()) {
                    return;
                } else {
                    dialog.show();
                    changePassword();
                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }
    }

}

