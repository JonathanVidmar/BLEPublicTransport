package com.lth.thesis.blepublictransport.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Config.BeaconConstants;
import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.Models.Station;
import com.lth.thesis.blepublictransport.Utils.NotificationHandler;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass
 * This fragment contains the view to show tickets in the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class ShowTicketFragment extends AbstractObserverFragment {
    private SharedPreferences ticketPreferences;
    private View view;
    private CountDownTimer timer;
    private ImageView destinationImage;
    private TextView currentStationShortLabel;
    private TextView currentStationLabel;
    private TextView destinationStationShortLabel;
    private TextView destinationLabel;
    private TextView boughtTimeLabel;
    private TextView boughtDayLabel;
    private TextView validTimeLabel;
    private TextView validDayLabel;
    private TextView nextDepartureLabel;
    private TextView timeOfNextDepartureLabel;
    private TextView trackNumberLabel;
    private TextView distanceLabel;

    public ShowTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_ticket, container, false);

        ticketPreferences = getActivity().getSharedPreferences(SettingConstants.TICKET_PREFERENCES, 0);

        destinationImage = (ImageView) view.findViewById(R.id.header_image);
        currentStationShortLabel = (TextView) view.findViewById(R.id.currentStationShortLabel);
        currentStationLabel = (TextView) view.findViewById(R.id.currentStationLabel);
        destinationStationShortLabel = (TextView) view.findViewById(R.id.destStationShortLabel);
        destinationLabel = (TextView) view.findViewById(R.id.destStationLabel);
        boughtTimeLabel = (TextView) view.findViewById(R.id.boughtTimeLabel);
        boughtDayLabel = (TextView) view.findViewById(R.id.boughtDayLabel);
        validTimeLabel = (TextView) view.findViewById(R.id.validUntilTimeLabel);
        validDayLabel = (TextView) view.findViewById(R.id.validDayLabel);
        nextDepartureLabel = (TextView) view.findViewById(R.id.nextDeparture);
        timeOfNextDepartureLabel = (TextView) view.findViewById(R.id.timeOfNextDepartureLabel);
        trackNumberLabel = (TextView) view.findViewById(R.id.track);
        distanceLabel = (TextView) view.findViewById(R.id.trackDistanceLabel);
        trackNumberLabel.setText("N/A");
        distanceLabel.setText("N/A");

        configureView();
        initTimer();
        //addRemoveButton();

        MainActivity a = (MainActivity) getActivity();
        a.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
        a.currentFragment = this;
        return view;
    }

    private void configureView(){
        String destinationString = ticketPreferences.getString(SettingConstants.BOUGHT_TICKET_DESTINATION, "");
        Station destination = BeaconConstants.DESTINATION_MAP.get(destinationString);

        int id = getResources().getIdentifier("com.lth.thesis.blepublictransport:drawable/" + destination.image, null, null);
        destinationImage.setImageResource(id);
        destinationStationShortLabel.setText(destination.abbreviation);
        destinationLabel.setText(destination.name);
        SimpleDateFormat getFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String ticketBought = ticketPreferences.getString(SettingConstants.BOUGHT_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");
        String validUntil = ticketPreferences.getString(SettingConstants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");
        try {
            Date bought = getFormatter.parse(ticketBought);
            Date valid = getFormatter.parse(validUntil);
            SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE dd MMM", Locale.US);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH.mm", Locale.US);
            boughtDayLabel.setText(dayFormatter.format(bought));
            validDayLabel.setText(dayFormatter.format(valid));
            boughtTimeLabel.setText(timeFormatter.format(bought));
            validTimeLabel.setText(timeFormatter.format(valid));

            Date nextDeparture = new Date();
            nextDeparture.setTime(System.currentTimeMillis() + (14 * 60 * 1000));
            timeOfNextDepartureLabel.setText(timeFormatter.format(nextDeparture));
        }catch (ParseException e) {

        }
        nextDepartureLabel.setText(destination.transportType);

        currentStationLabel.setText(BeaconConstants.HOME_STATION.name);
        currentStationShortLabel.setText(BeaconConstants.HOME_STATION.abbreviation);
    }

    /* Initiates the timer and updates the view. */
    private void initTimer(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String validUntil = ticketPreferences.getString(SettingConstants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");
        try {
            Date date = formatter.parse(validUntil);
            Date now = new Date();
            long timeLeft = date.getTime() - now.getTime();
            if(timeLeft > 0) {
                timer = new CountDownTimer(timeLeft, 1000) {
                    public void onTick(long millisUntilFinished) {
                        TextView mTextField = (TextView) view.findViewById(R.id.validTicketCounter);
                        mTextField.setText(convertSecondsToHMmSs(millisUntilFinished / 1000));
                    }

                    public void onFinish() {
                        clearTicket(getString(R.string.ticket_invalid));
                    }

                    public String convertSecondsToHMmSs(long seconds) {
                        long s = seconds % 60;
                        long m = (seconds / 60) % 60;
                        long h = (seconds / (60 * 60)) % 24;
                        return String.format("%d:%02d:%02d", h, m, s);
                    }

                }.start();
            }else{
                clearTicket(getString(R.string.ticket_invalid));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /* Adds a floating button with the remove action and its handler*/
    /*private void addRemoveButton(){
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), getString(R.string.ticket_removed), Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = ticketPreferences.edit();
                editor.putString(SettingConstants.VALID_TICKET_DATE, "1999-06-10'T'10:10:10'Z");
                editor.apply();

                setValidTicketCounterText(getString(R.string.ticket_not_valid));
                timer.cancel();
                clearTicket("");
            }
        });
    }*/

    /* Clears the ticket from the system. */
    private void clearTicket(String message) {
        TextView mTextField = (TextView) getActivity().findViewById(R.id.validTicketCounter);
        mTextField.setText(message);
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.notificationHandler.update(NotificationHandler.NO_TICKET_AVAILABLE);
    }

    @Override
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if(p.type == BeaconPacket.RANGED_BEACONS){
            if(p.beacons.size() > 0){
                updateDistanceText(getBeaconForDistance(p.beacons));
            }
        }
    }

    private PublicTransportBeacon getBeaconForDistance(ArrayList<PublicTransportBeacon> beacons){
        for(PublicTransportBeacon beacon : beacons){
            if(beacon.getID().equals(BeaconConstants.BEACON_LIST.get(BeaconConstants.INSTANCE_2).getID())){
                return beacon;
            }
        }
        return null;
    }

    private void updateDistanceText(PublicTransportBeacon beacon){
        if(beacon == null) { return; }
        final String trackName = beacon.getName();
        final String distance = BeaconHelper.getDistanceText(beacon);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackNumberLabel.setText(trackName);
                distanceLabel.setText(distance);
            }
        });
    }
}
