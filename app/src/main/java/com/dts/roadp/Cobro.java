package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Cobro extends PBase {

	private ListView listView;
	private TextView lblSel,lblPag,lblPend;

	private ArrayList<clsClasses.clsCobro> items= new ArrayList<clsClasses.clsCobro>();
	ArrayList<clsClasses.clsNCPP> docs= new ArrayList<clsClasses.clsNCPP>();
	private ListAdaptCobro adapter;
	private clsClasses.clsCobro selitem;

	private Runnable printcallback,printclose,printValidate, printexit;
	private printer prn;
	private clsDocCobro fdoc;
	private clsDocFactura fdocf;
	private AppMethods app;

	private String cliid,cod,itemid,prodid,sefect,corel,fserie,dtipo="",fechav,msgDesc="";
	private double ttot,tsel,tpag,tpagos,tpend,vefect,plim,cred,pg,sal,ssal,total,monto,pago;
	private boolean peexit;
	private boolean porcentaje = false, validarCred = false;
	private int fflag=1,fcorel,medPago,checkCheck=0, impres=0;
	private String crrf,docfact,anulado;
	private CheckBox cbCheckAll;
	private long fechaven;
	private RadioButton chkFactura,chkContado;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cobro);

		super.InitBase();
		addlog("Cobro",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		lblSel = (TextView) findViewById(R.id.lblSel);
		lblPag = (TextView) findViewById(R.id.lblPag);
		lblPend = (TextView) findViewById(R.id.lblPend);
		cbCheckAll= (CheckBox) findViewById(R.id.cbCheckAll);

		chkFactura = new RadioButton(this,null);
		chkContado = new RadioButton(this,null);

		cliid=gl.cliente;

		app = new AppMethods(this, gl, Con, db);

		setHandlers();

		initSession();

		clearAll();
		listItems();

		cbCheckAll.setChecked(true);

		showTotals();

		gl.pagomodo=0;
		cod=gl.cliente;

		printcallback= new Runnable() {
			public void run() {
				askPrint();
			}
		};

		printclose= new Runnable() {
			public void run() {

				if(gl.banderaCobro){
					Cobro.super.finish();
				}else{
					if(browse==4){
						if (gl.validarCred==1) validaCredito(); //#CKFK 20190503 Printclose
						browse = 0;
					}
				}
				//Cobro.super.finish();

			}
		};

		printexit= new Runnable() {
			public void run() {

				if(gl.banderaCobro){
					Cobro.super.finish();
				}else{
					if(browse==4){
						if (gl.validarCred==1) validaCredito(); //#CKFK 20190503 Printclose
						browse = 0;
					}
				}
				//Cobro.super.finish();
			}
		};

		prn=new printer(this,printexit,gl.validimp);
		fdoc=new clsDocCobro(this,prn.prw,gl.peMon,gl.peDecImp, gl.numSerie, "");
		fdoc.deviceid=gl.numSerie;

		fdocf = new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(cliid),gl.codCliNuevo,gl.peModal);
		fdocf.deviceid=gl.numSerie;
	}

	// Events

	public void paySelect(View view) {

		try{

			docs.clear();

			calcSelected();

			if (tsel==0) {
				mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
			}

			gl.pagomodo=0;
			gl.pagoval=tsel;
			gl.pagolim=tsel;

			if (dtipo.equalsIgnoreCase("R")) gl.pagocobro=false; else gl.pagocobro=true;

			browse=1;
			if(gl.validarCred!=2) gl.validarCred = 1;

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
 			msgbox("Error al seleccionar método de pago: "+e.getMessage());
		}

	}

	//Probando
	public void payCash(View view) {

		try{

			if(gl.validarCred!=2) gl.validarCred = 1;

			docs.clear();

			calcSelected();

			if (tsel==0) {
				mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
			}

			inputEfectivo();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void checkAll(View view) {

		try{
			check();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("checkAll: "+ e.getMessage());
		}

	}

	public void check() {

		if (items.size()==0) return;

		try{

			if(cbCheckAll.isChecked()){

				dtipo=items.get(0).Tipo;

				if (dtipo.equalsIgnoreCase("R")) {
					clearAll();
					items.get(0).flag=1;

				}else{
					for (int i = 0; i <items.size(); i++) {
						dtipo=items.get(0).Tipo;

						if (!dtipo.equalsIgnoreCase("R")) {
							items.get(i).flag=1;
						}
					}
				}

				adapter.refreshItems();

				docs.clear();

				calcSelected();
				showTotals();
			}else{
				for (int i = 0; i <items.size(); i++) {
					items.get(i).flag=2;
				}

				adapter.refreshItems();

				docs.clear();

				calcSelected();
				showTotals();
			}

			/*for (int i = 0; i <items.size(); i++) {
				items.get(i).flag=0;
			}

			adapter.refreshItems();

			calcSelected();
			showTotals();*/

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("check: "+ e.getMessage());
		}

	}

	public void sinRef(View view) {

		try{

			gl.pagomodo=1;
			gl.pagoval=0;
			gl.pagolim=0;
			gl.pagocobro=true;
			browse=1;
			dtipo="";

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("Sin referencia: " + e.getMessage());
		}
	}

	private void setHandlers(){

		try{

			chkFactura.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (chkFactura.isChecked()==true) chkContado.setChecked(false);
				}
			});

			chkContado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					if (chkContado.isChecked()==true) chkFactura.setChecked(false);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int flag;

					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCobro vItem = (clsClasses.clsCobro) lvObj;

					adapter.setSelectedIndex(position);

					dtipo = vItem.Tipo;
					if (dtipo.equalsIgnoreCase("R")) clearAll();

					flag = vItem.flag;
					if (flag == 0) flag = 1;else flag = 0;
					vItem.flag = flag;

					adapter.refreshItems();

					docs.clear();

					calcSelected();
					showTotals();

				}

			});

			cbCheckAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(cbCheckAll.isChecked()) checkAll(buttonView);
				}
			});

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("SetHandlers: " + e.getMessage());
		}

	}


	// Main

	private void listItems(){
		Cursor DT;
		clsClasses.clsCobro vItem;

		items.clear();ttot=0;tpag=0;

		try {
			sql="SELECT DOCUMENTO,TIPODOC,VALORORIG,SALDO,FECHAEMIT,FECHAV " +
					"FROM P_COBRO WHERE CLIENTE='"+cliid+"' ORDER BY FECHAV";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCobro();

				vItem.Factura=DT.getString(0);
				vItem.Tipo=DT.getString(1);
				vItem.Valor=DT.getDouble(2);

				sal=DT.getDouble(3);
				pg=getDocPago(DT.getString(0),DT.getString(1));
				ssal=sal-pg;if (ssal<0) ssal=0;
				if (ssal>0) fflag=1; else fflag=0;

				vItem.Saldo=ssal;
				vItem.Pago=pg;
				vItem.flag=fflag;
				vItem.fini=du.sfecha(DT.getLong(4));
				vItem.ffin=du.sfecha(DT.getLong(5));

				ttot=ttot+DT.getDouble(3);
				tpag+=pg;

				items.add(vItem);

				DT.moveToNext();

			}

			adapter=new ListAdaptCobro(this,items);adapter.cursym=gl.peMon;
			listView.setAdapter(adapter);

			docs.clear();

			calcSelected();
			showTotals();

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox("listItems: "+ e.getMessage());
		}

	}

	public void doExit(View view){
		try{
			validaCredito();
		}catch (Exception e){
			mu.msgbox("doExit: "+e.getMessage());
		}
	}

	public boolean validaCredito(){
		Cursor DT;
		Cursor DTFecha;
		boolean vValida = true;

		try{
			sql = "SELECT MEDIAPAGO,LIMITECREDITO FROM P_CLIENTE WHERE CODIGO ='"+cod+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			medPago=DT.getInt(0);
			cred=DT.getInt(1);
			gl.facturaVen = 0;

			if (medPago == 4) {

				sql = "SELECT DOCUMENTO,TIPODOC,FECHAV,SALDO FROM P_COBRO WHERE CLIENTE ='"+cod+"' ORDER BY FECHAV";
				DTFecha = Con.OpenDT(sql);
				DTFecha.moveToFirst();

				for (int i = 0; i != DTFecha.getCount(); i++) {
					double tot = 0;
					docfact = DTFecha.getString(0);
					fechaven = DTFecha.getLong(2);
					fechav = du.sfecha(fechaven);

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					Date strDate = sdf.parse(fechav);
					if (System.currentTimeMillis() > strDate.getTime()) {
						gl.facturaVen += 1;
					}

					sql="SELECT ANULADO,MONTO,PAGO FROM D_COBROD WHERE DOCUMENTO = "+ docfact;
					DT = Con.OpenDT(sql);

					if(DT.getCount() != 0){

						DT.moveToFirst();
						anulado = DT.getString(0);
						monto = DT.getDouble(1);
						pago = DT.getDouble(2);
						tot = monto - pago;

						if (tot == 0){
							if (anulado.equals("N")) {
								gl.facturaVen -= 1;
							}
						}

					}

					DTFecha.moveToPosition(i);
				}


			}

			if(gl.vcredito){
				if(gl.facturaVen<0) gl.facturaVen=0;
				if(gl.facturaVen > 0) {
					vValida = false;
					msgAskFact();
				}else if(gl.facturaVen==0 & gl.media==4){
					//#AAS - 2019-03-21 - Cuando el credito disponible (gl.credito) del cliente sea  menor que 0 voy a preguntar si quiere hacer la venta al contado
					if (gl.credito<=0) {
						vValida = false;
						msgAskFact();
					}
				}else{
					exit();
				}
			}else{
				exit();
			}

		}catch (Exception e){
			mu.msgbox("validaCredito: "+e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return vValida;
	}

	private void  msgAskFact() {
		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Road");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(chkFactura.getParent()!= null){
				((ViewGroup) chkFactura.getParent()).removeView(chkFactura);
			}

			if(chkContado.getParent()!= null){
				((ViewGroup) chkContado.getParent()).removeView(chkContado);
			}

			if(gl.facturaVen > 0){

				alert.setMessage("Cliente tiene " +gl.facturaVen+ " Facturas vencidas.");

				chkFactura.setText("Pagar Facturas Vencidas");
				if(gl.rutatipo.equalsIgnoreCase("P")){
					chkContado.setVisibility(View.INVISIBLE);
				}else{
					chkContado.setVisibility(View.VISIBLE);
					chkContado.setText("Continuar la venta al contado");
				}

			} else if(gl.credito<=0){

				alert.setMessage("Cliente no tiene credito actualmente.");

				chkFactura.setText("Pagar Facturas");
				if(gl.rutatipo.equalsIgnoreCase("P")){
					chkContado.setText("Salir");
				}else{
					chkContado.setText("Continuar la venta al contado");
				}

			}

			layout.addView(chkFactura);
			layout.addView(chkContado);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					if(chkFactura.isChecked()){
						closekeyb();
						layout.removeAllViews();
					} else if (chkContado.isChecked()){
						if(gl.rutatipo.equalsIgnoreCase("V")){
							initVenta();
						}else{
							gl.closeVenta=true;
							finish();
						}

						layout.removeAllViews();
					}else{
						toast("Seleccione acción a realizar");
						closekeyb();
						msgAskFact();
					}
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
					layout.removeAllViews();
				}
			});

			alert.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void initVenta(){
		try{
			if (gl.peModal.equalsIgnoreCase("APR")) {
				startActivity(new Intent(this,Aprofam1.class));
			} else {
				browse=3;
				onPause();
				onResume();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void createDoc(){

		try{

			if (gl.pagomodo==0) {
				docList();
				if (!applyPay()) return;
			}

			if (saveCobro()) {

				if (!msgDesc.isEmpty()){
					toast(msgDesc);
				}

				listItems();

				if (dtipo.equalsIgnoreCase("R")) {
					if (prn.isEnabled()) {
						fdocf.buildPrint(crrf,0,gl.peModal);
						prn.printask(printcallback);
					}else if(!prn.isEnabled()){
						fdocf.buildPrint(crrf,0,gl.peModal);

						if(gl.validarCred==1){
							validaCredito();
						}else if(gl.validarCred==2){
							Cobro.super.finish();
						}

						gl.validarCred=0;
					}
				}else {
					if (prn.isEnabled()) {

						fdoc.buildPrint(corel,0,gl.peModal);
						browse = 4;
						prn.printask(printcallback);

						/*if(gl.validarCred==1){
							validaCredito();
						}

						gl.validarCred=0;*/

					}else if(!prn.isEnabled()){
						fdoc.buildPrint(corel,0,gl.peModal);

						if(gl.validarCred==1){
							validaCredito();
						}else if(gl.validarCred==2){
							Cobro.super.finish();
						}

						gl.validarCred=0;
					}
				}
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("createDoc: " + e.getMessage());
		}

	}

	public double MontoDescuentoPP(String documento){

		Cursor DT;
		double cantDias=0;
		clsDescuento clsDesc;
		clsClasses.clsNCPP vItem;
		long lfechaE;
		String sfechaE;

		long segsMilli = 1000;
		long minsMilli = segsMilli * 60;
		long horasMilli = minsMilli * 60;
		long diasMilli = horasMilli * 24;
		long diferencia;

		double result = 0;

		try{

			msgDesc="";

			sql=" SELECT P.FECHAV, P.DOCUMENTO, P.VALORORIG " +
				" FROM P_COBRO P" +
				" WHERE (P.DOCUMENTO = '" + documento + "')" +
				"  AND  (P.SALDO = P.VALORORIG)" +
				"  AND  (P.CANCELADO = 0)";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			for (int i = 0; i < DT.getCount(); i++ ) {

				cantDias=0;

				lfechaE=DT.getLong(0);
				sfechaE = du.sfecha(lfechaE);

				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Date strDate = sdf.parse(sfechaE);
				diferencia=strDate.getTime() - System.currentTimeMillis() ;

				cantDias = diferencia / diasMilli;

				if (cantDias>0){

					clsDesc=new clsDescuento(this,"",0,cantDias);
					double desc=clsDesc.getDescPP();

					if (desc>0){

						vItem = clsCls.new clsNCPP();
						vItem.Factura = DT.getString(1);
						vItem.MontoFact =  DT.getLong(2);
						vItem.MontoDesc = vItem.MontoFact*(desc/100);
						vItem.PorcDesc =  desc;
						vItem.MontoFactPago = DT.getLong(2);
						vItem.Total=vItem.MontoFactPago-vItem.MontoDesc;
						docs.add(vItem);

						msgDesc += vItem.Factura + ", ";

						result = vItem.MontoDesc;
					}

				}

				DT.moveToNext();
			}

			if(DT!=null){
				DT.close();
			}

			if (result>0){
				msgDesc = "Documento aplica para descuento  por pronto pago por un descuento de  " + result;
				toast(msgDesc);
			}

		}catch (Exception ex) {
         	msgbox("Ocurrió un error obteniendo el descuentos por pronto pago " + ex.getMessage());
		}

		return result;
	}

	public String sfecha(long f) {
		long vy,vm,vd;
		String s;

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"-";} else {s=s+"0"+String.valueOf(vd)+"-";}
		if (vm>9) { s=s+String.valueOf(vm)+"-";} else {s=s+"0"+String.valueOf(vm)+"-";}
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);}

		return s;
	}

	private boolean saveCobro(){
		Cursor DT;
		Cursor DT2;
		double tpago;
		String doc="";

		if (!assignCorel()) return false;

		corel= correlativo_recibo();

		fecha=du.getActDateTime();
		if (gl.peModal.equalsIgnoreCase("TOL")) fecha=app.fechaFactTol(du.getActDate());


		try {

			if (!dtipo.equalsIgnoreCase("R")) {
				db.beginTransaction();

				// Encabezado

				sql="SELECT SUM(VALOR) FROM T_PAGO";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				tpago=DT.getDouble(0);

                  ins.init("D_COBRO");
                  ins.add("COREL",corel);
                  ins.add("ANULADO","N");
                  ins.add("FECHA",fecha);
                  ins.add("EMPRESA",gl.emp);
                  ins.add("RUTA",gl.ruta);
                  ins.add("VENDEDOR",gl.vend);
                  ins.add("CLIENTE",gl.cliente);
                  ins.add("KILOMETRAJE",0);
                  ins.add("TOTAL",tpago);
                  ins.add("DEPOS","");
                  ins.add("CORELC","");
                  ins.add("BANDERA","");
                  ins.add("STATCOM","N");
                  ins.add("CALCOBJ","N");
                  ins.add("IMPRES",0);
                  ins.add("CODIGOLIQUIDACION",0);
                  ins.add("SERIE",fserie);
                  ins.add("CORELATIVO",fcorel);

				db.execSQL(ins.sql());

				if (gl.pagomodo==0) {

					// Cobro regular - Documentos

					sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD ";
					DT=Con.OpenDT(sql);

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						ins.init("D_COBROD");

						ins.add("COREL",corel);
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("DOCUMENTO",DT.getString(0));
						ins.add("TIPODOC",DT.getString(1));
						ins.add("MONTO",DT.getDouble(2));
						ins.add("PAGO",DT.getDouble(3));
						ins.add("CONTRASENA","");
						ins.add("ID_TRANSACCION",0);
						ins.add("REFERENCIA","");
						ins.add("ASIGNACION","");

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

				} else {

					ins.init("D_COBROD_SR");

					ins.add("COREL",corel);
					ins.add("DOCUMENTO",gl.cliente);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("TIPODOC","SR");
					ins.add("MONTO",tpago);
					ins.add("PAGO",tpago);
					ins.add("CONTRASENA","1");

					db.execSQL(ins.sql());

				}

				// Pagos

				sql="SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				DT=Con.OpenDT(sql);

				if (DT.getCount()>0){

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						ins.init("D_COBROP");
						ins.add("COREL",corel);
						ins.add("ITEM",DT.getInt(0));
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("CODPAGO",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("VALOR",DT.getDouble(3));
						ins.add("DESC1",DT.getString(4));
						ins.add("DESC2",DT.getString(5));
						ins.add("DESC3",DT.getString(6));
						ins.add("DEPOS","N");

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

                      // Ultimo corel
                      sql="UPDATE P_CORRELREC SET ACTUAL="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";
                      db.execSQL(sql);
                  }

				//Guardar las notas de crédito por pronto pago
				if (docs.size()>0){

					clsClasses.clsNCPP vItem;

					fecha=du.getActDateTime();

					for (int i = 0; i < docs.size(); i++ ) {

						vItem=docs.get(i);

						ins.init("D_NOTACRED_PP");

						ins.add("COREL",corel);
						ins.add("ANULADO","N");
						ins.add("FECHA", fecha);
						ins.add("RUTA",gl.ruta);
						ins.add("VENDEDOR",gl.vend);
						ins.add("CLIENTE",gl.cliente);
						ins.add("TOTAL",vItem.toString());
						ins.add("FACTURA",vItem.Factura);
						ins.add("MONTOFACTURA",vItem.MontoFact);
						ins.add("MONTODESCUENTO",vItem.MontoDesc);
						ins.add("PORCENTAJEDESC",vItem.PorcDesc);
						ins.add("MONTOPAGADOFACT",vItem.MontoFactPago);
						ins.add("STATCOM","N");
						ins.add("CODIGOLIQUIDACION",0);
						ins.add("IMPRES",0);

						db.execSQL(ins.sql());

					}
				}

				db.setTransactionSuccessful();
				db.endTransaction();

              }else{

				db.beginTransaction();

				sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD";
				DT2=Con.OpenDT(sql);

				if (DT2.getCount()>0){
					DT2.moveToFirst();
					doc=DT2.getString(0);
					crrf = doc;
				}

				sql="SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				DT=Con.OpenDT(sql);

				if (DT.getCount()>0) {

					DT.moveToFirst();

					while (!DT.isAfterLast()) {

						ins.init("D_FACTURAP");
						ins.add("COREL",doc);
						ins.add("ITEM",DT.getInt(0));
						ins.add("ANULADO","N");
						ins.add("EMPRESA",gl.emp);
						ins.add("CODPAGO",DT.getInt(1));
						ins.add("TIPO",DT.getString(2));
						ins.add("VALOR",DT.getDouble(3));
						ins.add("DESC1",DT.getString(4));
						ins.add("DESC2",DT.getString(5));
						ins.add("DESC3",DT.getString(6));
						ins.add("DEPOS","N");

						db.execSQL(ins.sql());

						DT.moveToNext();
					}

				}

				sql = "DELETE FROM P_COBRO WHERE DOCUMENTO='" + doc + "'";
				db.execSQL(sql);

				db.setTransactionSuccessful();

				db.endTransaction();
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox("saveCobro_ "+ e.getMessage());return false;
		}

		return true;
	}

	private String correlativo_recibo(){

		Cursor DT;
		int cor=0;
		String crr = "";

		try{

			sql="SELECT SERIE,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'" ;
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();
				cor =DT.getInt(1)+1;

				crr= DT.getString(0) + StringUtils.right("000000" + Integer.toString(cor), 6);

			}else{
				crr=gl.ruta+"_"+mu.getCorelBase();
			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return crr;

	}

	private void docList(){
		clsClasses.clsCobro vItem;
		int j=0;

		try {

			sql="DELETE FROM T_PAGOD";
			db.execSQL(sql);

			for (int i = 0; i < items.size(); i++ ) {

				vItem=items.get(i);

				if (vItem.flag==1) {

					if (vItem.Saldo>0) {

						j+=1;

						ins.init("T_PAGOD");

						ins.add("ITEM",j);
						ins.add("DOCUMENTO",vItem.Factura);
						ins.add("TIPODOC",vItem.Tipo);
						ins.add("MONTO",vItem.Saldo);
						ins.add("PAGO",0);

						db.execSQL(ins.sql());

					}
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("docList: "+ e.getMessage());
		}

	}

	private boolean applyPay(){
		Cursor DT;
		double tpago,apago,saldo,monto;
		int id;

		try {

			sql="SELECT SUM(VALOR) FROM T_PAGO";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {
				mu.msgbox("Total de pagos = 0 ");return false;
			}

			DT.moveToFirst();

			tpago=DT.getDouble(0);
			apago=tpago;

			sql="SELECT ITEM,MONTO FROM T_PAGOD";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				while (!DT.isAfterLast()) {
					id=DT.getInt(0);
					monto=DT.getDouble(1);

					if (apago>=monto) {
						saldo=monto;
						apago-=saldo;
					} else {
						saldo=apago;
						apago=0;
					}

					sql="UPDATE T_PAGOD SET PAGO="+saldo+" WHERE ITEM="+id;
					db.execSQL(sql);

					//MU.msgbox(sql+"\n"+apago);

					if (apago<=0) break;

					DT.moveToNext();

				}

				return true;

			}else  {
				return  false;
			}


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("ApplyPay: "+ e.getMessage());return false;
		}

	}


	// Pago Efectivo

	private void inputEfectivo() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Pago Efectivo");
			alert.setMessage("Valor a pagar");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(""+tsel);
			input.requestFocus();

			alert.setCancelable(false);
			showkeyb();

			checkCheck =  1;
			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					peexit=false;
					sefect=input.getText().toString();
					//closekeyb();
					checkCash();
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					peexit=true;
					closekeyb();
				}
			});

			alert.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void checkCash() {
		double epago;

		try {

			//#CKFK 2019-04-10 Quité epago=tsel; porque aquí se debe asignar el valor ingresado por el usuario
			epago=Double.parseDouble(sefect);

			if (epago==0) return;

			if (epago<0) throw new Exception();

			if (epago>tsel) {
				mu.msgbox("Total a pagar mayor que total de monto seleccionado");return;
			}

			if (!gl.pagocobro){
				if (epago<tsel) {
					mu.msgbox("Pago no está completo, no se puede aplicar el pago");
					return;
				}
			}


			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");

			ins.add("ITEM",1);
			ins.add("CODPAGO",1);
			ins.add("TIPO","E");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");

			db.execSQL(ins.sql());

			msgAskSave("Aplicar pago?");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			inputEfectivo();
			mu.msgbox("Pago incorrecto"+e.getMessage());
		}

	}


	// Impresion

	private void singlePrint() {
		try{
			prn.printask(printcallback);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	// Aux

	private void showTotals(){

		try{
			total = tsel + tpagos;

			lblSel.setText(mu.frmcur(total));
			lblPag.setText(mu.frmcur(tpagos));

			/*tpend=total-tpagos;
			plim=total-tpagos;*/

			if (tsel>=0) {
				lblPend.setText(mu.frmcur(tsel));
			} else {
				lblPend.setText(mu.frmcur(0));
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("No es posible mostrar los totales "+e.getMessage());
		}


	}

	private void calcSelected() {

		try{

			clsClasses.clsCobro vItem;
			Object lvObj;
			int flag,dc;
			double val;
            double montoDescuento = 0;

			tsel=0;
			tpagos=0;

			if (adapter!=null){

				if (adapter.getCount()>0){

					dc=adapter.getCount();

					for (int i = 0; i < dc; i++ ) {
						lvObj = listView.getItemAtPosition(i);
						vItem = (clsClasses.clsCobro)lvObj;

						flag=vItem.flag;
						if (flag==1) {

							if(vItem.Pago > 0){

								val=vItem.Pago;
								tpagos+=val;

								//#CKFK 20190412 Agregué esto para que se sumara el dato de lo  para que llegue
								val=vItem.Saldo;
								tsel+=val;

							} else{

								//Aplica descuento por pronto pago
								montoDescuento = MontoDescuentoPP(vItem.Factura);

								val=vItem.Saldo;
								vItem.DescuentoPP = montoDescuento;

								tsel+=val-montoDescuento;

							}
						}
					}

				}

			}


		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("calcSelected: "+ e.getMessage());
		}
	}

	private void clearAll() {
		try{
			for (int i = 0; i < items.size(); i++ ) {
				items.get(i).flag=0;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private double getDocPago(String doc,String ptipo){
		Cursor DT;
		double tp;

		try {

			sql="SELECT SUM(PAGO) FROM D_COBROD "+
					"WHERE (ANULADO='N') AND (DOCUMENTO='"+doc+"') AND (TIPODOC='"+ptipo+"') ";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				tp=DT.getDouble(0);

			}else{
				tp=0;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tp=0;
		}

		return tp;

	}

	private void initSession(){

		try {

			sql="DELETE FROM T_PAGO";

			db.execSQL(sql);

			sql="DELETE FROM T_PAGOD";
			db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("initSession"+ e.getMessage());
		}

	}

	private boolean assignCorel(){
		Cursor DT;
		int ca,ci,cf,ca1,ca2;

		fcorel=0;fserie="";

		try {

			sql="SELECT SERIE,INICIAL,FINAL,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()==0)return false;

			DT.moveToFirst();

			fserie=DT.getString(0);
			ci=DT.getInt(1);
			cf=DT.getInt(2);
			ca=DT.getInt(3);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			fcorel=0;fserie="";
			mu.msgbox("No está definido correlativo de recibos.");return false;
		}

		fcorel=ca+1;
		if (fcorel>cf) toast("Se ha acabado el talonario de los recibos.");

		return true;

	}

	private void askPrint() {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					impres++;toast("Impres "+impres);

					try {
						sql="UPDATE D_COBRO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
					}

					if (dtipo.equalsIgnoreCase("R")) {
						sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+crrf+"'";
						db.execSQL(sql);
					}

					if (impres>1) {

                       /* try {
                            sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+gl.dvcorreld+"'";
                            db.execSQL(sql);
                        } catch (Exception e) {
                            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        }*/

						gl.brw=0;

					} else {

						if (dtipo.equalsIgnoreCase("R")) {
							if (prn.isEnabled()) {
								fdocf.buildPrint(crrf,1,gl.peModal);
								prn.printnoask(printclose, "print.txt");
								prn.printnoask(printclose, "print.txt");
							}
						}else {
							if (prn.isEnabled()) {

								fdoc.buildPrint(corel,1,gl.peModal);
								browse = 4;
								prn.printnoask(printclose, "print.txt");
								prn.printnoask(printclose, "print.txt");
							}
						}

					}

					if(gl.validarCred==1){
						validaCredito();//#CKFK 20190503 AskPrint
					}else if(gl.validarCred==2){
						Cobro.super.finish();
					}

					gl.validarCred=0;

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					singlePrint();
					//prn.printask(printcallback);

				}
			});


			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void exit() {

		try{

			showTotals();
			if(tsel>0) {
				msgAskExit("Tiene documentos pendientes de pago. Salir");
			} else {
				super.finish();
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	// MsgDialogs

	private void msgAskOverPayd(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg  + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					msgAskSave("Aplicar pago y crear un recibo");
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

	private void msgAskSave(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					createDoc();
					check();
					//validaCredito();
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

	private void msgAskExit(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setCancelable(false);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
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


	// Activity Events

	@Override
	protected void onResume() {

		try{

			super.onResume();

			if (browse==1) {
				browse=0;

				if (gl.pagado) createDoc();
			}

			if (browse==2) {
				browse=0;
				super.finish();
			}

			if (browse==3){
				browse=0;

				super.finish();
			}


		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	@Override
	public void onBackPressed() {

		try{

			if(gl.banderaCobro){
				browse=2;
				onPause();
				onResume();
			}else{
				if (gl.vcredito) {
					validaCredito();
				}else{
					browse=2;
					onPause();
					onResume();
				}
			}

		}catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
	}

}
