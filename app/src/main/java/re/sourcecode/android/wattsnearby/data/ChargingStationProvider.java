package re.sourcecode.android.wattsnearby.data;

/**
 * Created by olem on 3/24/17.
 */

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * This class serves as the ContentProvider for WattsNearby's data. This class allows us to
 * bulkInsert data, query data, and delete data.
 *
 */
public class ChargingStationProvider extends ContentProvider {

    private static final String TAG = ChargingStationProvider.class.getSimpleName();

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_STATION = 100;
    public static final int CODE_STATION_ID = 101;
    public static final int CODE_CONNECTION = 200;
    public static final int CODE_CONNECTION_ID = 201;


    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ChargingStationDbHelper mOpenHelper;

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     * <p>
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     * <p>
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since ChargingStationDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new ChargingStationDbHelper(getContext());
        return true;
    }

    /**
     * Creates the UriMatcher that will match each URI to the CODE_STATIONS and CODE_STATIONS_NEARBY constant defined above.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_STATIONS and CODE_STATIONS_NEARBY
     */
    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ChargingStationContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://re.sourcecode.wattsnearby/station/ */
        matcher.addURI(authority, ChargingStationContract.PATH_STATION, CODE_STATION);
        /* This URI is content://re.sourcecode.wattsnearby/station/<id> */
        matcher.addURI(authority, ChargingStationContract.PATH_STATION + "/#", CODE_STATION_ID);
        /* This URI is content://re.sourcecode.wattsnearby/connection/ */
        matcher.addURI(authority, ChargingStationContract.PATH_CONNECTION, CODE_CONNECTION);
        /* This URI is content://re.sourcecode.wattsnearby/connection/<id> */
        matcher.addURI(authority, ChargingStationContract.PATH_CONNECTION + "/#", CODE_CONNECTION_ID);

        /*
         * This URI would look something like
         * content://re.sourcecode.wattsnearby/stations/nearby?lat=40.123213&lon=10.123123&distance=2
         * The "/#" signifies to the UriMatcher that if PATH_STATIONS is followed by nearby and
         * "GET" key value pairs,
         * that it should return the CODE_STATIONS_NEARBY code
         */
        //matcher.addURI(authority, ChargingStationContract.PATH_STATIONS + "/nearby", CODE_STATIONS_NEARBY);


        return matcher;
    }

    /**
     * In WattsNearby, we aren't going to do anything with this method. However, we are required to
     * override it as ChargingStationProvider extends ContentProvider and getType is an abstract method in
     * ContentProvider. Normally, this method handles requests for the MIME type of the data at the
     * given URI. For example, if your app provided images at a particular URI, then you would
     * return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return nothing in WattsNearby, but normally a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in WattsNearby.");
    }


    /**
     * Handles query requests from clients. We will use this method in Wattsnearby to query for all
     * of our Ocm data.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor retCursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://re.sourcecode.wattsnearby/station/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the weather in our weather table.
             *
             * In this case, we want to return a cursor that contains every row of weather data
             * in our weather table.
             */
            case CODE_STATION: {
                retCursor = db.query(
                        ChargingStationContract.StationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_STATION_ID: {
                long _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        ChargingStationContract.StationEntry.TABLE_NAME,
                        projection,
                        ChargingStationContract.StationEntry.COLUMN_ID + "= ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_CONNECTION: {
                retCursor = db.query(
                        ChargingStationContract.ConnectionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_CONNECTION_ID: {
                long _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        ChargingStationContract.ConnectionEntry.TABLE_NAME,
                        projection,
                        ChargingStationContract.ConnectionEntry._ID + "= ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set the notification URI for the cursor to the one passed into the function. This
        // causes the cursor to register a content observer to watch for changes that happen to
        // this URI and any of it's descendants. By descendants, we mean any URI that begins
        // with this path.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Handles requests to insert a single new row. In WattsNearby, we are only going to be
     * inserting single row of data at a time from a charging stations.
     *
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be null
     * @return nothing in WattsNearby, but normally the URI for the newly inserted item.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri;
        db.beginTransaction(); // EXLUSIVE

        switch (sUriMatcher.match(uri)) {

            case CODE_STATION:
                _id = db.insert(ChargingStationContract.StationEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ChargingStationContract.StationEntry.buildStationUri(_id);
                    db.setTransactionSuccessful();
                } else {
                    throw new UnsupportedOperationException("Unable to insert row into: " + uri);
                }
                break;

            case CODE_CONNECTION:
                _id = db.insert(ChargingStationContract.ConnectionEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ChargingStationContract.ConnectionEntry.buildStationUri(_id);
                    db.setTransactionSuccessful();
                } else {
                    throw new UnsupportedOperationException("Unable to insert row into: " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        db.endTransaction();
        // Use this on the URI passed into the function to notify any observers that the uri has
        // changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_STATION:
                numRowsDeleted = db.delete(
                        ChargingStationContract.StationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case CODE_CONNECTION:
                numRowsDeleted = db.delete(
                        ChargingStationContract.ConnectionEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows;

        switch(sUriMatcher.match(uri)){
            case CODE_STATION:
                rows = db.update(ChargingStationContract.StationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CODE_CONNECTION:
                rows = db.update(ChargingStationContract.ConnectionEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    /**
     * Handles requests to insert a set of new rows. In WattsNearby, we aren't going to do
     * anything with this method. However, we are required to override it as ChargingStationProvider
     * extends ContentProvider and bulkInsert is an abstract method in
     * ContentProvider. Rather than the bulk insert method, we are only going to implement
     * {@link ChargingStationProvider#insert}.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        throw new RuntimeException(
                "We are not implementing bulkInsert in WattsNearby. Use insert instead");

    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
