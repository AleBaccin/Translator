package com.example.bacci.traduttoreddefinitivo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    String valueToTranlate; //Global varaible, the string that will be translated.
    String translatedValue = ""; //The translated string.
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
                    createUrl();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    return;
                }
            };
            Spinner spinnerInitialized = (Spinner) findViewById(R.id.langFrom);
            spinnerInitialized.setOnItemSelectedListener(itemclickListener);
            spinnerInitialized = (Spinner)findViewById(R.id.langTo);
            spinnerInitialized.setOnItemSelectedListener(itemclickListener);
        }catch (Exception e)
        {

        }
        textWrited();
        loadSpinner();//Fill the spinners with the languages.
        this.setTitle("Translator");
        Spinner mspinner = (Spinner) findViewById(R.id.langFrom);
        mspinner.setOnTouchListener(new View.OnTouchListener() {
            EditText editText = (EditText) findViewById(R.id.etext);

            @Override
            public boolean onTouch(View v, MotionEvent event) { //When the user touches the spinner the keyboard will close itself
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

    public void Clear(View v) { // Clear the editText
        EditText empty = (EditText) findViewById(R.id.etext);
        empty.setText("");
    }
    public void loadSpinner()
    {
        String[] spin = getResources().getStringArray(R.array.lingue); // I fill the spinners with the values from the xml array "lingue"(languages).
        Spinner spinner = (Spinner) findViewById(R.id.langFrom);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spin);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(0,true); //True, to start the animation
        spinner = (Spinner) findViewById(R.id.langTo);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(1, true);

    }
    @Override
    protected void onPause() { //Handling the focus
        EditText empty = (EditText) findViewById(R.id.etext);
        // hide the keyboard in order to avoid getTextBeforeCursor on inactive InputConnection
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(empty.getWindowToken(), 0);
        super.onPause();
        createUrl();
    }
    public void textWrited() { //With the textwatcher the app can look for the word while the user types it in.
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
                createUrl();
            }
        };
        editText.addTextChangedListener(textWatcher);

    }
    public void createUrl() {
        exchangeHeaders();
        EditText fromEditText = (EditText) findViewById(R.id.etext);// get the values from the editText and the spinner
        valueToTranlate = fromEditText.getText().toString().toLowerCase();
        String[] abbr = getResources().getStringArray(R.array.abbreviazioni);
        Spinner spinner1 = (Spinner) findViewById(R.id.langFrom);
        Spinner spinner2 = (Spinner) findViewById(R.id.langTo);
        int index1 = (spinner1).getSelectedItemPosition();
        int index2 = (spinner2).getSelectedItemPosition(); //get the indexes to get the respective languages codes.
        String languageFrom = abbr[index1];
        String languageTo = abbr[index2];
        String finalUrl = "https://glosbe.com/gapi/translate?from=" + languageFrom + "&dest=" + languageTo + "&format=json&phrase=" + valueToTranlate; //Create the url for the research
        result translatedWordAndMeaning = new result();
        translatedWordAndMeaning.execute(finalUrl); //execute

        String word = "";
        String meaningS = "";
        if(translatedValue != "") { //The result is obtained as a string seprated by "/" so I will need to use string.split.
            String[] resultComponents = translatedValue.split("/");
            word = resultComponents[0];
            meaningS = resultComponents[1];
        }
        TextView translation = (TextView)findViewById(R.id.result); // Fill the textView
        translation.setText(word.toUpperCase());
        TextView meaning = (TextView)findViewById(R.id.meaning);
        meaning.setText(meaningS);
    }

    public class result extends AsyncTask<String, Integer, String> {

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
            String word = "";
            String meaningS = "";
            if(translatedValue != "") {
                String[] parts = translatedValue.split("/"); //The result is obtained as a string seprated by "/" so I will need to use string.split
                word = parts[0];
                meaningS = parts[1];
            }
            TextView translation = (TextView)findViewById(R.id.result);
            translation.setText(word.toUpperCase());
            TextView meaning = (TextView)findViewById(R.id.meaning);
            meaning.setText(meaningS);
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
                JSONArray tuuc = jsonObject.getJSONArray("tuc");    //Manipulating the Json to get the values that are needed
                try {
                    translatedValue = "...";
                    translatedValue = tuuc.getJSONObject(0).getJSONObject("phrase").getString("text");
                    JSONArray jsnArr = tuuc.getJSONObject(0).getJSONArray("meanings");
                    translatedValue = translatedValue +"/" + jsnArr.getJSONObject(0).getString("text");
                    Html.fromHtml(translatedValue).toString();
                    Log.e("e", translatedValue);
                }
                catch(Exception e)
                {
                    Log.e("e", "There is no translation for this word");
                    translatedValue ="";
                    TextView textView = (TextView)findViewById(R.id.result);
                    textView.setText("No translation");
                }
            }
            else {
                Log.e("e", "There is no translation for this word");
                translatedValue = "";
                TextView textView = (TextView) findViewById(R.id.result);
                textView.setText("No translation");
            }
        }
        catch(Exception e)
        {
            Log.e("e", "Errore");
        }

        return translatedValue;
    }

    public void exchangeLanguage(View v)
    {
        RotateAnimation rotate = new RotateAnimation(0,180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); // animazione
        rotate.setDuration(300);
        rotate.setInterpolator(new LinearInterpolator());
        ImageView rotateImage = (ImageView) findViewById(R.id.exchange);
        rotateImage.startAnimation(rotate);

        Spinner spinner1 = (Spinner) findViewById(R.id.langFrom);
        Spinner spinner2 = (Spinner) findViewById(R.id.langTo);
        int index1 = (spinner1).getSelectedItemPosition();
        int index2 = (spinner2).getSelectedItemPosition();
        String[] spin = getResources().getStringArray(R.array.lingue);
        spinner1.setSelection(index2, true);
        spinner2.setSelection(index1, true); //Exchange the values in the spinners
    }
    public void exchangeHeaders()
    {
        EditText wordToChange = (EditText)findViewById(R.id.etext); //Change the headers regarding to the spinners values
        Spinner spinner1 = (Spinner) findViewById(R.id.langFrom);
        int index1 = (spinner1).getSelectedItemPosition();
        String[] intest = getResources().getStringArray(R.array.intestazioni);
        String linguada = intest[index1];
        wordToChange.setHint(linguada);
    }

}
