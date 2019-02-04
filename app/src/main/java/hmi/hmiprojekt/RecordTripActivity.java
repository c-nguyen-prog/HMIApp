package hmi.hmiprojekt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hmi.hmiprojekt.Location.LocationHelper;
import hmi.hmiprojekt.MemoryAccess.TripWriter;
import hmi.hmiprojekt.TripComponents.Trip;

/**
 * @author Patrick Strobel
 * Activity to record a trip
 */
public class RecordTripActivity extends AppCompatActivity implements OnMapReadyCallback, OnSuccessListener<Location>, FragmentRecordWaypoint.FragmentListener {

    private static final int REQUEST_TAKE_PICTURE = 100;

    private GoogleMap mMap;
    private LocationHelper locationHelper;
    private LatLng lastPosition;
    private LatLng currentPosition;
    private RequestQueue mQueue;
    private Trip mTrip;
    private String pathToPicture;
    private Marker startMarker;
    private FragmentRecordWaypoint fragmentRecordWaypoint;
    private Uri pictureUri;
    private Menu menu;
    private boolean hasWaypoint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentRecordWaypoint = new FragmentRecordWaypoint();
        locationHelper = new LocationHelper(this);
        mQueue = Volley.newRequestQueue(this);
        currentPosition = getIntent().getParcelableExtra("currentPosition");
        mTrip = new Trip(getIntent().getStringExtra("tripName"));

        //create Directory so we can save images in it
        try {
            TripWriter.createTripDir(mTrip);
        } catch (Exception e) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("error", "Trip Ordner konnte nicht erstellt werden");
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }

        setTitle(getIntent().getStringExtra("tripName"));
        setContentView(R.layout.activity_record_trip);

        findViewById(R.id.fabAddWaypoint).setOnClickListener(view -> locationHelper.startLocationRequest(RecordTripActivity.this));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * callback from google maps fragment
     * move camera to start position
     * @param googleMap GoogleMap Instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        startMarker = mMap.addMarker(new MarkerOptions().position(currentPosition));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(currentPosition).zoom(15f).build()));
    }

    /**
     * draws polyline on google maps fragment for which it uses the last waypoint position and the current position
     * Uses the Google Directions API to determine the fastest path for a walking user
     */
    private void drawPolyline() {
        if(lastPosition != null) {
            String urlHead = "https://maps.googleapis.com/maps/api/directions/json?origin=";
            String origin = lastPosition.latitude + "," + lastPosition.longitude;
            String destination = currentPosition.latitude + "," + currentPosition.longitude;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                    urlHead + origin + "&destination=" + destination + "&mode=walking"
                            + "&key=" + getString(R.string.google_maps_key), null,
                    response -> {
                        try {
                            // retrieve necessary information from the provided JSON
                            String points = response.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");

                            List<LatLng> decodedPath = PolyUtil.decode(points);
                            mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(ContextCompat.getColor(this, R.color.colorPrimary)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, Throwable::printStackTrace);

            mQueue.add(request);
        } else {
        //don't draw polyline after first picture and remove initial start marker
        startMarker.remove();
        }
    }

    /**
     * creates new file location and starts Intent to take a picture
     */
    private void takePicture(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            File pictureFile;
            pictureFile = createPictureFile();

            if(pictureFile != null) {
                pathToPicture = pictureFile.getAbsolutePath();
                pictureUri = FileProvider.getUriForFile(this, "hmi.hmiprojekt.HMIApp.fileprovider", pictureFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                startActivityForResult(takePicture, REQUEST_TAKE_PICTURE);
            }

        }
    }

    /**
     * creates file for picture
     * @return created picture file
     */
    private File createPictureFile() {
        @SuppressLint("SimpleDateFormat")
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File tripDir = mTrip.getDir();
        File pictureFile = null;
        try {
            pictureFile = File.createTempFile(fileName, ".jpg", tripDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pictureFile;
    }

    /**
     * started Activitys report back and actions get performed
     * in this case the method is only used to receive information back from the started camera intent
     * if a picture was taken we proceed by starting the fragment
     * otherwise we delete the previously created file
     * @param requestCode predefined integer to switch to cases
     * @param resultCode resultCode given by Activity
     * @param data additional extra data passed down from called Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == REQUEST_TAKE_PICTURE){
            if( resultCode == RESULT_OK){

                fragmentRecordWaypoint.setPicture(pathToPicture);

                //opens fragment to view picture and perform additional actions
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_record_waypoint_container, fragmentRecordWaypoint)
                        .commit();

                //hide FAB and menu
                findViewById(R.id.fabAddWaypoint).setVisibility(View.GONE);
                menu.getItem(0).setVisible(false);

            } else {
                //delete created File if taking a picture failed
                //noinspection ResultOfMethodCallIgnored
                new File(pathToPicture).delete();
                getContentResolver().delete(pictureUri, null, null);
            }
        }


    }

    /**
     * OnSuccess listener used by LocationHelper class
     * @param location current location provided by LocationHelper
     */
    @Override
    public void onSuccess(Location location) {

        if(location != null){
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            takePicture();
        } else {
            Toast.makeText(getBaseContext()
                , "Position error pls try again"
                , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * returns to MainActivity or informers user if requirements are not met
     */
    private void saveTrip(){
        if(!hasWaypoint) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ein Trip kann ohne Bilder nicht gespeichert werden.")
                    .setNegativeButton("Löschen", (dialog, id) -> deleteTrip())
                    .setPositiveButton("Abbrechen", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            setResult(RESULT_OK);
            finishAfterTransition();
        }
    }

    /**
     * deletes created trip and returns back to MainActivity
     */
    private void deleteTrip(){
        TripWriter.deleteTrip(mTrip);
        setResult(RESULT_OK);
        finishAfterTransition();
    }

    /**
     * call back from Fragment which informs me that the user wants to safe the waypoint
     * writes given information and location in EXIF data of the picture
     * manipulates Map Fragment to show the new waypoint
     * @param name given title for the waypoint
     * @param desc given description for the waypoint
     */
    @Override
    public void onSaveWaypointListener(String name, String desc) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(pathToPicture);

            exif.setLatLong(currentPosition.latitude, currentPosition.longitude);
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, name);
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, desc);
            exif.saveAttributes();

        } catch (Exception e) {
            Log.e("EXIF", e.getLocalizedMessage());
        }

        // add marker and polyline to map
        mMap.addMarker(new MarkerOptions().position(currentPosition));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(currentPosition).zoom(15f).build()));
        drawPolyline();
        lastPosition = currentPosition;

        // remove fragment
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragmentRecordWaypoint)
                .commit();

        //show FAB and menu
        findViewById(R.id.fabAddWaypoint).setVisibility(View.VISIBLE);
        menu.getItem(0).setVisible(true);

        hasWaypoint = true;
    }

    /**
     * call back from Fragment which informs me that the user wants to delete the current waypoint
     * deletes unused File and switches out fragments
     */
    @Override
    public void onDeleteWaypointListener() {

        //noinspection ResultOfMethodCallIgnored
        new File(pathToPicture).delete();
        getContentResolver().delete(pictureUri, null, null);

        // remove fragment
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragmentRecordWaypoint)
                .commit();

        //show FAB and menu
        findViewById(R.id.fabAddWaypoint).setVisibility(View.VISIBLE);
        menu.getItem(0).setVisible(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_trip_menu, menu);
        this.menu = menu;
        return true;
    }

    public void onSaveTrip(MenuItem item) {
        saveTrip();
    }

    /**
     * perform custom actions on BackPress
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Willst du den Trip speichern?")
                .setPositiveButton("Speichern", (dialog, id) -> saveTrip())
                .setNegativeButton("Löschen", (dialog, id) -> deleteTrip());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
