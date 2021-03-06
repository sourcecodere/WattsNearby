package re.sourcecode.android.wattsnearby.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import re.sourcecode.android.wattsnearby.R;
/**
 * Created by SourcecodeRe on 5/1/17.
 *
 * Inspired and code copied from https://github.com/udacity/ud851-Sunshine developed by udacity.com
 *
 */

public class Preferences {



    /**
     * Returns metric or imperial based on user settings and defaults
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see fast chargers, false otherwise
     */
    public static String getUnitsValue(Context context) {
        String unitsValue;

        /* Key for accessing the preference for units */
        String unitsKey = context.getString(R.string.pref_units_key);


        boolean metricUnitsOnByDefault = context
                .getResources()
                .getBoolean(R.bool.default_metric_units);

        if( metricUnitsOnByDefault) {
            unitsValue = context.getResources().getString(R.string.pref_metric_value);
        }
        else {
            unitsValue = context.getResources().getString(R.string.pref_imperial_value);
        }

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(unitsKey, unitsValue);
    }


    /**
     * Returns true if the user prefers to see only fast chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see fast chargers, false otherwise
     */
    public static boolean areOnlyFastChargersEnabled(Context context) {
        /* Key for accessing the preference for showing only fast chargers */
        String displayOnlyFastChargersKey = context.getString(R.string.pref_enable_only_fast_chargers_key);

        /*
         * In WattsNearby, the user has the ability to say whether she would like se only fast chargers
         * enabled or not. If no preference has been chosen, we want to be able to determine
         * whether or not to show them. To do this, we reference a bool stored in bools.xml.
         */
        boolean shouldDisplayOnlyFastChargersByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_only_fast_chargers);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayOnlyFastChargersKey, shouldDisplayOnlyFastChargersByDefault);
    }


    /**
     * Returns true if the user prefers to see CEE 7/4 - Schuko - Type F chargers, false otherwise.
     * This preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see CEE 7/4 - Schuko - Type F chargers, false otherwise
     */
    public static boolean areSchukoEnabled(Context context) {
        /* Key for accessing the preference for showing tesla schuko */
        String displaySchukoKey = context.getString(R.string.pref_enable_schuko_key);

        boolean shouldDisplaySchukoByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_schuko);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displaySchukoKey, shouldDisplaySchukoByDefault);
    }

    /**
     * Returns true if the user prefers to see type 2 menneske chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see type 2 menneske chargers, false otherwise
     */
    public static boolean areType2MenneskeEnabled(Context context) {
        /* Key for accessing the preference for showing type 2 menneske chargers */
        String displayType2MenneskeKey = context.getString(R.string.pref_enable_type2_mennekes_key);

        boolean shouldDisplayType2MenneskeByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_type2_mennekes);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayType2MenneskeKey, shouldDisplayType2MenneskeByDefault);
    }

    /**
     * Returns true if the user prefers to see combo css eu chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see combo css eu chargers, false otherwise
     */
    public static boolean areComboCcsEuEnabled(Context context) {
        /* Key for accessing the preference for showing combo css eu chargers */
        String displayComboCcsEuKey = context.getString(R.string.pref_enable_combo_ccs_eu_key);

        boolean shouldDisplayComboCcsEuByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_combo_ccs_eu);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayComboCcsEuKey, shouldDisplayComboCcsEuByDefault);
    }

    /**
     * Returns true if the user prefers to see type 1 j1772 chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see type 1 j1772 chargers, false otherwise
     */
    public static boolean areType1j1772Enabled(Context context) {
        /* Key for accessing the preference for showing type 1 j1772 chargers */
        String displayType1j1772Key = context.getString(R.string.pref_enable_type1_j1772_key);

        boolean shouldDisplayType1j1772ByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_type1_j1772);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayType1j1772Key, shouldDisplayType1j1772ByDefault);
    }

    /**
     * Returns true if the user prefers to see type 1 ccs chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see type 1 ccs chargers, false otherwise
     */
    public static boolean areType1CcsEnabled(Context context) {
        /* Key for accessing the preference for showing type 1 ccs chargers */
        String displayType1CcsKey = context.getString(R.string.pref_enable_type1_ccs_key);

        boolean shouldDisplayType1CcsByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_type1_ccs);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        return sp.getBoolean(displayType1CcsKey, shouldDisplayType1CcsByDefault);
    }

    /**
     * Returns true if the user prefers to see chademo chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see chademo chargers, false otherwise
     */
    public static boolean areChademoEnabled(Context context) {
        /* Key for accessing the preference for showing chademo chargers */
        String displayChademoKey = context.getString(R.string.pref_enable_chademo_key);

        boolean shouldDisplayChademoByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_chademo);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayChademoKey, shouldDisplayChademoByDefault);
    }

    /**
     * Returns true if the user prefers to see tesla hpwc chargers, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see tesla hpwc chargers, false otherwise
     */
    public static boolean areTeslaHpwcEnabled(Context context) {
        /* Key for accessing the preference for showing tesla hpwc chargers */
        String displayTeslaHpwcKey = context.getString(R.string.pref_enable_tesla_hpwc_key);

        boolean shouldDisplayTeslaHpwcByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_tesla_hpwc);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayTeslaHpwcKey, shouldDisplayTeslaHpwcByDefault);
    }

    /**
     * Returns true if the user prefers to see other charger inputs, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to only see other charger inputs, false otherwise
     */
    public static boolean areOtherInputsEnabled(Context context) {
        /* Key for accessing the preference for showing other charger inputs */
        String displayOtherInputKey = context.getString(R.string.pref_enable_other_input_key);

        boolean shouldDisplayOtherInputByDefault = context
                .getResources()
                .getBoolean(R.bool.enable_other_input);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */

        return sp.getBoolean(displayOtherInputKey, shouldDisplayOtherInputByDefault);
    }
}
