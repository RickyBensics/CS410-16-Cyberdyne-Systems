package com.example.cyberdyne.ctfastrak_android_application;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Richard on 10/30/2016
 * Responsibilities:
 *  Richard:
 *      Import and parse GTFS data into Java objects
 *      Display all routes on a given day
 *      Display all busStops along that route
 *      Import and parse JSON data for real-time bus information
 *      Implement info box for bus routes, bus stops, and buses
 *  Curtis:
 *      Implement Search Bar
 *  Bart:
 *      Display and center on user location
 */

public class MapsActivity extends AppCompatActivity
        implements  LocationListener, OnInfoWindowClickListener {

    private ProgressDialog myProgress;
    private GoogleMap currentMap;

    private static final String MYTAG = "MYTAG";

    // Request Code to ask the user for permission to view their current location (***).
    // Value 8bit (value <256)
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    private static final LatLngBounds CONNECTICUT = new LatLngBounds( // restrictions for scrolling
            new LatLng(41.1, -73.65), new LatLng(42.02, -71.8)); // bottom left of map to top right of map


    private static final float DEFAULT_MIN_ZOOM = 9.0f; // restrictions for zooming
    private static final float DEFAULT_MAX_ZOOM = 22.0f;
    private float mMinZoom;
    private float mMaxZoom;
    private boolean lineSelected;

    private TextView infoBox;
    private StringBuilder info;
    private int currentDay;
    private int service_id;
    private int routeIndex;
    private String[] GTFSinfo;

    private String JSONData;
    private JSONObject obj, vehiclePosition, vehicle, position, trip;
    private JSONArray entity;
    private int RTinfoUpdated=0;
    private LinearLayout buttonList;
    private LinearLayout.LayoutParams layoutParameters;


    ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
    ArrayList<Integer> shape_ids = new ArrayList<Integer>();
    ArrayList<Integer> trip_ids = new ArrayList<Integer>();
    ArrayList<Integer> stop_ids = new ArrayList<Integer>();
    ArrayList<String> realtimeBusIds = new ArrayList<String>();
    ArrayList<String> realtimeBusIds2 = new ArrayList<String>();
    ArrayList<PolylineOptions> polylineOptions = new ArrayList<PolylineOptions>();
    ArrayList<Polyline> polylines = new ArrayList<Polyline>();
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<Marker> busMarkers = new ArrayList<Marker>();
    ArrayList<String> busMarkersIds = new ArrayList<String>();
    ArrayList<busStop> busStops = new ArrayList<busStop>();
    ArrayList<stopTime> stopTimes = new ArrayList<stopTime>();
    ArrayList<trip> trips = new ArrayList<trip>();
    ArrayList<shape> shapes = new ArrayList<shape>();
    ArrayList<route> routes = new ArrayList<route>();
    ArrayList<calendar> calendars = new ArrayList<calendar>();
    ArrayList<calendar_date> calendar_dates = new ArrayList<calendar_date>();
    ArrayList<busRT> realtimeBuses = new ArrayList<busRT>();

    final Handler h = new Handler();
    final int delay = 30000; // update every 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mMinZoom = DEFAULT_MIN_ZOOM;
        mMaxZoom = DEFAULT_MAX_ZOOM;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        // Display Progress Bar.
        myProgress.show();


        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Set callback listener, on Google Map ready.
        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });

        //new JsonTask().execute("http://65.213.12.244/realtimefeed/vehicle/vehiclepositions.json");


        AssetManager am = getAssets(); // gets files from assets folder
        InputStreamReader ims = null;
        BufferedReader reader = null;
        String line;
        try {
            ims = new InputStreamReader(am.open("calendar.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                GTFSinfo = line.split(",");
                calendars.add(new calendar(Integer.parseInt(GTFSinfo[0]),Integer.parseInt(GTFSinfo[1]),Integer.parseInt(GTFSinfo[2]),Integer.parseInt(GTFSinfo[3]),Integer.parseInt(GTFSinfo[4]),Integer.parseInt(GTFSinfo[5]),Integer.parseInt(GTFSinfo[6]),Integer.parseInt(GTFSinfo[7]),Integer.parseInt(GTFSinfo[8]),Integer.parseInt(GTFSinfo[9])));
            }
            Calendar calendarDay = Calendar.getInstance();
            currentDay = calendarDay.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, ... , 7 = Saturday
            if(currentDay > 1 && currentDay < 7) // Weekday schedule. This should not be hardcoded!!!
            {
                service_id = 1;
            }
            else if (currentDay == 7) // Saturday
            {
                service_id = 2;
            }
            else
            {
                service_id = 3; // Sunday
            }
            //service_id = 1;
            System.out.println("the current day is: " + service_id);

            ims = new InputStreamReader(am.open("trips.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                GTFSinfo = line.split(",");
                if(Integer.parseInt(GTFSinfo[1]) == service_id) // Only add the trips happenening on this day. This should improve time and memory
                {
                    if(!shape_ids.contains(Integer.parseInt(GTFSinfo[6])))
                    {
                        shape_ids.add(Integer.parseInt(GTFSinfo[6]));
                    }
                    trip_ids.add(Integer.parseInt(GTFSinfo[2]));
                    trips.add(new trip(Integer.parseInt(GTFSinfo[0]), Integer.parseInt(GTFSinfo[1]), Integer.parseInt(GTFSinfo[2]), GTFSinfo[3], Integer.parseInt(GTFSinfo[4]), GTFSinfo[5], Integer.parseInt(GTFSinfo[6])));
                }
            }
            System.out.println("Number of trips: " + trips.size());

            ims = new InputStreamReader(am.open("shapes.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                GTFSinfo = line.split(",");
                if(shape_ids.contains(Integer.parseInt(GTFSinfo[0]))) // Only add the shapes for the added trips
                {
                    shapes.add(new shape(Integer.parseInt(GTFSinfo[0]), Double.parseDouble(GTFSinfo[1]), Double.parseDouble(GTFSinfo[2]), Integer.parseInt(GTFSinfo[3]), Double.parseDouble(GTFSinfo[4])));
                }
            }
            System.out.println("Number of vertices: " + shapes.size());

            ims = new InputStreamReader(am.open("stop_times.txt"), "UTF-8"); // reads stop_times.txt and puts  comma separated values into arrayList of busStop objects
            reader = new BufferedReader(ims);
            reader.readLine(); // skip the first line of the text file
            while ((line = reader.readLine()) != null) {
                GTFSinfo = line.split(",");
                if(trip_ids.contains(Integer.parseInt(GTFSinfo[0]))) {
                    if(!stop_ids.contains(Integer.parseInt(GTFSinfo[3])))
                    {
                        stop_ids.add(Integer.parseInt(GTFSinfo[3]));
                    }
                    stopTimes.add(new stopTime(Integer.parseInt(GTFSinfo[0]), GTFSinfo[1], GTFSinfo[2], Integer.parseInt(GTFSinfo[3]), null, null, null, null, null)); // !!! Fix these null values
                }
            }
            System.out.println("Number of stop times: " + stopTimes.size());

            ims = new InputStreamReader(am.open("stops.txt"), "UTF-8"); // reads stops.txt and puts  comma separated values into arrayList of busStop objects
            reader = new BufferedReader(ims);
            reader.readLine(); // skip the first line of the text file
            while ((line = reader.readLine()) != null) {
                GTFSinfo = line.split(",");
                if(stop_ids.contains(Integer.parseInt(GTFSinfo[0])))
                {
                    busStops.add(new busStop(Integer.parseInt(GTFSinfo[0]), null, GTFSinfo[2], Double.parseDouble(GTFSinfo[3]), Double.parseDouble(GTFSinfo[4]), null, null, null, null)); // comma separated values stored in text file
                }
            }
            System.out.println("Number of stops: " + busStops.size());

            ims = new InputStreamReader(am.open("routes.txt"), "UTF-8");
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                for(trip busTrip : trips)
                    if(busTrip.getRoute_id().intValue() == Integer.parseInt(info[0])) {
                        routes.add(new route(Integer.parseInt(info[0]), null, info[2], null, null, Integer.parseInt(info[5]), null, info[7], info[8]));
                        break;
                    }
            }

            ims = new InputStreamReader(am.open("calendar_dates.txt"), "UTF-8"); // These are exceptions to the regular calendar. It is not implemented yet
            reader = new BufferedReader(ims);
            reader.readLine();
            while ((line = reader.readLine()) != null && line.length() != 0) { // the file had a newline character at the end
                String[] info = line.split(",");
                calendar_dates.add(new calendar_date(Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2])));
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // an error occurred reading file
        }



        infoBox = (TextView)findViewById(R.id.textView);

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
            polylineOptions.add(new PolylineOptions().addAll(coordinates).width(10).color(Color.parseColor(stringColor)).clickable(true));
            coordinates.clear();
        }
    }




    public void onMyMapReady(GoogleMap map) {

        map.setLatLngBoundsForCameraTarget(CONNECTICUT);
        map.setMinZoomPreference(mMinZoom);
        map.setMaxZoomPreference(mMaxZoom);

        currentMap = map;
        lineSelected = false;

        currentMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // Map loaded. Dismiss this dialog, removing it from the screen.
                myProgress.dismiss();

                askPermissionsAndShowMyLocation();
            }
        });
        currentMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        currentMap.getUiSettings().setZoomControlsEnabled(true);
        currentMap.setMyLocationEnabled(true);

        for (int i = 0; i < polylineOptions.size(); i++) {
            polylines.add(map.addPolyline(polylineOptions.get(i)));
        }

        /* LIST OF ROUTES IN INFOBOX */
        buttonList = (LinearLayout)findViewById(R.id.buttonList);
        layoutParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i < routes.size(); i++)
        {
            final Button myButton = new Button(this); // Buttons will have to be dynamically added into the scrollview instead of regular text to make the route / busStop selectable from the infoBox
            myButton.setText("Route: " + routes.get(i).getRoute_short_name());
            myButton.setId(i);

            StringBuilder color = new StringBuilder("");
            color.append("#FF" + routes.get(i).getRoute_color()); // Colors in text are stored as 16 bit hex. Convert to 32 bit with the leading to hex digits being the Alpha (transparency) value. FF = 100% 80 = 50%
            String stringColor = color.toString();
            myButton.setTextColor(Color.parseColor(stringColor));

            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(myButton.getText() + " was pressed");
                        selectPolyline(polylines.get(myButton.getId()));
                }
            });

            buttonList.addView(myButton, layoutParameters);
        }

        /* DISPLAY BUS STOPS ON POLYLINE CLICK */
        //Requires a lot of tinkering!
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() { // Add a listener for polyline clicks
            @Override
            public void onPolylineClick(Polyline polyline) {
                selectPolyline(polyline);
            }
        });

        // Set a listener for marker click.
        map.setOnInfoWindowClickListener(this);

    }

    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }

    // When you have the request results.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    // Show current location on Map.
                    this.showMyLocation();
                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // Find Location provider is openning.
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return;
        }

        // Millisecond
        final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation = null;
        try {
            // This code need permissions (Asked above ***)
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this);
            // Getting Location
            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        }
        // With Android API >= 23, need to catch SecurityException.
        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {

            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
           currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            currentMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            // Add Marker to Map
            MarkerOptions option = new MarkerOptions();
            option.title("My Location");
            option.snippet("....");
            option.position(latLng);
            Marker currentMarker = currentMap.addMarker(option);
            currentMarker.showInfoWindow();
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
        }


    }

    @Override
    public void onLocationChanged(Location location) {

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





    public void selectPolyline(Polyline polyline) {
        System.out.println("Polyline selected: " + polyline.getId());
        buttonList.removeAllViews();
        info = new StringBuilder("");
            System.out.println("# of markers before removal: " + markers.size());
            for(Marker marker : markers) {
                marker.remove();
            }
            for(Marker busMarker : busMarkers) {
                busMarker.remove();
            }
            System.out.println("# of markers before clear: " + markers.size());
            markers.clear();
            busMarkers.clear();
            System.out.println("# of markers after clear: " + markers.size());
            int i = 0;
            for (int j = 0; j < polylineOptions.size(); j++) {
                String hexColor = String.format("#%08X", (0xFFFFFFFF & polylineOptions.get(j).getColor()));
                char[] color = hexColor.toCharArray();
                if(polyline.hashCode() != polylines.get(j).hashCode()){
                    color[1] = '8';
                    color[2] = '0';
                    String stringColor = new String(color);
                    polylines.get(j).setColor(Color.parseColor(stringColor));
                    polylines.get(j).setWidth(6);
                }
                else{
                    color[1] = 'F';
                    color[2] = 'F';
                    String stringColor = new String(color);
                    polylines.get(j).setColor(Color.parseColor(stringColor));
                    polylines.get(j).setWidth(13);
                    //System.out.println("This route_id is " + routes.get(j).getRoute_id());

                    for (int k = 0; k < trips.size(); k++) {
                        if (trips.get(k).getRoute_id().intValue() == routes.get(j).getRoute_id().intValue() && trips.get(k).getService_id().intValue() == service_id) {
                            //System.out.println("Trip " + trips.get(k).getRoute_id() + " has been found on this route");
                            for (int m = 0; m < stopTimes.size(); m++) {
                                if (stopTimes.get(m).getTrip_id().intValue() == trips.get(k).getTrip_id().intValue()) {
                                    //System.out.println("Stop time found");
                                    for (int n = 0; n < busStops.size(); n++) {
                                        if (busStops.get(n).getStop_id().intValue() == stopTimes.get(m).getStop_id().intValue()) {
                                            //System.out.println("Marker added");
                                            markers.add(currentMap.addMarker(new MarkerOptions().position(new LatLng(busStops.get(n).getStop_lat(), busStops.get(n).getStop_lon())).title(busStops.get(n).getStop_name())));
                                            markers.get(markers.size()-1).setTag(n);
                                            //lineSelected = true;

                                            final Button myButton = new Button(this); // Buttons will have to be dynamically added into the scrollview instead of regular text to make the route / busStop selectable from the infoBox
                                            myButton.setText("Bus Stop: " + busStops.get(n).getStop_name());
                                            myButton.setId(i);

                                            myButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    System.out.println(myButton.getText() + " was pressed");
                                                    // Do something
                                                }
                                            });

                                            buttonList.addView(myButton, layoutParameters);

                                            i++;
                                        }
                                    }
                                }
                            };
                            routeIndex = j;
                            busMarkers.clear();
                            busMarkersIds.clear();
                            showBusesOnRoute(routeIndex);
                            RTinfoUpdated = 1;
                            h.postDelayed(new Runnable(){
                                public void run(){
                                    showBusesOnRoute(routeIndex);
                                    h.postDelayed(this, delay);
                                }
                            }, delay);
                            info.append("Route: " + routes.get(j).getRoute_short_name() + "\n");
                            info.append("# of buses active on this route : " + busMarkers.size() + "\n");
                            break;
                        }
                    }
                }
            }
            //System.out.println("lineSelected change to " + lineSelected);
            infoBox.setText(info);
    }




    public void onButtonClick(View v) {
        if(v.getId() == R.id.searchButton) {
            String compareSrch;
            EditText search = (EditText)findViewById(R.id.searchBox);

            compareSrch = search.getText().toString();

            for(Marker stopMarker : markers){ // This will only work if the route with the bus stop is selected. The search must be able to find the bus stop if the route is not selected. This should probably use busStops arraylist instead. Also, how about searching for routes too?
                if(compareSrch.equalsIgnoreCase(stopMarker.getTitle())){ // It's unlikely that the user will enter the busStop title word for word. maybe use String contains() in the case that the user enters a substring of a busStop name, and the user can select from a list of stops that contain that substring
                    currentMap.animateCamera(CameraUpdateFactory.newLatLng(stopMarker.getPosition()));
                    stopMarker.showInfoWindow();
                    break;
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Integer markerId = (Integer) marker.getTag(); //We need to get the relevant busStop info knowing which markerTag was selected
        int id = busStops.get(markerId).getStop_id();
        String headsign = "initial1";
        String prevHeadsign = "initial2";

        info = new StringBuilder("");
        for (stopTime stop : stopTimes) { // for each stop in the stopTimes array, append the relevant stop arrival and destination times
            if (stop.getStop_id().intValue() == id) {
                for(int i = 0; i < trips.size(); i++){
                    if(trips.get(i).getTrip_id().intValue() == stop.getTrip_id().intValue() && trips.get(i).getService_id().intValue() == service_id){
                        headsign = trips.get(i).getTrip_headsign();
                        if(!(headsign.equals(prevHeadsign))) { // The logic here assumes that headsigns are grouped together in GTFS file, which it isn't, so the logic has to be modified here
                            info.append("Route: " + trips.get(i).getTrip_headsign() + "\n");
                        }
                        prevHeadsign = headsign;
                        info.append("Arrival: " + stop.getArrival_time() + " Departure: " + stop.getDeparture_time() + "\n");
                        break;
                    }
                }
            }
        }
        infoBox.setText(info);
    }





    private class JsonTask extends AsyncTask<String, String, String> { // all JSON parsing logic goes here

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }
                //JSONData = buffer.toString();

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int tabs = 0;
            info = new StringBuilder("");
            info.append(result);
            for(int i = 0; i < info.length(); i++) // This is just for debugging readability purposes
            {
                if(info.charAt(i) == ',' || info.charAt(i) == '{' || info.charAt(i) == '[') {
                    for(int j = 0; j < tabs; j++) {
                        info.insert(i + 1, '\t');
                    }
                    info.insert(i + 1, '\n');
                    if(info.charAt(i) == '{') {
                        tabs += 1;
                    }
                }
                if(info.charAt(i) == '}')
                    tabs -= 1;
            }
            //infoBox.setText(info);
            JSONData = result;

            try { // Not sure if it makes a difference when this goes in doInBackground or onPostExecute
                System.out.println("Printing out JSONData: " + JSONData);
                    obj = new JSONObject(JSONData); // JSON objects are surrounded by { }
                    entity = obj.getJSONArray("entity"); // JSON Arrays are surrounded by [ ]
                realtimeBusIds2.clear();
                for (int i = 0; i < entity.length(); ++i) {
                    vehiclePosition = entity.getJSONObject(i);
                    //System.out.println(vehiclePosition.toString());
                    realtimeBusIds2.add(vehiclePosition.getString("id").toString()); // Gets all ids within JSON URL
                    if(!realtimeBusIds.contains(vehiclePosition.getString("id").toString())) // A new vehicle appears and must be added
                    {
                        vehicle = new JSONObject(vehiclePosition.getString("vehicle"));
                        position = new JSONObject(vehicle.getString("position"));
                        trip = new JSONObject(vehicle.getString("trip"));
                        realtimeBuses.add(new busRT(vehiclePosition.getString("alert"), vehiclePosition.getString("id"),  vehiclePosition.getString("trip_update"), Double.parseDouble(position.getString("latitude")), Double.parseDouble(position.getString("longitude")), Long.parseLong(vehicle.getString("timestamp")), Integer.parseInt(trip.getString("route_id")), Integer.parseInt(trip.getString("schedule_relationship")), Long.parseLong(trip.getString("start_date")), Integer.parseInt(trip.getString("trip_id"))));
                        realtimeBusIds.add(vehiclePosition.getString("id").toString());
                    }
                    else // update an existing bus location
                    {
                        vehicle = new JSONObject(vehiclePosition.getString("vehicle"));
                        position = new JSONObject(vehicle.getString("position"));
                        trip = new JSONObject(vehicle.getString("trip"));
                        for(int j = 0; j < realtimeBuses.size(); j++)
                        {
                            if(realtimeBuses.get(j).getId().toString().equals(vehiclePosition.getString("id").toString())) {
                                realtimeBuses.get(j).setLatitude(Double.parseDouble(position.getString("latitude")));
                                realtimeBuses.get(j).setLongitude(Double.parseDouble(position.getString("longitude")));
                            }
                        }
                    }
                }
                System.out.println("# of bus ids: " + realtimeBusIds.size());
                int n = realtimeBusIds.size();
                for(int i = 0; i < realtimeBusIds.size(); i++) // if bus is no longer there, delete it. Not entirely sure this works
                {
                    if(!realtimeBusIds2.contains(realtimeBusIds.get(i).toString())) {
                        realtimeBusIds.remove(i);
                        realtimeBuses.remove(i);
                    }
                }
                /*System.out.println("# of bus ids after remove: " + realtimeBusIds.size()); // Debugging purposes
                System.out.println("First list of buses");
                for(int i = 0; i < realtimeBusIds.size(); i++)
                    System.out.println(realtimeBusIds.get(i).toString());
                System.out.println("Second list buses");
                for(int i = 0; i < realtimeBusIds2.size(); i++)
                    System.out.println(realtimeBusIds2.get(i).toString());*/
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public void showBusesOnRoute(int routeIndex)
    {
        ArrayList<String> busIDsOnRoute = new ArrayList<String>();

        new JsonTask().execute("http://65.213.12.244/realtimefeed/vehicle/vehiclepositions.json");

        System.out.println("# of buses out there: " + realtimeBuses.size());

        if(realtimeBuses.size()!= 0) {

            for(int i = 0; i < realtimeBuses.size(); i++) // Get an array of all buses on route
                if(routes.get(routeIndex).getRoute_id().intValue() == realtimeBuses.get(i).getRoute_id().intValue())
                    busIDsOnRoute.add(realtimeBuses.get(i).getId().toString());

            if (busMarkersIds.isEmpty()) { // add markers when the line is clicked
                for (int i = 0; i < realtimeBuses.size(); i++)
                    if (routes.get(routeIndex).getRoute_id().intValue() == realtimeBuses.get(i).getRoute_id().intValue()) {
                        busMarkers.add(currentMap.addMarker(new MarkerOptions().position(new LatLng(realtimeBuses.get(i).getLatitude(), realtimeBuses.get(i).getLongitude())).title(realtimeBuses.get(i).getId().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                        busMarkersIds.add(realtimeBuses.get(i).getId().toString());
                    }
            }
            else {
                busMarkersIds.retainAll(busIDsOnRoute); // arraylist is now just an arraylist of pre-existing busMarkers
                for(int i = 0; i < busMarkers.size(); i++) {
                    if (busMarkersIds.contains(busMarkers.get(i).getTitle())) { // animate existing busMarker
                        for(int j = 0; j < realtimeBuses.size(); j++)
                                if(busMarkers.get(i).getTitle().compareTo(realtimeBuses.get(j).getId().toString())==0) {
                                    animateMarker(busMarkers.get(i), new LatLng(realtimeBuses.get(j).getLatitude(), realtimeBuses.get(j).getLongitude()), false);
                                }
                    }
                    else {
                        //animateMarker(busMarkers.get(i), new LatLng(0, 0), true);
                        busMarkers.get(i).setVisible(false);
                        busMarkers.remove(i); // remove a bus that has disappeared
                    }

                }
                for(int j = 0; j < busIDsOnRoute.size(); j++) { // add new bus markers
                    if(!busMarkersIds.contains(busIDsOnRoute.get(j))) {
                        for (int i = 0; i < realtimeBuses.size(); i++)
                            if (routes.get(routeIndex).getRoute_id().intValue() == realtimeBuses.get(i).getRoute_id().intValue()) {
                                busMarkers.add(currentMap.addMarker(new MarkerOptions().position(new LatLng(realtimeBuses.get(i).getLatitude(), realtimeBuses.get(i).getLongitude())).title(realtimeBuses.get(i).getId().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                                busMarkersIds.add(realtimeBuses.get(i).getId().toString());
                            }
                    }
                }
            }
        }

    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = currentMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

}

