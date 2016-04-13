package com.lth.thesis.blepublictransport.Beacons;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

/**
 * A class describing a packet sent with the {@link BeaconCommunicator}
 *
 * @author Jacob Arvidsson & Jonathan Vidmar
 * @version 1.0
 */
public class BeaconPacket {
    public int type;
    public ArrayList<PublicTransportBeacon> beacons;

    public final static int ENTERED_REGION = 0;
    public final static int EXITED_REGION = 1;
    public final static int RANGED_BEACONS = 2;

    public BeaconPacket(int type, ArrayList<PublicTransportBeacon> beacons) {
        this.type = type;
        this.beacons = beacons;
    }
}
