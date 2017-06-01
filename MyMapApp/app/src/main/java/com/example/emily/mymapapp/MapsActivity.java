package com.example.emily.mymapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final long MY_LOC_ZOOM_FACTOR = 15;


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

    public void switchView(View v) {
        if (mMap.getMapType() == (GoogleMap.MAP_TYPE_SATELLITE)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (mMap.getMapType() == (GoogleMap.MAP_TYPE_NORMAL)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }


    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled)
                Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status
            isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetWorkEnabled)
                Log.d("MyMaps", "getLocation: NETWORK is enabled");

            if (!isGPSenabled && !isNetWorkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");
            } else {
                this.canGetLocation = true;

                if (isNetWorkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMaps", "getLocation: GPSLoc update request successful.");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }

            }
        } catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation");
            e.printStackTrace();
        }
    }

    public void dropAMarker(String provider) {
        LatLng userLocation = null;
        if (locationManager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);

        }

        if (myLocation == null) {
            Log.d("MyMaps", "No location found");
            Toast.makeText(this, "No location found", Toast.LENGTH_SHORT);
            //display a message via Log.d and/or Toast
        }

        else {
            //get the user location
            userLocation = new LatLng(myLocation.getLongitude(), myLocation.getLatitude());

            //display a message with the lat/long
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //drop actual marker on map
            //if using circles, reference android circle class
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation).radius(1).strokeColor(Color.RED)
                    .strokeWidth(2).fillColor(Color.RED));

            mMap.animateCamera(update);
        }




    }


    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
            Toast.makeText(getApplicationContext(), "Using GPS", Toast.LENGTH_SHORT);

            //output is Log.d and Toast that GPS is enabled and working

            dropAMarker("GPS");
            //drop a marker on map- create a method called dropAMarker


            //remove the network location updates. Hint see the LocationManager for update removal method
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "getLocation- GPS enabled- requesting location updates");
            Toast.makeText(getApplicationContext(), "Using GPS", Toast.LENGTH_SHORT);

            //output in Log.d and toast that GPS is enabled and working

            //set up a switch statement to check the status input parameter
            //case LocationProvider.AVAILABLE --> output message to Log.d and Toast

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("MyMaps", "getLocation- location provider available");
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLocation();
            }

            /*if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLocation();
            }*/
            //case LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "getLocation- Network enabled- requesting location updates");
            Toast.makeText(getApplicationContext(), "Using network", Toast.LENGTH_SHORT);
            //output in Log.d and Toast that GPS is enabled and working

            dropAMarker("Network");
            //drop a marker on map create dropAMarker method

            
            //relaunch the network provider request (requestLocationUpdates (NETWORK_PROVIDER))
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in Log.d and Toast

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };


}
