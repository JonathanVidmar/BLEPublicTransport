package com.lth.thesis.blepublictransport.Fragments;

import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass
 * It should be extended by fragments wanting to observe the
 * applications update methods.
 *
 * @author      Jacob Arvidsson
 * @version     1.1                 (current version number of program)
 */
public abstract class ObserverFragment extends Fragment {

    public ObserverFragment() {
        // Required empty public constructor
    }

    public abstract void update(Object data);
}
