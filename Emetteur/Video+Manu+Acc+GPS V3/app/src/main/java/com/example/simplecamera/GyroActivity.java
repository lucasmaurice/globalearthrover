package com.example.simplecamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.widget.TextView;

public class GyroActivity extends Activity{
    public MjpegView mVideo;
    public DoRead mReadStream;
    //accelerometer
    public CAccelerometer mAccelerometer;

    // textViews for displaying data from sensors
   /* public  static TextView mTextViewCaptD = null;
    public  static TextView mTextViewCaptG = null;
    public  static TextView mTextViewCaptA = null;
    public  static TextView mTextViewAxeX = null;
    public  static TextView mTextViewAxeY = null;
    public  static TextView mTextViewAxeZ = null;

    public  static TextView mTextViewWiFi1 = null;
    public  static TextView mTextViewWiFi2 = null;
    public  static TextView mTextViewWiFi3 = null;*/

    // Creation de la communication wi-fi
    CNetworkCommunication mNetwork;

    // storage for sensor data
    public  static int i;
    public  static int iCaptD;
    public  static int iCaptG;
    public  static int iCaptA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_gyro);
        mVideo = (MjpegView) findViewById(R.id.mVideo);
        //On affecte alors à une chaine de caractère l’URL d’accès au streaming pour l’envoi de la requête http
        //String URL = "http://192.168.0.101:8080/video";
        Intent miIntent = getIntent();
        Bundle mbData = miIntent.getBundleExtra("DataBundle");
        String IP = mbData.getString("saddIP");
        String URL = "http://"+IP+":8080/video";


        //On associe notre Classe DoRead avec notre FirstActivity pour que celle-ci puisse acceder à notre objet mVideo (MjpegView) afin d'en définir sa source
        //La classe DoRead dérive d’AsyncTask, et va permettre d’initier la connection en créant et exécutant un client http
        mReadStream = new DoRead(this);
        Log.i("IPPPPPPPPPPPPPPPP", URL);
        mReadStream.execute(URL);
        // ouverture de la connexion wia l'objet mNetwork
        this.mNetwork =  new CNetworkCommunication();

        //Ouverture de la connexion à un server socket
        mNetwork.startConnection(IP); // test server

        //Création de l'accelerometre
        this.mAccelerometer = new CAccelerometer(this, this, this.mNetwork);

        //Initialisation des Textview
        /*this.mTextViewCaptD = (TextView)findViewById(R.id.textView1);
        this.mTextViewCaptG = (TextView)findViewById(R.id.textView3);
        this.mTextViewCaptA = (TextView)findViewById(R.id.textView5);
        this.mTextViewAxeX = (TextView)findViewById(R.id.textView7);
        this.mTextViewAxeY = (TextView)findViewById(R.id.textView9);
        this.mTextViewAxeZ = (TextView)findViewById(R.id.textView11);

        this.mTextViewWiFi1 = (TextView)findViewById(R.id.textView12);
        this.mTextViewWiFi2 = (TextView)findViewById(R.id.textView13);
        this.mTextViewWiFi3 = (TextView)findViewById(R.id.textView14);*/

        mNetwork.sendData("MODE","MANU");
    }


    //Ajout de la fermeture des connexions Wifi
    @Override
    protected void onStop() {
        super.onStop();
        this.mNetwork.close();
    }


    // display data
    /*public void Position(float iAX, float iAY, float iAZ, Integer iVitesse)
    {
        mTextViewAxeX.setText(" "+String.format("% 2.1f", iAX));
        mTextViewAxeY.setText(" "+String.format("% 2.1f", iAY));
        mTextViewAxeZ.setText(" "+String.format("% 2.1f", iAZ));
        mTextViewWiFi3.setText(iVitesse.toString());
    }*/
}
