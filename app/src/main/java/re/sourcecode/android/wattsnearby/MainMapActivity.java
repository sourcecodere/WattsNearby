package re.sourcecode.android.wattsnearby;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.Snackbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;

import re.sourcecode.android.wattsnearby.fragment.BottomSheetGenericFragment;
import re.sourcecode.android.wattsnearby.fragment.BottomSheetStationFragment;
import re.sourcecode.android.wattsnearby.sync.OCMSyncTask;
import re.sourcecode.android.wattsnearby.sync.OCMSyncTaskListener;
import re.sourcecode.android.wattsnearby.utilities.WattsDataUtils;
import re.sourcecode.android.wattsnearby.utilities.WattsImageUtils;
import re.sourcecode.android.wattsnearby.utilities.WattsMapUtils;


public class MainMapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        PlaceSelectionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainMapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_LOCATION = 0;  // For controlling necessary Permissions.

    private static final int INTENT_PLACE = 1; // For places search

    public static final String ARG_DETAIL_SHEET_STATION_ID = "stationid"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_DETAIL_SHEET_ABOUT = "about"; // Key for argument passed to the bottom sheet fragment
    public static final String ARG_WIDGET_INTENT_KEY = "stationid";

    private GoogleApiClient mGoogleApiClient; // The google services connection.
    private LocationRequest mLocationRequest; // Periodic location request object.

    private GoogleMap mMap; // The map object.

    private Location mLastLocation; // Last known position on the phone/car.

    private LatLng mLastCameraCenter; // Latitude and longitude of last camera center.
    private LatLng mLastOCMCameraCenter; // Latitude and longitude of last camera center where the OCM api was synced against the content provider.

    private BitmapDescriptor mMarkerIconCar; // Icon for the car.
    private BitmapDescriptor mMarkerIconStation; // Icon for charging stations.
    private BitmapDescriptor mMarkerIconStationFast; // Icon for fast charging stations.

    private Marker mCurrentLocationMarker; // car marker with position
    private HashMap<Long, Marker> mVisibleStationMarkers = new HashMap<>(); // hashMap <stationId, Marker> of station markers in the current map

    private Long mStationIdFromIntent; // for intent

    private SharedPreferences mSharedPrefs; // for onSharedPreferenceChangeListener

    /**
     * First call in the lifecycle. This is followed by onStart().
     *
     * @param savedInstanceState contains the activity previous frozen state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        //Check if Google Play Services Available or not
        if (!checkGooglePlayServices()) {
            Log.d("onCreate", "Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }
        if (!isOnline()) {
            Snackbar.make(
                    MainMapActivity.this.findViewById(R.id.main_layout),
                    getString(R.string.error_not_online),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                    getString(R.string.permission_explanation_snackbar_button),
                    new View.OnClickListener() {
                        /**
                         * Called when a view has been clicked.
                         *
                         * @param v The view that was clicked.
                         */
                        @Override
                        public void onClick(View v) {
                            //exit
                            finish();
                        }
                    })
                    .show();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // Fab for the my location. With onClickListener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_my_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "My location fab clicked!");
                if ((ContextCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkLocationPermission())) {
                    // Try to set last location, update car marker, and zoom to location
                    updateCurrentLocation(true);
                }

            }
        });


        // Create the car location marker bitmap
        mMarkerIconCar = WattsImageUtils.vectorToBitmap(this, R.drawable.ic_car_color_sharp, getResources().getInteger(R.integer.car_icon_add_to_size));
        // Create the charging station marker bitmap
        mMarkerIconStation = WattsImageUtils.vectorToBitmap(this, R.drawable.ic_station, getResources().getInteger(R.integer.station_icon_add_to_size));
        // Create the charging station marker bitmap
        mMarkerIconStationFast = WattsImageUtils.vectorToBitmap(this, R.drawable.ic_station_fast, getResources().getInteger(R.integer.station_icon_add_to_size));

        // Intent with stationId (e.g. from widget list item click)
        if (getIntent().hasExtra(ARG_WIDGET_INTENT_KEY)) {
            mStationIdFromIntent = getIntent().getLongExtra(ARG_WIDGET_INTENT_KEY, 0l);
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        buildGoogleApiClient(); // Get connection to google services.
        super.onStart();
    }

    /**
     * Dispatch onStop() to all fragments.  Ensure all loaders are stopped.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        // super
        super.onStop();
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            // Register listener for prefernce changes
            mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
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
     * Dispatch incoming result to the correct fragment. startActivityForResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
     * OnMapReadyCallback
     * <p>
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

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Disable my location button, using own fab for this
                mMap.setMyLocationEnabled(false);
                // Disable Map Toolbar
                mMap.getUiSettings().setMapToolbarEnabled(false);
                // Enable zoom control
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        } else {
            // Disable my location button, using own fab for this
            mMap.setMyLocationEnabled(false);
            // Disable Map Toolbar
            mMap.getUiSettings().setMapToolbarEnabled(false);
            // Enable zoom control
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
        // Setup callback for camera movement (onCameraMove).
        mMap.setOnCameraMoveListener(this);
        // Setup callback for when camera has stopped moving (onCameraIdle).
        mMap.setOnCameraIdleListener(this);
        // Setup callback for when user clicks on marker
        mMap.setOnMarkerClickListener(this);

        // Intent handling.
        if (mStationIdFromIntent != null) {
            // Todo zoom to position

            // Move the camera to station position
            LatLng stationLatLng = WattsDataUtils.getStationLatLng(getApplicationContext(), mStationIdFromIntent);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(stationLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.zoom_station_select)));

            // Open bottom sheet for station
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetStationFragment();

            Bundle args = new Bundle();
            args.putLong(ARG_DETAIL_SHEET_STATION_ID, mStationIdFromIntent);
            bottomSheetDialogFragment.setArguments(args);

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }

    /**
     * GoogleApiClient.ConnectionCallbacks
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Create location requests and setup location services.
        setupLocationServices();

        // Try to set last location, update car marker, and do not zoom to location
        updateCurrentLocation(false);
    }

    /**
     * GoogleApiClient.ConnectionCallbacks
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * GoogleApiClient.OnConnectionFailedListener
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * LocationListener
     */
    @Override
    public void onLocationChanged(Location location) {


        // Update last location the the new location
        mLastLocation = location;

        // Remove the old car marker
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        // Place current location car marker.
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = WattsImageUtils.getCarMarkerOptions(
                latLng,
                getString(R.string.marker_current),
                mMarkerIconCar
        );
        mCurrentLocationMarker = mMap.addMarker(markerOptions);


    }

    /**
     * GoogleMap.onMarkerClick called when marker is clicked
     *
     * @param marker clicked
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        Long stationId = (Long) marker.getTag();

        if (stationId != null) { //every station marker should have data (stationId), only the car does not
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetStationFragment();

            Bundle args = new Bundle();
            args.putLong(ARG_DETAIL_SHEET_STATION_ID, stationId);
            bottomSheetDialogFragment.setArguments(args);

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        } else {
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetGenericFragment();

            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }

        return false;
    }

    /**
     * GoogleMap.onCameraMove callback. Updates the center of the map
     */
    @Override
    public void onCameraMove() {

        // Get the current visible region of the map
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        // Get the center of current map view
        mLastCameraCenter = visibleRegion.latLngBounds.getCenter();

    }

    /**
     * GoogleMap.onCameraIdle
     * <p>
     * Callback. Checks if the new idle position of the map camera should initiate a OCM sync
     */
    @Override
    public void onCameraIdle() {

        // Init sync from OCM
        float[] results = new float[3];
        if ((mLastCameraCenter != null) && (mLastOCMCameraCenter != null)) {
            Location.distanceBetween(
                    mLastCameraCenter.latitude,
                    mLastCameraCenter.longitude,
                    mLastOCMCameraCenter.latitude,
                    mLastOCMCameraCenter.longitude,
                    results);

            float ocmCameraDelta = results[0]; //
            Log.d(TAG, "onCameraIdle camera delta: " + results[0] + ", " + results[1] + ", " + results[2]);
            // update content provider if significant movement
            if (ocmCameraDelta > getResources().getInteger(R.integer.delta_trigger_camera_significantly_changed)) {

                mLastOCMCameraCenter = mLastCameraCenter;

                executeOCMSync(mLastCameraCenter.latitude, mLastCameraCenter.longitude);

            }

            // Add and update markers for stations in the current visible area
            WattsMapUtils.updateStationMarkers(this, mMap, mVisibleStationMarkers, mMarkerIconStation, mMarkerIconStationFast);
        }
    }

    /**
     * Places API
     * <p>
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.d(TAG, "Place Selected: " + place.getName());
        LatLng placeLatLng = place.getLatLng();

        // move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.zoom_places_search)));
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
        //Log.d(TAG, "onSharedPreferenceChanged");
        for (Marker marker: mVisibleStationMarkers.values()) {
            marker.remove();
        }
        mVisibleStationMarkers = new HashMap<Long, Marker>();
        WattsMapUtils.updateStationMarkers(MainMapActivity.this, mMap, mVisibleStationMarkers, mMarkerIconStation, mMarkerIconStationFast);
    }

    /**
     * Places API
     * <p>
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }


    /**
     * Trigger the async task for OCM updates
     */
    protected synchronized void executeOCMSync(Double latitude, Double longitude) {
        // TODO: add some more rate limiting?
        OCMSyncTask OCMSyncTask = new OCMSyncTask(this,
                latitude,
                longitude,
                (double) getResources().getInteger(R.integer.ocm_radius_km),
                getResources().getInteger(R.integer.ocm_max_results),
                new OCMSyncTaskListener() {
                    @Override
                    public void onOCMSyncSuccess(Object object) {

                        // Also Add and update markers for stations in the current visible area
                        // every time an ocm sync if finished in case of slow updates
                        WattsMapUtils.updateStationMarkers(MainMapActivity.this, mMap, mVisibleStationMarkers, mMarkerIconStation, mMarkerIconStationFast);
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
     * Set up the location requests
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getResources().getInteger(R.integer.preferred_location_interval)); // ideal interval
        mLocationRequest.setFastestInterval(getResources().getInteger(R.integer.fastest_location_interval)); // the fastest interval my app can handle
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // highest accuracy
    }

    protected void setupLocationServices() {
        // Handle locations of handset
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkLocationPermission())) {

            //setup periodic location requests
            if (mLocationRequest == null) {
                createLocationRequest();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            Log.d(TAG, "Could not setup location services");
        }
    }

    protected void updateCurrentLocation(Boolean moveCamera) {
        // Handle locations of handset
        Log.d(TAG, "updateCurrentLocation called");
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkLocationPermission())) {

            if (mGoogleApiClient != null) {
                // Get the last location, and center the map
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {

                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = WattsImageUtils.getCarMarkerOptions(
                            latLng,
                            getString(R.string.marker_current),
                            mMarkerIconCar
                    );
                    // Remove the old car marker
                    if (mCurrentLocationMarker != null) {
                        mCurrentLocationMarker.remove();
                    }
                    mCurrentLocationMarker = mMap.addMarker(markerOptions);

                    if (moveCamera) {
                        // move the camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.zoom_default)));
                    }
                    // save camera center
                    mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

                    // OCM camera center is the same as last camera center at this point.
                    mLastOCMCameraCenter = mLastCameraCenter;
                }
            } else {
                Log.d(TAG, "GoogleApiClient not connected");
            }

        } else {
            Log.d(TAG, "No permissions");
        }
    }

    /**
     * Check if the user allows location (fine)
     *
     * @return True or False
     */
    public boolean checkLocationPermission() {
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
                        getString(R.string.permission_explanation_snackbar),
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                getString(R.string.permission_explanation_snackbar_button),
                                new View.OnClickListener() {
                                    /**
                                     * Called when a view has been clicked.
                                     *
                                     * @param v The view that was clicked.
                                     */
                                    @Override
                                    public void onClick(View v) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(MainMapActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSIONS_REQUEST_LOCATION);
                                    }
                                }).show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Callback for result of permission request.
     *
     * @param requestCode
     * @param permissions  list of permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
                    Toast.makeText(this, getString(R.string.permission_denied_toast), Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    /**
     * Check if the is online.
     *
     * From https://stackoverflow.com/a/4009133
     *
     * @return True or False
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * Check if the user allows Google play services. Prerequisite for this app, bail if denied.
     *
     * @return True or False
     */
    private boolean checkGooglePlayServices() {
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
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient");
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
