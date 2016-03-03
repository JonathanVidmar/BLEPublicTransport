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
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class BluetoothConnectionFragment extends ObserverFragment {
    private static final String DEBUG_TAG = "BluetoothFragment";
    private TextView statusText;
    private Button button;


    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        statusText = (TextView) view.findViewById(R.id.statusText);
        button = (Button) view.findViewById(R.id.connectButton);

        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //connectionThread.sentMessage();
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

    private void configureButton(final String text, final boolean enabled){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(enabled);
                button.setText(text);
            }
        });
    }

    @Override
    public void update(Object data) {
        BLEPublicTransport application = (BLEPublicTransport) getActivity().getApplication();
        switch (application.connectionState) {
            case BluetoothClient.NOT_PAIRED:
                setStatusText("No gates nearby.");
                break;
            case BluetoothClient.PENDING_DISCOVERABLE:
                setStatusText("Searching for gates nearby...");
                break;
            case BluetoothClient.PENDING_CONNECTION:
                setStatusText("Connecting to nearby gate...");
                break;
            case BluetoothClient.AWAITING_CONNECTION:
                setStatusText("Trying to connect to gate...");
                break;
            case BluetoothClient.PAIRED:
                setStatusText("Connected to gate, the gate will open when you are close enough.");
                break;
            default:
                break;
        }
    }
}

