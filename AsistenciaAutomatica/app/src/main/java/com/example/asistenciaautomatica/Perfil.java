package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Perfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
    }

    public void iniciarTutor(View view){
        Intent intent = new Intent(this, Tutor.class);
        startActivity(intent);

    }

    public void iniciarAsistente(View view){

    }
}
