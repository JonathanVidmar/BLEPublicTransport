package com.lth.thesis.blepublictransport.Beacons;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by Jonathan on 2/23/2016.
 */
public class WalkDetection implements SensorEventListener {


    // http://research.microsoft.com/pubs/166309/com273-chintalapudi.pdf
    // http://www.cl.cam.ac.uk/~ab818/StepDetectionSmartphones.pdf

    private final static long STD_WINDOW = 800;
    private final static long CRR_WINDOW = 2000;
    private final static double STD_MIN_INIT_MOTION = 0.6;
    private final static double STD_MIN_END_MOTION = 0.1;
    private final static double WALKING_THRESHOLD = 0.5;
    private final static long TAU_MIN = 600;
    private final static long TAU_MAX = 1500;
    private final static long TAU_STEP = 100;

    private final static int STATE_IDLE = 0;
    private final static int STATE_WALKING = 1;
    private final static int STATE_PENDING = 2;

    private final static int FOUND = 3;
    private final static int LOST = 4;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastWindow = -1;
    private int currentState = STATE_IDLE;
    private List<List<Double>> raw;
    private List<Double> rawBatch;
    private int lastBatchNumber = 0;

    // needed for accurate timestamps
    private long[] events = new long[2];
    private int it = 0;
    private long divisor; // to get from timestamp to milliseconds
    private long offset; // to get from event milliseconds to system milliseconds
    private boolean sensorClockCalibrated = false;

    public WalkDetection(Application app) {

        mSensorManager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        raw = new ArrayList<>();
        rawBatch = new ArrayList<>();
        startDetection();
    }

    public void startDetection() {
        mSensorManager.registerListener(this, mSensor, 1000);
    }

    public void killDetection() {
        mSensorManager.unregisterListener(this);
    }

    public int getState() {
        return currentState == STATE_PENDING ? STATE_IDLE : currentState;
    }

    /**
     * Called when sensor values have changed.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (sensorClockCalibrated) {


                if (lastWindow == -1) lastWindow = getTimestampMillis(event.timestamp);

                addRawReadingToBatches(event);

                switch (currentState) {
                    case STATE_IDLE:
                        if (timeWindowExceeded(STD_WINDOW)) {
                            if (motionWas(FOUND)) {
                                currentState = STATE_PENDING;
                                Log.i("walk", "PENDING");
                            }
                            else resetWindow();
                        }
                        break;
                    case STATE_PENDING:
                        if (timeWindowExceeded(CRR_WINDOW)) {
                            if (motionEqualsWalking()) {
                                currentState = STATE_WALKING;
                                Log.i("walk", "WALKING");
                            }
                            resetWindow();
                        }
                        break;
                    case STATE_WALKING:
                        if (timeWindowExceeded(STD_WINDOW)) {
                            if (motionWas(LOST)) {
                                currentState = STATE_IDLE;
                                Log.i("walk", "IDLE");
                            }
                            resetWindow();
                        }
                        break;
                }
            } else {
                if (it == 0) {
                    events[0] = event.timestamp;
                    it++;
                } else {
                    events[1] = event.timestamp;
                    initSensorClock(System.currentTimeMillis());
                }
            }
        }
    }

    private void addRawReadingToBatches(SensorEvent event) {
        int delta = FastMath.toIntExact((getTimestampMillis(event.timestamp) - lastWindow) / 100);
        double measurement = sqrt(pow(event.values[0], 2) + pow(event.values[1], 2) + pow(event.values[2], 2));
        if (delta != lastBatchNumber) {
            flushBatchReadings();
            lastBatchNumber = delta;
        }
        rawBatch.add(measurement);
    }

    private DescriptiveStatistics getLagSpecificStats(long maxLag) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int maxBatch = FastMath.toIntExact(maxLag) / 100 + 1;
        maxBatch = maxBatch > raw.size() ? raw.size() : maxBatch;
        for (int i = 0; i < maxBatch; i++)
            for (double d :
                    raw.get(i)) {
                stats.addValue(d);
            }
        return stats;
    }

    private DescriptiveStatistics getStatsForDataSegment(List<Double> array) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double d :
                array) {
            stats.addValue(d);
        }
        return stats;
    }

    private void flushBatchReadings() {
        ArrayList<Double> array = new ArrayList<>(rawBatch.size());
        array.addAll(rawBatch);
        raw.add(array);
        rawBatch.clear();
    }

    /**
     * Computes the maximum normalized auto correlation of raw with respect to time lags TAU_MIN and TAU_MAX
     */
    private double maxNormAutoCorr() {
        List<Double> rawConcat = new ArrayList<>();
        double std, mean, stdTau, meanTau, sum;
        double max = Double.MIN_VALUE;
        //int tauOpt = 0;
        for (List<Double> l :
                raw) {
            rawConcat.addAll(l);
        }
        //Log.i("walk", "Raw size: " + rawConcat.size());
        for (int tau = (FastMath.toIntExact(TAU_MIN) / 100) - 1; tau < FastMath.toIntExact(TAU_MAX) / 100; tau += TAU_STEP / 100) {
            int tauIndex = indexOfTau(tau);
            for (int m = 0; m < rawConcat.size() - (tauIndex * 2) - 1; m++) {
                DescriptiveStatistics stats = getStatsForDataSegment(rawConcat.subList(m, m + tauIndex));
                //Log.i("walk", "stats size: " + stats.getValues().length);
                std = stats.getStandardDeviation();
                mean = stats.getMean();
                DescriptiveStatistics statsTau = getStatsForDataSegment(rawConcat.subList(m + tauIndex, m + tauIndex * 2));
                //Log.i("walk", "statsTau size: " + statsTau.getValues().length);
                stdTau = statsTau.getStandardDeviation();
                meanTau = statsTau.getMean();
                sum = 0;
                int k;
                for (k = 0; k < tauIndex - 1; k++) {
                    sum += (rawConcat.get(m + k) - mean) * (rawConcat.get(m + k + tauIndex) - meanTau);
                }
                double temp = sum / (std * stdTau * tauIndex);
                //Log.i("walk", "Tau: " + tau + " - X(" + m + "," + tauIndex + ") = " + temp);
                max = max(max, temp);
                //if (temp == max) tauOpt = tau;
            }
        }
        return max;
    }

    private int indexOfTau(int tau) {
        int index = 0;
        for (int k = 0; k < tau; k++) {
            index += raw.get(k).size();
        }
        return index;
    }

    private void resetWindow() {
        raw.clear();
        lastWindow = -1;
    }

    private boolean timeWindowExceeded(long windowType) {
        flushBatchReadings();
        return System.currentTimeMillis() - lastWindow >= windowType;
    }

    private boolean motionEqualsWalking() {
        double temp = maxNormAutoCorr();
        //Log.i("walk", "corr: " + temp);
        //Log.i("walk", "std" + getLagSpecificStats(CRR_WINDOW).getStandardDeviation());
        return  temp >= WALKING_THRESHOLD;
    }

    private boolean motionWas(int event) {
        boolean eventResult = false;
        switch (event) {
            case FOUND:
                eventResult = getLagSpecificStats(STD_WINDOW).getStandardDeviation() > STD_MIN_INIT_MOTION;
                break;
            case LOST:
                eventResult = getLagSpecificStats(STD_WINDOW).getStandardDeviation() < STD_MIN_END_MOTION;
                break;
        }
        return eventResult;
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unneeded
    }

    private void initSensorClock(long sys) {

        sensorClockCalibrated = true;
        long timestampDelta = events[1] - events[0];

        if (timestampDelta > 500) { // in reality ~1 vs ~1,000,000
            // timestamps are in nanoseconds
            divisor = 1000000;
        } else {
            // timestamps are in milliseconds
            divisor = 1;
        }

        offset = sys - events[0] / divisor;
    }

    private long getTimestampMillis(long timestamp) {
        return timestamp / divisor + offset;
    }
}
