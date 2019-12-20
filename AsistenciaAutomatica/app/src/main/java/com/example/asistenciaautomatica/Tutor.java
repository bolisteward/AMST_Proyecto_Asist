package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Delayed;

public class Tutor extends AppCompatActivity {

    private static final String TAG = "Tutor";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //vars
    private boolean mLocationPermissionGaranted = false;
    public FusedLocationProviderClient mFusedLocationProviderClient;

    //Views
    public TextView txt_correo , txt_cellphone, txt_nombre ;
    public EditText txt_cedula;
    DatabaseReference db_reference;

    HashMap<String, String> info_user;

    private ImageView img_foto;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor);

        Intent intent = getIntent();
        HashMap<String , String> info_user = (HashMap<String,String>)intent.getSerializableExtra("info_user");


        txt_correo = findViewById(R.id.txt_correo);
        txt_cellphone=findViewById(R.id.txt_phone);
        img_foto = findViewById(R.id.img_foto);
        txt_nombre = findViewById(R.id.txt_nombre);
        txt_cedula = findViewById(R.id.txt_cedula);

        txt_correo.setText(info_user.get("user_email"));
        txt_nombre.setText(info_user.get("user_name"));
        txt_cellphone.setText(info_user.get("user_phone"));
        userId = info_user.get("user_id");
        String photo = info_user.get("user_photo");
        Picasso.get().load(photo).resize(300,300).error(R.drawable.usuario).into(img_foto);

        isServiceOk();
        iniciarBaseDeDatos();
        leerBaseDatos();

    }
    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

    }

    public void leerBaseDatos(){
        db_reference.child("Tutor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                    System.out.println("prueba");
                    System.out.println(data.get("ID"));
                    System.out.println(userId);

                    if (data.get("ID")!=null && data.get("ID").equals( userId )){
                        System.out.println("existe");
                        System.out.println(data.get("ID"));
                        System.out.println(userId);
                        txt_cedula.setText(data.get("Cedula"));
                        updateTutor(info_user.get("user_name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_cedula.getText().toString());
                    }
                    else {
                        System.out.println("No existe");
                        System.out.println(userId);
                        Context context = getApplicationContext();

                        new dialogo_cedula(Tutor.this);

                        System.out.println(txt_cedula.getText().toString());


                        /*
                            System.out.println(txt_cedula);

                            Toast.makeText(getApplicationContext(), "Ingrese cedula", Toast.LENGTH_SHORT).show();
                            /*new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                }
                            },5000);*/

                        //subirTutor(info_user.get("user_name"), info_user.get("user_phone"), userId, info_user.get("user_email"), txt_cedula.getText().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }

    /**
     * Crea un diálogo con personalizado para comportarse
     * como formulario de login
     *
     * @return Diálogo
     */
    public AlertDialog createAlertDialogo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Tutor.this);

        LayoutInflater inflater = Tutor.this.getLayoutInflater();

        View v = inflater.inflate(R.layout.cedula, null);

        builder.setView(v);

        Button aceptar = v.findViewById(R.id.btn_cedula);
        EditText cedula = v.findViewById(R.id.edit_cedula);

        aceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Crear Cuenta...
                        txt_cedula.setText(cedula.getText().toString());
                    }
                }
        );

        return builder.create();
    }

    public void updateTutor(String nombre, String phone, String remoteID, String correo, String cedula) {

        DatabaseReference subir_data = db_reference.child("Asistente").child(remoteID);
        subir_data.child(remoteID).child("Nombre").setValue(nombre);
        subir_data.child(remoteID).child("Correo").setValue(correo);
        subir_data.child(remoteID).child("Telefono").setValue(phone);
        subir_data.child(remoteID).child("Cedula").setValue(cedula);

    }

    public void subirTutor(String nombre, String phone, String userID, String correo, String cedula) {

        DatabaseReference subir_data = db_reference.child("Asistente");

        Map<String, String> dataUser = new HashMap<String, String>();
        dataUser.put("Nombre", nombre);
        dataUser.put("Correo", correo);
        dataUser.put("Telefono", phone);
        dataUser.put("ID",userID);
        dataUser.put("Cedula", cedula);
        subir_data.push().setValue(dataUser);

    }

    public void irRegistros(View view){
        Intent intent = new Intent(this, Lista_Asistencia.class);
        startActivity(intent);
    }


    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    public void crearEvento (View view){
        Intent intent = new Intent(this, FormularioCurso.class);
        intent.putExtra("tutorID", userId);
        startActivity(intent);
    }

    public boolean isServiceOk(){
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
