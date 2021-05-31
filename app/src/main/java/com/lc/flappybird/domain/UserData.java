package com.lc.flappybird.domain;

public class UserData {
    private String userName;
    private String score;
    private String time;//in seconds

    public UserData(String userName, String score, String time) {
        this.userName = userName;
        this.score = score;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
