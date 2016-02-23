package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by Jonathan on 2/23/2016.
 */
public class WalkDetection implements SensorEventListener {

    private final static long STD_WINDOW = 800;
    private final static long CRR_WINDOW = 2000;
    private final static double STD_MIN_MOTION = 0.6;

    private final static int STATE_IDLE = 0;
    private final static int STATE_WALKING = 1;
    private final static int STATE_PENDING = 2;

    private final static int FOUND = 3;
    private final static int LOST = 4;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DescriptiveStatistics stats;
    private long lastWindow;
    private int currentState = STATE_IDLE;

    public WalkDetection(Application app) {

        mSensorManager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stats = new DescriptiveStatistics();
        lastWindow = System.currentTimeMillis();
        createListener();
    }

    public void createListener() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void killListener() {
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
            stats.addValue(Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2)));

            switch (currentState) {
                case STATE_IDLE:
                    if (timeWindowExceeded(STD_WINDOW)) {
                        if (motionWas(FOUND)) currentState = STATE_PENDING;
                        else resetWindow();
                    }
                    break;
                case STATE_PENDING:
                    if (timeWindowExceeded(CRR_WINDOW)) {
                        // Så att eeeh... bara att fylla i algoritmen enligt nedan:
                        // http://research.microsoft.com/pubs/166309/com273-chintalapudi.pdf    <----- Algoritmen
                        // http://www.cl.cam.ac.uk/~ab818/StepDetectionSmartphones.pdf          <----- Opt.parametrar
                    }
                    break;
                case STATE_WALKING:
                    if (timeWindowExceeded(STD_WINDOW)) {
                        if (motionWas(LOST)) currentState = STATE_IDLE;
                        resetWindow();
                    }
                    break;
            }
        }
    }

    private void resetWindow() {
        stats.clear();
        lastWindow = System.currentTimeMillis();
    }

    private boolean timeWindowExceeded(long windowType) {
        return System.currentTimeMillis() - lastWindow >= windowType;
    }

    private boolean motionWas(int event) {
        boolean eventResult = false;
        switch (event) {
            case FOUND:
                eventResult = stats.getStandardDeviation() > STD_MIN_MOTION;
                break;
            case LOST:
                eventResult = stats.getStandardDeviation() < STD_MIN_MOTION;
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
}
