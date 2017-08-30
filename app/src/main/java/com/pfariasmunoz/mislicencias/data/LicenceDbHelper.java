package com.pfariasmunoz.mislicencias.data;

/**
 * Created by Pablo Farias on 26-08-17.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pfariasmunoz.mislicencias.data.LicenceContract.LicenceEntry;

/**
 * Database helper for Licencess app. Manages database creation and version management.
 */
public class LicenceDbHelper extends SQLiteOpenHelper {

    public static final String TAG = LicenceDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "licences.db";
    /** Database version.
     * If you change the database schema, you must change its version */
    private static final int DATABASE_VERSION = 1;


    /**
     * Constructs a new instance of {@link LicenceDbHelper}.
     *
     * @param context of the app
     */
    public LicenceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the licences table
        String SQL_CREATE_LICENCES_TABLE = "CREATE TABLE " + LicenceEntry.TABLE_NAME + " ("
                + LicenceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LicenceEntry.COLUMN_LICENCE_NUMBER + " TEXT NOT NULL, "
                + LicenceEntry.COLUMN_LICENCE_DURATION + " INTEGER NOT NULL DEFAULT 0, "
                + LicenceEntry.COLUMN_LICENCE_START + " TEXT NOT NULL, "
                + LicenceEntry.COLUMN_LICENCE_END + " TEXT NOT NULL, "
                + LicenceEntry.COLUMN_LICENCE_PAID + " INTEGER NOT NULL DEFAULT 0);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_LICENCES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
