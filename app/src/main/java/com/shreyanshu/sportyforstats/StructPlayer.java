package com.shreyanshu.sportyforstats;

import java.util.List;

public class StructPlayer {
    public int played_matches;
    public int won_matches;
    public int lost_matches;
    public float score;
    public int points_scored;
    public int points_conceded;
    public List<String> matches;
    public List<String> teams;


    StructPlayer( int played_matches,int won_matches, int lost_matches,float score,int points_scored, int points_conceded, List<String> all_matches,List<String> teams) {
        this.played_matches = played_matches;
        this.won_matches = won_matches;
        this.lost_matches = lost_matches;
        this.score = score;
        this.points_scored= points_scored;
        this.points_conceded= points_conceded;
        this.matches=all_matches;
        this.teams=teams;
    }

    StructPlayer(){

    }

}
