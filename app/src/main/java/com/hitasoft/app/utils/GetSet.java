package com.hitasoft.app.utils;

/****************
 *
 * @author 'Hitasoft Technologies'
 *
 * Description:
 * This class is used for get and set logged user data
 *
 * Revision History:
 * Version 1.0 - Initial Version
 *
 *****************/
public class GetSet {
    private static boolean isLogged = false;
    private static String userId = null;
    private static String friendID = null;
    private static String userName = null;
    private static String Email = null;
    private static String Password = null;
    private static String imageUrl = null;
    private static boolean isPremium = false;
    private static boolean bannerEnable = false;
    private static String adUnitId = null;
    private static boolean msgNotification = false;
    private static boolean likeNotification = false;
    private static boolean hideAds = false;
    private static String location = null;
    private static double latitude = 0.0;
    private static double longitude = 0.0;
    private static String token="";
    private static String maxAge = null;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        GetSet.token = token;
    }

    public static boolean isLogged() {
        return isLogged;
    }

    public static void setLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        GetSet.userId = userId;
    }
    public static String getFriendId() {
        return friendID;
    }

    public static void setFriendId(String friendID) {
        GetSet.friendID = friendID;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        GetSet.userName = userName;
    }

    public static String getEmail() {
        return Email;
    }

    public static void setEmail(String email) {
        Email = email;
    }

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }

    public static String getImageUrl() {
        return imageUrl;
    }

    public static void setImageUrl(String imageUrl) {
        GetSet.imageUrl = imageUrl;
    }

    public static boolean isPremium() {
        return isPremium;
    }

    public static void setPremium(boolean isPremium) {
        GetSet.isPremium = isPremium;
    }

    public static boolean isBannerEnable() {
        return bannerEnable;
    }

    public static void setBannerEnable(boolean bannerEnable) {
        GetSet.bannerEnable = bannerEnable;
    }

    public static String getAdUnitId() {
        return adUnitId;
    }

    public static void setAdUnitId(String adUnitId) {
        GetSet.adUnitId = adUnitId;
    }

    public static boolean isMsgNotificationEnable() {
        return msgNotification;
    }

    public static void setMsgNotification(boolean msgNotification) {
        GetSet.msgNotification = msgNotification;
    }

    public static boolean isLikeNotificationEnable() {
        return likeNotification;
    }

    public static void setLikeNotification(boolean likeNotification) {
        GetSet.likeNotification = likeNotification;
    }

    public static boolean isHideAds() {
        return hideAds;
    }

    public static void setHideAds(boolean hideAds) {
        GetSet.hideAds = hideAds;
    }

    public static String getLocation() {
        return location;
    }

    public static void setLocation(String location) {
        GetSet.location = location;
    }

    public static void reset() {
        GetSet.setLogged(false);
        GetSet.setEmail(null);
        GetSet.setPassword(null);
        GetSet.setUserId(null);
        GetSet.setUserName(null);
        GetSet.setImageUrl(null);
        GetSet.setLatitude(0);
        GetSet.setLongitude(0);
        GetSet.setLocation(null);
    }

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        GetSet.latitude = latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        GetSet.longitude = longitude;
    }

    public static void setMaxAge(String maxAge) {
        GetSet.maxAge = maxAge;
    }

    public static String getMaxAge() {
        return maxAge;
    }
}
