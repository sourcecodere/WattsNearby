package re.sourcecode.android.wattsnearby.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by olem on 4/9/17.
 */

public class WattsImageUtils {

    /**
     * Converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    public static BitmapDescriptor vectorToBitmap(Context context, int id, Integer addToScale) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() + addToScale,
                vectorDrawable.getIntrinsicHeight() + addToScale, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        //DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * @return MarkerOptions for the car
     */
    public static MarkerOptions getCarMarkerOptions(LatLng position, String title, BitmapDescriptor markerIcon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.icon(markerIcon);
        markerOptions.anchor(0.38f, 0.6f);
        markerOptions.position(position);
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
