package com.example.hue_doku.view.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.hue_doku.R;
import com.example.hue_doku.game.Cell;

import java.util.HashSet;

public class SudokuBoardView extends View {
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

    private int selectedRow = -1;
    private int selectedCol = -1;

    private OnTouchListener listener = null;

    private Cell[][] cells = null;
    private int[][] correctValues = null;

    public SudokuBoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

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
        colorPaints[0].setColor(res.getColor(R.color.sudokuDefault1));
        colorPaints[1] = new Paint();
        colorPaints[1].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[1].setColor(res.getColor(R.color.sudokuDefault2));
        colorPaints[2] = new Paint();
        colorPaints[2].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[2].setColor(res.getColor(R.color.sudokuDefault3));
        colorPaints[3] = new Paint();
        colorPaints[3].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[3].setColor(res.getColor(R.color.sudokuDefault4));
        colorPaints[4] = new Paint();
        colorPaints[4].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[4].setColor(res.getColor(R.color.sudokuDefault5));
        colorPaints[5] = new Paint();
        colorPaints[5].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[5].setColor(res.getColor(R.color.sudokuDefault6));
        colorPaints[6] = new Paint();
        colorPaints[6].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[6].setColor(res.getColor(R.color.sudokuDefault7));
        colorPaints[7] = new Paint();
        colorPaints[7].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[7].setColor(res.getColor(R.color.sudokuDefault8));
        colorPaints[8] = new Paint();
        colorPaints[8].setStyle(Paint.Style.FILL_AND_STROKE);
        colorPaints[8].setColor(res.getColor(R.color.sudokuDefault9));
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
        drawLines(canvas);
        drawText(canvas);
    }
    private void updateMeasurements(float width) {
        cellSizePixels = (width / size);
        noteSizePixels = cellSizePixels / sqrtSize;
        noteTextPaint.setTextSize(cellSizePixels / sqrtSize);
        textPaint.setTextSize(cellSizePixels / 1.5F);
        incorrectValTextPaint.setTextSize(cellSizePixels / 1.5F);
    }

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

    private void drawText(Canvas canvas) {
        if (cells == null) return;
        for(Cell[] cellRow: cells)
            for(Cell cell : cellRow) {
                int row = cell.getRow();
                int col = cell.getCol();
                int cellVal = cell.getValue();
                Rect textBounds = new Rect();
                if(cellVal == 0) {
                    // draw notes
                    HashSet<Integer> notes = cell.getNotes();
                    if(notes == null) return;
                    for(Integer note : notes) {
                        String valueString = note + "";
                        int rowInCell = (note - 1) / sqrtSize;
                        int colInCell = (note - 1) % sqrtSize;
                        noteTextPaint.getTextBounds(valueString, 0, valueString.length(), textBounds);
                        float textWidth = noteTextPaint.measureText(valueString);
                        float textHeight = textBounds.height();

                        canvas.drawText(valueString,
                                (col * cellSizePixels) + (colInCell * noteSizePixels) + (noteSizePixels / 2F) - (textWidth / 2),
                                (row * cellSizePixels) + (rowInCell * noteSizePixels) + (noteSizePixels / 2F) + (textHeight / 2), noteTextPaint);
                    }
                }
                else {
                    // draw number
                        String valueString = cell.getValue() + "";
                        Paint paintToUse = textPaint;
                        if(correctValues != null) {
                            paintToUse = cell.getValue() == correctValues[row][col] ? textPaint : incorrectValTextPaint;
                        }
                        paintToUse.getTextBounds(valueString, 0, valueString.length(), textBounds);
                        float textWidth = paintToUse.measureText(valueString);
                        float textHeight = textBounds.height();
                        canvas.drawText(valueString,
                                (col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                                (row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2, paintToUse);
                    }
                }

    }

    public void highlightStartingCells(Canvas canvas) {
        if(cells == null) return;
        for (Cell[] cellRow : cells)
            for(Cell cell : cellRow)
                if(cell.isStartingCell()) {
                    canvas.drawRect(cell.getCol() * cellSizePixels,
                            cell.getRow() * cellSizePixels,
                            (cell.getCol() + 1) * cellSizePixels,
                            (cell.getRow() + 1) * cellSizePixels,
                            thickLinePaint);
                }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean used = false;
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            handleTouchEvent(event.getX(), event.getY());
            used = true;
        }
        return used;
    }
    private void handleTouchEvent(float x, float y) {
        int possibleSelectedRow = (int) (y / cellSizePixels);
        int possibleSelectedCol = (int) (x / cellSizePixels);
        listener.onCellTouched(possibleSelectedRow, possibleSelectedCol);
    }

    public void updateSelectedCellUI(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        invalidate();
    }

    public void updateCells(Cell[][] cells) {
        this.cells = cells;
        invalidate();
    }

    public void setStartingCells(int[][] correctValues) {
        this.correctValues = correctValues;
    }


    public void registerListener(OnTouchListener listener) {this.listener = listener;}
    public interface OnTouchListener {
        public void onCellTouched(int row, int col);
    }
}
