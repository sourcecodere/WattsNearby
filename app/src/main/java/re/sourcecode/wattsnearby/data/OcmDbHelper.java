package re.sourcecode.wattsnearby.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import re.sourcecode.wattsnearby.data.OcmContract.StationEntry;
import re.sourcecode.wattsnearby.data.OcmContract.ConnectionEntry;

/**
 * Created by olem on 3/16/17.
 */

public class OcmDbHelper extends SQLiteOpenHelper {


    public static final String DB_NAME = "ocm.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     *
     */
    private static final int DB_VERSION = 1;

    public OcmDbHelper(Context context) {
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

                "CREATE TABLE " + StationEntry.TABLE_NAME + " (" +

                        StationEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +

                        StationEntry.COLUMN_OPERATOR_TITLE + " TEXT, " +
                        StationEntry.COLUMN_OPERATOR_WEBSITE + " TEXT, " +

                        StationEntry.COLUMN_UT_TITLE + " TEXT, " +
                        StationEntry.COLUMN_UT_ACCESSKEY + " INTEGER, " + //boolean
                        StationEntry.COLUMN_UT_MEMBERSHIP + " INTEGER, " + //boolean
                        StationEntry.COLUMN_UT_PAY_ON_SITE + " INTEGER, " + //boolean


                        StationEntry.COLUMN_LAT + " REAL NOT NULL, " +
                        StationEntry.COLUMN_LON + " REAL NOT NULL, " +
                        StationEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +

                        StationEntry.COLUMN_ADDR_TITLE + " TEXT, " +
                        StationEntry.COLUMN_ADDR_LINE1 + " TEXT, " +
                        StationEntry.COLUMN_ADDR_LINE2 + " TEXT, " +
                        StationEntry.COLUMN_ADDR_POSTCODE + " TEXT, " +
                        StationEntry.COLUMN_ADDR_STATE + " TEXT, " +
                        StationEntry.COLUMN_ADDR_TOWN + " TEXT, " +
                        StationEntry.COLUMN_ADDR_COUNTRY_TITLE + " TEXT, " +
                        StationEntry.COLUMN_ADDR_COUNTRY_ISO + " TEXT, " +

                        StationEntry.COLUMN_COMMENTS + " TEXT, " +

                        StationEntry.COLUMN_TIME_UPDATED + " INTEGER NOT NULL, " +

                        " UNIQUE (" + StationEntry.COLUMN_TIME_UPDATED + ") ON CONFLICT REPLACE);";


        final String SQL_CREATE_OCM_CONNTIONS_TABLE =

                "CREATE TABLE " + ConnectionEntry.TABLE_NAME + " (" +

                        ConnectionEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +

                        ConnectionEntry.COLUMN_CONN_TITLE + " TEXT, " +
                        ConnectionEntry.COLUMN_CONN_TYPE_ID + " INTEGER NOT NULL, " +
                        ConnectionEntry.COLUMN_CONN_LEVEL_FAST + " INTEGER, " + //boolean
                        ConnectionEntry.COLUMN_CONN_LEVEL_TITLE + " TEXT, " +
                        ConnectionEntry.COLUMN_CONN_AMP + " REAL, " +
                        ConnectionEntry.COLUMN_CONN_KW + " REAL, " +
                        ConnectionEntry.COLUMN_CONN_VOLT + " REAL, " +

                        ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_DESC + " TEXT, " +
                        ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_TITLE + " TEXT, " +

                        ConnectionEntry.COLUMN_CONN_STATION_ID + " INTEGER, " +

                        "FOREIGN KEY(" + ConnectionEntry.COLUMN_CONN_STATION_ID + ") REFERENCES " +
                        StationEntry.TABLE_NAME + "(" + StationEntry.COLUMN_ID + ")" + ")";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        db.execSQL(SQL_CREATE_OCM_TABLE);
        db.execSQL(SQL_CREATE_OCM_CONNTIONS_TABLE);
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
}
}
