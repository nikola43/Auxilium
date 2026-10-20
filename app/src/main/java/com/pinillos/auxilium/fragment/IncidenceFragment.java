package com.pinillos.auxilium.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pinillos.auxilium.activity.MainActivity;
import com.pinillos.auxilium.dialog.IncidenceOnClickDialog;
import com.pinillos.auxilium.model.Incidence;
import com.pinillos.auxilium.adapter.IncidenceAdapterItem;
import com.pinillos.auxilium.R;
import java.util.ArrayList;

public class IncidenceFragment extends Fragment {

    private static final String TAG = "IncidenceFragment";
    private DatabaseReference firebaseDatabaseReference;
    private ValueEventListener firebaseDatabaseListener;
    private View view;

    public IncidenceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_incidence, container, false);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (firebaseDatabaseListener != null) {
            firebaseDatabaseReference.removeEventListener(firebaseDatabaseListener);
        }
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        //Add firebase listener
        addFirebaseListener();
    }

    public void showIncidenceInfo(Incidence incidence) {
        IncidenceOnClickDialog.newInstance(incidence.getType(), incidence.getTimestamp()).show(getActivity().getFragmentManager(), TAG);
    }

    public void addFirebaseListener()
    {
        firebaseDatabaseReference = MainActivity.firebaseDatabaseReference;

        firebaseDatabaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Incidence> incidenceArrayList = new ArrayList<>();
                Drawable drawable;

                for (DataSnapshot dbObjects : dataSnapshot.getChildren()) {
                    for (DataSnapshot currentUser : dbObjects.getChildren()) {
                        for (DataSnapshot currentItem : currentUser.getChildren()) {
                            for (DataSnapshot currentField : currentItem.getChildren()) {
                                if (currentItem.getKey().equals("incidence")) {

                                    Incidence incidence = currentField.getValue(Incidence.class);

                                    if (incidence != null) {
                                        if (incidence.getType() != null && incidence.getStatus()) {
                                            switch (incidence.getType()) {
                                                case "Riesgo por incendio urbano y explosión":
                                                    drawable = view.getResources().getDrawable(R.mipmap.ic_incendio_round);
                                                    break;
                                                case "Riesgo por fallos en los suministros básicos":
                                                    drawable = view.getResources().getDrawable(R.mipmap.ic_electrico_round);
                                                    break;
                                                default:
                                                    drawable = view.getResources().getDrawable(R.mipmap.ic_incendio_round);
                                                    break;
                                            }
                                            incidence.setImagen(drawable);
                                            incidenceArrayList.add(incidence);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ListView lv = view.findViewById(R.id.incidence_listView);
                IncidenceAdapterItem adapter = new IncidenceAdapterItem(getActivity(), incidenceArrayList);

                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Incidence incidence = (Incidence) parent.getItemAtPosition(position);
                        showIncidenceInfo(incidence);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getActivity().getApplicationContext(), "Failed to alter database.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        };

        firebaseDatabaseReference.addValueEventListener(firebaseDatabaseListener);
    }
}
