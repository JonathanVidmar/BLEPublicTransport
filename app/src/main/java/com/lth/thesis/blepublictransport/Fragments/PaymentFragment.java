package com.lth.thesis.blepublictransport.Fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.lth.thesis.blepublictransport.Config.BeaconConstants;
import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Utils.NotificationHandler;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.lth.thesis.blepublictransport.Config.BeaconConstants.*;

/**
 * A simple {@link Fragment} subclass
 * This fragment contains the payment options for the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class PaymentFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // View components
    private View view;
    private ImageView headerImage;

    // Global variables
    private boolean isPricesDependent;
    private String destination;
    private int ticketPrice;
    private String[] destinationsArray;

    // Dialog components
    private boolean visaPayment = true;
    private LinearLayout visa;
    private LinearLayout mastercard;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_payment, container, false);
        destinationsArray = getResources().getStringArray(R.array.destination_array);
        destination = destinationsArray[0];

        headerImage = (ImageView) view.findViewById(R.id.header_image_dest);
        SharedPreferences settings = getActivity().getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);
        isPricesDependent = settings.getBoolean(SettingConstants.DESTINATION_DEPENDENT_PRICE, true);
        createChooseDestinationArea();
        createButton();

        // Resets the menu button color to white
        MainActivity main = (MainActivity) getActivity();
        main.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorIcons));
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
        final Date validTo = new Date();
        validTo.setTime(System.currentTimeMillis() + (120 * 60 * 1000));
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH.mm", Locale.US);

        String summeryText = "";
        if (isPricesDependent) {
            summeryText = BeaconConstants.HOME_STATION.name +  "\n" + destination + "\n" + timeFormatter.format(validTo) + "\n" + ticketPrice + " kr";
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setView(inflater.inflate(R.layout.dialog_payment, null))
                .setPositiveButton(getString(R.string.payment_buy_button_text), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        performPayment(destination, validTo);
                    }
                })
                .setNegativeButton(getString(R.string.payment_dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();
        alert.show();

        visa = (LinearLayout) alert.findViewById(R.id.visaButton);
        mastercard = (LinearLayout) alert.findViewById(R.id.mastercardButton);

        TextView dialog_description_box = (TextView) alert.findViewById(R.id.dialog_description_box);
        dialog_description_box.setText(summeryText);
        final int selectedColor = ContextCompat.getColor(getActivity(), R.color.color_dialog_selected);
        visa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!visaPayment) {
                    visaPayment = true;
                    visa.setBackgroundColor(selectedColor);
                    mastercard.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        mastercard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visaPayment) {
                    visaPayment = false;
                    visa.setBackgroundColor(Color.TRANSPARENT);
                    mastercard.setBackgroundColor(selectedColor);
                }
            }
        });
    }

    private void performPayment(String destination, Date validTo){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        String dateString = formatter.format(validTo);
        Date boughtDate = new Date();
        boughtDate.setTime(System.currentTimeMillis());
        String boughtString = formatter.format(boughtDate);

        SharedPreferences ticketPreferences = getActivity().getSharedPreferences(SettingConstants.TICKET_PREFERENCES, 0);
        SharedPreferences.Editor editor = ticketPreferences.edit();
        editor.putString(SettingConstants.BOUGHT_TICKET_DATE, boughtString);
        editor.putString(SettingConstants.VALID_TICKET_DATE, dateString);
        if (isPricesDependent) { editor.putString(SettingConstants.BOUGHT_TICKET_DESTINATION, destination); }
        editor.apply();

        BLEPublicTransport app = (BLEPublicTransport) getActivity().getApplication();
        app.notificationHandler.update(NotificationHandler.VALID_TICKET_AVAILABLE);

        ShowTicketFragment fragment = new ShowTicketFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, MainActivity.SHOW_TICKET_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Resources res = getResources();
        destination =  destinationsArray[pos];
        int[] prices = res.getIntArray(R.array.prices);
        ticketPrice = prices[pos];

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView infoText = (TextView) getActivity().findViewById(R.id.paymentInfoText);
                Resources res = getResources();
                infoText.setText(String.format(res.getString(R.string.payment_ticket_text), BeaconConstants.HOME_STATION.name, destination));
                int imgid = getResources().getIdentifier("com.lth.thesis.blepublictransport:drawable/" + BeaconConstants.DESTINATION_MAP.get(destination).image, null, null);
                headerImage.setImageResource(imgid);
                TextView priceTag = (TextView) getActivity().findViewById(R.id.priceTag);
                priceTag.setText(String.format(res.getString(R.string.payment_price_tag), ticketPrice));
            }
        });
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
