package com.lth.thesis.blepublictransport.Config;

import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Models.*;
import com.lth.thesis.blepublictransport.R;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing constants and configurations for the beacon objects.
 *
 * @author Jonathan Vidmar & Jacob Arvidsson
 * @version 1.0
 */

public class BeaconConstants {
    public final static String EDDYSTONE_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static Identifier NAMESPACE = Identifier.parse("0xf7826da6bc5b71e0893e");

    public static final Identifier INSTANCE_1 = Identifier.parse("0x41774c564931");
    public static final Identifier INSTANCE_2 = Identifier.parse("0x4e316a736752");
    public static final Identifier INSTANCE_3 = Identifier.parse("0x526270373372");
    public static final Identifier TEST_BEACON_INSTANCE = INSTANCE_1;

    public static final Region REGION_1 = new Region("BLEPublicTransport A", NAMESPACE, INSTANCE_1, null);
    public static final Region REGION_2 = new Region("BLEPublicTransport B", NAMESPACE, INSTANCE_2, null);
    public static final Region REGION_3 = new Region("BLEPublicTransport C", NAMESPACE, INSTANCE_3, null);
    public static final List<Region> REGIONS = Arrays.asList(REGION_1, REGION_2, REGION_3);

    // Beacon types
    public static final int BEACON_TYPE_STATION = 0;
    public static final int BEACON_TYPE_VEHICLE = 1;

    // Station
    public static final Station HOME_STATION = new Station("Lund central", "LU", "", "Pågatåg", "01");
    public static final Station MLM_STATION = new Station("Malmö central", "MLM", "img_mlm", "Pågatåg", "12 B");
    public static final Station HSB_STATION = new Station("Helsingborg central", "HSB", "img_hsb", "Öresundståg", "06");
    public static final Station YSD_STATION = new Station("Ystad station", "YSD", "img_ysd", "Pågatåg", "03");
    public static final Station CPH_STATION = new Station("Köpenhamn central", "CPH", "img_cph", "Öresundståg", "05");

    // Beacons
    public static final PublicTransportBeacon BEACON1 = new PublicTransportBeacon(INSTANCE_1, BEACON_TYPE_STATION, HOME_STATION,  "Kundservice", R.drawable.icon_information);
    public static final PublicTransportBeacon BEACON2 = new PublicTransportBeacon(INSTANCE_2, BEACON_TYPE_STATION, HOME_STATION, "Buss 2",  R.drawable.icon_tracks);
    public static final PublicTransportBeacon BEACON3 = new PublicTransportBeacon(INSTANCE_3, BEACON_TYPE_STATION, HOME_STATION,  "Espresso House", R.drawable.icon_coffe);


    public static final Map<String, Station> DESTINATION_MAP;
    static {
        Map<String, Station> map = new HashMap<>();
        map.put("Helsingborg central", HSB_STATION);
        map.put("Ystad station", YSD_STATION);
        map.put("Malmö central", MLM_STATION);
        map.put("Köpenhamn central", CPH_STATION);
        DESTINATION_MAP = Collections.unmodifiableMap(map);
    }

    public static final List<Train> ARRIVALS_LIST;
    static {
        List<Train> list = new ArrayList<>();
        list.add(new Train("Pågatåg", "2", "12.01"));
        list.add(new Train("Oresundståg", "1", "12.22"));
        list.add(new Train("Oresundståg", "3", "12.37"));
        list.add(new Train("SJ", "6", "13.02"));
        list.add(new Train("Pågatåg", "16", "13.24"));
        list.add(new Train("Öresundståg", "2", "13.43"));
        ARRIVALS_LIST = Collections.unmodifiableList(list);
    }
}
