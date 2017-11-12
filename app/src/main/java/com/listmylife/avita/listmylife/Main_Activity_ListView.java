package com.listmylife.avita.listmylife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by avita on 7/9/2017.
 */

public class Main_Activity_ListView extends BaseAdapter {
    ArrayList getTitles;
    ArrayList getDescriptions;
    ArrayList getDates;
    Context context;
    private static LayoutInflater inflater=null;

    public Main_Activity_ListView(Context mainActivity, ArrayList getTitle, ArrayList getDescription, ArrayList getDate)
    {
        context = mainActivity;
        getTitles = getTitle;
        getDescriptions = getDescription;
        getDates = getDate;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return getTitles.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public class Holder
    {
        TextView tv;
        TextView tv2;
        TextView tv3;
        //Button imageButton;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.main_activity_listview, null);
        holder.tv=  (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewTitle);
        holder.tv2 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewDescription);
        holder.tv3 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewDate);
        //holder.imageButton = (Button) rowView.findViewById(R.id.imageButton3);

        holder.tv.setText(getTitles.get(position).toString());
        holder.tv2.setText(getDescriptions.get(position).toString());
        holder.tv3.setText(getDates.get(position).toString());
        //holder.imageButton.setTag(position);

        return rowView;
    }
}
