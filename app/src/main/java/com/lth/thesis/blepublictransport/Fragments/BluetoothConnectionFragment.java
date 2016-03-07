package com.lth.thesis.blepublictransport.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.BluetoothClient.BluetoothClient;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.R;

/**
 * A simple {@link ObserverFragment} subclass
 * This fragment contains the status for automatically paying and for manually opening gates.
 *
 * @author Jacob Arvidsson
 * @version 1.1
 */
public class BluetoothConnectionFragment extends ObserverFragment {
    private static final String DEBUG_TAG = "BluetoothFragment";
    private TextView statusText;
    private Button button;
    private boolean hasOpenedGate = false;
    private BLEPublicTransport application;
    private boolean needsToConfigureButton = true;
    private boolean canConfigureText = true;

    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        statusText = (TextView) view.findViewById(R.id.statusText);
        button = (Button) view.findViewById(R.id.connectButton);
        application = (BLEPublicTransport) getActivity().getApplication();

        button.setVisibility(View.INVISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String open = (application.hasValidTicket()) ?  BluetoothClient.MESSAGE_OPEN_TIMER : BluetoothClient.MESSAGE_DONT_OPEN;
                application.manageGate((hasOpenedGate) ? BluetoothClient.MESSAGE_CLOSE : open);
                hasOpenedGate = true;
                configureButton("", View.INVISIBLE);
                setStatusText("The gate has opened");

            }
        });
        return view;
    }

    private void setStatusText(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(message);
            }
        });
    }

    private void configureButton(final String text, final int visible) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(visible);
                button.setText(text);
            }
        });
    }

    @Override
    public void update(Object data) {
        switch (application.connectionState) {
            case BluetoothClient.NOT_PAIRED:
                updateView("No gates nearby.");
                break;
            case BluetoothClient.PENDING_DISCOVERABLE:
                updateView("Searching for gates nearby...");
                break;
            case BluetoothClient.PENDING_CONNECTION:
                updateView("Connecting to nearby gate...");
                break;
            case BluetoothClient.AWAITING_CONNECTION:
                updateView("Connecting to nearby gate...");
                break;
            case BluetoothClient.PAIRED:
                setStatusText(application.payAutomatically() ? "Connected to gate, the gate will open " +
                        "when you are close enough." : "Do you want to open the gate?");
                break;
            case BluetoothClient.PAIRED_AND_WAITING_FOR_USER_INPUT:
                if (!hasOpenedGate && needsToConfigureButton) {
                    setStatusText("Do you want to open the gate?");
                    configureButton("Open gate", View.VISIBLE);
                    needsToConfigureButton = false;
                }
            default:
                break;
        }
    }

    public void updateView(String text) {
        setStatusText(text);
        needsToConfigureButton = true;
        hasOpenedGate = false;
    }
}

