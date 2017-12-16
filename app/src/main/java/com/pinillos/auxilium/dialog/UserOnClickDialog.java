package com.pinillos.auxilium.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinillos.auxilium.R;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

public class UserOnClickDialog extends BlurDialogFragment {

    public static UserOnClickDialog newInstance(String category, String group, String firstName, String lastName) {
        UserOnClickDialog frag = new UserOnClickDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putString("group", group);
        args.putString("firstName", firstName);
        args.putString("lastName", lastName);

        frag.setArguments(args);
        return frag;
    }

    // implement either onCreateView or onCreateDialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String category = getArguments().getString("category");
        String group = getArguments().getString("group");
        String firstName = getArguments().getString("firstName");
        String lastName = getArguments().getString("lastName");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.users_list_onclick, null);

        TextView userCategory = view.findViewById(R.id.user_category);
        userCategory.setText(category);

        TextView userGroup = view.findViewById(R.id.user_group);
        userGroup.setText(group);

        TextView userFname = view.findViewById(R.id.user_fname);
        userFname.setText(firstName);

        TextView userLname = view.findViewById(R.id.user_lname);
        userLname.setText(lastName);

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
        return builder.create();
    }
}