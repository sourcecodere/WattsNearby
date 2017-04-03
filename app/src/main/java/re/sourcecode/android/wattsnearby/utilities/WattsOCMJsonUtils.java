package re.sourcecode.android.wattsnearby.utilities;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;

/**
 * Created by olem on 3/6/17.
 */

public class WattsOCMJsonUtils {

    private static final String TAG = WattsOCMJsonUtils.class.getSimpleName();
    // https://api.openchargemap.io/v2/poi/?output=json&countrycode=NO&maxresults=10&latitude=60.029265&longitude=11.0952163&distanceunit=km&distance=2

    /* ID */
    private static final String OCM_ID = "ID";

    /* Operator information */
    private static final String OCM_OPERATOR = "OperatorInfo";
    private static final String OCM_OPERATOR_TITLE = "Title";
    private static final String OCM_OPERATOR_URL = "WebsiteURL";

    /* Usage Type */
    private static final String OCM_UT = "UsageType";
    private static final String OCM_UT_PAY_ON_SITE = "IsPayAtLocation";
    private static final String OCM_UT_MEMBERSHIP = "IsMembershipRequired";
    private static final String OCM_UT_ACCESSKEY = "IsAccessKeyRequired";
    private static final String OCM_UT_TITLE = "Title";

    /* Address info */
    private static final String OCM_ADDR = "AddressInfo";
    private static final String OCM_ADDR_LATITUDE = "Latitude";
    private static final String OCM_ADDR_LONGITUDE = "Longitude";
    private static final String OCM_ADDR_DISTANCE = "Distance";
    private static final String OCM_ADDR_TITLE = "Title";
    private static final String OCM_ADDR_LINE1 = "AddressLine1";
    private static final String OCM_ADDR_LINE2 = "AddressLine2";
    private static final String OCM_ADDR_TOWN = "Town";
    private static final String OCM_ADDR_STATE = "StateOrProvince";
    private static final String OCM_ADDR_POSTCODE = "Postcode";
    private static final String OCM_ADDR_COUNTRY = "Country";
    private static final String OCM_ADDR_COUNTRY_ISO = "ISOCode";
    private static final String OCM_ADDR_COUNTRY_TITLE = "Title";

    /* Comments */
    private static final String OCM_COMMENTS = "GeneralComments";

    /* Status */
    private static final String OCM_TIME_UPDATED = "DateLastStatusUpdate";

    /* Connections */
    private static final String OCM_CONNECTIONS = "Connections"; //LIST OF CONNECTIONS AT ADDRESS
    private static final String OCM_CONNECTION_ID = "ID";
    private static final String OCM_CONNECTIONS_TYPE = "ConnectionType";
    private static final String OCM_CONNECTIONS_TYPE_TITLE = "Title"; //e.g. CHAdeMO
    private static final String OCM_CONNECTIONS_TYPE_ID = "ID"; //e.g. 2 is CHAdeMO,  33 CCS
    private static final String OCM_CONNECTIONS_LEVEL = "Level";
    private static final String OCM_CONNECTIONS_LEVEL_FAST = "IsFastChargeCapable";
    private static final String OCM_CONNECTIONS_LEVEL_TITLE = "Title";
    private static final String OCM_CONNECTIONS_AMP = "Amps";
    private static final String OCM_CONNECTIONS_VOLT = "Voltage";
    private static final String OCM_CONNECTIONS_KW = "PowerKW";
    private static final String OCM_CONNECTIONS_CURRENT = "CurrentType";
    private static final String OCM_CONNECTIONS_CURRENT_DESC = "Description";
    private static final String OCM_CONNECTIONS_CURRENT_TITLE = "Title";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the charging stations in the current area
     * <p/>
     *
     * @param jsonString string from web response
     * @return JSONArray of OCM jsondata
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static JSONArray getOCMJsonArray(String jsonString)
            throws JSONException {
        return new JSONArray(jsonString);
    }

    /**
     * This method parses JSONobject and returns the OCM station id
     * <p/>
     *
     * @param jsonStation JSONObject
     * @return Long of OCM station id
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static Long getOCMStationIdFromJson(JSONObject jsonStation)
            throws JSONException {
        return jsonStation.getLong(OCM_ID);
    }

    /**
     * This method parses out the Station data as ContentValues
     * <p/>
     *
     * @param jsonStation JSONObject
     *
     * @return ContentValues describing OCM data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues getOCMStationContentValuesFromJson(JSONObject jsonStation)
            throws JSONException {


        Long id;

        String op_title;
        String op_url;

        Boolean pay_on_site;
        Boolean membership;
        Boolean accesskey;
        String usage_type_title;

        Double latitude;
        Double longitude;
        Double distance;
        String addr_title;
        String addr_line1;
        String addr_line2;
        String town;
        String state;
        String postcode;

        String country_title;
        String country_iso;

        String comments;

        String lastupdated;

        /* Get the id as long. */
        id = jsonStation.getLong(OCM_ID);

        /* Get the operatorInfo object and data. */
        JSONObject operator = jsonStation.getJSONObject(OCM_OPERATOR);
        op_title = operator.getString(OCM_OPERATOR_TITLE);
        op_url = operator.getString(OCM_OPERATOR_URL);

        /* Get the usage type object and data. */
        JSONObject usage_type = jsonStation.getJSONObject(OCM_UT);
        pay_on_site = usage_type.getBoolean(OCM_UT_PAY_ON_SITE);
        membership = usage_type.getBoolean(OCM_UT_MEMBERSHIP);
        accesskey = usage_type.getBoolean(OCM_UT_ACCESSKEY);
        usage_type_title = usage_type.getString(OCM_UT_TITLE);

        /* Get the address info and info. */
        JSONObject addr = jsonStation.getJSONObject(OCM_ADDR);
        latitude = addr.getDouble(OCM_ADDR_LATITUDE);
        longitude = addr.getDouble(OCM_ADDR_LONGITUDE);
        distance = addr.getDouble(OCM_ADDR_DISTANCE);
        addr_title = addr.getString(OCM_ADDR_TITLE);
        addr_line1 = addr.getString(OCM_ADDR_LINE1);
        addr_line2 = addr.getString(OCM_ADDR_LINE2);
        town = addr.getString(OCM_ADDR_TOWN);
        state = addr.getString(OCM_ADDR_STATE);
        postcode = addr.getString(OCM_ADDR_POSTCODE);

        /* Get the country info and data. */
        JSONObject country = addr.getJSONObject(OCM_ADDR_COUNTRY);
        country_iso = country.getString(OCM_ADDR_COUNTRY_ISO);
        country_title = country.getString(OCM_ADDR_COUNTRY_TITLE);

        /* get the general comments. */
        comments = jsonStation.getString(OCM_COMMENTS);

        /* get the last time updated. */
        // TODO: change to time object
        lastupdated = jsonStation.getString(OCM_TIME_UPDATED);

        ContentValues ocmStationContentValues = new ContentValues();

        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ID, id);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE, op_title);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_OPERATOR_WEBSITE, op_url);
        if(pay_on_site) {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_PAY_ON_SITE, 1);
        } else {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_PAY_ON_SITE, 0);
        }
        if (membership) {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_MEMBERSHIP, 1);
        } else {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_MEMBERSHIP, 0);
        }
        if (accesskey) {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_ACCESSKEY, 1);
        } else {
            ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_ACCESSKEY, 0);
        }
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_UT_TITLE, usage_type_title);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_LAT, latitude);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_LON, longitude);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_DISTANCE, distance);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE, addr_title);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_LINE1, addr_line1);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_LINE2, addr_line2);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN, town);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_STATE, state);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_POSTCODE, postcode);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_ISO, country_iso);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_TITLE, country_title);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_COMMENTS, comments);
        ocmStationContentValues.put(ChargingStationContract.StationEntry.COLUMN_TIME_UPDATED, lastupdated);

        return ocmStationContentValues;
    }

    /**
     * This method parses JSONArray from a jsonObject describing each connection at a station
     * <p/>
     *
     * @param jsonStation JSON containing the JSONArray for each connection
     *
     * @return Array of ContentValues
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getOCMConnectionsContentValuesFromJson(JSONObject jsonStation)
            throws JSONException {
        JSONArray connectionsJsonArray = jsonStation.getJSONArray(OCM_CONNECTIONS);


        ContentValues[] connectionsContentValues = new ContentValues[connectionsJsonArray.length()];

        for(int i = 0; i < connectionsJsonArray.length(); i++) {
            Long con_id;
            String type_title;
            int type_id;
            Boolean fast;
            String level_title;
            Double amp;
            Double volt;
            Double kw;
            String current_desc;
            String current_title;


            JSONObject connectionJson = connectionsJsonArray.getJSONObject(i);

            con_id = connectionJson.getLong(OCM_CONNECTION_ID);

            JSONObject connectionTypeJson = connectionJson.getJSONObject(OCM_CONNECTIONS_TYPE);
            type_title = connectionTypeJson.getString(OCM_CONNECTIONS_TYPE_TITLE);
            type_id = connectionTypeJson.getInt(OCM_CONNECTIONS_TYPE_ID);

            JSONObject connectionLevelJson = connectionJson.getJSONObject(OCM_CONNECTIONS_LEVEL);
            fast = connectionLevelJson.getBoolean(OCM_CONNECTIONS_LEVEL_FAST);
            level_title = connectionLevelJson.getString(OCM_CONNECTIONS_LEVEL_TITLE);

            amp = connectionJson.getDouble(OCM_CONNECTIONS_AMP);
            volt = connectionJson.getDouble(OCM_CONNECTIONS_VOLT);
            kw = connectionJson.getDouble(OCM_CONNECTIONS_KW);

            JSONObject connectionCurrentJson = connectionJson.getJSONObject(OCM_CONNECTIONS_CURRENT);
            current_desc = connectionCurrentJson.getString(OCM_CONNECTIONS_CURRENT_DESC);
            current_title = connectionCurrentJson.getString(OCM_CONNECTIONS_CURRENT_TITLE);

            ContentValues connectionContentValues = new ContentValues();
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_ID, con_id);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_TITLE, type_title);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID, type_id);
            if (fast) {
                connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST, 1);
            } else {
                connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST, 0);
            }
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_TITLE, level_title);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_AMP, amp);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_VOLT, volt);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_KW, kw);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_DESC, current_desc);
            connectionContentValues.put(ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_TITLE, current_title);

            connectionsContentValues[i] = connectionContentValues;

        }

        return  connectionsContentValues;
    }
}
