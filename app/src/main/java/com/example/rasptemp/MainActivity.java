package com.example.rasptemp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import org.w3c.dom.Text;

import java.text.DecimalFormat;
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
    private List<Sensor> sensor_array;
    private int sensor_count;
    private String time;
    private int page=1;
    private int sensor_max;
    private int sensor_min;
    SharedPreferences s_pref;
    private int page_count;
    public static TextView timerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerView  = (TextView) findViewById(R.id.timer_view);
        createFireBaseConnection();
        this.s_pref = getSharedPreferences("data", 0);
        this.sensor_max = this.s_pref.getInt("sensor_max", 25);
        this.sensor_min = this.s_pref.getInt("sensor_min", 15);
    }


    public void nextButtonAttributes(View v){
        if(this.page == this.page_count){
            return;
        }
        this.page++;
        this.updateView();
    }

    public void prevButtonAttributes(View v){
        if(this.page == 1){
            return;
        }
        this.page--;
        this.updateView();
    }

    public void setMax(View v){
        EditText max_temp_view = findViewById(R.id.setMax);
        String new_value = max_temp_view.getText().toString();
        Integer new_value_int = Integer.valueOf(new_value);
        if (new_value_int < this.sensor_min) return;
        this.sensor_max = new_value_int;
        SharedPreferences.Editor sensor_max_editor = this.s_pref.edit();
        sensor_max_editor.putInt("sensor_max", new_value_int);
        sensor_max_editor.commit();
        this.updateView();
    }

    public void setMin(View v){
        EditText min_temp_view = findViewById(R.id.setMin);
        String new_value = min_temp_view.getText().toString();
        Integer new_value_int = Integer.valueOf(new_value);
        if (new_value_int > this.sensor_max) return;
        this.sensor_min = new_value_int;
        SharedPreferences.Editor sensor_max_editor = this.s_pref.edit();
        sensor_max_editor.putInt("sensor_min", new_value_int);
        sensor_max_editor.commit();
        this.updateView();
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
        sensor_array = new ArrayList<Sensor>();
        sensor_count = 0;
        this.page_count = 1;
        for (Map.Entry<String,Map> entry : database_result.entrySet()){
            time = entry.getKey();
            for (Object sensor_info : entry.getValue().keySet()) {
                System.out.println(sensor_info);
                String[] sensor_name_splitted = ((String) sensor_info).split(":");
                String[] sensor_position = sensor_name_splitted[0].split("-");
                Integer sensor_class = Integer.parseInt(sensor_position[0]);
                if(sensor_class > this.page_count) {
                    page_count = sensor_class;
                }
                Integer sensor_shelf = Integer.parseInt(sensor_position[1]);
                String sensor_name = sensor_name_splitted[1];
                Sensor new_sensor = new Sensor(sensor_name, -1, sensor_class, sensor_shelf);
                new_sensor.setId(sensor_count);
                sensor_array.add(new_sensor);
                sensor_count++;

            }
            int i = 0;
            for (Object sensor_value : entry.getValue().values()) {
                sensor_array.get(i).setValue(Float.valueOf(String.valueOf(sensor_value)));
                i++;
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
        updateView();
    }



    @SuppressLint("SetTextI18n")
    protected void updateView() {
        Sensor sensor;

        TextView time_view = findViewById(R.id.date_time);
        time_view.setText(time);

        TextView page = findViewById(R.id.page);
        page.setText((this.page) + "/" + Integer.toString(this.page_count));

        TextView temp = findViewById(R.id.setMax);
        temp.setText(String.valueOf(this.sensor_max));

        temp = findViewById(R.id.setMin);
        temp.setText(String.valueOf(this.sensor_min));


        //Change the code to set fields dynamically
        sensor = getSensorWithClassAndShelf(this.page, 1);
        TextView sensor_name_view = findViewById(R.id.sensor_1_name);
        TextView sensor_value_view = findViewById(R.id.sensor_1_value);
        ProgressBar sensor_bar_view = findViewById(R.id.sensor_1_bar);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_2_name);
        sensor_value_view = findViewById(R.id.sensor_2_value);
        sensor_bar_view = findViewById(R.id.sensor_2_bar);
        sensor = getSensorWithClassAndShelf(this.page, 2);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_3_name);
        sensor_value_view = findViewById(R.id.sensor_3_value);
        sensor_bar_view = findViewById(R.id.sensor_3_bar);
        sensor = getSensorWithClassAndShelf(this.page, 3);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor = getSensorWithClassAndShelf(this.page, 4);
        sensor_name_view = findViewById(R.id.sensor_4_name);
        sensor_value_view = findViewById(R.id.sensor_4_value);
        sensor_bar_view = findViewById(R.id.sensor_4_bar);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_5_name);
        sensor_value_view = findViewById(R.id.sensor_5_value);
        sensor_bar_view = findViewById(R.id.sensor_5_bar);
        sensor = getSensorWithClassAndShelf(this.page, 5);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_6_name);
        sensor_value_view = findViewById(R.id.sensor_6_value);
        sensor_bar_view = findViewById(R.id.sensor_6_bar);
        sensor = getSensorWithClassAndShelf(this.page, 6);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_7_name);
        sensor_value_view = findViewById(R.id.sensor_7_value);
        sensor_bar_view = findViewById(R.id.sensor_7_bar);
        sensor = getSensorWithClassAndShelf(this.page, 7);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_8_name);
        sensor_value_view = findViewById(R.id.sensor_8_value);
        sensor_bar_view = findViewById(R.id.sensor_8_bar);
        sensor = getSensorWithClassAndShelf(this.page, 8);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_9_name);
        sensor_value_view = findViewById(R.id.sensor_9_value);
        sensor_bar_view = findViewById(R.id.sensor_9_bar);
        sensor = getSensorWithClassAndShelf(this.page, 9);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_10_name);
        sensor_value_view = findViewById(R.id.sensor_10_value);
        sensor_bar_view = findViewById(R.id.sensor_10_bar);
        sensor = getSensorWithClassAndShelf(this.page, 10);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_11_name);
        sensor_value_view = findViewById(R.id.sensor_11_value);
        sensor_bar_view = findViewById(R.id.sensor_11_bar);
        sensor = getSensorWithClassAndShelf(this.page, 11);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

        sensor_name_view = findViewById(R.id.sensor_12_name);
        sensor_value_view = findViewById(R.id.sensor_12_value);
        sensor_bar_view = findViewById(R.id.sensor_12_bar);
        sensor = getSensorWithClassAndShelf(this.page, 12);
        this.setBox(sensor, sensor_name_view, sensor_value_view, sensor_bar_view);

    }

    protected void setBox(Sensor sensor, TextView sensor_name_view, TextView sensor_value_view, ProgressBar sensor_bar_view) {
        if(sensor!=null) {
            this.setFieldValues(sensor_name_view, sensor_value_view, sensor_bar_view, sensor);
            this.setVisible(sensor_name_view, sensor_value_view, sensor_bar_view);
        }
        else {
            this.setInvisible(sensor_name_view, sensor_value_view, sensor_bar_view);
        }
    }

    protected void setInvisible(TextView sensor_name_view, TextView sensor_value_view, ProgressBar sensor_bar_view){
        sensor_name_view.setVisibility(View.INVISIBLE);
        sensor_value_view.setVisibility(View.INVISIBLE);
        sensor_bar_view.setVisibility(View.INVISIBLE);
    }

    protected void setVisible(TextView sensor_name_view, TextView sensor_value_view, ProgressBar sensor_bar_view){
        sensor_name_view.setVisibility(View.VISIBLE);
        sensor_value_view.setVisibility(View.VISIBLE);
        sensor_bar_view.setVisibility(View.VISIBLE);
    }

    protected void setFieldValues(TextView sensor_name_view, TextView sensor_value_view, ProgressBar sensor_bar_view, Sensor sensor) {
        DecimalFormat df = new DecimalFormat("#.0");
        float sensor_value = sensor.getValue();
        SpannableString sensor_name = this.underlineString(sensor.name);
        String sensor_value_string = df.format(sensor_value);
        sensor_bar_view.setProgress(Math.round(sensor_value));
        sensor_bar_view.setMax(50);
        sensor_name_view.setText(sensor_name);
        sensor_value_view.setText(sensor_value_string);
        this.setBarColor(sensor_value, sensor_bar_view);
        return;
    }

    protected Sensor getSensorWithClassAndShelf(int sensor_class, int sensor_shelf) {
        for(int i=0; i<this.sensor_array.size();i++) {
            Sensor sensor = this.sensor_array.get(i);
            if(sensor.getShelf() == sensor_shelf && sensor.getSensorPage() == sensor_class) {
                return sensor;
            }
        }
        return null;
    }

    protected SpannableString underlineString(String str) {
        SpannableString content = new SpannableString(str);
        content.setSpan(new UnderlineSpan(), 0, str.length(), 0);
        return content;
    }

    protected void setBarColor(float current_value, ProgressBar bar){
        if (Float.valueOf(current_value) > this.sensor_max || Float.valueOf(current_value) < this.sensor_min) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
        else {
            bar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        }
    }
}
