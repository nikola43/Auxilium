package com.pinillos.auxilium.fragment;


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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.activity.MainActivity;
import com.pinillos.auxilium.dialog.UserOnClickDialog;
import com.pinillos.auxilium.model.UserInformation;
import com.pinillos.auxilium.adapter.UserInformationAdapterItem;
import com.pinillos.auxilium.model.Position;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private static final String TAG = "UsersFragment";
    private DatabaseReference firebaseDatabaseReference;
    private ValueEventListener firebaseDatabaseListener;
    private View view;

    public UsersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_show_users, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        addFirebaseListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (firebaseDatabaseListener != null) {
            firebaseDatabaseReference.removeEventListener(firebaseDatabaseListener);
        }
    }

    public void showUserInfo(UserInformation userInformation) {
        UserOnClickDialog.newInstance(userInformation.getCategory(), userInformation.getGroup(),
                userInformation.getFirstName(), userInformation.getLastName2()).show(getActivity().getFragmentManager(), TAG);
    }

    private void addFirebaseListener() {
        firebaseDatabaseReference = MainActivity.firebaseDatabaseReference;
        firebaseDatabaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<UserInformation> userInformationArrayList = new ArrayList<>();
                Drawable drawable;

                for (DataSnapshot dbObjects : dataSnapshot.getChildren()) {
                    for (DataSnapshot currentUser : dbObjects.getChildren()) {
                        for (DataSnapshot currentItem : currentUser.getChildren()) {
                            if (currentItem.getKey().equals("information")) {

                                UserInformation userInformation = currentItem.getValue(UserInformation.class);

                                if ( userInformation != null && userInformation.getCategory() != null) {
                                    switch (userInformation.getCategory()) {
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
        };

        firebaseDatabaseReference.addValueEventListener(firebaseDatabaseListener);
    }
}
