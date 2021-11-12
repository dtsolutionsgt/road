package com.dts.roadp;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ConPedidoDetalle extends PBase {

    private TextView lblCliente, lblFecha, lblPedido,lblTotal;
    private ListView listaDetalle;
    private ListAdaptCFDVDet adapter;
    private ArrayList<clsClasses.clsCFDVDet> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_pedido_detalle);
        super.InitBase();

        lblCliente = (TextView)findViewById(R.id.lblCliente);
        lblFecha = (TextView)findViewById(R.id.lblFecha);
        lblPedido = (TextView)findViewById(R.id.lblPedido);
        lblTotal = (TextView)findViewById(R.id.lblTotal);
        listaDetalle = (ListView)findViewById(R.id.listaDetalle);

        lblCliente.setText(gl.PCliente);
        lblFecha.setText(gl.PFecha);
        lblPedido.setText(gl.Ppedido);
        lblTotal.setText("Total: "+gl.PTotal);
        setDetalle();
    }

    public void setDetalle() {

        Cursor DT;
        clsClasses.clsCFDVDet vItem;
        Double total = 0.0;
        items = new ArrayList<clsClasses.clsCFDVDet>();

        sql = "SELECT D_PEDIDOD.PRODUCTO,P_PRODUCTO.DESCLARGA,D_PEDIDOD.CANT,D_PEDIDOD.PRECIODOC, " +
                "D_PEDIDOD.IMP, D_PEDIDOD.DES,D_PEDIDOD.DESMON, D_PEDIDOD.TOTAL, D_PEDIDOD.UMVENTA, D_PEDIDOD.UMPESO " +
                "FROM D_PEDIDOD INNER JOIN P_PRODUCTO ON D_PEDIDOD.PRODUCTO = P_PRODUCTO.CODIGO " +
                "WHERE (D_PEDIDOD.COREL='"+gl.Ppedido+"')";

        DT=Con.OpenDT(sql);

        if (DT.getCount() > 0) {
            DT.moveToFirst();

            while (!DT.isAfterLast()) {
                vItem = clsCls.new clsCFDVDet();

                vItem.Producto = DT.getInt(0);
                vItem.Desclarga = DT.getString(1);
                vItem.Cant  = DT.getDouble(2);
                vItem.PrecioDoc  =DT.getDouble(3);
                vItem.Imp  = DT.getDouble(4);
                vItem.Des  = DT.getDouble(5);
                vItem.Desmon  = DT.getDouble(6);
                vItem.Total  = DT.getDouble(7);
                vItem.Umventa  = DT.getString(8);
                vItem.Umpeso  = DT.getString(9);

                items.add(vItem);
                DT.moveToNext();
            }
        }

        adapter = new ListAdaptCFDVDet(this, items);
        listaDetalle.setAdapter(adapter);
    }

    public void clear_globals() {
        gl.PCliente = "";
        gl.PFecha = "";
        gl.Ppedido = "";
        gl.PTotal = "";
    }

    @Override
    public void onBackPressed() {
        clear_globals();
        super.onBackPressed();
    }
}