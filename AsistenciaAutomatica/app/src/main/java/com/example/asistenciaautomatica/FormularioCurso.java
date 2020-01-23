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
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormularioCurso extends AppCompatActivity {

    private static final String TAG = "Asistente";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //vars
    private boolean mLocationPermissionGaranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String disp_Lat1;
    private String disp_Long1;
    private String disp_Lat2;
    private String disp_Long2;
    private String tutorID;
    private String estadoBateria;
    private DatabaseReference db_reference;

    //views
    private Button btn_Ubicacion1;
    private Button btn_Ubicacion2;
    private Button btn_Guardar;
    private EditText etNombreCurso;
    private EditText etDescripcion;
    private EditText etTimeRetraso;
    private EditText edtDispositivo;
    private TextView  etFecha;
    private TextView etHoraInicio;
    private TextView etHoraFin;
    private ImageButton btn_Fecha;
    private ImageButton btn_HoraInicio;
    private ImageButton btn_HoraFin;
    private CheckBox box_Retraso;
    private CheckBox box_CheckOut;
    private HashMap<String,String> eventos = new HashMap<>();
    private CheckBox box_Dispositivo;

    //variables grobales
    private  int anio,mes,dia,horas,minutos, horaFin, minFin, numDispositivo;



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
        edtDispositivo = findViewById(R.id.edtDispositivo);
        box_Retraso = findViewById(R.id.box_Retraso);
        box_CheckOut = findViewById(R.id.box_CheckOut);
        box_Dispositivo = findViewById(R.id.box_Dispositivo);
        btn_Fecha = findViewById(R.id.btn_Fecha);
        btn_HoraFin = findViewById(R.id.btn_HoraFin);
        btn_HoraInicio = findViewById(R.id.btn_HoraInicio);

        btn_Ubicacion1 = findViewById(R.id.btn_Ubicacion1);
        btn_Ubicacion2 = findViewById(R.id.btn_Ubicacion2);
        btn_Guardar = findViewById(R.id.btn_Guardar);
        tutorID = getIntent().getStringExtra("tutorID");

        iniciarBaseDeDatos();
        leerEventos();

    }

    /**
    Se recorre la sesion Eventos de la base de datos para extrar todos los nombre y id de eventos registrados
    para validar que sean unicos. Se implementa la accion de los botones de fecha, horaInicio y horaFin que muestran
    un Date y Time Picker Dialog para la sellecion de la fehca y hora del evento. De igual forma para la accion de los
    botones de ubicacion1 y ubicacion 2 para seleccionar la zona del evento.
     */
    private void leerEventos(){
        DatabaseReference db_evento = db_reference.child("Evento");

        db_evento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                    if (data!= null) {
                        eventos.put(snapshot.getKey(), data.get("Nom_evento"));
                    }
                }

                btn_Fecha.setOnClickListener(v -> {

                    final Calendar c = Calendar.getInstance();
                    anio=c.get(Calendar.YEAR);
                    mes=c.get(Calendar.MONTH);
                    dia=c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePicker = new DatePickerDialog(FormularioCurso.this, (view, year, month, dayOfMonth) -> etFecha.setText(year+"/"+(month+1)+"/"+dayOfMonth),anio,mes,dia);

                    datePicker.show();

                });

                btn_HoraInicio.setOnClickListener(v -> {

                    Date date = new Date();
                    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    String[] hora_actual = hourFormat.format(date).split(":");
                    horas = Integer.valueOf(hora_actual[0]);
                    minutos = Integer.valueOf(hora_actual[1]);

                    TimePickerDialog ponerhora= new TimePickerDialog(FormularioCurso.this, (view, hourOfDay, minute) -> etHoraInicio.setText(hourOfDay+":"+minute),horas,minutos,true);


                    ponerhora.show();
                });
                btn_HoraFin.setOnClickListener(v -> {

                    Date date = new Date();
                    DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    String[] hora_actual = hourFormat.format(date).split(":");
                    horaFin = Integer.valueOf(hora_actual[0]);
                    minFin = Integer.valueOf(hora_actual[1]);

                    TimePickerDialog ponerhoraFin= new TimePickerDialog(FormularioCurso.this, (view, hourOfDay, minute) -> {

                        String horaFinal = hourOfDay+":"+minute;
                        CompararTiempo(etHoraInicio.getText().toString(), horaFinal);

                    },horaFin,minFin,true);

                    ponerhoraFin.show();
                });

                btn_Ubicacion1.setOnClickListener(v -> {
                    if (isServiceOk()) {
                        numDispositivo = 1;
                        if (box_Dispositivo.isChecked()) {
                            String name_disp=edtDispositivo.getText().toString();
                            dispositivoGPS(name_disp);
                        }else {
                            getLocationPermission();
                        }
                    }
                });
                btn_Ubicacion2.setOnClickListener(v -> {
                    if (isServiceOk()) {
                        numDispositivo = 2;
                        if (box_Dispositivo.isChecked()) {
                            String name_disp= edtDispositivo.getText().toString();
                            dispositivoGPS(name_disp);
                        }else {
                            getLocationPermission();
                        }
                    }
                });


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
            }
        });
    }

    /*
    El metodo dispositivoGPS requiere del parametro nameDisp para obtener los datos del gps del dispositivo IOT
    cuyos datos se encuentran en la seccion Registros cuyos id's son el nombre o codigo unico del dispositivo.
    Solo se implementa este metodo cuando se selecciona el checkBox box_Dispositivo.
     */
    private void dispositivoGPS(String nameDisp){
        DatabaseReference db_dispositivo = db_reference.child("Registros");

        db_dispositivo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean existe = false;
                HashMap<String, String> info_disp = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot!=null && snapshot.getKey().equals(edtDispositivo.getText().toString())){
                        System.out.println(nameDisp+"hola");
                        info_disp =(HashMap<String, String>) snapshot.getValue();
                        existe = true;
                        break;
                    }
                }
                if (existe && info_disp!=null) {
                    System.out.println(existe);
                    DatabaseReference db_dispo = db_dispositivo.child(nameDisp);
                    db_dispo.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, String> info_dps = null;
                            String key = null;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                info_dps =(HashMap<String, String>) dataSnapshot.getValue();
                                key=snapshot.getKey();
                                break;
                            }
                            if (info_dps!=null && key!=null) {

                                DatabaseReference base = db_dispo.child(key);
                                base.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        HashMap<String, String> info =null;
                                        HashMap<String, String> punto =null;
                                        Boolean nulidad = false;
                                        String keydato = null;
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            info = (HashMap<String, String>) dataSnapshot.getValue();
                                            if (info!=null) {
                                                if (!info.get("lat").equals("0")) {
                                                    punto = (HashMap<String, String>) dataSnapshot.getValue();
                                                    nulidad = true;
                                                    keydato = dataSnapshot.getKey();
                                                    System.out.println(keydato);
                                                    break;
                                                }
                                                System.out.println("no existe");
                                            }
                                        }
                                        if (punto!=null && nulidad && keydato!=null){
                                            System.out.println(info);
                                            System.out.println("aki");
                                            if (numDispositivo == 1) {
                                                disp_Lat1 = punto.get("lat");
                                                disp_Long1 = punto.get("long");
                                                estadoBateria = punto.get("estBateria");
                                                System.out.println(numDispositivo);
                                                System.out.println(numDispositivo);
                                                System.out.println(disp_Lat1 + "-" + disp_Long1);
                                                System.out.println(disp_Lat2 + "-" + disp_Long2);
                                                Toast.makeText(FormularioCurso.this, "La bateria de su dispositivo es: " + estadoBateria, Toast.LENGTH_SHORT).show();
                                                DatabaseReference eliminar = db_reference.child("Registros").child(nameDisp);
                                                eliminar.child(keydato).removeValue();
                                            }
                                            if (numDispositivo == 2) {
                                                disp_Lat2 = punto.get("lat");
                                                disp_Long2 = punto.get("long");
                                                estadoBateria = punto.get("estBateria");
                                                System.out.println(numDispositivo);
                                                System.out.println(disp_Lat1 + "-" + disp_Long1);
                                                System.out.println(disp_Lat2 + "-" + disp_Long2);
                                                Toast.makeText(FormularioCurso.this, "La bateria de su dispositivo es: " + estadoBateria, Toast.LENGTH_SHORT).show();
                                                DatabaseReference eliminar = db_reference.child("Registros").child(nameDisp);
                                                eliminar.child(keydato).removeValue();
                                            }
                                            Guardar();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "Error!", databaseError.toException());
                                    }
                                });

                            }else{
                                System.out.println("falla disp");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error!", databaseError.toException());
                        }
                    });

                }else{
                    Toast.makeText(FormularioCurso.this, "El codigo o nombre del dispositivo ingresado no existe.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error!", databaseError.toException());
            }
        });
    }

    /**
    El metodo CompararTiempo toma dos parametros, la HoraInicio y la HoraFin del evento para compararlas entre si
    para validar que la hora final no sea menor a la hora incial del evento y se produzca un error en los registros de
    los estudiantes.
     */
    private void CompararTiempo(String HoraInicio, String HoraFin){
        String[] TiempoInicial = HoraInicio.split(":");
        int horaI = Integer.valueOf(TiempoInicial[0]);
        int minuteI = Integer.valueOf(TiempoInicial[1]);

        String[] TiempoFinal = HoraFin.split(":");
        int horaF = Integer.valueOf(TiempoFinal[0]);
        int minuteF = Integer.valueOf(TiempoFinal[1]);

        if (horaF >= horaI){
            if (horaF == horaI && minuteF<=minuteI) {
                Toast.makeText(getApplicationContext(), "La Hora de finalizacion no puede ser menor a la Hora de Inicio.", Toast.LENGTH_SHORT).show();
            }
            else{
                etHoraFin.setText(HoraFin);
            }

        }else{
            Toast.makeText(getApplicationContext(), "La Hora de finalizacion no puede ser menor a la Hora de Inicio.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     El metodo iniciarBase de datos permite establecer desde el inicio la referencia base
     que se utilizara para navegar por la base de datos de firebase.
     */
    private void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

    }

    /**
    Se implementa la funcion del boton guardar que verifica que los campos no esten vacios y que el nombre del evento
    no sea repetido para poder subir la informacion llamando a los respectivos metodos de subirDispositivo(), subirLista(),
    y subirFormularioCurso, finalmente se vuelve a la anterior activity de Tutor.
     */
    private void Guardar(){
        btn_Guardar.setOnClickListener(v -> {
            if ( Conectividad()) {

                if (disp_Lat1 != null && disp_Long1 != null && disp_Lat2 != null && disp_Long2 != null) {
                    if (etNombreCurso.getText() != null && etFecha.getText() != null && etHoraInicio.getText() != null) {
                        if (!eventos.containsValue(etNombreCurso.getText().toString())) {
                            if (box_Retraso.isChecked() && etTimeRetraso.getText() != null) {
                                subirFormularioCurso(etNombreCurso.getText().toString(), tutorID, etDescripcion.getText().toString(), etFecha.getText().toString(),
                                        etHoraInicio.getText().toString(), etHoraFin.getText().toString(), etTimeRetraso.getText().toString(), box_Retraso.isChecked(), box_CheckOut.isChecked());

                                subirDispositivo();
                                subirLista();
                                finish();
                            } else if (box_Retraso.isChecked() && etTimeRetraso == null) {
                                Toast.makeText(getApplicationContext(), " Ingrese tiempo de atraso, para continuar.", Toast.LENGTH_SHORT).show();
                            } else {
                                subirFormularioCurso(etNombreCurso.getText().toString(), tutorID, etDescripcion.getText().toString(), etFecha.getText().toString(),
                                        etHoraInicio.getText().toString(), etHoraFin.getText().toString(), "0", box_Retraso.isChecked(), box_CheckOut.isChecked());

                                subirDispositivo();
                                subirLista();
                                finish();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "Nombre de evento ya existe, ingrese otro nombre de evento.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), " Porfavor ingrese todos los campos obligatorios (*).", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(FormularioCurso.this, "No se pudo marcar la zona de asistencia. Revise su conexion a internet y marcar de nuevo la zona.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(FormularioCurso.this, "No dispone de conexion a internet.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
    El metodo subirFormularioCurso recibe los @parametros: nom_evento, tutor, descripcion, Fecha, horaInicio, horaFin,
    minRetraso, Retraso, Checkout para subir el nuevo evento o curso a la base de datos en Firebase en la sesion Evento.
     */
    private void subirFormularioCurso(String nom_evento, String tutor, String descripcion, String Fecha, String horaInicio, String horaFin, String minRetraso, Boolean Retraso, Boolean CheckOut){
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

    /**
    Se suben los datos a la lista de asistencias que se encuentra en la sesion Asistencias en la base de datos de
    firebase, se suben los campos del nombre del evento, la fecha, y una lista vacia que cotendra los asistentes al evento.
     */
    private void subirLista(){
        DatabaseReference db_lista = db_reference.child("Asistencias");
        HashMap<String, String> dataLista = new HashMap<String, String>();
        dataLista.put("evento",etNombreCurso.getText().toString());
        dataLista.put("fecha", etFecha.getText().toString());
        dataLista.put("lista", "-");
        db_lista.push().setValue(dataLista);
    }

    /**
    El metodo subirDispositivo permite subir las coordenadas de la zona del evento anteriormente marcada
    a la base de datos con el respectivo nombre del evento en l sesion Dispositivos.
     */
    private void subirDispositivo(){
        DatabaseReference subir_data = db_reference.child("Dispositivo");
        Map<String, String> dataDispositivo = new HashMap<String, String>();
        dataDispositivo.put("Evento", etNombreCurso.getText().toString());
        dataDispositivo.put("Latitud1", disp_Lat1);
        dataDispositivo.put("Longitud1", disp_Long1);
        dataDispositivo.put("Latitud2", disp_Lat2);
        dataDispositivo.put("Longitud2", disp_Long2);
        subir_data.push().setValue(dataDispositivo);
    }

    /**
     Devuelve un valor tipo Bool indicando si hay o no conectividad del dispositivo con alguna red de internet.
     */
    private boolean Conectividad(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return  false;
        }
    }

    /**
     Verifica si el servicio de google service esta activo para el correcto funcionamiento de las API's
     de google utilizadas como geolocalizacion, googleAccount.
     */
    private boolean isServiceOk(){
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

    /**
    Asistir() permite obtener las coordenadas de latitud y longitud del dispositivo en ese
    instante y los sobre-escribe en el txt_Latitud y txt_longitud de la interfaz, si se produce un error
    mandara una ioException o un mensaje de que la localizacion no se encuentra o es nula.
     */
    private void getDeviceLocation(){

        Log.d(TAG, "getDeviceLocation: getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGaranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        System.out.println(currentLocation);
                        if (currentLocation !=null) {
                            if (numDispositivo==1) {
                                disp_Lat1 = Double.toString(currentLocation.getLatitude());
                                disp_Long1 = Double.toString(currentLocation.getLongitude());
                            }
                            if (numDispositivo==2) {
                                disp_Lat2 = Double.toString(currentLocation.getLatitude());
                                disp_Long2 = Double.toString(currentLocation.getLongitude());
                            }
                        }
                        System.out.println(disp_Lat1+"-"+disp_Long1);
                        System.out.println(disp_Lat2+"-"+disp_Long2);
                        Guardar();

                    }else{
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(FormularioCurso.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    /**
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

    /**
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
