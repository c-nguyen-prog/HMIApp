package hmi.hmiprojekt.Location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private final static int PERMISSION_REQUEST_LOCATION = 200;
    private FusedLocationProviderClient mFusedLocationClient;
    private OnSuccessListener<Location> locationOnSuccessListener;

    /**
     * Constructor
     * @param locationOnSuccessListener put in "this" when you call this from an activity
     *                                  and override the OnSuccess() of that activity
     */
    public LocationHelper(OnSuccessListener<Location> locationOnSuccessListener){
        this.locationOnSuccessListener = locationOnSuccessListener;
    }

    public void startLocationRequest(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            requestLocation(activity);
        }
    }

    public void handlePermissionRequestResult(Activity activity, int[] grantResults){
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            requestLocation(activity);
        } else {
            // permission denied, boo!
            // TODO inform user
        }
    }

    //I have already checked them before
    @SuppressLint("MissingPermission")
    private void requestLocation(Context context) {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(mFusedLocationClient == null){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }

        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().size() == 0) {
                    return;
                }
                locationOnSuccessListener.onSuccess(locationResult.getLastLocation());
                mFusedLocationClient.removeLocationUpdates(this);
            }
        }, null);

    }
}
