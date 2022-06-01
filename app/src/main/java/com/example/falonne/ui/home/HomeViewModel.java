package com.example.falonne.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Welcome to the  application named 'Falonne' of the Neocampus icu team. \n\n" +
                "Its purpose is to send the GPS coordinates of the phone in the MQTT protocol that you will specify in the Settings part.\n\n\n" +
                "Attention!!!! you must first activate the localization and the access to the phone of this application.\n\n\n" +
                "Please leave the device ID part empty!");

    }

    public LiveData<String> getText() {
        return mText;
    }
}