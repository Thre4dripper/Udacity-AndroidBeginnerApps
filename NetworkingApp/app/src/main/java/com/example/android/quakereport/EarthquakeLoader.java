package com.example.android.quakereport;

import android.content.Context;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Abstract Class for Loader Callbacks
 **/
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    String URL;

    /**
     * ================================================= CONSTRUCTOR =====================================================
     **/
    public EarthquakeLoader(Context context, String url) {
        super(context);
        URL = url;
    }


    /**
     * =================== METHOD FOR DOING ALL THE NETWORKING WORK ON BACKGROUND THREAD======================
     **/
    @Override
    public List<Earthquake> loadInBackground() {
        List<Earthquake> earthquakes = new ArrayList<>();

        URL url = null;
        StringBuilder JSONResponse = new StringBuilder();
        try {
            url = new URL(URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            bufferedReader = new BufferedReader(inputStreamReader);

            line = bufferedReader.readLine();
            while (line != null) {
                JSONResponse.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }


        try {
            JSONObject rootObject = new JSONObject(JSONResponse.toString());

            JSONArray features = rootObject.getJSONArray("features");
            JSONObject earthquake, properties;
            for (int i = 0; i < features.length(); i++) {
                earthquake = features.getJSONObject(i);
                properties = earthquake.getJSONObject("properties");
                earthquakes.add(new Earthquake(((int) (properties.getDouble("mag") * 10.0)) / 10.0
                        , properties.getString("place"), properties.getLong("time"), properties.getString("url")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("mytag", "error");
        }


        // Return the list of earthquakes
        return earthquakes;
    }

    /**
     * ============================== WHEN LOADER START LOADING ============================================
     **/
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}