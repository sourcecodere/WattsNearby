package re.sourcecode.android.wattsnearby.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by olem on 3/16/17.
 */

public class ChargingStationContract {

    private static final String TAG = ChargingStationContract.class.getSimpleName();
    /*
    * The "Content authority" is a name for the entire content provider, similar to the
    * relationship between a domain name and its website. A convenient string to use for the
    * content authority is the package name for the app, which is guaranteed to be unique on the
    * Play Store.
    */
    public static final String CONTENT_AUTHORITY = "re.sourcecode.android.wattsnearby";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for Sunshine.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that Wattsnearby
     * can handle. For instance,
     *
     *     content://re.sourcecode.android.wattsnearby/station/
     *     [           BASE_CONTENT_URI         ][ PATH_STATION ]
     * and
     *     content://re.sourcecode.android.wattsnearby/connection/
     *     [           BASE_CONTENT_URI         ][ PATH_CONNECTION ]
     *
     * is a valid path for looking at wattsnearby data.
     *
     *      content://re.sourcecode.android.wattsnearby/givemeroot/
     *
     * will fail, as the ContentProvider hasn't been given any information on what to do with
     * "givemeroot". At least, let's hope not. Don't be that dev, reader. Don't be that dev.
     */
    public static final String PATH_STATION = "station";
    public static final String PATH_CONNECTION = "connection";

    /* Inner class that defines the table contents of the station table */
    public static final class StationEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Station table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STATION)
                .build();


        /* Used internally as the name of  table. */
        public static final String TABLE_NAME = "station";

        public static final String COLUMN_ID = "id";

        /* Operator */
        public static final String COLUMN_OPERATOR_TITLE = "operator_title";
        public static final String COLUMN_OPERATOR_WEBSITE = "operator_website";

        /* Usage Type */
        public static final String COLUMN_UT_PAY_ON_SITE = "is_pay_at_location";
        public static final String COLUMN_UT_MEMBERSHIP = "is_membership_required";
        public static final String COLUMN_UT_ACCESSKEY = "is_access_key_required";
        public static final String COLUMN_UT_TITLE = "usage_type_title";

        /* Address/Location info */
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_ADDR_TITLE = "addr_title";
        public static final String COLUMN_ADDR_LINE1 = "addr_line1";
        public static final String COLUMN_ADDR_LINE2 = "addr_line2";
        public static final String COLUMN_ADDR_TOWN = "addr_town";
        public static final String COLUMN_ADDR_STATE = "addr_state";
        public static final String COLUMN_ADDR_POSTCODE = "addr_post_code";
        public static final String COLUMN_ADDR_COUNTRY_ISO = "addr_country_iso";
        public static final String COLUMN_ADDR_COUNTRY_TITLE = "addr_country";

        /* Comments */
        public static final String COLUMN_COMMENTS = "comments";

        /* Status */
        public static final String COLUMN_TIME_UPDATED = "time_updated";



        /* Define a function to build a URI to find a specific station by it's identifier */
        public static Uri buildStationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /* ONE(station)-TO-MANY(Connections) */

    /* Inner class that defines the table contents of the stations connection table */
    public static final class ConnectionEntry implements BaseColumns {


        /* The base CONTENT_URI used to query the Station table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CONNECTION)
                .build();

        /* Used internally as the name of table. */
        public static final String TABLE_NAME = "connection";

        public static final String COLUMN_ID = "id";

        /* Connections */
        public static final String COLUMN_CONN_TITLE = "con_title";
        public static final String COLUMN_CONN_TYPE_ID = "con_type_id";
        public static final String COLUMN_CONN_LEVEL_FAST = "con_level_fast";
        public static final String COLUMN_CONN_LEVEL_TITLE = "con_level_title";
        public static final String COLUMN_CONN_AMP = "con_amp";
        public static final String COLUMN_CONN_VOLT = "con_volt";
        public static final String COLUMN_CONN_KW = "con_kw";
        public static final String COLUMN_CONN_CURRENT_TYPE_DESC = "con_current_type_desc";
        public static final String COLUMN_CONN_CURRENT_TYPE_TITLE = "con_current_title";

        public static final String COLUMN_CONN_STATION_ID = "con_station_id";

        /* Define a function to build a URI to find a specific station by it's identifier */
        public static Uri buildStationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
