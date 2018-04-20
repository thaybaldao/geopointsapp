package com.example.thay.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class StoresLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores_location);
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

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        showStoresInMap(mMap);

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

    private void showStoresInMap(final GoogleMap googleMap){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stores");
        query.whereExists("Location");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override  public void done(List<ParseObject> stores, ParseException e) {
                if (e == null) {

                    for(int i = 0; i < stores.size(); i++) {
                        LatLng storeLocation = new LatLng(stores.get(i).getParseGeoPoint("Location").getLatitude(), stores.get(i).getParseGeoPoint("Location").getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(storeLocation).title(stores.get(i).getString("Name")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }

                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery.clearAllCachedResults();

    }

    private ParseObject findClosestStore(List<ParseObject> nearStores){

        ParseObject storeWithMinimumDistance = nearStores.get(0);
        double minimumDistance = getCurrentUserLocation().distanceInKilometersTo(nearStores.get(0).getParseGeoPoint("Location"));

        for(int i = 1; i < nearStores.size(); i++) {
            if(getCurrentUserLocation().distanceInKilometersTo(nearStores.get(i).getParseGeoPoint("Location")) < minimumDistance) {
                storeWithMinimumDistance = nearStores.get(i);
                minimumDistance = getCurrentUserLocation().distanceInKilometersTo(nearStores.get(i).getParseGeoPoint("Location"));
            }
        }

        return storeWithMinimumDistance;
    }


    private void showClosestStore(final GoogleMap googleMap){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stores");
        query.whereNear("Location", getCurrentUserLocation());
        // you can set the limit of near stores you want
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override  public void done(List<ParseObject> nearStores, ParseException e) {
                if (e == null) {

                    ParseObject closestStore = findClosestStore(nearStores);

                    LatLng storeLocation = new LatLng(closestStore.getParseGeoPoint("Location").getLatitude(), closestStore.getParseGeoPoint("Location").getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(storeLocation).title(closestStore.getString("Name")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery.clearAllCachedResults();

    }

    private void showRouteToClosestStore(final GoogleMap googleMap){

    }


    private void alertDisplayer(String title,String message){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(StoresLocation.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(StoresLocation.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        android.app.AlertDialog ok = builder.create();
        ok.show();
    }
}
