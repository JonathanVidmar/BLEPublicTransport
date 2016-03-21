package com.lth.thesis.blepublictransport.Utils;

import android.util.Log;
import com.lth.thesis.blepublictransport.Config.SettingConstants;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Originally written in JS by Wouter Bulten 2015
 * Rewritten to Java by Jonathan Vidmar 2016
 * Copyright 2015 Wouter Bulten
 * GNU LESSER GENERAL PUBLIC LICENSE v3
 */
public class KalmanFilter {

    private double R;
    private double Q;
    private double A;
    private double B;
    private double C;
    private double cov;
    private double x;   // estimated signal without noise
    private double x1;
    private double x2;

    /**
     * Create 1-dimensional kalman filter
     *
     * @param R Process noise
     * @param Q Measurement noise
     * @param A State vector
     * @param B Control vector
     * @param C Measurement vector
     */
    public KalmanFilter(double R, double Q, double A, double B, double C) {

        this.R = R;
        this.Q = Q;
        this.A = A;
        this.B = B;
        this.C = C;

        cov = Double.NaN;
        x = Double.NaN;
    }


    public double filter(double z) {
        return filter(z, 0);
    }

    /**
     * Filter a new value
     *
     * @param z Measurement
     * @param u Control
     * @return x
     */
    public double filter(double z, double u) {

        if (Double.isNaN(x)) {
            x = (1 / C) * z;
            x1 = x;
            x2 = x1;
            cov = (1 / C) * Q * (1 / C);
        } else {

             R = u == 1 ? R : 0.01 * R;

            // Compute prediction
            double predX = (A * x) + (x - x1)/2 /*(B * u) */;
            double predCov = ((A * cov) * A) + R;

            // Kalman gain
            double K = predCov * C * (1 / ((C * predCov * C) + Q));

            // Correction
            x1 = x;
            x = predX + K * (z - (C * predX));
            cov = predCov - (K * C * predCov);
        }

        return x;
    }

    /**
     * Return the last filtered measurement
     *
     * @return x Estimated signal without noise
     */
    public double lastMeasurement() {
        return x;
    }

    /**
     * Set measurement noise Q
     *
     * @param noise Measurement noise
     */
    public void setMeasurementNoise(double noise) {
        Q = noise;
    }

    /**
     * Set the process noise R
     *
     * @param noise Process noise
     */
    public void setProcessNoise(double noise) {
        R = noise;
    }

    public static double getCalculatedNoise(int p) {
        double percent = (double) p / 100.0;
        return SettingConstants.KALMAN_NOISE_MIN +  (SettingConstants.KALMAN_NOISE_MAX - SettingConstants.KALMAN_NOISE_MIN) * percent;
    }
}


