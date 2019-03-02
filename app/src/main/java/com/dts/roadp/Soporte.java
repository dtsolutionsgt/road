package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;

import java.io.File;

public class Soporte extends PBase {

    private int tipo;
    private String email,subj,body,fname;
    private File file;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);

        super.InitBase();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        subj="Road :: "+gl.empnom+" "+du.sfecha(fecha)+" "+du.shora(fecha);
        body="Ruta :"+gl.rutanom+"\nVendedor : "+gl.vendnom;
    }

    //region Events

    public void doBaseDatos(View view) {
        tipo=1;
        msgAsk("Enviar la base de datos");
    }

    public void doCarga(View view) {
        tipo=2;
        msgAsk("Enviar archivo de carga");
    }

    public void doEnvio(View view) {
        tipo=3;
        //msgAsk("Enviar archivo de envio");
        toastcent("Pendiente implementación");
    }

    public void doError(View view) {
        tipo=4;
        msgAsk("Enviar archivo de error de envio");
    }

    public void doBitacora(View view) {
        tipo=5;
        // msgAsk("Enviar archivo de bitacora");
        toastcent("Pendiente implementación");
    }

    //endregion

    //region Main

    private void send(String msg) {
        Intent intent;

        try {
            if (!file.exists()) {
                msgbox(msg);return;
            }

            uri = Uri.fromFile(file);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "dtsolutionsgt@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subj);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            //startActivity(Intent.createChooser(emailIntent, null));
            startActivity(emailIntent);

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void sendDBase() {
        body="Base de datos \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/road.db";
        file=new File(fname);

        send("No se pudo enviar la base de datos datos");
    }

    private void sendCarga() {
        body="Carga \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/roadcarga.txt";
        file=new File(fname);

        send("El archivo de carga no existe. Antes de enviar, debe realizar una carga de datos");
    }

    private void sendEnvio() {

        send("");
    }

    private void sendError() {
        body="Error de envío \n"+body;
        fname= Environment.getExternalStorageDirectory()+"/roaderr.txt";
        file=new File(fname);

        send("El archivo de detalle de error de envío no existe");

    }

    private void sendBitacora() {

        send("");
    }

    //endregion

    //region Aux

    private void msgAsk(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Road Soporte");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                switch (tipo) {
                    case 1:
                        sendDBase();break;
                    case 2:
                        sendCarga();break;
                    case 3:
                        sendEnvio();break;
                    case 4:
                        sendError();break;
                    case 5:
                        sendBitacora();break;
                 }
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

    //endregion

}
