package com.lth.thesis.blepublictransport;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.common.api.ResultCallback;


/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentFragment extends Fragment {
    // Tag for debugging
    private static final String TAG = "Payment";
    public static final String PREFS_NAME = "MyPrefsFile";
    private TextView chooseDestination;


    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        boolean dependant = settings.getBoolean("dependantPayment", false); // true == dependent travel
        chooseDestination = (TextView) view.findViewById(R.id.chooseDestinationLabel);

        if(dependant){
            chooseDestination.setVisibility(View.VISIBLE);
        } else {
            chooseDestination.setVisibility(View.INVISIBLE);
        }
        setRetainInstance(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
