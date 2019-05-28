package com.dts.roadp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.dts.roadp.clsClasses.clsProd;

import java.io.File;
import java.util.ArrayList;

public class gridproducto extends PBase {

    private GridView gridView;
    private ListAdaptProductoGrid adaptergrid;
    private RelativeLayout relbotpan;

    private int selId,selIdx,iicon;
    private String rutatipo,codProd;

    private boolean rutapos,horizpos;

    private ArrayList<clsProd> items= new ArrayList<clsProd>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridproducto);

        rutapos=false;gl.rutapos=false;

        iicon=1;

        selId=-1;selIdx=-1;

        int ori=this.getResources().getConfiguration().orientation; // 1 - portrait , 2 - landscape
        horizpos=ori==2;

        if (horizpos) {
            gridView.setNumColumns(3);relbotpan.setVisibility(View.GONE);
        } else {
            gridView.setNumColumns(3);relbotpan.setVisibility(View.VISIBLE);
        }

        this.setTitle("ROAD");
        listItems();

        setHandlers();

    }

    public void setHandlers(){
        try{
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

                    clsProd vItem = (clsProd) adaptergrid.getItem(position);
                    codProd=vItem.Cod;

                    adaptergrid.setSelectedIndex(position);

                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    public void listItems() {
        Cursor DT;

        try{

            clsProd vItem;
            String vCod="",prodimg;
            items.clear();selIdx=-1;

            try {


                sql = "SELECT PM.CODIGO,P.DESCLARGA,P.DESCCORTA FROM P_PRODIMAGEN PM INNER JOIN \n" +
                        "P_PRODUCTO P ON P.CODIGO = PM.CODIGO";

                DT=Con.OpenDT(sql);

                if (DT.getCount() >0){

                    DT.moveToFirst();

                    while (!DT.isAfterLast()) {

                        vItem = clsCls.new clsProd();

                        vItem.Cod=DT.getString(0);
                        vItem.DesLarga=DT.getString(1);
                        vItem.DesCorta=DT.getString(2);
                        items.add(vItem);

                        DT.moveToNext();
                    }

                }


            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

            adaptergrid=new ListAdaptProductoGrid(this, items);
            gridView.setAdapter(adaptergrid);
            adaptergrid.setSelectedIndex(selIdx);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

}
