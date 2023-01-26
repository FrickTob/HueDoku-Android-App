package com.app.hue_doku.game;

/**
 * Simulates a sudoku board of variable size
 * @author tobyf
 */
public class Board {
    int size;
    Cell[][] cells;
    public Board(int size, Cell[][] cells) {
        this.size = size;
        this.cells = cells;
    }

    /**
     * getCell is used to retrieve specific cells from the board.
     * @param row the row number of the cell you would like to retrieve (0-indexed)
     * @param col the column number of the cell you would like to retrieve (0-indexed)
     * @return a board cell specified by row and col
     */
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

}
