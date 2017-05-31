package re.sourcecode.android.wattsnearby.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.data.WattsPreferences;
import re.sourcecode.android.wattsnearby.utilities.WattsImageUtils;

/**
 * Created by olem on 5/29/17.
 *
 * Loader to offload database queries from main UI of station markers in map.
 *
 */
public class StationMarkersLoader extends AsyncTaskLoader<HashMap<Long, MarkerOptions>> {

    private static final String TAG = StationMarkersLoader.class.getSimpleName();

    /* The data we need to get for each marker */
    private static final String[] STATION_MARKER_PROJECTION = {
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
    private static final int INDEX_ID = 0;
    private static final int INDEX_ADDRESS_TITLE = 1;
    private static final int INDEX_LAT = 2;
    private static final int INDEX_LON = 3;

    /* The data we need to count matching connections at station */
    private static final String[] CONNECTION_ID_PROJECTION = {
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID
    };

    private Context mContext;
    private LatLngBounds mLatLngBounds;

    private BitmapDescriptor mMarkerIconStation; // Icon for charging stations.
    private BitmapDescriptor mMarkerIconStationFast; // Icon for fast charging stations.

    public StationMarkersLoader(Context context, Bundle args) {
        super(context);
        mContext = context;
        if ((args != null ) && (args.containsKey(MainMapActivity.ARG_MAP_VISIBLE_BOUNDS))) {
            this.mLatLngBounds = args.getParcelable(MainMapActivity.ARG_MAP_VISIBLE_BOUNDS);
        }

        mMarkerIconStation = WattsImageUtils.vectorToBitmap(
                context,
                R.drawable.ic_station,
                context.getResources().getInteger(R.integer.station_icon_add_to_size)
        );
        // Create the charging station marker bitmap
        mMarkerIconStationFast = WattsImageUtils.vectorToBitmap(
                context,
                R.drawable.ic_station_fast,
                context.getResources().getInteger(R.integer.station_icon_add_to_size)
        );
    }

    @Override
    public HashMap<Long, MarkerOptions> loadInBackground() {

        if (mLatLngBounds != null) {
            return getVisibleMarkerOptions();
        }
        return null;
    }

    private HashMap<Long, MarkerOptions> getVisibleMarkerOptions() {
        Log.d(TAG, "getVisibleMarkerOptions");
        HashMap<Long, MarkerOptions> stationMarkers = new HashMap<>();
        ContentResolver wattsContentResolver = mContext.getContentResolver();
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
                if (mLatLngBounds.contains(stationPosition)) {


                    Cursor connectionCursor = getConnectionsFilteredByPrefs(
                            mContext,
                            wattsContentResolver,
                            stationId,
                            CONNECTION_ID_PROJECTION);
                    try {
                        if (connectionCursor.getCount() > 0) {
                            //Log.d(TAG, "station" + stationId + " has " + connectionCursor.getCount() + " matching connections");
                            // we have matching connections, add it to the map
                            MarkerOptions markerOptions;
                            if (checkFastChargingAtStation(wattsContentResolver, stationId)) {
                                markerOptions = WattsImageUtils.getStationMarkerOptions(
                                        stationPosition,
                                        stationTitle,
                                        mMarkerIconStationFast);
                                stationMarkers.put(stationId, markerOptions);
                            } else {
                                markerOptions = WattsImageUtils.getStationMarkerOptions(
                                        stationPosition,
                                        stationTitle,
                                        mMarkerIconStation);
                                stationMarkers.put(stationId, markerOptions);
                            }

                        }
                    } finally {
                        connectionCursor.close();
                    }


                }
            }

        } finally {
            getStationsCursor.close();
        }

        return stationMarkers;
    }


    /**
     * Checks database for fast charging capacity at certain station
     *
     * @param stationId the id of the station from OCM
     * @return true or false
     */
    private static Boolean checkFastChargingAtStation(ContentResolver wattsContentResolver, Long stationId) {

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
    private static Cursor getConnectionsFilteredByPrefs(Context context, ContentResolver wattsContentResolver, Long stationId, String[] projection) {

        final Uri getConnectionUri = ChargingStationContract.ConnectionEntry.CONTENT_URI;

        //IDs from https://api.openchargemap.io/v2/referencedata/
        String selection;
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
        return wattsContentResolver.query(
                getConnectionUri,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

}
