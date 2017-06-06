package re.sourcecode.android.wattsnearby.utilities;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;


/**
 * Created by SourcecodeRe on 5/5/17.
 * <p>
 * Util for simple DB queries
 */
public class WattsDataUtils {


    /**
     * Checks database for charging station and return LatLng for certain station
     *
     * @param context   application context
     * @param stationId id of station
     * @return LatLng of stationId
     */
    public static LatLng getStationLatLng(Context context, long stationId) {

        /* The data we need to get LatLng of a station */
        final String[] STATION_LATLNG_PROJECTION = {
                ChargingStationContract.StationEntry.COLUMN_LAT,
                ChargingStationContract.StationEntry.COLUMN_LON
        };

        final int INDEX_STATION_LAT = 0;
        final int INDEX_STATION_LON = 1;

        final Uri getStationUri = ChargingStationContract.StationEntry.buildStationUri(stationId);

        Cursor getLatLngCursor = context.getContentResolver().query(
                getStationUri,
                STATION_LATLNG_PROJECTION,
                null,
                null,
                null
        );
        try {
            if (getLatLngCursor != null && getLatLngCursor.getCount() > 0) {
                getLatLngCursor.moveToFirst();
                return new LatLng(getLatLngCursor.getDouble(INDEX_STATION_LAT), getLatLngCursor.getDouble(INDEX_STATION_LON));
            }

        } finally {
            if (getLatLngCursor != null) {
                getLatLngCursor.close();
            }
        }
        return null;
    }
}
