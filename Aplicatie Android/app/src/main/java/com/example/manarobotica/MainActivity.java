package com.example.manarobotica;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import java.io.IOException;
import android.content.Intent;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String RPI_URL = "http://192.168.1.167:5000/comanda";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View loadingOverlay = findViewById(R.id.loadingOverlay);

        configurariDeget(R.id.s0, R.id.t0, R.id.glowMic, 0, "Degetul Mic");
        configurariDeget(R.id.s1, R.id.t1, R.id.glowInelar, 1, "Inelar");
        configurariDeget(R.id.s2, R.id.t2, R.id.glowMijlociu, 2, "Mijlociu");
        configurariDeget(R.id.s3, R.id.t3, R.id.glowAratator, 3, "Arătător");
        configurariDeget(R.id.s4, R.id.t4, R.id.glowMare, 4, "Degetul Mare");

        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    trimiteComandaHTTP(i, 0);
                    Thread.sleep(1000);
                }
                // ascund ecranul de loading
                runOnUiThread(() -> {
                    loadingOverlay.animate()
                            .alpha(0f)
                            .setDuration(600)
                            .withEndAction(() -> loadingOverlay.setVisibility(View.GONE));
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        androidx.appcompat.widget.AppCompatButton btnMergiLaPreset = findViewById(R.id.btnMergiLaPreset);
        btnMergiLaPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PresetActivity.class);
                startActivityForResult(intent, 100);
            }
        });
    }

    private void configurariDeget(int seekBarId, int textViewId,int glowId, int fingerIndex, String fingerName) {
        SeekBar seekBar = findViewById(seekBarId);
        TextView textView = findViewById(textViewId);
        final View glowImage = findViewById(glowId);

        seekBar.setProgress(0);
        textView.setText(fingerName + ": 0°");
        glowImage.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // actualizez textul cand misc degetul pe ecran
                textView.setText(fingerName + ": " + progress + "°");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                glowImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                glowImage.setVisibility(View.INVISIBLE);
                trimiteComandaHTTP(fingerIndex, seekBar.getProgress());
            }
        });
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
            long startTime = System.currentTimeMillis();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    long endTime = System.currentTimeMillis();
                    long latenta = endTime - startTime;
                    android.util.Log.d("TEST_LATENTA", "Deget: " + id + " | Unghi: " + unghi + "° | Timp Retea: " + latenta + " ms");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int[] unghiuriPrimite = data.getIntArrayExtra("noi_unghiuri");

            if (unghiuriPrimite != null && unghiuriPrimite.length == 5) {

                SeekBar s0 = findViewById(R.id.s0);
                SeekBar s1 = findViewById(R.id.s1);
                SeekBar s2 = findViewById(R.id.s2);
                SeekBar s3 = findViewById(R.id.s3);
                SeekBar s4 = findViewById(R.id.s4);


                TextView t0 = findViewById(R.id.t0);
                TextView t1 = findViewById(R.id.t1);
                TextView t2 = findViewById(R.id.t2);
                TextView t3 = findViewById(R.id.t3);
                TextView t4 = findViewById(R.id.t4);


                s0.setProgress(unghiuriPrimite[0]);
                s1.setProgress(unghiuriPrimite[1]);
                s2.setProgress(unghiuriPrimite[2]);
                s3.setProgress(unghiuriPrimite[3]);
                s4.setProgress(unghiuriPrimite[4]);


                t0.setText("Degetul Mic: " + unghiuriPrimite[0] + "°");
                t1.setText("Inelar: " + unghiuriPrimite[1] + "°");
                t2.setText("Mijlociu: " + unghiuriPrimite[2] + "°");
                t3.setText("Arătător: " + unghiuriPrimite[3] + "°");
                t4.setText("Degetul Mare: " + unghiuriPrimite[4] + "°");
            }
        }
    }
}