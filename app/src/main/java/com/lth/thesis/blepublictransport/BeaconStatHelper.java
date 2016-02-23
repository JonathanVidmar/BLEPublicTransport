package com.lth.thesis.blepublictransport;

import org.altbeacon.beacon.Beacon;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by Jonathan on 2/23/2016.
 */
public class BeaconStatHelper {

    private DescriptiveStatistics stats;
    private KalmanFilter kf;
    private double lastCalculatedDistance;

    public BeaconStatHelper() {

        stats = new DescriptiveStatistics();
        stats.setWindowSize(15); // sätter antalet mätningar som används vid medianen
        lastCalculatedDistance = 0;

        // Baserat på konstant avstånd från beacon, dvs låga störningar från systemet (R), höga störningar från mätning (Q)
        // Värden borde baseras på faktiska statistiska mätvärden dock
        // filter(measuredValue) returnerar det uträknade värdet
        kf = new KFilterBuilder()
                .R(0.01)
                .Q(20.0)
                .build();
    }

    public void updateDistance(Beacon b){
        stats.addValue(b.getRssi());
        kf.filter(stats.getPercentile(50));
        calculateDistance(b.getTxPower());
    }
    private void calculateDistance(double txPower){
        double n = 3;   // Signal propogation exponent
        double d0 = 1;  // Reference distance in meters
        double C = 0;   // Gaussian variable for mitigating flat fading
        double fRSSI = kf.lastMeasurement();

        lastCalculatedDistance = d0 * Math.pow(10,(fRSSI - txPower - C)/ (-10 * n));
    }
    public double getDistance(){
        return lastCalculatedDistance;
    }

}
