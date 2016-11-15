package com.example.admin.receivebt;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
/*
 to use of BT needs to add :
 <uses-permission android:name="android.permission.BLUETOOTH"/>
 <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
 in AndroidManifest.xml
 The phone must :
 -> enable bluetooth
 -> pair the phone and the robot with the phone : code 1234
 */

public class BlueTooth {
    public static final int DEMANDE_AUTH_ACT_BT = 1;
    public static final int N_DEMANDE_AUTH_ACT_BT = 0;
    private static final String TAG = "BTT";
    
    BluetoothAdapter mbtAdapt; //BT adapter of the phone
    Activity mActivity; //main activity who instantiate blueT -> association
    boolean mbtActif = false;	//state of the association
    
    private Set<BluetoothDevice> mDevices; //liste of mDevices
    private BluetoothDevice[]mPairedDevices;// table of known devices
    
    int mDeviceSelected = -1; //the device choosen by the phone
    String[] mstrDeviceName;
    int miBlc = 0;				//used by connection
    boolean mbtConnected = false;
    
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // dummy UUID
    private BluetoothSocket mSocket;
    private OutputStream mOutStream;	//mSocket for communication
    private InputStream mInStream;		//mSocket for communication
    
    public Handler mHandler;
    private Thread mThreadReception =null;	//thread that receives data from device
    private String mstrData = "";
    private String mstrTampon = "";
    private String mstrStock = "";

    byte mbBuffer[] = new byte[200]; // large buffer !
    
    public BlueTooth(Activity Activity, Handler Handler)
    {
        this.mActivity = Activity;
        this.mHandler = Handler;

        this.Verif();
        mThreadReception = new Thread(new Runnable() { //create Thread for reception
            @Override
            public void run() {
                
                while(true)
                {
                    if(mbtAdapt != null)
                    {
                        if(mbtAdapt.isEnabled())
                        {
                            mbtActif = true;
                        }
                        else
                        {
                            mbtActif = false;
                        }
                    }
                    
                    if(mbtConnected == true) // reception of data when connected
                    {
                        reception();
                    }
                    try {
                        Thread.sleep(20, 0);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThreadReception.start(); //start thread
    }
    
    public void Verif() // Verification of BT adapter
    {
        mbtAdapt = BluetoothAdapter.getDefaultAdapter(); // recover BT informations on adapter
        if(mbtAdapt == null) {
            Log.i(TAG, "Not presentt");
        }
        else {
            Log.i(TAG, "Present");
        }
    }
    
    public void connexion()
    {
        this.Device_Connu();
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mActivity);
        adBuilder.setSingleChoiceItems(mstrDeviceName, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceSelected = which;
                dialog.dismiss();
                tryconnect();
            }
        });
        
        AlertDialog adb = adBuilder.create();
        adb.show();
    }
    
    public void Device_Connu() // recover all known devices
    {
        this.mDevices = mbtAdapt.getBondedDevices(); //recover the devices in a tab
        this.miBlc = mDevices.size(); // number of known devices
        this.mstrDeviceName = new String[this.miBlc]; //table will be given to pop up menu
        this.miBlc = 0;
        for(BluetoothDevice dev : this.mDevices) {
            this.mstrDeviceName[this.miBlc] = dev.getName();
            this.miBlc = this.miBlc + 1;
        }
        this.mPairedDevices = (BluetoothDevice[]) this.mDevices.toArray(new BluetoothDevice[this.mDevices.size()]); //cast of set in array.
    }
    
    public void tryconnect()
    {
        try {
            this.mSocket =this.mPairedDevices[this.mDeviceSelected].createRfcommSocketToServiceRecord(MY_UUID); //connection to vhchoosen device via Socket, mUUID: id of BT on device of the target
            this.mSocket.connect();
            Toast.makeText(this.mActivity, "Connected", Toast.LENGTH_SHORT).show();
            this.mbtConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.mActivity, "Try again", Toast.LENGTH_SHORT).show();
            try {
                mSocket.close();
            }
            catch(Exception e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public Boolean envoi(String strOrdre) // false -> error; true -> ok
    {
        try	{
            this.mOutStream = this.mSocket.getOutputStream(); //open output stream
            
            byte[] trame = strOrdre.getBytes();
            
            this.mOutStream.write(trame); //send frame via output stream
            this.mOutStream.flush();
            Log.i(TAG, "Send");
        }
        catch(Exception e2) {
            Log.i(TAG, "Error");
            tryconnect();
            try {
                this.mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return this.mbtConnected;
    }
    
    private String reception()
    {
        int iNbLu;
        int iIndex;
        try {
            this.mInStream = this.mSocket.getInputStream();// input stream
            
            if(this.mInStream.available() > 0 ) {
                iNbLu=this.mInStream.read(mbBuffer,0,20); //Caught received data
                mstrTampon = new String(mbBuffer,0,iNbLu); //Put into a temporary member
                mstrStock += mstrTampon;
                iIndex = mstrStock.indexOf('\0');
                while(iIndex != -1){
                    mstrData = mstrStock.substring(0,iIndex);
                    mstrStock = mstrStock.substring(iIndex+1);
                    Message msg = mHandler.obtainMessage();
                    msg.obj = mstrData;
                    mHandler.sendMessage(msg);
                    iIndex = mstrStock.indexOf('\0');
                }


            }
        }
        catch (Exception e) {
            Log.i(TAG, "Error");
            try {
                mSocket.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return mstrData;
    }
}








