package _99_util.sensor;

public class SensorReading_my {
    public String sensorId;
    public Double temperature;
    
    @Override
    public String toString() {
        return "SensorReading{" +
                 "sensorId='" + sensorId + '\'' +
                 ", temperature=" + temperature +
                 '}';
    }
    
    public SensorReading_my(String sensorId, Double temperature) {
        this.sensorId = sensorId;
        this.temperature = temperature;
    }
    
    public SensorReading_my() {
    }
}
