package com.lth.thesis.blepublictransport.Main;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.lth.thesis.blepublictransport.Beacons.BeaconCommunicator;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.BluetoothClient.BluetoothClient;
import com.lth.thesis.blepublictransport.Beacons.Constants;
import com.lth.thesis.blepublictransport.Beacons.NotificationHandler;
import com.lth.thesis.blepublictransport.Beacons.WalkDetection;

import org.altbeacon.beacon.*;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class BLEPublicTransport extends Application implements BootstrapNotifier, BeaconConsumer {
    // Private attributes
    private static final String DEBUG_TAG = "BLEPublicTransport";
    private BeaconCommunicator beaconCommunicator;
    private BluetoothClient bluetoothClient;
    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;
    private WalkDetection walkDetection;

    // Private states
    private boolean notCurrentlyRanging = true;

    // Public attributes
    public BeaconHelper beaconHelper;
    public NotificationHandler notificationHandler;

    // Public states
    public int connectionState = -1;
    public boolean active = true;

    public void onCreate() {
        super.onCreate();

        bluetoothClient = new BluetoothClient(this);
        walkDetection = new WalkDetection(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconHelper.eddystoneLayout));

        try {
            beaconManager.setForegroundScanPeriod(120l);
            beaconManager.setForegroundBetweenScanPeriod(0l);
            beaconManager.setBackgroundScanPeriod(120l);
            beaconManager.setBackgroundBetweenScanPeriod(0l);

            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        regionBootstrap = new RegionBootstrap(this, BeaconHelper.regions);
        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        notificationHandler = new NotificationHandler(this);
        beaconCommunicator = new BeaconCommunicator();
        beaconHelper = new BeaconHelper();
    }

    public void stop(){
        try {
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region1);
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region2);
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region3);
            beaconManager.stopMonitoringBeaconsInRegion(BeaconHelper.region1);
            beaconManager.stopMonitoringBeaconsInRegion(BeaconHelper.region2);
            beaconManager.stopMonitoringBeaconsInRegion(BeaconHelper.region3);
            regionBootstrap.disable();
            BluetoothAdapter.getDefaultAdapter().stopLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                }
            });
        }catch (RemoteException e){
            e.printStackTrace();
            Log.d(DEBUG_TAG, "Ranging or monitoring has failed to stop.");
        }
    }

    @Override
    public void didEnterRegion(Region arg0) {
        if (!beaconHelper.currentlyInMainRegion()) notificationHandler.create();
        beaconHelper.foundRegionInstance(arg0.getId2());
        if (active) {
            // Currently on a fragment
            beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.ENTERED_REGION, null));
            if (notCurrentlyRanging) {
                startRangingBeaconsInRegion();
            }

        } //else {
        // If we have already seen beacons before, but a fragment is not in
        // the foreground, we send a notification to the user on subsequent detections.
        //}
    }


    @Override
    public void didExitRegion(Region arg0) {
        beaconHelper.lostRegionInstance(arg0.getId2());

        if (!beaconHelper.currentlyInMainRegion()) {
            // removes notification of entering a region if it exists
            notificationHandler.cancel();
            // update mainActivity that region is no more
            beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.EXITED_REGION, null));
            // cancel ranging if active
            stopRangingBeacons();
            Log.d(DEBUG_TAG, "did exit region: " + arg0.getId1() + ", " + arg0.getId2() + ", " + arg0.getId3());
        }
    }

    private void stopRangingBeacons() {
        try {
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region1);
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region2);
            beaconManager.stopRangingBeaconsInRegion(BeaconHelper.region3);
            notCurrentlyRanging = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // No use at the moment
    }

    @Override
    public void onBeaconServiceConnect() {
        startMonitoringBeaconsInRegion();
    }

    public void startMonitoringBeaconsInRegion() {
        try {
            beaconManager.startMonitoringBeaconsInRegion(BeaconHelper.region1);
            beaconManager.startMonitoringBeaconsInRegion(BeaconHelper.region2);
            beaconManager.startMonitoringBeaconsInRegion(BeaconHelper.region3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startRangingBeaconsInRegion() {
        notCurrentlyRanging = false;
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                beaconHelper.updateBeaconDistances(beacons, walkDetection.getState());
                beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.RANGED_BEACONS, beacons));
                for (Beacon b : beacons) {
                    if (b.getId2().toString().equals(BeaconHelper.region2.getId2().toString())) {
                        bluetoothClient.updateClient(beaconHelper.getDistance(b));
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region1);
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region2);
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public BeaconCommunicator getBeaconCommunicator() {
        return beaconCommunicator;
    }

    public boolean hasValidTicket() {
        boolean hasValidTicket = false;
        SharedPreferences ticket = getSharedPreferences(Constants.TICKET_PREFERENCES, 0);
        String validUntil = ticket.getString(Constants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        try {
            Date date = formatter.parse(validUntil);
            Date now = new Date();
            long timeLeft = date.getTime() - now.getTime();
            hasValidTicket = (timeLeft >= 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hasValidTicket;
    }
}

