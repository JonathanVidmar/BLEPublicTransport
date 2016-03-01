package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.*;

/**
 * The main fragment class, subclass of Fragment,
 * which implements the BeaconConsumer which let's it detect iBeacons.
 */
public class StationHomeFragment extends ObserverFragment{
    protected static final String TAG = "StationHome";
    private View view;
    private ListView listView;
    private HashMap<String, Beacon> foundBeacons = new HashMap<>();
    private NearObjectListViewAdapter mAdapter;
    private ArrayList<Beacon> list;
    private BeaconHelper helper;

    public StationHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // Adds a placeholder text for when no region has been detected.
        ArrayList<Beacon> items = new ArrayList<Beacon>();
        //items.add("Looking for nearby facilities");

        mAdapter = new NearObjectListViewAdapter(getActivity(), items);
        // Updates the list view
        // Only the main thread can update the ui
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    listView = (ListView) view.findViewById(R.id.locationItems);
                    listView.setAdapter(mAdapter);
                    listView.setVisibility(View.INVISIBLE);
            }});
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        helper = app.beaconHelper;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (helper.currentlyInMainRegion()) enteredStation();
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
                listView.setVisibility(View.VISIBLE);

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

                listView.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * This method is called when ranging has found nearby beacons. It updates
     * each beacon value in the HashMap foundBeacons and then calls the update list
     * function.
     */
    public void foundObjectsNear(Collection<Beacon> beacons) {
        BeaconHelper helper = new BeaconHelper();
        for (Beacon oneBeacon : beacons) {
            String beaconName = helper.getBeaconName(oneBeacon.getId2());
            // helper.getDistanceText(oneBeacon.getDistance())
            foundBeacons.put(beaconName, oneBeacon);
        }
        updateList();
    }

    /**
     * This methods parses all the found beacons into strings and
     * updates the list view.
     */
    public void updateList() {

        list = new ArrayList<>();

            for (String key : foundBeacons.keySet()) {
                list.add(foundBeacons.get(key));
            }
            Collections.sort(list, new Comparator<Beacon>() {
                @Override
                public int compare(Beacon b2, Beacon b1){
                    if(helper.getDistance(b1) < helper.getDistance(b2)){
                        return 1;
                    }else if (helper.getDistance(b1) > helper.getDistance(b2)){
                        return -1;
                    }else{
                        return 0;
                    }
                }
            });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.updateList(list);
                mAdapter.notifyDataSetChanged();
                }
            });
    }

    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if(p.type == BeaconPacket.ENTERED_REGION){
            enteredStation();
        }else if(p.type == BeaconPacket.EXITED_REGION){
            leftStation();
        }else if(p.type == BeaconPacket.RANGED_BEACONS){
            foundObjectsNear(p.beacons);
        }
    }
}
