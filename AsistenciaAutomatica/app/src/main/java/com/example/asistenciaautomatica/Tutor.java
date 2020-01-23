package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Tutor extends AppCompatActivity {

    private static final String TAG = "Tutor";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    //Views
    private TextView txt_correo;
    private TextView txt_cellphone;
    private TextView txt_nombre;
    private TextView txt_cedula;
    private DatabaseReference db_reference;

    private Bundle info_user;
    private Spinner spinner;
    private List<String> eventos;
    private Button btn_CrearEvento;
    private Button btn_verRegistros;
    private Button btn_marcaSalida;
    private Button btn_estadisticas;
    private ImageView img_foto;
    private String userId;
    private String name_evento;
    private Boolean nuevoTutor;
    private Users tutor;
    private HashMap<String, String> info_evento = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor);

        txt_correo = findViewById(R.id.txt_correo);
        txt_cellphone=findViewById(R.id.txt_phone);
        img_foto = findViewById(R.id.img_foto);
        txt_nombre = findViewById(R.id.txt_nombre);
        txt_cedula = findViewById(R.id.txt_cedula);
        btn_CrearEvento = findViewById(R.id.btn_CrearEvento);
        btn_verRegistros =  findViewById(R.id.btn_verRegistros);
        btn_marcaSalida = findViewById(R.id.btn_marcaSalida);
        btn_estadisticas = findViewById(R.id.btn_estadisticas);
        spinner = findViewById(R.id.spinner);

        if (isServiceOk()) {
            iniciarBaseDeDatos();
            leerBaseDatos();
            leerEventos();
            marcarSalida(name_evento);
            verAsistencias(name_evento);
            verEstadisticas(name_evento);

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
    El metodo leerBaseDatos recorre los asistentes en la base de datos para identificar si el usuario
    ya ha iniciado sesion previamente para directamente cargar la informacion en la interfaz frafica,
    o si es un usuario nuevo. Este metodo implementa el boton de Crear Eventos enviando como info extra
    en el llamado de la activity el userId.
     */
    private void leerBaseDatos(){
        DatabaseReference db_tutor = db_reference.child("Tutor");

        db_tutor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nuevoTutor = true;
                info_user = getIntent().getBundleExtra("info_user");

                if (info_user!=null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                        System.out.println(data);
                        if (data != null) {
                            userId = data.get("idUser");
                            System.out.println(userId);
                            System.out.println(info_user.getString("user_id"));


                            if (userId.equals(info_user.getString("user_id"))) {
                                nuevoTutor = false;
                                System.out.println(nuevoTutor);
                                presentarDatos();
                                break;
                            }
                            System.out.println("ok");
                        }
                    }
                }
                System.out.println(nuevoTutor);
                System.out.println(userId);
                if (nuevoTutor){
                    newTutor();
                }
                btn_CrearEvento.setOnClickListener(v -> {
                    Intent intent = new Intent(Tutor.this, FormularioCurso.class);
                    intent.putExtra("tutorID", userId);
                    startActivity(intent);
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
                System.out.println(error.getMessage());
            }
        });
    }

    /**
     Se recorre la sesccion Evento de la base de datos para agregar todos los nombres de los eventos existentes
     en el spinner View, y se implementa su accion al ser accedido por el usuario para implementar los metodos de
     visualizacion de asistencia, de grafico estadistico y poder marcar la salida del evento.
     */
    private void leerEventos(){
        DatabaseReference db_evento = db_reference.child("Evento");


        db_evento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventos = new ArrayList<String>();
                eventos.add("Seleccione un Evento");
                info_user = getIntent().getBundleExtra("info_user");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                    if (data!= null) {
                        if (data.get("tutorID").equals(info_user.getString("user_id"))){
                            eventos.add(data.get("Nom_evento"));

                        }
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
                            Toast.makeText(Tutor.this,"Ha seleccionado el evento: " + name_evento, Toast.LENGTH_SHORT).show();
                            DatabaseReference db_eventoInfo = db_reference.child("Evento");
                            db_eventoInfo.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                        HashMap<String, String> dataEvento = (HashMap<String, String>) snapshot.getValue();
                                        if (dataEvento!= null && dataEvento.get("Nom_evento").equals(name_evento)) {
                                            info_evento = dataEvento;
                                            break;
                                        }
                                    }

                                    verAsistencias(name_evento);
                                    marcarSalida(name_evento);
                                    verEstadisticas(name_evento);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Error!", databaseError.toException());
                                }
                            });
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

    /**
    El metodo verAsistencias requiere del parametro @evento e implementa la accion del boton verRegistros en el cual se crea un objeto Intent
    para llamar la Activity Lista_Asistencia y envia el nombre del evento seleccionado, caso contrario si evento esta vacio mostrara un mensaje
     pidiendo selleccionar un evento.
     */
    private void verAsistencias(String evento){
        btn_verRegistros.setOnClickListener(v -> {
            if (evento!=null) {
                Intent intent = new Intent(Tutor.this, Lista_Asistencia.class);
                intent.putExtra("evento", evento);
                startActivity(intent);
            }else{
                Toast.makeText(Tutor.this,"Seleccione un evento o curso primero." + name_evento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     El metodo verEstadisticas requiere del parametro evento e implementa la accion del boton btn_estadisticas en el cual se crea un objeto Intent
     para llamar la Activity Grafica_pastel y envia el nombre del evento seleccionado, caso contrario si evento esta vacio mostrara un mensaje
     pidiendo selleccionar un evento.
     */
    private void verEstadisticas(String evento){
        btn_estadisticas.setOnClickListener(v -> {
            if (evento!=null) {
                Intent intent = new Intent(Tutor.this, Grafica_pastel.class);
                intent.putExtra("evento", evento);
                startActivity(intent);
            }else{
                Toast.makeText(Tutor.this,"Seleccione un evento o curso primero." + name_evento, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     El metodo marcarSalida requiere del parametro evento e implementa la accion del boton btn_marcaSalida en el cual
     se valida el nombre del evento seleccionado del spinner con la base de datos y se verifica que la opcion de marcar salida
     haya sido seleccionado, caso contrario mostrara un mensaje diciendo que la accion es innecesaria.
     */
    private void marcarSalida(String evento){
        btn_marcaSalida.setOnClickListener(v -> {

            if (evento!=null && !evento.equals("Seleccione un Evento") && info_evento!=null) {
                DatabaseReference db_mSalida = db_reference.child("Evento");

                if (info_evento.get("Nom_evento").equals(evento)) {
                    if (info_evento.get("Marcar_Salida").equals("true")) {
                        horaFinAsistentes(evento);
                    } else {
                        Toast.makeText(Tutor.this, "El evento seleccionado se cierra automaticamente a la hora estipulada en la creacion del evento." + name_evento, Toast.LENGTH_SHORT).show();
                    }

                }
            }else{
                Toast.makeText(Tutor.this,"Seleccione un evento o curso primero.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
    El metodo horaFinAsistentes toma como parametro el nombre del evento obtendio del spinner para colocar la hora de finalizacion
    del evento en todos los asistentes de dicho evento con el numero de horas totales a partir de su respectiva hora de asistencia.
     */
    private void horaFinAsistentes(String evento){
        Calendar calendar = Calendar.getInstance();
        int horas=calendar.get(Calendar.HOUR_OF_DAY);
        int minutos=calendar.get(Calendar.MINUTE);
        String horaFin= horas+":"+minutos;

        DatabaseReference db_horaFin = db_reference.child("Asistencias");

        db_horaFin.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> dataLista = (HashMap<String, String>) snapshot.getValue();

                    if (dataLista!= null) {
                        if (dataLista.get("evento").equals(evento)){

                            DatabaseReference ref = db_horaFin.child(snapshot.getKey()).child("lista");
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        HashMap<String, String> dataUser = (HashMap<String, String>) snapshot.getValue();

                                        if (snapshot.getKey()!=null && dataUser!=null) {
                                            ref.child(snapshot.getKey()).child("horaFin").setValue(horaFin);
                                            String[] horaInicio = dataUser.get("horaInicio").split(":");

                                            if (Integer.parseInt(horaInicio[0]) == horas) {
                                                int minTotal = minutos-Integer.parseInt(horaInicio[1]);
                                                String horaFinAsist = 0+"."+minTotal+"h";
                                                ref.child(snapshot.getKey()).child("numHoras").setValue(horaFinAsist);
                                            }else{
                                                int horasPresente = horas - Integer.parseInt(horaInicio[0]);

                                                if (minutos > Integer.parseInt(horaInicio[1])) {
                                                    int minTotal = minutos - Integer.parseInt(horaInicio[1]);
                                                    String horaFinAsist = horasPresente+"."+minTotal+"h";
                                                    ref.child(snapshot.getKey()).child("numHoras").setValue(horaFinAsist);
                                                }else {
                                                    int minTotal = 60 + minutos - Integer.parseInt(horaInicio[1]);
                                                    String horaFinAsist = (horasPresente-1)+"."+minTotal+"h";
                                                    ref.child(snapshot.getKey()).child("numHoras").setValue(horaFinAsist);
                                                }
                                            }
                                        }
                                    }
                                    Toast.makeText(Tutor.this, "Hora de salida: " + horaFin, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Error!", databaseError.toException());
                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error!", databaseError.toException());
            }
        });
    }

    /**
     El metodo newTutor permite subir los datos obtenidos de la cuenta de google con el que el usuario
     inicia sesion, se pide al usuario que ingrese el numero de cedula mediante un cuadro de dialogo, se
     sube toda la info obtenida a la base de datos en Firebase.
     */
    private void newTutor() {
        info_user = getIntent().getBundleExtra("info_user");
        if (info_user != null) {
            txt_nombre.setText(info_user.getString("user_name"));
            txt_cellphone.setText(info_user.getString("user_phone"));
            txt_correo.setText(info_user.getString("user_email"));
            userId = info_user.getString("user_id");
            String photo = info_user.getString("user_photo");
            Picasso.get().load(photo).resize(300, 300).error(R.drawable.usuario).into(img_foto);


            tutor = new Users(info_user.getString("user_name"), info_user.getString("user_email"), info_user.getString("user_phone"), info_user.getString("user_id"));

            DatabaseReference db_upload = db_reference.child("Tutor");

            db_upload.child(userId).setValue(tutor);

            createCustomDialog().show();
        }
    }

    /**
     Cuando el ususario ya existe se implementa el metodo presentarDatos, el cual permite tomar los datos del usuario
     de la base de datos y cargarlos en los respectivos TextView's del archivo tutor.xml.
     */
    private void presentarDatos(){
        info_user = getIntent().getBundleExtra("info_user");

        if (info_user != null) {
            userId = info_user.getString("user_id");
            String photo = info_user.getString("user_photo");
            Picasso.get().load(photo).resize(300, 300).error(R.drawable.usuario).into(img_foto);
        }
        DatabaseReference db_asist = db_reference.child("Tutor").child(userId);
        db_asist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users usr = dataSnapshot.getValue(Users.class);
                if (usr!=null) {
                    txt_nombre.setText(usr.getNombre());
                    txt_correo.setText(usr.getCorreo());
                    txt_cellphone.setText(usr.getTelefono());
                    txt_cedula.setText(usr.getMatricula());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error!", databaseError.toException());
            }
        });

    }

    /**
     Se crea un cuadro de dialogo de tipo AlertDialog el cual utuliza el archivo matricula.xml como interfaz grafica,
     para pedir el numero de cedula o matricula. Se obtiene el dato ingresado y es subido directamente a la base
     de datos del usuario registrado.
     */
    private AlertDialog createCustomDialog() {
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
                v1 -> {

                    txt_cedula.setText(edtMatricula.getText().toString());
                    //System.out.println("el numero de matricula es "+edtMatricula.getText().toString());
                    //System.out.println(userId);

                    DatabaseReference db_upload = FirebaseDatabase.getInstance().getReference().child("Tutor").child(userId);
                    db_upload.child("matricula").setValue(edtMatricula.getText().toString());

                    alertDialog.dismiss();
                }

        );
        return alertDialog;
    }

    /**
     Se cierra la sesion de la cuenta google con la cual ingreso el usuario y es enviado directamente a la MainActivity
     */
    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    /**
     Verifica si el servicio de google service esta activo para el correcto funcionamiento de las API's
     de google utilizadas como geolocalizacion, googleAccount.
     */
    private boolean isServiceOk(){
        Log.d(TAG, "isServiceOk: checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Tutor.this);

        if (available == ConnectionResult.SUCCESS){
            //Everything is fine and the user can make map request
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error ocured but we can resolt it
            Log.d(TAG, "isServiceOk: an error occures but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Tutor.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
