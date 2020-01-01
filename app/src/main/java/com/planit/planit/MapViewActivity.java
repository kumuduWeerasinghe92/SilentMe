package com.planit.planit;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.planit.planit.adapter.PlaceAutoSuggestAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;




        if(mLocationPermissionsGranted)
        {


            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String locaLongitude = extras.getString("locaLongitude");
                String locaLatitude = extras.getString("locaLatitude");
                String eventtitle = extras.getString("eventtitle");
                String radius = extras.getString("eventradius");
                String ispublic = extras.getString("ispublic");
                String issilent = extras.getString("issilent");

                if(issilent.equals("1"))
                {

                    issilentView.setImageDrawable(this.getDrawable(R.drawable.ic_silent));
                }else
                    {

                        issilentView.setImageDrawable(this.getDrawable(R.drawable.ic_notsilent));
                    }


                if(locaLatitude !=null && locaLongitude !=null) {
                    LatLng prelatlan = new LatLng(Double.valueOf(locaLatitude), Double.valueOf(locaLongitude));

                    moveCamera(prelatlan,DEFAULT_ZOOM,eventtitle,radius,ispublic);
                    currentLatLan = prelatlan;

                }else
                    {
                        Toast.makeText(MapViewActivity.this, "No location has set yet", Toast.LENGTH_SHORT).show();
                    }


            }else
            {
                Toast.makeText(MapViewActivity.this, "No location has set yet", Toast.LENGTH_SHORT).show();
            }



            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    &&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
            {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //widget
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private ImageView eventbtn;
    private ImageView issilentView;
    private Button mAddLocation;
    public PlaceAutoSuggestAdapter padapter;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng currentLatLan;

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        eventbtn = (ImageView) findViewById(R.id.ic_eventbtn2);
        issilentView = (ImageView) findViewById(R.id.ic_issilentview);




        getLocationPermission();
        init();
    }





    private void init()
    {
        Log.e(TAG,"init: initializing ");







        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });


        eventbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCamera(currentLatLan,DEFAULT_ZOOM);
            }
        });


        hideSoftKeyboard();
    }


    private void getDeviceLocation()
    {
        Log.d(TAG,"getDeviceLocation: getting the device current location ");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try
        {
            if(mLocationPermissionsGranted)
            {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if(task.isSuccessful())
                        {
                            Log.d(TAG,"onComplete: found location  ");
                            Location currentLocation = (Location) task.getResult();
                            if(currentLocation !=null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "Current Location");
                            }else
                            {
                                Toast.makeText(MapViewActivity.this,"Please enable GPS!",Toast.LENGTH_SHORT).show();
                            }
                        }else
                        {
                            Log.d(TAG,"onComplete: location null  ");
                            Toast.makeText(MapViewActivity.this,"unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch(SecurityException e)
        {
            Log.d(TAG,"getDeviceLocation: Security exception "+e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title, String redius,String ispublic)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        currentLatLan = latLng;

        if(ispublic.equals("1")) {
            MarkerOptions option = new MarkerOptions().position(latLng).title(title).icon(bitmapDescriptorFromVector(this, R.drawable.usersb));
            mMap.addMarker(option);


        }else
            {

                MarkerOptions option = new MarkerOptions().position(latLng).title(title).icon(bitmapDescriptorFromVector(this, R.drawable.userb));
                mMap.addMarker(option);
            }

        drawCircle(latLng,mMap,redius);


        hideSoftKeyboard();


    }

    private void drawCircle(LatLng point, GoogleMap gmap, String radius){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle

        circleOptions.radius(Integer.valueOf(radius));

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ea9090);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        gmap.addCircle(circleOptions);

    }

    private void moveCamera(LatLng latLng, float zoom, String title)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));



        hideSoftKeyboard();


    }

    private void moveCamera(LatLng latLng, float zoom)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));



        hideSoftKeyboard();


    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview);

        mapFragment.getMapAsync(MapViewActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



}
