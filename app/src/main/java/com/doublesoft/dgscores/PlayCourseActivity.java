package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayCourseActivity extends AppCompatActivity {

    Context context;
    DatabaseAdapter db;
    SimpleCursorAdapter adapter;
    Cursor players;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_course);

        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        players = db.getPlayers();
        players.moveToFirst();

        listView = (ListView) findViewById(R.id.choose_players_listview);

        adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_checked, players, new String[]{"NAME"}, new int[]{android.R.id.text1},0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView cb = ((CheckedTextView)view);
                if(!cb.isChecked()){
                    cb.setChecked(true);
                }
                else { cb.setChecked(false); }
            }
        });

        listView.setAdapter(adapter);

        FloatingActionButton addPlayer = (FloatingActionButton) findViewById(R.id.btnAddPlayerPlayCourse);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAddPlayer();
            }
        });

        FloatingActionButton chooseCourse = (FloatingActionButton) findViewById(R.id.btnChooseCourse);
        chooseCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChooseCourse();
            }
        });

    }

    private void setAddPlayer(){
        final EditText t = new EditText(context);
        t.setHint("Name");
        AlertDialog dialog = new AlertDialog.Builder(context)
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
                            players = db.getPlayers();
                            adapter.swapCursor(players);
                            //Toast.makeText(context, "Player " + name + " added", Toast.LENGTH_SHORT).show();
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

    private void setChooseCourse(){
        ArrayList<CheckedTextView> playerList = new ArrayList();
        Cursor c = adapter.getCursor();
        c.moveToFirst();
    }
}
