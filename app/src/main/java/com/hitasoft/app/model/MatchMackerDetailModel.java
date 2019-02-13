package com.hitasoft.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MatchMackerDetailModel {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("info")
    @Expose
    private Info info;

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

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }


    public class Info {

        @SerializedName("matchmaker")
        @Expose
        private List<Object> matchmaker = null;
        @SerializedName("user")
        @Expose
        private User user;
        @SerializedName("matchmakermember")
        @Expose
        private List<Matchmakermember> matchmakermember = null;
        @SerializedName("nomatchmakermember")
        @Expose
        private List<Nomatchmakermember> nomatchmakermember = null;

        public List<Object> getMatchmaker() {
            return matchmaker;
        }

        public void setMatchmaker(List<Object> matchmaker) {
            this.matchmaker = matchmaker;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<Matchmakermember> getMatchmakermember() {
            return matchmakermember;
        }

        public void setMatchmakermember(List<Matchmakermember> matchmakermember) {
            this.matchmakermember = matchmakermember;
        }

        public List<Nomatchmakermember> getNomatchmakermember() {
            return nomatchmakermember;
        }

        public void setNomatchmakermember(List<Nomatchmakermember> nomatchmakermember) {
            this.nomatchmakermember = nomatchmakermember;
        }

    }

    public class Matchmakermember {

        @SerializedName("register_id")
        @Expose
        private Integer registerId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("img")
        @Expose
        private String img;
        @SerializedName("image")
        @Expose
        private String image;

        public Integer getRegisterId() {
            return registerId;
        }

        public void setRegisterId(Integer registerId) {
            this.registerId = registerId;
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

    }

    public class Nomatchmakermember {

        @SerializedName("register_id")
        @Expose
        private Integer registerId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("img")
        @Expose
        private String img;
        @SerializedName("image")
        @Expose
        private String image;

        public Integer getRegisterId() {
            return registerId;
        }

        public void setRegisterId(Integer registerId) {
            this.registerId = registerId;
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
    }

    public class User {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("register_id")
        @Expose
        private Integer registerId;
        @SerializedName("firstname")
        @Expose
        private String firstname;
        @SerializedName("username")
        @Expose
        private String username;
        @SerializedName("mobile_no")
        @Expose
        private String mobileNo;
        @SerializedName("status")
        @Expose
        private Integer status;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;
        @SerializedName("birthday")
        @Expose
        private Object birthday;
        @SerializedName("age")
        @Expose
        private String age;
        @SerializedName("register_date")
        @Expose
        private String registerDate;
        @SerializedName("active_date")
        @Expose
        private String activeDate;
        @SerializedName("deactive_date")
        @Expose
        private String deactiveDate;
        @SerializedName("last_updated_date")
        @Expose
        private String lastUpdatedDate;
        @SerializedName("location")
        @Expose
        private String location;
        @SerializedName("latitude")
        @Expose
        private String latitude;
        @SerializedName("longitude")
        @Expose
        private String longitude;
        @SerializedName("purpose_plan")
        @Expose
        private Integer purposePlan;
        @SerializedName("interest_plan")
        @Expose
        private String interestPlan;
        @SerializedName("peoplefor")
        @Expose
        private Object peoplefor;
        @SerializedName("userstatus")
        @Expose
        private Integer userstatus;
        @SerializedName("premiumstatus")
        @Expose
        private Integer premiumstatus;
        @SerializedName("facebookid")
        @Expose
        private Object facebookid;
        @SerializedName("profileimage")
        @Expose
        private String profileimage;
        @SerializedName("images")
        @Expose
        private String images;
        @SerializedName("visitors")
        @Expose
        private Object visitors;
        @SerializedName("lastsearch")
        @Expose
        private Object lastsearch;
        @SerializedName("agestatus")
        @Expose
        private Integer agestatus;
        @SerializedName("distancestatus")
        @Expose
        private Integer distancestatus;
        @SerializedName("invisiblestatus")
        @Expose
        private Integer invisiblestatus;
        @SerializedName("adsstatus")
        @Expose
        private Object adsstatus;
        @SerializedName("onlinestatus")
        @Expose
        private Integer onlinestatus;
        @SerializedName("onlinetimestamp")
        @Expose
        private Integer onlinetimestamp;
        @SerializedName("notifications")
        @Expose
        private String notifications;
        @SerializedName("gender")
        @Expose
        private String gender;
        @SerializedName("info")
        @Expose
        private String info;
        @SerializedName("interest")
        @Expose
        private String interest;
        @SerializedName("bio")
        @Expose
        private String bio;
        @SerializedName("searchfilter")
        @Expose
        private String searchfilter;
        @SerializedName("token")
        @Expose
        private String token;
        @SerializedName("timestamp")
        @Expose
        private Integer timestamp;
        @SerializedName("unauthorized")
        @Expose
        private Integer unauthorized;
        @SerializedName("access_token")
        @Expose
        private String accessToken;
        @SerializedName("premium_from")
        @Expose
        private Integer premiumFrom;
        @SerializedName("message_notification")
        @Expose
        private Integer messageNotification;
        @SerializedName("like_notificarion")
        @Expose
        private Integer likeNotificarion;
        @SerializedName("comment_float")
        @Expose
        private Integer commentFloat;
        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("matchmaker")
        @Expose
        private Integer matchmaker;
        @SerializedName("sponser_id")
        @Expose
        private Integer sponserId;
        @SerializedName("mob")
        @Expose
        private String mob;
        @SerializedName("video_date")
        @Expose
        private Integer videoDate;
        @SerializedName("dinner")
        @Expose
        private Integer dinner;
        @SerializedName("image")
        @Expose
        private String image;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getRegisterId() {
            return registerId;
        }

        public void setRegisterId(Integer registerId) {
            this.registerId = registerId;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Object getBirthday() {
            return birthday;
        }

        public void setBirthday(Object birthday) {
            this.birthday = birthday;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getRegisterDate() {
            return registerDate;
        }

        public void setRegisterDate(String registerDate) {
            this.registerDate = registerDate;
        }

        public String getActiveDate() {
            return activeDate;
        }

        public void setActiveDate(String activeDate) {
            this.activeDate = activeDate;
        }

        public String getDeactiveDate() {
            return deactiveDate;
        }

        public void setDeactiveDate(String deactiveDate) {
            this.deactiveDate = deactiveDate;
        }

        public String getLastUpdatedDate() {
            return lastUpdatedDate;
        }

        public void setLastUpdatedDate(String lastUpdatedDate) {
            this.lastUpdatedDate = lastUpdatedDate;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public Integer getPurposePlan() {
            return purposePlan;
        }

        public void setPurposePlan(Integer purposePlan) {
            this.purposePlan = purposePlan;
        }

        public String getInterestPlan() {
            return interestPlan;
        }

        public void setInterestPlan(String interestPlan) {
            this.interestPlan = interestPlan;
        }

        public Object getPeoplefor() {
            return peoplefor;
        }

        public void setPeoplefor(Object peoplefor) {
            this.peoplefor = peoplefor;
        }

        public Integer getUserstatus() {
            return userstatus;
        }

        public void setUserstatus(Integer userstatus) {
            this.userstatus = userstatus;
        }

        public Integer getPremiumstatus() {
            return premiumstatus;
        }

        public void setPremiumstatus(Integer premiumstatus) {
            this.premiumstatus = premiumstatus;
        }

        public Object getFacebookid() {
            return facebookid;
        }

        public void setFacebookid(Object facebookid) {
            this.facebookid = facebookid;
        }

        public String getProfileimage() {
            return profileimage;
        }

        public void setProfileimage(String profileimage) {
            this.profileimage = profileimage;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }

        public Object getVisitors() {
            return visitors;
        }

        public void setVisitors(Object visitors) {
            this.visitors = visitors;
        }

        public Object getLastsearch() {
            return lastsearch;
        }

        public void setLastsearch(Object lastsearch) {
            this.lastsearch = lastsearch;
        }

        public Integer getAgestatus() {
            return agestatus;
        }

        public void setAgestatus(Integer agestatus) {
            this.agestatus = agestatus;
        }

        public Integer getDistancestatus() {
            return distancestatus;
        }

        public void setDistancestatus(Integer distancestatus) {
            this.distancestatus = distancestatus;
        }

        public Integer getInvisiblestatus() {
            return invisiblestatus;
        }

        public void setInvisiblestatus(Integer invisiblestatus) {
            this.invisiblestatus = invisiblestatus;
        }

        public Object getAdsstatus() {
            return adsstatus;
        }

        public void setAdsstatus(Object adsstatus) {
            this.adsstatus = adsstatus;
        }

        public Integer getOnlinestatus() {
            return onlinestatus;
        }

        public void setOnlinestatus(Integer onlinestatus) {
            this.onlinestatus = onlinestatus;
        }

        public Integer getOnlinetimestamp() {
            return onlinetimestamp;
        }

        public void setOnlinetimestamp(Integer onlinetimestamp) {
            this.onlinetimestamp = onlinetimestamp;
        }

        public String getNotifications() {
            return notifications;
        }

        public void setNotifications(String notifications) {
            this.notifications = notifications;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getInterest() {
            return interest;
        }

        public void setInterest(String interest) {
            this.interest = interest;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getSearchfilter() {
            return searchfilter;
        }

        public void setSearchfilter(String searchfilter) {
            this.searchfilter = searchfilter;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Integer getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Integer timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getUnauthorized() {
            return unauthorized;
        }

        public void setUnauthorized(Integer unauthorized) {
            this.unauthorized = unauthorized;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Integer getPremiumFrom() {
            return premiumFrom;
        }

        public void setPremiumFrom(Integer premiumFrom) {
            this.premiumFrom = premiumFrom;
        }

        public Integer getMessageNotification() {
            return messageNotification;
        }

        public void setMessageNotification(Integer messageNotification) {
            this.messageNotification = messageNotification;
        }

        public Integer getLikeNotificarion() {
            return likeNotificarion;
        }

        public void setLikeNotificarion(Integer likeNotificarion) {
            this.likeNotificarion = likeNotificarion;
        }

        public Integer getCommentFloat() {
            return commentFloat;
        }

        public void setCommentFloat(Integer commentFloat) {
            this.commentFloat = commentFloat;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public Integer getMatchmaker() {
            return matchmaker;
        }

        public void setMatchmaker(Integer matchmaker) {
            this.matchmaker = matchmaker;
        }

        public Integer getSponserId() {
            return sponserId;
        }

        public void setSponserId(Integer sponserId) {
            this.sponserId = sponserId;
        }

        public String getMob() {
            return mob;
        }

        public void setMob(String mob) {
            this.mob = mob;
        }

        public Integer getVideoDate() {
            return videoDate;
        }

        public void setVideoDate(Integer videoDate) {
            this.videoDate = videoDate;
        }

        public Integer getDinner() {
            return dinner;
        }

        public void setDinner(Integer dinner) {
            this.dinner = dinner;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

    }
}
