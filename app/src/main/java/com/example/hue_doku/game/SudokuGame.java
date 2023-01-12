package com.example.hue_doku.game;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import com.example.hue_doku.generation.GeneratingAlgorithm;
import com.example.hue_doku.generation.TerminalPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SudokuGame {

    public MutableLiveData<Pair<Integer, Integer>> selectedCellLiveData = new MutableLiveData<>();
    public MutableLiveData<Cell[][]> cellsLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isTakingNotesLiveData = new MutableLiveData<>();
    public MutableLiveData<HashSet<Integer>> activeNotesLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> numCorrectLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer> numMistakesLiveData = new MutableLiveData<>();

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isTakingNotes = false;
    private int[][] solvedGrid;
    private Board board;
    private int numCorrect = 0;
    private int numMistakes = 0;



    public SudokuGame(int difficulty) {
        Cell[][] cells = new Cell[9][9];
        int[][] startingVals;
        do {
            solvedGrid = TerminalPattern.createPattern();
            startingVals = GeneratingAlgorithm.deepCopy(solvedGrid);
            startingVals = GeneratingAlgorithm.generatePuzzle(startingVals, difficulty);
        }
        while (startingVals[0][0] == -1);
        for(int row = 0; row < 9; row++)
            for (int col = 0; col < 9; col++){
                cells[row][col] = new Cell(row, col, startingVals[row][col]);
                if(cells[row][col].getValue() != 0) {
                    cells[row][col].toggleStartingCell();
                    cells[row][col].toggleCorrectCell();
                    numCorrect++;
                }
            }

        board = new Board(9, cells);
            numCorrectLiveData.postValue(numCorrect);
        selectedCellLiveData.postValue(new Pair(selectedRow,selectedCol));
        cellsLiveData.postValue(board.cells);
        isTakingNotesLiveData.postValue(isTakingNotes);
    }

    public void handleInput(int num) {
        if(selectedRow == -1 || selectedCol == -1) return;
        if(board.cells[selectedRow][selectedCol].isStartingCell()) return;
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
            if(num != solvedGrid[selectedRow][selectedCol]) {
                numMistakes++;
                numMistakesLiveData.postValue(numMistakes);
            }
            else if(selectedCell.getValue() == solvedGrid[selectedRow][selectedCol]) {
                numCorrect++;
                selectedCell.toggleCorrectCell();
            }
            else {
                numCorrect--;
                selectedCell.toggleCorrectCell();
            }
            numCorrectLiveData.postValue(numCorrect);

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

}
