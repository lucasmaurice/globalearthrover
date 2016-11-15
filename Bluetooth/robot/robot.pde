#include <SoftwareSerial.h>
#include <HardwareSerial.h>

#define PORT_VITESSE_MD 5 
#define PORT_VITESSE_MG 6 
#define PORT_DIRECTION_MD 4 
#define PORT_DIRECTION_MG 7

#define CAPTEUR_DROIT 14
#define CAPTEUR_GAUCHE 15
#define CAPTEUR_ARRIERE 16
#define CAPTEUR_LIMITE 20


// define which serial is used by the bluetooth adapter
#define BLUETOOTH Serial1
// delay between two messages sent to the BT
#define DELAY_UPDATE_CAPTEUR 100
// size of the messages
#define TAILLE_BUF 11
// used by millis()
static unsigned long int iTempsEnvoieCapteur = 0;

//Variables globales
int URPWM = 3; // PWM Output 0?25000US?Every 50US represent 1cm
int URTRIG = 9; // PWM trigger pin
bool  bSensD = true;
bool bSensG = true;
bool bCapteurD = false;
bool bCapteurG = false;
bool bCapteurA = false;
int iVitesseMD = 0;
int iVitesseMG = 0;
int iDistance = 0;


void setup()
{
	//Initialisation of serial for debug
	Serial.begin(115200);
	//Initialisation of bluetooth adapter
	BLUETOOTH.begin(9600);
	iTempsEnvoieCapteur = millis();
	
	//Initialisation des moteurs

	pinMode(PORT_VITESSE_MD, OUTPUT);
	pinMode(PORT_VITESSE_MG, OUTPUT);
	pinMode(PORT_DIRECTION_MD, OUTPUT);
	pinMode(PORT_DIRECTION_MG, OUTPUT);

	//Initialisation des capteurs

	pinMode(14, INPUT);
	pinMode(15, INPUT);
	pinMode(16, INPUT);
	
	pinMode(URTRIG, OUTPUT);                     // A low pull on pin COMP/TRIG
	digitalWrite(URTRIG, HIGH);                  // Set to HIGH
	pinMode(URPWM, INPUT);
	
}

void loop()
{
	//buffer send to phone
	static char strEnvoiCapteurs[TAILLE_BUF] = "__________";
	// number of characters received in the frame
	static int iBclReception = 0;
	// counter sent to the phone
	static int iBcl;
	static char strTrame[TAILLE_BUF] = "__________";
	
	
	if (BLUETOOTH.available() > 0)
	{
		strTrame[iBclReception] = BLUETOOTH.read(); //the frame is read character per character (not the complete frame at the same time)
	
		if (strTrame[iBclReception] == ' '){
			strTrame[iBclReception] = '0';
		}
		
		if ((strTrame[iBclReception] == '\0')) {	 // search end of frame
			// display received message
			iBclReception = 0;
		}
		
		else if (iBclReception >= 8){
			iBclReception = 0;
		}
		
		else {
			iBclReception++;
		}
		//Décodage de la trame Bluetooth
		Serial.println(strTrame);
		iVitesseMD = (strTrame[0] - '0') * 100 + (strTrame[1] - '0') * 10 + (strTrame[2] - '0');
		iVitesseMG = (strTrame[3] - '0') * 100 + (strTrame[4] - '0') * 10 + (strTrame[5] - '0');

		bSensD = strTrame[6] - '0';
		bSensG = strTrame[7] - '0';

	}
	
	//Commande des moteurs
	
	analogWrite(PORT_VITESSE_MD, iVitesseMD);			//Envoi de la commande sur le moteur droit
	digitalWrite(PORT_DIRECTION_MD, bSensD);

	analogWrite(PORT_VITESSE_MG, iVitesseMG);			//Envoi de la commande sur le moteur gauche
	digitalWrite(PORT_DIRECTION_MG, bSensG);
	
	//Lectures des capteurs

	bCapteurD = digitalRead(CAPTEUR_DROIT);
	bCapteurG = digitalRead(CAPTEUR_GAUCHE);
	bCapteurA = digitalRead(CAPTEUR_ARRIERE);

	bCapteurD = !bCapteurD;
	bCapteurG = !bCapteurG;
	bCapteurA = !bCapteurA;

	
	digitalWrite(URTRIG, LOW);               // reading Pin PWM will output pulses
	digitalWrite(URTRIG, HIGH);
	
	unsigned long iDistanceMeasured;

	iDistanceMeasured = pulseIn(URPWM, LOW,10000);	//Lecture du capteur ultrason
	
	if (iDistanceMeasured == 0)
	{											// the reading is invalid.

		iDistance = 200;							//Erreur: 200 valeur eronné 
	}
	else
	{
		iDistance = iDistanceMeasured / 50;           // every 50us low level stands for 1cm
	}
	Serial.println(iDistance);
	//send icPt each ms
	
	if ((iTempsEnvoieCapteur + DELAY_UPDATE_CAPTEUR) < millis())
	{
		sprintf(strEnvoiCapteurs, "%d%d%d%3d1",bCapteurD,bCapteurG,bCapteurA,iDistance);	//Conversion des données capteur en type string
		iBcl = -1;
		do {
			iBcl++;
			if (strEnvoiCapteurs[iBcl] == ' '){
				strEnvoiCapteurs[iBcl] = '0';
			}
			BLUETOOTH.write(strEnvoiCapteurs[iBcl]);	//Envoi des données capteur

		} while (strEnvoiCapteurs[iBcl] != '\0');
		iTempsEnvoieCapteur = millis();
	}
	
}



