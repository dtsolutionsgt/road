package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edocsdk.Fimador;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Objects;

import Entidades.Detalle;
import Entidades.Receptor;
import Entidades.RespuestaEdoc;
import Entidades.gFormaPago;
import Entidades.gPagPlazo;
import Entidades.gRucRec;
import Entidades.gUbiRec;
import Entidades.rFE;
import Facturacion.CatalogoFactura;

public class DevolCli extends PBase {

	private ListView listView;
	private TextView lblCantProds,lblCantUnd,lblCantKgs,lblCantTotal;

	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;
	private AppMethods app;
	private CatalogoFactura Catalogo;

	private double cntprd=0.0,cntunis=0.0,cntkgs=0.0,cntotl=0.0;

	private printer prn;
	private clsDocDevolucion fdevol;
	private Runnable printcallback,printclose,printexit,printvoid;

	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos,impres;

	private rFE NotaCredito = new rFE();
	private clsClasses.clsMunicipio Municipio = clsCls.new clsMunicipio();
	private clsClasses.clsDepartamento Departamento = clsCls.new clsDepartamento();
	private clsClasses.clsCiudad Ciudad = clsCls.new clsCiudad();
	private clsClasses.clsSucursal Sucursal = clsCls.new clsSucursal();
	private clsClasses.clsCliente Cliente = clsCls.new clsCliente();
	private clsClasses.clsProducto Producto = clsCls.new clsProducto();
	private String urltoken = "";
	private String usuario = "";
	private String clave =  "";
	private String urlDoc =  "";
	private String QR =  "";
	private String urlcontingencia="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devol_cli);

		super.InitBase();
		addlog("DevolCli",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		lblCantProds = (TextView) findViewById(R.id.lblCantProds);
		lblCantUnd = (TextView) findViewById(R.id.lblCantUnd);
		lblCantKgs = (TextView) findViewById(R.id.lblCantKgs);
		lblCantTotal = (TextView) findViewById(R.id.lblCantTotal);

		emp=gl.emp;
		estado=gl.devtipo;
		cliid=gl.cliente;

		//#CKFK20230118 Agregamos esta información quemada como variables
		urltoken = gl.url_token;
		usuario = gl.usuario_api;
		clave =  gl.clave_api;
		urlDoc =  gl.url_doc;
		QR =  gl.qr_api;
		urlcontingencia= gl.url_b2c_hh;

		app = new AppMethods(this, gl, Con, db);

		setHandlers();

		browse=0;
		fecha=du.getActDateTime();
		gl.devrazon="0"; gl.devcord ="";gl.devtotal=0;

		clearData();

		printcallback= () -> askPrint();

		printexit= () -> {
			limpiavariables_devol();
			DevolCli.super.finish();
		};

		printvoid= () -> {
		};

		printclose= () -> {
			limpiavariables_devol();
			DevolCli.super.finish();
		};

		prn=new printer(this,printexit,gl.validimp);
		Catalogo = new CatalogoFactura(this, Con, db);

		fdevol=new clsDocDevolucion(this,prn.prw,gl.peMon,gl.peDecImp, "printnc.txt");
		fdevol.deviceid =gl.numSerie;
	}

	// Events

	public void showProd(View view) {
		try {
            sql = "SELECT VENTA FROM P_RUTA";
            Cursor DT = Con.OpenDT(sql);
            DT.moveToFirst();
            gl.rutatipo = DT.getString(0);

			browse=1;
			itempos=-1;

			if (gl.rutatipo.equalsIgnoreCase("P") ) {
			    //gl.prodtipo=0;
                gl.prodtipo=4;
            } else if (gl.rutatipo.equalsIgnoreCase("D")){
			    gl.prodtipo=4;
            } else if (gl.rutatipo.equalsIgnoreCase("V")){
                gl.prodtipo=4;
            }

			Intent intent = new Intent(this,Producto.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("showProd:  " + e.getMessage());
		}

	}

	public void finishDevol(View view){
		try{
			if (!hasProducts()) {
				mu.msgbox("¡No puede continuar, no ha agregado ninguno producto!");return;
			}

			msgAskComplete("Aplicar la devolución");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Main

	private void setHandlers(){

		try{
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

					adapter.setSelectedIndex(position);

					prodid = vItem.Cod;

					updCant(vItem.id);

				}
			});

			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

					adapter.setSelectedIndex(position);

					prodid = vItem.Cod;

					msgAskDel("Borrar producto");

					return true;
				}
			});

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("setHandlers: " + e.getMessage());
		}

	}

	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;
		String s;

		cntprd = 0;
		cntunis = 0;
		cntkgs = 0;
		cntotl = 0;

		items.clear();

		try {

			sql="SELECT T_CxCD.CODIGO, T_CxCD.CANT, P_CODDEV.DESCRIPCION, P_PRODUCTO.DESCCORTA, T_CxCD.ITEM,T_CxCD.PESO,T_CxCD.TOTAL "+
					" FROM T_CxCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_CxCD.CODIGO "+
					" LEFT JOIN P_CODDEV ON (P_CODDEV.CODIGO=T_CxCD.CODDEV AND P_CODDEV.ESTADO='"+estado+"') "+
					" ORDER BY P_PRODUCTO.DESCCORTA";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCFDV();

				vItem.Cod = DT.getString(0);
				vItem.Desc = DT.getString(3);
				vItem.Valor = DT.getString(2);
				s = mu.frmdec(DT.getDouble(1));
				vItem.Fecha = s;
				vItem.id = DT.getInt(4);

				cntprd = cntprd + 1;
				cntunis = cntunis + Double.parseDouble(s);
				cntkgs = mu.round(cntkgs + DT.getDouble(5), gl.peDec);
				cntotl = mu.round(cntotl + DT.getDouble(6), 2);

				items.add(vItem);

				// vItem.Cod = gl.CodDev;
				DT.moveToNext();
			}

			lblCantProds.setText(String.valueOf(cntprd));
			lblCantUnd.setText(String.valueOf(cntunis));
			lblCantKgs.setText(String.valueOf(cntkgs));
			lblCantTotal.setText(mu.frmcur(cntotl));

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
		}

		adapter=new ListAdaptDevCli(this,items);
		listView.setAdapter(adapter);
	}

	private void processItem(){

		try{

			String pid;
			pid=gl.gstr;
			if (mu.emptystr(pid)) {return;}

			prodid=pid;

			setCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setCant(){
		try{
			browse=2;

			itempos=-1;
			((appGlobals) vApp).prod=prodid;
			Intent intent = new Intent(this,DevCliCant.class);
			startActivity(intent);
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void updCant(int item){
		Cursor DT;
		String prid,rz;

		try {
			sql="SELECT CODIGO,CODDEV,CANT FROM T_CxCD WHERE Item="+item;
			DT=Con.OpenDT(sql);

			if(DT.getCount()==0) return;

			DT.moveToFirst();

			prid=DT.getString(0);
			rz=DT.getString(1);


			browse=2;

			itempos=item;
			gl.prod=prid;
			gl.devrazon=rz;
			//((appGlobals) vApp).dval=pcant;

			Intent intent = new Intent(this,DevCliCant.class);
			startActivity(intent);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("updCant: " + e.getMessage());
		}

	}

	private void processCant(){
		double cnt;
		String raz;

		try{
			cnt=gl.dval;
			if (cnt<0) return;

			raz=gl.devrazon;
			cant=cnt;

			addItem(raz);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void addItem(String raz){
		Cursor DT;
		int id;

		try {
			sql="DELETE FROM T_CxCD WHERE item="+itempos;
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		try {
			sql="DELETE FROM T_CxCD WHERE CODIGO='"+prodid+"' AND CODDEV='"+raz+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		try {
			sql="SELECT MAX(Item) FROM T_CxCD";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			id=DT.getInt(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			id=0;
		}

		id+=1;

		try {

			ins.init("T_CxCD");

			ins.add("Item",id);
			ins.add("CODIGO",prodid);
			ins.add("CANT",cant);
			ins.add("CODDEV",raz);
			ins.add("TOTAL",gl.dvtotal);
			ins.add("PRECIO",gl.dvprec);
			ins.add("PRECLISTA",0);
			ins.add("REF","Ninguna");
			ins.add("PESO",gl.dvpeso);
			ins.add("FECHA_CAD",0);
			ins.add("LOTE",gl.dvlote);
			ins.add("UMVENTA",gl.dvumventa);
			ins.add("UMSTOCK",gl.dvumstock);
			ins.add("UMPESO",gl.dvumpeso);
			ins.add("FACTOR",gl.dvfactor);
			ins.add("POR_PESO",(gl.dvporpeso?"S":"N"));
			ins.add("TIENE_LOTE",gl.tienelote);

			db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_CxCD WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		listItems();

	}

	private void saveDevol(){
		Cursor DT;
		String corel,pcod;
		Double pcant;

		gl.dvcorreld = obtienecorrel("D");gl.devcord=gl.dvcorreld;
		gl.dvcorrelnc = obtienecorrel("NC");gl.devcornc=gl.dvcorrelnc;

		fecha=du.getActDateTime();
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());

		cntotl=mu.round(cntotl,2);

		try {

            gl.despdevflag=true;

			if (gl.tiponcredito==1  ){

				db.beginTransaction();

				ins.init("D_CxC");

				ins.add("COREL",gl.dvcorreld);
				ins.add("RUTA",gl.ruta);
				ins.add("CLIENTE",gl.cliente);
				ins.add("FECHA",fecha);
				ins.add("ANULADO","N");
				ins.add("EMPRESA",gl.emp);
				ins.add("TIPO",estado);
				ins.add("REFERENCIA","Ninguna");
				ins.add("IMPRES",0);
				ins.add("STATCOM","N");
				ins.add("VENDEDOR",gl.vend);
				ins.add("TOTAL",cntotl);
				ins.add("SUPERVISOR",gl.codSupervisor);
				ins.add("AYUDANTE",gl.ayudanteID);
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("ESTADO","S");

				db.execSQL(ins.sql());

				ins.init("D_NOTACRED");

				ins.add("COREL",gl.dvcorrelnc);
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",gl.cliente);
				ins.add("TOTAL",cntotl);
				ins.add("FACTURA",gl.dvcorreld);
				ins.add("SERIE","0");
				ins.add("CORELATIVO",gl.dvactualnc);
				ins.add("STATCOM","N");
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("RESOLNC","N");
				ins.add("SERIEFACT",0);
				ins.add("CORELFACT",0);
				ins.add("IMPRES",0);
				ins.add("CERTIFICADA_DGI", 0);
				ins.add("TIPO_DOCUMENTO", "NC");

				db.execSQL(ins.sql());

				Sucursal = Catalogo.getSucursal();
				Cliente = Catalogo.getCliente(gl.cliente);

				int vNroDF;
				String vSerie;

				vNroDF = Integer.valueOf(gl.dvcorrelnc.substring(3,9));
				vSerie = StringUtils.right("000" + gl.dvcorrelnc.substring(0,3), 3);

				NotaCredito.gDGen.iTpEmis = "01";
				NotaCredito.gDGen.iDoc = "06"; //Tipo de documento (04:Nota de Crédito  referente a facturas, 06:Nota de crédito genérica )
				//(05:Nota de debito  referente a facturas, 07:Nota de debito genérica )
				NotaCredito.gDGen.dNroDF = String.valueOf(vNroDF);
				NotaCredito.gDGen.dPtoFacDF = vSerie; //000
				NotaCredito.gDGen.dFechaEm = du.getFechaCompleta()+"-05:00";
				NotaCredito.gDGen.iNatOp = "01";
				NotaCredito.gDGen.iTipoOp = 1;
				NotaCredito.gDGen.iDest = 1;
				NotaCredito.gDGen.iFormCAFE = 1;
				NotaCredito.gDGen.iEntCAFE = 1;
				NotaCredito.gDGen.dEnvFE = 1;
				NotaCredito.gDGen.iProGen = 2;
				NotaCredito.gDGen.iTipoTranVenta = 1;
				NotaCredito.gDGen.iTipoSuc = 2;
				NotaCredito.gDGen.dInfEmFE = gl.ruta + ";" + "0;" + Cliente.codigo + ";" + Sucursal.sitio_web + ";";

				NotaCredito.gDGen.Emisor.dNombEm = Sucursal.nombre;
				NotaCredito.gDGen.Emisor.dTfnEm = Sucursal.telefono;
				NotaCredito.gDGen.Emisor.dSucEm = Sucursal.codigo;
				NotaCredito.gDGen.Emisor.dCorElectEmi = Sucursal.correo;
				NotaCredito.gDGen.Emisor.dCoordEm = "+" + Sucursal.corx + ",-" + Sucursal.cory;
				NotaCredito.gDGen.Emisor.gUbiEm.dCodUbi = Sucursal.codubi;
				NotaCredito.gDGen.Emisor.dDirecEm = Sucursal.direccion;
				NotaCredito.gDGen.Emisor.gRucEmi.dRuc = Sucursal.nit;
				NotaCredito.gDGen.Emisor.gRucEmi.dDV = Sucursal.texto;
				NotaCredito.gDGen.Emisor.gRucEmi.dTipoRuc = Sucursal.tipoRuc;

				if (!Sucursal.codubi.isEmpty() || Sucursal.codubi != null) {
					Ciudad = clsCls.new clsCiudad();

					Ciudad = Catalogo.getCiudad(Sucursal.codubi);

					if (Ciudad!=null) {

						NotaCredito.gDGen.Emisor.gUbiEm.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
						NotaCredito.gDGen.Emisor.gUbiEm.dDistr =  (Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
						NotaCredito.gDGen.Emisor.gUbiEm.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

						if (Ciudad.provincia.isEmpty()) {
							NotaCredito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
						}

					} else {
						msgbox("No se encontraron los datos de la ubicación para este código:" + Cliente.ciudad);
						return;
					}
				}

				//#CKFK20221227 Antes se obtenia el distrito, provincia y correegimiento de esta forma
				/*if (!Sucursal.codMuni.isEmpty() || Sucursal.codMuni != null) {
					Municipio = clsCls.new clsMunicipio();
					Departamento = clsCls.new clsDepartamento();

					Municipio = Catalogo.getMunicipio(Sucursal.codMuni);
					Departamento = Catalogo.getDepartamento(Municipio.depar);

					if (Municipio.nombre.contains("/")) {

						String[] DireccionCompleta = Municipio.nombre.split("/");

						NotaCredito.gDGen.Emisor.gUbiEm.dCorreg = DireccionCompleta[1].trim().toUpperCase();
						NotaCredito.gDGen.Emisor.gUbiEm.dDistr = DireccionCompleta[0].trim().toUpperCase();

						if (!Departamento.nombre.isEmpty()) {
							NotaCredito.gDGen.Emisor.gUbiEm.dProv = Departamento.nombre.toUpperCase();
						} else {
							NotaCredito.gDGen.Emisor.gUbiEm.dProv = "PANAMA";
						}

					} else {
						msgbox("El nombre del corregimiento y distrito está mal formado para el código de municipio:" + Municipio.codigo);
						return;
					}

				}*/

				NotaCredito.gDGen.Receptor = new Receptor();
				NotaCredito.gDGen.Receptor.gRucRec = new gRucRec();
				NotaCredito.gDGen.Receptor.gUbiRec = new gUbiRec();
				NotaCredito.gDGen.Receptor.gRucRec.dTipoRuc = Cliente.tipoContribuyente;
				NotaCredito.gDGen.Receptor.iTipoRec = Cliente.tipoRec;
				NotaCredito.gDGen.Receptor.dCorElectRec = Cliente.email;
				NotaCredito.gDGen.Receptor.dTfnRec = Cliente.telefono;
				NotaCredito.gDGen.Receptor.cPaisRec = Cliente.codPais;
				NotaCredito.gDGen.Receptor.dNombRec = Cliente.nombre;
				NotaCredito.gDGen.Receptor.dDirecRec = (Cliente.direccion==null?"":Cliente.direccion.substring(0,(Cliente.direccion.length()>=100?100:Cliente.direccion.length())));
				NotaCredito.gDGen.Receptor.gUbiRec.dCodUbi = (Cliente.ciudad==null?"":Cliente.ciudad);

				if (Cliente.ciudad != null) {

					if (!Cliente.ciudad.isEmpty() ){

						Ciudad = clsCls.new clsCiudad();

						Ciudad = Catalogo.getCiudad(Cliente.ciudad);

						if (Ciudad!=null) {

							NotaCredito.gDGen.Receptor.gUbiRec.dCorreg = (Ciudad.corregimiento==null?"":Ciudad.corregimiento.toUpperCase().trim());
							NotaCredito.gDGen.Receptor.gUbiRec.dDistr = (Ciudad.distrito==null?"":Ciudad.distrito.toUpperCase().trim());
							NotaCredito.gDGen.Receptor.gUbiRec.dProv = (Ciudad.distrito==null?"":Ciudad.provincia.toUpperCase().trim());

							if (Ciudad.provincia.isEmpty()) {
								NotaCredito.gDGen.Receptor.gUbiRec.dProv = "PANAMA";
							}

						} else {
							if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
								msgbox("La ubicación del cliente está vacía Cliente: " + Cliente.nombre);
								return;
							}
						}
					}else {
						if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
							msgbox("La ubicación del cliente está vacía Cliente: " + Cliente.nombre);
							return;
						}
					}
				}else {
					if (Cliente.tipoRec.equals("01")||Cliente.tipoRec.equals("03")){
						msgbox("La ubicación del cliente está vacía Cliente: " + Cliente.nombre);
						return;
					}
				}

				// #CKFK20221206 Si el iTipoRec 01:Contribuyente, 02:Consumidor final, 03:Gobierno, 04:Extranjero
				/*if (NotaCredito.gDGen.Receptor.iTipoRec.equals("01") || NotaCredito.gDGen.Receptor.iTipoRec.equals("03")) {

					if (Cliente.nit.length()>0) {
						String[] DVRuc = Cliente.nit.split(" ");
						if (DVRuc.length > 1) {
							NotaCredito.gDGen.Receptor.gRucRec.dRuc = DVRuc[0].trim();
							if (DVRuc[1].trim().equals("")){
								NotaCredito.gDGen.Receptor.gRucRec.dDV =  StringUtils.right("00" + DVRuc[3].trim(),2);
							}else{
								NotaCredito.gDGen.Receptor.gRucRec.dDV =  StringUtils.right("00" + DVRuc[2].trim(),2);
							}
						}else{
							msgbox(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
							return;
						}
					}else {
						msgbox("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
						return;
					}
				}else{
					if (Cliente.nit.length()>0) {
						String[] DVRuc = Cliente.nit.split(" ");
						if (DVRuc.length > 1) {
							NotaCredito.gDGen.Receptor.gRucRec.dRuc = DVRuc[0].trim();
							if (DVRuc[1].trim().equals("")){
								NotaCredito.gDGen.Receptor.gRucRec.dDV = StringUtils.right("00" + DVRuc[3].trim(),2);
							}else{
								NotaCredito.gDGen.Receptor.gRucRec.dDV = StringUtils.right("00" + DVRuc[2].trim(),2);
							}
						}else{
							NotaCredito.gDGen.Receptor.gRucRec.dRuc = Cliente.nit;
							NotaCredito.gDGen.Receptor.gRucRec.dDV = "";
						}
					}
				}*/

				clsClasses.clsRUC BeRUC= Catalogo.getRUC(Cliente.nit);

				BeRUC= Catalogo.getRUC(Cliente.nit);
				if (NotaCredito.gDGen.Receptor.iTipoRec.equals("01") || NotaCredito.gDGen.Receptor.iTipoRec.equals("03")) {

					if(!BeRUC.sRUC.trim().equals("")){
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sRUC.trim();
					}else{
						msgbox("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
						return;
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						msgbox(" El RUC asociado al cliente, no tiene dígito verificador y el tipo de Receptor lo requiere.");
						return;
					}

				}else{

					if(!BeRUC.sRUC.trim().equals("")){
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sRUC.trim();
					}else{
						msgbox("El RUC asociado al cliente es vacío y el tipo de Receptor lo requiere.");
						return;
					}

					if (!BeRUC.sDV.trim().equals("")) {
						NotaCredito.gDGen.Receptor.gRucRec.dDV = BeRUC.sDV.trim();
					} else {
						NotaCredito.gDGen.Receptor.gRucRec.dRuc = BeRUC.sRUC;
						NotaCredito.gDGen.Receptor.gRucRec.dDV = "";
					}

				}

				sql="SELECT Item,CODIGO,CANT,CODDEV,TOTAL,PRECIO,PRECLISTA,REF,PESO,LOTE,UMVENTA,UMSTOCK,UMPESO,FACTOR,POR_PESO FROM T_CxCD WHERE CANT>0";
				DT=Con.OpenDT(sql);

				int Correlativo = 1;
				double TotalAcumulado  = 0;
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					Detalle detalle = new Detalle();

					pcod=DT.getString(1);
					pcant=DT.getDouble(2);

					ins.init("D_CxCD");

					ins.add("COREL",gl.dvcorreld);
					ins.add("ITEM",DT.getInt(0));
					ins.add("CODIGO",DT.getString(1));
					ins.add("CANT",DT.getDouble(2));
					ins.add("CODDEV",DT.getString(3));
					ins.add("ESTADO",estado);
					ins.add("TOTAL",DT.getDouble(4));
					ins.add("PRECIO",DT.getDouble(5));
					ins.add("PRECLISTA",DT.getDouble(6));
					ins.add("REF",DT.getString(7));
					ins.add("PESO",DT.getDouble(8));
					ins.add("FECHA_CAD",0);
					ins.add("LOTE",DT.getString(9));
					ins.add("UMVENTA",DT.getString(10));
					ins.add("UMSTOCK",DT.getString(11));
					ins.add("UMPESO",DT.getString(12));
					ins.add("FACTOR",DT.getDouble(13));
					db.execSQL(ins.sql());

					ins.init("D_NOTACREDD");

					ins.add("COREL",gl.dvcorrelnc);
					ins.add("PRODUCTO",DT.getString(1));
					ins.add("PRECIO_ORIG",DT.getDouble(5));
					ins.add("PRECIO_ACT",0);
					ins.add("CANT",DT.getDouble(2));
					ins.add("PESO",DT.getDouble(8));
					ins.add("POR_PESO", DT.getString(14));
					ins.add("UMVENTA",DT.getString(10));
					ins.add("UMSTOCK",DT.getString(11));
					ins.add("UMPESO",DT.getString(12));
					ins.add("FACTOR",DT.getDouble(13));
					ins.add("TIPO_DOCUMENTO", "NC");
					db.execSQL(ins.sql());

					Producto = clsCls.new clsProducto();
					Producto = Catalogo.getProducto(DT.getString(1));

					Double ntPeso = DT.getDouble(8);
					Double ntFactor = DT.getDouble(13);

					boolean porpeso = app.ventaPeso(Producto.codigo);

					if (!gl.peModal.equalsIgnoreCase("TOL")) {

						try {
							sql="INSERT INTO P_STOCK VALUES ('"+pcod+"',0,0,0)";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						}

						if (estado.equalsIgnoreCase("M")) {
							sql="UPDATE P_STOCK SET CANTM=CANTM+"+pcant+" WHERE CODIGO='"+pcod+"'";
						} else {
							sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"'";
						}
						db.execSQL(sql);

					}

					detalle.dSecItem = Correlativo;
					detalle.dDescProd = Producto.nombre;
					detalle.dCodProd = Producto.codigo;

					if (!Producto.um.isEmpty()) {
						String CodDGI;

						if (porpeso) {
							CodDGI = Catalogo.getUMDGI(gl.umpeso);
						} else {
							CodDGI = Catalogo.getUMDGI(Producto.um);
						}

						if (!CodDGI.isEmpty()) {
							detalle.cUnidad = CodDGI.toLowerCase();
						} else {
							detalle.cUnidad = Producto.um.toLowerCase();
						}
					}

					if (porpeso) {
						detalle.dCantCodInt = String.valueOf(ntPeso);
					} else {
						if (app.esRosty(Producto.codigo)) {
							detalle.dCantCodInt = String.valueOf(mu.round2(pcant * ntFactor));
						} else {
							detalle.dCantCodInt = String.valueOf(pcant);
						}
					}

					//String TotalItem = String.valueOf(mu.round2(Double.valueOf(detalle.dCantCodInt) * DT.getDouble(5)));
					String TotalItem = String.valueOf(DT.getDouble(4));

					if (Producto.subBodega.length() > 1) {
						detalle.dCodCPBSabr = Producto.subBodega.substring(0, 2);
						detalle.dCodCPBScmp = Producto.subBodega;
					}

					detalle.gPrecios.dPrUnit = String.valueOf(DT.getDouble(5));
					detalle.gPrecios.dPrUnitDesc = "0.000000";
					detalle.gPrecios.dPrItem = TotalItem;
					detalle.gPrecios.dValTotItem = TotalItem;
					detalle.gITBMSItem.dTasaITBMS = "00";
					detalle.gITBMSItem.dValITBMS = "0.00";

					NotaCredito.Detalles.add(detalle);

					Correlativo++;
					TotalAcumulado += mu.round2(Double.valueOf(TotalItem));
					DT.moveToNext();
				}

				String TotalNT = String.valueOf(mu.round2(TotalAcumulado));

				NotaCredito.gTot.dTotNeto = TotalNT;
				NotaCredito.gTot.dTotITBMS = "0.00";
				NotaCredito.gTot.dTotGravado = "0.00";
				NotaCredito.gTot.dTotDesc = "0.00";
				NotaCredito.gTot.dVTot = TotalNT;
				NotaCredito.gTot.dTotRec = TotalNT;
				NotaCredito.gTot.dNroItems = String.valueOf(NotaCredito.Detalles.size());
				NotaCredito.gTot.dVTotItems = TotalNT;

				gFormaPago PagosNt = new gFormaPago();

				if (Cliente.mediapago == 4) {
					PagosNt.iFormaPago = "01";
					NotaCredito.gTot.iPzPag = "2";

					NotaCredito.gTot.gPagPlazo = new ArrayList();
					gPagPlazo PagoPlazo = new gPagPlazo();
					PagoPlazo.dSecItem = "1";
					PagoPlazo.dFecItPlazo = Catalogo.FechaCredito(Cliente.diascredito);
					PagoPlazo.dValItPlazo = TotalNT;
					PagoPlazo.dInfPagPlazo = null;

					NotaCredito.gTot.gPagPlazo.add(PagoPlazo);
				} else {
					PagosNt.iFormaPago = "02";
					NotaCredito.gTot.iPzPag = "1";
				}

				PagosNt.dVlrCuota = TotalNT;
				NotaCredito.gTot.gFormaPago.add(PagosNt);

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactuald+" WHERE RUTA='"+gl.ruta+"' AND TIPO='D'";
				db.execSQL(sql);

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactualnc+" WHERE RUTA='"+gl.ruta+"' AND TIPO='NC'";
				db.execSQL(sql);

				db.setTransactionSuccessful();

				db.endTransaction();

				clsClasses.clsControlFEL ControlNotaCredito = clsCls.new clsControlFEL();
				RespuestaEdoc RespuestaEdoc =  new RespuestaEdoc();
				Fimador Firmador = new Fimador(this);

				if (ConexionValida()) {
					//#AT20230309 Intenta certificar 3 veces
					try {
						RespuestaEdoc = Firmador.EmisionDocumentoBTB(NotaCredito, urltoken, usuario, clave, urlDoc, gl.ambiente);

						if (RespuestaEdoc.Cufe == null) {
							for (int i = 0; i < 2; i++) {
								if (RespuestaEdoc.Cufe == null && !RespuestaEdoc.Estado.equals("15")) {
									RespuestaEdoc = Firmador.EmisionDocumentoBTB(NotaCredito, urltoken, usuario, clave, urlDoc, gl.ambiente);

									if (RespuestaEdoc.Cufe != null) {
										break;
									}
								} else {
									break;
								}

							}
						}
					} catch (Exception e) {
						addlog(Objects.requireNonNull(new Object() { }.getClass().getEnclosingMethod()).getName(),e.getMessage(),sql);
					}
				} else {
					RespuestaEdoc = Firmador.EmisionDocumentoBTC(NotaCredito,gl.url_b2c_hh,"/data/data/com.dts.roadp/"+ gl.archivo_p12,gl.qr_clave,QR,gl.ambiente);
				}

				if (RespuestaEdoc != null) {
					if	(RespuestaEdoc.Cufe != null) {
						int EstadoNT = 0;

						if (!RespuestaEdoc.Estado.isEmpty() || RespuestaEdoc.Estado != null) {

							ControlNotaCredito.Cufe = RespuestaEdoc.Cufe;
							ControlNotaCredito.TipoDoc = NotaCredito.gDGen.iDoc;
							ControlNotaCredito.NumDoc = NotaCredito.gDGen.dNroDF;
							ControlNotaCredito.Sucursal = gl.sucur;
							ControlNotaCredito.Caja = NotaCredito.gDGen.dPtoFacDF;

							if (RespuestaEdoc.Estado.equals("21")) {
								ControlNotaCredito.Estado = "01";
							} else {
								ControlNotaCredito.Estado = RespuestaEdoc.Estado;
							}

							ControlNotaCredito.Mensaje = RespuestaEdoc.MensajeRespuesta;
							ControlNotaCredito.ValorXml = RespuestaEdoc.XML != null ? Catalogo.ReplaceXML(RespuestaEdoc.XML) : "";

							String[] FechaEnv = NotaCredito.gDGen.dFechaEm.split("-05:00", 0);
							ControlNotaCredito.FechaEnvio = FechaEnv[0];
							ControlNotaCredito.TipFac = NotaCredito.gDGen.iDoc;
							ControlNotaCredito.FechaAgr = String.valueOf(du.getFechaCompleta());
							ControlNotaCredito.QR = RespuestaEdoc.UrlCodeQR;
							ControlNotaCredito.Corel = gl.devcornc;
							ControlNotaCredito.Ruta = gl.ruta;
							ControlNotaCredito.Vendedor = gl.vend;
							ControlNotaCredito.Correlativo = String.valueOf(NotaCredito.gDGen.dNroDF);
							ControlNotaCredito.Fecha_Autorizacion = RespuestaEdoc.FechaAutorizacion;
							ControlNotaCredito.Numero_Autorizacion = RespuestaEdoc.NumAutorizacion;

							if (RespuestaEdoc.Estado.equals("2")) {
								EstadoNT = 1;
								toastlong("NOTA DE CREDITO CERTIFICADA CON EXITO -- " + " ESTADO: " + RespuestaEdoc.Estado + " - " + RespuestaEdoc.MensajeRespuesta);
							} else {
								toastlong("NO SE LOGRÓ CERTIFICAR LA NOTA DE CREDITO -- " + " ESTADO: " + RespuestaEdoc.Estado + " - " + RespuestaEdoc.MensajeRespuesta);
							}
						}

						try{
							Catalogo.opendb();
						}catch (Exception e){
							Catalogo = new CatalogoFactura(this, Con, db);
						}

						Catalogo.UpdateEstadoNotaCredito(RespuestaEdoc.Cufe,"", EstadoNT);
						Catalogo.InsertarFELControl(ControlNotaCredito);
					}
				}

				Toast.makeText(this,"Devolución guardada", Toast.LENGTH_SHORT).show();

				sql="DELETE FROM T_CxCD";
				db.execSQL(sql);

				gl.closeCliDet = true;
    			gl.closeVenta = true;

    			//#CKFK 20210922 Puse esto en comentario porque no aplica realizar
				// esta asignación cuando no tiene una factura asociada
                //gl.devtotal = cntotl;

				createDoc();

				//#CKFK 20210922 Inicializa la variable del total de la devolución
				gl.devtotal=0;

				//msgAskSave("Aplicar pago y crear un recibo");

			} else {
				try {
					Intent i = new Intent(this, CliDet.class);
					gl.dvbrowse=3;
					gl.dvdispventa = cntotl;gl.devtotal = cntotl;
					gl.dvestado = estado;
					startActivity(i);
					finish();
				} catch (Exception e){
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				}
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
            gl.dvcorreld="";
			mu.msgbox(e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void createDoc(){
		String vModo="";

        //if (gl.rutatipo.equalsIgnoreCase("D")) gl.closeCliDet=false;
        if (gl.rutatipo.equalsIgnoreCase("D")) gl.closeCliDet=true;

		try{

			vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");

			if (prn.isEnabled()) {

				fdevol.buildPrint(gl.dvcorrelnc,0,vModo);
				//#CKFK 20190401 09:47AM Agregué la funcionalidad de enviar el nombre del archivo a imprimir
				prn.printask(printcallback, "printnc.txt");

			} else if(!prn.isEnabled()){

				fdevol.buildPrint(gl.dvcorrelnc,0,vModo);
				limpiavariables_devol();
				DevolCli.super.finish();

			}

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("createDoc: " + e.getMessage());
		}

	}

	private void delItem(){

		try {

			db.execSQL("DELETE FROM T_CxCD WHERE CODIGO='"+prodid+"'");

			listItems();

			adapter=new ListAdaptDevCli(this,items);
			listView.setAdapter(adapter);

			//#CKFK_20190328 Mostrar Totales
			lblCantProds.setText(String.valueOf(cntprd));
			lblCantUnd.setText(String.valueOf(cntunis));
			lblCantKgs.setText(String.valueOf(cntkgs));
			lblCantTotal.setText(mu.frmcur(cntotl));

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
	}

	// Aux
	public boolean ConexionValida() {
		boolean valida = false;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnected()) {
				valida = true;
			}

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
		}

		return valida;
	}

	private String obtienecorrel(String tipo){
		String correl="";
		Cursor DT;

		try{
			sql="SELECT SERIE,ACTUAL+1,FINAL,INICIAL FROM P_CORREL_OTROS WHERE RUTA='"+gl.ruta+"' AND TIPO='"+tipo+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				correl=DT.getString(0) + StringUtils.right("000000" + Integer.toString(DT.getInt(1)), 6);

				if (tipo.equals("D")){
					gl.dvactuald = String.valueOf(DT.getInt(1));
				}else{
					gl.dvactualnc = String.valueOf(DT.getInt(1));
				}
			} else {
				if (gl.peModal.equalsIgnoreCase("TOL")){
					mu.msgbox("No está definido correlativo, no se puede continuar con la devolución.\n");
				}else{
					correl=gl.ruta+"_"+mu.getCorelBase();
					if (tipo.equals("D")){
						gl.dvactuald = String.valueOf(1);
					}else{
						gl.dvactualnc = String.valueOf(1);
					}
				}
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return  correl;

	}

	private void clearData(){
		try {
			sql="DELETE FROM T_CxCD";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setCancelable(false);
			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
					closekeyb();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					closekeyb();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskComplete(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					saveDevol();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.setCancelable(false);
			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskDel(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿ " + msg  + " ?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					delItem();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { }
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void askPrint() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					impres++;toast("Impres "+impres);

					try {
						sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+gl.dvcorrelnc+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					if (impres>1) {

						try {
							sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+gl.dvcorrelnc+"'";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						}

						gl.brw=0;

					} else {

						String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
						fdevol.buildPrint(gl.dvcorrelnc,1,vModo);
						impres=0;
						prn.printask(printclose, "printnc.txt");

					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					prn.printask(printcallback, "printnc.txt");

				}
			});

			dialog.show();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private boolean hasProducts(){
		Cursor DT;

		try {
			sql="SELECT CODIGO FROM T_CxCD";
			DT=Con.OpenDT(sql);

			return DT.getCount()>0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
	}

	private void doExit(){
		try{

			limpiavariables_devol();

			sql="DELETE FROM T_CxCD";
			db.execSQL(sql);

			DevolCli.super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void limpiavariables_devol(){

		gl.tiponcredito=0;
		gl.devtipo ="";
		gl.devrazon = "";
		gl.dvumventa = "";
		gl.dvumstock ="";
		gl.dvumpeso = "";
		gl.dvlote="";
		gl.dvfactor=0.0;
		gl.dvpeso=0.0;
		gl.dvprec=0.0;
		gl.dvpreclista=0.0;
		gl.dvtotal=0.0;
		gl.dvbrowse=0;
		gl.tienelote=0;
		gl.dvporpeso=false;
		gl.dvdispventa=0.0;
		gl.dvcorreld="";
		gl.dvcorrelnc="";
		gl.dvestado="";
		gl.dvactuald="";
		gl.dvactualnc="";
	}

	// Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			if (gl.closeVenta==true){
				limpiavariables_devol();
				super.finish();
			}

			if (browse==1) {
				browse=0;
				processItem();return;
			}

			if (browse==2) {
				browse=0;
				processCant();return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin terminar devolución");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
}
