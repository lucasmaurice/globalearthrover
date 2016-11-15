package com.example.alexandre.projetct_gps;

/**
 * Created by Alexandre on 21/12/2015.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOError;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements LocationListener {
    private LocationManager lm;
    private TextView AfficheLong;
    private TextView AfficheLat;
    private TextView AfficheAlt;
    private TextView AffichePrec;
    private TextView AfficheStatu;
    protected TextView AfficheAzimut;
    protected double latitude;
    protected double longitude;
    protected double altitude;
    protected float precision;
    private StateDiagram mStateDiagram;
    private LatLng[] RoadWant;
    private int NbrPoints;
    private boolean bAuto;
    int buffKeyVal = 0;
    protected Compass mCompass; //Class which use compass(using Accelerometer and magneticfield sensor)
    protected ArrayList<LatLng> RoadWanted;//The Road which is defined by user on the GoogleMap
    protected CalculCap mCalculCap;//Class which explode GPS DATA and Compass DATA to define the direction where the robot will go
    ScheduledExecutorService scheduleTaskExecutor;
    CoordonneesDao BDD = null;//BDD manager
    private TextView AfficheLongBDD;
    private TextView AfficheLatBDD;
    private TextView AffichePassBDD;
    ListView mListView;
    CNetCom Network;
    protected String IP = "192.168.0.104";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_robot_auto);

        mStateDiagram = new StateDiagram(this);
        mCompass = new Compass(this);
        mCalculCap = new CalculCap(this);
        Network = new CNetCom(this);
        Network.startConnection();
        BDD = new CoordonneesDao(this);

        AfficheLong = (TextView) findViewById(R.id.TVlongitude);
        AfficheLat = (TextView) findViewById(R.id.TVlatitude);
        AfficheAlt = (TextView) findViewById(R.id.TValtitude);
        AffichePrec = (TextView) findViewById(R.id.TVprecision);
        AfficheStatu = (TextView) findViewById(R.id.TVstatuLocProv);
        AfficheAzimut = (TextView) findViewById(R.id.textView10);
        AfficheLatBDD = (TextView) findViewById(R.id.textView3);
        AfficheLongBDD = (TextView) findViewById(R.id.textView4);
        AffichePassBDD = (TextView) findViewById(R.id.textView5);
        mListView = (ListView) findViewById(R.id.listView);


    }

    /**
     * Called when transmission of the road is finished
     * @param Recu Contain all the transmission(use it for debug)
     * @param Road Contain the road
     */
    public void OnReceiveNetwork(String Recu,ArrayList<LatLng> Road) {
        Log.i("ReceiveNetwork","Call");
        PeriodicTask1 MyTask1 = new PeriodicTask1();
        switch (Recu) {
            case "START":
                try {
                    Log.i("ReceiveNetwork", "START");
                    RoadWanted = Road;
                    BDD.open();
                    for(int iBcl = 0;iBcl < RoadWanted.size();iBcl++){
                        BDD.ajouter(new Coordonnees(0,RoadWanted.get(iBcl).longitude,RoadWanted.get(iBcl).latitude,0));
                    }
                    BDD.close();
                    PeriodicTask2 MyTask2 = new PeriodicTask2();
                    scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

                    // Check sensors
                    scheduleTaskExecutor.scheduleAtFixedRate(MyTask1, 0, 100, TimeUnit.MILLISECONDS);
                    // Calculate the direction where robot should go
                    scheduleTaskExecutor.scheduleAtFixedRate(MyTask2, 0, 500, TimeUnit.MILLISECONDS);
                    Log.i("BDD","OK");
                }
                catch (IOError e){
                    e.printStackTrace();
                }
                break;
            case "STOP":
                try{
                   scheduleTaskExecutor.shutdown();
                    while (!scheduleTaskExecutor.isTerminated()){}
                }
                catch (IOError e){
                    e.printStackTrace();
                }
                break;
            case "DEFAULT":
                Log.i("ReceiveNetwork","DEFAULT");
                scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
                // Check sensors
                scheduleTaskExecutor.scheduleAtFixedRate(MyTask1, 0, 100, TimeUnit.MILLISECONDS);
                break;
        }

    }
    public void OnLocationAsk(){
        Log.i("GETLOC","Send");
        Network.sendData("LAT",Double.toString(this.latitude));
        Network.sendData("LONG",Double.toString(this.longitude));
        Network.sendData("LOCATION","OK");
    }

    @Override
    protected void onResume() {
        super.onResume();
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))//Check GPS enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//Test if GPS is enabled
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
        } else {
            new AlertDialog.Builder(this).setTitle("Your gps is disabled")
                    .setMessage("To work this app need gps, please activate it")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
        mCompass.Resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        lm.removeUpdates(this);
        mCompass.Pause();
    }


    @Override
    public void onLocationChanged(Location location) {//Called when  GPS position change
        Log.i("Location Change", "Ok");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        precision = location.getAccuracy();

        AfficheLong.setText(Double.toString(longitude));
        AfficheLat.setText(Double.toString(latitude));
        AfficheAlt.setText(Double.toString(altitude));
        AffichePrec.setText(Float.toString(precision));

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("Provider Enabled","Ok");
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

        switch (provider) {
            case LocationManager.GPS_PROVIDER://if GPS Enabled we activate GPS update and remove  Networks update if it is already activate
                Log.i("Provider Enabled","GPS");
                lm.removeUpdates(this);
                lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
                Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_LONG).show();
                break;
            case LocationManager.NETWORK_PROVIDER://if gps disabled we activate networks updates
                Log.i("Provider Enabled","Network");
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.i("Provider Enabled","Network But Gps");
                    lm.removeUpdates(this);
                    lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
                    Toast.makeText(getApplicationContext(), "Location switch on network mode", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("Provider Disabled","Ok");
        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                Log.i("Provider Disabled","Gps");
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
                lm.removeUpdates(this);
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//if network is enabled we activate location with it
                    Log.i("Provider Disabled","Gps,Network Ok");
                    lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
                    Toast.makeText(getApplicationContext(), "Location switch on network mode", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_LONG).show();
                break;
            case LocationManager.NETWORK_PROVIDER:
                Log.i("Provider Disabled","Network");
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.removeUpdates(this);
                    Toast.makeText(getApplicationContext(), "Network Disabled", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { //Called when when provider state change
        String statu = "";
        Log.i("StatusChanged","Ok");
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                statu = "HORS SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statu = "TEMPORAIREMENT INDISPONIBLE";
                break;
            case LocationProvider.AVAILABLE:
                statu = "ACTIVE";
                break;

        }
        AfficheStatu.setText(statu);
    }
    @Override
    public void onStop(){
        super.onStop();
        Network.close();
    }


    class PeriodicTask1 implements Runnable {
        public void run() {
            mStateDiagram.AcquisitionCpt();
            mStateDiagram.StateEvolution();
            mStateDiagram.Gestion();
        }
    }

    class PeriodicTask2 implements Runnable {
        public void run() {
            float fAngle = mCalculCap.getAngle(RoadWanted.get(0), RoadWanted.get(1));
            mStateDiagram.iOrdre = mCalculCap.CalCap(mCompass.azimut, fAngle);
        }
    }
}




