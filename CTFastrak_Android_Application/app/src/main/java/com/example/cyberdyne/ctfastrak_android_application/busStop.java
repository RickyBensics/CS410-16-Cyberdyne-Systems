package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 10/30/2016.
 */

public class busStop {
    Double stop_lat;
    String stop_code;
    Double stop_lon;
    Integer stop_id;
    String stop_url;
    String parent_station;
    String stop_name;
    String location_type;
    String zone_id;

    public busStop(Integer stop_id, String stop_code, String stop_name, Double stop_lat, Double stop_lon,  String zone_id, String stop_url, String location_type, String parent_station) {
        this.stop_lat = stop_lat;
        this.stop_code = stop_code;
        this.stop_lon = stop_lon;
        this.stop_id = stop_id;
        this.stop_url = stop_url;
        this.parent_station = parent_station;
        this.stop_name = stop_name;
        this.location_type = location_type;
        this.zone_id = zone_id;
    }

    public Double getStop_lat() {
        return stop_lat;
    }

    public String getStop_code() {
        return stop_code;
    }

    public Double getStop_lon() {
        return stop_lon;
    }

    public Integer getStop_id() {
        return stop_id;
    }

    public String getParent_station() {
        return parent_station;
    }

    public String getStop_url() {
        return stop_url;
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getLocation_type() {
        return location_type;
    }

    public String getZone_id() {
        return zone_id;
    }

    public void setStop_lat(Double stop_lat) {
        this.stop_lat = stop_lat;
    }

    public void setStop_code(String stop_code) {
        this.stop_code = stop_code;
    }

    public void setStop_lon(Double stop_lon) {
        this.stop_lon = stop_lon;
    }

    public void setStop_id(Integer stop_id) {
        this.stop_id = stop_id;
    }

    public void setParent_station(String parent_station) {
        this.parent_station = parent_station;
    }

    public void setStop_url(String stop_url) {
        this.stop_url = stop_url;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public void setLocation_type(String location_type) {
        this.location_type = location_type;
    }

    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }
}