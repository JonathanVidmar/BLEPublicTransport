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
    private HashMap<String, Double> foundBeacons;
    private NearObjectListViewAdapter mAdapter;

    public StationHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        foundBeacons = new HashMap<String, Double>();

        ArrayList<String> items = new ArrayList<String>();
        items.add("Looking for nearby facilities");
        mAdapter = new NearObjectListViewAdapter(getActivity(), items);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onBeaconServiceConnect() {
        // ID for the beacon: region, uuid, major, minor
        final Region region = new Region("com.jacobarvidsson.test", Identifier.parse("FF72C36E-157A-4E58-9837-CCE51B75C7F4"), null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "didEnterRegion");

                // Only the main thread can update the ui
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        TextView stationText = (TextView) getActivity().findViewById(R.id.found_label);
                        stationText.setText("Welcome to Kings Cross");
                        listView = (ListView) view.findViewById(R.id.locationItems);
                        listView.setAdapter(mAdapter);
                    }
                });


                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "didExitRegion");

                // Only the main thread can update the ui
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        TextView stationText = (TextView) getActivity().findViewById(R.id.found_label);
                        stationText.setText("No station near you");
                        listView.setVisibility(View.GONE);
                    }
                });

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
                for (Beacon oneBeacon : beacons) {
                    Log.d(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2()); // + "/" + oneBeacon.getId3());
                    String key = oneBeacon.getId1().toString() + oneBeacon.getId2().toString(); // + oneBeacon.getId3().toString();
                    foundBeacons.put(key, oneBeacon.getDistance());
                }
                updateList();
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateList() {
        if (getActivity() != null) {
            ArrayList<String> list = new ArrayList<String>();

            for (String s : foundBeacons.keySet()) {
                String text = "Distane: " + foundBeacons.get(s) + " Beacon:" + s;
                list.add(text);
                Log.d(TAG, text);
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

}
