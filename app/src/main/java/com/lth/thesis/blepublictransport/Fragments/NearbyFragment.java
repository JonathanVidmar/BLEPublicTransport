package com.lth.thesis.blepublictransport.Fragments;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Config.BeaconConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.Utils.NearbyListViewAdapter;
import com.lth.thesis.blepublictransport.R;

import java.util.*;

/**
 * A simple {@link AbstractObserverFragment} subclass
 * Shows a list of nearby objects if a station has been
 * entered. Is empty otherwise.
 *
 * @author Jacob Arvidsson & Jonathan Vidmar
 * @version 1.1
 */
public class NearbyFragment extends AbstractObserverFragment {
    private BLEPublicTransport application;
    private NearbyListViewAdapter mAdapter;
    private ListView listView;
    private MainActivity activity;

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
                setMode(true, nearbyButton, timeTableButton);
            }
        });
        timeTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(false, nearbyButton, timeTableButton);
            }
        });
        activity = (MainActivity) getActivity();
        activity.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorIcons));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        application = (BLEPublicTransport) getActivity().getApplication();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BeaconHelper.currentlyInMainRegion()) enteredStation();
    }

    /**
     * Sets the mode of the list view to either nearby objects or timetable.
     * @param button, if nearbyButton was pressed, true, otherwise false
     * @param nearbyButton, the button for nearby list
     * @param timeTableButton, the button for timetable list
     */
    private void setMode(final boolean button, final Button nearbyButton, final Button timeTableButton) {
        if ((button && !isNearbyMode) || !button && isNearbyMode) {
            isNearbyMode = button;
            mAdapter.showNearby(button);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int selected_color = ContextCompat.getColor(getActivity(), R.color.colorAccent);
                    int unselected_color = ContextCompat.getColor(getActivity(), R.color.colorPrimaryText);
                    nearbyButton.setTextColor((button) ? selected_color : unselected_color);
                    timeTableButton.setTextColor((!button) ? selected_color : unselected_color);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
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
     * @param visibility if the list of objects should be displayed.
     * @param text       text to be displayed on top of the screen.
     */
    private void updateLocation(final String text, final int visibility) {
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
     *
     * @param beacons the beacons found by ranging.
     */
    public void foundObjectsNear(final ArrayList<PublicTransportBeacon> beacons) {
        if (application.isAtStation) {
            if (isNearbyMode) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateList(beacons);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
            if (listView.getVisibility() != View.VISIBLE) {
                updateLocation(getString(R.string.welcome_message), View.VISIBLE);
            }
        } else {
            activity.executeNavigationTo(MainActivity.TRAIN_FRAGMENT);
        }
    }

    /* Observer method from the application. Receives the beacon information. */
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if (p.type == BeaconPacket.ENTERED_REGION) {
            enteredStation();
        } else if (p.type == BeaconPacket.EXITED_REGION) {
            leftStation();
        } else if (p.type == BeaconPacket.RANGED_BEACONS) {
            if (p.beacons.size() > 0) {
                foundObjectsNear(p.beacons);
            }
        }
    }
}
