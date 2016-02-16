package com.lth.thesis.blepublictransport;


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
public class SettingsFragment extends Fragment {
    Switch mSwitch;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //switchStatus = (TextView) getActivity().findViewById(R.id.priceOption);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mSwitch = (Switch) view.findViewById(R.id.priceOption);

        //set the switch to ON
        //mSwitch.setChecked(true);
        //attach a listener to check for changes in state
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    //switchStatus.setText("Switch is currently ON");
                }else{
                    //switchStatus.setText("Switch is currently OFF");
                }

            }
        });

        return view;
    }

}
