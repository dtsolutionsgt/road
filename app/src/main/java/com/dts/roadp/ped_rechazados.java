package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class ped_rechazados extends PBase {

    private ListView listView;
    private TextView lblDescrip;

    private ArrayList<clsClasses.clsPedRec> items= new ArrayList<clsClasses.clsPedRec>();
    private ListAdaptPedRec adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ped_rechazados);

        super.InitBase();
        addlog("PedRec",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        lblDescrip= (TextView) findViewById(R.id.lblDescrip);

        listItems();

    }

    public void listItems() {
        Cursor DT;
        clsClasses.clsPedRec vItem;
        int vP,f;
        double val;
        String sval;

        items.clear();
        selidx=-1;vP=0;

        try {

            sql=" SELECT P.COREL, P.FECHA, P.TOTAL, P.RAZON_RECHAZADO, C.NOMBRE "+
                    " FROM P_PEDIDO_RECHAZADO P INNER JOIN P_CLIENTE C ON P.CLIENTE = C.CODIGO " +
                    " WHERE C.CODIGO = '"+ gl.cliente +"' ORDER BY P.COREL ASC ";

            DT=Con.OpenDT(sql);

            if (DT.getCount()>0) {

                DT.moveToFirst();

                lblDescrip.setText(DT.getString(4));

                while (!DT.isAfterLast()) {

                    vItem =clsCls.new clsPedRec();

                    vItem.Factura=DT.getString(0);
                    vItem.Fecha=DT.getString(1);
                    vItem.Razon=DT.getString(3);
                    vItem.Cliente=DT.getString(4);

                    val=DT.getDouble(2);
                    try {
                        sval=mu.frmcur(val);
                    } catch (Exception e) {
                        sval=""+val;
                    }

                    vItem.Valor=sval;

                    items.add(vItem);

                    DT.moveToNext();
                }
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }

        adapter=new ListAdaptPedRec(this, items);
        listView.setAdapter(adapter);

        if (selidx>-1) {
            adapter.setSelectedIndex(selidx);
            listView.setSelection(selidx);
        }

        listView.setVisibility(View.VISIBLE);
    }

    public void regresar(View view) {
        super.finish();
    }
    @Override
    public void onBackPressed() {
        try{

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    protected void onResume() {
        try{
            super.onResume();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

}

