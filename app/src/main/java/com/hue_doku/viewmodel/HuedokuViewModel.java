package com.hue_doku.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.hue_doku.game.HuedokuGame;

import java.io.Serializable;

/**
 * ViewModel for the SudokuGame
 */
public class HuedokuViewModel extends ViewModel {
    public HuedokuGame game;
    public HuedokuViewModel(Application app, Integer difficulty, Context context) {
        game = new HuedokuGame(difficulty, context);
    }
}
