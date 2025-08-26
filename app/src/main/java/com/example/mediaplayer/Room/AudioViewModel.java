package com.example.mediaplayer.Room;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AudioViewModel extends ViewModel {
    private final MutableLiveData<Pair<Integer, Boolean>> sortBy = new MutableLiveData<>(new Pair<>(0, true));

    public LiveData<Pair<Integer, Boolean>> getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortType, boolean isAsc) {
        sortBy.setValue(new Pair<>(sortType, isAsc));
    }
}

