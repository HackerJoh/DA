package com.example.wst_app;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    static Spinner spinner;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    NumberPicker np;

    private static int max=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("wst", 0);

        spinner = findViewById(R.id.subject);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, android.R.layout.simple_dropdown_item_1line);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(preferences.getInt("subject", 0));

        np = findViewById(R.id.questionAnz);
        np.setMinValue(1);
        np.setMaxValue(20);
        np.setWrapSelectorWheel(true);
        np.setValue(preferences.getInt("anzQuestions", 10));

        TextView textView = findViewById(R.id.impressum);
        SpannableString content = new SpannableString("Impressum");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
    }

    public static String getFile(){
        String tmp="";
        String file;

        try{
            tmp= spinner.getSelectedItem().toString();
        }catch(NullPointerException e){
            file="metalltechnik.csv";
        }

        switch(tmp){
            case "Metalltechnik":
                file="metalltechnik.csv";
                break;
            case "Tischler":
                file="tischler.csv";
                break;
            case "Schweißtechnik":
                file="schweisstechnik.csv";
                break;
            default:
                file="metalltechnik.csv";
        }

        return file;
    }

    public void save(View view){
        editor = preferences.edit();
        editor.putInt("anzQuestions", np.getValue());
        editor.putInt("subject", spinner.getSelectedItemPosition());
        editor.apply();
        max=np.getValue();
        Log.d("Test","max set: "+max);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public static int getMax(){
        return max;
    }

    public void impressum(View view) {
        Intent intent = new Intent(this, ImpressumActivity.class);
        startActivity(intent);
    }
}
