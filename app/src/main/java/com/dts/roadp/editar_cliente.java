package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class editar_cliente extends PBase {

    private TextView txtNombreCliente, txtNitCliente, txtCanalCliente, txtSubCanalCliente;
    private ImageView btnBuscarCanal, imgRegresar,imgGuardar;
    private RelativeLayout relNomRuc;
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
        relNomRuc = (RelativeLayout) findViewById(R.id.relNomRuc);

        setData();

        if (!gl.peEditarNombre || !gl.peEditarNit ) {
            relNomRuc.setVisibility(View.GONE);
        }

        setHandlers();

    }

    private void GuardarData() {
        Cursor DT;
        String corel = gl.ruta+mu.getCorelBase();
        String tabla = "D_CLIENTE_MODIF";
        long f= du.getActDateTime();

        try {

            String txtCanal = txtCanalCliente.getText().toString();
            String txtSubCanal = txtSubCanalCliente.getText().toString();

            if (mu.emptystr(txtCanal) || mu.emptystr(txtSubCanal)) {
                toast("Los campos canal y subcanal son obligatorios");
            } else {
                //db.execSQL("DELETE FROM D_CLIENTE_MODIF");
                if (validaCliente() == 0) {

                    ins.init(tabla);

                    ins.add("COREL", corel);
                    ins.add("CODIGO", codigo);
                    ins.add("NOMBRE", nombre);
                    ins.add("CANAL", idCanal);
                    ins.add("NIT", ruc);
                    ins.add("SUBCANAL", idSubcanal);
                    ins.add("BLOQUEADO", "0");
                    ins.add("TIPONEG", "");
                    ins.add("TIPO", "");
                    ins.add("SUBTIPO", "");
                    ins.add("NIVELPRECIO", "0");
                    ins.add("MEDIAPAGO", "");
                    ins.add("LIMITECREDITO", "0");
                    ins.add("DIACREDITO", "0");
                    ins.add("DESCUENTO", "");
                    ins.add("BONIFICACION", "");
                    ins.add("IMPSPEC", "0");
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
                    ins.add("COORX", "0");
                    ins.add("COORY", "0");
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
                    ins.add("PERCEPCION", "0");
                    ins.add("TIPO_CONTRIBUYENTE", "");
                    ins.add("ID_DESPACHO", "0");
                    ins.add("ID_FACTURACION", "0");
                    ins.add("MODIF_PRECIO", "");
                    ins.add("INGRESA_CANASTAS", "0");
                    ins.add("PRIORIZACION", "");
                    ins.add("STATCOM", "N");
                    ins.add("FECHA_SISTEMA", f);

                    db.execSQL(ins.sql());

                    toast("Cliente actualizado correctamente");
                    UpdClienteP();
                    limpiar();
                } else {

                    upd.init(tabla);
                    upd.add("CANAL", idCanal);
                    upd.add("SUBCANAL", idSubcanal);
                    upd.add("STATCOM", "N");
                    upd.add("FECHA_SISTEMA", f);
                    upd.Where("CODIGO='"+codigo+"'");

                    db.execSQL(upd.SQL());

                    toast("Cliente actualizado correctamente");
                    UpdClienteP();
                    limpiar();

                }
            }

            Regresar();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
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

    private  void Regresar() {
        limpiar();
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

    private void setData() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    @Override
    public void onBackPressed() {
        try{
            Regresar();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }
}