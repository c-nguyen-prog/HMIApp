package hmi.hmiprojekt;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hmi.hmiprojekt.Location.LocationHelper;
import hmi.hmiprojekt.MemoryAccess.TripWriter;
import hmi.hmiprojekt.TripComponents.Trip;

public class RecordTripActivity extends AppCompatActivity implements OnMapReadyCallback, OnSuccessListener<Location> {

    private static final int REQUEST_TAKE_PICTURE = 100;

    private GoogleMap mMap;
    private LocationHelper locationHelper;
    private SupportMapFragment mapFragment;
    private LatLng lastPosition;
    private LatLng currentPosition;
    private RequestQueue mQueue;
    private Trip mTrip;
    private String pathToPicture;
    private Marker startMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(this);
        mQueue = Volley.newRequestQueue(this);
        currentPosition = getIntent().getParcelableExtra("currentPosition");
        mTrip = new Trip(getIntent().getStringExtra("tripName"));

        //create Directory so we can save images in it
        try {
            TripWriter.createTripDir(mTrip);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle(getIntent().getStringExtra("tripName"));
        setContentView(R.layout.activity_record_trip);

        findViewById(R.id.fabAddWaypoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationHelper.startLocationRequest(RecordTripActivity.this);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        startMarker = mMap.addMarker(new MarkerOptions().position(currentPosition));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
    }

    private void addPolyline(){
        if(lastPosition != null){
            drawPolyline(
                    lastPosition.latitude + "," + lastPosition.longitude,
                    currentPosition.latitude + "," + currentPosition.longitude);
        } else {
            //don't draw polyline after first picture and remove initial start marker
            startMarker.remove();
        }
    }

    private void drawPolyline(String origin,String destination) {

        String urlHead = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        StringBuilder url = new StringBuilder();
        url.append(urlHead).append(origin).append("&destination=").append(destination).append("&mode=walking").append("&key=").append(getString(R.string.google_maps_key));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // retrieve necessary information from the provided JSON
                    String points = response.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");

                    List<LatLng> decodedPath = PolyUtil.decode(points);
                    //TODO R.color.colorPrimary not working??
                    mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.parseColor("#E64A19")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    private void takePicture(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            File pictureFile;
            pictureFile = createPictureFile();

            if(pictureFile != null) {
                pathToPicture = pictureFile.getAbsolutePath();

                Uri pictureUri = FileProvider.getUriForFile(this, "hmi.hmiprojekt.HMIApp.fileprovider", pictureFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                startActivityForResult(takePicture, REQUEST_TAKE_PICTURE);
            }

        }
    }

    private File createPictureFile() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK){
            if( requestCode == REQUEST_TAKE_PICTURE){

                ExifInterface exif;
                try {
                    exif = new ExifInterface(pathToPicture);
                    exif.setLatLong(currentPosition.latitude, currentPosition.longitude);
                    exif.saveAttributes();

                } catch (Exception e) {
                    Log.e("EXIF", e.getLocalizedMessage());
                }

            }
        }


    }

    @Override
    public void onSuccess(Location location) {

        if(location != null){
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            takePicture();

            // add marker and polyline
            mMap.addMarker(new MarkerOptions().position(currentPosition));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            addPolyline();

            lastPosition = currentPosition;
        } else {
            Toast.makeText(getBaseContext()
                , "Position error pls try again"
                , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_trip_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // TODO snackbar
    }

    public void onSaveTrip(MenuItem item) {
        finishAfterTransition();
    }
}
