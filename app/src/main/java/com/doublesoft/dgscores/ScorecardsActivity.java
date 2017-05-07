package com.doublesoft.dgscores;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

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

        TextView testView = (TextView) findViewById(R.id.scorecards_text);

        scorecards.moveToFirst();

        testView.append(Integer.toString(scorecards.getCount()));
    }
}
