package re.sourcecode.wattsnearby.utilities;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by olem on 3/6/17.
 */

public class OcmJsonUtils {


    // http://api.openchargemap.io/v2/poi/?output=json&countrycode=NO&maxresults=10&latitude=60.029265&longitude=11.0952163&distanceunit=km&distance=2

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
    private static final String OCM_ADDR_POSTCODE = "PostCode";
    private static final String OCM_ADDR_COUNTRY = "Country";
    private static final String OCM_ADDR_COUNTRY_ISO = "ISOCode";
    private static final String OCM_ADDR_COUNTRY_TITLE = "Title";

    /* Comments */
    private static final String OCM_COMMENTS = "GeneralComments";

    /* Status */
    private static final String OCM_TIME_UPDATED = "DateLastStatusUpdate";

    /* Connections */
    private static final String OCM_CONNECTIONS = "Connections"; //LIST OF CONNECTIONS AT ADDRESS
    private static final String OCM_CONNECTIONS_TYPE = "ConnectionType";
    private static final String OCM_CONNECTIONS_TYPE_TITLE = "Title"; //e.g. CHAdeMO
    private static final String OCM_CONNECTIONS_TYPE_ID = "ConnectionTypeID"; //e.g. 2 is CHAdeMO,  33 CCS
    private static final String OCM_CONNECTIONS_LEVEL_FAST = "IsFastChargeCapable";
    private static final String OCM_CONNECTIONS_LEVEL_TITLE = "Title";
    private static final String OCM_CONNECTIONS_AMP = "Amps";
    private static final String OCM_CONNECTIONS_VOLT = "Voltage";
    private static final String OCM_CONNECTIONS_KW = "PowerKW";
    private static final String OCM_CONNECTIONS_CURRENT = "CurrentType";
    private static final String OCM_CONNECTIONS_CURRENT_TITLE = "Title";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the charging stations in the current area
     * <p/>
     *
     * @param ocmJsonStr JSON response from server
     *
     * @return Array of ContentValues describing OCM data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getOCMContentValuesFromJson(Context context, String ocmJsonStr)
            throws JSONException {

        JSONArray ocmJsonArray = new JSONArray(ocmJsonStr); //JSONobject from json string

        ContentValues[] ocmContentValues = new ContentValues[ocmJsonArray.length()];

        for(int i = 0; i < ocmJsonArray.length(); i++) {
            JSONObject ocmJsonObj = ocmJsonArray.getJSONObject(i);
            //


        }

    }

}
