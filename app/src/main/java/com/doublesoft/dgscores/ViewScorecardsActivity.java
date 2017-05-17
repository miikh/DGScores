package com.doublesoft.dgscores;

import android.os.Bundle;

/**
 * Created by Miika on 16.5.2017.
 */

public class ViewScorecardsActivity extends ScorecardsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_scorecard);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
