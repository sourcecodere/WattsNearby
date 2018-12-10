package re.sourcecode.android.wattsnearby.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.data.Preferences;
import re.sourcecode.android.wattsnearby.R;

/**
 * Created by SourcecodeRe on 4/29/17.
 * <p>
 * Inspired and code copied from https://github.com/udacity/ud851-Sunshine developed by udacity.com
 * <p>
 * The SettingsFragment serves as the display for all of the user's settings. In WattsNearby, the
 * user will be able to change their preference for seeing only fast chargers, and the user can
 * select only their car's connection outlets to filter out charging stations.
 * <p>
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences mSharedPreferences;


    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.prefernces_general);

        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof TwoStatePreference)) {
                // for things other than CheckBoxPreference or SwitchPreference, e.g. the PreferenceCategory
                String value = mSharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
        // reset the changed boolean every time the fragment is created
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(MainMapActivity.FILTER_CHANGED_KEY, false).apply();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = findPreference(key);
        if (preference != null && preference instanceof CheckBoxPreference) {
            // Get the current state of the CheckBoxPreference,
            // if any filter has changed flag it in a shared preference boolean
            boolean isOn = sharedPreferences.getBoolean(key, false);
            if (isOn) {
                Log.d(TAG, "Filter ( preferences ) changed to on: " + key);
                mSharedPreferences.edit().putBoolean(MainMapActivity.FILTER_CHANGED_KEY, true).apply();
            } else {
                Log.d(TAG, "Filter ( preferences ) changed to off: " + key);
                mSharedPreferences.edit().putBoolean(MainMapActivity.FILTER_CHANGED_KEY, true).apply();

            }
        } else if (preference != null && preference instanceof ListPreference) {
            String units = Preferences.getUnitsValue(getContext());
            mSharedPreferences.edit().putString(key, units);
            Log.d(TAG, "Units in preferences changed to " + units);
            setPreferenceSummary(preference, units);


        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add the shared preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the shared preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

