

int trigPin = 2;                 //Ultrasonik sensör Trig pini
int echoPin = 3;                 //Ultrasonik sensör Echo pini
//int led = 13;
boolean done = false;

const int buzzer = 8;           //BAZIR PİN ÇIKIŞI 
const int kirmiziLed = 12;      //( YEŞİL )KAPI AÇILDIĞI ZAMAN YANAN LEDİN ÇIKIŞ  PİNİ
const int yesilLed = 13;        //( KIRMIZI ) KAPI  KAPALIYKEN YANAN  LEDİN ÇIKIŞ  PİNİ
void setup() {
    
  pinMode (buzzer , OUTPUT);
  pinMode (kirmiziLed , OUTPUT);
  pinMode (yesilLed , OUTPUT);

 // initialize serial communication:
 Serial.begin(9600);
 
 pinMode(trigPin, OUTPUT);
 pinMode(echoPin, INPUT);
 
 digitalWrite(trigPin, LOW);
 
 //pinMode(led,OUTPUT);
}

void loop()
{
 
 long sure, santimetre;
 
 digitalWrite(trigPin, HIGH);
 delayMicroseconds(10);
 digitalWrite(trigPin, LOW);

 sure = pulseIn(echoPin, HIGH);


 santimetre = santimetrehesap(sure);
 
 if (santimetre > 15)
  {
     digitalWrite(buzzer , HIGH);
     delay(25);
     digitalWrite(buzzer , LOW);
     delay(15);
     digitalWrite(kirmiziLed , HIGH);
     digitalWrite(yesilLed , LOW);
     
    if (done == false)
    {
   //   digitalWrite(led,HIGH);
      Serial.println("DİKKAT!! KAPI AÇILDI");
      done = true;
    }
  }
  else
  {
     digitalWrite(buzzer , LOW);
      digitalWrite(kirmiziLed , LOW);
      digitalWrite(yesilLed, HIGH);
      
      
  //  digitalWrite(led,LOW);
    done = false;
  }
 delay(50);
}//loopun parantezi


long santimetrehesap(long mikrosaniye)
{
 // The speed of sound is 340 m/s or 29 microseconds per centimeter.
 // The ping travels out and back, so to find the distance of the
 // object we take half of the distance travelled.
 return mikrosaniye / 29 / 2;
}
