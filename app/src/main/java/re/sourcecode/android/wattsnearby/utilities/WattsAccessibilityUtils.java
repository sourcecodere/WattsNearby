package re.sourcecode.android.wattsnearby.utilities;

import android.content.Context;


import re.sourcecode.android.wattsnearby.R;

/**
 * Created by olem on 5/11/17.
 *
 * Util to get strings for accessibility
 */
public class WattsAccessibilityUtils {
    public static String getConnectionDescription(Context context, int connectionId) {
        switch (connectionId) {
            case 2: //
                return context.getString(R.string.pref_enable_chademo_label);
            case 33:
                return context.getString(R.string.pref_enable_combo_ccs_eu_label);
            case 30:
                return context.getString(R.string.pref_enable_tesla_hpwc_label);
            case 25:
                return context.getString(R.string.pref_enable_type2_mennekes_label);
            case 32:
                return context.getString(R.string.pref_enable_type1_ccs_label);
            case 1:
                return context.getString(R.string.pref_enable_type1_j1772_label);
            default:
                return context.getString(R.string.pref_enable_other_input_label);
        }

    }
}
