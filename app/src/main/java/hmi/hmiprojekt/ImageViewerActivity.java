package hmi.hmiprojekt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // will hide the title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // will hide notification bar

        setContentView(R.layout.activity_image_viewer);

        Intent intent = getIntent();
        File imgFile = (File)getIntent().getExtras().get("picture");

        String desc = intent.getStringExtra("Description");
        TextView textView1 = findViewById(R.id.desc);
        textView1.setText(desc);

        String name = intent.getStringExtra("Name");
        TextView textView2 = findViewById(R.id.name);
        textView2.setText(name);

        Bitmap myBitmap;
        ImageView myImage;

        if(imgFile.exists()) {

            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myImage = findViewById(R.id.imageView);

            myImage.setImageBitmap(myBitmap);
        }
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
}
