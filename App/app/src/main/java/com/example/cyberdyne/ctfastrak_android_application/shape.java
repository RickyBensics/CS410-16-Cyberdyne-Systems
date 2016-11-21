package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 11/5/2016.
 */

public class shape {
    Integer shape_id;
    Double shape_pt_lat;
    Double shape_pt_lon;
    Integer shape_pt_sequence;
    Double shape_dist_traveled;

    public shape(Integer shape_id, Double shape_pt_lat, Double shape_pt_lon, Integer shape_pt_sequence, Double shape_dist_traveled) {
        this.shape_id = shape_id;
        this.shape_pt_lat = shape_pt_lat;
        this.shape_pt_lon = shape_pt_lon;
        this.shape_pt_sequence = shape_pt_sequence;
        this.shape_dist_traveled = shape_dist_traveled;
    }

    public Integer getShape_id() {
        return shape_id;
    }

    public void setShape_id(Integer shape_id) {
        this.shape_id = shape_id;
    }

    public Double getShape_pt_lat() {
        return shape_pt_lat;
    }

    public void setShape_pt_lat(Double shape_pt_lat) {
        this.shape_pt_lat = shape_pt_lat;
    }

    public Double getShape_pt_lon() {
        return shape_pt_lon;
    }

    public void setShape_pt_lon(Double shape_pt_lon) {
        this.shape_pt_lon = shape_pt_lon;
    }

    public Integer getShape_pt_sequence() {
        return shape_pt_sequence;
    }

    public void setShape_pt_sequence(Integer shape_pt_sequence) {
        this.shape_pt_sequence = shape_pt_sequence;
    }

    public Double getShape_dist_traveled() {
        return shape_dist_traveled;
    }

    public void setShape_dist_traveled(Double shape_dist_traveled) {
        this.shape_dist_traveled = shape_dist_traveled;
    }
}
