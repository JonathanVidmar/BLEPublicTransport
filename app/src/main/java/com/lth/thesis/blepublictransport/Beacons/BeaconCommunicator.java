package com.lth.thesis.blepublictransport.Beacons;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jonathan on 2/12/2016.
 */
public class BeaconCommunicator extends Observable {

    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        setChanged();
        super.notifyObservers(data);
    }
}
