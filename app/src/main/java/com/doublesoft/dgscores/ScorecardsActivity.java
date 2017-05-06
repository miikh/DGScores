package com.doublesoft.dgscores;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ScorecardsActivity extends AppCompatActivity {

    Context context;
    Cursor scorecards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecards);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();

        scorecards = db.getScorecards();

        ListView listView = (ListView) findViewById(R.id.scorecards_listview);

        scorecards.moveToFirst();
    }
}
