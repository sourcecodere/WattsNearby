package re.sourcecode.android.wattsnearby.utilities;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by olem on 11/18/17.
 *
 * Parses the json received from google directions api.
 */

public class DirectionsJsonUtils {

    //private static final String TAG = DirectionsJsonUtils.class.getSimpleName();

    private static final String DIRECTIONS_ROUTES = "routes";
    private static final String DIRECTIONS_ROUTES_LEGS = "legs";
    private static final String DIRECTIONS_ROUTES_DISTANCE = "distance";
    private static final String DIRECTIONS_ROUTES_DISTANCE_TEXT = "text";
    private static final String DIRECTIONS_ROUTES_OVERVIEW_POLYLINE = "overview_polyline";
    private static final String DIRECTIONS_ROUTES_OVERVIEW_POLYLINE_POINTS = "points";


    /**
     * This method parses JSON from a web response and returns an jsonobject
     * describing the directions between car and clicked marker.
     * <p/>
     *
     * @param jsonString string from web response
     * @return JSONArray of directions jsondata
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static JSONObject getDirectionsJsonObject(String jsonString)
            throws JSONException {
        return new JSONObject(jsonString);

    }

    /**
     * This method parses JSONobject and returns the distance in text
     * from car to clicked marker
     * <p/>
     *
     * @param jsonDirections JSONObject
     * @return String of distance
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String getDirectionsDistanceFromJson(JSONObject jsonDirections)
            throws JSONException {

        if (!jsonDirections.isNull(DIRECTIONS_ROUTES)) {
            // Only present on route to the user. TODO: handle several route alternatives
            JSONObject jsonRoute = (JSONObject) jsonDirections.getJSONArray(DIRECTIONS_ROUTES).get(0);
            if (!jsonRoute.isNull(DIRECTIONS_ROUTES_LEGS)) {
                // Since only one destination and no waypoints are specified only one leg should be returned
                JSONObject jsonLeg = (JSONObject) jsonRoute.getJSONArray(DIRECTIONS_ROUTES_LEGS).get(0);
                if (!jsonLeg.isNull(DIRECTIONS_ROUTES_DISTANCE)) {
                    JSONObject jsonDistance = jsonLeg.getJSONObject(DIRECTIONS_ROUTES_DISTANCE);
                    if (!jsonDistance.isNull(DIRECTIONS_ROUTES_DISTANCE_TEXT)) {
                        return jsonDistance.getString(DIRECTIONS_ROUTES_DISTANCE_TEXT);
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method parses JSONobject and returns the overview polyline in text
     * from car to clicked marker
     * <p/>
     *
     * @param jsonDirections JSONObject
     * @return String of overview polyline
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String getDirectionsOverviewPolylineFromJson(JSONObject jsonDirections)
            throws JSONException {

        if (!jsonDirections.isNull(DIRECTIONS_ROUTES)) {
            // Only present on route to the user. TODO: handle several route alternatives
            JSONObject jsonRoute = (JSONObject) jsonDirections.getJSONArray(DIRECTIONS_ROUTES).get(0);
            if (!jsonRoute.isNull(DIRECTIONS_ROUTES_OVERVIEW_POLYLINE)) {
                JSONObject jsonOverviewPolyline = jsonRoute.getJSONObject(DIRECTIONS_ROUTES_OVERVIEW_POLYLINE);
                if (!jsonOverviewPolyline.isNull(DIRECTIONS_ROUTES_OVERVIEW_POLYLINE_POINTS)) {
                    return jsonOverviewPolyline.getString(DIRECTIONS_ROUTES_OVERVIEW_POLYLINE_POINTS);

                }
            }
        }
        return null;
    }
}
