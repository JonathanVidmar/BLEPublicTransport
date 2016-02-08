package com.lth.thesis.blepublictransport;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
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
public class PaymentFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Tag for debugging
    private static final String TAG = "Nearby";

    // Provides an entry point for Google Play services
    GoogleApiClient mGoogleApiClient = null;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    // Subscribe to messages in the background.
    private void backgroundSubscribe() {
        // Connect the GoogleApiClient.
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.i(TAG, "Subscribing for background updates.");
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .build();
            Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Subscribed successfully.");
                            } else {
                                Log.i(TAG, "Could not subscribe.");
                                //handleUnsuccessfulNearbyResult(status); <-- Den här gjorde massor saker
                            }
                        }
                    });
        }
    }
    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(getActivity().getApplicationContext(), 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Skickar en tillbaka hit
    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(getActivity().getApplicationContext(), PaymentFragment.class);
    }

    // Hanterar när man har blivit hitskickad, se ovan
    protected void onHandleIntent(Intent intent) {
        Nearby.Messages.handleIntent(intent, new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i(TAG, "Found message via PendingIntent: " + message);
            }

            @Override
            public void onLost(Message message) {
                Log.i(TAG, "Lost message via PendingIntent: " + message);
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        // If the user has requested a subscription or publication task that requires
        // GoogleApiClient to be connected, we keep track of that task and execute it here, since
        // we now have a connected GoogleApiClient.
        //executePendingTasks();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended: "
                + connectionSuspendedCauseToString(cause));
    }

    private static String connectionSuspendedCauseToString(int cause) {
        switch (cause) {
            case CAUSE_NETWORK_LOST:
                return "CAUSE_NETWORK_LOST";
            case CAUSE_SERVICE_DISCONNECTED:
                return "CAUSE_SERVICE_DISCONNECTED";
            default:
                return "CAUSE_UNKNOWN: " + cause;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // For simplicity, we don't handle connection failure thoroughly in this sample. Refer to
        // the following Google Play services doc for more details:
        // http://developer.android.com/google/auth/api-client.html
        Log.i(TAG, "connection to GoogleApiClient failed");
    }

}
