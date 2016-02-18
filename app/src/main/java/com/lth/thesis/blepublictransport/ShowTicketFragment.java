package com.lth.thesis.blepublictransport;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowTicketFragment extends Fragment {
    CountDownTimer timer;


    public ShowTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_show_ticket, container, false);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        SharedPreferences ticket = getActivity().getSharedPreferences(Constants.TICKET_PREFERENCES, 0);
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, 0);

        String validUntil = ticket.getString(Constants.VALID_TICKET_DATE, "2016-06-10'T'10:10:10'Z'");
        boolean dependant = settings.getBoolean(Constants.DESTINATION_DEPENDENT_PRICE, true);
        if(dependant){
            String destination = ticket.getString(Constants.VALID_TICKET_DESTINATION, "Nowhere");
            TextView mTextField = (TextView) view.findViewById(R.id.validTicketCounterText);
            mTextField.setText("Ticket is valid to " + destination + " until the timer is finished");

        }

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
                        clearTicket("INVALID TICKET");
                    }

                    public String convertSecondsToHMmSs(long seconds) {
                        long s = seconds % 60;
                        long m = (seconds / 60) % 60;
                        long h = (seconds / (60 * 60)) % 24;
                        return String.format("%d:%02d:%02d", h, m, s);
                    }

                }.start();
            }else{
                clearTicket("INVALID TICKET");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Ticket has been removed.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                SharedPreferences ticket = getActivity().getSharedPreferences(Constants.TICKET_PREFERENCES, 0);

                SharedPreferences.Editor editor = ticket.edit();
                editor.putString(Constants.VALID_TICKET_DATE, "1999-06-10'T'10:10:10'Z");
                editor.commit();
                TextView mTextField = (TextView) getActivity().findViewById(R.id.validTicketCounterText);
                mTextField.setText("This ticket is no longer valid.");
                timer.cancel();
                clearTicket("");
            }
        });

        return view;
    }

    private void clearTicket(String message) {
        TextView mTextField = (TextView) getActivity().findViewById(R.id.validTicketCounter);
        mTextField.setText(message);
        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.notificationHandler.update(NotificationHandler.NO_TICKET_AVAILABLE);
    }
}
