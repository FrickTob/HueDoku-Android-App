package com.hue_doku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
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

import com.app.hue_doku.R;
import com.hue_doku.viewmodel.HuedokuViewModel;
import com.hue_doku.viewmodel.MyViewModelFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Find dimensions of the screen running the app and change the layout accordingly
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

        // Initialize and create click listener for the play button that starts a new GameActivity
        final Button playButton = findViewById(R.id.homePlayButton);
        playButton.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Choose Difficulty");
            dialog.setItems(difficulties, (dialog1, which) -> {
                Intent intent = new Intent(view.getContext(), GameActivity.class);
                intent.putExtra("Difficulty", which + 1);
                startActivity(intent);
            });
            dialog.show();
        });


        // Initialize and set on click listener for the customize button
        final ImageButton customizeButton = findViewById(R.id.customizeButton);
        customizeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        customizeButton.setOnClickListener(view -> {
            createCustomizePopUp();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check to see if there is a saved game
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String inProgressBoard = prefs.getString("inProgressBoard", "");

        final Button continueButton = findViewById(R.id.continueButton);
        continueButton.setVisibility(View.GONE);

        // if there is a saved game, show continue button and make it resume game on click
        if(!inProgressBoard.equals("")) {
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), GameActivity.class);
                intent.putExtra("Difficulty", 6);
                startActivity(intent);
            });
        }
    }

    /**
     * Method to generate the customize pop up which allows the user to select their desired color
     * palette
     */
    private void createCustomizePopUp() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popUpView = getLayoutInflater().inflate(R.layout.popup_customize, null);
        dialogBuilder.setView(popUpView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        ImageButton closeButton = popUpView.findViewById(R.id.customizePopupCloseButton);
        closeButton.setOnClickListener(view -> dialog.dismiss());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        RelativeLayout defaultColorBox = popUpView.findViewById(R.id.defaultPaletteBox);
        defaultColorBox.setOnClickListener(view -> {
            dialog.dismiss();
            prefs.edit().putInt("ColorPalette", 1).apply();
        });

        RelativeLayout altColorBox1 = popUpView.findViewById(R.id.altPaletteBox1);
        altColorBox1.setOnClickListener(view -> {
            dialog.dismiss();
            prefs.edit().putInt("ColorPalette", 2).apply();
        });

        RelativeLayout altColorBox2 = popUpView.findViewById(R.id.altPaletteBox2);
        altColorBox2.setOnClickListener(view -> {
            dialog.dismiss();
            prefs.edit().putInt("ColorPalette", 3).apply();
        });

        // change the background of the color palette options depending on the currently selected palette
        if(prefs.getInt("ColorPalette", 1) == 2) {
            altColorBox1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.selectedboxborder, null));
            altColorBox2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
            defaultColorBox.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
        }
        else if (prefs.getInt("ColorPalette", 1) == 3) {
            altColorBox1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
            altColorBox2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.selectedboxborder, null));
            defaultColorBox.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
        }
        else {
            altColorBox1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
            altColorBox2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, null));
            defaultColorBox.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.selectedboxborder, null));
        }
    }
}