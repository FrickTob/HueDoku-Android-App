package com.app.hue_doku.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import com.app.hue_doku.generation.GeneratingAlgorithm;
import com.app.hue_doku.generation.TerminalPattern;

import java.util.Arrays;
import java.util.HashSet;

public class SudokuGame {

    public MutableLiveData<Pair<Integer, Integer>> selectedCellLiveData = new MutableLiveData<>();
    public MutableLiveData<Cell[][]> cellsLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isTakingNotesLiveData = new MutableLiveData<>();
    public MutableLiveData<HashSet<Integer>> activeNotesLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> totalNumCorrectLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer[]> correctNumsLiveData= new MutableLiveData<>();
    public MutableLiveData<Integer> numMistakesLiveData = new MutableLiveData<>();

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isTakingNotes = false;
    private int[][] solvedGrid;
    private Board board;
    private int numCorrect = 0;
    private Integer[] correctNums;
    private int numMistakes = 0;

    String continueBoard;
    String completeBoard;

    public SudokuGame(int difficulty, Context appContext) {
        Cell[][] cells = new Cell[9][9];
        int[][] startingVals;
        correctNums = new Integer[9];
        for(int i = 0; i < 9; i++)
            correctNums[i] = 0;

        if(difficulty == 6) {
            SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(appContext);
            String inProgressString = prefs.getString("inProgressBoard", "");
            String completeString = prefs.getString("completeBoard", "");
            System.out.println("In Progress String: " + inProgressString);
            solvedGrid = stringToGrid(completeString);
            startingVals = stringToGrid(inProgressString);
            numMistakes = prefs.getInt("Mistakes", 0);
        }
        else {
            do {
                solvedGrid = TerminalPattern.createPattern();
                startingVals = GeneratingAlgorithm.deepCopy(solvedGrid);
                startingVals = GeneratingAlgorithm.generatePuzzle(startingVals, difficulty);
            }
            while (startingVals[0][0] == -1);
        }
            for (int row = 0; row < 9; row++)
                for (int col = 0; col < 9; col++) {
                    cells[row][col] = new Cell(row, col, startingVals[row][col]);
                    if (cells[row][col].getValue() != 0) {
                        cells[row][col].toggleStartingCell();
                        cells[row][col].toggleCorrectCell();
                        numCorrect++;
                        correctNums[cells[row][col].getValue() - 1]++;
                    }
                }
        board = new Board(9, cells);

        totalNumCorrectLiveData.postValue(numCorrect);
        correctNumsLiveData.postValue(correctNums);
        selectedCellLiveData.postValue(new Pair(selectedRow,selectedCol));
        cellsLiveData.postValue(board.cells);
        isTakingNotesLiveData.postValue(isTakingNotes);
        numMistakesLiveData.postValue(numMistakes);
    }

    public void handleInput(int num) {
        if(selectedRow == -1 || selectedCol == -1) return;
        if(board.cells[selectedRow][selectedCol].isStartingCell()) return;
        if(board.cells[selectedRow][selectedCol].isCorrectCell()) return;
        Cell selectedCell = board.getCell(selectedRow, selectedCol);

        if(isTakingNotes && selectedCell.getValue() == 0) {
            if(selectedCell.getNotes().contains(num)) {
                selectedCell.removeNote(num);
            }
            else {
                selectedCell.addNote(num);
            }
            activeNotesLiveData.postValue(selectedCell.getNotes());
        }
        else if(!isTakingNotes) {
            if(selectedCell.getValue() == num) return;
            selectedCell.setValue(num);
            selectedCell.setColor(num - 1);
            selectedCell.clearNotes();
            if(num != solvedGrid[selectedRow][selectedCol]) { // incorrect
                numMistakes++;
                numMistakesLiveData.postValue(numMistakes);
            }
            else { // correct
                numCorrect++;
                correctNums[selectedCell.getValue() - 1]++;
                selectedCell.toggleCorrectCell();
            }
            totalNumCorrectLiveData.postValue(numCorrect);
            correctNumsLiveData.postValue(correctNums);

        }
        cellsLiveData.postValue(board.cells);
    }
    public void updateSelectedCell(int row, int col) {
        Cell cell = board.cells[row][col];

        selectedRow = row;
        selectedCol = col;
        selectedCellLiveData.postValue(new Pair(selectedRow, selectedCol));

        if(isTakingNotes) {
            activeNotesLiveData.postValue(cell.getNotes());
        }

    }

    public void toggleTakingNotes() {
        this.isTakingNotes = !this.isTakingNotes;
        isTakingNotesLiveData.postValue(isTakingNotes);
        HashSet<Integer> currentNotes;
        if(selectedRow == -1) currentNotes = new HashSet<>();
        else
            currentNotes = isTakingNotes ?
                    board.getCell(selectedRow, selectedCol).getNotes() : new HashSet<Integer>();

        activeNotesLiveData.postValue(currentNotes);
    }

    public void delete() {
        if(selectedRow == -1) return;
        Cell currCell = board.getCell(selectedRow, selectedCol);
        if(isTakingNotes) {
            currCell.clearNotes();
            activeNotesLiveData.postValue(currCell.getNotes());
        }
        else
            currCell.setValue(0);
        currCell.setColor(-1);
        cellsLiveData.postValue(board.cells);
    }

    public int[][] getSolvedGrid() {return solvedGrid;}

    public int[][] stringToGrid(String string) {
        int charToIntOffset = 48;
        int[][] grid = new int[9][9];
        for(int i = 0; i < 81; i++) {
            int row = (i) / 9;
            int col = (i) % 9;
            grid[row][col] = string.charAt(i)  - charToIntOffset;
        }
        return grid;

    }
}
