package com.lth.thesis.blepublictransport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jacobarvidsson on 11/02/16.
 */
public class NearObjectListViewAdapter extends BaseAdapter {
    private static ArrayList<String> objecList;

    private LayoutInflater mInflater;

    public NearObjectListViewAdapter(Context fragment, ArrayList<String> results){
        objecList = results;
        mInflater = LayoutInflater.from(fragment);
    }

    public void updateList(ArrayList<String> list) {
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
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_row_view, null);
            holder = new ViewHolder();
            holder.txtname = (TextView) convertView.findViewById(R.id.row_label);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtname.setText(objecList.get(position));

        return convertView;
    }

    static class ViewHolder{
        TextView txtname;
    }
}