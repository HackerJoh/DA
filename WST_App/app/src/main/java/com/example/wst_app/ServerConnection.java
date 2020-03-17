package com.example.wst_app;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ServerConnection extends AsyncTask<String, Void, List<String[]>> {
    private String filename;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    protected List<String[]> doInBackground(String... strings) {
        return downloadQuestions();
    }

    private List<String[]> downloadQuestions() {
        //Benötigte Variablen werden erstellt
        URL mUrl = null;
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            //es wird die URL zu der Datei am Server erstellt
            mUrl = new URL("https://www.docker.htl-wels.at/quiz/"+filename);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            //Wenn die URL nicht NULL (= nicht verbunden) ist
            assert mUrl != null;
            //Verbindung auf die URL wird erzeugt
            URLConnection connection = mUrl.openConnection();
            //ein BufferedReader wird auf den InputStream von der Verbindung erzeugt
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));  //TODO: Wirft SocketException
            String line = "";
            //solange die Zeile nicht null ist ....
            while((line = br.readLine()) != null){
                //wird der content (String[]) mit der zerteilten Zeile befüllt
                content = line.split(";");
                //Diese zerteilte Zeile wird in die Liste hinzugefügt
                csvLine.add(content);
            }
            //der BufferedReader wird geschlossen
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: fehlerbehandlung wenn kein Internet vorhanden
        }
        //die Liste wird zurückgegeben
        return csvLine;
    }
}
