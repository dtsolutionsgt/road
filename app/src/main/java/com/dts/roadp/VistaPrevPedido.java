package com.dts.roadp;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VistaPrevPedido extends PBase {

    private TextView txtContenido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_prev_pedido);

        txtContenido = (TextView) findViewById(R.id.txtContenido);


        try {
            readFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void readFile() throws FileNotFoundException {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File archivo = new File(sdcard,"vistaPedidop.txt");

            StringBuilder sb= new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = br.readLine()) != null) {
                sb.append(linea);
                sb.append("\r\n");
            }
            br.close();
            txtContenido.setText(sb.toString());
        } catch (Exception e) {

        }
    }

    public void Cerrar(View view) {
        super.finish();
    }
}