package com.lth.thesis.blepublictransport.Beacons;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

/**
 * Created by jacobarvidsson on 12/02/16.
 */
public class BeaconPacket {
    public int type;
    public Collection<Beacon> beacons;

    public final static int ENTERED_REGION = 0;
    public final static int EXITED_REGION = 1;
    public final static int RANGED_BEACONS = 2;
    public final static int CONNECTION_STATE = 3;


    public BeaconPacket(int type, Collection<Beacon> beacons){
        this.type = type;
        this.beacons = beacons;
    }
}
