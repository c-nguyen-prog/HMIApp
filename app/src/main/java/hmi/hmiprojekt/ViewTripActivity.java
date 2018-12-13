package hmi.hmiprojekt;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.List;

import hmi.hmiprojekt.MemoryAccess.TripReader;
import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.TripComponents.Waypoint;

public class ViewTripActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private RequestQueue mQueue;
    private SupportMapFragment mapFragment;
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
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.view_trip_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //place all markers and draw lines between them
        for ( Waypoint waypoint : mTrip.getWaypoints() ) {
            mMap.addMarker(new MarkerOptions().position(waypoint.getLatLng()).title(waypoint.getName()));

            if(previousWaypoint != null){
                addPolyline(previousWaypoint.getLatLng(), waypoint.getLatLng());
            }
            previousWaypoint = waypoint;
        }

        // set camera over start position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mTrip.getWaypoints().get(0).getLatLng()));
    }

    private void addPolyline(LatLng origin, LatLng destination){
        drawPolyline(
                origin.latitude + "," + origin.longitude,
                destination.latitude + "," + destination.longitude);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        //return true if custom behaviour was done
        // TODO ImageView öffnen
        //return false for default behaviour
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
