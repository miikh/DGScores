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
import android.view.View;
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
    Cursor players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();

        players = db.getPlayers();

        ListView listView = (ListView) findViewById(R.id.players_listview);

        //final TextView testView = (TextView) findViewById(R.id.textView);

        players.moveToFirst();
        //testView.append(Integer.toString(players.getCount()) + "\n");
        /*
        for(int i=0;i<players.getCount();i++){
            testView.append(players.getString(players.getColumnIndex("NAME")) + "\n");
            players.moveToNext();
        }*/

        players.moveToFirst();
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, players, new String[]{"NAME"}, new int[]{android.R.id.text1}, 0);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

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
                                    cv.put("name", name);
                                    cv.put("deleted", 0);
                                    db.insertPlayers(cv);
                                    //testView.append(name + "\n");
                                    players = db.getPlayers();
                                    adapter.swapCursor(players);
                                    Toast.makeText(context, "Player " + name + " added", Toast.LENGTH_SHORT).show();
                                }
                                //else { t.setError("Set player name"); }
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
        });
    }


}
