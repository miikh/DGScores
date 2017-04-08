package com.doublesoft.dgscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Players extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton btnAddPlayer = (FloatingActionButton) findViewById(R.id.btnAddPlayer);
    }


}
