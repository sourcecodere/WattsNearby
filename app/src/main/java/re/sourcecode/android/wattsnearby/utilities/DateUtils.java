package re.sourcecode.android.wattsnearby.utilities;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SourcecodeRe on 4/5/17.
 *
 * Util to handle date formats
 */
public class DateUtils {

    /**
     * This method returns the number of days since the epoch (January 01, 1970, 12:00 Midnight UTC)
     * in UTC time from the datestring from OCM
     *
     * @param dateString A date in OCM format, e.g. 2016-08-11T18:52:00Z.
     *
     * @return The number of days from the epoch to the date argument.
     */
    public static Long dateStringToEpoc(String dateString)
            throws ParseException{

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = format.parse(dateString);
        return date.getTime();
    }
}
