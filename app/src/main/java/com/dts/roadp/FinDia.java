package com.dts.roadp;

import java.io.File;
import java.io.StringReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

//  validaFinDia - Fin de Dia ya fue efectuado 
//  devproductos - delete from P_stock


public class FinDia extends PBase {

    private TextView lbl1;
    private ImageView img1;
    private ProgressBar pBar;

    private clsRepBuilder rep;
    private Runnable printclose;
    private printer prn;

    private String rutatipo, fserie, sp, devcorel;
    private int corelz, fac, faca, cfac, cfaca, rec, reca, ptot, ped, peda, fcorel, mw;
    private double val, tot, tte, ttc, ttk, tto, tre, trc, tro, tote, totc, depe, depc, bale, balc;
    private boolean idle = true, fullfd, fail;
    private clsFinDia claseFinDia;
    private DateUtils claseDateUtils;
    private AppMethods claseAppMethods;
    private clsDocument claseDocumento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_dia);

        super.InitBase();

        lbl1 = (TextView) findViewById(R.id.textView17);
        img1 = (ImageView) findViewById(R.id.imageView9);
        pBar = (ProgressBar) findViewById(R.id.progressBar1);
        pBar.setVisibility(View.INVISIBLE);

        rutatipo = ((appGlobals) vApp).rutatipog;

        fullfd = false;
        if (rutatipo.equalsIgnoreCase("T")) {
            fullfd = true;
        } else {
            if (rutatipo.equalsIgnoreCase("V")) fullfd = true;
            else fullfd = false;
        }

        if (gl.banderafindia) {
            lbl1.setVisibility(View.VISIBLE);
            img1.setVisibility(View.VISIBLE);
        } else {
            lbl1.setVisibility(View.INVISIBLE);
            img1.setVisibility(View.INVISIBLE);
        }

        rep = new clsRepBuilder(this, gl.prw, false, gl.peMon, gl.peDecImp);
        mw = 6 + gl.peDecImp + 7;

        printclose = new Runnable() {
            public void run() {
                FinDia.super.finish();
            }
        };

        prn = new printer(this, printclose);

    }

    public void iniciaCierre(View view) {
        //#HS_20181128_0906 Agregue validacion para FinDia.
        if (gl.banderafindia == true) {
            if (validaFinDia()) msgAskFinDiaTrue();
        } else {
            if (validaFinDia()) msgAsk();
        }
    }

    //#HS_20181123_0950 Agrege funcion para llamar activity deposito.
    public void ActivityDeposito() {
        Intent deposito = new Intent(this, Deposito.class);
        startActivity(deposito);
    }

    //#HS_20181123_1014 Agregue funcion para llamar activity de reimpresion de deposito.
    public void ActivityImpresionDeposito(int doctipo) {
        gl.tipo = doctipo;
        Intent intent = new Intent(this, Reimpresion.class);
        startActivity(intent);
    }

    public void ActivityMenu() {
        Intent menu = new Intent(this, Menu.class);
        startActivity(menu);
    }

	public void startFDD()  {

		boolean rslt;

		File fd=new File(Environment.getExternalStorageDirectory()+"/SyncFold/findia.txt");
		FileUtils.deleteQuietly(fd);

        idle = false;
        fail = false;

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			//#EJC20190226: En comentario porque agregué el insert de d_mov encabezado, se debe hacer en otra pantalla el insert del detalle de lo que hay que devoler.
		    //devProductos();
			buildReportsTOL();
		} else {
			//devProductos();
			buildReports();
		}

        if (fail) rslt = false;
        else rslt = completeProcess();

        pBar.setVisibility(View.INVISIBLE);
        idle = true;

        if (!rslt) {
            msgExit("Proceso cierre del día falló. Intente de nuevo, por favor.");
            delPrintFiles();
            return;
        }

		try {
			File f1 = new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt");
			File f2 = new File(Environment.getExternalStorageDirectory() + "/print.txt");
			FileUtils.copyFile(f1, f2);
		} catch (Exception e) {
			msgExit("No se pudo crear archivo de impresión.");
			return;
		}

        try  {
            db.execSQL("UPDATE FinDia SET val1="+du.getActDate());
        } catch (Exception e) {
            msgbox("No se pudo actualizar fecha de cierre.");
        }

        Toast.makeText(FinDia.this, "Cierre del día completo.", Toast.LENGTH_SHORT).show();

		gl.findiaactivo=true;
		gl.modoadmin = false;
		gl.autocom = 1;

        startActivity(new Intent(this, ComWS.class));

        try  {

            if (prn.isEnabled())  {
                final Handler shandler = new Handler();
                shandler.postDelayed(new Runnable() {
                    @Override
                    public void run()  {
                        Intent intent = new Intent(FinDia.this, PrintDialog.class);
                        startActivity(intent);
                    }
                }, 2000);
            }
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        FinDia.super.finish();
    }

	//#HS_20181127_1052 Agregué funcion de inicio de FinDia.
	public void iniciarFD()  {

        File fd = new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt");
        FileUtils.deleteQuietly(fd);

        idle = false;
        fail = false;

        if (gl.peModal.equalsIgnoreCase("TOL")) {
            devProductos();
            buildReportsTOL();
        } else {
            //devProductos();
            buildReports();
        }

        ((appGlobals) vApp).modoadmin = false;
        ((appGlobals) vApp).autocom = 1;
        startActivity(new Intent(this, ComWS.class));
    }

    public boolean completeProcess() {
        Cursor DT;
        String corel;

        try {

            db.beginTransaction();

            sql = "SELECT COREL FROM D_FACTURA WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_FACTURA WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAP WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAD_LOTES WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURAF WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_FACTURA_STOCK WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    sql = "DELETE FROM D_BONIF";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIF_LOTES";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_REL_PROD_BON";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIFFALT";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_PEDIDO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_PEDIDO WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_PEDIDOD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    sql = "DELETE FROM D_BONIF";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_REL_PROD_BON";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_BONIFFALT";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }

            sql = "SELECT COREL FROM D_COBRO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {

                    corel = DT.getString(0);

                    sql = "DELETE FROM D_COBRO WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_COBROD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_COBROP WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_DEPOS WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_DEPOS WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_DEPOSD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_MOV WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_MOV WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_MOVD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }
            //sql="DELETE FROM D_MOVD WHERE CODIGOLIQUIDACION=0";db.execSQL(sql);


            sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_CLINUEVO WHERE CODIGO='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_CLINUEVO_APR WHERE CODIGO='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "SELECT COREL FROM D_SOLICINV WHERE STATCOM='S'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() > 0) {

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    corel = DT.getString(0);
                    sql = "DELETE FROM D_SOLICINV WHERE COREL='" + corel + "'";
                    db.execSQL(sql);
                    sql = "DELETE FROM D_SOLICINVD WHERE COREL='" + corel + "'";
                    db.execSQL(sql);

                    DT.moveToNext();
                }
            }


            sql = "DELETE FROM D_ATENCION";
            db.execSQL(sql);
            sql = "DELETE FROM D_CLICOORD WHERE STATCOM='S'";
            db.execSQL(sql);


            corelz++;
            sql = "UPDATE FinDia SET Corel=" + corelz;
            db.execSQL(sql);

			/*sql="UPDATE P_RUTA SET Email='0'";
			db.execSQL(sql);*/

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (SQLException e) {
            db.endTransaction();
            //mu.msgbox("Error : " + e.getMessage());
            return false;
        }

        return true;
    }

    private void buildReports() {

        rep.empty();

		rep.add("CIERRE DEL DIA");
		rep.line();
		rep.add("Vendedor : "+ gl.vend+" "+gl.vendnom);
		rep.add("Fecha : "+du.sfecha(fecha)+" "+du.shora(fecha));
        rep.empty();

        if (fullfd) {
            rep.line();
            rep.add("INFORME Z #" + corelz);
        }

        repFacturas();
        if (gl.peModal.equalsIgnoreCase("APR")) repFacturasCredito();
        repPedidos();
        repProductos();
        repTotales();

        if (fullfd) {
            rep.empty();
            rep.line();
            rep.add("Siguiente factura : " + fserie + " - " + fcorel);
            rep.empty();
            rep.add("Siguiente Informe Z : " + mu.CStr(corelz + 1));
            rep.line();
        }

        rep.empty();
        rep.empty();
        rep.empty();

        try {

            sql = "DELETE FROM D_REPFINDIA";
            db.execSQL(sql);

            for (int i = 0; i < rep.items.size(); i++) {
                s = rep.items.get(i).trim();

                sql = "INSERT INTO D_REPFINDIA VALUES ('" + gl.ruta + "'," + i + ",'" + s + "')";
                db.execSQL(sql);
            }

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        rep.save();

    }

    //CKFK 20190226 Modifiqué este procedimiento a como debe ser el fin de día en Toledano
    private void buildReportsTOL() {

        rep.empty();
        /*rep.add("CIERRE DEL DIA");
        rep.line();
        rep.add("Vendedor : " + gl.vend + " " + gl.vendnom);
        rep.add("Fecha : " + du.sfecha(fecha) + " " + du.shora(fecha));*/
        //claseDocumento.encabezado(""); Preguntarle a Jaros como usa el encabezado que esta en P_ENCABEZADO_REPORTES_HH
        rep.empty();

        if (fullfd) {
            rep.line();
            rep.add("INFORME Z #" + corelz);
        }

        rep.line();

        repPedidosTol();
        repFacturasTol();
        repCobrosTol();
        repNotasCreditoTol();
        repProductos();
        repDevolTotal();
        repEstadoMalo();

        repTotalesTol();

        try {

            sql = "DELETE FROM D_REPFINDIA";
            db.execSQL(sql);

            for (int i = 0; i < rep.items.size(); i++) {
                s = rep.items.get(i).trim();

                sql = "INSERT INTO D_REPFINDIA VALUES ('" + gl.ruta + "'," + i + ",'" + s + "')";
                db.execSQL(sql);
            }

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

        rep.save();

    }

	//#EJC20190226: Insertar solo cabecera de d_mov.
    private boolean Inserta_Enc_D_Mov() {

        String corel;
        claseFinDia = new clsFinDia(this);
        int i;

        corel=gl.ruta+"_"+mu.getCorelBase();devcorel=corel;
        gl.corel_d_mov=corel;

        try {

            db.beginTransaction();
            ins.init("D_MOV");
            ins.add("COREL",corel);
            ins.add("RUTA",((appGlobals) vApp).ruta);
            ins.add("ANULADO","N");
            ins.add("FECHA",fecha);
            ins.add("TIPO","D");
            ins.add("USUARIO",((appGlobals) vApp).vend);
            ins.add("REFERENCIA","Cierre de dia");
            ins.add("STATCOM","N");
            ins.add("IMPRES",0);
            ins.add("CODIGOLIQUIDACION",0);
            db.execSQL(ins.sql());
            db.setTransactionSuccessful();
            db.endTransaction();

            return true;

        } catch (Exception e) {
            db.endTransaction();
            //mu.msgbox( e.getMessage());
            return false;
        }

    }

	private boolean devProductos() {
		Cursor DT;
		String corel,pcod,plote,um;
		Double pcant,pcantm,ppeso;
		claseFinDia = new clsFinDia(this);
		int i;


		corel=gl.ruta+"_"+mu.getCorelBase();devcorel=corel;
		gl.corel_d_mov=corel;

        try {

            db.beginTransaction();

            ins.init("D_MOV");
            ins.add("COREL", corel);
            ins.add("RUTA", ((appGlobals) vApp).ruta);
            ins.add("ANULADO", "N");
            ins.add("FECHA", fecha);
            ins.add("TIPO", "D");
            ins.add("USUARIO", ((appGlobals) vApp).vend);
            ins.add("REFERENCIA", "Cierre de dia");
            ins.add("STATCOM", "N");
            ins.add("IMPRES", 0);
            ins.add("CODIGOLIQUIDACION", 0);

            db.execSQL(ins.sql());

            sql = "SELECT CODIGO,LOTE,SUM(CANT),SUM(CANTM),SUM(PESO),UNIDADMEDIDA FROM P_STOCK GROUP BY CODIGO,LOTE,UNIDADMEDIDA " +
                    "HAVING SUM(CANT)>0 OR SUM(CANTM) >0";
            DT = Con.OpenDT(sql);

            i = 0;

            if (DT.getCount() > 0) {

                DT.moveToFirst();

                while (!DT.isAfterLast()) {

                    pcod = DT.getString(0);
                    plote = DT.getString(1);
                    pcant = DT.getDouble(2);
                    pcantm = DT.getDouble(3);
                    ppeso = DT.getDouble(4);
                    um = DT.getString(5);

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
                    i++;
                }
            }

            if (gl.peModal.equalsIgnoreCase("TOL")) {
                sql = "DELETE FROM P_STOCK";
                db.execSQL(sql);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            claseFinDia.updateDevBodega();

            //Toast.makeText(this,"Devolucion aplicada "+i, Toast.LENGTH_SHORT).show();
            return true;

        } catch (Exception e) {
            db.endTransaction();
            //mu.msgbox( e.getMessage());
            return false;
        }

    }


    // Validaciones

    private boolean validaFinDia() {
        Cursor DT;
        int pend, fechaUltimoCierre;

        claseFinDia = new clsFinDia(this);

        fechaUltimoCierre = claseFinDia.ultimoCierreFecha();

        //#HS_20181127_1033 Agregue validacion para cantidad de facturas.
        if (claseFinDia.getCantFactura() == 0) {
            msgExit("No hay facturas.");
            return false;
        }

        if (fullfd) {

            if (!validaPagosPend()) {
                msgPendPago("Existen facturas pendientes de pago. No se puede realizar fin del día");
                return false;
            }

            setFactCor();
            if (fcorel == 0) {
                msgExit("No Están definidos los correlativos de factura.");
                return false;
            }

            corelz = claseFinDia.setCorrelZ();
            if (corelz == 0) {
                //msgExit("No esta definido correlativo de cierre Z.");return false;
                claseFinDia.updateCorrelativoZ();
            }
        }

        if (du.getActDate() == fechaUltimoCierre) {
            msgExit("Fin de Día ya fue efectuado el día de hoy");
            return false;
        }

        //#HS_20181127_1033 Agregue validacion para verificar si ya se realizo el deposito.
        if (gl.banderafindia == true) {
            if (claseFinDia.getDeposito() != 2) {
                msgAskDeposito();
                return false;
            }
        }

        //#HS_20181127_1033 Agregue validacion para verificar si ya se realizo la impresion del deposito.
        if (gl.banderafindia == true) {
            if (claseFinDia.getImpresionDeposito() != 3) {
                msgAskImpresionDeposito();
                return false;
            }
        }

        // pendiente deposito
        pend = 0;

        try {
            sql = "SELECT COREL FROM D_FACTURA WHERE DEPOS<>'S' ";
            DT = Con.OpenDT(sql);
            pend = pend + DT.getCount();
        } catch (Exception e) {
        }

        try {
            sql = "SELECT COREL FROM D_COBRO WHERE DEPOS<>'S' ";
            DT = Con.OpenDT(sql);
            pend = pend + DT.getCount();
        } catch (Exception e) {
        }
			
		/*if (pend>0) {
			msgExitDepos("Existen documentos pendientes a depositar.");return false;
		}*/

        pend = getFactCount("SELECT SERIE,CORELATIVO FROM D_FACTURA", "Facturas :");
        if (pend == 0) {
            msgExit("No se puede realizar Cierre del día. No está registrada ninguna factura.");
            return false;
        }

        try {
            //  pendiente envio

            pend = 0;
            sp = "";

            pend = pend + getFactCount("SELECT SERIE,CORELATIVO FROM D_FACTURA WHERE STATCOM<>'S'", "Facturas :");
            pend = pend + claseAppMethods.getDocCount("SELECT COREL FROM D_PEDIDO WHERE STATCOM<>'S'", "Pedidos :");
            pend = pend + claseAppMethods.getDocCount("SELECT COREL FROM D_COBRO WHERE STATCOM<>'S'", "Recibos :");
            pend = pend + getDeposCount("SELECT TOTAL FROM D_DEPOS WHERE STATCOM<>'S'", "Depositos :");
            pend = pend + claseAppMethods.getDocCount("SELECT COREL FROM D_MOV WHERE STATCOM<>'S'", "Inventario :");
            //pend=pend+getDocCount("SELECT RUTA  FROM D_ATENCION WHERE STATCOM<>'N'","A:");

            if (pend > 0) {
                //msgExitCom("Existen datos pendientes de envio. Realize envio de datos antes de cierre del día.\n"+sp);return false;
            }
        } catch (Exception e) {
        }

        return true;
    }

    private int getFactCount(String ss, String pps) {
        Cursor DT;
        int cnt;
        String st;

        try {
            sql = ss;
            DT = Con.OpenDT(sql);
            cnt = DT.getCount();
            sp += pps + " ";
			
			/*
			if (cnt>0) {
				
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					st=st+DT.getString(0)+"-"+DT.getInt(1)+", ";
					DT.moveToNext();
				}				
				sp=sp+st+"\n";
			}
			*/

            sp = sp + cnt + "\n";

            return cnt;
        } catch (Exception e) {
            mu.msgbox(sql + "\n" + e.getMessage());
            return 0;
        }
    }

    private int getDeposCount(String ss, String pps) {
        Cursor DT;
        int cnt;
        String st;

        try {
            sql = ss;
            DT = Con.OpenDT(sql);
            cnt = DT.getCount();
            sp += pps + " ";
			/*
			if (cnt>0) {				
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					st=st+mu.frmdec(DT.getDouble(0))+", ";
					DT.moveToNext();
				}				
				sp=sp+st+"\n";
			}
			*/

            sp = sp + cnt + "\n";

            return cnt;
        } catch (Exception e) {
            mu.msgbox(sql + "\n" + e.getMessage());
            return 0;
        }
    }

    private boolean validaPagosPend() {
        Cursor dt;

        try {
            sql = "SELECT DISTINCT CLIENTE FROM D_FACTURA WHERE (ANULADO='N') AND (COREL NOT IN " +
                    "(SELECT DISTINCT D_FACTURA_1.COREL " +
                    "FROM D_FACTURA AS D_FACTURA_1 INNER JOIN " +
                    "D_FACTURAP ON D_FACTURA_1.COREL=D_FACTURAP.COREL))";
            dt = Con.OpenDT(sql);

            return (dt.getCount() == 0);
        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            return false;
        }

    }

    private void requisitosFinDia() {

    }


    // Reportes

    private void repProductos() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;

        rep.empty();
        rep.addc("PRODUCTOS VENDIDOS");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {
            sql = "SELECT  D_FACTURAD.PRODUCTO, P_PRODUCTO.DESCLARGA, SUM(D_FACTURAD.CANT) AS CANT,D_FACTURAD.UMVENTA, SUM(D_FACTURAD.PESO),D_FACTURAD.UMPESO " +
                    "FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURAD.COREL = D_FACTURA.COREL " +
                    "INNER JOIN P_PRODUCTO ON D_FACTURAD.PRODUCTO = P_PRODUCTO.CODIGO " +
                    "WHERE (D_FACTURA.BANDERA<>'F') AND (D_FACTURA.ANULADO<>'S') " +
                    "GROUP BY D_FACTURAD.PRODUCTO, P_PRODUCTO.DESCLARGA,D_FACTURAD.UMVENTA " +
                    "ORDER BY P_PRODUCTO.DESCLARGA";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            while (!DT.isAfterLast()) {

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2 + " " + s3);
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Productos : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.empty();
    }

    private void repFacturas() {
        Cursor DT;
        String s1, s2, s3;

        tot = 0;
        fac = 0;
        faca = 0;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS");
        rep.empty();
        rep.add3lrr("Factura", "Total", "");
        rep.line();

        try {
            sql = "SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0);
                s2 = DT.getString(1);
                s1 = s1 + "-" + s2;
                val = DT.getDouble(2);
                s2 = mu.frmcur(val);
                s3 = DT.getString(3);
                if (s3.equalsIgnoreCase("N")) {
                    s3 = "";
                    tot += val;
                    fac++;
                } else {
                    s3 = "ANULADO";
                    faca++;
                }

                rep.add3lrr(s1, s2, s3);

                DT.moveToNext();

            }

        } catch (Exception e) {
            mu.msgbox("Facturas : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.add3lrr("Total", mu.frmcur(tot), "");
        rep.empty();
    }

    private void repFacturasCredito() {
        Cursor DT;
        String s1, s2, s3;

        tot = 0;
        cfac = 0;
        cfaca = 0;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS CREDITO");
        rep.empty();
        rep.add3lrr("Factura", "Total", "");
        rep.line();

        try {
            sql = "SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";

            sql = "SELECT D_FACTURA.SERIE, D_FACTURA.CORELATIVO, D_FACTURA.ANULADO, SUM(D_FACTURAP.VALOR) AS TOTAL " +
                    "FROM D_FACTURA INNER JOIN D_FACTURAP ON D_FACTURA.COREL =D_FACTURAP.COREL " +
                    "WHERE  (D_FACTURAP.TIPO='K') GROUP BY D_FACTURA.SERIE, D_FACTURA.CORELATIVO, D_FACTURA.ANULADO " +
                    "ORDER BY D_FACTURA.CORELATIVO";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0);
                s2 = DT.getString(1);
                s1 = s1 + "-" + s2;
                val = DT.getDouble(2);
                s2 = mu.frmval(val);
                s3 = DT.getString(3);
                if (s3.equalsIgnoreCase("N")) {
                    s3 = "";
                    tot += val;
                    cfac++;
                } else {
                    s3 = "ANULADO";
                    cfaca++;
                }

                rep.add3lrr(s1, s2, s3);

                DT.moveToNext();

            }

        } catch (Exception e) {
            mu.msgbox("Facturas credito : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.add3lrr("Total", mu.frmval(tot), "");
        rep.empty();
    }

    private void repPedidos() {
        Cursor DT;
        String s1, s2, s3;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE PEDIDOS");
        rep.empty();
        rep.addtot("Cliente", "Total  ");
        rep.line();

        ptot = 0;
        ped = 0;
        peda = 0;

        try {
            sql = "SELECT P_CLIENTE.NOMBRE,D_PEDIDO.TOTAL,D_PEDIDO.ANULADO " +
                    "FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO " +
                    "WHERE (D_PEDIDO.BANDERA<>'F') ORDER BY D_PEDIDO.COREL";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(0) + "";
                val = DT.getDouble(1);
                s2 = mu.frmval(val);
                s3 = DT.getString(2);
                if (s3.equalsIgnoreCase("N")) {
                    s2 = s2 + "  ";
                    ptot += val;
                    ped++;
                } else {
                    s2 = s2 + " A";
                    peda++;
                }

                rep.addtot(s1, s2);

                DT.moveToNext();

            }

        } catch (Exception e) {
            mu.msgbox("Pedidos : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.addtot("Total", mu.frmval(ptot) + "  ");
        rep.empty();
    }

    private void repTotales() {

        rep.line();
        rep.empty();
        rep.add("LIQUIDACION");
        rep.line();

        rep.addtot("Facturas emitidas", "" + fac);
        rep.addtot("Facturas anuladas", "" + faca);
        rep.addtot("Total facturas", "" + (faca + fac));
        rep.line();

        rep.addtot("Pedidos emitidos", "" + ped);
        rep.addtot("Pedidos anulados", "" + peda);
        rep.addtot("Total pedidos", "" + (peda + ped));
        rep.line();

        totRecibos();

        rep.addtot("Recibos emitidos", "" + rec);
        rep.addtot("Recibos anulados", "" + reca);
        rep.addtot("Total recibos", "" + (reca + rec));

        detVentas();

        rep.empty();
        rep.addtot("Ventas efectivo", tte);
        rep.addtot("Ventas cheque", ttc);
        rep.addtot("Ventas credito", ttk);
        rep.addtot("Ventas otro", tto);
        rep.line();
        tot = tte + ttc + ttk + tto;
        rep.addtot("Ventas total", tot);

        detRecibos();

        rep.empty();
        rep.addtot("Recibos efectivo", tre);
        rep.addtot("Recibos cheque", trc);
        rep.addtot("Recibos otro", tro);
        rep.line();
        tot = tre + trc + tro;
        rep.addtot("Recibos total", tot);

        tote = tte + tre;
        totc = ttc + trc;

        rep.empty();
        rep.addtot("Total efectivo", tote);
        rep.addtot("Total cheque", totc);
        rep.line();
        rep.addtot("Pagos total", tote + totc);

        totDeposito();

        rep.empty();
        rep.addtot("Deposito efectivo", depe);
        rep.addtot("Deposito cheque", depc);
        rep.line();
        rep.addtot("Depositos total", depe + depc);

        bale = tote - depe;
        balc = totc - depc;

        rep.empty();
        rep.addtot("Balance efectivo", bale);
        rep.addtot("Balance cheque", balc);
        rep.line();
        rep.addtot("Balance", (bale + balc));
        rep.line();

    }

    private void repDevolTotal() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;


        rep.empty();
        rep.addc("DEVOLUCION A BODEGA");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {

            sql = "SELECT  D_MOVD.PRODUCTO, P_PRODUCTO.DESCLARGA, SUM(D_MOVD.CANT) AS CANT,D_MOVD.UNIDADMEDIDA, SUM(D_MOVD.PESO),'' " +
                    "FROM D_MOVD INNER JOIN D_MOV ON D_MOVD.COREL = D_MOV.COREL " +
                    "INNER JOIN P_PRODUCTO ON D_MOVD.PRODUCTO = P_PRODUCTO.CODIGO  " +
                    "WHERE (D_MOV.COREL='" + devcorel + "') AND ((D_MOVD.CANT>0) OR (D_MOVD.PESO>0)) " +
                    "GROUP BY D_MOVD.PRODUCTO, P_PRODUCTO.DESCLARGA,D_MOVD.UNIDADMEDIDA	" +
                    "ORDER BY P_PRODUCTO.DESCLARGA ";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) {
                rep.line();
                rep.empty();
                return;
            }

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2 + " " + s3);
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Devolucion a bodega : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.empty();
    }

    private void repEstadoMalo() {
        Cursor DT;
        String s1, s2, s3, s4, s5, ump;

        tot = 0;

        rep.empty();
        rep.addc("INVENTARIO DANADO");
        rep.empty();
        if (gl.usarpeso) {
            rep.line();
            rep.addpu("PESO", "CANTIDAD", mw);
        }
        rep.line();

        try {

            sql = "SELECT  P_STOCK.CODIGO, P_PRODUCTO.DESCLARGA, SUM(P_STOCK.CANTM) AS CANT,P_STOCK.UNIDADMEDIDA, SUM(P_STOCK.PLIBRA),'' " +
                    "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO = P_PRODUCTO.CODIGO  " +
                    "WHERE ((P_STOCK.CANTM>0) OR (P_STOCK.PLIBRA>0)) " +
                    "GROUP BY P_STOCK.CODIGO, P_PRODUCTO.DESCLARGA,P_STOCK.UNIDADMEDIDA	" +
                    "ORDER BY P_PRODUCTO.DESCLARGA ";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) {
                rep.line();
                rep.empty();
                return;
            }

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

				/*
				s1=DT.getString(1);
				s1=rep.ltrim(s1,rep.prw-14);
				s3=DT.getString(3);s3=rep.ltrim(s3,6);
				val=DT.getDouble(2);s2=mu.frmint(val);
				s2=rep.rtrim(s2,6)+" "+s3;
				val=DT.getDouble(4);s4=mu.frmdec(val);
				ump=DT.getString(5);
				*/

                s1 = DT.getString(1);
                s1 = rep.ltrim(s1, rep.prw - mw - 1);
                s3 = DT.getString(3);
                s3 = rep.ltrim(s3, 6); // umventa
                val = DT.getDouble(2);
                s2 = mu.frmdecimal(val, gl.peDecImp);
                s2 = rep.rtrim(s2, mw - 7); // cant
                val = DT.getDouble(4);
                s4 = mu.frmdecimal(val, gl.peDecImp);
                s4 = rep.rtrim(s4, mw - 7); // peso
                ump = DT.getString(5);
                ump = rep.ltrim(ump, 6); // umpeso

                if (gl.usarpeso) {
					/*
					s1=DT.getString(1);
					s1=rep.ltrim(s1,rep.prw-2);
					rep.add(s1);
					s4=rep.rtrim(s4,8)+" "+rep.rtrim(ump,3);
					s5=s4+" "+s2;
					rep.add(s5);
					*/

                    s1 = DT.getString(1);
                    s1 = rep.ltrim(s1, rep.prw - 2);
                    rep.add(s1);

                    s4 = s4 + " " + ump;
                    s5 = s2 + " " + s3;
                    rep.addpu(s4, s5, mw);
                } else {
                    rep.add(s1 + " " + s2);
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Inventario dañado : " + e.getMessage());
            fail = true;
        }

        rep.line();
        rep.empty();
    }

    private void totRecibos() {
        Cursor DT;
        String s1;

        rec = 0;
        reca = 0;

        try {
            sql = "SELECT ANULADO FROM D_COBRO WHERE BANDERA<>'F'";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                s1 = DT.getString(0);
                if (s1.equalsIgnoreCase("N")) rec++;
                else reca++;

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Recibos : " + e.getMessage());
            fail = true;
        }

    }

    private void totDeposito() {
        Cursor DT;

        depe = 0;
        depc = 0;

        try {
            sql = "SELECT SUM(TOTEFEC),SUM(TOTCHEQ) FROM D_DEPOS WHERE CODIGOLIQUIDACION=0 AND ANULADO='N'";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            depe = DT.getDouble(0);
            depc = DT.getDouble(1);
        } catch (Exception e) {
            mu.msgbox("Depositos : " + e.getMessage());
            fail = true;
            depe = 0;
            depc = 0;
        }

    }

    private void detVentas() {
        Cursor DT;
        String s1;
        double val;
        int flag;

        tte = 0;
        ttc = 0;
        ttk = 0;
        tto = 0;

        try {
            sql = "SELECT SUM(D_FACTURAP.VALOR) AS Suma,D_FACTURAP.TIPO " +
                    "FROM D_FACTURAP INNER JOIN D_FACTURA ON D_FACTURAP.COREL = D_FACTURA.COREL " +
                    "GROUP BY D_FACTURAP.TIPO, D_FACTURA.ANULADO, D_FACTURA.BANDERA " +
                    "HAVING (D_FACTURA.ANULADO='N') AND (D_FACTURA.BANDERA<>'F')";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                val = DT.getDouble(0);
                s1 = DT.getString(1);

                flag = 0;
                if (s1.equalsIgnoreCase("E")) {
                    tte += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("C")) {
                    ttc += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("K")) {
                    ttk += val;
                    flag = 1;
                }
                if (flag == 0) tto += val;

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Det. ventas : " + e.getMessage());
            fail = true;
        }

    }

    private void detRecibos() {
        Cursor DT;
        String s1;
        double val;
        int flag;

        tre = 0;
        trc = 0;
        tro = 0;

        try {
            sql = "SELECT SUM(D_COBROP.VALOR) AS Suma,D_COBROP.TIPO " +
                    "FROM D_COBROP INNER JOIN D_COBRO ON D_COBROP.COREL = D_COBRO.COREL " +
                    "GROUP BY D_COBROP.TIPO, D_COBRO.ANULADO, D_COBRO.BANDERA " +
                    "HAVING (D_COBRO.ANULADO='N') AND (D_COBRO.BANDERA<>'F')";

            DT = Con.OpenDT(sql);

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                val = DT.getDouble(0);
                s1 = DT.getString(1);

                flag = 0;
                if (s1.equalsIgnoreCase("E")) {
                    tre += val;
                    flag = 1;
                }
                if (s1.equalsIgnoreCase("C")) {
                    trc += val;
                    flag = 1;
                }
                if (flag == 0) tro += val;

                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox("Det. ventas : " + e.getMessage());
            fail = true;
        }

    }


    // Aux

    //#HS_20181121_1431 Se dejo en comentario porque se agrego a la clase clsFinDia.
	/*private void setCorrelZ() {
		Cursor DT;

		try {
			sql="SELECT Corel FROM FinDia";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			corelz=DT.getInt(0);
		} catch (Exception e) {
			corelz=0;
		}
	}*/

    private void setFactCor() {
        Cursor DT;

        fcorel = 0;
        fserie = "";

        try {
            sql = "SELECT SERIE,CORELULT FROM P_COREL WHERE RUTA='" + gl.ruta + "'";
            DT = Con.OpenDT(sql);

            DT.moveToFirst();

            fserie = DT.getString(0);
            fcorel = DT.getInt(1) + 1;

        } catch (Exception e) {
            fcorel = 0;
            fserie = "";
            return;
        }
    }

    //#HS_20181121_1642 Se puso en comentario porque se agregó en la clase clsFinDia.
	/*private int ultimoCierreFecha() {
		Cursor DT;
		int rslt=0;

		try {
			sql="SELECT val1 FROM FinDia";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			rslt=DT.getInt(0);
		} catch (Exception e) {
			rslt=0;
		}

		return rslt;
	}*/

    private void delPrintFiles() {
        try {
            new File(Environment.getExternalStorageDirectory() + "/print.txt").delete();
        } catch (Exception e) {
        }
        try {
            new File(Environment.getExternalStorageDirectory() + "/SyncFold/findia.txt").delete();
        } catch (Exception e) {
        }
    }


    // Mensajes

    private void msgAskDeposito() {

        AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

        dialog1.setTitle("Road");
        dialog1.setMessage("No se ha realizado el depósito.");

        dialog1.setIcon(R.drawable.ic_quest);

        dialog1.setPositiveButton("Realizar Depósito.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityDeposito();
            }
        });

        dialog1.show();

    }

    private void msgAskImpresionDeposito() {

        AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);

        dialog1.setTitle("Road");
        dialog1.setMessage("Debe imprimir el recibo de depósito");

        dialog1.setIcon(R.drawable.ic_quest);

        dialog1.setPositiveButton("Imprimir Documento", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityImpresionDeposito(1);
            }
        });

        dialog1.show();

    }

    private void msgAsk() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Road");
        dialog.setMessage("Este proceso prepara el sistema para el siguiente día de venta. Continuar ?");

        dialog.setIcon(R.drawable.ic_quest);

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startFDD();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();

    }

	private void msgAskFinDiaTrue()
	{

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Road");
		dialog.setMessage("Este proceso prepara el sistema para el siguiente día de venta. Continuar ?");
		dialog.setIcon(R.drawable.ic_quest);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				//#HS_20181121_1453 Se habilito el mensaje de confirmación.
				msgAsk2();
		    }
		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {}
		});

		dialog.show();

	}

	private void msgAsk2()
	{

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿Está seguro de continuar?");
		dialog.setIcon(R.drawable.ic_quest);

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				msgAskDevInventario();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {}
		});
		
		dialog.show();
			
	}

	//#HS_20181121_1506 Se creo la pregunta para la devolución de inventario.
	private void msgAskDevInventario() {

        if (!Ya_Realizo_Devolucion()) {

            browse=1;
            startActivity(new Intent(this,DevolBodTol.class));

            toastlong("No ha efectuado la devolución a bodega,debe proceder a realizarla antes de fin del dia");

            /*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Road");
            dialog.setMessage("No ha efectuado la devolución a bodega, ¿Quiere proceder a realizarla?");
            dialog.setIcon(R.drawable.ic_quest);
            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(gl.banderafindia == false)
                    {
                        startFDD();
                    }else {
                        startFDD();//iniciarFD();
                    }
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityMenu();
                }
            });

            dialog.show();*/

        } else  {
            startFDD();
        }

    }

    private void msgExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FinDia.super.finish();
            }
        });

        dialog.show();
    }

    private void msgPendPago(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gl.filtrocli = 2;
                startActivity(new Intent(FinDia.this, Clientes.class));
            }
        });

        dialog.show();
    }

	private void msgExitCom(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
								
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    	((appGlobals) vApp).tipo=0;
				Intent intent = new Intent(FinDia.this,ComWS.class);
				startActivity(intent);
				
		    	//FinDia.super.finish();
		    }
		});
			
		dialog.show();		
	}
	
	private void msgExitDepos(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
								
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    	((appGlobals) vApp).tipo=0;
		    	Intent intent = new Intent(FinDia.this,Deposito.class);
				startActivity(intent);	
				
		    	//FinDia.super.finish();
		    }
		});
			
		dialog.show();		
	}
	
	public void msgAskFlag(View view) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage("Limpiar bandera cierre del día?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	db.execSQL("UPDATE FinDia SET val1=0");
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {}
		});
		
		dialog.show();
			
	}


    private boolean Tiene_Inventario_Devolucion()
    {

        boolean TieneInvDevol = false;

        Cursor DT;

        try
        {

            double vTotalLbsB = 0;
            double vTotalUB = 0;

            //Producto con lote
            sql= " SELECT SUM(ifnull(S.PESO,0)) AS PESOTOT, SUM(ifnull(S.CANT,0))AS CANTUNI " +
            " FROM P_STOCK S, P_PRODUCTO P " +
            " WHERE P.ES_PROD_BARRA = 0 AND S.CODIGO= P.CODIGO " ;

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB =   DT.getDouble(0);
                vTotalUB = DT.getDouble(1);
            }

            //Producto con HU
            sql = " SELECT SUM(ifnull(S.CANT,0)) AS PESOTOT, COUNT(S.CODIGO)AS CANTUNI " +
            " FROM P_STOCKB S, P_PRODUCTO P " +
            " WHERE P.ES_PROD_BARRA = 1 AND S.CODIGO= P.CODIGO AND (S.COREL = '' OR S.COREL IS NULL)";

                    /*+
            " AND S.BARRA NOT IN (SELECT BARRA FROM D_BONIF_BARRA WHERE COREL NOT IN (" +
            " SELECT COREL FROM D_FACTURA WHERE ANULADO = 'S')) "; */

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB +=   DT.getDouble(0);
                vTotalUB += DT.getDouble(1);
            }

            //Devolución de dañado.
            sql = " SELECT SUM(S.PESO) AS PESOTOT, SUM(S.CANT)AS CANTUNI " +
            " FROM D_CXC E, D_CxCD S, P_PRODUCTO P WHERE E.COREL = S.COREL AND S.CODIGO= P.CODIGO AND E.ANULADO = 'N' ";

            DT=Con.OpenDT(sql);

            if (DT.getCount()!=0)
            {
                DT.moveToFirst();

                vTotalLbsB +=   DT.getDouble(0);
                vTotalUB += DT.getDouble(1);
            }

            TieneInvDevol = (vTotalUB > 0);

        }catch (Exception e) {
            Log.e("TieneInvDevol",e.getMessage());
        }

        return  TieneInvDevol;

    }


    private boolean Ya_Realizo_Devolucion()
    {

        boolean Ya_Realizo_Devol = false;

        Cursor DT;

        try
        {

            double vTotalLbsB = 0;
            double vTotalUB = 0;
            boolean vTieneInvDevol = false;

            sql = "SELECT STATCOM FROM D_MOV WHERE TIPO = 'D' AND ANULADO = 'N'";
            DT=Con.OpenDT(sql);

            if (DT.getCount()==0)
            {

                vTieneInvDevol = Tiene_Inventario_Devolucion();

                if (vTieneInvDevol)
                {
                    Ya_Realizo_Devol = false;

                }else
                {
                    Ya_Realizo_Devol = Inserta_Enc_D_Mov();
                }

            }else
            {
                Ya_Realizo_Devol = true;
            }

        }catch (Exception e) {
            Log.e("TieneInvDevol",e.getMessage());
        }

        return  Ya_Realizo_Devol;

    }


	// Activity Events
	
	@Override
	public void onBackPressed() {
		if (idle) super.onBackPressed();
	}


    //CKFK 20190226 Agregué esta función para generar el listado de facturas de Toledano
    private void repFacturasTol() {
        Cursor DT;
        String s1, s2, s3;
        String vComunicacion = "";
        String vAuxCorel, vCadena;
        double sumagrav, sumaimp, sumanograv, totporfila, totgrav, totnograv, TotItbm, sumados, i;
        boolean anulada = false;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE FACTURAS");
        rep.line();
        rep.empty();
        rep.add("No. Doc");
        rep.add("      GRAV.  NO.GR  ITBM   Total  TP");
        rep.line();

        try {

            //IIf(Not CellCom Or gFinDia, " WHERE F.STATCOM = 'N' ", " ")
            if ((!gl.CellCom) || (gl.banderafindia)) {
                vComunicacion = " WHERE F.STATCOM = 'N' ";
            }

            //sql="SELECT SERIE,CORELATIVO,TOTAL,ANULADO FROM D_FACTURA WHERE BANDERA<>'F' ORDER BY CORELATIVO";
            sql = " SELECT F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    vComunicacion + " AND D.IMP > 0 " +
                    " GROUP BY F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO " +
                    " UNION SELECT F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " 0 AS GRAVADO, SUM(D.TOTAL) AS NO_GRAVADO " +
                    " FROM D_FACTURA F INNER JOIN D_FACTURAD D ON F.COREL = D .COREL " +
                    vComunicacion + " AND D.IMP = 0 " +
                    " GROUP BY F.SERIE, F.CORELATIVO, F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                totgrav = 0;
                totnograv = 0;
                TotItbm = 0;
                sumados = 0;
                vAuxCorel = "";
                sumagrav = 0;
                sumanograv = 0;
                totporfila=0;
                sumaimp = 0;

                i=0;

                while (!DT.isAfterLast()) {

                    s1 = DT.getString(2);

                    if (!vAuxCorel.equalsIgnoreCase(s1)) {
                        vAuxCorel = s1;
                        totporfila = 0;

                        anulada = (DT.getString(5).equalsIgnoreCase("S"));

                        vCadena = DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)), 6);

                        if (anulada) vCadena += " - ANULADA";

                        rep.add(vCadena);

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(7);
                            totgrav = totgrav + DT.getDouble(6);
                            TotItbm = TotItbm + DT.getDouble(4);
                        }

                        totporfila = DT.getDouble(3);//Campo Total
                        sumaimp = DT.getDouble(4);//Campo ImpMonto
                        sumanograv = DT.getDouble(7);//Campo No Gravado
                        sumagrav = DT.getDouble(6);;//Campo Gravado

                        if ((i + 1) <= (DT.getCount() - 1)) {

                            DT.moveToNext();

                            if (!vAuxCorel.equalsIgnoreCase(DT.getString(0))){
                                vCadena = StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                                vCadena += StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                                vCadena += StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                                vCadena +=  StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                                rep.add(vCadena);
                            }

                            DT.moveToPrevious();

                        }  else{

                            vCadena = StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                            vCadena +=  StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                            vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                            vCadena +=  StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                            rep.add(vCadena);
                        }
                    }
                    else {

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(5);
                            totgrav = totgrav + DT.getDouble(6);
                        }

                        sumanograv = sumanograv + DT.getDouble(7);//NoGravado
                        sumagrav = sumagrav + DT.getDouble(6);//Gravado

                        vCadena = StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                        rep.add(vCadena);

                    }
                    i += 1;

                    DT.moveToNext();
                }

                rep.line();

                sumados = totgrav + totnograv + TotItbm;

                vCadena = "Total";
                rep.add(vCadena);
                vCadena = StringUtils.leftPad(mu.frmcur(totgrav), 10, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(totnograv), 9, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(TotItbm), 8, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(sumados), 9, " ");
                rep.add(vCadena);
            }


        } catch(Exception e){
            mu.msgbox("Facturas : " + e.getMessage());
            fail = true;
        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de pedidos de Toledano
    private void repPedidosTol() {
        Cursor DT;
        String s1, s2, s3;
        String vComunicacion = "";
        String vAuxCorel, vCadena;
        double sumagrav, sumaimp, sumanograv, totporfila, totgrav, totnograv, TotItbm, sumados, i;
        boolean anulada = false;

        rep.line();
        rep.empty();
        rep.add("LISTADO DE PEDIDOS");
        rep.line();
        rep.empty();
        rep.add("No. Doc");
        rep.add("      GRAV.  NO.GR  ITBM   Total  TP");
        rep.line();

        try {

            if ((!gl.CellCom) || (gl.banderafindia)) {
                vComunicacion = " WHERE F.STATCOM = 'N' ";
            }

            sql = " SELECT F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " SUM(D.PRECIODOC * D.CANT) AS GRAVADO, 0 AS NO_GRAVADO " +
                    " FROM D_PEDIDO F INNER JOIN D_PEDIDOD D ON F.COREL = D .COREL " +
                    vComunicacion + " AND D.IMP > 0 " +
                    " GROUP BY F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO " +
                    " UNION SELECT F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO, " +
                    " 0 AS GRAVADO, SUM(D.TOTAL) AS NO_GRAVADO " +
                    " FROM D_PEDIDO F INNER JOIN D_PEDIDOD D ON F.COREL = D .COREL " +
                    vComunicacion + " AND D.IMP = 0 " +
                    " GROUP BY F.COREL, F.TOTAL, F.IMPMONTO, F.ANULADO ";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {
                DT.moveToFirst();

                totgrav = 0;
                totnograv = 0;
                TotItbm = 0;
                sumados = 0;
                vAuxCorel = "";
                sumagrav = 0;
                sumanograv = 0;
                totporfila=0;
                sumaimp = 0;

                i=0;

                while (!DT.isAfterLast()) {

                    s1 = DT.getString(2);

                    if (!vAuxCorel.equalsIgnoreCase(s1)) {
                        vAuxCorel = s1;
                        totporfila = 0;

                        anulada = (DT.getString(5).equalsIgnoreCase("S"));

                        vCadena = DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)), 6);

                        if (anulada) vCadena += " - ANULADO";

                        rep.add(vCadena);

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(7);
                            totgrav = totgrav + DT.getDouble(6);
                            TotItbm = TotItbm + DT.getDouble(4);
                        }

                        totporfila = DT.getDouble(3);//Campo Total
                        sumaimp = DT.getDouble(4);//Campo ImpMonto
                        sumanograv = DT.getDouble(7);//Campo No Gravado
                        sumagrav = DT.getDouble(6);;//Campo Gravado

                        if ((i + 1) <= (DT.getCount() - 1)) {

                            DT.moveToNext();

                            if (!vAuxCorel.equalsIgnoreCase(DT.getString(0))){
                                vCadena = StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                                vCadena += StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                                vCadena += StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                                vCadena +=  StringUtils.right( StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                                rep.add(vCadena);
                            }

                            DT.moveToPrevious();

                        }  else{

                            vCadena = StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                            vCadena +=  StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                            vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                            vCadena +=  StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                            rep.add(vCadena);
                        }
                    }
                    else {

                        if (!anulada) {
                            totnograv = totnograv + DT.getDouble(5);
                            totgrav = totgrav + DT.getDouble(6);
                        }

                        sumanograv = sumanograv + DT.getDouble(7);//NoGravado
                        sumagrav = sumagrav + DT.getDouble(6);//Gravado

                        vCadena = StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumagrav), 10),10);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumanograv), 9),9);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(sumaimp), 8),8);
                        vCadena += StringUtils.right(StringUtils.leftPad(mu.frmcur_sm(totporfila), 9),9) + " F";
                        rep.add(vCadena);

                    }
                    i += 1;

                    DT.moveToNext();
                }

                rep.line();

                sumados = totgrav + totnograv + TotItbm;

                vCadena = "Total";
                rep.add(vCadena);
                vCadena = StringUtils.leftPad(mu.frmcur(totgrav), 10, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(totnograv), 9, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(TotItbm), 8, " ");
                vCadena = vCadena + StringUtils.leftPad(mu.frmcur(sumados), 9, " ");
                rep.add(vCadena);
            }


        } catch(Exception e){
            mu.msgbox("Pedidos: " + e.getMessage());
            fail = true;
        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de cobros de Toledano
    private void repCobrosTol(){

        try{

            /* S = "LISTADO DE COBROS"
        SP.WriteLine(S)
        SP.WriteLine(gPrLine)

        S = "No. Rec " & Space(10) & "Total" & Space(10) & "  E"
        SP.WriteLine(S)

        SQL = "SELECT COREL, TOTAL, ANULADO FROM D_COBRO WHERE STATCOM='N'"

        Try
        openDT(DT, SQL)
        Catch ex As Exception
        MsgBox(ex.Message)
        If Not SP Is Nothing Then SP.Close()
        Return -1
        End Try

        TotalRecibos = 0

        If DT.Rows.Count > 0 Then

        For i = 0 To DT.Rows.Count - 1

        anulada = IIf(DT.Rows(i).Item("ANULADO") = "N", False, True)
        S = DT.Rows(i).Item("COREL")

        If Not anulada Then TotalRecibos = TotalRecibos + DT.Rows(i).Item("TOTAL")

        S = S & Strings.Right(Space(25) & FrmCur(DT.Rows(i).Item("TOTAL")), 25) & IIf(anulada, "  A", "")
        SP.WriteLine(S)
        Next
        End If
        SP.WriteLine(gPrLine)
        S = "Total:  " + Strings.Right(Space(27) & FrmCur(TotalRecibos), 27)
        SP.WriteLine(S)
        SP.WriteLine(" ")
        DT.Reset()*/

        }catch (Exception ex){

        }
    }

    //CKFK 20190226 Agregué esta función para generar el listado de notas de crédito de Toledano
    private void repNotasCreditoTol(){

        try{

            /* S = "LISTADO DE NOTAS DE CREDITO"
            SP.WriteLine(S)
            SP.WriteLine(gPrLine)

            S = "No. N.C " & Space(10) & "Total" & Space(10) & "  E"
            SP.WriteLine(S)

            S = "AFECTAN CREDITO"
            SP.WriteLine(S)
            SP.WriteLine(gPrLine)

            SQL = "SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N, D_FACTURAP F " & _
                 " WHERE N.FACTURA = F.COREL AND N.STATCOM='N'  AND F.TIPO = 'K'  " & _
                 " UNION SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N, D_CXC C " & _
                 " WHERE N.FACTURA = C.COREL AND N.STATCOM='N' "

            Try
                openDT(DT, SQL)
            Catch ex As Exception
                MsgBox(ex.Message)
                If Not SP Is Nothing Then SP.Close()
                Return -1
            End Try

            TotalNotaCred = 0
            TotalNotaCredCred = 0
            TotalNotaCredCont = 0

            If DT.Rows.Count > 0 Then

                For i = 0 To DT.Rows.Count - 1

                    anulada = IIf(DT.Rows(i).Item("ANULADO") = "N", False, True)
                    S = DT.Rows(i).Item("COREL")

                    If Not anulada Then
                        TotalNotaCred = TotalNotaCred + DT.Rows(i).Item("TOTAL")
                        TotalNotaCredCred = TotalNotaCredCred + DT.Rows(i).Item("TOTAL")
                    End If

                    S = S & Strings.Right(Space(25) & FrmCur(DT.Rows(i).Item("TOTAL")), 25) & IIf(anulada, "  A", "")
                    SP.WriteLine(S)
                Next
            End If

            S = "AFECTAN CONTADO"
            SP.WriteLine(S)
            SP.WriteLine(gPrLine)

            SQL = "SELECT DISTINCT N.COREL, N.TOTAL, N.ANULADO FROM D_NOTACRED N " & _
                  " WHERE N.STATCOM='N' AND N.FACTURA IN (SELECT COREL FROM D_FACTURAP WHERE TIPO <> 'K')"

            Try
                openDT(DT, SQL)
            Catch ex As Exception
                MsgBox(ex.Message)
                If Not SP Is Nothing Then SP.Close()
                Return -1
            End Try

            If DT.Rows.Count > 0 Then

                For i = 0 To DT.Rows.Count - 1

                    anulada = IIf(DT.Rows(i).Item("ANULADO") = "N", False, True)
                    S = DT.Rows(i).Item("COREL")

                    If Not anulada Then
                        TotalNotaCred = TotalNotaCred + DT.Rows(i).Item("TOTAL")
                        TotalNotaCredCont = TotalNotaCredCont + DT.Rows(i).Item("TOTAL")
                    End If

                    S = S & Strings.Right(Space(25) & FrmCur(DT.Rows(i).Item("TOTAL")), 25) & IIf(anulada, "  A", "")
                    SP.WriteLine(S)
                Next
            End If

            SP.WriteLine(gPrLine)
            S = "Total NC Credito:  " + Strings.Right(Space(27) & FrmCur(TotalNotaCredCred), 27)
            SP.WriteLine(S)
            S = "Total NC Contado:  " + Strings.Right(Space(27) & FrmCur(TotalNotaCredCont), 27)
            SP.WriteLine(S)
            S = "Total:  " + Strings.Right(Space(27) & FrmCur(TotalNotaCred), 27)
            SP.WriteLine(S)
            SP.WriteLine(" ")
            DT.Reset()*/

        }catch (Exception ex){

        }

    }

    //CKFK 20190226 Agregué esta función para generar los totales del Cierre Z
    private void repTotalesTol() {

        try{

            /*

            S = "ACUMULADOS A LA FECHA"
            SP.WriteLine(CenterTrim(S))
            SP.WriteLine("")
            S = "Ventas Gravadas       :" + Strings.Right(Space(13) & FrmCur(totgrav), 13)
            SP.WriteLine(S)
            S = "Ventas No Gravadas    :" + Strings.Right(Space(13) & FrmCur(totnograv), 13)
            SP.WriteLine(S)
            S = "Acumulado ITBM        :" + Strings.Right(Space(13) & FrmCur(TotItbm), 13)
            SP.WriteLine(S)
            S = "GT                    :" + Strings.Right(Space(13) & FrmCur(sumados), 13)
            SP.WriteLine(S)
            SP.WriteLine("")

            'Totales para liquidacion
            S = "Facturas Credito      :" + Strings.Right(Space(13) & CStr(FacturasCredito()), 13)
            SP.WriteLine(S)
            S = "Facturas Contado      :" + Strings.Right(Space(13) & CStr(FacturasContado()), 13)
            SP.WriteLine(S)
            S = "Facturas Anuladas     :" + Strings.Right(Space(13) & CStr(FacturasAnuladas()), 13)
            SP.WriteLine(S)
            S = "Cantidad Facturas     :" + Strings.Right(Space(13) & CStr(TotalFacturas()), 13)
            SP.WriteLine(S)
            S = "Recibos               :" + Strings.Right(Space(13) & CStr(TotRecibos()), 13)
            SP.WriteLine(S)
            S = "Recibos anulados      :" + Strings.Right(Space(13) & CStr(RecibosAnulados()), 13)
            SP.WriteLine(S)
            'S = "Notas de credito      :" + Strings.Right(Space(13) & CStr(TotNotaC()), 13)
            'SP.WriteLine(S)
            S = "Cantidad de NC        :" + Strings.Right(Space(13) & CStr(NotasCredito()), 13)
            SP.WriteLine(S)
            S = "Cant. NC anuladas     :" + Strings.Right(Space(13) & CStr(NotasCreditoAnuladas()), 13)
            SP.WriteLine(S)
            SP.WriteLine("")

            Dim vTotalNC_Credito As Double = TotNotaC_Credito()
            Dim vTotalNC_Contado As Double = TotNotaC_Contado()
            Dim vTotalCredito As Double = TotalCredito2()
            Dim vTotalContado As Double = TotalEfectivo2()

            S = "Venta Credito         :" + Strings.Right(Space(13) & FrmCur(vTotalCredito), 13)
            SP.WriteLine(S)
            S = "Total NC Credito      :" + Strings.Right(Space(13) & FrmCur(vTotalNC_Credito), 13)
            SP.WriteLine(S)
            S = "Total Credito         :" + Strings.Right(Space(13) & FrmCur(vTotalCredito - vTotalNC_Credito), 13)
            SP.WriteLine(S)
            S = "Venta Contado         :" + Strings.Right(Space(13) & FrmCur(vTotalContado), 13)
            SP.WriteLine(S)
            S = "Total NC Contado      :" + Strings.Right(Space(13) & FrmCur(vTotalNC_Contado), 13)
            SP.WriteLine(S)
            S = "Total Contado         :" + Strings.Right(Space(13) & FrmCur(vTotalContado - vTotalNC_Contado), 13)
            SP.WriteLine(S)
            'S = "Venta Total           :" + Strings.Right(Space(13) & FrmCur(totgrav + totnograv + TotItbm), 13)
            'SP.WriteLine(S)
            S = "Venta Total           :" + Strings.Right(Space(13) & FrmCur(vTotalCredito + vTotalContado), 13)
            SP.WriteLine(S)
            S = "Gran Total           :" + Strings.Right(Space(13) & FrmCur(vTotalCredito + vTotalContado), 13)
            SP.WriteLine(S)
            S = "Total Recibos         :" + Strings.Right(Space(13) & FrmCur(TotalRecibos), 13)
            SP.WriteLine(S)
            SP.WriteLine("")

            Try
                SQL = "select serie,corelult from p_corel"
                openDT(DT2, SQL)
                If DT2.Rows.Count > 0 Then
                    S = "Siguiente Factura     :" + Strings.Right(Space(13) & Trim(DT2.Rows(0).Item("SERIE")) + Strings.Right("000000" & Trim(Str(DT2.Rows(0).Item("CORELULT") + 1)), 6), 13)
                Else
                    S = "Siguiente Factura     :" & Strings.Right(Space(13) & "0", 13)
                End If
                SP.WriteLine(S)
                DT2.Reset()
            Catch ex As Exception
                MsgBox("Error " & ex.Message)
            End Try

            Try

                SQL = "SELECT SERIE, ACTUAL FROM P_CORRELREC"
                openDT(DT2, SQL)
                If DT2.Rows.Count > 0 Then
                    S = "Siguiente Recibo      :" & Strings.Right(Space(13) & Trim(DT2.Rows(0).Item("SERIE")) + Strings.Right("000000" & Trim(DT2.Rows(0).Item("ACTUAL") + 1), 6), 13)
                Else
                    S = "Siguiente Recibo      :" & Strings.Right(Space(13) & "0", 13)
                End If
                SP.WriteLine(S)
                DT2.Reset()
            Catch ex As Exception
                MsgBox("Error " & ex.Message)
            End Try

            Try

                SQL = "SELECT SERIE, ACTUAL FROM P_CORREL_OTROS WHERE TIPO = 'NC'"
                openDT(DT2, SQL)
                If DT2.Rows.Count > 0 Then
                    S = "Siguiente Nota Credito:" & Strings.Right(Space(13) & Trim(DT2.Rows(0).Item("SERIE")) + Strings.Right("000000" & Trim(DT2.Rows(0).Item("ACTUAL") + 1), 6), 13)
                Else
                    S = "Siguiente Nota Credito:" & Strings.Right(Space(13) & "0", 13)
                End If
                SP.WriteLine(S)
                DT2.Reset()
            Catch ex As Exception
                MsgBox("Error " & ex.Message)
            End Try

            '*** OSCAR4
            If CorelZ <> "" Then
                Dim corelativoZ As Integer = CInt(DaCorelativoZ()) + 1
                S = "Siguiente Informe Z   :" + Strings.Right(Space(13) & CStr(corelativoZ), 13)
            Else
                S = "Siguiente Informe Z   :" + Strings.Right(Space(13) & "0", 13)
            End If

            '***
            SP.WriteLine(S)

            rep.add("Siguiente factura     :" + fserie + " - " + fcorel);
            rep.add("Siguiente Recibo      :" + fserie + StringUtils.right("000000" + Integer.toString(fcorel), 6));
            rep.add("Siguiente Nota Credito: " + fserie + StringUtils.right("000000" + Integer.toString(fcorel), 6));
            rep.add("Siguiente Informe Z   : " + mu.CStr(corelz + 1));

             */
        }catch (Exception ex){

        }

    }
}