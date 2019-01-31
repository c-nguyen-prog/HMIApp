package hmi.hmiprojekt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;

import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.TripComponents.Waypoint;

public class ImageViewerActivity extends AppCompatActivity implements NewEditDescDialog.NewEditDescDialogListener {

    private ImageView mImageView;
    private TextView descView;
    Waypoint currWaypoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // will hide the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // will hide notification bar

        setContentView(R.layout.activity_image_viewer);


        Trip currTrip = null;
        try {
            currTrip = TripReader.readTrip((File)getIntent().getExtras().get("tripDir"));
        } catch (ParseException e) {
            Toast.makeText(getBaseContext()
                    , "Das Bild ist beschädigt"
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
        int index = getIntent().getExtras().getInt("waypointIndex");
        currWaypoint = currTrip.getWaypoints().get(index);

        descView = findViewById(R.id.desc);
        descView.setText(currWaypoint.getDesc());

        TextView name = findViewById(R.id.name);
        name.setText(currWaypoint.getName());

        Bitmap myBitmap;

        if(currWaypoint.getImg().exists()) {

            myBitmap = BitmapFactory.decodeFile(currWaypoint.getImg().getAbsolutePath());
            mImageView = findViewById(R.id.imageView);

            mImageView.setImageBitmap(myBitmap);

            int previousIndex = index-1;
            int nextIndex = index+1;

            final Trip finalTrip = currTrip;
            final Context currContext = this;

            mImageView.setOnTouchListener(new OnSwipeTouchListener(getBaseContext(), this) {
               public void onSwipeTop() {
                    //Toast.makeText(currContext, "top", Toast.LENGTH_SHORT).show();
                }
                public void onSwipeRight() {
                    if (previousIndex < 0) return;
                    viewOtherImage(previousIndex);
                }
                public void onSwipeLeft() {
                    if (nextIndex >= finalTrip.getWaypoints().size()) return;
                    viewOtherImage(nextIndex);
                }
                public void onSwipeBottom() {
                    //Toast.makeText(currContext, "bottom", Toast.LENGTH_SHORT).show();
                }
                private void viewOtherImage(int index) {
                    Intent intent = new Intent(currContext, ImageViewerActivity.class);
                    intent.putExtra("tripDir", finalTrip.getDir());
                    intent.putExtra("waypointIndex", index);
                    finish();
                    startActivity(intent);
                }

            });
        }


        ImageButton editButton = findViewById(R.id.imageButton);
        editButton.setOnClickListener(view -> showNewEditDescDialog());
    }

    private void showNewEditDescDialog() {
        NewEditDescDialog dialog = new NewEditDescDialog();
        dialog.setTitle(currWaypoint.getDesc());
        dialog.show(getSupportFragmentManager(), currWaypoint.getDesc());
    }

    public void hide(View view) {
        FrameLayout fLay = findViewById(R.id.frameLayout);

        if (fLay.getVisibility() == View.VISIBLE)
            fLay.setVisibility(View.INVISIBLE);
        else
            fLay.setVisibility(View.VISIBLE);

        TextView desc = findViewById(R.id.desc);

        if (desc.getVisibility() == View.VISIBLE)
            desc.setVisibility(View.INVISIBLE);
        else
            desc.setVisibility(View.VISIBLE);

        TextView name = findViewById(R.id.name);

        if (name.getVisibility() == View.VISIBLE)
            name.setVisibility(View.INVISIBLE);
        else
            name.setVisibility(View.VISIBLE);

        ImageButton imgBtn = findViewById(R.id.imageButton);

        if(imgBtn.getVisibility() == View.VISIBLE)
            imgBtn.setVisibility(View.INVISIBLE);
        else
            imgBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void returnDesc(String desc) {
        if (desc != null) {
            currWaypoint.setDesc(desc);
            descView.setText(desc);
        }
    }
}
