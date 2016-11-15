package com.example.alexandre.projetct_gps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Alexandre on 18/01/2016.
 */
public class CNetCom extends CNetworkCommunication {
    ArrayList<LatLng> Road = new ArrayList<>();
    boolean Start = false;
    MainActivity mMainActivity;
    int DataPlace;
    double longitude;
    double latitude;
    String Memory = new String();

    /** CNetCom Constructor
     *
     * @param Activity for association
     */
    public CNetCom(MainActivity Activity){
        mMainActivity = Activity;
    }

    /**
     * Called when the phone receive something on network
     * @param dataReceived
     */
    public void onReception(CDataReceived dataReceived) {
        Log.i(dataReceived.getDataName(),dataReceived.getDataValue());
        Memory = Memory + dataReceived.getDataName() +" // "+ dataReceived.getDataValue()+" // ";
        switch (dataReceived.getDataName()){
            case "PLACE":
                DataPlace = Integer.valueOf(dataReceived.getDataValue());
                Road.add(new LatLng(0,0));
                break;
            case "LAT":
                latitude = Double.valueOf(dataReceived.getDataValue());
                break;
            case "LONG":
                longitude = Double.valueOf(dataReceived.getDataValue());
                Road.add(new LatLng(latitude,longitude));
                break;
            case "START":
                mMainActivity.OnReceiveNetwork("START",Road);
                break;
            case "DEFAULT":
                mMainActivity.OnReceiveNetwork("DEFAULT",Road);
                break;
            case "GETLOC":
                if(dataReceived.getDataValue() == "GET"){
                    Log.i("GETLOC","Reception");
                    mMainActivity.OnLocationAsk();
                }
                break;
        }

    }

}
