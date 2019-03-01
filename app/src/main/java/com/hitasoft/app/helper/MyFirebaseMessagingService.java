package com.hitasoft.app.helper;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hitasoft.app.howzu.CallActivity;
import com.hitasoft.app.howzu.ChatActivity;
import com.hitasoft.app.howzu.MainScreenActivity;
import com.hitasoft.app.howzu.MainViewProfileDetailActivity;
import com.hitasoft.app.howzu.R;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Hitasoft on 03/11/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    public static String chatUserID = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Data Payload: ");
    }

    @Override
    public void handleIntent(Intent intent) {
        Log.d("jigar the FCM", "handleIntent =" + intent.getExtras());
        Log.d("jigar the FCM data ", "Data" + intent.getExtras().getString("data"));
        try {
            long sendTime = (long) intent.getExtras().get("google.sent_time");
            //  sendTime = sendTime / 1000L;
            JSONObject json = new JSONObject(intent.getExtras().getString("data"));
            sendPushNotification(json, sendTime);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject data, long sendTime) {
        //optionally we can display the json into log
        Log.e(TAG, "jigar the Notification JSON " + data.toString());
        try {
            //parsing json data
            String title = DefensiveClass.optString(data, "title");
            JSONObject msg = data.optJSONObject("message");

            String userId = DefensiveClass.optInt(msg, Constants.TAG_USERID);
            String type = DefensiveClass.optString(msg, Constants.TAG_TYPE);
            String message = DefensiveClass.optString(msg, Constants.TAG_MESSAGE);
            String chatId = DefensiveClass.optString(msg, Constants.TAG_CHAT_ID);
            String userImage = DefensiveClass.optString(msg, Constants.TAG_USERIMAGE);
            String userName = DefensiveClass.optString(msg, Constants.TAG_USERNAME);

            String img = Constants.RESIZE_URL + CommonFunctions.getImageName(userImage) + Constants.IMAGE_RES;

            //get the logined user details from preference
            Constants.pref = getApplicationContext().getSharedPreferences("ChatPref",
                    MODE_PRIVATE);
            Constants.editor = Constants.pref.edit();

            if (Constants.pref.getBoolean("isLogged", false)) {
                GetSet.setLogged(true);
                GetSet.setUserId(Constants.pref.getString("user_id", null));

                if (Constants.pref.getBoolean("like_notification", false)) {
                    GetSet.setLikeNotification(true);
                } else {
                    GetSet.setLikeNotification(false);
                }
                if (Constants.pref.getBoolean("message_notification", false)) {
                    GetSet.setMsgNotification(true);
                } else {
                    GetSet.setMsgNotification(false);
                }

                if (type.equals("audio") || type.equals("video")) {
                    long diffInMs = System.currentTimeMillis() - sendTime;
                    long diffSeconds = diffInMs / 1000;

                    TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    int isPhoneCallOn = telephony.getCallState();

                    if (diffSeconds < 30 && !CallActivity.isInCall && isPhoneCallOn == 0) {
                        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Constants.TAG_TYPE, type);
                        intent.putExtra("from", "receive");
                        intent.putExtra(Constants.TAG_CHAT_ID, chatId);
                        intent.putExtra(Constants.TAG_USERNAME, userName);
                        intent.putExtra(Constants.TAG_IMAGE_URL, userImage);
                        intent.putExtra(Constants.TAG_USERID, userId);
                        startActivity(intent);
                    }
                }

                if (!chatUserID.equals(userId) && !type.equals("audio") && !type.equals("video") && !type.equals("bye")) {
                    showSmallNotification(userId, type, chatId, message, img, userName);
                }

                if (CallActivity.isInCall && type.equals("bye")) {
                    CallActivity.callEnd = true;
                    CallActivity.getInstance().finish();
                }
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    public void showSmallNotification(String userId, String type, String chatId, String message, String img, String userName) {
        Intent intent = null;
        String title = getString(R.string.app_name);
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        if (type.equals("friend_request")) {
            intent = new Intent(getApplicationContext(), MainViewProfileDetailActivity.class);
            intent.putExtra("from", "notification");
            intent.putExtra("userId", userId);
        } else if (type.equals("chat_message")) {
            if (userId.equals("")) {
                userId = "0";
            }
            if (message.equals("")) {
                message = "Sent a photo";
            }
            title = userName + " sent message";
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_CHAT_ID, chatId);
            map.put(Constants.TAG_USERIMAGE, img);
            map.put(Constants.TAG_USERNAME, userName);
            map.put(Constants.TAG_USERID, userId);
            map.put(Constants.TAG_BLOCKED_BY_ME, "false");
            map.put(Constants.TAG_BLOCK, "false");
            map.put(Constants.TAG_USER_STATUS, "1");
            map.put(Constants.TAG_ONLINE, "1");
            map.put(Constants.TAG_LAST_ONLINE, "0");

            intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("data", map);
            intent.putExtra("position", 0);
            intent.putExtra("from", "notification");
        } else if (type.equals("premium_purchase")) {
            intent = new Intent(getApplicationContext(), MainScreenActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), MainScreenActivity.class);
        }

        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "my channel";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;


        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        Notification notification;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notification = mBuilder.setContentIntent(resultPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle(title)
                    .setSmallIcon(R.mipmap.notifyicon)
                    .setLargeIcon(getBitmapFromURL(img))
                    .setContentText(message)
                    .setChannelId(CHANNEL_ID)
                    .build();


            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        } else {


            notification = mBuilder.setSmallIcon(R.mipmap.notifyicon).setTicker(title).setWhen(when)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle(title)
                    .setSmallIcon(R.mipmap.notifyicon)
                    .setLargeIcon(getBitmapFromURL(img))
                    .setContentText(message)
                    .build();
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(userId,m, notification);

    }

    //The method will return Bitmap from an image URL
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(getResources(), R.mipmap.appicon);
        }
    }

}