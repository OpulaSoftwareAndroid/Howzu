package com.hitasoft.app.howzu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hitasoft.app.helper.AppRTCAudioManager;
import com.hitasoft.app.helper.NetworkReceiver;
import com.hitasoft.app.helper.OnCallEvents;
import com.hitasoft.app.helper.PeerConnectionClient;
import com.hitasoft.app.helper.PhoneStateReceiver;
import com.hitasoft.app.helper.SignallingClient;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CallActivity extends AppCompatActivity implements View.OnClickListener, SignallingClient.SignalingInterface, NetworkReceiver.ConnectivityReceiverListener, PeerConnectionClient.PeerConnectionEvents, OnCallEvents, PhoneStateReceiver.PhoneState {
    private static final String TAG = "CallActivity";
    public static boolean isInCall = false, callEnd = false, callEndByReceiver = false;
    public static String type = "", chatid = "", name = "", imgUrl = "", from = "", toastText = "", toID = "";
    static CallActivity callActivity;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    ImageView callImage, speakerCall, declineCall, muteCall, userImage, bgImg;
    TextView userName, callType, callTime, swipeUp, swipeDown;
    Display display;
    LinearLayout acceptLay;
    RelativeLayout parentLay, callView, declineBtn, declineLay;
    HashMap<String, String> data = new HashMap<String, String>();
    FrameLayout frameLayout;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    List<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
    SurfaceViewRenderer localVideoView;
    SurfaceViewRenderer remoteVideoView;
    Vibrator vibrator;
    Ringtone ringtone;
    CountDownTimer countDownTimer;
    LottieAnimationView arrow;
    Timer callTimer = new Timer();
    Animation downAnimation, upAnimation, fadeInAnimation;
    ToneGenerator toneGenerator;
    PowerManager.WakeLock wl, wl_cpu;
    private PeerConnectionClient peerConnectionClient;
    private AppRTCAudioManager audioManager;
    public View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    if (event.getY() < 0) {
                        Log.d(TAG, "onTouch: " + event);

                        callImage.startAnimation(upAnimation);
                        upAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                swipeUp.startAnimation(fadeInAnimation);
                                swipeDown.startAnimation(fadeInAnimation);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                acceptLay.setVisibility(View.GONE);
                                declineLay.setVisibility(View.VISIBLE);
                                callType.setVisibility(View.GONE);
                                if (ringtone != null) {
                                    ringtone.stop();
                                    ringtone = null;
                                }
                                if (vibrator != null) {
                                    vibrator.cancel();
                                    vibrator = null;
                                }
                                isCallAttend = true;
                                onCallAccept();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                    } else {
                        callImage.startAnimation(downAnimation);
                        downAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                swipeUp.startAnimation(fadeInAnimation);
                                swipeDown.startAnimation(fadeInAnimation);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (ringtone != null) {
                                    ringtone.stop();
                                    ringtone = null;
                                }
                                if (vibrator != null) {
                                    vibrator.cancel();
                                    vibrator = null;
                                }
                                missedCallAlert(type);
//                                toastText = "Call declined";
//                                showToast(toastText);
                                disconnect();
                                isCallConnected = false;
                                /*If Receiver declined the call*/
                                callEndByReceiver = true;
                                callEnd = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                    break;
            }
            return true;
        }
    };
    private EglBase rootEglBase;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean enableAudio = true, enableVideo = true, isCallAttend = false, isCallConnected = false, isMute = false, speaker = false;
    Dialog permissionDialog;

    public static CallActivity getInstance() {
        return callActivity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();
        isInCall = true;
        callActivity = this;
        HowzuApplication.getInstance().setConnectivityListener(this);
        HowzuApplication.getInstance().setPhoneStateListener(this);

        type = getIntent().getExtras().getString("type");
        from = getIntent().getExtras().getString("from");

        Log.d(TAG,"jigar the call activity type have "+type);
        Log.d(TAG,"jigar the call activity from have "+from);

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(CallActivity.this, new String[]{CAMERA, RECORD_AUDIO, READ_PHONE_STATE, WAKE_LOCK}, 101);
        } else {
            initView();
            initFunctions();
        }
    }

    private void initFunctions() {
        initRender();
    }

    private void initView() {
        parentLay = findViewById(R.id.parentLay);
        callView = findViewById(R.id.callView);
        speakerCall = findViewById(R.id.speakerCall);
        declineCall = findViewById(R.id.declineCall);
        muteCall = findViewById(R.id.muteCall);
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        callType = findViewById(R.id.callType);
        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        frameLayout = findViewById(R.id.videoCallView);
        acceptLay = findViewById(R.id.acceptLay);
        declineLay = findViewById(R.id.declineLay);
        callImage = findViewById(R.id.callImage);
        callTime = findViewById(R.id.callTime);
        arrow = findViewById(R.id.arrow);
        declineBtn = findViewById(R.id.declineBtn);
        bgImg = findViewById(R.id.bgImg);
        swipeUp = findViewById(R.id.swipeUp);
        swipeDown = findViewById(R.id.swipeDown);

        muteCall.setOnClickListener(this);
        speakerCall.setOnClickListener(this);
        declineCall.setOnClickListener(this);

        upAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.slide_up);
        downAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.slide_down);
        fadeInAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.fade_in);


        SignallingClient.getInstance().init(this);

        if (from.equals("receive")) {
            try {
                chatid = getIntent().getExtras().getString(Constants.TAG_CHAT_ID);
                userName.setText(getIntent().getExtras().getString(Constants.TAG_USERNAME));
                imgUrl = getIntent().getExtras().getString(Constants.TAG_IMAGE_URL);
                toID = getIntent().getExtras().getString(Constants.TAG_USERID);
            } catch (Exception e) {
                Log.d(TAG,"jigar the exception main call activity type have "+e);

                e.printStackTrace();
            }

            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            Log.e("jigar screen on", "screen on" + isScreenOn);
            if (!isScreenOn) {
                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
                wl.acquire(35 * 1000L /*10 minutes*/);

                wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
                wl_cpu.acquire(35 * 1000L /*10 minutes*/);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }

            acceptLay.setVisibility(View.VISIBLE);
            declineLay.setVisibility(View.GONE);
            if (checkPermissions()) {
                callImage.setOnTouchListener(touchListener);
            } else {
                requestPermission(new String[]{CAMERA, RECORD_AUDIO, READ_PHONE_STATE, WAKE_LOCK}, 101);
            }

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            switch (audio.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    ringtone.play();
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
                    vibrator.vibrate(pattern, 0);
                    break;
            }
        } else {
            data = (HashMap<String, String>) getIntent().getExtras().get("data");
            chatid = data.get(Constants.TAG_CHAT_ID);
            userName.setText(data.get(Constants.TAG_USERNAME));
            imgUrl = data.get(Constants.TAG_USERIMAGE);

            arrow.setVisibility(View.VISIBLE);
            Log.d(TAG,"jigar the main call data activity type have "+data.toString());


            toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 30000);
        }

        if (type.equals("audio")) {
            enableVideo = false;
            frameLayout.setVisibility(View.GONE);
            if (from.equals("receive")) {
                callImage.setImageResource(R.drawable.call_atten);
            }

            Picasso.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .into(bgImg);

            Log.d(TAG, "jigar getsetImgURl: " + GetSet.getImageUrl());
            Picasso.with(this)
                    .load(GetSet.getImageUrl())
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                    .centerCrop()
                    .into(userImage);
            callType.setText(R.string.audio_calling);
            speakerCall.setImageResource(R.drawable.speaker);
        } else if (type.equals("video")) {
            Log.v(TAG, "type=" + type);
            if (frameLayout.getVisibility() == View.GONE) {
                frameLayout.setVisibility(View.VISIBLE);
            }

            if (from.equals("receive")) {
                callImage.setImageResource(R.drawable.video_call);
            }
            callType.setText(R.string.video_calling);
            userImage.setVisibility(View.GONE);
            speakerCall.setImageResource(R.drawable.change_camera);
        }

    }

    private void openPermissionDialog(String permissionList) {
        permissionDialog = new Dialog(CallActivity.this);
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
                disconnect();
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
                callEndByReceiver = true;
//                toastText = "Call ended";
//                showToast(toastText);
                disconnect();
            }
        });
        permissionDialog.show();
    }

    private void initRender() {
        // Create video renderers.
        callImage.setOnTouchListener(touchListener);
        rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localVideoView.setZOrderMediaOverlay(true);
        localVideoView.setEnableHardwareScaler(true);
        remoteVideoView.setEnableHardwareScaler(false);
        localVideoView.setMirror(true);
        remoteVideoView.setMirror(false);
        updateVideoViews(false);


        iceServers.add(new PeerConnection.IceServer(Constants.STUN_SERVER));

        audioManager = AppRTCAudioManager.create(CallActivity.this);
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });

        SignallingClient.getInstance().createOrJoin(chatid);

        if (type.equals("audio")) {
            // If capturing format is not specified for screencapture, use screen resolution.
            peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters.createAudioDefault();

            peerConnectionClient = PeerConnectionClient.getInstance();
            peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

            peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), null,
                    null, null, iceServers);
            if (!from.equals("receive")) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        } else if (type.equals("video")) {
            remoteRenderers.add(remoteVideoView);

            // If capturing format is not specified for screencapture, use screen resolution.
            peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters.createVideoDefault();

            peerConnectionClient = PeerConnectionClient.getInstance();
            peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

            VideoCapturer videoCapturer = null;
            if (peerConnectionParameters.videoCallEnabled) {
                videoCapturer = createVideoCapturer();
            }
            peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localVideoView,
                    remoteRenderers, videoCapturer, iceServers);
        }
    }

    private void onCallAccept() {
        if (isCallConnected && isCallAttend) {

            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            Log.v(TAG, "onCallAccept");
            peerConnectionClient.createAnswer();
            callTime.setVisibility(View.VISIBLE);
            startCountDown("answer");
            if (type.equals("video")) {
                updateVideoViews(true);
            }
        }
    }

    private void callApi(String from) {


        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CALLING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "jigar the calling api response we have " + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.v(TAG, "jigar the json error  in calling api response we have " + e);

                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Log.v(TAG, "jigar the null pointer error  in calling api response we have " + e);

                            e.printStackTrace();
                        } catch (Exception e) {
                            Log.v(TAG, "jigar the main exception in calling api response we have " + e);

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.v(TAG, "jigar the error in calling api response we have " + error);

            }

        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();

                map.put(Constants.TAG_FROM_ID, GetSet.getUseridLikeToken());
                map.put(Constants.TAG_TO_ID, data.get(Constants.TAG_USERID));
                map.put(Constants.TAG_CHATID, data.get(Constants.TAG_CHAT_ID));
                map.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
                if (from.equals("audio")) {
                    map.put(Constants.TAG_TYPE, from);
                } else {
                    map.put(Constants.TAG_TYPE, from);
                }
                Log.v(TAG, "jigar the params in calling api response we have " + map);

                Log.v(TAG, "sendChatParams" + map);
                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(CallActivity.this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Disconnect strVisitingIdLikeToken remote resources, dispose of local resources, and exit.
    public void disconnect() {
        isInCall = false;
        SignallingClient.getInstance().close(chatid);
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (localVideoView != null) {
            localVideoView.release();
            localVideoView = null;
        }
        if (remoteVideoView != null) {
            remoteVideoView.release();
            remoteVideoView = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HowzuApplication.activityResumed();
        if (!NetworkReceiver.isConnected()) {
            toastText = "Poor internet connection";
            showToast(toastText);
            disconnect();
        }
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            Log.d(TAG, "Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private boolean captureToTexture() {
        return true;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }


    // This method is called when the audio manager reports audio device change,
    // e.g. strVisitingIdLikeToken wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.v(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    /**
     * This method will be called directly by the app when it is the initiator and has got the local media
     * or when the remote peer sends a message through socket that it is ready to transmit AV data
     */
    @Override
    public void onTryToStart() {
        Log.v(TAG, "onTryToStart");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!SignallingClient.getInstance().isStarted && SignallingClient.getInstance().isChannelReady) {
                    SignallingClient.getInstance().isStarted = true;

                    if (SignallingClient.getInstance().isInitiator) {
                        startCountDown("waiting");
                        peerConnectionClient.createOffer();
                    }
                }
            }
        });
    }

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        Log.v(TAG, "onIceCandidateReceived" + iceCandidate);
        SignallingClient.getInstance().emitIceCandidate(iceCandidate, chatid);
    }

    /**
     * SignallingCallback - called when the room is created - i.e. you are the initiator
     */
    @Override
    public void onCreatedRoom() {
        Log.v(TAG, "onCreatedRoom");
        callApi(type);
        SignallingClient.getInstance().emitMessage("got user media", chatid);
    }

    /**
     * SignallingCallback - called when you join the room - you are a participant
     */
    @Override
    public void onJoinedRoom() {
        Log.v(TAG, "onJoinedRoom");
        SignallingClient.getInstance().emitMessage("got user media", chatid);
    }

    @Override
    public void onNewPeerJoined() {
        Log.v(TAG, "onNewPeerJoined");
    }

    @Override
    public void onRemoteHangUp(String msg) {
        Log.v(TAG, "onRemoteHangUp");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (from.equals("receive")) {
                    callEnd = true;
                    callEndByReceiver = false;
                } else if (from.equals("send")) {
                    callEndByReceiver = true;
                    callEnd = false;
                }
                disconnect();
            }
        });
    }

    /**
     * SignallingCallback - Called when remote peer sends offer
     */
    @Override
    public void onOfferReceived(final JSONObject data) {
        Log.v(TAG, "onOfferReceived");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                    onTryToStart();
                }

                try {
                    String sdpDescription = data.getString("sdp");
                    Log.v("sdp", "sdp=" + sdpDescription);
                    peerConnectionClient.setRemoteDescription(new SessionDescription(SessionDescription.Type.OFFER, sdpDescription));
                    isCallConnected = true;
                    onCallAccept();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startCountDown(String callType) {
        if (callType.equals("answer")) {
            Log.v(TAG, "startCountDown=" + callType);
            long startTime = SystemClock.elapsedRealtime();
            callTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    //Function call every second
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(TAG, "onChronometerTick");
                            long hours = (((SystemClock.elapsedRealtime() - startTime) / 1000) / 60) / 60;
                            long minutes = ((SystemClock.elapsedRealtime() - startTime) / 1000) / 60;
                            long seconds = ((SystemClock.elapsedRealtime() - startTime) / 1000) % 60;
                            callTime.setText(twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds));
                        }
                    });
                }
            }, 0, 1000);
        } else {
            long time;
            if (callType.equals("waiting")) {
                time = 30000;
            } else {
                time = 15000;
            }
            countDownTimer = new CountDownTimer(time, 1000) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long millisUntilFinished) {
                    if (!NetworkReceiver.isConnected()) {
                        toastText = "Poor internet connection";
                        showToast(toastText);
                        disconnect();
                    }
                }

                @Override
                public void onFinish() {
                    Log.v(TAG, "countDownTimer=Ended");
                    if (isCallConnected && isCallAttend) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            Log.v(TAG, "countDownTimer=cancel");
                        }
                    } else {
                        missedCallAlert(type);
                        callEnd = false;
                        callEndByReceiver = false;
//                        toastText = "Call declined";
//                        showToast(toastText);
                        disconnect();
                    }
                }
            };

            countDownTimer.start();
        }
    }

    private String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        } else if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    /**
     * SignallingCallback - Called when remote peer sends answer to your offer
     */

    @Override
    public void onAnswerReceived(JSONObject data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callType.setVisibility(View.GONE);
                arrow.setVisibility(View.GONE
                );
                declineCall.setOnClickListener(CallActivity.this);
                callTime.setVisibility(View.VISIBLE);
                startCountDown("answer");

            }
        });

        Log.v(TAG, "onAnswerReceived" + data);
        try {
            isCallConnected = true;
            isCallAttend = true;
            if (toneGenerator != null) {
                toneGenerator.stopTone();
                toneGenerator = null;
            }
            toastText = "Call ended";
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            peerConnectionClient.setRemoteDescription(new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
            updateVideoViews(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote IceCandidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        try {
            Log.d(TAG, "onIceCandidateReceived");
            peerConnectionClient.addRemoteIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateVideoViews(final boolean remoteVisible) {
        Log.v(TAG, "updateVideoViews=" + remoteVisible);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) localVideoView.getLayoutParams();

                if (remoteVisible) {
                    remoteVideoView.setVisibility(View.VISIBLE);
                    params.height = HowzuApplication.dpToPx(CallActivity.this, 90);
                    params.width = HowzuApplication.dpToPx(CallActivity.this, 90);
                    params.setMargins(0, 0, HowzuApplication.dpToPx(CallActivity.this, 20), HowzuApplication.dpToPx(CallActivity.this, 140));
                    localVideoView.setLayoutParams(params);
                }
            }

        });

    }

    private void missedCallAlert(String type) {
        /*Constants.API_IOS_MISSED_ALERT It used only to show missed call alert in I-OS device*/
        StringRequest request = new StringRequest(Request.Method.POST, Constants.API_IOS_MISSED_ALERT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String request) {
                        Log.v(TAG, "sendChatRes" + request);
                        try {
                            JSONObject json = new JSONObject(request);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
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

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();

                map.put(Constants.TAG_FROM_ID, GetSet.getUseridLikeToken());
                if (type.equals("receive")) {
                    map.put(Constants.TAG_TO_ID, toID);
                } else {
                    map.put(Constants.TAG_TO_ID, data.get(Constants.TAG_USERID));
                }
                map.put(Constants.TAG_TYPE, "bye");
                Log.v(TAG, "sendChatParams" + map);

                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(request, TAG);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MISSED_CALL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "sendChatRes" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
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

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                Log.i(TAG, "getHeaders: " + map);
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_FROM_ID, GetSet.getUseridLikeToken());
                if (from.equals("receive")) {
                    map.put(Constants.TAG_TO_ID, toID);
                } else {
                    map.put(Constants.TAG_TO_ID, data.get(Constants.TAG_USERID));
                }
                map.put(Constants.TAG_CHATID, chatid);
                map.put(Constants.TAG_CHATTIME, String.valueOf(System.currentTimeMillis() / 1000L));
                map.put(Constants.TAG_TYPE, type);
                Log.v(TAG, "sendChatParams" + map);

                return map;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(req, TAG);

    }

    @Override
    protected void onPause() {
        super.onPause();
        HowzuApplication.activityPaused();
        Log.v("Close", "Pause");
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Close", "Destroy");
        if (isCallAttend && NetworkReceiver.isConnected()) {
            toastText = "Call ended";
            showToast(toastText);
            disconnect();
        } else if (!isCallConnected && !callEndByReceiver && !callEnd) {
            if (from.equals("receive") || from.equals("send")) {
                toastText = "Call ended";
            } else {
                toastText = "Call declined";
            }
            showToast(toastText);
        } else {
            if (from.equals("send") && callEndByReceiver) {
                callEndByReceiver = false;
                toastText = "Call declined";
            } else if (from.equals("receive") && callEndByReceiver) {
                callEndByReceiver = false;
                toastText = "Call ended";
            } else if (from.equals("send") && callEnd) {
                callEnd = false;
                toastText = "Call ended";
            } else if (from.equals("receive") && callEnd) {
                callEnd = false;
                toastText = "Call declined";
            } else {
                toastText = "Call ended";
            }
            showToast(toastText);
            disconnect();
        }
        from = "";
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("Close", "Stop");
    }


    /**
     * Util Methods
     */


    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.v(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.v(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.v(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.v(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
//            int permissionCamera = ContextCompat.checkSelfPermission(CallActivity.this,
//                    CAMERA);
//            int permissionAudio = ContextCompat.checkSelfPermission(CallActivity.this,
//                    RECORD_AUDIO);
//            int permissionPhoneState = ContextCompat.checkSelfPermission(CallActivity.this,
//                    READ_PHONE_STATE);
//            int permissionWakeLock = ContextCompat.checkSelfPermission(CallActivity.this,
//                    READ_PHONE_STATE);

            boolean isPermissionEnabled = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = false;
                    break;
                } else {
                    isPermissionEnabled = true;
                }
            }

            if (isPermissionEnabled) {
                if (localVideoView != null) {
                    localVideoView.release();
                }

                if (remoteVideoView != null) {
                    remoteVideoView.release();
                }

                initView();
                initFunctions();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WAKE_LOCK) &&
                            shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 101);
                    } else {
                        if (permissionDialog == null || !permissionDialog.isShowing())
                            openPermissionDialog("Camera, Record Audio, Read phone state and Write External Storage");
                    }
                }
//                ActivityCompat.requestPermissions(CallActivity.this, new String[]{CAMERA, RECORD_AUDIO, READ_PHONE_STATE, WAKE_LOCK}, 101);
            }
        }
    }

    private boolean checkPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(CallActivity.this,
                CAMERA);
        int permissionAudio = ContextCompat.checkSelfPermission(CallActivity.this,
                RECORD_AUDIO);
        int permissionPhoneState = ContextCompat.checkSelfPermission(CallActivity.this,
                READ_PHONE_STATE);
//        int permissionStorage = ContextCompat.checkSelfPermission(CallActivity.this,
//                WRITE_EXTERNAL_STORAGE);
        int permissionWakeLock = ContextCompat.checkSelfPermission(CallActivity.this,
                WAKE_LOCK);
        return permissionCamera == PackageManager.PERMISSION_GRANTED &&
                permissionAudio == PackageManager.PERMISSION_GRANTED &&
//                permissionStorage == PackageManager.PERMISSION_GRANTED &&
                permissionWakeLock == PackageManager.PERMISSION_GRANTED &&
                permissionPhoneState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(CallActivity.this, permissions, requestCode);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            toastText = "Poor internet connection";
            showToast(toastText);
            disconnect();
        }
    }


    @Override
    public void onLocalDescription(SessionDescription sdp) {
        Log.v(TAG, "onLocalDescription");
        runOnUiThread(() -> {
            if (SignallingClient.getInstance().isInitiator) {
                Log.d("onCreateSuccess", "SignallingClient emit ");
                SignallingClient.getInstance().emitMessage(sdp, chatid);
            } else {
                SignallingClient.getInstance().emitMessage(sdp, chatid);
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        onIceCandidateReceived(candidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onIceConnected() {
        Log.v(TAG, "onIceConnected");
    }

    @Override
    public void onIceDisconnected() {
        Log.v(TAG, "onIceDisconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!NetworkReceiver.isConnected()) {
                    toastText = "Poor internet connection";
                } else if (!isCallConnected) {
                    toastText = "Call Cancelled";
                }
                showToast(toastText);
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    @Override
    public void onCallHangUp() {

    }

    @Override
    public void onCameraSwitch() {

    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {

    }

    @Override
    public boolean onToggleMic() {
        return false;
    }

    @Override
    public void onIncomingCall() {
        Log.v(TAG, "onIncomingCall");
        showToast("Call ended");
        disconnect();
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.declineCall:
                if (!isCallConnected) {
                    callEnd = true;
                    callEndByReceiver = false;
                    missedCallAlert(type);
                } else {
                    toastText = "Call ended";
                }
                disconnect();
                break;
            case R.id.muteCall:
                if (enableAudio) {
                    enableAudio = false;
                    peerConnectionClient.setAudioEnabled(false);
                    muteCall.setBackground(getResources().getDrawable(R.drawable.white_round_solid));
                    muteCall.setImageResource(R.drawable.mute_gray);
                } else {
                    enableAudio = true;
                    peerConnectionClient.setAudioEnabled(true);
                    muteCall.setBackground(getResources().getDrawable(R.drawable.white_round_opacity));
                    muteCall.setImageResource(R.drawable.mute);
                }
                break;

            case R.id.speakerCall:
                if (type.equals("audio")) {
                    if (!speaker) {
                        speaker = true;
                        audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        speakerCall.setBackground(getResources().getDrawable(R.drawable.white_round_solid));
                        speakerCall.setImageResource(R.drawable.speaker_gray);
                    } else {
                        speaker = false;
                        audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                        speakerCall.setBackground(getResources().getDrawable(R.drawable.white_round_opacity));
                        speakerCall.setImageResource(R.drawable.speaker);
                    }
                } else {
                    peerConnectionClient.switchCamera();
                }
                break;

        }
    }

}
