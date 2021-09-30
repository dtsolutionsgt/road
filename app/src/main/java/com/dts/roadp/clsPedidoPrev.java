package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class clsPedidoPrev extends clsDocument{
    private ArrayList<clsPedidoPrev.itemData> items= new ArrayList<clsPedidoPrev.itemData>();

    private int decimp;

    public clsPedidoPrev(Context context, int printwidth, String cursym, int decimpres, String archivo) {
        super(context, printwidth, cursym, decimpres, archivo);
    }

    protected boolean buildDetail() {
        clsPedidoPrev.itemData item;
        double tmpTot = 0;
        String cu;
        String umTemp="";

        rep.line();
        rep.add("CODIGO   DESCRIPCION                ");
        rep.add("CANT     UM             PRECIO     VALOR ");
        rep.line();

        for (int i = 0; i <items.size(); i++) {
            item=items.get(i);
            rep.add(item.cod + " " + item.nombre);

            umTemp = (item.um.length()>2?item.um.substring(0,3):item.um);
            tmpTot += item.tot;

            cu=frmdecimal(item.cant,decimp)+" "+rep.ltrim(umTemp,6);
            rep.add3fact(cu+"    ",item.prec,item.tot);
        }

        rep.line();
        rep.addtot("TOTAL A PAGAR", tmpTot);

        return true;
    }

    protected boolean loadDocData(String corelCliente) {
        Cursor DT;
        clsPedidoPrev.itemData item;
        loadHeadData(corelCliente);
        items.clear();

        try {
            sql="SELECT V.PRODUCTO, P.DESCCORTA,V.CANT, V.UM, V.PRECIO, V.TOTAL FROM " +
                    "T_VENTA AS V INNER JOIN P_PRODUCTO AS P ON P.CODIGO = V.PRODUCTO";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                item =new clsPedidoPrev.itemData();

                item.cod=DT.getString(0);
                item.nombre=DT.getString(1);
                item.cant=DT.getDouble(2);
                item.um=DT.getString(3);
                item.prec=DT.getDouble(4);
                item.tot=DT.getDouble(5);

                items.add(item);
                DT.moveToNext();
            }
            if(DT!=null) DT.close();
        } catch (Exception e) {
            Toast.makeText(cont, "Error"+ e, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    protected  boolean loadHeadData(String corelCliente) {
        Cursor DT;
        super.loadHeadData(corelCliente);
        nombre = "VISUALIZACION DEL PEDIDO";

        sql = "SELECT CODIGO, NOMBRE FROM P_CLIENTE WHERE CODIGO='"+corelCliente+"'";
        DT=Con.OpenDT(sql);

        if (DT.getCount() > 0) {
            DT.moveToFirst();

            clicod = DT.getString(0);
            cliente = DT.getString(1);
            fsfecha = getFecha();
        }

        if (DT != null) DT.close();

        return  true;
    }

    private class itemData {
        public String cod,nombre,um,ump;
        public double cant,prec,imp,descper,desc,tot, peso;
    }

    private String getFecha() {
        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        Date fecha = new Date();

        return df.format(fecha);
    }
}
