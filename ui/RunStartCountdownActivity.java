package com.example.running_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.running_app.R;

public class RunStartCountdownActivity extends AppCompatActivity {
    private TextView countdownText;
    private int countdownValue = 3;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_start_countdown);

        countdownText = findViewById(R.id.countdown_text);

        timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (countdownValue >= 1) {
                    countdownText.setText(String.valueOf(countdownValue));
                    countdownValue--;
                }
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        timer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}

