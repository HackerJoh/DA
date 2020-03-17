package com.example.wst_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class StatisticActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_statistic);

        SharedPreferences preferences;
        preferences = getSharedPreferences("wst", 0);

        int maxProz = preferences.getInt("maxProzent", 0);
        TextView statsHigh = findViewById(R.id.statsHigh);
        statsHigh.setText(maxProz + "%");

        int avgAnz = preferences.getInt("avgAnz", 20);
        TextView statsAvgAnz = findViewById(R.id.statsAvgAnz);
        statsAvgAnz.setText(avgAnz + "");

        int traiAnz = preferences.getInt("TraiAnz", 0);
        TextView statsTraiAnz = findViewById(R.id.statsTraiAnz);
        statsTraiAnz.setText(traiAnz + "");

        int prüfAnz = preferences.getInt("PrüfAnz", 0);
        TextView statsPrüfAnz = findViewById(R.id.statsPrüfAnz);
        statsPrüfAnz.setText(prüfAnz + "");
    }
    public void backToMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
