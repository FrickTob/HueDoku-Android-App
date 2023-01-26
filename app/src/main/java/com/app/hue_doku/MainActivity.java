package com.app.hue_doku;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if(displayMetrics.widthPixels >= 1200) // tablet
            setContentView(R.layout.activity_main_tablet);
        else // phone
            setContentView(R.layout.activity_main_phone);
        String[] difficulties = {"Beginner",
                "Intermediate",
                "Advanced",
                "Expert",
                "Insane"};
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
            }
        });


        final ImageButton customizeButton = findViewById(R.id.customizeButton);
        customizeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        customizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked!");
                createCustomizePopUp(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String inProgressBoard = prefs.getString("inProgressBoard", "");

        final Button continueButton = findViewById(R.id.continueButton);
        continueButton.setVisibility(View.GONE);
        if(!inProgressBoard.equals("")) {
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), GameActivity.class);
                    intent.putExtra("Difficulty", 6);
                    startActivity(intent);
                }
            });
        }
    }
    private void createCustomizePopUp(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popUpView = getLayoutInflater().inflate(R.layout.popup_customize, null);
        dialogBuilder.setView(popUpView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        ImageButton closeButton = popUpView.findViewById(R.id.customizePopupCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        RelativeLayout defaultColorBox = popUpView.findViewById(R.id.defaultPaletteBox);
        defaultColorBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                prefs.edit().putInt("ColorPalette", 1).apply();
            }
        });

        RelativeLayout altColorBox1 = popUpView.findViewById(R.id.altPaletteBox1);
        altColorBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                prefs.edit().putInt("ColorPalette", 2).apply();
            }
        });
        if(prefs.getInt("ColorPalette", 1) == 2) {
            altColorBox1.setBackground(getResources().getDrawable(R.drawable.selectedboxborder));
            defaultColorBox.setBackground(getResources().getDrawable(R.drawable.border));
        }
        else {
            altColorBox1.setBackground(getResources().getDrawable(R.drawable.border));
            defaultColorBox.setBackground(getResources().getDrawable(R.drawable.selectedboxborder));
        }
    }
}