package com.globalearthrover.djls.robotserver;


/**
 * Created by Alexandre on 23/12/2015.
 */
public class StateDiagram {
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
    protected int iOrdre;

    //Variable Etat
    private int iState;
    private int iStateOld;

    //Capteurs
    private double CapteurUltrason = DISTLIMITE*2;
    private static boolean CapteurInfDroite = false;
    private static boolean CapteurInfGauche = false;
    private static boolean CapteurInfArr = false;

    //Distance Limite
    private static final int DISTLIMITE = 100;
    private static final int DISTLIMITEMIN = 20;

    //Association
    private MainActivity mActivity;

    public StateDiagram(MainActivity mActivitys) {
        mActivity = mActivitys;
        iState = ARRET;
        iStateOld = ARRET;
    }

    public void  AcquisitionCpt(){
        if(mActivity.mRobot.Capteur_IR_Droit() == 1){
            CapteurInfDroite = true;
        }
        else{
            CapteurInfDroite = false;
        }
        if(mActivity.mRobot.Capteur_IR_Gauche() == 1){
            CapteurInfGauche = true;
        }
        else{
            CapteurInfGauche = false;
        }
        if(mActivity.mRobot.Capteur_IR_Arriere() == 1){
            CapteurInfArr = true;
        }
        else{
            CapteurInfArr = false;
        }
    }

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
                if((CapteurInfDroite == true) && (CapteurInfGauche == true)){
                    iState = OBSTACLE;                }
                else if(CapteurInfDroite == true){
                    iState = OBSTACLE;
                }
                else if(CapteurInfGauche == true){
                    iState = OBSTACLE;
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
                            if ((CapteurInfDroite != true) && (CapteurInfGauche != true)) {
                                iState = RECULE;
                            }
                            else{
                                iState = OBSTACLE;
                            }

                            break;
                    }
                }
                break;

            case DROITE:
                if((CapteurInfDroite == true) && (CapteurInfGauche == true)){
                    iState = OBSTACLE;                }
                else if(CapteurInfDroite == true){
                    iState = OBSTACLE;
                }
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
                            iState = RECULE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
                }
                break;
            case GAUCHE:
                if((CapteurInfDroite == true) && (CapteurInfGauche == true)){
                    iState = OBSTACLE;                }
                else if(CapteurInfGauche == true){
                    iState = OBSTACLE;
                }
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
                            iState = RECULE;
                        }
                        else{
                            iState = OBSTACLE;
                        }
                        break;
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
        Gestion();

    }
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
                    Droite();;
                    break;
            }
        }
    }

    //Fonction pour controler le robot
    protected void Avancer(){
        mActivity.iVitesseMD = 200;
        mActivity.iVitesseMG = 200;
        mActivity.iSensMD=1;
        mActivity.iSensMG=1;
    }
    protected void Reculer(){
        mActivity.iVitesseMD = 200;
        mActivity.iVitesseMG = 200;
        mActivity.iSensMD=0;
        mActivity.iSensMG=0;
    }
    protected void Droite(){
        mActivity.iVitesseMD = 200;
        mActivity.iVitesseMG = 200;
        mActivity.iSensMD=1;
        mActivity.iSensMG=0;
    }
    protected void Gauche(){
        mActivity.iVitesseMD = 200;
        mActivity.iVitesseMG = 200;
        mActivity.iSensMD=0;
        mActivity.iSensMG=1;
    }
    protected void Arreter() {
        mActivity.iVitesseMD = 0;
        mActivity.iVitesseMG = 0;
    }
}
