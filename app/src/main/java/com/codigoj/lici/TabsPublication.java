package com.codigoj.lici;

import android.content.Intent;

import java.text.SimpleDateFormat;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.codigoj.lici.data.AppPreferences;
import com.codigoj.lici.model.Administrator;
import com.codigoj.lici.utils.DeleteTokenService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class TabsPublication extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private AppPreferences appPreferences;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Local data
    private FirebaseUser user;
    private String id;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_publication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        //Load firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //Shared Preferences
        appPreferences = new AppPreferences(this);
        //Verify the state of session
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //verify that the token is valid yet
                if (user != null ){
                    user = firebaseAuth.getCurrentUser();
                    id = user.getUid();
                }
                else {
                    finish();
                    //Create a new token for the next user session
                    Intent intent = new Intent(TabsPublication.this, DeleteTokenService.class);
                    startService(intent);

                    appPreferences.cleanPreferences();
                    startActivity(new Intent(TabsPublication.this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        };
        //Check if have available publications
        int num_pub_available = appPreferences.getDataint(Administrator.KEY_NUM_PUBLICATIONS,-1);
        Log.d("num-publica", String.valueOf(num_pub_available));
        //Check if have available time for service
        String validy_string = appPreferences.getDataString(Administrator.KEY_DATE_VALIDITY, "");
        String timeServerString = appPreferences.getDataString(Administrator.KEY_TIMESTAMP, "");
        Date validTo = new Date();
        Date timeServer = new Date(Long.parseLong(timeServerString));
        try {
            SimpleDateFormat dfv = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            validTo = dfv.parse(validy_string);
        } catch (ParseException e) {
            Log.d("Datelici-convertion", e.getMessage());
        }
        Log.d("Datelici-server",timeServer.toString());
        Log.d("Datelici-validez", validTo.toString());
        if (timeServer.after(validTo)){
            num_pub_available = 0;
        }

        final int num = num_pub_available;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Deseas crear una nueva publicación?", Snackbar.LENGTH_LONG)
                        .setAction("Si", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(TabsPublication.this, NewPublication.class);
                                i.putExtra("cantidad_pub", num);
                                startActivity(i);
                            }
                        }).show();
            }
        });
        if (num == 0){
            fab.setOnClickListener(null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment_Event fe = new Fragment_Event();
            switch (position) {
                case 0:
                    return fe;
                case 1:
                    Fragment_Promotion fp = new Fragment_Promotion();
                    return fp;
                default:
                    return fe;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Mis Eventos";
                case 1:
                    return "Mis Promociones";
                default:
                    return "Mis Eventos";
            }
        }
    }

    //--------------------------
    //Navegation Drawer
    //--------------------------

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile_nav) {
            finish();
            startActivity(new Intent(this, ProfileActivity.class)
            .putExtra("activity_preview", "TabsPublication"));
        } else if (id == R.id.nav_politics) {
            startActivity(new Intent(this, PoliticsActivity.class));
        } else if (id == R.id.nav_about){
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            firebaseAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}