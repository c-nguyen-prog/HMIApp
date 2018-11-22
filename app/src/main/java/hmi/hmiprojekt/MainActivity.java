package hmi.hmiprojekt;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;

import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.TripComponents.Trip;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mainFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        initRecycler();

    }

    private void initRecycler() {
        // create RecyclerView-Object and set LayoutManager
        RecyclerView mainRecycler = findViewById(R.id.recyclerview_main);
        mainRecycler.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mainRecycler.setHasFixedSize(true); // always size matching constraint

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        // read in the trips that are going to be showed on RecyclerView
        Trip[] trips = new Trip[0];
        try {
            trips = TripReader.readTrips();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext()
                    , "Unable to read your data"
                    , Toast.LENGTH_SHORT).show();
        }

        // create Adapter and fill it with Trips
        TripAdapter tripAdapter = new TripAdapter(trips);
        mainRecycler.setAdapter(tripAdapter);
        tripAdapter.notifyDataSetChanged();
    }
}
