package com.lth.thesis.blepublictransport.Beacons;

import com.lth.thesis.blepublictransport.Utils.KalmanFilter;
import com.lth.thesis.blepublictransport.Utils.MeasurementUtil;
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
    private double processNoise;
    public Map<Identifier, PublicTransportBeacon> beaconList = new HashMap<>();
    private boolean selfCorrection;
    private long lastSelfCorrectingBeaconUpdate = 0;
    public MeasurementUtil measurementUtil;



    public BeaconHelper(boolean selfCorrection) {
        this.selfCorrection = selfCorrection;
        beaconList.put(INSTANCE_1, BEACON1);
        beaconList.put(INSTANCE_2, BEACON2);
        beaconList.put(INSTANCE_3, BEACON3);
        measurementUtil = new MeasurementUtil();
    }

    public boolean isBeaconAtStation(Identifier instance){
        return beaconList.get(instance).getType() == BEACON_TYPE_STATION;
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
    public String getDistanceText(PublicTransportBeacon beacon) {
        double distance = beacon.getDistance();
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(distance) + " meters";
    }

    public double getDistance(Beacon beacon) {
        return beacon.getDistance();
    }

    public void updateBeaconDistances(Collection<Beacon> beacons, double movementState) {
        for (Beacon b : beacons) {
            checkSelfCorrection(b);
            beaconList.get(b.getId2()).updateDistance(b, movementState, txPower, processNoise);
            measurementUtil.update(b, this);
        }

    }

    public int getImage(Beacon b) {
        return beaconList.get(b.getId2()).getImage();
    }

    private void checkSelfCorrection(Beacon b) {
        // Reset to preset after 10 seconds without update
        boolean selfCorrectingBeaconTimedOut = System.currentTimeMillis() - lastSelfCorrectingBeaconUpdate < 10000;
        lastSelfCorrectingBeaconUpdate = System.currentTimeMillis();

        if (!selfCorrection || selfCorrectingBeaconTimedOut) txPower = b.getTxPower();
    }

    public void updateSelfCorrection(boolean update) {
        selfCorrection = update;
    }

    public void updateProcessNoise(int progress) {
        processNoise = KalmanFilter.getCalculatedNoise(progress);
    }
    public double getProcessNoise() {
        return processNoise;
    }
}
