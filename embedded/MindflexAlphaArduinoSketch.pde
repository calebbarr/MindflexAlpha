// Use Arduino Fio with Bluetooth Bee to broadcast
// brainwave data from Mindflex

#include <SoftwareSerial.h>
#include <Brain.h>

#define RxD 2
#define TxD 3

SoftwareSerial Eeg(RxD, TxD);
Brain brain(Eeg);

void setup() {
  Eeg.begin(9600);
  Serial.begin(38400); // default data rate for BT Bee
  // set name
  Serial.print("\r\n+STNA=mindflex\r\n");
  delay(1000);
  // initiate bluetooth bee connection
  Serial.print("\r\n+INQ=1\r\n");
  delay(2000);   // wait for pairing
}

void loop() {
   // Expect packets about once per second.
    if (brain.update()) {
        Serial.println(brain.readCSV());
        // signal strength, attention, meditation, 
        // delta, theta, low alpha, high alpha, low beta,
        // high beta, low gamma, high gamma
    }
}
