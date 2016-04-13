package com.lth.thesis.blepublictransport.Models;

/**
 * Created by jacobarvidsson on 11/04/16.
 */
public class Train extends AbstractBeaconPlace{
    public String nextArrival; // Kan göras om till date
    public String track;

    public Train(String name, String track, String nextArrival){
        this.name = name;
        this.track = track;
        this.nextArrival = nextArrival;
    }
}
