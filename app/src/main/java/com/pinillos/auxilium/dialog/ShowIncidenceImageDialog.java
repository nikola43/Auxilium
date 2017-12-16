package com.pinillos.auxilium.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.pinillos.auxilium.R;
import com.squareup.picasso.Picasso;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

public class ShowIncidenceImageDialog extends BlurDialogFragment {

    public static ShowIncidenceImageDialog newInstance(String url) {
        ShowIncidenceImageDialog frag = new ShowIncidenceImageDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("url", url);
        frag.setArguments(args);
        return frag;
    }

    // implement either onCreateView or onCreateDialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        final String imageUrl = getArguments().getString("url");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.show_marker_image, null);

        final ImageView imageView = view.findViewById(R.id.show_marker_image_view);


       final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Descargando imagen", "Espere por favor");



        Picasso.with(getActivity()).load(imageUrl).into(imageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                handler.sendMessage(handler.obtainMessage());
                dialog.dismiss();
            }

            @Override
            public void onError() {
                dialog.dismiss();
            }
        });


        try {
            Looper.loop();
        } catch (RuntimeException e) {
        }


        builder.setView(view.getRootView());
        return builder.create();
    }
}
