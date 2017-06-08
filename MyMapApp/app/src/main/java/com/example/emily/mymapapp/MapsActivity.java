package com.example.emily.mymapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.PointOfInterest;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetWorkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final long MY_LOC_ZOOM_FACTOR = 15;
    private boolean canGetLoc = false;
    EditText pointOfInt;



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

        //mMap.setMyLocationEnabled(true);
    }

    public void switchView(View v) {
        if (mMap.getMapType() == (GoogleMap.MAP_TYPE_SATELLITE)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (mMap.getMapType() == (GoogleMap.MAP_TYPE_NORMAL)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    public void clear(View v) {
        mMap.clear();
    }

    public void trackOff(View v) {
        if (locationManager != null) {
            locationManager = null;
        }
    }


    public void getLocation(View v) {
        if (canGetLoc) {
            canGetLoc = false;
            Toast.makeText(getApplicationContext(), "Tracking off", Toast.LENGTH_SHORT).show();

        }

        else {
            canGetLoc = true;
            try {
                Toast.makeText(getApplicationContext(), "Tracking on", Toast.LENGTH_SHORT).show();

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

                    else if (isNetWorkEnabled) {
                        Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);

                        Log.d("MyMaps", "getLocation: NetworkLoc update request successful.");
                        Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                    }

                }
            } catch (Exception e) {
                Log.d("MyMaps", "Caught exception in getLocation");
                e.printStackTrace();
            }
        }

    }

    public void dropAMarker(String provider) {
        LatLng userLocation = new LatLng(0, 0);

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
            if (provider.equals("GPS")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else if (provider.equals("Network")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

        }

        if (myLocation == null) {
            Log.d("MyMaps", "No location found");
            Toast.makeText(this, "No location found", Toast.LENGTH_SHORT).show();
            //display a message via Log.d and/or Toast
        }

        else {
            //get the user location
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display a message with the lat/long
            String loca = "Latitude: " + myLocation.getLatitude() + " Longitude: " +
                    myLocation.getLongitude();
            Toast.makeText(this, loca, Toast.LENGTH_SHORT).show();

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //drop actual marker on map
            //if using circles, reference android circle class
            if (provider.equals("GPS")) {
                mMap.addCircle(new CircleOptions()
                        .center(userLocation).radius(5).strokeColor(Color.RED)
                        .strokeWidth(2).fillColor(Color.RED));
                Log.d("MyMap", "Dropping network marker");
                mMap.animateCamera(update);


            }
            else if (provider.equals("Network")) {
                mMap.addCircle(new CircleOptions()
                        .center(userLocation).radius(5).strokeColor(Color.GREEN)
                        .strokeWidth(2).fillColor(Color.GREEN));
                Log.d("MyMap", "Dropping GPS marker");
                mMap.animateCamera(update);


            }

            //  mMap.animateCamera(update);
        }




    }


    public void searchPOI(View v) {
        setContentView(R.layout.activity_maps);

        pointOfInt = (EditText) findViewById(R.id.editText_search);
        String locSearch = "" + pointOfInt;

        Geocoder gc = new Geocoder(this);
        try {
            List<Address> addressList = gc.getFromLocationName(locSearch, 5);
            


        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MyMaps", "No POI near");
            Toast.makeText(this, "" + locSearch + " not found", Toast.LENGTH_SHORT).show();

        }



    }




    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "getLocation- Network enabled- requesting location updates");
            Toast.makeText(getApplicationContext(), "Using network", Toast.LENGTH_SHORT).show();
            //output in Log.d and Toast that GPS is enabled and working

            dropAMarker("Network");
            //drop a marker on map create dropAMarker method


            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        locationListenerNetwork);
            }

            catch (SecurityException e) {
                Log.d("MyMaps", "getLocation- onLocationChanged network issue");
            }


            //relaunch the network provider request (requestLocationUpdates (NETWORK_PROVIDER))
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in Log.d and Toast
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "location provider in onstatuschanged FOR NETWORKING is available");
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("self", "location provider network out of service");
                    Toast.makeText(getApplicationContext(), "tracker unavailable", Toast.LENGTH_SHORT).show();
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("self", "location provider network out of service");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);

                    } catch (SecurityException e) {
                        Log.d("self", "onstatuschangednetwork security exception 2");
                    }
                    break;

                default:
                    Log.d("self", "location provider network out of service");
                    break;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };





    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
            Toast.makeText(getApplicationContext(), "Using GPS", Toast.LENGTH_SHORT).show();

            //output is Log.d and Toast that GPS is enabled and working

            dropAMarker("GPS");
            isNetWorkEnabled = false;
            //drop a marker on map- create a method called dropAMarker



            //remove the network location updates. Hint see the LocationManager for update removal method
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "getLocation- GPS enabled- requesting location updates");
            Toast.makeText(getApplicationContext(), "Using GPS", Toast.LENGTH_SHORT).show();
            //output in Log.d and toast that GPS is enabled and working


            //set up a switch statement to check the status input parameter

            switch(status) {
                case LocationProvider.AVAILABLE: {
                    Log.d("MyMaps", "getLocation- GPS provider available");
                    Toast.makeText(getApplicationContext(), "GPS available", Toast.LENGTH_SHORT).show();
                    break;
                }

                case LocationProvider.OUT_OF_SERVICE: {
                    Log.d("MyMaps", "getLocation- GPS provider out of service");
                    Toast.makeText(getApplicationContext(), "GPS out of service", Toast.LENGTH_SHORT).show();


                }

                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    Log.d("MyMaps", "getLocation- GPS provider temporarily unavailable");

                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerGPS);

                    } catch (SecurityException e) {
                        Log.d("self", "onstatuschangednetwork security exception 2");
                    }
                    break;

                }
            }
            //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
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


    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {

    }
}


