package com.lth.thesis.blepublictransport.Models;

import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;

/**
 * Created by jacobarvidsson on 11/04/16.
 */
public class Train extends AbstractBeaconPlace{
    public String nextArrival; // Kan göras om till date
    public PublicTransportBeacon track;
    public String destination;

    public Train(String name, String destination, PublicTransportBeacon track, String nextArrival){
        this.name = name;
        this.destination = destination;
        this.track = track;
        this.nextArrival = nextArrival;
    }
}
