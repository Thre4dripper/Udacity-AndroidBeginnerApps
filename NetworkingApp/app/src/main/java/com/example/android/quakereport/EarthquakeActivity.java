/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {
    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    static CustomAdapter customAdapter;
    List<Earthquake> earthquakes = new ArrayList<>();
    TextView textView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        progressBar = findViewById(R.id.progress);

        textView = findViewById(R.id.emptyView);
        customAdapter = new CustomAdapter(this, new ArrayList<>());

        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        earthquakeListView.setEmptyView(textView);
        earthquakeListView.setAdapter(customAdapter);

        getSupportLoaderManager().initLoader(1, null, this);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(earthquakes.get(position).getLink()));
                startActivity(intent);
            }
        });

    }

    /**============================== LOADER METHODS ===============================================**/
    /**
     * ------------------------------------------ When Loader is Created -------------------------------------------------
     **/
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {

        if (id == 1) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String minMagnitude = sharedPrefs.getString(
                    getString(R.string.settings_min_magnitude_key),
                    getString(R.string.settings_min_magnitude_default));

            String orderBy = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default)
            );

            Uri baseUri = Uri.parse(USGS_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("format", "geojson");
            uriBuilder.appendQueryParameter("limit", "100");
            uriBuilder.appendQueryParameter("minmag", minMagnitude);
            uriBuilder.appendQueryParameter("orderby", orderBy);

            return new EarthquakeLoader(this, uriBuilder.toString());
        } else {
            return new EarthquakeLoader(EarthquakeActivity.this, "");
        }
    }

    /**
     * ------------------------------------------ When Loader is finished doing background work -------------------------------------------------
     **/
    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        customAdapter.clear();
        customAdapter.addAll(data);
        earthquakes.clear();
        earthquakes = data;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected)
            textView.setText("No EarthquakeData");
        else textView.setText("No Internet Connection");
        progressBar.setVisibility(View.GONE);


    }

    /**
     * ------------------------------------------ When Loader is Reset -------------------------------------------------
     **/
    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        customAdapter = new CustomAdapter(this, new ArrayList<>());
    }

    /**
     * ===================================== INFLATING MENU ITEMS =========================================
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    /**
     * ===================================== CLICKING MENU ITEMS =========================================
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
