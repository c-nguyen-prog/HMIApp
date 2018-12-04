package hmi.hmiprojekt;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import hmi.hmiprojekt.Location.LocationHelper;
import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.MemoryAccess.TripWriter;
import hmi.hmiprojekt.TripComponents.Trip;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location>
        , NewTripDialog.NewTripDialogListener {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private LocationHelper locationHelper;
    private Trip newTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(this);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        findViewById(R.id.mainFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTrip();
                checkLocationSetting();
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

    // creates a new Trip and writes it, if "ok" is pressed
    private void createTrip() {
        showNewTripDialog();
        locationHelper.startLocationRequest(this);
    }

    private void showNewTripDialog() {
        NewTripDialog tripDialog = new NewTripDialog();
        tripDialog.show(getSupportFragmentManager(), "new trip dialog");
    }

    protected void checkLocationSetting() {

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(new LocationRequest())
                .build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(settingsRequest);

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied.
                createTrip();
            }
        });

        //OnFailure ask User to change settings
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    //Location Service is off
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (Exception sendEx) {
                        // Ignore error
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        createTrip();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MainActivity.this, "Cannot start trip without location", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onSuccess(Location location) {

        if (location != null) {
            Intent intent = new Intent(MainActivity.this, RecordTripActivity.class);
            intent.putExtra("startPosition", new LatLng( location.getLatitude(), location.getLongitude()));
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext()
                    , "Position error pls try again"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    // gets tripName from NewTripDialog
    @Override
    public void writeTripDir(String tripName) {
        if (tripName != null && tripName.length() >= 1) {
            try {
                newTrip = new Trip(tripName);
                TripWriter.createTripDir(newTrip);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
