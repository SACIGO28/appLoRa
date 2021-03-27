package com.example.lorawan;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button rssi_nt, snr_bt;
    ImageButton agregar_ib, borrar_ib;
    RadioButton f9, f8, f4;
    Spinner lugares;
    public static int frec;
    String inicial = "Seleccione el Gateway";
    public static String gateway;
    public static JSONArray ja;
    String accion = "", nombreG = "";

    ArrayList<String> spinner;
    char peticion = 'D';        // D-atos   // S-pinner  // eXtras  //E-liminar

    public static double LatitudGW, LongitudGW;
    ArrayList<Double> latG1,lonG1;
    int actual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssi_nt = (Button) findViewById(R.id.bt_rssi);
        snr_bt = (Button) findViewById(R.id.bt_snr);
        agregar_ib = (ImageButton) findViewById(R.id.ib_agregar);
        borrar_ib = (ImageButton) findViewById(R.id.ib_borrar);
        f9 = (RadioButton) findViewById(R.id.freq915);
        f8 = (RadioButton) findViewById(R.id.freq868);


        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);  
        getSupportActionBar().setDisplayShowHomeEnabled(true);  //aTRAS
        getSupportActionBar().setIcon(R.mipmap.ic_gw_foreground);

        gateway = inicial;
        lugares = (Spinner) findViewById(R.id.sp_lugar);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            llenarspinner();
        } else {
            System.out.println("No hay conexión a internet");
            Toast.makeText(MainActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }

        lugares.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int posicion, long l) {

                gateway = adapterView.getItemAtPosition(posicion).toString();
                actual = posicion-1;
                try {
                      LatitudGW = latG1.get(posicion - 1);
                      LongitudGW = lonG1.get(posicion - 1);

                    System.out.println("SELECCIONADO ---- G: " + gateway + " LA: " + LatitudGW + " LO: " + LongitudGW);
                }catch (Exception e){
                    System.out.println("ERROR EN SPINNER");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        rssi_nt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accion = "RSSI";
                validar();
            }
        });

        snr_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accion = "SNR";
                validar();
            }
        });

        agregar_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    agregar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        borrar_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (gateway.equals(inicial))
                        Toast.makeText(MainActivity.this, "No se seleccionó Gateway", Toast.LENGTH_SHORT).show();
                    else
                        ///  borrar("http://45.5.188.200:5001/spinner/" + gateway);           //VirtualBox
                        borrar("http://45.5.188.200:5000/lora-spinner?gateway="+gateway);           //Kubernetes
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void agregar() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            int REQUEST_LOCATION = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            LocationServices
                    .getFusedLocationProviderClient(getApplicationContext())
                    .getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    System.out.println("°°°°°°°° ddddddddddddd ");
                  
                    if (location != null) {
                        System.out.println("LATITUD: " + location.getLatitude() + " LONGITUD: " + location.getLongitude() + " ALTITUD: " + location.getAltitude());
                        ADubicacion(location);
                    } else {
                        Toast.makeText(MainActivity.this, "Ubicación nula, volver a intentar", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

    }


    private void ADubicacion(final Location location) {
        AlertDialog.Builder mydialogUbi = new AlertDialog.Builder(  MainActivity.this);
        mydialogUbi.setTitle("Gateway");
        mydialogUbi.setMessage("Escriba el nombre que desea agregar:");
        final EditText ubiInput = new EditText(MainActivity.this);
        ubiInput.setInputType(InputType.TYPE_CLASS_TEXT);
        mydialogUbi.setView(ubiInput);

        mydialogUbi.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                assert connectivityManager != null;
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    peticion = 'X';
                    nombreG = ubiInput.getText().toString();
                    if(nombreG.equals("")){
                        Toast.makeText(MainActivity.this, "Nombre no válido", Toast.LENGTH_SHORT).show();
                    }else {
                        latG1.add(location.getLatitude());
                        lonG1.add(location.getLongitude());
                        System.out.println("LAT: "+ latG1 +" LON: "+ lonG1);

                        ///    String myurl = "http://45.5.188.200:5001/add/" + nombreG + "/" + location.getLatitude() + "/" + location.getLongitude();     //VirtualBox
                        String myurl = "http://45.5.188.200:5000/lora-spinner-add?gateway=" + nombreG + "&latitud=" + location.getLatitude() + "&longitud=" + location.getLongitude();      //Kubernetes
                        
			System.out.println("url add: " + myurl);
                        new MainActivity.Consultar().execute(myurl);
                    }
                }else {
                    System.out.println("No hay conexión a internet");
                    Toast.makeText(MainActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
                }
            }

        });
        mydialogUbi.show();
    }

    private void borrar(String myurl) {
        peticion = 'E';

        new MainActivity.Consultar().execute(myurl);
    }

    private void llenarspinner() {
        peticion = 'S';
        spinner = new ArrayList<String>();
        spinner.clear();

         ///  String myurl = "http://45.5.188.200:5001/spinner";       //VirtualBox
        String myurl = "http://45.5.188.200:5000/lora-spinner";        //Kubernetes
        new MainActivity.Consultar().execute(myurl);

        spinner.add(inicial);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinner);
        adapter.notifyDataSetChanged();
        lugares.setAdapter(adapter);

    }

    @SuppressLint("StaticFieldLeak")            ///
    private class Consultar extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... param) {
            // params comes from the execute() call: params[0] is the url.
            try {
                consultarUrl(param[0]);
                System.out.println("doInBackGround ");
                return "";
            } catch (IOException | JSONException e) {
                System.out.println("doInBackGround CATCH!!!" + e);
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
    }

    private void consultarUrl(String myurl) throws IOException, JSONException {
        System.out.println("°°°°°°°° Inicio consultarUrl ");

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if(peticion=='E')
            conn.setRequestMethod("DELETE");
        else
            conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        System.out.println("°°°°°°°° MEDIOOOOO");
        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        System.out.println("!!!!!!!!!!!!!! " + conn.getResponseCode());

        if(peticion=='D' || peticion=='S') {
            System.out.println("PETICIÓN DIFERENTE DE eXtra");

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;

            System.out.println("!!!!!! Buffered Reader: " + br);

            while ((output = br.readLine()) != null) {
                System.out.println("OUT: " + output);


                ja = server(output);

                System.out.println("TAMAÑO JA1 = " + ja.length() + "  JSON: " + ja);
                conn.disconnect();
            }
        }

        switch (peticion) {
            case 'D':
                System.out.println("TRAER SNR - RSSI");
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                if(accion.equals("RSSI")) {
                    intent.putExtra("select","RSSI");
                }else if(accion.equals("SNR")){
                    intent.putExtra("select","SNR");
                }
                startActivity(intent);
                break;

            case 'S':
                System.out.println("LLENAR SPINNER");
                latG1 = new ArrayList<Double>();
                lonG1 = new ArrayList<Double>();

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo0;
                    try {
                        jo0 = (JSONObject) ja.get(i);
                        latG1.add(jo0.getDouble("latitud"));
                        lonG1.add(jo0.getDouble("longitud"));

                        spinner.add(jo0.getString("gateway"));
                        System.out.println("GATEWAAAAAAAAAAY - LAT: "+jo0.getDouble("latitud")+" LON: "+jo0.getDouble("longitud")+" GW: "+jo0.getString("gateway"));
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("ERROR en iteración " + i + " error: " + e);
                    }
                }
                break;

            case 'X':
                System.out.println("CASEEEE AGREGAR");                //AGREGAR NUEVO GW

                spinner.add(nombreG);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinner);
                adapter.notifyDataSetChanged();
                break;

            case 'E':
                System.out.println("CASEEEE eLIMINAR");

                spinner.remove(gateway);
                ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinner);
                adapter2.notifyDataSetChanged();

                System.out.println("ANTEEES: "+latG1+", "+lonG1);
                lonG1.remove(actual);
                latG1.remove(actual);
                System.out.println("despuEEES: "+latG1+", "+lonG1);

                break;
        }

        System.out.println("°°°°°°°° Fin consultarUrl ");
    }

    private JSONArray server(String output) throws JSONException {
        output = "{\"feeds\":"+ output +"}";
        JSONObject jo = new JSONObject(output); // JOOOOOOOOOOOOOOOOOOOOOO
        System.out.println("TAMAÑO Jo = "+jo.length() +"  JSON: "+jo);
        JSONArray jaa;                   //###
        jaa = (JSONArray) jo.get("feeds");
        return jaa;
    }


    private void validar() {
        peticion = 'D';
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            System.out.println("ITEM SELECCIONADO: " + gateway);
            if (f9.isChecked())
                frec = 915;
            if (f8.isChecked())
                frec = 868;

            if (f9.isChecked() || f8.isChecked()) {
                if(gateway.equals(inicial))
                    Toast.makeText(MainActivity.this, "Lugar no seleccionado", Toast.LENGTH_SHORT).show();
                else{
                      ///      String myurl = "http://45.5.188.200:5001/" + frec + "/" + gateway;       //VirtualBox
                    String myurl = "http://45.5.188.200:5000/lora-gateway-param?frec=" + frec + "&gate=" + gateway;     //Kubernetes
                    System.out.println("url maps: " + myurl);
                    new MainActivity.Consultar().execute(myurl);
                }
            } else
                Toast.makeText(MainActivity.this, "Frecuencia no seleccionada", Toast.LENGTH_SHORT).show();

        }else{
            System.out.println("No hay conexión a internet");
            Toast.makeText(MainActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

}
