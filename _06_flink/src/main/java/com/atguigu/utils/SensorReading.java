package com.atguigu.utils;

public class SensorReading {
    public String sensorId;
    public Double temperature;
    
    @Override
    public String toString() {
        return "SensorReading{" +
                 "sensorId='" + sensorId + '\'' +
                 ", temperature=" + temperature +
                 '}';
    }
    
    public SensorReading(String sensorId, Double temperature) {
        this.sensorId = sensorId;
        this.temperature = temperature;
    }
    
    public SensorReading(String sensorId, Integer integer) {
    }
}
