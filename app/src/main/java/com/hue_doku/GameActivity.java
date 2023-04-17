package com.hue_doku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.hue_doku.R;
import com.hue_doku.game.Cell;
import com.hue_doku.view.custom.HuedokuBoardView;
import com.hue_doku.viewmodel.HuedokuViewModel;
import com.hue_doku.viewmodel.MyViewModelFactory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements HuedokuBoardView.OnTouchListener, View.OnClickListener {
    private HuedokuViewModel viewModel;
    private final Button[] buttons = new Button[9];
    private ImageButton notesButton;
    Integer difficulty = 1;

    TextView difficultyTitle;
    TextView timerText;
    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    String timeString;

    String bestTime;
    String timeKey = "";

    int colorPaletteSelection;

    boolean isContinuedGame = false;
    boolean isComplete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Find user selected color palette
        colorPaletteSelection = prefs.getInt("ColorPalette", 1);

        // Check if there is a game in progress (difficulty of 6)
        difficulty = getIntent().getIntExtra("Difficulty", 1);
        if(difficulty == 6) {
            difficulty = prefs.getInt("Difficulty", 1);
            time = Double.longBitsToDouble(prefs.getLong("time", prefs.getLong("time", 0L)));
            isContinuedGame=true;
        }

        // Remove memory of saved game
        prefs.edit().remove("Difficulty").apply();

        // Check user selected difficulty and update time save key and activity title accordingly
        String titleText = "";
        switch (difficulty) {
            case 1: timeKey = "BeginnerTime";
            titleText = "Beginner";
            break;
            case 2: timeKey = "IntermediateTime";
                titleText = "Intermediate";
            break;
            case 3: timeKey = "AdvancedTime";
                titleText = "Advanced";
            break;
            case 4: timeKey = "ExpertTime";
                titleText = "Expert";
            break;
            case 5: timeKey = "InsaneTime";
                titleText = "Insane";
            break;
        }

        // Get the best time for the selected difficulty to be displayed upon a completed game
        bestTime = prefs.getString(timeKey, "No Previous Times");

        setContentView(R.layout.activity_game);

        // Initialize timer text view to be updated later
        timerText = findViewById(R.id.timerText);

        // Set title text of the activity
        difficultyTitle = findViewById(R.id.difficultyTitle);
        difficultyTitle.setText(titleText);

        timer = new Timer();

        // Initialize all buttons
        buttons[0] = findViewById(R.id.oneButton);
        buttons[1] = findViewById(R.id.twoButton);
        buttons[2] = findViewById(R.id.threeButton);
        buttons[3] = findViewById(R.id.fourButton);
        buttons[4] = findViewById(R.id.fiveButton);
        buttons[5] = findViewById(R.id.sixButton);
        buttons[6] = findViewById(R.id.sevenButton);
        buttons[7] = findViewById(R.id.eightButton);
        buttons[8] = findViewById(R.id.nineButton);
        notesButton = findViewById(R.id.notesButton);
        ImageButton eraseButton = findViewById(R.id.eraseButton);


        // Create click listeners and set background colors for all buttons
        for(int i = 0; i < 9; i++) {
            buttons[i].setOnClickListener(this);
            buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
        }
        notesButton.setOnClickListener(view -> viewModel.game.toggleTakingNotes());
        eraseButton.setOnClickListener(view -> viewModel.game.delete());

        // Initialize and attach ontouchlistener to the board view
        HuedokuBoardView boardView = findViewById(R.id.sudokuBoard);
        boardView.registerListener(this);

        // Initialize viewModel (with difficulty 6 if continued game)
        if(isContinuedGame) viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication(), 6, getApplicationContext())).get(HuedokuViewModel.class);
        else viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication(), difficulty, getApplicationContext())).get(HuedokuViewModel.class);

        // Set up observers for live data in the HuedokuGame stored in the viewModel for UI updating
        viewModel.game.activeNotesLiveData.observe(this, this::updateHighlightedKeysUI);
        viewModel.game.isTakingNotesLiveData.observe(this, this::updateNoteTakingUI);
        viewModel.game.selectedCellLiveData.observe(this, this::updateSelectedCellUI);
        viewModel.game.cellsLiveData.observe(this, this::updateCells);
        viewModel.game.numMistakesLiveData.observe(this, numMistakes -> {
            if(numMistakes >= 3) triggerGameOver();
            else updateNumMistakes(numMistakes);
        });
        viewModel.game.correctNumsLiveData.observe(this, this::updateCompletedKeysUI);

        int numCells = 81;
        viewModel.game.totalNumCorrectLiveData.observe(this, numCorrect -> {
            if(numCorrect == numCells) triggerGameComplete();});
        boardView.setCorrectValues(viewModel.game.getSolvedGrid());
    }


    /**
     * Creates an AlertDialog when game is completed with statistics and stores relevant data
     */
    private void triggerGameComplete() {
        timeString =(String) timerText.getText();
        isComplete = true;
        timerTask.cancel();
        AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
        String newBest = "";
        if(betterTime()) {
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString(timeKey, timeString).apply();
            newBest = " New Best Time!";
        }
        dialog.setTitle("Congratulations! Game Complete");
        dialog.setMessage("Time: " + timeString + "\nPrevious Best: " + bestTime + "\n" +newBest);
        dialog.setPositiveButton("New Game", (dialogInterface, i) -> {
            Intent intent = new Intent(getBaseContext(), GameActivity.class);
            intent.putExtra("Difficulty", difficulty);
            startActivity(intent);
            finish();
        });
        dialog.setNegativeButton("Home", (dialogInterface, i) -> {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        });
        dialog.show();
    }

    /**
     * Used to check if there is a new fastest time that needs to be stored for future games
     * @return Whether or not the time it took to complete the current HueDoku game is less than the
     * previous stored time for that difficulty level
     */
    private boolean betterTime() {
        if(bestTime.equals("No Previous Times")) return true;
        if(timeString.charAt(0) < bestTime.charAt(0))
            return true;
        else if(timeString.charAt(0) == bestTime.charAt(0)) {
            if (timeString.charAt(1) < bestTime.charAt(1))
                return true;
            else if (timeString.charAt(1) == bestTime.charAt(1)) {
                if(timeString.charAt(3) < bestTime.charAt(3))
                    return true;
                else if (timeString.charAt(3) == bestTime.charAt(3)) {
                    return timeString.charAt(4) < bestTime.charAt(4);
                }
            }
        }
        return false;
    }

    /**
     * Generated AlertDialog to notify the user that the game is over and allows them to start a new
     * game of the same difficulty or navigate to the home screen
     */
    public void triggerGameOver() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
        isComplete = true;
        dialog.setTitle("Game Over!");
        dialog.setPositiveButton("New Game", (dialogInterface, i) -> {
            Intent intent = new Intent(getBaseContext(), GameActivity.class);
            intent.putExtra("Difficulty", difficulty);
            startActivity(intent);
            finish();
        });
        dialog.setNegativeButton("Home", (dialogInterface, i) -> {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        });
        dialog.show();
    }

    /**
     * Update cells in the UI
     * @param cells 2x2 array of cells to be used to update the huedoku board UI
     */
    private void updateCells(Cell[][] cells) {
        HuedokuBoardView huedokuBoardView = findViewById(R.id.sudokuBoard);
        huedokuBoardView.updateCells(cells);
    }

    /**
     * Sets the cell selected by the user to be used to update the huedoku baord UI
     * @param cell the currently selected cell
     */
    private void updateSelectedCellUI(Pair<Integer,Integer> cell) {
        if (cell == null) return;
        HuedokuBoardView huedokuBoardView = findViewById(R.id.sudokuBoard);
        huedokuBoardView.updateSelectedCellUI(cell.first,cell.second);
    }

    /**
     * Sets whether or not the user is taking notes and updates the notesButton appearance accordingly
     * @param isTakingNotes true if user is taking notes and false otherwise
     */
    private void updateNoteTakingUI(boolean isTakingNotes) {
        notesButton.setColorFilter(isTakingNotes ? Color.GREEN : Color.BLACK, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * Given a set of integers which represents the notes associated with the selected cell, make
     * the corresponding buttons appear dark gray if they are in the set and their normal color
     * otherwise
     * @param notes Set of integers which are the notes attached to the highlighted cell
     */
    private void updateHighlightedKeysUI(HashSet<Integer> notes) {
        Integer[] totalEachNum = viewModel.game.correctNumsLiveData.getValue();
        if(notes == null) return;
        for(int i = 0; i < 9; i++)
            if(notes.contains(i+1) || Objects.requireNonNull(totalEachNum)[i] >= 9)
                buttons[i].setBackgroundColor(Color.DKGRAY);
            else
                buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
    }

    /**
     * Given a list of the number of correct cells associated with a given color, make the
     * corresponding color button dark gray if all of the cells of that color are correctly colored
     * @param totals a 9-element array of integers which represent the number of correctly colored
     *               squares for each color
     */
    private void updateCompletedKeysUI(Integer[] totals) {
        if(totals == null) return;
        for(int i = 0; i < 9; i++)
            if(totals[i] >= 9)
                buttons[i].setBackgroundColor(Color.DKGRAY);
            else
                buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
    }

    /**
     * Update the text of the mistakes textView with the given number of mistakes
     * @param numMistakes the number of mistakes to be displayed in the activity
     */
    private void updateNumMistakes(int numMistakes) {
        TextView mistakesText = findViewById(R.id.mistakesText);
        mistakesText.setText(String.format(getResources().getString(R.string.mistakesText), numMistakes));
    }


    /**
     * Returns the correct color given the palette the user has selected and the index of the color
     * (0-8) as there are 9 distinct huedoku colors
     * @param selectedPalette the number of the palette user has selected (currently only 2 palettes possible)
     * @param i the index of the color you want to get from the specified palette
     * @return the hexidecimal code corresponding to the desired color
     */
    public int getButtonColor(int selectedPalette, int i) {
        if(selectedPalette == 2) {
            switch (i) {
                case 0: return getResources().getColor(R.color.sudokuAlt11);
                case 1: return getResources().getColor(R.color.sudokuAlt12);
                case 2: return getResources().getColor(R.color.sudokuAlt13);
                case 3: return getResources().getColor(R.color.sudokuAlt14);
                case 4: return getResources().getColor(R.color.sudokuAlt15);
                case 5: return getResources().getColor(R.color.sudokuAlt16);
                case 6: return getResources().getColor(R.color.sudokuAlt17);
                case 7: return getResources().getColor(R.color.sudokuAlt18);
                case 8: return getResources().getColor(R.color.sudokuAlt19);
                default: return -1;
            }
        }
        else if (selectedPalette == 3) {
            switch (i) {
                case 0: return getResources().getColor(R.color.sudokuAlt21);
                case 1: return getResources().getColor(R.color.sudokuAlt22);
                case 2: return getResources().getColor(R.color.sudokuAlt23);
                case 3: return getResources().getColor(R.color.sudokuAlt24);
                case 4: return getResources().getColor(R.color.sudokuAlt25);
                case 5: return getResources().getColor(R.color.sudokuAlt26);
                case 6: return getResources().getColor(R.color.sudokuAlt27);
                case 7: return getResources().getColor(R.color.sudokuAlt28);
                case 8: return getResources().getColor(R.color.sudokuAlt29);
                default: return -1;
            }
        }
        else {
            switch (i) {
                case 0: return getResources().getColor(R.color.sudokuDefault1);
                case 1: return getResources().getColor(R.color.sudokuDefault2);
                case 2: return getResources().getColor(R.color.sudokuDefault3);
                case 3: return getResources().getColor(R.color.sudokuDefault4);
                case 4: return getResources().getColor(R.color.sudokuDefault5);
                case 5: return getResources().getColor(R.color.sudokuDefault6);
                case 6: return getResources().getColor(R.color.sudokuDefault7);
                case 7: return getResources().getColor(R.color.sudokuDefault8);
                case 8: return getResources().getColor(R.color.sudokuDefault9);
                default: return -1;
            }
        }

    }


    @Override
    public void onCellTouched(int row, int col) {
        viewModel.game.updateSelectedCell(row, col);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.oneButton: {
                viewModel.game.handleInput(1);
                break;
            }
            case R.id.twoButton: {
                viewModel.game.handleInput(2);
                break;
            }
            case R.id.threeButton: {
                viewModel.game.handleInput(3);
                break;
            }
            case R.id.fourButton: {
                viewModel.game.handleInput(4);
                break;
            }
            case R.id.fiveButton: {
                viewModel.game.handleInput(5);
                break;
            }
            case R.id.sixButton: {
                viewModel.game.handleInput(6);
                break;
            }
            case R.id.sevenButton: {
                viewModel.game.handleInput(7);
                break;
            }
            case R.id.eightButton: {
                viewModel.game.handleInput(8);
                break;
            }
            case R.id.nineButton: {
                viewModel.game.handleInput(9);
                break;
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
        isComplete = false;
    }
    public void startTimer() {
        if(timerText == null) return;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    time++;
                    timerText.setText(getTimerText());
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
    public String getTimerText() {
        int timeRound = (int) Math.round(time);
        int seconds = ((timeRound % 86400) % 3600) % 60;
        int minutes = ((timeRound % 86400) % 3600) / 60;
        return String.format(Locale.ENGLISH, "%02d", minutes) + ":" + String.format(Locale.ENGLISH, "%02d",seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(isComplete) {
            prefs.edit().remove("inProgressBoard").apply();
            prefs.edit().remove("completeBoard").apply();
            prefs.edit().remove("Difficulty").apply();
            prefs.edit().remove("time").apply();
            prefs.edit().remove("Mistakes").apply();
        }
        else {
            String inProgressString = gridToString(Objects.requireNonNull(viewModel.game.cellsLiveData.getValue()));
            String completeInProgressString = gridToString(viewModel.game.getSolvedGrid());


            prefs.edit().putString("inProgressBoard", inProgressString).apply();
            prefs.edit().putString("completeBoard", completeInProgressString).apply();
            prefs.edit().putInt("Difficulty", difficulty).apply();
            prefs.edit().putLong("time", Double.doubleToRawLongBits(time)).apply();
            if(viewModel.game.numMistakesLiveData.getValue() != null)
                prefs.edit().putInt("Mistakes", viewModel.game.numMistakesLiveData.getValue()).apply();
        }
    }

    public String gridToString(int[][] grid) {
        StringBuilder gridString = new StringBuilder();
        for(int[] gridRow : grid) {
            for (int val : gridRow)
                gridString.append(val);
        }

        return gridString.toString();
    }
    public String gridToString(Cell[][] grid) {
        StringBuilder gridString = new StringBuilder();
        for(Cell[] cellRow : grid) {
            for (Cell cell : cellRow)
                gridString.append(cell.getValue());
        }

        return gridString.toString();
    }

}