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

        //get tripDir vom intent and read in Trip
        File tripDir = (File) getIntent().getSerializableExtra("tripDir");
        try {
            mTrip = TripReader.readTrip(tripDir);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.view_trip_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int i = 0;
        //place all markers and draw lines between them
        for ( Waypoint waypoint : mTrip.getWaypoints() ) {
            //TODO getLatLng NPE when picture doesnt contain LatLng
            try {
                mMap.setOnMarkerClickListener(this);
                mMap.addMarker(new MarkerOptions().position(waypoint.getLatLng()).title(waypoint.getName()).snippet(Integer.toString(i++)));
            } catch (Exception e) {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            if(previousWaypoint != null){
                addPolyline(previousWaypoint.getLatLng(), waypoint.getLatLng());
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

    private void addPolyline(LatLng origin, LatLng destination){
        drawPolyline(
                origin.latitude + "," + origin.longitude,
                destination.latitude + "," + destination.longitude);
    }

    private void drawPolyline(String origin,String destination) {

        String urlHead = "https://maps.googleapis.com/maps/api/directions/json?origin=";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlHead + origin + "&destination=" + destination + "&mode=walking" + "&key=" + getString(R.string.google_maps_key), null, response -> {
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
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(this, ImageViewerActivity.class);

        Waypoint clickedWaypoint = mTrip.getWaypoints().get(Integer.parseInt(marker.getSnippet()));

        File pictureFile = clickedWaypoint.getImg();
        intent.putExtra("picture", pictureFile);

        String desc = clickedWaypoint.getDesc();
        intent.putExtra("Description", desc);

        String name = clickedWaypoint.getName();
        intent.putExtra("Name", name);

        startActivity(intent);
        //return true if custom behaviour was done
        // TODO Nico hier custom ImageView öffnen !!
        //return false for default behaviour
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
