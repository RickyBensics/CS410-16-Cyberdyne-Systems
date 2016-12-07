package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 11/20/2016.
 */

public class busRT {
    String alert;
    String id;
    String trip_update;
    Double latitude;
    Double longitude;
    Long timestamp;
    Integer route_id;
    Integer schedule_relationship;
    Long start_date;
    String trip_id;

    public busRT(String alert, String id, String trip_update, Double latitude, Double longitude, Long timestamp, Integer route_id, Integer schedule_relationship, Long start_date, String trip_id) {

        this.alert = alert;
        this.id = id;
        this.trip_update = trip_update;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.route_id = route_id;
        this.schedule_relationship = schedule_relationship;
        this.start_date = start_date;
        this.trip_id = trip_id;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrip_update() {
        return trip_update;
    }

    public void setTrip_update(String trip_update) {
        this.trip_update = trip_update;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getRoute_id() {
        return route_id;
    }

    public void setRoute_id(Integer route_id) {
        this.route_id = route_id;
    }

    public Integer getSchedule_relationship() {
        return schedule_relationship;
    }

    public void setSchedule_relationship(Integer schedule_relationship) {
        this.schedule_relationship = schedule_relationship;
    }

    public Long getStart_date() {
        return start_date;
    }

    public void setStart_date(Long start_date) {
        this.start_date = start_date;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

}
