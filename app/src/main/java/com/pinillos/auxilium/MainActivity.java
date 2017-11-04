package com.pinillos.auxilium;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "LoginActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseUser user;


    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;
    FloatingActionButton fabPickPlace;

    private GoogleMap mMap;
    int status;

    double longitudeGPS, latitudeGPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }


        //Instace api client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

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
        myRef = mFirebaseDatabase.getInstance().getReference( );
        user = mAuth.getCurrentUser();


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

                int incidenceIcon;

                //first clear map
                mMap.clear();

                //retrieve data from database
                for (DataSnapshot dbObjects : dataSnapshot.getChildren())
                {
                    String objectType = dbObjects.getKey();

                    for (DataSnapshot currentItem : dbObjects.getChildren())
                    {
                        //if retrieved database object are incident type
                        if ( objectType.equals("incidence") )
                        {
                            //retrieve incidence object from database
                            String incidenceID = currentItem.getKey();
                            Incidence incidence = currentItem.getValue(Incidence.class);

                            //according to incidence type, shows different icon
                            switch (incidence.getType())
                            {
                                case "Incendio" : incidenceIcon = R.mipmap.ic_incendio_round; break;
                                case "Corte luz": incidenceIcon = R.mipmap.ic_electrico_round; break;
                                default: incidenceIcon = R.mipmap.ic_incendio_round; break;
                            }

                            //if the incidence is activated, add google maps marker
                            if ( incidence.getStatus() )
                            {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(incidence.getLat(), incidence.getLon()))
                                        .title(incidence.getType())
                                        .snippet("Creada por "+incidence.getCreatorID())
                                        .icon(BitmapDescriptorFactory.fromResource(incidenceIcon))
                                        .draggable(true));
                            }

                            /*
                            toastMessage
                            (
                                incidenceID+"\n"+
                                incidence.getCreatorID()+"\n"+
                                incidence.getLat()+"\n"+
                                incidence.getLon()+"\n"+
                                incidence.getStatus()+"\n"+
                                incidence.getTimestamp()+"\n"+
                                incidence.getType()
                            );
                            */
                        }

                        //if retrieved database object are position type
                        if ( objectType.equals("position"))
                        {

                            //retrieve position object from database
                            String positionID = currentItem.getKey();
                            Position position = currentItem.getValue(Position.class);

                            //add user position marker
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(position.getLat(), position.getLon()))
                                    .title(position.getCreatorID())
                                    .snippet("Lat: "+ position.getLat()+ " Lon: "+position.getLon()));

                            /*
                            toastMessage
                                    (
                                                    "usuario: "+ position.getCreatorID()+"\n"+
                                                    position.getTimestamp() + "\n" +
                                                    position.getLat() + "\n" +
                                                    position.getLon()

                                    );
                            */
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Bottom navigation
        BottomNavigationView bottomNavigationView =  findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.action_favorites:
                                toastMessage("menu_uno");
                                break;
                            case R.id.action_schedules:
                                toastMessage("menu_dos");
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
            //if (!checkLocation())
            //  return false;


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng myLocationLatLon = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());



             mMap.addMarker(new MarkerOptions()
                    .position(myLocationLatLon)
                    .title(user.getEmail()));

            myRef = mFirebaseDatabase.getInstance().getReference("position");
            String posicionID = myRef.push().getKey();
            Position myPosition =  new Position(user.getEmail(),latitudeGPS, longitudeGPS, getCurrentTimeStamp());
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
                    Position myPosition =  new Position(user.getEmail(),latitudeGPS, longitudeGPS, getCurrentTimeStamp());
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(fabPickPlace, connectionResult.getErrorMessage() + "", Snackbar.LENGTH_LONG).show();
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

                int iconoIncidencia = 0;

                switch (tipoIncidencia)
                {
                    case "Incendio" : iconoIncidencia = R.mipmap.ic_incendio_round; break;
                    case "Corte luz": iconoIncidencia = R.mipmap.ic_electrico_round; break;
                    default: iconoIncidencia = R.mipmap.ic_incendio_round; break;
                }

                //Añadimos marcador
                mMap.addMarker(new MarkerOptions()
                        .position(posicionIncidencia)
                        .title(tipoIncidencia)
                        .snippet("Creada por " + user.getEmail())
                        .icon(BitmapDescriptorFactory.fromResource(iconoIncidencia))
                        .draggable(true));

                //Movemos la camara al marcador
                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionIncidencia));

                //Write incidence to database
                writeIncidenceToFirebase(user.getEmail(),posicionIncidencia.latitude, posicionIncidencia.longitude, true, getCurrentTimeStamp(), tipoIncidencia);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                toastMessage(marker.getPosition().latitude+" "+marker.getPosition().longitude);
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Incidence creada por: \n"+
                           "Titulo: "+marker.getTitle()+"\n"+
                           "Descripcion: "+marker.getSnippet()+"\n"+
                           "Numero: "+marker.getId()+"\n"+
                           "Latitud: "+marker.getPosition().latitude+"\n"+
                           "Longitud: "+marker.getPosition().longitude+"\n")
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

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }



    public String seleccionarTipoIncidencia()
    {

        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message mesg)
            {
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
        try{ Looper.loop(); }
        catch(RuntimeException e){}

        return  seleccionado[0];

    }

    private void readIncidenceFromFirebase()
    {
        Incidence incidence = new Incidence();
    }

    /*

     */
    private void writeIncidenceToFirebase(String creatorID, double lat, double lon, boolean state, String currentTimeStamp, String incidenceType) {

        myRef = mFirebaseDatabase.getInstance().getReference("incidence");
        String incidenceID = myRef.push().getKey();
        Incidence incidence = new Incidence(creatorID, lat, lon, state, currentTimeStamp, incidenceType);

        //Write incidence to firebase database
        myRef.child(incidenceID).setValue(incidence);

    }


    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toastMessage(longitudeGPS+" "+latitudeGPS);
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
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
    };
}
