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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

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
    private Timer myTimer;

    // States
    private boolean hasNotBeenDeniedBefore = true;
    private int currentState = 0;

    // SettingConstants
    private static final String DEBUG_TAG = "BluetoothClient";
    private static final String SERVER_ADDRESS = "BC:6E:64:29:37:ED";
    private static final double PAIRING_THRESHOLD = 10;
    private static final double OPEN_THRESHOLD = 3;

    // State constants
    public static final int NOT_PAIRED = 0;
    public static final int PAIRED = 1;
    public static final int AWAITING_CONNECTION = 2;
    public static final int PENDING_DISCOVERABLE = 3;
    public static final int PENDING_CONNECTION = 4;
    public static final int MANUAL_AND_WAITING_FOR_USER_INPUT = 5;
    public static final int AUTOMATIC_AND_INSIDE_THRESHOLD = 6;
    public static final int WAITING_FOR_USER_TO_LEAVE = 7;

    public static final String MESSAGE_OPEN = "200";
    public static final String MESSAGE_OPEN_TIMER = "201";
    public static final String MESSAGE_CLOSE = "410";
    public static final String MESSAGE_UNAUTHORIZED = "400";
    public static final String MESSAGE_UNAUTHORIZED_TIMER = "401";
    public static final String MESSAGE_PING = "100";


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
        } else {
            if(currentState != NOT_PAIRED){
                killPairing();
            }
        }
    }

    private void killPairing() {
        currentState = NOT_PAIRED;
        connectionThread.cancel();
        application.beaconHelper.updateTxPower(-59);
        Log.d(DEBUG_TAG, "NOT PAIRED ANYMORE");
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
                if (payAutomatically) {
                    if (distance < OPEN_THRESHOLD) currentState = AUTOMATIC_AND_INSIDE_THRESHOLD;
                } else currentState = MANUAL_AND_WAITING_FOR_USER_INPUT;
                break;
            case AUTOMATIC_AND_INSIDE_THRESHOLD:
                if (application.hasValidTicket()) {
                    sendMessage(MESSAGE_OPEN);
                    currentState = WAITING_FOR_USER_TO_LEAVE;
                } else if (hasNotBeenDeniedBefore) {
                    sendMessage(MESSAGE_UNAUTHORIZED);
                    hasNotBeenDeniedBefore = false;
                }
                break;
            case WAITING_FOR_USER_TO_LEAVE:
                if (distance > OPEN_THRESHOLD) sendMessage(MESSAGE_CLOSE);
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
            if (connectedThread != null) {
                connectedThread.write(message.getBytes());
            } else {
                connectionFailed();
            }
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
                setTimerToPingConnection();
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
                        application.beaconHelper.updateTxPower(Integer.valueOf(message));
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
                connectionFailed();
            }
        }
    }

    private void connectionFailed() {
        cancelTimer();
        currentState = PENDING_CONNECTION;
        connectionThread.cancel();
    }

    private void setTimerToPingConnection() {
        if (myTimer == null) {
            myTimer = new Timer("myTimer");
            myTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendMessage(MESSAGE_PING);
                }
            },0, 1000);
        }
    }

    private void cancelTimer(){
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
    }
}
