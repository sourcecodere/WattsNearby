package re.sourcecode.android.wattsnearby;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import android.os.Bundle;

import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;

// Location
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

// Google Places
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;


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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.maps.android.PolyUtil;

import java.util.HashMap;
import java.util.Arrays;
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
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, // To clean up polyline from directions
        GoogleMap.OnInfoWindowClickListener,
        BottomSheetStationFragment.OnDirectionsReceivedListener,  //to send distance data from map to bottom sheet
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks {

    private static final String TAG = MainMapActivity.class.getSimpleName();

    private static final int PERM_REQUEST_FINE_LOC = 0; // For controlling necessary Permissions.
    private static final int INTENT_PLACE = 1; // Intent id for places search

    private static final int LOADER_MARKERS = 3; // ID for loading markers in async thread
    private static final int LOADER_DIRECTIONS = 4; // ID for loading directions in async thread

    private GoogleMap mMap; // The map object.

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LatLng mLastLocation;           // Last known position on the phone/car.
    private LatLng mLastCameraCenter;       // Latitude and longitude of last camera center.
    private LatLng mLastOCMCameraCenter;    // Latitude and longitude of last camera center where 
    // the OCM api was synced against the content provider.

    private boolean mCamFollowsCar = false; // Enable automatic camera movement as the car moves.
    // Enabled when my location is clicked.
    // Disabled when other parts of the map is clicked.

    // Keys for storing the positions above
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_OCM_CAMERA_POSITION = "ocm_sync_camera_position";

    private boolean mLocationPermissionGranted; // For checking runtime permission for fine location

    private MarkerOptions mMarkerOptionsCar;    // Icon for the car.
    private Marker mCurrentLocationMarker;      // Car marker with position

    private Polyline mDirectionsPolyLine;       // Directions poly line from car to selected marker

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Marker> mVisibleStationMarkers = new HashMap<>(); // hashMap 
    // <stationId, Marker>
    // of station markers
    // in the current map

    private long mStationIdFromIntent;         // For intent

    private ProgressBar mProgressBar; // For loading markers
    private ProgressBar mProgressBarSpinner; // For waiting for location

    private FirebaseAnalytics mFirebaseAnalytics; // Setup analytics

    private static final String mBottomSheetStationFragmentTag = "BottomSheetStation"; // To communicate with fragment interface

    public static final String ARG_DETAIL_SHEET_STATION_ID = "station_id"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_DETAIL_SHEET_ABOUT = "about"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_WIDGET_INTENT_KEY = "station_id";
    public static final String ARG_MAP_VISIBLE_BOUNDS = "visible_bounds";
    public static final String ARG_DIRECTIONS_DEST = "destination"; // used in directions loader
    public static final String ARG_DIRECTIONS_ORIGIN = "origin"; // used in directions loader

    // shared preferences
    public static final String FILTER_CHANGED_KEY = "filters_changed"; // used in main to check for changes and in settings fragment to set changes

    /**
     * AppCompatActivity override
     * <p/>
     * First call in the lifecycle. This is followed by onStart().
     *
     * @param savedInstanceState contains the activity previous frozen state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "Build config " + BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);

        // google cloud api key for Directions API, Maps SDK for Android, Places SDK for Android
        String apiKey = BuildConfig.GOOGLE_CLOUD_API_KEY;

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main_map);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mLastCameraCenter = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            mLastOCMCameraCenter = savedInstanceState.getParcelable(KEY_OCM_CAMERA_POSITION);
        }
        //else {
        //    getLastLocation(false); // try to get last location early
        //}

        MapsInitializer.initialize(this);

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


        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Setup the banner ad
        MobileAds.initialize(getApplicationContext(),
                getString(R.string.banner_app_id));
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("2B3C903E8A681D2047F678BBCD58B109")
                .build();
        adView.loadAd(adRequest);

        // Intent with stationId (e.g. from widget list item click)
        if (getIntent().hasExtra(ARG_WIDGET_INTENT_KEY)) {
            mStationIdFromIntent = getIntent().getLongExtra(ARG_WIDGET_INTENT_KEY, 0L);
        }

        // set the default value of filter changed flag to false
        // and the units changed key to the default
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(FILTER_CHANGED_KEY, false)
                .apply();

        // Progressbar when loading markers to map
        mProgressBar = findViewById(R.id.progress_bar);
        //mProgressBar.setIndeterminate(true);
        //mProgressBar.setVisibility(View.VISIBLE);

        // Progressbar spinner waiting for position
        mProgressBarSpinner = findViewById(R.id.progress_bar_spinner);
        //mProgressBarSpinner.setIndeterminate(true);
        if (mLastLocation == null) {
            mProgressBarSpinner.setVisibility(View.VISIBLE);
        }
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
        if (mMap != null) {
            savedInstanceState.putParcelable(KEY_CAMERA_POSITION, mLastCameraCenter);
            savedInstanceState.putParcelable(KEY_LOCATION, mLastLocation);
            savedInstanceState.putParcelable(KEY_OCM_CAMERA_POSITION, mLastOCMCameraCenter);
            super.onSaveInstanceState(savedInstanceState);
        }

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
            // If search for place is tapped.
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG
            );

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            startActivityForResult(intent, INTENT_PLACE);
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
            boolean success = googleMap
                    .setMapStyle(
                            MapStyleOptions
                                    .loadRawResourceStyle(this, R.raw.style_json)
                    );

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
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


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getLocationPermission();
        }

        // Start the periodic updates for locations TODO: Check if this should be done in onCreate()
        startPeriodicLocationUpdates();

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


        // Fab for the my location. With onClickListener
        FloatingActionButton fab = findViewById(R.id.fab_my_location);
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

                mCamFollowsCar = true;
                if (mLocationPermissionGranted) {
                    // Try to get the last location
                    getLastLocation(mCamFollowsCar);
                }
            }
        });


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

            bottomSheetDialogFragment.show(getSupportFragmentManager(), mBottomSheetStationFragmentTag);
        }

        // Moves the bottom map elements up a bit to fit the adView.
        // mMap.setPadding(0, 0, 0, AdSize.SMART_BANNER.getHeightInPixels(this));

        // lastly try to move the camera to current position
        updateCurrentLocation(true);

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
        mCamFollowsCar = false;

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

            bottomSheetDialogFragment.show(getSupportFragmentManager(), mBottomSheetStationFragmentTag);
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
        mCamFollowsCar = false;
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCameraMove");
        }
        // Get the current visible region of the map, and save the center LatLng
        mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
        mCamFollowsCar = false;
    }

    /**
     * GoogleMap.onCameraIdle
     * <p/>
     * Callback. Checks if the new idle position of the map camera should initiate a OCM sync
     */
    @Override
    public void onCameraIdle() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCameraIdle");
        }

        Resources resources = getResources();

        int currentZoom = Math.round(mMap.getCameraPosition().zoom);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "currentZoom " + currentZoom + " triggerlevel " + getResources().getInteger(R.integer.min_zoom_level));
        }

        // First check that the zoom level is high enough
        // to make it reasonable to trigger a sync at all
        if (currentZoom > getResources().getInteger(R.integer.min_zoom_level)) {

            // The computed distance is stored in results[0].
            // If results has length 2 or greater, the initial bearing is stored in results[1].
            // If results has length 3 or greater, the final bearing is stored in results[2].
            float[] results = new float[3];
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "mLastCameraCenter " + mLastCameraCenter + " mLastOCMCameraCenter " + mLastOCMCameraCenter);
            }
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
            } else if ((mLastOCMCameraCenter == null) && (mLastCameraCenter != null)) {
                // This happens on the first try, so just initiate the OCM sync
                mLastOCMCameraCenter = mLastCameraCenter;

                initiateOCMSync(
                        mLastCameraCenter,
                        (double) getResources().getInteger(R.integer.ocm_radius_km_far)
                );
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
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng placeLatLng = place.getLatLng();
                Log.i(TAG, "Place: " + place.getName() + ", " + placeLatLng + ", " + place.getId());

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

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
                Toast.makeText(this, getText(R.string.toast_place_selection_failed) + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();

            }

            super.onActivityResult(requestCode, resultCode, data);
        }
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
                @SuppressWarnings("unchecked")
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
                // remove the indeterminate progressbar showing ocm sync in progress
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
        BottomSheetStationFragment stationFrag = (BottomSheetStationFragment) getSupportFragmentManager().findFragmentByTag(mBottomSheetStationFragmentTag);
        if (stationFrag != null) {
            stationFrag.updateDistance(distance);
        }
    }

    /**
     * Restart the loader for station markers with current LatLngBounds of camera.
     */
    public void refreshMapStationMarkers() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "refreshMapStationMarkers");
        }
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
        // show that the sync is in progress to the user
        mProgressBar.setVisibility(View.VISIBLE);
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


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getLastLocation(final boolean moveCamera) {
        /*
         * Get the best and most recent location of the device, which may be null in
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location location = task.getResult();
                            // Stop the spinner progress bar
                            //mProgressBarSpinner.setIndeterminate(false);
                            mProgressBarSpinner.setVisibility(View.GONE);
                            if (location != null) {
                                mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                // move the camera to the new position
                                updateCurrentLocation(moveCamera);
                            } else {
                                // Start the spinner progress bar waiting for position
                                mProgressBarSpinner.setVisibility(View.VISIBLE);
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
                                                        getLastLocation(false);
                                                    }
                                                }).show();
                            }
                        }
                    }
                });
            }
        } catch (
                SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    protected void startPeriodicLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(getResources().getInteger(R.integer.preferred_location_interval));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.fastest_location_interval));

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                // do work here
                                Location location = locationResult.getLastLocation();
                                mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                // Try to update the current location and move the camera. If location is available.
                                updateCurrentLocation(mCamFollowsCar);
                            }
                        },
                        Looper.myLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    protected void updateCurrentLocation(boolean moveCamera) {
        // Handle locations of handset
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "updateCurrentLocation");
        }
        if (mLocationPermissionGranted) {

            if (mLastLocation == null) {
                // Try to get the last location
                getLastLocation(false);
            } else {

                if (mCurrentLocationMarker == null) {
                    mMarkerOptionsCar.position(mLastLocation);
                    mCurrentLocationMarker = mMap.addMarker(mMarkerOptionsCar);
                }

                // Move the car markers current position if it has changed
                if (mCurrentLocationMarker.getPosition() != mLastLocation) {
                    // New position!
                    mCurrentLocationMarker.setPosition(mLastLocation);
                }

                if (moveCamera) {
                    // move the camera
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            mLastLocation,
                            getResources().getInteger(R.integer.zoom_default)
                    );
                    mMap.animateCamera(cameraUpdate);
                }

                LatLng newMapCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

                if ((mLastCameraCenter != null) && (mLastCameraCenter != newMapCenter)) {
                    // save camera center
                    mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

                    // OCM camera center to something at this point
                    // to trigger a sync
                    mLastOCMCameraCenter = new LatLng(0d, 0d);
                }
            }
        }
    }

    /**
     * Check if the user allows location (fine)
     * <p/>
     */
    private void getLocationPermission() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getLocationPermission");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
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
                                        ActivityCompat.requestPermissions(
                                                MainMapActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERM_REQUEST_FINE_LOC);
                                    }
                                }).show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        MainMapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERM_REQUEST_FINE_LOC);
            }
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERM_REQUEST_FINE_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLastLocation(true);
                    updateCurrentLocation(true); // TODO fix, this forces OCM for some reason
                } else {
                    // Permission denied, exit the app and show explanation toast.
                    Toast toast = Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    getLocationPermission();
                }
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
}
