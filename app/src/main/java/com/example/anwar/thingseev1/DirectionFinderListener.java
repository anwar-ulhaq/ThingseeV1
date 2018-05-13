package com.example.anwar.thingseev1;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import com.example.anwar.thingseev1.Route;

/**
 * Created by thath on 5/4/18.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
