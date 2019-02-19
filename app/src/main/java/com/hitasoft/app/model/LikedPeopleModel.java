package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LikedPeopleModel {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("info")
    @Expose
    private List<Info> info = null;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    public class Info {

        @SerializedName("notification_id")
        @Expose
        private Integer notificationId;

        @SerializedName("userid")
        @Expose
        private String userId;

        @SerializedName("register_id")
        @Expose
        private String registerId;
        @SerializedName("friend_id")
        @Expose
        private String friendId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("img")
        @Expose
        private String img;
        @SerializedName("image")
        @Expose
        private String image;
        @SerializedName("message")
        @Expose
        private String message;
        @SerializedName("time_msg")
        @Expose

        private String timeMsg;
        @SerializedName("type")
        @Expose

        private Integer type;
        @SerializedName("status")
        @Expose

        private String status;
        @SerializedName("matchMaker")
        @Expose
        private double matchMaker;

        public Integer getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(Integer notificationId) {
            this.notificationId = notificationId;
        }

        public String getRegisterId() {
            return registerId;
        }

        public void setRegisterId(String registerId) {
            this.registerId = registerId;
        }

        public String getUserIdLikeToken() {
            return userId;
        }

        public void setUserIdLikeToken(String userId) {
            this.userId = userId;
        }



        public String getFriendId() {
            return friendId;
        }

        public void setFriendId(String friendId) {
            this.friendId = friendId;
        }
        public double getMatchMaker() {
            return matchMaker;
        }

        public void setMatchMaker(double matchMaker) {
            this.matchMaker = matchMaker;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimeMsg() {
            return timeMsg;
        }

        public void setTimeMsg(String timeMsg) {
            this.timeMsg = timeMsg;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }
}
