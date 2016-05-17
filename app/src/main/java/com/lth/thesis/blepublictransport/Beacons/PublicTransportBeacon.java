package com.lth.thesis.blepublictransport.Beacons;

import com.lth.thesis.blepublictransport.Models.AbstractBeaconPlace;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

/**
 * Beacon class to hold values specific to our use cases
 *
 * @author Jonathan Vidmar
 * @version 1.0
 */
public class PublicTransportBeacon {
    private Identifier id;
    private int type;
    private String simpleName;
    private Integer image;
    private BeaconStatistics stats;
    private boolean inProximity;
    private AbstractBeaconPlace beacon;

    public PublicTransportBeacon(Identifier id, int type, AbstractBeaconPlace beacon, String simpleName, Integer image){
        this.id = id;
        this.type = type;
        this.simpleName = simpleName;
        this.image = image;
        this.stats = new BeaconStatistics();
        this.inProximity = false;
        this.beacon = beacon;
    }

    public int getType() { return type; }

    public String getName() { return simpleName; }

    public Identifier getID() {
        return id;
    }

    public Integer getImage() {
        return image;
    }

    /* Kalman filter*/
    public double getDistance() { return stats.getDistance(); }

    public void updateDistance(Beacon b, double movementState, double txPower, double processNoise){
        stats.updateDistance(b, movementState, txPower, processNoise);
    }

    public boolean inProximity(){
        return inProximity;
    }

    public void updateProximity(boolean prox){
        inProximity = prox;
    }
}
