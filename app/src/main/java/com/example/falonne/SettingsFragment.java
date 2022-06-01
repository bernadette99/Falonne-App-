package com.example.falonne;


import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.falonne.databinding.ActivityMainBinding;
import com.example.falonne.databinding.FragmentSettingsBinding;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;




import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.falonne.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;


    //Récupération du fournisseur de contenu GPS
    private String lp = LocationManager.GPS_PROVIDER;


    public static EditText getHot() {
        return hote;
    }

    public static EditText hote = null;

    public static EditText getName() {
        return name;
    }


    public static EditText name = null;

    public static EditText getPassw() {
        return passw;
    }

    public static EditText passw = null;

    public static EditText getPor() {
        return por;
    }

    public static EditText por = null;

    public static EditText getDevideID() {
        return devideID;
    }

    public static EditText devideID = null;

    public static EditText getTopic() {
        return topic;
    }

    public static EditText topic = null;

    public static TextView getTxtSop() {
        return txtSop;
    }

    public static TextView txtSop, textConnectOrFailed, textPublishing;


    public static MqttAndroidClient client;
    String IDClient = MqttClient.generateClientId();

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding bindinng;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hote = (EditText) view.findViewById(R.id.hote);
        name = (EditText) view.findViewById(R.id.NomUtilisateur);
        passw = (EditText) view.findViewById(R.id.MotDePasse);
        por = (EditText) view.findViewById(R.id.port);
        devideID = (EditText) view.findViewById(R.id.deviceID);
        topic = (EditText) view.findViewById(R.id.topic);

        txtSop = (TextView) view.findViewById(R.id.txtStop);
        textConnectOrFailed = (TextView) view.findViewById(R.id.connectedOrFailed);
        textPublishing = (TextView) view.findViewById(R.id.publishing);



        View.OnClickListener listener = new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ForegroundService.class);
                switch (view.getId()) {
                    case R.id.button_start:
                        //starts service for the given Intent

                        // startService(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(getActivity(), intent);
                        }
                        break;
                    case R.id.btn_stop_update:
                        //stops service for the given Intent
                        //ContextCompat.stopService(intent);


                        getActivity().stopService(intent);


                        break;
                }
            }
        };

        view.findViewById(R.id.button_start).setOnClickListener(listener);
        view.findViewById(R.id.btn_stop_update).setOnClickListener(listener);





    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}