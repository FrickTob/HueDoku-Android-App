package com.app.hue_doku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.app.hue_doku.game.Cell;
import com.app.hue_doku.view.custom.SudokuBoardView;
import com.app.hue_doku.viewmodel.MyViewModelFactory;
import com.app.hue_doku.viewmodel.SudokuViewModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements SudokuBoardView.OnTouchListener , View.OnClickListener {

    private SudokuViewModel viewModel;
    private final Button[] buttons = new Button[9];
    private ImageButton notesButton;
    private ImageButton eraseButton;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        colorPaletteSelection = prefs.getInt("ColorPalette", 1);
        difficulty = getIntent().getIntExtra("Difficulty", 1);
        if(difficulty == 6) {
            difficulty = prefs.getInt("Difficulty", 1);
            time = Double.longBitsToDouble(prefs.getLong("time", prefs.getLong("time", 0L)));
            isContinuedGame=true;
        }
        prefs.edit().remove("Difficulty").apply();
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
        bestTime = prefs.getString(timeKey, "No Previous Times");

        setContentView(R.layout.activity_game);

        timerText = findViewById(R.id.timerText);
        difficultyTitle = findViewById(R.id.difficultyTitle);

        difficultyTitle.setText(titleText);

        timer = new Timer();

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
        eraseButton = findViewById(R.id.eraseButton);



        for(int i = 0; i < 9; i++) {
            buttons[i].setOnClickListener(this);
            buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
        }


        notesButton.setOnClickListener(view -> viewModel.game.toggleTakingNotes());

        eraseButton.setOnClickListener(view -> viewModel.game.delete());

        SudokuBoardView boardView = findViewById(R.id.sudokuBoard);
        boardView.registerListener(this);

        if(isContinuedGame) viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication(), 6, getApplicationContext())).get(SudokuViewModel.class);
        else viewModel = new ViewModelProvider(this, new MyViewModelFactory(getApplication(), difficulty, getApplicationContext())).get(SudokuViewModel.class);

        viewModel.game.activeNotesLiveData.observe(this, new Observer<HashSet<Integer>>() {
            @Override
            public void onChanged(HashSet<Integer> integers) {updateHighlightedKeysUI(integers);}
        });
        viewModel.game.isTakingNotesLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {updateNoteTakingUI(aBoolean);}
        });
        viewModel.game.selectedCellLiveData.observe(this, new Observer<Pair<Integer, Integer>>() {
            @Override
            public void onChanged(Pair<Integer, Integer> cell) {updateSelectedCellUI(cell);}
        });
        viewModel.game.cellsLiveData.observe(this, new Observer<Cell[][]>() {
            @Override
            public void onChanged(Cell[][] cells) {updateCells(cells);}
        });
        viewModel.game.numMistakesLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer numMistakes) {
                if(numMistakes >= 3) triggerGameOver();
                else updateNumMistakes(numMistakes);
            }
        });
        viewModel.game.correctNumsLiveData.observe(this, new Observer<Integer[]>() {
            @Override
            public void onChanged(Integer[] integers) {updateCompletedKeysUI(integers);}
        });

        int numCells = 81;
        viewModel.game.totalNumCorrectLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer numCorrect) {
                if(numCorrect == numCells)
                    triggerGameComplete();
            }
        });
        boardView.setStartingCells(viewModel.game.getSolvedGrid());
    }


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
        dialog.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getBaseContext(), GameActivity.class);
                intent.putExtra("Difficulty", difficulty);
                startActivity(intent);
                finish();
            }
        });
        dialog.setNegativeButton("Home", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }
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
                    if(timeString.charAt(4) < bestTime.charAt(4))
                        return true;
                }
            }
        }
        return false;
    }

    public void triggerGameOver() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
        isComplete = true;
        dialog.setTitle("Game Over!");
        dialog.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getBaseContext(), GameActivity.class);
                intent.putExtra("Difficulty", difficulty);
                startActivity(intent);
                finish();
            }
        });
        dialog.setNegativeButton("Home", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }
    private void updateCells(Cell[][] cells) {
        SudokuBoardView sudokuBoardView = findViewById(R.id.sudokuBoard);
        sudokuBoardView.updateCells(cells);
    }
    private void updateSelectedCellUI(Pair<Integer,Integer> cell) {
        if (cell == null) return;
        SudokuBoardView sudokuBoardView = findViewById(R.id.sudokuBoard);
        sudokuBoardView.updateSelectedCellUI(cell.first,cell.second);
    }

    private void updateNoteTakingUI(boolean isTakingNotes) {
        notesButton.setColorFilter(isTakingNotes ? Color.GREEN : Color.BLACK, PorterDuff.Mode.MULTIPLY);
    }

    private void updateHighlightedKeysUI(HashSet<Integer> notes) {
        Integer[] totalEachNum = viewModel.game.correctNumsLiveData.getValue();
        if(notes == null) return;
        for(int i = 0; i < 9; i++)
            if(notes.contains(i+1) || totalEachNum[i] >= 9)
                buttons[i].setBackgroundColor(Color.DKGRAY);
            else
                buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
    }

    private void updateCompletedKeysUI(Integer[] totals) {
        if(totals == null) return;
        for(int i = 0; i < 9; i++)
            if(totals[i] >= 9)
                buttons[i].setBackgroundColor(Color.DKGRAY);
            else
                buttons[i].setBackgroundColor(getButtonColor(colorPaletteSelection, i));
    }

    private void updateNumMistakes(int numMistakes) {
        TextView mistakesText = findViewById(R.id.mistakesText);
        mistakesText.setText("Mistakes: " + numMistakes + "/3");
    }


    public int getButtonColor(int selectedPalette, int i) {
        if(selectedPalette == 2) {
            switch (i) {
                case 0:
                    return getResources().getColor(R.color.sudokuAlt11);
                case 1:
                    return getResources().getColor(R.color.sudokuAlt12);
                case 2:
                    return getResources().getColor(R.color.sudokuAlt13);
                case 3:
                    return getResources().getColor(R.color.sudokuAlt14);
                case 4:
                    return getResources().getColor(R.color.sudokuAlt15);
                case 5:
                    return getResources().getColor(R.color.sudokuAlt16);
                case 6:
                    return getResources().getColor(R.color.sudokuAlt17);
                case 7:
                    return getResources().getColor(R.color.sudokuAlt18);
                case 8:
                    return getResources().getColor(R.color.sudokuAlt19);
                default:
                    return -1;
            }
        }
        else {
            switch (i) {
                case 0:
                    return getResources().getColor(R.color.sudokuDefault1);
                case 1:
                    return getResources().getColor(R.color.sudokuDefault2);
                case 2:
                    return getResources().getColor(R.color.sudokuDefault3);
                case 3:
                    return getResources().getColor(R.color.sudokuDefault4);
                case 4:
                    return getResources().getColor(R.color.sudokuDefault5);
                case 5:
                    return getResources().getColor(R.color.sudokuDefault6);
                case 6:
                    return getResources().getColor(R.color.sudokuDefault7);
                case 7:
                    return getResources().getColor(R.color.sudokuDefault8);
                case 8:
                    return getResources().getColor(R.color.sudokuDefault9);
                default:
                    return -1;
            }
        }

    }


    @Override
    public void onCellTouched(int row, int col) {
        viewModel.game.updateSelectedCell(row, col);
    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                    }
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
        System.out.println("Stopping: Complete? " + isComplete);
        if(isComplete) {
            prefs.edit().remove("inProgressBoard").apply();
            prefs.edit().remove("completeBoard").apply();
            prefs.edit().remove("Difficulty").apply();
            prefs.edit().remove("time").apply();
            prefs.edit().remove("Mistakes").apply();
        }
        else {
            String inProgressString = gridToString(viewModel.game.cellsLiveData.getValue());
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