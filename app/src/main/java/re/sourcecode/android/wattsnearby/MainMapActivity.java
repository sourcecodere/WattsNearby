package re.sourcecode.android.wattsnearby;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.Snackbar;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.maps.android.PolyUtil;

import java.util.HashMap;
import java.util.List;

import re.sourcecode.android.wattsnearby.fragment.BottomSheetGenericFragment;
import re.sourcecode.android.wattsnearby.fragment.BottomSheetStationFragment;
import re.sourcecode.android.wattsnearby.loader.DirectionsLoader;
import re.sourcecode.android.wattsnearby.loader.StationMarkersLoader;
import re.sourcecode.android.wattsnearby.sync.OCMSyncTask;
import re.sourcecode.android.wattsnearby.sync.OCMSyncTaskListener;
import re.sourcecode.android.wattsnearby.utilities.DataUtils;
import re.sourcecode.android.wattsnearby.utilities.ImageUtils;
import re.sourcecode.android.wattsnearby.utilities.MarkerUtils;

/**
 * Created by SourceCodeRe
 **/

public class MainMapActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PlaceSelectionListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, // To clean up polyline from directions
        GoogleMap.OnInfoWindowClickListener,
        BottomSheetStationFragment.OnDirectionsReceivedListener,  // to send distance data from map to bottom sheet
        GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks {

    private static final String TAG = MainMapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_LOCATION = 0;  // For controlling necessary Permissions.

    private static final int INTENT_PLACE = 1; // For places search

    private static final int LOADER_MARKERS = 3; // ID for loading markers on async loader thread

    private static final int LOADER_DIRECTIONS = 4; // ID for loading directions on async loader thread

    private GoogleApiClient mGoogleApiClient; // The google services connection.
    private LocationRequest mLocationRequest; // Periodic location request object.

    private GoogleMap mMap; // The map object.

    private LatLng mLastLocation; // Last known position on the phone/car.
    private LatLng mLastCameraCenter; // Latitude and longitude of last camera center.
    private LatLng mLastOCMCameraCenter; // Latitude and longitude of last camera center where the OCM api was synced against the content provider.

    private MarkerOptions mMarkerOptionsCar; // Icon for the car.
    private Marker mCurrentLocationMarker; // Car marker with position

    private Polyline mDirectionsPolyLine; // Directions poly line from car to selected marker

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Marker> mVisibleStationMarkers = new HashMap<>(); // hashMap <stationId, Marker> of station markers in the current map

    private long mStationIdFromIntent; // For intent

    private int mBottomSheetStationFragmentId; // To communicate with fragment interface

    private ProgressBar mProgressBar;

    // Setup analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String ARG_DETAIL_SHEET_STATION_ID = "station_id"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_DETAIL_SHEET_ABOUT = "about"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_WIDGET_INTENT_KEY = "station_id";
    public static final String ARG_MAP_VISIBLE_BOUNDS = "visible_bounds";
    public static final String FILTER_CHANGED_KEY = "changed"; // used in main to check for changes and in settings fragment to set changes
    public static final String ARG_DIRECTIONS_DEST = "destination"; // used in directions loader
    public static final String ARG_DIRECTIONS_ORIGIN = "origin"; // used in directions loader
    /**
     * AppCompatActivity override
     * <p/>
     * First call in the lifecycle. This is followed by onStart().
     *
     * @param savedInstanceState contains the activity previous frozen state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        MapsInitializer.initialize(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);



        if (!isOnline()) {
            Snackbar.make(
                    MainMapActivity.this.findViewById(R.id.main_layout),
                    getString(R.string.error_not_online),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            getString(R.string.snackbar_ok_btn),
                            new View.OnClickListener() {
                                /**
                                 * Called when a view has been clicked.
                                 *
                                 * @param v The view that was clicked.
                                 */
                                @Override
                                public void onClick(View v) {
                                    //exit
                                    analyticsLogSelectContent(
                                            getString(R.string.analytics_id_snack_bar),
                                            getString(R.string.analytics_name_snack_bar_not_online),
                                            getString(R.string.analytics_content_type_snack_bar)
                                    );
                                    finish();
                                }
                            })
                    .show();
        }


        // Fab for the my location. With onClickListener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_my_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "My location fab clicked!");
                }
                analyticsLogSelectContent(
                        getString(R.string.analytics_id_fab),
                        getString(R.string.analytics_content_type_fab),
                        getString(R.string.analytics_name_fab_my_location));

                // Try to set last location, update car marker, and zoom to location
                updateCurrentLocation(true);


            }
        });


        // Intent with stationId (e.g. from widget list item click)
        if (getIntent().hasExtra(ARG_WIDGET_INTENT_KEY)) {
            mStationIdFromIntent = getIntent().getLongExtra(ARG_WIDGET_INTENT_KEY, 0L);
        }

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        // Setup the banner ad
        MobileAds.initialize(getApplicationContext(),
                getString(R.string.banner_app_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("2B3C903E8A681D2047F678BBCD58B109")
                .build();
        adView.loadAd(adRequest);

        // set the default value of filter changed flag to false
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FILTER_CHANGED_KEY, false).apply();

        // for progressbar when loading markers to map
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

    }

    /**
     * AppCompatActivity override
     * <p/>
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @SuppressLint("UseSparseArrays")
    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onResume");
        }
        super.onResume();
        mGoogleApiClient.connect();
        boolean changedPrefs = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FILTER_CHANGED_KEY, false);
        if (changedPrefs) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onResume preferences changed! resetting the markers");
            }
            // Remove the markers in the map
            for (Marker value : mVisibleStationMarkers.values()) {
                value.remove();
            }
            mVisibleStationMarkers = new HashMap<>();
            refreshMapStationMarkers();
        }
        if (mDirectionsPolyLine != null) {
            mDirectionsPolyLine.remove();
        }

    }

    /**
     * AppCompatActivity override
     * <p/>
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPause");
            //Log.d(TAG, "onPause mVisibleStationMarkers: " + mVisibleStationMarkers.size());
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * AppCompatActivity override
     * <p/>
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStart");
        }
        buildGoogleApiClient(); // Get connection to google services.
        super.onStart();
    }

    /**
     * AppCompatActivity override
     * <p/>
     * Dispatch onStop() to all fragments.  Ensure all loaders are stopped.
     */
    @Override
    protected void onStop() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStop");
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        // super
        super.onStop();
    }

    /**
     * AppCompatActivity override
     * <p/>
     * Save all appropriate fragment state.
     *
     * @param savedInstanceState the saved state
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onSaveInstanceState");
            //Log.d(TAG, "onSaveInstanceState mVisibleStationMarkers: " + mVisibleStationMarkers.size());
        }
//        savedInstanceState.putSerializable("markers", (Serializable) mVisibleStationMarkers);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onRestoreInstanceState mVisibleStationMarkers:" + mVisibleStationMarkers.size());
        }
//        if (savedInstanceState != null) {
//            mVisibleStationMarkers = (HashMap<Long, Marker>) savedInstanceState.getSerializable("markers");
//        }

    }

    /**
     * AppCompatActivity override
     * <p/>
     *
     * @param menu the options menu
     * @return boolean true or false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateOptionsMenu");
        }
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * AppCompatActivity override
     * <p/>
     *
     * @param item the menu item selected
     * @return boolean true or false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onOptionsItemSelected");
        }
        int id = item.getItemId();
        if (id == R.id.action_search) {
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder
                        (PlaceAutocomplete.MODE_OVERLAY)
                        .setBoundsBias(mMap.getProjection().getVisibleRegion().latLngBounds)
                        .build(MainMapActivity.this);
                startActivityForResult(intent, INTENT_PLACE);
            } catch (GooglePlayServicesRepairableException |
                    GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return false;
        } else if (id == R.id.action_about) {
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetGenericFragment();

            Bundle args = new Bundle();
            args.putBoolean(ARG_DETAIL_SHEET_ABOUT, true);
            bottomSheetDialogFragment.setArguments(args);

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * OnMapReadyCallback
     * <p/>
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMapReady");
        }
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        //Check if Google Play Services Available or not
        if (!checkGooglePlayServices()) {
            if (BuildConfig.DEBUG) {
                Log.d("onCreate", "Google Play Services are not available");
            }
            finish();
        } else {
            if (BuildConfig.DEBUG) {
                Log.d("onCreate", "Google Play Services available.");
            }
        }

        // Create the car location marker, set position later
        mMarkerOptionsCar = MarkerUtils.getCarMarkerOptions(
                getString(R.string.marker_current),
                ImageUtils.vectorToBitmap(
                        this,
                        R.drawable.ic_car_color_sharp,
                        getResources().getInteger(R.integer.car_icon_add_to_size)
                )
        );

        // Init loader for markers. No args, means it returns null in onLoadFinished
        // use restartLoader for updating map markers.
        getSupportLoaderManager().initLoader(LOADER_MARKERS, null, this);
        // Init loader for directions. No args, means it returns null in onLoadFinished
        // use restartLoader for updating directions.
        getSupportLoaderManager().initLoader(LOADER_DIRECTIONS, null, this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Disables the my location button, since we are using own fab for this..
            mMap.setMyLocationEnabled(false);
        }

        // Disables the my location button, since we are using own fab for this..
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Disable Map Toolbar
        mMap.getUiSettings().setMapToolbarEnabled(false);
        // Enable zoom control
        mMap.getUiSettings().setZoomControlsEnabled(true);


        // Setup callback for camera movement (onCameraMove).
        mMap.setOnCameraMoveListener(this);
        // Setup callback for when camera has stopped moving (onCameraIdle).
        mMap.setOnCameraIdleListener(this);
        // Setup callback for when user clicks on marker
        mMap.setOnMarkerClickListener(this);
        // Setup callback for when user clicks on the marker title
        mMap.setOnInfoWindowClickListener(this);
        // Setup callback for when user clicks on other parts of the map
        mMap.setOnMapClickListener(this);

        // Intent handling.
        if (mStationIdFromIntent != 0L) {
            // TODO: move center a bit when bottom sheet opens.

            // Move the camera to station position
            LatLng stationLatLng = DataUtils.getStationLatLng(getApplicationContext(), mStationIdFromIntent);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    stationLatLng,
                    getResources().getInteger(R.integer.zoom_station_widget)
            );
            mMap.animateCamera(cameraUpdate);


            // Open bottom sheet for station
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetStationFragment();

            Bundle args = new Bundle();
            args.putLong(ARG_DETAIL_SHEET_STATION_ID, mStationIdFromIntent);
            bottomSheetDialogFragment.setArguments(args);

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }

        // Moves the bottom map elements up a bit to fit the adView.
        mMap.setPadding(0, 0, 0, AdSize.BANNER.getHeightInPixels(this));
    }

    /**
     * GoogleApiClient.ConnectionCallbacks
     * </p>
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onConnected");
        }

        // Create location requests and setup location services.
        setupLocationServices();

        // Try to set last location, update car marker, and do not zoom to location
        updateCurrentLocation(false);
    }

    /**
     * GoogleApiClient.ConnectionCallbacks
     * </p>
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onConnectionSuspended");
        }
    }

    /**
     * GoogleApiClient.OnConnectionFailedListener
     * </p>
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onConnectionFailed");
        }
    }

    /**
     * LocationListener
     * </p>
     */
    @Override
    public void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLocationChanged");
        }
        // Update last location the the new location
        mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Move the car markers current position if we already have it in the map
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.setPosition(mLastLocation);

        } else { //else add it to the map
            mMarkerOptionsCar.position(mLastLocation);
            mCurrentLocationMarker = mMap.addMarker(mMarkerOptionsCar);
        }
    }

    /**
     * GoogleMap.onMarkerClick called when marker is clicked
     * </p>
     *
     * @param marker clicked
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMarkerClick");
        }
        Long stationId = (Long) marker.getTag();

        if (stationId != null) { //every station marker should have data (stationId), the car doesn't.

            // restart the loader for directions
            Bundle args = new Bundle();
            args.putParcelable(ARG_DIRECTIONS_ORIGIN, mLastLocation);
            args.putParcelable(ARG_DIRECTIONS_DEST, marker.getPosition());
            getSupportLoaderManager().restartLoader(LOADER_DIRECTIONS, args, this).forceLoad();

            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetStationFragment();

            args = new Bundle();
            args.putLong(ARG_DETAIL_SHEET_STATION_ID, stationId);
            bottomSheetDialogFragment.setArguments(args);
            mBottomSheetStationFragmentId = bottomSheetDialogFragment.getId();

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        } else {
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetGenericFragment();

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // Just do the same as when a marker icon is clicked
        onMarkerClick(marker);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mDirectionsPolyLine != null) {
            mDirectionsPolyLine.remove();
        }
    }

    /**
     * GoogleMap.onCameraMove callback. Updates the center of the map
     * </p>
     */
    @Override
    public void onCameraMove() {
        //if (BuildConfig.DEBUG) {
        //Log.d(TAG, "onCameraMove");
        //}
        // Get the current visible region of the map, and save the center LatLng
        mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
    }

    /**
     * GoogleMap.onCameraIdle
     * <p/>
     * Callback. Checks if the new idle position of the map camera should initiate a OCM sync
     */
    @Override
    public void onCameraIdle() {
        //if (BuildConfig.DEBUG) {
        //Log.d(TAG, "onCameraIdle");
        //}

        Resources resources = getResources();

        int currentZoom = Math.round(mMap.getCameraPosition().zoom);
        //if (BuildConfig.DEBUG) {
        //Log.d(TAG, currentZoom.toString());
        //}

        // First check that the zoom level is high enough
        // to make it reasonable to trigger a sync at all
        if (currentZoom > getResources().getInteger(R.integer.min_zoom_level)) {

            // The computed distance is stored in results[0].
            // If results has length 2 or greater, the initial bearing is stored in results[1].
            // If results has length 3 or greater, the final bearing is stored in results[2].
            float[] results = new float[3];

            if ((mLastCameraCenter != null) && (mLastOCMCameraCenter != null)) {
                Location.distanceBetween(
                        mLastCameraCenter.latitude,
                        mLastCameraCenter.longitude,
                        mLastOCMCameraCenter.latitude,
                        mLastOCMCameraCenter.longitude,
                        results);

                // Get the distance from last sync.
                int distanceFromLastOCMSync = Math.round(results[0]);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Distance from last OCM sync: " + distanceFromLastOCMSync);
                    Log.d(TAG, "Current zoom level: " + currentZoom);
                }
                // If zoom is between min_zoom_level and zoom_level_near
                // and the camera movement distance is more than significant_cam_move_far
                if ((currentZoom < resources.getInteger(R.integer.zoom_level_near))
                        && (distanceFromLastOCMSync > resources.getInteger(R.integer.significant_cam_move_far))) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Far zoom.");
                    }

                    mLastOCMCameraCenter = mLastCameraCenter;

                    initiateOCMSync(
                            mLastCameraCenter,
                            (double) getResources().getInteger(R.integer.ocm_radius_km_far)
                    );

                    // If zoom is more than zoom_level_near
                    // and the camera movement distance is more than significant_cam_move_near
                } else if ((currentZoom >= resources.getInteger(R.integer.zoom_level_near))
                        && (distanceFromLastOCMSync >= resources.getInteger(R.integer.significant_cam_move_near))) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Near zoom.");
                    }

                    mLastOCMCameraCenter = mLastCameraCenter;

                    initiateOCMSync(
                            mLastCameraCenter,
                            (double) getResources().getInteger(R.integer.ocm_radius_km_near)
                    );

                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Need to move the camera more to sync..");
                    }
                }
            }

            refreshMapStationMarkers();

        }
    }


    /**
     * Get result from places activity
     * <p/>
     * Dispatch incoming result to the correct fragment. startActivityForResult
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityResult");
        }
        if (requestCode == INTENT_PLACE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                this.onPlaceSelected(place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                this.onError(status);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Places API
     * <p/>
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Place Selected: " + place.getName());
        }
        LatLng placeLatLng = place.getLatLng();

        // move the camera
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                placeLatLng,
                getResources().getInteger(R.integer.zoom_places_search)
        );
        mMap.animateCamera(cameraUpdate);
        // TODO: could possibly be removed if distance from last OCM sync would work on large distances
        initiateOCMSync(
                placeLatLng,
                (double) getResources().getInteger(R.integer.ocm_radius_km_near)
        );
    }

    /**
     * Places API
     * <p/>
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, getText(R.string.toast_place_selection_failed) + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == LOADER_MARKERS) {
            return new StationMarkersLoader(getApplicationContext(), args);
        } else if (id == LOADER_DIRECTIONS) {

            return new DirectionsLoader(getApplicationContext(), args);
        } else {
            return null;
        }
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {

        int id = loader.getId();
        if (id == LOADER_MARKERS) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLoadFinished");
                Log.d(TAG, "onLoadFinished mVisibleStationMarkers before clean up:" + mVisibleStationMarkers.size());
            }
            if (data instanceof HashMap) {
                HashMap<Long, MarkerOptions> hashMapData = (HashMap<Long, MarkerOptions>) data;
                // clean up map
                for (HashMap.Entry<Long, Marker> entry : mVisibleStationMarkers.entrySet()) {
                    Long stationId = entry.getKey();
                    Marker marker = entry.getValue();
                    if (hashMapData.get(stationId) == null) {
                        marker.remove();
                    }
                }
                mVisibleStationMarkers.keySet().retainAll(hashMapData.keySet()); //filters out not visible stations from hashmap

                for (HashMap.Entry<Long, MarkerOptions> entry : hashMapData.entrySet()) {
                    Long stationId = entry.getKey();
                    MarkerOptions markerOptions = entry.getValue();
                    if ((mMap != null) && (!mVisibleStationMarkers.containsKey(stationId))) {
                        Marker tmpMarker = mMap.addMarker(markerOptions);
                        tmpMarker.setTag(stationId);
                        mVisibleStationMarkers.put(stationId, tmpMarker);
                    }
                }
                // remove the indeterminate progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onLoadFinished mVisibleStationMarkers after clean up:" + mVisibleStationMarkers.size());
                }
            } else {
                Log.e(TAG, "Got wrong data from loader");
            }

        } else if (id == LOADER_DIRECTIONS) {
            if (data instanceof ContentValues) {
                ContentValues contentValuesData = (ContentValues) data;

                String distance = contentValuesData.getAsString(DirectionsLoader.ARG_DISTANCE);
                onDistanceReceived(distance);

                String overview_polyline = contentValuesData.getAsString(DirectionsLoader.ARG_OVERVIEW_POLYLINE);

                List<LatLng> points = PolyUtil.decode(overview_polyline);
                if (mDirectionsPolyLine != null) {
                    mDirectionsPolyLine.remove();
                }
                mDirectionsPolyLine = mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(getResources().getDimension(R.dimen.directions_poly_line_width))
                        .color(getResources().getColor(R.color.accent))
                );

            }
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onLoadReset");
        }
    }

    /**
     * Own interface method from BottomSheetStationFragment.
     * Used the update distance when loader has finished
     *
     * @param distance is a string with the distance from google directions api
     */
    @Override
    public void onDistanceReceived(String distance) {
        BottomSheetStationFragment stationFrag = (BottomSheetStationFragment) getSupportFragmentManager()
                .findFragmentById(mBottomSheetStationFragmentId);
        if (stationFrag != null) {
            stationFrag.updateDistance(distance);
        }
    }

    /**
     * Callback for result of permission request.
     * <p/>
     *
     * @param requestCode  the permission request code
     * @param permissions  list of permissions
     * @param grantResults the result of the grant
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onRequestPermissionsResult");
        }
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // Permission denied, exit the app and show explanation toast.
                    Toast toast = Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    /**
     * Restart the loader for station markers with current LatLngBounds of camera.
     */
    public void refreshMapStationMarkers() {
        // start the indeterminate progressbar
        mProgressBar.setVisibility(View.VISIBLE);
        // restart the loader for markers
        Bundle args = new Bundle();
        args.putParcelable(ARG_MAP_VISIBLE_BOUNDS, mMap.getProjection().getVisibleRegion().latLngBounds);
        getSupportLoaderManager().restartLoader(LOADER_MARKERS, args, this).forceLoad();
    }

    /**
     * Trigger the async task for OCM updates
     * <p/>
     */
    protected void initiateOCMSync(LatLng latLng, double ocm_radius) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "initiateOCMSync");
        }
        // TODO: add some more rate limiting?
        OCMSyncTask OCMSyncTask = new OCMSyncTask(this,
                latLng,
                ocm_radius,
                getResources().getInteger(R.integer.ocm_max_results),
                new OCMSyncTaskListener() {
                    @Override
                    public void onOCMSyncSuccess(Object object) {

                        // Also Add and update markers for stations in the current visible area
                        // every time an ocm sync if finished in case of slow updates
                        refreshMapStationMarkers();
                    }

                    @Override
                    public void onOCMSyncFailure(Exception exception) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_OCM_sync_failure) + exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        );

        OCMSyncTask.execute();


    }


    protected void setupLocationServices() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "setupLocationServices");
        }
        // Handle locations of handset
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (checkLocationPermission())) {

            //setup periodic location requests
            if (mLocationRequest == null) {
                createLocationRequest();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Could not setup location services");
            }
            Snackbar.make(
                    MainMapActivity.this.findViewById(R.id.main_layout),
                    getString(R.string.snackbar_service_not_connected),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            getString(R.string.snackbar_ok_btn),
                            new View.OnClickListener() {
                                /**
                                 * Called when a view has been clicked.
                                 *
                                 * @param v The view that was clicked.
                                 */
                                @Override
                                public void onClick(View v) {
                                    //exit
                                    analyticsLogSelectContent(
                                            getString(R.string.analytics_id_snack_bar),
                                            getString(R.string.analytics_name_snack_bar_no_location_service),
                                            getString(R.string.analytics_content_type_snack_bar)
                                    );
                                    recreate();
                                }
                            })
                    .show();
        }

    }

    /**
     * Set up the location requests
     * <p/>
     */
    protected void createLocationRequest() {
        //if (BuildConfig.DEBUG) {
        //Log.d(TAG, "createLocationRequest");
        //}
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getResources().getInteger(R.integer.preferred_location_interval)); // ideal interval
        mLocationRequest.setFastestInterval(getResources().getInteger(R.integer.fastest_location_interval)); // the fastest interval my app can handle
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // highest accuracy
    }

    protected void updateCurrentLocation(boolean moveCamera) {
        // Handle locations of handset
        if (BuildConfig.DEBUG) {
            //Log.d(TAG, "updateCurrentLocation moveCamera: " + moveCamera);
            //Log.d(TAG, "updateCurrentLocation Location permissions: " + checkLocationPermission());
            //Log.d(TAG, "updateCurrentLocation Access fine location: " + (ContextCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));

            Log.d(TAG, "updateCurrentLocation");

        }
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (checkLocationPermission())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "updateCurrentLocation mGoogleApiClient: " + mGoogleApiClient);
            }
            if (mGoogleApiClient != null) {

                // Set the last location from the LocationServices
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);



                if (location != null) {
                    long locationAge = System.currentTimeMillis() - location.getTime();
                    if (locationAge >= getResources().getInteger(R.integer.max_last_location_age) * 1000) {
                        // Try again to force a location update
                        setupLocationServices();
                        Toast.makeText(this, getText(R.string.toast_old_last_location),
                                Toast.LENGTH_SHORT).show();
                    }
                    mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Move the car markers current position
                    if (mCurrentLocationMarker != null) {
                        mCurrentLocationMarker.setPosition(mLastLocation);
                    } else {
                        mMarkerOptionsCar.position(mLastLocation);
                        mCurrentLocationMarker = mMap.addMarker(mMarkerOptionsCar);
                    }

                    if (moveCamera) {
                        // move the camera
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                mLastLocation,
                                getResources().getInteger(R.integer.zoom_default)
                        );
                        mMap.animateCamera(cameraUpdate);
                    }
                    // save camera center
                    mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

                    // OCM camera center to something at this point
                    // to trigger a sync
                    mLastOCMCameraCenter = new LatLng(0d, 0d);
                } else {
                    Log.e(TAG, "updateCurrentLocation no getLastLocation from LocationServices");
                    Snackbar.make(
                            MainMapActivity.this.findViewById(R.id.main_layout),
                            getString(R.string.snackbar_no_last_location),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(
                                    getString(R.string.snackbar_ok_btn),
                                    new View.OnClickListener() {
                                        /**
                                         * Called when a view has been clicked.
                                         *
                                         * @param v The view that was clicked.
                                         */
                                        @Override
                                        public void onClick(View v) {
                                            setupLocationServices();


                                        }
                                    }).show();

                }
            } else {
                Log.e(TAG, "GoogleApiClient not connected");
            }

        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No permissions");
            }
        }
    }

    /**
     * Log analytics SELECT_CONTENT event
     * <p/>
     */
    private void analyticsLogSelectContent(String id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * Check if the user allows location (fine)
     * <p/>
     *
     * @return True or False
     */
    private boolean checkLocationPermission() {
        //if (BuildConfig.DEBUG) {
        //Log.d(TAG, "checkLocationPermission");
        //}
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(
                        MainMapActivity.this.findViewById(R.id.main_layout),
                        getString(R.string.snackbar_permission_explanation),
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                getString(R.string.snackbar_ok_btn),
                                new View.OnClickListener() {
                                    /**
                                     * Called when a view has been clicked.
                                     *
                                     * @param v The view that was clicked.
                                     */
                                    @Override
                                    public void onClick(View v) {
                                        //Prompt the user once explanation has been shown
                                        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_LOCATION);
                                    }
                                }).show();


            } else {
                // No explanation needed, we can request the permission.
                askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainMapActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainMapActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainMapActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainMapActivity.this, new String[]{permission}, requestCode);
            }
        }
    }

    /**
     * Check if the is online.
     * <p/>
     * From https://stackoverflow.com/a/4009133
     *
     * @return True or False
     */
    public boolean isOnline() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "isOnline");
        }
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }


    /**
     * Check if the user allows Google play services. Prerequisite for this app, bail if denied.
     * <p/>
     *
     * @return True or False
     */
    private boolean checkGooglePlayServices() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "checkGooglePlayServices");
        }
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        1234).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Setup the GoogleApiClient for play services (maps)
     * <p/>
     */
    protected synchronized void buildGoogleApiClient() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "buildGoogleApiClient");
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
            mGoogleApiClient.connect();
        }
    }
}
