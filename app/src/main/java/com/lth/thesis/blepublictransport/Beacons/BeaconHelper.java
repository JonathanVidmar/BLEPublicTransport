package com.lth.thesis.blepublictransport.Beacons;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.lth.thesis.blepublictransport.Config.BeaconConstants.*;

/**
 * A helper class to deal with Beacons
 *
 * @author Jacob Arvidsson & Jonathan Vidmar
 * @version 1.3
 */
public class BeaconHelper {

    public double txPower = -59;
    private Map<Identifier, PublicTransportBeacon> beaconList = new HashMap<>();



    public BeaconHelper() {
        beaconList.put(INSTANCE_1, new PublicTransportBeacon(SIMPLE_NAME_1, THUMB_IMAGE_1));
        beaconList.put(INSTANCE_2, new PublicTransportBeacon(SIMPLE_NAME_2, THUMB_IMAGE_2));
        beaconList.put(INSTANCE_3, new PublicTransportBeacon(SIMPLE_NAME_3, THUMB_IMAGE_3));
    }

    public void lostRegionInstance(Identifier instance) {
        beaconList.get(instance).updateProximity(false);
    }

    public void foundRegionInstance(Identifier instance) {
        beaconList.get(instance).updateProximity(true);
    }

    public boolean currentlyInMainRegion() {
        boolean inMainRegion = false;
        for (Region region :
                REGIONS) {
            inMainRegion = inMainRegion || beaconList.get(region.getId2()).inProximity();
        }
        return inMainRegion;
    }

    /**
     * This method is a getter for the name attribute of a beacon
     *
     * @param b, region.getId2()
     * @return the name of the beacon.
     */
    public String getBeaconName(Beacon b) {
        return beaconList.get(b.getId2()).getName();
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
        return beaconList.get(beacon.getId2()).getDistance();
    }

    public void updateBeaconDistances(Collection<Beacon> beacons, double movementState) {
        for (Beacon b : beacons) {
            beaconList.get(b.getId2()).updateDistance(b, movementState, txPower);
        }
    }

    public int getImage(Beacon b) {
        return beaconList.get(b.getId2()).getImage();
    }
}