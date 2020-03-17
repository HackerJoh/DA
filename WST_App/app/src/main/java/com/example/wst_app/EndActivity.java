package com.example.wst_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.app.progresviews.ProgressLine;

public class EndActivity extends AppCompatActivity {
    int rightAnswers = 0;
    int prüfAnz = 0;

    private ProgressLine endStats;

    TextView greets;
    ImageView smiley;
    CardView end;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        endStats = findViewById(R.id.endProg);
        preferences = getSharedPreferences("wst", 0);

        //Die extras werden über den Intent geholt
        Bundle extras = getIntent().getExtras();
        //wenn die extras nicht Null sind, werden die richtigen Fragen gesetzt
        if (extras != null) {
            rightAnswers = extras.getInt("rightAnswers");
        }

        int anzQuestions = preferences.getInt("anzQuestions", 10);
        greets = findViewById(R.id.greets);
        smiley = findViewById(R.id.imageEnd);
        end = findViewById(R.id.endPoints);

        //hier wird der Prozentsatz, den man bei eienr Prüfung erreichen würde angezeigt
        int answers = rightAnswers;
        int proz = (answers*100)/anzQuestions;        //TODO: Prozent und Anzahl der Fragen richtig auswerten

        int tmp = preferences.getInt("maxProzent", 0);
        if(tmp<proz){
            editor.putInt("maxProzent", proz);
        }

        prüfAnz = preferences.getInt("PrüfAnz", 0);
        prüfAnz++;
        editor.putInt("PrüfAnz", prüfAnz);

        editor.apply();

        Log.d("Test","proz "+proz);

        if(proz < 40){
            smiley.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp);
            greets.setText("Da ist noch Luft nach oben!");
            end.setCardBackgroundColor(Color.parseColor("#ff3300"));
        }else if(proz < 80){
            smiley.setImageResource(R.drawable.ic_sentiment_neutral_black_24dp);
            greets.setText("Da geht noch mehr!");
            end.setCardBackgroundColor(Color.parseColor("#ff6600"));
        }else{
            smiley.setImageResource(R.drawable.ic_tag_faces_black_24dp);
            greets.setText("Sehr gut! Weiter so!");
            end.setCardBackgroundColor(Color.parseColor("#9ACD32"));
        }
        endStats.setmValueText(answers);
        endStats.setmPercentage(proz);
    }

    public void toCategory(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
    }
}
