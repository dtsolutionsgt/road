package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import java.security.spec.ECField;
import java.util.ArrayList;
import com.dts.roadp.clsClasses.clsFinDiaItems;

//#HS_20181121_1548 Se agregó lpa clase para FinDia.
public class clsFinDia extends PBase{

    private ArrayList<clsFinDiaItems> items = new ArrayList<clsFinDiaItems>();

    private int active, corelz;
    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private Context cont;

    public clsFinDia(Context context){
        try{
            cont = context;
            active=0;
            Con = new BaseDatos(context);
            opendatabase();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void opendatabase() {
        try {
            db = Con.getWritableDatabase();
            if (db!= null)
            {
                Con.vDatabase =db;
                active=1;
            }else{
                active = 0;
            }
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
            active= 0;
        }
    }

    public int getCantFactura(){
        Cursor DT;
        int result=0;

        try
        {
            sql="SELECT COUNT(COREL) FROM D_FACTURA";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            result = DT.getInt(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getCantFactura: " + e.getMessage());
        }

        return result;
    }

    public int getCantFacturaAnuluadas(){
        Cursor DT;
        int result=0;

        try
        {
            sql="SELECT COUNT(COREL) FROM D_FACTURA WHERE DEPOS<>'S'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            result = DT.getInt(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getCantFacturaAnuluadas: " + e.getMessage());
        }

        return result;
    }

    public int getCantFacturaEfectuada(){
        Cursor DT;
        int result=0;

        try
        {
            sql="SELECT COUNT(COREL) FROM D_FACTURA WHERE DEPOS<>'N'";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            result = DT.getInt(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getCantFacturaEfectuada: " + e.getMessage());
        }

        return result;
    }

    public int setCorrelZ(){
        Cursor DT;
        int corelz=0;

        try {
            sql="SELECT Corel FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                corelz=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("setCorrelZ: "+ e.getMessage());
        }
        return corelz;
    }

    public int ultimoCierreFecha() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val1 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("ultimoCierreFecha: " + e.getMessage());
        }
        return rslt;
    }

    public int getDevBodega() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val5 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getDevBodega: " + e.getMessage());
        }
        return rslt;
    }

    public void updateDevBodega(){
        try{
            sql="UPDATE FinDia SET val5="+1;
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateDevBodega: " + e.getMessage());
        }
    }

    public int getDeposito() {
        Cursor DT;
        int rslt=0;

        try 
        {
            sql="SELECT val4 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getdeposito: " + e.getMessage());
        }
        return rslt;
    }

    public void updateDeposito(){
        try{
            sql="UPDATE FinDia SET val4="+2;
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateDeposito: " + e.getMessage());
        }
    }

    public int getImpresionDeposito() {

        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val3 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getImpDeposito: " + e.getMessage());
        }
        return rslt;
    }

    public void updateImpDeposito(){
        try{
            sql="UPDATE FinDia SET val3="+3;
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateImpDeposito: " + e.getMessage());
        }
    }

    public int getcomunicacion() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val2 FROM FinDia";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                rslt=DT.getInt(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("getcomunicacion: " + e.getMessage());
        }
        return rslt;
    }

    public void updateComunicacion(){
        try{
            sql="UPDATE FinDia SET val2="+4;
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateComunicacion: " + e.getMessage());
        }
    }

    public void updateCorrelativoZ(){
        try{
            sql="UPDATE FinDia SET Corel="+1;
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateCorrelativoZ: " + e.getMessage());
        }
    }

    public void updateFinDia(){
        try{
            sql="UPDATE FinDia SET val1="+du.getActDate();
            db.execSQL(sql);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox("updateFinDia: " + e.getMessage());
        }
    }

    //#HS_20181127_1052 Agregue funcion que elimina los datos de las tablas D.
    public boolean eliminarTablasD(){
        Cursor DT;
        String corel;
        corelz=setCorrelZ();

        try {

            db.beginTransaction();

            sql="SELECT COREL FROM D_FACTURA WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_FACTURA WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAP WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_FACTURAD_LOTES WHERE COREL='"+corel+"'";db.execSQL(sql);

                    sql="DELETE FROM D_BONIF";db.execSQL(sql);
                    sql="DELETE FROM D_BONIF_LOTES";db.execSQL(sql);
                    sql="DELETE FROM D_REL_PROD_BON";db.execSQL(sql);
                    sql="DELETE FROM D_BONIFFALT";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_PEDIDO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_PEDIDO WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_PEDIDOD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    sql="DELETE FROM D_BONIF";db.execSQL(sql);
                    sql="DELETE FROM D_REL_PROD_BON";db.execSQL(sql);
                    sql="DELETE FROM D_BONIFFALT";db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql="SELECT COREL FROM D_COBRO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel=DT.getString(0);

                    sql="DELETE FROM D_COBRO WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_COBROD WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_COBROP WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_DEPOS WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_DEPOS WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_DEPOSD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_MOV WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_MOV WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_MOVD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }
            //sql="DELETE FROM D_MOVD WHERE CODIGOLIQUIDACION=0";db.execSQL(sql);


            sql="SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_CLINUEVO WHERE CODIGO='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_CLINUEVO_APR WHERE CODIGO='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="SELECT COREL FROM D_SOLICINV WHERE STATCOM='S'";
            DT=Con.OpenDT(sql);
            if (DT.getCount()>0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel=DT.getString(0);
                    sql="DELETE FROM D_SOLICINV WHERE COREL='"+corel+"'";db.execSQL(sql);
                    sql="DELETE FROM D_SOLICINVD WHERE COREL='"+corel+"'";db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql="DELETE FROM D_ATENCION";db.execSQL(sql);
            sql="DELETE FROM D_CLICOORD WHERE STATCOM='S'";db.execSQL(sql);


            corelz++;
            sql="UPDATE FinDia SET Corel="+corelz;
            db.execSQL(sql);

			/*sql="UPDATE P_RUTA SET Email='0'";
			db.execSQL(sql);*/

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            //mu.msgbox("Error : " + e.getMessage());
            return false;
        }

        return true;
    }


}
