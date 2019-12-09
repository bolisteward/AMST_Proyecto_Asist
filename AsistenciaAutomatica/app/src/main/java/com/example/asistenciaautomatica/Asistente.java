package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Asistente extends AppCompatActivity {
    private static final String TAG = "Asistente";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //vars
    private boolean mLocationPermissionGaranted = false;
    public FusedLocationProviderClient mFusedLocationProviderClient;

    //views
    HashMap<String, String> info_user;
    public TextView txt_nombre, txt_correo, txt_phone,  txt_Latitud, txt_Longitud;
    public ImageView img_foto;
    public String userId;
    public Spinner spinner;
    public String[] eventos;
    DatabaseReference db_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistente);

        txt_Latitud = findViewById(R.id.txt_Latitud);
        txt_Longitud = findViewById(R.id.txt_Longitud);
        txt_nombre = findViewById(R.id.txt_nombre);
        txt_correo = findViewById(R.id.txt_correo);
        txt_phone = findViewById(R.id.txt_phone);
        img_foto = findViewById(R.id.img_foto);
        spinner = findViewById(R.id.spinner);


        Intent intent = getIntent();
        info_user = (HashMap<String, String>) intent.getSerializableExtra("info_user");

        txt_nombre.setText(info_user.get("user_name"));
        txt_phone.setText(info_user.get("user_phone"));
        txt_correo.setText(info_user.get("user_email"));
        userId = info_user.get("user_id");
        String photo = info_user.get("user_photo");
        Picasso.get().load(photo).resize(300,300).error(R.drawable.usuario).into(img_foto);



        iniciarBaseDeDatos();
        leerBaseDatos();
        //leerDispositivo();

    }

    public void cerrarSesion(View view) {
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    public void iniciarBaseDeDatos() {
        db_reference = FirebaseDatabase.getInstance().getReference();
    }

    public void leerBaseDatos(){
        db_reference.child("Asistente").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getChildren();
                    if (data.get("ID")== userId ){
                        updateAsistente(info_user.get("user-name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_Latitud.getText().toString(), txt_Longitud.getText().toString());
                    }
                    else{
                        subirAsistente(info_user.get("user-name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_Latitud.getText().toString(), txt_Longitud.getText().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }

    public void updateAsistente(String nombre, String phone, String remoteID, String correo, String lat, String lon) {

        DatabaseReference subir_data = db_reference.child("Asistente").child(remoteID);
        subir_data.child(remoteID).child("Nombre").setValue(nombre);
        subir_data.child(remoteID).child("Correo").setValue(correo);
        subir_data.child(remoteID).child("Telefono").setValue(phone);
        subir_data.child(remoteID).child("Latitud").setValue(lat);
        subir_data.child(remoteID).child("Longitud").setValue(lon);

    }

    public void subirAsistente(String nombre, String phone, String userID, String correo, String lat, String lon) {

        DatabaseReference subir_data = db_reference.child("Asistente");

        Map<String, String> dataUser = new HashMap<String, String>();
        dataUser.put("Nombre", nombre);
        dataUser.put("Correo", correo);
        dataUser.put("Telefono", phone);
        dataUser.put("Latitud", lat);
        dataUser.put("Longitud", lon);
        dataUser.put("ID",userID);

        subir_data.push().setValue(dataUser);
    }

    public void Asistir (View view){
        subirAsistencia(userId);
    }

    public void subirAsistencia(String userID) {
        String ID = userID;
        boolean[] verifica = verifica_Asistencia();
        boolean presente = verifica[0];
        boolean retraso = verifica[1];
        DatabaseReference subir_data = db_reference.child("Asistencias").child(userID);
        subir_data.setValue(ID);
        subir_data.child(ID).child("Asistencia").setValue(presente);
        subir_data.child(ID).child("Retraso").setValue(retraso);
        System.out.println("Data Asistente subido");
    }

    public boolean[] verifica_Asistencia (){
        boolean presente =false;
        boolean retraso = false;
        int user_lat = Integer.parseInt (txt_Latitud.getText().toString());
        int user_long = Integer.parseInt (txt_Longitud.getText().toString());
        if ((user_lat <= (Integer.parseInt(disp_Lat)+0.00010)) && (user_long <= (Integer.parseInt(disp_Long)+0.00010))){
            presente = true;
        }
        if (!presente){
            Toast.makeText(getApplicationContext(),"Se encuentra fuera del rango",Toast.LENGTH_SHORT).show();
        }
        boolean verifica[] = {presente, retraso};
        return verifica;
    }

    public void leerDispositivo(){
        db_reference.child("Dispositivo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getChildren();
                    if (data.get("Evento")== evento )
                    disp_Lat = data.get("Latitud");
                    disp_Long = data.get("Longitud");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }

    public void leerEventos(){
        db_reference.child("Evento").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getChildren();
                    if (data.get("ID")== userId ){
                        updateAsistente(info_user.get("user-name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_Latitud.getText().toString(), txt_Longitud.getText().toString());
                    }
                    else{
                        subirAsistente(info_user.get("user-name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_Latitud.getText().toString(), txt_Longitud.getText().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }
    /*
    public void leerDispositivo(){
        db_reference.child("Dispositivo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getChildren();
                    System.out.println(data);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }*/

    public boolean isServiceOk(){
        Log.d(TAG, "isServiceOk: checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Asistente.this);

        if (available == ConnectionResult.SUCCESS){
            //Everything is fine and the user can make map request
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error ocured but we can resolt it
            Log.d(TAG, "isServiceOk: an error occures but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Asistente.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /*
    Asistir() permite obtener las coordenadas de latitud y longitud del dispositivo en ese
    instante y los sobre-escribe en el txt_Latitud y txt_longitud de la interfaz, si se produce un error
    mandara una ioException o un mensaje de que la localizacion no se encuentra o es nula.
     */

    public void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGaranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            txt_Latitud.setText(Double.toString(currentLocation.getLatitude()));
                            txt_Longitud.setText(Double.toString(currentLocation.getLongitude()));

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(Asistente.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    /*
    EL metodo getLocationPermission() verifica que los permisos y privilegios hayan sido aceptado,
    de no ser asi llama al metodo onResquestPermissionsResults para solicitarlos.
     */

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permission.");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGaranted = true;
                getDeviceLocation();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /*
    onRequestPermissionResult() permite solicitar al usuario los permisos de poder
    utilizar su ubicacion otorgandole a la app los privilegios para obtener las datos de
    latitud y longitud. De no ser asi, mostrara un mensaje de falla o no concedido.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGaranted = false;
        Log.d(TAG, "onRequestPermissionsResult: called.");
        switch ( requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length >0){
                    for (int i =0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGaranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: failed.");
                            return;
                        }
                    }
                    mLocationPermissionGaranted =true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted.");
                    //obtener la localizacion
                    getDeviceLocation();
                }
            }
        }
    }





}
