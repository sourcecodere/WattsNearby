package re.sourcecode.android.wattsnearby.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by olem on 3/31/17.
 */

public final class WattsOCMNetworkUtils {

    private static final String TAG = WattsOCMNetworkUtils.class.getSimpleName();

    // API URI sample:  https://api.openchargemap.io/v2/poi/?output=json&maxresults=10&latitude=60.029265&longitude=11.0952163&distanceunit=km&distance=2
    /* The base OCM API url */
    private static final String OCM_BASE_URL = "https://api.openchargemap.io/v2/poi/";

    /* The format/output we want the OCM API to return */
    private static final String output = "json";
    /* The distanceunit we want the OCM API to return */
    private static final String distance_unit = "km";
    /* The maximum results we want the OCM API to return, default 100 */
    private static final Integer max_results = 100;

    /* The position parameters query */
    private static final String LAT_PARAM = "latitude";
    private static final String LON_PARAM = "longitude";
    private static final String DISTANCE_PARAM = "distance"; // in km

    /* The format parameter allows us to designate whether we want JSON, XML or KML from the OCM API*/
    private static final String OUTPUT_PARAM = "output";
    /* The distance unit parameter allows ut to get distance in km or miles from the OCM API*/
    private static final String DISTANCE_UNIT_PARAM = "distanceunit";
    private static final String MAX_RES_PARAM = "maxresults";

    /**
     * Retrieves the proper URL to query for the OCM data.
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @param distance The distance/zoom level of the current location
     * @return URL to query ocm service
     */
    public static URL getUrl(Double latitude, Double longitude, Double distance) {
            Uri ocmQueryUri = Uri.parse(OCM_BASE_URL).buildUpon()
                    .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
                    .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
                    .appendQueryParameter(DISTANCE_PARAM, String.valueOf(distance))
                    .appendQueryParameter(OUTPUT_PARAM, output)
                    .appendQueryParameter(DISTANCE_UNIT_PARAM, distance_unit)
                    .appendQueryParameter(MAX_RES_PARAM, Integer.toString(max_results))
                    .build();

        try {
            URL ocmQueryUrl = new URL(ocmQueryUri.toString());
            Log.v(TAG, "URL: " + ocmQueryUrl);
            return ocmQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }
    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}
