package com.lth.thesis.blepublictransport.BluetoothClient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.BeaconPacket;

import org.altbeacon.beacon.Beacon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Bluetooth Client object.
 *
 * <P>A Bluetooth Client that connects to a Bluetooth Server.
 *
 * @author Jacob Arvidsson
 * @version 1.0
 */

public class BluetoothClient {
    private final static String DEBUG_TAG = "BluetoothClient";
    private BluetoothAdapter bluetoothAdapter;
    private BLEPublicTransport application;

    private BluetoothDevice remoteDevice;
    private ConnectThread connectionThread;

    private static final int PENDING_CONNECTION = 4;
    private static final int PENDING_DISCOVERABILITY = 3;
    private static final int AWAITING_CONNECTION = 2;
    private static final int PAIRED = 1;
    private static final int NOT_PAIRED = 0;

    private int currentState = 0;
    private double PAIRING_THRESHOLD = 6;
    private double OPEN_THRESHOLD = 1.5;
    private final static String SERVER_ADDRESS = "BC:6E:64:29:37:ED";
    private boolean hasOpened = false;

    /* Receiver for when I Bluetooth divice has been found. */
    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DEBUG_TAG, "Found device");

            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getAddress().equals(SERVER_ADDRESS)) {
                    remoteDevice = device;
                    currentState = PENDING_CONNECTION;
                }
            }
        }
    };

    /* Constructor */
    public BluetoothClient(BLEPublicTransport application){
        this.application = application;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        application.registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    /* Should be called in Application on ranging */    // och detta skulle kunna göras i app, så kan man bara skicka med distance.
    public void update(Object data) {
        BeaconPacket p = (BeaconPacket) data;
        if (p.type == BeaconPacket.RANGED_BEACONS) {
            for (Beacon b : p.beacons) {
                if (b.getId2().toString().equals(BeaconHelper.region2.getId2().toString())) {
                    checkProximityToGate(application.beaconHelper.getDistance(b));
                }
            }

        }
    }

    private void checkProximityToGate(double distance) {
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
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(SERVER_ADDRESS)) {
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

    /** Should call some cool method in BLEPublicTransport instead.   */
    private void callApplication(String string){
        Log.d(DEBUG_TAG, string);
    }

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
        }

        public void sentMessage() {
            connectedThread.write("test".getBytes());
            callApplication("Message sent");
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
                    String message = new String(Arrays.copyOfRange(buffer, 0, bytes));
                    int rssi = Integer.valueOf(message);
                    application.beaconHelper.txPower = rssi;
                    //callApplication("Received message");
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                callApplication("Message sent");
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
