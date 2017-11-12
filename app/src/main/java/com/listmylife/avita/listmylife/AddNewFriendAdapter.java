package com.listmylife.avita.listmylife;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avita on 7/31/2017.
 */

public class AddNewFriendAdapter extends BaseAdapter implements Filterable{
    ArrayList getUsers;
    ArrayList isFriend;
    Context context;
    ArrayList objects;
    private static LayoutInflater inflater = null;

    public AddNewFriendAdapter(Context mainActivity, ArrayList getItem, ArrayList isFriends) {
        getUsers = getItem;
        isFriend = isFriends;
        context = mainActivity;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return getUsers.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class Holder {
        TextView tv;
        ImageButton button;
        //ImageView imageView;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        AddNewFriendAdapter.Holder holder = new AddNewFriendAdapter.Holder();
        View rowView;
        if(isFriend.get(i).equals("friends")) {
            rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.is_friends_already, null);
            holder.tv = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView);
            holder.button = (ImageButton) rowView.findViewById(com.listmylife.avita.listmylife.R.id.button3);

            holder.tv.setText(getUsers.get(i).toString());
            holder.button.setTag(getUsers.get(i).toString());

            return rowView;
        }
        else if(isFriend.get(i).equals("sent"))
        {
            rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.is_friend_request_sent, null);
            holder.tv = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView);
            holder.button = (ImageButton) rowView.findViewById(com.listmylife.avita.listmylife.R.id.button3);

            holder.tv.setText(getUsers.get(i).toString());
            holder.button.setTag(getUsers.get(i).toString());

            return rowView;
        }
        else
        {
            rowView = inflater.inflate(com.listmylife.avita.listmylife.R.layout.is_not_friends_already, null);
            holder.tv = (TextView) rowView.findViewById(com.listmylife.avita.listmylife.R.id.textView);
            holder.button = (ImageButton) rowView.findViewById(com.listmylife.avita.listmylife.R.id.button3);

            holder.tv.setText(getUsers.get(i).toString());
            holder.button.setTag(getUsers.get(i).toString());

            return rowView;
        }
    }

    Filter myFilter = new Filter() {

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            System.out.println("Constraint " + constraint);
            Log.d("-----------", "publishResults");
            if (results.count > 0 && results != null) {
                getUsers = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            } else {
                getUsers.clear();
                notifyDataSetInvalidated();
            }

        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("-----------", "performFiltering");
            FilterResults results = new FilterResults();
            List<String> filteredArrList = new ArrayList<String>();
            if (objects == null) {
                objects = new ArrayList<String>(getUsers);
            }
            //Locale locale = Locale.getDefault();
            if (constraint == null || constraint.length() == 0) {
                // set the Original result to return
                results.count = objects.size();
                results.values = objects;
                Log.d("TESTING_FILTER",results.values.toString());

                return results;

            } else {
                constraint = (String) constraint.toString().toLowerCase();
                Log.d("TESTING_FILTER",constraint.toString());

                for (int i = 0; i < objects.size(); i++) {
                    String name = objects.get(i).toString();
                    // System.out.println(name);
                    Log.d("TESTING_FILTER_NAME", name.toUpperCase().toString());
                    Log.d("TESTING_FILTER_CON", constraint.toString().toUpperCase());

                    if(name.toUpperCase().toString().startsWith(constraint.toString().toUpperCase()))
                    {
                        filteredArrList.add(name);
                    }
                }
                Log.d("TESTING_FILTERING",filteredArrList.toString());
                // set the Filtered result to return
                results.count = filteredArrList.size();
                results.values = filteredArrList;
                Log.d("TESTING_FILTER",results.values.toString());

                return results;
            }

        }

    };
    @Override
    public Filter getFilter() {
        return myFilter;
    }

}
