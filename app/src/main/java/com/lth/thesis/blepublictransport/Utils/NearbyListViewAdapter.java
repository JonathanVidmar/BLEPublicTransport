package com.lth.thesis.blepublictransport.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lth.thesis.blepublictransport.Beacons.BeaconHelper;
import com.lth.thesis.blepublictransport.Beacons.PublicTransportBeacon;
import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Models.Train;
import com.lth.thesis.blepublictransport.R;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

/**
 * Created by jacobarvidsson on 11/02/16.
 */
public class NearbyListViewAdapter extends BaseAdapter {
    private static ArrayList<PublicTransportBeacon> nearbyList;
    private static ArrayList<Train> arrivalList;
    private static boolean showsNearby = true;

    private LayoutInflater mInflater;

    public NearbyListViewAdapter(Context fragment, ArrayList<PublicTransportBeacon> results){
        nearbyList = results;
        mInflater = LayoutInflater.from(fragment);
    }

    public void showNearby(boolean show){
        showsNearby = show;
    }

    public void updateList(ArrayList<PublicTransportBeacon> list) {
        nearbyList = list;
    }

    public void updateArrivalsList(ArrayList<Train> list) { arrivalList = list; }

    @Override
    public int getCount() {
        if(showsNearby){
            return nearbyList.size();
        }else{
            return arrivalList.size();
        }
    }

    @Override
    public Object getItem(int arg0) {
        if(showsNearby){
            return nearbyList.get(arg0);
        }else{
            return arrivalList.get(arg0);
        }
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_row_view, null);
            holder = new ViewHolder();
            holder.txtname = (TextView) convertView.findViewById(R.id.row_label);
            holder.destinationName = (TextView) convertView.findViewById(R.id.destinationLabel);
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_img);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(showsNearby){
            holder.txtname.setText(nearbyList.get(position).getName());
            holder.destinationName.setText(BeaconHelper.getDistanceText(nearbyList.get(position)));
            holder.imageView.setImageResource(nearbyList.get(position).getImage());
        }else{
            Train t = arrivalList.get(position);
            String header = t.nextArrival + " - " + t.name;
            holder.txtname.setText(header);
            String detailLabel = "Train leaves from track " + t.track;
            holder.destinationName.setText(detailLabel);
            holder.imageView.setImageResource(R.drawable.icon_tracks);
        }
        return convertView;
    }

    static class ViewHolder{
        TextView txtname;
        TextView destinationName;
        ImageView imageView;
    }
}