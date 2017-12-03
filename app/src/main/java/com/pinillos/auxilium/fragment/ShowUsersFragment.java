package com.pinillos.auxilium.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.activity.MainActivity;
import com.pinillos.auxilium.model.UserInformation;
import com.pinillos.auxilium.Adapters.UserInformationAdapterItem;
import com.pinillos.auxilium.model.Position;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShowUsersFragment extends Fragment {

    private static final String TAG = "ShowUsersFragment";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private View view;
    private ValueEventListener firebaseDatabaseListener;

    public ShowUsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_show_users, container, false);



        return view;


    }

    @Override
    public void onActivityCreated(Bundle state) {

        super.onActivityCreated(state);


        //Add firebase event listener
        firebaseDatabase = MainActivity.firebaseDatabase;
        firebaseDatabaseReference = MainActivity.firebaseDatabaseReference;

        firebaseDatabaseListener = firebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<UserInformation> userInformationArrayList = new ArrayList<>();
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


                            if (itemType.equals("information")) {
                                UserInformation userInformation = currentItem.getValue(UserInformation.class);

                                String userCategory = userInformation.getCategory();

                                if ( userInformation != null && userInformation.getCategory() != null)
                                {
                                    switch (userCategory) {
                                        case "Medico":
                                            drawable = getResources().getDrawable(R.mipmap.ic_medical_round);
                                            break;
                                        case "Bombero":
                                            drawable = getResources().getDrawable(R.mipmap.ic_fireman_round);
                                            break;
                                        default:
                                            drawable = getResources().getDrawable(R.mipmap.ic_incendio_round);
                                            break;
                                    }
                                    userInformation.setImagen(drawable);

                                    userInformationArrayList.add(userInformation);
                                }
                            }
                        }
                    }
                }

                ListView lv = view.findViewById(R.id.user_listView);
                UserInformationAdapterItem adapter = new UserInformationAdapterItem(getActivity(), userInformationArrayList);

                lv.setAdapter(adapter);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        UserInformation u = (UserInformation) parent.getItemAtPosition(position);
                        showUserInfo(u);
                    }
                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getActivity().getApplicationContext(), "Failed to alter database.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (firebaseDatabaseListener != null) {
            firebaseDatabaseReference.removeEventListener(firebaseDatabaseListener);
        }
    }

    public void showUserInfo(UserInformation userInformation)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.users_list_onclick, null);

        TextView userCategory = view.findViewById(R.id.user_category);
        userCategory.setText(userInformation.getCategory());

        TextView userGroup = view.findViewById(R.id.user_group);
        userGroup.setText(userInformation.getGroup());

        TextView userFname = view.findViewById(R.id.user_fname);
        userFname.setText(userInformation.getFirstName());

        TextView userLname = view.findViewById(R.id.user_lname);
        userLname.setText(userInformation.getLastName1());

        builder.setView(view.getRootView())
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.show();
    }

}
