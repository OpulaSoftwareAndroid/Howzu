package com.hitasoft.app.howzu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.hitasoft.app.customclass.Blur;
import com.hitasoft.app.customclass.CircleTransform;
import com.hitasoft.app.customclass.ImagePicker;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.helper.ImageCompression;
import com.hitasoft.app.helper.ImageStorage;
import com.hitasoft.app.helper.MyFirebaseMessagingService;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, AbsListView.OnScrollListener
        , NetworkReceiver.ConnectivityReceiverListener {
    public static EditText editText;
    public static ProgressDialog pd;
    public static String userId, chatId, userImage, userName, lastMessage = "", lastDate = "";
    public static int position;
    public static boolean imageUploading = false, fromChat = false;
    public static ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>();
    String TAG = "ChatActivity", from = "";
    ListView listView;
    TextView username, nullText, online, chatStatus;
    ProgressWheel progress;
    RelativeLayout chatUserLay, bottomLay, mainLay;
    ImageView userimage, attachbtn, optionbtn, backbtn, send, audioCallBtn, videoCallBtn, scrollDown;
    ViewGroup header, footer;
    Display display;
    AVLoadingIndicatorView typing, topProgress;
    ChatAdapter chatAdapter;
    boolean pulldown = false, loading = false, meTyping;
    int currentPage = 0;
    Handler handler = new Handler();
    Runnable runnable;
    ArrayList<HashMap<String, String>> tempAry = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> data = new HashMap<String, String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Socket mSocket;

    /**
     * Function for receiving the instant messages & typing status
     **/

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        Log.d(TAG,"jigar the emittor have data on typing is "+data);
                        if (data.getString("sender_id").equals(userId)) {
                            if (data.getString("message").equals("type")) {
                                if (typing.getVisibility() != View.VISIBLE) {
                                    typing.setVisibility(View.VISIBLE);
                                    typing.smoothToShow();
                                    if (chats.size() > 0 && chats.size() < listView.getLastVisiblePosition()) {
                                        listView.setSelection(chats.size() - 1);
                                    }
                                }
                            } else {
                                typing.setVisibility(View.INVISIBLE);
                                typing.smoothToHide();
                            }
                        }
                    } catch (JSONException e) {
                        Log.d(TAG,"jigar the emittor json error have is "+e);

                        e.printStackTrace();

                    }
                }
            });
        }
    };


    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v("onMessage", "onMessage=" + args[0]);
            Log.d(TAG,"jigar the emittor on message have data is "+args[0]);

            String s = args[0].toString();
            JSONObject data = (JSONObject) args[0];
            try {
                if (data.getString("sender_id").equals(userId)) {
                    HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("message", data.getJSONObject("message").getString("message"));
                    hmap.put("sender", data.getString("sender_id"));
                    hmap.put("date", data.getJSONObject("message").getString("chatTime"));
                    hmap.put("type", data.getJSONObject("message").getString("type"));
                    hmap.put("image", data.getJSONObject("message").getString("upload_image"));
                    chats.add(hmap);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //	nullLay.setVisibility(View.GONE);
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                Log.d(TAG,"jigar the error json have emittor on message have data is "+e);

                e.printStackTrace();
            }
        }
    };
    private Dialog permissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        listView = findViewById(R.id.listView);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);
        progress = findViewById(R.id.progress);
        chatUserLay = findViewById(R.id.chatUserLay);
        userimage = findViewById(R.id.userImg);
        username = findViewById(R.id.userName);
        online = findViewById(R.id.online);
        attachbtn = findViewById(R.id.attachbtn);
        audioCallBtn = findViewById(R.id.audioCallBtn);
        videoCallBtn = findViewById(R.id.videoCallBtn);
        optionbtn = findViewById(R.id.optionbtn);
        backbtn = findViewById(R.id.backbtn);
        bottomLay = findViewById(R.id.bottom);
        mainLay = findViewById(R.id.mainLay);
        chatStatus = findViewById(R.id.chat_status);

        display = getWindowManager().getDefaultDisplay();

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();

        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.chat_header, null);
        listView.addHeaderView(header);

        LayoutInflater inflater2 = getLayoutInflater();
        footer = (ViewGroup) inflater2.inflate(R.layout.chat_footer, null);
        listView.addFooterView(footer);

        listView.setSmoothScrollbarEnabled(true);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setScrollingCacheEnabled(false);

        topProgress = header.findViewById(R.id.topProgress);
        nullText = header.findViewById(R.id.nulltext);
        typing = footer.findViewById(R.id.typing);

        // register connection status listener
        HowzuApplication.getInstance().setConnectivityListener(this);

        chatUserLay.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        audioCallBtn.setVisibility(View.VISIBLE);
        videoCallBtn.setVisibility(View.VISIBLE);
        optionbtn.setVisibility(View.VISIBLE);
        topProgress.setVisibility(View.GONE);
        nullText.setVisibility(View.GONE);

        send.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        attachbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        userimage.setOnClickListener(this);
        editText.addTextChangedListener(this);
        audioCallBtn.setOnClickListener(this);
        videoCallBtn.setOnClickListener(this);


        lastMessage = "";
        data = (HashMap<String, String>) getIntent().getExtras().get("data");
        userName = data.get(Constants.TAG_USERNAME);
        userId = data.get(Constants.TAG_USERID);
        userImage = data.get(Constants.TAG_USERIMAGE);
        chatId = data.get(Constants.TAG_CHAT_ID);
        position = getIntent().getExtras().getInt("position");
        from = getIntent().getExtras().getString("from");

        if (from.equals("notification")) {
            getChatStatusFromNotification();
        }

        String img = Constants.RESIZE_URL + CommonFunctions.getImageName(userImage) + Constants.IMAGE_RES;
        Picasso.with(ChatActivity.this).load(userImage)
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                .centerCrop()
                .into(userimage);
        username.setText(userName);

        if (data.get(Constants.TAG_BLOCKED_BY_ME).equals("true")) {
            chatStatus.setVisibility(View.VISIBLE);
            chatStatus.setText(getString(R.string.you_blocked));
            editText.setEnabled(false);
            send.setOnClickListener(null);
            attachbtn.setOnClickListener(null);
            audioCallBtn.setOnClickListener(null);
            videoCallBtn.setOnClickListener(null);
        } else if (data.get(Constants.TAG_USER_STATUS).equals("0")) {
            Picasso.with(this).load(R.drawable.user_placeholder).into(userimage);
            chatStatus.setVisibility(View.VISIBLE);
            chatStatus.setText(userName + " " + getString(R.string.account_deactivated));
            editText.setEnabled(false);
            send.setOnClickListener(null);
            attachbtn.setOnClickListener(null);
            optionbtn.setOnClickListener(null);
            audioCallBtn.setOnClickListener(null);
            videoCallBtn.setOnClickListener(null);
        } else if (data.get(Constants.TAG_BLOCK).equals("true")) {
            chatStatus.setVisibility(View.VISIBLE);
            chatStatus.setText(userName + " " + getString(R.string.other_blocked));
            editText.setEnabled(false);
            send.setOnClickListener(null);
            attachbtn.setOnClickListener(null);
            audioCallBtn.setOnClickListener(null);
            videoCallBtn.setOnClickListener(null);
        } else {
            chatStatus.setVisibility(View.GONE);
        }

        try {
            if (data.get(Constants.TAG_ONLINE).equals("1")) {
                online.setText("Online");
                online.setSelected(false);
            } else {
                online.setText("Last seen at " + getTime(Long.parseLong(data.get(Constants.TAG_LAST_ONLINE)), Constants.TAG_LAST_SEEN));
                online.setSelected(true);
            }
        } catch (NumberFormatException e) {
            Log.v(TAG, "jigar the number format exception in  Chat Params=" +e);

            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.v(TAG, "jigar the null pointer exception in  Chat Params=" +e);

            e.printStackTrace();
        } catch (Exception e) {
            Log.v(TAG, "jigar the main exception in  Chat Params=" +e);

            e.printStackTrace();
        }

        /*Socket Connection*/

        JSONObject jobj = new JSONObject();
        try {
            jobj.put("join_id", GetSet.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HowzuApplication app = (HowzuApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on("message", onMessage);
        mSocket.on("typing", onTyping);
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_CONNECT", "EVENT_CONNECT");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
            }
        });
        mSocket.connect();

        mSocket.emit("join", jobj);

        chats.clear();
        chatAdapter = new ChatAdapter(ChatActivity.this, chats);
        listView.setAdapter(chatAdapter);
        getChat(0);

        pd = new ProgressDialog(ChatActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (data.get(Constants.TAG_BLOCK).equals("false")) {
            editText.setError(null);
            if (runnable != null)
                handler.removeCallbacks(runnable);
            if (!meTyping) {
                meTyping = true;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put("receiver_id", userId);
                    jobj.put("sender_id", GetSet.getUserId());
                    jobj.put("message", "type");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("typing", jobj);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (data.get(Constants.TAG_BLOCK).equals("false")) {
            runnable = new Runnable() {

                public void run() {
                    Log.v(TAG, "stop typing");
                    meTyping = false;
                    JSONObject jobj = new JSONObject();
                    try {
                        jobj.put(Constants.TAG_RECEIVER_ID, userId);
                        jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                        jobj.put(Constants.TAG_MESSAGE, "untype");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("typing", jobj);
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(this);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "sent", timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage("sent", timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.i(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        imageUploading = true;
                        new uploadImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        final Picasso picasso = Picasso.with(this);
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            picasso.resumeTag(this);
        } else {
            picasso.pauseTag(this);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "firstVisibleItem: " + firstVisibleItem);
        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (CommonFunctions.isNetworkAvailable(ChatActivity.this)) {
                getChat(currentPage);
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MyFirebaseMessagingService.chatUserID = data.get(Constants.TAG_USERID);
        HowzuApplication.activityResumed();
        if (NetworkReceiver.isConnected()) {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        } else {
            HowzuApplication.showSnack(this, findViewById(R.id.mainLay), false);
        }
        if (imageUploading) {
            pd.show();
        }
        if (CallActivity.from.equals("send") || CallActivity.from.equals("receive")) {
//            CallActivity.from = "";
            pulldown = true;
            chats.clear();
            currentPage = 0;
            getChat(0);
        }

    }

    @Override
    public void onPause() {
        editText.setError(null);
        MyFirebaseMessagingService.chatUserID = "";
        HowzuApplication.activityPaused();
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), true);
        //imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if (imageUploading) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
        super.onPause();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        HowzuApplication.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    protected void onDestroy() {
        MyFirebaseMessagingService.chatUserID = "";
        disconnectSocket();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        getChat(0);
        disconnectSocket();
        if (from.equals("notification")) {
            finish();
            MainScreenActivity.resumeMessage = true;
            Intent i = new Intent(ChatActivity.this, MainScreenActivity.class);
            startActivity(i);
        } else if (from.equals("message")) {
            ChatActivity.fromChat = true;
        }
        super.onBackPressed();
    }

    private ArrayList<HashMap<String, String>> parsing(String url) {
        ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(url);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            if (response.equalsIgnoreCase("true")) {
                String chatId = DefensiveClass.optString(json, Constants.TAG_CHAT_ID);
                JSONArray chat = json.optJSONArray(Constants.TAG_CHATS);
                if (chat != null) {
                    for (int i = 0; i < chat.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject temp = chat.getJSONObject(i);
                        map.put("type", DefensiveClass.optString(temp, Constants.TAG_TYPE));
                        map.put("image", DefensiveClass.optString(temp, Constants.TAG_UPLOAD_IMAGE));
                        map.put("message", DefensiveClass.optString(temp, Constants.TAG_MESSAGE));
                        map.put("sender", DefensiveClass.optString(temp, Constants.TAG_SENDER_ID));
                        map.put("date", DefensiveClass.optString(temp, Constants.TAG_CHAT_TIME));
                        chats.add(map);
                    }
                }
            } else if (response.equalsIgnoreCase("error")) {
                CommonFunctions.disabledialog(ChatActivity.this, "Error", json.getString("message"));
            } else {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    public Bitmap compressImage(String imagePath, int maxWidth, int maxHeight) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        if (actualHeight == 0) {
            float scale = (float) maxWidth / options.outWidth;
            actualHeight = (int) Math.round(options.outHeight * scale);
        }

        if (actualWidth == 0) {
            float scale = (float) maxHeight / options.outHeight;
            actualWidth = (int) Math.round(options.outWidth * scale);
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        if (bmp != null) {
            bmp.recycle();
        }
        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private Transformation blurTransformation(final Context mContext) {
        Transformation blurTrans = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap blurred = Blur.fastblur(mContext, source, 10);
                source.recycle();
                return blurred;
            }

            @Override
            public String key() {
                return "blur()";
            }
        };
        return blurTrans;
    }

    private String getImageName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    /**
     * To convert timestamp to Date
     **/
    private String getTime(long timeStamp, String to) {
        DateFormat sdf;
        try {
            if (to.equals(Constants.TAG_DATE)) {
                sdf = new SimpleDateFormat("MMM d, yyyy");
            } else if (to.equals(Constants.TAG_LAST_SEEN)) {
                sdf = new SimpleDateFormat("dd/MM/yy, h:mm a");
            } else {
                sdf = new SimpleDateFormat("h:mm a");
            }
            Date netDate = (new Date(timeStamp * 1000));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }


    /**
     * function for showing the popup window
     **/

    public void viewOptions(View v) {
        final String[] values;
        if (data.get(Constants.TAG_BLOCKED_BY_ME).equals("true")) {
            values = new String[]{getString(R.string.unblock), getString(R.string.clear_chat), getString(R.string.unfriend_user)};
        } else if (data.get(Constants.TAG_BLOCK).equals("true")) {
            values = new String[]{getString(R.string.clear_chat), getString(R.string.unfriend_user)};
        } else {
            values = new String[]{getString(R.string.block), getString(R.string.clear_chat), getString(R.string.unfriend_user)};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.options_item, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.options, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(ChatActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 50 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (values[position].equals(getString(R.string.unblock)) || values[position].equals(getString(R.string.block))) {
                    dialog("block");
                    popup.dismiss();
                } else if (values[position].equals(getString(R.string.clear_chat))) {
                    dialog("clearchat");
                    popup.dismiss();
                } else if (values[position].equals(getString(R.string.unfriend_user))) {
                    dialog("unfriend");
                    popup.dismiss();
                }

            }

        });
    }

    private void dialog(final String type) {
        final Dialog dialog = new Dialog(ChatActivity.this);
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

        if (type.equals("block")) {
            if (data.get(Constants.TAG_BLOCKED_BY_ME).equals("true")) {
                title.setText(getString(R.string.unblock));
                subTxt.setText(getString(R.string.really_unblock));
            } else {
                title.setText(getString(R.string.block));
                subTxt.setText(getString(R.string.really_block));
            }
        } else if (type.equals("unfriend")) {
            title.setText(getString(R.string.unfriend_user));
            subTxt.setText(getString(R.string.really_unfriend));
        } else {
            title.setText(getString(R.string.clear_chat));
            subTxt.setText(getString(R.string.clear_confirmation_msg));
        }
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (type.equals("block"))
                    blockUser();
                else if (type.equals("unfriend"))
                    unfriendUser();
                else
                    clearChat();
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

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("message");
            mSocket.off("typing");
            mSocket.disconnect();
        }
    }

    /**
     * Function for Sent a message to Socket
     */

    private void callSocket(String usrMessage, String imageUrl, String type) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put("userName", GetSet.getUserName());
            message.put("userImage", GetSet.getImageUrl());
            message.put("chatTime", String.valueOf(System.currentTimeMillis() / 1000L));
            message.put("type", type);
            message.put("upload_image", imageUrl);
            message.put("message", usrMessage);
            jobj.put("receiver_id", userId);
            jobj.put("sender_id", GetSet.getUserId());
            jobj.put("message", message);
            Log.v(TAG, "sendjsoninsocket=" + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * API Implementation
     */

    private void getChat(final Integer params) {
        loading = true;
        if (pulldown) {
            bottomLay.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            bottomLay.setVisibility(View.GONE);
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            progress.spin();
        }

        final int offset = (params * 20);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
//                            Log.v(TAG, "getChatRes=" + res);
                            Log.v(TAG, "jigar the get Chat response we have =" + res);

                            tempAry.clear();
                            tempAry.addAll(parsing(res));
                            Collections.reverse(tempAry);
                            ArrayList<HashMap<String, String>> backup = new ArrayList<HashMap<String, String>>();
                            backup.addAll(chats);
                            chats.clear();
                            chats.addAll(tempAry);
                            chats.addAll(backup);

                            if (tempAry.size() == 0) {
                                listView.setOnScrollListener(null);
                            } else {
                                listView.setOnScrollListener(ChatActivity.this);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (pulldown) {
                                        pulldown = false;
                                        listView.setSelection(chats.size() - 1);
                                        chatAdapter.notifyDataSetChanged();
                                        listView.setSelection(tempAry.size());

                                    } else {
                                        chatAdapter.notifyDataSetChanged();
                                        if (chats.size() > 0) {
                                            listView.setSelection(chats.size() - 1);
                                        }
                                    }
                                }
                            });

                            if (chats.size() > 18) {
                                if (tempAry.size() == 0) {
                                    nullText.setVisibility(View.VISIBLE);
                                    topProgress.setVisibility(View.GONE);
                                }
                            }
                            loading = false;
                            topProgress.setVisibility(View.GONE);
                            bottomLay.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.VISIBLE);
                            progress.stopSpinning();
                            progress.setVisibility(View.GONE);
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
                Log.v(TAG, "jigar the error in volley is in  Chat " + error);

            }

        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_CHAT_ID, chatId);
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                Log.v(TAG, "jigar the get Chat Params=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void sendChat(final String type, final String params) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "jigar the send Chat message response " + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else if (response.equalsIgnoreCase("false")) {
                                CommonFunctions.disabledialog(ChatActivity.this, "Blocked User", json.getString("message"));
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(ChatActivity.this, "Error!", json.getString("message"));
                            } else {
                                Toast.makeText(ChatActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.v(TAG, "jigar the json error in send Chat message response " + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.v(TAG, "jigar the null pointer error in send Chat message response " + e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.v(TAG, "jigar the main exception error in send Chat message response " + e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.v(TAG, "jigar the main volley error in send Chat message response " + error.getMessage());
            }

        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_CHAT_ID, chatId);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_RECEIVER_ID, userId);
                map.put(Constants.TAG_CHAT_TIME, String.valueOf(System.currentTimeMillis() / 1000L));
                map.put(Constants.TAG_TYPE, type);
                if (type.equals("text")) {
                    map.put(Constants.TAG_MESSAGE, params);
                    map.put(Constants.TAG_UPLOAD_IMAGE, "");
                } else {
                    map.put(Constants.TAG_MESSAGE, "");
                    map.put(Constants.TAG_UPLOAD_IMAGE, params);
                }
///                Log.v(TAG, "sendChatParams" + map);
                Log.v(TAG, "jigar the main exception error in send Chat message response " + map);

                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void blockUser() {
        final ProgressDialog pd = new ProgressDialog(ChatActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_BLOCK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "blockuserRes=" + res);
                        try {
                            pd.dismiss();
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                finish();
                                MainScreenActivity.resumeMessage = true;
                                Intent i = new Intent(ChatActivity.this, MainScreenActivity.class);
                                startActivity(i);
                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(ChatActivity.this, "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pd.dismiss();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            pd.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pd.dismiss();
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
                map.put(Constants.TAG_BLOCK_USER_ID, userId);
                Log.v(TAG, "blockuserParams" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void getChatStatusFromNotification() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "getChatStatusFromNotificationRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                                data.put(Constants.TAG_CHAT_ID, DefensiveClass.optString(json, Constants.TAG_CHAT_ID));
                                data.put(Constants.TAG_BLOCKED_BY_ME, DefensiveClass.optInt(json, Constants.TAG_BLOCKED_BY_ME));
                                data.put(Constants.TAG_BLOCK, DefensiveClass.optInt(json, Constants.TAG_BLOCK));
                                data.put(Constants.TAG_USER_STATUS, DefensiveClass.optInt(json, Constants.TAG_USER_STATUS));
                                data.put(Constants.TAG_ONLINE, DefensiveClass.optInt(json, Constants.TAG_ONLINE));
                                data.put(Constants.TAG_LAST_ONLINE, DefensiveClass.optInt(json, Constants.TAG_LAST_ONLINE));

                                if (data.get(Constants.TAG_BLOCKED_BY_ME).equals("true")) {
                                    chatStatus.setVisibility(View.VISIBLE);
                                    chatStatus.setText(getString(R.string.you_blocked));
                                    editText.setEnabled(false);
                                    send.setOnClickListener(null);
                                    attachbtn.setOnClickListener(null);
                                } else if (data.get(Constants.TAG_USER_STATUS).equals("0")) {
                                    Picasso.with(ChatActivity.this).load(R.drawable.user_placeholder).transform(new CircleTransform()).into(userimage);
                                    chatStatus.setVisibility(View.VISIBLE);
                                    chatStatus.setText(getString(R.string.account_deactivated));
                                    editText.setEnabled(false);
                                    send.setOnClickListener(null);
                                    attachbtn.setOnClickListener(null);
                                    optionbtn.setOnClickListener(null);
                                } else {
                                    chatStatus.setVisibility(View.GONE);
                                }

                                try {
                                    if (data.get(Constants.TAG_ONLINE).equals("1")) {
                                        online.setText("Online");
                                        online.setSelected(false);
                                    } else {
                                        online.setText("Last seen at " + getTime(Long.parseLong(data.get(Constants.TAG_LAST_ONLINE)), Constants.TAG_LAST_SEEN));
                                        online.setSelected(true);
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

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
                map.put(Constants.TAG_FRIEND_ID, userId);
                Log.v(TAG, "getChatStatusFromNotificationParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void clearChat() {


        pd.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CLEAR_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            pd.dismiss();
                            JSONObject json = new JSONObject(res);
                            Log.v(TAG, "clearChatRes=" + res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                fromChat = true;
                                finish();

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(ChatActivity.this, "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pd.dismiss();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            pd.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pd.dismiss();
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
                map.put(Constants.TAG_CHAT_ID, chatId);
                Log.v(TAG, "clearChatParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void unfriendUser() {
        pd.show();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UNFRIEND_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "unfriendUserRes=" + res);
                            JSONObject json = new JSONObject(res);
                            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                Toast.makeText(ChatActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                finish();
                                Intent intent = new Intent(ChatActivity.this, MainScreenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
                map.put(Constants.TAG_UNFRIEND_USER_ID, userId);
                Log.v(TAG, "unfriendUserParams=" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
                    RECORD_AUDIO);

            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "audio");
                video.putExtra("data", data);
                startActivity(video);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, 100);
                    } else {
                        openPermissionDialog("Camera, Record Audio, Write External Storage");
                    }
                }
            }
        } else if (requestCode == 101) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
                    RECORD_AUDIO);

            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "video");
                video.putExtra("data", data);
                startActivity(video);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, 101);
                    } else {
                        openPermissionDialog("Camera, Record Audio, Write External Storage");
                    }
                }
            }
        } else if (requestCode == 102) {
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE);
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.pickImage(this, "Select your image:");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 102);
                    } else {
                        openPermissionDialog("Camera, Write External Storage");
                    }
                }
            }
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(ChatActivity.this, permissions, requestCode);
    }

    public class ChatAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        ViewHolder holder = null;
        private Context mContext;

        public ChatAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
        }

        @Override
        public int getCount() {

            return Items.size();
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_item, parent, false);//layout
                holder = new ViewHolder();

                holder.leftMessage = convertView.findViewById(R.id.left_text);
                holder.leftLay = (RelativeLayout) convertView.findViewById(R.id.left_lay);
                holder.rightMessage = convertView.findViewById(R.id.right_text);
                holder.rightLay = (RelativeLayout) convertView.findViewById(R.id.right_lay);
                holder.date = convertView.findViewById(R.id.date);
                holder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
                holder.rightImage = (ImageView) convertView.findViewById(R.id.right_image);
                holder.leftCorner = (ImageView) convertView.findViewById(R.id.left_corner);
                holder.rightCorner = (ImageView) convertView.findViewById(R.id.right_corner);
                holder.leftImageLay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.rightImageLay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.leftDownloadIcon = (ImageView) convertView.findViewById(R.id.left_download_icon);
                holder.rightDownloadIcon = (ImageView) convertView.findViewById(R.id.right_download_icon);
                holder.leftProgress = (ProgressWheel) convertView.findViewById(R.id.left_progress);
                holder.rightProgress = (ProgressWheel) convertView.findViewById(R.id.right_progress);
                holder.missedCallLay = (LinearLayout) convertView.findViewById(R.id.missedCallLay);
                holder.missedText = (TextView) convertView.findViewById(R.id.missedText);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {

                final HashMap<String, String> tempMap = Items.get(position);

                if (tempMap.get("sender").equals(GetSet.getUserId())) {
                    holder.leftLay.setVisibility(View.GONE);
                    holder.rightLay.setVisibility(View.VISIBLE);

                    if (tempMap.get(Constants.TAG_TYPE).equals("text")) {
                        holder.rightCorner.setVisibility(View.VISIBLE);
                        holder.rightMessage.setVisibility(View.VISIBLE);
                        //	holder.rightMessage.setText(StringEscapeUtils.unescapeJava(tempMap.get("message")));
                        holder.rightMessage.setText(tempMap.get(Constants.TAG_MESSAGE));
                        holder.rightImageLay.setVisibility(View.GONE);
                        holder.missedCallLay.setVisibility(View.GONE);
                    } else if (tempMap.get(Constants.TAG_TYPE).equals("missed")) {
                        holder.leftLay.setVisibility(View.GONE);
                        holder.rightLay.setVisibility(View.GONE);
                        holder.missedCallLay.setVisibility(View.VISIBLE);
                        holder.missedText.setText("Missed " + tempMap.get(Constants.TAG_MESSAGE) + " call at " + getTime(Long.parseLong(tempMap.get("date")), ""));
                    } else {
                        holder.rightImageLay.setVisibility(View.VISIBLE);
                        holder.rightDownloadIcon.setVisibility(View.GONE);
                        holder.missedCallLay.setVisibility(View.GONE);
                        if (!tempMap.get("image").equals("")) {
                            String imgSplit = getImageName(tempMap.get("image"));
                            String imgSplitUrl = Constants.RESIZE_CHAT_URL + imgSplit + "/50/50";
                            Log.v(TAG, "imgSplit=" + imgSplitUrl);
                            int imgSize = HowzuApplication.dpToPx(mContext, 150);
                            ImageStorage imageStorage = new ImageStorage(mContext);
                            if (imageStorage.checkifImageExists("thumb", imgSplit)) {
                                File file = imageStorage.getImage("thumb", imgSplit);
                                if (file != null) {
                                    Log.v(TAG, "file=" + file.getAbsolutePath());
                                    holder.rightDownloadIcon.setVisibility(View.GONE);
                                    Picasso.with(mContext).load(file).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.rightImage);
                                }
                            } else {
                                Picasso.with(mContext).load(imgSplitUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.rightImage);
                            }
                        }
                        holder.rightMessage.setVisibility(View.GONE);
                        holder.rightCorner.setVisibility(View.GONE);
                    }

                } else {
                    holder.leftLay.setVisibility(View.VISIBLE);
                    holder.rightLay.setVisibility(View.GONE);


                    if (tempMap.get(Constants.TAG_TYPE).equals("text")) {
                        holder.leftCorner.setVisibility(View.VISIBLE);
                        holder.leftMessage.setVisibility(View.VISIBLE);
                        holder.leftMessage.setText(tempMap.get(Constants.TAG_MESSAGE));
                        holder.leftImageLay.setVisibility(View.GONE);
                        holder.missedCallLay.setVisibility(View.GONE);
                    } else if (tempMap.get(Constants.TAG_TYPE).equals("missed")) {
                        holder.leftLay.setVisibility(View.GONE);
                        holder.rightLay.setVisibility(View.GONE);
                        holder.missedCallLay.setVisibility(View.VISIBLE);
                        holder.missedText.setText("Missed " + tempMap.get(Constants.TAG_MESSAGE) + " call at " + getTime(Long.parseLong(tempMap.get("date")), ""));
                    } else {
                        holder.leftImageLay.setVisibility(View.VISIBLE);
                        holder.leftDownloadIcon.setVisibility(View.VISIBLE);
                        holder.missedCallLay.setVisibility(View.GONE);
                        if (!tempMap.get("image").equals("")) {
                            String imgSplit = getImageName(tempMap.get("image"));
                            String imgSplitUrl = Constants.RESIZE_CHAT_URL + imgSplit + "/50/50";
                            //Log.v("imgSplit", "imgSplit="+imgSplit);
                            ImageStorage imageStorage = new ImageStorage(mContext);
                            if (imageStorage.checkifImageExists("thumb", imgSplit)) {
                                File file = imageStorage.getImage("thumb", imgSplit);
                                if (file != null) {
                                    //Log.v("file", "file="+file.getAbsolutePath());
                                    holder.leftDownloadIcon.setVisibility(View.GONE);
                                    int imgSize = HowzuApplication.dpToPx(mContext, 150);
                                    ;
                                    Picasso.with(mContext).load(file).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                                }
                            } else {
                                Picasso.with(mContext).load(imgSplitUrl).transform(blurTransformation(mContext)).into(holder.leftImage);
                            }
                        }
                        holder.leftMessage.setVisibility(View.GONE);
                        holder.leftCorner.setVisibility(View.GONE);
                    }
                }

                try {
                    String chatDate = getTime(Long.parseLong(tempMap.get("date")), Constants.TAG_DATE);
                    if (position == 0) {
                        holder.date.setVisibility(View.VISIBLE);
                        holder.date.setText(chatDate);
                    } else {
                        String ldate = getTime(Long.parseLong(Items.get(position - 1).get("date")), Constants.TAG_DATE);
                        if (ldate.equals(chatDate)) {
                            holder.date.setVisibility(View.GONE);
                        } else {
                            holder.date.setVisibility(View.VISIBLE);
                            holder.date.setText(chatDate);
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                Log.v("ht", "ht=" + HowzuApplication.dpToPx(mContext, 150));

                holder.rightImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                        } else {
                            View v = (View) view.getParent();
                            ImageStorage imageStorage = new ImageStorage(mContext);
                            String imgSplit = getImageName(tempMap.get("image"));
                            Log.v("imgSplit", "imgSplit=" + imgSplit);
                            if (imageStorage.checkifImageExists("sent", imgSplit)) {
                                Log.v("Already Downloaded", "Already Downloaded");
                                File file = imageStorage.getImage("sent", imgSplit);
                                Intent intent = new Intent(ChatActivity.this, DownloadImage.class);
                                intent.putExtra("image", file.getAbsolutePath());
                                Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                            } else {
                                ProgressWheel progress = (ProgressWheel) v.findViewById(R.id.right_progress);
                                progress.setVisibility(View.VISIBLE);
                                progress.spin();
                                new DownloadAsyncTask(mContext, v, tempMap.get("image"), imgSplit, "sent").execute();
                            }
                        }
                    }
                });

                holder.leftImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                        } else {
                            View v = (View) view.getParent();
                            ImageStorage imageStorage = new ImageStorage(mContext);
                            String imgSplit = getImageName(tempMap.get("image"));
                            Log.v("imgSplit", "imgSplit=" + imgSplit);
                            if (imageStorage.checkifImageExists("thumb", imgSplit)) {
                                Log.v("Already Downloaded", "Already Downloaded in thumb");
                                File file = imageStorage.getImage("received", imgSplit);
                                Intent intent = new Intent(ChatActivity.this, DownloadImage.class);
                                intent.putExtra("image", file.getAbsolutePath());
                                Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                            } else {
                                ProgressWheel progress = (ProgressWheel) v.findViewById(R.id.left_progress);
                                progress.setVisibility(View.VISIBLE);
                                progress.spin();
                                new DownloadAsyncTask(mContext, v, tempMap.get("image"), imgSplit, "received").execute();
                            }
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        private class ViewHolder {
            RelativeLayout leftLay, rightLay, rightImageLay, leftImageLay;
            TextView leftMessage, rightMessage, date, missedText;
            ImageView leftCorner, rightCorner, leftDownloadIcon, rightDownloadIcon, leftImage, rightImage;
            ProgressWheel leftProgress, rightProgress;
            LinearLayout missedCallLay;
            //SimpleDraweeView leftImage, rightImage;
        }
    }

    private class DownloadAsyncTask extends AsyncTask<String, String, Bitmap> {

        String imageUrl = "", imgName = "", from = "";
        Context context;
        View view;

        private DownloadAsyncTask(Context context, View view, String imageloadingurl, String imgSplit, String from) {
            this.context = context;
            this.imageUrl = imageloadingurl;
            this.imgName = imgSplit;
            this.from = from;
            this.view = view;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //load image directly
            Bitmap image = downloadImage(imageUrl);

            if (image == null) {
                return null;
            } else {
                try {
                    final ImageStorage imageStorage = new ImageStorage(context);
                    String imageStatus = imageStorage.saveToSdCard(image, from, imgName);
                    if (imageStatus.equals("success")) {
                        Log.v("Downloaded", "Downloaded");
                        final File file = imageStorage.getImage(from, imgName);
                        final int imgSize = HowzuApplication.dpToPx(context, 150);
                        Log.v("file path", "file path=" + file.getAbsolutePath());
                        image = compressImage(file.getAbsolutePath(), imgSize, imgSize);
                    } else {
                        return null;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final ProgressWheel progress;
            final ImageView downloadIcon, image;
            if (from.equals("sent")) {
                progress = (ProgressWheel) view.findViewById(R.id.right_progress);
                downloadIcon = (ImageView) view.findViewById(R.id.right_download_icon);
                image = (ImageView) view.findViewById(R.id.right_image);
            } else {
                progress = (ProgressWheel) view.findViewById(R.id.left_progress);
                downloadIcon = (ImageView) view.findViewById(R.id.left_download_icon);
                image = (ImageView) view.findViewById(R.id.left_image);
            }

            if (bitmap == null) {
                Log.v("bitmapFailed", "bitmapFailed");
                progress.setVisibility(View.GONE);
                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            } else {
                Log.v("onBitmapLoaded", "onBitmapLoaded");
                try {
                    final ImageStorage imageStorage = new ImageStorage(context);
                    String status = imageStorage.saveThumbNail(bitmap, imgName);
                    int imgSize = HowzuApplication.dpToPx(context, 150);

                    if (status.equals("success")) {
                        File thumbFile = imageStorage.getImage("thumb", imgName);
                        if (thumbFile != null) {
                            Log.v("file", "file=" + thumbFile.getAbsolutePath());
                            Picasso.with(context).load(thumbFile).resize(imgSize, imgSize).centerCrop().into(image);
                            progress.setVisibility(View.GONE);
                            downloadIcon.setVisibility(View.GONE);

                            // Function for delete image from server
                            //	deleteImageFromServer(imgName);
                        }
                    } else {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        public Bitmap downloadImage(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    /**
     * for upload user image
     **/

    class uploadImage extends AsyncTask<String, String, String> {

        String exsistingFileName = "";

        @Override
        protected String doInBackground(String... imgpath) {
            String response = "";
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
                exsistingFileName = imgpath[0];
                Log.v(" exsistingFileName", exsistingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(exsistingFileName));
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
                dos.writeBytes("chat");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + exsistingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v("buffer", "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v("bytesRead", "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                response = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            if (!ChatActivity.this.isFinishing()) {
                pd.show();
            }
        }

        @Override
        protected void onPostExecute(String res) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            imageUploading = false;
            try {
                Log.v("json", "json" + res);

                JSONObject jsonobject = new JSONObject(res);
                String status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = DefensiveClass.optString(image, "Message");
                    String name = DefensiveClass.optString(image, "Name");
                    String viewUrl = DefensiveClass.optString(image, "View_url");

                    File dir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "Images/Sent");

                    if (dir.exists()) {
                        File from = new File(exsistingFileName);
                        File to = new File(dir + "/" + name);
                        if (from.exists()) {
                            from.renameTo(to);
                        }

                        ImageStorage imageStorage = new ImageStorage(ChatActivity.this);
                        File file = imageStorage.getImage("sent", name);

                        final int imgSize = HowzuApplication.dpToPx(ChatActivity.this, 150);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        //String imgstatus = imageStorage.saveThumbNail(bitmap, name);
                    }

                    callSocket("", viewUrl, "image");

                    HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("message", "");
                    hmap.put("sender", GetSet.getUserId());
                    hmap.put("date", String.valueOf(System.currentTimeMillis() / 1000L));
                    hmap.put("type", "image");
                    hmap.put("image", viewUrl);
                    chats.add(hmap);
                    chatAdapter.notifyDataSetChanged();

                    sendChat("image", name);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });

                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (editText.getText().toString().trim().length() > 0) {
                    if (data.get(Constants.TAG_BLOCK).equals("false")) {
                        meTyping = false;
                        callSocket(editText.getText().toString().trim(), "", "text");

                        String msg = editText.getText().toString().trim();

                        sendChat("text", msg);
                    }

                    HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put("message", editText.getText().toString().trim());
                    hmap.put("sender", GetSet.getUseridLikeToken());
                    hmap.put("date", String.valueOf(System.currentTimeMillis() / 1000L));
                    hmap.put("type", "text");
                    hmap.put("image", "");
                    chats.add(hmap);
                    chatAdapter.notifyDataSetChanged();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                    editText.setText("");

                } else {
                    editText.setError("Please enter your message");
                }
                break;
            case R.id.backbtn:
                getChat(0);
                disconnectSocket();
                if (from.equals("notification")) {
                    MainScreenActivity.resumeMessage = true;
                    finish();
                    Intent i = new Intent(ChatActivity.this, MainScreenActivity.class);
                    startActivity(i);
                } else if (from.equals("message")) {
                    ChatActivity.fromChat = true;
                }
                finish();
                break;
            case R.id.optionbtn:
                viewOptions(v);
                break;
            case R.id.attachbtn:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 102);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }

                break;
            case R.id.userImg:
                if (data.get(Constants.TAG_USER_STATUS).equals("1")) {
                    if (from.equalsIgnoreCase("profile")) {
                        finish();
                    } else {
                        Intent p = new Intent(ChatActivity.this, MainViewProfileDetailActivity.class);
                        p.putExtra("from", "chat");
                        p.putExtra("strFriendID", userId);
                        startActivity(p);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, userName + " " + getString(R.string.account_deactivated), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.audioCallBtn:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
                } else {
                    Intent video = new Intent(ChatActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "audio");
                    video.putExtra("data", data);
                    startActivity(video);
                }
                break;
            case R.id.videoCallBtn:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 101);
                } else {
                    Intent video = new Intent(ChatActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "video");
                    video.putExtra("data", data);
                    startActivity(video);
                }
                break;
        }
    }

    private void openPermissionDialog(String permissionList) {
        permissionDialog = new Dialog(ChatActivity.this);
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
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }
}
