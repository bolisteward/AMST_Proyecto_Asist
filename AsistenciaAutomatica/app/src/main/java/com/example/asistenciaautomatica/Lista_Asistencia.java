package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Lista_Asistencia extends AppCompatActivity {

    public String nombreAsistente;
    DatabaseReference db_reference;
    DatabaseReference db_reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista__asistencia);

        db_reference = FirebaseDatabase.getInstance().getReference();
        db_reference2 = FirebaseDatabase.getInstance().getReference();
        leerAsistencia();
    }

    public void leerAsistencia() {
        db_reference.child("Asistencias").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mostrarRegistrosPorPantalla(snapshot);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }


    public void mostrarRegistrosPorPantalla(DataSnapshot snapshot){
        LinearLayout contNombre = (LinearLayout) findViewById(R.id.ContenedorNombre);
        LinearLayout contAsiste= (LinearLayout) findViewById(R.id.ContenedorAsiste);

        String userVal = String.valueOf(snapshot.child("").getValue());
        String asisteVal = String.valueOf(snapshot.child("axis").getValue());
        TextView temp = new TextView(getApplicationContext());
        temp.setText(userVal+" C");
        contNombre.addView(temp);
        TextView axis = new TextView(getApplicationContext());
        axis.setText(asisteVal);
        contAsiste.addView(axis);
    }

    public void obtenerNombreAsistente (String userId){
        db_reference2.child("Asistente").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(userId )){
                        HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                        nombreAsistente = data.get("Nombre");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.toException());
            }
        });
    }


}
