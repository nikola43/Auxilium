package com.pinillos.auxilium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        LocationListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private boolean trackingIsEnabled;
    private com.google.android.gms.location.LocationListener locationListener;


    // Location API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;
    private int PLACE_PICKER_REQUEST = 1;


    FloatingActionButton fabPickPlace;
    FloatingActionButton fabLayerSelect;

    private GoogleMap mMap;
    int status;

    double longitudeGPS, latitudeGPS;
    UserInformation userInformation;


    private static final String LOCATION_KEY = "location-key";
    private static final String ACTIVITY_KEY = "activity-key";


    // Activity Recognition API
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    @DrawableRes
    private int mImageResource = R.drawable.ic_pool;

    // Códigos de petición
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_CHECK_SETTINGS = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        // Establecer punto de entrada para la API de ubicación
        //buildGoogleApiClient();

        // Crear configuración de peticiones
        createLocationRequest();

        // Crear opciones de peticiones
        buildLocationSettingsRequest();

        // Verificar ajustes de ubicación actuales
        checkLocationSettings();

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        updateValuesFromBundle(savedInstanceState);




        //Reference toolbar UI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Reference floating button
        fabPickPlace = findViewById(R.id.fab);

        //create incidence
        fabPickPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        fabLayerSelect = findViewById(R.id.layerSelector);

        fabLayerSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showLayerSelectionDialog();
            }
        });


        //Drawer menu reference and initialization
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation view reference and initialization
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //instace firebase auth and database
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        //User login
        mAuthListener = new FirebaseAuth.AuthStateListener() {
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
        myRef.addValueEventListener(new ValueEventListener() {
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



                                if (itemType.equals("position")) {

                                    //retrieve position object from database
                                    String positionID = currentField.getKey();
                                    position = currentField.getValue(Position.class);



                                    if (position != null)
                                        {
                                            //toastMessage(position.getCreatorID());

                                            int userIcon;
                                            switch (userInformation.getCategory())
                                            {
                                                case "Bombero": userIcon = R.mipmap.ic_fireman_round; break;
                                                case "Medico": userIcon = R.mipmap.ic_medical_round; break;
                                                default: userIcon = R.mipmap.ic_fireman_round;
                                            }

                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(position.getLat(), position.getLon()))
                                                    .title(userInformation.getFirstName()+" "+userInformation.getLastName1())
                                                    .snippet(position.getTimestamp())
                                                    .icon(BitmapDescriptorFactory.fromResource(userIcon))
                                                    .draggable(true));
                                        }


                                }

                                if (itemType.equals("information")) {
                                    userInformation = currentItem.getValue(UserInformation.class);
                                    //toastMessage(userInformation.getFirstName());




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
                toastMessage("Failed to alter database.");
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        //get google play services status
        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                GooglePlayServicesUtil.getErrorDialog(status, this,
                        100).show();
            }
        }

        //Add support to map fragment
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        Fragment fragment = new IncidenceFragment();

                        switch (item.getItemId()) {
                            case R.id.action_favorites:
                                toastMessage("menu_uno");
                                getSupportFragmentManager().beginTransaction().replace(R.id.incidenceLayout, getSupportFragmentManager().findFragmentById(R.id.map)).commit();
                                break;
                            case R.id.action_schedules:
                                toastMessage("menu_dos");


                                getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout, fragment).commit();
                                break;
                            case R.id.action_music:
                                toastMessage("menu_tres");
                                break;
                        }
                        return false;
                    }
                });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            trackingIsEnabled = !trackingIsEnabled;
            toastMessage(String.valueOf(trackingIsEnabled));


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }



            /*
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng myLocationLatLon = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());


            myRef = mFirebaseDatabase.getInstance().getReference("position");
            String posicionID = myRef.push().getKey();
            Position myPosition = new Position(user.getEmail(), latitudeGPS, longitudeGPS, getCurrentTimeStamp());
            myRef.child(posicionID).setValue(myPosition);


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocationLatLon, 15.0f));

            //add location change listener with update duration 2000 millicseconds or 10 meters
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                public void onLocationChanged(Location location) {


                    longitudeGPS = location.getLongitude();
                    latitudeGPS = location.getLatitude();


                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(user.getEmail()));


                    myRef = mFirebaseDatabase.getInstance().getReference("position");
                    String posicionID = myRef.push().getKey();
                    Position myPosition = new Position(user.getEmail(), latitudeGPS, longitudeGPS, getCurrentTimeStamp());
                    myRef.child(posicionID).setValue(myPosition);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }

            });
            */


        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();

        //updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setOnInfoWindowClickListener(this);
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
                toastMessage(marker.getPosition().latitude + " " + marker.getPosition().longitude);
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Incidence creada por: \n" +
                "Titulo: " + marker.getTitle() + "\n" +
                "Descripcion: " + marker.getSnippet() + "\n" +
                "Numero: " + marker.getId() + "\n" +
                "Latitud: " + marker.getPosition().latitude + "\n" +
                "Longitud: " + marker.getPosition().longitude + "\n")
                .setPositiveButton("Terminar incidencia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        toastMessage("navegacion");
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        toastMessage("cancelado");
                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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


    public String seleccionarTipoIncidencia() {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        final String tipos_incidencias[] = new String[8];
        tipos_incidencias[0] = "Terremoto";
        tipos_incidencias[1] = "Incendio";
        tipos_incidencias[2] = "Corte luz";
        tipos_incidencias[3] = "Accidente";
        tipos_incidencias[4] = "AAAAAAAAA";
        tipos_incidencias[5] = "BBBBBBBBB";
        tipos_incidencias[6] = "BBBBBBBBB";
        tipos_incidencias[7] = "BBBBBBBBB";

        final String[] seleccionado = new String[1];

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Selecciona el tipo de la incidencia: ")
                .setItems(tipos_incidencias, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        seleccionado[0] = tipos_incidencias[which];
                        handler.sendMessage(handler.obtainMessage());

                    }
                });
        builder.show();
        try {
            Looper.loop();
        } catch (RuntimeException e) {
        }

        return seleccionado[0];

    }

    private void readIncidenceFromFirebase() {
        Incidence incidence = new Incidence();
    }

    /*

     */
    private void writeIncidenceToFirebaseDB(String creatorID, double lat, double lon, boolean state, String currentTimeStamp, String incidenceType) {

        myRef = mFirebaseDatabase.getInstance().getReference();
        String incidenceID = myRef.push().getKey();

        Incidence incidence = new Incidence(creatorID, lat, lon, state, currentTimeStamp, incidenceType);

        //Write incidence to firebase database
        myRef.child("users").child(user.getUid()).child("incidence").child(incidenceID).setValue(incidence);
    }

    private void writeUserPositionToFirebaseDB(String creatorID, double lat, double lon, String currentTimeStamp)
    {
        myRef = mFirebaseDatabase.getInstance().getReference();
        String positionID = myRef.push().getKey();

        Position myPosition = new Position(user.getEmail(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), getCurrentTimeStamp());
        myRef.child("users").child(user.getUid()).child("position").child(positionID).setValue(myPosition);
    }

    public void showLayerSelectionDialog() {

        final CharSequence[] items = new CharSequence[4];

        items[0] = "Normal";
        items[1] = "Terreno";
        items[2] = "Satelite";
        items[3] = "Híbrido";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        //return builder.create();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


            // Obtenemos la última ubicación al ser la primera vez
            processLastLocation();
            // Iniciamos las actualizaciones de ubicación
            startLocationUpdates();
            // Y también las de reconocimiento de actividad
            startActivityUpdates();

            if (mLastLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15.0f));
                //toastMessage(mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
            } else {
                Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_LONG).show();
            }



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
    protected void onSaveInstanceState(Bundle outState) {
        // Protegemos la ubicación actual antes del cambio de configuración
        outState.putParcelable(LOCATION_KEY, mLastLocation);
        outState.putInt(ACTIVITY_KEY, mImageResource);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                StringBuilder stBuilder = new StringBuilder();
                String placename = String.format("%s", place.getName());
                String latitude = String.valueOf(place.getLatLng().latitude);
                String longitude = String.valueOf(place.getLatLng().longitude);
                String address = String.format("%s", place.getAddress());

                stBuilder.append("Name: ");
                stBuilder.append(placename);
                stBuilder.append("\n");
                stBuilder.append("Latitude: ");
                stBuilder.append(latitude);
                stBuilder.append("\n");
                stBuilder.append("Logitude: ");
                stBuilder.append(longitude);
                stBuilder.append("\n");
                stBuilder.append("Address: ");
                stBuilder.append(address);


                String tipoIncidencia = null;

                tipoIncidencia = seleccionarTipoIncidencia();

                //toastMessage(stBuilder.toString());

                // Cogemos la posicion de la incidencia
                LatLng posicionIncidencia = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                //Movemos la camara al marcador
                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionIncidencia));

                //Write incidence to database
                writeIncidenceToFirebaseDB(user.getEmail(), posicionIncidencia.latitude, posicionIncidencia.longitude, true, getCurrentTimeStamp(), tipoIncidencia);
            }
        }

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "El usuario permitió el cambio de ajustes de ubicación.");
                        processLastLocation();
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d(TAG, "El usuario no permitió el cambio de ajustes de ubicación");
                        break;
                }
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdates();

            } else {
                Toast.makeText(this, "Permisos no otorgados", Toast.LENGTH_LONG).show();
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(Constants.UPDATE_INTERVAL)
                .setFastestInterval(Constants.UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "Los ajustes de ubicación satisfacen la configuración.");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d(TAG, "Los ajustes de ubicación no satisfacen la configuración. " +
                                    "Se mostrará un diálogo de ayuda.");
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG, "El Intent del diálogo no funcionó.");
                            // Sin operaciones
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Los ajustes de ubicación no son apropiados.");
                        break;

                }
            }
        });
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);

                updateLocationUI();
            }

            if (savedInstanceState.containsKey(ACTIVITY_KEY)) {
                mImageResource = savedInstanceState.getInt(ACTIVITY_KEY);

                updateRecognitionUI();
            }


        }
    }

    private void updateLocationUI() {
        //toastMessage(mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        //mLatitude.setText(String.valueOf(mLastLocation.getLatitude()));
        //mLongitude.setText(String.valueOf(mLastLocation.getLongitude()));
    }

    private void updateRecognitionUI() {
        //mDectectedActivityIcon.setImageResource(mImageResource);
    }

    private void stopActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi
                .removeLocationUpdates(mGoogleApiClient, new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        toastMessage("Localizacion On"+location.toString());
                    }
                });
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void getLastLocation() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            manageDeniedPermission();
        }
    }

    private void processLastLocation() {
        getLastLocation();
        if (mLastLocation != null) {
            updateLocationUI();
        }
    }

    private void startActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.ACTIVITY_RECOGNITION_INTERVAL,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    private void startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            int GPS_TIME_INTERVAL = 60000; // get gps location every 1 min
            final int GPS_DISTANCE= 1000; // set the distance value in meter


            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, String.format("Nueva ubicación: (%s, %s)",
                                    location.getLatitude(), location.getLongitude()));
                            mLastLocation = location;

                            //toastMessage("Ultima posicion: "+mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
                            writeUserPositionToFirebaseDB(user.getEmail(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), getCurrentTimeStamp());
                            //updateLocationUI();
                        }
                    });

        } else {
            manageDeniedPermission();
        }
    }

    private void manageDeniedPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Aquí muestras confirmación explicativa al usuario
            // por si rechazó los permisos anteriormente
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    private boolean isLocationPermissionGranted() {
        int permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Conexión suspendida");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(
                this,
                "Error de conexión con el código:" + connectionResult.getErrorCode(),
                Toast.LENGTH_LONG)
                .show();
        Snackbar.make(fabPickPlace, connectionResult.getErrorMessage() + "", Snackbar.LENGTH_LONG).show();

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.format("Nueva ubicación: (%s, %s)", location.getLatitude(), location.getLongitude()));
        mLastLocation = location;
        //updateLocationUI();
    }


    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Detección de actividad iniciada");

        } else {
            Log.e(TAG, "Error al iniciar/remover la detección de actividad: "
                    + status.getStatusMessage());
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(Constants.ACTIVITY_KEY, -1);

            mImageResource = Constants.getActivityIcon(type);
            updateRecognitionUI();
        }

    }

}
