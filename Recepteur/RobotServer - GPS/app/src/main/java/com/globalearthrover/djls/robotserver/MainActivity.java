package com.globalearthrover.djls.robotserver;

import java.util.ArrayList;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Bundle;
import java.lang.Thread;
import java.lang.Runnable;

import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOError;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements LocationListener {

    public enum T_ETAT{
        IDLE, MANU, AUTO
    }

    //--Wireless managing
    WifiManager mWifiManager;
    String strIpAdress;
    String strSSID;

    //--State Managing
    CGraphEtat mGraphEtat;

    //--Monitoring Interface managing
    Thread mInterfaceThread;
    Handler mInterfaceHandler;
    TextView mTextViewIpAdress;
    TextView mTextViewControlMode;
    Button bpStart;
    Button bpStop;
    ProgressBar progBarMotD;
    ProgressBar progBarMotG;

    //--Network
    CNetCom mNetwork;

    //--Robot
    Robot mRobot;
    Thread mBluetoothThread;
    int iVitesseMG;
    int iVitesseMD;
    int iSensMD;
    int iSensMG;

    //--Network
    boolean isRunning;


    private LocationManager lm;
    protected double latitude;
    protected double longitude;
    protected double altitude;
    protected float precision;
    private StateDiagram mStateDiagram;
    protected Compass mCompass; //Class which use compass(using Accelerometer and magneticfield sensor)
    protected ArrayList<LatLng> RoadWanted;//The Road which is defined by user on the GoogleMap
    protected CalculCap mCalculCap;//Class which explode GPS DATA and Compass DATA to define the direction where the robot will go
    ScheduledExecutorService scheduleTaskExecutor;
    CoordonneesDao BDD = null;//BDD manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //--Wireless managing
        this.mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        this.strSSID = "";
        this.strIpAdress = "";

        //--Network
        this.mNetwork = new CNetCom();
        this.isRunning = false;

        //--State Managing
        this.mGraphEtat = new CGraphEtat();

        //--Link the interface to the code
        setContentView(R.layout.activity_main);
        this.bpStart = (Button) findViewById(R.id.bpStartServer);
        this.bpStop = (Button) findViewById(R.id.bpStopServer);
        this.progBarMotD = (ProgressBar) findViewById(R.id.progMotD);
        this.progBarMotG = (ProgressBar) findViewById(R.id.progMotG);
        this.mTextViewIpAdress = (TextView) findViewById(R.id.textIpAdress);
        this.mTextViewControlMode = (TextView) findViewById(R.id.textMode);

        //--Robot / Bluetooth
        this.mRobot = new Robot(this);
        this.mBluetoothThread = new Thread(new CBluetoothThread());
        this.mRobot.mBluetooth.connexion();
        this.mBluetoothThread.start();

        //--Monitoring Interface managing
        this.mInterfaceThread = new Thread(new CInterfaceThread());
        this.mInterfaceHandler = new Handler();
        this.mInterfaceThread.start();
        this.progBarMotD.setProgress(255);
        this.progBarMotG.setProgress(255);

        //--Location and GPS Mode
        mStateDiagram = new StateDiagram(this);
        mCompass = new Compass(this);
        mCalculCap = new CalculCap(this);
        BDD = new CoordonneesDao(this);
    }

    public void onServerStartingBP(View view){
        mNetwork.startConnection();
    }

    public void onServerStoppingBP(View view){
        mNetwork.close();
        mNetwork = new CNetCom();
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
                try {
                    scheduleTaskExecutor.shutdown();
                    //wait
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
        mNetwork.sendData("LAT",Double.toString(this.latitude));
        mNetwork.sendData("LONG",Double.toString(this.longitude));
        mNetwork.sendData("LOCATION","OK");
    }

    private class CBluetoothThread implements Runnable{
        int iSpeedD=0;
        int iSpeedG=0;
        int iSensD=0;
        int iSensG=0;
        public void run(){
            while(!Thread.currentThread().isInterrupted()) {
                if (mRobot.mBluetooth.mbtConnected && ((iSpeedD != iVitesseMD)||(iSpeedG != iVitesseMG)||(iSensMG != iSensG)||(iSensMD != iSensD))){
                    iSpeedD = iVitesseMD;
                    iSpeedG = iVitesseMG;
                    iSensD = iSensMD;
                    iSensG = iSensMG;
                    mRobot.ControleMoteur(iVitesseMD,iVitesseMG,iSensMD,iSensMG);
                }

                try {
                    Thread.sleep(50,0);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CInterfaceHandler implements Runnable{
        int iSpeedD;
        int iSpeedG;
        public void run() {
            bpStop.setEnabled(isRunning);
            bpStart.setEnabled(!isRunning);

            mTextViewIpAdress.setText(strIpAdress);

            iSpeedD = iVitesseMD;
            iSpeedG = iVitesseMG;
            if (iSensMD == 0) {
                iSpeedD = -iSpeedD;
            }
            if (iSensMG == 0) {
                iSpeedG = -iSpeedG;
            }

            progBarMotG.setProgress(255 + iSpeedD);
            progBarMotD.setProgress(255 + iSpeedG);

            switch (mGraphEtat.getState()){
                case IDLE:
                    mTextViewControlMode.setText("IDLE");
                    break;
                case MANU:
                    mTextViewControlMode.setText("Manual mode");
                    break;
                case AUTO:
                    mTextViewControlMode.setText("Automatic mode");
                    break;
                default:
                    mTextViewControlMode.setText("Error");
            }
        }
    }

    public class CInterfaceThread implements Runnable{
        int iSpeedD;
        int iSpeedG;
        T_ETAT etatold;

        public void run(){
            boolean bRun;
            String ipAddress;
            etatold = T_ETAT.IDLE;
            while(!Thread.currentThread().isInterrupted()){
                bRun = mNetwork.isServerRunning();
                ipAddress = Formatter.formatIpAddress(mWifiManager.getConnectionInfo().getIpAddress());
                if((bRun != isRunning)||(iSpeedD != iVitesseMD)||(iSpeedG != iVitesseMG)||(strIpAdress != ipAddress)||(etatold != mGraphEtat.getState())){
                    isRunning = bRun;
                    iSpeedD = iVitesseMD;
                    iSpeedG = iVitesseMG;
                    strIpAdress = ipAddress;
                    etatold = mGraphEtat.getState();
                    mInterfaceHandler.post(new CInterfaceHandler());
                }
            }
        }
    }

    public class CGraphEtat{
        private T_ETAT current_state;

        public CGraphEtat(){
            current_state = T_ETAT.IDLE;
        }

        public void setState(String strData){
            switch(strData){
                case "AUTO":
                    current_state = T_ETAT.AUTO;
                    break;
                case "MANU":
                    current_state = T_ETAT.MANU;
                    break;
                default:
                    current_state = T_ETAT.IDLE;
            }
        }
        public T_ETAT getState(){
            T_ETAT state;
            state = current_state;
            return state;
        }
    }

    public class CNetCom extends CNetworkCommunication{
        ArrayList<LatLng> Road = new ArrayList<>();
        int DataPlace;
        double longitude;
        double latitude;
        String Memory;
        public void onReception(CDataReceived dataReceived){
            Log.i("Socket", "Reception of " + dataReceived.getDataName() + " with value " + dataReceived.getDataValue());
            if(dataReceived.getDataName().contains("MODE")){
                Log.i("Socket Reception","Mode Update");
                mGraphEtat.setState(dataReceived.getDataValue());
            }
            else {
                switch (mGraphEtat.getState()) {
                    case MANU:
                        switch (dataReceived.getDataName()) {
                            case "mGauche":
                                iVitesseMG = Integer.parseInt(dataReceived.getDataValue());
                                break;
                            case "mDroit":
                                iVitesseMD = Integer.parseInt(dataReceived.getDataValue());
                                break;
                            case "sGauche":
                                iSensMG = Integer.parseInt(dataReceived.getDataValue());
                                break;
                            case "sDroit":
                                iSensMD = Integer.parseInt(dataReceived.getDataValue());
                                break;
                        }
                        break;
                    case AUTO:
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
                                OnReceiveNetwork("START",Road);
                                break;
                            case "DEFAULT":
                                OnReceiveNetwork("DEFAULT",Road);
                                break;
                            case "GETLOC":
                                if(dataReceived.getDataValue().equals("GET")){
                                    Log.i("GETLOC","Reception");
                                    OnLocationAsk();
                                }
                                break;
                        }
                        break;
                }
            }
        }
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
    public void onStop(){
        super.onStop();
        mNetwork.close();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
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
