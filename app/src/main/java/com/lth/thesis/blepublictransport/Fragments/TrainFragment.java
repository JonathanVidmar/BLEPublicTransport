package com.lth.thesis.blepublictransport.Fragments;


import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
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

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrainFragment extends AbstractObserverFragment {
    private BLEPublicTransport application;
    private HorizontalScrollView sv;
    private RelativeLayout prevStation;
    private RelativeLayout currStation;
    private View currStationIcon;
    private View nextStationIcon;
    private CircularProgressBar circularProgressBar;
    private TextView timeLeft;
    private TextView timeUntilStatus;
    private TextView lundcText;
    private TextView lundcTime;
    private final static float START_TIME = 2760;
    private float timeElapsed = 60;
    private final static float DEPARTURE_TIME = 70;
    private CountDownTimer timer;

    public TrainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train, container, false);

        application = (BLEPublicTransport) getActivity().getApplication();
        prevStation = (RelativeLayout) view.findViewById(R.id.hjarup);
        currStation = (RelativeLayout) view.findViewById(R.id.lundc);
        currStationIcon = view.findViewById(R.id.lundcIcon);
        nextStationIcon = view.findViewById(R.id.gunnesboIcon);
        sv = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);
        timeLeft = (TextView) view.findViewById(R.id.timeLeft);
        timeUntilStatus = (TextView) view.findViewById(R.id.timeUntilText);
        lundcText = (TextView) view.findViewById(R.id.lundcText);
        lundcTime = (TextView) view.findViewById(R.id.lundcTime);
        circularProgressBar = (CircularProgressBar) view.findViewById(R.id.departureCircularProgressBar);
        final GradientDrawable background = (GradientDrawable) nextStationIcon.getBackground();
        background.setColor(Color.WHITE);


        TextView status = (TextView) view.findViewById(R.id.train_ticket_status);
        View statusLine = view.findViewById(R.id.train_ticket_line);
        if(application.hasValidTicket()) {
            status.setText("Show ticket");
            status.setTextColor(getResources().getColor(R.color.colorPrimaryText));
        } else {
            status.setText("Buy ticket");
            statusLine.setVisibility(View.VISIBLE);
        }
        LinearLayout ticketButton = (LinearLayout) view.findViewById(R.id.ticket_button);
        ticketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity main = (MainActivity) getActivity();
                if(application.hasValidTicket()){
                    main.executeNavigationTo(MainActivity.SHOW_TICKET_FRAGMENT);
                }else{
                    main.executeNavigationTo(MainActivity.PAYMENT_FRAGMENT);
                }
            }
        });
        // run when view is visible for the first time
        view.post(new Runnable() {
                           @Override
                           public void run() {
                               Handler handler = new Handler();
                               handler.postDelayed(new Runnable() {
                                   public void run() {
                                       scrollToView(sv, prevStation);
                                       if(application.hasValidTicket()) {
                                           circularProgressBar.setProgressWithAnimation(100.0f * timeElapsed / START_TIME, 500); // duration in millis
                                           initTimer();
                                           timeUntilStatus.setText("Arriving at Helsingborg C");
                                       }
                                   }
                               }, 200); //time in millis
                           }
                       }
        );

        return view;
    }

    @Override
    public void onPause() {
        if (timer != null) timer.cancel();
        super.onPause();
    }

    private void initTimer() {
        timer = new CountDownTimer((int)(START_TIME-timeElapsed) * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeElapsed++;
                circularProgressBar.setProgress(100.0f*timeElapsed/START_TIME);
                timeLeft.setText(timeLeftToString());
                if (timeElapsed == DEPARTURE_TIME) {
                    leftStation();
                }
            }

            public void onFinish() {
                timeElapsed = 0;
                initTimer();
            }

        }.start();
    }

    private void leftStation() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            currStationIcon.setBackground(getContext().getResources().getDrawable(R.drawable.station_visited));
            animateNextStation();
            lundcTime.setVisibility(View.VISIBLE);
            lundcText.setTypeface(null, Typeface.NORMAL);
            lundcText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDivider));
            scrollToView(sv, currStation);
        }
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
        anim.setDuration(1000L);
        anim.start();
    }

    private void animateNextStation() {
        int colorFrom = 0xFFFFFFFF;
        int colorTo = getResources().getColor(R.color.colorAccent);
        final ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                colorFrom,
                colorTo);

        final GradientDrawable background = (GradientDrawable) nextStationIcon.getBackground();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                background.setColor((Integer) animator.getAnimatedValue());
            }


        });
        valueAnimator.setDuration(1600);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
    }

    private String timeLeftToString() {

            int minutes = (int) (Math.floor((START_TIME - timeElapsed) / 60));
            int seconds = (int) ((START_TIME - timeElapsed) % 60);

            return twoDigitString(minutes) + " : " + twoDigitString(seconds);
        }

    private String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
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
        if(p.type == BeaconPacket.RANGED_BEACONS){
            if(p.beacons.size() > 0){ foundObjectsNear(p.beacons); }
        }
    }

    /**
     * Called when ranging has found nearby beacons.
     * It updates each beacon's value in the HashMap: foundBeacons and then calls sortList()
     * @param  beacons the beacons found by ranging.
     */
    public void foundObjectsNear(final ArrayList<PublicTransportBeacon> beacons) {
        if(application.isAtStation){
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.executeNavigationTo(MainActivity.STATION_FRAGMENT);
        }else{
            // Still on the train
        }
    }

}
