package com.dts.roadp;

import android.database.Cursor;
import com.dts.roadp.clsClasses.clsAyudante;
import com.dts.roadp.clsClasses.clsVehiculo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ayudante_vehiculo extends PBase {

    private ListView listAyu,listVehi;
    private Button btnOmitir,btnAsignar;

    private ArrayList<clsClasses.clsAyudante> items = new ArrayList<clsClasses.clsAyudante>();
    private ArrayList<clsClasses.clsVehiculo> items1 = new ArrayList<clsClasses.clsVehiculo>();
    private clsAyudante selitem;
    private clsVehiculo selitem1;
    private ListAdaptAyudante adapter;
    private ListAdaptVehiculo adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayudante_vehiculo);

        listAyu =  (ListView) findViewById(R.id.listAyudante);
        listVehi =  (ListView) findViewById(R.id.listVehiculo);

        btnOmitir = (Button)findViewById(R.id.btnOmitr);
        btnAsignar = (Button)findViewById(R.id.btnAsignar);

        ListaAyudantes();
        ListaVehiculos();
    }

    private void ListaAyudantes(){

        Cursor DT;
        clsAyudante item;

        items.clear();

        try {

            sql="SELECT CODIGO,NOMBRE FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 5 ";

            DT=Con.OpenDT(sql);

            if(DT.getCount()>0) {

                DT.moveToFirst();

                while (!DT.isAfterLast())
                {

                    item = clsCls.new clsAyudante();

                    item.idayudante=DT.getString(0);
                    item.nombreayudante=DT.getString(1);

                    items.add(item);

                    DT.moveToNext();
                }

            }

            adapter=new ListAdaptAyudante(this,items);
            listAyu.setAdapter(adapter);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage()+"\n"+sql);

        }

    }

    private void  ListaVehiculos(){
        Cursor DT;
        clsVehiculo item1;

        items1.clear();

    try{

        sql="SELECT CODIGO, MARCA, PLACA FROM P_VEHICULO ";
        DT=Con.OpenDT(sql);

        if(DT.getCount()>0) {
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                item1 = clsCls.new clsVehiculo();

                item1.idVehiculo=DT.getString(0);
                item1.marca=DT.getString(1);
                item1.placa=DT.getString(1);

                items1.add(item1);

                DT.moveToNext();
            }

        }

        adapter1=new ListAdaptVehiculo(this,items1);
        listVehi.setAdapter(adapter1);

    }catch (Exception e){
        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        mu.msgbox(e.getMessage()+"\n"+sql);

    }

    }

    private void setHandlers(){

        try {
            /*listAyu.setOnItemClickListener(new OnItemClickListener(){


            });
        }catch (Exception e){
*/
        }catch (Exception e){

        }

    }

}
