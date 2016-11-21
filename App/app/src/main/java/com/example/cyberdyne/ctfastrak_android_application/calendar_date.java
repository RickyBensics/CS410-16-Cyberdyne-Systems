package com.example.cyberdyne.ctfastrak_android_application;

/**
 * Created by Richard on 11/18/2016.
 */

public class calendar_date {
    Integer service_id;
    Integer date;
    Integer exception_type;

    public calendar_date(Integer service_id, Integer date, Integer exception_type) {
        this.service_id = service_id;
        this.date = date;
        this.exception_type = exception_type;
    }

    public Integer getService_id() {
        return service_id;
    }

    public void setService_id(Integer service_id) {
        this.service_id = service_id;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getException_type() {
        return exception_type;
    }

    public void setException_type(Integer exception_type) {
        this.exception_type = exception_type;
    }
}
