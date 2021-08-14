package com.shreyanshu.sportyforstats;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class match extends AppCompatActivity {
    String player1, player2, player3, player4, team1, team2;
    String team_collection_path = "";
    int point1 = 0, point2 = 0;
    float win_ratio1, win_ratio2, lose_ratio1, lose_ratio2, total_points = 0;
    TextView tv_points_team1, tv_points_team2;
    FirebaseFirestore db;
    StructPlayer s_team1, s_team2;
    boolean doubleBackToExitPressedOnce = false, doubleFinishToFinish = false;
    StructMatch[] common_matches;
    int no_common_matches = -1;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(R.id.m_LinearLayout), "Please click BACK again to exit", Snackbar.LENGTH_LONG)
                .show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        db = FirebaseFirestore.getInstance();

        tv_points_team1 = findViewById(R.id.ma_points_team1);
        tv_points_team2 = findViewById(R.id.ma_points_team2);

        player1 = getIntent().getStringExtra("player1");
        player2 = getIntent().getStringExtra("player2");
        player3 = getIntent().getStringExtra("player3");
        player4 = getIntent().getStringExtra("player4");
        // TextView tv_player1, tv_player2, tv_player3, tv_player4;

        if (player1.equals("")) {
            findViewById(R.id.ma_player1).setVisibility(View.GONE);
            team_collection_path = "players";
            team1 = player2;
        } else {
            if (!player2.equals("")) {
                team_collection_path = "d_teams";
                team1 = combineSort(player1.toCharArray(), player2.toCharArray());
            }
            ((TextView) findViewById(R.id.ma_player1)).setText(player1);
        }

        if (player2.equals("")) {
            team_collection_path = "players";
            findViewById(R.id.ma_player2).setVisibility(View.GONE);
            team1 = player1;
        } else {
            ((TextView) findViewById(R.id.ma_player2)).setText(player2);
        }

        if (player3.equals("")) {
            findViewById(R.id.ma_player3).setVisibility(View.GONE);
            team2 = player4;
        } else {
            ((TextView) findViewById(R.id.ma_player3)).setText(player3);
        }

        if (player4.equals("")) {
            findViewById(R.id.ma_player4).setVisibility(View.GONE);
            team2 = player3;
        } else {
            if (!player3.equals("")) {
                team2 = combineSort(player3.toCharArray(), player4.toCharArray());
            }
            ((TextView) findViewById(R.id.ma_player4)).setText(player4);
        }

        db.collection(team_collection_path).document(team1).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                s_team1 = documentSnapshot.toObject(StructPlayer.class);
                if (s_team1 == null) {
                    s_team1 = new StructPlayer(0, 0, 0, 100, 0, 0, null, null);
                    db.collection(team_collection_path).document(team1).set(s_team1);
                    db.collection("players").document(player1).update("teams", FieldValue.arrayUnion(team1));
                    db.collection("players").document(player2).update("teams", FieldValue.arrayUnion(team1));
                    db.collection("common_data").document("common_data").update("team_total", FieldValue.increment(100));
                }
                ((TextView) findViewById(R.id.am_points1)).setText(s_team1.score + "");
                if (s_team2 != null) {
                    get_total_points();
                }
            }
        });
        db.collection(team_collection_path).document(team2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                s_team2 = documentSnapshot.toObject(StructPlayer.class);
                if (s_team2 == null) {
                    s_team2 = new StructPlayer(0, 0, 0, 100, 0, 0, null, null);
                    db.collection(team_collection_path).document(team2).set(s_team2);
                    db.collection("players").document(player3).update("teams", FieldValue.arrayUnion(team2));
                    db.collection("players").document(player4).update("teams", FieldValue.arrayUnion(team2));
                    db.collection("common_data").document("common_data").update("team_total", FieldValue.increment(100));
                }
                ((TextView) findViewById(R.id.am_points2)).setText(s_team2.score + "");
                if (s_team1 != null) {
                    get_total_points();
                }
            }
        });
    }

    private void get_total_points() {
        final Thread calculateWinRatio = new Thread(new Runnable() {
            @Override
            public void run() {
                win_ratio1 = winRatio(s_team1, s_team2);
                win_ratio2 = winRatio(s_team2, s_team1);
                lose_ratio1 = loseRatio(s_team2, s_team1);
                lose_ratio2 = loseRatio(s_team1, s_team2);
            }
        });

        db.collection("common_data").document("common_data").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (player1.equals("") || player2.equals("")) {
                    total_points = (float) Float.parseFloat(documentSnapshot.get("total_points").toString());
                } else {
                    total_points = (float) Float.parseFloat(documentSnapshot.get("team_total").toString());
                }
                calculateWinRatio.start();
                getCommonMatches();

            }
        });
    }

    private String combineSort(char[] s1, char[] s2) {
        int l = s1.length;
        boolean one = true;
        if (l > s2.length) {
            l = s2.length;
            one = false;
        }
        for (int i = 0; i < l; i++) {
            if (s1[i] < s2[i]) {
                return (String.valueOf(s1) + "," + String.valueOf(s2));
            } else if (s2[i] < s1[i]) {
                return (String.valueOf(s2) + "," + String.valueOf(s1));
            }
        }
        if (one) {
            return (String.valueOf(s1) + "," + String.valueOf(s2));
        } else {
            return (String.valueOf(s2) + "," + String.valueOf(s1));
        }
    }

    public float winRatio(StructPlayer win_player, StructPlayer lose_player) {
        return ((total_points + lose_player.score) / (win_player.score * 10));

    }

    public float loseRatio(StructPlayer win_player, StructPlayer lose_player) {
        return ((lose_player.score + win_player.score) / (win_player.score * 10));
    }

    public void onPointScore(View view) {
        if (view.getId() == R.id.ma_team1_ll) point1++;
        else point2++;
        updatePoints();
    }

    public void updatePoints() {
        tv_points_team1.setText(point1 + "");
        tv_points_team2.setText(point2 + "");
    }

    public void onGameFinish(View view) {
        if (doubleFinishToFinish) {
            try {
                final String documentId = db.collection("matches").document().getId();
                StructMatch struct_match = new StructMatch(Calendar.getInstance().getTime(), player1, player2, player3, player4, point1, point2);
                db.collection("matches").document(documentId).set(struct_match);
                setStats(documentId);
            } catch (Exception e){
                Toast.makeText(match.this,"Error Check Internet and try again",Toast.LENGTH_SHORT).show();
            }
        }

        this.doubleFinishToFinish = true;
        Toast.makeText(this, "Cross check score and press again to Continue", Toast.LENGTH_SHORT)
                .show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleFinishToFinish = false;
            }
        }, 1000);
    }


    public void setStats(String id) {
        s_team1.played_matches++;
        s_team2.played_matches++;
        Intent intent = new Intent(this, result.class);
        intent.putExtra("team1", team1);
        intent.putExtra("team2", team2);

        if (point1 > point2) {
            float winner_get = win_ratio1 * (point1 - point2), loser_lose = lose_ratio2 * (point1 - point2);
            intent.putExtra("score1", winner_get);
            intent.putExtra("score2", -loser_lose);
            s_team1.won_matches++;
            s_team1.score += winner_get;
            Log.d("TAG", "setStats: '" + s_team1.score);
            s_team2.lost_matches++;
            s_team2.score -= loser_lose;
            if (player1.equals("") | player2.equals(""))
                db.collection("common_data").document("common_data").update("total_points", FieldValue.increment(winner_get - loser_lose));
            else
                db.collection("common_data").document("common_data").update("team_total", FieldValue.increment(winner_get - loser_lose));
        } else {
            float winner_get = win_ratio2 * (point2 - point1), loser_lose = lose_ratio1 * (point2 - point1);
            intent.putExtra("score1", 0.00);
            intent.putExtra("score2", 0.00);
            s_team2.won_matches++;
            s_team2.score += win_ratio2 * (point2 - point1);
            s_team1.lost_matches++;
            s_team1.score -= lose_ratio1 * (point2 - point1);
            if (player1.equals("") | player2.equals(""))
                db.collection("common_data").document("common_data").update("total_points", FieldValue.increment(winner_get - loser_lose));
            else
                db.collection("common_data").document("common_data").update("team_total", FieldValue.increment(winner_get - loser_lose));
        }

        s_team1.points_scored += point1;
        s_team2.points_scored += point2;

        s_team1.points_conceded += point2;
        s_team2.points_conceded += point1;

        if (s_team1.matches == null) {
            s_team1.matches = new ArrayList<>();
        }
        if (s_team2.matches == null) {
            s_team2.matches = new ArrayList<>();
        }

        s_team1.matches.add(id);
        s_team2.matches.add(id);
        if (player1.equals("") | player2.equals("")) intent.putExtra("doubles", false);
        else intent.putExtra("doubles", true);
        db.collection(team_collection_path).document(team1).set(s_team1);
        db.collection(team_collection_path).document(team2).set(s_team2);
        startActivity(intent);
    }

    public void customInput(View view) {
        final TextView textView = (TextView) view;
        final AlertDialog.Builder alert = new AlertDialog.Builder(match.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_input_dialog, null);
        final EditText t_password = mView.findViewById(R.id.ld_password);
        //final Context context = this;
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String text = t_password.getText().toString();
                try {
                    Integer.parseInt(text);
                    textView.setText(text);
                    point1 = Integer.parseInt(tv_points_team1.getText().toString());
                    point2 = Integer.parseInt(tv_points_team2.getText().toString());
                } catch (NumberFormatException ignored) {
                }
            }
        });
        alertDialog.show();
    }

    public void team1Stats(View view) {
        showStats(s_team1);
    }

    public void team2Stats(View view) {
        showStats(s_team2);
    }

    public void showStats(StructPlayer team) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View mView = match.this.getLayoutInflater().inflate(R.layout.detail_score_dialog, null);
        alert.setView(mView);
        //final Context context = this;
        setData(team, mView);
        final AlertDialog alertDialog = alert.create();
        mView.findViewById(R.id.ds_b_matches).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                viewPreviousEncounters();
            }
        });

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();
    }

    public void viewPreviousEncounters() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View mView = match.this.getLayoutInflater().inflate(R.layout.previous_encounter_dialog, null);
        alert.setView(mView);
        //final Context context = this;
        setMatchData(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void setMatchData(View mView) {
        LinearLayout ll_team1_matches, ll_team2_matches;
        ((TextView) mView.findViewById(R.id.pe_team1)).setText(player1 + "\n" + player2);
        ((TextView) mView.findViewById(R.id.pe_team2)).setText(player3 + "\n" + player4);
        ll_team1_matches = mView.findViewById(R.id.pe_prev_team1_matches);
        ll_team2_matches = mView.findViewById(R.id.pe_prev_team2_matches);
        for (int i = 0; i < no_common_matches; i++) {
            TextView tv_match1 = new TextView(match.this);
            TextView tv_match2 = new TextView(match.this);
            tv_match1.setTextSize(46);
            tv_match2.setTextSize(46);
            tv_match1.setGravity(Gravity.CENTER_HORIZONTAL);
            tv_match2.setGravity(Gravity.CENTER_HORIZONTAL);
            if (common_matches[i].player1.equals(player1) | common_matches[i].player2.equals(player1)) {
                tv_match1.setText(common_matches[i].points1 + "");
                tv_match2.setText(common_matches[i].points2 + "");
                if (common_matches[i].points1 > common_matches[i].points2) {
                    tv_match1.setTextColor(Color.GREEN);
                    tv_match2.setTextColor(Color.RED);
                } else {
                    tv_match2.setTextColor(Color.GREEN);
                    tv_match1.setTextColor(Color.RED);
                }

            } else {
                tv_match1.setText(common_matches[i].points2 + "");
                tv_match2.setText(common_matches[i].points1 + "");
                if (common_matches[i].points1 > common_matches[i].points2) {
                    tv_match2.setTextColor(Color.GREEN);
                    tv_match1.setTextColor(Color.RED);
                } else {
                    tv_match1.setTextColor(Color.GREEN);
                    tv_match2.setTextColor(Color.RED);
                }
            }

            ll_team1_matches.addView(tv_match1);
            ll_team2_matches.addView(tv_match2);
        }

    }

    public void getCommonMatches() {
        final int[] a = {0};
        if (s_team1.matches == null | s_team2.matches == null) {
            return;
        }
        common_matches = new StructMatch[s_team1.matches.size() + s_team2.matches.size()];
        int i = -1;
        for (String match1 : s_team1.matches) {
            for (String match2 : s_team2.matches) {
                if (match1.equals(match2)) {
                    i++;
                    final int finalI = i;
                    db.collection("matches").document(match2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            a[0]++;
                            common_matches[finalI] = documentSnapshot.toObject(StructMatch.class);
                            if (a[0] == no_common_matches) {
                                match.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(match.this, "Got History", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
        no_common_matches = i;
    }

    @SuppressLint("SetTextI18n")
    public void setData(StructPlayer team, View mView) {
        final TextView tv_score, tv_won, tv_matches, tv_lost, tv_points_scored, tv_conceded;
        tv_score = mView.findViewById(R.id.ds_score);
        tv_score.setText(team.score + "");
        tv_won = mView.findViewById(R.id.ds_won);
        tv_won.setText(team.won_matches + "");
        tv_matches = mView.findViewById(R.id.ds_matches);
        tv_matches.setText(team.played_matches + "");
        tv_lost = mView.findViewById(R.id.ds_lost);
        tv_lost.setText(team.lost_matches + "");
        tv_points_scored = mView.findViewById(R.id.ds_points_scored);
        tv_points_scored.setText(team.points_scored + "");
        tv_conceded = mView.findViewById(R.id.ds_points_conceded);
        tv_conceded.setText(team.points_conceded + "");
    }
}
