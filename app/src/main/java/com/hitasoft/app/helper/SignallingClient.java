package com.hitasoft.app.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import com.hitasoft.app.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Webrtc_Step3
 * Created by vivek-3102 on 11/03/17.
 */

public class SignallingClient {
    private static SignallingClient instance;
    private Socket socket;
    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;
    private SignalingInterface callback;
    private static final String TAG = "CallActSignalling";

    //This piece of code should not go into production!!
    //This will help in cases where the node server is running in non-https server and you want to ignore the warnings
    @SuppressLint("TrustAllX509TrustManager")
    private final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) {
        }
    }};

    public static SignallingClient getInstance() {
        if (instance == null) {
            instance = new SignallingClient();
        }
        return instance;
    }

    public void init(SignalingInterface signalingInterface) {
        this.callback = signalingInterface;
        try {
            /*SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, trustAllCerts, null);
            IO.setDefaultHostnameVerifier((hostname, session) -> true);
            IO.setDefaultSSLContext(sslcontext);*/
            //set the socket.io url here
            socket = IO.socket(Constants.SOCKET_URL);
            socket.connect();
            Log.v("Socket", "Connected!!!");

            //room created event.
            socket.on("created", args -> {
                Log.v(TAG, "created call() called with: args = [" + Arrays.toString(args) + "]");
                isInitiator = true;
                callback.onCreatedRoom();
            });

            //room is full event
            socket.on("full", args -> Log.d("SignallingClient", "full call() called with: args = [" + Arrays.toString(args) + "]"));

            //peer joined event
            socket.on("join", args -> {
                Log.v(TAG, "jigar the join call() called with: args = [" + Arrays.toString(args) + "]");
                isChannelReady = true;
                callback.onNewPeerJoined();
            });

            //when you joined a chat room successfully
            socket.on("joined", args -> {
                Log.v(TAG, "jigar the joined call() called with: args = [" + Arrays.toString(args) + "]");
                isChannelReady = true;
                callback.onJoinedRoom();
            });

            //log event
            socket.on("log", args -> Log.d("SignallingClient", "log call() called with: args = [" + Arrays.toString(args) + "]"));

            //bye event
            socket.on("bye", args -> callback.onRemoteHangUp(String.valueOf(args[0]) ));

            //messages - SDP and ICE candidates are transferred through this
            socket.on("rtcmessage", args -> {
                Log.v(TAG, "jigar the message call() called with: args = [" + Arrays.toString(args) + "]");
                if (args[0] instanceof String) {
                    Log.v(TAG, "jigar the  String received :: " + args[0]);
                    String data = (String) args[0];
                    if (data.equalsIgnoreCase("got user media")) {
                        callback.onTryToStart();
                    }
                    if (data.equalsIgnoreCase("bye")) {
                        callback.onRemoteHangUp(data);
                    }
                } else if (args[0] instanceof JSONObject) {
                    try {

                        JSONObject data = (JSONObject) args[0];
                        Log.v(TAG, "jigar the Json Received :: " + data.toString());
                        String type = data.optString("type", "got user media");
                        if (type.equalsIgnoreCase("offer") && !isInitiator) {
                            callback.onOfferReceived(data);
                        } else if (type.equalsIgnoreCase("answer") && isStarted && isInitiator) {
                            callback.onAnswerReceived(data);
                        } else if (type.equalsIgnoreCase("candidate") && isStarted) {
                            callback.onIceCandidateReceived(data);
                        } else if (type.equalsIgnoreCase("got user media")){
                            callback.onTryToStart();
                        }

                    } catch (Exception e) {
                        Log.d(TAG,"jigar the main exception in getting signal is "+e);
                        e.printStackTrace();
                    }
                }
            });
        } catch (URISyntaxException e) {
            Log.d(TAG,"jigar the main uri syntax in getting signal is "+e);

            e.printStackTrace();
        }
    }

    public void createOrJoin(String message) {
        Log.v(TAG, "jigar the emitInitStatement() called with: event = [" + "create or join" + "], message = [" + message + "]");
        socket.emit("create or join", message);
    }

    public void emitMessage(String message, String roomID) {
        try {
            Log.v(TAG, "jigar the emitMessage() called with: message = [" + message + "]");
            JSONObject obj = new JSONObject();
            obj.put("room", roomID);
            obj.put("message", message);
            Log.v(TAG, obj.toString());
            socket.emit("rtcmessage", obj);
        } catch (JSONException e){
            Log.d(TAG,"jigar the emit json exception in getting signal is "+e);

            e.printStackTrace();
        }
    }

    public void emitMessage(SessionDescription message, String roomID) {
        try {
            Log.v(TAG, "jigar the emitMessage() called with: message = [" + message + "]");
            JSONObject msg = new JSONObject();
            msg.put("type", message.type.canonicalForm());
            msg.put("sdp", message.description);
            JSONObject obj = new JSONObject();
            obj.put("room", roomID);
            obj.put("message", msg);
            Log.v(TAG, obj.toString());
            socket.emit("rtcmessage", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void emitIceCandidate(IceCandidate iceCandidate, String roomID) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "candidate");
            msg.put("label", iceCandidate.sdpMLineIndex);
            msg.put("id", iceCandidate.sdpMid);
            msg.put("candidate", iceCandidate.sdp);
            JSONObject object = new JSONObject();
            object.put("room", roomID);
            object.put("message", msg);
            socket.emit("rtcmessage", object);
            Log.v(TAG, object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public void close(String roomID) {
        if (socket != null){
            socket.emit("bye", roomID);
            socket.disconnect();
            socket = null;
        }
        isInitiator = false;
        isChannelReady = false;
        isStarted = false;
    }


    public interface SignalingInterface {
        void onRemoteHangUp(String msg);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);

        void onTryToStart();

        void onCreatedRoom();

        void onJoinedRoom();

        void onNewPeerJoined();
    }
}
