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

        nombre="DEVOLUCION DE CANASTA";

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

            sql="SELECT M.PRODUCTO,P.DESCLARGA,M.CANT,M.PESO,M.LOTE,M.UNIDADMEDIDA "+
                "FROM D_MOVDCAN M INNER JOIN P_PRODUCTO P ON M.PRODUCTO=P.CODIGO "+
                "WHERE M.COREL='"+corel+"'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

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

        detailToledano();
        return true;
    }

    protected boolean detailToledano() {
        itemData item;
        String ss;

        rep.add("");
        rep.add("CODIGO   DESCRIPCION        UM  VALOR");
        rep.add("            CANTIDAD           PRECIO");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
            ss=ss+rep.rtrim(item.um,4)+" "+rep.rtrim(frmdecimal(item.prec,2),5);
            rep.add(ss);
            ss=rep.rtrim("",2)+" "+rep.rtrim(frmdecimal(item.cant,2),8);
            ss=rep.ltrim(ss,prw-10);
            ss=ss+" "+rep.rtrim(frmdecimal(item.tot,2),9);
            rep.add(ss);

        }

        rep.line();

        return true;
    }

    protected boolean buildFooter() {


        rep.add("");
        rep.addtot("TOTAL ", tot);
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
        public double cant,prec,tot,peso;
    }
}
