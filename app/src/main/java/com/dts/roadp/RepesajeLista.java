package com.dts.roadp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class RepesajeLista extends PBase {

    private ListView listView;
    private TextView lblProd,lblPres,lblCant,lblPrec,lblTot;

    private ArrayList<clsClasses.clsCD> items= new ArrayList<clsClasses.clsCD>();
    private ListAdaptRepesList adapter;

    private String prodid;
    private boolean esbarra;

    private AppMethods app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repesaje_lista);

        super.InitBase();

        listView = (ListView) findViewById(R.id.listView1);

        lblProd= (TextView) findViewById(R.id.textView31);
        lblPres= (TextView) findViewById(R.id.textView42);
        lblCant= (TextView) findViewById(R.id.textView35);
        lblPrec= (TextView) findViewById(R.id.textView36);
        lblTot = (TextView) findViewById(R.id.textView37);

        prodid=gl.gstr;
        lblProd.setText(gl.gstr2);

        app = new AppMethods(this, gl, Con, db);
        esbarra=app.prodBarra(prodid);

        if (esbarra) lblPres.setText("Barra"); else lblPres.setText("Codigo");

        setHandlers();

        listItems();
    }


    // Events

    public void repesaje(View view) {
        startActivity(new Intent(this,Repesaje.class));
    }

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object lvObj = listView.getItemAtPosition(position);
                    clsClasses.clsCD vItem = (clsClasses.clsCD) lvObj;

                    prodid = vItem.Cod;
                    adapter.setSelectedIndex(position);

                } catch (Exception e) {
                    mu.msgbox(e.getMessage());
                }
            }
       });

    }


    // Main

    private void listItems() {
        if (esbarra) listItemsBarra();else listItemsSingle();
    }

    private void listItemsSingle() {
        Cursor DT;
        clsClasses.clsCD item;

        items.clear();

        try {
            sql = "SELECT PESO,TOTAL FROM T_VENTA WHERE PRODUCTO='"+prodid+"' ";

            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            item = clsCls.new clsCD();

            item.Cod = prodid;
            item.Desc = mu.frmdecimal(DT.getDouble(0),gl.peDecImp);
            item.Text = mu.frmdecimal(DT.getDouble(1),2);

            items.add(item);

            lblCant.setText("1");
            lblPrec.setText(mu.frmdecimal(DT.getDouble(0),gl.peDecImp));
            lblTot.setText(mu.frmdecimal(DT.getDouble(1),2));

        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }

        adapter=new ListAdaptRepesList(this, items);
        listView.setAdapter(adapter);
    }

    private void listItemsBarra() {

    }

    // Aux



}