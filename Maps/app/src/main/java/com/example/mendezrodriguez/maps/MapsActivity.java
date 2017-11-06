package com.example.mendezrodriguez.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener{
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    PolylineOptions lineOptions;
    ArrayList<LatLng> points;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.newInstance(new GoogleMapOptions());
        mapFragment.getMapAsync(this);

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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        agregarLogos();
        dibujarRutas();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        CameraPosition cam = new CameraPosition.Builder().target(new LatLng(22.1467265, -101.0136284)).zoom(15).bearing(0).tilt(30).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.action_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

    public void agregarLogos()
    {
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.141640, -101.032407)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.148350, -101.012943)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.127022, -101.030512)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.150544, -100.990304)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.143226, -101.001642)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.135469, -100.979958)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.138073, -101.001045)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.155912, -101.004267)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.151824, -100.974552)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.142356, -100.957080)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.181607, -100.950277)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.097395, -100.885266)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.richi)).anchor(0.0f, 1.0f).title("Restaurante Richards").position(new LatLng(22.014435, -100.839735)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.sm)).anchor(0.0f, 1.0f).title("Sonoras Meat").position(new LatLng(22.141540, -101.032291)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.sm)).anchor(0.0f, 1.0f).title("Sonoras Meat").position(new LatLng(22.143276, -101.001661)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.villa)).anchor(0.0f, 1.0f).title("Villa Italian").position(new LatLng(22.182344, -100.950542)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.parrilla)).anchor(0.0f, 1.0f).title("La Parrilla").position(new LatLng(22.155748, -101.003809)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.parrilla)).anchor(0.0f, 1.0f).title("La Parrilla").position(new LatLng(22.014431, -100.839679)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.burger)).anchor(0.0f, 1.0f).title("La Burger").position(new LatLng(22.149266, -101.001567)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.ines)).anchor(0.0f, 1.0f).title("Doña Ines").position(new LatLng(22.155351, -101.003723)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.ines)).anchor(0.0f, 1.0f).title("Doña Ines").position(new LatLng(22.015530, -100.840031)));

    }

    public void dibujarRutas()
    {
        lineOptions = new PolylineOptions();
        points = new ArrayList<>();
        points.add(new LatLng(22.164796, -101.019634));
        points.add(new LatLng(22.164672, -101.018685));
        points.add(new LatLng(22.164394, -101.017878));
        points.add(new LatLng(22.164245, -101.017275));
        points.add(new LatLng(22.164076, -101.016258));
        points.add(new LatLng(22.165497, -101.015794));
        points.add(new LatLng(22.167693, -101.017417));
        points.add(new LatLng(22.168140, -101.017645));
        points.add(new LatLng(22.167926, -101.019219));
        points.add(new LatLng(22.167573, -101.019458));
        points.add(new LatLng(22.167290, -101.019530));
        points.add(new LatLng(22.166907, -101.019887));
        points.add(new LatLng(22.166695, -101.020109));
        points.add(new LatLng(22.164565, -101.019686));
        points.add(new LatLng(22.161564, -101.019348));
        points.add(new LatLng(22.158349, -101.019106));
        points.add(new LatLng(22.158150, -101.020764));
        points.add(new LatLng(22.156318, -101.020377));
        points.add(new LatLng(22.154321, -101.019406));
        points.add(new LatLng(22.151062, -101.019594));
        points.add(new LatLng(22.148747, -101.019696));
        points.add(new LatLng(22.148322, -101.019531));
        points.add(new LatLng(22.148074, -101.019209));
        points.add(new LatLng(22.148074, -101.018678));
        points.add(new LatLng(22.148382, -101.015325));
        points.add(new LatLng(22.148183, -101.015084));
        points.add(new LatLng(22.148183, -101.014757));
        points.add(new LatLng(22.148392, -101.014516));
        points.add(new LatLng(22.148849, -101.009125));
        points.add(new LatLng(22.150981, -100.981910));
        points.add(new LatLng(22.151865, -100.981973));
        points.add(new LatLng(22.153356, -100.981839));
        points.add(new LatLng(22.154548, -100.981576));
        points.add(new LatLng(22.156009, -100.980573));
        points.add(new LatLng(22.156476, -100.979430));
        points.add(new LatLng(22.157152, -100.978534));
        points.add(new LatLng(22.158126, -100.976040));
        points.add(new LatLng(22.158056, -100.974629));
        points.add(new LatLng(22.156297, -100.974591));
        points.add(new LatLng(22.154220, -100.974242));
        points.add(new LatLng(22.152978, -100.973550));
        points.add(new LatLng(22.152054, -100.973309));
        points.add(new LatLng(22.150355, -100.972714));
        points.add(new LatLng(22.151130, -100.969887));
        points.add(new LatLng(22.151378, -100.968712));
        points.add(new LatLng(22.151308, -100.959018));
        points.add(new LatLng(22.151169, -100.958401));
        points.add(new LatLng(22.151119, -100.957913));
        points.add(new LatLng(22.150463, -100.957253));
        points.add(new LatLng(22.149916, -100.956851));
        points.add(new LatLng(22.149827, -100.956470));
        points.add(new LatLng(22.149887, -100.956186));
        points.add(new LatLng(22.150881, -100.955601));
        points.add(new LatLng(22.151159, -100.955199));
        points.add(new LatLng(22.151338, -100.954464));
        points.add(new LatLng(22.151517, -100.952206));
        points.add(new LatLng(22.152093, -100.945474));
        points.add(new LatLng(22.152272, -100.942947));
        points.add(new LatLng(22.152342, -100.942448));
        points.add(new LatLng(22.152300, -100.941681));
        points.add(new LatLng(22.152475, -100.939769));
        points.add(new LatLng(22.149047, -100.939635));
        points.add(new LatLng(22.146463, -100.939522));
        points.add(new LatLng(22.145842, -100.939969));
        points.add(new LatLng(22.141768, -100.933870));
        points.add(new LatLng(22.141579, -100.933264));
        points.add(new LatLng(22.141480, -100.931810));
        points.add(new LatLng(22.141162, -100.925024));
        points.add(new LatLng(22.141063, -100.922969));
        points.add(new LatLng(22.140159, -100.923425));
        points.add(new LatLng(22.139930, -100.919600));
        points.add(new LatLng(22.140854, -100.919520));
        points.add(new LatLng(22.140685, -100.916843));
        points.add(new LatLng(22.133503, -100.917313));
        points.add(new LatLng(22.130147, -100.917504));
        points.add(new LatLng(22.129938, -100.917445));
        points.add(new LatLng(22.128686, -100.917547));
        points.add(new LatLng(22.128030, -100.917713));
        points.add(new LatLng(22.127185, -100.918223));
        points.add(new LatLng(22.123070, -100.921683));
        points.add(new LatLng(22.122812, -100.921860));
        points.add(new LatLng(22.121684, -100.922914));
        points.add(new LatLng(22.121277, -100.923170));
        points.add(new LatLng(22.117007, -100.925158));
        points.add(new LatLng(22.116485, -100.925412));
        points.add(new LatLng(22.115491, -100.925986));
        points.add(new LatLng(22.114805, -100.926732));
        points.add(new LatLng(22.113553, -100.929403));
        points.add(new LatLng(22.113424, -100.930095));
        points.add(new LatLng(22.111009, -100.935476));
        points.add(new LatLng(22.110691, -100.935181));
        points.add(new LatLng(22.110746, -100.935012));
        points.add(new LatLng(22.110468, -100.934738));
        points.add(new LatLng(22.110746, -100.934110));
        points.add(new LatLng(22.110637, -100.933643));
        points.add(new LatLng(22.108738, -100.931782));
        points.add(new LatLng(22.109583, -100.930790));
        points.add(new LatLng(22.107913, -100.929143));
        points.add(new LatLng(22.109610, -100.930788));
        points.add(new LatLng(22.108761, -100.931782));
        points.add(new LatLng(22.110626, -100.933608));
        points.add(new LatLng(22.110745, -100.934110));
        points.add(new LatLng(22.110512, -100.934764));
        points.add(new LatLng(22.110765, -100.935001));
        points.add(new LatLng(22.111890, -100.932601));
        points.add(new LatLng(22.112000, -100.932495));
        points.add(new LatLng(22.114510, -100.926970));
        points.add(new LatLng(22.115012, -100.926216));
        points.add(new LatLng(22.115613, -100.925720));
        points.add(new LatLng(22.116562, -100.925283));
        points.add(new LatLng(22.116830, -100.925162));
        points.add(new LatLng(22.120060, -100.923673));
        points.add(new LatLng(22.121397, -100.923037));
        points.add(new LatLng(22.121839, -100.922696));
        points.add(new LatLng(22.122917, -100.921696));
        points.add(new LatLng(22.123031, -100.921538));
        points.add(new LatLng(22.126510, -100.918569));
        points.add(new LatLng(22.128221, -100.917535));
        points.add(new LatLng(22.128937, -100.917390));
        points.add(new LatLng(22.135029, -100.917009));
        points.add(new LatLng(22.140634, -100.916639));
        points.add(new LatLng(22.140793, -100.917020));
        points.add(new LatLng(22.141131, -100.923441));
        points.add(new LatLng(22.141608, -100.932298));
        points.add(new LatLng(22.141701, -100.933393));
        points.add(new LatLng(22.145904, -100.939817));
        points.add(new LatLng(22.146444, -100.939450));
        points.add(new LatLng(22.152347, -100.939606));
        points.add(new LatLng(22.152817, -100.939674));
        points.add(new LatLng(22.152439, -100.943080));
        points.add(new LatLng(22.151365, -100.955197));
        points.add(new LatLng(22.151514, -100.955910));
        points.add(new LatLng(22.151903, -100.956215));
        points.add(new LatLng(22.151943, -100.956720));
        points.add(new LatLng(22.151540, -100.957277));
        points.add(new LatLng(22.151426, -100.958060));
        points.add(new LatLng(22.155013, -100.958728));
        points.add(new LatLng(22.154693, -100.961158));
        points.add(new LatLng(22.153739, -100.967662));
        points.add(new LatLng(22.153620, -100.967836));
        points.add(new LatLng(22.153317, -100.968193));
        points.add(new LatLng(22.152964, -100.969467));
        points.add(new LatLng(22.152939, -100.969722));
        points.add(new LatLng(22.152472, -100.971683));
        points.add(new LatLng(22.152964, -100.971782));
        points.add(new LatLng(22.153299, -100.971723));
        points.add(new LatLng(22.158476, -100.971865));
        points.add(new LatLng(22.158059, -100.974496));
        points.add(new LatLng(22.161094, -100.974464));
        points.add(new LatLng(22.160462, -100.977351));
        points.add(new LatLng(22.160006, -100.979951));
        points.add(new LatLng(22.157060, -100.979635));
        points.add(new LatLng(22.156419, -100.983532));
        points.add(new LatLng(22.152971, -100.982302));
        points.add(new LatLng(22.151047, -100.982233));
        points.add(new LatLng(22.150848, -100.984263));

        lineOptions.addAll(points);
        lineOptions.width(7);
        lineOptions.color(Color.RED);
        mMap.addPolyline(lineOptions);
    }
}
