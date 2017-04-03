package re.sourcecode.android.wattsnearby.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.utilities.WattsOCMNetworkUtils;
import re.sourcecode.android.wattsnearby.utilities.WattsOCMJsonUtils;

/**
 * Created by olem on 3/31/17.
 */

public class WattsOCMSyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = WattsOCMSyncTask.class.getSimpleName();

    private Context context;
    private Double latitude;
    private Double longitude;
    private Double distance;

    /* ContentResolver for query, updates and inserts */
    private static ContentResolver mWattsContentResolver;

    /* The data we need to get to check if a station has changed */
    public static final String[] STATION_LAST_CHANGED_PROJECTION = {
            ChargingStationContract.StationEntry.COLUMN_TIME_UPDATED,
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_TIME_UPDATED = 0;

    public WattsOCMSyncTask(Context context, Double latitude, Double longitude, Double distance) {
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(Void... params) {
        syncStations(this.context, this.latitude, this.longitude, this.distance);
        return null;
    }

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
        try {
            /* Get a handle on the ContentResolver to update and insert data */
            mWattsContentResolver = context.getContentResolver();

            /*
            * The getUrl method will return the URL that we need to get the ocm JSON for the
            * nearby charging stations. It will create a URL based off of the latitude,
            * longitude and distance (the current map zoom level)
            */
            URL ocmRequestUrl = WattsOCMNetworkUtils.getUrl(context, latitude, longitude, distance);

            /* Use the URL to retrieve the JSON */
            String jsonOcmResponse = WattsOCMNetworkUtils.getResponseFromHttpUrl(ocmRequestUrl);

            /* Parse as JSONArray */
            JSONArray ocmJsonArray = WattsOCMJsonUtils.getOCMJsonArray(jsonOcmResponse);

            /* iterate through each station and update the local cache database*/
            for(int i = 0; i < ocmJsonArray.length(); i++) {
                cacheStation(ocmJsonArray.getJSONObject(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cacheStation(JSONObject jsonStation) {
        try {
        /* get the OCM id */
            Long id = WattsOCMJsonUtils.getOCMStationIdFromJson(jsonStation);

        /* query the cache for the current id */
            Uri stationByIdUri = ChargingStationContract.StationEntry.buildStationUri(id);
        /* */
            Cursor currentStationCursor = mWattsContentResolver.query(
                    stationByIdUri,
                    STATION_LAST_CHANGED_PROJECTION,
                    null,
                    null,
                    null);

        /*
         * If currentStationCursor is empty, moveToFirst will return false, then we insert, else we update.
         */
            if (!currentStationCursor.moveToFirst()) {
                ContentValues stationValues = WattsOCMJsonUtils.getOCMStationContentValuesFromJson(jsonStation);
                ContentValues[] connectionsValues = WattsOCMJsonUtils.getOCMConnectionsContentValuesFromJson(jsonStation);
                /* new station data, insert it */
                mWattsContentResolver.insert(
                        ChargingStationContract.StationEntry.CONTENT_URI,
                        stationValues
                );
                /* new connection data, insert it */
                for(int i = 0; i < connectionsValues.length; i++) {
                    mWattsContentResolver.insert(ChargingStationContract.ConnectionEntry.CONTENT_URI,
                            connectionsValues[i]
                    );
                }
            } else {
            /* update if timestamp is newer */
            //TODO
                String date = currentStationCursor.getString(INDEX_TIME_UPDATED);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }





    }


}
