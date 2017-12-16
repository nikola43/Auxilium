package com.pinillos.auxilium.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinillos.auxilium.R;
import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

public class IncidenceOnClickDialog extends BlurDialogFragment {

    public static IncidenceOnClickDialog newInstance(String incidenceType, String incidenceTimestamp) {
        IncidenceOnClickDialog frag = new IncidenceOnClickDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("incidenceType", incidenceType);
        args.putString("incidenceTimestamp", incidenceTimestamp);
        frag.setArguments(args);
        return frag;
    }

    // implement either onCreateView or onCreateDialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String incidenceTypeText = getArguments().getString("incidenceType");
        String incidenceTimestampText = getArguments().getString("incidenceTimestamp");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.incidence_list_onclick, null);

        TextView incidenceType = view.findViewById(R.id.incidence_type);
        incidenceType.setText(incidenceTypeText);

        TextView creationDate = view.findViewById(R.id.incidence_creation_date);
        creationDate.setText(incidenceTimestampText);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
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