package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PublicKey;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FormularioCurso extends AppCompatActivity {

    private static final String TAG = "Asistente";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //vars
    private boolean mLocationPermissionGaranted = false;
    public FusedLocationProviderClient mFusedLocationProviderClient;
    public String disp_Lat1, disp_Long1, disp_Lat2, disp_Long2;
    public String ref_latitud, ref_longitud; //variables de referencia para guiar los datos a sus respectivas variables.
    public String tutorID;
    DatabaseReference db_reference;

    //views
    public Button btn_Ubicacion1, btn_Ubicacion2, btn_Guardar;
    public EditText etNombreCurso, etDescripcion, etTimeRetraso;
    public TextView  etFecha, etHoraInicio, etHoraFin;
    public Button btn_Fecha, btn_HoraInicio, btn_HoraFin;
    public CheckBox box_Retraso, box_CheckOut;

    //variables grobales
    private  int year,mes,dia,horas,minutos, horaFin, minFin ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_curso);

        etNombreCurso = findViewById(R.id.etNombreCurso);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        etTimeRetraso = findViewById(R.id.etTimeRetraso);
        box_Retraso = findViewById(R.id.box_Retraso);
        box_CheckOut = findViewById(R.id.box_CheckOut);
        btn_Fecha = findViewById(R.id.btn_Fecha);
        btn_HoraFin = findViewById(R.id.btn_HoraFin);
        btn_HoraInicio = findViewById(R.id.btn_HoraInicio);

        btn_Ubicacion1 = findViewById(R.id.btn_Ubicacion1);
        btn_Ubicacion2 = findViewById(R.id.btn_Ubicacion2);
        btn_Guardar = findViewById(R.id.btn_Guardar);
        tutorID = getIntent().getStringExtra("tutorID");

        /*
        if(isServiceOk()){
            getLocationPermission();
        }*/

        btn_Fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // se obtiene la fecha actual del sistema
                final Calendar c = Calendar.getInstance();
                // se captura el año el mes y el dia de la fecha actual en tres variables tipo int
                year=c.get(Calendar.YEAR);
                mes=c.get(Calendar.MONTH);
                dia=c.get(Calendar.DAY_OF_MONTH);

                //creamos el dialogDatePicker
                DatePickerDialog datePicker = new DatePickerDialog(FormularioCurso.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etFecha.setText(year+"/"+month+"/"+dayOfMonth);
                    }
                },year,mes,dia);

                // se muestra el cuadro de dialogo
                datePicker.show();

            }
        });


        // colocamos el boton poner hora a la escucha

        btn_HoraInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // lo mismo que el anterior solo que en este se establece la hora actual
                final Calendar c = Calendar.getInstance();

                horas=c.get(Calendar.HOUR_OF_DAY);
                minutos=c.get(Calendar.MINUTE);

                // creamos el DialogTimePicker
                TimePickerDialog ponerhora= new TimePickerDialog(FormularioCurso.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        etHoraInicio.setText(hourOfDay+":"+minute);
                    }
                },horas,minutos,true);

                //mostramos el elemento Timer como una ventana de dialogo
                ponerhora.show();
            }
        });
        btn_HoraFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // lo mismo que el anterior se establece la hora actual
                final Calendar c = Calendar.getInstance();

                horaFin=c.get(Calendar.HOUR_OF_DAY);
                minFin=c.get(Calendar.MINUTE);

                // creamos el DialogTimePicker
                TimePickerDialog ponerhoraFin= new TimePickerDialog(FormularioCurso.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        String horaFinal = hourOfDay+":"+minute;

                        CompararTiempo(etHoraInicio.getText().toString(), horaFinal);

                        etHoraFin.setText(horaFinal);
                    }
                },horaFin,minFin,true);

                //mostramos el elemento Timer como una ventana de dialogo
                ponerhoraFin.show();
            }
        });

        iniciarBaseDeDatos();

    }

    public void CompararTiempo(String HoraInicio, String HoraFin){
        String[] TiempoInicial = HoraInicio.split(":");
        int horaI = Integer.valueOf(TiempoInicial[0]);
        int minuteI = Integer.valueOf(TiempoInicial[1]);

        String[] TiempoFinal = HoraInicio.split(":");
        int horaF = Integer.valueOf(TiempoFinal[0]);
        int minuteF = Integer.valueOf(TiempoFinal[1]);

        System.out.println(TiempoInicial);
        System.out.println(TiempoFinal);
        while (horaI >= horaF){
            Toast.makeText(getApplicationContext(), "La Hora de finalizacion no puede ser menor a la Hora de Inicio.", Toast.LENGTH_SHORT).show();

        }
        if (horaI == horaF){
            while (minuteI >= minuteF){
                Toast.makeText(getApplicationContext(), "La Hora de finalizacion no puede ser menor a la Hora de Inicio.", Toast.LENGTH_SHORT).show();
            }

        }

    }


    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

    }

    public void Guardar(View view){

        if ( Conectividad()) {

            if (disp_Lat1 != null && disp_Long1 != null && disp_Lat2 != null && disp_Long2 != null) {
                if (etNombreCurso.getText() != null && etFecha.getText() != null && etHoraInicio.getText() != null) {
                    if (box_Retraso.isChecked() && etTimeRetraso.getText() != null) {
                        subirFormularioCurso(etNombreCurso.getText().toString(), tutorID, etDescripcion.getText().toString(), etFecha.getText().toString(),
                                etHoraInicio.getText().toString(), etHoraFin.getText().toString(), etTimeRetraso.getText().toString(), box_Retraso.isChecked(), box_CheckOut.isChecked());


                        subirDispositivo();
                        Intent intent = new Intent(this, Tutor.class);
                        startActivity(intent);
                    } else if (box_Retraso.isChecked() && etTimeRetraso == null) {
                        Toast.makeText(getApplicationContext(), " Ingrese tiempo de atraso, para continuar.", Toast.LENGTH_SHORT).show();
                    } else {
                        subirFormularioCurso(etNombreCurso.getText().toString(), tutorID, etDescripcion.getText().toString(), etFecha.getText().toString(),
                                etHoraInicio.getText().toString(), etHoraFin.getText().toString(), "1000", box_Retraso.isChecked(), box_CheckOut.isChecked());

                        subirDispositivo();
                        Intent intent = new Intent(this, Tutor.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), " Porfavor ingrese todos los campos obligatorios (*).", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(this, "No se pudo marcar la zona de asistencia. Revise su conexion a internet y marcar de nuevo la zona.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "No dispone de conexion a internet.", Toast.LENGTH_SHORT).show();
        }

    }

    public void subirFormularioCurso(String nom_evento, String tutor,String descripcion, String Fecha, String horaInicio, String horaFin, String minRetraso, Boolean Retraso, Boolean CheckOut){
        DatabaseReference subir_data = db_reference.child("Evento");

        Map<String, String> dataCurso = new HashMap<String, String>();
        dataCurso.put("Nom_evento", nom_evento);
        dataCurso.put("Descripcion", descripcion);
        dataCurso.put("Fecha", Fecha);
        dataCurso.put("horaInicio",horaInicio);
        dataCurso.put("horaFin",horaFin);
        dataCurso.put("Retraso",Retraso.toString());
        dataCurso.put("Marcar_Salida", CheckOut.toString());
        dataCurso.put("minRetraso",minRetraso);
        dataCurso.put("tutorID",tutor);

        subir_data.push().setValue(dataCurso);
    }

    public void subirDispositivo(){
        DatabaseReference subir_data = db_reference.child("Dispositivo");
        Map<String, String> dataDispositivo = new HashMap<String, String>();
        dataDispositivo.put("Latitud1", disp_Lat1);
        dataDispositivo.put("Longitud1", disp_Long1);
        dataDispositivo.put("Latitud2", disp_Lat2);
        dataDispositivo.put("Longitud2", disp_Long2);

        subir_data.push().setValue(dataDispositivo);
    }

    public void ubicarZona(View view){
        if(view.getId() == R.id.btn_Ubicacion1){
            getDeviceLocation();
            if (ref_latitud!=null) {
                disp_Lat1 = ref_latitud;
                disp_Long1 = ref_longitud;
            }
        }
        if(view.getId() == R.id.btn_Ubicacion2){
            getDeviceLocation();
            if (ref_latitud!=null) {
                disp_Lat2 = ref_latitud;
                disp_Long2 = ref_longitud;
            }
        }
    }

    public boolean Conectividad (){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
            // Si hay conexión a Internet en este momento
        } else {
            return  false;
            // No hay conexión a Internet en este momento
        }
    }


    public boolean isServiceOk(){
        Log.d(TAG, "isServiceOk: checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FormularioCurso.this);

        if (available == ConnectionResult.SUCCESS){
            //Everything is fine and the user can make map request
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error ocured but we can resolt it
            Log.d(TAG, "isServiceOk: an error occures but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FormularioCurso.this, available, ERROR_DIALOG_REQUEST);
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

                            ref_latitud = Double.toString(currentLocation.getLatitude());
                            ref_longitud = Double.toString(currentLocation.getLongitude());

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(FormularioCurso.this, "unable to get current location", Toast.LENGTH_SHORT).show();
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
