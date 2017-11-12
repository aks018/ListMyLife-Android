package com.listmylife.avita.listmylife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;

public class Chat_Class extends BaseAdapter{
    ArrayList usernames;
    ArrayList messages;
    ArrayList dates;
    Context context;
    ArrayList<Boolean> getCurrentUsers;
    ArrayList<RequestCreator> images;
    private static LayoutInflater inflater=null;
    public Chat_Class(Context mainActivity, ArrayList username, ArrayList message, ArrayList isCurrent) {
        // TODO Auto-generated constructor stub
        usernames=username;
        context=mainActivity;
        messages = message;
        getCurrentUsers = isCurrent;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return usernames.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        TextView tv2;
        ImageView img;
        TextView tv3;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        if(getCurrentUsers.get(position)==true) {
            rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.my_custom_image_text_list_view, null);
            holder.tv = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView1);
            holder.tv2 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView2);
            //holder.tv3 = (TextView) rowView.findViewById(R.id.textView13);
            holder.tv.setText(usernames.get(position).toString());
            holder.tv2.setText(messages.get(position).toString());
            //holder.tv3.setText("Sent at: " + dates.get(position).toString());

            return rowView;
        }
        /*if(messages.get(position-1).toString().equals(messages.get(position).toString())&& position>=1)
        {
            rowView = inflater.inflate(R.layout.my_custom_list_view_right_continue, null);
            holder.tv = (TextView) rowView.findViewById(R.id.textView1);
            holder.tv.setText(usernames.get(position).toString());

            return rowView;
        }*/
        rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.my_custom_list_view_right, null);
        holder.tv = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView1);
        //holder.img = (ImageView) rowView.findViewById(R.id.imageView1);
        holder.tv2 = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView2);
        //holder.tv3 = (TextView) rowView.findViewById(R.id.textView15);
        holder.tv.setText(usernames.get(position).toString());
        holder.tv2.setText(messages.get(position).toString());
        //holder.tv3.setText("Sent at: " + dates.get(position).toString());


        return rowView;
    }


}