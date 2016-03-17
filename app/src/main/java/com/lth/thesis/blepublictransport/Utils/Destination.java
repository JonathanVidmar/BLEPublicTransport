package com.lth.thesis.blepublictransport.Utils;

/**
 * Created by jacobarvidsson on 17/03/16.
 */
public class Destination {
    public String name;
    public String image;
    public String abbreviation;
    public String transportType;
    public String track; // This could point to a beacon or something.

    public Destination(String name, String abbreviation, String image, String transportType, String track){
        this.name = name;
        this.abbreviation = abbreviation;
        this.image = image;
        this.transportType = transportType;
        this.track = track;
    }
}
