package com.example.stefa.mycustommaps;

import android.*;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.Criteria;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE2 = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE3 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
        } catch (SecurityException e) {
            Toast.makeText(this, "Exception onCreate", Toast.LENGTH_SHORT).show();
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
        boolean fullAccess = requestPermissions();

        try {
            addSeedsToMap();

            mMap.setMyLocationEnabled(true);

            Location location = locationManager.getLastKnownLocation(provider);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (inRange(marker.getPosition().latitude, marker.getPosition().longitude)) {
                        Toast.makeText(getApplicationContext(), "In Range", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Out Off Range", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            //double dist = distFrom(location.getLatitude(), location.getLongitude(), 55.406263, 10.405972);
            //Toast.makeText(this, dist+"", Toast.LENGTH_SHORT).show();



        } catch (SecurityException e) {
            Toast.makeText(this, "Exception onMapReady"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void addSeedsToMap()
    {
        ArrayList<Seed> seeds = getSeeds();

        for (Seed seed : seeds) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(seed.latitude, seed.longitude)).title(seed.title));
        }
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // miles (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist*1000;
    }

    public boolean inRange(double lat, double lng)
    {
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            double range = distFrom(location.getLatitude(), location.getLongitude(), lat, lng);

            if (range <= 100) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            Toast.makeText(this, "Exception InRange", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public boolean requestPermissions()
    {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE2);
            return false;
        }

        if (checkSelfPermission(android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.VIBRATE}, LOCATION_PERMISSION_REQUEST_CODE3);
            return false;
        }

        return true;
    }

    public ArrayList<Seed> getSeeds() {
        ArrayList<Seed> seeds = new ArrayList<Seed>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream stream = getAssets().open("seeds.xml");
            Document doc = builder.parse(stream);
            Element element = doc.getDocumentElement();

            // get all child nodes
            NodeList nodes = element.getChildNodes();

            // print the text content of each child
            for (int i = 0; i < nodes.getLength(); i++) {
                NodeList seedNodes = nodes.item(i).getChildNodes();

                Seed seed = new Seed();
                for (int m = 0; m < seedNodes.getLength(); m++) {
                    Node node = seedNodes.item(m);
                    if (node.getNodeType() == 3) {
                        continue;
                    }

                    if(node.getNodeName().toLowerCase().contains("title")) {
                        seed.title = node.getTextContent();
                    }

                    if(node.getNodeName().toLowerCase().contains("latitude")) {
                        seed.latitude = Double.parseDouble(node.getTextContent());
                    }

                    if(node.getNodeName().toLowerCase().contains("longitude")) {
                        seed.longitude = Double.parseDouble(node.getTextContent());
                    }
                }



                if (seed.longitude == 0.0 || seed.latitude == 0.0) {
                    continue;
                }
                Log.d("Seed:", seed.toString());
                seeds.add(seed);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return seeds;
    }
}