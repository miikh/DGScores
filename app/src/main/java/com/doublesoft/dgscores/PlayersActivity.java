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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PlayersActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);
        context = getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();

        Cursor players = db.getPlayers();
        final TextView testView = (TextView) findViewById(R.id.textView);

        players.moveToFirst();
        testView.append(Integer.toString(players.getCount()) + "\n");
        for(int i=0;i<players.getCount();i++){
            testView.append(players.getString(players.getColumnIndex("NAME")) + "\n");
            players.moveToNext();
        }

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
                                    db.insertPlayers(cv);
                                    testView.append(name + "\n");
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
