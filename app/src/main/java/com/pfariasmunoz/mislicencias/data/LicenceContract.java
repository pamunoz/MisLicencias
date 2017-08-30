package com.pfariasmunoz.mislicencias.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Pablo Farias on 26-08-17.
 */

public class LicenceContract {

    public static final String TAG = LicenceContract.class.getSimpleName();
    /**
     * the Content Authority which is used to help identify the Content Provider
     * which we’d setup before in the AndroidManifest
     */
    public static final String CONTENT_AUTHORITY = "com.pfariasmunoz.mislicencias";

    /**
     * we concatonate the CONTENT_AUTHORITY constant with the scheme “content://”
     * we will create the BASE_CONTENT_URI which will be shared by every URI associated with {@link LicenceContract}
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * This constants stores the path for each of the tables which will be appended to the base content URI.
     */
    public static final String PATH_LICENCES = "licences";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private LicenceContract() {}

    /**
     * Inner class that defines constant values for the licences database table.
     * Each entry in the table represents a single licence.
     */
    public static class LicenceEntry implements BaseColumns {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of licences.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LICENCES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single licence.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LICENCES;

        /**
         * inside each of the Entry classes in the contract,
         * we create a full URI for the class as a constant called CONTENT_URI.
         * The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LICENCES);

        /** Name of database table for licences */
        public static final String TABLE_NAME = "licences";

        /**
         * Unique ID number for the licence (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;
        /**
         * Number of the licence.
         *
         * Type: TEXT
         */
        public static final String COLUMN_LICENCE_NUMBER = "licence_number";
        /**
         * Duration of the licence.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_LICENCE_DURATION = "duration";
        /**
         * Start of the licence.
         *
         * Type: TEXT
         */
        public static final String COLUMN_LICENCE_START = "start";
        /**
         * End of the licence.
         *
         * Type: TEXT
         */
        public static final String COLUMN_LICENCE_END = "end";
        /**
         * Paid of the licence.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_LICENCE_PAID = "paid";


        /**
         * Possible values for the gender of the pet.
         */
        public static final int UNPAID = 0;
        public static final int PAID = 1;
        /**
         * Return whether the paid value is valid
         */
        public static boolean isValidPaid(int isPaid) {
            if (isPaid == 0 || isPaid == 1) {
                return true;
            }
            return false;
        }
        /**
         * Returns whether or not the duration is valid.
         */
        public static boolean isValidDuration(int duration) {
            if (duration > 0 || duration < 32) {
                return true;
            }
            return false;
        }
    }
}
