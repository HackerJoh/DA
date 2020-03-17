package com.example.wst_app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.progresviews.ProgressLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CategoryActivity extends AppCompatActivity {
    public static boolean permission;

    private ProgressLine anfanger;
    private ProgressLine fortgeschritten;
    private ProgressLine profi;

    private HashSet<String> fragenKat1 = new HashSet<>();
    private  HashSet<String> fragenKat2 = new HashSet<>();
    private  HashSet<String> fragenKat3 = new HashSet<>();

    private static HashMap<String, ArrayList<String>> fragenMap = new HashMap<>();

    private static boolean alreadyConnected=false;

    public static float maxCount=0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_category);

        anfanger = findViewById(R.id.beginnerProg);
        fortgeschritten = findViewById(R.id.fortgeschrittenProg);
        profi = findViewById(R.id.profiProg);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        if (isOnline(this) && !alreadyConnected) {
            getQuestions();
            alreadyConnected=true;
        }

        checkKat();

        int anfProg = GameTraiActivity.getKat1Size();
        int anfProgPerc = (int) (GameTraiActivity.getKat1Size()/maxCount*100);
        int fortProg = GameTraiActivity.getKat2Size();
        int fortProgPerc = (int) (GameTraiActivity.getKat2Size()/maxCount*100);
        int profiProg = GameTraiActivity.getKat3Size();
        int profiProgPerc = (int) (GameTraiActivity.getKat3Size()/maxCount*100);
        maxCount = GameTraiActivity.counterMax;

        Log.d("Test","anfProg "+anfProg);
        Log.d("Test","fortProg "+fortProg);
        Log.d("Test","profiProg "+profiProg);

        anfanger.setmValueText(anfProg);
        anfanger.setmPercentage(anfProgPerc);
        fortgeschritten.setmValueText(fortProg);
        fortgeschritten.setmPercentage(fortProgPerc);
        profi.setmValueText(profiProg);
        profi.setmPercentage(profiProgPerc);
        //Rechte werden eingefordert, wenn nicht schon vorhanden
        getPermissions();
    }
    public void gameExam(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
    public void gameTrai(View view) {
        Intent intent = new Intent(this, GameTraiActivity.class);
        startActivity(intent);
    }
    public void description(View view){
        showPopup(CategoryActivity.this);
    }
    private void showPopup(final Activity context) {
        RelativeLayout viewGroup = context.findViewById(R.id.rl_custom_layout);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.activity_description, viewGroup);
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(900);
        popup.setHeight(600);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable(context.getResources(), ""));
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
        ImageButton close = layout.findViewById(R.id.ib_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public void startGame(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }

    public void getPermissions(){
        //Diese Anfrage ist erst ab SDK Version 23 enthalten, ansonsten wird dies über das Manifest erstellt
        if (Build.VERSION.SDK_INT >= 23) {
            //Benötigt wird das Recht zum Schreiben in eine Datei, welche am externen Storage liegt
            int accessCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //Der benutzer wird gefragt, ob man sich die Rechte "nehmen" darf
            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED) {
                // Die Anazeige für die Abfrage wird erstellt
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};

                // Dialog zum bestätigen der Rechte wird erstellt
                ActivityCompat.requestPermissions(this, permissions, 1);
                permission= true;
            }
        }
    }

    public static boolean isOnline(Context ctx) {
        //Es wird der ConnectionManager vom System abgeholt (zuständig für jeglichen Internetverkehr)
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        //die aktuelle Netzwerkinformation wird abgeholt
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        //Wenn das Internet eingeschalten ist und man eine Verbindung zum Internet hat
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getQuestions(){
        //Temporäres String-Array
        String line[];
        //ServerConnection (Klasse) wird erzeugt
        ServerConnection srv = new ServerConnection();
        //Filename wird gesetzt
        srv.setFilename(SettingsActivity.getFile());
        //AsyncTask wird gestartet und im Hintergrund abgearbeitet
        //Dieser Task wird nur für die gegebene URL verwendet
        //Zurückgegeben werden die Fragen mit den Antworten in eine ArrayList
        ArrayList<String[]> list = (ArrayList<String[]>) srv.doInBackground("https://www.docker.htl-wels.at/quiz/metalltechnik.csv");
        //Temporäre Antwortliste
        ArrayList<String> antworten = null;

        for (int i = 0; i < list.size(); i++) {
            //Frage/Antwort an der Stelle "i" in der ArrayList wird geholt
            line = list.get(i);
            //Antwortliste wird von der Map (Frage zu Antwort) geholt
            antworten = fragenMap.get(line[0]);
            //Wenn die ArrayList mit Antworten nicht existiert wird eine neue Erstellt
            if (antworten == null) {
                antworten = new ArrayList<>();
            }
            //Ansonsten wird mit der zurückgegebenen Liste gearbeitet

            //Fragen wird ausgelesen und die jeweiligen Antworten (mit Richtigkeit) in die Antwortliste hinzugefügt
            String frage = line[0];
            antworten.add(line[1]);
            antworten.add(line[2]);
            antworten.add(line[3]);
            antworten.add(line[4]);
            antworten.add(line[5]);
            antworten.add(line[6]);
            antworten.add(line[7]);
            antworten.add(line[8]);

            //Fragen und Antworten werden in die Mapp hinzugefügt
            fragenMap.put(frage, antworten);
        }

        Log.d("SizeList","map: "+fragenMap.size());
    }
    public void checkKat() {
        //TODO: context statt environment
        //Verzeichnisstruktur wird erzeugt
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath = baseDir + File.separator + "wstApp" + File.separator;
        File f = new File(filePath);

        //Wenn diese Struktur existiert, werden die Kategorien aus den CSV-Dateien geladen
        if (f.exists()) {
            BufferedReader readerKat1 = null;
            BufferedReader readerKat2 = null;
            BufferedReader readerKat3 = null;
//            String fileKat1 = "fragenKat1.csv";
////            String fileKat2 = "fragenKat2.csv";
////            String fileKat3 = "fragenKat3.csv";
            String fileKat1 = "metalltechnik.csv";
            String fileKat2 = "metalltechnik.csv";
            String fileKat3 = "metalltechnik.csv";
            try {
                readerKat1 = new BufferedReader(new InputStreamReader(new FileInputStream(filePath + File.separator + fileKat1)));
                readerKat2 = new BufferedReader(new InputStreamReader(new FileInputStream(filePath + File.separator + fileKat2)));
                readerKat3 = new BufferedReader(new InputStreamReader(new FileInputStream(filePath + File.separator + fileKat3)));

                String line="";
                while ((line= readerKat1.readLine()) != null) {
                    fragenKat1.add(line);
                }
                line="";
                while ((line= readerKat2.readLine()) != null) {
                    fragenKat2.add(line);
                }
                line="";
                while ((line= readerKat3.readLine()) != null) {
                    fragenKat3.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//            kat1Size=fragenKat1.size();
//            kat2Size=fragenKat2.size();
//            kat3Size=fragenKat3.size();
        }
    }
    public static HashMap<String, ArrayList<String>> retQuestions(){
        return fragenMap;
    }
}
