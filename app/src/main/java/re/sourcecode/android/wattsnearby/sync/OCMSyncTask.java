package re.sourcecode.android.wattsnearby.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.utilities.DateUtils;
import re.sourcecode.android.wattsnearby.utilities.OCMNetworkUtils;
import re.sourcecode.android.wattsnearby.utilities.OCMJsonUtils;

/**
 * Created by SourcecodeRe on 3/31/17.
 * <p>
 * Async task to sync data from OCM
 */
public class OCMSyncTask extends AsyncTask<Void, Void, Void> {

    //private static final String TAG = OCMSyncTask.class.getSimpleName();
    private LatLng mLatLng;
    private Double mDistance;
    private int mMaxResults;

    /* ContentResolver for query, updates and inserts */
    private static ContentResolver mWattsContentResolver;

    /* The data we need to get to check if a station has changed */
    private static final String[] STATION_LAST_CHANGED_PROJECTION = {
            ChargingStationContract.StationEntry.COLUMN_TIME_UPDATED,
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    private static final int INDEX_TIME_UPDATED = 0;

    private OCMSyncTaskListener mCallback;
    private Exception mException;


    public OCMSyncTask(Context context, LatLng latLng, Double distance, int max_results, OCMSyncTaskListener callback) {
        this.mWattsContentResolver = context.getContentResolver();
        this.mLatLng = latLng;
        this.mDistance = distance;
        this.mMaxResults = max_results;
        this.mCallback = callback;
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
        try {
            if (android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();
            syncStations(this.mLatLng, this.mDistance, this.mMaxResults);
        } catch (Exception e) {
            mException = e;
        }
        return null;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param aVoid The result of the operation computed by {@link #doInBackground}.
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        if (mCallback != null) {
            mCallback.onOCMSyncSuccess(aVoid);
        } else {
            mCallback.onOCMSyncFailure(mException);
        }
        super.onPostExecute(aVoid);
    }

    /**
     * Performs the network request for updated charging stations, parses the JSON from that request, and
     * inserts the new station information into our ContentProvider.
     *
     * @param distance The current map zoom level
     * @param latLng   The current LatLng position
     */
    private static void syncStations(LatLng latLng, double distance, int max_results) {
        try {


            /*
            * The getUrl method will return the URL that we need to get the ocm JSON for the
            * nearby charging stations. It will create a URL based off of the latitude,
            * longitude and distance (the current map zoom level)
            */
            URL ocmRequestUrl = OCMNetworkUtils.getUrl(latLng, distance, max_results);

            /* Use the URL to retrieve the JSON */
            String jsonOcmResponse = OCMNetworkUtils.getResponseFromHttpUrl(ocmRequestUrl);

            /* Parse as JSONArray */
            JSONArray ocmJsonArray = OCMJsonUtils.getOCMJsonArray(jsonOcmResponse);

            /* iterate through each station and update the local cache database*/
            for (int i = 0; i < ocmJsonArray.length(); i++) {
                cacheStation(ocmJsonArray.getJSONObject(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cacheStation(JSONObject jsonStation) {
        try {
            //Log.d(TAG, "caching station");

            /* get the OCM id */
            long id = OCMJsonUtils.getOCMStationIdFromJson(jsonStation);

            /* query the cache for the current id */
            Uri stationByIdUri = ChargingStationContract.StationEntry.buildStationUri(id);

            /* */
            Cursor currentStationCursor = mWattsContentResolver.query(
                    stationByIdUri,
                    STATION_LAST_CHANGED_PROJECTION,
                    null,
                    null,
                    null);

            boolean stationInDB = false;

            if (currentStationCursor != null && currentStationCursor.getCount() > 0) {
                stationInDB = currentStationCursor.moveToFirst();
            }

            //Log.d(TAG, stationByIdUri.toString() + " " + stationInDB);
            /*
            * If currentStationCursor is empty, moveToFirst will return false, then we insert, else we update.
            */
            if (!stationInDB) {
                if (currentStationCursor != null) {
                    currentStationCursor.close();
                }

                ContentValues stationValues = OCMJsonUtils.getOCMStationContentValuesFromJson(jsonStation);
                ContentValues[] connectionsValues = OCMJsonUtils.getOCMConnectionsContentValuesFromJson(jsonStation);

                /* new station data, insert it */

                mWattsContentResolver.insert(
                        ChargingStationContract.StationEntry.CONTENT_URI,
                        stationValues
                );

                /* new connection data, insert it */
                for (ContentValues connectionsValue : connectionsValues) {
                    mWattsContentResolver.insert(
                            ChargingStationContract.ConnectionEntry.CONTENT_URI,
                            connectionsValue
                    );
                }

            } else {
            /* update only if timestamp is newer */

                long station_id = OCMJsonUtils.getOCMStationIdFromJson(jsonStation);
                long db_entry_changed = currentStationCursor.getLong(INDEX_TIME_UPDATED);
                long json_entry_changed = DateUtils.dateStringToEpoc(OCMJsonUtils.getOCMLastChangedFromJson(jsonStation));

                if (db_entry_changed < json_entry_changed) {
                    /* delete the old data */
                    mWattsContentResolver.delete(
                            ChargingStationContract.ConnectionEntry.CONTENT_URI,
                            ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID + "=?",
                            new String[]{Long.toString(station_id)}
                    );
                    mWattsContentResolver.delete(
                            ChargingStationContract.StationEntry.CONTENT_URI,
                            ChargingStationContract.StationEntry.COLUMN_ID + "=?",
                            new String[]{Long.toString(station_id)}
                    );
                    ContentValues stationValues = OCMJsonUtils.getOCMStationContentValuesFromJson(jsonStation);
                    ContentValues[] connectionsValues = OCMJsonUtils.getOCMConnectionsContentValuesFromJson(jsonStation);

                    /* new station data, insert it */
                    mWattsContentResolver.insert(
                            ChargingStationContract.StationEntry.CONTENT_URI,
                            stationValues
                    );

                    /* new connection data, insert it */
                    for (ContentValues connectionsValue : connectionsValues) {
                        mWattsContentResolver.insert(ChargingStationContract.ConnectionEntry.CONTENT_URI,
                                connectionsValue
                        );
                    }

                }
                //close db cursor
                currentStationCursor.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
