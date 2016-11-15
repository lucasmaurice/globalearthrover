package com.example.mathieu.gps;

import android.support.v7.app.AppCompatActivity;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;





public class StateDiagram extends AppCompatActivity {


    //Etats
    public static final int ARRET = 0;
    public static final int AVANCE = 1;
    public static final int RECULE = 2;
    public static final int OBSTACLE = 3;
    public static final int OBSTACLE_GAUCHE = 4;
    public static final int OBSTACLE_DROIT = 5;
    public static final int OBSTACLE_AVANT = 6;
    public static final int GAUCHE = 7;
    public static final int DROITE = 8;

    //Ordres
    public static final int ARRETER = 0;
    public static final int AVANCER = 1;
    public static final int RECULER = 2;
    public static final int TOURNERD = 3;
    public static final int TOURNERG = 4;

    //Variable Ordre
    private int iOrdre;

    //Variable Etat
    private int iState = ARRET;
    private int iStateOld = ARRET;

    //Capteurs
    private double CapteurUltrason = DISTLIMITE*2;
    private boolean CapteurInfDroite = false;
    private boolean CapteurInfGauche = false;
    private boolean CapteurInfArr = false;

    //Distance Limite
    private static final int DISTLIMITE = 100;
    private static final int DISTLIMITEMIN = 20;

    public Robot mRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScheduledExecutorService scheduleTaskExecutor;
        super.onCreate(savedInstanceState);

        this.mRobot = new Robot(this);

        //mise en place de la tache periodique d'execution du graphe d'etats
        PeriodicTask1 MyTask1 = new PeriodicTask1();
        scheduleTaskExecutor= Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.scheduleAtFixedRate(MyTask1, 0, 100, TimeUnit.MILLISECONDS);

    }

    //acquisition des données du capteur
    public void AcquisitionCpt(){

        //récupération des capteurs réels
        CapteurInfDroite = mRobot.Capteur_IR_Droit();
        CapteurInfGauche =  mRobot.Capteur_IR_Gauche();
        CapteurInfArr =  mRobot.Capteur_IR_Arriere();

        //Simulation de la distance Limite donnée par le Capteur ultrason
        if(MainActivity.CheckBoxDistLim.isChecked()){
            CapteurUltrason = DISTLIMITE;
        }
        else{
            CapteurUltrason = DISTLIMITE*2;
        }


    }

    //Evolution du gaphe d'etats
    public void StateEvolution(){
        switch(iState){

            case ARRET:
                switch(iOrdre){
                    case AVANCER:
                        if((CapteurInfDroite != true) && (CapteurInfGauche != true)){
                            iState = AVANCE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
                    case TOURNERD:
                        if(CapteurInfDroite != true){
                            iState = DROITE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
                    case TOURNERG:
                        if(CapteurInfGauche != true){
                            iState = GAUCHE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
                    case RECULER:
                        if(CapteurInfArr != true){
                            iState = RECULE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
                }
                break;

            case AVANCE:
                if((CapteurInfDroite == true) || (CapteurInfGauche == true)){
                    iState = OBSTACLE;
                }
                else if(CapteurUltrason <= DISTLIMITE){
                    iState = OBSTACLE_AVANT;
                }
                else{
                    switch(iOrdre) {
                        case ARRETER:
                            iState = ARRET;
                            break;

                        case TOURNERD:
                            iState = DROITE;
                            break;

                        case TOURNERG:
                            iState = GAUCHE;
                            break;

                        case RECULER:
                            if(CapteurInfArr != true){
                                iState = RECULE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;
                    }

                }
                break;

            case RECULE:
                if(CapteurInfArr == true){
                    iState = OBSTACLE;
                }

                else{
                    switch(iOrdre) {
                        case ARRETER:
                            iState = ARRET;
                            break;

                        case TOURNERD:
                            if (CapteurInfDroite != true) {
                                iState = DROITE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case TOURNERG:
                            if (CapteurInfGauche != true) {
                                iState = GAUCHE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case AVANCER:
                            if((CapteurInfDroite != true) && (CapteurInfGauche != true)){
                                iState = AVANCE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;
                    }
                }
                break;

            case DROITE:

                if(CapteurInfDroite == true){
                    iState = OBSTACLE;
                }
                else{
                    switch(iOrdre) {
                        case ARRETER:
                            iState = ARRET;
                            break;

                        case RECULER:
                            if (CapteurInfArr != true) {
                                iState = RECULE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case TOURNERG:
                            if (CapteurInfGauche != true) {
                                iState = GAUCHE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case AVANCER:
                            if ((CapteurInfDroite != true) && (CapteurInfGauche != true)) {
                                iState = AVANCER;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;
                    }
                }
                break;
            case GAUCHE:
                if(CapteurInfGauche == true){
                    iState = OBSTACLE;
                }
                else{
                    switch(iOrdre) {
                        case ARRETER:
                            iState = ARRET;
                            break;

                        case RECULER:
                            if (CapteurInfArr != true) {
                                iState = RECULE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case TOURNERD:
                            if (CapteurInfDroite != true) {
                                iState = DROITE;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;

                        case AVANCER:
                            if ((CapteurInfDroite != true) && (CapteurInfGauche != true)) {
                                iState = AVANCER;
                            }
                            else{
                                iState = OBSTACLE;
                            }
                            break;
                    }
                }

                break;

            case OBSTACLE:
                if((CapteurInfDroite == true) && (CapteurInfGauche == true) && (CapteurUltrason <= DISTLIMITE)){
                    iState = OBSTACLE_AVANT;
                }
                else if ((CapteurInfDroite == true) && (CapteurUltrason <= DISTLIMITE)){
                    iState = OBSTACLE_DROIT;
                }
                else if ((CapteurInfGauche == true) && (CapteurUltrason <= DISTLIMITE)){
                    iState = OBSTACLE_GAUCHE;
                }
                else if (CapteurInfArr == true){
                    iState = ARRET;
                }
                else if((CapteurInfDroite != true) && (CapteurInfGauche != true) && (CapteurInfArr != true)){
                    iState = ARRET;
                }
                break;

            case OBSTACLE_AVANT:
                if(CapteurUltrason > DISTLIMITE){
                    iState = OBSTACLE;
                }
                break;

            case OBSTACLE_DROIT:
                if(CapteurUltrason > DISTLIMITE){
                    iState = OBSTACLE;
                }
                break;
            case OBSTACLE_GAUCHE:
                if(CapteurUltrason > DISTLIMITE){
                    iState = OBSTACLE;
                }
                break;

        }

    }

    //Gestion des actions associees au etats
    public void Gestion(){
        if(iStateOld != iState){
            iStateOld = iState;
            switch (iState){

                case ARRET:
                    Arreter();
                    break;

                case AVANCE:
                    Avancer();
                    break;

                case RECULE:
                    Reculer();
                    break;

                case DROITE:
                    Droite();
                    break;

                case GAUCHE:
                    Gauche();
                    break;

                case OBSTACLE:
                    Avancer();
                    break;

                case OBSTACLE_AVANT:
                    if(CapteurUltrason > DISTLIMITEMIN){
                        Droite();
                    }
                    else{
                        Reculer();
                    }
                    break;

                case OBSTACLE_DROIT:
                    Gauche();
                    break;

                case OBSTACLE_GAUCHE:
                    Droite();
                    break;
            }
        }
    }

    //Fonction pour controler le robot
    protected void Avancer(){
        mRobot.ControleMoteur(100,100,1,1);
    }
    protected void Reculer(){
        mRobot.ControleMoteur(100,100,0,0);
    }
    protected void Droite(){
        mRobot.ControleMoteur(50,50,1,0);
    }
    protected void Gauche(){
        mRobot.ControleMoteur(50,50,0,1);
    }
    protected void Arreter(){
        mRobot.ControleMoteur(0,0,1,1);
    }



    //taches d'execution du graphe d'etats
    class PeriodicTask1 implements Runnable {
        public void run() {
            AcquisitionCpt();
            StateEvolution();
            runOnUiThread(new TaskUI1());
        }
    }

    class TaskUI1 implements Runnable{
        public void run() {

            Gestion();
        }
    }
}

