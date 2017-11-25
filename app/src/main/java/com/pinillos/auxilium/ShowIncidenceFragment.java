package com.pinillos.auxilium;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowIncidenceFragment extends Fragment {

    private static final String TAG = "ShowIncidenceFragment";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private FloatingActionButton addUserToIncidenceButton;

    public ShowIncidenceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_incidence, container, false);

        // Inflate the layout for this fragment
        return rootView;

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
        TextView textView = getActivity().findViewById(R.id.incidence_name);
        toastMessage(textView.getText().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.incidence_onclick_layout, null))
        .setPositiveButton("Finalizar incidencia", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            // Send the positive button event back to the host activity
            //mListener.onDialogPositiveClick(NoticeDialogFragment.this);
        }
    })
            .setNegativeButton("Atrás", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the negative button event back to the host activity
                    //mListener.onDialogNegativeClick(NoticeDialogFragment.this);
                }
            });
        builder.show();
    }

    public void toastMessage(String message) {
        Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
