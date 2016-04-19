package com.lth.thesis.blepublictransport.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Config.BeaconConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.Utils.NearbyListViewAdapter;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Identifier;

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
    private NearbyListViewAdapter mAdapter;
    private BeaconHelper helper;
    private boolean isNearbyMode = true;

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nearby_fragment, container, false);
        mAdapter = new NearbyListViewAdapter(getActivity(), new ArrayList<PublicTransportBeacon>());
        mAdapter.updateArrivalsList(new ArrayList<>(BeaconConstants.ARRIVALS_LIST));
        listView = (ListView) view.findViewById(R.id.locationItems);
        listView.setAdapter(mAdapter);
        listView.setVisibility(View.INVISIBLE);

        final Button nearbyButton = (Button) view.findViewById(R.id.nearbyTabButton);
        final Button timeTableButton = (Button) view.findViewById(R.id.timetableTabButton);

        nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNearbyMode){
                    isNearbyMode = true;
                    mAdapter.showNearby(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nearbyButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                            timeTableButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        timeTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNearbyMode){
                    mAdapter.showNearby(false);
                    isNearbyMode = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeTableButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                            nearbyButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        MainActivity a = (MainActivity) getActivity();
        a.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorIcons));

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
        updateLocation(getString(R.string.beacons_found), View.INVISIBLE);
    }

    /* Runs when beacons out of range and updates the views components. */
    public void leftStation() {
        updateLocation(getString(R.string.beacons_lost), View.INVISIBLE);
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
    public void foundObjectsNear(final ArrayList<PublicTransportBeacon> beacons) {
        Identifier closestID = beacons.get(0).getID();
        if(helper.isBeaconAtStation(closestID)){
            if(isNearbyMode){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateList(beacons);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
            if(listView.getVisibility() != View.VISIBLE){
                updateLocation("Welcome to Lund's Central Station", View.VISIBLE);
            }
        }else{
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment train = new TrainFragment();
            fragmentTransaction.replace(R.id.fragment_container, train, MainActivity.TRAIN_FRAGMENT);
            fragmentTransaction.commit();
        }
    }

    /* Observer method from the application. Receives the beacon information. */
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if(p.type == BeaconPacket.ENTERED_REGION){
            enteredStation();
        }else if(p.type == BeaconPacket.EXITED_REGION){
            leftStation();
        }else if(p.type == BeaconPacket.RANGED_BEACONS){
            if(p.beacons.size() > 0){ foundObjectsNear(p.beacons); }
        }
    }
}
