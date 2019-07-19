package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ComWSSend extends PBase {

    private TextView lbl1;
    private ProgressBar pbar;

    private WebService ws;
    private clsDataBuilder dbld;

    private String URL="";
    private int pcount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_wssend);

        super.InitBase();

        lbl1 = (TextView) findViewById(R.id.textView72);lbl1.setText("Enviando . . .");
        pbar = (ProgressBar) findViewById(R.id.progressBar5);

        dbld = new clsDataBuilder(this);

         Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                initSession();
            }
        };
        mtimer.postDelayed(mrunner,500);

    }

    //region Events

    public void doSend(View view) {
         //initSession();
    }

    //endregion

    //region Main

    private void initSession() {
        Cursor dt;

        if (!getWSURL()) return;

        try {
            pcount=0;
            sql="SELECT COUNT(COREL) FROM D_PEDIDO WHERE STATCOM='N'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                pcount = dt.getInt(0);
            } else {
                pcount=0;
            }

            if (pcount==0) toast("No existen pedidos pendientes de envio.");
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgExit(e.getMessage());
        }

        if (pcount==0) {
            finish();return;
        }

        if (pcount==1) {
            lbl1.setText("Enviando 1 pedido . . .");
        } else {
            lbl1.setText("Enviando "+pcount+" pedidos . . .");
        }

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                sendData();
            }
        };
        mtimer.postDelayed(mrunner,200);

    }

    private void sendData() {
        Cursor dt;
        String cor;

        try {

            ws = new WebService(ComWSSend.this, URL);
            dbld.clear();

            sql = "SELECT COREL FROM D_PEDIDO WHERE STATCOM='N'";
            dt = Con.OpenDT(sql);

            dt.moveToFirst();
            while (!dt.isAfterLast()) {
                cor = dt.getString(0);
                dbld.insert("D_PEDIDO", "WHERE COREL='"+cor+"'");
                dbld.insert("D_PEDIDOD","WHERE COREL='"+cor+"'");

                dt.moveToNext();
            }

            ws.sqls.clear();
            for (int i = 0; i <dbld.items.size(); i++) {
                ws.sqls.add(dbld.items.get(i));
            }

            ws.commit();

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            msgExit(e.getMessage());return;
        }

    }

    //endregion

    //region Web Service

    @Override
    public void wsCallBack(int callmode,Boolean throwing,String errmsg) {
        String ss;

        try {
            super.wsCallBack(callmode,throwing, errmsg);

            sql = "UPDATE D_PEDIDO SET STATCOM='S' WHERE STATCOM='N'";
            db.execSQL(sql);

            if (pcount==1) {
                toast("Enviado 1 pedido.");
            } else {
                toast("Enviados "+pcount+" pedidos.");
            }
            finish();
        } catch (Exception e) {
            msgExit("Error de envio : " + e.getMessage());
        }
    }

    //endregion

    //region Aux

    public boolean getWSURL() {
        Cursor dt;

        try {

            sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0){
                dt.moveToFirst();

                URL = dt.getString(1);
                if (gl.isOnWifi==1) URL = dt.getString(0);

                if (URL.isEmpty()) {
                    msgExit("No existe configuración para transferencia de datos");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());return false;
        }

    }

    //endregion

    //region Dialogs

    private void msgExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Envío de pedidos");
        dialog.setMessage(msg+"\nURL:"+URL);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.show();
    }

    //endregion

    //region Activity Events


    //endregion

}
