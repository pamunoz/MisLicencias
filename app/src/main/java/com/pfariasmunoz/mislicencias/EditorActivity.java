package com.pfariasmunoz.mislicencias;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pfariasmunoz.mislicencias.data.LicenceContract.LicenceEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Allows user to create a new licence or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>, DatePickerDialog.OnDateSetListener {

    /** Identifier for the licence data loader */
    private static final int EXISTING_LICENCE_LOADER = 0;

    /** Content URI for the existing licence (null if it's a new licence) */
    private Uri mCurrentLicenceUri;

    /** EditText field to enter the licence's number */
    @BindView(R.id.et_licence_number)
    EditText mNumberEditText;

    /** Spinner field to enter the licence's duration */
    @BindView(R.id.np_duration)
    NumberPicker mDurationSpinner;

    /** EditText field to enter the licence's start date */
    @BindView(R.id.et_start_date)
    EditText mStartDateEditText;

    /** EditText field to enter the licence's end date */
    @BindView(R.id.et_end_date)
    EditText mEndDateEditText;

    @BindView(R.id.bt_save_licence)
    Button mSaveLicenceButton;

    @BindView(R.id.bt_delete_licence)
    Button mDeleteLicenceButton;
    //    @BindView(R.id.tv_licence_duration)
    //TextView mDurationTextView;
    Locale mLocale;

    int mOldValue = 0;

    private boolean mLicenceChanged = false;

    /** The DatePicker for the start Date of the licence */
    private DatePickerDialog mStartLicenceDatePicker;
    private DatePickerDialog mEndLicenceDatePicker;
    /** The licencer for the dialog */

    public static final int DIALOG_ID = 0;

    private SimpleDateFormat mDateFormatter;

    private Calendar mCalendar;
    private Calendar mNewCalendar;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mLicenceChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new licence or editing an existing one.
        Intent intent = getIntent();
        mCurrentLicenceUri = intent.getData();

        // If the intent DOES NOT contain a licence content URI, then we know that we are
        // creating a new licence.
        if (mCurrentLicenceUri == null) {
            // This is a new licencia, so change the app bar to say "Add a licence"
            setTitle(getString(R.string.editor_activity_title_new_licence));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit licence"
            setTitle(getString(R.string.editor_activity_title_edit_licence));

            mDeleteLicenceButton.setVisibility(View.VISIBLE);

            // Initialize a loader to read the licence data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_LICENCE_LOADER, null, this);
        }

        mLocale = new Locale("es_CL");
        mCalendar = Calendar.getInstance(mLocale);
        mDateFormatter = new SimpleDateFormat("dd-MM-yyyy", mLocale);
        mStartDateEditText.setInputType(InputType.TYPE_NULL);
        mEndDateEditText.setInputType(InputType.TYPE_NULL);
        mStartDateEditText.setClickable(false);
        mEndDateEditText.setClickable(false);
        mDurationSpinner.setMinValue(1);
        mDurationSpinner.setMaxValue(31);
        mDurationSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (mNewCalendar != null) {
                    mEndDateEditText.setText(mDateFormatter.format(mNewCalendar.getTime()));
                }

            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNumberEditText.setOnTouchListener(mTouchListener);
        mDurationSpinner.setOnTouchListener(mTouchListener);
        mStartDateEditText.setOnTouchListener(mTouchListener);

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mCalendar = new GregorianCalendar(year, month, day);
        mNewCalendar = (Calendar) mCalendar.clone();
        mNewCalendar.add(Calendar.DATE, mDurationSpinner.getValue() - 1);
        setDate(mCalendar, mNewCalendar);

    }

    private void setDate(final Calendar calendar, Calendar newCalendar) {

        mStartDateEditText.setText(mDateFormatter.format(calendar.getTime()));
        mEndDateEditText.setText(mDateFormatter.format(newCalendar.getTime()));

    }

    @OnClick(R.id.et_start_date)
    public void onDatePicker() {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getFragmentManager(), "date");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all licence attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                LicenceEntry._ID,
                LicenceEntry.COLUMN_LICENCE_NUMBER,
                LicenceEntry.COLUMN_LICENCE_DURATION,
                LicenceEntry.COLUMN_LICENCE_START,
                LicenceEntry.COLUMN_LICENCE_END,
                LicenceEntry.COLUMN_LICENCE_PAID};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,               // Parent activity context
                mCurrentLicenceUri, // Query the content URI for the current pet
                projection,         // Columns to include in the resulting Cursor
                null,               // No selection clause
                null,               // No selection arguments
                null);              // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested i
            int numberColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_NUMBER);
            int durationColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_DURATION);
            int startColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_START);
            int endColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_END);
            int paidColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_PAID);

            // Extract out the value from the Cursor for the given column index
            String licenceNumber = cursor.getString(numberColumnIndex);
            int licenceDuration = cursor.getInt(durationColumnIndex);
            String licenceStart = cursor.getString(startColumnIndex);
            String licenceEnd = cursor.getString(endColumnIndex);
            String licencePaid = cursor.getString(paidColumnIndex);

            // Update the views on the screen with the values from the database
            mNumberEditText.setText(licenceNumber);
            mStartDateEditText.setText(licenceStart);
            mEndDateEditText.setText(licenceEnd);
            mDurationSpinner.setValue(licenceDuration);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNumberEditText.setText("");
        mStartDateEditText.setText("");
        mEndDateEditText.setText("");
        mDurationSpinner.setValue(1);

    }

    @OnClick(R.id.bt_save_licence)
    public void saveLicence() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String licenceNumber = mNumberEditText.getText().toString().trim();
        String startDate = mStartDateEditText.getText().toString().trim();
        String endDate = mEndDateEditText.getText().toString().trim();
        String duration = String.valueOf(mDurationSpinner.getValue());
        String isPaid = "Paid";

        // Check if this is supposed to be a new licence
        // and check if all the fields in the editor are blank
        if (mCurrentLicenceUri == null
                && TextUtils.isEmpty(licenceNumber)
                && TextUtils.isEmpty(startDate)
                && TextUtils.isEmpty(endDate)
                && TextUtils.isEmpty(duration)
                && TextUtils.isEmpty(isPaid)) {
            // Since no fields were modified, we can return early without creating a new licence.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and licence attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(LicenceEntry.COLUMN_LICENCE_NUMBER, licenceNumber);
        values.put(LicenceEntry.COLUMN_LICENCE_START, startDate);
        values.put(LicenceEntry.COLUMN_LICENCE_END, endDate);
        values.put(LicenceEntry.COLUMN_LICENCE_PAID, LicenceEntry.UNPAID);
        values.put(LicenceEntry.COLUMN_LICENCE_DURATION, duration);

        // Determine if this is a new or existing licence by checking if mCurrentPetUri is null or not
        if (mCurrentLicenceUri == null) {
            // This is a NEW licence, so insert a new licence into the provider,
            // returning the content URI for the new licence.
            Uri newUri = getContentResolver().insert(LicenceEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_licence_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_licence_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING licence, so update the licence with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentLicenceUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_licence_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_licence_successful), Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mLicenceChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.string_unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
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
