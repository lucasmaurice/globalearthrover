/*----------------------------------------------*/
/* Cours d'Informatique Industrielle I2         */
/* (c) JN Martin 2013-2014                      */
/* Gestion de l'afficheur LCD Hitachi HD44780   */
/* Carte ARDUINO-IUT                            */
/* Fichier : AfficheurLCD.h                     */
/* Librairie Arduino <LiquidCrystal.h>          */
/*----------------------------------------------*/
 

#ifndef _AfficheurLCD_h
#define _AfficheurLCD_h

#include "LiquidCrystal.h"

/* connexion MCU-LCD
 * LCD RS pin to digital pin 24
 * LCD Enable pin to digital pin 25
 * LCD D4 pin to digital pin 26
 * LCD D5 pin to digital pin 27
 * LCD D6 pin to digital pin 28
 * LCD D7 pin to digital pin 29
 * LCD R/W pin to ground 
 * Lignes n°0 et n°1
 * Colonnes n°0 à n°15
 * Initialisation avec les n° des bornes du kit IUT
 * LiquidCrystal lcd(rs, enable, d4, d5, d6, d7) 
 */
extern LiquidCrystal lcd;
void InitLCD();

#endif
