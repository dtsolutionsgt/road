package com.dts.roadp;

import android.app.Application;

import com.dts.roadp.clsClasses.clsBonifItem;
import com.dts.roadp.clsClasses.clsDemoDlg;
import com.epson.eposdevice.Device;
import com.epson.eposdevice.printer.Printer;

import java.util.ArrayList;

public class appGlobals extends Application {

	public String ruta,rutanom,rutasup,sucur,rutatipo,rutatipog,vend,vendnom,gstr,gstr2,prod,um,umpres,umpresp,umstock,cliente,clitipo;
	public String ubas,emp, empnom,imgpath,umpeso,lotedf,impresora, tipoImpresora, codSupervisor, ayudante, ayudanteID, vehiculo, vehiculoID;
	public String wsURL,bonprodid,bonbarid,bonbarprod,pprodname,contrib,ateninistr,tcorel,URLtemp,URLLocal,URLRemoto,
			iddespacho, coddespacho,pedCorel, rutaPedido;
	public int itemid,gint,tipo,nivel,prodtipo,prw,boldep,vnivel,vnivprec,media;
	public int autocom,pagomodo,filtrocli,prdlgmode;
	public long nuevaFecha,atentini;
	public double dval,dpeso,pagoval,pagolim,bonprodcant,percepcion,costo,credito,umfactor,prectemp;
	public double precprev,cexist,cstand,precuni,pesouni;
	public boolean CellCom,closeDevBod;
	public String ref1,ref2,ref3,fnombre,fnit,fdir,escaneo,corel_d_mov,barra, parNumVer, parFechaVer;
	public String parTipoVer,umprev,modpedid;
	public int tiponcredito,validarCred,gpsdist;
    public boolean vcredito,vcheque,vchequepost,validimp,tolsuper=false,tolpedsend,tolprodcrit;
	public boolean closeCliDet,closeVenta,promapl,pagado,pagocobro,sinimp,rutapos,devol,modoadmin;
	public boolean usarpeso,banderafindia,depparc,incNoLectura,cobroPendiente,findiaactivo,banderaCobro;
	public boolean permitir_cantidad_mayor, permitir_producto_nuevo,pedidomod,listapedidos;
	public boolean validar_posicion_georef;
	public int mpago;
	public String prodCanasta, corelFac, devcord;
	public String IdCanal, IdSubcanal, EditarClienteCanal, EditarClienteSubcanal;
	public boolean ingresaCanastas, enviaMov, enviaPedidosParcial, enviaClientes;
	public String EditarClienteCodigo, EditarClienteNombre, EditarClienteRuc;
	public String CliNombre,CliNit, CliDireccion, CliProvincia, CliDistrito, CliCiudad, CliTel, CliEmail, CliContacto, CliCsPollo, CliCsEmbutido,CliCsHuevo,
			CliCsRes, CliCsCerdo, CliCsCongelados, CliCsSalsas, IdDep, IdMun, CliCodVen="", corelCliente="", CliNomVen;

    public ArrayList<clsClasses.clsPedItem> peditems= new ArrayList<clsClasses.clsPedItem>();

	//#CKFK 20190619 Agregué estas dos variables para los valores de clientes nuevo
	public String cuentaCliNuevo, codCliNuevo;

	//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true por defecto estará en false
	public boolean debug=true;

	public boolean gpsCliente = false;
	public boolean repPrefactura = false;

	//Devolución Cliente
	public String devtipo,devrazon,dvumventa,dvumstock,dvumpeso,dvlote;
	public double dvfactor,dvpeso,dvprec,dvpreclista,dvtotal;
	public int dvbrowse=0,tienelote,facturaVen,brw=0;
    public boolean dvporpeso,devfindia,devprncierre,gpspass,despdevflag;
    public double dvdispventa,devtotal;
    public String dvcorreld,dvcorrelnc,dvestado,dvactuald,dvactualnc,devcornc, dvcorelnd, dvactualnd;

	// Parametros Extra
	public String peModal,peMon,peFormatoFactura,CodDev;
	public Boolean peStockItf,peSolicInv,peAceptarCarga,peBotInv,peBotPrec,endPrint;
	public Boolean peBotStock,peVehAyud,peEnvioParcial,peOrdPorNombre;
	public boolean peImprFactCorrecta, pTransBarra;
	public int peDec,peDecCant,peDecImp,peLimiteGPS,peMargenGPS,peVentaGps;
	public Boolean peEditarNombre, peEditarNit, peEditarCanal, peEditarSubcanal, peEditarDir, peEditarContacto, peEditarEmail, peEditarTel, peEditarDistrito;
	
	// Descuentos
	
	public String promprod;
	public double promcant,promdesc,prommdesc,descglob,descgtotal;
	public int prommodo;
	
	// Bonificaciones
	
	public ArrayList<clsBonifItem> bonus = new ArrayList<clsBonifItem>();
	
	public clsDemoDlg clsDemo;
	
	// GPS
	public double gpspx,gpspy,gpscpx,gpscpy,gpscdist;
	
	//Id de Dispositivo Móvil
	public String deviceId,devicename,numSerie;

	// Epson
	
	public Device mDevice=null;
	public Printer mPrinter=null;
	public boolean mPrinterSet=false;
	public String mPrinterIP;

	//Cobros
	public int escbro=0;

	//Desglose de efectivo
	public double totDep;

	//Comunicación con WS
	public int isOnWifi = 0;

	//Parámetros configuración barra
	public int pLongitudBarra;
	public String pPrefijoBarra;
	public int pCantImpresion;
}
