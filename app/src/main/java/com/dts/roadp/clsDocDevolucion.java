package com.dts.roadp;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

public class clsDocDevolucion extends clsDocument {

    private ArrayList<itemData> items= new ArrayList<itemData>();

    private double tot,desc,imp,stot,percep;
    private boolean sinimp;
    private String 	contrib,recfact,estadoDev,corelNC,corelF,asignacion;

    public clsDocDevolucion(Context context, int printwidth, String cursym, int decimpres, String archivo) {
        super(context, printwidth, cursym, decimpres, archivo);
        docpedido=false;
        docfactura=false;
        docrecibo=false;
        docdevolucion=true;
    }

    protected boolean buildDetail() {
        itemData item;
        int onceMerc = 0;

        //rep.add("");

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            if(onceMerc==0){
                rep.addp("Mercancia "+ item.estado,"");
                onceMerc = 1;
            }

        }

        rep.add("");

        detailToledano();
        return true;
    }

    protected boolean detailToledano() {
        itemData item;
        String ss;

        rep.add("");
        rep.add("CODIGO   DESCRIPCION            UM  CANT");
        rep.add("         PRECIO                    VALOR");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
            ss=ss+rep.rtrim(item.um,4)+" "+rep.rtrim(frmdecimal(item.cant,2),5);
            rep.add(ss);

            //ss=rep.rtrim(frmdecimal(item.peso,2),10)+" "+rep.rtrim(frmdecimal(item.prec,2),8); #AT Se quitó el Kilogramos
            ss="        "+rep.rtrim(frmdecimal(item.prec,2),8);
            ss=rep.ltrim(ss,prw-10);
            ss=ss+" "+rep.rtrim(frmdecimal(item.tot,2),9);
            rep.add(ss);

        }

        rep.line();

        return true;
    }

    protected boolean buildFooter() {

        rep.add("");
        rep.addtot("TOTAL NOTA CREDITO ", tot);
        rep.add("");
        rep.add("");
        rep.add("");
        rep.line();
        rep.add("            FIRMA VENDEDOR               ");
        rep.empty();
        rep.empty();
        rep.line();
        rep.add("            NOMBRE VENDEDOR             ");
        rep.empty();

        if(!corelF.isEmpty()) {
            rep.addc("Aplica a factura: " + corelF);
        }

        rep.add("No. Serie : "+deviceid);
        rep.add(resol);
        rep.add(resfecha);
        rep.add("");

        return super.buildFooter();
    }

    protected boolean loadHeadData(String corel) {
        Cursor DT;
        String cli,vend,val, anulado;
        int ff, impres, cantimpres;

        super.loadHeadData(corel);

        nombre="NOTA DE CREDITO";

        try {
            sql="SELECT N.RUTA,N.VENDEDOR,N.CLIENTE,N.TOTAL,N.FECHA,N.COREL, N.ANULADO, N.IMPRES "+
                "FROM D_NOTACRED N "+
                "WHERE N.COREL = '"+corel+"'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            serie=DT.getString(5);
            ruta=DT.getString(0);

            vend=DT.getString(1);
            cli=DT.getString(2);

            tot=DT.getDouble(3);
            ffecha=DT.getInt(4);fsfecha=sfecha(ffecha);

            anulado=DT.getString(6);
            impres=DT.getInt(7);
            cantimpres=0;

            if (anulado.equals("S")?true:false){
                cantimpres = -1;
            }else if (cantimpres == 0 && impres > 0){
                cantimpres = 1;
            }

            if (cantimpres>0){
                nombre = "COPIA DE NOTA DE CREDITO";
            }else if (cantimpres==-1){
                nombre = "NOTA DE CREDITO ANULADA";
            }else if (cantimpres==0){
                nombre = "NOTA DE CREDITO";
            }

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

        try {
            sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION,NIT FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            val=DT.getString(0);
            percep=DT.getDouble(1);

            contrib=""+DT.getString(2);
            if (contrib.equalsIgnoreCase("C")) sinimp=true;
            if (contrib.equalsIgnoreCase("F")) sinimp=false;

            clicod=cli;
            clidir=DT.getString(3);
            nit=DT.getString(4);

        } catch (Exception e) {
            val=cli;
        }

        cliente=val;

        return true;

    }

    protected boolean loadDocData(String corel) {
        Cursor DT;
        itemData item;

        loadHeadData(corel);

        items.clear();

        try {
            corelF="";

            sql="SELECT N.COREL,F.ASIGNACION, F.SERIE, F.CORELATIVO " +
                 "FROM D_NOTACRED N INNER JOIN D_FACTURA F ON F.COREL = N.FACTURA " +
                 "WHERE N.COREL = '"+corel+"'";

            DT=Con.OpenDT(sql);

            if(DT.getCount() != 0){

                DT.moveToFirst();
                corelNC = DT.getString(0);
                asignacion = DT.getString(1); //Tengo el campo corel de la tabla D_CxC
                corelF = DT.getString(2) + StringUtils.right("000000" + Integer.toString(DT.getInt(3)), 6);

            }else{

                sql="SELECT N.COREL, N.FACTURA " +
                        "FROM D_NOTACRED N " +
                        "WHERE N.COREL = '"+corel+"'";

                DT=Con.OpenDT(sql);

                if(DT.getCount() >0){

                    DT.moveToFirst();
                    corelNC = DT.getString(0);
                    asignacion = DT.getString(1); //Tengo el campo corel de la tabla D_CxC
                    corelF = "";
                }
            }

            sql="SELECT C.CODIGO,P.DESCLARGA,C.ESTADO,C.PESO,C.PRECIO,C.TOTAL,C.CANT,C.UMVENTA " +
                "FROM D_CxCD C INNER JOIN P_PRODUCTO P ON C.CODIGO = P.CODIGO " +
                "WHERE (C.COREL='"+asignacion+"')";
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    item =new itemData();

                    item.cod=DT.getString(0);
                    item.nombre=DT.getString(1);
                    item.estado=DT.getString(2);
                    item.peso=DT.getDouble(3);
                    item.prec=DT.getDouble(4);
                    item.tot=DT.getDouble(5);
                    item.cant=DT.getDouble(6);
                    item.um=DT.getString(7);

                    items.add(item);

                    DT.moveToNext();

                    if (item.estado.equals("B")){
                        item.estado="En Buen Estado";
                    }else{
                        item.estado="En Mal Estado";
                    }
                }

            }

        } catch (Exception e) {

        }

        return true;
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
        public String cod,nombre,estado,um;
        public double cant,prec,tot,peso;
    }
}
