/*-------------------------------------------*/
/* Cours d'Informatique Industrielle I2      */
/* (c) JN Martin 2013-2014                   */
/* tempoNB basée sur la fonction millis()    */
/*-------------------------------------------*/

#include <Arduino.h>

#ifndef _TempoNonBloquante_h
#define _TempoNonBloquante_h
//
void Tempo_RW(bool bRead,unsigned long &Debut,unsigned long &Duree);
bool TestTempoNB(void);
void DemTempoNB(unsigned long Duree);

#endif
