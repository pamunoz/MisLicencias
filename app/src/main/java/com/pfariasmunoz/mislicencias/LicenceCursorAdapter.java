package com.pfariasmunoz.mislicencias;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pfariasmunoz.mislicencias.data.LicenceContract.LicenceEntry;

/**
 * {@link LicenceCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of licence data as its data source. This adapter knows
 * how to create list items for each row of licence data in the {@link Cursor}.
 */
public class LicenceCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link LicenceCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public LicenceCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
    }

    /**
     * This method binds the licence data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the number for the current licence can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView numberTextView = (TextView) view.findViewById(R.id.licence_number);
        TextView durationTextView = (TextView) view.findViewById(R.id.licence_duration);
        TextView startTextView = (TextView) view.findViewById(R.id.licence_start);
        TextView endTextView = (TextView) view.findViewById(R.id.licence_end);
        TextView isPaidView = (TextView) view.findViewById(R.id.licence_is_paid);

        // Find the column of the pets attributes that we're interested in
        int numberColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_NUMBER);
        int durationColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_DURATION);
        int startColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_START);
        int endColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_END);
        int isPaidColumnIndex = cursor.getColumnIndex(LicenceEntry.COLUMN_LICENCE_PAID);

        // Read the pet attributes from the Cursor for the current licence
        String licenceNumber = cursor.getString(numberColumnIndex);
        Integer licenceDuration = cursor.getInt(durationColumnIndex);
        String startLicence = cursor.getString(startColumnIndex);
        String endLicence = cursor.getString(endColumnIndex);
        String isPaidLicence = cursor.getString(isPaidColumnIndex);

        // Update the TextViews with the attributes for the current pet
        numberTextView.setText(licenceNumber);
        durationTextView.setText(String.valueOf(licenceDuration));
        startTextView.setText(startLicence);
        endTextView.setText(endLicence);
        String paidValue = context.getResources().getString(R.string.licence_paid);
        String unpaidValue = context.getResources().getString(R.string.licence_unpaid);
        int value = Integer.parseInt(isPaidLicence);
        if (value == 0) {
            isPaidView.setText(unpaidValue);
        }
        if (value == 1) {
            isPaidView.setText(paidValue);
        }

    }
}
