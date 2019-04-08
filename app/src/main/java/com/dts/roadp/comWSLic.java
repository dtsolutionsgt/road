package com.dts.roadp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class comWSLic extends PBase {

    private TextView lblInfo, lblParam, lblRec, lblEnv;
    private ProgressBar barInfo;
    private EditText txtRuta, txtWS, txtEmp;
    private ImageView imgRec, imgEnv, imgExis;
    private RelativeLayout ralBack;

    private int isbusy, fecha, lin, reccnt;
    private String err, ruta, rutatipo, sp, docstock;
    private boolean fFlag, showprogress, errflag;

    private SQLiteDatabase dbT;
    private BaseDatos ConT;
    private BaseDatos.Insert insT;
    private AppMethods clsAppM;

    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayList<String> results = new ArrayList<String>();

    private ArrayList<clsClasses.clsEnvio> items = new ArrayList<clsClasses.clsEnvio>();
    private ListAdaptEnvio adapter;

    private clsDataBuilder dbld;
    private DateUtils DU;


    // Web Service -

    public AsyncCallRec wsRtask;
    public AsyncCallSend wsStask;

    private static String sstr, fstr, fprog, finf, ferr, fterr, idbg, dbg, ftmsg, esql, ffpos;
    private int scon, running, pflag, stockflag, conflag;
    private String ftext, slsync, senv, gEmpresa, ActRuta,  strliqid;
    private boolean  ftflag;

    private final String NAMESPACE = "http://tempuri.org/";
    private String METHOD_NAME, URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wslic);

        super.InitBase();
        addlog("ComWSLic", "" + du.getActDateTime(), gl.vend);

        System.setProperty("line.separator", "\r\n");

        dbld = new clsDataBuilder(this);
        clsAppM = new AppMethods(this, gl, Con, db);

        lblInfo = (TextView) findViewById(R.id.lblETipo);
        lblParam = (TextView) findViewById(R.id.lblProd);
        barInfo = (ProgressBar) findViewById(R.id.progressBar2);
        txtRuta = (EditText) findViewById(R.id.txtRuta);
        txtRuta.setEnabled(false);
        txtWS = (EditText) findViewById(R.id.txtWS);
        txtWS.setEnabled(false);
        txtEmp = (EditText) findViewById(R.id.txtEmp);
        txtEmp.setEnabled(false);

        lblRec = (TextView) findViewById(R.id.btnRec);
        lblEnv = (TextView) findViewById(R.id.btnSend);

        imgEnv = (ImageView) findViewById(R.id.imageView6);
        imgRec = (ImageView) findViewById(R.id.imageView5);

        ralBack = (RelativeLayout) findViewById(R.id.relwsmail);

        isbusy = 0;

        lblInfo.setText("");
        lblParam.setText("");
        barInfo.setVisibility(View.INVISIBLE);

        ruta = gl.ruta;
        ActRuta = ruta;
        gEmpresa = gl.emp;
        rutatipo = gl.rutatipog;

        getWSURL();

        //#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
        if (gl.debug) {
            if (mu.emptystr(txtRuta.getText().toString())) {
                txtRuta.setText("8001-1");
                txtEmp.setText("03");
                txtWS.setText("http://192.168.1./wsAndr/wsandr.asmx");
            }
        }

        setHandlers();

    }


    //region Events

    public void askRec(View view) {

        if (isbusy == 1) {
            toastcent("Por favor, espere que se termine la tarea actual.");
            return;
        }

        msgAskConfirmaRecibido();

    }

    public void askSend(View view) {

        try {
            if (isbusy == 1) {
                toastcent("Por favor, espere que se termine la tarea actual.");
                return;
            }

            if (gl.contlic) {

            }

            if (gl.banderafindia) {
                if (!puedeComunicar()) {
                    mu.msgbox("No ha hecho fin de dia, no puede comunicar datos");
                    return;
                }
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Envio");
            dialog.setMessage("¿Enviar datos?");

            dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runSend();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private boolean puedeComunicar() {

        boolean vPuedeCom = false;

        try {

            //#CKFK 20190304 Agregué validación para verificar si ya se realizó la comunicación de los datos.
            if (gl.banderafindia) {
           } else {
                return true;
            }

        } catch (Exception ex) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), ex.getMessage(), "");
        }

        return vPuedeCom;
    }

    private void setHandlers() {
        ralBack.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeRight() {
                onBackPressed();
            }

            public void onSwipeLeft() {
            }
        });
    }

    //endregion

    //region Main

    private void runRecep() {

        try {
            if (isbusy == 1) return;

            if (!setComParams()) return;

            //#CKFK 20190313 Agregué esto para ocultar el teclado durante la carga de los datos
            View view = this.getCurrentFocus();
            view.clearFocus();
            if (view != null) {
                keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            isbusy = 1;

            barInfo.setVisibility(View.VISIBLE);
            barInfo.invalidate();
            lblInfo.setText("Iniciando proceso de carga..");

            lblInfo.setText("Conectando ...");

            wsRtask = new AsyncCallRec();
            wsRtask.execute();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void runSend() {

        try {
            if (isbusy == 1) return;

            if (!setComParams()) return;

            isbusy = 1;

            barInfo.setVisibility(View.VISIBLE);
            barInfo.invalidate();
            lblInfo.setText("Conectando ...");

            showprogress = true;
            wsStask = new AsyncCallSend();
            wsStask.execute();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    public void writeData(View view) {

        try {
            dbld.clear();
            dbld.insert("D_PEDIDO", "WHERE 1=1");
            dbld.insert("D_PEDIDOD", "WHERE 1=1");
            dbld.save();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }


    }


    //endregion

    //region Web Service Methods

    public int fillTable(String value, String delcmd) {

        int rc;
        String s, ss;

        METHOD_NAME = "getIns";

        sstr = "OK";

        try {

            idbg = idbg + " filltable ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(value);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc = resSoap.getPropertyCount() - 1;
            idbg = idbg + " rec " + rc + "  ";

            s = "";
            if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
                if (rc == 1) {
                    stockflag = 0;//return 1;
                } else {
                    stockflag = 1;
                }
            }

            // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
            // 	idbg=idbg+" RC ="+rc+"---";
            //}


            for (int i = 0; i < rc; i++) {
                String str = "";
                try {
                    str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);
                    //s=s+str+"\n";
                } catch (Exception e) {
                    mu.msgbox("error: " + e.getMessage());
                }

                if (i == 0) {

                    idbg = idbg + " ret " + str + "  ";

                    if (str.equalsIgnoreCase("#")) {
                        listItems.add(delcmd);
                    } else {
                        idbg = idbg + str;
                        ftmsg = ftmsg + "\n" + str;
                        ftflag = true;
                        sstr = str;
                        return 0;
                    }
                } else {
                    try {
                        sql = str;
                        listItems.add(sql);
                        sstr = str;
                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                        sstr = e.getMessage();
                    }
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            //#EJC20190226: Evitar que se muestre OK después del nombre de la tabla cuando da error de timeOut.
            sstr = e.getMessage();
            idbg = idbg + " ERR " + e.getMessage();
            return 0;
        }
    }

    public int commitSQL() {
        int rc;
        String s, ss;

        METHOD_NAME = "Commit";
        sstr = "OK";

        if (dbld.size() == 0) return 1;

        s = "";
        for (int i = 0; i < dbld.size(); i++) {
            ss = dbld.items.get(i);
            s = s + ss + "\n";
        }
        if (showprogress) {
            fprog = "Enviando ...";
            wsStask.onProgressUpdate();
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(s);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            sstr = s;
            return 0;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            sstr = e.getMessage();
        }

        return 0;
    }

    public int OpenDTt(String sql) {
        int rc;

        METHOD_NAME = "OpenDT";

        results.clear();

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(sql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc = resSoap.getPropertyCount() - 1;

            for (int i = 0; i < rc + 1; i++) {
                String str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                if (i == 0) {
                    sstr = str;
                    if (!str.equalsIgnoreCase("#")) {
                        sstr = str;
                        return 0;
                    }
                } else {
                    results.add(str);
                }
            }

            return 1;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            sstr = e.getMessage();
        }

        return 0;

    }

    public int getTest() {

        METHOD_NAME = "TestWS";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("Value");
            param.setValue("OK");

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);

            transport.call(NAMESPACE + METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString() + "..";

            return 1;
        } catch (Exception e) {
            sstr = e.getMessage();
        }

        return 0;
    }

    //endregion

    //region WS Recepcion Methods

    private boolean getData() {
        Cursor DT;
        BufferedWriter writer = null;
        FileWriter wfile;
        int rc, scomp, prn, jj;
        String s, val = "";


        try {

            String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
            wfile = new FileWriter(fname, false);
            writer = new BufferedWriter(wfile);

            db.execSQL("DELETE FROM P_LIQUIDACION");

            sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
            DT = Con.OpenDT(sql);

            if (DT.getCount() > 0) {

                DT.moveToFirst();

                val = DT.getString(0);

            } else {
                val = "N";
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            val = "N";
        }

        if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
        else gl.peStockItf = false;


        listItems.clear();
        scomp = 0;
        idbg = "";
        stockflag = 0;

        ftmsg = "";
        ftflag = false;

        try {

            if (!AddTable("P_PARAMEXT")) return false;
            procesaParamsExt();

            if (!AddTable("P_NIVELPRECIO")) return false;

            if (!AddTable("P_RUTA")) return false;
            if (!AddTable("P_CLIENTE")) return false;
            if (!AddTable("P_CLIENTE_FACHADA")) return false;
            if (!AddTable("P_CLIRUTA")) return false;
            if (!AddTable("P_CLIDIR")) return false;
            if (!AddTable("P_PRODUCTO")) return false;
            if (!AddTable("P_FACTORCONV")) return false;
            if (!AddTable("P_LINEA")) return false;
            if (!AddTable("P_PRODPRECIO")) return false;
            if (!AddTable("TMP_PRECESPEC")) return false;
            if (!AddTable("P_DESCUENTO")) return false;
            if (!AddTable("P_EMPRESA")) return false;
            if (!AddTable("P_SUCURSAL")) return false;
            if (!AddTable("P_BANCO")) return false;
            if (!AddTable("P_STOCKINV")) return false;

            if (!AddTable("P_CODATEN")) return false;
            if (!AddTable("P_CODDEV")) return false;
            if (!AddTable("P_CODNOLEC")) return false;
            if (!AddTable("P_NIVELPRECIO")) return false;
            if (!AddTable("P_COREL")) return false;
            if (!AddTable("P_CORELNC")) return false;
            if (!AddTable("P_CORRELREC")) return false;
            if (!AddTable("P_CORREL_OTROS")) return false;
            if (!AddTable("P_STOCK_APR")) return false;
            if (!AddTable("P_STOCK")) return false;
            if (!AddTable("P_STOCKB")) return false;
            if (!AddTable("P_STOCK_PALLET"))
                return false;//#CKFK 20190304 10:48 Se agregó esta tabla para poder importar los pallets
            if (!AddTable("P_COBRO")) return false;
            if (!AddTable("P_CLIGRUPO")) return false;
            if (!AddTable("P_MEDIAPAGO")) return false;
            if (!AddTable("P_BONIF")) return false;
            if (!AddTable("P_BONLIST")) return false;
            if (!AddTable("P_PRODGRUP")) return false;
            if (!AddTable("P_IMPUESTO")) return false;
            if (!AddTable("P_VENDEDOR")) return false;
            if (!AddTable("P_MUNI")) return false;
            if (!AddTable("P_VEHICULO")) return false;
            if (!AddTable("P_HANDHELD")) return false;
            if (!AddTable("P_IMPRESORA")) return false;

            if (!AddTable("P_REF1")) return false;
            if (!AddTable("P_REF2")) return false;
            if (!AddTable("P_REF3")) return false;

            if (!AddTable("P_ARCHIVOCONF")) return false;
            if (!AddTable("P_ENCABEZADO_REPORTESHH")) return false;
            if (!AddTable("P_PORCMERMA")) return false;


            // Objetivos

            if (!AddTable("O_RUTA")) return false;
            if (!AddTable("O_COBRO")) return false;
            if (!AddTable("O_PROD")) return false;
            if (!AddTable("O_LINEA")) return false;


            // Mercadeo

            if (!AddTable("P_MEREQTIPO")) return false;
            if (!AddTable("P_MEREQUIPO")) return false;
            if (!AddTable("P_MERESTADO")) return false;
            if (!AddTable("P_MERPREGUNTA")) return false;
            if (!AddTable("P_MERRESP")) return false;
            if (!AddTable("P_MERMARCACOMP")) return false;
            if (!AddTable("P_MERPRODCOMP")) return false;

            //if (gl.contlic) {
            //	if (!AddTable("LIC_CLIENTE")) return false;
            //}

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            return false;
        }

        ferr = "";

        try {

            rc = listItems.size();
            reccnt = rc;
            if (rc == 0) return true;

            fprog = "Procesando ...";
            wsRtask.onProgressUpdate();

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase = dbT;
            insT = ConT.Ins;

            prn = 0;
            jj = 0;

            Log.d("M", "So far we are good");

            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {

                sql = listItems.get(i);
                esql = sql;
                sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
                sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");

                try {
                    writer.write(sql);
                    writer.write("\r\n");
                } catch (Exception e) {
                    Log.d("M", "Something happend here " + e.getMessage());
                }

                try {
                    dbT = ConT.getWritableDatabase();
                    dbT.execSQL(sql);
                } catch (Exception e) {
                    Log.d("M", "Something happend there " + e.getMessage());
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage() + "EJC", "Yo fui " + sql);
                    Log.e("z", e.getMessage());
                }

                try {
                    if (i % 10 == 0) {
                        fprog = "Procesando: " + i + " de: " + (rc - 1);
                        wsRtask.onProgressUpdate();
                        SystemClock.sleep(20);
                    }
                } catch (Exception e) {
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    Log.e("z", e.getMessage());
                }
            }

            fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
            wsRtask.onProgressUpdate();

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            Log.d("M", "We are ok");

            fprog = "Documento de inventario recibido en BOF...";
            wsRtask.onProgressUpdate();

            fprog = "Fin de la actualización";
            wsRtask.onProgressUpdate();

            scomp = 1;

            try {
                ConT.close();
            } catch (Exception e) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            }

            try {
                writer.close();
            } catch (Exception e) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                msgbox(new Object() {
                }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            }

            return true;

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fprog = "Actualización incompleta";
            wsRtask.onProgressUpdate();

            Log.e("Error", e.getMessage());
            try {
                ConT.close();
            } catch (Exception ee) {
                addlog(new Object() {
                }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            }

            sstr = e.getMessage();
            ferr = sstr + "\n" + sql;
            esql = sql;
            return false;
        }

    }

    private void procesaParamsExt() {
        Cursor dt;
        String sql, val = "";
        int ival, rc;

        try {

            rc = listItems.size();
            reccnt = rc;
            if (rc == 0) return;

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase = dbT;
            insT = ConT.Ins;

            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {

                sql = listItems.get(i);
                esql = sql;
                dbT.execSQL(sql);

                try {
                    if (i % 10 == 0) {

                        SystemClock.sleep(20);
                    }
                } catch (Exception e) {
                    Log.e("z", e.getMessage());
                }
            }

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            try {
                sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
                dt = Con.OpenDT(sql);
                dt.moveToFirst();
                val = dt.getString(0);
            } catch (Exception e) {
                val = "N";
            }
            if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
            else gl.peStockItf = false;

            try {
                sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=3";
                dt = Con.OpenDT(sql);
                dt.moveToFirst();
                gl.peModal = dt.getString(0).toUpperCase();
            } catch (Exception e) {
                gl.peModal = "-";
            }

            try {
                ConT.close();
            } catch (Exception e) {
            }

        } catch (Exception e) {
            try {
                ConT.close();
            } catch (Exception ee) {
            }
        }

    }

    private boolean AddTable(String TN) {
        String SQL;

        try {

            fprog = TN;
            idbg = TN;
            wsRtask.onProgressUpdate();
            SQL = getTableSQL(TN);

            if (fillTable(SQL, "DELETE FROM " + TN) == 1) {
                if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
                idbg = idbg + SQL + "#" + "PASS OK";
                return true;
            } else {
                if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
                idbg = idbg + SQL + "#" + " PASS FAIL  ";
                fstr = "Tab:" + TN + " " + sstr;
                return false;
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = "Tab:" + TN + ", " + e.getMessage();
            idbg = idbg + e.getMessage();
            return false;
        }
    }

    private String getTableSQL(String TN) {
        String SQL = "";


        if (TN.equalsIgnoreCase("P_CLIRUTA")) {
            SQL = "SELECT RUTA,CLIENTE,SEMANA,DIA,SECUENCIA,-1 AS BANDERA FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'";
            return SQL;
        }

        if (TN.equalsIgnoreCase("P_SUCURSAL")) {
            SQL = " SELECT CODIGO, EMPRESA, DESCRIPCION, NOMBRE, DIRECCION, TELEFONO, NIT, TEXTO " +
                    " FROM P_SUCURSAL WHERE CODIGO IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '" + ActRuta + "')";
            return SQL;
        }

        return SQL;
    }

    private boolean validaDatos(boolean completo) {

        Cursor dt;

        try {

            if (!rutatipo.equalsIgnoreCase("P")) {
                sql = "SELECT RESOL FROM P_COREL";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    msgbox("No está definido correlativo de facturas");
                    return false;
                }
            }

            sql = "SELECT Codigo FROM P_CLIENTE";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                msgbox("Lista de clientes está vacia");
                return false;
            }

            sql = "SELECT Ruta FROM P_CLIRUTA";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                msgbox("Lista de clientes por ruta está vacia");
                return false;
            }

            sql = "SELECT Codigo FROM P_PRODUCTO";
            dt = Con.OpenDT(sql);
            if (dt.getCount() == 0) {
                msgbox("Lista de productos está vacia");
                return false;
            }

            if (completo) {

                sql = "SELECT Nivel FROM P_PRODPRECIO ";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    msgbox("Lista de precios está vacia");
                    return false;
                }

                sql = "SELECT Producto FROM P_FACTORCONV ";
                dt = Con.OpenDT(sql);
                if (dt.getCount() == 0) {
                    msgbox("Lista de conversiones está vacia");
                    return false;
                }

                if (gl.peStockItf) {
                    sql = "SELECT Codigo FROM P_STOCK ";
                    dt = Con.OpenDT(sql);
                    if (dt.getCount() == 0) {
                        msgbox("La carga de productos está vacia");
                        return false;
                    }
                }

            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            Log.d("ValidaDatos", e.getMessage());
        }

        return true;
    }

    //#CKFK_20190325 Agregué funcion ActualizaStatcom que es una copia de FinDia pero sin el CommitSQL
    private boolean ActualizaStatcom() {

        try {

            db.beginTransaction();
            db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
            db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
            db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
            db.execSQL("UPDATE D_CXC SET STATCOM='S'");
            db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
            db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
            db.execSQL("UPDATE D_MOV SET STATCOM='S'");
            db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
            db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
            db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
            db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
            db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
            db.execSQL("UPDATE P_RUTA SET PARAM2 = ''");
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            msgbox("ActualizaStatcom(): " + e.getMessage());
            return false;
        }
        return true;
    }

    private void encodeData() {
        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                encodePrinters();
            }
        };
        mtimer.postDelayed(mrunner,200);
    }

    private void encodePrinters() {
        CryptUtil cu=new CryptUtil();
        Cursor dt;
        String prid,ser,se;

        try {
            sql="SELECT IDIMPRESORA,NUMSERIE FROM P_IMPRESORA";
            dt=Con.OpenDT(sql);

            if (dt.getCount() > 0) dt.moveToFirst();
            while (!dt.isAfterLast()) {

                prid=dt.getString(0);
                ser=dt.getString(1);
                se=cu.encrypt(ser);

                sql="UPDATE P_IMPRESORA SET NUMSERIE='"+se+"' WHERE IDIMPRESORA='"+prid+"'";
                db.execSQL(sql);

                dt.moveToNext();
            }

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    //endregion

    //region WS Recepcion Handling Methods

    public void wsExecute() {

        running = 1;
        fstr = "No connect";
        scon = 0;

        try {

            if (getTest() == 1) scon = 1;

            idbg = idbg + sstr;

            if (scon == 1) {
                fstr = "Sync OK";
                if (!getData()) fstr = "Recepcion incompleta : " + fstr;
            } else {
                fstr = "No se puede conectar al web service : " + sstr;
            }

        } catch (Exception e) {
            scon = 0;
            fstr = "No se puede conectar al web service. " + e.getMessage();
            Log.d("E", fstr + sstr);
        }

    }

    public void wsFinished() {

        barInfo.setVisibility(View.INVISIBLE);
        lblParam.setVisibility(View.INVISIBLE);
        running = 0;
        try {
            if (fstr.equalsIgnoreCase("Sync OK")) {

                lblInfo.setText(" ");
                s = "Recepción completa.";

                if (stockflag == 1) {
                    s = s + "\nSe actualizó inventario.";
                }

                clsAppM.estandartInventario();
                validaDatos(true);
                encodeData();

                msgAskExit(s);

            } else {
                lblInfo.setText(fstr);
                mu.msgbox("Ocurrió error : \n" + fstr + " (" + reccnt + ") ");
                mu.msgbox("::" + esql);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                addlog("Recepcion", fstr, esql);
                return;
            }


            isbusy = 0;

            paramsExtra();
            //mu.msgbox("::"+esql);

            if (ftflag) msgbox(ftmsg);
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }


    }

    private class AsyncCallRec extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                wsExecute();
            } catch (Exception e) {
                if (scon == 0) {
                    fstr = "No se puede conectar al web service : " + sstr;
                    //lblInfo.setText(fstr);
                }
                //msgbox(fstr);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsFinished();
            } catch (Exception e) {
                Log.d("onPostExecute", e.getMessage());
            }

        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                synchronized (this) {
                    if (!lblInfo.getText().toString().matches("")) lblInfo.setText(fprog);
                }
            } catch (Exception e) {
                Log.d("onProgressUpdate", e.getMessage());
            }
        }

    }

    //endregion

    //region WS Envio Methods

    private boolean sendData() {

        errflag = false;

        senv = "Envío terminado \n \n";

        items.clear();
        dbld.clearlog();

        try {
            envioCoord();
            dbld.savelog();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }

        return errflag;
    }

    public void envioCoord() {
        Cursor DT;
        String cod, ss;
        int stp;
        double px, py;
        fprog = " ";
        wsStask.onProgressUpdate();

        try {
            sql = "SELECT CODIGO,COORX,COORY,STAMP FROM D_CLICOORD WHERE STATCOM='N'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                cod = DT.getString(0);
                px = DT.getDouble(1);
                py = DT.getDouble(2);
                stp = DT.getInt(3);

                try {

                   dbld.clear();

                    ss = "UPDATE P_CLIENTE SET COORX=" + px + ",COORY=" + py + " WHERE (CODIGO='" + cod + "')";
                    dbld.add(ss);

                         if (commitSQL() == 1) {
                            sql = "UPDATE D_CLICOORD SET STATCOM='S' WHERE (CODIGO='" + cod + "') AND (STAMP=" + stp + ") ";
                            db.execSQL(sql);
                        } else {
                            fterr += "\n" + sstr;
                        }


                } catch (Exception e) {
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    fterr += "\n" + e.getMessage();
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = e.getMessage();
        }

    }

    public void addItem(String nombre,int env,int pend) {
        clsClasses.clsEnvio item;

        try {
            item=clsCls.new clsEnvio();

            item.Nombre=nombre;
            item.env=env;
            item.pend=pend;

            items.add(item);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
        }
    }

    //endregion

    //region WS Envio Handling Methods

    public void wsSendExecute(){

        running=1;fstr="No connect";scon=0;
        errflag=false;

        try {

            if (getTest()==1) scon=1;

            if (scon==1) {
                fstr="Sync OK";

                if (!sendData()) {
                    fstr="Envio incompleto : "+sstr;
                } else {
                }
            } else {
                fstr="No se puede conectar al web service : "+sstr;
            }

        } catch (Exception e) {
            scon=0;
            fstr="No se puede conectar al web service. "+e.getMessage();
            Log.d("E", fstr + sstr);
        }
    }

    public void wsSendFinished(){

        barInfo.setVisibility(View.INVISIBLE);
        lblParam.setVisibility(View.INVISIBLE);
        running=0;

        //senv="Envio completo\n";

        try{
            if (scon==0) {
                lblInfo.setText(fstr);writeErrLog(fstr);
                mu.msgbox(fstr);
                lblInfo.setText(fstr);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                addlog("Envío", fterr + " " + fstr, esql);
                return;
            }

            if (!errflag) {
                lblInfo.setText(" ");
                msgResultEnvio(senv);

            } else {
                lblInfo.setText(fterr);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
                mu.msgbox("Ocurrió error : \n" + fterr );
                addlog("Envío", fterr, esql);
                return;
            }

            //if (!errflag) ComWS.super.finish();

            isbusy=0;

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private class AsyncCallSend extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                Looper.prepare();
                wsSendExecute();
            } catch (Exception e) {
                if (scon == 0) {
                    fstr = "No se puede conectar al web service : " + sstr;
                    //lblInfo.setText(fstr);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                wsSendFinished();
                Looper.loop();
            }catch (Exception e) {
                Log.d("onPostExecute", e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                lblInfo.setText(fprog);
            } catch (Exception e) { }
        }

    }

    //endregion

    //region Aux

    public void comManual(View view) {
        try{
            Intent intent = new Intent(this,ComDrop.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void getWSURL() {
        Cursor DT;
        String wsurl;

        txtRuta.setText(ruta);
        txtEmp.setText(gEmpresa);

        try {

            sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
                DT.moveToFirst();

                //if (gl.tipo==0) {
                //	wsurl=DT.getString(1);
                //} else {
                //	wsurl=DT.getString(0);
                //}

                wsurl=DT.getString(0);

                URL=wsurl;
                txtWS.setText(URL);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            //MU.msgbox(e.getMessage());
            //URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
            URL="*";txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");
            //URL="*";txtWS.setText("http://192.168.1.142/wsimagen/baktun1.asmx");
            //txtWS.setText("");
            return;

        }

    }

    private boolean setComParams() {
        String ss;

        ss=txtRuta.getText().toString().trim();

        try{
            if (mu.emptystr(ss)) {
                mu.msgbox("La ruta no esta definida.");return false;
            }
            ActRuta=ss;

            ss=txtEmp.getText().toString().trim();
            if (mu.emptystr(ss)) {
                mu.msgbox("La empresa no esta definida.");return false;
            }
            gEmpresa=ss;

            ss=txtWS.getText().toString().trim();
            //ss="http://192.168.1.142/wsAndr/wsandr.asmx";
            if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
                mu.msgbox("La dirección de Web service no esta definida.");return false;
            }
            URL=ss;
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
        return true;
    }

    private int getDocCount(String ss,String pps) {

        Cursor DT;
        int cnt = 0;
        String st;

        try {

            sql=ss;
            DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
                DT.moveToFirst();
                cnt=DT.getInt(0);
            }

            if (cnt>0) {
                st=pps+" "+cnt;
                sp=sp+st+", ";
            }

        } catch (Exception e) {
            //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            //mu.msgbox(sql+"\n"+e.getMessage());
        }

        return cnt;

    }

    private void paramsExtra() {
        try {
            AppMethods app=new AppMethods(this,gl,Con,db);
            app.parametrosExtra();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(e.getMessage());
        }
    }

    private void writeErrLog(String errstr) {
        BufferedWriter writer = null;
        FileWriter wfile;

        try {
            String fname = Environment.getExternalStorageDirectory()+"/roaderror.txt";

            wfile=new FileWriter(fname,false);
            writer = new BufferedWriter(wfile);
            writer.write(errstr);writer.write("\r\n");
            writer.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    private void restarApp(){
        try{
            PackageManager packageManager = this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent =Intent.makeRestartActivityTask(componentName);
            //Intent mainIntent = IntentCompat..makeRestartActivityTask(componentName);
            this.startActivity(mainIntent);
            System.exit(0);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void msgResultEnvio(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Envio correcto", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskExit(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage(msg);
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (gl.modoadmin) {
                        restarApp();
                    } else {
                        finish();
                    };
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskExitComplete() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle(R.string.app_name);
            dialog.setMessage("Está seguro de salir de la aplicación?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });

            dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void msgAskConfirmaRecibido(){

        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Licencia");
            dialog.setMessage("¿Recibir la licencia?");

            dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runRecep();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        try{
            if (isbusy==0) {
                if (gl.modoadmin) {
                    msgAskExitComplete();
                } else {
                    super.onBackPressed();
                }
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

	/*@Override
	protected void onResume() {
		super.onResume();
		try {
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
	}

	@Override
	protected void onPause() {
		try {
			this.wakeLock.release();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		this.wakelock.release();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		this.wakelock.release();
	}
	*/

    //endregion


}
