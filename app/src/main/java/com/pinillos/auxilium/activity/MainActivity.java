package com.pinillos.auxilium.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pinillos.auxilium.BuildConfig;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.Util.MessageUtil;
import com.pinillos.auxilium.fragment.MapsFragment;
import com.pinillos.auxilium.fragment.ShowIncidenceFragment;
import com.pinillos.auxilium.fragment.ShowUsersFragment;
import com.pinillos.auxilium.model.Incidence;
import com.pinillos.auxilium.model.UserInformation;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static int PLACE_PICKER_REQUEST = 1;
    private Toolbar toolbar;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String TAG = MainActivity.class.getSimpleName();

    //Firebase auth and db instances
    public static FirebaseDatabase firebaseDatabase;
    public static FirebaseAuth firebaseAuth;
    public static FirebaseAuth.AuthStateListener firebaseAuthListener;
    public static DatabaseReference firebaseDatabaseReference;
    public static FirebaseUser firebaseUser;
    public static StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureAndInicializeToolbar();
        configureAndInicializeDraerNavigation();
        configureAndInicializeBottomNavigation();
        configureAndInicializeFirebase();

     //Inicializamos el MapsFragemnts al inicio
        Fragment mapsFragment = new MapsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mapsFragment);
        transaction.commit();

    }

    private void configureAndInicializeFirebase() {
        //instace firebase auth and database
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

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
    }

    private void configureAndInicializeToolbar() {
        //Reference toolbar UI
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
    }

    private void configureAndInicializeBottomNavigation() {
        //Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                        switch (item.getItemId()) {
                            case R.id.action_mapa:
                                getSupportActionBar().setDisplayShowTitleEnabled(false);
                                toolbar.setBackgroundResource(R.color.transparent);

                                Fragment mapsFragments = new MapsFragment();
                                transaction.replace(R.id.fragment_container, mapsFragments).commit();
                                return true;
                            case R.id.action_incidencias:
                                getSupportActionBar().setDisplayShowTitleEnabled(true);
                                toolbar.setTitle("Incidencias");
                                toolbar.setBackgroundResource(R.color.colorPrimary);

                                Fragment showIncidenceFragment = new ShowIncidenceFragment();
                                transaction.replace(R.id.fragment_container, showIncidenceFragment).commit();
                                return true;
                            case R.id.action_users:
                                getSupportActionBar().setDisplayShowTitleEnabled(true);
                                toolbar.setTitle("Usuarios");
                                toolbar.setBackgroundResource(R.color.colorPrimary);

                                Fragment showUsersFragment = new ShowUsersFragment();
                                transaction.replace(R.id.fragment_container, showUsersFragment).commit();
                                return true;
                        }
                        return false;
                    }
                });
        //bottomNavigationView.setSelectedItemId(R.id.action_incidencias);
    }

    private void configureAndInicializeDraerNavigation() {
        //Drawer menu reference and initialization
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Navigation view reference and initialization
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_camera) {
            MessageUtil.toastMessage(this, "Funcionalidad en pruebas");


        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
               // imageView.setImageBitmap(imageBitmap);

                MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "fd" , "f");

            }
            */

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        //mRequestingLocationUpdates = false;
                        //updateUI();
                        break;
                }
                break;
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);


                if ( place != null )
                {
                    String tipoIncidencia;
                    String placeName = String.format("%s", place.getName());
                    String latitude = String.valueOf(place.getLatLng().latitude);
                    String longitude = String.valueOf(place.getLatLng().longitude);
                    String address = String.format("%s", place.getAddress());

                    tipoIncidencia = seleccionarTipoIncidencia();


                    //Write incidence to database


                    writeIncidenceToFirebaseDB(firebaseUser.getEmail(), place.getLatLng().latitude, place.getLatLng().longitude, true, getCurrentTimeStamp(), tipoIncidencia);

                }
            }
        }
    }

    private void writeIncidenceToFirebaseDB(String creatorID, double lat, double lon, boolean state, String currentTimeStamp, String incidenceType) {

        firebaseDatabaseReference = firebaseDatabase.getInstance().getReference();
        String incidenceID = firebaseDatabaseReference.push().getKey();

        Incidence incidence = new Incidence(creatorID, lat, lon, state, currentTimeStamp, incidenceType, null);

        //Write incidence to firebase database
        firebaseDatabaseReference.child("users").child(firebaseUser.getUid()).child("incidence").child(incidenceID).setValue(incidence);
    }

    public String seleccionarTipoIncidencia() {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        //Get incidence types from resources
        Resources res = getResources();
        final String[] incidenceTypes = res.getStringArray(R.array.incidenceType_array);

        final String[] selectedType = new String[1];

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Selecciona el tipo de la incidencia: ")
                .setItems(incidenceTypes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType[0] = incidenceTypes[which];
                        handler.sendMessage(handler.obtainMessage());
                    }
                });
        builder.show();
        try {
            Looper.loop();
        } catch (RuntimeException e) {
        }

        return selectedType[0];

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

}