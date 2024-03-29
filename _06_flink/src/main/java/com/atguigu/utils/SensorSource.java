package com.atguigu.utils;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;



public  class SensorSource implements SourceFunction<SensorReading> {
    private boolean running = true;
    private Random random = new Random();
    
    @Override
    public void run(SourceContext<SensorReading> ctx) throws Exception {
        while (running) {
            for (int i = 1; i < 4; i++) {
                ctx.collect(new SensorReading(
                  "sensor_" + i,
                  random.nextGaussian()
                ));
            }
            Thread.sleep(1000L);
        }
    }
    
    @Override
    public void cancel() {
        running = false;
    }
}
