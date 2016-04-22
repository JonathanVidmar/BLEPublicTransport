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
    public static final Identifier INSTANCE_4 = Identifier.parse("0x6432617a6b63");
    public static final Identifier INSTANCE_5 = Identifier.parse("0x746b4e4d674c");
    public static final Identifier INSTANCE_6 = Identifier.parse("0x674f7541744c");
    public static final Identifier INSTANCE_7 = Identifier.parse("0x6e486169426d");
    public static final Identifier INSTANCE_8 = Identifier.parse("0x456c32394745");
    public static final Identifier INSTANCE_9 = Identifier.parse("0x725671434f6c");

    public static final Identifier TEST_BEACON_INSTANCE = INSTANCE_1;

    public static final Region REGION_1 = new Region("BLEPublicTransport A", NAMESPACE, INSTANCE_1, null);
    public static final Region REGION_2 = new Region("BLEPublicTransport B", NAMESPACE, INSTANCE_2, null);
    public static final Region REGION_3 = new Region("BLEPublicTransport C", NAMESPACE, INSTANCE_3, null);
    public static final Region REGION_4 = new Region("BLEPublicTransport D", NAMESPACE, INSTANCE_4, null);
    public static final Region REGION_5 = new Region("BLEPublicTransport E", NAMESPACE, INSTANCE_5, null);
    public static final Region REGION_6 = new Region("BLEPublicTransport F", NAMESPACE, INSTANCE_6, null);
    public static final Region REGION_7 = new Region("BLEPublicTransport G", NAMESPACE, INSTANCE_7, null);
    public static final Region REGION_8 = new Region("BLEPublicTransport H", NAMESPACE, INSTANCE_8, null);
    public static final Region REGION_9 = new Region("BLEPublicTransport I", NAMESPACE, INSTANCE_9, null);
    public static final List<Region> REGIONS = Arrays.asList(REGION_1, REGION_2, REGION_3, REGION_4, REGION_5, REGION_6, REGION_7, REGION_8, REGION_9);

    // Beacon types
    public static final int BEACON_TYPE_STATION = 0;
    public static final int BEACON_TYPE_VEHICLE = 1;

    // Station
    public static final Station HOME_STATION = new Station("Lund central", "LU", "", "Pågatåg", "01");
    public static final Station MLM_STATION = new Station("Malmö central", "MLM", "img_mlm", "Pågatåg", "12 B");
    public static final Station HSB_STATION = new Station("Helsingborg central", "HSB", "img_hsb", "Øresundståg", "06");
    public static final Station YSD_STATION = new Station("Ystad station", "YSD", "img_ysd", "Pågatåg", "03");
    public static final Station CPH_STATION = new Station("Köpenhamn central", "CPH", "img_cph", "Øresundståg", "05");

    // Beacons
    public static final Map<Identifier, PublicTransportBeacon> BEACON_LIST;
    static {
        Map<Identifier, PublicTransportBeacon> map = new HashMap<>();
        map.put(INSTANCE_1, new PublicTransportBeacon(INSTANCE_1, BEACON_TYPE_STATION, HOME_STATION,  "Kundservice", R.drawable.icon_information));
        map.put(INSTANCE_2, new PublicTransportBeacon(INSTANCE_2, BEACON_TYPE_STATION, HOME_STATION, "Gate 2",  R.drawable.icon_tracks));
        map.put(INSTANCE_3, new PublicTransportBeacon(INSTANCE_3, BEACON_TYPE_STATION, HOME_STATION,  "Espresso House", R.drawable.icon_coffe));
        map.put(INSTANCE_4, new PublicTransportBeacon(INSTANCE_4, BEACON_TYPE_STATION, HOME_STATION,  "Track 2", R.drawable.icon_tracks));
        map.put(INSTANCE_5, new PublicTransportBeacon(INSTANCE_5, BEACON_TYPE_STATION, HOME_STATION,  "Track 1", R.drawable.icon_tracks));
        map.put(INSTANCE_6, new PublicTransportBeacon(INSTANCE_6, BEACON_TYPE_VEHICLE, HOME_STATION,  "Pågatåg - Ystad", R.drawable.icon_tracks));
        map.put(INSTANCE_7, new PublicTransportBeacon(INSTANCE_7, BEACON_TYPE_VEHICLE, HOME_STATION,  "Øresundståg - Köpenhamn", R.drawable.icon_tracks));
        map.put(INSTANCE_8, new PublicTransportBeacon(INSTANCE_8, BEACON_TYPE_VEHICLE, HOME_STATION,  "Pågatåg - Trellborg", R.drawable.icon_tracks));
        map.put(INSTANCE_9, new PublicTransportBeacon(INSTANCE_9, BEACON_TYPE_VEHICLE, HOME_STATION,  "Øresundståg - Helsingborg", R.drawable.icon_tracks));
        BEACON_LIST = Collections.unmodifiableMap(map);
    }

    public static final Map<String, Station> DESTINATION_MAP;
    static {
        Map<String, Station> map = new HashMap<>();
        map.put(HSB_STATION.name, HSB_STATION);
        map.put(YSD_STATION.name, YSD_STATION);
        map.put(MLM_STATION.name, MLM_STATION);
        map.put(CPH_STATION.name, CPH_STATION);
        DESTINATION_MAP = Collections.unmodifiableMap(map);
    }

    public static final List<Train> ARRIVALS_LIST;
    static {
        List<Train> list = new ArrayList<>();
        list.add(new Train("Pågatåg", "Ystad", BEACON_LIST.get(INSTANCE_4), "12.01"));
        list.add(new Train("Øresundståg", "Köpenhamn", BEACON_LIST.get(INSTANCE_5), "12.22"));
        list.add(new Train("Øresundståg", "Helsingborg", BEACON_LIST.get(INSTANCE_4), "12.37"));
        list.add(new Train("SJ", "Stockholm", BEACON_LIST.get(INSTANCE_5), "13.02"));
        list.add(new Train("Pågatåg", "Trelleborg", BEACON_LIST.get(INSTANCE_4), "13.24"));
        list.add(new Train("Øresundståg", "Göteborg", BEACON_LIST.get(INSTANCE_5), "13.43"));
        ARRIVALS_LIST = Collections.unmodifiableList(list);
    }
}
