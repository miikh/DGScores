package com.doublesoft.dgscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Players extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnAdd_player = (Button) findViewById(R.id.btnAdd_player);

        btnAdd_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Players.this, AddPlayer.class);
                Players.this.startActivity(i);
            }
        });
    }


}
