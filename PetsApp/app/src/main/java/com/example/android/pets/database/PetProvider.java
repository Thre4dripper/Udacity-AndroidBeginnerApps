package com.example.android.pets.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import static com.example.android.pets.database.PetContract.*;

public class PetProvider extends ContentProvider {

    public static final int PETS = 100;
    public static final int PET_ID = 101;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    PetDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**==================================== QUERY METHOD ======================================**/
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                cursor = null;
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    /**==================================== TYPE METHOD ======================================**/
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**==================================== INSERT METHOD ======================================**/
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long returnedId;
        int match = sUriMatcher.match(uri);
        if (match == PETS) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (TextUtils.isEmpty(name)) {

                Toast.makeText(getContext(), "Empty Name Field", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Check that the gender is valid
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || (gender != PetEntry.GENDER_UNKNOWN && gender != PetEntry.GENDER_MALE && gender != PetEntry.GENDER_FEMALE)) {

                Toast.makeText(getContext(), "Invalid Gender", Toast.LENGTH_SHORT).show();
                return null;
            }

            // If the weight is provided, check that it's greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {

                Toast.makeText(getContext(), "Invalid Weight", Toast.LENGTH_SHORT).show();
                return null;
            }
            else if(weight==null) {
                values.remove(PetEntry.COLUMN_PET_WEIGHT);
                values.put(PetEntry.COLUMN_PET_WEIGHT,0);
            }

            returnedId = db.insert(PetEntry.TABLE_NAME, null, values);
        } else {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(PetEntry.CONTENT_URI, returnedId);
    }

    /**==================================== DELETE METHOD ======================================**/
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted= db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted= db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    /**==================================== UPDATE METHOD ======================================**/
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
            throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**==================================== HELPER UPDATE METHOD ======================================**/
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (TextUtils.isEmpty(name)) {

                Toast.makeText(getContext(), "Empty Name Field", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || (gender != PetEntry.GENDER_UNKNOWN && gender != PetEntry.GENDER_MALE && gender != PetEntry.GENDER_FEMALE)) {

                Toast.makeText(getContext(), "Invalid Gender", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {

                Toast.makeText(getContext(), "Invalid Weight", Toast.LENGTH_SHORT).show();
                return -1;
            }
            else if(weight==null) {
                values.remove(PetEntry.COLUMN_PET_WEIGHT);
                values.put(PetEntry.COLUMN_PET_WEIGHT,0);
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the db
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable db to update the data
        SQLiteDatabase db = dbHelper.getWritableDatabase();



        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);


        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}