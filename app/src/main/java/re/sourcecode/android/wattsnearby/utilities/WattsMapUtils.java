package re.sourcecode.android.wattsnearby.utilities;


import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by olem on 4/19/17.
 */

public class WattsMapUtils {
    /**
     * @return MarkerOptions for the car (not LatLng)
     */
    public static MarkerOptions getCarMarkerOptions(BitmapDescriptor markerIcon, String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.icon(markerIcon);
        markerOptions.anchor(0.38f, 0.6f);
        return markerOptions;
    }

}