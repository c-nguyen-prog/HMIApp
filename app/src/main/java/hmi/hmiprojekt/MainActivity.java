package hmi.hmiprojekt;

import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.RelativeLayout;


import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;

import hmi.hmiprojekt.Connection.NearbyConnect;
import hmi.hmiprojekt.Connection.Zipper;
import hmi.hmiprojekt.Location.LocationHelper;
import hmi.hmiprojekt.MemoryAccess.Config;
import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.MemoryAccess.TripWriter;
import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.Welcome.Application;
import hmi.hmiprojekt.Welcome.Preference;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location>
        , NewTripDialog.NewTripDialogListener {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int REQUEST_VIEW_TRIP = 400;
    private static final int REQUEST_RECORD_TRIP = 700;
    private final static int PERMISSION_REQUEST_LOCATION = 200;
    private final static int PERMISSION_WRITE_EXTERNAL_STORAGE = 300;
    private final static int PERMISSION_BLUETOOTH_ADMIN = 500;
    private final static int PERMISSION_ACCESS_WIFI_STATE = 600;
    private LocationHelper locationHelper;
    private String tripName;
    private TripAdapter tripAdapter;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    WifiManager wifiManager;
    NearbyConnect connectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(this);
        setContentView(R.layout.activity_main);
        FloatingActionButton sendFAB = findViewById(R.id.sendFAB);

        sendFAB.setOnClickListener(v -> receiveTrip());

        findViewById(R.id.mainFab).setOnClickListener(view -> showNewTripDialog());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            initRecycler();

            initShowcaseTutorial();

            tripAdapter.setOnItemClickListener((position, v) -> {
                Trip clickedTrip = tripAdapter.getTrip(position);
                Intent intent = new Intent(MainActivity.this, ViewTripActivity.class);
                intent.putExtra("tripDir", clickedTrip.getDir());
                startActivityForResult(intent, REQUEST_VIEW_TRIP);
            });
        }
    }

    private void initShowcaseTutorial() {
        //Get preference and checks if it's the first time MainActivity is loaded, if yes->showcase
        Preference preference = Application.getApp().getPreference();
        Log.e("Preference REQ", Boolean.toString(preference.isMAFirstTimeLaunch()));
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        if (preference.isMAFirstTimeLaunch()) {
            preference.setMAFirstTimeLaunch(false);
            Log.e("Preference SET", Boolean.toString(preference.isMAFirstTimeLaunch()));
            //Toast.makeText(this, "This is first time MA is loaded", Toast.LENGTH_SHORT).show();

            ShowcaseView showcase = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.mainFab, this))
                    .setContentTitle("SHOWCASE TUTORIAL")
                    .setContentText("CLICK HERE TO START A TRIP")
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .hideOnTouchOutside()
                    .build();
            showcase.setButtonPosition(lps);

        } else {
            //Code for when MainActivity is rendered again
            //Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
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
            if (Config.createTripFolder()) {initRecycler(); return;}
            e.printStackTrace();
            Toast.makeText(getBaseContext()
                    , "Unable to access your data"
                    , Toast.LENGTH_SHORT).show();
        }

        // create Adapter and fill it with Trips
        tripAdapter = new TripAdapter(trips);
        mainRecycler.setAdapter(tripAdapter);
        tripAdapter.notifyDataSetChanged();
        registerForContextMenu(mainRecycler);
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

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied.
            startTrip();
        });

        //OnFailure ask User to change settings
        task.addOnFailureListener(this, e -> {
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
                        Toast.makeText(MainActivity.this, "Cannot start trip without location", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_VIEW_TRIP:
                if(resultCode == Activity.RESULT_CANCELED) {
                    //TODO move FAB up when Snackbar shows
                    Snackbar.make(findViewById(R.id.activity_main), "Der ausgewählte Trip scheint beschädigt zu sein", Snackbar.LENGTH_LONG).show();
                }
                break;
            case REQUEST_RECORD_TRIP:
                if(resultCode == Activity.RESULT_CANCELED) {
                    if (data != null) {
                        if (data.hasExtra("error")) {
                            Snackbar.make(findViewById(R.id.activity_main), data.getStringExtra("error"), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(findViewById(R.id.activity_main), "Fehler beim Aufnehmen eines Trips", Snackbar.LENGTH_LONG).show();
                        }
                    }
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
            case PERMISSION_BLUETOOTH_ADMIN:{
                if(!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(getBaseContext(),
                            "Can't send or receive without Bluetooth",
                            Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSION_ACCESS_WIFI_STATE:{
                if(!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(getBaseContext(),
                            "Can't send or receive without Wifi",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onSuccess(Location location) {

        if (location != null) {

            Intent intent = new Intent(MainActivity.this, RecordTripActivity.class);
            intent.putExtra("currentPosition", new LatLng( location.getLatitude(), location.getLongitude()));
            intent.putExtra("tripName", tripName);
            startActivityForResult(intent, REQUEST_RECORD_TRIP);
        } else {
            Snackbar.make(findViewById(R.id.activity_main), "Positionsfehler", Snackbar.LENGTH_SHORT).show();
        }
    }

    // gets tripName from NewTripDialog
    @Override
    public void returnTripName (String tripName) {
        if (tripName != null && tripName.length() >= 1) {
            this.tripName = tripName;
            checkLocationSetting();
        } else {
            Snackbar.make(findViewById(R.id.activity_main), "Bitte geben sie einen Namen an.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sendTrip(Trip trip){
        if(bluetoothAdapter==null){
            Toast.makeText(getApplicationContext(),"Bluetooth not available",Toast.LENGTH_SHORT).show();
        } else {
            connectionsClient = new NearbyConnect(new File(Environment.getExternalStorageDirectory() + "/roadbook/zip.zip"),
                    Nearby.getConnectionsClient(this), getApplicationContext());
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!bluetoothAdapter.isEnabled()) {
                setBluetoothAdapter();
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            }
            try {
                if (!wifiManager.isWifiEnabled()) {
                    setWifi();
                    Toast.makeText(getApplicationContext(),"Wifi enabled",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"Wifi not available",Toast.LENGTH_SHORT).show();
            }
            Zipper.zip(trip.getDir().getAbsolutePath(),Environment.getExternalStorageDirectory() + "/roadbook/zip.zip");
            connectionsClient.start();
        }
    }

    private void receiveTrip(){
        if(bluetoothAdapter==null){
            Toast.makeText(getApplicationContext(),"Bluetooth not available",Toast.LENGTH_SHORT).show();
        } else {
            connectionsClient = new NearbyConnect(null, Nearby.getConnectionsClient(this), getApplicationContext());
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!bluetoothAdapter.isEnabled()) {
                setBluetoothAdapter();
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            }
            try {
                if (!wifiManager.isWifiEnabled()) {
                    setWifi();
                    Toast.makeText(getApplicationContext(),"Wifi enabled",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),"Wifi not available",Toast.LENGTH_SHORT).show();
            }
            connectionsClient.start();
        }
    }

    public void setBluetoothAdapter(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
        }else {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            } else {
                bluetoothAdapter.disable();
            }
        }
    }

    public void setWifi(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_ACCESS_WIFI_STATE);
        }else {
            wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            } else {
                wifiManager.setWifiEnabled(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = tripAdapter.getPosition();
        } catch (Exception e) {
            Log.d("CONTEXTMENU ", e.getLocalizedMessage());
            return super.onContextItemSelected(item);
        }
        Trip trip = tripAdapter.getTrip(position);
        switch (item.getItemId()) {
            case R.id.send:
                sendTrip(trip);
                break;
            case R.id.delete:
                TripWriter.deleteTrip(trip);
                initRecycler();
                break;
        }
        return super.onContextItemSelected(item);
    }
}
