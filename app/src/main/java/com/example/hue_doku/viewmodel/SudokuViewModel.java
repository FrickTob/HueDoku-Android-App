package com.example.hue_doku.viewmodel;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.hue_doku.game.SudokuGame;

public class SudokuViewModel extends ViewModel {
    public SudokuGame game = new SudokuGame(1);
    public SudokuViewModel(Application app, Integer difficulty) {
        game = new SudokuGame(difficulty);
    }
}
