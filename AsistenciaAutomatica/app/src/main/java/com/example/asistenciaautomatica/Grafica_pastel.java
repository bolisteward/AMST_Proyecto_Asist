package com.example.asistenciaautomatica;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Grafica_pastel extends AppCompatActivity {

    private static final String TAG = "Grafica_pastel";
    private PieChart pieChart;
    private PieDataSet pieDataSet;
    private String[] estado = new String[]{"Presente", "Atrasado", "No asiste"};
    private int[] cantEstudiantes;
    private int[] colors = new int[]{Color.RED, Color.GREEN, Color.YELLOW};
    private  int totalAsistentes;
    public String evento;
    DatabaseReference db_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafica_pastel);

        pieChart = findViewById(R.id.lineChart);
        evento = getIntent().getStringExtra("evento");
        db_reference = FirebaseDatabase.getInstance().getReference().child("Asistencias");

        leerAsistencia();
    }

    /**
    Serecorre la sesion Asistencias de la base de datos para determinar que lista de asistencia pertenece el nombre
     del evento y la recorre para determinar cuantos estudiantes o participantes tienen atraso, presente, o no asistieron
     y son agregados a la lista cantEstudiantes y se llama a createChart() para crear el grafico de pastel con los datos.
     */
    public void leerAsistencia() {
        db_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();

                    if (data!= null && data.get("evento").equals(evento)) {
                        DatabaseReference db_lista = db_reference.child(snapshot.getKey()).child("lista");

                        db_lista.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int presente = 0;
                                int atrasado = 0;
                                int noAsiste = 0;

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                                    if (data!=null){
                                        if (data.get("estado").equals("Presente")){
                                            presente+=1;
                                        }
                                        if (data.get("estado").equals("Atrasado")){
                                            atrasado+=1;
                                        }
                                        if (data.get("estado").equals("No asiste")){
                                            System.out.println("fallo");
                                            noAsiste+=1;
                                        }
                                    }
                                }
                                totalAsistentes = presente+atrasado+noAsiste;
                                cantEstudiantes = new int[]{presente, atrasado, noAsiste};
                                createChart();

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

    /**
    El metodo getSameChart() permite personalizar el contenido de la grafica, para ello toma parametros de ingreso
    como @parameters chart, descripcion, textColor, background y animateY y se agrega legenda de los datos.
     */
    private Chart getSameChart (Chart chart, String descripcion, int textColor, int background, int animateY){
        chart.getDescription().setText(descripcion);
        chart.getDescription().setTextSize(30);
        chart.getDescription().setTextColor(textColor);
        chart.setBackgroundColor(background);
        chart.animateY(animateY);
        legend(chart);

        return chart;
    }

    /**
    El metodo legend requiere del @PARAMETRO  de tipo Chart para configurar las entradas en la legenda del
    grafico de pastel.
     */
    private void legend (Chart chart){
        Legend legend =  chart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(15);

        ArrayList <LegendEntry> entries = new ArrayList<>();
        for (int i=0; i<estado.length; i++){
            LegendEntry entry =  new LegendEntry();
            entry.formColor = colors [i];
            entry.label = estado[i];
            entries.add(entry);
        }

        legend.setCustom(entries);
    }

    /**
    Se retorna una ArrayList con las entradas que se ingresaran en el grafico de pastel o PieChart,
    en donde se agregan objetos tipo PieEntry().
     */
    private ArrayList<PieEntry> getPieEntries(){
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i=0; i<estado.length; i++){
            float promedio = cantEstudiantes[i]*(100/totalAsistentes);
            entries.add(new PieEntry(promedio, cantEstudiantes[i]));

        }
        return  entries;
    }

    /**
    createChart() permite crear una grafica de pastel y su respectiva configuracion de su forma, como el
    radio del circuilo interior y se inserta los datos obtendios del metodo getPieData().
     */
    public void createChart(){
        pieChart = (PieChart) getSameChart(pieChart, "Estado Asistencias", Color.BLACK, Color.TRANSPARENT, 3000);
        pieChart.setHoleRadius(40);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(50);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setData(getPieData());
        pieChart.invalidate();

    }

    /**
    Permite configurar la interfaz del dataSet como color y el tamano del texto.
     */
    private DataSet getData(DataSet dataSet) {
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(30);
        return dataSet;
    }

    /**
    getPieDara() retorna un objeto tipo PieData en la cual se ingresa un dataSet que ha sido cargado con las
    entradas respectivas obtenidas del metodo getPieEntries().
     */
    private PieData getPieData(){
        PieDataSet pieDataSet = (PieDataSet) getData(new PieDataSet(getPieEntries(),"" ));
        pieDataSet.setSliceSpace(5);
        pieDataSet.setValueFormatter(new PercentFormatter());
        return  new PieData(pieDataSet);
    }
}
