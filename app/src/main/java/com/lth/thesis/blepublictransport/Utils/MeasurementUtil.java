package com.lth.thesis.blepublictransport.Utils;

import android.os.Environment;
import android.util.Log;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Config.BeaconConstants;
import org.altbeacon.beacon.Beacon;

import java.io.*;

/**
 * Utility class for performing measurements on a test beacon
 *
 * @author Jonathan Vidmar
 * @version 1.0
 */
public class MeasurementUtil {
    private double lastDistance;
    private double lastRawDistance;
    private double lastDistanceWOSC;
    private double lastAltBeaconDistance;
    private double processNoise;

    public MeasurementUtil() {}

    public void update(Beacon b, BeaconHelper bh) {
        if (b.getId2().equals(BeaconConstants.TEST_BEACON_INSTANCE)) {
            lastDistance = BeaconConstants.BEACON_LIST.get(b.getId2()).getDistance();
            Log.d("testy", "" + lastDistance);
            lastRawDistance = BeaconConstants.BEACON_LIST.get(b.getId2()).getRawDistance();
            lastDistanceWOSC = BeaconConstants.BEACON_LIST.get(b.getId2()).getDistanceWOSC();
            lastAltBeaconDistance = b.getDistance();
            processNoise = bh.getProcessNoise();
        }
    }

    public double getMeasurement() {
        return lastDistance;
    }

    public double getAltBeaconMeasurement(){
        return lastAltBeaconDistance;
    }

    public double getRawMeasurement(){
        return lastRawDistance;
    }

    public double getWOSCMeasurement(){
        return lastDistanceWOSC;
    }


    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.d("sure", "not writeable");
        return false;
    }

    public void export(String data){
        if (isExternalStorageWritable()) {
            String filename = "/Documents/" + processNoise + "_" + System.currentTimeMillis() + ".txt";
            File root= Environment.getExternalStorageDirectory();
            File textfile = new File(root, filename);

            try {
                textfile.createNewFile();
                FileOutputStream f = new FileOutputStream(textfile);
                PrintWriter out = new PrintWriter(f);
                out.print(data);
                out.flush();
                out.close();
                f.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
