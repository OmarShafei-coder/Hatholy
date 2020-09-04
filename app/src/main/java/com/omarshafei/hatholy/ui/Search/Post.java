package com.omarshafei.hatholy.ui.Search;

public class Post {

    private String phoneNumber;
    private String missingType;
    private String imageUrl;

    public Post(String phoneNumber, String missingType) {
        this.phoneNumber = phoneNumber;
        this.missingType = missingType;
    }

    public Post(String phoneNumber, String missingType, String imageUrl) {
        this.phoneNumber = phoneNumber;
        this.missingType = missingType;
        this.imageUrl = imageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMissingType() {
        return missingType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMissingType(String missingType) {
        this.missingType = missingType;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

