package com.example.bacci.traduttoreddefinitivo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    String valoreprimario; //variabili globali la prima è la parola da cercare e la seconda è il risultato
    String traduzionesign = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try{
            AdapterView.OnItemSelectedListener itemclickListener = new AdapterView.OnItemSelectedListener(){

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    creaurl();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    return;
                }
            };
            Spinner spinneron = (Spinner) findViewById(R.id.da);
            spinneron.setOnItemSelectedListener(itemclickListener);
            spinneron = (Spinner)findViewById(R.id.acosa);
            spinneron.setOnItemSelectedListener(itemclickListener);
        }catch (Exception e)
        {

        }
        ScriviTesto(); //chiamo scrivo testo e riempispinner nell'oncreate per  riempire gli spinner
        riempispinner();
        this.setTitle("Traduttore");
        Spinner mspinner = (Spinner) findViewById(R.id.da);
        mspinner.setOnTouchListener(new View.OnTouchListener() {
            EditText editText = (EditText) findViewById(R.id.etext);

            @Override
            public boolean onTouch(View v, MotionEvent event) { //chiudo la tastiera al tocco dello spinner
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                return false;
            }
        }) ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Clear(View v) { // pulisco la edittext
        EditText svuota = (EditText) findViewById(R.id.etext);
        svuota.setText("");
    }
    public void riempispinner()
    {
        String[] spin = getResources().getStringArray(R.array.lingue); //riempio gli spinner con i valori dell'array xml lingue
        Spinner spinner = (Spinner) findViewById(R.id.da);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spin);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(0,true); //,true per fare animazione
        spinner = (Spinner) findViewById(R.id.acosa);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(1, true);

    }
    @Override
    protected void onPause() { //gestisco il focus
        EditText svuota = (EditText) findViewById(R.id.etext);
        // hide the keyboard in order to avoid getTextBeforeCursor on inactive InputConnection
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(svuota.getWindowToken(), 0);
        super.onPause();
        creaurl();
    }
    public void ScriviTesto() { //con il textwatcher mi cerca la parola mentre la scrivo
        final EditText editText = (EditText) findViewById(R.id.etext);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                creaurl();
            }
        };
        editText.addTextChangedListener(textWatcher);

    }
    public void creaurl() {
        cambiaintestazione();
        EditText daedit = (EditText) findViewById(R.id.etext);// prendo i valori di settext e spinner
        valoreprimario = daedit.getText().toString().toLowerCase();
        String[] abbr = getResources().getStringArray(R.array.abbreviazioni);
        Spinner spinner1 = (Spinner) findViewById(R.id.da);
        Spinner spinner2 = (Spinner) findViewById(R.id.acosa);
        int index1 = (spinner1).getSelectedItemPosition();
        int index2 = (spinner2).getSelectedItemPosition(); //ottengo gli indici per poter ottenere quindi i rispettivi codici delle 2 lingue
        String linguada = abbr[index1];
        String linguaa = abbr[index2];
        String finale = "https://glosbe.com/gapi/translate?from=" + linguada + "&dest=" + linguaa + "&format=json&phrase=" + valoreprimario; //creo l'url per la ricerca
        finale parolaTradotta = new finale();
        parolaTradotta.execute(finale); //eseguo

        String parola = "";
        String signifato = "";
        if(traduzionesign != "") { //siccome ottengo il significato e la traduzione in un unica stringa separata da / devo fare uno split
            String[] parts = traduzionesign.split("/");
            parola = parts[0];
            signifato = parts[1];
        }
        TextView traduzione = (TextView)findViewById(R.id.risultato); // riempio le textview
        traduzione.setText(parola.toUpperCase());
        TextView significato = (TextView)findViewById(R.id.significato);
        significato.setText(signifato);
    }

    public class finale extends AsyncTask<String, Integer, String> {

        protected void onPreExecute() {

        }


        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if(connection.getResponseCode() != 200)
                {

                }
                InputStream is = connection.getInputStream();
                return decodeStream(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String params)
        {
            String parola = "";
            String signifato = "";
            if(traduzionesign != "") {
                String[] parts = traduzionesign.split("/"); //siccome ottengo il significato e la traduzione in un unica stringa separata da / devo fare uno split
                parola = parts[0];
                signifato = parts[1];
            }
            TextView traduzione = (TextView)findViewById(R.id.risultato);
            traduzione.setText(parola.toUpperCase());
            TextView significato = (TextView)findViewById(R.id.significato);
            significato.setText(signifato);
        }
    }
    private String decodeStream(InputStream inputStream)
    {

        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while((line = reader.readLine()) !=null)
                buffer.append(line);
            String finalJSon = buffer.toString();

            JSONObject jsonObject = new JSONObject(finalJSon);

            String result = jsonObject.getString("result");

            if(result.equals("ok"))
            {
                JSONArray tuuc = jsonObject.getJSONArray("tuc");
                try {
                    traduzionesign = "...";
                    traduzionesign= tuuc.getJSONObject(0).getJSONObject("phrase").getString("text");
                    JSONArray dope = tuuc.getJSONObject(0).getJSONArray("meanings");
                    traduzionesign = traduzionesign +"/" + dope.getJSONObject(0).getString("text");
                    Html.fromHtml(traduzionesign).toString();
                    Log.e("e", traduzionesign);
                }
                catch(Exception e)
                {
                    Log.e("e", "Non c'è una traduzione per questa parola");
                    traduzionesign ="";
                    TextView textView = (TextView)findViewById(R.id.risultato);
                    textView.setText("Nessuna traduzione");
                }
            }
            else {
                Log.e("e", "Non c'è una traduzione per questa parola");
                traduzionesign = "";
                TextView textView = (TextView) findViewById(R.id.risultato);
                textView.setText("Nessuna traduzione");
            }
        }
        catch(Exception e)
        {
            Log.e("e", "Errore");
        }

        return traduzionesign;
    }

    public void scambialingua(View v)
    {
        RotateAnimation ruota = new RotateAnimation(0,180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); // animazione
        ruota.setDuration(300);
        ruota.setInterpolator(new LinearInterpolator());
        ImageView ruotaimmagine = (ImageView) findViewById(R.id.scambio);
        ruotaimmagine.startAnimation(ruota);

        Spinner spinner1 = (Spinner) findViewById(R.id.da);
        Spinner spinner2 = (Spinner) findViewById(R.id.acosa);
        int index1 = (spinner1).getSelectedItemPosition();
        int index2 = (spinner2).getSelectedItemPosition();
        String[] spin = getResources().getStringArray(R.array.lingue);
        spinner1.setSelection(index2, true);
        spinner2.setSelection(index1, true); //scambio i valori degli spinner
    }
    public void cambiaintestazione()
    {
        EditText dacambiare = (EditText)findViewById(R.id.etext); //cambio l'intestazione in base alla lingua
        Spinner spinner1 = (Spinner) findViewById(R.id.da);
        int index1 = (spinner1).getSelectedItemPosition();
        String[] intest = getResources().getStringArray(R.array.intestazioni);
        String linguada = intest[index1];
        dacambiare.setHint(linguada);
    }

}
