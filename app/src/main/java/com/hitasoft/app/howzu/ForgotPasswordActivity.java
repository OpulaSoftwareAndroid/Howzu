package com.hitasoft.app.howzu;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomEditText;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();
    private RelativeLayout actionbar;
    private ImageView backbtn;
    //private CustomTextView toolbarTitle;
    //private TextInputLayout emailLay;
    private EditText edtEmail;
    private CustomTextView btnReset;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    LinearLayout mainLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ForgotPasswordActivity.this.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
        findViews();

        btnReset.setOnClickListener(this);
        backbtn.setOnClickListener(this);
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-09-19 17:32:52 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {

        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        //toolbarTitle = (CustomTextView) findViewById(R.id.toolbarTitle);
        //emailLay = (TextInputLayout) findViewById(R.id.emailLay);
        edtEmail =  findViewById(R.id.edtEmail);
        btnReset = (CustomTextView) findViewById(R.id.btnReset);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);

        //toolbarTitle.setText(getString(R.string.forgot_password));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnReset:
                if (TextUtils.isEmpty("" + edtEmail.getText())) {
                    Toast.makeText(this, getString(R.string.please_verify_mail), Toast.LENGTH_SHORT).show();
                } else if (!(edtEmail.getText().toString().matches(emailPattern)) && !Constants.EMAIL_ADDRESS_PATTERN.matcher("" + edtEmail.getText()).matches()) {
                    Toast.makeText(this, getString(R.string.please_verify_mail), Toast.LENGTH_SHORT).show();
                } else {
                    if (HowzuApplication.isNetworkAvailable(ForgotPasswordActivity.this)) {
                        btnReset.setClickable(false);
                        btnReset.setEnabled(false);
                        //emailLay.setError(null);
                        resetUserPassword(edtEmail.getText().toString());
                    } else {
                        HowzuApplication.showSnack(ForgotPasswordActivity.this, mainLay, false);
                    }
                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }
    }

    private void resetUserPassword(String mail) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FORGOT_PASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String result) {
                        JSONObject jonj;
                        try {
                            btnReset.setClickable(true);
                            btnReset.setEnabled(true);
                            jonj = new JSONObject(result);
                            if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(getApplicationContext(), jonj.getString(Constants.TAG_MESSAGE), Toast.LENGTH_LONG).show();
                                edtEmail.setText("");
                            } else {
                                Toast.makeText(getApplicationContext(), jonj.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                btnReset.setClickable(true);
                btnReset.setEnabled(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_EMAIL, mail);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }
}
