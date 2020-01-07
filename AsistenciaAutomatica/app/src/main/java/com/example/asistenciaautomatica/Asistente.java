package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.security.AccessController.getContext;

public class Asistente extends AppCompatActivity{
    private static final String TAG = "Asistente";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //vars
    private boolean mLocationPermissionGaranted = false;
    public FusedLocationProviderClient mFusedLocationProviderClient;

    //views
    Bundle info_user;
    Button btn_Asistir, btn_salida, btn_historial;
    public TextView txt_nombre, txt_correo, txt_phone,  txt_Latitud, txt_Longitud, txt_matricula;
    public ImageView img_foto;
    public String userId, disp_Lat1, disp_Long1, disp_Lat2, disp_Long2, name_evento;
    public String idEvento, horaActual, idLista;
    public Spinner spinner;
    public Boolean nuevoAsist;
    public List<String> eventos;
    public Users asistente;
    private  int anio,mes,dia,horas,minutos;

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
        txt_matricula = findViewById(R.id.txt_matricula);
        img_foto = findViewById(R.id.img_foto);
        spinner = findViewById(R.id.spinner);
        btn_Asistir = findViewById(R.id.btn_Asistir);
        btn_salida = findViewById(R.id.btn_salida);
        btn_historial = findViewById(R.id.btn_historial);


        iniciarBaseDeDatos();
        leerBaseDatos();

        leerEventos();
        Asistir();
        btn_historial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historialDialog().show();
            }
        });

    }

    /*
    El metodo newAsist permite subir los datos obtenidos de la cuenta de google con el que el usuario
    inicia sesion, implemente el metodo getLocationPermission() para obtener la ubicacion y pide al usuario que
    ingrese el numero de matricula mediante un cuadro de dialogo.
     */
    public void newAsist() {
        info_user = getIntent().getBundleExtra("info_user");
        if (info_user != null) {
            txt_nombre.setText(info_user.getString("user_name"));
            txt_phone.setText(info_user.getString("user_phone"));
            txt_correo.setText(info_user.getString("user_email"));
            userId = info_user.getString("user_id");
            String photo = info_user.getString("user_photo");
            Picasso.get().load(photo).resize(300, 300).error(R.drawable.usuario).into(img_foto);


            asistente = new Users(info_user.getString("user_name"), info_user.getString("user_email"), info_user.getString("user_phone"), info_user.getString("user_id"));

            DatabaseReference db_upload = db_reference.child("Asistente");

            db_upload.child(userId).setValue(asistente);

            if (isServiceOk()) {
                getLocationPermission();

            }
            createCustomDialog().show();
        }
    }

    /*
    Se crea un cuadro de dialogo de tipo AlertDialog el cual utuliza el archivo matricula.xml como interfaz grafica.
    Se obtiene el dato ingresado y es subido directamente a la base de datos del usuario registrado.
     */
    public AlertDialog createCustomDialog() {
        final AlertDialog alertDialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflar y establecer el layout para el dialogo
        // Pasar nulo como vista principal porque va en el diseño del diálogo
        View v = inflater.inflate(R.layout.matricula, null);
        //builder.setView(inflater.inflate(R.layout.dialog_signin, null))
        EditText edtMatricula = v.findViewById(R.id.edtMatricula);
        Button btn_aceptar = v.findViewById(R.id.btn_aceptar);
        builder.setView(v);
        alertDialog = builder.create();
        // Add action buttons
        btn_aceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        txt_matricula.setText(edtMatricula.getText().toString());

                        DatabaseReference db_upload = FirebaseDatabase.getInstance().getReference().child("Asistente").child(userId);
                        db_upload.child("matricula").setValue(edtMatricula.getText().toString());

                        alertDialog.dismiss();
                    }
                }

        );
        return alertDialog;
    }

    /*
    Cuando el ususario ya existe se implementa el metodo presentarDatos, el cual permite tomar los datos del usuario
    de la base de datos y cargarlos en los respectivos TextView's del archivo asistente.xml. Unicamente la ubicacion
    se actualiza.
     */
    public void presentarDatos(){
        info_user = getIntent().getBundleExtra("info_user");

        if (info_user != null) {
            userId = info_user.getString("user_id");
            String photo = info_user.getString("user_photo");
            Picasso.get().load(photo).resize(300, 300).error(R.drawable.usuario).into(img_foto);

            if (isServiceOk()) {
                getLocationPermission();
            }
        }
        DatabaseReference db_asist = db_reference.child("Asistente").child(userId);
        db_asist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users usr = dataSnapshot.getValue(Users.class);
                if (usr!=null) {
                    txt_nombre.setText(usr.getNombre());
                    txt_correo.setText(usr.getCorreo());
                    txt_phone.setText(usr.getTelefono());
                    txt_matricula.setText(usr.getMatricula());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error!", databaseError.toException());
            }
        });

    }

    /*
    Se cierra la sesion de la cuenta google con la cual ingreso el usuario y es enviado directamente a la MainActivity
     */
    public void cerrarSesion(View view) {
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    /*
    El metodo iniciarBase de datos permite establecer desde el inicio la referencia base
    que se utilizara para navegar por la base de datos de firebase.
     */
    public void iniciarBaseDeDatos() {
        db_reference = FirebaseDatabase.getInstance().getReference();
    }

    /*
    Se recorre la base de datos en firebase en la sesion Asistente para determinar si el usuario que ingresa
    es nuevo o ya ha ingresado anteriormente. Segun el caso, se llamara al respectivo metodo.
     */
    public void leerBaseDatos(){
        DatabaseReference asistente = db_reference.child("Asistente");

        asistente.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nuevoAsist = true;
                info_user = getIntent().getBundleExtra("info_user");

                if (info_user!=null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                        if (data != null) {
                            String userId = data.get("idUser");

                            if (userId.equals(info_user.getString("user_id"))) {
                                nuevoAsist = false;
                                presentarDatos();

                                break;
                            }
                        }
                    }
                }
                if (nuevoAsist){
                    newAsist();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
                System.out.println(error.getMessage());
            }
        });
    }

    /*
    El metodo marcarSalida() implemente la accion del boton de marcar salida del estudiante, en el se verifica
    el evento que ha seleccionado de la lista en el spinner y luego se compara y verifican la hora del asistente
    con respecto a la hora de inicio y finalizacion del evento para obtener la cant de horas asistidas.
     */
    public void marcarSalida(){
        btn_salida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name_evento!=null) {
                    Boolean presente  = verifica_Asistencia();
                    if (presente) {
                        DatabaseReference db_mSalida = db_reference.child("Evento");

                        db_mSalida.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                                    if (data != null) {
                                        if (data.get("Nom_evento").equals(name_evento)) {
                                            DatabaseReference db_lista = db_reference.child(snapshot.getKey()).child("lista").child(userId);

                                            Calendar calendar = Calendar.getInstance();
                                            int horas=calendar.get(Calendar.HOUR_OF_DAY);
                                            int minutos=calendar.get(Calendar.MINUTE);
                                            String horaFin= horas+":"+minutos;

                                            db_lista.child("horaFin").setValue(horaFin);

                                            String[] horaInicio = data.get("horaInicio").split(":");

                                            if (Integer.parseInt(horaInicio[0]) == horas) {
                                                Double numHoras = cantHoras(0, Integer.parseInt(horaInicio[1]));
                                                db_lista.child("numHoras").setValue(numHoras);
                                            }else{
                                                int horasPresente = horas - Integer.parseInt(horaInicio[0]);
                                                if (minutos > Integer.parseInt(horaInicio[1])) {
                                                    int min = minutos - Integer.parseInt(horaInicio[1]);
                                                    Double numHoras = cantHoras(horasPresente, min);
                                                    db_lista.child("numHoras").setValue(numHoras);
                                                }else {
                                                    int min = 60 + minutos - Integer.parseInt(horaInicio[1]);
                                                    Double numHoras = cantHoras(horasPresente-1, min);
                                                    db_lista.child("numHoras").setValue(numHoras);
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Error!", databaseError.toException());
                            }
                        });
                    }else{
                        Toast.makeText(Asistente.this,"Se encuentra fuera de la zona del evento: " + name_evento, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Asistente.this,"Seleccione un evento o curso primero." + name_evento, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /*
    Devuelve un Double con la cantidad de horas ingresando los parametros horas y minutos.
     */
    public double cantHoras(int horas, int minutos){
        double numHoras;
        numHoras = horas+(minutos*100/60);
        return numHoras;
    }

    /*
    Crea un Alert Dialog utilizando con Interfaz grafica historial_asistencias.xml, en el se presenta
    todos los eventos que el usuario ha podido asistir, el boton aceptar es utilizado para salir del
    cuadro de dialogo.
     */
    public AlertDialog historialDialog() {
        final AlertDialog alertDialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflar y establecer el layout para el dialogo
        // Pasar nulo como vista principal porque va en el diseño del diálogo
        View v = inflater.inflate(R.layout.historial_asistencias, null);

        LinearLayout contEvents = v.findViewById(R.id.contEventos);

        info_user = getIntent().getBundleExtra("info_user");
        if (info_user!= null) {

            userId = info_user.getString("user_id");

            try {
                DatabaseReference db_historial = db_reference.child("Asistente").child(userId).child("listEventos");

                db_historial.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                            TextView asitEvento = new TextView(getApplicationContext());
                            String mensaje = "*" + snapshot.getValue(Boolean.parseBoolean(snapshot.getKey())).toString();
                            asitEvento.setText(mensaje);
                            asitEvento.setTextSize(20);
                            asitEvento.setHintTextColor(getResources().getColor(android.R.color.black));
                            contEvents.addView(asitEvento);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error!", databaseError.toException());
                    }
                });
            }catch (NullPointerException e){
                Toast.makeText(Asistente.this, "No ha asistido a ningún evento todavía.", Toast.LENGTH_SHORT).show();
            }
        }
        Button btn_aceptar = v.findViewById(R.id.btn_aceptar);
        builder.setView(v);
        alertDialog = builder.create();
        // Add action buttons
        btn_aceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }

        );
        return alertDialog;
    }

    /*
    El metodo Asistir() verifica el evento existente y extrae las coordenadas de la zona del evento y
    las compara con las del estudiante para validar la asistencia. Ademas, se realiza la respectiva
    validacion de la fecha y hora del evento.
     */
    public void Asistir(){
        btn_Asistir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                anio=calendar.get(Calendar.YEAR);
                mes=calendar.get(Calendar.MONTH)+1;
                dia=calendar.get(Calendar.DAY_OF_MONTH);
                horas=calendar.get(Calendar.HOUR_OF_DAY);
                minutos=calendar.get(Calendar.MINUTE);
                horaActual= horas+":"+minutos;

                DatabaseReference db_eventoAsistir = db_reference.child("Evento");

                if (name_evento!=null && !name_evento.equals("Seleccione un Evento")) {
                    db_eventoAsistir.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, String> info_evento = null;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                                if (data!= null && data.get("Nom_evento").equals(name_evento)) {
                                    info_evento = data;
                                    idEvento = snapshot.getKey();
                                    break;
                                }
                            }

                            if (info_evento!= null) {

                                String[] fecha_evento = info_evento.get("Fecha").split("/");
                                String[] hora_evento = info_evento.get("horaInicio").split(":");
                                String[] hora_finEvento = info_evento.get("horaFin").split(":");
                                int minRetrado = Integer.parseInt(info_evento.get("minRetraso"));
                                Boolean retraso = Boolean.parseBoolean(info_evento.get("Retraso"));

                                if (Conectividad()) {
                                    if (Integer.parseInt(fecha_evento[0]) == anio && Integer.parseInt(fecha_evento[1]) == mes && Integer.parseInt(fecha_evento[2]) == dia) {

                                        if (retraso) {
                                            if (horas == Integer.parseInt(hora_evento[0]) && minutos <= (Integer.parseInt(hora_evento[1]) + minRetrado)
                                                    && minutos >= Integer.parseInt(hora_evento[1])) {
                                                subirAsistencia(false);

                                            } else if (horas >= Integer.parseInt(hora_evento[0]) && minutos > (Integer.parseInt(hora_evento[1]) + minRetrado)) {
                                                if (horas == Integer.parseInt(hora_finEvento[0]) && minutos <= Integer.parseInt(hora_finEvento[1])) {
                                                    subirAsistencia(true);

                                                } else if (horas < Integer.parseInt(hora_finEvento[0])) {
                                                    subirAsistencia(true);

                                                } else {
                                                    Toast.makeText(Asistente.this, "Es posible que el evento ya haya finalizado. \n Contactese con al tutor o administrador del " +
                                                            "evento para mayor informacion.", Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                Toast.makeText(Asistente.this, "Aun no empieza el evento. Contactese con al tutor o administrador del " +
                                                        "evento para mayor informacion.", Toast.LENGTH_SHORT).show();

                                            }
                                        } else {
                                            if (horas >= Integer.parseInt(hora_evento[0]) && minutos >= Integer.parseInt(hora_evento[1])) {
                                                if (horas > Integer.parseInt(hora_finEvento[0])) {
                                                    Toast.makeText(Asistente.this, "Es posible que el evento ya haya finalizado. \n Contactese con al tutor o administrador del " +
                                                            "evento para mayor informacion.", Toast.LENGTH_SHORT).show();

                                                } else if (horas == Integer.parseInt(hora_finEvento[0]) && minutos <= Integer.parseInt(hora_finEvento[1])) {
                                                    subirAsistencia(false);

                                                } else if (horas < Integer.parseInt(hora_finEvento[0])) {
                                                    subirAsistencia(false);

                                                } else {
                                                    Toast.makeText(Asistente.this, "Es posible que el evento ya haya finalizado. \n Contactese con al tutor o administrador del " +
                                                            "evento para mayor informacion.", Toast.LENGTH_SHORT).show();

                                                }
                                            } else {
                                                Toast.makeText(Asistente.this, "Aun no empieza el evento o el evento ya finalizo. \n Contactese con al tutor o administrador del " +
                                                        "evento para mayor informacion.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } else {
                                        Toast.makeText(Asistente.this, "Aun no empieza el evento o el evento ya finalizo. Contactese con al tutor o administrador del " +
                                                "evento para mayor informacion.", Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(Asistente.this, "No dispone de conexion a Internet.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error!", databaseError.toException());
                        }
                    });
                }else{
                    Toast.makeText(Asistente.this, "Escoja el evento/curso primero",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    El metodo subirAsistencia(0 permite subir la asistencia del usuario a la base de datos en firebase,
    en las respectivas ramas de Asistente y Asistencias, tomando como dato previo el parametro tipo Boolean
    de atrasado.
     */
    public void subirAsistencia(Boolean atrasado) {
        boolean present = verifica_Asistencia();
        boolean atraso = atrasado;
        DatabaseReference db_listaAsistencia = db_reference.child("Asistencias");
        DatabaseReference db_dataAsistente = db_reference.child("Asistente").child(userId).child("listEventos");

        db_dataAsistente.child(idEvento).setValue(name_evento);
        System.out.println(present+"-"+atrasado);

        db_listaAsistencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> info_lista = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    info_lista = (HashMap<String, String>) snapshot.getValue();
                    System.out.println(name_evento);
                    System.out.println(info_lista);
                    if (info_lista!= null) {
                        if (info_lista.get("evento").equals(name_evento)) {
                            idLista = snapshot.getKey();
                        }else{
                            System.out.println("no lista");
                        }
                    }
                }
                System.out.println(idLista);

                if (atraso) {
                    System.out.println(idLista);
                    System.out.println(userId);
                    Lista lista = new Lista(userId, txt_nombre.getText().toString(), horaActual, "Atrasado", idEvento,0);
                    db_listaAsistencia.child(idLista).child("lista").child(userId).setValue(lista);
                    Toast.makeText(Asistente.this, "Asistencia marcada como: Atrasado", Toast.LENGTH_SHORT).show();
                }else{
                    Lista lista = new Lista(userId, txt_nombre.getText().toString(), horaActual, "Presente", idEvento,0);
                    db_listaAsistencia.child(idLista).child("lista").child(userId).setValue(lista);
                    Toast.makeText(Asistente.this, "Asistencia marcada como: Presente", Toast.LENGTH_SHORT).show();
                }
                System.out.println("Data Asistente subido");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error!", databaseError.toException());
            }
        });



    }

    /*
    El metodo verifica_Asistencia() permite verificar si el estudiante se encuentra dentro de la zona
    de asistencia del evento, caso contrario enviara un mensaje de aviso. Regresa como valor un valor
    tipo Boolean con la respuesta de la validacion.
     */
    public boolean verifica_Asistencia (){
        boolean presente =false;
        Double user_lat = Double.valueOf(txt_Latitud.getText().toString());
        Double user_long = Double.valueOf(txt_Longitud.getText().toString());
        Double Dps_Lat1 = Double.valueOf(disp_Lat1);
        Double Dps_Lat2 = Double.valueOf(disp_Lat2);
        Double Dps_Long1 = Double.valueOf(disp_Long1);
        Double Dps_Long2 = Double.valueOf(disp_Long2);

        if (Dps_Lat1 >=Dps_Lat2) {
            if ((Dps_Long1>=Dps_Long2) && (user_lat<=Dps_Lat1) && (user_lat>=Dps_Lat2) && (user_long<=Dps_Long1) && (user_long >=Dps_Long2)){
                presente = true;
            }
            if ((Dps_Long1<=Dps_Long2) && (user_lat<=Dps_Lat1) && (user_lat>=Dps_Lat2) && (user_long>=Dps_Long1) && (user_long <=Dps_Long2)) {
                presente = true;
            }
            if (!presente) {
                Toast.makeText(getApplicationContext(), "Se encuentra fuera del rango", Toast.LENGTH_SHORT).show();
            }
        }
        if (Dps_Lat1 <=Dps_Lat2) {
            if ((Dps_Long1>=Dps_Long2) && (user_lat>=Dps_Lat1) && (user_lat<=Dps_Lat2) && (user_long<=Dps_Long1) && (user_long >=Dps_Long2)){
                presente = true;
            }
            if ((Dps_Long1<=Dps_Long2) && (user_lat>=Dps_Lat1) && (user_lat<=Dps_Lat2) && (user_long>=Dps_Long1) && (user_long <=Dps_Long2)) {
                presente = true;
            }
            if (!presente) {
                Toast.makeText(getApplicationContext(), "Se encuentra fuera del rango", Toast.LENGTH_SHORT).show();
            }
        }

        return presente;
    }

    public void leerEventos(){
        DatabaseReference db_evento = db_reference.child("Evento");

        db_evento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventos = new ArrayList<String>();
                eventos.add("Seleccione un Evento");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                    if (data!= null) {
                        eventos.add(data.get("Nom_evento"));
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, eventos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item );
                spinner.setAdapter(adapter);

                AdapterView.OnItemSelectedListener eventSelected = new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
                        name_evento = spinner.getItemAtPosition(position).toString();
                        if (position!=0) {
                            Toast.makeText(Asistente.this,"Ha seleccionado el evento: " + name_evento, Toast.LENGTH_SHORT).show();
                            leerDispositivo(name_evento);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                };
                spinner.setOnItemSelectedListener(eventSelected);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
            }
        });
    }

    public void leerDispositivo(String curso){
        DatabaseReference db_dispositivo = db_reference.child("Dispositivo");
        db_dispositivo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                    if (data!=null) {
                        if (data.get("Evento").equals(curso)) {
                            disp_Lat1 = data.get("Latitud1");
                            disp_Long1 = data.get("Longitud1");
                            disp_Lat2 = data.get("Latitud2");
                            disp_Long2 = data.get("Longitud2");
                            break;
                        }
                    }
                }
                Asistir();
                marcarSalida();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
                System.out.println(error.getMessage());
            }
        });
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

    /*
    Verifica si es servicio de google service esta activo para el correcto funcionamiento de las API's
    de google utilizadas como geolocalizacion, googleAccount.
     */
    public boolean isServiceOk(){
        Log.d(TAG, "isServiceOk: checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Asistente.this);

        if (available == ConnectionResult.SUCCESS){
            //Everything is fine and the user can make map request
            Log.d(TAG, "isServiceOk: verything is fine and the user can make map request");
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
                            if (currentLocation !=null) {
                                txt_Latitud.setText(String.valueOf(currentLocation.getLatitude()));
                                txt_Longitud.setText(String.valueOf(currentLocation.getLongitude()));
                                DatabaseReference db_upload = FirebaseDatabase.getInstance().getReference().child("Asistente").child(userId);
                                db_upload.child("asistLat").setValue(String.valueOf(currentLocation.getLatitude()));
                                db_upload.child("asistLong").setValue(String.valueOf(currentLocation.getLongitude()));
                            }

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
