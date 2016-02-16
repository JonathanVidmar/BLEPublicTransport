package com.lth.thesis.blepublictransport;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingssssFragment extends Fragment {
    public static final String PREFS_NAME = "MyPrefsFile";
    private Switch mSwitch;
    private TextView switchStatus;


    public SettingssssFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mSwitch = (Switch) view.findViewById(R.id.priceOption);
        switchStatus = (TextView) view.findViewById(R.id.switchStatus);

        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        boolean silent = settings.getBoolean("dependantPayment", true); // false is default value

        mSwitch.setChecked(silent);

        //attach a listener to check for changes in state
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("dependantPayment", isChecked);
                editor.commit();
                if(isChecked){
                    switchStatus.setText("Prices depends on destination");
                }else{
                    switchStatus.setText("Prices are independent of destination");
                }
            }
        });

        //check the current state before we display the screen
        if(mSwitch.isChecked()){
            switchStatus.setText("Prices depends on destination");
        }
        else {
            switchStatus.setText("Prices are independent of destination");
        }

        return view;
    }

}
