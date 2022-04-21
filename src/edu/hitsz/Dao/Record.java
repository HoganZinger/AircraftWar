package edu.hitsz.Dao;

import java.io.Serializable;

public class Record implements Serializable {
    private static final long serialVersionUID = 6866904399011716299L;
    private int rank;
    private String userName;
    private int score;
    private String time;

    public Record(String userName, int score, String time) {
        this.userName = userName;
        this.score = score;
        this.time = time;
    }
    public int getRank(){
        return rank;
    }

    public void setRank(int rank){
        this.rank = rank;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score){
        this.score = score;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        this.time = time;
    }
}
