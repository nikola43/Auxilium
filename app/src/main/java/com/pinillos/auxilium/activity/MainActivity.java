package com.pinillos.auxilium.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.pinillos.auxilium.BuildConfig;
import com.pinillos.auxilium.R;
import com.pinillos.auxilium.Util.MessageUtil;
import com.pinillos.auxilium.fragment.MapsFragment;
import com.pinillos.auxilium.fragment.ShowIncidenceFragment;
import com.pinillos.auxilium.fragment.ShowUsersFragment;
import com.pinillos.auxilium.model.UserInformation;


import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureAndInicializeToolbar();
        configureAndInicializeDraerNavigation();
        configureAndInicializeBottomNavigation();


     //Inicializamos el MapsFragemnts al inicio
        Fragment mapsFragment = new MapsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mapsFragment);
        transaction.commit();
    }

    private void configureAndInicializeToolbar() {
        //Reference toolbar UI
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void configureAndInicializeBottomNavigation() {
        //Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        Fragment showIncidenceFragment = new ShowIncidenceFragment();
                        Fragment showUsersFragment = new ShowUsersFragment();
                        Fragment mapsFragments = new MapsFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                        switch (item.getItemId()) {
                            case R.id.action_mapa:
                                MessageUtil.toastMessage(getApplicationContext(), "menu_uno");
                                transaction.replace(R.id.fragment_container, mapsFragments).commit();
                                break;
                            case R.id.action_incidencias:
                                MessageUtil.toastMessage(getApplicationContext(), "menu_dos");
                                transaction.replace(R.id.fragment_container, showIncidenceFragment).commit();
                                break;
                            case R.id.action_users:
                                transaction.replace(R.id.fragment_container, showUsersFragment).commit();
                                MessageUtil.toastMessage(getApplicationContext(), "menu_tres");
                                break;
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
            //firebaseAuth.signOut();
            //Intent intent = new Intent(this, LoginActivity.class);
            //startActivity(intent);
            MessageUtil.toastMessage(this, "Funcionalidad en pruebas");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        MessageUtil.toastMessage(this, "Funcionalidad en pruebas");

        if (id == R.id.nav_camera) {

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

}