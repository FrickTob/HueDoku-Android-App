package com.example.hue_doku.viewmodel;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

    public class MyViewModelFactory implements ViewModelProvider.Factory {
        private Application mApplication;
        private int mParam;


        public MyViewModelFactory(Application application, int param) {
            mApplication = application;
            mParam = param;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new SudokuViewModel(mApplication, mParam);
        }
    }
