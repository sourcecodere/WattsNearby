package re.sourcecode.android.wattsnearby;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import re.sourcecode.android.wattsnearby.data.ChargingStationContract;


/**
 * Created by olem on 4/23/17.
 */

public class BottomSheetFragmentStation extends BottomSheetDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BottomSheetFragmentStation.class.getSimpleName();

    TextView viewOpTitleView;
    TextView viewOpWebSite;
    TextView viewUtTitle;
    TextView viewAccessKey;
    TextView viewMembership;
    TextView viewPayOnSite;
    TextView viewAddrTitle;
    TextView viewAddr1;
    TextView viewAddr2;
    TextView viewPostCode;
    TextView viewState;
    TextView viewTown;
    TextView viewCountry;
    Switch viewFavorite;
    ImageButton viewGoogleDirectionsBtn;
    ImageButton viewGoogleMapsBtn;

    /*
     * The columns of data that we are interested in displaying within our BottomSheetDialogFragment's
     * station display.
     */
    public static final String[] STATION_DETAIL_PROJECTION = {

            ChargingStationContract.StationEntry.COLUMN_OPERATOR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_OPERATOR_WEBSITE,
            ChargingStationContract.StationEntry.COLUMN_UT_TITLE,
            ChargingStationContract.StationEntry.COLUMN_UT_ACCESSKEY,
            ChargingStationContract.StationEntry.COLUMN_UT_MEMBERSHIP,
            ChargingStationContract.StationEntry.COLUMN_UT_PAY_ON_SITE,
            ChargingStationContract.StationEntry.COLUMN_LAT,
            ChargingStationContract.StationEntry.COLUMN_LON,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TITLE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_LINE1,
            ChargingStationContract.StationEntry.COLUMN_ADDR_LINE2,
            ChargingStationContract.StationEntry.COLUMN_ADDR_POSTCODE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_STATE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN,
            ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_TITLE,
            ChargingStationContract.StationEntry.COLUMN_FAVORITE
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_OPERATOR_TITLE = 0;
    public static final int INDEX_OPERATOR_WEBSITE = 1;
    public static final int INDEX_UT_TITLE = 2;
    public static final int INDEX_UT_ACCESSKEY = 3;
    public static final int INDEX_UT_MEMBERSHIP = 4;
    public static final int INDEX_UT_PAY_ON_SITE = 5;
    public static final int INDEX_LAT = 6;
    public static final int INDEX_LON = 7;
    public static final int INDEX_ADDR_TITLE = 8;
    public static final int INDEX_ADDR_LINE1 = 9;
    public static final int INDEX_ADDR_LINE2 = 10;
    public static final int INDEX_ADDR_POSTCODE = 11;
    public static final int INDEX_ADDR_STATE = 12;
    public static final int INDEX_ADDR_TOWN = 13;
    public static final int INDEX_ADDR_COUNTRY_TITLE = 14;
    public static final int INDEX_FAVORITE = 15;


    /*
    * This ID will be used to identify the Loader responsible for loading the station details
    * for a particular charging station. In some cases, one Activity can deal with many Loaders.
    * However, in our case, there is only one. We will still use this ID to initialize the loader
    * and create the loader for best practice. Please note that 353 was chosen arbitrarily.
    * You can use whatever number you like, so long as it is unique and consistent.
    */
    private static final int ID_STATION_LOADER = 122;

    /* the station id*/
    private Long mStationId;

    /* The URI that is used to access the chosen station's details */
    private Uri mStationUri;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ((getArguments() != null) && (getArguments().containsKey(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID))) { //user pushed a station

            mStationId = getArguments().getLong(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID);
            mStationUri = ChargingStationContract.StationEntry.buildStationUri(mStationId);

            View contentView = View.inflate(getContext(), R.layout.bottom_sheet_station, null);

            viewOpTitleView = (TextView) contentView.findViewById(R.id.sheet_station_operator_title);
            viewOpWebSite = (TextView) contentView.findViewById(R.id.sheet_station_operator_web);
            viewUtTitle = (TextView) contentView.findViewById(R.id.sheet_station_usage_type_title);
            viewAccessKey = (TextView) contentView.findViewById(R.id.sheet_station_key);
            viewMembership = (TextView) contentView.findViewById(R.id.sheet_station_membership);
            viewPayOnSite = (TextView) contentView.findViewById(R.id.sheet_pay_on_site);
            viewAddrTitle = (TextView) contentView.findViewById(R.id.sheet_station_addr_title);
            viewAddr1 = (TextView) contentView.findViewById(R.id.sheet_station_addr1);
            viewAddr2 = (TextView) contentView.findViewById(R.id.sheet_station_addr2);
            viewPostCode = (TextView) contentView.findViewById(R.id.sheet_station_addr_post_code);
            viewState = (TextView) contentView.findViewById(R.id.sheet_station_addr_state);
            viewTown = (TextView) contentView.findViewById(R.id.sheet_station_addr_town);
            viewCountry = (TextView) contentView.findViewById(R.id.sheet_station_country);
            viewFavorite = (Switch) contentView.findViewById(R.id.favorite_switch);
            viewGoogleDirectionsBtn = (ImageButton) contentView.findViewById(R.id.btn_google_maps_direction);
            viewGoogleMapsBtn = (ImageButton) contentView.findViewById(R.id.btn_google_maps);

            /* This connects our Activity into the loader lifecycle. */
            getLoaderManager().initLoader(ID_STATION_LOADER, null, this);

            return contentView;

        }
        return null;
    }

    /**
     * LoaderManager.LoaderCallbacks<Cursor>
     * <p>
     * Instantiate and return a new Loader for the given ID.
     *
     * @param loaderId The ID whose loader is to be created.
     * @param args     Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        if (mStationUri != null) {
            switch (loaderId) {

                case ID_STATION_LOADER:

                    return new CursorLoader(getActivity(),
                            mStationUri,
                            STATION_DETAIL_PROJECTION,
                            null,
                            null,
                            null);

                default:
                    throw new RuntimeException("Loader Not Implemented: " + loaderId);
            }
        } else {
            return null;
        }
    }

    /**
     * LoaderManager.LoaderCallbacks<Cursor>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            // null or empty cursor
            return;
        }
        viewOpTitleView.setText(data.getString(INDEX_OPERATOR_TITLE));
        viewOpWebSite.setText(data.getString(INDEX_OPERATOR_WEBSITE));
        viewUtTitle.setText(data.getString(INDEX_UT_TITLE));
        if (data.getInt(INDEX_UT_ACCESSKEY) == 1) {
            viewAccessKey.setText("ACCESSKEY");
        } else {
            viewAccessKey.setText("NO ACCESSKEY");
        }
        if (data.getInt(INDEX_UT_MEMBERSHIP) == 1) {
            viewMembership.setText("MEMBERSHIP");
        } else {
            viewMembership.setText("NO MEMBERSHIP");
        }
        if (data.getInt(INDEX_UT_PAY_ON_SITE) == 1) {
            viewPayOnSite.setText("PAY_ON_SITE");
        } else {
            viewPayOnSite.setText("NO PAY ON SITE");
        }
        viewAddrTitle.setText(data.getString(INDEX_ADDR_TITLE));
        viewAddr1.setText(data.getString(INDEX_ADDR_LINE1));
        viewAddr2.setText(data.getString(INDEX_ADDR_LINE2));
        viewPostCode.setText(data.getString(INDEX_ADDR_POSTCODE));
        viewState.setText(data.getString(INDEX_ADDR_STATE));
        viewTown.setText(data.getString(INDEX_ADDR_TOWN));
        viewCountry.setText(data.getString(INDEX_ADDR_COUNTRY_TITLE));
        if (data.getInt(INDEX_FAVORITE) == 1) {
            viewFavorite.setChecked(true);
        } else {
            viewFavorite.setChecked(false);
        }
        viewFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged");
                ContentResolver wattsContentResolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues();
                if (isChecked) {
                    values.put(ChargingStationContract.StationEntry.COLUMN_FAVORITE, 1);
                    wattsContentResolver.update(mStationUri, values, null, null);
                } else {
                    values.put(ChargingStationContract.StationEntry.COLUMN_FAVORITE, 0);
                    wattsContentResolver.update(mStationUri, values, null, null);
                }
            }
        });

    }

    /**
     * LoaderManager.LoaderCallbacks<Cursor>
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}




