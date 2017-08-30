package com.pfariasmunoz.mislicencias.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pfariasmunoz.mislicencias.data.LicenceContract.LicenceEntry;

public class LicenceProvider extends ContentProvider {

    public static final String TAG = ContentProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the licences table */
    private static final int LICENCES = 100;

    /** URI matcher code for the content URI for a single licence in the licences table */
    private static final int LICENCE_ID = 101;


    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.pfariasmunoz.mislicencias/licencias" will map to the
        // integer code {@link #LICENCES}. This URI is used to provide access to MULTIPLE rows
        // of the pets tale.
        sUriMatcher.addURI(LicenceContract.CONTENT_AUTHORITY, LicenceContract.PATH_LICENCES, LICENCES);

        // The content URI of the form "content://com.pfariasmunoz.mislicencias/licencias/#" will map to the
        // integer code {@link #LICENCE_ID}. This URI is used to provide access to ONE single row
        // or the licences table.
        //
        // In this case, the "#" wildcard is used where "#" can e substituted for an integer,.
        // For example, "content://com.pfariasmunoz.mislicencias/licencias/3" matches, ut
        // "content://com.pfariasmunoz.mislicencias/licencias/" (without a number at the end) doesn't match.
        sUriMatcher.addURI(LicenceContract.CONTENT_AUTHORITY, LicenceContract.PATH_LICENCES +"/#", LICENCE_ID);
    }


    /** Database helper object */
    private LicenceDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new LicenceDbHelper(getContext());
        return true;
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection,
     * selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the the result of the query
        Cursor cursor;

        // Find out if the uriMatcher can match the uri to a specific code.
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LICENCES:
                // For the LICENCES code query directly the licences tale directly with the given
                // projection, selection, selection arguments, and sort order. The cursor could
                // contain multiples rows of the licences table.
                cursor = database.query(LicenceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LICENCE_ID:
                // For the LICENCE_ID code, extract out the ID from the URI.
                // for an example URI, such ass: "content://com.pfariasmunoz.mislicencias/licences/3",
                // the selection will be "_id=?" and the selection argument will e a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // argument that will fill in the "?". Since we have one "?" in the selection
                // we have 1 String in the selection arguments String array.
                selection = LicenceEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                // this will perform a query in the licences table where the id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(LicenceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query Unknown uni: " + uri);

        }
        // Set the notification URI on the Cursor
        // so we know what content URI the Cursor was created for.
        // iF the data at this URI changes, then we know we need to update the cursor.
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LICENCES:
                return LicenceEntry.CONTENT_LIST_TYPE;
            case LICENCE_ID:
                return LicenceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown Uri " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LICENCES:
                return insertLicence(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertLicence(Uri uri, ContentValues values) {
        // Check that the licence number is not null
        String licenceNumber = values.getAsString(LicenceEntry.COLUMN_LICENCE_NUMBER);
        if (licenceNumber == null) {
            throw new IllegalArgumentException("The Licence requires a number");
        }

        // Check that the paid value is valid
        Integer isPaid = values.getAsInteger(LicenceEntry.COLUMN_LICENCE_PAID);
        if (isPaid == null && !LicenceEntry.isValidPaid(isPaid)) {
            throw new IllegalArgumentException("The licence requires a values to know is it was paid");
        }

        // Check that the start date is not null
        String startLicence = values.getAsString(LicenceEntry.COLUMN_LICENCE_START);
        if (startLicence == null) {
            throw new IllegalArgumentException("The licence requires a start date");
        }

        // Check that the end value is not null
        String endLicence = values.getAsString(LicenceEntry.COLUMN_LICENCE_END);
        if (endLicence == null) {
            throw new IllegalArgumentException("The end licence is required");
        }

        // check that the duration is valid
        Integer duration = values.getAsInteger(LicenceEntry.COLUMN_LICENCE_DURATION);
        if (duration == null && !LicenceEntry.isValidDuration(duration)) {
            throw new IllegalArgumentException("The duration of the licence is required");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(LicenceEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listener that the data has change for the pet content URI
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Delete the data at the given selection and selection arguments.
    */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted;

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LICENCES:
                // Delete all rows that match the selection and selection args
                // For  case LICENCES:
                rowsDeleted = database.delete(LicenceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LICENCE_ID:
                // Delete a single row given by the ID in the UR
                selection = LicenceEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // For case PET_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(LicenceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LICENCES:
                return updateLicence(uri, values, selection, selectionArgs);
            case LICENCE_ID:
                selection = LicenceEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateLicence(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateLicence(Uri uri, ContentValues values,
                              String selection, String[] selectionArgs) {
        // If the {@link LicenceEntry#COLUMN_LICENCE_NUMBER} key is present,
        // check that the name value is not null.
        if (values.containsKey(LicenceEntry.COLUMN_LICENCE_NUMBER)) {
            String licenceNumber = values.getAsString(LicenceEntry.COLUMN_LICENCE_NUMBER);
            if (licenceNumber == null) {
                throw new IllegalArgumentException("The licence requires a number");
            }
        }
        // If the {@link LicenceEntry#COLUMN_LICENCE_DURATION} key is present,
        // check that the duration value is not null.
        if (values.containsKey(LicenceEntry.COLUMN_LICENCE_DURATION)) {
            Integer duration = values.getAsInteger(LicenceEntry.COLUMN_LICENCE_DURATION);
            if (duration == null) {
                throw new IllegalArgumentException("The licence requires a duration");
            }
        }
        // If the {@link LicenceEntry#COLUMN_LICENCE_START} key is present,
        // check that the start date value is not null.
        if (values.containsKey(LicenceEntry.COLUMN_LICENCE_START)) {
            String startLicence = values.getAsString(LicenceEntry.COLUMN_LICENCE_START);
            if (startLicence == null) {
                throw new IllegalArgumentException("The licence requires a start date");
            }
        }
        // If the {@link LicenceEntry#COLUMN_LICENCE_END} key is present,
        // check that the end date value is not null.
        if (values.containsKey(LicenceEntry.COLUMN_LICENCE_END)) {
            String endLicence = values.getAsString(LicenceEntry.COLUMN_LICENCE_END);
            if (endLicence == null) {
                throw new IllegalArgumentException("The licence requieres a end date");
            }
        }

        if (values.containsKey(LicenceEntry.COLUMN_LICENCE_PAID)) {
            Integer isPaidLicence = values.getAsInteger(LicenceEntry.COLUMN_LICENCE_PAID);
            if (isPaidLicence == null) {
                throw new IllegalArgumentException("The licence requires a value if paid");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(LicenceEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}
