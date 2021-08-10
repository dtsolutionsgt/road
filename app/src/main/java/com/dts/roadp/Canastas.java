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
    private TextView lblRec, lblEntr, lblCanastaStock;
    private EditText txtCanastasEnt, txtCanastasRec;
    private AlertDialog ad;
    private long regFecha;
    private boolean editando;
    private String producto = "";
    private AppMethods app;
    private long fecha;
    private int entregado, recibido, iEntregado;

    ListAdaptCanasta adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canastas);

        super.InitBase();
        addlog("Canastas",""+du.getActDateTime(),gl.vend);
        app= new AppMethods(this, gl, Con, db);
        listView = findViewById(R.id.listaCanastas);
        lblRec = findViewById(R.id.lblTotRec);
        lblEntr = findViewById(R.id.lblTotEntr);
        fecha = app.fechaFactTol(du.getActDateTime());
        entregado=0;
        recibido=0;
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
        lblCanastaStock = (TextView) vistaDialog.findViewById(R.id.lblCanastaStock);

        dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                guardarCanastas();
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

                        if (item.editar) {
                            editarRegistro(item);
                        } else {
                            toastcent("No puede modificar este registro");
                        }

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
                        guardarCanastas();
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
                    "b.DESCLARGA, " +
                    "a.STATCOM " +
                "FROM D_CANASTA a " +
                "INNER JOIN P_PRODUCTO b ON b.CODIGO = a.PRODUCTO " +
                "WHERE a.CLIENTE = '" + gl.cliente +"' " +
                "AND a.ANULADO = 0 " +
                "ORDER BY a.FECHA DESC;";

            DT = Con.OpenDT(sql);

            if (DT == null || DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsClasses.clsCanasta item = clsCls.new clsCanasta();


                item.fecha = DT.getInt(1);
                item.fechaFormato = du.sfecha(item.fecha);
                item.cliente = DT.getString(2);
                item.producto = DT.getString(3);
                item.cantrec = DT.getInt(4);
                item.cantentr = DT.getInt(5);
                item.codigo = DT.getString(6);
                item.desccorta = DT.getString(7);
                item.desclarga = DT.getString(8);
                item.editar = DT.getString(9).equalsIgnoreCase("N");

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
            iEntregado = reg.cantentr;
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

    public void guardarCanastas() {
        boolean cerrarDialog = true;
        try {
            opendb();
            String txtCanRec = txtCanastasRec.getText().toString();
            String txtCanEntr = txtCanastasEnt.getText().toString();

            if (mu.emptystr(txtCanRec) || mu.emptystr(txtCanEntr)) {
                toast("Los campos recibido y entregado son obligatorios.");
                cerrarDialog = false;
                return;
            }
            int cantRec = Integer.parseInt(txtCanRec);
            int cantEntr = Integer.parseInt(txtCanEntr);

            recibido = cantRec;
            entregado = cantEntr;

            if (cantRec == 0 && cantEntr == 0) {
                toast("El campo recibido o entregado debe ser mayor a cero.");
                cerrarDialog = false;
                return;
            }

            if (gl.corelFac != null || !mu.emptystr(gl.corelFac)){
                if (cantEntr == 0 && gl.ingresaCanastas){
                    toast("La cantidad de canastas entregadas debe ser mayor que cero.");
                    cerrarDialog=false;
                    return;
                }
            }

            if (editando) {
                actualizaStock(iEntregado);
            }

            if (!hayExistencias()) {
                toast("No hay suficientes canastas para entregar.");
                cerrarDialog = false;
                return;
            }

            if (!editando) {

                if (!registroValido()) {
                    toastcent("El cliente ya tiene un registro con el tipo de canasta seleccionado.");
                    cerrarDialog = false;
                    return;
                }

                ins.init("D_CANASTA");
                ins.add("RUTA", gl.ruta);
                ins.add("FECHA", fecha);
                ins.add("CLIENTE", gl.cliente);
                ins.add("PRODUCTO", gl.prodCanasta);
                ins.add("CANTREC", cantRec);
                ins.add("CANTENTR", cantEntr);
                ins.add("STATCOM", "N");
                ins.add("CORELTRANS", (gl.corelFac == null || gl.corelFac.isEmpty()) ? "": gl.corelFac);

                db.execSQL(ins.sql());
            } else {
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
            recibido=0;entregado=0;
            iEntregado=0;
            actualizaStock(-cantEntr);
            clearCanastas();
            listItems();
            db.close();

            toast("Se guardó correctamente el registro de canastas");
        }catch (Exception e) {
            mu.msgbox("Ocurrió un error: "+ e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        } finally {
            if (cerrarDialog) {
                editando = false;
                gl.prodCanasta = "";
                regFecha = 0;
                ad.dismiss();
            }
        }
    }

    public void clearCanastas() {
        txtCanastasRec.setText("");
        txtCanastasEnt.setText("");
        txtCanastasEnt.requestFocus();
    }

    public void setTitulo() {
        ad.setTitle("Canastas");
        if (gl.prodCanasta != "") {
            opendb();
            String sql;
            sql = "SELECT desccorta, desclarga from p_producto WHERE codigo = '" + gl.prodCanasta + "'";
            Cursor DT = Con.OpenDT(sql);

            if (DT != null && DT.getCount() >= 1) {
                DT.moveToFirst();
                String nom = DT.getString(0);

                ad.setTitle(nom);
            }

            //Indica la existencia actual de las canastas
            sql = "SELECT CANT FROM P_STOCK WHERE CODIGO='"+ gl.prodCanasta +"'";
            Cursor st = Con.OpenDT(sql);

            if (st != null && st.getCount() >= 1){
                st.moveToFirst();
                int cant = st.getInt(0);
                lblCanastaStock.setText("Existencia: "+cant);
            }else{
                lblCanastaStock.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean registroValido() {
        String sql = "SELECT * FROM D_CANASTA " +
                "WHERE RUTA = '"+ gl.ruta +"' " +
                "AND CLIENTE = '"+gl.cliente+"' " +
                "AND FECHA="+ fecha +" " +
                "AND PRODUCTO= '"+gl.prodCanasta+"';";

        Cursor total = Con.OpenDT(sql);

        if (total != null && total.getCount() >= 1) return false;

        return true;
    }

    public void regresar(View view) {
        finish();
    }

    public boolean hayExistencias() {
        String sql = "SELECT CANT FROM P_STOCK WHERE CODIGO='"+gl.prodCanasta+"'";
        Cursor st = Con.OpenDT(sql);

        if (st != null || st.getCount() >= 1){
            st.moveToFirst();

            int cant = st.getInt(0);
            if (editando) {
                cant += iEntregado;
            }

            if(cant < entregado) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private void actualizaStock(int cantidad) {
        String sql = "SELECT CANT FROM P_STOCK WHERE CODIGO='"+gl.prodCanasta+"'";
        Cursor st = Con.OpenDT(sql);

        if (st != null || st.getCount() >= 1){
            st.moveToFirst();

            int cant = st.getInt(0);
            cant += cantidad;

            upd.init("P_STOCK");
            upd.Where("CODIGO = '"+ gl.prodCanasta +"'");
            upd.add("CANT", cant);

            db.execSQL(upd.SQL());
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
            txtCanastasEnt.requestFocus();
        }
    }
}