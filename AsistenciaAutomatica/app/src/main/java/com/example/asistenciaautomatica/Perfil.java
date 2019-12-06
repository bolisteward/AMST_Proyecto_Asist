package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class Perfil extends AppCompatActivity {

    HashMap<String, String> info_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Intent intent = getIntent();
        info_user = (HashMap<String, String>)intent.getSerializableExtra("info_user");


    }

    public void iniciarTutor(View view){
        Intent intent = new Intent(this, Tutor.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);

    }

    public void iniciarAsistente(View view){
        Intent intent = new Intent(this, Asistente.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);
    }
}
