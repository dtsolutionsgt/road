package com.dts.roadp;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocCanastaBod extends clsDocument {

    private ArrayList<itemData> items= new ArrayList<itemData>();

    private double tot,desc,imp,stot,percep;
    private boolean sinimp;
    private String 	contrib,recfact,estadoDev,corelNC,corelF,asignacion;
    private int totitems, totcant;

    public clsDocCanastaBod(Context context, int printwidth, String cursym, int decimpres, String archivo) {
        super(context, printwidth, cursym, decimpres, archivo);
        docpedido=false;
        docfactura=false;
        docrecibo=false;
        docdevolucion=false;
        doccanastabod=true;
    }

    protected boolean loadHeadData(String corel) {
        Cursor DT;
        String vend,val;

        super.loadHeadData(corel);

        if(modofact.equalsIgnoreCase("TOL")) nombre="DEVOLUCION DE CANASTA";
        if(modofact.equalsIgnoreCase("*")) nombre="DEVOLUCION DE BODEGA";

        try {
            sql="SELECT COREL, RUTA, TIPO, REFERENCIA, USUARIO, FECHA "+
                "FROM D_MOV "+
                "WHERE ANULADO =  'N'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            serie=DT.getString(0);
            ruta=DT.getString(1);

            tipo=DT.getString(2);
            ref=DT.getString(3);
            vend=DT.getString(4);

            ffecha=DT.getInt(5);fsfecha=sfecha(ffecha);

            corel=serie;
        } catch (Exception e) {
            Toast.makeText(cont,"loadHeadData"+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
        }


        try {
            sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+vend+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            val=DT.getString(0);

            vendcod=vend;
        } catch (Exception e) {
            val=vend;
        }

        vendedor=val;

        return true;

    }

    protected boolean loadDocData(String corel) {
        Cursor DT;
        itemData item;

        loadHeadData(corel);

        items.clear();

        try {

            if(modofact.equalsIgnoreCase("TOL")) {
                sql="SELECT M.PRODUCTO,P.DESCLARGA,M.CANT,M.PESO,M.LOTE,M.UNIDADMEDIDA "+
                    "FROM D_MOVDCAN M INNER JOIN P_PRODUCTO P ON M.PRODUCTO=P.CODIGO "+
                    "WHERE M.COREL='"+corel+"'";
            }

            if(modofact.equalsIgnoreCase("*")) {
                sql="SELECT M.PRODUCTO,P.DESCLARGA,M.CANT,M.PESO,M.LOTE,M.UNIDADMEDIDA "+
                    "FROM D_MOVD M INNER JOIN P_PRODUCTO P ON M.PRODUCTO=P.CODIGO "+
                    "WHERE M.COREL='"+corel+"'";
            }


            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            totitems = DT.getCount();

            while (!DT.isAfterLast()) {

                item =new itemData();

                item.cod=DT.getString(0);
                item.nombre=DT.getString(1);
                item.cant=DT.getDouble(2);
                item.peso=DT.getDouble(3);
                item.lote=DT.getString(4);
                item.um=DT.getString(5);

                items.add(item);

                DT.moveToNext();
            }

        } catch (Exception e) {

        }

        return true;
    }

    protected boolean buildDetail() {
        rep.add("");

        if(modofact.equalsIgnoreCase("TOL")) detailToledano();
        if(modofact.equalsIgnoreCase("*")) detailToledano();

        return true;
    }

    protected boolean detailToledano() {
        itemData item;
        String ss;

        rep.add("");
        rep.add("CODIGO   DESCRIPCION        UM");
        rep.add("          CANTIDAD              PESO");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
            ss=ss+rep.rtrim(item.um,4);
            rep.add(ss);
            ss=rep.rtrim("", 6)+" "+rep.rtrim(frmdecimal(item.cant,2),8);
            ss=rep.ltrim(ss,prw-10);
            ss=ss+" "+rep.rtrim(frmdecimal(item.peso,3),9);
            rep.add(ss);

            totcant += item.cant;
            tot += item.peso;
        }

        rep.line();
        ss=rep.rtrim("", 6)+" "+rep.rtrim(frmdecimal(totcant,2),8);
        ss=rep.ltrim(ss,prw-10);
        ss=ss+" "+rep.rtrim(frmdecimal(tot,3),9);
        rep.add(ss);

        return true;
    }

    protected boolean buildFooter() {

        rep.add("");
        rep.add("Total items: " + totitems);
        rep.add("");
        rep.add("Serial : "+deviceid);
        rep.add("");

        return super.buildFooter();
    }


    // Aux

    public double round2(double val){
        int ival;

        val=(double) (100*val);
        double rslt=Math.round(val);
        rslt=Math.floor(rslt);

        ival=(int) rslt;
        rslt=(double) ival;

        return (double) (rslt/100);
    }

    private class itemData {
        public String cod,nombre,estado,um,lote;
        public double cant,peso;
    }
}
