package re.sourcecode.android.wattsnearby;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.Snackbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import re.sourcecode.android.wattsnearby.sync.WattsOCMSyncTask;
import re.sourcecode.android.wattsnearby.sync.WattsOCMSyncTaskListener;
import re.sourcecode.android.wattsnearby.utilities.WattsImageUtils;
import re.sourcecode.android.wattsnearby.utilities.WattsMapUtils;


public class MainMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationButtonClickListener{

    private static final String TAG = MainMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation; // last known position on the phone

    LatLng mLastCameraCenter; // lat and lon of last camera center
    LatLng mLastOCMCameraCenter; // lat and lon of last camera center where the OCM api was synced against the content provider

    Marker mCurrentLocationMarker; // car position
    BitmapDescriptor mCurrentLocationMarkerIcon; // icon for the car
    LocationRequest mLocationRequest; // periodic location request object

    HashMap<Long, Marker> mVisibleStationMarkers = new HashMap<>(); // hashMap of station markers in the current map

    public static final int PERMISSIONS_REQUEST_LOCATION = 0;

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        buildGoogleApiClient();
        super.onStart();

    }

    /**
     * Dispatch onStop() to all fragments.  Ensure all loaders are stopped.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();

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

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
        // Setup callback for my location button (zoom to my location)
        mMap.setOnMyLocationButtonClickListener(this);
        // Setup callback for camera movement (onCameraMove).
        mMap.setOnCameraMoveListener(this);
        // Setup callback for when camera has stopped moving (onCameraIdle).
        mMap.setOnCameraIdleListener(this);

    }

    /**
     * GoogleApiClient.ConnectionCallbacks
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Create location requests and setup location services.
        setupLocationServices();

        // Try to set last location, create car marker, and zoom to location
        centerOnCurrentLocation();

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
        MarkerOptions markerOptions = WattsMapUtils.getCarMarkerOptions(
                latLng,
                getString(R.string.marker_current),
                mCurrentLocationMarkerIcon
        );
        mCurrentLocationMarker = mMap.addMarker(markerOptions);


    }

    /**
     * GoogleMap.onMyLocationButtonClick callback
     *
     * @return true or false
     */
    @Override
    public boolean onMyLocationButtonClick() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkLocationPermission())) {
            // Try to set last location, create car marker, and zoom to location
            centerOnCurrentLocation();
            
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
     * GoogleMap.onCameraIdle callback. Checks if the new idle position of the map camera should initiate a OCM sync
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

            //TODO: delete markers outside of current area...

            // Add and update markers for stations in the current visible area
            WattsMapUtils.updateStationMarkers(this, mMap, mVisibleStationMarkers);
        }
    }

    /**
     * Trigger the async task for OCM updates
     */
    protected synchronized void executeOCMSync(Double latitude, Double longitude) {
        // TODO: add some more rate limiting?
        WattsOCMSyncTask wattsOCMSyncTask = new WattsOCMSyncTask(this,
                latitude,
                longitude,
                (double) getResources().getInteger(R.integer.ocm_radius_km),
                new WattsOCMSyncTaskListener() {
                    @Override
                    public void onOCMSyncSuccess(Object object) {

                        // Also Add and update markers for stations in the current visible area
                        // every time an ocm sync if finished in case of slow updates
                        WattsMapUtils.updateStationMarkers(MainMapActivity.this, mMap, mVisibleStationMarkers);
                    }

                    @Override
                    public void onOCMSyncFailure(Exception exception) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_OCM_sync_failure) + exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        );

        wattsOCMSyncTask.execute();
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
    protected void centerOnCurrentLocation() {
        // Handle locations of handset
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkLocationPermission())) {

            if (mGoogleApiClient != null) {
                // Get the last location, and center the map
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    // Create the car location marker bitmap
                    mCurrentLocationMarkerIcon = WattsImageUtils.vectorToBitmap(this, R.drawable.ic_car_color_sharp, ContextCompat.getColor(this, R.color.colorPrimary), getResources().getInteger(R.integer.car_icon_add_to_size));


                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = WattsMapUtils.getCarMarkerOptions(
                            latLng,
                            getString(R.string.marker_current),
                            mCurrentLocationMarkerIcon
                    );
                    // Remove the old car marker
                    if (mCurrentLocationMarker != null) {
                        mCurrentLocationMarker.remove();
                    }
                    mCurrentLocationMarker = mMap.addMarker(markerOptions);

                    // move the camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.zoom_default)));

                    // save camera center
                    mLastCameraCenter = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

                    // OCM camera center is the same as last camera center at this point.
                    mLastOCMCameraCenter = mLastCameraCenter;
                }
            } else {
                Log.d(TAG, "GoogleApiClient not connected");
            }
            //TODO: sync the first stations to content provider


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
     * Check if the user allows Google play services. Prerequisite for this app, bail if denied.
     *
     * @return True or False
     */
    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Setup the GoogleApiClient for play services (maps)
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

}
