package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Perfil extends AppCompatActivity {
    private static final String TAG = "Perfil";
    private Bundle info_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        info_user = getIntent().getBundleExtra("info_user");
    }

    /**
    startTutor implementa la accion del boton de Tutor para iniciar la actividad de Tutor, se envia como
    dato extra un Bundle con la informacion del usuario extraida de la anterior Activity.
     */
    public void startTutor(View view){
        Intent intent = new Intent(this, Tutor.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);

    }
    /**
     startAsistente implementa la accion del boton de Asistente para iniciar la actividad de Asistente, se envia como
     dato extra un Bundle con la informacion del usuario extraida de la anterior Activity.
     */
    public void startAsistente(View view){
        Intent intent = new Intent(this, Asistente.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);
    }
}
