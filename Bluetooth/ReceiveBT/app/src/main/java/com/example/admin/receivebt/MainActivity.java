package com.example.admin.receivebt;



import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Button mButConnect =null;
    public Button mButDroite =null;
    public Button mButGauche =null;
    public Button mButAvancer=null;
    public Button mButReculer=null;
    public Button mButStop =null;
    private SeekBar barProgress;
    public Robot mRobot;
    public static TextView mTextView1 = null;
    public static TextView mTextView3 = null;
    public static TextView mTextView5 = null;
    public static  TextView mTextView7= null;
    private Thread mThreadEnvoi = null;
    public static  int iVitesseMD;
    public static  int iVitesseMG;
    public static  int iSensMD;
    public static  int iSensMG;
    public static  int iCapteurD;
    public static  int iCapteurG;
    public static  int iCapteurA;
    public static  int iDist;
    public boolean ARU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        //Initialisation des boutons de tests
        this.mButConnect = (Button) findViewById(R.id.button1);
        this.mButConnect.setOnClickListener(this);

        this.mButDroite = (Button) findViewById(R.id.droite);
        this.mButDroite.setOnClickListener(this);

        this.mButGauche = (Button) findViewById(R.id.gauche);
        this.mButGauche.setOnClickListener(this);

        this.mButAvancer = (Button) findViewById(R.id.avancer);
        this.mButAvancer.setOnClickListener(this);

        this.mButReculer = (Button) findViewById(R.id.reculer);
        this.mButReculer.setOnClickListener(this);

        this.mButStop = (Button) findViewById(R.id.stop);
        this.mButStop.setOnClickListener(this);

        barProgress=(SeekBar) findViewById(R.id.seekBar);
        barProgress.setMax(255);

        //Initialisation des Textview
        this.mTextView1 = (TextView)findViewById(R.id.textView1);
        this.mTextView3 = (TextView)findViewById(R.id.textView3);
        this.mTextView5 = (TextView)findViewById(R.id.textView5);
        this.mTextView7 = (TextView)findViewById(R.id.textView7);

        this.mRobot = new Robot(this);
        this.mThreadEnvoi = new Thread(new Runnable() {
            @Override

            public void run() {                                 //Thread qui commande les moteurs par envoi Bluetooth
                while(true) {

                    if (mRobot.mBluetooth.mbtConnected==true){
                        if(ARU == false) {
                            iVitesseMD = barProgress.getProgress();
                            iVitesseMG = iVitesseMD;
                        }
                        iCapteurD = mRobot.Capteur_IR_Droit();
                        iCapteurG = mRobot.Capteur_IR_Gauche();
                        iCapteurA = mRobot.Capteur_IR_Arriere();
                        iDist = mRobot.Capteur_Distance();
                        mRobot.ControleMoteur(iVitesseMD, iVitesseMG, iSensMD, iSensMG);
                    }
                    try {
                        Thread.sleep(50, 0);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() { // This sends a message to UI, it contains a "runnable" executed by UI thread
                        @Override
                        public void run() {
                            mTextView1.setText(Integer.toString(iCapteurD));
                            mTextView3.setText(Integer.toString(iCapteurG));
                            mTextView5.setText(Integer.toString(iCapteurA));
                            mTextView7.setText(Integer.toString(iDist));
                        }
                    });
                }
            }
        }
        );
        mThreadEnvoi.start();
    }

    public void onClick(View v) {
        switch(v.getId()) { // who click ?

            //Bluetooth connexion Button
            case R.id.button1:
                this.mRobot.mBluetooth.connexion();
                break;
            case R.id.droite:
                iSensMD=0;
                iSensMG=1;
                break;
            case R.id.gauche:
                iSensMD=1;
                iSensMG=0;
                break;
            case R.id.avancer:
                ARU = false;
                iSensMD=1;
                iSensMG=1;
                break;
            case R.id.reculer:
                ARU = false;
                iSensMD=0;
                iSensMG=0;
                break;
            case R.id.stop:
                ARU = true;
                iVitesseMD=0;
                iVitesseMG=0;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // nothing special

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
