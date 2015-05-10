package edu.lb.max.signpractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    TextView urlResult;
    EditText takeUrl;
    Button Download;
    ImageView signImage;
    private static final String DEBUG_TAG = "HttpGet";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlResult = (TextView) findViewById(R.id.textView);
        takeUrl = (EditText) findViewById(R.id.editText);
        Download = (Button) findViewById(R.id.button);
        Download.setOnClickListener(DownloadButtonListener);
        signImage = (ImageView) findViewById(R.id.signBitmap);
    }

    View.OnClickListener DownloadButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String url = takeUrl.getText().toString();
            ConnectivityManager ConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = ConnMgr.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()){
                new DownloadWebpageTask().execute(url);
            } else {
                urlResult.setText("no network connection available.");
            }
        }
    };

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private class DownloadWebpageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);           
                
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "IO Exception at downloadUrl");
                return null;
            }
        }
        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        private Bitmap downloadUrl(String myUrl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
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
                String contentAsString = readIt(is, len);
                //parse html and get src and alt of img tag
                Elements imageTags = Jsoup.parse(contentAsString).getElementsByTag("IMG");
                String ImageUri = imageTags.attr('src').toString();
                String ImageName = imageTags.attr('alt').toString();
                // Convert the IS into an image
                BitmapFactory bitfac = new BitmapFactory();
                String baseUri = 'http://gebaren.ugent.be';
                Bitmap bitty = bitfac.decodeStream(is);
//                TODO: set downloadurl seperatly from getting teh string or img
                String s = bitty.toString();

                return  bitty;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
            signImage.setImageBitmap(result);
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
