package com.example.hue_doku.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

    public class MyViewModelFactory implements ViewModelProvider.Factory {
        private Application mApplication;
        private int mParam;
        private Context mParam2;


        public MyViewModelFactory(Application application, int param, Context context) {
            mApplication = application;
            mParam = param;
            mParam2 = context;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new SudokuViewModel(mApplication, mParam, mParam2);
        }
    }
