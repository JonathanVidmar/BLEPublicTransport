package com.lth.thesis.blepublictransport.Main;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.lth.thesis.blepublictransport.Beacons.BeaconCommunicator;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.BluetoothClient.BluetoothClient;
import com.lth.thesis.blepublictransport.Utils.NotificationHandler;
import com.lth.thesis.blepublictransport.Utils.WalkDetection;
import static com.lth.thesis.blepublictransport.Config.BeaconConstants.*;

import org.altbeacon.beacon.*;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class BLEPublicTransport extends Application implements BootstrapNotifier, BeaconConsumer {
    // Private attributes
    private static final String DEBUG_TAG = "BLEPublicTransport";
    private BeaconCommunicator beaconCommunicator;
    private BluetoothClient bluetoothClient;
    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;
    private WalkDetection walkDetection;
    private SharedPreferences settings;

    // Private states
    private boolean notCurrentlyRanging = true;
    public boolean shouldNotify = true;

    // Public attributes
    public BeaconHelper beaconHelper;
    public NotificationHandler notificationHandler;

    // Public states
    public int connectionState = -1;
    public boolean active = true;
    public boolean isAtStation = true;

    private HashMap<String, Beacon> foundBeacons = new HashMap<>();


    public void onCreate() {
        super.onCreate();

        settings = getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);
        beaconHelper = new BeaconHelper(settings.getBoolean(SettingConstants.SELF_CORRECTING_BEACON, true));
        beaconHelper.updateProcessNoise(settings.getInt(SettingConstants.KALMAN_SEEK_VALUE, 83));
        bluetoothClient = new BluetoothClient(this);
        walkDetection = new WalkDetection(this);
        updateWalkDetectionListener(settings.getBoolean(SettingConstants.WALK_DETECTION, false));
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_LAYOUT));

        try {
            //beaconManager.setForegroundScanPeriod(1100l);
            //beaconManager.setForegroundBetweenScanPeriod(0l);
            //beaconManager.setBackgroundScanPeriod(120l);
            //beaconManager.setBackgroundBetweenScanPeriod(0l);

            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        regionBootstrap = new RegionBootstrap(this, REGIONS);
        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        notificationHandler = new NotificationHandler(this);
        beaconCommunicator = new BeaconCommunicator();
    }

    public void manageGate(String state){
        bluetoothClient.sendMessage(state);
    }

    public void stop(){
        try {

            for (Region region :
                    REGIONS) {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.stopMonitoringBeaconsInRegion(region);
            }
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
        }
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
            for (Region region :
                    REGIONS) {
                beaconManager.stopRangingBeaconsInRegion(region);
            }
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
            for (Region region :
                    REGIONS) {
                beaconManager.startRangingBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startRangingBeaconsInRegion() {
        notCurrentlyRanging = false;
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                beaconHelper.updateBeaconDistances(beacons, walkDetectionEnabled() ? walkDetection.getState() : 1);
                ArrayList<PublicTransportBeacon> sortedBeacons = sortedListOfBeacons(beacons);
                beaconCommunicator.notifyObservers(new BeaconPacket(BeaconPacket.RANGED_BEACONS, sortedBeacons));

                if (simulatingGate()) {
                    for (Beacon b : beacons) {
                        if (b.getId2().equals(INSTANCE_2)) {
                            bluetoothClient.updateClient(beaconHelper.getDistance(b));
                            if (shouldNotify && connectionState == BluetoothClient.MANUAL_AND_WAITING_FOR_USER_INPUT) {
                                notificationHandler.update(NotificationHandler.OPEN_GATE);
                                shouldNotify = false;
                            }
                        }
                    }
                }

            }
        });
        try {
            for (Region region :
                    REGIONS) {
                beaconManager.startRangingBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when ranging has found nearby beacons.
     * It updates each beacon's value in the HashMap: foundBeacons and then calls sortList()
     * @param  beacons the beacons found by ranging.
     */
    public ArrayList<PublicTransportBeacon> sortedListOfBeacons(Collection<Beacon> beacons) {
        for (Beacon b : beacons) {
            String beaconName = beaconHelper.getBeaconName(b);
            foundBeacons.put(beaconName, b);
        }
        ArrayList<PublicTransportBeacon> list = sortList();
        Identifier closestID = list.get(0).getID();
        isAtStation = beaconHelper.isBeaconAtStation(closestID);
        return list;
    }

    /* Parses all the found beacons and sorts them in order of distance. */
    public ArrayList<PublicTransportBeacon> sortList() {
        ArrayList<PublicTransportBeacon> list = new ArrayList<>();
        ArrayList<Beacon> temp = new ArrayList<>();

        for (String key : foundBeacons.keySet()) {
            temp.add(foundBeacons.get(key));
        }
        Collections.sort(temp, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon b2, Beacon b1) {
                if (beaconHelper.getDistance(b1) < beaconHelper.getDistance(b2)) {
                    return 1;
                } else if (beaconHelper.getDistance(b1) > beaconHelper.getDistance(b2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for(Beacon b: temp){
            list.add(beaconHelper.beaconList.get(b.getId2()));
        }
        return list;
    }

    public BeaconCommunicator getBeaconCommunicator() {
        return beaconCommunicator;
    }

    public boolean hasValidTicket() {
        SharedPreferences ticket = getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);
        boolean hasSubscription = ticket.getBoolean(SettingConstants.HAS_SUBSCRIPTION, false);
        return hasSubscription || hasSingleTicket();
    }

    private boolean hasSingleTicket(){
        boolean hasValidTicket = false;
        SharedPreferences ticket = getSharedPreferences(SettingConstants.TICKET_PREFERENCES, 0);
        String validUntil = ticket.getString(SettingConstants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");

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
    private boolean walkDetectionEnabled() { return settings.getBoolean(SettingConstants.WALK_DETECTION, false); }
    private boolean simulatingGate() { return settings.getBoolean(SettingConstants.SIMULATE_GATE, false); }
    public boolean payAutomatically(){
        return settings.getBoolean(SettingConstants.PAY_AUTOMATICALLY, true);
    }
    public void updateWalkDetectionListener(boolean enabled){
        if(enabled) walkDetection.startDetection();
        else walkDetection.killDetection();
    }
}

