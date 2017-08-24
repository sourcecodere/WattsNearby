package re.sourcecode.android.wattsnearby.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import re.sourcecode.android.wattsnearby.MainMapActivity;
import re.sourcecode.android.wattsnearby.R;
import re.sourcecode.android.wattsnearby.adapter.ConnectionAdapter;
import re.sourcecode.android.wattsnearby.data.ChargingStationContract;
import re.sourcecode.android.wattsnearby.utilities.WidgetUtils;


/**
 * Created by SourcecodeRe on 4/23/17.
 * <p>
 * Bottom sheet for station details
 */
public class BottomSheetStationFragment extends BottomSheetDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BottomSheetStationFragment.class.getSimpleName();

    TextView viewTitle;
    TextView viewOpWebSite;
    TextView viewUtTitle;
    TextView viewUsageText;
    TextView viewAddress;
    TextView viewPostCode;
    TextView viewState;
    TextView viewTown;
    TextView viewCountry;
    SwitchCompat viewFavorite;
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
            ChargingStationContract.StationEntry.COLUMN_ADDR_POSTCODE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_STATE,
            ChargingStationContract.StationEntry.COLUMN_ADDR_TOWN,
            ChargingStationContract.StationEntry.COLUMN_ADDR_COUNTRY_ISO,
            ChargingStationContract.StationEntry.COLUMN_FAVORITE
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_STATION_OPERATOR_TITLE = 0;
    public static final int INDEX_STATION_OPERATOR_WEBSITE = 1;
    public static final int INDEX_STATION_UT_TITLE = 2;
    public static final int INDEX_STATION_UT_ACCESSKEY = 3;
    public static final int INDEX_STATION_UT_MEMBERSHIP = 4;
    public static final int INDEX_STATION_UT_PAY_ON_SITE = 5;
    public static final int INDEX_STATION_LAT = 6;
    public static final int INDEX_STATION_LON = 7;
    public static final int INDEX_STATION_ADDR_TITLE = 8;
    public static final int INDEX_STATION_ADDR_LINE1 = 9;
    public static final int INDEX_STATION_ADDR_POSTCODE = 10;
    public static final int INDEX_STATION_ADDR_STATE = 11;
    public static final int INDEX_STATION_ADDR_TOWN = 12;
    public static final int INDEX_STATION_ADDR_COUNTRY_CODE = 13;
    public static final int INDEX_STATION_FAVORITE = 14;

    /* The columns of data that we are interested in displaying within our BottomSheetDialogFragment's
     * connections display. */
    public static final String[] CONNECTIONS_DETAIL_PROJECTION = {
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_TYPE_ID,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_FAST,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_TITLE,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_LEVEL_TITLE,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_CURRENT_TYPE_TITLE,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_AMP,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_VOLT,
            ChargingStationContract.ConnectionEntry.COLUMN_CONN_KW
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_CONN_TYPE_ID = 0;
    public static final int INDEX_CONN_LEVEL_FAST = 1;
    public static final int INDEX_CONN_TITLE = 2;
    public static final int INDEX_CONN_LEVEL_TITLE = 3;
    public static final int INDEX_CONN_CURRENT_TYPE_TITLE = 4;
    public static final int INDEX_CONN_AMP = 5;
    public static final int INDEX_CONN_VOLT = 6;
    public static final int INDEX_CONN_KW = 7;

    /*
    * This ID will be used to identify the Loader responsible for loading the station details
    * for a particular charging station. In some cases, one Activity can deal with many Loaders.
    * However, in our case, there is only one. We will still use this ID to initialize the loader
    * and create the loader for best practice. Please note that 353 was chosen arbitrarily.
    * You can use whatever number you like, so long as it is unique and consistent.
    */
    private static final int ID_STATION_LOADER = 122;
    private static final int ID_CONNECTION_LOADER = 142;

    /* The URI that is used to access the chosen station's details */
    private Uri mStationUri;
    private Uri mConnectionUri;

    /* Data for external maps intent */
    private Double mLatitude;
    private Double mLongitude;
    private String mLabel;

    private ConnectionAdapter mConnectionAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ((getArguments() != null) && (getArguments().containsKey(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID))) { //user pushed a station

            View rootView = inflater.inflate(R.layout.fragment_station, container, false);

            /* the station id*/
            Long stationId = getArguments().getLong(MainMapActivity.ARG_DETAIL_SHEET_STATION_ID);

            mStationUri = ChargingStationContract.StationEntry.buildStationUri(stationId);
            mConnectionUri = ChargingStationContract.ConnectionEntry.buildStationUri(stationId);

            viewTitle = (TextView) rootView.findViewById(R.id.sheet_station_title);
            viewOpWebSite = (TextView) rootView.findViewById(R.id.sheet_station_operator_web);
            viewUtTitle = (TextView) rootView.findViewById(R.id.sheet_station_usage_type_title);
            viewUsageText = (TextView) rootView.findViewById(R.id.sheet_station_usage_text);
            viewAddress = (TextView) rootView.findViewById(R.id.sheet_station_addr);
            viewPostCode = (TextView) rootView.findViewById(R.id.sheet_station_addr_post_code);
            viewState = (TextView) rootView.findViewById(R.id.sheet_station_addr_state);
            viewTown = (TextView) rootView.findViewById(R.id.sheet_station_addr_town);
            viewCountry = (TextView) rootView.findViewById(R.id.sheet_station_addr_country);
            viewFavorite = (SwitchCompat) rootView.findViewById(R.id.favorite_switch);
            viewGoogleDirectionsBtn = (ImageButton) rootView.findViewById(R.id.btn_google_maps_direction);
            viewGoogleMapsBtn = (ImageButton) rootView.findViewById(R.id.btn_google_maps);



            /*
            * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
            * do things like set the adapter of the RecyclerView and toggle the visibility.
            */
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_list);
            /*
            * A LinearLayoutManager is responsible for measuring and positioning item views within a
            * RecyclerView into a linear list. This means that it can produce either a horizontal or
            * vertical list depending on which parameter you pass in to the LinearLayoutManager
            * constructor. In our case, we want a vertical list, so we pass in the constant from the
             * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
             *
            * There are other LayoutManagers available to display your data in uniform grids,
            * staggered grids, and more! See the developer documentation for more details.
            *
            * The third parameter (shouldReverseLayout) should be true if you want to reverse your
            * layout. Generally, this is only true with horizontal lists that need to support a
            * right-to-left layout.
            */
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

            /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
            mRecyclerView.setLayoutManager(layoutManager);

            /*
            * Use this setting to improve performance if you know that changes in content do not
            * change the child layout size in the RecyclerView
            */
            //mRecyclerView.setHasFixedSize(true);

            mConnectionAdapter = new ConnectionAdapter(getActivity());
            mRecyclerView.setAdapter(mConnectionAdapter);

            /* This connects our Activity into the loader lifecycle. */
            getLoaderManager().initLoader(ID_STATION_LOADER, null, this);
            getLoaderManager().initLoader(ID_CONNECTION_LOADER, null, this);

            return rootView;

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

        if ((mStationUri != null) && (mConnectionUri != null)) {
            switch (loaderId) {

                case ID_STATION_LOADER:
                    return new CursorLoader(getActivity(),
                            mStationUri,
                            STATION_DETAIL_PROJECTION,
                            null,
                            null,
                            null);

                case ID_CONNECTION_LOADER:
                    return new CursorLoader(getActivity(),
                            mConnectionUri,
                            CONNECTIONS_DETAIL_PROJECTION,
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
        if (loader.getId() == ID_STATION_LOADER) {
            if (data.getString(INDEX_STATION_ADDR_TITLE) != null) {
                viewTitle.setText(data.getString(INDEX_STATION_ADDR_TITLE));
            } else {
                viewTitle.setText(data.getString(INDEX_STATION_OPERATOR_TITLE));
            }

            if (data.getString(INDEX_STATION_UT_TITLE) != null) {
                viewUtTitle.setText(data.getString(INDEX_STATION_UT_TITLE));
            }

            if (data.getString(INDEX_STATION_OPERATOR_WEBSITE) != null) {
                viewOpWebSite.setText(data.getString(INDEX_STATION_OPERATOR_WEBSITE));
            }

            // Maybe we need addr2 also? Skipped to save space.
            viewAddress.setText(data.getString(INDEX_STATION_ADDR_LINE1));
            viewPostCode.setText(data.getString(INDEX_STATION_ADDR_POSTCODE));
            viewState.setText(data.getString(INDEX_STATION_ADDR_STATE));
            viewTown.setText(data.getString(INDEX_STATION_ADDR_TOWN));
            viewCountry.setText(data.getString(INDEX_STATION_ADDR_COUNTRY_CODE));


            ArrayList<String> usageClauses = new ArrayList<>();

            if (data.getInt(INDEX_STATION_UT_ACCESSKEY) == 1) {
                usageClauses.add((String) getText(R.string.ut_accesskey));
            }

            if (data.getInt(INDEX_STATION_UT_MEMBERSHIP) == 1) {
                usageClauses.add((String) getText(R.string.ut_membership));
            }

            if (data.getInt(INDEX_STATION_UT_PAY_ON_SITE) == 1) {
                usageClauses.add((String) getText(R.string.ut_pay_on_site));

            }

            for (int i = 0; i < usageClauses.size(); i++) {
                if (!viewUsageText.getText().equals("")) {
                    viewUsageText.setText(
                            viewUsageText.getText() + "-" + usageClauses.get(i)
                    );
                } else {
                    viewUsageText.setText(usageClauses.get(i));
                }
            }

            if (data.getInt(INDEX_STATION_FAVORITE) == 1) {
                viewFavorite.setChecked(true);
            } else {
                viewFavorite.setChecked(false);
            }
            // the favorite switch with listener
            viewFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged");
                    ContentResolver wattsContentResolver = getActivity().getContentResolver();
                    ContentValues values = new ContentValues();
                    if (isChecked) {
                        values.put(ChargingStationContract.StationEntry.COLUMN_FAVORITE, 1);
                        // Update the database
                        wattsContentResolver.update(mStationUri, values, null, null);
                        // Refresh any widgets
                        WidgetUtils.sendRefreshBroadcast(getContext());
                    } else {
                        values.put(ChargingStationContract.StationEntry.COLUMN_FAVORITE, 0);
                        // Update the database
                        wattsContentResolver.update(mStationUri, values, null, null);
                        // Refresh any widgets
                        WidgetUtils.sendRefreshBroadcast(getContext());
                    }
                }
            });

            mLatitude = data.getDouble(INDEX_STATION_LAT);
            mLongitude = data.getDouble(INDEX_STATION_LON);
            if (data.getString(INDEX_STATION_OPERATOR_TITLE) != null) {
                mLabel = data.getString(INDEX_STATION_OPERATOR_TITLE);
            } else {
                mLabel = data.getString(INDEX_STATION_ADDR_TITLE);
            }
            // the google map button
            viewGoogleMapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mLatitude, mLongitude, mLatitude, mLongitude, mLabel);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    getContext().startActivity(intent);
                }
            });
            // the google directions button
            viewGoogleDirectionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", mLatitude, mLongitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    getContext().startActivity(intent);
                }
            });

        } else if (loader.getId() == ID_CONNECTION_LOADER) {
            mConnectionAdapter.swapCursor(data);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
            if (data.getCount() != 0) {
                /* Finally, make sure the connection data is visible */
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * LoaderManager.LoaderCallbacks<Cursor>
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mConnectionAdapter.swapCursor(null);
    }

}




