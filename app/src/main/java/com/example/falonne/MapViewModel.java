package com.example.falonne;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel  extends ViewModel {
    private final MutableLiveData<String> mText;

    public MapViewModel(){

        mText = new MutableLiveData<>();
        mText.setValue("Not yet implemented. Go to settings page !!!");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
