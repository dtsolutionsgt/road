package Facturacion;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.dts.roadp.AppMethods;
import com.dts.roadp.BaseDatos;
import com.dts.roadp.DateUtils;
import com.dts.roadp.PBase;
import com.dts.roadp.appGlobals;
import com.dts.roadp.clsClasses;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class CatalogoFactura extends PBase {

    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private String sql;
    private Context cont;
    private appGlobals gl;
    private AppMethods app;

    public CatalogoFactura(Context contexto) {
        cont = contexto;
        gl = ((appGlobals) (((Activity) cont).getApplication()));

        try {

            Con = new BaseDatos(cont);
            opendb();
            ins=Con.Ins;upd=Con.Upd;

            app = new AppMethods(cont,gl,Con,db);
            du=new DateUtils();

        } catch (Exception e) {
            Toast.makeText(cont, "CatalogoFactura : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void InsertarFELControl(clsClasses.clsControlFEL ItemFEL) {
        Cursor dt;
        try {

            sql = "SELECT MAX(IdTablaControl) FROM D_FACTURA_CONTROL_CONTINGENCIA";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();
            ItemFEL.Id = dt.getInt(0) + 1;

            ins.init("D_FACTURA_CONTROL_CONTINGENCIA");

            ins.add("IdTablaControl", ItemFEL.Id);
            ins.add("Cufe", ItemFEL.Cufe);
            ins.add("TipoDocumento", ItemFEL.TipoDoc);
            ins.add("NumeroDocumento", ItemFEL.NumDoc);
            ins.add("Sucursal", ItemFEL.Sucursal);
            ins.add("Caja", ItemFEL.Caja);
            ins.add("Estado", ItemFEL.Estado);
            ins.add("Mensaje", ItemFEL.Mensaje);
            ins.add("Valor_XML", ItemFEL.ValorXml);
            ins.add("FechaEnvio", ItemFEL.FechaEnvio);
            ins.add("TipoFactura", ItemFEL.TipFac);
            ins.add("Fecha_Agr", ItemFEL.FechaAgr);
            ins.add("QR", ItemFEL.QR);
            ins.add("COREL", ItemFEL.Corel);
            ins.add("RUTA", ItemFEL.Ruta);
            ins.add("VENDEDOR", ItemFEL.Vendedor);
            ins.add("HOST", ItemFEL.Host);
            ins.add("CODIGOLIQUIDACION", ItemFEL.CodLiquidacion);
            ins.add("CORELATIVO", ItemFEL.Correlativo);
            ins.add("QRIMAGE", ItemFEL.QRImg);

            db.execSQL(ins.sql());

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    public clsClasses.clsMunicipio getMunicipio(String CodMuni) {
        clsClasses.clsMunicipio Municipio = clsCls.new clsMunicipio();
        Cursor dt;
        try {
            sql= "SELECT * FROM P_MUNI WHERE CODIGO = '"+CodMuni+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Municipio.codigo = dt.getString(0);
                Municipio.depar = dt.getString(1);
                Municipio.nombre = dt.getString(2);
            }

            if(dt!=null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Municipio;
    }

    public clsClasses.clsDepartamento getDepartamento(String CodDep) {
        clsClasses.clsDepartamento Departamento = clsCls.new clsDepartamento();
        Cursor dt;
        try {
            sql= "SELECT * FROM P_DEPAR WHERE CODIGO = '"+CodDep+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Departamento.nombre = dt.getString(2);
            }

            if(dt!=null) dt.close();
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Departamento;
    }

    public clsClasses.clsCliente getCliente(String CodCliente) {
        clsClasses.clsCliente Cliente = clsCls.new clsCliente();
        Cursor dt;
        try {
            sql= "SELECT NOMBRE, TIPO_CONTRIBUYENTE, TIPORECEPTOR, EMAIL, TELEFONO, COD_PAIS, DIRECCION, CIUDAD, NIT, MUNICIPIO, MEDIAPAGO, DIACREDITO " +
                    " FROM P_CLIENTE WHERE CODIGO = '"+CodCliente+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Cliente.nombre = dt.getString(0);
                Cliente.tipoContribuyente = dt.getString(1);
                Cliente.tipoRec = dt.getString(2);
                Cliente.email = dt.getString(3);
                Cliente.telefono = dt.getString(4);
                Cliente.codPais = dt.getString(5);
                Cliente.direccion = dt.getString(6);
                Cliente.ciudad = dt.getString(7);
                Cliente.nit = dt.getString(8);
                Cliente.muni = dt.getString(9);
                Cliente.mediapago = Integer.valueOf(dt.getString(10));
                Cliente.diascredito = Integer.valueOf(dt.getString(11));
            }

            if(dt!=null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName()+" - "+e.getMessage());
        }

        return Cliente;
    }

    public clsClasses.clsSucursal getSucursal() {
        clsClasses.clsSucursal Sucursal = clsCls.new clsSucursal();
        Cursor dt;

        try {
            sql="SELECT * FROM P_SUCURSAL";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Sucursal.codigo = dt.getString(0);
                Sucursal.descripcion = dt.getString(2);
                Sucursal.nombre = dt.getString(3);
                Sucursal.direccion = dt.getString(4);
                Sucursal.telefono = dt.getString(5);
                Sucursal.nit = dt.getString(6);
                Sucursal.texto = dt.getString(7);
                Sucursal.tipoSucursal = dt.getString(8);
                Sucursal.correo = dt.getString(9);
                Sucursal.corx = dt.getString(10);
                Sucursal.cory = dt.getString(11);
                Sucursal.codubi = dt.getString(12);
                Sucursal.tipoRuc = dt.getString(13);
                Sucursal.codMuni = dt.getString(14);

            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Sucursal;
    }

    public clsClasses.clsProducto getProducto(String Codigo) {
        clsClasses.clsProducto Producto = clsCls.new clsProducto();
        Cursor dt;
        try {

            sql="SELECT DESCLARGA, UNIDBAS, SUBBODEGA  FROM P_PRODUCTO " +
                    " WHERE CODIGO = '"+Codigo+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Producto.codigo = Codigo;
                Producto.nombre = dt.getString(0);
                Producto.um = dt.getString(1);
                Producto.subBodega = dt.getString(2);
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Producto;
    }

    public String getUMDGI(String CodigoUM) {
        Cursor dt;
        String Codigo="";
        try {
            sql="SELECT CODIGO_DGI  FROM P_MEDIDA " +
                    " WHERE CODIGO = '"+CodigoUM+"'";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            if (dt.getCount() > 0) {
                Codigo = dt.getString(0);
            }

            if (dt != null) dt.close();
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return Codigo;
    }

    public ArrayList<clsClasses.clsBeNotaCreditoDet> GetDetalleNT(String Corel) {
        ArrayList<clsClasses.clsBeNotaCreditoDet> lista = new ArrayList<>();
        clsClasses.clsBeNotaCreditoDet item;
        Cursor dt;

        try {
            lista. clear();

            sql = "SELECT A.*, B.DESCCORTA " +
                  " FROM D_NOTACREDD A " +
                  " INNER JOIN P_PRODUCTO B ON B.CODIGO = A.PRODUCTO " +
                  " WHERE A.COREL = '"+Corel+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount()>0) {
                dt.moveToFirst();
                while (!dt.isAfterLast()) {
                    item = clsCls.new clsBeNotaCreditoDet();

                    item.corel = dt.getString(0);
                    item.codigoProd = dt.getString(1);
                    item.precio = dt.getString(2);
                    item.cant = dt.getString(4);
                    item.peso = dt.getString(5);
                    item.porpeso = dt.getString(6);
                    item.umpeso = dt.getString(9);
                    item.factor = dt.getString(10);
                    item.producto = dt.getString(11);

                    lista.add(item);
                    dt.moveToNext();
                }
            }

            if (dt != null) dt.close();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return lista;
    }

    public void UpdateEstadoNotaCredito(String Cufe, int NTCertificada) {
        try {
            sql = "UPDATE D_NOTACRED SET CUFE ='" + Cufe + "', CERTIFICADA_DGI=" + NTCertificada + "  WHERE COREL='" + gl.devcornc + "'";
            db.execSQL(sql);
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }
    }

    public void UpdateEstadoFactura(String Cufe, int EstadoFac, String corel) {
        try {
            sql="UPDATE D_FACTURA SET CUFE ='"+Cufe+"', CERTIFICADA_DGI="+EstadoFac+"  WHERE COREL='"+corel+"'";
            db.execSQL(sql);
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }
    }

    public String FechaCredito(int diascredito) {
        String fechaNueva= "";
        try {
            SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fecha.parse(du.getFechaCompleta()));
            calendar.add(Calendar.DAY_OF_YEAR, diascredito);

            fechaNueva = fecha.format(calendar.getTime())+"-05:00";

        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() +" - "+ e.getMessage());
        }

        return  fechaNueva;
    }

    public String ReplaceXML(String texto) {
        String Vocales = "ÁáÉéÍíÓóÚúÑñÜü", VocalesSinAcento = "AaEeIiOoUuNnUu";
        char[] array = texto.toCharArray();
        try {

            for (int index = 0; index < array.length; index++) {
                int pos = Vocales.indexOf(array[index]);
                if (pos > -1) {
                    array[index] = VocalesSinAcento.charAt(pos);
                }
            }

        } catch (Exception e) {
            msgbox(new Object() {} .getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }

        return new String(array);
    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
        } catch (Exception e) {
        }
    }
}
