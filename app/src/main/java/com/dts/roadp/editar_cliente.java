package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class editar_cliente extends PBase {

    private TextView txtNombreCliente, txtNitCliente, txtCanalCliente, txtSubCanalCliente;
    private ImageView btnBuscarCanal, imgRegresar,imgGuardar;
    private String codigo, nombre, ruc, canal, subcanal, idCanal, idSubcanal;
    private TableRow trNombre, trNit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cliente);

        super.InitBase();
        btnBuscarCanal = (ImageView) findViewById(R.id.btnBuscarCanal);
        imgGuardar = (ImageView) findViewById(R.id.imgGuardar);
        txtNombreCliente = (TextView) findViewById(R.id.txtNombreCliente);
        txtNitCliente = (TextView) findViewById(R.id.txtNitCliente);
        txtCanalCliente = (TextView) findViewById(R.id.txtCanalCliente);
        txtSubCanalCliente = (TextView) findViewById(R.id.txtSubCanalCliente);
        trNombre = (TableRow) findViewById(R.id.trNombre);
        trNit = (TableRow) findViewById(R.id.trNit);

        codigo = gl.EditarClienteCodigo;
        nombre = gl.EditarClienteNombre;
        ruc = gl.EditarClienteRuc;
        canal = gl.EditarClienteCanal;
        subcanal = gl.EditarClienteSubcanal;
        idCanal = gl.IdCanal;
        idSubcanal = gl.IdSubcanal;

        txtNombreCliente.setText(nombre);
        txtNitCliente.setText(ruc);
        txtCanalCliente.setText(canal);
        txtSubCanalCliente.setText(subcanal);

        if (!gl.peEditarNombre) {
            trNombre.setVisibility(View.GONE);
        }

        if  (!gl.peEditarNit) {
            trNit.setVisibility(View.GONE);
        }

        setHandlers();

    }

    private void GuardarData() {
        Cursor DT;
        String corel = mu.getCorelBase();
        String tabla = "D_CLIENTE_MODIF";

        try {

            String txtCanal = txtCanalCliente.getText().toString();
            String txtSubCanal = txtSubCanalCliente.getText().toString();

            if (mu.emptystr(txtCanal) || mu.emptystr(txtSubCanal)) {
                toast("Los campos canal y subcanal son obligatorios");
            } else {
                //db.execSQL("DELETE FROM D_CLIENTE_MODIF");
                if (validaCliente() == 0) {
                    toast("ValidaCliente: "+validaCliente()+" codigo: "+codigo);
                    ins.init(tabla);

                    ins.add("COREL", corel);
                    ins.add("CODIGO", codigo);
                    ins.add("NOMBRE", nombre);
                    ins.add("CANAL", canal);
                    ins.add("NIT", ruc);
                    ins.add("SUBCANAL", subcanal);
                    ins.add("BLOQUEADO", "");
                    ins.add("TIPONEG", "");
                    ins.add("TIPO", "");
                    ins.add("SUBTIPO", "");
                    ins.add("NIVELPRECIO", "");
                    ins.add("MEDIAPAGO", "");
                    ins.add("LIMITECREDITO", "");
                    ins.add("DIACREDITO", "");
                    ins.add("DESCUENTO", "");
                    ins.add("BONIFICACION", "");
                    ins.add("ULTVISITA", "");
                    ins.add("IMPSPEC", "");
                    ins.add("INVTIPO", "");
                    ins.add("INVEQUIPO", "");
                    ins.add("INV1", "");
                    ins.add("INV2", "");
                    ins.add("INV3", "");
                    ins.add("MENSAJE", "");
                    ins.add("TELEFONO", "");
                    ins.add("DIRTIPO", "");
                    ins.add("DIRECCION", "");
                    ins.add("SUCURSAL", "");
                    ins.add("COORX", "");
                    ins.add("COORY", "");
                    ins.add("FIRMADIG", "");
                    ins.add("CODBARRA", "");
                    ins.add("VALIDACREDITO", "");
                    ins.add("PRECIO_ESTRATEGICO", "");
                    ins.add("NOMBRE_PROPIETARIO", "");
                    ins.add("NOMBRE_REPRESENTANTE", "");
                    ins.add("BODEGA", "");
                    ins.add("COD_PAIS", "");
                    ins.add("FACT_VS_FACT", "");
                    ins.add("CHEQUEPOST", "");
                    ins.add("PERCEPCION", "");
                    ins.add("TIPO_CONTRIBUYENTE", "");
                    ins.add("ID_DESPACHO", "");
                    ins.add("ID_FACTURACION", "");
                    ins.add("MODIF_PRECIO", "");
                    ins.add("INGRESA_CANASTAS", "");
                    ins.add("PRIORIZACION", "");
                    ins.add("STATCOM", "");

                    db.execSQL(ins.sql());

                    toast("Cliente actualizado correctamente");
                    UpdClienteP();
                    limpiar();
                } else {

                    upd.init(tabla);
                    upd.add("CANAL", canal);
                    upd.add("SUBCANAL", subcanal);
                    upd.Where("CODIGO='"+codigo+"'");

                    db.execSQL(upd.SQL());

                    toast("Cliente actualizado correctamente");
                    UpdClienteP();
                    limpiar();

                }
            }

            RegresarCLiDet();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            mu.msgbox(e.getMessage());
        }
    }

    private void UpdClienteP() {
        try {
            upd.init("P_CLIENTE");
            upd.add("CANAL", idCanal);
            upd.add("SUBCANAL", idSubcanal);
            upd.Where("CODIGO='"+codigo+"'");

            db.execSQL(upd.SQL());
        } catch (Exception e) {
            addlog(new Object() { }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            mu.msgbox(e.getMessage());
        }
    }

    public int validaCliente() {
        opendb();
        Cursor DT;

        sql = "SELECT CODIGO FROM D_CLIENTE_MODIF WHERE CODIGO='"+codigo+"'";
        DT=Con.OpenDT(sql);

        return  DT.getCount();

    }

    private  void VerCanales() {
        Intent intent = new Intent(this, CanalSubcanal.class);
        startActivity(intent);
    }

    private  void RegresarCLiDet() {
        limpiar();
        //Intent intent = new Intent(this, Clientes.class);
        //startActivity(intent);
        finish();
    }

    private void limpiar() {
        gl.EditarClienteNombre = "";
        gl.EditarClienteRuc = "";
        gl.EditarClienteCanal = "";
        gl.EditarClienteSubcanal = "";

        txtNombreCliente.setText("");
        txtNitCliente.setText("");
        txtCanalCliente.setText("");
        txtSubCanalCliente.setText("");

    }

    private void setHandlers() {
        btnBuscarCanal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerCanales();
            }
        });

        imgGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuardarData();
            }
        });

    }

    @Override
    public void onBackPressed() {
        try{
            RegresarCLiDet();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }
}