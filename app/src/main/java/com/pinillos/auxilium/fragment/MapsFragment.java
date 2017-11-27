package com.pinillos.auxilium.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pinillos.auxilium.AuxiliumApplication;
import com.pinillos.auxilium.BuildConfig;
import com.pinillos.auxilium.model.Incidence;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.model.UserInformation;
import com.pinillos.auxilium.Util.MessageUtil;
import com.pinillos.auxilium.activity.MainActivity;
import com.pinillos.auxilium.model.Position;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MapsFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    // Location API
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    //Firebase auth and db instances
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference firebaseDatabaseReference;
    private FirebaseUser firebaseUser;

    private FloatingActionButton fabShowLayerSelector;
    private com.getbase.floatingactionbutton.FloatingActionButton createIncidenceFab, layerSelectorFab, initTrackingFab;
    private ValueEventListener firebaseDatabaseListener;


    private GoogleMap mMap;
    private String mLastUpdateTime;
    //FBA for place and layer select
    //User info from firebase database
    private UserInformation userInformation;
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean trackingIsEnabled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, null);

        inicializeViews(view);

        //Add support to map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setClickable(true);


        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        configureAndInicializeFireBase();

        return view;
    }

    private void inicializeViews(View view) {

        //Reference floating button
        createIncidenceFab = view.findViewById(R.id.create_incidence_fab);
        createIncidenceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(getActivity());
                    getActivity().startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        layerSelectorFab = view.findViewById(R.id.layer_selector_fab);
        layerSelectorFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = new CharSequence[4];

                items[0] = "Normal";
                items[1] = "Terreno";
                items[2] = "Satelite";
                items[3] = "Híbrido";

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Seleccione un tipo de mapa")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        break;
                                    case 1:
                                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                        break;
                                    case 2:
                                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                        break;
                                    case 3:
                                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                        break;
                                }
                            }
                        });
                builder.show();
            }
        });

        initTrackingFab = view.findViewById(R.id.init_tracking_fab);
        initTrackingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackingIsEnabled = !trackingIsEnabled;
                if (trackingIsEnabled)
                {
                    MessageUtil.toastMessage(getContext(), "Tracking iniciado");

                    Drawable d = getResources().getDrawable( R.drawable.ic_switch_off_tracking );
                    initTrackingFab.setIconDrawable(d);
                    if (!mRequestingLocationUpdates)
                    {
                        mRequestingLocationUpdates = true;
                        //setButtonsEnabledState();
                        startLocationUpdates();
                    }
                }
                else
                {
                    MessageUtil.toastMessage(getContext(),"Tracking parado");
                    Drawable d = getResources().getDrawable( R.drawable.ic_switch_on_tracking );
                    initTrackingFab.setIconDrawable(d);
                    stopLocationUpdates();
                }
            }
        });
    }

    private void configureAndInicializeFireBase() {
        //instace firebase auth and database
        firebaseAuth = MainActivity.firebaseAuth;
        firebaseDatabase = MainActivity.firebaseDatabase;
        firebaseDatabaseReference = MainActivity.firebaseDatabaseReference;
        firebaseUser = MainActivity.firebaseUser;

        //User login
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        //Add firebase event listener
        firebaseDatabaseListener = firebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMap.clear();

                        Position position = null;
                        //retrieve data from database
                        for (DataSnapshot dbObjects : dataSnapshot.getChildren()) {
                            String objectType = dbObjects.getKey();
                            //toastMessage(objectType);

                            for (DataSnapshot currentUser : dbObjects.getChildren()) {
                                final String userID = currentUser.getKey();
                                //toastMessage(userID);

                                for (DataSnapshot currentItem : currentUser.getChildren()) {

                                    String itemType = currentItem.getKey();
                                    //toastMessage(itemType);


                                    for (DataSnapshot currentField : currentItem.getChildren()) {

                                        if (itemType.equals("information")) {
                                            userInformation = currentItem.getValue(UserInformation.class);
                                            //toastMessage(userInformation.getFirstName());
                                        }

                                        if (itemType.equals("position")) {

                                            //retrieve position object from database
                                            String positionID = currentField.getKey();

                                            if (positionID.equals("last_position"))
                                            {

                                                Position lastPosition = currentField.getValue(Position.class);

                                                if (lastPosition != null) {
                                                    //toastMessage(position.getCreatorID());

                                                    int userIcon;
                                                    switch (userInformation.getCategory()) {
                                                        case "Bombero":
                                                            userIcon = R.mipmap.ic_fireman_round;
                                                            break;
                                                        case "Medico":
                                                            userIcon = R.mipmap.ic_medical_round;
                                                            break;
                                                        default:
                                                            userIcon = R.mipmap.ic_fireman_round;
                                                    }

                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(lastPosition.getLat(), lastPosition.getLon()))
                                                            .title(userInformation.getFirstName() + " " + userInformation.getLastName1())
                                                            .snippet(lastPosition.getTimestamp())
                                                            .icon(BitmapDescriptorFactory.fromResource(userIcon))
                                                            .draggable(true));
                                                }
                                            }
                                        }



                                        //if retrieved database object are incident type
                                        if (itemType.equals("incidence")) {
                                            //retrieve incidence object from database
                                            String incidenceID = currentField.getKey();
                                            Incidence incidence = currentField.getValue(Incidence.class);

                                            //according to incidence type, shows different icon
                                            int incidenceIcon;
                                            switch (incidence.getType()) {
                                                case "Incendio":
                                                    incidenceIcon = R.mipmap.ic_incendio_round;
                                                    break;
                                                case "Corte luz":
                                                    incidenceIcon = R.mipmap.ic_electrico_round;
                                                    break;
                                                default:
                                                    incidenceIcon = R.mipmap.ic_incendio_round;
                                                    break;
                                            }

                                            //if the incidence is activated, add google maps marker
                                            if (incidence.getStatus()) {
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(incidence.getLat(), incidence.getLon()))
                                                        .title(incidence.getType())
                                                        .snippet("Creada por " + incidence.getCreatorID())
                                                        .icon(BitmapDescriptorFactory.fromResource(incidenceIcon))
                                                        .draggable(true));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Failed to read value
                        MessageUtil.toastMessage(getContext(),"Failed to alter database.");
                        Log.w(TAG, "Failed to read value.", databaseError.toException());
                    }
                });
    }


    @Override
    public void onPause() {
        super.onPause();
        //firebaseDatabaseReference.removeValue();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
        if (firebaseDatabaseListener != null) {
            firebaseDatabaseReference.removeEventListener(firebaseDatabaseListener);
        }
        mGoogleApiClient.disconnect();


    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                writeUserPositionToFirebaseDB(firebaseUser.getUid(), mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mLastUpdateTime);

                updateLocationUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void writeUserPositionToFirebaseDB(String creatorID, double lat, double lon, String currentTimeStamp) {
        firebaseDatabaseReference = firebaseDatabase.getInstance().getReference();
        String positionID = firebaseDatabaseReference.push().getKey();

        Position myPosition = new Position(firebaseUser.getEmail(), mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), getCurrentTimeStamp());
        firebaseDatabaseReference.child("users").child(firebaseUser.getUid()).child("position").child(positionID).setValue(myPosition);
        firebaseDatabaseReference.child("users").child(firebaseUser.getUid()).child("position").child("last_position").setValue(myPosition);
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        updateLocationUI();
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        //setButtonsEnabledState();
                    }
                });
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            //MessageUtil.toastMessage(getContext(),"Lat: "+mCurrentLocation.getLatitude()+ " Lon: "+mCurrentLocation.getLatitude()+" "+" hora: "+mLastUpdateTime);
        }
    }

    /**
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


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setOnInfoWindowClickListener(getActivity());
        mMap.setMyLocationEnabled(true);


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                MessageUtil.toastMessage(getActivity(), marker.getPosition().latitude + " " + marker.getPosition().longitude);
            }
        });
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {

//        return  ActivityCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED;


        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            MessageUtil.showSnackbar(this.getView(), getActivity(), R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                MessageUtil.showSnackbar(getView(), getActivity(), R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }


        @Override
        public void onConnected (@Nullable Bundle bundle){


            if (mCurrentLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15.0f));
            } else {
                MessageUtil.toastMessage(getActivity(), "Ubicación no encontrada");
            }
        }

        @Override
        public void onConnectionSuspended ( int i){

        }


        @Override
        public void onInfoWindowClick (Marker marker){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Incidence creada por:" +
                    "Titulo: " + marker.getTitle() + "\n" +
                    "Descripcion: " + marker.getSnippet() + "\n" +
                    "Numero: " + marker.getId() + "\n" +
                    "Latitud: " + marker.getPosition().latitude + "\n" +
                    "Longitud: " + marker.getPosition().longitude + "\n")
                    .setPositiveButton("Terminar incidencia", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MessageUtil.toastMessage(getActivity(), "navegacion");
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MessageUtil.toastMessage(getActivity(), "cancelado");
                        }
                    });
            // Create the AlertDialog object and return it
            builder.show();
        }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mCurrentLocation = task.getResult();

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15.0f));

                            } else {
                                Log.w(TAG, "getLastLocation:exception", task.getException());
                            }
                        }
                    });
        }

}


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
