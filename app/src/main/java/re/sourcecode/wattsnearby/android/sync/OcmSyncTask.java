package re.sourcecode.wattsnearby.android.sync;

import android.content.ContentValues;
import android.content.Context;

import java.net.URL;

import re.sourcecode.wattsnearby.android.utilities.OcmNetworkUtils;
import re.sourcecode.wattsnearby.android.utilities.OcmJsonUtils;

/**
 * Created by olem on 3/31/17.
 */

public class OcmSyncTask {

    private static final String TAG = OcmSyncTask.class.getSimpleName();

    /**
     * Performs the network request for updated charging stations, parses the JSON from that request, and
     * inserts the new station information into our ContentProvider.
     *
     * @param distance  The current map zoom level
     * @param longitude The current position longitude
     * @param latitude  The current position latitude
     * @param context   Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncStations(Context context, Double latitude, Double longitude, Double distance) {
        try {/*
         * The getUrl method will return the URL that we need to get the ocm JSON for the
         * nearby charging stations. It will create a URL based off of the latitude,
         * longitude and distance (the current map zoom level)
         */

            URL ocmRequestUrl = OcmNetworkUtils.getUrl(context, latitude, longitude, distance);

        /* Use the URL to retrieve the JSON */
            String jsonOcmResponse = OcmNetworkUtils.getResponseFromHttpUrl(ocmRequestUrl);

        /* Parse the JSON into a list of station values */
            ContentValues[] stationValues = OcmJsonUtils.getOCMContentValuesFromJson(context, jsonOcmResponse);

            // TODO: put it into the ContentProvider


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
