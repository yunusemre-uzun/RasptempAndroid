package com.example.rasptemp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseDatabase firebase_database;
    private DatabaseReference database_reference;
    private List<String> sensor_name_array;
    private List<String> sensor_value_array;
    private List<Integer> sensor_id_array=null;
    private int sensor_count;
    private String time;
    private int padding=0;
    private int sensor_max = 25;
    private SensorRegisterer sensor_registerer = SensorRegisterer.getInstance();
    public static TextView timerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerView  = (TextView) findViewById(R.id.timer_view);
        createFireBaseConnection();
    }

    protected  void startCounter() {
        new CountDownTimer(60000, 1000){
            public void onTick(long millisUntilFinished){
                MainActivity.timerView.setText(String.valueOf(millisUntilFinished/1000));
            }
            public  void onFinish(){
                timerView.setText("Yeni veriler bekleniyor.");
            }
        }.start();
    }

    protected void  createFireBaseConnection(){
        firebase_database = FirebaseDatabase.getInstance();
        database_reference = firebase_database.getReference("server").child("temperatures");
        database_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String, Map> map = (Map<String, Map>) dataSnapshot.getValue();
                ParseSensorData(map);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    protected void ParseSensorData(Map<String, Map> database_result) {
        sensor_name_array = new ArrayList<String>();
        sensor_value_array = new ArrayList<String>();
        sensor_count = 0;
        for (Map.Entry<String,Map> entry : database_result.entrySet()){
            time = entry.getKey();
            for (Object sensor_name : entry.getValue().keySet()) {
                System.out.println(sensor_name);
                sensor_name_array.add((String) sensor_name);
                sensor_count++;
            }
            for (Object sensor_value : entry.getValue().values()) {
                sensor_value_array.add(String.valueOf(sensor_value));
            }
        }
        timerView.setText("Yeni veriler indirildi.");
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateSensorData();
        startCounter();
    }


    protected  void updateSensorData() {
        createSensors();
        updateView();
    }

    protected void createSensors() {
        if (this.sensor_id_array == null) {
            this.sensor_id_array = new ArrayList<Integer>();
        }
        for(int i=0;i<this.sensor_count;i++) {
            Sensor temp_sensor = new Sensor(this.sensor_name_array.get(i), Float.valueOf(this.sensor_value_array.get(i)));
            int id = this.sensor_registerer.RegisterNewSensor(temp_sensor);
            if(id != -1) {
                this.sensor_id_array.add(id);
            }
        }
    }

    protected void updateView() {
        SpannableString sensor_name;
        float sensor_value;
        TextView time_view = findViewById(R.id.date_time);
        time_view.setText(time);


        //Change the code to set fields dynamically
        if(this.padding*3 < this.sensor_value_array.size()){
            TextView sensor_name_1 = findViewById(R.id.sensor_1_name);
            TextView sensor_value_1 = findViewById(R.id.sensor_1_value);
            ProgressBar sensor_bar_1 = findViewById(R.id.sensor_1_bar);
            sensor_value = Float.valueOf(sensor_value_array.get(this.padding*3 + 0));
            sensor_name = this.underlineString(sensor_name_array.get(this.padding*3 +0));
            sensor_bar_1.setProgress(Math.round(sensor_value));
            this.setBarColor(sensor_value, sensor_bar_1);
            sensor_bar_1.setMax(this.sensor_max);
            sensor_name_1.setText(sensor_name);
            sensor_value_1.setText(Float.toString(sensor_value));
        }


        if(this.padding*3 + 1 < this.sensor_value_array.size()) {
            TextView sensor_name_2 = findViewById(R.id.sensor_2_name);
            TextView sensor_value_2 = findViewById(R.id.sensor_2_value);
            ProgressBar sensor_bar_2 = findViewById(R.id.sensor_2_bar);
            sensor_value = Float.valueOf(sensor_value_array.get(this.padding * 3 + 1));
            sensor_name = this.underlineString(sensor_name_array.get(this.padding * 3 + 1));
            sensor_bar_2.setProgress(Math.round(sensor_value));
            this.setBarColor(sensor_value, sensor_bar_2);
            sensor_bar_2.setMax(this.sensor_max);
            sensor_name_2.setText(sensor_name);
            sensor_value_2.setText(Float.toString(sensor_value));
        }

        if(this.padding*3 + 2 < this.sensor_value_array.size()) {
            TextView sensor_name_3 = findViewById(R.id.sensor_3_name);
            TextView sensor_value_3 = findViewById(R.id.sensor_3_value);
            ProgressBar sensor_bar_3 = findViewById(R.id.sensor_3_bar);
            sensor_value = Float.valueOf(sensor_value_array.get(this.padding * 3 + 2));
            sensor_name = this.underlineString(sensor_name_array.get(this.padding * 3 + 2));
            sensor_bar_3.setProgress(Math.round(sensor_value));
            this.setBarColor(sensor_value, sensor_bar_3);
            sensor_bar_3.setMax(this.sensor_max);
            sensor_name_3.setText(sensor_name);
            sensor_value_3.setText(Float.toString(sensor_value));
        }
    }

    protected SpannableString underlineString(String str) {
        SpannableString content = new SpannableString(str);
        content.setSpan(new UnderlineSpan(), 0, str.length(), 0);
        return content;
    }

    protected void setBarColor(float current_value, ProgressBar bar){
        if (Float.valueOf(current_value) > this.sensor_max*0.75) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
    }
}
