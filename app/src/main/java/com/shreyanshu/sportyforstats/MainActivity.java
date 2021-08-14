package com.shreyanshu.sportyforstats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    ImageView img_1, img_2, img_3, img_4;
    LinearLayout ll_one, ll_two, ll_three, ll_four;
    Button[] b_players = new Button[28];
    int selected_one = 0, selected_two = 0;
    int img_height;
    String player1 = "", player2 = "", player3 = "", player4 = "";
    boolean doubleBackToExitPressedOnce = false, doubleFinishToFinish = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onStop();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(R.id.am_all_layout), "Please click BACK again to exit", Snackbar.LENGTH_LONG)
                .show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img_height = findViewById(R.id.am_linear_layout).getMeasuredHeight();
        Log.d("TAG", "onCreate: " + img_height);
        img_1 = findViewById(R.id.am_img1);
        img_2 = findViewById(R.id.am_img2);
        img_3 = findViewById(R.id.am_img3);
        img_4 = findViewById(R.id.am_img4);
        ll_one = findViewById(R.id.am_one);
        ll_two = findViewById(R.id.am_two);
        ll_three = findViewById(R.id.am_three);
        ll_four = findViewById(R.id.am_four);
        int k = 0;
        for (int i = 1; i < 8; i++) {
            b_players[k] = (Button) ll_one.getChildAt(i);
            if (i > 4) b_players[k].setVisibility(View.GONE);
            k++;
        }
        for (int i = 1; i < 8; i++) {
            b_players[k] = (Button) ll_two.getChildAt(i);
            if (i < 5) b_players[k].setVisibility(View.GONE);
            k++;
        }
        for (int i = 1; i < 8; i++) {
            b_players[k] = (Button) ll_three.getChildAt(i);
            if (i > 4) b_players[k].setVisibility(View.GONE);
            k++;
        }
        for (int i = 1; i < 8; i++) {
            b_players[k] = (Button) ll_four.getChildAt(i);
            if (i < 5) b_players[k].setVisibility(View.GONE);
            k++;
        }
    }

    public void onPlayerSelect(View view) {
        LinearLayout ll_layout = (LinearLayout) view.getParent();
        ImageView iv_img = (ImageView) (ll_layout.getChildAt(0));
        String player_name = ((Button) view).getText().toString();
        iv_img.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, getImage(player_name)));
        iv_img.setVisibility(View.VISIBLE);
        iv_img.setMaxHeight(img_height);

        Log.d("TAG", "onCreate: " + iv_img);
        for (int i = 1; i < 8; i++) {
            ll_layout.getChildAt(i).setVisibility(View.GONE);
        }
        if (ll_layout.getId() == R.id.am_one) {
            player1 = player_name;
            selected_one++;
            if (selected_one == 1) {
                for (int i = 1; i < 8; i++) {
                    ll_two.getChildAt(i).setVisibility(View.VISIBLE);
                }
            }
        } else if (ll_layout.getId() == R.id.am_two) {
            player2 = player_name;
            selected_one++;
            if (selected_one == 1) {
                for (int i = 1; i < 8; i++) {
                    ll_one.getChildAt(i).setVisibility(View.VISIBLE);
                }
            }
        } else if (ll_layout.getId() == R.id.am_three) {
            player3 = player_name;
            selected_two++;
            if (selected_two == 1) {
                for (int i = 1; i < 8; i++) {
                    ll_four.getChildAt(i).setVisibility(View.VISIBLE);
                }
            }
        } else {
            player4 = player_name;
            selected_two++;
            if (selected_two == 1) {
                for (int i = 1; i < 8; i++) {
                    ll_three.getChildAt(i).setVisibility(View.VISIBLE);
                }
            }
        }
        for (int i = 0; i < 28; i++) {
            String name = b_players[i].getText().toString();
            if (name.equals(player1) | name.equals(player2) | name.equals(player3) | name.equals(player4))
                b_players[i].setVisibility(View.GONE);
        }
    }


    public int getImage(String player_name) {
        //Log.d("TAG", "player_name: "+player_name);
        if (player_name.equals("Honey")) return (R.drawable.img_honey);
        if (player_name.equals("Deepanshu")) return (R.drawable.img_dippu);
        if (player_name.equals("Himanshu")) return (R.drawable.img_manu);
        if (player_name.equals("Mayank")) return (R.drawable.img_mayank);
        if (player_name.equals("Bunty")) return (R.drawable.img_salil);
        if (player_name.equals("Saumya")) return (R.drawable.img_saumya);
        if (player_name.equals("Tanu")) return (R.drawable.img_tanu);
        return R.drawable.ic_launcher_background;
    }

    public void onPlayerDeselect(View view) {
        view.setVisibility(View.GONE);
        if (view.getId() == R.id.am_img1) player1 = "";
        if (view.getId() == R.id.am_img2) player2 = "";
        if (view.getId() == R.id.am_img3) player3 = "";
        if (view.getId() == R.id.am_img4) player4 = "";

        for (int i = 0; i < 28; i++) {
            String name = b_players[i].getText().toString();
            if (!(name.equals(player1) | name.equals(player2) | name.equals(player3) | name.equals(player4)))
                b_players[i].setVisibility(View.VISIBLE);
        }
    }

    public void onMatchStart(View view) {
        if ((player1.equals("") & player2.equals(""))) {
            if (player3.equals("") & player4.equals("")) {
                Snackbar.make(findViewById(R.id.am_all_layout), "Select Players to start game", Snackbar.LENGTH_LONG)
                        .show();
            } else
                Snackbar.make(findViewById(R.id.am_all_layout), "You need Opponent to play", Snackbar.LENGTH_LONG)
                        .show();
        } else if (player3.equals("") & player4.equals("")) {
            Snackbar.make(findViewById(R.id.am_all_layout), "You need Opponent to play", Snackbar.LENGTH_LONG)
                    .show();
        } else {
            startActivity(new Intent(this, match.class)
                    .putExtra("player1", player1)
                    .putExtra("player2", player2)
                    .putExtra("player3", player3)
                    .putExtra("player4", player4));
        }
    }

    public void viewResult(View view) {
        startActivity(new Intent(MainActivity.this, result.class).putExtra("team1", "").putExtra("team2", ""));
    }
}