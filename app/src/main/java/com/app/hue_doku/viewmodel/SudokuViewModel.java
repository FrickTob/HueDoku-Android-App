package com.app.hue_doku.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.app.hue_doku.game.SudokuGame;

public class SudokuViewModel extends ViewModel {
    public SudokuGame game;
    public SudokuViewModel(Application app, Integer difficulty, Context context) {
        game = new SudokuGame(difficulty, context);
    }
}
