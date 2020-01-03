package com.example.asistenciaautomatica;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.InputDevice;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class dialogo_cedula {


    public interface finalizaCuadroDialogo{
        void ResultadoCuadroDialogo(String peso);
    }


    private finalizaCuadroDialogo interfaz;



    public dialogo_cedula(Context contexto, finalizaCuadroDialogo actividad){

        interfaz = actividad;


        final Dialog dialogo = new Dialog(contexto);

        try {
            dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogo.setCancelable(false);
            dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogo.setContentView(R.layout.matricula);

            final EditText edtMatricula = (EditText) dialogo.findViewById(R.id.edtMatricula);
            Button btn_aceptar = dialogo.findViewById(R.id.btn_aceptar);
            edtMatricula.setInputType(InputType.TYPE_CLASS_NUMBER);

            btn_aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interfaz.ResultadoCuadroDialogo(edtMatricula.getText().toString());
                    dialogo.dismiss();
                }
            });

            dialogo.show();
        }catch (NullPointerException npe){
            System.out.println(npe.getMessage());
        }

    }
}
