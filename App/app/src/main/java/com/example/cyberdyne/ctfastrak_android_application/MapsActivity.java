package com.example.cyberdyne.ctfastrak_android_application;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
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

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private StringBuilder info;
    private int currentDay;
    private int service_id;
    private int routeIndex;
    private String[] GTFSinfo;

    private String JSONData;
    ProgressDialog pd;
    private JSONObject obj, vehiclePosition, vehicle, position, trip;
    private JSONArray entity;
    private int RTinfoUpdated = 0;

    ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
    ArrayList<Integer> shape_ids = new ArrayList<Integer>();
    ArrayList<Integer> trip_ids = new ArrayList<Integer>();
    ArrayList<Integer> stop_ids = new ArrayList<Integer>();
    ArrayList<String> realtimeBusIds = new ArrayList<String>();
    ArrayList<String> realtimeBusIds2 = new ArrayList<String>();
    ArrayList<PolylineOptions> polylines = new ArrayList<PolylineOptions>();
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<Marker> busMarkers = new ArrayList<Marker>();
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
                routes.add(new route(Integer.parseInt(info[0]), null, info[2], null, null, Integer.parseInt(info[5]), null, info[7], info[8]));
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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMinZoom = DEFAULT_MIN_ZOOM;
        mMaxZoom = DEFAULT_MAX_ZOOM;

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        infoBox = (TextView)findViewById(R.id.textView);



        /*Button myButton = new Button(this); // Buttons will have to be dynamically added into the scrollview instead of regular text to make the route / busStop selectable from the infoBox
        myButton.setText("Push Me");

        LinearLayout ll = (LinearLayout)findViewById(R.id.buttonLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.addView(myButton, lp);*/


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
            //System.out.println(stringColor);
            polylines.add(new PolylineOptions().addAll(coordinates).width(10).color(Color.parseColor(stringColor)).clickable(true));
            coordinates.clear();
        }
    }



    @Override
    public void onMapReady(GoogleMap map) {

        map.setLatLngBoundsForCameraTarget(CONNECTICUT);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CONNECTICUT_CAMERA));
        map.setMinZoomPreference(mMinZoom);
        map.setMaxZoomPreference(mMaxZoom);

        currentMap = map;
        lineSelected = false;

        lineHashCodes = new int[polylines.size()];
        for (int i = 0; i < polylines.size(); i++) {
            lineHashCodes[i] = map.addPolyline(polylines.get(i)).hashCode();
        }

        /* DISPLAY BUS STOPS ON POLYLINE CLICK */
        //Requires a lot of tinkering!
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() { // Add a listener for polyline clicks
            @Override
            public void onPolylineClick(Polyline polyline) {
                info = new StringBuilder("");
                if (lineSelected) { // This will never occur the first time a route is selected
                    //System.out.println("lineSelected is already " + lineSelected);
                    for (int j = 0; j < markers.size(); j++) {
                        markers.get(j).remove();
                        lineSelected = false;
                    }
                }
                if (!lineSelected) {
                    currentMap.clear(); // redraw polylines. The lines not selected will be made transparent and only the one selected will display bus stop markers
                    markers.clear();
                    int i = 0;
                    for (int j = 0; j < polylines.size(); j++) {
                        String hexColor = String.format("#%08X", (0xFFFFFFFF & polylines.get(j).getColor()));
                        char[] color = hexColor.toCharArray();
                        if(polyline.hashCode() != lineHashCodes[j]){
                            color[1] = '8';
                            color[2] = '0';
                            String stringColor = new String(color);
                            lineHashCodes[j] = currentMap.addPolyline(polylines.get(j).color(Color.parseColor(stringColor)).width(6)).hashCode();
                        }
                        else{
                            color[1] = 'F';
                            color[2] = 'F';
                            String stringColor = new String(color);
                            lineHashCodes[j] = currentMap.addPolyline(polylines.get(j).color(Color.parseColor(stringColor)).width(13)).hashCode();
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
                                                    Marker stopMarker = currentMap.addMarker(new MarkerOptions().position(new LatLng(busStops.get(n).getStop_lat(), busStops.get(n).getStop_lon())).title(busStops.get(n).getStop_name()));
                                                    stopMarker.setTag(n);
                                                    markers.add(stopMarker);
                                                    i++;
                                                    lineSelected = true;
                                                }
                                            }
                                        }
                                    };
                                    routeIndex = j;
                                    showBusesOnRoute(j);
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
            }
        });

        // Set a listener for marker click.
        map.setOnInfoWindowClickListener(this);

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
                //for (trip busTrip : trips) {
                for(int i = 0; i < trips.size(); i++){
                    //if (busTrip.getTrip_id().intValue() == stop.getTrip_id().intValue()) {
                    if(trips.get(i).getTrip_id().intValue() == stop.getTrip_id().intValue() && trips.get(i).getService_id().intValue() == service_id){
                        //times.append("Route: " + busTrip.getTrip_headsign());
                        headsign = trips.get(i).getTrip_headsign();
                        if(!(headsign.equals(prevHeadsign))) { // The logic here assumes that headsigns are grouped together in GTFS file, which it isn't, so the logic has to be modifiedhere
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
                System.out.println("# of bus ids after remove: " + realtimeBusIds.size()); // Debugging purposes
                System.out.println("First list of buses");
                for(int i = 0; i < realtimeBusIds.size(); i++)
                    System.out.println(realtimeBusIds.get(i).toString());
                System.out.println("Second list buses");
                for(int i = 0; i < realtimeBusIds2.size(); i++)
                    System.out.println(realtimeBusIds2.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public void showBusesOnRoute(int j)
    {
        new JsonTask().execute("http://65.213.12.244/realtimefeed/vehicle/vehiclepositions.json");

        System.out.println("# of buses out there: " + realtimeBuses.size());
        if(busMarkers.size() !=0) { // The logic here must be changed. Instead of clearing every busMarker each time interval, the positions of existing realtime buses must be updated instead, and others removed or added if necessary
            for(int i = 0; i < busMarkers.size(); i++)
                busMarkers.get(i).remove();
        }
        busMarkers.clear();
        if(realtimeBuses.size()!= 0) {
            for(int i = 0; i < realtimeBuses.size(); i++)
            {
                if(routes.get(j).getRoute_id().intValue() == realtimeBuses.get(i).getRoute_id().intValue())
                    busMarkers.add(currentMap.addMarker(new MarkerOptions().position(new LatLng(realtimeBuses.get(i).getLatitude(), realtimeBuses.get(i).getLongitude())).title(realtimeBuses.get(i).getId().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
            }
        }
    }
}

