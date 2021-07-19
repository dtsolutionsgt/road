package com.dts.roadp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Canastas extends PBase {
    private ListView listView;
    private TextView lblRec;
    private TextView lblEntr;
    private EditText txtCanastasEnt, txtCanastasRec;
    private AlertDialog ad;
    private int regFecha;
    private boolean editando;
    private String producto = "";

    ListAdaptCanasta adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canastas);

        super.InitBase();
        addlog("Canastas",""+du.getActDateTime(),gl.vend);

        listView = findViewById(R.id.listaCanastas);
        lblRec = findViewById(R.id.lblTotRec);
        lblEntr = findViewById(R.id.lblTotEntr);
        createDialog();
        setHandlers();
        listItems();
    }

    private void createDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View vistaDialog = inflater.inflate(R.layout.dialog_canastas, null, false);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.canastas);
        dialog.setTitle("Canastas");
        dialog.setView(vistaDialog);

        txtCanastasEnt = (EditText) vistaDialog.findViewById(R.id.txtCanastasEnt);
        txtCanastasRec = (EditText) vistaDialog.findViewById(R.id.txtCanastasRec);

        dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveCanastas();
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clearCanastas();
                setTitulo();
            }
        });
        ad = dialog.create();
    }

    private void setHandlers() {

        try {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {

                        Object lvObj = listView.getItemAtPosition(position);
                        clsClasses.clsCanasta item = (clsClasses.clsCanasta) lvObj;
                        adapter.setSelectedIndex(position);

                        editarRegistro(item);

                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                        mu.msgbox(e.getMessage());
                    }
                };
            });

            txtCanastasRec.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER) {
                        saveCanastas();
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void listItems() {
        ArrayList<clsClasses.clsCanasta> items = new ArrayList<clsClasses.clsCanasta>();
        int tEntr = 0, tRec = 0;

        try {
            String sql;
            Cursor DT;

            sql = "SELECT " +
                "a.RUTA, " +
                "a.FECHA, " +
                "a.CLIENTE, " +
                "a.PRODUCTO, " +
                "a.CANTREC, " +
                "a.CANTENTR," +
                "b.CODIGO, " +
                "b.DESCCORTA, " +
                "b.DESCLARGA " +
                "FROM D_CANASTA a " +
                "INNER JOIN P_PRODUCTO b ON b.CODIGO = a.PRODUCTO " +
                "WHERE a.CLIENTE = '" + gl.cliente +"' " +
                "ORDER BY a.FECHA DESC;";

            DT = Con.OpenDT(sql);

            if (DT == null || DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsClasses.clsCanasta item = clsCls.new clsCanasta();


                item.fecha = DT.getInt(1);
                item.fechaFormato = du.sfecha(item.fecha) + " "+ du.shora(item.fecha);
                item.cliente = DT.getString(2);
                item.producto = DT.getString(3);
                item.cantrec = DT.getInt(4);
                item.cantentr = DT.getInt(5);
                item.codigo = DT.getString(6);
                item.desccorta = DT.getString(7);
                item.desclarga = DT.getString(8);

                items.add(item);
                tEntr += item.cantentr;
                tRec += item.cantrec;

                DT.moveToNext();
            }

            DT.close();
        }catch (Exception e) {
            mu.msgbox( e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            Log.d("listaCanasta", e.getMessage());
        }

        adapter = new ListAdaptCanasta(this,items);
        listView.setAdapter(adapter);

        lblRec.setText(String.valueOf(tRec));
        lblEntr.setText(String.valueOf(tEntr));
    }

    public void editarRegistro(clsClasses.clsCanasta reg) {
        try {
            gl.prodCanasta = reg.producto;
            regFecha = reg.fecha;
            producto = reg.producto;
            txtCanastasEnt.setText(reg.cantentr+"");
            txtCanastasRec.setText(reg.cantrec+"");
            editando = true;
            setTitulo();
            ad.show();
        } catch (Exception e){
            mu.msgbox(e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void showCanastas(View view) {
        try {
            editando = false;
            gl.prodCanasta = "";
            regFecha = 0;
            clearCanastas();
            setTitulo();

            Intent i = new Intent(this, TipoCanasta.class);
            startActivity(i);

            ad.show();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    public void saveCanastas() {
        try {
            opendb();
            int cantRec = Integer.parseInt(txtCanastasRec.getText().toString());
            int cantEntr = Integer.parseInt(txtCanastasEnt.getText().toString());

            if (!editando) {
                ins.init("D_CANASTA");
                ins.add("RUTA", gl.ruta);
                ins.add("FECHA", du.getActDateTime());
                ins.add("CLIENTE", gl.cliente);
                ins.add("PRODUCTO", gl.prodCanasta);
                ins.add("CANTREC", cantRec);
                ins.add("CANTENTR", cantEntr);
                ins.add("STATCOM", "N");

                db.execSQL(ins.sql());
            }else {
                upd.init("D_CANASTA");
                upd.Where(
                    "RUTA = '" +
                    gl.ruta +
                    "' AND CLIENTE = '" +
                    gl.cliente +
                    "' AND PRODUCTO = '" +
                    producto+
                    "' AND FECHA = " +
                    regFecha
                );
                upd.add("PRODUCTO", producto);
                upd.add("CANTREC", cantRec);
                upd.add("CANTENTR", cantEntr);
                upd.add("STATCOM", "N");
                db.execSQL(upd.SQL());
            }
            clearCanastas();
            listItems();
            db.close();

            toast("Se guardó correctamente el registro de canastas");
        }catch (Exception e) {
            mu.msgbox("Ocurrió un error: "+ e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        } finally {
            editando = false;
            gl.prodCanasta = "";
            regFecha = 0;
            ad.dismiss();
        }
    }

    public void clearCanastas() {
        txtCanastasRec.setText("");
        txtCanastasEnt.setText("");
    }

    public void setTitulo() {
        ad.setTitle("Canastas");
        if (gl.prodCanasta != "") {
            opendb();
            String sql = "SELECT desccorta, desclarga from p_producto WHERE codigo = '" + gl.prodCanasta + "'";
            Cursor DT = Con.OpenDT(sql);

            if (DT != null && DT.getCount() >= 1) {
                DT.moveToFirst();
                String nom = DT.getString(0);

                ad.setTitle(nom);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mu.emptystr(gl.prodCanasta)){
            ad.setTitle("Canastas");
            ad.dismiss();
        }else {
            setTitulo();
        }
    }
}