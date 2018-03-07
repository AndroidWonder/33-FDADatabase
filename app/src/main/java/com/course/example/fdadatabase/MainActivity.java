package com.course.example.fdadatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private TextView text = null;

    //messages from background thread contain data for UI
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            String title =(String) msg.obj;
            text.append(title + "\n" +"\n");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text=(TextView)findViewById(R.id.texter);

        Thread t = new Thread(background);
        t.start();
    }

    //thread connects to USDA Food Composition DB, gets response code, JSON search results,
    //places data into Log and sends messages to display data on UI
    Runnable background = new Runnable() {
        public void run(){

            StringBuilder builder = new StringBuilder();

            //check out A1 BBQ Sauce
            String Url = "https://api.nal.usda.gov/ndb/reports/?ndbno=45140555&type=b&format=json&api_key=DEMO_KEY";

            InputStream is = null;

            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("JSON", "The response is: " + response);
                //if response code not 200, end thread
                if (response != 200) return;
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            }	catch(IOException e) {}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException e) {}
                }
            }

            //convert StringBuilder to String
            String readJSONFeed = builder.toString();
            Log.e("JSON", readJSONFeed);

            //set up Message variable
            Message msg;

            //decode JSON
            try {
                JSONObject obj = new JSONObject(readJSONFeed);
                JSONObject report = obj.getJSONObject("report");

                String type = report.getString("type");
                Log.i("JSON", "type " + type);

                JSONObject food = report.getJSONObject("food");
                String name  = food.getString("name");
                Log.i("JSON", "name " + name);
                msg = handler.obtainMessage();
                msg.obj = name;
                handler.sendMessage(msg);

                JSONObject ing = food.getJSONObject("ing");
                String desc  = ing.getString("desc");
                Log.i("JSON", "desc " + desc);

                //parse for UI
                String[] tokens = desc.split(",");

                for (int i=0; i<tokens.length; i++) {

                    msg = handler.obtainMessage();
                    msg.obj = tokens[i];
                    handler.sendMessage(msg);

                }

            } catch (JSONException e) {e.getMessage();
                e.printStackTrace();
            }
        }

    };

}

