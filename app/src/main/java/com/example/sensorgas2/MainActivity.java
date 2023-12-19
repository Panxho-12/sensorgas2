package com.example.sensorgas2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    private TextView textView;
    private static final String TAG = "MainActivity";
    private static final String URL_READ = "https://api.thingspeak.com/channels/2372296/fields/1.json?api_key=1IUPJ7UGXQQ2P5JL&results=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.valorgas);

        // Iniciar la actualización periódica
        startPeriodicUpdate();
    }

    public void ventiladorOn(View view) {
        final String url = "https://api.thingspeak.com/update?api_key=1JS2FJQ0VARR41LI&field2=1";
        final AsyncHttpClient client = new AsyncHttpClient();

        // Llamada inicial
        sendRequest(client, url);
    }





    public void ventiladorOff(View view) {
        final String url = "https://api.thingspeak.com/update?api_key=1JS2FJQ0VARR41LI&field2=0";
        final AsyncHttpClient client = new AsyncHttpClient();

        // Llamada inicial
        sendRequest(client, url);
    }

    private void sendRequest(final AsyncHttpClient client, final String url) {
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    String response = new String(responseBody);
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                    // Si la respuesta es 0, realizar una llamada recursiva
                    if ("0".equals(response.trim())) {
                        sendRequest(client, url);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Manejar la lógica de falla según sea necesario
            }
        });
    }

    private void startPeriodicUpdate() {
        // Crear un temporizador que ejecuta la tarea cada cierto tiempo
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Actualizar la interfaz de usuario en el hilo principal
                runOnUiThread(() -> readJSON());
            }
        }, 0, 1000); // Actualizar cada 5000 milisegundos (5 segundos), ajusta según tus necesidades
    }

    public void readJSON() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_READ, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    String response = new String(responseBody);
                    proccessJSON(response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Manejar el fallo de la solicitud aquí si es necesario
            }
        });
    }

    private void proccessJSON(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray feeds = root.getJSONArray("feeds");
            String valor, texto = "";

            for (int i = 0; i < feeds.length(); i++) {
                valor = feeds.getJSONObject(i).getString("field1");
                texto = texto + valor + "\n";
            }
            textView.setText(texto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
