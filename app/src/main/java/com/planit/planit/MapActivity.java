package com.planit.planit;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.planit.planit.adapter.PlaceAutoSuggestAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {

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
                if(locaLatitude !=null && locaLongitude !=null) {
                    LatLng prelatlan = new LatLng(Double.valueOf(locaLatitude), Double.valueOf(locaLongitude));
                    moveCamera(prelatlan,DEFAULT_ZOOM,"Previous Location");

                }else
                    {
                        getDeviceLocation();
                    }


            }else
                {
                    getDeviceLocation();
                }

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    moveCamera(latLng,mMap.getCameraPosition().zoom,"Tapped location");
                }
            });

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
    private static final String ACCESS_NOTIFICATION_ = Manifest.permission.ACCESS_NOTIFICATION_POLICY;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 6666;

    //widget
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private Button mAddLocation;
    public PlaceAutoSuggestAdapter padapter;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng currentLatLan;

    private void getNotificationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_NOTIFICATION_POLICY};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                ACCESS_NOTIFICATION_) == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mAddLocation = (Button) findViewById(R.id.ic_addLocationBtn);



        getNotificationPermission();
        getLocationPermission();
        init();
        padapter = new PlaceAutoSuggestAdapter(MapActivity.this,android.R.layout.simple_list_item_1);
        mSearchText.setAdapter(padapter);
        mSearchText.setOnItemClickListener(this );
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        geoLocate();
    }



    private void init()
    {
        Log.e(TAG,"init: initializing ");



        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                Toast.makeText(MapActivity.this, event.getKeyCode()+"", Toast.LENGTH_SHORT).show();
                if(event.getKeyCode() ==66)
                {
                    padapter.setIsEnter(true);

                }

                return false;
            }
        });





        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });


        mAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("locaLatitude", currentLatLan.latitude+"" );
                intent.putExtra("locaLongitude", currentLatLan.longitude+"" );
                setResult(RESULT_OK, intent);
                finish();


            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate()
    {
        Log.d(TAG,"geoLocate: initializing ");

        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try
        {

            list = geocoder.getFromLocationName(searchString,1);
        }catch(IOException e)
        {
            Log.d(TAG,"geoLocate: ioExcetion "+ e.getMessage());
        }

        if(list.size()>0)
        {
            Address address = list.get(0);

            Log.d(TAG,"geoLocate: found a location "+ address.toString());

            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }

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
                                    Toast.makeText(MapActivity.this,"Please enable GPS!",Toast.LENGTH_SHORT).show();
                                }
                        }else
                            {
                                Log.d(TAG,"onComplete: location null  ");
                                Toast.makeText(MapActivity.this,"unable to get current location",Toast.LENGTH_SHORT).show();
                            }
                    }
                });
            }

        }catch(SecurityException e)
        {
            Log.d(TAG,"getDeviceLocation: Security exception "+e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        currentLatLan = latLng;

        MarkerOptions option = new MarkerOptions().position(latLng).title(title);
        mMap.clear();
        mMap.addMarker(option);


        hideSoftKeyboard();


    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
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
