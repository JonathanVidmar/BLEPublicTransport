package com.lth.thesis.blepublictransport.Main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.lth.thesis.blepublictransport.Fragments.*;
import com.lth.thesis.blepublictransport.R;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Observer {
    private BLEPublicTransport application;
    private Fragment currentFragment;
    private ImageView menuButton;

    // SettingConstants
    public static final String STATION_FRAGMENT = "stationFragment";
    public static final String PAYMENT_FRAGMENT = "paymentFragment";
    public static final String SETTINGS_FRAGMENT = "settingsFragment";
    public static final String TICKET_FRAGMENT = "ticketFragment";
    public static final String SHOW_TICKET_FRAGMENT = "showTicketFragment";
    public static final String BLUETOOTH_PARING_FRAGMENT = "bluetoothFragment";
    public static final String MEASUREMENT_FRAGMENT = "measurementFragment";
    public static final String TRAIN_FRAGMENT = "trainFragment";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (BLEPublicTransport) getApplication();
        application.active = true;
        application.getBeaconCommunicator().addObserver(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        performFragmentTransactionFromIntent();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        menuButton = (ImageView) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(nav);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void changeMenuColor(int color){
        menuButton.setColorFilter(color);
    }

    private void performFragmentTransactionFromIntent() {
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras == null) {
            executeNavigationTo(STATION_FRAGMENT);
        } else {
            String frag = extras.getString("fragment");
            if (frag != null) {
                switch (frag) {
                    case "nearby":
                        executeNavigationTo(STATION_FRAGMENT);
                        break;
                    case "payment":
                        executeNavigationTo(PAYMENT_FRAGMENT);
                        break;
                    case "ticket":
                        executeNavigationTo(TICKET_FRAGMENT);
                        break;
                }
            } else {
                // Both "Nearby" case as well as default behaviour
                executeNavigationTo(STATION_FRAGMENT);
            }
        }
    }

    private void executeNavigationTo(String destination) {
        resetBackgroundColor();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        currentFragment = getSupportFragmentManager().findFragmentByTag(destination);
        switch (destination) {
            case STATION_FRAGMENT:
                if (currentFragment == null) currentFragment = new NearbyFragment();
                break;
            case PAYMENT_FRAGMENT:
                if (currentFragment == null) currentFragment = new PaymentFragment();
                break;
            case SETTINGS_FRAGMENT:
                if (currentFragment == null) currentFragment = new SettingsFragment();
                break;
            case TICKET_FRAGMENT:
                if (currentFragment == null) currentFragment = new ShowTicketFragment();
                break;
            case BLUETOOTH_PARING_FRAGMENT:
                if (currentFragment == null) currentFragment = new GateConnectionFragment();
                break;
            case MEASUREMENT_FRAGMENT:
            if (currentFragment == null) currentFragment = new MeasurementFragment();
            break;
        }
        fragmentTransaction.replace(R.id.fragment_container, currentFragment, destination);
        fragmentTransaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        application.active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        application.active = true;
        performFragmentTransactionFromIntent();
    }

    // Needed to refresh intent to new one when coming from notification with application still running
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_station_home:
                executeNavigationTo(STATION_FRAGMENT);
                break;
            case R.id.nav_payment:
                executeNavigationTo(PAYMENT_FRAGMENT);
                break;
            case R.id.nav_settings:
                executeNavigationTo(SETTINGS_FRAGMENT);
                break;
            case R.id.nav_bluetooth_connection:
                executeNavigationTo(BLUETOOTH_PARING_FRAGMENT);
                break;
            case R.id.nav_measurement:
                executeNavigationTo(MEASUREMENT_FRAGMENT);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (currentFragment != null && currentFragment.isVisible()) {
            if (currentFragment instanceof AbstractObserverFragment) {
                ((AbstractObserverFragment) currentFragment).update(data);
            }
        }
    }

    private void resetBackgroundColor() {
        View v = ((DrawerLayout) findViewById(R.id.drawer_layout)).getRootView();
        v.setBackgroundColor(Color.WHITE);
    }
}
