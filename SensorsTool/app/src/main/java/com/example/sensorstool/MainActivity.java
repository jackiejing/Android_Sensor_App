package com.example.sensorstool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //Accelerometer
    //Accelerometer Reference: https://www.youtube.com/watch?v=YrI2pCZC8cc
    private TextView xText, yText, zText;
    private Sensor accelerometerSensor;
    private SensorManager sensormanager;

    //Pressure
    //Pressure Reference: https://www.youtube.com/watch?v=reDLrzGyAfk
    private TextView pText;
    private Sensor pressureSensor;

    //Gravity
    //Gravity Reference: https://www.youtube.com/watch?v=OAevile2UrI
    private TextView xgText, ygText, zgText;
    private  Sensor gravitySensor;
    private boolean gravitySensorPresent;

    //Compass
    //Compass Reference: https://www.youtube.com/watch?v=IzzGVLnZBfQ
    private TextView aText;
    private ImageView compass;
    private Sensor magnetometerSensor;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotation = new float[9];
    private float[] orientation = new float[3];
    boolean isLastAccelerometerArray = false;
    boolean isLastMagnetometerArray = false;
    long lastUpdateTime = 0;
    float currentDegree = 0f;

    //Location
    //Location Reference: https://www.youtube.com/watch?v=FzQc9f_vVjs
    private TextView textView;
    private String stringLatitude = "0.0";
    private String stringLongitude = "0.0";
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Pressure
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float [] values = sensorEvent.values;
            pText.setText(String.format("%.4f hPa",values[0]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Accelerometer
        sensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensormanager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);

        //Pressure
        pText = findViewById(R.id.pText);
        pressureSensor = sensormanager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        //Gravity
        xgText = (TextView)findViewById(R.id.xgText);
        ygText = (TextView)findViewById(R.id.ygText);
        zgText = (TextView)findViewById(R.id.zgText);
        if (sensormanager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            gravitySensor = sensormanager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            gravitySensorPresent = true;
        }
        else{
            xgText.setText("Sensor has error.");
            gravitySensorPresent = false;
        }

        //Compass
        aText = (TextView)findViewById(R.id.aText);
        compass = (ImageView)findViewById(R.id.compass);
        magnetometerSensor = sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Pressure
        sensormanager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_UI);

        //Gravity
        if (sensormanager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            sensormanager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        //Compass
        sensormanager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensormanager.registerListener(this, magnetometerSensor,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Pressure
        sensormanager.unregisterListener(sensorEventListener);

        //Gravity
        if (sensormanager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            sensormanager.unregisterListener(this, gravitySensor);
        }

        //Compass
        sensormanager.unregisterListener(this, accelerometerSensor);
        sensormanager.unregisterListener(this,magnetometerSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float [] values = sensorEvent.values;
        //Accelerometer
        xText.setText(String.format("X: %.7f m/s^2",values[0]));
        yText.setText(String.format("Y: %.7f m/s^2",values[1]));
        zText.setText(String.format("Z: %.7f m/s^2",values[2]));

        //Gravity
        xgText.setText(String.format("X: %.7f m/s^2",values[0]));
        ygText.setText(String.format("Y: %.7f m/s^2",values[1]));
        zgText.setText(String.format("Z: %.7f m/s^2",values[2]));

        //Compass
        if(sensorEvent.sensor == accelerometerSensor){
            System.arraycopy(values, 0, lastAccelerometer, 0, values.length);
            isLastAccelerometerArray = true;
        }
        else if(sensorEvent.sensor == magnetometerSensor){
            System.arraycopy(values, 0, lastMagnetometer, 0, values.length);
            isLastMagnetometerArray = true;
        }
        if(isLastAccelerometerArray && isLastMagnetometerArray && System.currentTimeMillis() - lastUpdateTime > 250){
            SensorManager.getRotationMatrix(rotation, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotation, orientation);
            float azimuthRadian = orientation[0];
            float azimuthDegree = (float) Math.toDegrees(azimuthRadian);
            RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, -azimuthDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);
            compass.startAnimation(rotateAnimation);
            currentDegree = -azimuthDegree;
            lastUpdateTime = System.currentTimeMillis();
            int x = (int) azimuthDegree;
            aText.setText(x +"°");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //Location
    public void buttonGetLocation(View view) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null){
                    stringLatitude = Double.toString(location.getLatitude());
                    stringLongitude = Double.toString(location.getLongitude());
                }

            }
        });

        textView.setText("Latitude: "+stringLatitude+"\nLongitude: "+stringLongitude);
    }
}