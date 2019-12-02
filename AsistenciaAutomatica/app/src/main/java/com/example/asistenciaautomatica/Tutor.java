package com.example.asistenciaautomatica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    TextView txt_id , txt_correo , txt_cellphone  ;
    DatabaseReference db_reference;


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


        iniciarBaseDeDatos();
        leerTweets();
        escribirTweets(info_user.get("user_name"));

    }
    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference().child("Grupo");

    }

    public void leerTweets(){
        db_reference.child("Grupo 0").child("tweets")
                .addValueEventListener(new ValueEventListener() {
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

    public void escribirTweets(String autor){
        String tweet = "hola mundo firebase 2";
        String fecha = "31/10/2019";
        Map<String, String> hola_tweet = new HashMap<String, String>();
        hola_tweet.put("autor", autor);
        hola_tweet.put("fecha", fecha);
        DatabaseReference tweets = db_reference.child("Grupo 0").child("tweets");
        tweets.setValue(tweet);
        tweets.child(tweet).child("autor").setValue(autor);
        tweets.child(tweet).child("fecha").setValue(fecha);
    }

    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);

    }



}
