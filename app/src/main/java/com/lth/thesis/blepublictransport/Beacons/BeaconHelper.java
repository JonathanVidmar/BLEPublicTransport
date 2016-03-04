package com.lth.thesis.blepublictransport.Beacons;

import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * A helper class to deal with Beacons
 *
 * @author Jacob Arvidsson & Jonathan Vidmar
 * @version 1.2
 */
public class BeaconHelper {
    public final static String eddystoneLayout = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    private static Identifier namespace = Identifier.parse("0xf7826da6bc5b71e0893e");
    private static final Identifier instance1 = Identifier.parse("0x41774c564931");
    private static final Identifier instance2 = Identifier.parse("0x4e316a736752");
    private static final Identifier instance3 = Identifier.parse("0x526270373372");
    public static final Region region1 = new Region("BLEPublicTransport A", namespace, instance1, null);
    public static final Region region2 = new Region("BLEPublicTransport B", namespace, instance2, null);
    public static final Region region3 = new Region("BLEPublicTransport C", namespace, instance3, null);
    public static final List<Region> regions = Arrays.asList(region1, region2, region3);
    public double txPower = -59;
    public HashMap<String, Integer> images = new HashMap<>();

    private Map<String, BeaconStatHelper> beaconStatList = new HashMap<>();
    public HashMap<Identifier, Boolean> currentlyInBeaconRegionProximity = new HashMap<>();
    private HashMap<String, String> beaconList = new HashMap<>();

    public void lostRegionInstance(Identifier instance) {
        currentlyInBeaconRegionProximity.put(instance, false);
    }

    public void foundRegionInstance(Identifier instance) {
        currentlyInBeaconRegionProximity.put(instance, true);
    }

    public boolean currentlyInMainRegion() {
        return currentlyInBeaconRegionProximity.get(instance1) ||
                currentlyInBeaconRegionProximity.get(instance2) ||
                currentlyInBeaconRegionProximity.get(instance3);
    }


    public BeaconHelper() {

        beaconList.put("0x41774c564931", "Kundservice");
        beaconList.put("0x4e316a736752", "Spår 2");
        beaconList.put("0x526270373372", "Espresso House");

        images.put("0x41774c564931", R.drawable.icon_information);
        images.put("0x4e316a736752", R.drawable.icon_tracks);
        images.put("0x526270373372", R.drawable.icon_coffe);


        beaconStatList.put("0x41774c564931", new BeaconStatHelper());
        beaconStatList.put("0x4e316a736752", new BeaconStatHelper());
        beaconStatList.put("0x526270373372", new BeaconStatHelper());

        currentlyInBeaconRegionProximity.put(instance1, false);
        currentlyInBeaconRegionProximity.put(instance2, false);
        currentlyInBeaconRegionProximity.put(instance3, false);
    }

    /**
     * This method is a getter for the name attribute of a beacon
     *
     * @param instanceId, region.getId2()
     * @return the name of the beacon.
     */
    public String getBeaconName(Identifier instanceId) {
        return beaconList.get(instanceId.toString());
    }

    /**
     * This method returns the distance in a formatted string.
     *
     * @param beacon, the beacon of which distance to is to be returned
     * @return the text to be displayed.
     */
    public String getDistanceText(Beacon beacon) {
        double distance = getDistance(beacon);
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return String.valueOf(df.format(distance)) + " meters";
    }

    public double getDistance(Beacon beacon) {
        return beaconStatList.get(beacon.getId2().toString()).getDistance();
    }

    public void updateBeaconDistances(Collection<Beacon> beacons, double movementState) {
        /*for (Beacon b :
                beacons) {
            if (b.getId2().toString().equals(instance3.toString())) {
                txPower = b.getRssi();
            }
        }*/
        for (Beacon b : beacons) {
            beaconStatList.get(b.getId2().toString()).updateDistance(b, movementState, txPower);
        }
    }

}
