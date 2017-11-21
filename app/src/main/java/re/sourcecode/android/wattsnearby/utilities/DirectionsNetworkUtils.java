package re.sourcecode.android.wattsnearby.utilities;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import re.sourcecode.android.wattsnearby.BuildConfig;

/**
 * Created by olem on 11/6/17.
 *
 * Handles network requests to google direction api.
 */

public class DirectionsNetworkUtils {

    private static final String TAG = DirectionsNetworkUtils.class.getSimpleName();

    /* The base Directions API url */
    private static final String DIRECTIONS_BASE_URL = "https://maps.googleapis.com/maps/api/directions/";

    /* The format/output we want the API to return */
    private static final String output = "json";
    /* The units we want the  API to return */
    private static final String distance_unit = "metric"; //TODO add support for imperial

    /* The position parameters query */
    private static final String ORIGIN_PARAM = "origin"; // e.g. origin=41.43206,-81.38992
    private static final String DESTINATION_PARAM = "destination";

    /* The units parameter */
    private static final String UNITS_PARAM = "units";

    /* The API key parameter*/
    private static final String KEY_PARAM = "key"; //


    public static URL getUrl(String key, LatLng origin, LatLng destination) {
        Uri directionsQueryUri = Uri.parse(DIRECTIONS_BASE_URL).buildUpon()
                .appendPath(output)
                .appendQueryParameter(
                        ORIGIN_PARAM,
                        String.format("%f,%f",origin.latitude, origin.longitude)
                )
                .appendQueryParameter(
                        DESTINATION_PARAM,
                        String.format("%f,%f", destination.latitude, destination.longitude)
                )
                .appendQueryParameter(
                        UNITS_PARAM,
                        distance_unit
                )
                .appendQueryParameter(KEY_PARAM, key)
                .build();
        try {
            URL directionsQueryUrl = new URL(directionsQueryUri.toString());
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "URL: " + directionsQueryUrl);
            }
            return directionsQueryUrl;
        } catch (MalformedURLException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return  null;
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
