package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Lista_Asistencia extends AppCompatActivity {

    private static final String TAG = "Lista_Asistencia";
    DatabaseReference db_reference;
    String name_evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista__asistencia);

        name_evento = getIntent().getStringExtra("evento");
        db_reference = FirebaseDatabase.getInstance().getReference().child("Asistencias");
        leerAsistencia();
    }

    public void leerAsistencia() {
        db_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                    if (data!= null && data.get("evento").equals(name_evento)) {
                        DatabaseReference db_lista = db_reference.child(snapshot.getKey()).child("lista");
                        db_lista.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    mostrarRegistrosPorPantalla(snapshot);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Error!", databaseError.toException());
                            }
                        });
                        break;
                    }
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
        LinearLayout contEstado= (LinearLayout) findViewById(R.id.ContenedorEstado);
        LinearLayout contHoraInicio= (LinearLayout) findViewById(R.id.ContenedorHora);
        LinearLayout contNumHoras= (LinearLayout) findViewById(R.id.ContenedorCantHoras);

        String Name = String.valueOf(snapshot.child("nombre").getValue());
        String estado = String.valueOf(snapshot.child("estado").getValue());
        String horas = String.valueOf(snapshot.child("horaInicio").getValue());
        String cantHoras = String.valueOf(snapshot.child("numHoras").getValue());
        System.out.println(Name);
        System.out.println(estado);
        System.out.println(horas);
        System.out.println(cantHoras);

        TextView userName = new TextView(getApplicationContext());
        userName.setText(Name);
        userName.setTextSize(20);
        contNombre.addView(userName);
        TextView Estado = new TextView(getApplicationContext());
        Estado.setText(estado);
        userName.setTextSize(20);
        contEstado.addView(Estado);
        TextView horaI = new TextView(getApplicationContext());
        horaI.setText(horas);
        userName.setTextSize(20);
        contHoraInicio.addView(horaI);
        TextView numHoras = new TextView(getApplicationContext());
        numHoras.setText(cantHoras);
        userName.setTextSize(20);
        contNumHoras.addView(numHoras);
    }


}
