package com.lth.thesis.blepublictransport;

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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // Tag for debugging
    private static final String TAG = "Payment";
    public static final String PREFS_NAME = "MyPrefsFile";
    private RelativeLayout chooseDestination;
    private View view;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_payment, container, false);

        SharedPreferences settings = getActivity().getSharedPreferences(Constants.SETTINGS_PREFERENCES, 0);
        boolean dependant = settings.getBoolean(Constants.DESTINATION_DEPENDENT_PRICE, true); // true == dependent travel
        chooseDestination = (RelativeLayout) view.findViewById(R.id.destinationView);

        Spinner spinner = (Spinner) view.findViewById(R.id.destination_spinner);
        spinner.getBackground().setColorFilter((Color.parseColor("#FFFFFF")), PorterDuff.Mode.SRC_ATOP);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.destination_array, R.layout.spinner_destination_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if(dependant){
            chooseDestination.setVisibility(View.VISIBLE);
        }else{
            chooseDestination.setVisibility(View.GONE);
            TextView infoText = (TextView) view.findViewById(R.id.paymentInfoText);
            infoText.setText("The ticket will cost: ");
        }
        setRetainInstance(true);

        return view;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        Resources res = getResources();
        String[] destinations = res.getStringArray(R.array.destination_array);
        final String destination =  destinations[pos];
        int[] prices = res.getIntArray(R.array.prices);
        final int price =  prices[pos];


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
