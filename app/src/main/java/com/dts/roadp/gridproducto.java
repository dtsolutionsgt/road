package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class gridproducto extends PBase {

    private GridView gridView;
    private ListAdaptProductoGrid adaptergrid;
    private RelativeLayout relbotpan;

    private int selId,selIdx,iicon;
    private String rutatipo,codProd;
    private int act,prodtipo;

    private boolean rutapos,horizpos;
    boolean ordPorNombre;

    private ArrayList<clsCD> items= new ArrayList<clsCD>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridproducto);

        super.InitBase();

        gridView = (GridView) findViewById(R.id.dgprods);
        relbotpan = (RelativeLayout) findViewById(R.id.relbotpan);

        iicon=1;

        selId=-1;selIdx=-1;

        prodtipo=gl.prodtipo;
        gl.prodtipo=0;

        ordPorNombre=gl.peOrdPorNombre;

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

                    clsCD vItem = (clsCD) adaptergrid.getItem(position);
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

            clsCD vItem;
            String vF;
            items.clear();selIdx=-1;

            try {

                switch (prodtipo) {
                    case 0: // Preventa
                        sql="SELECT CODIGO,DESCLARGA,DESCCORTA,UNIDBAS FROM P_PRODUCTO WHERE 1=1 ";
                       /* if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (LINEA='"+famid+"') ";
                        if (vF.length()>0) {sql=sql+"AND ((DESCCORTA LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}
                        */
                        if (ordPorNombre) sql+="ORDER BY DESCCORTA"; else sql+="ORDER BY CODIGO";
                        break;

                }

                DT=Con.OpenDT(sql);

                if (DT.getCount() >0){

                    DT.moveToFirst();

                    while (!DT.isAfterLast()) {

                        vItem = clsCls.new clsCD();

                        vItem.Cod=DT.getString(0);
                        vItem.DesLarga=DT.getString(1);
                        vItem.Desc=DT.getString(2);
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
