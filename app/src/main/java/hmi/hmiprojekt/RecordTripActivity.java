package hmi.hmiprojekt;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import hmi.hmiprojekt.Location.LocationHelper;

public class RecordTripActivity extends FragmentActivity implements OnMapReadyCallback, OnSuccessListener<Location> {

    private GoogleMap mMap;
    LocationHelper locationHelper;
    SupportMapFragment mapFragment;
    LatLng startPosition;
    LatLng currentPosition;
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(this);
        startPosition = getIntent().getParcelableExtra("startPosition");
        mQueue = Volley.newRequestQueue(this);

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

        mMap.addMarker(new MarkerOptions().position(startPosition).title("Start Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));
    }

    private void addPolyline(){
        drawPolyline(
                startPosition.latitude + "," + startPosition.longitude,
                currentPosition.latitude + "," + currentPosition.longitude);
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
    public void onSuccess(Location location) {

        if(location != null){
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            //line
            mMap.addMarker(new MarkerOptions().position(currentPosition).title("Waypoint"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            addPolyline();

            startPosition = currentPosition;
            currentPosition = null;
        } else {
            Toast.makeText(getBaseContext()
                , "Position error pls try again"
                , Toast.LENGTH_SHORT).show();
    }
    }
}
