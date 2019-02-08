package com.hitasoft.app.howzu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ImagePicker;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SetDialogsActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    LinearLayout lin_partner, lin_timepass, lin_friends, lin_dating;
    Dialog dialogInterest, dialogPeopleFor, dialogAddPhotos;
    TextView btnNext;
    private InterestAdapter interestAdapter;
    ArrayList<HashMap<String, String>> arrayListInterest = new ArrayList<HashMap<String, String>>();
    private List<String> selectedList = new ArrayList<>();
    private List<String> selectedListID = new ArrayList<>();
    String strSelectedInterest;
    CustomTextView btn_set_password;
    ArrayList<HashMap<String, String>> peopleFor = new ArrayList<HashMap<String, String>>();
    ProgressWheel interestProgress, peopleProgress, photoProgress;
    private PeopleAdapter peopleAdapter;
    private String peopleForId = "";
    TextView btnAddPhoto;
    private ImageView userImageView;
    private String profileImage = "", strProfileImage="";
    private static boolean imageUploading;
    ProgressDialog dialog;
    private String imagesJson = "";
    private List<String> imagesAry = new ArrayList<>();
    private HashMap<String, String> hashMap;
    SharedPreferences.Editor editor;
    private String interest = "";
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 100;
    SharedPreferences pref;
    ArrayList arrayListInterestName, arrayListInterestID;
    String strCountryCode, strMobileNumber, strName, strDateOfBirth, strPassword,strLatitude,strLongitude,strLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_dialogs);

        Bundle extras = getIntent().getExtras();

        if (extras == null) {
//            strCountryCode="+91";
//            strMobileNumber="9662290050";
            return;
        } else {
            strCountryCode = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE);
            strMobileNumber = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER);
            strName = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_NAME);
            strDateOfBirth = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE);
            strPassword = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_PASSWORD);
            strLatitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE);
            strLongitude = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE);
            strLocation = extras.getString(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION);

        }
//        strCountryCode = "+91";
//        strMobileNumber = "96668191391";
//        strName = "jigr7aabt2";
//        strDateOfBirth = "20/09/1991";
//        strPassword = "jiagr4718";

        arrayListInterestID = new ArrayList<String>();
        arrayListInterestName = new ArrayList<String>();
        pref = getSharedPreferences("ChatPref", MODE_PRIVATE);
        editor = pref.edit();
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Constants.pref = getSharedPreferences("ChatPref", MODE_PRIVATE);
        Constants.editor = pref.edit();

        openPeopleForDialog();


        dialogPeopleFor.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    SetDialogsActivity.this.finish();
                }
                return true;
            }

        });

        if (hashMap != null && hashMap.size() > 0) {

            profileImage = hashMap.get(Constants.TAG_IMAGE_URL);
        }
    }


    //People Dialog
    private void openPeopleForDialog() {
        dialogPeopleFor = new Dialog(SetDialogsActivity.this);
        dialogPeopleFor.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPeopleFor.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogPeopleFor.setContentView(R.layout.dialog_people_for);
        dialogPeopleFor.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogPeopleFor.setCancelable(false);

        RecyclerView recyclerView = dialogPeopleFor.findViewById(R.id.recyclerView);
        CustomTextView btnNext = dialogPeopleFor.findViewById(R.id.btnNext);
        peopleProgress = dialogPeopleFor.findViewById(R.id.peopleProgress);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
//        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        recyclerView.setLayoutManager(gridLayoutManager);

        peopleFor = new ArrayList<>();
        peopleProgress.setVisibility(View.VISIBLE);
        peopleProgress.spin();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PEOPLE_INTEREST_FOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getSettingsRes=" + res);
                            JSONObject response = new JSONObject(res);
                            String strStatus=response.getString(Constants.TAG_STATUS);

                            String strMessage=response.getString(Constants.TAG_MSG);
                            JSONArray jsonArrayInfo=response.getJSONArray(Constants.TAG_INFO);

                            if (strStatus.equals("1")) {
//                                JSONObject result = response.optJSONObject(Constants.TAG_RESULT);
//                                JSONArray people_for = result.getJSONArray(Constants.TAG_PEOPLEFOR);
//                                JSONObject result = response.optJSONObject(Constants.TAG_);
//                                JSONArray people_for = result.getJSONArray(Constants.TAG_PEOPLEFOR);

                                for (int i = 0; i < jsonArrayInfo.length(); i++) {
                                    JSONObject temp = jsonArrayInfo.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                                    map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                    map.put(Constants.TAG_IMG, DefensiveClass.optString(temp, Constants.TAG_IMG));

//                                    map.put(Constants.TAG_ICON, DefensiveClass.optString(temp, Constants.TAG_ICON));
                                    peopleFor.add(map);
                                }

                                peopleAdapter = new PeopleAdapter(getApplicationContext(), peopleFor);
                                recyclerView.setAdapter(peopleAdapter);
                                peopleAdapter.notifyDataSetChanged();
                                peopleProgress.setVisibility(View.GONE);
                                peopleProgress.stopSpinning();
                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getApplicationContext(), "Error", response.getString("message"));
                            } else {
                                peopleProgress.setVisibility(View.GONE);
                                peopleProgress.stopSpinning();
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
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HowzuApplication.isNetworkAvailable(getApplicationContext())) {
                    if (peopleForId != null && !peopleForId.equalsIgnoreCase("")) {

                        openPhotoDialog();
                        setProfile("updatePeopleFor");
//                        openInterestDialog();
                    } else {
                        Toast.makeText(getApplicationContext(), "Select anyone", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!dialogPeopleFor.isShowing()) {
            dialogPeopleFor.show();
        }
    }

    class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;

        public PeopleAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
        }

        @NonNull
        @Override
        public PeopleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.item_people_for, parent, false);//layout
            return new PeopleAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull PeopleAdapter.ViewHolder holder, int position) {
            try {
                if (position == 0 || position == 1) {
                    GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) holder.detail_lay.getLayoutParams();
                    layoutParams.setMargins(0, -5, 0, 0);
                    holder.detail_lay.setLayoutParams(layoutParams);
                } else if (position == (Items.size() - 1)) {
                    GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) holder.detail_lay.getLayoutParams();
                    layoutParams.setMargins(0, 0, 0, -5);
                    holder.detail_lay.setLayoutParams(layoutParams);
                }

                final HashMap<String, String> map = Items.get(position);
                if (map.isEmpty()) {
                    holder.detail_lay.setVisibility(View.INVISIBLE);
                } else {
                    holder.detail_lay.setVisibility(View.VISIBLE);
                    holder.name.setText(map.get(Constants.TAG_NAME));
                    Picasso.with(mContext).load(map.get(Constants.TAG_IMG)).into(holder.icon);
                    if (peopleForId.equals("")) {
                        holder.icon_bg.setSelected(false);
                    } else {
                        if (peopleForId.equals(map.get(Constants.TAG_ID))) {
                            holder.icon_bg.setSelected(true);
                        } else {
                            holder.icon_bg.setSelected(false);
                        }
                    }
                }


                holder.detail_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        peopleForId = String.valueOf(map.get(Constants.TAG_ID));
                        notifyDataSetChanged();
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon, icon_bg;
            TextView name;
            LinearLayout detail_lay;

            public ViewHolder(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon_bg = (ImageView) itemView.findViewById(R.id.icon_bg);
                name = itemView.findViewById(R.id.name);
                detail_lay = itemView.findViewById(R.id.detail_lay);
//                int width = itemView.getWidth();
//                gridWidth = width * 47 / 100;
//                detail_lay.setLayoutParams(new LinearLayout.LayoutParams(gridWidth, gridWidth));
            }
        }
    }


    //People Photo dialog

    private void openPhotoDialog() {
        dialogAddPhotos = new Dialog(SetDialogsActivity.this);
        dialogAddPhotos.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAddPhotos.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogAddPhotos.setContentView(R.layout.dialog_add_photos);
        dialogAddPhotos.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogAddPhotos.setCancelable(false);

        TextView btnSkip, btnAddPhoto, btnNext, btnFinish;
        ImageView userImageView;
        RelativeLayout imageLayout;
        btnSkip = dialogAddPhotos.findViewById(R.id.btnSkip);
        btnSkip.setVisibility(View.GONE);
        btnAddPhoto = dialogAddPhotos.findViewById(R.id.btnAddPhoto);
        btnNext = dialogAddPhotos.findViewById(R.id.btnNext);
        userImageView = dialogAddPhotos.findViewById(R.id.userImage);
        imageLayout = dialogAddPhotos.findViewById(R.id.imageLayout);
        photoProgress = dialogAddPhotos.findViewById(R.id.photoProgress);

        this.btnAddPhoto = btnAddPhoto;
        this.btnNext = btnNext;
        this.userImageView = userImageView;

        ViewGroup.LayoutParams params = userImageView.getLayoutParams();
        params.width = HowzuApplication.dpToPx(getApplicationContext(), 150);
        params.height = HowzuApplication.dpToPx(getApplicationContext(), 200);

        userImageView.setLayoutParams(params);
        Picasso.with(getApplicationContext()).load(GetSet.getImageUrl()).resize(params.width, params.height)
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .centerCrop().into(userImageView);


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileImage == null || profileImage.equalsIgnoreCase("")) {
                    dialogAddPhotos.dismiss();
                    if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                        dialogPeopleFor.dismiss();
                    }
                    openInterestDialog();
                } else {
                    if (HowzuApplication.isNetworkAvailable(SetDialogsActivity.this)) {
                        imageUploading = true;
                        new uploadImage().execute(profileImage);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogAddPhotos.dismiss();
                if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                    dialogPeopleFor.dismiss();
                }
                openInterestDialog();
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(SetDialogsActivity.this, "Select your image:");
                }
            }
        });

        userImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                } else {
                    ImagePicker.pickImage(SetDialogsActivity.this, "Select your image:");
                }
            }
        });

        if (dialogAddPhotos != null) {
            dialogAddPhotos.show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class uploadImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "", status, existingFileName = "";

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_IMAGE;
            try {
                existingFileName = imgpath[0];
                Log.v(TAG, " existingFileName=" + existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("user");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e(TAG, "MediaPlayer-Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v(TAG, "buffer=" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v(TAG, "bytesRead=" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e(TAG, "MediaPlayer-File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e(TAG, "MediaPlayer-error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e(TAG, "MediaPlayer-error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e(TAG, "MediaPlayer-Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e(TAG, "MediaPlayer-error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v(TAG, "json=" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String name = DefensiveClass.optString(image, "Name");
                    String viewUrl = DefensiveClass.optString(image, "View_url");

                    File dir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "Images/MainViewProfileDetailActivity");

                    if (dir.exists()) {
                        File from = new File(existingFileName);
                        File to = new File(dir + "/" + name);
                        if (from.exists())
                            from.renameTo(to);
//                        strProfileImage = name;
                        if(viewUrl==null || viewUrl.equals("null"))
                        {
                            strProfileImage="";
                        }else {

                            strProfileImage = viewUrl;
                        }
                    }

                    imagesAry = new ArrayList<>();
                    imagesAry.add(viewUrl);
                    try {
                        JSONArray jsonArray = new JSONArray(imagesAry);
                        imagesJson = String.valueOf(jsonArray);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing()) {
//                pd.show();
                photoProgress.setVisibility(View.VISIBLE);
                photoProgress.spin();
            }
        }

        @Override
        protected void onPostExecute(Integer unused) {
            try {
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }

                photoProgress.setVisibility(View.GONE);
                photoProgress.stopSpinning();
                profileImage = imagesAry.get(0);
                imageUploading = false;
                GetSet.setImageUrl(profileImage);
                editor.putString(Constants.TAG_USERIMAGE, profileImage);
                editor.commit();
                openInterestDialog();

                //  setProfile("updateImage");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void setProfile(String dialogType) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "saveSettingsRes1=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                editor.putString(Constants.TAG_PEOPLE_FOR, peopleForId);
                                editor.putString(Constants.TAG_INTEREST, interest);
                                editor.commit();

                                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {

//                                    if (facebookId != null && !facebookId.equalsIgnoreCase("")) {
//                                        GetSet.setImageUrl(profileImage);
//                                        editor.putString(Constants.TAG_USERIMAGE, profileImage);
//                                        editor.commit();
//                                        imagesAry = new ArrayList<>();
//                                        imagesAry.add(profileImage);
//                                        try {
//                                            JSONArray jsonArray = new JSONArray(imagesAry);
//                                            imagesJson = String.valueOf(jsonArray);
//                                        } catch (NullPointerException e) {
//                                            e.printStackTrace();
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        peopleProgress.setVisibility(View.GONE);
//                                        peopleProgress.stopSpinning();
//                                        setProfile("updateImage");
//                                    } else {
//                                        if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
//                                            peopleProgress.setVisibility(View.GONE);
//                                            peopleProgress.stopSpinning();
//                                            dialogPeopleFor.dismiss();
//                                        }
//                                        openPhotoDialog();
//                                    }

                                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                                    if (dialogAddPhotos != null && dialogAddPhotos.isShowing()) {
                                        photoProgress.setVisibility(View.GONE);
                                        photoProgress.stopSpinning();
                                        dialogAddPhotos.dismiss();
                                    }
                                    if (dialogPeopleFor != null && dialogPeopleFor.isShowing()) {
                                        dialogPeopleFor.dismiss();
                                    }
                                    SetDialogsActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            openInterestDialog();
                                        }
                                    });
                                } else {
                                    interestProgress.setVisibility(View.GONE);
                                    interestProgress.stopSpinning();
                                    if (dialogInterest != null && dialogInterest.isShowing()) {
                                        dialogInterest.dismiss();
                                    }

                                    if (LoginActivity.lat == 0 || LoginActivity.longit == 0 || LoginActivity.loc.equals("")) {
                                        HashMap<String, String> fbdata = new HashMap<>();
                                        Intent i = new Intent(getApplicationContext(),
                                                LocationActivity.class);
                                        i.putExtra("data", fbdata);
                                        startActivity(i);
                                    } else {
                                        finish();
                                        Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);

                                    }
                                }
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getApplicationContext(), "Error", json.getString("message"));
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

                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
                    if (peopleProgress != null) {
                        peopleProgress.setVisibility(View.GONE);
                        peopleProgress.stopSpinning();
                    }
                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                    if (photoProgress != null) {
                        photoProgress.setVisibility(View.GONE);
                        photoProgress.stopSpinning();
                    }
                } else {
                    if (interestProgress != null) {
                        interestProgress.setVisibility(View.GONE);
                        interestProgress.stopSpinning();
                    }
                }
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
                if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
                    map.put(Constants.TAG_PEOPLE_FOR, peopleForId);
                    map.put(Constants.TAG_SHOW_AGE, pref.getString(Constants.TAG_SHOW_AGE, ""));
                    map.put(Constants.TAG_SHOW_LOCATION, pref.getString(Constants.TAG_SHOW_LOCATION, ""));
                    map.put(Constants.TAG_INVISIBLE, "" + pref.getString(Constants.TAG_INVISIBLE, ""));
                    map.put(Constants.TAG_HIDE_ADS, "" + pref.getBoolean(Constants.TAG_HIDE_ADS, true));
                    map.put(Constants.TAG_MESSAGE_NOTIFICATION, "" + pref.getBoolean(Constants.TAG_MESSAGE_NOTIFICATION, true));
                    map.put(Constants.TAG_LIKE_NOTIFICATION, "" + pref.getBoolean(Constants.TAG_LIKE_NOTIFICATION, true));
                } else if (dialogType.equalsIgnoreCase("updateImage")) {
                    map.put(Constants.TAG_USERIMAGE, profileImage);
                    map.put(Constants.TAG_IMAGES, imagesJson);
                } else {
                    JSONArray intjson = new JSONArray(selectedList);
                    interest = String.valueOf(intjson);
                    map.put(Constants.TAG_INTERESTS, interest);
                }
                Log.v(TAG, "saveSettingsParams=" + map);
                return map;
            }
        };

        if (dialogType.equalsIgnoreCase("updatePeopleFor")) {
            if (peopleProgress != null) {
                peopleProgress.setVisibility(View.VISIBLE);
                peopleProgress.spin();
            }
        } else if (dialogType.equalsIgnoreCase("updateImage")) {
            if (photoProgress != null) {
                photoProgress.setVisibility(View.VISIBLE);
                photoProgress.spin();
            }
        } else {
            if (interestProgress != null) {
                interestProgress.setVisibility(View.VISIBLE);
                interestProgress.spin();
            }
        }

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    //Interest Dialog

    private void openInterestDialog() {
        ImageView btnSearch, btnCancel;
        Display display;
        RecyclerView recyclerView;
        EditText edtSearch;
        CustomTextView btnNext;

        dialogInterest = new Dialog(SetDialogsActivity.this);
        dialogInterest.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogInterest.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogInterest.setContentView(R.layout.dialog_interest);
        dialogInterest.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogInterest.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        dialogInterest.setCancelable(false);

        recyclerView = dialogInterest.findViewById(R.id.recyclerView);
        edtSearch = dialogInterest.findViewById(R.id.edtSearch);
        btnCancel = dialogInterest.findViewById(R.id.btnCancel);
        btnSearch = dialogInterest.findViewById(R.id.btnSearch);
        btnNext = dialogInterest.findViewById(R.id.btnFinish);
        interestProgress = dialogInterest.findViewById(R.id.interestProgress);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnCancel.setVisibility(View.VISIBLE);
                } else {
                    btnCancel.setVisibility(View.GONE);
                }
                interestAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(SetDialogsActivity.this));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtSearch.setText("");
                btnCancel.setVisibility(View.GONE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HowzuApplication.isNetworkAvailable(SetDialogsActivity.this)) {
                    if (selectedList.size() > 0) {
//                        dialogInterest.dismiss();


                        for(int i =0;i<selectedListID.size();i++)
                        {
                            if(i==0)
                            {
                                strSelectedInterest = selectedListID.get(i);
                            }else
                            {
                                strSelectedInterest = strSelectedInterest+","+selectedListID.get(i);
                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Selected items are "+strSelectedInterest+
//                                " and i am here for "+peopleForId
//                                , Toast.LENGTH_SHORT).show();



                        new CallAPI().execute();
//                        signup();
//                        Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
//                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(i);

//                        setProfile("");

                    } else
                        Toast.makeText(getApplicationContext(), "Select anyone interest", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        interestProgress.setVisibility(View.VISIBLE);
        interestProgress.spin();

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_INTEREST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getSettingsRes=" + res);
                            System.out.println("jigar the response on  interest arrayListComments is " + res);
                            JSONObject response = new JSONObject(res);
                            if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("1")) {
                                String strStatus = response.getString(Constants.TAG_STATUS);
                                String strMsg = response.getString(Constants.TAG_MSG);

                                if (strStatus.equals("1") && strMsg.equals(Constants.TAG_SUCCESS)) {
                                    JSONArray interest = response.getJSONArray(Constants.TAG_INFO);

                                    for (int i = 0; i < interest.length(); i++) {
                                        JSONObject temp = interest.getJSONObject(i);
                                        HashMap<String, String> map = new HashMap<String, String>();
                                        map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                                        map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                        arrayListInterest.add(map);

                                    }
                                }

                                interestAdapter = new InterestAdapter(getApplication(), arrayListInterest);
                                recyclerView.setAdapter(interestAdapter);
                                interestAdapter.notifyDataSetChanged();
                                interestProgress.setVisibility(View.GONE);
                                interestProgress.stopSpinning();

                            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getApplication(), "Error", response.getString("message"));
                            } else {
                                interestProgress.setVisibility(View.GONE);
                                interestProgress.stopSpinning();
                            }
                        } catch (JSONException e) {
                            System.out.println("jigar the json error in response is " + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("jigar the other error exception is " + e);

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
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }
        };
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

        dialogInterest.show();
    }
    public class CallAPI extends AsyncTask<String, String, String> {

        public CallAPI(){
            //set context variables if required
        }

        HashMap<String, String> params = new HashMap<String, String>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            interestProgress.setVisibility(View.VISIBLE);
            interestProgress.spin();

            params.put(Constants.TAG_MOBILE_NUMBER, strMobileNumber);
            params.put(Constants.TAG_PASSWORD, strPassword);
            params.put(Constants.TAG_NAME, strName);
            params.put(Constants.TAG_REQUEST_USERNAME, strName);
            params.put(Constants.TAG_GENDER, "male");
            params.put(Constants.TAG_BIO, "male");
            params.put(Constants.TAG_PURPOSE_PLAN, peopleForId);
            params.put(Constants.TAG_INTEREST_PLAN, strSelectedInterest);
            params.put(Constants.TAG_IMAGES, strProfileImage);
            params.put(Constants.TAG_DATE_OF_BIRTH, strDateOfBirth);
            params.put(Constants.TAG_SPONSOR_ID, "");
            params.put(Constants.TAG_COUNTRY_CODE, strCountryCode);
            params.put(Constants.TAG_LAT, strLatitude);
            params.put(Constants.TAG_LON, strLongitude);
            params.put(Constants.TAG_LOCATION, strLocation);
            Log.v(TAG, "jigar signup Params=" + params);
            System.out.println("jigar the parameter of insert is "+params);
            //                params.put(Constants.TAG_TYPE, "email");

        }

        @Override
        protected String doInBackground(String... params1) {
            String strResponse="";
            try {
                strResponse=   performPostCall(Constants.API_USER_SIGNUP,params);
            } catch (Exception e) {
                System.out.println("jigar the error is "+e.getMessage());
            }
            return strResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("jigar the response is "+s);
            try {
                Log.v(TAG, "jigar getSettingsRes=" + s);
                JSONObject response = new JSONObject(s);
                String strStatus=response.getString(Constants.TAG_STATUS);
                String strMsg=response.getString(Constants.TAG_MSG);
                String strInfo=response.getString(Constants.TAG_INFO);

                if(strStatus.equals("0"))
                {
                    Toast.makeText(SetDialogsActivity.this,strMsg,Toast.LENGTH_LONG).show();
                }else
                {
                    pref = getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
                    editor = pref.edit();
                    editor.putString(Constants.TAG_USERID,strInfo);
//        editor.apply();
                    editor.commit();
                    System.out.println("jigar the new user id is "+strInfo);
                    Toast.makeText(SetDialogsActivity.this,"User Registration Successful",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(SetDialogsActivity.this,MainScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constants.TAG_USERID,strInfo);
                    startActivity(intent);
                    finish();

                }
                interestProgress.setVisibility(View.GONE);
                interestProgress.stopSpinning();

            } catch (JSONException e) {
                interestProgress.setVisibility(View.GONE);
                interestProgress.stopSpinning();

                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
            interestProgress.setVisibility(View.GONE);
            interestProgress.stopSpinning();

        }
    }

    class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> implements Filterable {

        ArrayList<HashMap<String, String>> Items;
        ArrayList<HashMap<String, String>> interestsList;
        InterestAdapter.ViewHolder holder = null;
        private Context mContext;

        public InterestAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
            interestsList = data;
        }

        @NonNull
        @Override
        public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_interest, parent, false);//layout
            return new InterestAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InterestAdapter.ViewHolder holder, int position) {
            final HashMap<String, String> interest = Items.get(position);

            holder.txtInterest.setText(interest.get(Constants.TAG_NAME));
            if (selectedList.contains(interest.get(Constants.TAG_NAME))) {
                holder.btnInterest.setChecked(true);
                holder.txtInterest.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            } else {
                holder.btnInterest.setChecked(false);
                holder.txtInterest.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
            }

            holder.btnInterest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            System.out.println("jigar the selected interest are "+arrayListInterest.get(position));
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });

            holder.txtInterest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.btnInterest.setChecked(!holder.btnInterest.isChecked());
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });

            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.btnInterest.setChecked(!holder.btnInterest.isChecked());
                    for (HashMap<String, String> item : Items) {
                        if (item.get(Constants.TAG_ID).equalsIgnoreCase(interest.get(Constants.TAG_ID))) {
                            setUI(holder.btnInterest, holder.txtInterest, holder.btnInterest.isChecked(), position);
                            break;
                        }
                    }
                }
            });
        }

        private void setUI(CheckBox btnInterest, TextView txtInterest, boolean checked, int position) {
            btnInterest.setChecked(checked);
            if (checked) {
                selectedList.add(Items.get(position).get(Constants.TAG_NAME));
                selectedListID.add(Items.get(position).get(Constants.TAG_ID));
                txtInterest.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            } else {
                selectedList.remove(Items.get(position).get(Constants.TAG_NAME));
                selectedListID.remove(Items.get(position).get(Constants.TAG_ID));
                txtInterest.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public void setInterests(ArrayList<HashMap<String, String>> filteredInterests) {
            this.Items = filteredInterests;
        }


        @Override
        public android.widget.Filter getFilter() {
            return FilterHelper.newInstance(interestsList, this);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            AppCompatCheckBox btnInterest;
            CustomTextView txtInterest;
            RelativeLayout itemLay;

            public ViewHolder(View itemView) {
                super(itemView);
                btnInterest = itemView.findViewById(R.id.btnInterest);
                txtInterest = itemView.findViewById(R.id.txtInterest);
                itemLay = itemView.findViewById(R.id.itemLay);
            }
        }
    }

    public static class FilterHelper extends Filter {
        static ArrayList<HashMap<String, String>> interestsList;
        static InterestAdapter adapter;

        public static FilterHelper newInstance(ArrayList<HashMap<String, String>> interestsList, InterestAdapter adapter) {
            FilterHelper.adapter = adapter;
            FilterHelper.interestsList = interestsList;
            return new FilterHelper();
        }

        /*
        - Perform actual filtering.
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                //HOLD FILTERS WE FIND
                ArrayList<HashMap<String, String>> foundFilters = new ArrayList<>();

                //ITERATE CURRENT LIST
                for (HashMap<String, String> hashMap : interestsList) {

                    //SEARCH
                    if (hashMap.get(Constants.TAG_NAME).toLowerCase().contains(constraint.toString().toLowerCase())) {
                        //ADD IF FOUND
                        foundFilters.add(hashMap);
                    }
                }

                //SET RESULTS TO FILTER LIST
                filterResults.count = foundFilters.size();
                filterResults.values = foundFilters;
            } else {
                //NO ITEM FOUND.LIST REMAINS INTACT
                filterResults.count = interestsList.size();
                filterResults.values = interestsList;
            }

            //RETURN RESULTS
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            adapter.setInterests((ArrayList<HashMap<String, String>>) filterResults.values);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dialogInterest.dismiss();
        dialogPeopleFor.dismiss();
        SetDialogsActivity.this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {

            boolean isPermissionEnabled = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = false;
                    break;
                } else {
                    isPermissionEnabled = true;
                }
            }

            if (!isPermissionEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                    } else {
                        openPermissionDialog("Camera and Write External Storage");
                    }
                }
            }
        }
    }

    private void openPermissionDialog(String permissionList) {
        Dialog permissionDialog = new Dialog(SetDialogsActivity.this);
        permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        permissionDialog.setContentView(R.layout.logout_dialog);
        permissionDialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        permissionDialog.setCancelable(false);
        permissionDialog.setCanceledOnTouchOutside(false);

        TextView title = permissionDialog.findViewById(R.id.headerTxt);
        TextView subTxt = permissionDialog.findViewById(R.id.subTxt);
        TextView yes = permissionDialog.findViewById(R.id.yes);
        TextView no = permissionDialog.findViewById(R.id.no);
        title.setVisibility(View.GONE);
        subTxt.setText("This app requires " + permissionList + " permissions to access the features. Please turn on");
        yes.setText(R.string.grant);
        no.setText(R.string.nope);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
//                finish();
            }
        });

        no.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(getApplicationContext(), requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(getApplicationContext());
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "profile", timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage("profile", timestamp + ".jpg");
                profileImage = file.getAbsolutePath();
                //   String filepath = ImagePicker.getImageFilePath(this, requestCode, resultCode, data);
                Log.i(TAG, "selectedImageFile: " + file);
                Picasso.with(getApplicationContext()).load(file).resize(180, 250)
                        .centerCrop().into(userImageView);
                btnAddPhoto.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }

        }
    }


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }


        return result.toString();
    }

    public String  performPostCall(String requestURL,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            System.setProperty("http.keepAlive", "false");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(150000);
            conn.setConnectTimeout(150000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            System.out.println("jigar the response on httpurl sign up is " + response);

            e.printStackTrace();
        }
        System.out.println("jigar the response on httpurl sign up is " + response);

        return response;
    }
//    public void signup() {
//        interestProgress.setVisibility(View.VISIBLE);
//        interestProgress.spin();
//        System.setProperty("http.keepAlive", "false");
//
//        StringRequest req1 = new StringRequest(Request.Method.POST, Constants.API_SIGNUP,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String res) {
//                        try {
//                            Log.v(TAG, "jigar getSettingsRes=" + res);
//                            JSONObject response = new JSONObject(res);
//                            String strStatus=response.getString(Constants.TAG_STATUS);
//                            String strMsg=response.getString(Constants.TAG_MSG);
//                            String strInfo=response.getString(Constants.TAG_INFO);
//
//                            if(strStatus.equals("0"))
//                            {
//                                Toast.makeText(SetDialogsActivity.this,strMsg,Toast.LENGTH_LONG).show();
//                            }else
//                            {
//                                GetSet.setUserId(Constants.pref.getString(strInfo, null));
//                                editor.putString(Constants.TAG_USERID, GetSet.getUserId());
//                                System.out.println("jigar the new user id is "+GetSet.getUserId());
//                                Toast.makeText(SetDialogsActivity.this,"User Registration Successful",Toast.LENGTH_LONG).show();
//                            }
//                            interestProgress.setVisibility(View.GONE);
//                            interestProgress.stopSpinning();
//
//                        } catch (JSONException e) {
//                            interestProgress.setVisibility(View.GONE);
//                            interestProgress.stopSpinning();
//
//                            e.printStackTrace();
//                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "jigar Error: " + error.getMessage());
//                System.out.println("jigar the error is "+error.getMessage());
//                interestProgress.setVisibility(View.GONE);
//                interestProgress.stopSpinning();
//
//            }
//
//
//
//        }) {
//
//
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
////                params.put(Constants.TAG_TYPE, "email");
//                params.put(Constants.TAG_COUNTRY_CODE, strCountryCode);
//                params.put(Constants.TAG_MOBILE_NUMBER, strMobileNumber);
//                params.put(Constants.TAG_PASSWORD, strPassword);
//                params.put(Constants.TAG_NAME, strName);
//                params.put(Constants.TAG_REQUEST_USERNAME, strName);
//                params.put(Constants.TAG_GENDER, "male");
//                params.put(Constants.TAG_BIO, "male");
//                params.put(Constants.TAG_PURPOSE_PLAN, peopleForId);
//                params.put(Constants.TAG_INTEREST_PLAN, strSelectedInterest);
//                params.put(Constants.TAG_IMAGES, strProfileImage);
//                params.put(Constants.TAG_DATE_OF_BIRTH, strDateOfBirth);
//                params.put(Constants.TAG_SPONSOR_ID, "");
//                params.put(Constants.TAG_LAT, strLatitude);
//                params.put(Constants.TAG_LON, strLongitude);
//                params.put(Constants.TAG_LOCATION, strLocation);
//                Log.v(TAG, "jigar signupParams=" + params);
//                return params;
//            }
//        };
//
////        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(
////                0,
////                -1,
////                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//
////        req1.setRetryPolicy(mRetryPolicy);
//        req1.setRetryPolicy(new DefaultRetryPolicy(0, -1, 0));
////        req1.setRetryPolicy(
////                new DefaultRetryPolicy(
////                        1500000,
////                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
////                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
////                )
////        );
//        HowzuApplication.getInstance().addToRequestQueue(req1, "");
//
//
//    }



//    private void addDeviceId() {
//        final String token = SharedPrefManager.getInstance(SetDialogsActivity.this).getDeviceToken();
//        final String deviceId = android.provider.Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(),
//                Settings.Secure.ANDROID_ID);
//        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_DEVICE_ID,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String res) {
//                        Log.v(TAG, "addDeviceIdRes=" + res);
//                        try {
//                            JSONObject json = new JSONObject(res);
//                            if (!DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
//                                Toast.makeText(getActivity(), json.getString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//            }
//
//        }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, GetSet.getUserId());
//                map.put(Constants.TAG_DEVICE_TOKEN, token);
//                map.put(Constants.TAG_DEVICE_TYPE, "1");
//                map.put(Constants.TAG_DEVICE_ID, deviceId);
//                map.put(Constants.TAG_DEVICE_MODE, "1");
//                Log.v(TAG, "addDeviceIdParams=" + map);
//                return map;
//            }
//        };
//
//        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
//    }

//    private void setOnlineStatus() {
//        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ONLINE,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String res) {
//                        try {
//                            Log.v(TAG, "setOnlineStatusRes=" + res);
//                        } catch (NullPointerException e) {
//                            e.printStackTrace();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//            }
//
//        }) {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, GetSet.getUserId());
//                map.put(Constants.TAG_STATUS, "1");
//                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
//                Log.v(TAG, "setOnlineStatusParams=" + map);
//                return map;
//            }
//        };
//
//        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
//    }



}
