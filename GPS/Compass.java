package com.example.alexandre.projetct_gps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOError;

/**
 * Created by Alexandre on 06/01/2016.
 * Requiring  accelerometer and magnetic field sensor to work
 */
public class Compass implements SensorEventListener,Parcelable{
    private SensorManager Sm;
    Sensor Accelerometer;
    Sensor MagneticField;
    MainActivity mMainActivity;
    float azimut = 0;
    float[] mGravity = null;
    float[] mGeomagnetic = null;
    public static final String RESTART_GOOD = "OK";

    /** Instantiate the captors to make a compass, if you don't have the sensor needed show an alert dialog.
     *
     * @param main Main activity context for association
     */
    public Compass(MainActivity main) {
        mMainActivity = main;
        Sm = (SensorManager) mMainActivity.getSystemService(mMainActivity.SENSOR_SERVICE);
        Accelerometer = Sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        MagneticField = Sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if((Accelerometer == null) || (MagneticField == null)){
            String stDial = new String();
            if((Accelerometer == null)){
                stDial=stDial.concat("Accelerometer sensor");
            }
            if((MagneticField == null)){
                stDial = stDial.concat("MagneticField sensor");
            }
            new AlertDialog.Builder(mMainActivity).setTitle("Your gps is disabled")
                    .setMessage("You don't have"+" "+stDial)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    /**
     *
     * @param mActivity
     * @return return RESTART_GOOD if restard is ok, el
     */
    public String Restart(MainActivity mActivity){
        if(mActivity != null) {
            mMainActivity = mActivity;
            Sm = (SensorManager) mActivity.getSystemService(mActivity.SENSOR_SERVICE);
            Accelerometer = Sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            MagneticField = Sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            return RESTART_GOOD;
        }
        else{
            return null;
        }

    }

    public void Resume(){
        if(Sm != null) {
            Sm.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Sm.registerListener(this, MagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    public void Pause(){
        if(Sm != null) {
            Sm.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    public void onSensorChanged (SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = (float) Math.toDegrees(orientation[0]);// orientation contains: azimut, pitch and roll
                if(azimut < 0.0f){// orientation[0] is between -180 and 180°
                    azimut += 360.0f;
                }
                mMainActivity.AfficheAzimut.setText(Double.toString(azimut));
            }
        }
    }
    protected void finish(){
        try {
            Sm.unregisterListener(this);
            Sm = null;
        }
        catch (IOError e){
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(azimut);
        if(mGravity != null) {
            dest.writeInt(1);
            dest.writeValue(mGravity);
        }
        else{
            dest.writeInt(0);
        }
        if(mGeomagnetic != null) {
            dest.writeInt(1);
            dest.writeFloatArray(mGeomagnetic);
        }
        else{
            dest.writeInt(0);
        }
    }
    public static final Parcelable.Creator<Compass> CREATOR = new Parcelable.Creator<Compass>(){
        public Compass createFromParcel(Parcel in){
            return new Compass(in);
        }

        @Override
        public Compass[] newArray(int size) {
            return new Compass[0];
        }
    };
    private Compass(Parcel in){
        int i;
        this.azimut = in.readFloat();
        i = in.readInt();
        if(i == 1) {
            mGravity = in.createFloatArray();
        }
        i = in.readInt();
        if(i == 1) {
            mGeomagnetic = in.createFloatArray();
        }
    }
}
