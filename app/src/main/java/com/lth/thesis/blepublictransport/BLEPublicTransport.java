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
import java.util.Observer;

public class BLEPublicTransport extends Application implements BootstrapNotifier, BeaconConsumer {
    private static final String TAG = "BLEPublicTransport";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    public boolean active = true;
    private NotificationManager notificationManager;
    private BeaconManager beaconManager;
    private BeaconCommunicator beaconCommunicator;


    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));

        // Wakes up application
        //Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, BeaconHelper.region);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        beaconCommunicator = new BeaconCommunicator();
    }

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region: " + arg0);
        if (active) {
            // Currently on a fragment
            // Send data about beacons{
            beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.ENTERED_REGION, null));

        } else {
            // If we have already seen beacons before, but a fragment is not in
            // the foreground, we send a notification to the user on subsequent detections.
            Log.d(TAG, "Sending notification.");
            sendNotification();
        }
    }


    @Override
    public void didExitRegion(Region region) {
        // removes notification of entering a region if it exists
        notificationManager.cancel(1);
        // update mainActivity that region is no more
        beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.EXITED_REGION, null));

    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // No use at the moment
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("BLE Public Transport")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.drawable.ic_notification_bus);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onBeaconServiceConnect() {
        startMonitoringBeaconsInRegion();
        startRangingBeaconsInRegion();
    }

    public void startMonitoringBeaconsInRegion() {
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startRangingBeaconsInRegion() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon b = beacons.iterator().next();
                    Log.i("beacon", "Ranged for beacon: " + b.toString());
                    beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.RANGED_BEACONS, beacons));
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }
    public BeaconCommunicator getBeaconCommunicator() {
        return beaconCommunicator;
    }
}

