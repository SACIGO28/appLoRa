package com.example.lorawan;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ImageButton escala_ib;

    int RSSI, SNR, freq;
    String gateway;
    Double Latitud, Longitud;
    JSONArray ja = MainActivity.ja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("°°°°°°°° inicio OnCreate: ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        escala_ib = (ImageButton) findViewById(R.id.ib_escala);

        if (getIntent().getStringExtra("select").equals("SNR")) {
            escala_ib.setImageResource(R.mipmap.snr);
        }
        if (getIntent().getStringExtra("select").equals("RSSI")) {
            escala_ib.setImageResource(R.mipmap.rssi);
        }

        /*           //DELETE TO UPLOAD
       escala_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this," RSSI",Toast.LENGTH_SHORT).show();            }
        });*/

        System.out.println("°°°°°°°° Fin OnCreate ");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("°°°°°°°° inicio OnMapReady: ");
  /*        try {           //DELETE TO UPLOAD
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("°°°°°°°° NO SE HIZO ESPERA ");
        }
        System.out.println("°°°°°°°° 222222222222222222 ");
*/
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            int REQUEST_LOCATION = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }else{
            // permission has been granted, continue as usual
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            LatLng auxiliar = new LatLng(MainActivity.LatitudGW,MainActivity.LongitudGW);
            System.out.println("°°°°°°°° 333333333333333 ");

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auxiliar, 17));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(auxiliar));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_gw_foreground)).anchor(0.4f, 0.4f).position(auxiliar).title(MainActivity.gateway));

        }

        //    LatLng pinUAO = new LatLng(3.353675, -76.522519);           //DELETE TO UPLOAD
        //     LatLng pinBG = new LatLng(3.489075, -76.499359);
        //    LatLng pinSC = new LatLng(3.476649, -76.515322);

        /*
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pinUAO, 17));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pinUAO));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_gw_foreground)).anchor(0.4f, 0.4f).position(pinUAO).title(MainActivity.gateway));
*/
        try {
            System.out.println("FRECUENCIA: " + MainActivity.frec);
            LatLng lalo;
            List<LatLng> latLngsV = new ArrayList<>();
            List<LatLng> latLngsV2 = new ArrayList<>();
            List<LatLng> latLngsA = new ArrayList<>();
            List<LatLng> latLngsA2 = new ArrayList<>();
            List<LatLng> latLngsR = new ArrayList<>();
            List<LatLng> latLngsR2 = new ArrayList<>();

            for (int i = 0; i < ja.length(); i++) {

                JSONObject jo0;
                try {
                    jo0 = (JSONObject) ja.get(i);

                    Latitud = jo0.getDouble("latitud");       //SERVER UAO
                    Longitud = jo0.getDouble("longitud");
                    //     Altitud = jo0.getDouble("altitud");
                    RSSI = jo0.getInt("rssi");
                    SNR = jo0.getInt("snr");
                    freq = jo0.getInt("frecuencia");
                    gateway = jo0.getString("gateway");

                    //https://www.youtube.com/watch?v=72OpUnGP5Wg           //DELETE TO UPLOAD

                    lalo = new LatLng(Latitud, Longitud);

                    if (getIntent().getStringExtra("select").equals("RSSI")) {

                        if (0 > RSSI && RSSI >= -50) {
                            latLngsV.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-50 > RSSI && RSSI >= -70) {
                            latLngsV.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-70 > RSSI && RSSI >= -90) {
                            latLngsV2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-90 > RSSI && RSSI >= -100) {
                            latLngsA.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-100 > RSSI && RSSI >= -110) {
                            latLngsA2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-110 > RSSI && RSSI >= -120) {
                            latLngsR.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-120 > RSSI && RSSI >= -140) {
                            latLngsR2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else
                            System.out.println("Valor de RSSI por fuera de rangos: " + RSSI);

                    } else {
                        if (25 > SNR && SNR >= 13) {
                            latLngsV.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (13 > SNR && SNR >= 9) {
                            latLngsV2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (9 > SNR && SNR >= 5) {
                            latLngsA.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (5 > SNR && SNR >= 1) {
                            latLngsA2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (1 > SNR && SNR >= -5) {
                            latLngsR.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else if (-5 > SNR && SNR >= -25) {
                            latLngsR2.add(lalo);
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground_png))
                                    .anchor(0.4f, 0.4f).position(lalo).title("RSSI: " + RSSI + "dBm  SNR: " + SNR + "dB"));
                        } else
                            System.out.println("Valor de SNR por fuera de rangos: " + SNR);
                    }
                    System.out.println("XXX - Lat=" + Latitud + " Lon=" + Longitud + " RSSI=" + RSSI + " SNR=" + SNR + " Gw=" + gateway + " Freq=" + freq);


                } catch (Exception e) {
                    System.out.println("EXEPTION XXX - Lat=" + Latitud + " Lon=" + Longitud + " RSSI=" + RSSI + " SNR=" + SNR + " Gw=" + gateway + " Freq=" + freq);
                }
            }
            float[] startPoints = {
                    0.2f, 1f
            };
            int[] colorsV2 = {
                    Color.rgb(80, 200, 120), Color.rgb(76, 187, 23)
            };
            int[] colorsV = {
                    Color.rgb(0, 225, 0), Color.rgb(127, 255, 0)
            };
            int[] colorsA = {
                    Color.rgb(255, 255, 0), Color.rgb(253, 232, 15)
            };
            int[] colorsA2 = {
                    Color.rgb(255, 215, 0), Color.rgb(255, 192, 5)
            };
            int[] colorsR = {
                    Color.rgb(255, 0, 0), Color.rgb(255, 20, 60)
            };
            int[] colorsR2 = {
                    Color.rgb(150, 0, 24), Color.rgb(230, 43, 80)
            };

            Gradient gradientV = new Gradient(colorsV, startPoints);
            Gradient gradientV2 = new Gradient(colorsV2, startPoints);

            Gradient gradientA = new Gradient(colorsA, startPoints);
            Gradient gradientA2 = new Gradient(colorsA2, startPoints);

            Gradient gradientR = new Gradient(colorsR, startPoints);
            Gradient gradientR2 = new Gradient(colorsR2, startPoints);


            groupmap(latLngsR2, gradientR2);
            groupmap(latLngsR, gradientR);

            groupmap(latLngsA2, gradientA2);
            groupmap(latLngsA, gradientA);

            groupmap(latLngsV2, gradientV2);
            groupmap(latLngsV, gradientV);

            System.out.println("°°°°°°°° Fin OnMapReady ");
        } catch (Exception e) {
            System.out.println("¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡  ERROR  !!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }
    }

    private void groupmap(List<LatLng> latLngs, Gradient gradient) {

        try {
            HeatmapTileProvider provider;
            provider = new HeatmapTileProvider.Builder().data(latLngs).gradient(gradient).build();
            provider.setOpacity(0.4);
            provider.setRadius(30);
            TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            tileOverlay.clearTileCache();

        } catch (Exception e) {
            System.out.println("¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡ PROVIDER NULL !!!!!!!!!!!!!!! " + e);
        }
    }

}