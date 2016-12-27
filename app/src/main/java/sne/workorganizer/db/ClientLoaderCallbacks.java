package sne.workorganizer.db;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

/**
 * Implementation of interface the LoaderManager will call
 * to report about changes in the state of the client loader.
 */
public class ClientLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = ClientLoaderCallbacks.class.getName();
    private final Spinner _clientSelector;
    private IdNamePair _selectedClient;

    public ClientLoaderCallbacks(Spinner clientSelector)
    {
        _clientSelector = clientSelector;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(_clientSelector.getContext(), DatabaseContract.Clients.CONTENT_URI,
                new String[] { DatabaseHelper.CLIENTS_PK+" AS _id", DatabaseHelper.CLIENTS_COL_FULLNAME },
                null, null, DatabaseHelper.CLIENTS_COL_FULLNAME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        SimpleCursorAdapter clientsAdapter = (SimpleCursorAdapter) _clientSelector.getAdapter();
        clientsAdapter.swapCursor(data);
        setSelectedClient(_selectedClient);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        SimpleCursorAdapter clientsAdapter = (SimpleCursorAdapter) _clientSelector.getAdapter();
        clientsAdapter.swapCursor(null);
    }

    public IdNamePair getSelectedClient()
    {
        _selectedClient = new IdNamePair();
        _selectedClient.setId(((Cursor) _clientSelector.getSelectedItem()).getString(0));
        _selectedClient.setName(((Cursor) _clientSelector.getSelectedItem()).getString(1));
        Log.i(TAG, "save() client_name = "+_selectedClient.getName());
        return _selectedClient;
    }

    public void setSelectedClient(IdNamePair selectedClient)
    {
        _selectedClient = selectedClient;
        if (_selectedClient != null)
        {
            SimpleCursorAdapter clientsAdapter = (SimpleCursorAdapter) _clientSelector.getAdapter();
            Cursor data = clientsAdapter.getCursor();
            if (data == null) return;

            data.moveToFirst();
            int position = 0;
            do
            {
                String id = data.getString(data.getColumnIndex("_id"));
                Log.i(TAG, "onLoadFinished() Client " + data.getString(1));
                if (_selectedClient.getId().equals(id))
                {
                    Log.i(TAG, "onLoadFinished() new client position " + position);
                    _clientSelector.setSelection(position);
                    break;
                }
                ++position;
            }
            while (data.moveToNext());
        }
    }
}
