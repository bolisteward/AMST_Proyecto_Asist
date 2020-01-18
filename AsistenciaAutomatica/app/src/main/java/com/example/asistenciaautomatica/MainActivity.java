package com.example.asistenciaautomatica;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.ObjectStreamException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Varibales públicas
    static final int GOOGLE_SIGN_IN = 123;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth =  FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(
                R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");

        if(msg != null){
            if(msg.equals("cerrarSesion")){
                cerrarSesion();
            }
        }
    }

    /**
    Se cierra la sesion de inicio de sesion con google en caso de algun error producido o campos ingresados
    como la constrasena fueron erroneos.
     */
    private void cerrarSesion() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,task -> updateUI(null));
    }

    /**
    Permite mostrar en forma de cuadro de dialogo la interfaz que el API de google account aporta para
    iniciar sesion con cuenta google.
     */
    public void iniciarSesion(View view){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    /**
    Muestra el resultado de haber iniciado sesion con google, en caso de que la cuenta no exista o algun
    parametro o error de inicio de sesion se presente, mostrara un mensaje sobre el fallo de inicio de sesion
    con la cuenta Google, caso contrario llamara al metodo de autentificacion con cuenta firabase.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle (account);
            }catch (ApiException e){
                Log.w("TAG", "Fallo el inicio de sesion con Google.",e);
            }
        }
    }

    /*
    firebaseAuthWithGoogle() permite auntetificar la cuenta google con que se inicio sesion para ingresar a la cuenta de firebase
    mediante la credencial unica que Google provee por usuario.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle: "+account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    MainActivity.this.updateUI(user);
                } else {
                    Toast.makeText(getApplicationContext(), "Error de inicio de sesion con firebase", Toast.LENGTH_SHORT).show();
                    System.out.println("error");
                    MainActivity.this.updateUI(null);
                }
            }
        });
    }

    /**
    updateUI Permite extrar todos los datos del usuario como el nombre, correo, mail, photo, id y numero telefonico,
     y son incorporados en un objeto Bundle mediante clave-valor y enviados a la siguiente activity para su respectivo uso.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null){

            Bundle info_user = new Bundle();

            info_user.putString("user_name", user.getDisplayName());
            info_user.putString("user_email", user.getEmail());
            info_user.putString("user_photo", String.valueOf(user.getPhotoUrl()));
            info_user.putString("user_id", user.getUid());

            if (user.getPhoneNumber() !=null){
                info_user.putString("user_phone", user.getPhoneNumber());
                //usuario = new Users(user.getDisplayName(), user.getEmail(), user.getPhoneNumber(), String.valueOf(user.getPhotoUrl()), user.getUid());
                System.out.println("Si tiene numero celular");
            }else {
                info_user.putString("user_phone", "Sin numero");

                System.out.println("Sin numero");
            }

            finish();

            Intent intent = new Intent(MainActivity.this, Perfil.class);
            intent.putExtra("info_user", info_user);
            startActivity(intent);

        }else {
            Toast.makeText(getApplicationContext(),"Aun no se ha registrado en google.", Toast.LENGTH_SHORT).show();
            System.out.println("Sin registrarse");
        }
    }

}
