package com.example.cyberdyne.ctfastrak_android_application;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Richard on 10/30/2016
 */

public class MapsActivity extends AppCompatActivity
        implements  OnInfoWindowClickListener, OnMapReadyCallback {

    private static final LatLngBounds CONNECTICUT = new LatLngBounds( // restrictions for scrolling
            new LatLng(41.1, -73.65), new LatLng(42.02, -71.8)); // bottom left of map to top right of map
    private static final CameraPosition CONNECTICUT_CAMERA = new CameraPosition.Builder() // center camera initially on Hartford. Probably a good idea to change this to user location instead
            .target(new LatLng(41.7637, -72.6851)).zoom(11.0f).bearing(0).tilt(0).build();

    private static final float DEFAULT_MIN_ZOOM = 9.0f; // restrictions for zooming
    private static final float DEFAULT_MAX_ZOOM = 22.0f;
    private float mMinZoom;
    private float mMaxZoom;
    private boolean lineSelected;
    private int[] lineHashCodes;
    private GoogleMap currentMap;
    private TextView infoBox;

    ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
    ArrayList<PolylineOptions> polylines = new ArrayList<PolylineOptions>();
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<busStop> busStops = new ArrayList<busStop>();
    ArrayList<stopTime> stopTimes = new ArrayList<stopTime>();
    ArrayList<trip> trips = new ArrayList<trip>();
    ArrayList<shape> shapes = new ArrayList<shape>();
    ArrayList<route> routes = new ArrayList<route>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AssetManager am = getAssets(); // gets files from assets folder
        InputStreamReader ims = null;
        BufferedReader reader = null;
        String line;
        try {
            ims = new InputStreamReader(am.open("stops.txt"), "UTF-8"); // reads stops.txt and puts  comma separated values into arrayList of busStop objects
            reader = new BufferedReader(ims);
            reader.readLine(); // skip the first line of the text file
            while ((line = reader.readLine()) != null) {
                String[] stopInfo = line.split(",");
                busStops.add(new busStop(Integer.parseInt(stopInfo[0]), null, stopInfo[2], Double.parseDouble(stopInfo[3]), Double.parseDouble(stopInfo[4]), null, null, null, null)); // comma separated values stored in text file
            }

            ims = new InputStreamReader(am.open("stop_times.txt"), "UTF-8"); // reads stop_times.txt and puts  comma separated values into arrayList of busStop objects
            reader = new BufferedReader(ims);
            reader.readLine(); // skip the first line of the text file
            while ((line = reader.readLine()) != null) {
                String[] stopInfo = line.split(",");
                stopTimes.add(new stopTime(Integer.parseInt(stopInfo[0]), stopInfo[1], stopInfo[2], Integer.parseInt(stopInfo[3]), null, null, null, null, null)); // !!! Fix these null values
            }

            ims = new InputStreamReader(am.open("trips.txt"), "UTF-8"); // you get the idea by now
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                trips.add(new trip(Integer.parseInt(info[0]), info[1], Integer.parseInt(info[2]), info[3], Integer.parseInt(info[4]), info[5], Integer.parseInt(info[6])));
            }

            ims = new InputStreamReader(am.open("shapes.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                shapes.add(new shape(Integer.parseInt(info[0]), Double.parseDouble(info[1]), Double.parseDouble(info[2]), Integer.parseInt(info[3]), Double.parseDouble(info[4])));
            }
            ims = new InputStreamReader(am.open("routes.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                routes.add(new route(Integer.parseInt(info[0]), null, info[2], null, null, Integer.parseInt(info[5]), null, info[7], info[8]));
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // an error occurred reading file
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMinZoom = DEFAULT_MIN_ZOOM;
        mMaxZoom = DEFAULT_MAX_ZOOM;

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        infoBox = (TextView)findViewById(R.id.textView);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setLatLngBoundsForCameraTarget(CONNECTICUT);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CONNECTICUT_CAMERA));
        map.setMinZoomPreference(mMinZoom);
        map.setMaxZoomPreference(mMaxZoom);

        /* POLYLINES FOR ROUTES */
        for (route busRoute : routes) { // for each busRoute in the routes arraylist, we take the route_id, match it with the corresponding route_id in the trips arraylist, then match it with the correct shape_id so we can add all the vertices of a route to the map
            for (int i = 0; i < trips.size(); i++) {
                if (busRoute.getRoute_id().intValue() == trips.get(i).getRoute_id().intValue()) {
                    for (shape vertex : shapes) {
                        if (vertex.getShape_id().intValue() == trips.get(i).getShape_id().intValue()) {
                            coordinates.add(new LatLng(vertex.getShape_pt_lat(), vertex.getShape_pt_lon()));
                        }
                    }
                    break;
                }
            }

            StringBuilder color = new StringBuilder("");
            color.append("#FF" + busRoute.getRoute_color()); // Colors in text are stored as 16 bit hex. Convert to 32 bit with the leading to hex digits being the Alpha (transparency) value. FF = 100% 80 = 50%
            String stringColor = color.toString();
            polylines.add(new PolylineOptions().addAll(coordinates).width(10).color(Color.parseColor(stringColor)).clickable(true));
            coordinates.clear();
        }

        lineHashCodes = new int[polylines.size()];
        for (int i = 0; i < polylines.size(); i++) {
            lineHashCodes[i] = map.addPolyline(polylines.get(i)).hashCode();
        }

        /* DISPLAY BUS STOPS ON POLYLINE CLICK */
        currentMap = map;
        lineSelected = false;
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() { // Add a listener for polyline clicks
            @Override
            public void onPolylineClick(Polyline polyline) {
                //int i = 0;
                // Flip the values of the r, g and b components of the polyline's color.
                //int strokeColor = polyline.getColor() ^ 0x00ffffff;

                if (lineSelected) { // This will never occur the first time a route is selected
                    //System.out.println("lineSelected is already " + lineSelected);
                    for (int j = 0; j < markers.size(); j++) {
                        markers.get(j).remove();
                        lineSelected = false;
                    }
                }
                if (!lineSelected) {
                    //polyline.setColor(strokeColor);
                    int i = 0;
                    for (int j = 0; j < polylines.size(); j++) {
                        if (polyline.hashCode() == lineHashCodes[j]) // The polyline selected has been found in the polylines arraylist
                        {
                            //System.out.println("This route_id is " + routes.get(j).getRoute_id());
                            for (int k = 0; k < trips.size(); k++) {
                                if (trips.get(k).getRoute_id().intValue() == routes.get(j).getRoute_id().intValue()) {
                                    //System.out.println("Trip " + trips.get(k).getRoute_id() + " has been found on this route");
                                    for (int m = 0; m < stopTimes.size(); m++) {
                                        if (stopTimes.get(m).getTrip_id().intValue() == trips.get(k).getTrip_id().intValue()) {
                                            //System.out.println("Stop time found");
                                            for (int n = 0; n < busStops.size(); n++) {
                                                if (busStops.get(n).getStop_id().intValue() == stopTimes.get(m).getStop_id().intValue()) {
                                                    //System.out.println("Marker added");
                                                    Marker stopMarker = currentMap.addMarker(new MarkerOptions().position(new LatLng(busStops.get(n).getStop_lat(), busStops.get(n).getStop_lon())).title(busStops.get(n).getStop_name()));
                                                    stopMarker.setTag(i);
                                                    markers.add(stopMarker);
                                                    i++;
                                                    lineSelected = true;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    //System.out.println("lineSelected change to " + lineSelected);
                }
            }
        });

        // Set a listener for marker click.
        map.setOnInfoWindowClickListener(this);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Integer markerId = (Integer) marker.getTag(); //We need to get the relevant busStop info knowing which markerTag was selected
        int id = busStops.get(markerId).getStop_id();
        String headsign = "initial1";
        String prevHeadsign = "initial2";

        StringBuilder times = new StringBuilder("");
        for (stopTime stop : stopTimes) { // for each stop in the stopTimes array, append the relevant stop arrival and destination times
            if (stop.getStop_id().intValue() == id) {
                //for (trip busTrip : trips) {
                for(int i = 0; i < trips.size(); i++){
                    //if (busTrip.getTrip_id().intValue() == stop.getTrip_id().intValue()) {
                    if(trips.get(i).getTrip_id().intValue() == stop.getTrip_id().intValue()){
                        //times.append("Route: " + busTrip.getTrip_headsign());
                        headsign = trips.get(i).getTrip_headsign();
                        if(!(headsign.equals(prevHeadsign))) {
                            times.append("Route: " + trips.get(i).getTrip_headsign() + "\n");
                        }
                        prevHeadsign = headsign;
                        times.append("Arrival: " + stop.getArrival_time() + " Departure: " + stop.getDeparture_time() + "\n");
                        break;
                    }
                }
            }
        }
        infoBox.setText(times);
        //Toast.makeText(this, times, Toast.LENGTH_LONG).show();
    }
}