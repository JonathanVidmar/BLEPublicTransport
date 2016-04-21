package com.lth.thesis.blepublictransport.Beacons;

import android.content.SharedPreferences;

import com.lth.thesis.blepublictransport.Config.SettingConstants;
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
    private boolean selfCorrection;
    private long lastSelfCorrectingBeaconUpdate = 0;
    public MeasurementUtil measurementUtil;

    // Beacons
    public static final Map<Identifier, PublicTransportBeacon> BEACON_LIST;
    static {
        Map<Identifier, PublicTransportBeacon> map = new HashMap<>();
        map.put(INSTANCE_1, BEACON1);
        map.put(INSTANCE_2, BEACON2);
        map.put(INSTANCE_3, BEACON3);
        BEACON_LIST = Collections.unmodifiableMap(map);
    }

    public BeaconHelper(boolean selfCorrection) {
        this.selfCorrection = selfCorrection;
        measurementUtil = new MeasurementUtil();
    }

    /**
     * This method is a getter for the name attribute of a beacon
     *
     * @param b, region.getId2()
     * @return the name of the beacon.
     */
    public static String getBeaconName(Beacon b) {
        return BEACON_LIST.get(b.getId2()).getName();
    }

    /**
     * This method returns the distance in a formatted string.
     *
     * @param beacon, the beacon of which distance to is to be returned
     * @return the text to be displayed.
     */
    public static String getDistanceText(PublicTransportBeacon beacon) {
        double distance = beacon.getDistance();
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(distance) + " meters";
    }

    /**
     * For given beacon, returns if the type is a station
     * @param instance, id of the beacon
     * @return boolean, if it is of type station
     */
    public static boolean isBeaconAtStation(Identifier instance){
        return BEACON_LIST.get(instance).getType() == BEACON_TYPE_STATION;
    }

    public static void lostRegionInstance(Identifier instance) {
        BEACON_LIST.get(instance).updateProximity(false);
    }

    public static void foundRegionInstance(Identifier instance) {
        BEACON_LIST.get(instance).updateProximity(true);
    }

    public static boolean currentlyInMainRegion() {
        boolean inMainRegion = false;
        for (Region region : REGIONS) {
            inMainRegion = inMainRegion || BEACON_LIST.get(region.getId2()).inProximity();
        }
        return inMainRegion;
    }

    public void updateBeaconDistances(Collection<Beacon> beacons, double movementState) {
        for (Beacon b : beacons) {
            checkSelfCorrection(b);
            BEACON_LIST.get(b.getId2()).updateDistance(b, movementState, txPower, processNoise);
            measurementUtil.update(b, this);
        }

    }

    public static int getImage(Beacon b) {
        return BEACON_LIST.get(b.getId2()).getImage();
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
