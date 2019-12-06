package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FormularioCurso extends AppCompatActivity {
    DatabaseReference db_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_curso);

        iniciarBaseDeDatos();
    }


    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference();

        leerAsistencia();

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
}
