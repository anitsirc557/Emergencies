package dte.masteriot.mdp.emergencies;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RadioButton mapita, hybrid, satellite;
    String coordenadas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapita = (RadioButton)findViewById(R.id.mapita);
        hybrid = (RadioButton)findViewById(R.id.hybrid);
        satellite = (RadioButton)findViewById(R.id.satellite);

        coordenadas =  getIntent().getStringExtra("coordinates");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    public void modoMapa(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }
    public void modoSatellite(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }
    public void modoHybrid(View view){
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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

        double latitude;
        double longitude;
        //Pasar coordenadas de string a int
        latitude = Double.parseDouble(coordenadas.substring(1,15));
        longitude = Double.parseDouble(coordenadas.substring(19,32));

        //Crear objeto LatLng con esas dos variables coordenadas_camara
        LatLng coordenadas_camara = new LatLng(longitude, latitude);
        //Generar marker
        mMap.addMarker(new MarkerOptions().position(coordenadas_camara).title("Marker in Madrid"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas_camara));

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
