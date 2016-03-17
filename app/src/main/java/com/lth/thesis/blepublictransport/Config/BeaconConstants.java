package com.lth.thesis.blepublictransport.Config;

import com.lth.thesis.blepublictransport.R;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.List;

/**
 * A class containing constants and configurations for the beacon objects.
 *
 * @author Jonathan Vidmar
 * @version 1.0
 */

public class BeaconConstants {
    public final static String EDDYSTONE_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static Identifier NAMESPACE = Identifier.parse("0xf7826da6bc5b71e0893e");
    public static final Identifier INSTANCE_1 = Identifier.parse("0x41774c564931");
    public static final Identifier INSTANCE_2 = Identifier.parse("0x4e316a736752");
    public static final Identifier INSTANCE_3 = Identifier.parse("0x526270373372");
    public static final Identifier TEST_BEACON_INSTANCE = INSTANCE_1;
    public static final String SIMPLE_NAME_1 = "Kundservice";
    public static final String SIMPLE_NAME_2 = "Spår 2";
    public static final String SIMPLE_NAME_3 = "Espresso House";
    public static final Integer THUMB_IMAGE_1 = R.drawable.icon_information;
    public static final Integer THUMB_IMAGE_2 = R.drawable.icon_tracks;
    public static final Integer THUMB_IMAGE_3 = R.drawable.icon_coffe;
    public static final Region REGION_1 = new Region("BLEPublicTransport A", NAMESPACE, INSTANCE_1, null);
    public static final Region REGION_2 = new Region("BLEPublicTransport B", NAMESPACE, INSTANCE_2, null);
    public static final Region REGION_3 = new Region("BLEPublicTransport C", NAMESPACE, INSTANCE_3, null);
    public static final List<Region> REGIONS = Arrays.asList(REGION_1, REGION_2, REGION_3);
}
