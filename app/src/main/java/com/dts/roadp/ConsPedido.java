package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ConsPedido extends PBase {

    private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
    private ListAdaptCFDV adapter;
    private ListView listaPedido;
    private TextView lblTotal;
    private String itemPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cons_pedido);
        super.InitBase();
        listaPedido = (ListView)findViewById(R.id.pedidoLista);
        lblTotal = (TextView)findViewById(R.id.lblTotal);
        getPedidos();
        setHandlers();
    }

    public void getPedidos() {

        Cursor DT;
        clsClasses.clsCFDV vItem;
        Double total = 0.0;

        sql = "SELECT " +
                "D_PEDIDO.COREL," +
                "P_CLIENTE.NOMBRE," +
                "D_PEDIDO.FECHA," +
                "D_PEDIDO.TOTAL," +
                "D_PEDIDO.ANULADO," +
                "D_PEDIDO.STATCOM "+
                "FROM D_PEDIDO " +
                "INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO "+
                "ORDER BY D_PEDIDO.COREL DESC";

        DT=Con.OpenDT(sql);

        if (DT.getCount() > 0) {
            DT.moveToFirst();

            while (!DT.isAfterLast()) {
                vItem = clsCls.new clsCFDV();

                vItem.Cod = DT.getString(0);
                vItem.Desc = DT.getString(1);
                vItem.Fecha  = du.sfecha(DT.getLong(2));
                vItem.Valor  = mu.frmcur(DT.getDouble(3));
                vItem.Anulado  = DT.getString(4);
                vItem.Statuscom  = DT.getString(5);
                vItem.Flag = 2;

                total += DT.getDouble(3);

                items.add(vItem);
                DT.moveToNext();
            }

            lblTotal.setText("TOTAL: " + mu.frmcur(total).toString());
        }

        adapter = new ListAdaptCFDV(this, items);
        listaPedido.setAdapter(adapter);
    }

    private void setHandlers(){
        try{
            listaPedido.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = listaPedido.getItemAtPosition(position);
                        clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

                        itemPedido = vItem.Cod;
                        adapter.setSelectedIndex(position);

                        gl.Ppedido = itemPedido;
                        gl.PFecha = vItem.Fecha;
                        gl.PCliente = vItem.Desc;
                        gl.PTotal = vItem.Valor;

                        ver_detalle();

                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        mu.msgbox( e.getMessage());
                    }
                };
            });

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void ver_detalle() {
        Intent intent = new Intent(this, ConPedidoDetalle.class);
        startActivity(intent);
    }
}