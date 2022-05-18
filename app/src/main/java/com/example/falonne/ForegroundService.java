package com.example.falonne;



import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ForegroundService extends Service implements LocationListener, com.google.android.gms.location.LocationListener{
    public ForegroundService() {
    }



    double latitude;
    double longitude;
    private static final int PERMS_CALL_ID = 1234;
    FusedLocationProviderClient fusedLocationProviderClient = null;


    // Récupération d'une instance du LocationManager
    private LocationManager locationManager;

    EditText topic = null;

    private static final int  NOTIFICATION_ID = 1;
    String Sname, Shot,Spor, Spassw , deviseID, Stopic;

    public MqttAndroidClient client ;
    String IDClient = MqttClient.generateClientId();



    @Override
    public void onCreate() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String imeiNumber1 = telephonyManager.getDeviceId(0);
            String imeiNumber2 = telephonyManager.getDeviceId(1);
            deviseID = imeiNumber1 + "/" + imeiNumber2;
            SettingsFragment.getDevideID().setText(deviseID);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_app";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MyApp", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();
            //notification= permet de montrer à l'utilisateur q'une l'application tourne en arrière plan
            startForeground(NOTIFICATION_ID, notification);
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onResume();
        init();
        //If service is killed while starting, it restarts.
        SettingsFragment.txtSop.setText("The App is activated !! ");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return null;
    }

    @Override
    public void onDestroy() {
        SettingsFragment.txtSop.setText("The App is disabled !! ");
        onPause();
        super.onDestroy();

    }


    //@SuppressWarnings( "MissingPermission" )  //on cherche à supprimer tous les warnings de type permission
    protected void onResume() {
        //super.onResume();
        checkPermissions(); //on cherche à s'abonner


    }

    private void checkPermissions(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //on vérifie si les permissions sont bien activées et si non ,  on renvoie un popup demandant de l'activée
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getBaseContext(), new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMS_CALL_ID);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        // Récupération d'une instance du LocationManager

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //on demande à récupérer le LOCATION_SERVICE fournie par android
        //Définition d'un instance de LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))  { //si un fournisseur particulier est autorisé; si on a bien un capteur de type GPS qui est activé,

            //Vérification toutes les 10 secondes (10000 millisecondes ) si la position change
            //d'au moins 10 mètres. Si c'est le cas, l'écouteur (instance de MajListener)
            //va etre averti
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,  this);  //alors on s'abonne aux events

        }
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))  {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5,  this);  //alors on s'abonne aux events

        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))  {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5,  this);  //alors on s'abonne aux events

        }
    }

    //si les positions sont activées, on vient ici
    //cette méthode déclanchera à chaque fois q'une demande d'activation des permissions sera proposée
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMS_CALL_ID)//on récupère la position d'où on vient
            checkPermissions();
    }


    protected void onPause(){
        // super.onPause();
        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,  this);

//        googleApiClient.disconnect();
        SettingsFragment.txtSop.setText("The App is disabled !! ");
        SettingsFragment.textConnectOrFailed.setText("");
        SettingsFragment.textPublishing.setText("");

        if(locationManager !=null){
            locationManager.removeUpdates(this);

        }

    }

    public void onProviderEnabled(@NonNull String provider) {
        // LocationListener.super.onProviderEnabled(provider);
    }

    public void onProviderDisabled(@NonNull String provider) {
        // LocationListener.super.onProviderDisabled(provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //LocationListener.super.onStatusChanged(provider, status, extras);
    }

    public void onLocationChanged(@NonNull Location location) { // à chaque fois qu'on reçoit une info de localisation
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 40000, 2, this);  //alors on s'abonne aux events

        Toast.makeText(this, "Location: " + latitude + "/" + longitude, Toast.LENGTH_LONG).show();  //déclanche un toast à chaque fois qu'on a une localisation
        if(client !=null && client.isConnected()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                publish();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void publish(){
        JSONObject publishMessage = new JSONObject();
        try {

            //create a list of lat and long values
            JSONArray lonLatValue = new JSONArray(new double[]{latitude, longitude});
            //create a list of variable lat and long
            JSONArray value_units = new JSONArray(new String[]{"Lat", "Lag"});

            //use to read the deviceID of the phone

            publishMessage.put("unitID", deviseID);
            publishMessage.put("Value", lonLatValue);
            publishMessage.put("Value-Units", value_units);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String StringPublishMessage = publishMessage.toString();
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = StringPublishMessage.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);



            if (Stopic != null){
                Toast.makeText(this, "dans publish", Toast.LENGTH_LONG).show();
                client.publish(Stopic, message, null, null);
                SettingsFragment.textPublishing.setText("Publishing...");

            }


        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void init(){
        Sname = SettingsFragment.getName().getText().toString();

        // Sname = MainActivity.name.getText().toString();
        Spassw = SettingsFragment.getPassw().getText().toString();
        Shot = SettingsFragment.getHot().getText().toString();
        Spor = SettingsFragment.getPor().getText().toString();
        Stopic = SettingsFragment.getTopic().getText().toString();

        client = new MqttAndroidClient(getApplicationContext(), "tcp://" + Shot + ":" + Spor, IDClient);

        Toast.makeText(getBaseContext(), "name :" + Sname, Toast.LENGTH_LONG).show();
        Log.d("name : ", Sname);
        Log.d("hot : ", Shot);
        Log.d("port : ", Spor);
        Log.d("password : ", Spassw);


        MqttConnectOptions options = new MqttConnectOptions();

        options.setCleanSession(false);
        if (Sname == null) {
            Toast.makeText(getBaseContext(), "Please enter Username !", Toast.LENGTH_LONG).show();
        } else if (Spassw == null) {
            Toast.makeText(getBaseContext(), "Please enter Password !", Toast.LENGTH_LONG).show();

        } else {
            options.setUserName(Sname);
            options.setPassword(Spassw.toCharArray());
        }

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Toast.makeText(getBaseContext(), " URL:" + serverURI, Toast.LENGTH_LONG).show();

            }

            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), " connectionLost :" + cause.getMessage(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void messageArrived(String Stopic, MqttMessage message) throws Exception {
                Toast.makeText(getBaseContext(), " messageArrived:" + message.toString(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });

        try {

            client.connect(options, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    SettingsFragment.textConnectOrFailed.setText("You are connected to MQTT broker");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(), "Failed to connect to : " + Shot + ":" + Spor + " error " + exception.getMessage() + "Topic : " + topic, Toast.LENGTH_LONG).show();
                    SettingsFragment.textConnectOrFailed.setText("Failed to connect to MQTT broker");
                    SettingsFragment.textPublishing.setText("");


                }
            });
        }catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "exception " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


        Toast.makeText(getBaseContext(), "lolllll", Toast.LENGTH_LONG).show();

    }





}
