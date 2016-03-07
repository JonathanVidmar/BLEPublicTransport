package com.lth.thesis.blepublictransport.Fragments;

import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass
 * It should be extended by fragments wanting to observe the
 * applications update methods.
 *
 * @author      Jacob Arvidsson & Jonathan Vidmar
 * @version     1.1
 */
public abstract class AbstractObserverFragment extends Fragment {

    public AbstractObserverFragment() {
        // Required empty public constructor
    }

    public abstract void update(Object data);
}
