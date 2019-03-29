package com.dts.roadp;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocDevolucion extends clsDocument {

    private ArrayList<itemData> items= new ArrayList<itemData>();

    private double tot,desc,imp,stot,percep;
    private boolean sinimp;
    private String 	contrib,recfact,estadoDev,corelNC,corelF,asignacion;

    public clsDocDevolucion(Context context, int printwidth, String cursym, int decimpres) {
        super(context, printwidth, cursym, decimpres);
        docpedido=false;
        docfactura=false;
        docrecibo=false;
        docdevolucion=true;
    }

    protected boolean buildDetail() {
        itemData item;
        int onceMerc = 0;

        rep.add("");

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
        rep.add("CODIGO   DESCRIPCION        UM  CANT");
        rep.add("       KGS    PRECIO           VALOR");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);

            ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
            ss=ss+rep.rtrim(item.um,4)+" "+rep.rtrim(frmdecimal(item.cant,2),5);
            rep.add(ss);
            ss=rep.rtrim(frmdecimal(item.peso,2),10)+" "+rep.rtrim(frmdecimal(item.prec,2),8);
            ss=rep.ltrim(ss,prw-10);
            ss=ss+" "+rep.rtrim(frmdecimal(item.tot,2),9);
            rep.add(ss);

        }

        rep.line();

        return true;
    }

    protected boolean buildFooter() {

        if(corelNC.equals(asignacion)){

            rep.add("");
            rep.addtot("TOTAL NOTA CREDITO ", tot);
            rep.add("");
            rep.add("");
            rep.add("");
            rep.line();
            rep.addc("Firma Cliente");
            rep.add("");
            rep.addc("Aplica a factura: "+corelF);
            rep.add("");
            rep.addc("EXIJA SU FACTURA ORIGINAL ");
            rep.addc("PARA COMPROBAR  EL PAGO. ");
            rep.add("");

            rep.add("Serial : "+deviceid);
            rep.add(resol);
            rep.add(resfecha);
            rep.add("");

        }else{

            rep.add("");
            rep.addtot("TOTAL NOTA CREDITO ", tot);
            rep.add("");
            rep.add("");
            rep.add("");
            rep.line();
            rep.addc("Firma Cliente");
            rep.add("");
            rep.addc("EXIJA SU FACTURA ORIGINAL ");
            rep.addc("PARA COMPROBAR  EL PAGO. ");
            rep.add("");

            rep.add("Serial : "+deviceid);
            rep.add(resol);
            rep.add(resfecha);
            rep.add("");

        }



        return super.buildFooter();
    }

    protected boolean loadHeadData(String corel) {
        Cursor DT;
        String cli,vend,val;
        int ff;

        super.loadHeadData(corel);

        nombre="NOTA DE CREDITO";

        try {
            sql="SELECT N.RUTA,N.VENDEDOR,N.CLIENTE,N.TOTAL,N.FECHA,N.FACTURA "+
                "FROM D_NOTACRED N INNER JOIN D_CxC C ON C.TOTAL = N.TOTAL "+
                "WHERE C.COREL = '"+corel+"'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            serie=DT.getString(5);
            ruta=DT.getString(0);

            vend=DT.getString(1);
            cli=DT.getString(2);

            tot=DT.getDouble(3);
            ffecha=DT.getInt(4);fsfecha=sfecha(ffecha);

        } catch (Exception e) {
            Toast.makeText(cont,"loadHeadData"+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
        }


        try {
            sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+vend+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            val=DT.getString(0);
        } catch (Exception e) {
            val=vend;
        }

        vendedor=val;

        try {
            sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            val=DT.getString(0);
            percep=DT.getDouble(1);

            contrib=""+DT.getString(2);
            if (contrib.equalsIgnoreCase("C")) sinimp=true;
            if (contrib.equalsIgnoreCase("F")) sinimp=false;

            clicod=cli;
            clidir=DT.getString(3);

        } catch (Exception e) {
            val=cli;
        }

        try {
            sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            resol="Resolucion No. : "+DT.getString(0);
            ff=DT.getInt(1);resfecha="De Fecha: "+sfecha(ff);
            ff=DT.getInt(2);resvence="Vigente hasta: "+sfecha(ff);

        } catch (Exception e) {
            Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
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
            sql="SELECT N.COREL,F.ASIGNACION, F.COREL " +
                 "FROM D_NOTACRED N INNER JOIN D_FACTURA F ON F.ASIGNACION = N.COREL " +
                 "INNER JOIN D_CxC C ON N.TOTAL = C.TOTAL " +
                 "WHERE C.COREL = '"+corel+"'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            if(DT.getCount() != 0){

                corelNC = DT.getString(0);
                asignacion = DT.getString(1);
                corelF = DT.getString(2);


            }else{

                corelNC = "";
                asignacion = "*";
                corelF = "";

            }

            sql="SELECT C.CODIGO,P.DESCLARGA,C.ESTADO,C.PESO,C.PRECIO,C.TOTAL,C.CANT,C.UMVENTA " +
                "FROM D_CxCD C INNER JOIN P_PRODUCTO P ON C.CODIGO = P.CODIGO " +
                "WHERE (C.COREL='"+corel+"')";
            DT=Con.OpenDT(sql);

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
