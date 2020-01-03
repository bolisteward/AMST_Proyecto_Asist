package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class Perfil extends AppCompatActivity {
    private static final String TAG = "Perfil";
    Bundle info_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        info_user = getIntent().getBundleExtra("info_user");
    }

    public void startTutor(View view){
        Intent intent = new Intent(this, Tutor.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);

    }

    public void startAsistente(View view){
        Intent intent = new Intent(this, Asistente.class);
        intent.putExtra("info_user", info_user );
        startActivity(intent);
    }
}
