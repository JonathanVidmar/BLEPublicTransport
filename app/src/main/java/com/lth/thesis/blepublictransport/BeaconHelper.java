package com.lth.thesis.blepublictransport;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jacob Arvidsson on 12/02/16.
 */
public class BeaconHelper {
    private final static String eddystoneLayout = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    private static Identifier namespace = Identifier.parse("0xf7826da6bc5b71e0893e");
    private static final Identifier instance1 = Identifier.parse("0x41774c564931");
    private static final Identifier instance2 = Identifier.parse("0x4e316a736752");
    private static final Identifier instance3 = Identifier.parse("0x526270373372");
    public static final Region region1 = new Region("BLEPublicTransport A", namespace, instance1, null);
    public static final Region region2 = new Region("BLEPublicTransport B", namespace, instance2, null);
    public static final Region region3 = new Region("BLEPublicTransport C", namespace, instance3, null);
    public static final List<Region> regions = Arrays.asList(region1, region2, region3);

    public HashMap<Identifier, Boolean> currentlyInBeaconRegionProximity = new HashMap<>();

    public void lostRegionInstance(Identifier instance){
        currentlyInBeaconRegionProximity.put(instance, false);
    }

    public void foundRegionInstance(Identifier instance){
        currentlyInBeaconRegionProximity.put(instance, true);
    }

    public boolean currentlyInMainRegion() {
        return currentlyInBeaconRegionProximity.get(instance1) ||
                currentlyInBeaconRegionProximity.get(instance2) ||
                currentlyInBeaconRegionProximity.get(instance3);
    }

    private HashMap<String, String> beaconList = new HashMap<>();

    public BeaconHelper(){
        beaconList.put("0x41774c564931", "Spår 1");
        beaconList.put("0x4e316a736752", "Spår 2");
        beaconList.put("0x526270373372", "Spår 3");

        currentlyInBeaconRegionProximity.put(instance1, false);
        currentlyInBeaconRegionProximity.put(instance2, false);
        currentlyInBeaconRegionProximity.put(instance3, false);
    }

    /**
     * This method is a getter for the name attribute of a beacon
     * @param instanceId, region.getId2()
     * @return the name of the beacon.
     */
    public String getBeaconName(Identifier instanceId){
        return beaconList.get(instanceId.toString());
    }

    /**
     * This method converts the distance to a string to be displayed.
     * @param distance, the actual distance to the beacon
     * @return the text to be displayed.
     */
    public String getDistanceText(Double distance){
        if(distance < 1){
            return "Less then 1 meter";
        } else {
            return String.valueOf(Math.round(distance)) + " meters";
        }
    }
}
