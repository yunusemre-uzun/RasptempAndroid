package com.example.rasptemp;

import java.util.ArrayList;
import java.util.List;

public class SensorRegisterer {
    private static SensorRegisterer single_instance = null;
    private List<Sensor> registered_sensors = null;
    private int registered_sensor_count = 0;

    private SensorRegisterer()
    {
        System.out.println("Singleton class");
        this.registered_sensors = new ArrayList<Sensor>();
    }

    public static SensorRegisterer getInstance()
    {
        if (single_instance == null){
            SensorRegisterer.single_instance = new SensorRegisterer();
        }
        return SensorRegisterer.single_instance;
    }

    public int RegisterNewSensor(Sensor sensor) {
        if(sensor.getId() == -1) {
            sensor.setId(this.registered_sensor_count);
            this.registered_sensors.add(sensor);
            this.registered_sensor_count++;
            return registered_sensor_count-1;
        }
        else {
            if (this.isRegistered(sensor.getId()))
                return -1;
            sensor.setId(this.registered_sensor_count);
            this.registered_sensors.add(sensor);
            this.registered_sensor_count++;
            return registered_sensor_count-1;
        }
    }

    private boolean isRegistered(int id) {
        for(int i=0; i<this.registered_sensor_count;i++) {
            if(this.registered_sensors.get(i).getId() == id)
                return  false;
        }
        return true;
    }

    public boolean updateValueWithId(int id, float value) {
        for(int i=0; i<this.registered_sensor_count;i++) {
            if(this.registered_sensors.get(i).getId() == id) {
                this.registered_sensors.get(i).setValue(value);
                return true;
            }
        }
        return false;
    }

    public Sensor getSensorWithId(int id) {
        for(int i=0; i<this.registered_sensor_count;i++) {
            if(this.registered_sensors.get(i).getId() == id) {
                return this.registered_sensors.get(i);
            }
        }
        return null;
    }
}
