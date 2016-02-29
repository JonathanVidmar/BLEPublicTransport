package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.altbeacon.beacon.*;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observer;

public class BLEPublicTransport extends Application implements BootstrapNotifier, BeaconConsumer {
    private static final String TAG = "BLEPublicTransport";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    public boolean active = true;
    private BeaconManager beaconManager;
    private BeaconCommunicator beaconCommunicator;
    private boolean notCurrentlyRanging = true;
    public BeaconHelper beaconHelper;
    public NotificationHandler notificationHandler;
    private WalkDetection wd;


    public void onCreate() {
        super.onCreate();

        wd = new WalkDetection(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconHelper.eddystoneLayout));

        try {
            beaconManager.setForegroundScanPeriod(120l); // 20 mS
            beaconManager.setForegroundBetweenScanPeriod(0l); // 0ms
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        regionBootstrap = new RegionBootstrap(this, BeaconHelper.regions);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
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
            Log.i("region", "did exit region: " + arg0.getId1() + ", " + arg0.getId2() + ", " + arg0.getId3());
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
                beaconHelper.updateBeaconDistances(beacons, wd.getState());
                beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.RANGED_BEACONS, beacons));
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region1);
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region2);
            beaconManager.startRangingBeaconsInRegion(BeaconHelper.region3);
        } catch (RemoteException e) {
        }
    }

    public BeaconCommunicator getBeaconCommunicator() {
        return beaconCommunicator;
    }

    public boolean hasValidTicket() {
        boolean hasValidTicket = false;
        SharedPreferences ticket = getSharedPreferences(Constants.TICKET_PREFERENCES, 0);
        String validUntil = ticket.getString(Constants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = formatter.parse(validUntil);
            Date now = new Date();
            long timeLeft = date.getTime() - now.getTime();
            hasValidTicket = (timeLeft < 0) ? false : true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hasValidTicket;
    }
}

