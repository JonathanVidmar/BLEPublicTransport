package com.lth.thesis.blepublictransport.Fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Beacons.Constants;
import com.lth.thesis.blepublictransport.Beacons.NotificationHandler;
import com.lth.thesis.blepublictransport.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass
 * This fragment contains the payment options for the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1                 (current version number of program)
 */
public class PaymentFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    SharedPreferences settings;
    private int dest = 0;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        setRetainInstance(true);

        settings = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, 0);

        final boolean dependantPrices = settings.getBoolean(Constants.DESTINATION_DEPENDENT_PRICE, true);
        RelativeLayout chooseDestination = (RelativeLayout) view.findViewById(R.id.destinationView);

        Spinner spinner = (Spinner) view.findViewById(R.id.destination_spinner);
        spinner.getBackground().setColorFilter((Color.parseColor("#FFFFFF")), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.destination_array, R.layout.spinner_destination_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(dependantPrices){
            chooseDestination.setVisibility(View.VISIBLE);
        }else{
            chooseDestination.setVisibility(View.GONE);
            TextView infoText = (TextView) view.findViewById(R.id.paymentInfoText);
            infoText.setText(R.string.payment_cost_text);
        }

        final Button button = (Button) view.findViewById(R.id.payButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Date validTo = new Date();
                validTo.setTime(System.currentTimeMillis() + (120 * 60 * 1000));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String dateString = formatter.format(validTo);

                SharedPreferences ticketPreferences = getActivity().getSharedPreferences(Constants.TICKET_PREFERENCES, 0);
                SharedPreferences.Editor editor = ticketPreferences.edit();
                editor.putString(Constants.VALID_TICKET_DATE, dateString);

                if(dependantPrices){
                    Resources res = getResources();
                    String[] destinations = res.getStringArray(R.array.destination_array);
                    final String destination =  destinations[dest];
                    editor.putString(Constants.VALID_TICKET_DESTINATION, destination);
                }

                editor.apply();
                BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
                app.notificationHandler.update(NotificationHandler.VALID_TICKET_AVAILABLE);

                ShowTicketFragment fragment = new ShowTicketFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment, "showTicketTag");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Resources res = getResources();
        String[] destinations = res.getStringArray(R.array.destination_array);
        final String destination =  destinations[pos];
        int[] prices = res.getIntArray(R.array.prices);
        final int price =  prices[pos];
        dest = pos;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView infoText = (TextView) getActivity().findViewById(R.id.paymentInfoText);
                infoText.setText(" A trip from your current station to " + destination + " will cost: ");
                TextView priceTag = (TextView) getActivity().findViewById(R.id.priceTag);
                priceTag.setText(price + " kr");
            }
        });
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
