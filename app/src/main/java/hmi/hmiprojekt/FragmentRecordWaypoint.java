package hmi.hmiprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES10;
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

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

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


        // This rotates the picture to show it in the correct orientation
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


        // This scales the picture to fit the imageView while maintaining the right aspect ratio

        int maxSize = getMaxTextureSize();
        if(bitmap.getWidth() > maxSize || bitmap.getHeight() > maxSize ){
            Log.e("SCALE: ", "Image was scaled");
            float scale = Math.min(((float) 300 / bitmap.getWidth()), ((float) 300 / bitmap.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        // set modified picture to imageView
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

    //https://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap
    public static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }

}
