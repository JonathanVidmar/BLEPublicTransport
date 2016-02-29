package com.lth.thesis.blepublictransport;

import android.util.Log;
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
    private DescriptiveStatistics prevXDelta;

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

        prevXDelta = new DescriptiveStatistics();
        prevXDelta.setWindowSize(5);
        prevXDelta.addValue(0.0);
        prevXDelta.addValue(0.0);
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
            cov = (1 / C) * Q * (1 / C);
        } else {

             R = u == 1 ? 10 : 0.001;

            // Compute prediction
            double predX = (A * x) + (B * u);
            double predCov = ((A * cov) * A) + R;

            // Kalman gain
            double K = predCov * C * (1 / ((C * predCov * C) + Q));

            // Correction

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
}

/**
 * Simple bulider class for 1-dimensional kalman filter
 */
class KFilterBuilder {
    private double R = 1;
    private double Q = 1;
    private double A = 1;
    private double B = 0;
    private double C = 1;

    KFilterBuilder R(double R) {
        this.R = R;
        return this;
    }

    KFilterBuilder Q(double Q) {
        this.Q = Q;
        return this;
    }

    KFilterBuilder A(double A) {
        this.A = A;
        return this;
    }

    KFilterBuilder B(double B) {
        this.B = B;
        return this;
    }

    KFilterBuilder C(double C) {
        this.C = C;
        return this;
    }

    KalmanFilter build() {
        return new KalmanFilter(R, Q, A, B, C);
    }
}


