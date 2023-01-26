package com.app.hue_doku.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * View model factory that allows for more parameters to be passed to the SodukuViewModel which
 * stores game data
 */
    public class MyViewModelFactory implements ViewModelProvider.Factory {
        private Application mApplication;
        private int difficulty;
        private Context appContext;
        public MyViewModelFactory(Application application, int param, Context context) {
            mApplication = application;
            difficulty = param;
            appContext = context;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new HuedokuViewModel(mApplication, difficulty, appContext);
        }
    }
