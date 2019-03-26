package com.dts.roadp;

import android.app.Application;

import com.dts.roadp.clsClasses.clsBonifItem;
import com.dts.roadp.clsClasses.clsDemoDlg;
import com.epson.eposdevice.Device;
import com.epson.eposdevice.printer.Printer;

import java.util.ArrayList;

public class appGlobals extends Application {

	public String ruta,rutanom,sucur,rutatipo,rutatipog,vend,vendnom,gstr,gstr2,prod,um,umpres,umstock,cliente,clitipo;
	public String ubas,emp, empnom,imgpath,umpeso,lotedf,impresora, tipoImpresora, codSupervisor, ayudante, ayudanteID, vehiculo, vehiculoID;
	public String wsURL,bonprodid,pprodname,contrib,ateninistr,tcorel;
	public int itemid,gint,tipo,nivel,prodtipo,prw,boldep,vnivel,vnivprec,media;
	public int autocom,pagomodo,filtrocli;
	public long nuevaFecha,atentini;
	public double dval,dpeso,pagoval,pagolim,bonprodcant,percepcion,costo,credito,umfactor,prectemp;
	public boolean CellCom;
	public String ref1,ref2,ref3,fnombre,fnit,fdir,escaneo,corel_d_mov,barra;
	public int tiponcredito;
    public boolean vcredito,vcheque,vchequepost;
	public boolean closeCliDet,closeVenta,promapl,pagado,pagocobro,sinimp,rutapos,contlic,devol,modoadmin;
	public boolean usarpeso,banderafindia,depparc,incNoLectura,cobroPendiente,findiaactivo,banderaCobro;
	public int mpago;

	//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true por defecto estará en false
	public boolean debug=true;

	//Devolución Cliente
	public String devtipo,devrazon,dvumventa,dvumstock,dvumpeso,dvlote;
	public double dvfactor,dvpeso,dvprec,dvpreclista,dvtotal;
	public int dvbrowse=0,tienelote,facturaVen;
    public boolean dvporpeso;
    public double dvdispventa;
    public String dvcorreld,dvcorrelnc,dvestado,dvactuald,dvactualnc;

	// Parametros Extra
	public String peModal,peMon,peFormatoFactura,CodDev;
	public Boolean peStockItf,peSolicInv,peAceptarCarga,peBotInv,peBotPrec;
	public Boolean peBotStock,peVehAyud,peEnvioParcial,peOrdPorNombre,peImprFactCorrecta;
	public int peDec,peDecCant,peDecImp;
	
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
	public String deviceId;

	// Epson
	
	public Device mDevice=null;
	public Printer mPrinter=null;
	public boolean mPrinterSet=false;
	public String mPrinterIP;

	//Cobros
	public int escbro=0;

}
