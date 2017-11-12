package com.listmylife.avita.listmylife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by avita on 7/19/2017.
 */

public class MessagesListView extends BaseAdapter {
    ArrayList getTitles;
    ArrayList getMessages;
    ArrayList getTimes;
    ArrayList getUsers;
    Context context;
    private static LayoutInflater inflater=null;

    public MessagesListView(Context mainActivity, ArrayList getTitle, ArrayList getMessage, ArrayList getTime, ArrayList getUser)
    {
        context = mainActivity;
        getTitles = getTitle;
        getMessages = getMessage;
        getTimes = getTime;
        getUsers = getUser;


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
    public class Holder{
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.message_layout, null);
        holder.textView1 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewTitle);
        holder.textView2 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewMessage);
        holder.textView3 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewTime);
        holder.textView4 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textViewUsername);


        holder.textView1.setText(getTitles.get(i).toString());
        holder.textView2.setText(getMessages.get(i).toString());
        holder.textView3.setText(getTimes.get(i).toString());
        holder.textView4.setText(getUsers.get(i) + ": ");




        return rowView;

    }
}
