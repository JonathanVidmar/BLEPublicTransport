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

import java.util.*;

/**
 * The main fragment class, subclass of Fragment,
 * which implements the BeaconConsumer which let's it detect iBeacons.
 */
public class StationHomeFragment extends Fragment implements Observer{
    protected static final String TAG = "StationHome";
    private View view;
    private ListView listView;
    private HashMap<String, Double> foundBeacons = new HashMap<String, Double>();
    private NearObjectListViewAdapter mAdapter;

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
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.getBeaconCommunicator().addObserver(this);
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

    @Override
    public void update(Observable observable, Object data) {

    }
}
