package re.sourcecode.android.wattsnearby.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import re.sourcecode.android.wattsnearby.R;

/**
 * Created by SourcecodeRe on 4/9/17.
 * <p>
 * Util to handle vector graphics in WattsNearby
 */
public class WattsImageUtils {

    /**
     * Converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    public static BitmapDescriptor vectorToBitmap(Context context, int id, Integer addToScale) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, null);
        Bitmap bitmap;
        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() + addToScale,
                    vectorDrawable.getIntrinsicHeight() + addToScale, Bitmap.Config.ARGB_8888);


            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

            //DrawableCompat.setTint(vectorDrawable, color);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }

    public static Drawable getConnectionIcon(Context context, int connectionId) {
        switch (connectionId) {
            case 28:
                return ContextCompat.getDrawable(context, R.drawable.ic_schuko);
            case 2: //
                return ContextCompat.getDrawable(context, R.drawable.ic_chademo);
            case 33:
                return ContextCompat.getDrawable(context, R.drawable.ic_combo_ccs_eu);
            case 30:
                return ContextCompat.getDrawable(context, R.drawable.ic_tesla_hpwc);
            case 25:
                return ContextCompat.getDrawable(context, R.drawable.ic_type2_mennekes);
            case 32:
                return ContextCompat.getDrawable(context, R.drawable.ic_type1_ccs);
            case 1:
                return ContextCompat.getDrawable(context, R.drawable.ic_type1_j1772);
            default:
                return ContextCompat.getDrawable(context, R.drawable.ic_other_input);
        }

    }
}
