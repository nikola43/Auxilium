package com.pinillos.auxilium.Others;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinillos.auxilium.R;
import com.pinillos.auxilium.model.Incidence;

import java.util.ArrayList;


public class IncidenceAdapterItem extends BaseAdapter {

    protected Activity activity;
    protected ArrayList<Incidence> items;

    public IncidenceAdapterItem(Activity activity, ArrayList<Incidence> items) {
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

    public void addAll(ArrayList<Incidence> category) {
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
            v = inf.inflate(R.layout.incidence_list_view_layout, null);
        }

        Incidence incidence = items.get(position);

        TextView title = (TextView) v.findViewById(R.id.nombre_incidencia);
        title.setText(incidence.getType());

        TextView description = (TextView) v.findViewById(R.id.fecha_hora);
        description.setText(incidence.getTimestamp());

        ImageView imagen = (ImageView) v.findViewById(R.id.incidence_item);
        imagen.setImageDrawable(incidence.getImage());

        return v;
    }
}