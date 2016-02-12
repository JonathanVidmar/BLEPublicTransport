package com.lth.thesis.blepublictransport;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * The main fragment class, subclass of Fragment,
 * which implements the BeaconConsumer which let's it detect iBeacons.
 */
public class StationHomeFragment extends Fragment implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    private View view;
    private ListView listView;
    private HashMap<String, Double> foundBeacons = new HashMap<String, Double>();
    private NearObjectListViewAdapter mAdapter;
    private final Region region = new Region("com.jacobarvidsson.test", Identifier.parse("FF72C36E-157A-4E58-9837-CCE51B75C7F4"), null, null);

    public StationHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // Adds a placeholder text for when no region has been detected.
        ArrayList<String> items = new ArrayList<String>();
        items.add("Looking for nearby facilities");

        mAdapter = new NearObjectListViewAdapter(getActivity(), items);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        beaconManager = BeaconManager.getInstanceForApplication(getActivity());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setForegroundScanPeriod(1000l);
        beaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unbind if needed
        if (beaconManager.isBound(this)) beaconManager.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            beaconManager.stopMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            // oops?
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        // ID for the beacon: region, uuid, major, minor
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "didEnterRegion");
                enteredStation();

                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "didExitRegion");

                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d("SCANNING!!", "AAAAAAAAaaaAAAaaAAaaaA");
                foundObjectsNear(beacons);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return getActivity().bindService(intent, serviceConnection, i);
    }

    /**
     * This is run when the beacon has been detected. It changes the display
     * text and shows the list view that shows nearest objects.
     */
    public void enteredStation() {
        // Only the main thread can update the ui
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sets the information text
                TextView stationText = (TextView) getActivity().findViewById(R.id.found_label);
                stationText.setText("Welcome to Kings Cross");

                // Updates the list view
                listView = (ListView) view.findViewById(R.id.locationItems);
                listView.setAdapter(mAdapter);
            }
        });
    }

    /**
     * This is run when the beacons no longer is in range. It changes the display text
     * and removes the list that shows all nearby objects.
     */
    public void leftStation() {
        // Only the main thread can update the ui
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sets the information text
                TextView stationText = (TextView) getActivity().findViewById(R.id.found_label);
                stationText.setText("No station near you");

                // Deletes the list view
                listView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is called when ranging has found nearby beacons. It updates
     * each beacon value in the HashMap foundBeacons and then calls the update list
     * function.
     */
    public void foundObjectsNear(Collection<Beacon> beacons) {
        for (Beacon oneBeacon : beacons) {
            String key = oneBeacon.getId1().toString() + oneBeacon.getId2().toString();
            foundBeacons.put(key, oneBeacon.getDistance());
        }
        updateList();
    }

    /**
     * This methods parses all the found beacons into strings and
     * updates the list view.
     */
    public void updateList() {
        if (getActivity() != null) {

        ArrayList<String> list = new ArrayList<String>();

            for (String s : foundBeacons.keySet()) {
                String text = "Distane: " + foundBeacons.get(s) + " Beacon:" + s;
                list.add(text);
            }
            mAdapter.updateList(list);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

    }

}
