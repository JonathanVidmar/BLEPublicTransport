package com.lth.thesis.blepublictransport.Models;

import java.util.ArrayList;
/**
 * Created by jacobarvidsson on 17/03/16.
 */
public class Station extends AbstractBeaconPlace{
    public String image;
    public String abbreviation;
    public String transportType;
    public String track;
    public ArrayList<Train> arrivalsList;


    public Station(String name, String abbreviation, String image, String transportType, String track){
        this.name = name;
        this.abbreviation = abbreviation;
        this.image = image;
        this.transportType = transportType;
        this.track = track;
    }
}
