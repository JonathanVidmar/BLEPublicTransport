package com.lth.thesis.blepublictransport.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.lth.thesis.blepublictransport.Config.SettingConstants;
import com.lth.thesis.blepublictransport.R;

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

        settings = getActivity().getSharedPreferences(SettingConstants.SETTINGS_PREFERENCES, 0);

        setUpDependentPriceSwitch();
        setUpAutomaticPaymentSwitch();
        setUpSubscriptionSwitch();

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
}