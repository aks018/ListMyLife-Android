package com.listmylife.avita.listmylife;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by avita on 8/2/2017.
 */

public class AddingFriendsForMessageAdapter extends ArrayAdapter {



    private ArrayList dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        CheckBox checkBox;
    }

    public AddingFriendsForMessageAdapter(ArrayList data, Context context) {
        super(context, com.listmylife.avita.listmylife.R.layout.messages_layout, data);
        this.dataSet = data;
        this.mContext = context;

    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public DataModel getItem(int position) {
        return (DataModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(com.listmylife.avita.listmylife.R.layout.messages_layout, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(com.listmylife.avita.listmylife.R.id.code);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(com.listmylife.avita.listmylife.R.id.checkBox1);

            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        DataModel item = getItem(position);


        viewHolder.txtName.setText(item.name);
        viewHolder.checkBox.setChecked(item.checked);


        return result;
    }
}
