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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.android.pets.database.PetContract;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    //This variable can tell us whether this activity is called from FAB or by clicking menu item
    Uri uri=null;

    private boolean mPetHasChanged = false;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetContract.PetEntry.GENDER_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent=getIntent();

        if(intent.getStringExtra("key")==null)
        {
            invalidateOptionsMenu();
            setTitle("Add Pet");
        }
        else
        {
            setTitle("Edit Pet");
            uri=Uri.parse(intent.getStringExtra("key"));
            getLoaderManager().initLoader(1,null,this);
        }



        // Find all relevant views that we will need to read user input from
        mNameEditText =  findViewById(R.id.edit_pet_name);
        mBreedEditText =  findViewById(R.id.edit_pet_breed);
        mWeightEditText =  findViewById(R.id.edit_pet_weight);
        mGenderSpinner =  findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender =  PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender =  PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    /**==================================== INFLATING MENU ITEMS ======================================**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file if it is in Edit Mode
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor,menu);

        return true;
    }

    /**==================================== HIDING DELETE BUTTON FROM MENU ITEMS ======================================**/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (uri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**==================================== CLICKING MENU ITEMS ======================================**/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(uri==null)
                {
                    if(insertData()!=null)
                        finish();
                }
                else
                {
                    if(updateData()!=-1)
                        finish();
                }

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**==================================== METHOD FOR UPDATING THE DATA IN DATABASE ======================================**/
    private int updateData() {
        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, mWeightEditText.getText().toString().trim());

        return getContentResolver().update(uri,values,null,null);

    }

    /**==================================== METHOD FOR INSERTING THE DATA IN DATABASE ======================================**/
    private Uri insertData() {

            ContentValues values = new ContentValues();
            values.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
            values.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
            values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
            values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, mWeightEditText.getText().toString().trim());

            return getContentResolver().insert(PetContract.PetEntry.CONTENT_URI,values);


    }

    /**==================================== METHOD DELETE WITH CONFORMATION BOX ======================================**/
    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
               getContentResolver().delete(uri,null,null);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                uri,
                null,
                null,
                null,
                null);
    }

    /**------------------------------------ When Loader has Finished its Work -----------------------------**/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //checking of valid data other wise edit activity will crash
        if(data==null || data.getCount()<1)return;

        data.moveToPosition(0);
        mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME)));

        data.moveToPosition(0);
        mBreedEditText.setText(data.getString(data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED)));

        data.moveToPosition(0);
        mWeightEditText.setText(data.getString(data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_WEIGHT)));

        data.moveToPosition(0);
        mGenderSpinner.setSelection(data.getInt(data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_GENDER)));
    }

    /**------------------------------------ When Loader is Resets -----------------------------**/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    /**==================================== METHOD FOR CHANGING BACK BUTTON BEHAVIOR ======================================**/
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**==================================== CREATED A TOUCH LISTENER AT ONCE ======================================**/
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
// the view, and we change the mPetHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    /**============================== CREATED DIALOG BOX FOR UNSAVED CHANGES AT ONCE =============================**/
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}