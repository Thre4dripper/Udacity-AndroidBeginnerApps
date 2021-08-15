package com.example.android.quakereport;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Earthquake {
    double magnitude;
    String location;
    String dateToDisplay;
    String link;

    public Earthquake(double magnitude, String location, long time, String link) {
        this.magnitude = magnitude;
        this.location = location;

        dateToDisplay = new SimpleDateFormat("MMM dd, yyyy HH:mm a").format(new Date(time));
        this.link = link;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public String getLocation() {
        return location;
    }

    public String getDateToDisplay() {
        return dateToDisplay;
    }

    public String getLink() {
        return link;
    }
}
