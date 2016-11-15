package com.example.simplecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Please replace the value of IP by the IP of your server
 */

public class MainActivityAuto extends Activity {
    private int NbrPoints;
    protected float fRmax ;
    protected ArrayList<LatLng> RoadWanted;
    protected boolean bAuto;
    protected double latitude = 0;
    protected double longitude = 0;
    protected CNetCom mNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Intent miIntent = getIntent();
        Bundle mbData = miIntent.getBundleExtra("DataBundle");
        String IP = mbData.getString("saddIP");
        String URL = "http://"+IP+":8080/video";
        mNetwork = new CNetCom(this);
        mNetwork.startConnection(IP);
        mNetwork.sendData("MODE","AUTO");
    }

    public void onClickDefineRoad(View view){// Open a google map where user can define a road or a destination
        final String[] Choice = new String[]{"Define a destination", "Define a road"};
        int iSelected =0;

        new AlertDialog.Builder(this).setTitle("Choose what you want to do")//Permit to ask if user want trace a road or just a destination
                .setItems(Choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0://Define a destination
                                bAuto = false;
                                break;
                            case 1://define a road
                                bAuto = true;
                                break;
                        }
                        dialog.dismiss();
                        /*mNetwork.sendData("GETLOC","GET");
                        mNetwork.bRobotLoc = true;
                        latitude = mNetwork.latitude;
                        longitude = mNetwork.longitude;*/
                        Intent myIntent = new Intent(MainActivityAuto.this,MapsActivity.class);
                        Bundle myBundle = new Bundle();//robots actual latitude and longitude are passed to the google map activity by a bundle
                        myBundle.putDouble("LATITUDE", latitude);
                        myBundle.putDouble("LONGITUDE", longitude);
                        myBundle.putBoolean("TYPE", bAuto);
                        myIntent.putExtras(myBundle);
                        MainActivityAuto.this.startActivityForResult(myIntent, 1);

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void OnDataReceive(){
        latitude = mNetwork.latitude;
        longitude = mNetwork.longitude;
        Intent myIntent = new Intent(MainActivityAuto.this,MapsActivity.class);
        Bundle myBundle = new Bundle();//robots actual latitude and longitude are passed to the google map activity by a bundle
        myBundle.putDouble("LATITUDE", latitude);
        myBundle.putDouble("LONGITUDE", longitude);
        myBundle.putBoolean("TYPE", bAuto);
        myIntent.putExtras(myBundle);
        MainActivityAuto.this.startActivityForResult(myIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){// User finished with the map
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle RBundle = data.getBundleExtra("bundle");
                    NbrPoints = RBundle.getInt("Size");//Size of our Array
                    fRmax = RBundle.getFloat("Radius");//Distance maximum between start and end
                    RoadWanted = RBundle.getParcelableArrayList("Road");//ArrayList with all the points defined by user
                    Toast.makeText(getApplicationContext(), "Road receive", Toast.LENGTH_LONG).show();
                    /**
                     * Network Communication
                     */
                    for(int iBcl = 0; iBcl < RoadWanted.size();iBcl++){
                        mNetwork.sendData("PLACE",Integer.toString(iBcl));
                        mNetwork.sendData("LAT",Double.toString(RoadWanted.get(iBcl).latitude));
                        mNetwork.sendData("LONG",Double.toString(RoadWanted.get(iBcl).longitude));

                    }
                    mNetwork.sendData("START","START");
                }
                break;
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        //mNetwork.close();
    }
}