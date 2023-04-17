package com.hue_doku.game;

import java.util.HashSet;

/**
 * The cell class simulates a sudoku cell with a row and column number of the board it is a member of,
 * the underlying value 0-9 (0 if empty cell. Not displayed to user, but used for baord generation
 * and to determine correct value the current cell color (-1 if empty cell).
 * @author tobyf
 */
public class Cell {
    private int row;
    private int col;
    private int value;
    private int color;
    private boolean isStartingCell;
    private boolean isCorrectCell;
    private HashSet<Integer> notes;

    public Cell(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.color = -1;
        isStartingCell = false;
        notes = new HashSet<Integer>();
    }

    /**
     * @return the row number of this cell (0-indexed)
     */
    public int getRow() {return row;}

    /**
     *
     * @return the column number of this cell (0-indexed)
     */
    public int getCol() {return col;}

    /**
     * @return the color of the cell (-1 if empty)
     */
    public int getColor() {return color;}

    /**
     * @param color the integer value of the color of this cell
     */
    public void setColor(int color) {this.color = color;}

    /**
     * @param value the value you would like this cell to be (0-9) 0 if empty
     */
    public void setValue(int value) {this.value = value;}

    /**
     * @return the current value of this cell (0-9) 0 if empty
     */
    public int getValue() {return value;}

    /**
     * @return whether or not this cell is a starting cell set upon board generation
     */
    public boolean isStartingCell() {return isStartingCell;}

    /**
     * Used to set cells as starting cells upon board generation as cells by default are not starting cells
     */
    public void toggleStartingCell() {this.isStartingCell = !this.isStartingCell;}

    /**
     * @return a set of integers corresponding to the current list of notes attached to this cell
     */
    public HashSet<Integer> getNotes() {return notes;}

    /**
     * @param num the number that you would like to be added to the list of notes associated with
     *            this cell
     */
    public void addNote(int num) {notes.add(num);}

    /**
     * @param num the number that you would like to remove from the current list of notes associated
     *            with this cell
     */
    public void removeNote(int num) {notes.remove(num);}

    /**
     * Remove all notes associated with this cell. Used when a color is assigned to the cell.
     */
    public void clearNotes() {notes.clear();}

    /**
     * @return whether or not this cell is correct
     */
    public boolean isCorrectCell() {return isCorrectCell;}

    /**
     * If cell is currently incorrect, make it correct and visa versa
     */
    public void toggleCorrectCell() {this.isCorrectCell = !this.isCorrectCell;}

}
