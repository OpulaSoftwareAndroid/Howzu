package com.hitasoft.app.model;

public class POJONotificationDetails {


        private String strNotificationMessage,strNotificationTime,strNotificationType,
                strNotificationImageMainURL,strNotificationImageProfileURL,strNotificationFriendID,strNotificationName;
        private int strNotificationID;

        public POJONotificationDetails( int strNotificationID,String strNotificationName,String strNotificationMessage
                ,String strNotificationTime,String strNotificationType,String strNotificationImageMainURL
        ,String strNotificationImageProfileURL,String strNotificationFriendID)
        {
            this.strNotificationName = strNotificationName;
            this.strNotificationMessage = strNotificationMessage;
            this.strNotificationID = strNotificationID;
            this.strNotificationFriendID = strNotificationFriendID;
            this.strNotificationImageProfileURL = strNotificationImageProfileURL;
            this.strNotificationImageMainURL = strNotificationImageMainURL;
            this.strNotificationTime = strNotificationTime;
            this.strNotificationType = strNotificationType;

        }

        public String getNotificationName() {
            return strNotificationName;
        }

        public void setNotificationName(String strNotificationName) {
            this.strNotificationName = strNotificationName;
        }


        public String getNotificationMessage() {
            return strNotificationMessage;
        }

        public void setNotificationMessage(String strNotificationMessage) {
            this.strNotificationMessage = strNotificationMessage;
        }

        public String getNotificationFriendID() {
            return strNotificationFriendID;
        }

        public void setNotificationFriendID(String strNotificationFriendID) {
            this.strNotificationFriendID = strNotificationFriendID;
        }


        public int getNotificationID() {
            return strNotificationID;
        }

        public void setNotificationID(int strNotificationTime) {
            this.strNotificationID = strNotificationID;
        }

        public String getNotificationTime() {
            return strNotificationTime;
        }
        public void setNotificationTime(String strNotificationTime) {
            this.strNotificationTime = strNotificationTime;
        }

        public String getNotificationType() {
            return strNotificationType;
        }
        public void setNotificationType(String strNotificationType) {
            this.strNotificationType = strNotificationType;
        }

        public String getNotificationImageMainURL() {
                return strNotificationImageMainURL;

        }
        public void setNotificationImageMainURL(String strNotificationImageMainURL) {
                this.strNotificationImageMainURL = strNotificationImageMainURL;
        }

        public String getNotificationImageProfileUrl() {
            return strNotificationImageProfileURL;
        }
        public void setNotificationImageProfileUrl(String strNotificationImageProfileURL) {
            this.strNotificationImageProfileURL = strNotificationImageProfileURL;
        }



}
