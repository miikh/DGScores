package com.doublesoft.dgscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class AddPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button confirm_button = (Button) findViewById(R.id.confirm_button);
        Button cancel_button = (Button) findViewById(R.id.cancel_button);

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddPlayer.this, PlayersActivity.class);
                AddPlayer.this.startActivity(i);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(AddPlayer.this, PlayersActivity.class);
                AddPlayer.this.startActivity(i);
            }
        });
    }
}
