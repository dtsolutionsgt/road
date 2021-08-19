package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class activity_despacho_list extends PBase {

    private ListView listView;
    private TextView lblCantReg, txtClienteDespacho;
    private LA_Ds_pedido adapter;
    private clsDs_pedidoObj Ds_pedidoObj;

    private String clinom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despacho_list);

        super.InitBase();

        listView = (ListView) findViewById(R.id.lvDespacho);
        lblCantReg = (TextView) findViewById(R.id.lblCant2);
        txtClienteDespacho = (TextView) findViewById(R.id.txtClienteDespacho);

        Ds_pedidoObj=new clsDs_pedidoObj(this,Con,db);

        setHandlers();

        listItems();

        gl.modpedid="";

    }

//region Events

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsDs_pedido item = (clsClasses.clsDs_pedido)lvObj;

                adapter.setSelectedIndex(position);

                gl.iddespacho=item.corel;
                gl.cliente = item.cliente;

                iniciaVenta();

            };
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {

            selid = 0;

            if (position>0){
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsDs_pedido item = (clsClasses.clsDs_pedido)lvObj;

                adapter.setSelectedIndex(position);

                gl.iddespacho=item.corel;
                gl.cliente = item.cliente;

                msgbox("Quiere cancelar la entrega de este pedido");

            }

            return true;
        });

    }

//endregion

//region Main

    private void listItems() {

        gl.iddespacho="";

        try {
            Ds_pedidoObj.fill("WHERE (CLIENTE='"+gl.cliente+"') AND (BANDERA='N')");

            for (int i = 0; i <Ds_pedidoObj.count; i++) {
                nombreCliente(Ds_pedidoObj.items.get(i).cliente);
                Ds_pedidoObj.items.get(i).add1=clinom;
            }

            adapter=new LA_Ds_pedido(this ,this,Ds_pedidoObj.items);
            listView.setAdapter(adapter);
            lblCantReg.setText("Total: " + Ds_pedidoObj.count);
            txtClienteDespacho.setText(clinom);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void iniciaVenta() {
        Cursor DT;

        gl.closeCliDet = false;
        gl.closeVenta = false;
        gl.credito=0;
        gl.banderaCobro = false;
        gl.rutatipo="V";

        try {

            sql="SELECT NOMBRE,NOMBRE_PROPIETARIO,DIRECCION,ULTVISITA,TELEFONO,LIMITECREDITO,NIVELPRECIO,PERCEPCION,TIPO_CONTRIBUYENTE, " +
                    "COORX,COORY,MEDIAPAGO,NIT,VALIDACREDITO,BODEGA,CHEQUEPOST,TIPO,DIACREDITO "+
                    "FROM P_CLIENTE WHERE CODIGO='"+gl.cliente+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            gl.nivel=DT.getInt(6);
            gl.percepcion=DT.getDouble(7);
            gl.contrib=""+DT.getString(8);;
            gl.media=DT.getInt(11);
            gl.fnombre=DT.getString(0);
            gl.fnit=DT.getString(12);
            gl.fdir=DT.getString(2);
            gl.vcredito = DT.getString(13).equalsIgnoreCase("S");
            gl.vcheque = DT.getString(14).equalsIgnoreCase("S");
            gl.vchequepost = DT.getString(15).equalsIgnoreCase("S");
            gl.clitipo = DT.getString(16);

            startActivity(new Intent(activity_despacho_list.this,Venta.class));
            finish();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

//endregion

//region Aux

    private void nombreCliente(String cliid) {
        Cursor dt;

        clinom="";
        try {
            sql="SELECT Nombre FROM P_CLIENTE WHERE Codigo='"+cliid+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                clinom=dt.getString(0);
            }

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

//endregion

//region Dialogs


//endregion

//region Activity Events

    @Override
    protected void onResume() {
        super.onResume();
        try {
           Ds_pedidoObj.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

//endregion

}