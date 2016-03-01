package com.lth.thesis.blepublictransport.Beacons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

/**
 * Created by jacobarvidsson on 11/02/16.
 */
public class NearObjectListViewAdapter extends BaseAdapter {
    private static ArrayList<Beacon> objecList;

    private LayoutInflater mInflater;
    private BLEPublicTransport app;

    public NearObjectListViewAdapter(Context fragment, ArrayList<Beacon> results){
        objecList = results;
        mInflater = LayoutInflater.from(fragment);
        app = (BLEPublicTransport) fragment.getApplicationContext();
    }

    public void updateList(ArrayList<Beacon> list) {
        objecList = list;
    }

    @Override
    public int getCount() {
        return objecList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return objecList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        BeaconHelper helper = app.beaconHelper;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_row_view, null);
            holder = new ViewHolder();
            holder.txtname = (TextView) convertView.findViewById(R.id.row_label);
            holder.destinationName = (TextView) convertView.findViewById(R.id.destinationLabel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtname.setText(helper.getBeaconName(objecList.get(position).getId2()));
        holder.destinationName.setText(helper.getDistanceText(objecList.get(position)));

        return convertView;
    }

    static class ViewHolder{
        TextView txtname;
        TextView destinationName;
    }
}