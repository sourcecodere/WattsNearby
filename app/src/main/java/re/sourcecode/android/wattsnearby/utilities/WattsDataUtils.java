package re.sourcecode.android.wattsnearby.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.data.WattsPreferences;

/**
 * Created by olem on 5/5/17.
 */

public class WattsDataUtils {


    /**
     *
     * Helper method to filter out connections not matching user preferences.
     *
     *
     * @param context application contex
     * @param wattsContentResolver content resolver
     * @param stationId the station id
     * @param projection for columns in table
     * @return a filtered cursor of connections at a station
     *
     */
    public static Cursor getConnectionsFilteredByPrefs(Context context, ContentResolver wattsContentResolver, Long stationId, String[] projection) {

        final Uri getConnectionUri = ChargingStationContract.ConnectionEntry.CONTENT_URI;

        //IDs from https://api.openchargemap.io/v2/referencedata/
        String selection = null;
        List<String> selectionArgsArray = new ArrayList<>();

        // first filter on station id
        selection = ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID + "=?";
        selectionArgsArray.add(stationId.toString());

        //filter out if only fast chargers are enabled
        if(WattsPreferences.areOnlyFastChargersEnabled(context)) {
            selection += " AND " + ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST + "=?";
            selectionArgsArray.add("1"); // 1 is true
        }

        String subSelection = null;
        // filter out if other connections is enabled
        if (WattsPreferences.areOtherInputsEnabled(context)) {
            // first
            subSelection = " AND  (" + ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + " NOT IN (?, ?, ?, ?, ?, ?)";
            selectionArgsArray.add("2");
            selectionArgsArray.add("33");
            selectionArgsArray.add("30");
            selectionArgsArray.add("25");
            selectionArgsArray.add("32");
            selectionArgsArray.add("1");
        }

        // filter out the explicit connections from preferences
        if (WattsPreferences.areChademoEnabled(context)) {
            // ID 2
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("2");
        }
        if (WattsPreferences.areComboCcsEuEnabled(context)) {
            // ID 33
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("33");
        }
        if (WattsPreferences.areTeslaHpwcEnabled(context)) {
            // ID 30
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("30");
        }
        if (WattsPreferences.areType2MenneskeEnabled(context)) {
            // ID 25
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("25");
        }
        if (WattsPreferences.areType1CcsEnabled(context)) {
            // ID 32
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("32");
        }
        if (WattsPreferences.areType1j1772Enabled(context)) {
            // ID 1
            if (subSelection == null) {
                subSelection = " AND (";
            } else {
                subSelection += " OR ";
            }
            subSelection += ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + "=?";
            selectionArgsArray.add("1");
        }
        if (subSelection != null) {
            subSelection += ")";
            selection += subSelection;
        }

        //convert selection args to string array
        String[] selectionArgs = new String[selectionArgsArray.size()];
        selectionArgs = selectionArgsArray.toArray(selectionArgs);

        // run the query
        Cursor getConnectionsCursor = wattsContentResolver.query(
                getConnectionUri,
                projection,
                selection,
                selectionArgs,
                null
        );

        return getConnectionsCursor;
    }

    /**
     * Checks database for fast charging capacity at certain station
     *
     * @param stationId
     * @return true or false
     */
    public static Boolean checkFastChargingAtStation(ContentResolver wattsContentResolver, Long stationId) {

        /* The data we need to check for fast charging capacity */
        final String[] CONNECTION_CAPACITY_PROJECTION = {
                ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST
        };

        final int INDEX_STATION_FAST = 0;

        final Uri getConnectionUri = ChargingStationContract.ConnectionEntry.CONTENT_URI;

        Cursor getConnectionsCursor = wattsContentResolver.query(
                getConnectionUri,
                CONNECTION_CAPACITY_PROJECTION,
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
     * Checks database for charging station and return LatLng for certain station
     *
     * @param context
     * @param stationId
     * @return LatLng of statioin
     */
    public static LatLng getStationLatLng(Context context, Long stationId) {

        /* The data we need to get LatLng of a station */
        final String[] STATION_LATLNG_PROJECTION = {
                ChargingStationContract.StationEntry.COLUMN_LAT,
                ChargingStationContract.StationEntry.COLUMN_LON
        };

        final int INDEX_STATION_LAT = 0;
        final int INDEX_STATIO_LON = 1;

        final Uri getStationUri = ChargingStationContract.StationEntry.buildStationUri(stationId);

        Cursor getLatLngCursor = context.getContentResolver().query(
                getStationUri,
                STATION_LATLNG_PROJECTION,
                null,
                null,
                null
                );
        try {
            getLatLngCursor.moveToFirst();
            return new LatLng( getLatLngCursor.getDouble(INDEX_STATION_LAT), getLatLngCursor.getDouble(INDEX_STATIO_LON));
        } finally {
            getLatLngCursor.close();
        }
    }
}
