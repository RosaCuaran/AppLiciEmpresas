package com.codigoj.lici;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.codigoj.lici.data.AppPreferences;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener{

    private static final String TAG = Activity.class.getName();
    private GoogleMap mMap;
    Marker markerLoc;
    private static final int LOCATION_REQUEST_CODE = 1;
    EditText direction;
    private Toolbar toolbar;

    //Atributos
    private double lat = 0;
    private double lon = 0;
    //Datos
    private AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toolbar = (Toolbar) findViewById(R.id.appbar);
        direction = (EditText) findViewById(R.id.direction);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        appPreferences = new AppPreferences(this);
        Typeface berlinSansFB= Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        direction.setTypeface(berlinSansFB);
        loadProfile();
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
        // Controls UI
        if(android.os.Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                setOptions();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Mostrar diálogo explicativo
                } else {
                    // Solicitar permiso
                    ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
                }
            }
        } else {
            setOptions();
            mMap.setOnMapLongClickListener(this);
            mMap.setOnMapClickListener(this);
            double latitud = 1.2058837;
            double longitud = -77.28578700000003;
            LatLng pasto = new LatLng(latitud, longitud);
            CameraPosition camera = CameraPosition.builder()
                    .target(pasto)
                    .zoom(14)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            // ¿Permisos asignados?
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setOptions();
                Toast.makeText(this, "Se activo localización", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setOptions(){
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public void getMyLocationAddress( double lat, double lon ) {
        Geocoder geocoder= new Geocoder(this, Locale.ENGLISH);
        try {
            //Place your latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(lat,lon, 1);
            if(addresses != null) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for(int i=0; i<fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)+ " ");
                }
                direction.setText(strAddress.toString());
            }
            else
                direction.setText("No se encuentra dirección");
                Log.i(TAG, "No location found..!");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Servicio no disponible", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btn_save_location) {
            String dir = direction.getText().toString();
            appPreferences.saveDataString(ProfileActivity.KEY_DIRECTION, dir);
            appPreferences.saveDataString(ProfileActivity.KEY_LATITUD, String.valueOf(lat));
            appPreferences.saveDataString(ProfileActivity.KEY_LONGITUD, String.valueOf(lon));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadProfile(){
        //Verify if exist the profile
        //Search the data of company in the SharedPrefernces
        if (appPreferences.getSharedPreferences().contains(ProfileActivity.KEY_DIRECTION)){
            String dir = appPreferences.getDataString(ProfileActivity.KEY_DIRECTION, "");
            direction.setText(dir);
        }

    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
        setLat(latLng.latitude);
        setLon(latLng.longitude);
        getMyLocationAddress(lat,lon);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        limpiarMarcadores();
    }

    private void limpiarMarcadores() {
        mMap.clear();
    }
}
