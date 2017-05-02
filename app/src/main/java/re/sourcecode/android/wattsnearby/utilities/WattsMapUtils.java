package re.sourcecode.android.wattsnearby.utilities;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.data.WattsPreferences;

/**
 * Created by olem on 4/19/17.
 */

public class WattsMapUtils {

    private static final String TAG = WattsMapUtils.class.getSimpleName();

    /* The data we need to get for each marker */
    public static final String[] STATION_MARKER_PROJECTION = {
            ChargingStationContract.StationEntry.COLUMN_ID,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_LAT,
            ChargingStationContract.StationEntry.COLUMN_LON,
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_ID = 0;
    public static final int INDEX_ADDRESS_TITLE = 1;
    public static final int INDEX_LAT = 2;
    public static final int INDEX_LON = 3;

    /* The data we need to check for fast charging capacity */
    private static final String[] CONNECTIO_CAPACITY_PROJECTION = {
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST
    };
    private static final int INDEX_STATION_FAST = 0;


    /**
     * Adds the station markers for the current visible region from the content provider.
     * Also removes markers outside of the current visible region.
     *
     * @param context current app context
     * @param map     is the current map shown in the app
     */
    public static void updateStationMarkers(Context context,
                                            GoogleMap map,
                                            HashMap<Long, Marker> visibleStationMarkers,
                                            BitmapDescriptor iconStation,
                                            BitmapDescriptor iconStationFast) {

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
                String stationTitle = getStationsCursor.getString(INDEX_ADDRESS_TITLE);


                if (visibleBounds.contains(stationPosition)) {
                    //Log.d(TAG, "Id: " + getStationsCursor.getLong(INDEX_ID) + " within bounds");
                    if (!visibleStationMarkers.containsKey(stationId)) {
                        Marker tmpMarker;
                        if (checkForFastCharging(wattsContentResolver, stationId)) {
                            tmpMarker = map.addMarker(WattsImageUtils.getStationMarkerOptions(stationPosition, stationTitle, iconStationFast));
                        } else {
                            tmpMarker = map.addMarker(WattsImageUtils.getStationMarkerOptions(stationPosition, stationTitle, iconStation));
                        }
                        tmpMarker.setTag(stationId); // save the station id directly on the marker as a tag.
                        visibleStationMarkers.put(stationId, tmpMarker); // save the stationId and marker in hashTable for house cleaning
                    }

                } else {
                    //Log.d(TAG, "Id: " + getStationsCursor.getLong(INDEX_ID) + " outside of bounds");
                    if (visibleStationMarkers.containsKey(stationId)) {
                        // Remove the marker from the map (value)
                        visibleStationMarkers.get(stationId).remove();
                        // Remove the reference in the hashmap (key)

                        visibleStationMarkers.remove(stationId);
                    }
                }
            }

        } finally

        {
            getStationsCursor.close();
        }

    }

    /**
     * Checks database for fast charging capacity at certain station
     *
     * @param stationId
     * @return true or false
     */
    private static Boolean checkForFastCharging(ContentResolver wattsContentResolver, Long stationId) {

        Uri getConnectionUri = ChargingStationContract.ConnectionEntry.CONTENT_URI;

        Cursor getConnectionsCursor = wattsContentResolver.query(
                getConnectionUri,
                CONNECTIO_CAPACITY_PROJECTION,
                ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID + "=?",
                new String[]{Long.toString(stationId)},
                null
        );

        try {
            while (getConnectionsCursor.moveToNext()) {
                if (getConnectionsCursor.getInt(INDEX_STATION_FAST) > 0) {
                    return true;
                }
            }
        } finally {
            getConnectionsCursor.close();
        }

        return false;

    }

    /**
     * Checks database for charing station outlets
     *
     * @param stationId
     * @return hasmap of boolean charing types
     */
    //TODO:
}