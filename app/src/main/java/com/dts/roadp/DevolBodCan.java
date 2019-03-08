package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class DevolBodCan extends PBase {

    private ListView listView;
    private TextView lblReg;
    private ImageView imgNext;

    //private ArrayList<clsClasses.clsExist> items= new ArrayList<clsClasses.clsExist>();
    //private ListAdaptExist adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devol_bod_can);

        super.InitBase();
        addlog("DevolBodCan",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);
        lblReg = (TextView) findViewById(R.id.textView61);lblReg.setText("");
        imgNext = (ImageView) findViewById(R.id.imgTitLogo);

        setHandlers();

        listItems();

    }


    //region Events

    public void doSave(View view) {
        try{
            if (!validaDevolucion()) {
                msgbox("La devolución está vacia, no se puede aplicar");
            } else {
                msgAskSave("Aplicar la devolución");
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void setHandlers() {

        try{
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Object lvObj = listView.getItemAtPosition(position);
                        //clsClasses.clsExist item = (clsClasses.clsExist) lvObj;

                        //adapter.setSelectedIndex(position);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }
                }
            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Main

    private void listItems() {

    }

    private void save() {
        Cursor DT;
        String corel,pcod,plote,um;
        Double pcant,pcantm,ppeso;

        corel=gl.ruta+"_"+mu.getCorelBase();

        try {

            db.beginTransaction();

            ins.init("D_MOV");
            ins.add("COREL",corel);
            ins.add("RUTA",gl.ruta);
            ins.add("ANULADO","N");
            ins.add("FECHA",du.getActDate());
            ins.add("TIPO","D");
            ins.add("USUARIO",gl.vend);
            ins.add("REFERENCIA","Devolucion");
            ins.add("STATCOM","N");
            ins.add("IMPRES",0);
            ins.add("CODIGOLIQUIDACION",0);

            db.execSQL(ins.sql());

            sql="SELECT CODIGO,LOTE,SUM(CANT),SUM(CANTM),SUM(PESO),UNIDADMEDIDA FROM P_STOCK GROUP BY CODIGO,LOTE,UNIDADMEDIDA " +
                "HAVING SUM(CANT)>0 OR SUM(CANTM) >0";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod=DT.getString(0);
                    plote=DT.getString(1);
                    pcant=DT.getDouble(2);
                    pcantm=DT.getDouble(3);
                    ppeso=DT.getDouble(4);
                    um=DT.getString(5);

                    ins.init("D_MOVD");

                    ins.add("COREL",corel);
                    ins.add("PRODUCTO",pcod);
                    ins.add("CANT",pcant);
                    ins.add("CANTM",pcantm);
                    ins.add("PESO",ppeso);
                    ins.add("PESOM",ppeso);
                    ins.add("LOTE",plote);
                    ins.add("CODIGOLIQUIDACION",0);
                    ins.add("UNIDADMEDIDA",um);

                    db.execSQL(ins.sql());

                    DT.moveToNext();
                }
            }

            sql="DELETE FROM P_STOCK";
            db.execSQL(sql);

            sql="UPDATE FinDia SET val5 = 5";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

            gl.closeVenta=true;
            finish();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            mu.msgbox( e.getMessage());
        }

    }

    //endregion

    //region Aux

    private boolean validaDevolucion() {
        Cursor dt;
        int cantstock=0,cantcan=0;

        try {
            sql="SELECT CANT FROM P_STOCK WHERE CANT+CANTM>0";
            dt=Con.OpenDT(sql);

            cantstock=dt.getCount();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }

        return (cantstock+cantcan>0);
    }

    private void msgAskSave(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Devolución a bodega");
            dialog.setMessage("¿" + msg + "?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    save();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

}
