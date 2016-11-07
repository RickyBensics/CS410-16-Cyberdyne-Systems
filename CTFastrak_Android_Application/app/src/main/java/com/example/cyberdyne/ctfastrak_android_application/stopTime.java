package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 11/5/2016.
 */

public class stopTime {
    Integer trip_id;
    String arrival_time;
    String departure_time;
    Integer stop_id;
    Integer stop_sequence;
    Integer stop_headsign;
    Integer pickup_type;
    Integer drop_off_type;
    Double shape_dist_traveled;

    public stopTime(Integer trip_id, String arrival_time, String departure_time, Integer stop_id, Integer stop_sequence, Integer stop_headsign, Integer pickup_type, Integer drop_off_type, Double shape_dist_traveled) {
        this.trip_id = trip_id;
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
        this.stop_id = stop_id;
        this.stop_sequence = stop_sequence;
        this.stop_headsign = stop_headsign;
        this.pickup_type = pickup_type;
        this.drop_off_type = drop_off_type;
        this.shape_dist_traveled = shape_dist_traveled;
    }

    public Integer getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(Integer trip_id) {
        this.trip_id = trip_id;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    public Integer getStop_id() {
        return stop_id;
    }

    public void setStop_id(Integer stop_id) {
        this.stop_id = stop_id;
    }

    public Integer getStop_sequence() {
        return stop_sequence;
    }

    public void setStop_sequence(Integer stop_sequence) {
        this.stop_sequence = stop_sequence;
    }

    public Integer getStop_headsign() {
        return stop_headsign;
    }

    public void setStop_headsign(Integer stop_headsign) {
        this.stop_headsign = stop_headsign;
    }

    public Integer getPickup_type() {
        return pickup_type;
    }

    public void setPickup_type(Integer pickup_type) {
        this.pickup_type = pickup_type;
    }

    public Integer getDrop_off_type() {
        return drop_off_type;
    }

    public void setDrop_off_type(Integer drop_off_type) {
        this.drop_off_type = drop_off_type;
    }

    public Double getShape_dist_traveled() {
        return shape_dist_traveled;
    }

    public void setShape_dist_traveled(Double shape_dist_traveled) {
        this.shape_dist_traveled = shape_dist_traveled;
    }
}
