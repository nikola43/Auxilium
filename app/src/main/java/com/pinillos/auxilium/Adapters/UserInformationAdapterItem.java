package com.pinillos.auxilium.Adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinillos.auxilium.R;
import com.pinillos.auxilium.model.UserInformation;

import java.util.ArrayList;

public class UserInformationAdapterItem extends BaseAdapter {

    protected Activity activity;
    protected ArrayList<UserInformation> items;

    public UserInformationAdapterItem(Activity activity, ArrayList<UserInformation> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public void addAll(ArrayList<UserInformation> category) {
        for (int i = 0; i < category.size(); i++) {
            items.add(category.get(i));
        }
    }

    @Override
    public Object getItem(int arg0) {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.users_list_view_layout, null);
        }

        UserInformation userInformation = items.get(position);

        TextView title = (TextView) v.findViewById(R.id.user_group);
        title.setText(userInformation.getCategory());

        TextView description = (TextView) v.findViewById(R.id.user_fname);
        description.setText(userInformation.getFirstName());

        ImageView imagen = (ImageView) v.findViewById(R.id.user_item);
        imagen.setImageDrawable(userInformation.getImagen());

        return v;
    }
}