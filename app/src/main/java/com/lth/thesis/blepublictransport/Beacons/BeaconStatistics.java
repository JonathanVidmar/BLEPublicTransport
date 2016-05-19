package com.lth.thesis.blepublictransport.Beacons;

import com.lth.thesis.blepublictransport.Utils.KFBuilder;
import com.lth.thesis.blepublictransport.Utils.KalmanFilter;
import org.altbeacon.beacon.Beacon;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;

/**
 * A helper class
 *
 * @author  Jonathan Vidmar
 * @version 1.0
 */
public class BeaconStatistics {

    private DescriptiveStatistics mostRecentRSSI;
    private DescriptiveStatistics mostRecentTxPower;
    private KalmanFilter kf;
    private double lastCalculatedDistance;
    private double lastRawDistance;
    private double lastWOSC;
    private static final int WINDOW = 20;

    public BeaconStatistics() {

        mostRecentRSSI = new DescriptiveStatistics();
        mostRecentRSSI.setWindowSize(WINDOW);
        mostRecentTxPower = new DescriptiveStatistics();
        mostRecentTxPower.setWindowSize(WINDOW);

        lastCalculatedDistance = 0;
        lastRawDistance = 0;
        lastWOSC = 0;

        // Baserat på konstant avstånd från beacon, dvs låga störningar från systemet (R), höga störningar från mätning (Q)
        // Värden borde baseras på faktiska statistiska mätvärden dock
        // filter(measuredValue) returnerar det uträknade värdet
        kf = new KFBuilder()
                // filter for RSSI
                .R(10) // Initial process noise
                .Q(60.0) // Initial measurement noise
                .build();
    }

    public void updateDistance(Beacon b, double movementState, double txPower, double processNoise) {
        double lastFilteredReading = -1;

        mostRecentRSSI.addValue(b.getRssi());
        mostRecentTxPower.addValue(txPower);

        // Update measurement noise continually
        double mNoise = Math.sqrt((100 * 9 / Math.log(10))*Math.log(1 + Math.pow(mostRecentRSSI.getMean() / mostRecentRSSI.getStandardDeviation(), 2)));
        if (!Double.isInfinite(mNoise) && !Double.isNaN(mNoise)) kf.setMeasurementNoise(mNoise);
        kf.setProcessNoise(processNoise);
        lastFilteredReading = kf.filter(mostRecentRSSI.getPercentile(50), movementState);
        lastCalculatedDistance = calculateDistance(mostRecentTxPower.getPercentile(50), lastFilteredReading);
        lastRawDistance = calculateDistance(b.getTxPower(), b.getRssi());
        lastWOSC = calculateDistance(b.getTxPower(), lastFilteredReading);
    }

    private double calculateDistance(double txPower, double rssi) {
        double n = 2.5;   // Signal propogation exponent
        double d0 = 1;  // Reference distance in meters
        double C = 0;   // Gaussian variable for mitigating flat fading

        // model specific adjustments for Samsung S3 as per Android Beacon Library
        double mReceiverRssiSlope = 0;
        double mReceiverRssiOffset = -4;

        // calculation of adjustment
        double adjustment = mReceiverRssiSlope * rssi + mReceiverRssiOffset;
        double adjustedRssi = rssi - adjustment;


        // Log-distance path loss model
        return d0 * Math.pow(10.0, (adjustedRssi - txPower - C) / (-10 * n));
    }

    public double getDistance() {
        return lastCalculatedDistance;
    }
    public double getRawDistance() {
        return lastRawDistance;
    }

    public double getDistanceWOSC() {
        return lastWOSC;
    }

}
