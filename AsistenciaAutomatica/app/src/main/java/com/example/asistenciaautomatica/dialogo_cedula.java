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

    public dialogo_cedula(Context contexto){

        final Dialog dialogo = new Dialog(contexto);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setCancelable(false);
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setContentView(R.layout.cedula);

        final EditText edit_cedula = (EditText) dialogo.findViewById(R.id.edit_cedula);
        Button aceptar = dialogo.findViewById(R.id.btn_cedula);
        edit_cedula.setInputType(InputType.TYPE_CLASS_NUMBER);

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        dialogo.show();

    }
}
