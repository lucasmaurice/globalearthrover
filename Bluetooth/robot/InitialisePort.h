/*-------------------------------------------*/
/* Cours d'Informatique Embarquée Info2      */
/* (c) JN Martin 01-2014                     */
/* Initialisation des PORTS                  */
/* Carte ARDUINO-IUT                         */
/* Fichier : InitialisePort.h                */
/*-------------------------------------------*/

/*-----------------------------------------------
Initialisations conformes à la carte ARDUINO-IUT
Entrée = IN = 0, Sortie = OUT = 1, NU = 0 
-------------------------------------------------
PA7	29 Afficheur LCD 2x16 ligne D7	Sortie 1
PA6	28 Afficheur LCD 2x16 ligne D6	Sortie 1
PA5	27 Afficheur LCD 2x16 ligne D5	Sortie 1
PA4	26 Afficheur LCD 2x16 ligne D4	Sortie 1
PA3	25 Afficheur LCD 2x16 ligne EN	Sortie 1
PA2	24 Afficheur LCD 2x16 ligne RS	Sortie 1
PA1	23 NU 0
PA0	22 Afficheur uOLED Reset		Sortie 1
-------------------------------------------------
PB7	PWM13 NU 0	
PB6	PWM12 NU 0
PB5	PWM11 NU 0
PB4	PWM10 Ethernet SSTCP	Sortie 1
PB3	50 Ethernet MISO		Entree 0
PB2	51 Ethernet MOSI		Sortie 1
PB1	52 Ethernet SCK			Sortie 1
PB0	53 Ethernet SSHard		Sortie 1
-------------------------------------------------
PC7	30 Inter1	Entree 0	
PC6	31 Inter0	Entree 0	
PC5	32 BP1		Entree 0	
PC4	33 BP0		Entree 0
PC3	34 LED3		Sortie 1
PC2	35 LED2		Sortie 1		
PC1	36 LED1		Sortie 1
PC0	37 LED0		Sortie 1
-------------------------------------------------
PD7	38 Inter1	Entree 0	
PD6	NC 0	
PD5	NC 0	
PD4	NC 0
PD3	18 Tx1 BNC4	Sortie 1
PD2	19 Rx1 IRQ clavier I2C Entree 0		
PD1	20 SDA I2C	Sortie 1
PD0	21 SCL I2C	Sortie 1
-------------------------------------------------
PE7	NC 0	
PE6	NC 0	
PE5	PWM3 INT5 BNC1 Entree 0	
PE4	PWM2 INT4 BNC2 Entree 0
PE3	PWM5 BUZZER    Sortie 1
PE2	NC 0		
PE1	1 Tx0 USB PROG	Sortie 1
PE0	0 Rx0 USB PROG	Entree 0
-------------------------------------------------
PF7	A7 LED7B Sortie 1	
PF6	A6 LED6B Sortie 1	
PF5	A5 LED5B Sortie 1
PF4	A4 LED4B Sortie 1
PF3	A3 LED7 Sortie 1
PF2	A2 LED6 Sortie 1
PF1	A1 LED5 Sortie 1
PF0	A0 LED4 Sortie 1
-------------------------------------------------
PG7	-	
PG6	-	
PG5	PWM4 carte SD SSSD	Sortie 1
PG4	NC 0
PG3	NC 0
PG2	carte BusCAN SS		Sortie 1
PG1	40 Entree 0
PG0	41 Entree 0
-------------------------------------------------
PH7	NC 0	
PH6	PWM9 LED3B Sortie 1
PH5	PWM8 LED2B Sortie 1
PH4	PWM7 LED1B Sortie 1
PH3	PWM6 LED0B Sortie 1
PH2	NC 0
PH1	16 Tx2 Afficheur uOLED RX Sortie 1
PH0	17 Rx2 Afficheur uOLED TX Entree 0
-------------------------------------------------
PJ7 à PJ2 NC 0	
PJ1	14 Tx3 BNC3 PCINT10 Sortie 1
PJ0	15 Rx3 Entree 0
-------------------------------------------------
PK7	A15 Entree 0	
PK6	A14 Entree 0
PK5	A13 Entree 0
PK4	A12 Capteur de lumière LUX Entree 0
PK3	A11 Capteur de température Entree 0
PK2	A10 Potentiomètre2 Entree 0
PK1	A9  Potentiomètre1 Entree 0
PK0	A8 BNC5 Entree 0
-------------------------------------------------
PL7	42 Entree 0	
PL6	43 Entree 0
PL5	44 Entree 0
PL4	45 Entree 0
PL3	46 Entree 0
PL2	47 Entree 0
PL1	48 Entree 0
PL0	49 BNC6 IC4 Entree 0
-----------------------------------------------*/
#ifndef _InitialisePort_h
#define _InitialisePort_h

#include <Arduino.h>

#define INIT_A 0b11111101    // initialisation DDRA
#define INIT_B 0b00010111    // initialisation DDRB
#define INIT_C 0b00001111    // initialisation DDRC
#define INIT_D 0b00001011    // initialisation DDRD
#define INIT_E 0b00001010    // initialisation DDRE
#define INIT_F 0b11111111    // initialisation DDRF
#define INIT_G 0b11100100    // initialisation DDRG
#define INIT_H 0b01111010    // initialisation DDRH
#define INIT_J 0b00000010    // initialisation DDRJ
#define INIT_K 0b00000000    // initialisation DDRK
#define INIT_L 0b00000000    // initialisation DDRL

//proto
void InitPort(void);
bool TestAppuiBP1(void);
bool TestAppuiBP0(void);
bool TestInterSW1(void);
bool TestInterSW0(void);

#endif