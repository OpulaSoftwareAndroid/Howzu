package com.hitasoft.app.model;

public class MyMembersModel {
    private String memberName;
    private int memberProfile;

    public MyMembersModel(String memberName, int memberProfile) {
        this.memberName = memberName;
        this.memberProfile = memberProfile;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getMemberProfile() {
        return memberProfile;
    }

    public void setMemberProfile(int memberProfile) {
        this.memberProfile = memberProfile;
    }
}
