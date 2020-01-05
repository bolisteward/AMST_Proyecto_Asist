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
    public TextView txt_correo , txt_cellphone, txt_nombre, txt_cedula;
    DatabaseReference db_reference;

    Bundle info_user;
    public Button btn_CrearEvento;
    public ImageView img_foto;
    public String userId;
    public Boolean nuevoTutor;
    public Users tutor;


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

        if (isServiceOk()) {
            iniciarBaseDeDatos();
            leerBaseDatos();
        }

    }
    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

    }

    public void leerBaseDatos(){
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
                btn_CrearEvento.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Tutor.this, FormularioCurso.class);
                        intent.putExtra("tutorID", userId);
                        startActivity(intent);
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error!", error.toException());
                System.out.println(error.getMessage());
            }
        });
    }

    public void newTutor() {
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
    public void presentarDatos(){
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
     * Crea un diálogo con personalizado para comportarse
     * como formulario de login
     *
     * @return Diálogo
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

                        txt_cedula.setText(edtMatricula.getText().toString());
                        //System.out.println("el numero de matricula es "+edtMatricula.getText().toString());
                        //System.out.println(userId);

                        DatabaseReference db_upload = FirebaseDatabase.getInstance().getReference().child("Tutor").child(userId);
                        db_upload.child("matricula").setValue(edtMatricula.getText().toString());

                        alertDialog.dismiss();
                    }
                }

        );
        return alertDialog;
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
