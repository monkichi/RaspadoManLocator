package com.parse.RaspadoCartLocator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class RaspadoMapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    //LocationManager for getting location coordinates
    private LocationManager mLocationManager;
    //Location variable to store users last location
    private Location userLastLocation;
    private String provider;
    private LatLng usersLastLocLatLng;
    private String currentUserType;
    private boolean isLocationOn =false;
    private ParseUser currentUser;
    private LatLng nearestSellerLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get reference to currentUser object
        currentUser = ParseUser.getCurrentUser();
        //Find out what type of user current user is either USER or SELLER
        currentUserType = currentUser.getString("sellerOrUser");

        if (currentUserType==null) {
            Toast.makeText(getApplicationContext(),"Didnt Get User or Seller",Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_raspado_maps);
        }
        else if (currentUserType.equals("seller")){
                //set the view to be
                setContentView(R.layout.activy_maps_layout_seller);

                //inflate the buttons
                final Button editMenuListButton = (Button) findViewById(R.id.editMenuButton);
                editMenuListButton.setText("Menu");
                editMenuListButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Create the intent to start the seller menu activity

                        //start the edit meu


                    }
                });
                final Button locationTrackOnOffButton = (Button) findViewById(R.id.locationOnOffButton);
                locationTrackOnOffButton.setText("Locate On");
                locationTrackOnOffButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isLocationOn){
                            locationTrackOnOffButton.setText("Locate On");
                            isLocationOn = false;
                            Log.e("isLocationOnClick", String.valueOf(isLocationOn));
                        }
                        else{
                            locationTrackOnOffButton.setText("Locate Off");
                            isLocationOn = true;
                            Log.e("isLocationOnClick", String.valueOf(isLocationOn));
                        }
                    }
                });



            }
        else if (currentUserType.equals("user")){
                setContentView(R.layout.activity_raspado_maps);


            }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

      //Must check location tracking permissions before doing anything with users location

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i("Permissions Test", "Permissions are not set");
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},32);

            }
                //Start doing getting current users location logic
                Log.i("Permissions Test","Permissions are set");
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                provider = mLocationManager.getBestProvider(new Criteria(), false);
                //Get the last know users location
                userLastLocation = mLocationManager.getLastKnownLocation(provider);
                if (userLastLocation == null) {
                    //Start to request location updates
                    mLocationManager.requestLocationUpdates(provider, 400, 1, this);
                    //Get the last known users location
                    userLastLocation = mLocationManager.getLastKnownLocation(provider);
                    Log.i("lastknownLocation","Users last location is null");
                }
                else{
                    Log.i("lastknownLocation","Users last loc"+ userLastLocation.toString());
                    //usersLastLoc
                    usersLastLocLatLng = new LatLng(userLastLocation.getLatitude(),userLastLocation.getLongitude());

                }

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
        mMap.setOnMarkerClickListener(this);
        // Add a marker in the last known location and move the camera
        if(usersLastLocLatLng!=null){
            mMap.addMarker(new MarkerOptions().position(usersLastLocLatLng).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usersLastLocLatLng,5));

            //Check if user has logged whether they are users or not
            if (currentUserType == null){

            }
            //User has a user or seller string
            else {
                    //Query all the nearest raspados to the users location
                    ParseQuery<ParseObject> nearestSellerQuery = ParseQuery.getQuery("sellers");
                    nearestSellerQuery.whereNear("sellerLocation",new ParseGeoPoint(usersLastLocLatLng.latitude,usersLastLocLatLng.longitude));
                    nearestSellerQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e==null){
                                if(objects.size()>0){
                                    for (final ParseObject nearestSellerObject: objects){
                                        Log.i("nearestUserData", "User: ");
                                        //Get the sellers name
                                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                                        //Query all the sellers are returned except we are already a seller
                                        query.whereNotEqualTo("objectId",currentUser.getObjectId());
                                        query.whereEqualTo("objectId", nearestSellerObject.getString("userObjectId"));
                                        query.findInBackground(new FindCallback<ParseUser>() {
                                            public void done(List<ParseUser> objects, ParseException e) {
                                                if (e == null) {
                                                    //Get the sellers location geo point from parse server to add on display later
                                                    ParseGeoPoint nearestSellersGeoPoint = new ParseGeoPoint(nearestSellerObject.getParseGeoPoint("sellerLocation"));
                                                    //Create LatLng from GeoPoint
                                                    LatLng nearestSellerLatLng = new LatLng(nearestSellersGeoPoint.getLatitude(),nearestSellersGeoPoint.getLongitude());
                                                    //Add the marker of the sellers location to map, display with different blue color
                                                    Marker currentSeller  = mMap.addMarker(new MarkerOptions().position(nearestSellerLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestSellerLatLng,1));
                                                    nearestSellerObject.put("markerId", currentSeller.getId());
                                                    try {
                                                        nearestSellerObject.save();
                                                    } catch (ParseException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                } else {
                                                    // Something went wrong.
                                                }
                                            }
                                        });


                                    }
                                }
                            }
                            else{
                                //Error getting the nearest raspados
                                Log.e("nearest raspado error",e.getMessage());
                            }
                        }
                    });
                }
            }
        }


    @Override
    public void onLocationChanged(Location location) {
       //Check if they want to keep being tracked
        Log.e("isLocationOn", String.valueOf(isLocationOn));
           //Clear the map before
           mMap.clear();
           Log.i("onLocationChange","Location moved" + location.getLatitude() + " " + location.getLongitude() + "isOn = " + isLocationOn);

        //Always update the marker in map when user moves location
           LatLng currLocationCoordinates= new LatLng(location.getLatitude(),location.getLongitude());
           // Add a marker in curr location and move camera
           mMap.addMarker(new MarkerOptions().position(currLocationCoordinates).title("Your location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocationCoordinates,1));
        final ParseGeoPoint usersChangedLocationGeoPoint = new ParseGeoPoint(currLocationCoordinates.latitude,currLocationCoordinates.longitude);

        //Query all the nearest raspados to the users location
        ParseQuery<ParseObject> nearestSellerQuery = ParseQuery.getQuery("sellers");
        nearestSellerQuery.whereNear("sellerLocation",usersChangedLocationGeoPoint);
        nearestSellerQuery.whereNotEqualTo("userObjectId",currentUser.getObjectId());
        nearestSellerQuery.findInBackground(new FindCallback<ParseObject>() {


            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for (ParseObject nearestSellerObject: objects){
                            Log.i("nearestUserData", "");
                            //Get the sellers location geo point from parse server to add on display later
                            ParseGeoPoint nearestSellersGeoPoint = new ParseGeoPoint(nearestSellerObject.getParseGeoPoint("sellerLocation"));
                            //Create LatLng from GeoPoint
                            nearestSellerLatLng = new LatLng(nearestSellersGeoPoint.getLatitude(),nearestSellersGeoPoint.getLongitude());

                            //Get the sellers name
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("objectId", nearestSellerObject.getString("userObjectId"));
                            query.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> objects, ParseException e) {
                                    if (e == null) {
                                       if (objects.size()>0){
                                           // The query was successful.
                                           //Add the marker of the sellers location to map, display with different blue color
                                           mMap.addMarker(new MarkerOptions().position(nearestSellerLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(objects.get(0).getUsername()));
                                           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestSellerLatLng,1));
                                       }
                                        else{
                                           Log.i("sellerParseUser", "Size of array is " + objects.size());
                                       }
                                    } else {
                                        // Something went wrong.
                                    }
                                }
                            });

                        }
                    }
                }
                else{
                    //Error getting the nearest raspados
                    Log.e("nearest raspado error",e.getMessage());
                }
            }
        });

        //Only update the sellers location to database when location track is on
        if(isLocationOn){
            if (currentUserType.equals("seller")){
                //Keep updating the database everytime the location is changed
                ParseQuery<ParseObject> currentSellerQuery = ParseQuery.getQuery("sellers");
                currentSellerQuery.whereEqualTo("userObjectID", ParseUser.getCurrentUser().getObjectId());
                currentSellerQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(e==null){
                            if (objects.size()>0){
                                for (ParseObject sellerObject : objects){
                                    //Add the current locations geo point to the database for the current seller
                                    sellerObject.put("sellerLocation",usersChangedLocationGeoPoint);
                                    sellerObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e==null){
                                                //Saved sellers position

                                            }
                                            else{

                                            }
                                        }
                                    });
                                }
                                Log.i("sellerQueryCheck","seller query was succesful "  );
                            }
                        }
                        else{
                            //Error getting the sellers object for current seller
                            Log.e("sellerQueryError",e.getMessage());
                        }
                    }
                });

            }

        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("MarkerClick","Marker was clicked");
        //Start the sellers profile activity with the given info
        Intent sellerInfoProfile = new Intent(getApplicationContext(),SellerInfoProfile.class);
        Bundle markerIdBundle = new Bundle();
        markerIdBundle.putString("markerId", marker.getId());
        sellerInfoProfile.putExtras(markerIdBundle);
        startActivity(sellerInfoProfile);
        return true;
    }
}
