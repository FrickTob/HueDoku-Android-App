package com.example.hue_doku.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    private int row;
    private int col;
    private int value;
    private int color;
    private boolean isStartingCell;
    private boolean isCorrectCell;
    private HashSet<Integer> notes;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.value = 0;
        this.color = -1;
        isStartingCell = false;
        isCorrectCell = false;
        notes = new HashSet<Integer>();
    }

    public Cell(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.color = -1;
        isStartingCell = false;
        notes = new HashSet<Integer>();
    }


    public int getRow() {return row;}
    public void setRow(int row) {this.row = row;}
    public int getCol() {return col;}
    public void setCol(int col) {this.col = col;}
    public int getColor() {return color;}
    public void setColor(int color) {this.color = color;}
    public void setValue(int value) {this.value = value;}
    public int getValue() {return value;}
    public boolean isStartingCell() {return isStartingCell;}
    public void toggleStartingCell() {this.isStartingCell = !this.isStartingCell;}

    public HashSet<Integer> getNotes() {return notes;}
    public void addNote(int num) {notes.add(num);}
    public void removeNote(int num) {notes.remove(num);}
    public void clearNotes() {notes.clear();}
    
    public boolean isCorrectCell() {return isCorrectCell;}
    public void toggleCorrectCell() {this.isCorrectCell = !this.isCorrectCell;}

}
