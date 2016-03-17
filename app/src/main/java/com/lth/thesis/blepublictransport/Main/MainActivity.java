package com.lth.thesis.blepublictransport.Main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.MenuItem;

import android.view.View;
import com.lth.thesis.blepublictransport.Fragments.GateConnectionFragment;
import com.lth.thesis.blepublictransport.Fragments.AbstractObserverFragment;
import com.lth.thesis.blepublictransport.Fragments.PaymentFragment;
import com.lth.thesis.blepublictransport.Fragments.SettingsFragment;
import com.lth.thesis.blepublictransport.Fragments.ShowTicketFragment;
import com.lth.thesis.blepublictransport.Fragments.NearbyFragment;
import com.lth.thesis.blepublictransport.R;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Observer {
    private BLEPublicTransport application;
    private Toolbar toolbar;
    private Fragment currentFragment;

    // SettingConstants
    public static final String STATION_FRAGMENT = "stationFragment";
    public static final String PAYMENT_FRAGMENT = "paymentFragment";
    public static final String SETTINGS_FRAGMENT = "settingsFragment";
    public static final String TICKET_FRAGMENT = "ticketFragment";
    public static final String SHOW_TICKET_FRAGMENT = "showTicketFragment";
    public static final String BLUETOOTH_PARING_FRAGMENT = "bluetoothFragment";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                toolbar.setTitle("Nearby");
                if (currentFragment == null) currentFragment = new NearbyFragment();
                break;
            case PAYMENT_FRAGMENT:
                toolbar.setTitle("Payment");
                if (currentFragment == null) currentFragment = new PaymentFragment();
                break;
            case SETTINGS_FRAGMENT:
                toolbar.setTitle("Settings");
                if (currentFragment == null) currentFragment = new SettingsFragment();
                break;
            case TICKET_FRAGMENT:
                toolbar.setTitle("Destination");
                if (currentFragment == null) currentFragment = new ShowTicketFragment();
                break;
            case BLUETOOTH_PARING_FRAGMENT:
                toolbar.setTitle("Bluetooth");
                if (currentFragment == null) currentFragment = new GateConnectionFragment();
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
