package com.example.hue_doku;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] difficulties = {"Beginner", "Intermediate", "Advanced", "Expert", "Insane"};

        final Button playButton = findViewById(R.id.homePlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Choose Difficulty");
                dialog.setItems(difficulties, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(view.getContext(), GameActivity.class);
                        intent.putExtra("Difficulty", which + 1);
                        startActivity(intent);
                    }
                } );
                dialog.show();

               // Intent intent = new Intent(view.getContext(), GameActivity.class);
               // startActivity(intent);

            }
        });
    }
}