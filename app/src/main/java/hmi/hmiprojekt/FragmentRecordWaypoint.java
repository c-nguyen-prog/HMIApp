package hmi.hmiprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class FragmentRecordWaypoint extends Fragment {

    private FragmentListener listener;
    private String pathToPicture;
    private EditText editName;
    private EditText editDesc;

    public interface FragmentListener {
        void onSaveWaypointListener(String name, String desc);
        void onDeleteWaypointListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_record_waypoint, container, false);

        ImageView imageView = v.findViewById(R.id.RecordedImageView);
        editName = v.findViewById(R.id.editName);
        editDesc = v.findViewById(R.id.editDesc);

        Bitmap bitmap = BitmapFactory.decodeFile(pathToPicture);

        // TODO clean up and investigate why image is turned in the first place
        // TODO when gallery app shows picture in right orientation ???

        try {
            ExifInterface exif = new ExifInterface(pathToPicture);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            // rotating bitmap
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (Exception e) {
            Log.e("BitMap Rotate Error: ", e.getMessage());
        }

        // TODO fix "Bitmap too large to be uploaded into a texture"
        imageView.setImageBitmap(bitmap);

        v.findViewById(R.id.btnSave).setOnClickListener(view -> {
            listener.onSaveWaypointListener(editName.getText().toString(), editDesc.getText().toString());

            //clear fields
            editName.getText().clear();
            editDesc.getText().clear();
        });

        v.findViewById(R.id.btnDelete).setOnClickListener(view -> {
            listener.onDeleteWaypointListener();

            //clear fields
            editName.getText().clear();
            editDesc.getText().clear();
        });

        return v;
    }

    public void setPicture(String pathToPicture) {
        this.pathToPicture = pathToPicture;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof FragmentListener){
            listener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
            + " Must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
