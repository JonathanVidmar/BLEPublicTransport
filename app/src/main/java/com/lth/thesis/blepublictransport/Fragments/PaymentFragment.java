package com.lth.thesis.blepublictransport.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Utils.NotificationHandler;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass
 * This fragment contains the payment options for the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class PaymentFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private View view;
    private boolean isPricesDependent;
    private int dest = 0;
    private boolean visaPayment = true;
    private Button visa;
    private Button mastercard;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_payment, container, false);
        setRetainInstance(true);

        SharedPreferences settings = getActivity().getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);
        isPricesDependent = settings.getBoolean(SettingConstants.DESTINATION_DEPENDENT_PRICE, true);

        createChooseDestinationArea();
        createButton();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void createChooseDestinationArea(){
        RelativeLayout chooseDestination = (RelativeLayout) view.findViewById(R.id.destinationView);
        Spinner spinner = (Spinner) view.findViewById(R.id.destination_spinner);
        spinner.getBackground().setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.destination_array, R.layout.spinner_destination_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(isPricesDependent){
            chooseDestination.setVisibility(View.VISIBLE);
        }else{
            chooseDestination.setVisibility(View.GONE);
            TextView infoText = (TextView) view.findViewById(R.id.paymentInfoText);
            infoText.setText(R.string.payment_cost_text);
        }
    }

    private void createButton(){
        final Button button = (Button) view.findViewById(R.id.payButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void showDialog(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setView(inflater.inflate(R.layout.dialog_payment, null))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        performPayment();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();
        alert.show();
        visa = (Button) alert.findViewById(R.id.visaButton);
        mastercard = (Button) alert.findViewById(R.id.mastercardButton);

        visa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!visaPayment) {
                    visaPayment = true;
                    visa.setBackgroundColor(Color.parseColor("#EEF4FC"));
                    mastercard.setBackgroundColor(Color.WHITE);
                }
            }
        });

        mastercard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visaPayment) {
                    visaPayment = false;
                    visa.setBackgroundColor(Color.WHITE);
                    mastercard.setBackgroundColor(Color.parseColor("#EEF4FC"));
                }
            }
        });
    }

    private void performPayment(){
        Date validTo = new Date();
        validTo.setTime(System.currentTimeMillis() + (120 * 60 * 1000));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String dateString = formatter.format(validTo);

        SharedPreferences ticketPreferences = getActivity().getSharedPreferences(SettingConstants.TICKET_PREFERENCES, 0);
        SharedPreferences.Editor editor = ticketPreferences.edit();
        editor.putString(SettingConstants.VALID_TICKET_DATE, dateString);
        editor.apply();

        ShowTicketFragment fragment = new ShowTicketFragment();
        if (isPricesDependent) {
            Resources res = getResources();
            String[] destinations = res.getStringArray(R.array.destination_array);
            final String destination = destinations[dest];
            fragment.destination = destination;
        }

        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.notificationHandler.update(NotificationHandler.VALID_TICKET_AVAILABLE);

        android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, MainActivity.SHOW_TICKET_FRAGMENT);
        fragmentTransaction.commit();
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
                Resources res = getResources();
                infoText.setText(String.format(res.getString(R.string.payment_ticket_text), destination));
                TextView priceTag = (TextView) getActivity().findViewById(R.id.priceTag);
                priceTag.setText(String.format(res.getString(R.string.payment_price_tag), price));
            }
        });
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
