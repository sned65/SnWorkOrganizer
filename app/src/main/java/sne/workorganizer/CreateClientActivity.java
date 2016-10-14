package sne.workorganizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;

public class CreateClientActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);

        startActionMode(new ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                getMenuInflater().inflate(R.menu.menu_editor_actions, menu);
                //mode.setTitle("New Client");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                switch (item.getItemId())
                {
                case R.id.btn_cancel:
                    cancel();
                    return true;
                case R.id.btn_save:
                    save();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {

            }
        });
    }

    private void cancel()
    {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void save()
    {
        TextView nameView = (TextView) findViewById(R.id.client_name);
        TextView phoneView = (TextView) findViewById(R.id.client_phone);
        TextView socialView = (TextView) findViewById(R.id.client_social);
        TextView emailView = (TextView) findViewById(R.id.client_email);

        Client client = new Client();
        client.setName(nameView.getText().toString());
        client.setPhone(phoneView.getText().toString());
        client.setSocial(socialView.getText().toString());
        client.setEmail(emailView.getText().toString());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.createClient(client);

        Intent result = new Intent();
        result.putExtra(ClientListActivity.KEY_NEW_CLIENT, client);
        setResult(RESULT_OK, result);
        finish();
    }
}
