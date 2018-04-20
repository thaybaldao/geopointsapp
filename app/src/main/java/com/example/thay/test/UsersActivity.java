package com.example.thay.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LocationCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class UsersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                intent.putExtra("function", 1);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        saveCurrentUserLocation();

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

        final int function = getIntent().getExtras().getInt("function");

        if(function == 1){
            showCurrentUserInMap(mMap);
        }
        else if(function == 2){
            showAllUsersInMap(mMap);
        }
        else if(function == 3){
            showClosestUser(mMap);
        }

    }


    private void saveCurrentUserLocation(){

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        ParseGeoPoint.getCurrentLocationInBackground(10000, criteria,new LocationCallback() {
            @Override
            public void done(ParseGeoPoint userLocation, ParseException e) {
                if (userLocation != null) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.put("Location", userLocation);
                        currentUser.saveInBackground();
                    } else {
                        alertDisplayer("Well... you're not logged in...","Login first!");
                    }
                } else {
                    Log.d("userLocation", "Error: " + e.getMessage());
                }
            }
        });
    }

    private ParseGeoPoint getCurrentUserLocation(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            alertDisplayer("Well... you're not logged in...","Login first!");
        }

            return currentUser.getParseGeoPoint("Location");

    }

    private void showCurrentUserInMap(final GoogleMap googleMap){

        alertDisplayer("I got here too...","");

        //ParseGeoPoint currentUserLocation = getCurrentUserLocation();
        //alertDisplayer("and here...","");
        //LatLng storeLocation = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
        //googleMap.addMarker(new MarkerOptions().position(storeLocation).title(ParseUser.getCurrentUser().getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }


    private void showAllUsersInMap(final GoogleMap googleMap){

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereExists("Location");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override  public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {

                    for(int i = 0; i < users.size(); i++) {
                        LatLng storeLocation = new LatLng(users.get(i).getParseGeoPoint("Location").getLatitude(), users.get(i).getParseGeoPoint("Location").getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(storeLocation).title(users.get(i).getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }

                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery.clearAllCachedResults();

    }

    private ParseUser findClosestUser(List<ParseUser> nearUsers){

        ParseUser userWithMinimumDistance = nearUsers.get(0);
        double minimumDistance = getCurrentUserLocation().distanceInKilometersTo(nearUsers.get(0).getParseGeoPoint("Location"));

        for(int i = 1; i < nearUsers.size(); i++) {
            if(getCurrentUserLocation().distanceInKilometersTo(nearUsers.get(i).getParseGeoPoint("Location")) < minimumDistance) {
                userWithMinimumDistance = nearUsers.get(i);
                minimumDistance = getCurrentUserLocation().distanceInKilometersTo(nearUsers.get(i).getParseGeoPoint("Location"));
            }
        }

        return userWithMinimumDistance;
    }

    private void showClosestUser(final GoogleMap googleMap){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNear("Location", getCurrentUserLocation());
        // you can set the limit of near stores you want
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override  public void done(List<ParseUser> nearUsers, ParseException e) {
                if (e == null) {

                    ParseUser closestUser = findClosestUser(nearUsers);

                    double distance = getCurrentUserLocation().distanceInKilometersTo(closestUser.getParseGeoPoint("Location"));

                    Toast.makeText(getApplicationContext(),"The closest user from you is " + closestUser.getUsername().toString() + ". \n You are " + distance + "km from this user.", Toast.LENGTH_SHORT).show();

                    LatLng storeLocation = new LatLng(closestUser.getParseGeoPoint("Location").getLatitude(), closestUser.getParseGeoPoint("Location").getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(storeLocation).title(closestUser.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery.clearAllCachedResults();

    }

    private void alertDisplayer(String title,String message){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(UsersActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //Intent intent = new Intent(UsersActivity.this, LoginActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //startActivity(intent);
                    }
                });
        android.app.AlertDialog ok = builder.create();
        ok.show();
    }

// Caveats

//At the moment there are a couple of things to watch out for:

//Each ParseObject class may only have one key with a ParseGeoPoint object.
//Using the whereNear constraint will also limit results to within 100 miles.
//Points should not equal or exceed the extreme ends of the ranges. Latitude should not be -90.0 or 90.0. Longitude should not be -180.0 or 180.0. Attempting to set latitude or longitude out of bounds will cause an error.

}
