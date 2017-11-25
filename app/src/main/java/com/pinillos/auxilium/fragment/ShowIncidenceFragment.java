package com.pinillos.auxilium;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pinillos.auxilium.model.Position;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowIncidenceFragment extends Fragment {

    private static final String TAG = "ShowIncidenceFragment";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;

    public ShowIncidenceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incidence, container, false);


    }

    @Override
    public void onActivityCreated(Bundle state) {

        super.onActivityCreated(state);




        //Add firebase event listener
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


        firebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Incidence> incidenceArrayList = new ArrayList<>();
                Drawable drawable;

                Position position = null;
                //retrieve data from database
                for (DataSnapshot dbObjects : dataSnapshot.getChildren()) {
                    String objectType = dbObjects.getKey();
                    //toastMessage(objectType);

                    for (DataSnapshot currentUser : dbObjects.getChildren()) {
                        final String userID = currentUser.getKey();
                        //toastMessage(userID);

                        for (DataSnapshot currentItem : currentUser.getChildren()) {

                            String itemType = currentItem.getKey();
                            //toastMessage(itemType);


                            for (DataSnapshot currentField : currentItem.getChildren()) {

                                //if retrieved database object are incident type
                                if (itemType.equals("incidence")) {
                                    //retrieve incidence object from database
                                    String incidenceID = currentField.getKey();
                                    Incidence incidence = currentField.getValue(Incidence.class);

                                    //Resources res = getResources();
                                    //final String[] incidenceTypes = res.getStringArray(R.array.incidenceType_array);

                                    //according to incidence type, shows different icon

                                    if ( incidence != null )
                                    {
                                        String incidenceType = incidence.getType();

                                        if (incidenceType != null)
                                        {
                                            switch (incidenceType) {
                                                case "Riesgo por incendio urbano y explosión":
                                                    drawable = getResources().getDrawable( R.mipmap.ic_incendio_round );
                                                    break;
                                                case "Riesgo por fallos en los suministros básicos":
                                                    drawable = getResources().getDrawable( R.mipmap.ic_electrico_round );
                                                    break;
                                                default:
                                                    drawable = getResources().getDrawable( R.mipmap.ic_incendio_round );
                                                    break;
                                            }
                                            incidence.setImagen(drawable);

                                            //if the incidence is activated, add google maps marker
                                            if (incidence.getStatus()) {
                                                incidenceArrayList.add(incidence);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ListView lv = getView().findViewById(R.id.incidence_listView);
                IncidenceAdapterItem adapter = new IncidenceAdapterItem(getActivity(),incidenceArrayList);

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

                Toast.makeText(getActivity().getApplicationContext(),"Failed to alter database.",Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void showIncidenceInfo(Incidence incidence)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Incidence creada por:" +
                "Tipo: " + incidence.getType() + "\n" +
                "Estado: " + incidence.getStatus()+ "\n" +
                "Fecha/hora: " + incidence.getTimestamp() + "\n" +
                "Creador: " + incidence.getCreatorID()+ "\n")
                .setPositiveButton("Asignar usuarios", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }
}
