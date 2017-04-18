package re.sourcecode.android.wattsnearby.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by olem on 3/16/17.
 */

public class ChargingStationDbHelper extends SQLiteOpenHelper {


    private static final String TAG = ChargingStationDbHelper.class.getSimpleName();

    public static final String DB_NAME = "ocm.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     *
     */
    private static final int DB_VERSION = 1;

    public ChargingStationDbHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our ocm data.
         */
        final String SQL_CREATE_OCM_TABLE =

                "CREATE TABLE " + ChargingStationContract.StationEntry.TABLE_NAME + " (" +

                        ChargingStationContract.StationEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +

                        ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_OPERATOR_WEBSITE + " TEXT, " +

                        ChargingStationContract.StationEntry.COLUMN_UT_TITLE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_UT_ACCESSKEY + " INTEGER, " + //boolean
                        ChargingStationContract.StationEntry.COLUMN_UT_MEMBERSHIP + " INTEGER, " + //boolean
                        ChargingStationContract.StationEntry.COLUMN_UT_PAY_ON_SITE + " INTEGER, " + //boolean


                        ChargingStationContract.StationEntry.COLUMN_LAT + " REAL NOT NULL, " +
                        ChargingStationContract.StationEntry.COLUMN_LON + " REAL NOT NULL, " +
                        ChargingStationContract.StationEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +

                        ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_LINE1 + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_LINE2 + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_POSTCODE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_STATE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_TITLE + " TEXT, " +
                        ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_ISO + " TEXT, " +

                        ChargingStationContract.StationEntry.COLUMN_COMMENTS + " TEXT, " +

                        ChargingStationContract.StationEntry.COLUMN_TIME_UPDATED + " INTEGER NOT NULL " +

                         ");";


        final String SQL_CREATE_OCM_CONNECTION_TABLE =

                "CREATE TABLE " + ChargingStationContract.ConnectionEntry.TABLE_NAME + " (" +

                        ChargingStationContract.ConnectionEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +

                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_TITLE + " TEXT, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID + " INTEGER NOT NULL, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST + " INTEGER, " + //boolean
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_TITLE + " TEXT, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_AMP + " REAL, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_KW + " REAL, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_VOLT + " REAL, " +

                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_DESC + " TEXT, " +
                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_TITLE + " TEXT, " +

                        ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID + " INTEGER NOT NULL, " +

                        "FOREIGN KEY(" + ChargingStationContract.ConnectionEntry.COLUMN_CONN_STATION_ID + ") REFERENCES " +
                        ChargingStationContract.StationEntry.TABLE_NAME + "(" + ChargingStationContract.StationEntry.COLUMN_ID + ")" + ")";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        db.execSQL(SQL_CREATE_OCM_TABLE);
        db.execSQL(SQL_CREATE_OCM_CONNECTION_TABLE);
    }

    /**
     * Called when the database has been opened.  The implementation
     * should check {@link SQLiteDatabase#isReadOnly} before updating the
     * database.
     * <p>
     * This method is called after the database connection has been configured
     * and after the database schema has been created, upgraded or downgraded as necessary.
     * If the database connection must be configured in some way before the schema
     * is created, upgraded, or downgraded, do it in {@link #onConfigure} instead.
     * </p>
     *
     * @param db The database.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChargingStationContract.ConnectionEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChargingStationContract.StationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}

