package com.lth.thesis.blepublictransport.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrainFragment extends AbstractObserverFragment {
    private BeaconHelper helper;
    public TrainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        helper = app.beaconHelper;
        return inflater.inflate(R.layout.fragment_train, container, false);
    }

    /* Observer method from the application. Receives the beacon information. */
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if(p.type == BeaconPacket.ENTERED_REGION){
            // Probably shoudn't be handled here
        }else if(p.type == BeaconPacket.EXITED_REGION){
            // Left train
        }else if(p.type == BeaconPacket.RANGED_BEACONS){
            if(p.beacons.size() > 0){ foundObjectsNear(p.beacons); }
        }
    }

    /**
     * Called when ranging has found nearby beacons.
     * It updates each beacon's value in the HashMap: foundBeacons and then calls sortList()
     * @param  beacons the beacons found by ranging.
     */
    public void foundObjectsNear(final ArrayList<PublicTransportBeacon> beacons) {
        Identifier closestID = beacons.get(0).getID();
        if(helper.isBeaconAtStation(closestID)){
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment train = new NearbyFragment();
            fragmentTransaction.replace(R.id.fragment_container, train, MainActivity.STATION_FRAGMENT);
            fragmentTransaction.commit();
        }else{
            // Still on the train
        }
    }

}
