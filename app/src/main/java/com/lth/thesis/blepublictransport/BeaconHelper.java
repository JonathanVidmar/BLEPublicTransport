package com.lth.thesis.blepublictransport;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.HashMap;

/**
 * Created by Jacob Arvidsson on 12/02/16.
 */
public class BeaconHelper {
    private final static String eddystoneLayout = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    private Identifier namespace = Identifier.parse("0xf7826da6bc5b71e0893e");

    Region region = new Region("BLEPublicTransport", namespace, null, null);

    private HashMap<String, String> beaconList = new HashMap<String, String>();

    public BeaconHelper(){
        beaconList.put("0x526270373372", "Spår 1");
        beaconList.put("0x41774c564931", "Spår 2");
        beaconList.put("0x4e316a736752", "Spår 3");
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
            return String.valueOf(Math.round(distance));
        }
    }


}
