package com.example.emily.mymapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetWorkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000*15*1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in SanFran and move the camera
        LatLng sanfran = new LatLng(37.77, 238.59);
        mMap.addMarker(new MarkerOptions().position(sanfran).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanfran));

        /*LatLng sanDiego = new LatLng(32.7, 243.84);
        mMap.addMarker(new MarkerOptions().position(sanDiego).title("Marker in current location, San Diego"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanDiego));*/

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "Failed Permission check fine");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "Failed permission check coarse");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        mMap.setMyLocationEnabled(true);
    }

    public void switchView (View v) {
        if (mMap.getMapType() == (GoogleMap.MAP_TYPE_SATELLITE)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        else if (mMap.getMapType() == (GoogleMap.MAP_TYPE_NORMAL)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }


    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSenabled)
                Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status
            isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetWorkEnabled)
                Log.d("MyMaps", "getLocation: NETWORK is enabled");

            if(!isGPSenabled && !isNetWorkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            }

            else {
                this.canGetLocation = true;

                if(isNetWorkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

                if(isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMaps", "getLocation: GPSLoc update request successful.");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }

            }
        }

        catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation");
            e.printStackTrace();
        }
    }
}
