package com.example.wst_app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    //Maximale Anzahl von fragen (wird in getQuestions() gesetzt)
    public static int counterMax = 0;
    //Collection für die 3 Kategorien
    public static HashSet<String> fragenKat1 = new HashSet<>();
    public static HashSet<String> fragenKat2 = new HashSet<>();
    public static HashSet<String> fragenKat3 = new HashSet<>();
    //Frage, die zur überprüfung der Fragen in den Kategorien benötigt wird (wird in setQuestion() gesetzt)
    public String frageZuKat;
    //wieviele Runden gespielt wurden
    private int  counter = 0;
    //wieviele Antworten richtig waren
    private int rightAnswers = 0;

    //private boolean alreadyConnected=false;
    //Liste der 4 Antwort-Button
    ArrayList<Button> answers = new ArrayList<>();
    //Button für die nächste Frage
    Button nextQuestion;
    //Button für das Beenden des Trainings
    Button endGame;
    //Permission zum schreiben
    boolean permission = false;
    //Map mit der Frage (String) als Schlüssel und Antworten (ArrayList<String> als Werte
    static HashMap<String, ArrayList<String>> fragenMap = new HashMap<>();
    //Für die zufällige Reihenfolge (wird am Beginn des Trainings erstellt)
    static ArrayList<Integer> reihenfolgeFragen = new ArrayList<Integer>();
    //Fragen in einer Liste (wird als Schlüssel für die Map verwendet)
    static ArrayList<String> fragenListe = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Permissions werden aus der Start-Activity gesetzt
        permission = CategoryActivity.permission;
        //Intent wird geholt, um mit den gegebenen Extras (Bundle) zu arbeiten
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //Wenn der User online ist und der Internetzugriff genehmigt ist, holt man sich die Fragen

        Log.d("SizeList",""+fragenMap.size());

        //lokal gespeicherte Kategorien werden geladen

        fragenMap = CategoryActivity.retQuestions();
        Log.d("SizeList",""+fragenMap.size());
        getQuestions();

        if (fragenKat1.size() == 0) {
            //wenn noch keine extras existieren und noch keine lokale Datei vorhanden ist, werden alle Fragen in die
            //Kategorie 1 geschoben
            fragenKat1 = new HashSet<String>(fragenListe);
        }

        //Antwortbuttons werden gesetzt und zur Liste hinzugefügt
        Button button1 = (Button) findViewById(R.id.answer1);
        Button button2 = (Button) findViewById(R.id.answer2);
        Button button3 = (Button) findViewById(R.id.answer3);
        Button button4 = (Button) findViewById(R.id.answer4);
        answers.addAll(Arrays.asList(button1, button2, button3, button4));

        //"Weiter"-Button wird gesetzt
        nextQuestion = (Button) findViewById(R.id.skipBtn);

        //"Ende"-Button wird gesetzt
        endGame = findViewById(R.id.exitBtn);

        startGame();
    }
    public static void getQuestions() {

        Log.d("SizeList","map: "+fragenMap.size());
        for (Map.Entry<String, ArrayList<String>> entry : fragenMap.entrySet()) {
            String k = entry.getKey();
            fragenListe.add(k);
        }

        //Die Anzahl der maximalen Runden (Training alle Fragen) wird gesetzt
        if(( counterMax = SettingsActivity.getMax())==0){
            counterMax=10;
        }

        int i = 0;
        //Solange i kleiner der maximalen Anzahl der Runden ist
        while (i < counterMax) {
            //Wenn noch kein Eintrag in der Reihenfolgeliste ist, wird die zufällige Zahl direkt hinzugefügt
            if (reihenfolgeFragen.size() == 0) {
                int rand = (int) (Math.random() * fragenMap.size());
                reihenfolgeFragen.add(rand);
                i++;
            } else {
                //Wenn Einträge vorhanden sind, wird eine zufallszahl Erzeugt
                int rand = (int) (Math.random() * fragenMap.size());

                //Solange diese nicht in der Liste vorhanden ist, wird sie hinzugefügt, ansonsten ignoriert
                if (!reihenfolgeFragen.contains(rand)) {
                    reihenfolgeFragen.add(rand);
                    i++;
                }
            }
        }
    }


    public void startGame() {

        //Textviews (Frage und  Frage x/Fragen Max) werden initialisiert
        TextView headline = (TextView) findViewById(R.id.headline);
        TextView counterView = (TextView) findViewById(R.id.questionCntr);

        //Der counter wird erhöht (damit man bei 1 anfängt zu zählen
        counter++;

        //Text in der Überschrift wird gesetzt
        headline.setText(counter + ". Frage");

        //Text in Frage x / Frage max wird gesetzt
        counterView.setText("" + counter + "/" + counterMax);

        setQuestion();
    }

    private void setQuestion() {

        //Textview für die Frage wird initialisiert
        TextView question = (TextView) findViewById(R.id.question);

        //Aktuelle Frage wird durch die erste Zahl der zufälligen Reihenfolge-Liste abgeholt
        Log.d("SizeList",""+fragenListe.size());
        String frage = fragenListe.get(reihenfolgeFragen.get(0));  //TODO: Wirft IndexOutOfboundsException

        //Zufällige Zahl wird aus der Liste gelöscht
        reihenfolgeFragen.remove(0);

        //AntwortListe wird mit dem Key (frage) abgeholt
        ArrayList<String> antworten = fragenMap.get(frage);

        //Die Frage wird global gespeichert, um später zu überprüfen ob sich die Frage in Kategorie 1, 2 oder 3 befindet
        frageZuKat = frage;
        Log.d("SizeList",frageZuKat);

        //Antworten werden geholt (Richtigkeiten werden übersprungen)
        String antEins = antworten.get(0);
        String antZwei = antworten.get(2);
        String antDrei = antworten.get(4);
        String antVier = antworten.get(6);

        //Die Frage wird Angezeigt
        question.setText(frage);

        //Eine zufällige Auswahl der AntwortButton wird erzeugt
        ArrayList<Integer> numbers = new ArrayList<>();
        boolean ready = false;
        while (!ready) {
            if (numbers.size() != 4) {
                int answerNumber = (int) (Math.random() * 4);
                if (numbers.size() == 0) {
                    numbers.add(answerNumber);
                } else if (!numbers.contains(answerNumber)) {
                    numbers.add(answerNumber);
                }
            }
            if (numbers.size() == 4)
                ready = true;
        }

        //Es werden die Antwortmöglichkeiten gesetzt
        answers.get(numbers.indexOf(0)).setText(antEins);
        answers.get(numbers.indexOf(1)).setText(antZwei);
        answers.get(numbers.indexOf(2)).setText(antDrei);
        answers.get(numbers.indexOf(3)).setText(antVier);

        //Es wird pro Button ein Tag gesetzt (1=True; 0=false)
        answers.get(numbers.indexOf(0)).setTag(1);
        answers.get(numbers.indexOf(1)).setTag(0);
        answers.get(numbers.indexOf(2)).setTag(0);
        answers.get(numbers.indexOf(3)).setTag(0);
    }

    @Override
    public void onClick(View view) {
        Log.d("ClickTester","oncklick possible"+ view.getId());
        switch (view.getId()) {
            //Wenn AntwortButton 1 (Links oben) gedrückt wird:
            case R.id.answer1:
                Log.d("ClickTester","Anser Possible");
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(0).getTag().toString()) == 1) {
                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(0).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(0).setBackgroundResource(R.drawable.mybuttonred);
                }
                break;
            //Wenn AntwortButton 2 (Rechts oben) gedrückt wird:
            case R.id.answer2:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(1).getTag().toString()) == 1) {
                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(1).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(1).setBackgroundResource(R.drawable.mybuttonred);
                }
                break;
            //Wenn AntwortButton 3 (Links unten) gedrückt wird:
            case R.id.answer3:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(2).getTag().toString()) == 1) {
                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(2).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(2).setBackgroundResource(R.drawable.mybuttonred);
                }
                break;
            //Wenn AntwortButton 4 (rechts unten) gedrückt wird:
            case R.id.answer4:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(3).getTag().toString()) == 1) {
                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(3).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(3).setBackgroundResource(R.drawable.mybuttonred);
                }
                break;
        }

        for (int i = 0; i < answers.size(); i++) {
            //Alle Buttons werden deaktiviert
            answers.get(i).setClickable(false);
            //Die richtige Antwort wird grün hervorgehoben
            if (Integer.parseInt(answers.get(i).getTag().toString()) == 1) {
                final int nr = i;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        answers.get(nr).setBackgroundResource(R.drawable.mybuttongreen);
                        nextQuestion.setEnabled(true);
                    }
                }, 500); //Es gibt eine Verzögerung von 0.5 Sekunden
            }
        }
        //Diese Schleife ist nützlich, wenn man eine falsche Antwort ausgewählt hat (man sieht dadurch die richtige Antwort)

        //Der Button für die nächste Frage wird aktiviert

    }


    public void writeCsvs() throws IOException {

        //TODO: context statt environment
        //Verzeichnisstuktur wird aufgebaut
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath = baseDir + File.separator + "wstApp" + File.separator;
        File f = new File(filePath);

        //wenn das Verzeichnis noch nicht existiert, wird es erzeugt
        if (!f.exists()) {
            f.mkdirs();
        }

        //Dateinamen der Kategorien
        String fileKat1 = "fragenKat1.csv";
        String fileKat2 = "fragenKat2.csv";
        String fileKat3 = "fragenKat3.csv";

        //Entsprechende Datei für jede Kategorie
        File kat1 = new File(filePath + File.separator + fileKat1);
        File kat2 = new File(filePath + File.separator + fileKat2);
        File kat3 = new File(filePath + File.separator + fileKat3);

        //Normaler Writer für Kategorien
        BufferedWriter writerKat1 = new BufferedWriter(new FileWriter(kat1));
        BufferedWriter writerKat2 = new BufferedWriter(new FileWriter(kat2));
        BufferedWriter writerKat3 = new BufferedWriter(new FileWriter(kat3));

        try {
            //Alle Fragen aus Kategorie 1 werden in die Datei geschrieben
            for (String s : fragenKat1) {
                String[] tmp = new String[1];
                tmp[0] = s;
                writerKat1.write(String.valueOf(tmp));
            }

            //Alle Fragen aus Kategorie 2 werden in die Datei geschrieben
            for (String s : fragenKat2) {
                String[] tmp = new String[1];
                tmp[0] = s;
                writerKat2.write(String.valueOf(tmp));
            }

            //Alle Fragen aus Kategorie 3 werden in die Datei geschrieben
            for (String s : fragenKat3) {
                String[] tmp = new String[1];
                tmp[0] = s;
                writerKat3.write(String.valueOf(tmp));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void continueToQuestion(View view) {
        //Wenn der Counter noch nicht die maximale Rundenanzahl erreicht hat ...
        if (counter != counterMax) {

            nextQuestion.setEnabled(false);
            answers.get(0).setBackgroundResource(R.drawable.mybuttonexam);
            answers.get(0).setClickable(true);
            answers.get(1).setBackgroundResource(R.drawable.mybuttonexam);
            answers.get(1).setClickable(true);
            answers.get(2).setBackgroundResource(R.drawable.mybuttonexam);
            answers.get(2).setClickable(true);
            answers.get(3).setBackgroundResource(R.drawable.mybuttonexam);
            answers.get(3).setClickable(true);
            startGame();
        } else {
            try {
                //Wenn die maximale Anzahl der Runden erreicht wurde, werden die Kategorien in die CSV-Dateien geschrieben
                writeCsvs();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Ein Inent wird erzeugt, um die Anazahl der richtigen Fragen weiterzuleiten
            Intent intent = new Intent(GameActivity.this, EndActivity.class);
            intent.putExtra("rightAnswers", rightAnswers);
            startActivity(intent);
        }
    }

}
