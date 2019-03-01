package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsBonifItem;
import com.dts.roadp.clsClasses.clsDemoDlg;
import com.epson.eposdevice.Device;
import com.epson.eposdevice.printer.Printer;

import android.app.Application;

public class appGlobals extends Application {

	//#HS_20181122_1507 Se agregaron la variables impresora, tipoImpresora, supervisor, ayudante, ayudanteID, vehiculo, vehiculoID.
	public String ruta,sucur,rutatipo,rutatipog,vend,vendnom,gstr,gstr2,prod,um,umpres,umstock,cliente,ubas,emp,imgpath,umpeso,impresora, tipoImpresora, codSupervisor, ayudante, ayudanteID, vehiculo, vehiculoID;
	public String devtipo,devrazon,wsURL,bonprodid,pprodname,contrib,ateninistr,tcorel;
	public int itemid,gint,tipo,nivel,prodtipo,prw,boldep,atentini,vnivel,vnivprec,media;
	public int autocom,nuevaFecha,pagomodo,filtrocli;
	public double dval,dpeso,pagoval,pagolim,bonprodcant,percepcion,costo,credito,umfactor,prectemp;
	public boolean CellCom;
	public String ref1,ref2,ref3,fnombre,fnit,fdir,escaneo,corel_d_mov;

	//CM_20190225: Variable para validar crédito.
    public  boolean vcredito;
	public boolean vcheque,vchequepost;

	public boolean closeCliDet,closeVenta,promapl,pagado,pagocobro,sinimp,rutapos,contlic,devol,modoadmin;
	public boolean usarpeso,banderafindia,depparc,incNoLectura,cobroPendiente,findiaactivo;
	public int mpago;

	// Parametros Extra
	public String peModal,peMon,peFormatoFactura;
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

}
