package com.example.rasptemp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.CountDownTimer;
import android.widget.TextView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public int counter=60;
    TextView timerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerView = (TextView) findViewById(R.id.timer_view);
        new CountDownTimer(30000, 1000){
            public void onTick(long millisUntilFinished){
                timerView.setText(String.valueOf(counter));
                counter--;
            }
            public  void onFinish(){
                timerView.setText("Yeni veriler indiriliyor.");
            }
        }.start();
    }
}
