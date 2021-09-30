package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class clsDetCobro extends clsDocument{

    private ArrayList<clsDetCobro.itemData> items= new ArrayList<clsDetCobro.itemData>();

    protected MiscUtils mu;
    protected DateUtils dtU;
    protected double tmpPago=0;

    public clsDetCobro(Context context, int printwidth, String cursym, int decimpres, String archivo) {
        super(context, printwidth, cursym, decimpres, archivo);
        mu=new MiscUtils(context,cursym);
        dtU = new DateUtils();
    }

    protected boolean buildDetail() {
        clsDetCobro.itemData item;
        double tmpTot = 0, tmpPago=0, tempSaldo=0;

        rep.line();
        rep.add("DOCUMENTO         VALOR ORIG.    EMITIDO");
        rep.add("    SALDO               PAGO       VENCE");
        rep.line();

        for(int i=0; i<items.size(); i++) {
            item=items.get(i);

            tmpPago += item.pago;
            tmpTot += item.saldo;

            rep.add3fact("No."+item.doc,item.valororig,item.emitido);
            rep.add3rrl(item.saldo,item.pago,item.vence);
        }

        rep.line();
        rep.addtot("TOTAL A PAGAR", tmpTot);
        rep.addtot("TOTAL PAGADO", tmpPago);
        rep.addtot("TOTAL DE COBRO", tmpPago + tmpTot);
        rep.line();

        return  true;
    }

    protected  boolean loadDocData(String corelCliente) {
        Cursor DT;
        clsDetCobro.itemData item;
        loadHeadData(corelCliente);
        double tmpSaldo=0;
        items.clear();

        try {
            sql = "SELECT DOCUMENTO, VALORORIG, SALDO, CANCELADO, FECHAEMIT, FECHAV, TIPODOC FROM P_COBRO WHERE CLIENTE='"+corelCliente+"'";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                while(!DT.isAfterLast()) {
                    item  = new clsDetCobro.itemData();

                    item.doc = DT.getString(0);
                    item.valororig = DT.getDouble(1);

                    item.emitido = dtU.sfecha(DT.getLong(4));
                    item.vence = dtU.sfecha(DT.getLong(5));
                    item.tipo = DT.getString(6);

                    tmpPago = getDocPago(DT.getString(0), DT.getString(6));
                    item.pago = tmpPago;

                    tmpSaldo= (tmpPago<=0)?DT.getDouble(2):DT.getDouble(2) - item.pago;
                    item.saldo = tmpSaldo;

                    items.add(item);
                    DT.moveToNext();
                }

               // tmpPago = getDocPago("0238012643", "F1");
                if(DT!=null) DT.close();
            }
        } catch (Exception e) {
            mu.msgbox("Ocurrió un error" + e.getMessage());
        }
        return  true;
    }

    protected  boolean loadHeadData(String corelCliente) {
        Cursor DT;
        super.loadHeadData(corelCliente);
        nombre = "DETALLE DE COBROS";

        sql = "SELECT CODIGO, NOMBRE, NIT, DIRECCION FROM P_CLIENTE WHERE CODIGO='"+corelCliente+"'";
        DT=Con.OpenDT(sql);

        if (DT.getCount() > 0) {
            DT.moveToFirst();

            clicod = DT.getString(0);
            cliente = DT.getString(1);
            nit = DT.getString(2);
            clidir = DT.getString(3);
            fsfecha = getFecha();
        }

        if (DT != null) DT.close();

        return  true;
    }

    protected double getDocPago(String doc,String ptipo){
        Cursor DT;
        double tp;

        try {
            sql="SELECT SUM(PAGO) FROM D_COBROD "+
                    "WHERE ANULADO='N' AND DOCUMENTO='"+doc+"' AND TIPODOC='"+ptipo+"'";
            DT=Con.OpenDT(sql);

            if(DT != null && DT.getCount()>0){

                DT.moveToFirst();

                tp=DT.getDouble(0);

            }else{
                tp=0;
            }

        } catch (Exception e) {
            mu.msgbox("Ocurrió un error " + e.getMessage()+" "+sql);
            tp=0;
        }

        return tp;
    }

    private class itemData {
        public String doc,emitido, vence,tipo;
        public double valororig, saldo, pago;
    }

    private String getFecha() {
        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        Date fecha = new Date();

        return df.format(fecha);
    }
}
