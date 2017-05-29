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
