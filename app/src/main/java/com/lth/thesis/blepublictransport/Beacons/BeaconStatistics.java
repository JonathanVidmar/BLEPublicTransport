package com.lth.thesis.blepublictransport.Beacons;

import com.lth.thesis.blepublictransport.Utils.KFBuilder;
import com.lth.thesis.blepublictransport.Utils.KalmanFilter;
import org.altbeacon.beacon.Beacon;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * A helper class
 *
 * @author  Jonathan Vidmar
 * @version 1.0
 */
public class BeaconStatistics {

    private DescriptiveStatistics stats;
    private DescriptiveStatistics stats2;
    private DescriptiveStatistics filteredStats;
    private KalmanFilter kf;
    private double lastCalculatedDistance;
    private double lastFilteredReading = -1;
    private static final int WINDOW = 30;

    public BeaconStatistics() {

        stats = new DescriptiveStatistics();
        stats.setWindowSize(WINDOW);
        stats2 = new DescriptiveStatistics();
        stats2.setWindowSize(WINDOW);
        filteredStats = new DescriptiveStatistics();
        filteredStats.setWindowSize(6);

        lastCalculatedDistance = 0;

        // Baserat på konstant avstånd från beacon, dvs låga störningar från systemet (R), höga störningar från mätning (Q)
        // Värden borde baseras på faktiska statistiska mätvärden dock
        // filter(measuredValue) returnerar det uträknade värdet
        kf = new KFBuilder()
                .R(0.001)
                .Q(16.0)
                .build();
    }

    public void updateDistance(Beacon b, double movementState, double txPower) {

        stats.addValue(b.getRssi());
        stats2.addValue(txPower);
        stats.getSortedValues();
        stats2.getSortedValues();
        lastFilteredReading = stats.getPercentile(50);
        double mNoise = Math.sqrt((100*9/Math.log(10))*Math.log(1+Math.pow(filteredStats.getMean()/filteredStats.getStandardDeviation(), 2)));
        if (!Double.isInfinite(mNoise) && !Double.isNaN(mNoise)) kf.setMeasurementNoise(mNoise);
        calculateDistance(stats2.getPercentile(50), movementState);
    }

    private void calculateDistance(double txPower, double movementState) {
        double n = 2.5;   // Signal propogation exponent
        double d0 = 1;  // Reference distance in meters
        double C = 0;   // Gaussian variable for mitigating flat fading

        lastCalculatedDistance = kf.filter(d0 * Math.pow(10, (lastFilteredReading - txPower - C) / (-10 * n)), movementState);
        if (!Double.isNaN(lastCalculatedDistance)) filteredStats.addValue(lastCalculatedDistance);
    }

    public double getDistance() {
        return filteredStats.getPercentile(50);
    }

}
