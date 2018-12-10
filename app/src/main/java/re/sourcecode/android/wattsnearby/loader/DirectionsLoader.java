package re.sourcecode.android.wattsnearby.loader;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.support.v4.content.Loader;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.utilities.DirectionsJsonUtils;
import re.sourcecode.android.wattsnearby.utilities.DirectionsNetworkUtils;
import re.sourcecode.android.wattsnearby.data.Preferences;

/**
 * Created by SourceCodeRe
 *
 * Loader for directions from google maps v2 api
 */

public class DirectionsLoader extends AsyncTaskLoader<ContentValues> {

    //private static final String TAG = AsyncTaskLoader.class.getSimpleName();

    private String mApiKey;
    private LatLng mOrigin;
    private LatLng mDestination;

    // Args for returned bundle with directions data
    public static final String ARG_DISTANCE = "distance";
    public static final String ARG_OVERVIEW_POLYLINE = "overview_polyline";


    public DirectionsLoader(Context context, Bundle args) {
        super(context);
        this.mApiKey = context.getString(R.string.google_api_key);
        if ((args != null)
                && (args.containsKey(MainMapActivity.ARG_DIRECTIONS_ORIGIN))
                && (args.containsKey(MainMapActivity.ARG_DIRECTIONS_DEST))) {

            this.mOrigin = args.getParcelable(MainMapActivity.ARG_DIRECTIONS_ORIGIN);
            this.mDestination = args.getParcelable(MainMapActivity.ARG_DIRECTIONS_DEST);
        }
    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     * Implementations should not deliver the result directly, but should return them
     * from this method, which will eventually end up calling {@link #deliverResult} on
     * the UI thread.  If implementations need to process the results on the UI thread
     * they may override {@link #deliverResult} and do so there.
     * <p>
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * <p>
     * When the load is canceled, this method may either return normally or throw
     * {@link OperationCanceledException}.  In either case, the {@link Loader} will
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public ContentValues loadInBackground() {
        if ((mDestination != null) && (mOrigin != null)) {
            try {

                String units = Preferences.getUnitsValue(getContext());

                URL directionsRequestURL = DirectionsNetworkUtils.getUrl(mApiKey, mOrigin, mDestination, units);

                String stringDirectionsResponse = DirectionsNetworkUtils.getResponseFromHttpUrl(directionsRequestURL);

                JSONObject jsonDirectionsResponse = DirectionsJsonUtils.getDirectionsJsonObject(stringDirectionsResponse);

                String distance = DirectionsJsonUtils.getDirectionsDistanceFromJson(jsonDirectionsResponse);

                String overviewPolylineEncoded = DirectionsJsonUtils.getDirectionsOverviewPolylineFromJson(jsonDirectionsResponse);
                if ((distance != null) && (overviewPolylineEncoded != null)) {

                    ContentValues directionsContentValues = new ContentValues();

                    directionsContentValues.put(ARG_DISTANCE, distance);
                    directionsContentValues.put(ARG_OVERVIEW_POLYLINE, overviewPolylineEncoded);

                    return directionsContentValues;
                }
                return null;

            } catch (OperationCanceledException | JSONException | IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
