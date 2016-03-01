package com.lth.thesis.blepublictransport.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class ObserverFragment extends Fragment {


    public ObserverFragment() {
        // Required empty public constructor
    }

    public abstract void update(Object data);
}
