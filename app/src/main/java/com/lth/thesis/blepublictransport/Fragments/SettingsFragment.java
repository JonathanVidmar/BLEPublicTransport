package com.lth.thesis.blepublictransport.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import android.widget.TextView;
import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;
import com.lth.thesis.blepublictransport.Utils.KalmanFilter;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass
 * This fragment contains the settings for the application.
 *
 * @author      Jacob Arvidsson
 * @version     1.1
 */
public class SettingsFragment extends Fragment {
    private SharedPreferences settings;
    private Switch dependentSwitch;
    private Switch autoSwitch;
    private Switch subscriptionSwitch;
    private SeekBar kalmanSeek;
    private TextView kalmanFilterValue;
    private Switch selfCorrectionSwitch;
    private Switch wdSwitch;
    private Switch gateSimulationSwitch;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /* Sets up all the components of the view. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        dependentSwitch = (Switch) view.findViewById(R.id.dependentSwitch);
        autoSwitch = (Switch) view.findViewById(R.id.automaticallySwitch);
        subscriptionSwitch = (Switch) view.findViewById(R.id.subscriptionSwitch);
        kalmanSeek = (SeekBar) view.findViewById(R.id.kalmanSeek);
        kalmanFilterValue = (TextView) view.findViewById(R.id.kalmanValue);
        selfCorrectionSwitch = (Switch) view.findViewById(R.id.selfCorrectionSwitch);
        wdSwitch = (Switch) view.findViewById(R.id.walkDetectionSwitch);
        gateSimulationSwitch = (Switch) view.findViewById(R.id.gateSimulationSwitch);

        settings = getActivity().getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);

        setUpDependentPriceSwitch();
        setUpAutomaticPaymentSwitch();
        setUpSubscriptionSwitch();
        setUpKalmanSeek();
        setUpSelfcorrectingSwitch();
        setUpWalkDetectionSwitch();
        setUpGateSimulationSwitch();

        MainActivity a = (MainActivity) getActivity();
        a.changeMenuColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryText));
        return view;
    }

    /* Sets the correct text and adds an onCheckedChange listener to the dependent price switch */
    private void setUpDependentPriceSwitch(){
        boolean isDependent = settings.getBoolean(SettingConstants.DESTINATION_DEPENDENT_PRICE, true);
        dependentSwitch.setChecked(isDependent);
        dependentSwitch.setText((isDependent) ? R.string.settings_dependent_text : R.string.settings_independent_text);
        dependentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.DESTINATION_DEPENDENT_PRICE, isChecked);
                editor.apply();
                if (isChecked) {
                    dependentSwitch.setText(R.string.settings_dependent_text);
                } else {
                    dependentSwitch.setText(R.string.settings_independent_text);
                }
            }
        });
    }

    /* Sets the correct text and adds an onCheckedChange listener to the automatic payment switch */
    private void setUpAutomaticPaymentSwitch(){
        boolean payAutomatically = settings.getBoolean(SettingConstants.PAY_AUTOMATICALLY, true);
        autoSwitch.setChecked(payAutomatically);
        autoSwitch.setText((payAutomatically) ? R.string.settings_automatic_text : R.string.settings_manually_text);
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.PAY_AUTOMATICALLY, isChecked);
                editor.apply();
                if (isChecked) {
                    autoSwitch.setText(R.string.settings_automatic_text);
                } else {
                    autoSwitch.setText(R.string.settings_manually_text);
                }
            }
        });
    }

    /* Sets the correct text and adds an onCheckedChange listener to the automatic payment switch */
    private void setUpSubscriptionSwitch(){
        boolean hasSubscription = settings.getBoolean(SettingConstants.HAS_SUBSCRIPTION, true);
        subscriptionSwitch.setChecked(hasSubscription);
        subscriptionSwitch.setText((hasSubscription) ? R.string.settings_subscription_text : R.string.settings_no_subscription_text);
        subscriptionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.HAS_SUBSCRIPTION, isChecked);
                editor.apply();
                if (isChecked) {
                    subscriptionSwitch.setText(R.string.settings_subscription_text);
                } else {
                    subscriptionSwitch.setText(R.string.settings_no_subscription_text);
                }
            }
        });
    }



    /* Sets the correct text and adds a onChange listener to the kalman filter seekbar */
    private void setUpKalmanSeek(){
        int kalmanSeekValue = settings.getInt(SettingConstants.KALMAN_SEEK_VALUE, 83);
        kalmanSeek.setProgress(kalmanSeekValue);
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        kalmanFilterValue.setText(df.format(KalmanFilter.getCalculatedNoise(kalmanSeekValue)));
        kalmanSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);
                kalmanFilterValue.setText(df.format(KalmanFilter.getCalculatedNoise(progress)));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(SettingConstants.KALMAN_SEEK_VALUE, seekBar.getProgress());
                editor.apply();
                ((BLEPublicTransport) getActivity().getApplication()).beaconHelper.updateProcessNoise(seekBar.getProgress());
            }
        });
    }

    /* Sets the correct text and adds an onCheckedChange listener to the self-correcting beacon switch */
    private void setUpSelfcorrectingSwitch(){
        boolean selfcorrection = settings.getBoolean(SettingConstants.SELF_CORRECTING_BEACON, true);
        selfCorrectionSwitch.setChecked(selfcorrection);
        selfCorrectionSwitch.setText((selfcorrection) ? R.string.settings_enabled : R.string.settings_disabled);
        selfCorrectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.SELF_CORRECTING_BEACON, isChecked);
                editor.apply();
                ((BLEPublicTransport) getActivity().getApplication()).beaconHelper.updateSelfCorrection(isChecked);
                selfCorrectionSwitch.setText((isChecked) ? R.string.settings_enabled : R.string.settings_disabled);
            }
        });
    }

    /* Sets the correct text and adds an onCheckedChange listener to the walk detection switch */
    private void setUpWalkDetectionSwitch(){
        boolean walkDetection = settings.getBoolean(SettingConstants.WALK_DETECTION, false);
        wdSwitch.setChecked(walkDetection);
        wdSwitch.setText((walkDetection) ? R.string.settings_enabled : R.string.settings_disabled);
        wdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.WALK_DETECTION, isChecked);
                editor.apply();
                ((BLEPublicTransport) getActivity().getApplication()).updateWalkDetectionListener(isChecked);
                wdSwitch.setText((isChecked) ? R.string.settings_enabled : R.string.settings_disabled);
            }
        });
    }

    /* Sets the correct text and adds an onCheckedChange listener to the gate simulation switch */
    private void setUpGateSimulationSwitch(){
        boolean simulateGate = settings.getBoolean(SettingConstants.SIMULATE_GATE, false);
        gateSimulationSwitch.setChecked(simulateGate);
        gateSimulationSwitch.setText((simulateGate) ? R.string.settings_enabled : R.string.settings_disabled);
        gateSimulationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SettingConstants.SIMULATE_GATE, isChecked);
                editor.apply();
                gateSimulationSwitch.setText((isChecked) ? R.string.settings_enabled : R.string.settings_disabled);
            }
        });
    }
}
