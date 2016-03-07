package com.lth.thesis.blepublictransport.Beacons;

import org.altbeacon.beacon.Beacon;

/**
 * Beacon class to hold values specific to our use cases
 *
 * @author Jonathan Vidmar
 * @version 1.0
 */
public class PublicTransportBeacon {

    private String simpleName;
    private Integer image;
    private BeaconStatistics stats;
    private boolean inProximity;

    public PublicTransportBeacon(String simpleName, Integer image){
        this.simpleName = simpleName;
        this.image = image;
        this.stats = new BeaconStatistics();
        this.inProximity = false;
    }

    public String getName() {
        return simpleName;
    }

    public double getDistance() {
        return stats.getDistance();
    }

    public void updateDistance(Beacon b, double movementState, double txPower){
        stats.updateDistance(b, movementState, txPower);
    }

    public Integer getImage() {
        return image;
    }

    public boolean inProximity(){
        return inProximity;
    }

    public void updateProximity(boolean prox){
        inProximity = prox;
    }
}
