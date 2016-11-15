package com.example.simplecamera;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class ManualActivity extends Activity {
    /*public MjpegView mVideo;
    public DoRead mReadStream;*/

    //pour les différents boutons
    Button mbuttonLeft = null;              //bouton pour tourner a gauche
    Button mbuttonRight = null;             //bouton pour tourner a droite
    Button mbuttonClose = null;             //bouton pour arreter l activite
    Button mbuttonARU = null;               //bouton d ARU

    //pour la gestion des moteurs
    public String cVitesseMD = "";       //reglage vitesse moteur droit
    public String cVitesseMG = "";       //reglage vitesse moteur gauche
    public String cSensMD = "";          //sens moteur droit
    public String cSensMG = "";          //sens moteur gauche
    public int iVitesseMoteur = 0;   //recuperation de la progression de la seekbar

    SeekBar seekBar;                        //seekbar pour acceleration du robot

    //pour la WiFi
    public CNetworkCommunication mNetwork;
    //pour savoir la donnée envoyée via le WiFi
    public String mDroit;
    public String mGauche;
    public String sDroit;
    public String sGauche;
    public boolean ARU = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        //mVideo = (MjpegView) findViewById(R.id.mVideo);
        //On affecte alors à une chaine de caractère l’URL d’accès au streaming pour l’envoi de la requête http
        //String URL = "http://192.168.0.101:8080/video";
        Intent miIntent = getIntent();
        Bundle mbData = miIntent.getBundleExtra("DataBundle");
        String IP = mbData.getString("saddIP");
       // String URL = "http://"+IP+":8080/video";


        //On associe notre Classe DoRead avec notre FirstActivity pour que celle-ci puisse acceder à notre objet mVideo (MjpegView) afin d'en définir sa source
        //La classe DoRead dérive d’AsyncTask, et va permettre d’initier la connection en créant et exécutant un client http
        /*mReadStream = new DoRead(this);
        Log.i("IPPPPPPPPPPPPPPPP", URL);
        mReadStream.execute(URL);*/
        //Initialisation des boutons
        this.mbuttonLeft = (Button) findViewById(R.id.Leftbutton);

        this.mbuttonRight = (Button) findViewById(R.id.Rightbutton);

        this.mbuttonClose = (Button) findViewById(R.id.Close);

        this.mbuttonARU = (Button) findViewById(R.id.buttonARU);

        //Initialisation de la seekbar
        seekBar = (SeekBar) findViewById(R.id.seekBarAcc);
        seekBar.setMax(255);
        seekBar.setProgress(0);     //pour que le robot soit a l arret au demarrage de l appli
        //Implémentation et ouverture de connexion (via la création d'un objet)
        mNetwork = new CNetworkCommunication();
        mNetwork.startConnection(IP);
        // /Attention, utiliser une seule des deux surcharge suivante
       //soit sur le serveur, soit sur le client        ​
       // Ouverture de la connexion à un server socket
        mNetwork.sendData("MODE", "MANU");

    }
    public void onClick(View v) {
        switch (v.getId()) { // who click ?
            case R.id.Topbutton:
                cSensMD = String.valueOf(1);
                cSensMG = String.valueOf(1);
                break;

            case R.id.Backbutton:
                cSensMD = String.valueOf(0);
                cSensMG = String.valueOf(0);
                break;
            case R.id.Leftbutton:
                cSensMD = String.valueOf(1);
                cSensMG = String.valueOf(0);
                break;

            case R.id.Rightbutton:
                cSensMD = String.valueOf(0);
                cSensMG = String.valueOf(1);
                break;

            case R.id.buttonARU:
                if(ARU==false){
                    ARU=true;
                }
                else if(ARU){
                    ARU = false;
                }
                break;

            case R.id.buttonClose:
                this.finish();
                break;
        }
        GestionRobot(); //méthode permettant de faire varier la vitesse des moteurs en fonction de la seekbar
    }
    public void GestionRobot(){
            iVitesseMoteur = seekBar.getProgress();
            if(ARU) {
                cVitesseMD = String.valueOf(iVitesseMoteur);
                cVitesseMG = String.valueOf(iVitesseMoteur);
            }
            else if(ARU==false){
                cVitesseMD = String.valueOf(0);
                cVitesseMG = String.valueOf(0);
            }
                //Envoie de data via la connexion
                //Attention, ne pas mettre de /0 ni dans l'index ni dans la valeur
                mNetwork.sendData("mDroit", cVitesseMD);
                mNetwork.sendData("sDroit", cSensMD);
                mNetwork.sendData("mGauche", cVitesseMG);
                mNetwork.sendData("sGauche", cSensMG);

    }
    protected void onStop(){
        super.onStop();
        this.mNetwork.close();
    }
}