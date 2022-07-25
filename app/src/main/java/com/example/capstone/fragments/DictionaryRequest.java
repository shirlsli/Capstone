package com.example.capstone.fragments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;

import javax.net.ssl.HttpsURLConnection;
import android.os.AsyncTask;
import android.util.Log;

import com.example.capstone.CallbackListener;

public class DictionaryRequest extends AsyncTask<String, Integer, String> {

    private CallbackListener listener;

    public DictionaryRequest(CallbackListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {

        final String app_id = "d3faa4e0";
        final String app_key = "00d30320623f7a2bc2992f103a1bbcde";
        try {
            URL url = new URL(params[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept","application/json");
            urlConnection.setRequestProperty("app_id",app_id);
            urlConnection.setRequestProperty("app_key",app_key);

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            return stringBuilder.toString();

        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("print_message", "Get message" + e.getMessage());
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i("Results of Dictionary", "onPostExecute" + result);
        listener.onCallbackEvent(result);
    }
}
