package edu.lb.max.signpractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;
import java.security.SecureRandom;
import java.sql.Time;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private TextView signName;
    private ImageView signImage;
    private TextView countDown;
    private  Bitmap signBitmap;

    private static final String DEBUG_TAG = "HttpGet";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signImage = (ImageView) findViewById(R.id.signBitmap);
        signName = (TextView) findViewById(R.id.signName);
        countDown = (TextView) findViewById(R.id.countDown);
        ConnectivityManager ConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = ConnMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()){
            initiateGuess();
        } else {
            Toast.makeText(getApplicationContext(), "no network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initiateGuess() {
        new DownloadTask().execute("32");
        new CountDownTimer(10000, 100) {

            public void onTick(long millisUntilFinished) {
                countDown.setText("seconds remaining: " + millisUntilFinished / 1000);
                if (millisUntilFinished <= 1000) countDown.setText("OMG UR SO SLOW :p x) :')");
                if (millisUntilFinished <= 900) countDown.setBackgroundColor(Color.RED);
                if (millisUntilFinished <= 700){
                    countDown.setBackgroundColor(Color.BLUE);
                    countDown.setTextColor(Color.YELLOW);
                }
                if (millisUntilFinished <= 500) countDown.setBackgroundColor(Color.GREEN);
                if (millisUntilFinished <= 300) countDown.setBackgroundColor(Color.RED);
            }

            public void onFinish() {
                countDown.setText("TIMESUP!!!! AUFSTEIGEN!!!");
                signImage.setImageBitmap(signBitmap);
                countDown.setOnClickListener(restartGuessClick);
            }
        }.start();
    }

    View.OnClickListener restartGuessClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            countDown.setOnClickListener(null);
            signImage.setImageBitmap(null);
            initiateGuess();
        }
    };


    // Establishes an HttpUrlConnection and retrieves
    // a random sign entry whose name and image icon are
    // set as the next 'display' and 'answer' for the app
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private Bitmap image = null;
        private SecureRandom r = new SecureRandom();
        private String baseUri = "http://gebaren.ugent.be";
        private String allWordsUri = "/alfabet.php?letter=!";
        private String wordUri = "/alfabet.php?id=";
        @Override
        protected String doInBackground(String... seed) {
//           generate random sign based on seed, if link invalid IOexception catched and reload doinbackground
            r.setSeed(r.generateSeed(Integer.parseInt(seed[0])));
            int random = 17090+r.nextInt(22400-17000);

            // params comes from the execute() call: params[0] is the url.
            try {
//              get the inputstream of the random words' webpage
                InputStream myStream = downloadUrl(baseUri+wordUri+random);
//              get the image and wordname of this word
                ArrayList<String> list = parseNameAndImageURI(myStream);
//              return the imagestream by using the image uri on the first arraylist spot
                myStream = downloadUrl(list.get(0));
                BitmapFactory bitfac = new BitmapFactory();
                image = bitfac.decodeStream(myStream);
                return list.get(1);
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "IO Exception at downloadUrl OR random number failed a page match");
                return  doInBackground(seed[0]);
            } catch(ArrayIndexOutOfBoundsException e1){
                return "failed3";
            }
        }
        private ArrayList<String> parseNameAndImageURI(InputStream myStream) throws IOException {

            try {
                String contentAsString = readIt(myStream);
                if (contentAsString.contains("Woord niet gevonden!")) throw new IOException();
                //parse html and get src and alt of img tag
                Document doc = Jsoup.parse(contentAsString);
                Elements bleh = doc.select("img[src*=sign]");
                String ImageUri= bleh.attr("src");
                String ImageName= bleh.attr("alt");
                ArrayList<String> temp = new ArrayList<String>(2);
                temp.add(baseUri + ImageUri);
                temp.add(ImageName);
                return temp;
            } finally {
                if (myStream != null) {
                    myStream.close();
                }

            }

        }


        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
        /*
        several design choices here: we could download our image and word separately or we can send
        them back together or we could dowload all possible words and store them with a progress bar the first time
        */
        private InputStream downloadUrl(String myUrl) throws IOException, UnknownServiceException {
            InputStream is;
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            return  is;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            signName.setText(result);
            signBitmap = image;
        }
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
}
