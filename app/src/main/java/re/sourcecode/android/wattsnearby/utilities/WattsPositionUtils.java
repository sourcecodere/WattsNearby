package re.sourcecode.android.wattsnearby.utilities;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by olem on 4/10/17.
 */

public class WattsPositionUtils {

    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String DISTANCE_KEY = "distance";


    /**
     *  This util get the distance, latitude and longitude of the current map view
     *
     * @param visibleRegion of the current map view
     * @return hashmap of distance, latitude and longitude
     */
    public static Map<String, Double> getLatLngDistFromVisibleRegion(VisibleRegion visibleRegion) {

        Double distance;
        Double latitude;
        Double longitude;
        Map<String, Double> retMap = new HashMap<>();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farRight.latitude + nearRight.latitude)/2,
                (farRight.longitude + nearRight.longitude)/2,
                (farLeft.latitude + nearLeft.latitude)/2,
                (farLeft.longitude + nearLeft.longitude)/2,
                distanceWidth
        );

        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceHeight
        );

        if (distanceWidth[0] > distanceHeight[0]) {
            distance = (double) distanceWidth[0];
        } else {
            distance = (double) distanceHeight[0];
        }

        latitude = visibleRegion.latLngBounds.getCenter().latitude;
        longitude = visibleRegion.latLngBounds.getCenter().longitude;

        retMap.put(DISTANCE_KEY, distance);
        retMap.put(LATITUDE_KEY, latitude);
        retMap.put(LONGITUDE_KEY, longitude);

        return retMap;
    }


}
