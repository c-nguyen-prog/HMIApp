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

public class NewEditDescDialog extends AppCompatDialogFragment {
    private EditText editTextEditDescription;
    private NewEditDescDialog.NewEditDescDialogListener listener;
    private String title = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (NewEditDescDialog.NewEditDescDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString() +
                    "must implement NewDescListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // build view
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_desc, null);
        builder.setView(view)
                .setTitle("Beschreibung ändern")
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String desc = editTextEditDescription.getText().toString();
                        listener.returnDesc(desc);
                    }
                });

        editTextEditDescription = view.findViewById(R.id. editText_edit_desc);
        editTextEditDescription.setText(title);
        return builder.create();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public interface NewEditDescDialogListener {
        void returnDesc(String desc);
    }
}
