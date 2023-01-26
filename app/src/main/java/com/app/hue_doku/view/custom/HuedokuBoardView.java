package com.app.hue_doku.view.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.app.hue_doku.R;
import com.app.hue_doku.game.Cell;

import java.util.HashSet;

/**
 * Custom view to display sudoku board to users
 * @author tobyf
 */
public class HuedokuBoardView extends View {
    private Paint thickLinePaint;
    private Paint thinLinePaint;
    private Paint selectedCellPaint;
    private Paint conflictingCellPaint;
    private Paint textPaint;
    private Paint incorrectValTextPaint;
    private Paint noteTextPaint;
    private Paint startingCellFillPaint;
    private Paint[] colorPaints = new Paint[9];

    private int sqrtSize = 3;
    private int size = 9;

    // set in OnDraw method
    private float cellSizePixels = 0f;
    private float noteSizePixels = 0f;
    private float borderOffset = 0f;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private OnTouchListener listener = null;

    private Cell[][] cells = null;
    private int[][] correctValues = null;

    private int selectedColorPalette = 1;

    public HuedokuBoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        selectedColorPalette = prefs.getInt("ColorPalette", 1);
        // Set up paints
        thickLinePaint = new Paint();
        thickLinePaint.setColor(Color.BLACK);
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setStrokeWidth(10f);

        thinLinePaint = new Paint();
        thinLinePaint.setColor(Color.BLACK);
        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setStrokeWidth(4f);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.GRAY);
        selectedCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        conflictingCellPaint = new Paint();
        conflictingCellPaint.setColor(Color.LTGRAY);
        conflictingCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setFakeBoldText(true);

        incorrectValTextPaint = new Paint();
        incorrectValTextPaint.setColor(Color.RED);
        incorrectValTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        noteTextPaint = new Paint();
        noteTextPaint.setColor(Color.BLACK);
        noteTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        startingCellFillPaint = new Paint();
        startingCellFillPaint.setColor(Color.DKGRAY);
        startingCellFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        Resources res = getContext().getResources();
        colorPaints[0] = new Paint();
        colorPaints[0].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[0].setColor(getColor(selectedColorPalette, 0));
        colorPaints[1] = new Paint();
        colorPaints[1].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[1].setColor(getColor(selectedColorPalette, 1));
        colorPaints[2] = new Paint();
        colorPaints[2].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[2].setColor(getColor(selectedColorPalette, 2));
        colorPaints[3] = new Paint();
        colorPaints[3].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[3].setColor(getColor(selectedColorPalette, 3));
        colorPaints[4] = new Paint();
        colorPaints[4].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[4].setColor(getColor(selectedColorPalette, 4));
        colorPaints[5] = new Paint();
        colorPaints[5].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[5].setColor(getColor(selectedColorPalette, 5));
        colorPaints[6] = new Paint();
        colorPaints[6].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[6].setColor(getColor(selectedColorPalette, 6));
        colorPaints[7] = new Paint();
        colorPaints[7].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[7].setColor(getColor(selectedColorPalette, 7));
        colorPaints[8] = new Paint();
        colorPaints[8].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[8].setColor(getColor(selectedColorPalette, 8));
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizePixels = Math.min(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(sizePixels,sizePixels);
    }

    @Override
    protected void onDraw (Canvas canvas) {
        updateMeasurements(canvas.getWidth());
        // Draw all nums and lines
        cellSizePixels = canvas.getWidth() / size;
        fillCells(canvas);
        drawNotes(canvas);
        drawLines(canvas);
    }

    /**
     * Based on the width of the view (which is the same as the height as this view is always square,
     * set measurements so that drawing functions work effectively
     * @param width the current width of the view
     */
    private void updateMeasurements(float width) {
        cellSizePixels = (width / size);
        noteSizePixels = cellSizePixels / sqrtSize;
        borderOffset = 4f;
        noteTextPaint.setTextSize(cellSizePixels / sqrtSize);
        textPaint.setTextSize(cellSizePixels / 1.5F);
        incorrectValTextPaint.setTextSize(cellSizePixels / 1.5F);
    }

    /**
     * Draw the lines dividing the sudoku grid
     * @param canvas the canvas on which to draw the grid
     */
    private void drawLines(Canvas canvas) {
        canvas.drawRect(0f,0f, canvas.getWidth(), canvas.getHeight(),thickLinePaint);

         for(int i = 0; i < size; i++) {
            if(i % sqrtSize == 0) {
                // thick line
                canvas.drawLine(i*cellSizePixels, 0F, i*cellSizePixels, canvas.getHeight(), thickLinePaint);
                canvas.drawLine(0F, i*cellSizePixels, canvas.getWidth(), i*cellSizePixels, thickLinePaint);
            }
            else {
                // thin line
                canvas.drawLine(i*cellSizePixels, 0F, i*cellSizePixels, canvas.getHeight(), thinLinePaint);
                canvas.drawLine(0F, i*cellSizePixels, canvas.getWidth(), i*cellSizePixels, thinLinePaint);
            }
        }
    }

    /**
     * Fill the cells with correct colors
     * @param canvas the canvas on which to fill in the cells
     */
    private void fillCells(Canvas canvas) {
        if(cells == null) return;

        for(Cell[] cellRow : cells)
            for(Cell cell : cellRow) {
                int row = cell.getRow();
                int col = cell.getCol();
                if(cell.isStartingCell()) {
                    fillCell(canvas, row, col, colorPaints[cell.getValue() - 1]);
                }
                else if (cell.getColor() != -1) {
                    fillCell(canvas, row, col, colorPaints[cell.getColor()]);
                }
                else if(row == selectedRow && col == selectedCol)
                    fillCell(canvas, row, col, selectedCellPaint);
                else if (row== selectedRow || col == selectedCol)
                    fillCell(canvas, row, col, conflictingCellPaint);
                else if(row / sqrtSize == selectedRow / sqrtSize && col / sqrtSize == selectedCol / sqrtSize)
                    fillCell(canvas, row, col, conflictingCellPaint);
            }
    }

    private void fillCell(Canvas canvas, int row, int col, Paint paint) {
        canvas.drawRect( col*cellSizePixels, row*cellSizePixels,(col+1)*cellSizePixels, (row+1)*cellSizePixels, paint);
    }

    /**
     * Draw colored circles to represent current notes on each cell and draw X on cells that have
     * incorrect values
     * @param canvas the canvas on which to draw the notes
     */
    private void drawNotes(Canvas canvas) {
        if (cells == null) return;
        for(Cell[] cellRow: cells)
            for(Cell cell : cellRow) {
                int row = cell.getRow();
                int col = cell.getCol();
                int cellVal = cell.getValue();
                Rect textBounds = new Rect();
                if(cellVal == 0) { // draw notes
                    HashSet<Integer> notes = cell.getNotes();
                    if(notes == null) return;
                    for(Integer note : notes) {
                        String valueString = note + "";
                        int rowInCell = (note - 1) / sqrtSize;
                        int colInCell = (note - 1) % sqrtSize;
                        noteTextPaint.getTextBounds(valueString, 0, valueString.length(), textBounds);
                        float textWidth = noteTextPaint.measureText(valueString);
                        float textHeight = textBounds.height();
                        canvas.drawCircle((col* cellSizePixels) + (colInCell * noteSizePixels) + (noteSizePixels / 2), (row * cellSizePixels) + (rowInCell * noteSizePixels) + (noteSizePixels / 2), noteSizePixels / 2.5F, colorPaints[note - 1]);
                    }
                }
                else if(cell.getValue() != correctValues[row][col]) { // draw x for incorrect answer
                    String xString = "X";
                    textPaint.getTextBounds(xString, 0, xString.length(), textBounds);
                    float textWidth = textPaint.measureText(xString);
                    float textHeight = textBounds.height();
                    canvas.drawText(xString,
                            (col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                            (row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2, textPaint);
                }
            }

    }

    /**
     *
     * @param event user touch event passed when user touches sudoku board
     * @return whether or not the touch event was used. Currently the only touch events that will
     * return true are ACTION_DOWN events
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean used = false;
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            handleTouchEvent(event.getX(), event.getY());
            used = true;
        }
        return used;
    }

    /**
     * Calculates the row and column of the cell the user touched after a touch event.
     * @param x x coordinate of touch event
     * @param y y coordinate of touch event
     */
    private void handleTouchEvent(float x, float y) {
        int possibleSelectedRow = (int) (y / cellSizePixels);
        int possibleSelectedCol = (int) (x / cellSizePixels);
        listener.onCellTouched(possibleSelectedRow, possibleSelectedCol);
    }

    /**
     * Changes the selected row and selected column and redraws the view with this updated info
     * @param row the newly selected row
     * @param col the newly selected column
     */
    public void updateSelectedCellUI(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        invalidate();
    }

    /**
     * Replace the current cell array with cells
     * @param cells the new cell array to be drawn in the view
     */
    public void updateCells(Cell[][] cells) {
        this.cells = cells;
        invalidate();
    }

    /**
     * Sets the currect values of cells to correctValues
     * @param correctValues the new correct values array to be used to draw the view
     */
    public void setCorrectValues(int[][] correctValues) {
        this.correctValues = correctValues;
    }


    /**
     * Set the on touch listener for this view
     * @param listener the listener for the view
     */
    public void registerListener(OnTouchListener listener) {this.listener = listener;}
    public interface OnTouchListener {
        public void onCellTouched(int row, int col);
    }

    /**
     * Returns the correct color given the palette the user has selected and the index of the color
     * (0-8) as there are 9 distinct huedoku colors
     * @param paletteNum the number of the palette user has selected (currently only 2 palettes possible)
     * @param index the index of the color you want to get from the specified palette
     * @return the hexidecimal code corresponding to the desired color
     */
    public int getColor(int paletteNum, int index) {
        Resources res = getContext().getResources();
        if(paletteNum == 2) { // alt colors 1
            switch(index) {
                case 0: return res.getColor(R.color.sudokuAlt11);
                case 1: return res.getColor(R.color.sudokuAlt12);
                case 2: return res.getColor(R.color.sudokuAlt13);
                case 3: return res.getColor(R.color.sudokuAlt14);
                case 4: return res.getColor(R.color.sudokuAlt15);
                case 5: return res.getColor(R.color.sudokuAlt16);
                case 6: return res.getColor(R.color.sudokuAlt17);
                case 7: return res.getColor(R.color.sudokuAlt18);
                case 8: return res.getColor(R.color.sudokuAlt19);
            }
        }
        else { // default colors
            switch(index) {
                case 0: return res.getColor(R.color.sudokuDefault1);
                case 1: return res.getColor(R.color.sudokuDefault2);
                case 2: return res.getColor(R.color.sudokuDefault3);
                case 3: return res.getColor(R.color.sudokuDefault4);
                case 4: return res.getColor(R.color.sudokuDefault5);
                case 5: return res.getColor(R.color.sudokuDefault6);
                case 6: return res.getColor(R.color.sudokuDefault7);
                case 7: return res.getColor(R.color.sudokuDefault8);
                case 8: return res.getColor(R.color.sudokuDefault9);
            }
        }
        return -1;
    }
}
