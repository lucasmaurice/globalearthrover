package com.example.alexandre.projetct_gps;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Alexandre on 13/01/2016.
 */

//La classe robot gère le contôle des moteur du robot ainsi que la lecture de ses capteurs

//L'appel d'une méthode de cette classe doit se faire par un objet mRobot de cette classe

public class Robot{

    //Déclarations des variables

    public BlueTooth mBluetooth;
    public String strTrameEnvoi = "";
    public static int iCaptD;
    public static int iCaptG;
    public static int iCaptA;
    public static int iDistance;

    //Récupération des données capteur reçu par le téléphone

    static public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String myString= (String) msg.obj;
            iCaptD = Integer.parseInt(myString.substring(0,1));
            iCaptG = Integer.parseInt(myString.substring(1,2));
            iCaptA = Integer.parseInt(myString.substring(2,3));
            iDistance = Integer.parseInt(myString.substring(3,6));
        }
    };



    public Robot(Activity myActivity) {             //Instanciation d'un objet mBluetooth
        this.mBluetooth= new  BlueTooth(myActivity,mHandler);
    }

    public int Capteur_IR_Droit(){              //Retourne l'état du capteur IR droit (1 ou 0)

        return iCaptD;
    }

    public int Capteur_IR_Gauche(){             //Retourne l'état du capteur IR gauche (1 ou 0)

        return iCaptG;
    }

    public int Capteur_IR_Arriere(){            //Retourne l'état du capteur IR arrière (1 ou 0)

        return iCaptA;
    }

    public int Capteur_Distance(){              //Retourne la valeur mesuré en cm du capteur ultrason

        return iDistance;                           //return 200 correspond à une mesure invalide
    }

    //Méthode qui envoie les commandes moteurs

    public void ControleMoteur(int iVMD, int iVMG,int iSensMD,int iSensMG) {
        strTrameEnvoi = String.format("%3d%3d%d%d", iVMD, iVMG, iSensMD, iSensMG) + "\0"; //Conversion en type string des commandes moteur
        mBluetooth.envoi(strTrameEnvoi); //Appel de la méthode envoi par l'objet de classe Bluetooth
    }
}

