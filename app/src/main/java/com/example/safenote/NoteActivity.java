package com.example.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Button addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO add a location
            }
        });
        Button viewLocation = findViewById(R.id.view_location);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO send an intent, jump to some map app?
            }
        });

        Button finish = findViewById(R.id.finish_note);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO store the note
            }
        });

    }
}