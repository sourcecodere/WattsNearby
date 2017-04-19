package re.sourcecode.android.wattsnearby.utilities;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * Created by olem on 4/19/17.
 */

public class WattsMapUtils {

    private static final String TAG = WattsMapUtils.class.getSimpleName();

    /* ContentResolver for query, updates and inserts */
    private static ContentResolver mWattsContentResolver;

    /* The data we need to get for each marker */
    public static final String[] STATION_MARKER_PROJECTION = {
            ChargingStationContract.StationEntry.COLUMN_ID,
            ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_LAT,
            ChargingStationContract.StationEntry.COLUMN_LON,
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_ID = 0;
    public static final int INDEX_OPERATOR_TITLE = 1;
    public static final int INDEX_LAT = 2;
    public static final int INDEX_LON = 3;

    /**
     * @return MarkerOptions for the car
     */
    public static MarkerOptions getCarMarkerOptions(LatLng position, String title, BitmapDescriptor markerIcon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.icon(markerIcon);
        markerOptions.anchor(0.38f, 0.6f);
        markerOptions.position(position);
        return markerOptions;
    }

    /**
     * @return MarkerOptions for the stations
     */
    public static MarkerOptions getStationMarkerOptions(LatLng position, String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.position(position);
        return markerOptions;
    }

    /**
     * Adds the station markers for the current visible region from the content provider.
     * Also removes markers outside of the current visible region.
     *
     * @param context current app context
     * @param map     is the current map shown in the app
     */
    public static void updateStationMarkers(Context context, GoogleMap map, HashMap<Long, Marker> visibleStationMarkers) {

        LatLngBounds visibleBounds = map.getProjection().getVisibleRegion().latLngBounds;

        ContentResolver wattsContentResolver = context.getContentResolver();

        Uri getStationsUri = ChargingStationContract.StationEntry.CONTENT_URI;

        Cursor getStationsCursor = wattsContentResolver.query(
                getStationsUri,
                STATION_MARKER_PROJECTION,
                null,
                null,
                null);

        try {
            while (getStationsCursor.moveToNext()) {

                Long stationId = getStationsCursor.getLong(INDEX_ID);
                LatLng stationPosition = new LatLng(
                        getStationsCursor.getDouble(INDEX_LAT),
                        getStationsCursor.getDouble(INDEX_LON));
                String stationTitle = getStationsCursor.getString(INDEX_OPERATOR_TITLE);


                if (visibleBounds.contains(stationPosition)) {
                    //Log.d(TAG, "Id: " + getStationsCursor.getLong(INDEX_ID) + " within bounds");
                    if (!visibleStationMarkers.containsKey(stationId)) {
                        visibleStationMarkers.put(
                                stationId,
                                map.addMarker(getStationMarkerOptions(stationPosition, stationTitle))
                        );
                    }

                } else {
                    //Log.d(TAG, "Id: " + getStationsCursor.getLong(INDEX_ID) + " outside of bounds");
                    if (visibleStationMarkers.containsKey(stationId)) {
                        // Remove the marker from the map (value)
                        visibleStationMarkers.get(stationId).remove();
                        // Remove the reference in the hashmap (key)
                        visibleStationMarkers.remove(stationId);
                    }
                    // Remove the marker from the map

                }
            }

        } finally {
            getStationsCursor.close();
        }

    }
}