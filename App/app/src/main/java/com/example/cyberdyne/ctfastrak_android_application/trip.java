package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 11/5/2016.
 */

public class trip implements java.io.Serializable{
    String block_id;
    Integer route_id;
    Integer direction_id;
    String trip_headsign;
    Integer shape_id;
    Integer service_id;
    Integer trip_id;

    public trip(Integer route_id, Integer service_id, Integer trip_id, String trip_headsign, Integer direction_id, String block_id, Integer shape_id) {
        this.block_id = block_id;
        this.route_id = route_id;
        this.direction_id = direction_id;
        this.trip_headsign = trip_headsign;
        this.shape_id = shape_id;
        this.service_id = service_id;
        this.trip_id = trip_id;
    }

    public String getBlock_id() {
        return block_id;
    }

    public void setBlock_id(String block_id) {
        this.block_id = block_id;
    }

    public Integer getRoute_id() {
        return route_id;
    }

    public void setRoute_id(Integer route_id) {
        this.route_id = route_id;
    }

    public Integer getDirection_id() {
        return direction_id;
    }

    public void setDirection_id(Integer direction_id) {
        this.direction_id = direction_id;
    }

    public String getTrip_headsign() {
        return trip_headsign;
    }

    public void setTrip_headsign(String trip_headsign) {
        this.trip_headsign = trip_headsign;
    }

    public Integer getShape_id() {
        return shape_id;
    }

    public void setShape_id(Integer shape_id) {
        this.shape_id = shape_id;
    }

    public Integer getService_id() {
        return service_id;
    }

    public void setService_id(Integer service_id) {
        this.service_id = service_id;
    }

    public Integer getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(Integer trip_id) {
        this.trip_id = trip_id;
    }
}
