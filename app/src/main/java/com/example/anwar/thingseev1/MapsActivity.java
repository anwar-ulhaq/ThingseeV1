package com.example.anwar.thingseev1;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    // create variables using in application
    private GoogleMap mMap;
    Button btFindPath;
    TextView tvDistance, tvDuration;
    EditText etOrigin, etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Change BG
        getWindow().getDecorView().setBackgroundColor( Color.parseColor( "#ED5565") );


        // link variables to boxes with ID
        btFindPath = (Button) findViewById(R.id.buttonFindPath);

        btFindPath.setBackgroundColor( Color.parseColor("#DA4453") );
        tvDistance = (TextView) findViewById(R.id.textViewDistance);
        tvDuration = (TextView) findViewById(R.id.textViewDuration);
        etOrigin = (EditText) findViewById(R.id.editTextOrigin);
        etDestination = (EditText) findViewById(R.id.editTextDestination);

        btFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
    }


    private void sendRequest(){

        // create 2 variables named as origin and destination to take text entered
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();


        // check if nothing entered ==> remind user
        if(origin.isEmpty()){
            Toast.makeText(this, "Please ENTER Origin Address !!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(destination.isEmpty()){
            Toast.makeText(this, "Please ENTER Destination Address !!!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng metropolia = new LatLng(60.220942, 24.805261);

        SharedPreferences EventPref = getSharedPreferences("EventString", Activity.MODE_PRIVATE);

        LatLng CurrentLocation = new LatLng( Double.parseDouble( EventPref.getString("1", "")  ) ,Double.parseDouble( EventPref.getString("2", "")  ));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLocation, 15));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Metropolia University of Applied Sciences")
                .position(CurrentLocation)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},500);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    // remove last search (markers and polyline) at beginning
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }

    }


    // when we have a route from data ==> present to interface
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {

            // zoom up start location at level 13, set text to Duration and Distance.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 13));
            ((TextView) findViewById(R.id.textViewDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.textViewDistance)).setText(route.distance.text);

            // create markers on map at origin and destination position
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_point))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            // create a polylineOption and add the list of Latlng coordinates to it
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            // add polylineOptions to mMap, then polyPath stores polyline that mMap.adPolyline returns
            // we are able to remove it later for new search
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
