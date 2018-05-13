package com.example.anwar.thingseev1;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by thath on 5/4/18.
 */

class Route {

    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
