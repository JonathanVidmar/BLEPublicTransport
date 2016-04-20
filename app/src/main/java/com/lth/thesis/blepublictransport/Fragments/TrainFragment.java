package com.lth.thesis.blepublictransport.Fragments;


import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewParent;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

import com.lth.thesis.blepublictransport.Utils.VerticalTextView;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrainFragment extends AbstractObserverFragment {
    private BeaconHelper helper;
    private View view;
    private HorizontalScrollView sv;
    private RelativeLayout station;
    private CircularProgressBar circularProgressBar;

    public TrainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        helper = app.beaconHelper;
        view = inflater.inflate(R.layout.fragment_train, container, false);
        station = (RelativeLayout) view.findViewById(R.id.lastStation);
        sv = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);
        circularProgressBar = (CircularProgressBar) view.findViewById(R.id.departureCircularProgressBar);


        // run when view is visible for the first time
        view.post(new Runnable() {
                           @Override
                           public void run() {
                               Handler handler = new Handler();
                               handler.postDelayed(new Runnable() {
                                   public void run() {
                                       scrollToView(sv, station);
                                       circularProgressBar.setProgressWithAnimation(65, 500); // duration in millis
                                   }
                               }, 200); //time in millis
                           }
                       }
        );
        return view;
    }

    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    private void scrollToView(final HorizontalScrollView scrollViewParent, final View view) {
        // Get deepChild Offset
        Point childOffset = new Point();
        getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
        // Scroll to child.
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(scrollViewParent);
        anim.setPropertyName("scrollX");
        anim.setIntValues(childOffset.x);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setDuration(600L);
        anim.start();
    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumalated Offset.
     */
    private void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
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
