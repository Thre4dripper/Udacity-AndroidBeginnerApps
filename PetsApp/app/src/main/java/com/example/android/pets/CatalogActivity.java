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
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android.pets.database.PetContract;
import com.example.android.pets.database.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    PetCursorAdapter cursorAdapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(intent);
        });


        View emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_circular);
        cursorAdapter = new PetCursorAdapter(this, null);


        // Find the ListView which will be populated with the pet data
        ListView petListView =  findViewById(R.id.list);
        petListView.setAdapter(cursorAdapter);
        petListView.setEmptyView(emptyView);


        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.putExtra("key", Uri.withAppendedPath(PetEntry.CONTENT_URI, String.valueOf(id)).toString());
                startActivity(intent);
            }
        });


        getLoaderManager().initLoader(0, null, this);
    }

/**==================================== INFLATING MENU ITEMS ======================================**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**==================================== CLICKING MENU ITEMS ======================================**/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
                Toast.makeText(this, "All Data Cleared", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**==================================== INSERTING DUMMY DATA ======================================**/
    private void insertData() {


        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, "testName");
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, "testBreed");
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, 1);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 45);

        getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);

    }

    /**==================================== CURSOR LOADER METHODS  ======================================**/
    /**------------------------------------ When Loader is Created -----------------------------
     *
     * In this Cursor Loader Class, we don't need to create a abstract class for doing background work like the networking call
     * as cursor loader already provides us a class to do the same.
     *
     * **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getApplicationContext(),
                PetEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    /**------------------------------------ When Loader has Finished its Work -----------------------------**/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
        progressBar.setVisibility(View.GONE);
    }

    /**------------------------------------ When Loader is Resets -----------------------------**/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
