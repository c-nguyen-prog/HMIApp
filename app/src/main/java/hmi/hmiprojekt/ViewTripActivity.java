package hmi.hmiprojekt;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
import com.google.maps.android.PolyUtil;

import org.json.JSONException;

import java.io.File;
import java.text.ParseException;
import java.util.List;

import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.TripComponents.Waypoint;

/**
 * @author Patrick Strobel
 * Activity to view a recorded trip and provide a mean to view recorded pictures by clicking on marker
 */
public class ViewTripActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private RequestQueue mQueue;
    private Waypoint previousWaypoint;
    private Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trip);

        mQueue = Volley.newRequestQueue(this);

        //get tripDir from intent and read in Trip
        File tripDir = (File) getIntent().getSerializableExtra("tripDir");
        try {
            mTrip = TripReader.readTrip(tripDir);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        setTitle(mTrip.getName());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.view_trip_map);
        mapFragment.getMapAsync(this);
    }

    /**
     * callback from google maps fragment
     * loops through waypoints, sets marker and adds polylines between them
     * adds identifier to marker
     * catches corrupted files, ie. pictures without location information or trips without waypoints
     * @param googleMap GoogleMap Instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int i = 0;
        //place all markers and draw lines between them
        for ( Waypoint waypoint : mTrip.getWaypoints() ) {

            try {
                mMap.setOnMarkerClickListener(this);
                mMap.addMarker(new MarkerOptions().position(waypoint.getLatLng()).title(waypoint.getName()).snippet(Integer.toString(i++)));
            } catch (Exception e) {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            if(previousWaypoint != null){
                drawPolyline(previousWaypoint.getLatLng(), waypoint.getLatLng());
            }
            previousWaypoint = waypoint;
        }

        // set camera over start position
        if(mTrip.getWaypoints().size() == 0) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(CameraPosition
                            .builder()
                            .target(mTrip.getWaypoints().get(0).getLatLng())
                            .zoom(15f)
                            .build()));
        }
    }

    /**
     * draws polyline on google maps fragment using the Google Directions API
     * @param origin origin location as LatLng
     * @param destination destination location as LatLng
     */
    private void drawPolyline(LatLng origin, LatLng destination) {

        String urlHead = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        String ori = origin.latitude + "," + origin.longitude;
        String dest = destination.latitude + "," + destination.longitude;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlHead + ori + "&destination=" + dest + "&mode=walking" + "&key=" + getString(R.string.google_maps_key), null,
                response -> {
                    try {
                        // retrieve necessary information from the provided JSON and add it to map
                        String points = response.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                        List<LatLng> decodedPath = PolyUtil.decode(points);
                        mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(ContextCompat.getColor(this, R.color.colorPrimary)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        mQueue.add(request);

    }

    /**
     * Takes the identifier of the marker and
     * the directory of the current trip and starts the ImageViewerActivity
     * @param marker marker which was clicked on by user
     * @return value true because custom behaviour was performed
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra("tripDir", mTrip.getDir());
        intent.putExtra("waypointIndex", Integer.parseInt(marker.getSnippet()));
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
        super.onBackPressed();
    }
}
