package hmi.hmiprojekt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
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
import hmi.hmiprojekt.TripComponents.Trip;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location>
        , NewTripDialog.NewTripDialogListener {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private final static int PERMISSION_REQUEST_LOCATION = 200;
    private final static int PERMISSION_WRITE_EXTERNAL_STORAGE = 300;
    private LocationHelper locationHelper;
    private String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mainFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewTripDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            initRecycler();
        }
    }

    private void initRecycler() {
        // create RecyclerView-Object and set LayoutManager
        RecyclerView mainRecycler = findViewById(R.id.recyclerview_main);
        mainRecycler.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mainRecycler.setHasFixedSize(true); // always size matching constraint

        // read in the trips that are going to be showed on RecyclerView
        //TODO put in own method and use it in onResume instead of init method
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

    private void startTrip() {
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
                startTrip();
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
                        startTrip();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                locationHelper.handlePermissionRequestResult(this, grantResults);
            }
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initRecycler();
                } else {
                    Toast.makeText(getBaseContext()
                            , "Unable to read your data"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onSuccess(Location location) {

        if (location != null) {

            Intent intent = new Intent(MainActivity.this, RecordTripActivity.class);
            intent.putExtra("startPosition", new LatLng( location.getLatitude(), location.getLongitude()));
            intent.putExtra("tripName", tripName);
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext()
                    , "Position error pls try again"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    // gets tripName from NewTripDialog
    @Override
    public void returnTripName (String tripName) {
        if (tripName != null && tripName.length() >= 1) {
            this.tripName = tripName;
            checkLocationSetting();
        }
    }
}
