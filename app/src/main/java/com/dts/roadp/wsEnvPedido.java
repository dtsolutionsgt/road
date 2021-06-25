package com.dts.roadp;

import android.os.Environment;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class wsEnvPedido extends wsBase {

    private String command,corel;

    private String pdir= Environment.getExternalStorageDirectory().getPath() + "/RoadPedidos";


    public wsEnvPedido(String Url) {
        super(Url);
    }

    public void execute(String commandlist,String correlativo) {
        command=commandlist;
        corel=correlativo;
        super.execute(null);
    }

    @Override
    protected void wsExecute() {
        try {
            if (commit()) {
                FileWriter wfile = new FileWriter(pdir+"/"+corel+".txt", false);
                BufferedWriter writer=new BufferedWriter(wfile);
                writer.write(corel);writer.write("\r\n");writer.close();
            }
        } catch (Exception e) {}
    }

    public boolean commit() {
        String sstr,mNAME = "Commit";

        try {

            SoapObject request = new SoapObject(NAMESPACE, mNAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(command);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + mNAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString();

            if (sstr.equalsIgnoreCase("#")) {
                return true;
            } else {
                error=sstr;return false;
            }
        } catch (Exception e) {
            error=e.getMessage();return false;
        }
    }

}
