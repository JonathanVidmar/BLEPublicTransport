package com.lth.thesis.blepublictransport.Fragments;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.BluetoothClient.BluetoothClient;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

/**
 * A simple {@link AbstractObserverFragment} subclass
 * This fragment contains the status for automatically paying and for manually opening gates.
 *
 * @author Jacob Arvidsson
 * @version 1.1
 */
public class GateConnectionFragment extends AbstractObserverFragment {
    private static final String DEBUG_TAG = "BluetoothFragment";
    private TextView statusText;
    private Button button;
    private BLEPublicTransport application;
    private boolean reenteredFragment = false;
    private boolean hasNotYetBeenDenied = true;
    private boolean animationOngoing = false;

    public GateConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        reenteredFragment = true;
        statusText = (TextView) view.findViewById(R.id.statusText);
        button = (Button) view.findViewById(R.id.connectButton);
        application = (BLEPublicTransport) getActivity().getApplication();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reenteredFragment = false;
                String message;
                String status;
                if(application.hasValidTicket()){
                    message = BluetoothClient.MESSAGE_OPEN_TIMER;
                    status = getString(R.string.gate_authorized);
                    startColorAnimationOnBackground(statusText.getRootView(), R.color.colorSuccess);
                } else {
                    message = BluetoothClient.MESSAGE_UNAUTHORIZED_TIMER;
                    status = getString(R.string.gate_unauthorized);
                    startColorAnimationOnBackground(statusText.getRootView(), R.color.colorFailure);
                }
                application.manageGate(message);
                setStatusText(status);
                updateButtonVisibility(View.INVISIBLE);

            }
        });

        MainActivity a = (MainActivity) getActivity();
        a.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
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

    private void updateButtonVisibility(final int newVisibility) {
        if (button.getVisibility() != newVisibility) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(newVisibility);
            }
        });
        }
    }

    @Override
    public void update(Object data) {
        if (reenteredFragment) {
            switch (application.connectionState) {
                case BluetoothClient.NOT_PAIRED:
                    updateView(getString(R.string.no_gates_nearby));
                    updateButtonVisibility(View.INVISIBLE);
                    break;
                case BluetoothClient.PENDING_DISCOVERABLE:
                    updateView(getString(R.string.searching_for_gate));
                    updateButtonVisibility(View.INVISIBLE);
                    break;
                case BluetoothClient.PENDING_CONNECTION:
                    updateView(getString(R.string.connecting_to_gate));
                    updateButtonVisibility(View.INVISIBLE);
                    break;
                case BluetoothClient.AWAITING_CONNECTION:
                    updateView(getString(R.string.connecting_to_gate));
                    updateButtonVisibility(View.INVISIBLE);
                    break;
                case BluetoothClient.PAIRED:
                    setStatusText(getString(R.string.approach_gate));
                    break;
                case BluetoothClient.AUTOMATIC_AND_INSIDE_THRESHOLD:
                    if (application.hasValidTicket()) {
                        updateView(getString(R.string.gate_authorized));
                        startColorAnimationOnBackground(statusText.getRootView(), R.color.colorSuccess);
                    } else if (hasNotYetBeenDenied) {
                        updateView(getString(R.string.gate_unauthorized));
                        startColorAnimationOnBackground(statusText.getRootView(), R.color.colorFailure);
                        hasNotYetBeenDenied = false;
                    }
                    break;
                case BluetoothClient.WAITING_FOR_USER_TO_LEAVE:
                    updateView(getString(R.string.gate_authorized));
                    startColorAnimationOnBackground(statusText.getRootView(), R.color.colorSuccess);
                    break;
                case BluetoothClient.MANUAL_AND_WAITING_FOR_USER_INPUT:
                    setStatusText(getString(R.string.connected_to_gate));
                    updateButtonVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    public void updateView(String text) {
        setStatusText(text);
    }

    public void startColorAnimationOnBackground(View view, int toColor){
        if (!animationOngoing && ((ColorDrawable)view.getBackground()).getColor() == Color.WHITE){
            animationOngoing = true;
            ObjectAnimator anim = ObjectAnimator.ofObject(view,"backgroundColor", new ArgbEvaluator(),Color.WHITE, ContextCompat.getColor(application.getApplicationContext(), toColor));
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationOngoing = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.setDuration(300).start();
        }
    }
}

