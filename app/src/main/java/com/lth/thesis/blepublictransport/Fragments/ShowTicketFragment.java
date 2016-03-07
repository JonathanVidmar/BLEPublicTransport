package com.lth.thesis.blepublictransport.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Utils.NotificationHandler;
import com.lth.thesis.blepublictransport.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass
 * This fragment contains the view to show tickets in the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class ShowTicketFragment extends Fragment {
    SharedPreferences ticketPreferences;
    View view;
    CountDownTimer timer;

    public ShowTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_ticket, container, false);

        ticketPreferences = getActivity().getSharedPreferences(SettingConstants.TICKET_PREFERENCES, 0);
        SharedPreferences settingsPreferences = getActivity().getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);

        boolean isPriceDependent = settingsPreferences.getBoolean(SettingConstants.DESTINATION_DEPENDENT_PRICE, true);

        if(isPriceDependent){
            String destination = ticketPreferences.getString(SettingConstants.VALID_TICKET_DESTINATION, getString(R.string.ticket_no_destination));
            setValidTicketCounterText(String.format(getResources().getString(R.string.ticket_text_with_destination), destination));
        }

        initTimer();
        addRemoveButton();

        return view;
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
    private void addRemoveButton(){
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
    }

    /* Clears the ticket from the system. */
    private void clearTicket(String message) {
        TextView mTextField = (TextView) getActivity().findViewById(R.id.validTicketCounter);
        mTextField.setText(message);
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.notificationHandler.update(NotificationHandler.NO_TICKET_AVAILABLE);
    }

    /* Sets the status message. */
    private void setValidTicketCounterText(final String text){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView mTextField = (TextView) view.findViewById(R.id.validTicketCounterText);
                mTextField.setText(text);
            }
        });
    }
}
