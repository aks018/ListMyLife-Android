package com.listmylife.avita.listmylife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by avita on 7/22/2017.
 */

public class New_List_Adapter extends BaseAdapter {
    ArrayList getItems;
    Context context;
    private static LayoutInflater inflater=null;
    public New_List_Adapter(Context mainActivity, ArrayList getItem)
    {
        getItems = getItem;
        context = mainActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return getItems.size();
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
        ImageButton button;
        //ImageView imageView;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.new_list_adapter, null);
        holder.tv=  (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView20);
        holder.button = (ImageButton) rowView.findViewById(com.listmylife.avita.listmylife.R.id.buttonDelete);


        holder.tv.setText(getItems.get(i).toString());
        holder.button.setTag(getItems.get(i).toString());





        return rowView;
    }

}
