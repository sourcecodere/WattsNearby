package re.sourcecode.android.wattsnearby.utilities;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by SourcecodeRe on 6/4/17.
 *
 * Utils for map markers
 */

public class MarkerUtils {

    /**
     * @return MarkerOptions for the car
     */
    public static MarkerOptions getCarMarkerOptions(String title, BitmapDescriptor markerIcon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.icon(markerIcon);
        markerOptions.anchor(0.38f, 0.6f);
        return markerOptions;
    }

    /**
     * @return MarkerOptions for the car
     */
    public static MarkerOptions getStationMarkerOptions(LatLng position, String title, BitmapDescriptor markerIcon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.icon(markerIcon);
        markerOptions.anchor(1.0f, 0.5f);
        markerOptions.position(position);
        return markerOptions;
    }
}
