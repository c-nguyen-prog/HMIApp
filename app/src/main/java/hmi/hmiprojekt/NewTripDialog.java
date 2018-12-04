package hmi.hmiprojekt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import hmi.hmiprojekt.R;

public class NewTripDialog extends AppCompatDialogFragment {

    private EditText editTextTripName;
    private NewTripDialogListener listener;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (NewTripDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString() +
                    "must implement NewTripDialogListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // build view
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_trip, null);
        builder.setView(view)
                .setTitle("Creating a new Trip")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tripName = editTextTripName.getText().toString();
                        listener.writeTripDir(tripName);
                    }
                });

        editTextTripName = view.findViewById(R.id.editText_trip_name);
        return builder.create();
    }

    public interface NewTripDialogListener {
        void writeTripDir(String tripName);
    }
}
