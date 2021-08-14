package com.shreyanshu.sportyforstats;

import java.util.Date;

public class StructMatch {
    public Date date;
    public String player1;
    public String player2;
    public String player3;
    public String player4;
    public int points1;
    public int points2;

    StructMatch( Date date, String player1, String player2,String player3,String player4, int points1,int points2){
        this.date = date;
        this.player1 = player1;
        this.player2 = player2;
        this.player3 = player3;
        this.player4 = player4;
        this.points1 = points1;
        this.points2 = points2;
    }

    StructMatch(){

    }
}
