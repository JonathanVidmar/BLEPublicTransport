package com.lth.thesis.blepublictransport.BluetoothClient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.lth.thesis.blepublictransport.Beacons.Constants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Bluetooth Client object.
 * A Bluetooth Client that connects to a Bluetooth Server.
 *
 * @author Jacob Arvidsson
 * @version 1.0
 */

public class BluetoothClient {
    // Private attributes
    private BLEPublicTransport application;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice remoteDevice;
    private ConnectThread connectionThread;
    private boolean payAutomatically;

    // States
    private boolean hasOpened = false;
    private int currentState = 0;

    // Constants
    private static final String DEBUG_TAG = "BluetoothClient";
    private static final String SERVER_ADDRESS = "BC:6E:64:29:37:ED";
    private static final double PAIRING_THRESHOLD = 6;
    private static final double OPEN_THRESHOLD = 2;

    // State constants
    public static final int NOT_PAIRED = 0;
    public static final int PAIRED = 1;
    public static final int AWAITING_CONNECTION = 2;
    public static final int PENDING_DISCOVERABLE = 3;
    public static final int PENDING_CONNECTION = 4;
    public static final int PAIRED_AND_WAITING_FOR_USER_INPUT = 5;

    public static final String MESSAGE_OPEN = "200";
    public static final String MESSAGE_CLOSE = "410";
    public static final String MESSAGE_DONT_OPEN = "401";


    /**
     * Constructor of the class.
     * Initiates the Bluetooth Adapter and register for device discovery.
     *
     * @param application context from where the ranging is performed.
     */
    public BluetoothClient(BLEPublicTransport application) {
        this.application = application;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BroadcastReceiver discoveryResult = new BroadcastReceiver() {
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
        application.registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        payAutomatically = application.payAutomatically();

    }

    /**
     * Checks if the device is in range of connectivity.
     * Calls checkProximityToGate if in range and if not
     * in range and the device is paired or attempting connection
     * it is canceled and the thread is closed.
     *
     * @param distance context from where the ranging is performed.
     */
    public void updateClient(double distance) {
        application.connectionState = currentState;
        if (distance < PAIRING_THRESHOLD) {
            checkProximityToGate(distance);
        }else {
            if(currentState != NOT_PAIRED){
                currentState = NOT_PAIRED;
                connectionThread.cancel();
                Log.d(DEBUG_TAG, "NOT PAIRED ANYMORE");
                application.beaconHelper.txPower = -59;
            }
        }
    }

    private void checkProximityToGate(double distance) {
        switch (currentState) {
            case NOT_PAIRED:
                if (findBondedDevices()) {
                    currentState = PENDING_CONNECTION;
                } else {
                    currentState = PENDING_DISCOVERABLE;
                    findDevices();
                }
                break;

            case PENDING_DISCOVERABLE:
                break;

            case PENDING_CONNECTION:
                startConnectionThread();
                break;

            case AWAITING_CONNECTION:
                break;

            case PAIRED:
                if(payAutomatically) {
                    if (distance < OPEN_THRESHOLD) {
                        if (!hasOpened) {
                            sendMessage(MESSAGE_OPEN);
                            hasOpened = true;
                        }
                    } else {
                        if (hasOpened) {
                            sendMessage(MESSAGE_CLOSE);
                            hasOpened = false;
                        }
                    }
                    break;
                }else{
                    if(distance < 5){
                        currentState = PAIRED_AND_WAITING_FOR_USER_INPUT;
                    }
                }
        }

    }

    public void sendMessage(String message){
        connectionThread.sentMessage(message);
    }

    /** Sets up device to discover other devices. */
    public void findDevices() {
        Log.d(DEBUG_TAG, "Starting discovery for remote device...");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (bluetoothAdapter.startDiscovery()) {
            Log.d(DEBUG_TAG, "Discovery thread started, scanning for devices");
        }
    }

    /** Finds already bonded devices. */
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

    /* Creates and starts a connection thread */
    private void startConnectionThread() {
        connectionThread = new ConnectThread(remoteDevice);
        connectionThread.start();
    }

    // ------------  PRIVATE CLASS: CONNECT THREAD ----------------------- //

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private CommunicationThread connectedThread;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("90B41C8F-A6CD-417C-B406-5BC9C5471636"));
                Log.d(DEBUG_TAG, "Device created");
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Failure in creating socket device");
                currentState = PENDING_CONNECTION;
            }
            mmSocket = tmp;
        }

        /**
         * Cancels discovery and connects the device through socket.
         * This will block until it succeeds or throw exception.
         */
        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                currentState = AWAITING_CONNECTION;
                mmSocket.connect();
                connectedThread = new CommunicationThread(mmSocket);
                connectedThread.start();

            } catch (IOException connectException) {
                Log.d(DEBUG_TAG, "Unable to connect; close the socket and get out");
                currentState = PENDING_CONNECTION;
                cancel();
            }
        }

        public void sentMessage(String message) {
            Log.d(DEBUG_TAG, message);
            connectedThread.write(message.getBytes());
        }

        /* Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Failure to close socket");
            }
        }
    }

    // ------------  PRIVATE CLASS: CONNECTED THREAD ----------------------- //

    private class CommunicationThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public CommunicationThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                currentState = PENDING_CONNECTION;
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            if(mmInStream != null && mmOutStream != null){
                currentState = PAIRED;
                Log.d(DEBUG_TAG, "Paired");
            }
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
                    try {
                        application.beaconHelper.txPower = Integer.valueOf(message);
                    }catch (NumberFormatException e){
                        Log.d(DEBUG_TAG, "INTEGER FAILURE");
                    }
                } catch (IOException e) {
                    Log.d(DEBUG_TAG, "Reading input stream failure");
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Failure to send message!");
                currentState = PENDING_CONNECTION;
                hasOpened = !hasOpened;
                connectionThread.cancel();
            }
        }
    }
}
