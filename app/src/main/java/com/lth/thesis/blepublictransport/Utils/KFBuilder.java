package com.lth.thesis.blepublictransport.Utils;
/**
 * Simple builder class for 1-dimensional Kalman filter with predefined
 */
public class KFBuilder {
    private double R = 1;
    private double Q = 1;
    private double A = 1;
    private double B = 0;
    private double C = 1;

    public KFBuilder R(double R) {
        this.R = R;
        return this;
    }

    public KFBuilder Q(double Q) {
        this.Q = Q;
        return this;
    }

    public KFBuilder A(double A) {
        this.A = A;
        return this;
    }

    public KFBuilder B(double B) {
        this.B = B;
        return this;
    }

    public KFBuilder C(double C) {
        this.C = C;
        return this;
    }

    public KalmanFilter build() {
        return new KalmanFilter(R, Q, A, B, C);
    }
}
