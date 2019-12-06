package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class Tutor extends AppCompatActivity {

    private static final String TAG = "Tutor";
    private static final int ERROR_DIALOG_REQUEST = 9001;


    TextView txt_id , txt_correo , txt_cellphone  ;
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

        txt_id = findViewById(R.id.txt_userId);
        txt_correo = findViewById(R.id.txt_correo);
        txt_cellphone=findViewById(R.id.txt_phone);


        txt_id.setText(info_user.get("user_id"));
        txt_correo.setText(info_user.get("user_email"));
        txt_cellphone.setText(info_user.get("user_phone"));
        userId = info_user.get("user_id");
        String photo = info_user.get("user_photo");
        Picasso.with(getApplicationContext()).load(photo).resize(300,300).into(img_foto);


        isServiceOk();

        iniciarBaseDeDatos();
        leerBaseDatos();
        subirTutor(info_user.get("user-name"), info_user.get("user_phone"), userId, info_user.get("user_email"));


    }
    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

    }

    public void leerBaseDatos(){
        db_reference.child("Tutor").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println(snapshot);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }

    public void irRegistros(View view){
        Intent intent = new Intent(this, FormularioCurso.class);
        startActivity(intent);
    }


    public void leerAsistencia(){
        db_reference.child("Asistencias").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println(snapshot);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }

    public void subirTutor(String nombre, String phone, String userID, String correo) {


        String asistente= userID;
        DatabaseReference subir_data = db_reference.child("Asistente").child(userID);
        subir_data.setValue(asistente);
        subir_data.child(asistente).child("Nombre").setValue(nombre);
        subir_data.child(asistente).child("Correo").setValue(correo);
        subir_data.child(asistente).child("Telefono").setValue(phone);
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
