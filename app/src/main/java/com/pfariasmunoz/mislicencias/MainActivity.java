package com.pfariasmunoz.mislicencias;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pfariasmunoz.mislicencias.data.LicenceContract.LicenceEntry;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies a particular Loader being used in this component
    private static final int LICENCE_LOADER = 0;

    LicenceCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(editorIntent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView licenceListView = (ListView) findViewById(R.id.list);

        // Set up an Adapter to create a list item for each row of licence data in the Cursor.
        // There is no licence data yet (untill the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new LicenceCursorAdapter(this, null);
        licenceListView.setAdapter(mCursorAdapter);

        // Set up item click listener
        licenceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new Intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                // Form the content URI that represent the specific pet that was clicked on,
                // by appending the "id" (passed as imput to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentUri = ContentUris.withAppendedId(LicenceEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentUri);

                // Launch the {@link EditorActivity} to display the data for the current licence.
                startActivity(intent);
            }
        });

        // kick off the loader
        getLoaderManager().initLoader(LICENCE_LOADER, null, this);
    }

    /*
   * Callback that's invoked when the system has initialized the Loader and
   * is ready to start the query. This usually happens when initLoader() is
   * called. The loaderID argument contains the ID value passed to the
   * initLoader() call.
   */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        // Define a projection that specifies the columns from the table that we care about
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
                LicenceEntry.CONTENT_URI, // Query the content URI for the current pet
                projection,         // Columns to include in the resulting Cursor
                null,               // No selection clause
                null,               // No selection arguments
                null);              // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link LicenceCursorAdapter} with this new cursor containing updated licence data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data need to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
