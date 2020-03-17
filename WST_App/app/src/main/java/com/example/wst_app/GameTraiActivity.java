package com.example.wst_app;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class GameTraiActivity extends AppCompatActivity implements View.OnClickListener{

    //Maximale Anzahl von fragen (wird in getQuestions() gesetzt)
    public static int counterMax = 0;
    //Collection für die 3 Kategorien
    public  HashSet<String> fragenKat1 = new HashSet<>();
    public  HashSet<String> fragenKat2 = new HashSet<>();
    public  HashSet<String> fragenKat3 = new HashSet<>();
    //Frage, die zur überprüfung der Fragen in den Kategorien benötigt wird (wird in setQuestion() gesetzt)
    public String frageZuKat;
    //wieviele Runden gespielt wurden
    private int  counter = 0;
    //wieviele Antworten richtig waren
    private int rightAnswers = 0;

    private static int kat1Size=0;
    private static int kat2Size=0;
    private static int kat3Size=0;

    int traiAnz = 0;
    int questionGes = 0;
    int durchGes = 0;

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
    private HashMap<String, ArrayList<String>> fragenMap = new HashMap<>();
    //Für die zufällige Reihenfolge (wird am Beginn des Trainings erstellt)
    private ArrayList<Integer> reihenfolgeFragen = new ArrayList<Integer>();
    //Fragen in einer Liste (wird als Schlüssel für die Map verwendet)
    private ArrayList<String> fragenListe = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gametraining);

        //Permissions werden aus der Start-Activity gesetzt
        permission = CategoryActivity.permission;
        //Intent wird geholt, um mit den gegebenen Extras (Bundle) zu arbeiten
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //Wenn der User online ist und der Internetzugriff genehmigt ist, holt man sich die Fragen

        Log.d("SizeList",""+fragenMap.size());

        //lokal gespeicherte Kategorien werden geladen
        checkKat();

        fragenMap = CategoryActivity.retQuestions();
        Log.d("SizeList",""+fragenMap.size());
        getQuestions();
        //Wenn extras existieren:
//        if (extras != null) {
//            //holen wir uns den count (Anzahl gespielter Fragen, rightAnswers (Anzahl der richtigen Fragen
//            //fragenKat1 (Fragen in Kategorie 1), fragenKat2 (Fragen in Kategorie 2) und fragenKat3 (Fragen in Kategorie 3)
//            Log.d("SizeList","bis hier");
//            counter = extras.getInt("counter");
//            rightAnswers = extras.getInt("rightAnswers");
//            fragenKat1 = (HashSet<String>) extras.get("kat1");
//            fragenKat2 = (HashSet<String>) extras.get("kat2");
//            fragenKat3 = (HashSet<String>) extras.get("kat3");
//        } else
        if (fragenKat1.size() == 0) {
            Log.d("Test1","Hier");
            //wenn noch keine extras existieren und noch keine lokale Datei vorhanden ist, werden alle Fragen in die
            //Kategorie 1 geschoben
            fragenKat1 = new HashSet<String>(fragenListe);
            for(String s:fragenKat1){
                Log.d("Test1",s);
            }
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

    public void getQuestions() {

        Log.d("SizeList","map: "+fragenMap.size());
        for (Map.Entry<String, ArrayList<String>> entry : fragenMap.entrySet()) {
            String k = entry.getKey();
            fragenListe.add(k);
        }

        //Die Anzahl der maximalen Runden (Training alle Fragen) wird gesetzt
        counterMax = fragenMap.size();

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
                    //Ist die Frage in Kategorie 1, wird sie ...
                    if (fragenKat1.contains(frageZuKat)) {
                        //... aus Kategorie 1 entfernt ...
                        removeFromKat1();

                        //... und zu Kategorie 2 hinzugefügt
                        addToKat2();
                        //Ist die Frage in Kategorie 2, wird sie ...
                    } else if (fragenKat2.contains(frageZuKat)) {
                        //... aus Kategorie 2 entfernt ....
                        removeFromKat2();

                        //... und in Kategorie 3 hinzugefügt
                        addToKat3();
                    } else if (fragenKat3.contains(frageZuKat)) {
                        //Existiert die Frage in Kategorie 3, passiert nichts
                    } else {
                        //Existiert die Frage in keiner Kategorie, wird sie automatisch zu Kategorie 3 Hinzugefügt
                        addToKat1();
                    }

                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(0).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(0).setBackgroundResource(R.drawable.mybuttonred);
                    //Ist die Frage in Kategorie 2, wird sie in Kategorie 1 verschoben
                    if (fragenKat2.contains(frageZuKat)) {
                        addToKat1();
                        removeFromKat2();
                        //Ist die Frage in Kategorie 1, wird sie in Kategorie 2 verschoben
                    } else if (fragenKat3.contains(frageZuKat)) {
                        removeFromKat3();
                        addToKat2();
                    }
                    //Ist die Frage weder in Kategorie 1 noch 2, wird sie nicht verändert (bleibt in Kategorie 1)
                }
                break;
            //Wenn AntwortButton 2 (Rechts oben) gedrückt wird:
            case R.id.answer2:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(1).getTag().toString()) == 1) {
                    //Ist die Frage in Kategorie 1, wird sie ...
                    if (fragenKat1.contains(frageZuKat)) {
                        //... aus Kategorie 1 entfernt ...
                        removeFromKat1();

                        //... und zu Kategorie 3 hinzugefügt
                        addToKat2();
                        //Ist die Frage in Kategorie 2, wird sie ...
                    } else if (fragenKat2.contains(frageZuKat)) {
                        //... aus Kategorie 2 entfernt ....
                        removeFromKat2();

                        //... und in Kategorie 3 hinzugefügt
                        addToKat3();
                    } else if (fragenKat3.contains(frageZuKat)) {
                        //Existiert die Frage in Kategorie 3, passiert nichts
                    } else {
                        //Existiert die Frage in keiner Kategorie, wird sie automatisch zu Kategorie 3 Hinzugefügt
                        addToKat1();
                    }

                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(1).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(1).setBackgroundResource(R.drawable.mybuttonred);
                    //Ist die Frage in Kategorie 2, wird sie in Kategorie 1 verschoben
                    if (fragenKat2.contains(frageZuKat)) {
                        addToKat1();
                        removeFromKat2();
                        //Ist die Frage in Kategorie 1, wird sie in Kategorie 2 verschoben
                    } else if (fragenKat3.contains(frageZuKat)) {
                        removeFromKat3();
                        addToKat2();
                    }
                    //Ist die Frage weder in Kategorie 1 noch 2, wird sie nicht verändert (bleibt in Kategorie 1)
                }
                break;
            //Wenn AntwortButton 3 (Links unten) gedrückt wird:
            case R.id.answer3:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(2).getTag().toString()) == 1) {
                    //Ist die Frage in Kategorie 1, wird sie ...
                    if (fragenKat1.contains(frageZuKat)) {
                        //... aus Kategorie 1 entfernt ...
                        removeFromKat1();

                        //... und zu Kategorie 3 hinzugefügt
                        addToKat2();
                        //Ist die Frage in Kategorie 2, wird sie ...
                    } else if (fragenKat2.contains(frageZuKat)) {
                        //... aus Kategorie 2 entfernt ....
                        removeFromKat2();

                        //... und in Kategorie 3 hinzugefügt
                        addToKat3();
                    } else if (fragenKat3.contains(frageZuKat)) {
                        //Existiert die Frage in Kategorie 3, passiert nichts
                    } else {
                        //Existiert die Frage in keiner Kategorie, wird sie automatisch zu Kategorie 3 Hinzugefügt
                        addToKat1();
                    }

                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(2).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(2).setBackgroundResource(R.drawable.mybuttonred);
                    //Ist die Frage in Kategorie 2, wird sie in Kategorie 1 verschoben
                    if (fragenKat2.contains(frageZuKat)) {
                        addToKat1();
                        removeFromKat2();
                        //Ist die Frage in Kategorie 1, wird sie in Kategorie 2 verschoben
                    } else if (fragenKat3.contains(frageZuKat)) {
                        removeFromKat3();
                        addToKat2();
                    }
                    //Ist die Frage weder in Kategorie 1 noch 2, wird sie nicht verändert (bleibt in Kategorie 1)
                }
                break;
            //Wenn AntwortButton 4 (rechts unten) gedrückt wird:
            case R.id.answer4:
                //wenn der Tag des Buttons = 1 ist, wird weiter überprüft
                if (Integer.parseInt(answers.get(3).getTag().toString()) == 1) {
                    //Ist die Frage in Kategorie 1, wird sie ...
                    if (fragenKat1.contains(frageZuKat)) {
                        //... aus Kategorie 1 entfernt ...
                        removeFromKat1();

                        //... und zu Kategorie 3 hinzugefügt
                        addToKat2();
                        //Ist die Frage in Kategorie 2, wird sie ...
                    } else if (fragenKat2.contains(frageZuKat)) {
                        //... aus Kategorie 2 entfernt ....
                        removeFromKat2();

                        //... und in Kategorie 3 hinzugefügt
                        addToKat3();
                    } else if (fragenKat3.contains(frageZuKat)) {
                        //Existiert die Frage in Kategorie 3, passiert nichts
                    } else {
                        //Existiert die Frage in keiner Kategorie, wird sie automatisch zu Kategorie 3 Hinzugefügt
                        addToKat1();
                    }

                    //Da die Antwort richtig ist, wird der Hintergrund der Antwort auf grün geändert
                    answers.get(3).setBackgroundResource(R.drawable.mybuttongreen);
                    //Der Counter für die richtigen Fragen wird um 1 erhöht
                    rightAnswers++;
                } else {
                    //Wenn die Frage falsch war, wird der Hintergrund der Antwort auf rot geändert
                    answers.get(3).setBackgroundResource(R.drawable.mybuttonred);
                    //Ist die Frage in Kategorie 2, wird sie in Kategorie 1 verschoben
                    if (fragenKat2.contains(frageZuKat)) {
                        addToKat1();
                        removeFromKat2();
                        //Ist die Frage in Kategorie 1, wird sie in Kategorie 2 verschoben
                    } else if (fragenKat3.contains(frageZuKat)) {
                        removeFromKat3();
                        addToKat2();
                    }
                    //Ist die Frage weder in Kategorie 1 noch 2, wird sie nicht verändert (bleibt in Kategorie 1)
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
                        //Der Button für die nächste Frage wird aktiviert
                        nextQuestion.setEnabled(true);
                    }
                }, 500); //Es gibt eine Verzögerung von 0.5 Sekunden
            }
        }
        //Diese Schleife ist nützlich, wenn man eine falsche Antwort ausgewählt hat (man sieht dadurch die richtige Antwort)


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

        //Alle Fragen aus Kategorie 1 werden in die Datei geschrieben
        for (String s : fragenKat1) {
            writerKat1.write(s+";\n");
            writerKat1.flush();
        }

        //Alle Fragen aus Kategorie 2 werden in die Datei geschrieben
        for (String s : fragenKat2) {
            writerKat2.write(s+";\n");
            writerKat2.flush();
        }

        //Alle Fragen aus Kategorie 3 werden in die Datei geschrieben
        for (String s : fragenKat3) {
            writerKat3.write(s+";\n");
            writerKat3.flush();
        }
    }


    public void endGame(View view) {
        //Wenn der Button "Ende" gedrückt wird, werden die Kategorien in die CSV-Datei geschrieben
        try {
            SharedPreferences preferences;
            preferences = getSharedPreferences("wst", 0);
            SharedPreferences.Editor editor = preferences.edit();
            traiAnz = preferences.getInt("TraiAnz", 0);
            traiAnz++;
            editor.putInt("PrüfAnz", traiAnz);

            questionGes = preferences.getInt("questionsGes", 0);
            durchGes = preferences.getInt("durchGes", 0);
            questionGes = questionGes + counter;
            durchGes++;
            editor.putInt("questionsGes", questionGes);
            editor.putInt("durchGes", durchGes);
            editor.putInt("avgAnz", questionGes/durchGes);

            stopGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void continueToQuestion(View view) {
        //Wenn der Counter noch nicht die maximale Rundenanzahl erreicht hat ...
        if (counter != counterMax) {
            nextQuestion.setEnabled(false);
            answers.get(0).setBackgroundResource(R.drawable.mybutton);
            answers.get(0).setClickable(true);
            answers.get(1).setBackgroundResource(R.drawable.mybutton);
            answers.get(1).setClickable(true);
            answers.get(2).setBackgroundResource(R.drawable.mybutton);
            answers.get(2).setClickable(true);
            answers.get(3).setBackgroundResource(R.drawable.mybutton);
            answers.get(3).setClickable(true);
            startGame();
        } else {
            try {
                //Wenn die maximale Anzahl der Runden erreicht wurde, werden die Kategorien in die CSV-Dateien geschrieben
                stopGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopGame() throws IOException {
        writeCsvs();

        //Ein Intent wird erzeugt, um die Anzahl der richtigen Fragen in die EndActivity weiterzugeben
        Intent intent = new Intent(GameTraiActivity.this, CategoryActivity.class);
        startActivity(intent);
    }

    //Ausgelagerte Metoden zum Löschen aus den >Kategorien
    private void removeFromKat1() {
        fragenKat1.remove(frageZuKat);
    }

    private void removeFromKat2() {
        fragenKat2.remove(frageZuKat);
    }

    private void removeFromKat3() {
        fragenKat3.remove(frageZuKat);
    }

    //Ausgelagerte Metoden zum Hinzufügen in die >Kategorien
    private void addToKat1() {
        fragenKat1.add(frageZuKat);
    }

    private void addToKat2() {
        fragenKat2.add(frageZuKat);
    }

    private void addToKat3() {
        fragenKat3.add(frageZuKat);
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
            String fileKat1 = "fragenKat1.csv";
            String fileKat2 = "fragenKat2.csv";
            String fileKat3 = "fragenKat3.csv";
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

            kat1Size=fragenKat1.size();
            kat2Size=fragenKat2.size();
            kat3Size=fragenKat3.size();
        }
    }

    public static int getKat1Size(){
        return kat1Size;
    }

    public static int getKat2Size(){
        return kat2Size;
    }
    public static int getKat3Size(){
        return kat3Size;
    }


}
