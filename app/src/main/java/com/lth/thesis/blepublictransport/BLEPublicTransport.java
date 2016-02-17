package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.altbeacon.beacon.*;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Collection;
import java.util.List;
import java.util.Observer;

public class BLEPublicTransport extends Application implements BootstrapNotifier, BeaconConsumer {
    private static final String TAG = "BLEPublicTransport";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    public boolean active = true;
    private BeaconManager beaconManager;
    private BeaconCommunicator beaconCommunicator;
    private boolean notCurrentlyRanging = true;
    private BeaconHelper beaconHelper;
    public NotificationHandler notificationHandler;


    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));

        // Wakes up application
        //Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, BeaconHelper.regions);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        notificationHandler = new NotificationHandler(this);
        beaconCommunicator = new BeaconCommunicator();
        beaconHelper = new BeaconHelper();
    }

    @Override
    public void didEnterRegion(Region arg0) {
        if (!beaconHelper.currentlyInMainRegion()) notificationHandler.create();
        beaconHelper.foundRegionInstance(arg0.getId2());

        // Log.i("region", "did enter region: " + arg0.getId1() + ", " + arg0.getId2() + ", " + arg0.getId3());
        if (active) {
            // Currently on a fragment
            // Send data about beacons{
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
                beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.RANGED_BEACONS, beacons));
                // Log.i("region", "did range region: " + region.getId1() + ", " + region.getId2() + ", " + region.getId3());
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
}

