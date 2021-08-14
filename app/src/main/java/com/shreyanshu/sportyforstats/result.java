package com.shreyanshu.sportyforstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class result extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }

    LinearLayout ll_layout;
    boolean b_score = true, b_won = false, b_matches = false, b_lost = false, b_points_scored = false, b_conceded = false, b_doubles = false;
    //String[] a_score, a_won, a_matches, a_lost, a_pointsScored, a_conceded;
    StructPlayer[] s_teams;
    FirebaseFirestore db;
    Animation animation;
    int got_matches = 0;
    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = FirebaseFirestore.getInstance();
        final String t1 = getIntent().getStringExtra("team1");
        final String t2 = getIntent().getStringExtra("team2");
        final float score1 = getIntent().getFloatExtra("score1", 0);
        Log.d("TAG", "onCreate: " + score1);
        final float score2 = getIntent().getFloatExtra("score2", 0);
        String team_collection;
        ll_layout = findViewById(R.id.ar_layout);
        ll_layout.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Toast.makeText(result.this, "ACTION DOWN left2right swipe", Toast.LENGTH_SHORT).show ();
                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(result.this, "ACTION DOWN left2right swipe", Toast.LENGTH_SHORT).show ();
                        x1 = motionEvent.getX();
                        break;
                    case MotionEvent.ACTION_UP:

                        Toast.makeText(result.this, "ACTION UP left2right swipe", Toast.LENGTH_SHORT).show ();
                        x2 = motionEvent.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE)
                        {
                            Toast.makeText(result.this, "left2right swipe", Toast.LENGTH_SHORT).show ();
                        }
                        else
                        {
                            // consider as something else - a screen tap for example
                        }
                        break;
                }
                return result.super.onTouchEvent(motionEvent);
            }
        });
        b_doubles = getIntent().getBooleanExtra("doubles", true);
        if (b_doubles) team_collection = "d_teams";
        else team_collection = "players";
        animation = AnimationUtils.loadAnimation(result.this, R.anim.bounce);
        db.collection(team_collection).orderBy("score", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                DecimalFormat df = new DecimalFormat("00.00");
                ll_layout.startAnimation(animation);
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                int n = documents.size();
                ll_layout.addView(new TransactionView(result.this, "Team Name", "Score", 0, 0, "Won", "Matches", "Lost", "Scored", "Conceded"));
                s_teams = new StructPlayer[n + 1];
                int i = 1;
                for (DocumentSnapshot document : documents) {
                    String teamName = document.getId();
                    s_teams[i] = document.toObject(StructPlayer.class);
                    int this_game = 0;
                    assert s_teams[i] != null;
                    if (teamName.equals(t1)) {
                        if (score1 > 0) {
                            int k = i;
                            float score_now = s_teams[i].score;
                            try {
                                while (Float.parseFloat(documents.get(k).get("score").toString()) + score1 > score_now) {
                                    k++;
                                }
                            } catch (Exception e) {// Do Nothing}
                            }
                            this_game = i - k;
                        } else {
                            int k = i;
                            float score_now = s_teams[i].score;
                            try {
                                while (s_teams[k].score + score1 > score_now) {
                                    k--;
                                }
                            } catch (Exception e) {// Do Nothing}
                            }
                            this_game = i - k;
                        }
                    }
                    if (teamName.equals(t2)) {
                        if (score2 > 0) {
                            int k = i;
                            float score_now = s_teams[i].score;
                            try {
                                while (Float.parseFloat(documents.get(k).get("score").toString()) + score2 > score_now) {
                                    k++;
                                }
                            } catch (Exception e) {// Do Nothing
                            }
                            this_game = i - k;
                        } else {
                            int k = i;
                            float score_now = s_teams[i].score;
                            try {
                                while (s_teams[k].score + score2 < score_now) {
                                    k--;
                                }
                            } catch (Exception e) {// Do Nothing}
                            }
                            this_game = i - k;
                        }
                    }
                    assert s_teams[i] != null;

                    String matches;
                    try {
                        matches = s_teams[i].matches.size() + "";
                    } catch (NullPointerException nullPointerException) {
                        matches = "0";
                    }
                    String tName;
                    if (b_doubles)
                        tName = teamName.split(",")[0] + "," + "\n" + teamName.split(",")[1];
                    else tName = teamName;
                    LinearLayout ll_now = new TransactionView(result.this, tName, df.format(s_teams[i].score), i, this_game, "" + s_teams[i].won_matches, matches, s_teams[i].lost_matches + "", s_teams[i].points_scored + "", s_teams[i].points_conceded + "");
                    ll_layout.addView(ll_now);
                    ll_now.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            result.this.seeDetail(view);
                        }
                    });
                    i++;
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    public void seeDetail(View view) {
        final int index = ll_layout.indexOfChild(view);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View mView = result.this.getLayoutInflater().inflate(R.layout.detail_score_dialog, null);
        alert.setView(mView);
        final TextView tv_score, tv_won, tv_matches, tv_lost, tv_points_scored, tv_conceded;
        tv_score = mView.findViewById(R.id.ds_score);
        tv_score.setText(s_teams[index].score + "");
        if (b_score)
            mView.findViewById(R.id.ds_LScore).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        tv_won = mView.findViewById(R.id.ds_won);
        tv_won.setText(s_teams[index].won_matches + "");
        if (b_won)
            mView.findViewById(R.id.ds_LWon).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        tv_matches = mView.findViewById(R.id.ds_matches);
        tv_matches.setText(s_teams[index].matches.size() + "");
        if (b_matches)
            mView.findViewById(R.id.ds_LMatches).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        tv_lost = mView.findViewById(R.id.ds_lost);
        tv_lost.setText(s_teams[index].lost_matches + "");
        if (b_lost)
            mView.findViewById(R.id.ds_LLost).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        tv_points_scored = mView.findViewById(R.id.ds_points_scored);
        tv_points_scored.setText(s_teams[index].points_scored + "");
        if (b_points_scored)
            mView.findViewById(R.id.ds_LPointsScored).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        tv_conceded = mView.findViewById(R.id.ds_points_conceded);
        tv_conceded.setText(s_teams[index].points_conceded + "");
        if (b_conceded)
            mView.findViewById(R.id.ds_LConceded).setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
        //final Context context = this;

        mView.findViewById(R.id.ds_LScore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_score) {
                    result.this.b_score = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_score = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        mView.findViewById(R.id.ds_LWon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_won) {
                    result.this.b_won = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_won = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        mView.findViewById(R.id.ds_LMatches).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_matches) {
                    result.this.b_matches = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_matches = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        mView.findViewById(R.id.ds_LLost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_lost) {
                    result.this.b_lost = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_lost = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        mView.findViewById(R.id.ds_LPointsScored).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_points_scored) {
                    result.this.b_points_scored = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_points_scored = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        mView.findViewById(R.id.ds_LConceded).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result.this.b_conceded) {
                    result.this.b_conceded = false;
                    view.setBackgroundResource(R.drawable.white_backgound);
                } else {
                    result.this.b_conceded = true;
                    view.setBackground(ContextCompat.getDrawable(result.this, R.drawable.list_item_back));
                }
            }
        });
        final AlertDialog alertDialog = alert.create();

        Button button_matches = mView.findViewById(R.id.ds_b_matches);
        button_matches.setText("All Matches");
        button_matches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                LinearLayout layout = (LinearLayout) ll_layout.getChildAt(index);
                get_matches(((TextView) layout.getChildAt(1)).getText().toString(), s_teams[index]);
            }
        });
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int n = ll_layout.getChildCount();
                for (int i = 0; i < n; i++) {
                    LinearLayout layout = (LinearLayout) ll_layout.getChildAt(i);
                    if (b_score)
                        layout.getChildAt(2).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(2).setVisibility(View.GONE);
                    if (b_won)
                        layout.getChildAt(3).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(3).setVisibility(View.GONE);
                    if (b_matches)
                        layout.getChildAt(4).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(4).setVisibility(View.GONE);
                    if (b_lost)
                        layout.getChildAt(5).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(5).setVisibility(View.GONE);
                    if (b_points_scored)
                        layout.getChildAt(6).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(6).setVisibility(View.GONE);
                    if (b_conceded)
                        layout.getChildAt(7).setVisibility(View.VISIBLE);
                    else
                        layout.getChildAt(7).setVisibility(View.GONE);

                }
            }
        });
        alertDialog.show();
    }

    public void get_matches(final String teamName, StructPlayer s_team1) {
        got_matches = 0;
        final StructMatch[] matches = new StructMatch[s_team1.matches.size()];
        /*if (s_team1.matches.size() > 10) {
            Toast.makeText(result.this, "matches more than 10\n try enquiring team with matches less than 10", Toast.LENGTH_SHORT).show();
            return;
        }*/
        boolean go = false;
        final int size = s_team1.matches.size();
        for (int i = 10; true; i += 10) {
            int start = i - 10;
            if (i >= s_team1.matches.size()) {
                i = s_team1.matches.size();
                go = true;
            }
            db.collection("matches").whereIn(FieldPath.documentId(), s_team1.matches.subList(start, i)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        matches[got_matches] = documentSnapshot.toObject(StructMatch.class);
                        got_matches++;
                    }
                    if (matches[size - 1] != null)
                        show_matches_dialog(teamName, matches);
                }
            });
            if (go) break;
        }
    }

    public void show_matches_dialog(String teamName, StructMatch[] matches) {
        got_matches = 0;
        String player1 = teamName.split(",\n")[0];
        String player2="";
        if (b_doubles) player2 = teamName.split(",\n")[1];

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View mView = result.this.getLayoutInflater().inflate(R.layout.previous_matches_dialog, null);
        alert.setView(mView);
        ((TextView) mView.findViewById(R.id.pm_teamName)).setText(teamName);
        LinearLayout ll_matches = mView.findViewById(R.id.pm_layout);
        for (StructMatch match : matches) {
            if (match.player1.equals(player1) | match.player1.equals(player2)) {
                String opponent = match.player3 + ",\n" + match.player4;
                String score = match.points2 + " - " + match.points1;
                if (match.points1 > match.points2) {
                    ll_matches.addView(new team_match(result.this, opponent, score, true));
                } else {
                    ll_matches.addView(new team_match(result.this, opponent, score, false));
                }
            } else {
                String opponent = match.player1 + ",\n" + match.player2;
                String score = match.points1 + " - " + match.points2;
                if (match.points2 > match.points1) {
                    ll_matches.addView(new team_match(result.this, opponent, score, true));
                } else {
                    ll_matches.addView(new team_match(result.this, opponent, score, false));
                }
            }
        }
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

}

class team_match extends LinearLayout {

    public team_match(Context context) {
        super(context);
    }

    public team_match(Context context, String opponent, String score, boolean won) {
        super(context);
        TextView tv_opponent, tv_score;
        LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        tv_opponent = new TextView(context);
        tv_opponent.setPadding(15, 25, 15, 25);
        tv_opponent.setLayoutParams(labelLayoutParams);
        tv_opponent.setText(opponent);
        tv_opponent.setTextSize(24);
        tv_opponent.setTextColor(Color.BLACK);
        addView(tv_opponent);

        tv_score = new TextView(context);
        tv_score.setPadding(15, 25, 15, 25);
        tv_score.setLayoutParams(labelLayoutParams);
        tv_score.setTextSize(36);
        tv_score.setText(score);
        tv_score.setGravity(Gravity.CENTER_HORIZONTAL);
        if (won) tv_score.setTextColor(Color.GREEN);
        else
            tv_score.setTextColor(Color.BLACK);
        addView(tv_score);

    }
}

class TransactionView extends LinearLayout {

    Context context;

    public TransactionView(Context context) {
        super(context);
    }

    @SuppressLint("SetTextI18n")
    public TransactionView(Context context, String name, String points, int i, int this_game, String won, String matches, String lost, String points_scored, String conceded) {
        super(context);
        this.context = context;
        LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams labelLayoutParams1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        LinearLayout.LayoutParams labelLayoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        TextView tv_name, tv_score, tv_this, tv_won, tv_matches, tv_lost, tv_points_scored, tv_conceded;

        tv_this = new TextView(context);
        tv_this.setPadding(15, 25, 15, 25);
        tv_this.setLayoutParams(labelLayoutParams1);
        if (this_game == 0) {
            tv_this.setText(i + "");
            tv_this.setTextColor(Color.BLACK);
        } else {
            if (this_game < 0) {
                tv_this.setTextColor(Color.RED);
                tv_this.setText("" + this_game);
            } else {
                tv_this.setTextColor(Color.GREEN);
                tv_this.setText("+" + this_game);
            }
        }
        addView(tv_this);

        tv_name = new TextView(context);
        tv_name.setText(name);
        tv_name.setPadding(15, 25, 15, 25);
        tv_name.setLayoutParams(labelLayoutParams2);
        tv_name.setTextColor(Color.BLACK);
        addView(tv_name);

        tv_score = new TextView(context);
        tv_score.setText(points);
        tv_score.setPadding(15, 25, 15, 25);
        tv_score.setLayoutParams(labelLayoutParams);
        tv_score.setTextColor(Color.BLACK);
        addView(tv_score);


        tv_won = new TextView(context);
        tv_won.setText(won);
        tv_won.setPadding(15, 25, 15, 25);
        tv_won.setLayoutParams(labelLayoutParams);
        tv_won.setTextColor(Color.BLACK);
        addView(tv_won);
        tv_won.setVisibility(GONE);

        tv_matches = new TextView(context);
        tv_matches.setText(matches);
        tv_matches.setPadding(15, 25, 15, 25);
        tv_matches.setLayoutParams(labelLayoutParams);
        tv_matches.setTextColor(Color.BLACK);
        addView(tv_matches);
        tv_matches.setVisibility(GONE);

        tv_lost = new TextView(context);
        tv_lost.setText(lost);
        tv_lost.setPadding(15, 25, 15, 25);
        tv_lost.setLayoutParams(labelLayoutParams);
        tv_lost.setTextColor(Color.BLACK);
        addView(tv_lost);
        tv_lost.setVisibility(GONE);

        tv_points_scored = new TextView(context);
        tv_points_scored.setText(points_scored);
        tv_points_scored.setPadding(15, 25, 15, 25);
        tv_points_scored.setLayoutParams(labelLayoutParams);
        tv_points_scored.setTextColor(Color.BLACK);
        addView(tv_points_scored);
        tv_points_scored.setVisibility(GONE);

        tv_conceded = new TextView(context);
        tv_conceded.setText(conceded);
        tv_conceded.setPadding(15, 25, 15, 25);
        tv_conceded.setLayoutParams(labelLayoutParams);
        tv_conceded.setTextColor(Color.BLACK);
        addView(tv_conceded);
        tv_conceded.setVisibility(GONE);
    }
}