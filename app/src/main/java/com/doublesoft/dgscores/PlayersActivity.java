package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PlayersActivity extends AppCompatActivity {

    Context context;
    DatabaseAdapter db;
    Cursor players;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        ListView listView = (ListView) findViewById(R.id.players_listview);

        players = db.getPlayers();
        players.moveToFirst();
        //adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, players, new String[]{"NAME"}, new int[]{android.R.id.text1}, 0);
        adapter = new PlayerAdapter(context, android.R.layout.simple_list_item_1, players, new String[]{"NAME"}, new int[]{android.R.id.text1}, 0);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView) view).getText().toString();
                Toast.makeText(context, "Katso pelaajan " + name + " statistiikka", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton btnAddPlayer = (FloatingActionButton) findViewById(R.id.btnAddPlayer);

        btnAddPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              setAddPlayerButton();
            }
        });

        registerForContextMenu(listView);

    }

    void setAddPlayerButton(){
        final EditText t = new EditText(context);
        t.setHint("Name");
        AlertDialog dialog = new AlertDialog.Builder(PlayersActivity.this)
                .setTitle("Add Player")
                .setIcon(android.R.drawable.ic_input_add)
                .setView(t)
                .setPositiveButton("Add Player", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = t.getText().toString().trim();
                        if(name.length() > 0 ) {
                            ContentValues cv = new ContentValues();
                            cv.put("NAME", name);
                            cv.put("DELETED", 0);
                            if(db.insertPlayers(cv)) {
                                players = db.getPlayers();
                                adapter.swapCursor(players);
                                Toast.makeText(context, "Player " + name + " added", Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(context, "Player already found!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if(item.getItemId() ==  R.id.delete)
        {
            DatabaseAdapter db = new DatabaseAdapter(context);
            db.open();
            long id = Long.parseLong(menuInfo.targetView.getTag().toString());
            db.deletePlayer(id);
            players = db.getPlayers();
            adapter.swapCursor(players);
        }
        else return false;
        return true;
    }

    private class PlayerAdapter extends SimpleCursorAdapter{

        LayoutInflater inflater;

        public PlayerAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            view.setTag(cursor.getString(0));
            TextView name = (TextView)view;
            name.setText(cursor.getString(1));
        }
    }

}
