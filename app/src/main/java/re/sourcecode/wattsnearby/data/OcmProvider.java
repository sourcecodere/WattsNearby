package re.sourcecode.wattsnearby.data;

/**
 * Created by olem on 3/24/17.
 */

import android.annotation.TargetApi;
import android.content.ContentProvider;
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
public class OcmProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_STATIONS = 100;
    //public static final int CODE_STATIONS_NEARBY = 101; //nearby?nearby?lat=40.123213&lon=10.123123&distance=2


    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private OcmDbHelper mOpenHelper;

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
        final String authority = OcmContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://re.sourcecode.wattsnearby/stations/ */
        matcher.addURI(authority, OcmContract.PATH_STATIONS, CODE_STATIONS);

        /*
         * This URI would look something like
         * content://re.sourcecode.wattsnearby/stations/nearby?lat=40.123213&lon=10.123123&distance=2
         * The "/#" signifies to the UriMatcher that if PATH_STATIONS is followed by nearby and
         * "GET" key value pairs,
         * that it should return the CODE_STATIONS_NEARBY code
         */
        //matcher.addURI(authority, OcmContract.PATH_STATIONS + "/nearby", CODE_STATIONS_NEARBY);


        return matcher;
    }

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
         * lengthy operations will cause lag in your app. Since OcmDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new OcmDbHelper(getContext());
        return true;
    }

    /**
     * Handles requests to insert a set of new rows. In WattsNearby, we are only going to be
     * inserting multiple rows of data at a position from a api query. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_STATIONS:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        // TODO: insert connections
                        long _id = db.insert(OcmContract.StationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
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

        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *
             *      content://re.sourcecode.wattsnearby/stations/nearby?lat=40.123213&lon=10.123123&distance=2
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return the stations for a particular position. The position in this code is encoded in
             * milliseconds and is at the very end of the URI (1472214172) and can be accessed
             * programmatically using Uri's getLastPathSegment method.
             *
             * In this case, we want to return a cursor that contains one row of weather data for
             * a particular date.
             */
            //case CODE_STATIONS_NEARBY: {

                /*
                 * In order to determine the position with this URI, we look at the last
                 * path segment. In the comment above, the last path segment is 1472214172 and
                 * represents the number of seconds since the epoch, or UTC time.
                 */
            //String lat  = uri.getQueryParameter(OcmContract.StationEntry.COLUMN_LAT);
            //String lon  = uri.getQueryParameter(OcmContract.StationEntry.COLUMN_LON);
            //String distance  = uri.getQueryParameter(OcmContract.StationEntry.COLUMN_DISTANCE);

                /*
                 * The query method accepts a string array of arguments, as there may be more
                 * than one "?" in the selection statement. Even though in our case, we only have
                 * one "?", we have to create a string array that only contains one element
                 * because this method signature accepts a string array.
                 */


            // TODO: make query for specific area in map

            //break;
            //}

            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://re.sourcecode.wattsnearby/stations/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the weather in our weather table.
             *
             * In this case, we want to return a cursor that contains every row of weather data
             * in our weather table.
             */
            case CODE_STATIONS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        OcmContract.StationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
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

            case CODE_STATIONS:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        OcmContract.StationEntry.TABLE_NAME,
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

    /**
     * In WattsNearby, we aren't going to do anything with this method. However, we are required to
     * override it as OcmProvider extends ContentProvider and getType is an abstract method in
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
     * In WattsNearby, we aren't going to do anything with this method. However, we are required to
     * override it as OcmProvider extends ContentProvider and insert is an abstract method in
     * ContentProvider. Rather than the single insert method, we are only going to implement
     * {@link OcmProvider#bulkInsert}.
     *
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be null
     * @return nothing in WattsNearby, but normally the URI for the newly inserted item.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "We are not implementing insert in WattsNearby. Use bulkInsert instead");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("We are not implementing update in WattsNearby");
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
