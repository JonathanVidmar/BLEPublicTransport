package com.lth.thesis.blepublictransport;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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

import org.altbeacon.beacon.Beacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothConnectionFragment extends ObserverFragment {
    private static final String DEBUG_TAG = "BluetoothFragment";
    private BluetoothAdapter bluetoothAdapter;
    private TextView statusText;
    private Button connectButton;

    private BluetoothDevice remoteDevice;
    private ConnectThread connectionThread;

    private final static int PENDING_CONNECTION = 4;
    private final static int PENDING_DISCOVERABILITY = 3;
    private final static int AWAITING_CONNECTION = 2;
    private final static int PAIRED = 1;
    private final static int NOT_PAIRED = 0;

    private int currentState = 0;
    private double PAIRING_THRESHOLD = 6;
    private double OPEN_THRESHOLD = 1.5;
    private final static String SERVER_ADDRESS = "BC:6E:64:29:37:ED";
    private boolean hasOpened = false;

    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // When discovery finds a device
            Log.d(DEBUG_TAG, "Found device");
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getAddress().equals(SERVER_ADDRESS)) {
                    remoteDevice = device;
                    currentState = PENDING_CONNECTION;
                }
            }
        }
    };

    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        BLEPublicTransport application = (BLEPublicTransport) getActivity().getApplication();
        BeaconHelper beaconHelper = application.beaconHelper;
        if (p.type == BeaconPacket.RANGED_BEACONS) {
            for (Beacon b : p.beacons) {
                if (b.getId2().toString().equals(BeaconHelper.region2.getId2().toString())) {
                    checkProximityToGate(b.getDistance());
                }
            }

        }
    }

    private void checkProximityToGate(double distance) {
        Log.d(DEBUG_TAG, currentState + "");
        if (distance < PAIRING_THRESHOLD) {
            switch (currentState) {
                case NOT_PAIRED:
                    if (findBondedDevices()) {
                        currentState = PENDING_CONNECTION;
                    } else {
                        // Kommer inte komma hit i testfallen
                        currentState = PENDING_DISCOVERABILITY;
                        findDevices();
                    }
                    break;
                case PENDING_CONNECTION:
                    startConnectionThread();
                    break;
                case AWAITING_CONNECTION:
                    break;
                case PAIRED:
                    // distance < threshold 2
                    if(distance < OPEN_THRESHOLD) {
                        if(!hasOpened){
                            connectionThread.sentMessage();
                            hasOpened = true;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        statusText = (TextView) view.findViewById(R.id.statusText);
        connectButton = (Button) view.findViewById(R.id.connectButton);
        connectButton.setVisibility(View.INVISIBLE);

        // Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectButton.setEnabled(false);
        if (bluetoothAdapter != null) {
            // Register for bluetooth states and discovery
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
                // Här borde det läggas tille en koll om användaren säger nej.. men det måste göras i
                // on activity result, så jag pallar inte. Men knappen borde vara dissablad tills vi vet det.
                // och sen borde findDevices att köras.
            }
        } else {
            notifyWithMessage("Please buy a new phone");
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //connectionThread.sentMessage();
            }
        });
        return view;
    }

    public void findDevices() {
        Log.d(DEBUG_TAG, "Starting discovery for remote device...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (bluetoothAdapter.startDiscovery()) {
            Log.d(DEBUG_TAG, "Discovery thread started, scanning for devices");
        }
    }

    private boolean findBondedDevices() {
        boolean foundDevice = false;
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(SERVER_ADDRESS)) {
                    notifyWithMessage("Device found: " + device.getName());
                    remoteDevice = device;
                    foundDevice = true;
                    currentState = PENDING_CONNECTION;
                }
            }
        }
        return foundDevice;
    }

    private void startConnectionThread() {
        connectionThread = new ConnectThread(remoteDevice);
        connectionThread.start();
    }

    private void notifyWithMessage(final String message) {
        Log.d(DEBUG_TAG, message);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(message);
            }
        });
    }


    // ------------  PRIVATE CLASS: CONNECT THREAD ----------------------- //

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private ConnectedThread connectedThread;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            connectedThread = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("90B41C8F-A6CD-417C-B406-5BC9C5471636"));
                Log.d(DEBUG_TAG, "Device created");
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Failure in creating socket device");
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                currentState = AWAITING_CONNECTION;
                mmSocket.connect();
                notifyWithMessage("Connected to other device");
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.start();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.d(DEBUG_TAG, "Failure to connect.");

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                currentState = PENDING_CONNECTION;
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        public void sentMessage() {
            connectedThread.write("test".getBytes());
            notifyWithMessage("Sent message");
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // ------------  PRIVATE CLASS: CONNECTED THREAD ----------------------- //

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            currentState = PAIRED;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    notifyWithMessage("Received message");
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                notifyWithMessage("Message sent!");
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}

