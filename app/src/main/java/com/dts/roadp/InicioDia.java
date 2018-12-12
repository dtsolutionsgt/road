package com.dts.roadp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;


public class InicioDia extends PBase implements View.OnClickListener{

    private TextView etFecha;
    private ImageView ibObtenerFecha;

    private static final String CERO = "0";
    private static final String BARRA = "/";

    public final Calendar c = Calendar.getInstance();

    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    private int cyear, cmonth, cday, fechae;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_dia);

        etFecha = (TextView)findViewById(R.id.lblFecha);
        ibObtenerFecha = (ImageView)findViewById(R.id.imgCalendario);
        ibObtenerFecha.setOnClickListener(this);

        super.InitBase();

        setActDate();
        fechae=fecha;etFecha.setText(du.sfecha(fechae));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgCalendario:
                obtenerFecha();
                break;
        }
    }

    private void obtenerFecha(){
        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
                etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
            }
        },anio, mes, dia);

        recogerFecha.show();
    }

    private void setActDate(){
        final Calendar c = Calendar.getInstance();
        cyear = c.get(Calendar.YEAR);
        cmonth = c.get(Calendar.MONTH)+1;
        cday = c.get(Calendar.DAY_OF_MONTH);
        fecha=du.cfecha(cyear,cmonth,cday);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}