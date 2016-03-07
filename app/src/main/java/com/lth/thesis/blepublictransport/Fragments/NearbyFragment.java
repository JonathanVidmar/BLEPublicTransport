package com.lth.thesis.blepublictransport.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Utils.NearbyListViewAdapter;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;

import java.util.*;

/**
 * A simple {@link AbstractObserverFragment} subclass
 * Shows a list of nearby objects if a station has been
 * entered. Is empty otherwise.
 *
 * @author      Jacob Arvidsson & Jonathan Vidmar
 * @version     1.1
 */
public class NearbyFragment extends AbstractObserverFragment {
    private ListView listView;
    private HashMap<String, Beacon> foundBeacons = new HashMap<>();
    private NearbyListViewAdapter mAdapter;
    private BeaconHelper helper;

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<Beacon> items = new ArrayList<>();

        mAdapter = new NearbyListViewAdapter(getActivity(), items);
        listView = (ListView) view.findViewById(R.id.locationItems);
        listView.setAdapter(mAdapter);
        listView.setVisibility(View.INVISIBLE);

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

    /* Runs when beacons is in range and updates the views components. */
    public void enteredStation() {
        updateLocation(getString(R.string.station_entered_text), View.VISIBLE);
    }

    /* Runs when beacons out of range and updates the views components. */
    public void leftStation() {
        updateLocation(getString(R.string.station_no_nearby_text), View.INVISIBLE);
    }

    /**
     * Update view's components.
     * Sets the text on top of the screen and hides or shows the list of objects.
     *
     * @param  visibility if the list of objects should be displayed.
     * @param  text text to be displayed on top of the screen.
     */
    private void updateLocation(final String text, final int visibility){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView stationText = (TextView) getActivity().findViewById(R.id.found_label);
                stationText.setText(text);
                listView.setVisibility(visibility);
            }
        });
    }

    /**
     * Called when ranging has found nearby beacons.
     * It updates each beacon's value in the HashMap: foundBeacons and then calls sortList()
     * @param  beacons the beacons found by ranging.
     */
    public void foundObjectsNear(Collection<Beacon> beacons) {
        for (Beacon b : beacons) {
            String beaconName = helper.getBeaconName(b);
            foundBeacons.put(beaconName, b);
        }
        sortList();
    }

    /* Parses all the found beacons and sorts them in order of distance. */
    public void sortList() {
        ArrayList<Beacon> list = new ArrayList<>();
            for (String key : foundBeacons.keySet()) {
                list.add(foundBeacons.get(key));
            }
            Collections.sort(list, new Comparator<Beacon>() {
                @Override
                public int compare(Beacon b2, Beacon b1) {
                    if (helper.getDistance(b1) < helper.getDistance(b2)) {
                        return 1;
                    } else if (helper.getDistance(b1) > helper.getDistance(b2)) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        updateList(list);
    }

    /* Updates the list in the adapter and notifies it of changes. */
    private void updateList(final ArrayList<Beacon> list){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.updateList(list);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /* Observer method from the application. Receives the beacon information. */
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
