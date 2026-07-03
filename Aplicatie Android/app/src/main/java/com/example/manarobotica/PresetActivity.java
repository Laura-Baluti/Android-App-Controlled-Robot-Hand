package com.example.manarobotica;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import okhttp3.*;
import java.io.IOException;
import android.content.Intent;

public class PresetActivity extends AppCompatActivity {

    private static final String RPI_URL = "http://192.168.1.167:5000/comanda";
    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler();
    private final int[] ultimeleUnghiuri = {0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        androidx.appcompat.widget.AppCompatButton btnDeschis = findViewById(R.id.btnDeschis);
        androidx.appcompat.widget.AppCompatButton btnInchis = findViewById(R.id.btnInchis);
        androidx.appcompat.widget.AppCompatButton btnPace = findViewById(R.id.btnPace);
        androidx.appcompat.widget.AppCompatButton btnOk = findViewById(R.id.btnOk);
        androidx.appcompat.widget.AppCompatButton btnLike = findViewById(R.id.btnLike);
        androidx.appcompat.widget.AppCompatButton btnHome = findViewById(R.id.btnHome);

        btnDeschis.setOnClickListener(v -> executaGest(0, 0, 0, 0, 0));
        btnInchis.setOnClickListener(v -> executaGest(180, 180, 180, 180, 180));
        btnPace.setOnClickListener(v -> executaGest(180, 180, 0, 0, 180));
        btnOk.setOnClickListener(v -> executaGest(0, 0, 0, 180, 180));
        btnLike.setOnClickListener(v -> executaGest(180, 180, 180, 180, 0));

        btnHome.setOnClickListener(v -> {
            Intent intentInapoi = new Intent();
            intentInapoi.putExtra("noi_unghiuri", ultimeleUnghiuri);
            setResult(RESULT_OK, intentInapoi);
            finish();
        });
    }

    private void executaGest(int u0, int u1, int u2, int u3, int u4) {
        ultimeleUnghiuri[0] = u0;
        ultimeleUnghiuri[1] = u1;
        ultimeleUnghiuri[2] = u2;
        ultimeleUnghiuri[3] = u3;
        ultimeleUnghiuri[4] = u4;
        int delayIntreDegete = 1000;

        for (int i = 0; i < 5; i++) {
            final int idDeget = i;
            final int valoareUnghi = ultimeleUnghiuri[i];

            handler.postDelayed(() -> trimiteComandaHTTP(idDeget, valoareUnghi), (long) i * delayIntreDegete);}
    }

    private void trimiteComandaHTTP(int id, int unghi) {
        String json = "{\"id_deget\": " + id + ", \"valoare_unghi\": " + unghi + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(RPI_URL)
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}