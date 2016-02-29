package com.lth.thesis.blepublictransport;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.Menu;
import android.view.MenuItem;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Observer {

    private NavigationView navigationView;
    private Toolbar toolbar;

    private static StationHomeFragment stationFragment;
    private static PaymentFragment paymentFragment;
    private static SettingsFragment settingsFragment;
    private static ShowTicketFragment ticketFragment;
    private static BluetoothConnectionFragment bluetoothFragment;
    private static final String STATION_FRAGMENT = "stationFragment";
    private static final String PAYMENT_FRAGMENT = "paymentFragment";
    private static final String SETTINGS_FRAGMENT = "settingsFragment";
    private static final String TICKET_FRAGMENT = "ticketFragment";
    private static final String BLUETOOTH_PARING_FRAGMENT = "bluetoothFragment";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private String currentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        BLEPublicTransport application = (BLEPublicTransport) getApplication();
        application.active = true;
        application.getBeaconCommunicator().addObserver(this);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        performFragmentTransactionFromIntent();

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Jacob är bäst", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void performFragmentTransactionFromIntent() {
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras == null) {
            executeNavigationTo(BLUETOOTH_PARING_FRAGMENT);
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

    private void executeNavigationTo(String destination){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (destination) {
            case STATION_FRAGMENT:
                toolbar.setTitle("Nearby");
                currentFragmentTag = STATION_FRAGMENT;
                stationFragment = (StationHomeFragment) getSupportFragmentManager().findFragmentByTag(STATION_FRAGMENT);
                if (stationFragment == null) stationFragment = new StationHomeFragment();
                fragmentTransaction.replace(R.id.fragment_container, stationFragment, STATION_FRAGMENT);
                break;
            case PAYMENT_FRAGMENT:
                toolbar.setTitle("Payment");
                currentFragmentTag = PAYMENT_FRAGMENT;
                paymentFragment = (PaymentFragment) getSupportFragmentManager().findFragmentByTag(PAYMENT_FRAGMENT);
                if (paymentFragment == null) paymentFragment = new PaymentFragment();
                fragmentTransaction.replace(R.id.fragment_container, paymentFragment, PAYMENT_FRAGMENT);
                break;
            case SETTINGS_FRAGMENT:
                toolbar.setTitle("Settings");
                currentFragmentTag = SETTINGS_FRAGMENT;
                settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT);
                if (settingsFragment == null) settingsFragment = new SettingsFragment();
                fragmentTransaction.replace(R.id.fragment_container, settingsFragment, SETTINGS_FRAGMENT);
                break;
            case TICKET_FRAGMENT:
                toolbar.setTitle("Ticket");
                currentFragmentTag = TICKET_FRAGMENT;
                ticketFragment = (ShowTicketFragment) getSupportFragmentManager().findFragmentByTag(TICKET_FRAGMENT);
                if (ticketFragment == null) ticketFragment = new ShowTicketFragment();
                fragmentTransaction.replace(R.id.fragment_container, ticketFragment, TICKET_FRAGMENT);
                break;
            case BLUETOOTH_PARING_FRAGMENT:
                toolbar.setTitle("Bluetooth");
                currentFragmentTag = BLUETOOTH_PARING_FRAGMENT;
                bluetoothFragment = (BluetoothConnectionFragment) getSupportFragmentManager().findFragmentByTag(BLUETOOTH_PARING_FRAGMENT);
                if (bluetoothFragment == null) bluetoothFragment = new BluetoothConnectionFragment();
                fragmentTransaction.replace(R.id.fragment_container, bluetoothFragment, BLUETOOTH_PARING_FRAGMENT);
                break;
            }

        fragmentTransaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        BLEPublicTransport application = (BLEPublicTransport) getApplication();
        application.active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        BLEPublicTransport application = (BLEPublicTransport) getApplication();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // no use atm for that menu
        // getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        // Should be redundant
        if (currentFragment != null && currentFragment.isVisible()) {
            if (currentFragment instanceof StationHomeFragment) {
                ((StationHomeFragment) currentFragment).update(data);
            }
        }
    }

}
