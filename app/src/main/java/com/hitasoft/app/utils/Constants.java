package com.hitasoft.app.utils;

import android.content.SharedPreferences;

import java.util.regex.Pattern;

/**
 * Created by hitasoft on 26/5/15.
 */
public class Constants {

    /**
     * FaceBook App id
     **/
    public static final String App_ID = "718268961615879";
    /**
     * Api
     **/
//    public static final String URL = "http://howzuapp.com/";
    public static final String URL = "http://www.ilovemisskey.com/";

//    public static final String URL = "http://howzuapp.com/beta/";
//    public static final String URL = "http://192.168.1.29/howzu/";
    /**
     * Socket Url
     **/
    public static final String SOCKET_URL = "http://howzuapp.com:8085";

    /**
     * Stun Server
     * */
    public static final String STUN_SERVER = "stun:139.59.77.194:3478";


    /**
     * Api function names
     **/

    public static final String API_GET_PEOPLE = URL + "api/" + "getpeople";
    public static final String API_GET_PROFILE = URL + "api/" + "getprofile";
    public static final String API_GET_PROFILE_COMMENTS = URL + "api/" + "girlprofilecommentlist";
    public static final String API_GET_USER_NOTIFICATION = URL + "api/" + "notification";

    public static final String API_MATCH = URL + "api/" + "match";
    public static final String API_NEW_MATCH_AND_FRIEND_REQUEST = URL + "api/" + "sentfriendrequest";
    public static final String API_NEW_ACCEPT_FRIEND_REQUEST = URL + "api/" + "friendrequeststatus";
    public static final String API_NEW_ACCEPT_DECLINE_VIDEO_CHAT_REQUEST = URL + "api/" + "videochatrequeststatus";
    public static final String API_NEW_ACCEPT_DECLINE_INVITATION_REQUEST = URL + "api/" + "matchmakerrequeststatus";

    public static final String API_UNMATCH = URL + "api/" + "unmatch";
    public static final String API_NEW_UNMATCH = URL + "api/" + "dislikeprofile";

    public static final String API_SEND_MESSAGE = URL + "api/" + "sendmessage";
    public static final String API_ADMIN_DATAS = URL + "api/" + "admindatas";
    public static final String API_PEOPLE_INTEREST_FOR = URL + "api/" + "peoplefor";

    public static final String API_COUNTRY_CODE = URL + "api/" + "countrylist";
    public static final String API_OTP_VERIFICATION = URL + "api/" + "signupmobileverify";
    public static final String API_USER_SIGNUP= URL + "api/" + "signup";
    public static final String API_PROFILE = URL + "api/" + "profile";
    public static final String API_NEW_VIEW_PROFILE_DETAIL = URL + "api/" + "girlprofiledetail";
    public static final String API_SET_FILTER = URL + "api/" + "setfilter";
    public static final String API_SET_PROFILE = URL + "api/" + "setprofile";
    public static final String API_DELETE_ACCOUNT = URL + "api/" + "deleteaccount";
    public static final String API_PAY_PREMIUM = URL + "api/" + "paypremium";
    public static final String API_VISITORS = URL + "api/" + "visitors";
    public static final String API_VISIT_PROFILE = URL + "api/" + "visitprofile";
    public static final String API_FRIEND_REQUEST = URL + "api/" + "friendrequests";
    public static final String API_NEW_FRIEND_REQUEST = URL + "api/" + "friendrequestlist";
    public static final String API_FRIEND_LIST = URL + "api/" + "friendslist";
    public static final String API_GET_CHAT = URL + "api/" + "getchat";
    public static final String API_REPORT = URL + "api/" + "report";
    public static final String API_GET_COUNTS = URL + "api/" + "getcounts";
    public static final String API_ONLINE = URL + "api/" + "onlinestatus";
    public static final String API_CREATE_CHAT = URL + "api/" + "createchat";
    public static final String API_GET_CHAT_MESSAGE = URL + "api/" + "getchatmessage";
    public static final String API_GET_MY_MEMBERS = URL + "api/" + "interest";
    public static final String API_GET_INTEREST = URL + "api/" + "interest";

    public static final String API_BLOCK = URL + "api/" + "blockchat";
    public static final String API_FAVORITE = URL + "api/" + "favorite";
    public static final String API_ADD_DEVICE_ID = URL + "api/" + "adddeviceid";
    public static final String API_DELETE_IMAGE = URL + "api/" + "deleteimage";
    public static final String API_MANUAL_PAY_PREMIUM = URL + "api/" + "manualpaypremium";
    public static final String API_CLEAR_CHAT = URL + "api/" + "clearchat";
    public static final String API_UNFRIEND_USER = URL + "api/" + "unfriend";
    public static final String API_CALLING = URL + "api/" + "rtcmessage";
    public static final String API_MISSED_CALL = URL + "api/" + "missedcall";
    public static final String API_FORGOT_PASSWORD = URL + "api/" + "forgotpassword";
    public static final String API_CHANGE_PASSWORD = URL + "api/" + "changepassword";
    public static final String API_IOS_MISSED_ALERT = URL + "api/" + "iosmissedalert";

    /*Token not Included*/
    public static final String API_SIGNUP = URL + "api/" + "signup";
    public static final String API_SIGNIN = URL + "api/" + "emaillogin";
    public static final String API_TOS = URL + "api/" + "mobiletermsandcondition";
    public static final String API_PUSH_SIGNOUT = URL + "api/" + "pushsignout";
    public static final String API_APP_OF_DAY = URL + "api/" + "appoftheday";
    public static final String API_PREMIUM_LIST = URL + "api/" + "premiumlist";
    public static final String API_GENERATE_CLIENT_TOKEN = URL + "api/" + "generateclienttoken";
    public static final String API_LOGIN = URL + "api/" + "login";
    public static final String API_GET_ACCESS_TOKEN = URL + "api/" + "getaccess";
    public static final String API_GET_USER_DETAILS_BY_ID = URL + "api/" + "userdetailbyid";


    public static final String API_UPLOAD_IMAGE = URL + "api/" + "uploadimage";
    /**
     * RESIZE IMAGE
     **/
    public static final String RESIZE_URL = URL + "resize/";
    public static final String RESIZE_CHAT_URL = URL + "resizechat/";
    /**
     * Default UserImage resolution
     **/
    public static final int IMG_WT_HT = 200;
    public static final String IMAGE_RES = "/" + IMG_WT_HT + "/" + IMG_WT_HT;
    public static final String TAG_STATUS = "status";
    public static final String TAG_SUCCESS = "Success";
    public static final String TAG_TYPE_FRIEND_REQUEST = "1";
    public static final String TAG_TYPE_LIKE = "2";
    public static final String TAG_TYPE_DISLIKE = "3";
    public static final String TAG_TYPE_VIDEO_CHAT = "4";
    public static final String TAG_TYPE_SEND_INVITATION = "5";
    public static final String TAG_REQUEST_ACCEPTED = "1";
    public static final String TAG_REQUEST_DECLINE = "2";

    public static final String TAG_SUCCESS_OTP = "Success Send OTP";

    public static final String TAG_RESULT = "result";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // common key
    public static final String TAG_PEOPLES = "peoples";
    public static final String TAG_USERID = "user_id";
    public static final String TAG_AVAILABILITY = "availability";
    public static final String TAG_HASH = "hash";
    public static final String TAG_ANDROID_VERSION = "android_version";
    public static final String TAG_ANDROID_UPDATE = "android_update";
    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );

    // people api
    public static final String TAG_USERNAME = "user_name";
    public static final String TAG_REQUEST_USERNAME = "username";
    public static final String TAG_NEW_USERNAME = "username";

    public static final String TAG_GENDER = "gender";
    public static final String TAG_AGE = "age";
    public static final String TAG_MOBILE_NUMBER = "mobile_no";
    public static final String TAG_BIO = "bio";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LON = "lon";
    public static final String TAG_LATITUDE = "latitude";
    public static final String TAG_LONGITUDE = "longitude";

    public static final String TAG_PURPOSE_PLAN = "purpose_plan";
    public static final String TAG_INTEREST_PLAN = "interest_plan";
    public static final String TAG_ONLINE = "online";
    public static final String TAG_ONLINE_STATUS = "onlinestatus";

    public static final String TAG_LOCATION = "location";
    public static final String TAG_INFO = "info";
    public static final String TAG_USER_INFO = "user";

    public static final String TAG_INTEREST = "interest";
    public static final String TAG_INTERESTS = "interests";
    public static final String TAG_IMAGES = "images";
    public static final String TAG_DATE_OF_BIRTH = "dob";
    public static final String TAG_SHOW_AGE = "show_age";
    public static final String TAG_SHOW_DISTANCE = "show_distance";
    public static final String TAG_AGE_STATUS = "agestatus";
    public static final String TAG_DISTANCE_STATUS = "distancestatus";

    public static final String TAG_INVISIBLE = "invisible";
    public static final String TAG_INVISIBLE_STATUS = "invisiblestatus";

    public static final String TAG_REPORT = "report";
    public static final String TAG_PREMIUM_MEMBER = "premium_member";
    public static final String TAG_PREMIUM_FROM = "premium_from";

    public static final String TAG_SEND_MATCH = "send_match";
    public static final String TAG_SPONSOR_ID = "sponser_id";

    // matches api
    public static final String TAG_MATCHES = "matches";
    public static final String TAG_CHAT_ID = "chat_id";
    public static final String TAG_USERIMAGE = "user_image";
    public static final String TAG_PROFILE_IMAGE = "profileimage";

    public static final String TAG_MESSAGE = "message";
    public static final String TAG_MSG = "msg";
    public static final String TAG_MEMBER = "member";

//    public static final String TAG_AGE = "age";
    public static final String TAG_CHAT_TIME = "chat_time";
    public static final String TAG_LAST_TO_READ = "last_to_read";
    public static final String TAG_USER_STATUS = "user_status";
    // settings api
    public static final String TAG_NOTIFICATIONS = "notifications";
    public static final String TAG_NEW_MATCHES = "new_matches";
    public static final String TAG_SHOW_ME = "show_me";
    public static final String TAG_MEN = "men";
    public static final String TAG_WOMEN = "women";
    public static final String TAG_DISTANCE = "distance";
    public static final String TAG_DISTANCE_IN = "distance_in";
    public static final String TAG_SHOW_AGES = "show_ages";
    public static final String TAG_MESSAGE_NOTIFICATION = "message_notification";
    public static final String TAG_LIKE_NOTIFICATION = "like_notification";
    public static final String TAG_HIDE_ADS = "hide_ads";
    public static final String TAG_CHATS = "chats";
    public static final String TAG_RECEIVER_ID = "receiver_id";

    // chat api
    public static final String TAG_SENDER_ID = "sender_id";
    public static final String TAG_TYPE = "type";
    public static final String TAG_UPLOAD_IMAGE = "upload_image";
    public static final String TAG_FAVORITE = "favorite";
    public static final String TAG_LAST_ONLINE = "last_online";
    public static final String TAG_LAST_REPLIED = "last_replied";
    public static final String TAG_BLOCK = "block";
    public static final String TAG_BLOCKED_BY_ME = "blocked_by_me";
    public static final String TAG_CLEAR_CHAT = "clear_chat";
    public static final String TAG_LAST_SEEN = "last_seen";
    public static final String TAG_DATE = "date";
    // admin datas
    public static final String TAG_PEOPLEFOR = "peoplefor";
    public static final String TAG_ID = "id";
    public static final String TAG_REGISTERED_ID = "register_id";

    public static final String TAG_NAME = "name";

    public static final String TAG_COMMENT_USER_NAME = "comment_user";
    public static final String TAG_NOTIFICATION_ID = "notification_id";
    public static final String TAG_NOTIFICATION_FRIEND_ID = "friend_id";

    public static final String TAG_NOTIFICATION_TYPE = "type";
    public static final String TAG_NOTIFICATION_MESSAGE = "message";
    public static final String TAG_NOTIFICATION_SENDER_NAME = "name";
    public static final String TAG_NOTIFICATION_SENDER_PROFILE_IMAGE = "img";
    public static final String TAG_NOTIFICATION_SENDER_URL = "image";
    public static final String TAG_NOTIFICATION_TIME = "time_msg";
    public static final String TAG_INTENT_FROM = "from";
    public static final String TAG_INTENT_PROFILE_PAGE = "profile";

    public static final String TAG_COMMENT_ID = "comment_id";
    public static final String TAG_COMMENT = "comment";
    public static final String TAG_COMMENT_USER_IMAGE = "image";

    public static final String TAG_ICON = "icon";
    public static final String TAG_MAX_AGE = "max_age";
    public static final String TAG_MAX_DISTANCE = "max_distance";
    public static final String TAG_ADMIN_ENABLE_ADS = "admin_enable_ads";
    public static final String TAG_AD_UNIT_ID = "ad_unit_id";
    public static final String TAG_PRICE = "price";
    public static final String TAG_DAYS = "days";
    public static final String TAG_TOKEN = "token";
    public static final String TAG_AUTHORIZATION = "Authorization";
    public static final String TAG_ACCESS_TOKEN = "access_token";
    public static final String TAG_ACCESS_TOKEN_NEW = "token";

    public static final String TAG_EXPIRY = "expiry";
    public static final String TAG_INTENT_FRIEND_ID_PROFILE_PAGE = "strFriendID";

    public static final String TAG_CURRENCY = "currency";
    public static final String TAG_TITLE = "title";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_VIEW_URL = "view_url";
    public static final String TAG_PAYMENT = "payment";
    public static final String TAG_LICENSE_KEY = "license_key";
    // profile api
    public static final String TAG_FILTER = "filter";
    public static final String TAG_PEOPLE_FOR = "people_for";
    public static final String TAG_SHOW_LOCATION = "show_location";
    public static final String TAG_MEMBERSHIP_VALID = "membership_valid";
    public static final String TAG_LIMIT = "limit";
    public static final String TAG_OFFSET = "offset";
    public static final String TAG_TIMESTAMP = "timestamp";
    public static final String TAG_DEVICE_TOKEN = "device_token";
    public static final String TAG_DEVICE_TYPE = "device_type";
    public static final String TAG_DEVICE_ID = "device_id";
    public static final String TAG_DEVICE_MODE = "device_mode";
    public static final String TAG_IMAGE_URL = "image_url";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_PASSWORD = "password";
    public static final String TAG_VISITORS = "visitors";
    public static final String TAG_FRIEND_REQUEST = "friend_request";
    public static final String TAG_DEVICE_REGISTERED = "device_registered";
    public static final String TAG_FOLLOW_ID = "follow_id";
    public static final String TAG_SORT = "sort";
    public static final String TAG_SEARCH_KEY = "search_key";
    public static final String TAG_FAVOURITE_USER_ID = "favorite_user_id";
    public static final String TAG_FRIEND_ID = "friend_id";
    public static final String TAG_VISIT_USER_ID = "visit_user_id";
    public static final String TAG_REPORT_USER_ID = "report_user_id";
    public static final String TAG_PREMIUM_ID = "premium_id";
    public static final String TAG_CURRENCY_CODE = "currency_code";
    public static final String TAG_PAY_NONCE = "pay_nonce";
    public static final String TAG_IMAGE = "image";
    public static final String TAG_IMG = "img";

    public static final String TAG_BLOCK_USER_ID = "block_user_id";
    public static final String TAG_UNFRIEND_USER_ID = "unfriend_user_id";
    public static final String ISLOGGED = "isLogged";
    public static final String TAG_OLD_PASSWORD = "old_password";
    public static final String TAG_NEW_PASSWORD = "new_password";

    //call api
    public static final String TAG_FROM_ID = "fromId";
    public static final String TAG_TO_ID = "toId";
    public static final String TAG_CHATID = "chatId";
    public static final String TAG_CHATTIME = "chatTime";
// TAG Interest Static



    /**
     * preference
     **/
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static final String TAG_COUNTRY_ID = "country_id";
    public static final String TAG_COUNTRY_CODE = "country_code";
    public static final String TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE = "country_code";
    public static final String TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER = "phone_number";
    public static final String TAG_LOGIN_INTENT_DETAIL_NAME = "name";
    public static final String TAG_LOGIN_INTENT_DETAIL_BIRTHDATE = "birth_date";
    public static final String TAG_LOGIN_INTENT_DETAIL_LATITUDE = "lat";
    public static final String TAG_LOGIN_INTENT_DETAIL_LONGITUDE = "lon";
    public static final String TAG_LOGIN_INTENT_DETAIL_LOCATION = "location";
    public static final String TAG_LOGIN_INTENT_DETAIL_PASSWORD = "password";

}
