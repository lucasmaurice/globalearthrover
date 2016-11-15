package com.example.simplecamera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import java.lang.Math;

public class CAccelerometer implements SensorEventListener {

    // Objet accelerometre declaré
    public SensorManager mSensorManager;
    public Sensor mAccelerometer;

    // Contexte passé
    public Context mContexte;
    public GyroActivity mActivity;

    //Implémentation de la connexion et ouverture via la création d'un objet
    CNetworkCommunication mNetwork;

    // Variable pour stocker la valeur de l'acceleration sur les differents axes
    public float[] mAccelerometerVector = new float[3]; // X, Y, Z
    public int miVitesse = 0;

    public CAccelerometer(Context mContexteRecu, GyroActivity activity, CNetworkCommunication network){
        this.mContexte = mContexteRecu;
        this.mActivity = activity;

        this.mNetwork = network;

        // object mSensorManager of class  SensorManager manage sensors
        mSensorManager = (SensorManager)mContexte.getSystemService(MainActivity.SENSOR_SERVICE);
        /*1st parameter : what object will receive informations given by the sensor
        * 2nd parameter : Sensor. TYPE_ACCELEROMETER kind of sensor
        * 3rd : delay between 2 refreshes SensorManager.SENSOR_DELAY_NORMAL
        */
        // We use accelerometer sensor
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ne rien faire
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // reading sensor data
            mAccelerometerVector= event.values;

            // processsing data to set the speed of the wheels
            if(mAccelerometerVector[2] > 0 || mAccelerometerVector[0] > 0){
                if(mAccelerometerVector[0] < 3.0f){
                    miVitesse = 255;
                }
                else if(mAccelerometerVector[0] > 3.0f && mAccelerometerVector[0] < 6.0f){
                    miVitesse = (int) (255.0f - ((mAccelerometerVector[0]-3) / 0.01171875f));
                }
                else if(mAccelerometerVector[0] > 6.0f && mAccelerometerVector[0] < 7.0f){
                    miVitesse = 0;
                }
                else if(mAccelerometerVector[0] > 7.0f && mAccelerometerVector[0] < 9.0f){
                    miVitesse = (int) - ((mAccelerometerVector[0]-7) / 0.0078125f) ;
                }
                else if(mAccelerometerVector[0] > 9.0f){
                    miVitesse = -255;
                }
            }
            else if(mAccelerometerVector[2] <= 0){
                miVitesse = -255;
            }
            else{
                miVitesse = 0;
            }

            // displaying
            //mActivity.Position(mAccelerometerVector[0], mAccelerometerVector[1], mAccelerometerVector[2], miVitesse);
            this.EnvoiDonneesVitesse();

        }
    }

    public void EnvoiDonneesVitesse(){
        // motors speed
        Integer miSpeedLeft = miVitesse;
        Integer miSpeedRight = miVitesse;

        // motors direction
        Integer miDirectionLeft = 0;
        Integer miDirectionRight = 0;

        if (miSpeedLeft >= 0){ // if the left speed is positive
            // 1 = forward ; 0 = backward
            miDirectionLeft = 1;
            // else it stays at 0
        }

        if (miSpeedRight >= 0){ // if the right speed is positive
            miDirectionRight = 1;
            // else it stays at 0
        }

        // taking unsigned numbers
        miSpeedLeft = Math.abs(miSpeedLeft);
        miSpeedRight = Math.abs(miSpeedRight);

        // sending data to the other phone with wi-fi
        //Log.i("execution socket", "un string quelconque");
        mNetwork.sendData("mGauche", miSpeedLeft.toString()); // left speed
        mNetwork.sendData("mDroit", miSpeedRight.toString()); // right speed
        mNetwork.sendData("sGauche", miDirectionLeft.toString()); // left direction
        mNetwork.sendData("sDroit", miDirectionRight.toString()); // right direction
        SystemClock.sleep(10);

    }
}
