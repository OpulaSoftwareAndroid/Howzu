package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 7/3/18.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {
    String TAG = "LoginFragment";

    ProgressDialog dialog;
    EditText email, password;
    TextView signin, forgotPassword, txtShowPassword;
    //TextInputLayout emailLay, passwordLay;
    LinearLayout mainLay;
    Display display;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private HashMap<String, String> hashMap;
    public boolean showPassword = false;

    public LoginFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Log.v(TAG, "income");
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        email = getView().findViewById(R.id.email);
        password = getView().findViewById(R.id.password);
        signin = getView().findViewById(R.id.signin);
        /*emailLay = getView().findViewById(R.id.emailLay);
        passwordLay = getView().findViewById(R.id.passwordLay);*/
        txtShowPassword = getView().findViewById(R.id.txtShowPassword);
        forgotPassword = getView().findViewById(R.id.forgotPassword);
        mainLay = getView().findViewById(R.id.mainLay);
        display = getActivity().getWindowManager().getDefaultDisplay();

//        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "font_regular.ttf");
//        passwordLay.setTypeface(typeface);
//        emailLay.setTypeface(typeface);
        if (hashMap != null && hashMap.size() > 0) {
            email.setText(hashMap.get(Constants.TAG_EMAIL));
        }
        signin.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        txtShowPassword.setOnClickListener(this);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    txtShowPassword.setVisibility(View.VISIBLE);
                } else {
                    txtShowPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        editor = pref.edit();

        Constants.pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
        Constants.editor = pref.edit();

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private boolean validateEmail() {
        if (email.getText().toString().matches(emailPattern) ||
                Constants.EMAIL_ADDRESS_PATTERN.matcher("" + email.getText()).matches()) {
            /*emailLay.setError(null);
            emailLay.setErrorEnabled(false);*/
            return true;
        } else {
            Toast.makeText(getActivity(), "Please fill valid email", Toast.LENGTH_SHORT).show();
            requestFocus(email);
            return false;
        }
    }

    private boolean validatePassword() {
        if (password.getText().toString().trim().isEmpty()) {
            Toast.makeText(getActivity(), "Please fill valid password", Toast.LENGTH_SHORT).show();
            requestFocus(password);
            return false;
        } else if (password.getText().toString().length() < 6) {
            Toast.makeText(getActivity(), "Password should atleast 6 characters", Toast.LENGTH_SHORT).show();
            requestFocus(password);
            return false;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * API Implementation
     */

    public void login() {
        StringRequest loginReq = new StringRequest(Request.Method.POST, Constants.API_SIGNIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "loginRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONObject results = new JSONObject(res);
                            String status = results.getString("status");
                            if (status.equalsIgnoreCase("true")) {
                                JSONObject values = results.getJSONObject(Constants.TAG_PEOPLES);


                                GetSet.setLogged(true);
                                GetSet.setUserId(DefensiveClass.optString(values, Constants.TAG_USERID));
                                GetSet.setUserName(DefensiveClass.optString(values, Constants.TAG_USERNAME));
                                GetSet.setImageUrl(DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                GetSet.setToken(DefensiveClass.optString(values, Constants.TAG_TOKEN));

                                editor.putBoolean(Constants.ISLOGGED, true);
                                editor.putString(Constants.TAG_USERID, GetSet.getUserId());
                                editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                                editor.putString(Constants.TAG_GENDER, DefensiveClass.optString(values, Constants.TAG_GENDER));
                                editor.putString(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
                                editor.putString(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
                                editor.putString(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
                                editor.putString(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
                                editor.putString(Constants.TAG_ONLINE, DefensiveClass.optString(values, Constants.TAG_ONLINE));
                                editor.putString(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                editor.putString(Constants.TAG_INFO, DefensiveClass.optString(values, Constants.TAG_INFO));
                                editor.putString(Constants.TAG_INTEREST, DefensiveClass.optString(values, Constants.TAG_INTEREST));
                                editor.putString(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
                                editor.putString(Constants.TAG_IMAGES, DefensiveClass.optString(values, Constants.TAG_IMAGES));
                                editor.putString(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
                                editor.putString(Constants.TAG_SHOW_DISTANCE, DefensiveClass.optString(values, Constants.TAG_SHOW_DISTANCE));
                                editor.putString(Constants.TAG_INVISIBLE, DefensiveClass.optString(values, Constants.TAG_INVISIBLE));
                                editor.putString(Constants.TAG_REPORT, DefensiveClass.optString(values, Constants.TAG_REPORT));
                                editor.putString(Constants.TAG_TOKEN, DefensiveClass.optString(values, Constants.TAG_TOKEN));

                                if (DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER).equals("true") || DefensiveClass.optString(values, Constants.TAG_PREMIUM_MEMBER).equals("1")) {
                                    GetSet.setPremium(true);
                                    editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, true);
                                } else {
                                    GetSet.setPremium(true);
                                    editor.putBoolean(Constants.TAG_PREMIUM_MEMBER, false);
                                }

                                editor.putString(Constants.TAG_AD_UNIT_ID, DefensiveClass.optString(values, Constants.TAG_AD_UNIT_ID));
                                GetSet.setAdUnitId(DefensiveClass.optString(values, Constants.TAG_AD_UNIT_ID));
                                if (DefensiveClass.optString(values, Constants.TAG_ADMIN_ENABLE_ADS).equals("true")) {
                                    GetSet.setBannerEnable(true);
                                    editor.putBoolean(Constants.TAG_ADMIN_ENABLE_ADS, true);
                                } else {
                                    GetSet.setBannerEnable(false);
                                    editor.putBoolean(Constants.TAG_ADMIN_ENABLE_ADS, false);
                                }

                                if (DefensiveClass.optString(values, Constants.TAG_HIDE_ADS).equals("true")) {
                                    GetSet.setHideAds(true);
                                    editor.putBoolean(Constants.TAG_HIDE_ADS, true);
                                } else {
                                    GetSet.setHideAds(false);
                                    editor.putBoolean(Constants.TAG_HIDE_ADS, false);
                                }

                                if (DefensiveClass.optString(values, Constants.TAG_LIKE_NOTIFICATION).equals("true")) {
                                    GetSet.setLikeNotification(true);
                                    editor.putBoolean(Constants.TAG_LIKE_NOTIFICATION, true);
                                } else {
                                    GetSet.setLikeNotification(false);
                                    editor.putBoolean(Constants.TAG_LIKE_NOTIFICATION, false);
                                }

                                if (DefensiveClass.optString(values, Constants.TAG_MESSAGE_NOTIFICATION).equals("true")) {
                                    GetSet.setMsgNotification(true);
                                    editor.putBoolean(Constants.TAG_MESSAGE_NOTIFICATION, true);
                                } else {
                                    GetSet.setMsgNotification(false);
                                    editor.putBoolean(Constants.TAG_MESSAGE_NOTIFICATION, false);
                                }
                                editor.commit();

                                GetSet.setLocation(DefensiveClass.optString(values, Constants.TAG_LOCATION));
                                getAccessToken();
                            } else if (res.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString(Constants.TAG_MESSAGE));
                            } else {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(getActivity(), results.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "something went to be wrong", Toast.LENGTH_SHORT).show();
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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_EMAIL, email.getText().toString());
                params.put(Constants.TAG_PASSWORD, password.getText().toString());
                params.put(Constants.TAG_LAT, Double.toString(LoginActivity.lat));
                params.put(Constants.TAG_LON, Double.toString(LoginActivity.longit));
                params.put(Constants.TAG_LOCATION, LoginActivity.loc);
                params.put(Constants.TAG_DEVICE_ID, android.provider.Settings.Secure.getString(getActivity().getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID));
                Log.v(TAG, "loginParams=" + params);
                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(loginReq, "");
    }

    private void getAccessToken() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ACCESS_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            JSONObject results = new JSONObject(res);
                            Log.e(TAG, "getAccessTokenResult: " + results);
                            if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                String accessToken = DefensiveClass.optString(results, Constants.TAG_ACCESS_TOKEN);
                                editor.putString(Constants.TAG_AUTHORIZATION, accessToken);
                                editor.putString(Constants.TAG_EXPIRY, DefensiveClass.optString(results, Constants.TAG_EXPIRY));
                                editor.commit();

                                addDeviceId();

                                setOnlineStatus();

                                getActivity().finish();
                                Intent i = new Intent(getActivity(), MainScreenActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);

                                getActivity().overridePendingTransition(R.anim.fade_in,
                                        R.anim.fade_out);
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
                Long timeStamp = System.currentTimeMillis() / 1000L;
                String hashString = pref.getString(Constants.TAG_TOKEN, "").trim() + timeStamp.toString().trim();
                hashString = new String(android.util.Base64.encode(hashString.trim().getBytes(), android.util.Base64.DEFAULT));
                map.put(Constants.TAG_ID, pref.getString(Constants.TAG_USERID, ""));
                map.put(Constants.TAG_HASH, hashString.trim());
                map.put(Constants.TAG_TIMESTAMP, "" + timeStamp);
                Log.v(TAG, "getAccessTokenParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(getActivity()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_DEVICE_ID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (!response.equalsIgnoreCase("true")) {
                                Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
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
                Log.i(TAG, "getHeaders: " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
                Log.i(TAG, "getHeaders: " + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_DEVICE_TOKEN, token);
                map.put(Constants.TAG_DEVICE_TYPE, "1");
                map.put(Constants.TAG_DEVICE_ID, deviceId);
                map.put(Constants.TAG_DEVICE_MODE, "1");
                Log.v(TAG, "addDeviceIdParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void setOnlineStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "setOnlineStatusRes=" + res);
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
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
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
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
                map.put(Constants.TAG_STATUS, "1");
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                Log.v(TAG, "setOnlineStatusParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /**
     * Dialog for forgot password
     **/

    private void forgotPassword() {
        final Dialog passDialog = new Dialog(getContext());
        passDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passDialog.setContentView(R.layout.forget_password);
        passDialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        passDialog.setCancelable(true);
        passDialog.setCanceledOnTouchOutside(false);

        final EditText msg = (EditText) passDialog.findViewById(R.id.email);
        TextView reset = (TextView) passDialog.findViewById(R.id.reset);
        ImageView backbtn = (ImageView) passDialog.findViewById(R.id.backbtn);

        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!(msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    dialog.show();
                    passDialog.dismiss();
                    resetUserPassword(msg.getText().toString());
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDialog.dismiss();
            }
        });


        if (!passDialog.isShowing()) {
            passDialog.show();
        }
    }

    private void resetUserPassword(String mail) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FORGOT_PASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        JSONObject jonj;
                        try {

                            jonj = new JSONObject(result);
                            dialog.dismiss();
                            if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(getContext(), jonj.getString(Constants.TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            } else {

                                Toast.makeText(getContext(), jonj.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_EMAIL, mail);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /*Onclick Event*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin:
                if (!validateEmail()) {
                    return;
                } else if (!validatePassword()) {
                    return;
                } else {
                    if (HowzuApplication.isNetworkAvailable(getActivity())) {
                        dialog.show();
                        login();
                    } else {
                        HowzuApplication.showSnack(getActivity(), mainLay, false);
                    }
                }
                break;
            case R.id.forgotPassword:
//                forgotPassword();
                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
                break;
            case R.id.txtShowPassword:
                if (!showPassword) {
                    showPassword = true;
                    password.setTransformationMethod(null);
                    txtShowPassword.setText(R.string.hide);
                } else {
                    showPassword = false;
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    txtShowPassword.setText(R.string.show);
                }
                break;
        }
    }

    public void setHashMap(HashMap<String, String> hashMap) {
        this.hashMap = hashMap;
    }

}
