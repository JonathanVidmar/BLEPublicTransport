package com.lth.thesis.blepublictransport.Fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass
 * This fragment contains a simple measurement tool for logging different settings on the Kalman Filter
 *
 * @author Janathan Vidmar
 * @version 1.0
 */
public class MeasurementFragment extends Fragment {
    private Button markDistanceButton;
    private NumberPicker stepDistance;
    private EditText currentMetersMarked;
    private double currentDistance;
    private StringBuilder sb;
    private double lastDistanceSet;
    private String[] valueArray;
    private double stepLength;
    private BeaconHelper bh;
    private long startTime;

    private static final int MAX_STEP = 20;
    private static final int MIN_STEP = 1;

    public MeasurementFragment() {
        // Required empty public constructor
    }

    /* Sets up all the components of the view. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measurement, container, false);
        markDistanceButton = (Button) view.findViewById(R.id.markDistance);
        stepDistance = (NumberPicker) view.findViewById(R.id.numberPicker);
        currentMetersMarked = (EditText) view.findViewById(R.id.currentMetersMarked);
        stepDistance.setMinValue(0);
        stepDistance.setWrapSelectorWheel(true);
        markDistanceButton.setOnClickListener(markDistanceListener());
        markDistanceButton.setOnLongClickListener(resetTestListener());
        updateDisplayedValuesForStepDistance();
        sb = new StringBuilder();
        bh = ((BLEPublicTransport) getActivity().getApplication()).beaconHelper;
        currentMetersMarked.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            updateDisplayedValuesForStepDistance();
                            hideKeyboard();
                            return true; // consume.
                        }
                        return false; // pass on to other listeners.
                    }
                });

        return view;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateDisplayedValuesForStepDistance() {
        String values = "";
        for (int i = MIN_STEP; i <= MAX_STEP; i++) {
            double value =  Double.valueOf(currentMetersMarked.getText().toString()) / ((double) i / 10.0);
            double frac = value - (long) value;
            if (frac == 0)
                values = values + " " + i;
        }
        valueArray = values.substring(1).split(" ");
        stepDistance.setDisplayedValues(null);
        stepDistance.setMaxValue(valueArray.length - 1);
        stepDistance.setDisplayedValues(valueArray);

    }

    private View.OnLongClickListener resetTestListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                exportTest();
                resetTest();
                return true;
            }
        };
    }

    private View.OnClickListener markDistanceListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markDistanceButton.getText().equals("Begin test!")) {
                    initTest();
                    notifyWithToast("Test started!");
                } else if (markDistanceButton.getText().equals("Mark start!")) {
                    markDistanceButton.setText("Mark distance!");
                    notifyWithToast("Marked!");
                } else if (currentDistance == 0) {
                    markDistanceButton.setText("Hold to reset!");
                }
                else {
                    currentDistance -= stepLength;
                    currentMetersMarked.setText(formatDistance(currentDistance));
                    notifyWithToast("Marked!");
                }
                logValues();
            }
        };
    }

    private void initTest() {
        markDistanceButton.setText("Mark start!");
        stepDistance.setEnabled(false);
        currentMetersMarked.setEnabled(false);
        currentDistance = Double.valueOf(currentMetersMarked.getText().toString());
        lastDistanceSet = currentDistance;
        stepLength = Double.valueOf(valueArray[stepDistance.getValue()]) / 10.0;
        startTime = System.currentTimeMillis();
    }

    private void notifyWithToast(String message) {
        Context context = getActivity().getApplication().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    private String formatDistance(double d) {
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(d);
    }

    private void resetTest() {
        markDistanceButton.setText("Begin test!");
        sb.setLength(0);
        stepDistance.setEnabled(true);
        currentMetersMarked.setEnabled(true);
        currentMetersMarked.setText(Double.valueOf(lastDistanceSet).toString());
        notifyWithToast("Test was reset!");
    }

    private void exportTest() {
        bh.measurementUtil.export(sb.toString());
    }

    private void logValues() {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 100;
        sb.append(elapsedTime + " " + currentDistance + " " + formatDistance(bh.measurementUtil.getMeasurement()).replace(',', '.') + "\n");
    }
}
