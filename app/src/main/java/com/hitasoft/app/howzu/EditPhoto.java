package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.customclass.ImagePicker;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 28/2/17.
 */

public class EditPhoto extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    String TAG = "EditPhoto";

    public static ProgressDialog pd;
    ViewPager viewPager;
    ImageView backbtn;
    TextView addPhoto;
    Display display;
    RelativeLayout mainLay;
    FloatingActionButton fab;

    public static boolean imageUploading = false;
    ViewPagerAdapter viewPagerAdapter;
    String userImage = "", imagesJson = "";
    HashMap<String, String> profileMap = new HashMap<String, String>();
    ArrayList<String> imagesAry = new ArrayList<String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_photo);

        viewPager = findViewById(R.id.view_pager);
        backbtn = findViewById(R.id.backbtn);
        fab = findViewById(R.id.fab);
        mainLay = findViewById(R.id.mainLay);
        addPhoto = findViewById(R.id.add_photo);

        profileMap = (HashMap<String, String>) getIntent().getExtras().get("data");
        imagesAry = (ArrayList<String>) getIntent().getExtras().get("images");

        display = getWindowManager().getDefaultDisplay();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        viewPagerAdapter = new ViewPagerAdapter(EditPhoto.this, imagesAry);
        viewPager.setAdapter(viewPagerAdapter);

        backbtn.setOnClickListener(this);
        fab.setOnClickListener(this);
        addPhoto.setOnClickListener(this);

        pd = new ProgressDialog(EditPhoto.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(this);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "profile", timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage("profile", timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                //   String filepath = ImagePicker.getImageFilePath(this, requestCode, resultCode, data);
                Log.i(TAG, "selectedImageFile: " + file);
                imageUploading = true;
                new uploadImage().execute(filepath);
            } else {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
        if (imageUploading) {
            pd.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        if (imageUploading) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        ProfileActivity.activity.finish();
        Intent e = new Intent(EditPhoto.this, ProfileActivity.class);
        e.putExtra("strVisitingIdLikeToken", "myprofile");
        e.putExtra("strFriendID", GetSet.getUserId());
        e.putExtra("sendMatch", "");
        startActivity(e);
    }

    private void dialog(final String from) {
        final Dialog dialog = new Dialog(EditPhoto.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.headerTxt);
        TextView subTxt = dialog.findViewById(R.id.subTxt);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);

        title.setVisibility(View.GONE);

        if (from.equals("delete")) {
            subTxt.setText(getString(R.string.delete_image));
        } else if (from.equals("default")) {
            subTxt.setText(getString(R.string.default_image));
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                userImage = "";
                if (from.equals("delete")) {
                    deleteImageFromServer(CommonFunctions.getImageName(imagesAry.get(viewPager.getCurrentItem())));
                    imagesAry.remove(viewPager.getCurrentItem());
                    if (viewPager.getCurrentItem() == 0) {
                        userImage = imagesAry.get(0);
                    }
                    viewPagerAdapter.notifyDataSetChanged();
                } else if (from.equals("default")) {
                    userImage = imagesAry.get(viewPager.getCurrentItem());
                    imagesAry.remove(viewPager.getCurrentItem());
                    imagesAry.add(0, userImage);
                }

                try {
                    JSONArray jsonArray = new JSONArray(imagesAry);
                    imagesJson = String.valueOf(jsonArray);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                saveSettings(from);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * function for showing the popup window
     **/
    public void viewOptions(View v) {
        String[] values = new String[]{getString(R.string.default_photo), getString(R.string.delete)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditPhoto.this,
                R.layout.options_item, android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                if (viewPager.getCurrentItem() == 0 && position == 0) {
                    text.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                return view;
            }
        };
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.edit_photo_options, null);
        final PopupWindow popup = new PopupWindow(EditPhoto.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 45 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(mainLay, Gravity.BOTTOM | Gravity.RIGHT, HowzuApplication.dpToPx(EditPhoto.this, 15), HowzuApplication.dpToPx(EditPhoto.this, 90));

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        popup.dismiss();
                        if (viewPager.getCurrentItem() != 0) {
                            dialog("default");
                        }
                        break;
                    case 1:
                        popup.dismiss();
                        if (imagesAry.size() == 1) {
                            Toast.makeText(EditPhoto.this, "MainViewProfileDetailActivity image should have atleast one!!", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog("delete");
                        }
                        break;
                }
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        @Override
        public int getCount() {
            return temp.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int posi) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_fullscreen,
                    collection, false);

            ImageView image = itemView.findViewById(R.id.imgDisplay);
            if (!temp.get(posi).equals("")) {
                Picasso.with(context).load(temp.get(posi)).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder).into(image);
            }

            collection.addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    /**
     * for upload user image
     **/

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
                    }

                    imagesAry.add(viewUrl);
                    try {
                        JSONArray jsonArray = new JSONArray(imagesAry);
                        imagesJson = String.valueOf(jsonArray);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    saveSettings("add");

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
            if (!EditPhoto.this.isFinishing()) {
                pd.show();
            }
        }

        @Override
        protected void onPostExecute(Integer unused) {
            try {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                imageUploading = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * API Implementation
     */

    private void saveSettings(final String from) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_PROFILE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        try {
                            Log.v(TAG, "saveSettingsRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(EditPhoto.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                                viewPagerAdapter.notifyDataSetChanged();
                                if (from.equals("add")) {
                                    viewPager.setCurrentItem((imagesAry.size() - 1), true);
                                } else if (from.equals("default")) {
                                    viewPager.setCurrentItem(0, true);
                                }
                                if (from.equals("default") || (from.equals("delete") && viewPager.getCurrentItem() == 0)) {
                                    GetSet.setImageUrl(userImage);
                                    if (MainScreenActivity.userImage != null) {
                                        Picasso.with(EditPhoto.this).load(GetSet.getImageUrl()).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                                                .into(MainScreenActivity.userImage);
                                    }

                                    if (HomeFragment.userImage != null) {
                                        Picasso.with(EditPhoto.this).load(GetSet.getImageUrl()).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                                                .into(HomeFragment.userImage);
                                    }

                                    if (FindPeople.userImage != null) {
                                        Picasso.with(EditPhoto.this)
//                                                .load(GetSet.getImageUrl()).placeholder(R.drawable.user_placeholder)
                                                .load(Constants.pref.getString("user_image", null))
                                                .error(R.drawable.user_placeholder)
                                                .into(FindPeople.userImage);
                                    }
                                    editor.putString(Constants.TAG_USERIMAGE, userImage);
                                    editor.commit();
                                }
                                /*if (strVisitingIdLikeToken.equals("delete") || strVisitingIdLikeToken.equals("default")){
                                    finish();
                                    MainViewProfileDetailActivity.activity.finish();
                                    Intent e = new Intent(EditPhoto.this, MainViewProfileDetailActivity.class);
                                    e.putExtra("strVisitingIdLikeToken", "myprofile");
                                    e.putExtra("strFriendID", GetSet.getUserId());
                                    e.putExtra("sendMatch", "");
                                    startActivity(e);
                                }*/
                            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(EditPhoto.this, "Error", json.getString(Constants.TAG_MESSAGE));
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
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
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
                if (from.equals("add")) {
                    map.put(Constants.TAG_IMAGES, imagesJson);
                } else if (from.equals("default")) {
                    map.put(Constants.TAG_USERIMAGE, userImage);
                    map.put(Constants.TAG_IMAGES, imagesJson);
                } else if (from.equals("delete")) {
                    if (viewPager.getCurrentItem() == 0) {
                        map.put(Constants.TAG_USERIMAGE, userImage);
                        map.put(Constants.TAG_IMAGES, imagesJson);
                    } else {
                        map.put(Constants.TAG_IMAGES, imagesJson);
                    }
                }
                Log.v(TAG, "saveSettingsParams=" + map);
                return map;
            }
        };
        if (!pd.isShowing()) {
            pd.show();
        }
        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    public void deleteImageFromServer(final String image) {
        StringRequest deleteImg = new StringRequest(Request.Method.POST, Constants.API_DELETE_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "deleteImageFromServerRes" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {

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
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_IMAGE, image);
                params.put(Constants.TAG_USERID, GetSet.getUserId());
                params.put(Constants.TAG_TYPE, "user");
                Log.v(TAG, "deleteImageFromServerParams=" + params);
                return params;
            }

        };

        HowzuApplication.getInstance().addToRequestQueue(deleteImg, "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                ProfileActivity.activity.finish();
                Intent e = new Intent(EditPhoto.this, ProfileActivity.class);
                e.putExtra("strVisitingIdLikeToken", "myprofile");
                e.putExtra("strFriendID", GetSet.getUserId());
                e.putExtra("sendMatch", "");
                startActivity(e);
                break;
            case R.id.fab:
                viewOptions(fab);
                break;
            case R.id.add_photo:
                if (ContextCompat.checkSelfPermission(EditPhoto.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(EditPhoto.this, CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditPhoto.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    if (imagesAry.size() < 5) {
                        ImagePicker.pickImage(EditPhoto.this, "Select your image:");
                    } else {
                        Toast.makeText(getApplicationContext(), "Cannot upload more than 5 images", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
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
        Dialog permissionDialog = new Dialog(EditPhoto.this);
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
                Uri uri = Uri.fromParts("package", EditPhoto.this.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
//                finish();
            }
        });

        no.setVisibility(View.VISIBLE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(EditPhoto.this, permissions, requestCode);
    }
}
