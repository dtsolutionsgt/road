package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FacturaRes extends PBase {

	private ListView listView;
	private TextView lblPago,lblFact,lblTalon,lblMPago,lblCred,lblPend,lblCash;
	private ImageView imgBon,imgMPago,imgCred,imgPend, imgCash;
	private CheckBox contadoCheck;
	private TextView lblVuelto;
	private EditText txtVuelto;
	private RelativeLayout rl_facturares;

	private List<String> spname = new ArrayList<String>();
	private ArrayList<clsClasses.clsCDB> items= new ArrayList<clsClasses.clsCDB>();
	private ListAdaptTotals adapter;
	
	private Runnable printcallback,printclose,printexit;
	
	private clsDescGlob clsDesc;
	private printer prn;
	private printer prn_nc;
	private clsDocFactura fdoc;
	private clsDocDevolucion fdev;
	private AppMethods app;

	private long fecha,fechae;
	private int fcorel,clidia,media;
	private String itemid,cliid,corel,sefect,fserie,desc1,svuelt,corelNC;
	private int cyear, cmonth, cday, dweek,stp=0,brw=0,notaC,impres;

	private double dmax,dfinmon,descpmon,descg,descgmon,descgtotal,tot,stot0,stot,descmon,totimp,totperc,credito;
	private double dispventa;
	private boolean acum,cleandprod,peexit,pago,saved,rutapos,porpeso,pagocompleto=false;


	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_factura_res);

		super.InitBase();
        addlog("FacturaRes",""+du.getActDateTime(),gl.vend);

		listView = (ListView) findViewById(R.id.listView1);
		lblPago = (TextView) findViewById(R.id.TextView01);
		lblFact = (TextView) findViewById(R.id.lblFact);
		lblTalon = (TextView) findViewById(R.id.lblTalon);
		lblMPago = (TextView) findViewById(R.id.lblCVence);
		lblCred = (TextView) findViewById(R.id.lblPend);
		lblPend = (TextView) findViewById(R.id.lblCVence2);
		lblCash = (TextView) findViewById(R.id.textView4);
		contadoCheck = (CheckBox) findViewById(R.id.checkContado);

		imgBon = (ImageView) findViewById(R.id.imageView6);
		imgMPago = (ImageView) findViewById(R.id.imageView1);
		imgCred = (ImageView) findViewById(R.id.imageView3);
		imgPend = (ImageView) findViewById(R.id.imageView12);
		imgCash = (ImageView) findViewById(R.id.imageView2);

		rl_facturares=(RelativeLayout)findViewById(R.id.relativeLayout1);
		rl_facturares.setVisibility(View.VISIBLE);

		lblVuelto = new TextView(this,null);
		txtVuelto = new EditText(this,null);

		cliid=gl.cliente;
		rutapos=gl.rutapos;
		media=gl.media;
		credito=gl.credito;
		gl.cobroPendiente = false;
		dispventa = gl.dvdispventa;dispventa=mu.round(dispventa,2);
		notaC = gl.tiponcredito;

		app = new AppMethods(this, gl, Con, db);

		if (rutapos) {
			lblMPago.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblCred.setText("Pago\nTarjeta");
			//imgCred.setImageResource(R.drawable.card_credit);
		} else {
			lblMPago.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblCred.setText("Pago\nCrédito");
			//imgCred.setImageResource(R.drawable.credit);
		}

		if (media==1) {
			contadoCheck.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		}

		if (media <= 3){
			contadoCheck.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.VISIBLE);
			imgPend.setVisibility(View.VISIBLE);
		}

		if (media==4) {

			if (gl.vcredito) {

				if (credito<=0 || gl.facturaVen != 0) {
					contadoCheck.setVisibility(View.INVISIBLE);
					lblCash.setVisibility(View.VISIBLE);
					imgCash.setVisibility(View.VISIBLE);
					lblPend.setVisibility(View.INVISIBLE);
					imgPend.setVisibility(View.INVISIBLE);
					imgCred.setVisibility(View.INVISIBLE);
					lblCred.setVisibility(View.INVISIBLE);
					imgMPago.setVisibility(View.VISIBLE);
					lblMPago.setVisibility(View.VISIBLE);
				}else if(credito > 0){
					contadoCheck.setVisibility(View.VISIBLE);
					lblCash.setVisibility(View.INVISIBLE);
					imgCash.setVisibility(View.INVISIBLE);
					lblPend.setVisibility(View.INVISIBLE);
					imgPend.setVisibility(View.INVISIBLE);
					imgCred.setVisibility(View.VISIBLE);
					lblCred.setVisibility(View.VISIBLE);
					imgMPago.setVisibility(View.INVISIBLE);
					lblMPago.setVisibility(View.INVISIBLE);
				}
			} else {
				contadoCheck.setVisibility(View.VISIBLE);
				lblCash.setVisibility(View.INVISIBLE);
				imgCash.setVisibility(View.INVISIBLE);
				lblPend.setVisibility(View.INVISIBLE);
				imgPend.setVisibility(View.INVISIBLE);
				imgCred.setVisibility(View.VISIBLE);
				lblCred.setVisibility(View.VISIBLE);
				imgMPago.setVisibility(View.INVISIBLE);
				lblMPago.setVisibility(View.INVISIBLE);
			}

		}

		if (gl.dvbrowse!=0){
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		}

		fechae=fecha;
		dweek=mu.dayofweek();

		clsDesc=new clsDescGlob(this);

		descpmon=totalDescProd();

		dmax=clsDesc.dmax;
		acum=clsDesc.acum;

		try {
			db.execSQL("DELETE FROM T_PAGO");
		} catch (SQLException e) {
		}

		processFinalPromo();

		printcallback= new Runnable() {
		    public void run() {

				if (notaC==2){

					String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
					fdev.buildPrint(gl.dvcorrelnc,0, vModo);

					prn_nc.printnoask(printclose, "printnc.txt");
					if (impres>0) prn_nc.printnoask(printclose, "printnc.txt");
				}

				askPrint();
		    }
		};

		printclose= new Runnable() {
		    public void run() {
		    	//FacturaRes.super.finish();
		    }
		};

		printexit= new Runnable() {
			public void run() {
				FacturaRes.super.finish();
			}
		};

		prn=new printer(this,printexit,gl.validimp);
		prn_nc=new printer(this,printclose,gl.validimp);

		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "");
		fdoc.deviceid =gl.deviceId;

		fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
		fdev.deviceid =gl.deviceId;

		saved=false;
		assignCorel();

		cliPorDia();

		setHandlers();

		txtVuelto.setInputType(InputType.TYPE_CLASS_NUMBER);

	}

	//region Events

	public void prevScreen(View view) {
		try{
			clearGlobals();
			if(gl.dvbrowse!=0){
				gl.dvbrowse =0;
			}
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void paySelect(View view) {

		try{
			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			gl.pagoval=tot;
			gl.pagolim=tot;
			gl.pagocobro=false;
			browse=1;

			Intent intent = new Intent(this,Pago.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("paySelect: " + e.getMessage());
		}

	}

	public void checkedBox(View view){
		contadoCheck.setVisibility(View.VISIBLE);
		if(contadoCheck.isChecked()){
			contadoCheck.setText("Pagar al Contado");
			lblCash.setVisibility(View.VISIBLE);
			imgCash.setVisibility(View.VISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.INVISIBLE);
			lblCred.setVisibility(View.INVISIBLE);
			imgMPago.setVisibility(View.VISIBLE);
			lblMPago.setVisibility(View.VISIBLE);
		} else if(!contadoCheck.isChecked()) {
			contadoCheck.setText("Pagar al Contado");
			lblCash.setVisibility(View.INVISIBLE);
			imgCash.setVisibility(View.INVISIBLE);
			lblPend.setVisibility(View.INVISIBLE);
			imgPend.setVisibility(View.INVISIBLE);
			imgCred.setVisibility(View.VISIBLE);
			lblCred.setVisibility(View.VISIBLE);
			imgMPago.setVisibility(View.INVISIBLE);
			lblMPago.setVisibility(View.INVISIBLE);
		}
	}

	public void payCash(View view) {

		try{

			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			//inputEfectivo();
			inputVuelto();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("payCash: " + e.getMessage());
		}

	}

	public void payCred(View view) {

		try{

			if (fcorel==0) {
				msgbox("No existe un correlativo disponible, no se puede emitir factura");return;
			}

			inputCredito();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("payCred: " + e.getMessage());
		}

	}

	public void showBon(View view) {

		try{

			Intent intent = new Intent(this,BonVenta.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setHandlers(){

		try{

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					prevScreen(null);
				}
				public void onSwipeLeft() {
					if (imgCash.getVisibility()==View.VISIBLE) {
						payCash(null);
					}
				}
			});

			txtVuelto.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {}

				public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

				public void onTextChanged(CharSequence s, int start,int before, int count) {

					//Davuelto();
				}

			});

			txtVuelto.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((keyCode == KeyEvent.KEYCODE_ENTER)) {

						DaVuelto(v);

						return true;
					}else if ((keyCode == KeyEvent.KEYCODE_DEL)){
						lblVuelto.setText("");
					}

					return false;
				}
			});

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void DaVuelto(View view) {
		try{
			Davuelto();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void pendientePago(View view){
		try{
			askPendientePago();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}


	//endregion

	//region Main

	private void processFinalPromo(){

		descg=gl.descglob;
		descgtotal=gl.descgtotal;

		try{

			//descgmon=(double) (stot0*descg/100);
			descgmon=(double) (descg*descgtotal/100);
			totalOrder();

			if (descg>0) {
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						showPromo();
					}
				}, 300);
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("processFinalPromo: " + e.getMessage());
		}

	}

	public void showPromo(){

		try {

			browse=1;
			gl.promprod="";
			gl.promcant=0;
			gl.promdesc=descg;

			Intent intent = new Intent(this,DescBon.class);
			startActivity(intent);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox( e.getMessage());
		}

	}

	private void updDesc(){

		try{

			descg=gl.promdesc;
			//descgmon=(double) (stot0*descg/100);
			descgmon=(double) (descg*descgtotal/100);
			totalOrder();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void totalOrder(){
		double dmaxmon;

		cleandprod=false;

		try{
			if (acum) {
				dfinmon=descpmon+descgmon;
				cleandprod=false;
			} else {
				if (descpmon>=descgmon) {
					dfinmon=descpmon;
					cleandprod=false;
				} else {
					dfinmon=descgmon;
					cleandprod=true;
				}
			}

			dmaxmon=(double) (stot0*dmax/100);
			if (dmax>0) {
				if (dfinmon>dmaxmon) dfinmon=dmax;
			}

			descmon=mu.round2(dfinmon);
			stot=mu.round2(stot0);

			fillTotals();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("totalOrder: " + e.getMessage());
		}


	}

	private void fillTotals() {
		clsClasses.clsCDB item;

		items.clear();

		try {

			if (gl.sinimp) {

				totimp=mu.round2(totimp);
				stot=stot-totimp;

				totperc=stot*(gl.percepcion/100);
				totperc=mu.round2(totperc);

				tot=stot+totimp-descmon+totperc;
				tot=mu.round2(tot);

				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);

				item = clsCls.new clsCDB();
				item.Cod="Impuesto";item.Desc=mu.frmcur(totimp);item.Bandera=0;
				items.add(item);

				if (gl.contrib.equalsIgnoreCase("C")) {
					item = clsCls.new clsCDB();
					item.Cod="Percepción";item.Desc=mu.frmcur(totperc);item.Bandera=0;
					items.add(item);
				}

				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);

				if (gl.dvbrowse!=0){
					item = clsCls.new clsCDB();
					item.Cod="Nota Crédito";item.Desc=mu.frmcur(-dispventa);item.Bandera=0;
					items.add(item);

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot-dispventa);item.Bandera=1;
					items.add(item);

				}else{
					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
					items.add(item);
				}



			} else {

				totimp=mu.round2(totimp);
				tot=stot-descmon;
				tot=mu.round2(tot);


				item = clsCls.new clsCDB();
				item.Cod="Subtotal";item.Desc=mu.frmcur(stot);item.Bandera=0;
				items.add(item);

				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);

				if (gl.dvbrowse!=0){

					item = clsCls.new clsCDB();
					item.Cod="Nota Crédito";item.Desc=mu.frmcur(-dispventa);item.Bandera=0;
					items.add(item);

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot-dispventa);item.Bandera=1;
					items.add(item);

				}else{

					item = clsCls.new clsCDB();
					item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
					items.add(item);

				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		adapter=new ListAdaptTotals(this,items);
		listView.setAdapter(adapter);
	}

 	private void finishOrder(){


		if (!saved) {
			if (!saveOrder()) return;
		}

		impressOrder();

	}

	private void impressOrder(){
		try{

			rl_facturares.setVisibility(View.INVISIBLE);

			if(gl.dvbrowse!=0) gl.dvbrowse =0;

			impres=0;

			if (prn.isEnabled()) {

				if (gl.peModal.equalsIgnoreCase("APR")) {
					fdoc.buildPrintExt(corel,2,"APR");
				} else if (gl.peModal.equalsIgnoreCase("...")) {
					//
				} else if (gl.peModal.equalsIgnoreCase("TOL")) {

					if (!gl.cobroPendiente) {
						if (impres==0) {
							fdoc.buildPrint(corel, 0,gl.peFormatoFactura);
						} else {
							fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
						}
					}else{
						fdoc.buildPrint(corel,4,gl.peFormatoFactura);
					}
				}

				if (gl.peImprFactCorrecta) {
					prn.printask(printcallback);
				} else {
					singlePrint();
				}

			}else if(!prn.isEnabled()){
				if (gl.peModal.equalsIgnoreCase("APR")) {
					fdoc.buildPrintExt(corel,2,"APR");
				} else if (gl.peModal.equalsIgnoreCase("...")) {
					//
				} else if (gl.peModal.equalsIgnoreCase("TOL")) {

					if (!gl.cobroPendiente) {
						if (impres==0) {
							fdoc.buildPrint(corel, 0,gl.peFormatoFactura);
						} else {
							fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
						}
					}else{
						fdoc.buildPrint(corel,4,gl.peFormatoFactura);
					}
				}

				if (notaC==2){
					fdev.buildPrint(gl.dvcorrelnc,0);
				}
			}

			gl.closeCliDet=true;
			gl.closeVenta=true;

			if (!prn.isEnabled()) super.finish();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("impressOrder: "  + e.getMessage());

			gl.closeCliDet = true;
			gl.closeVenta = true;

			super.finish();
		}

	}

 	private void singlePrint() {
		try{
			prn.printask(printcallback);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

 	}

	private boolean saveOrder(){
		Cursor dt;
		String vprod,vumstock,vumventa,vbarra;
		double vcant,vpeso,vfactor,peso,factpres,vtot,vprec;
		int mitem,bitem;
		int dev_ins=1;
		corel=gl.ruta+"_"+mu.getCorelBase();

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			fecha=du.getActDate();
		} else {
			fecha=du.getActDateTime();
		}

        sql="SELECT MAX(ITEM) FROM D_FACT_LOG";
        dt=Con.OpenDT(sql);

        if(dt.getCount()>0){
            dt.moveToFirst();
            mitem=dt.getInt(0);
        }else{
            mitem=0;
        }

		mitem++;

		try {

			db.beginTransaction();

			//region D_FACTURA

			sql="SELECT SUM(TOTAL),SUM(DESMON),SUM(IMP),SUM(PESO) FROM T_VENTA";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			peso=dt.getDouble(3);

			ins.init("D_FACTURA");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",fecha);
			ins.add("EMPRESA",gl.emp);
			ins.add("RUTA",gl.ruta);
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);

			ins.add("KILOMETRAJE",0);
			ins.add("FECHAENTR",fecha);
			ins.add("FACTLINK"," ");
	   		ins.add("TOTAL",tot);
			ins.add("DESMONTO",descmon);
			ins.add("IMPMONTO",totimp+totperc);
			ins.add("PESO",peso);

			ins.add("BANDERA","N");
			ins.add("STATCOM","N");
			ins.add("CALCOBJ","N");
			ins.add("SERIE",fserie);
			ins.add("CORELATIVO",fcorel);
			ins.add("IMPRES",0);

			ins.add("ADD1",gl.ref1);
			ins.add("ADD2",gl.ref2);
			ins.add("ADD3",gl.ref3);

			ins.add("DEPOS","N");
			ins.add("PEDCOREL","");
			ins.add("REFERENCIA","");

			if (gl.dvbrowse!=0){
				ins.add("ASIGNACION",gl.dvcorreld);
			}else{
				ins.add("ASIGNACION","");
			}

			ins.add("SUPERVISOR",gl.codSupervisor);
			ins.add("AYUDANTE",gl.ayudanteID);//#HS_20181207 Agregue parametro de ayudanteID
			ins.add("VEHICULO",gl.vehiculoID);//#HS_20181207 Agregue parametro de vehiculoID
			ins.add("CODIGOLIQUIDACION",0);
			ins.add("RAZON_ANULACION","");

			db.execSQL(ins.sql());

			//endregion

			//region Bonificacion

			clsBonifSave bonsave=new clsBonifSave(this,corel,"V");

			bonsave.ruta=gl.ruta;
			bonsave.cliente=gl.cliente;
			bonsave.fecha=fecha;
			bonsave.emp=gl.emp;

			bonsave.save();

			//endregion

			//region Devolución de  producto.
			if (gl.dvbrowse!=0){

				Cursor DT;
				String pcod;
				Double pcant;

				if (dev_ins==1){

				ins.init("D_CxC");

				ins.add("COREL",gl.dvcorreld);
				ins.add("RUTA",gl.ruta);
				ins.add("CLIENTE",gl.cliente);
				ins.add("FECHA",fecha);
				ins.add("ANULADO","N");
				ins.add("EMPRESA",gl.emp);
				ins.add("TIPO", gl.dvestado);
				ins.add("REFERENCIA",corel);
				ins.add("IMPRES",0);
				ins.add("STATCOM","N");
				ins.add("VENDEDOR",gl.vend);
				ins.add("TOTAL",dispventa);
				ins.add("SUPERVISOR",gl.codSupervisor);
				ins.add("AYUDANTE",gl.ayudanteID);
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("ESTADO","S");

				db.execSQL(ins.sql());


				ins.init("D_NOTACRED");

				ins.add("COREL",gl.dvcorrelnc);corelNC=gl.dvcorrelnc;
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",gl.cliente);
				ins.add("TOTAL",dispventa);
				ins.add("FACTURA",corel);
				ins.add("SERIE","0");
				ins.add("CORELATIVO",gl.dvactualnc);
				ins.add("STATCOM","N");
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("RESOLNC","N");
				ins.add("SERIEFACT",0);
				ins.add("CORELFACT",0);
				ins.add("IMPRES",0);

				db.execSQL(ins.sql());


				sql="SELECT Item,CODIGO,CANT,CODDEV,TOTAL,PRECIO,PRECLISTA,REF,PESO,LOTE,UMVENTA,UMSTOCK,UMPESO,FACTOR,POR_PESO FROM T_CxCD WHERE CANT>0";
				DT=Con.OpenDT(sql);

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					pcod=DT.getString(1);
					pcant=DT.getDouble(2);

					ins.init("D_CxCD");

					ins.add("COREL",gl.dvcorreld);
					ins.add("ITEM",DT.getInt(0));
					ins.add("CODIGO",DT.getString(1));
					ins.add("CANT",DT.getDouble(2));
					ins.add("CODDEV",DT.getString(3));
					ins.add("ESTADO",gl.dvestado);
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
					ins.add("POR_PRESO", DT.getString(14));
					ins.add("UMVENTA",DT.getString(10));
					ins.add("UMSTOCK",DT.getString(11));
					ins.add("UMPESO",DT.getString(12));
					ins.add("FACTOR",DT.getDouble(13));
					db.execSQL(ins.sql());

					if (!gl.peModal.equalsIgnoreCase("TOL")) {

						try {
							sql="INSERT INTO P_STOCK VALUES ('"+pcod+"',0,0,0)";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						}

						if (gl.dvestado.equalsIgnoreCase("M")) {
							sql="UPDATE P_STOCK SET CANTM=CANTM+"+pcant+" WHERE CODIGO='"+pcod+"'";
						} else {
							sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"'";
						}
						db.execSQL(sql);

					}

					DT.moveToNext();

				}

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactuald+" WHERE RUTA='"+gl.ruta+"' AND TIPO='D'";
				db.execSQL(sql);

				sql="UPDATE P_CORREL_OTROS SET ACTUAL="+gl.dvactualnc+" WHERE RUTA='"+gl.ruta+"' AND TIPO='NC'";
				db.execSQL(sql);

				Toast.makeText(this,"Devolución guardada", Toast.LENGTH_SHORT).show();

				sql="DELETE FROM T_CxCD";
				db.execSQL(sql);

				dev_ins = 0;

                }

			}

			//endregion

			//region D_FACTURAD

			sql="SELECT PRODUCTO,CANT,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2,UM,FACTOR,UMSTOCK FROM T_VENTA";
			dt=Con.OpenDT(sql);

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				porpeso=prodPorPeso(dt.getString(0));
				factpres=dt.getDouble(12);
				peso=dt.getDouble(8);
				vumstock=app.umStock(dt.getString(0));

			  	ins.init("D_FACTURAD");
				ins.add("COREL",corel);
				ins.add("PRODUCTO",dt.getString(0));
				ins.add("EMPRESA",gl.emp);
				ins.add("ANULADO","N");
				ins.add("CANT",dt.getDouble(1));
				ins.add("PRECIO",dt.getDouble(2));
				ins.add("IMP",dt.getDouble(3));
				ins.add("DES",dt.getDouble(4));
				ins.add("DESMON",dt.getDouble(5));
				ins.add("TOTAL",dt.getDouble(6));
				ins.add("PRECIODOC",dt.getDouble(7));
				ins.add("PESO",dt.getDouble(8));
				ins.add("VAL1",dt.getDouble(9));
				ins.add("VAL2",dt.getString(10));
				ins.add("UMVENTA",dt.getString(11));
				ins.add("FACTOR",dt.getDouble(12));
				ins.add("UMSTOCK",vumstock);
				ins.add("UMPESO",gl.umpeso); //#HS_20181120_1625 Se agrego el valor gl.umpeso anteriormente estaba ""

			    db.execSQL(ins.sql());

			    vprod=dt.getString(0);
				//vumstock=dt.getString(13);
				vcant=dt.getDouble(1);
				vpeso=dt.getDouble(8);
				vfactor=vpeso/(vcant*factpres);
				vumventa=dt.getString(11);

				if (esProductoConStock(dt.getString(0))) {
					rebajaStockUM(vprod, vumstock, vcant, vfactor, vumventa,factpres,peso);
				}

			    dt.moveToNext();
			}

			//endregion

			//region D_FACTURAP

			if(!gl.cobroPendiente) {

				sql = "SELECT ITEM,CODPAGO,TIPO,VALOR,DESC1,DESC2,DESC3 FROM T_PAGO";
				dt = Con.OpenDT(sql);

				dt.moveToFirst();
				while (!dt.isAfterLast()) {

					ins.init("D_FACTURAP");
					ins.add("COREL", corel);
					ins.add("ITEM", dt.getInt(0));
					ins.add("ANULADO", "N");
					ins.add("EMPRESA", gl.emp);
					ins.add("CODPAGO", dt.getInt(1));
					ins.add("TIPO", dt.getString(2));
					ins.add("VALOR", dt.getDouble(3));
					ins.add("DESC1", dt.getString(4));
					ins.add("DESC2", dt.getString(5));
					ins.add("DESC3", dt.getString(6));
					ins.add("DEPOS", "N");

					db.execSQL(ins.sql());

					dt.moveToNext();
				}

			} else {

				try {
					ins.init("P_COBRO");

					ins.add("DOCUMENTO", corel);
					ins.add("EMPRESA", gl.emp);
					ins.add("RUTA", gl.ruta);
					ins.add("CLIENTE", gl.cliente);
					ins.add("TIPODOC", "R");
					ins.add("VALORORIG", tot);
					ins.add("SALDO", tot);
					ins.add("CANCELADO", 0);
					ins.add("FECHAEMIT", fecha);
					ins.add("FECHAV", fecha);
					ins.add("CONTRASENA", corel);
					ins.add("ID_TRANSACCION", 0);
					ins.add("REFERENCIA", 0);
					ins.add("ASIGNACION", 0);

					db.execSQL(ins.sql());

					Toast.makeText(this, "Se guardo la factura pendiente de pago",Toast.LENGTH_LONG).show();

				}catch (Exception e){
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					mu.msgbox("PendientePago: "+e.getMessage());
				}

			}

			//endregion

			//region D_FACTURAF

			ins.init("D_FACTURAF");

			ins.add("COREL",corel);
			ins.add("NOMBRE",gl.fnombre);
			ins.add("NIT",gl.fnit);
			ins.add("DIRECCION",gl.fdir);

			db.execSQL(ins.sql());

			//endregion

			//region D_STOCKB_DEV

			sql="INSERT INTO D_FACTURA_BARRA SELECT * FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA)";
			db.execSQL(sql);

			sql="UPDATE D_FACTURA_BARRA SET Corel='"+corel+"' WHERE Corel=''";
			db.execSQL(sql);

			try {

				sql="SELECT BARRA,CODIGO,PESO FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA)";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0) dt.moveToFirst();
				while (!dt.isAfterLast()) {

					ins.init("D_STOCKB_DEV");

					ins.add("BARRA",dt.getString(0));
					ins.add("RUTA",gl.ruta);
					ins.add("VENDEDOR",gl.vend);
					ins.add("CODIGO",dt.getString(1));
					ins.add("COREL",corel);
					ins.add("FECHA",fecha);
					ins.add("PESO",dt.getDouble(2));
					ins.add("CODIGOLIQUIDACION",0);

					db.execSQL(ins.sql());

					dt.moveToNext();
				}
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			}

			sql="DELETE FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA)";
			db.execSQL(sql);

			//endregion

			//region Bonificaciones

            //region D_BONIF

			sql="SELECT ITEM,PRODID,BONIID,CANT,PRECIO,COSTO,UMVENTA,UMSTOCK,UMPESO,FACTOR FROM T_BONITEM";
			dt=Con.OpenDT(sql);

			if(dt.getCount()>0){

				dt.moveToFirst();bitem=1;
				while (!dt.isAfterLast()) {

					vcant= dt.getDouble(3);
					vprec= dt.getDouble(4);
					vtot= vcant*vprec;vtot=mu.roundr(vtot,2);

					ins.init("D_BONIF");

					ins.add("COREL",corel);
					ins.add("ITEM",bitem);
					ins.add("FECHA",fecha);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("RUTA",gl.ruta);
					ins.add("CLIENTE",gl.cliente);
					ins.add("PRODUCTO",dt.getString(2));
					ins.add("CANT",vcant);
					ins.add("VENPED","V");
					ins.add("TIPO","");
					ins.add("PRECIO",vprec);
					ins.add("COSTO",dt.getDouble(5));
					ins.add("TOTAL",vtot );
					ins.add("STATCOM","N");
					ins.add("UMVENTA",dt.getString(6));
					ins.add("UMSTOCK",dt.getString(7) );
					ins.add("UMPESO",dt.getString(8));
					ins.add("FACTOR",dt.getDouble(9));

					db.execSQL(ins.sql());

					vprod=dt.getString(2);
					vumstock=dt.getString(7);
					vumventa=dt.getString(6);
					vfactor=dt.getDouble(9);
					peso=vcant*vfactor;
					factpres=app.factorPres(vprod,vumventa,vumstock);

					rebajaStockBonif(vprod, vumstock, vcant, vfactor, vumventa,factpres,peso);

					dt.moveToNext();bitem++;
				}
			}

            //endregion

            //region T_BARRA_BONIF

            sql="INSERT INTO D_FACTURA_BARRA SELECT * FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA_BONIF)";
            db.execSQL(sql);

            sql="UPDATE D_FACTURA_BARRA SET Corel='"+corel+"' WHERE Corel=''";
            db.execSQL(sql);

			sql="SELECT BARRA,CODIGO,PESO FROM T_BARRA_BONIF";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				while (!dt.isAfterLast()) {

					vbarra=dt.getString(0);
					vprod=dt.getString(1);
					vpeso=dt.getDouble(2);

					vumstock=app.umStock(vprod);
					vumventa=app.umVenta(vprod);
					vfactor=app.factorPeso(vprod);

					ins.init("D_BONIF_BARRA");

					ins.add("COREL",corel);
					ins.add("BARRA",vbarra);
					ins.add("PESO",vpeso);
					ins.add("PRODUCTO",vprod);
					ins.add("UMVENTA",vumventa);
					ins.add("UMSTOCK",vumstock);
					ins.add("UMPESO",gl.umpeso);
					ins.add("FACTOR",vfactor);

					db.execSQL(ins.sql());

					dt.moveToNext();
				}
			}

			sql="DELETE FROM P_STOCKB WHERE BARRA IN (SELECT BARRA FROM T_BARRA_BONIF)";
			db.execSQL(sql);

            //endregion

			//region T_BONIFFALT

            sql = "SELECT PRODID,PRODUCTO,CANT FROM T_BONIFFALT";
            dt = Con.OpenDT(sql);

            if (dt.getCount() > 0) dt.moveToFirst();
            while (!dt.isAfterLast()) {

                ins.init("D_BONIFFALT");

                ins.add("COREL", corel);
                ins.add("FECHA", fecha);
                ins.add("ANULADO", "N");
                ins.add("RUTA", gl.ruta);
                ins.add("CLIENTE", gl.cliente);
                ins.add("PRODUCTO", dt.getString(1));
                ins.add("CANT", dt.getDouble(2));

                db.execSQL(ins.sql());

                dt.moveToNext();
            }

            //endregion

			//endregion

			//region Actualizacion de ultimo correlativo

			sql="UPDATE P_COREL SET CORELULT="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";
			db.execSQL(sql);

			ins.init("D_FACT_LOG");
			ins.add("ITEM",mitem);
			ins.add("SERIE",fserie);
			ins.add("COREL",fcorel);
			ins.add("FECHA",0);
			ins.add("RUTA",gl.ruta);
			db.execSQL(ins.sql());

			//endregion

			db.setTransactionSuccessful();
			db.endTransaction();

			if(gl.dvbrowse!=0){
				gl.dvbrowse =0;
				gl.tiponcredito=0;
			}

			saved=true;

			upd.init("P_CLIRUTA");
			upd.add("BANDERA",0);
			upd.Where("CLIENTE='"+cliid+"'");
			db.execSQL(upd.SQL());

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            db.endTransaction();
            mu.msgbox("Error (factura) " + e.getMessage());return false;
        }

		saveAtten(tot);

		return true;
	}

	private void rebajaStockUM(String prid,String umstock,double cant,double factor, String umventa,double factpres,double ppeso) {
		Cursor dt;
		double cantapl,dispcant,actcant,pesoapl,disppeso,actpeso,speso,factlote;
		String lote,doc,stat;

		if (porpeso) {
			actcant=cant;
			actpeso=ppeso;
		} else {
			actcant=cant*factpres;
			actpeso=cant*factor;
		}

		try {

			sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
				"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"') ORDER BY CANT";
			//sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
			//		"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') ORDER BY CANT";


			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				cant=dt.getDouble(0);
				speso=dt.getDouble(2);
				lote=dt.getString(4);
				doc=dt.getString(5);
				stat=dt.getString(9);

				if (actcant>cant) cantapl=cant;else cantapl=actcant;
				dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
				actcant=actcant-cantapl;

				if (porpeso) {
					if (actpeso>speso) pesoapl=speso;else pesoapl=actpeso;
					actpeso=actpeso-pesoapl;
				} else {
					pesoapl=cantapl*factor;
				}
				disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

				// Stock

				sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"') AND (UNIDADMEDIDA='"+umstock+"')";
				//sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"')";
				db.execSQL(sql);

				sql="DELETE FROM P_STOCK WHERE (CANT<=0) AND (CANTM<=0)";
				db.execSQL(sql);


				// Factura Stock

				ins.init("D_FACTURA_STOCK");

				ins.add("COREL",corel);
				ins.add("CODIGO",prid );
				ins.add("CANT",cantapl );
				ins.add("CANTM",dt.getDouble(1));
				ins.add("PESO",pesoapl);
				ins.add("plibra",dt.getDouble(3));
				ins.add("LOTE",lote );

				ins.add("DOCUMENTO",doc);
				ins.add("FECHA",dt.getInt(6));
				ins.add("ANULADO",dt.getInt(7));
				ins.add("CENTRO",dt.getString(8));
				ins.add("STATUS",stat);
				ins.add("ENVIADO",dt.getInt(10));
				ins.add("CODIGOLIQUIDACION",dt.getInt(11));
				ins.add("COREL_D_MOV",dt.getString(12));
				ins.add("UNIDADMEDIDA",umstock);

				db.execSQL(ins.sql());

				// Factura lotes

				factlote=factpres;if (factlote<1) factlote=1/factlote;

				try {

					ins.init("D_FACTURAD_LOTES");

					ins.add("COREL",corel);
					ins.add("PRODUCTO",prid );
					ins.add("LOTE",lote );

					//if (porpeso) {
						ins.add("CANTIDAD",cantapl);
					//} else {
						//ins.add("CANTIDAD",cantapl*factpres);
					//}

					ins.add("PESO",pesoapl);
					ins.add("UMSTOCK",umstock);
					ins.add("UMPESO",gl.umpeso);
					ins.add("UMVENTA",umventa);

					db.execSQL(ins.sql());

				} catch (SQLException e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);

					sql="UPDATE D_FACTURAD_LOTES SET CANTIDAD=CANTIDAD+"+cantapl+",PESO=PESO+"+pesoapl+"  " +
						"WHERE (COREL='"+corel+"') AND (PRODUCTO='"+prid+"') AND (LOTE='"+lote+"')";
					db.execSQL(sql);
					//mu.msgbox(e.getMessage()+"\n"+ins.sql());
				}

				//if (actcant<=0) return;

				dt.moveToNext();
			}

            db.execSQL("DELETE FROM D_FACTURAD_LOTES WHERE CANTIDAD<=0");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("rebajaStockUM: "+e.getMessage());
		}
	}

	private void rebajaStockBonif(String prid,String umstock,double cant,double factor, String umventa,double factpres,double ppeso) {
		Cursor dt;
		double cantapl,dispcant,actcant,pesoapl,disppeso,actpeso,speso;
		String lote,doc,stat;

		if (porpeso) {
			actcant=cant;
			actpeso=ppeso;
		} else {
			actcant=cant*factpres;
			actpeso=cant*factor;
		}

		try {

			sql="SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV " +
					"FROM P_STOCK WHERE (CANT>0) AND (CODIGO='"+prid+"') AND (UNIDADMEDIDA='"+umstock+"') ORDER BY CANT";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				cant=dt.getDouble(0);
				speso=dt.getDouble(2);
				lote=dt.getString(4);
				doc=dt.getString(5);
				stat=dt.getString(9);

				if (actcant>cant) cantapl=cant;else cantapl=actcant;
				dispcant=cant-cantapl;if (dispcant<0) dispcant=0;
				actcant=actcant-cantapl;

				if (porpeso) {
					if (actpeso>speso) pesoapl=speso;else pesoapl=actpeso;
					actpeso=actpeso-pesoapl;
				} else {
					pesoapl=cantapl*factor;
				}
				disppeso=speso-pesoapl;if (disppeso<0) disppeso=0;

				// Stock

				sql="UPDATE P_STOCK SET CANT="+dispcant+",PESO="+disppeso+" WHERE (CODIGO='"+prid+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"') AND (UNIDADMEDIDA='"+umstock+"')";
				db.execSQL(sql);

				sql="DELETE FROM P_STOCK WHERE (CANT<=0) AND (CANTM<=0)";
				db.execSQL(sql);


				// Bonif Stock

				ins.init("D_BONIF_STOCK");

				ins.add("COREL",corel);
				ins.add("CODIGO",prid );
				ins.add("CANT",cantapl );
				ins.add("CANTM",dt.getDouble(1));
				ins.add("PESO",pesoapl);
				ins.add("plibra",dt.getDouble(3));
				ins.add("LOTE",lote );

				ins.add("DOCUMENTO",doc);
				ins.add("FECHA",dt.getInt(6));
				ins.add("ANULADO",dt.getInt(7));
				ins.add("CENTRO",dt.getString(8));
				ins.add("STATUS",stat);
				ins.add("ENVIADO",dt.getInt(10));
				ins.add("CODIGOLIQUIDACION",dt.getInt(11));
				ins.add("COREL_D_MOV",dt.getString(12));
				ins.add("UNIDADMEDIDA",umstock);

				db.execSQL(ins.sql());

				// Bonif lotes

				try {
					ins.init("D_BONIF_LOTES");

					ins.add("COREL",corel);
					ins.add("PRODUCTO",prid );
					ins.add("LOTE",lote );

					if (porpeso) {
						ins.add("CANT",cantapl);
					} else {
						ins.add("CANT",cantapl/factpres);
					}

					ins.add("PESO",pesoapl);
					ins.add("UMSTOCK",umstock);
					ins.add("UMPESO",gl.umpeso);
					ins.add("UMVENTA",umventa);
					ins.add("FACTOR",factor);

					db.execSQL(ins.sql());

					//Toast.makeText(this,ins.SQL(),Toast.LENGTH_LONG).show();

				} catch (SQLException e) {
					sql="UPDATE D_BONIF_LOTES SET CANT=CANT+"+cantapl+",PESO=PESO+"+pesoapl+"  " +
						"WHERE (COREL='"+corel+"') AND (PRODUCTO='"+prid+"') AND (LOTE='"+lote+"')";
					db.execSQL(sql);
				}

				//if (actcant<=0) return;

				dt.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("rebajaStockUM: "+e.getMessage());
		}

	}

	private void saveAtten(double tot) {
		long ti,tf,td;

		ti=gl.atentini;tf=du.getActDateTime();
		td=du.timeDiff(tf,ti);if (td<1) td=1;

		try {
			ins.init("D_ATENCION");

			ins.add("RUTA",gl.ruta);
			ins.add("FECHA",ti);
			ins.add("HORALLEG",gl.ateninistr);
			//ins.add("HORALLEG",DU.shora(ti)+":00");
			ins.add("HORASAL",du.shora(tf)+":00");
			ins.add("TIEMPO",td);

			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("DIAACT",du.dayofweek(ti));
			ins.add("DIA",du.dayofweek(ti));
			ins.add("DIAFLAG","S");

			ins.add("SECUENCIA",1);
			ins.add("SECUENACT",1);
			ins.add("CODATEN","");
			ins.add("KILOMET",0);

			ins.add("VALORVENTA",tot);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",clidia);
			ins.add("CODOPER","V");
			ins.add("COREL",corel);

			ins.add("SCANNED",gl.escaneo);
			ins.add("STATCOM","N");
			ins.add("LLEGO_COMPETENCIA_ANTES",0);

			ins.add("CoorX",gl.gpspx);
			ins.add("CoorY",gl.gpspy);
			ins.add("CliCoorX",gl.gpscpx);
			ins.add("CliCoorY",gl.gpscpy);
			ins.add("Dist",gl.gpscdist);

			db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//String s=gl.ruta+" / "+ti+" / "+gl.ateninistr;
			mu.msgbox("Error (att) : " + e.getMessage());
		}

	}

	private double totalDescProd(){
		Cursor DT;

		try {
			sql="SELECT SUM(DESMON),SUM(TOTAL),SUM(IMP) FROM T_VENTA";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                DT.moveToFirst();

                tot=DT.getDouble(1);
                stot0=tot+DT.getDouble(0);

                totimp=DT.getDouble(2);

                return DT.getDouble(0);
            }else {
			    return 0;
            }

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("totalDescProd: " + e.getMessage());

			return 0;
		}

	}

	private void assignCorel(){
		Cursor DT;
		int ca,ci,cf,ca1,ca2;

		fcorel=0;fserie="";

		try {

			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN FROM P_COREL WHERE RUTA='"+gl.ruta+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                DT.moveToFirst();

                fserie=DT.getString(0);
                ca1=DT.getInt(1);
                ci=DT.getInt(2);
                cf=DT.getInt(3);
            }else  {
                fcorel=0;fserie="";
                mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n");
                return;
            }


			sql="SELECT MAX(COREL) FROM D_FACT_LOG WHERE RUTA='"+gl.ruta+"' AND SERIE='"+fserie+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){

                DT.moveToFirst();

                ca2=DT.getInt(0);

            }else {
                ca2=0;
            }

		ca=ca1;if (ca2>ca) ca=ca2;
		fcorel=ca+1;

		if (fcorel>cf) {
			mu.msgbox("Se ha acabado el talonario de facturas. No se puede continuar con la venta.");
			fcorel=0;return;
		}

		//#HS_20181128_1602 Cambie el texto del mensaje.
		if (fcorel==cf) mu.msgbox("Esta es la última factura disponible.");

		lblFact.setText("Factura : "+fserie+" - "+fcorel);

		s="Talonario : "+fcorel+" / "+cf+"\n";
		s=s+"Disponible : "+(cf-fcorel);
		lblTalon.setText(s);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("assignCorel: " + e.getMessage());
        }

	}

	private boolean esProductoConStock(String prcodd) {
		Cursor DT;

		try {
			sql="SELECT TIPO FROM P_PRODUCTO WHERE CODIGO='"+prcodd+"'";
           	DT=Con.OpenDT(sql);

           	if(DT.getCount()>0){

                DT.moveToFirst();

                return DT.getString(0).equalsIgnoreCase("P");
            }else {
           	    return false;
            }


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("esProductoConStock: " + e.getMessage());
			return false;
	    }
	}

	//endregion

	//region Pago

	private void inputEfectivo() {
		try{
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Pago Efectivo");
			alert.setMessage("Monto a pagar");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(""+tot);
			input.requestFocus();

			showkeyb();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					peexit=false;
					sefect=input.getText().toString();
					closekeyb();
					applyCash();
					checkPago();
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

	private void inputVuelto() {

		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            if (gl.dvbrowse!=0){
                double totdv;
                if (tot>=dispventa){
                    totdv = mu.round(tot-dispventa,2);
                    alert.setTitle("A pagar : "+mu.frmcur(totdv));
                }
            }else{
                alert.setTitle("A pagar : "+mu.frmcur(tot));
            }

			alert.setMessage("Pagado con billete : ");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(txtVuelto.getParent()!= null){
				txtVuelto.setText("");
				((ViewGroup)txtVuelto.getParent()).removeView(txtVuelto);
			}

			if(lblVuelto.getParent()!= null){
				lblVuelto.setText("");
				((ViewGroup)lblVuelto.getParent()).removeView(lblVuelto);
			}

			layout.addView(txtVuelto);
			layout.addView(lblVuelto);lblVuelto.setTextSize(20);lblVuelto.setTextColor(Color.rgb(54,184,238));lblVuelto.setGravity(Gravity.LEFT);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			/*alert.setPositiveButton("Vuelto", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					double pg,vuel;

					peexit=false;
					svuelt=input.getText().toString();
					sefect=""+tot;

					try {
						pg=Double.parseDouble(svuelt);
						if (pg<tot) {
							msgbox("Monto menor que total");return;
						}

						vuel=pg-tot;
					} catch (NumberFormatException e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						msgbox("Monto incorrecto");return;
					}

					applyCash();
					if (vuel==0) {
						checkPago();
					} else {
						vuelto("Vuelto : "+mu.frmcur(vuel));
						//dialog.dismiss();
					}

				}
			});*/

			alert.setPositiveButton("Pagar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					peexit=false;
					svuelt= txtVuelto.getText().toString();
                    gl.brw=1;

                    if (!svuelt.equalsIgnoreCase("")){
                        if (Double.parseDouble(svuelt)<0.0){
                            toast("Monto ingresado no genera vuelto");
                        }
                    }

                    svuelt=""+tot;
                    sefect=""+tot;

					applyCash();
					checkPago();

				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					peexit=true;
					lblVuelto.setText("");
					txtVuelto.setText("");
					layout.removeAllViews();

				}
			});

			alert.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("InputVuelto: " + e.getMessage());
		}

	}

	public void Davuelto(){

		try{

			double pg,vuel;

			svuelt= txtVuelto.getText().toString();

			if (!svuelt.equals("")){

				pg=Double.parseDouble(svuelt);

				if (pg > 0) {

					if (gl.dvbrowse!=0){
						double totdv;
						totdv = mu.round(tot-dispventa,2);

						if (pg<totdv) {
							msgbox("Monto menor que total");
						}

						vuel=pg-tot;

						lblVuelto.setText(String.format("    Vuelto: " + mu.frmcur(vuel)) );

					}else{

						if (pg<tot) {
							msgbox("Monto menor que total");
						}

						vuel=pg-tot;

						lblVuelto.setText(String.format("    Vuelto: " + mu.frmcur(vuel)) );

					}

				}

			}

		}catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void vuelto(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);

			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					checkPago();
				}
			});
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void applyCash() {
		double epago;

		try {
			epago=Double.parseDouble(sefect);
			if (epago==0) return;

			if (epago<0) throw new Exception();

			//if (epago>plim) {
			//	MU.msgbox("Total de pago mayor que total de saldos.");return;
			//}

			//if (epago>tsel) {
			//	msgAskOverPayd("Total de pago mayor que saldo\nContinuar");return;
			//}

			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");

			ins.add("ITEM",1);
			ins.add("CODPAGO",1);
			ins.add("TIPO","E");

			if(gl.dvbrowse!=0){
				if (epago>=dispventa) {
					epago=mu.round(epago-dispventa,2);
				}
			}

			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");

		    db.execSQL(ins.sql());

			//msgAskSave("Aplicar pago y crear un recibo");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			inputEfectivo();
			mu.msgbox("Pago incorrecto"+e.getMessage());
	    }

	}

	private void inputCredito() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Pago Crédito");
			alert.setMessage("Valor a pagar");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			input.setText(""+tot);
			input.requestFocus();

			alert.setCancelable(false);
			showkeyb();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					peexit=false;
					sefect=input.getText().toString();
					closekeyb();
					applyCredit();
					checkPago();
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

	private void applyCredit() {
		double epago;

		try {
			epago=Double.parseDouble(sefect);
			if (epago==0) return;

			if (epago<0) throw new Exception();

			//if (epago>plim) {
			//	MU.msgbox("Total de pago mayor que total de saldos.");return;
			//}

			//if (epago>tsel) {
			//	msgAskOverPayd("Total de pago mayor que saldo\nContinuar");return;
			//}

			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);

			ins.init("T_PAGO");

			ins.add("ITEM",1);
			ins.add("CODPAGO",4);
			ins.add("TIPO","K");
			ins.add("VALOR",epago);
			ins.add("DESC1","");
			ins.add("DESC2","");
			ins.add("DESC3","");

		    db.execSQL(ins.sql());

			//msgAskSave("Aplicar pago y crear un recibo");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			inputEfectivo();
			mu.msgbox("Pago incorrecto"+e.getMessage());
	    }

	}

	private void inputCard() {

		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Número de tarjeta");

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setText("");input.requestFocus();

			showkeyb();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (checkNum(input.getText().toString())) addPagoTar();
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					closekeyb();
				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private boolean checkNum(String s) {

		try{
			if (mu.emptystr(s)) {
				showkeyb();
				inputCard();
				mu.msgbox("Número incorrecto");showkeyb();
				return false;
			}

			desc1=s;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("checkNum: " + e.getMessage());
		}

		return true;

	}

	private void addPagoTar(){

		sql="DELETE FROM T_PAGO";
		db.execSQL(sql);

		try {

			ins.init("T_PAGO");

			ins.add("ITEM",1);
			ins.add("CODPAGO",3);
			ins.add("TIPO","K");
			ins.add("VALOR",tot);
			ins.add("DESC1",desc1);
			ins.add("DESC2","");
			ins.add("DESC3","");

	    	db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		checkPago();

	}

	private void checkPago() {
		Cursor DT;
		double tpago;

		if (pagocompleto) return;

		try {

			sql="SELECT SUM(VALOR) FROM T_PAGO";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

                DT.moveToFirst();

                tpago=DT.getDouble(0);

            }else  {
			    tpago=0;
            }

		s=mu.frmcur(tpago);

        if (gl.dvbrowse!=0){
            if (gl.brw>0){
                lblPago.setText("Pago COMPLETO.\n"+s);
                pago=true;
				pagocompleto=true;
                //if (rutapos) askSavePos(); else askSave();
                finishOrder();
            }
        }	else{
            if (tpago<tot) {
                lblPago.setText("Pago incompleto.\n"+s);
                pago=false;
            } else {
                lblPago.setText("Pago COMPLETO.\n"+s);
                pago=true;
				pagocompleto=true;
                //if (rutapos) askSavePos(); else askSave();
                finishOrder();
            }
        }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox( e.getMessage());
        }

	}

	private String androidid() {
		String uniqueID="";
		try {
			uniqueID = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			uniqueID="0000000000";
		}

		return uniqueID;
	}

	//endregion

	//region Aux

	public void askSave(View view) {
		try{
			checkPago();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void askSave() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Guardar la factura?");

			dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finishOrder();
				}
			});

			dialog.setNegativeButton("Salir", null);

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void askSavePos() {
		double vuel;
		String sv="";

		try{
			try {
				vuel=Double.parseDouble(svuelt);

				if (vuel<tot) throw new Exception();

				if (vuel>tot) {
					vuel=vuel-tot;
					sv="Vuelto : "+mu.frmcur(vuel)+"\n\n";
				} else {
					sv="SIN VUELTO";
				}

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				sv="";
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage(sv+"¿Guardar la factura?");

			dialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finishOrder();
				}
			});

			dialog.setNegativeButton("Salir", null);

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//#HS_20181212 Dialogo para Pendiente de pago
	private void askPendientePago() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Está factura quedará PENDIENTE DE PAGO, deberá realizar el pago posteriormente. ¿Está seguro?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gl.cobroPendiente = true;
					finishOrder();
				}
			});

			dialog.setNegativeButton("Cancelar", null);
			dialog.setCancelable(false);
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

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gl.closeCliDet = true;
					gl.closeVenta = true;

					impres++;toast("Impres "+impres);

					try {
						if (!gl.cobroPendiente){
							sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
							db.execSQL(sql);
						}
					} catch (Exception e) {
						msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
					}

					try {
						sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+corelNC+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					if (impres>1) {

						try {
							if (!gl.cobroPendiente){
								sql="UPDATE D_FACTURA SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
								db.execSQL(sql);
							}
						} catch (Exception e) {
							msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
						}

						try {
							sql="UPDATE D_NOTACRED SET IMPRES=IMPRES+1 WHERE COREL='"+corelNC+"'";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						}

						gl.brw=0;
						FacturaRes.super.finish();
					} else {

						if (!gl.cobroPendiente) {
							fdoc.buildPrint(corel, 10,gl.peFormatoFactura);
						}else{
							fdoc.buildPrint(corel,4,gl.peFormatoFactura);
						}

						prn.printask(printcallback);

					}
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//singlePrint();
					//prn.printask(printcallback);
					finish();
				}
			});

			dialog.show();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	private void clearGlobals() {

		try {

			db.execSQL("DELETE FROM T_PAGO");

			db.execSQL("DELETE FROM T_BONITEM WHERE PRODID='*'");

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}
	
	private void checkPromo() {
		Cursor DT;
		
		imgBon.setVisibility(View.INVISIBLE);
		
		try {
			sql="SELECT ITEM FROM T_BONITEM";
           	DT=Con.OpenDT(sql);
			if (DT.getCount()>0) imgBon.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }
	}
	
	private void cliPorDia() {
		Cursor DT;
		
		int dweek=mu.dayofweek();
		
		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                clidia=DT.getCount();
            }else {
                clidia=0;
            }

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox("cliPorDia: " + e.getMessage() );
		}
			
	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			return false;
		}
	}

	private void hidekeyboard() {
		try{
			View sview = this.getCurrentFocus();

			if (sview != null) {
				InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(sview.getWindowToken(), 0);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
	
	//region Activity Events

	@Override
	protected void onResume() {
		try{
			super.onResume();

			checkPromo();
			checkPago();

			if (browse==1) {
				browse=0;
				if (gl.promapl) updDesc();
				return;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}	

	@Override
	public void onBackPressed() {
		try{
			clearGlobals();
			super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	//endregion

}
