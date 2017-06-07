package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Sisältää kierroksen aloittamisen, eli pelaajan ja radan valinnan toiminnallisuudet
 */
public class StartCourseActivity extends AppCompatActivity {

    Context context;
    DatabaseAdapter db;
    CustomCursorAdapter adapter;
    Cursor playersCursor;
    ListView listView;

    /**
     * Alustaa pelaajien valintanäkymän hakemalla tietokantaan lisätyt pelaajat
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_course);

        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        playersCursor = db.getPlayers();
        playersCursor.moveToFirst();

        listView = (ListView) findViewById(R.id.choose_players_listview);

        adapter = new CustomCursorAdapter(context, android.R.layout.simple_list_item_checked, playersCursor, new String[]{"NAME"}, new int[]{android.R.id.text1},0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView cb = ((CheckedTextView)view);
                if(!adapter.players.contains(cb.getText().toString())){
                    adapter.players.add(cb.getText().toString());
                }
                else {
                    adapter.players.remove(cb.getText().toString());}
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

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("gameId", 0);
        setResult(Activity.RESULT_OK, i);
        playersCursor.close();
        db.close();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * Pelaajan lisäämispainikkeen dialogi
     */
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
                            cv.put("NAME", name);
                            cv.put("DELETED", 0);
                            if(db.insertPlayers(cv)) {
                                playersCursor = db.getPlayers();
                                adapter.swapCursor(playersCursor);
                                //Toast.makeText(context, "Player " + name + " added", Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(context, "Player already found!", Toast.LENGTH_LONG).show();
                            }
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

    /**
     * Etenemispainikkeen tarkistus, onko riittävästi pelaajia valittu
     */
    private void setChooseCourse(){
        if(adapter.players.size() > 0){
            Intent i = new Intent(StartCourseActivity.this, ChooseCourseActivity.class);
            i.putExtra("players", adapter.players);
            i.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            playersCursor.close();
            db.close();
            this.startActivity(i);
        }
        else {Toast.makeText(context, "Choose players first!", Toast.LENGTH_LONG).show();}

    }

    private class CustomCursorAdapter extends SimpleCursorAdapter{

        ArrayList<String> players;

        CustomCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            players = new ArrayList<>();
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            CheckedTextView v = (CheckedTextView) view;

            if(players.contains(v.getText().toString())){
                v.setChecked(true);
            }
            else{
                v.setChecked(false);
            }

        }
    }
}
