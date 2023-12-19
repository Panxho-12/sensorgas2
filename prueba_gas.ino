#include <ESP8266WiFi.h>
#include <ThingSpeak.h>

// Definir pines
const int pinSensorMQ5 = A0;  // Conectar el pin analógico del sensor MQ-5 a A0
const int pinMotorInput1 = D3;  // Conectar al pin Input 1 del puente H
const int pinMotorInput2 = D4;  // Conectar al pin Input 2 del puente H

// Credenciales de WiFi y ThingSpeak
const char *ssid = "iPhone de Francisco";
const char *password = "chutopancho";
const unsigned long channelID = 2372296;
const char *apiKey = "1JS2FJQ0VARR41LI";
const char *myCounterReadAPIKey = "U1DKK51LLT6RXTGV";

WiFiClient client;

int estado = 0; // Variable para almacenar el estado del ventilador

void setup() {
  Serial.begin(9600);
  pinMode(pinSensorMQ5, INPUT);
  pinMode(pinMotorInput1, OUTPUT);
  pinMode(pinMotorInput2, OUTPUT);

  // Inicializar conexión WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando a WiFi...");
  }
  Serial.println("Conectado a WiFi");
  ThingSpeak.begin(client);
}

void loop() {
  int statusCode = 0;

  // Leer valor del gas de ThingSpeak
  int valorgas = ThingSpeak.readIntField(channelID, 1, myCounterReadAPIKey);
  Serial.print("Valor gas obtenida:");
  Serial.println(valorgas);

  // Leer el valor del switch
  int valorswitch = ThingSpeak.readIntField(channelID, 2, myCounterReadAPIKey);
  statusCode = ThingSpeak.getLastReadStatus();
  Serial.print(statusCode);
  if (statusCode == 200) {
    Serial.print("Valor switch: ");
    Serial.println(valorswitch);
  } else {
    Serial.println("No se puede leer el canal");
  }

  // Leer el valor del sensor
  int valorSensor = analogRead(pinSensorMQ5);
  // Mostrar el valor en el monitor serial
  Serial.print("Valor del sensor: ");
  Serial.println(valorSensor);

  // Verificar si el nivel de gas es mayor a 30
  if (valorgas > 50) {
    // Encender el ventilador si no está activo
    if (estado == 0) {
      digitalWrite(pinMotorInput1, HIGH);
      digitalWrite(pinMotorInput2, LOW);
      estado = 1; // Cambiar estado a prendido
    }
  } else {
  
    if (valorgas < 50){
          // Apagar el ventilador si está activo
      if (estado == 1) {
        digitalWrite(pinMotorInput1, LOW);
        digitalWrite(pinMotorInput2, LOW);
        estado = 0; // Cambiar estado a apagado
    }

  }

  }

  // Enviar datos a ThingSpeak
  ThingSpeak.writeField(channelID, 1, valorSensor, apiKey);

  // Control manual del ventilador
  if (valorswitch == 1) {
    // Encender ventilador manual
    digitalWrite(pinMotorInput1, HIGH);
    digitalWrite(pinMotorInput2, LOW);
    estado = 1; // Cambiar estado a prendido
  } else {
    // Apagar ventilador manual
    digitalWrite(pinMotorInput1, LOW);
    digitalWrite(pinMotorInput2, LOW);
    estado = 0; // Cambiar estado a apagado
  }

  delay(5000);  // Enviar datos cada 5 segundos
}
