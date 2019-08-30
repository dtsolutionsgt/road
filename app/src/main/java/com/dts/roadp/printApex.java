package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentExPCL_PP.PaperWidth;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.BarcodeExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.RotationAngle;
import honeywell.printer.configuration.expcl.BluetoothConfiguration_ExPCL;
import honeywell.printer.configuration.expcl.GeneralStatus_ExPCL;
import honeywell.printer.configuration.expcl.PrinterOptions_ExPCL;
import honeywell.printer.configuration.expcl.PrintheadStatus_ExPCL;
import honeywell.printer.configuration.expcl.VersionInformation_ExPCL;

// 00:17:AC:15:EC:C3

public class printApex extends printBase {

    private String ss,ess;
    private appGlobals appG;
    private PBase clsPBase;
    private boolean validprint;

    private Connection_Bluetooth connection_bluetooth;

    private ArrayList<String> lines = new ArrayList<String>();
    private ArrayList<DocumentExPCL_PP> documentlistPP = new  ArrayList<DocumentExPCL_PP>();

    private DocumentExPCL_PP docExPCL_PP;
    private ParametersExPCL_PP paramExPCL_PP;

    public printApex(Context context,String printerMAC,boolean validprinter) {
        super(context,printerMAC);
        validprint=validprinter;
        appG = new appGlobals();
        clsPBase=new PBase();

        docExPCL_PP = new DocumentExPCL_PP(PaperWidth.PaperWidth_384);
        paramExPCL_PP = new ParametersExPCL_PP();
    }


    // Main

    public void printask(Runnable callBackHook) {

        hasCallback=true;
        callback=callBackHook;

        fname="print.txt";errmsg="";exitprint=false;
        msgAskPrint();
    }

    public void printask() {
        hasCallback=false;

        fname="print.txt";errmsg="";exitprint=false;
        msgAskPrint();
    }

    public void printask(Runnable callBackHook, String fileName){
        hasCallback=true;
        callback=callBackHook;

        fname=fileName;errmsg="";exitprint=false;
        msgAskPrint();
    }

    public void printnoask(Runnable callBackHook, String fileName){
        hasCallback=true;
        callback=callBackHook;

        fname=fileName;errmsg="";
        try {
            if (loadFile())  doStartPrint();
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());
        }
    }

    public boolean print() 	{
        hasCallback=false;

        fname="print.txt";errmsg="";

        try	{
            if (loadFile())	doStartPrint();	else return false;
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());return false;
        }

        return true;
    }

   public boolean printBarra(ArrayList<String> listitems) 	{
        hasCallback=false;

        try	{
            if (loadFileBarra(listitems)){
                doStartPrintBarra();
            }
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());return false;
        }

        return true;
    }

    public void printask(String fileName) {
        hasCallback=false;

        fname=fileName;	errmsg="";
        exitprint=false;
        msgAskPrint();
    }

    public boolean print(String fileName) {
        hasCallback=false;

        fname=fileName;errmsg="";

        try 		{
            if (loadFile())	{
                doStartPrint();
            } else {
                return false;
            }
        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());return false;
        }

        return true;
    }


    // Private

    private boolean loadFile() {

        File ffile;
        String ss;

        try {

            File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
            ffile = new File(file1.getPath());

        } catch (Exception e) {
            showmsg("Error: " + e.getMessage());
            return false;
        }

        try {

            byte[] readBuffer = new byte[(int) ffile.length()];
            InputStream inputStream = new BufferedInputStream(new FileInputStream(ffile));
            if(inputStream.read(readBuffer) == 0)
                throw new Exception("Imposible leer el archivo o el archivo está vacío");
            inputStream.close();
            printData = readBuffer;

            return true;

        } catch (Exception e) {

            showmsg("Error: " + e.getMessage());

            return false;
        }
    }

   private boolean loadFileBarra(ArrayList<String> listitems) {

        try {

            int ccnt;
            String txt;
            documentlistPP.clear();

            lines = listitems;

            showmsg("Lines : "+lines.size());

            ccnt=lines.size();

            for (int i = 0; i <ccnt; i++) {
                docExPCL_PP = new DocumentExPCL_PP(PaperWidth.PaperWidth_384);
                txt="";
                ss=lines.get(i);
                txt=lines.get(i);
                docExPCL_PP.drawBarCode(0, 150, RotationAngle.RotationAngle_0, true, BarcodeExPCL_PP.Code128, (byte) 25, ss);

                documentlistPP.add(docExPCL_PP);

            }

            return true;

        } catch (Exception e) {
            try {

            } catch (Exception e1) {}

            showmsg("Error: " + e.getMessage());

            return false;
        }
    }

    private void doStartPrint() {
        if (!validprint) {
            showmsg("¡La impresora no está autorizada!");return;
        }

        appG.endPrint = true;
        showmsg("Imprimiendo ..." );
        AsyncPrintCall wsRtask = new AsyncPrintCall();
        wsRtask.execute();
    }

    private void doStartPrintBarra() {
        if (!validprint) {
            showmsg("¡La impresora no está autorizada!");return;
        }

        appG.endPrint = true;
        showmsg("Imprimiendo ..." );
        AsyncPrintBarraCall wsRtask = new AsyncPrintBarraCall();
        wsRtask.execute();
    }

    private class AsyncPrintCall extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                processPrint();
            } catch (Exception e) {
                ss=ss + e.getMessage();
                Log.d("Err_Impr",e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                doCallBack();
            } catch (Exception e) {
                ss=ss+e.getMessage();
                clsPBase.addlog("onPostExecute", "" , ss);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

    private class AsyncPrintBarraCall extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                processPrintBarra();
            } catch (Exception e) {
                ss=ss + e.getMessage();
                Log.d("Err_Impr",e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                doCallBack();
            } catch (Exception e) {
                ss=ss+e.getMessage();
                clsPBase.addlog("onPostExecute", "" , ss);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

    private void doCallBack() {

        if (!hasCallback) return;

        try {
            final Handler cbhandler = new Handler();
            cbhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.run();
                    } catch (Exception ee) {}
                }
            }, 500);
        } catch (Exception e) {
            ess=e.getMessage();
            ss=ss+e.getMessage();
        }

    }

    public void processPrintBarra() {

        ss="p1..";

        try {

            connection_bluetooth = null;

            //Looper.prepare();

            connection_bluetooth = Connection_Bluetooth.createClient(printerAddress);

            if (!prconn.getIsOpen()) connection_bluetooth.open();

            for (DocumentExPCL_PP doc: documentlistPP){

                printData = doc.getDocumentData();

                int intento =0;

                while (!connection_bluetooth.getIsOpen() && intento <10) {
                    try{

                        connection_bluetooth.open();

                        intento+=1;

                    } catch (Exception e) {
                        ss = ss + "Error : " + e.getMessage()+ ", intento " + intento;
                        Log.d("processPrint_ERR: ", ss);
                        clsPBase.addlog("processPrint", "" , ss);}
                }

                if(!connection_bluetooth.getIsOpen() && intento ==10){
                    showmsg("No fue posible abrir la conexión con la impresora, se intentó: " + intento);
                }else{
                    connection_bluetooth.write(printData,0,printData.length);

                    prthread.sleep(500);

                    connection_bluetooth.clearWriteBuffer();
                }

            }

            connection_bluetooth.close();

        } catch (Exception e) {
            ss = ss + "Error : " + e.getMessage();
            Log.d("processPrint_ERR: ", ss);

            try {
                if (connection_bluetooth != null) prconn.close();
            } catch (Exception ee) {
                ss=ss + ee.getMessage();
            }
        }

    }

    public void processPrint() {

        ss="p1..";

        try {

            connection_bluetooth = null;

            //Looper.prepare();

            connection_bluetooth = Connection_Bluetooth.createClient(printerAddress);

            //CKFK 20190630 06:11 PM agregué a este proceso que intente la conexión con la impresora 10 veces
            // y si no logra abrir la conexión de mensaje de que no pudo establecer conexión
            //Además reduje el sleep a 500
            int intento =0;

            while (!connection_bluetooth.getIsOpen() && intento <10) {
                try{

                    connection_bluetooth.open();

                    intento+=1;

                } catch (Exception e) {
                    ss = ss + "Error : " + e.getMessage()+ ", intento " + intento;
                    Log.d("processPrint_ERR: ", ss);
                    clsPBase.addlog("processPrint", "" , ss);}
            }

            if(!prconn.getIsOpen() && intento ==10){
                showmsg("No fue posible abrir la conexión con la impresora, se intentó: " + intento);
                return;
            }else{
                connection_bluetooth.write(printData);

                prthread.sleep(500);

                connection_bluetooth.clearWriteBuffer();
                //printclose.run();
                connection_bluetooth.close();

            }

            //	if (!prconn.getIsOpen()) prconn.open();

        } catch (Exception e) {
            ss = ss + "Error : " + e.getMessage();
            Log.d("processPrint_ERR: ", ss);

            try {
                if (prconn != null) prconn.close();
            } catch (Exception ee) {
                ss=ss + ee.getMessage();
            }
        }

    }

    private void msgAskRePrint() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage("¿Quiere volver a intentar la impresión?");

        dialog.setCancelable(false);
        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                msgAskPrint();
            }

        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Handler cbhandler = new Handler();
                cbhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            appG.devprncierre=true;
                            printclose.run();
                        } catch (Exception e) {
                            //showmsg(e.getMessage());
                        }

                    }
                }, 200);
            }
        });

        dialog.show();

    }

    // Aux

    private void msgAskPrint() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage("¿La impresora está lista?");

        dialog.setCancelable(false);
        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (loadFile())
                        doStartPrint();
                } catch (Exception e) {
                    showmsg("Error: " + e.getMessage());
                }
            }

        });

        //#EJC20181130:Se comentarió por solicitud de auditor de SAT.
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Handler cbhandler = new Handler();
                cbhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        msgAskRePrint();
                    }
                }, 200);
            }
        });

        dialog.show();

    }

    public void showmsg(String MsgStr) {
        errmsg=MsgStr;
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
