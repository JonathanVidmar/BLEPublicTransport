package com.lth.thesis.blepublictransport.Utils;

import java.util.HashMap;

/**
 * Created by jacobarvidsson on 17/03/16.
 */
public class TicketHelper {
    public static final Station homeStation = new Station("Lund central", "LU", "", "Pågatåg", "01");
    public HashMap<String, Station> destinationsMap = new HashMap<>();

    public TicketHelper(){
        destinationsMap.put("Helsingborg central", new Station("Helsingborg central", "HSB", "img_hsb", "Öresundståg", "06"));
        destinationsMap.put("Ystad station", new Station("Ystad station", "YSD", "img_ysd", "Pågatåg", "03"));
        destinationsMap.put("Malmö central", new Station("Malmö central", "MLM", "img_mlm", "Pågatåg", "12 B"));
        destinationsMap.put("Köpenhamn central", new Station("Köpenhamn central", "CPH", "img_cph", "Öresundståg", "05"));
    }
}
