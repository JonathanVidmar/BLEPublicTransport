package com.lth.thesis.blepublictransport.Utils;

import java.util.HashMap;

/**
 * Created by jacobarvidsson on 17/03/16.
 */
public class TicketHelper {
    private HashMap<String, Destination> destinationsMap = new HashMap<>();

    public TicketHelper(){
        destinationsMap.put("Lund central", new Destination("Lund central", "LU", "", "Pågatåg", "01"));
        destinationsMap.put("Malmö central", new Destination("Malmö central", "MLM", "", "Pågatåg", "12 B"));
        destinationsMap.put("Köpenhamn central", new Destination("Köpenhamn central", "CPH", "", "Öresundståg", "05"));
    }
}
