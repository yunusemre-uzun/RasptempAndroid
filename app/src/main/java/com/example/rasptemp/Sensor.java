package com.example.rasptemp;

public class Sensor {
    private float value, max_limit, min_limit;
    private int id = -1;
    public String name;
    private Integer sensor_page;
    private Integer shelf;
    public Sensor(String name, float value, int sensor_page, int shelf) {
        this.name = name;
        this.value = value;
        this.sensor_page = sensor_page;
        this.shelf = shelf;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setId(int id) {
        this.id = id;
    }

    public  float getValue() {
        return this.value;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setMax_limit(float max_limit) {
        this.max_limit = max_limit;
    }

    public void setMin_limit(float min_limit) {
        this.min_limit = min_limit;
    }

    public int getId() {
        return  this.id;
    }

    public int getSensorPage() { return  this.sensor_page; }

    public int getShelf() { return  this.shelf; }
}
