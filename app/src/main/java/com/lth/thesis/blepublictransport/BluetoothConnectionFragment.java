package com.lth.thesis.blepublictransport;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothConnectionFragment extends Fragment {
    public static final int DISCOVERY_REQUEST = 1;
    private static final String tag = "BluetoothFragment";
    private BluetoothAdapter bluetoothAdapter;
    private TextView statusText;
    private BluetoothDevice remoteDevice;
    private Button connectButton;

    /**
     * This can also be done by checking requestCode == REQUEST_ENABLE_BT and
     * resultCode  == RESULT.OK but using a BroadcastReceiver and listening
     * for this broadcast can be useful to detect changes made to the Bluetooth
     * state while your app is running.
     */
    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state){
                case(BluetoothAdapter.STATE_TURNING_ON):
                {
                    notifyWithMessage("Bluetooth turning on...");
                    break;
                }
                case(BluetoothAdapter.STATE_ON):
                {
                    notifyWithMessage("Bluetooth is on!");
                    break;
                }
                case(BluetoothAdapter.STATE_TURNING_OFF): {
                    notifyWithMessage("Bluetooth turning off...");

                    break;
                }
                case(BluetoothAdapter.STATE_OFF):
                {
                    notifyWithMessage("Bluetooth is off!");
                    break;
                }
            }
            notifyWithMessage(bluetoothAdapter.getName() + " : " + bluetoothAdapter.getAddress());
        }
    };

    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        statusText = (TextView) view.findViewById(R.id.statusText);

        // Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            if(!bluetoothAdapter.isEnabled()){
                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                getActivity().getApplicationContext().registerReceiver(bluetoothState, intentFilter);

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
            }else {
                notifyWithMessage(bluetoothAdapter.getName() + " : " + bluetoothAdapter.getAddress());
            }
        } else {
            notifyWithMessage("Device does not support Bluetooth");
        }


        connectButton = (Button) view.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bluetoothConnect(view);
            }
        });
        return view;
    }

    public void bluetoothConnect(View view){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                notifyWithMessage(device.getName() + ": " + device.getAddress());
                remoteDevice = device;
            }
        }

        if(remoteDevice == null) {
            findDevices();
        }
    }

    private String getLastUsedRemoteDevice(){
        SharedPreferences prefs = getActivity().getPreferences(getActivity().MODE_PRIVATE);
        return prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
    }

    public void findDevices(){
        notifyWithMessage("Starting discovery for remote device...");
        getActivity().registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if(bluetoothAdapter.startDiscovery()) {
            notifyWithMessage("Discovery thread started, scanning for devices");
        }
    }

    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // When discovery finds a device
            Log.d(tag, "Found device");
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                notifyWithMessage(device.getName() + " - " + device.getAddress());
                remoteDevice = device;
                ConnectThread connection = new ConnectThread(remoteDevice);
                connection.run();
            }
        }
    };

    private void notifyWithMessage(final String message){
        Log.d(tag, message);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(message);
            }
        });
    }


    // ------------  PRIVATE CLASS: CONNECT THREADS ----------------------- //

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("90B41C8F-A6CD-417C-B406-5BC9C5471636"));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}

