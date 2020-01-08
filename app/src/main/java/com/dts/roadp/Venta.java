package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.roadp.clsClasses.clsVenta;

import java.util.ArrayList;

public class Venta extends PBase {

	private ListView listView;
	private TextView lblProd,lblPres,lblCant,lblPrec,lblTot,lblTit;
	private EditText txtBarra;
	private ImageView imgroad,imgscan;
	private CheckBox chkBorrar;

	private ArrayList<clsVenta> items= new ArrayList<clsVenta>();
	private ListAdaptVenta adapter;
	private clsVenta selitem;
	private Precio prc;
	private PrecioTran prctr;

	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();

	private int browse,val;

	private double cant,desc,mdesc,prec,precsin,imp,impval;
	private double descmon,tot,totsin,percep,ttimp,ttperc,ttsin,prodtot;
	private double px,py,cpx,cpy,cdist;

	private String emp,cliid,rutatipo,prodid,um,tiposcan;
	private int nivel,dweek,clidia,scanc=0;
	private boolean sinimp,rutapos,softscanexist,porpeso,usarscan,contrans;

	private AppMethods app;

	// Location
	private LocationManager locationManager;
	private Location location;

	private LocationListener locationListener;

	private boolean isGPSEnabled,isNetworkEnabled;
	private double  latitude,longitude;
	private String  barcode;

	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long  MIN_TIME_BW_UPDATES = 1000; // in Milliseconds

	private Runnable scanCallBack;

	private boolean isDialogBarraShowed = false;

	private AlertDialog.Builder dialogBarra;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_venta);

		super.InitBase();
		addlog("Venta",""+du.getActDateTime(),gl.vend);

		setControls();

		emp=gl.emp;
		nivel=gl.nivel;
		cliid=gl.cliente;
		rutatipo=gl.rutatipo;
		rutapos=gl.rutapos;

		gl.atentini=du.getActDateTime();
		gl.ateninistr=du.geActTimeStr();

		app = new AppMethods(this, gl, Con, db);

		if (rutatipo.equalsIgnoreCase("V")) {
			lblTit.setText("Venta");
			imgroad.setImageResource(R.drawable.pedidos_1_gray);
		} else {
			lblTit.setText("Preventa");
			imgroad.setImageResource(R.drawable.pedidos_3_gray);
		}
		if (rutapos) imgroad.setImageResource(R.drawable.pedidos_3_gray);

		contrans=gl.pTransBarra;

		if (contrans) {
			prctr=new PrecioTran(this,mu,2,Con,db);
			//msgbox("Con transaccion");
		} else {
			//msgbox("Sin transaccion");
		}
		prc=new Precio(this,mu,2);

		setHandlers();

		initValues();

		browse=0;
		gl.closeVenta = false;

		showCredit();
		setGPS();
		cliPorDia();
		validaNivelPrecio();

		txtBarra.requestFocus();txtBarra.setText("");

		dialogBarra= new AlertDialog.Builder(this);

		scanCallBack= new Runnable() {
			public void run() {

				try {
					if (contrans) {
						addBarcodeTrans();
					} else {
						addBarcode();
					}
				} catch (Exception e) {
				}

				Handler handlerTimer = new Handler();
				handlerTimer.postDelayed(new Runnable() {
					public void run() {
						txtBarra.setText("");
					}
				}, 1000);
			}
		};
	}

	//region Events

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try{
			//if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				barcode=contents;
				//toast(barcode);
			}
			//}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void doFocus(View view) {
		try {
			txtBarra.requestFocus();
		} catch (Exception e) {}
	}

	public void showProd(View view) {
		try{
			gl.gstr="";
			browse=1;

			if (rutatipo.equalsIgnoreCase("P")) gl.prodtipo=0;else gl.prodtipo=1;

			Intent intent = new Intent(this,Producto.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void finishOrder(View view) {
		try{
			clsBonifGlob clsBonG;
			clsDeGlob clsDeG;
			String s,ss;

			if (!hasProducts()) {
				mu.msgbox("No puede continuar, no ha vendido ninguno producto !");return;
			}

			gl.gstr="";
			browse=1;

			// Descuentos

			gl.bonprodid="*";
			gl.bonus.clear();

			clsDeG=new clsDeGlob(this,tot);ss="";

			if (clsDeG.tieneDesc()) {

				gl.descglob=clsDeG.valor;
				gl.descgtotal=clsDeG.vmonto;

				for (int i = 0; i <clsDeG.items.size(); i++) {
					s=clsDeG.items.get(i).valor+" , "+clsDeG.items.get(i).lista;
					ss=ss+s+"\n";
				}
			}

			ss=ss+"acum : "+clsDeG.acum+" , limit "+clsDeG.maxlimit+"\n";
			ss=ss+"Valor : "+clsDeG.valor+"\n";
			ss=ss+"acum : "+clsDeG.valacum+"\n";
			ss=ss+"max : "+clsDeG.valmax+"\n";
			//mu.msgbox(ss);

			// Bonificacion

			gl.bonprodid="*";
			gl.bonus.clear();

			clsBonG=new clsBonifGlob(this,tot);
			if (clsBonG.tieneBonif()) {
				for (int i = 0; i <clsBonG.items.size(); i++) {
					//s=clsBonG.items.get(i).valor+"   "+clsBonG.items.get(i).tipolista+"  "+clsBonG.items.get(i).lista;
					//Toast.makeText(this,s, Toast.LENGTH_SHORT).show();
					gl.bonus.add(clsBonG.items.get(i));
				}
			} else {

			}

			if (gl.dvbrowse!=0){
				if (tot<gl.dvdispventa){
					mu.msgbox("No puede totalizar la factura, es menor al monto permitido para la nota de crédito: " + gl.dvdispventa);return;
				}
			}
			gl.brw=0;
			if (rutatipo.equalsIgnoreCase("V")) {
				Intent intent = new Intent(this,FacturaRes.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,PedidoRes.class);
				startActivity(intent);
			}

			//Toast.makeText(this,"Bon global "+clsBonG.items.size(), Toast.LENGTH_SHORT).show();

			if (gl.bonus.size()>0) {
				//Intent intent = new Intent(this,BonList.class);
				//startActivity(intent);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("finishOrder: "+e.getMessage());
		}

	}

	public void showPromo(View view){
		try{
			gl.gstr="*";
			browse=3;

			Intent intent = new Intent(this,ListaPromo.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void doSoftScan(View view) {

		//if (softscanexist) {
			try{
				browse=5;barcode="";

				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, 0);
			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
		//} else {
			//doFocus(view);
		//}
	}

	private void setHandlers(){
		try{

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					onBackPressed();
				}
				public void onSwipeLeft() {
					finishOrder(null);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsVenta vItem = (clsVenta)lvObj;

						prodid=vItem.Cod;
						gl.nuevoprecio=0;
						adapter.setSelectedIndex(position);

						//#CKFK 20190517 Agregué la validación de que esta pantalla solo se levanta cuando sea venta directa
						if (prodBarra(prodid) && gl.rutatipo.equalsIgnoreCase("V")) {
							gl.gstr=prodid;
							gl.gstr2=vItem.Nombre;
							browse=4;
							startActivity(new Intent(Venta.this,RepesajeLista.class));
						} else {
							setCant();
						}
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
				};
			});

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

					try {

						Object lvObj = listView.getItemAtPosition(position);
						clsVenta vItem = (clsVenta)lvObj;

						prodid=vItem.Cod;
						gl.nuevoprecio=0;
						adapter.setSelectedIndex(position);

						//if (prodBarra(prodid)) return true;
						//#CKFK 20190517 Agregué la validación de que esta pantalla solo se levanta cuando sea venta directa
						if (prodRepesaje(prodid) && gl.rutatipo.equalsIgnoreCase("V")) {
							gl.gstr=prodid;
							gl.gstr2=vItem.Nombre;
							showItemMenu();
						} else {
							msgAskDel("Borrar producto");
						}
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;
				}
			});
		}catch (Exception e)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		txtBarra.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

			public void onTextChanged(CharSequence s, int start, int before, int count) 			{
/*
				//scanc++;lblTit.setText("Scan : "+scanc);
				Log.d("ElHuevo","Negro");

				barcode = txtBarra.getText().toString();

				if (barcode.length()>=10) {
					txtBarra.requestFocus();
					if (!mu.emptystr(barcode)) scanCallBack.run();
				}

				Handler handlerTimer = new Handler();
				handlerTimer.postDelayed(new Runnable() {
					public void run() {
							txtBarra.requestFocus();
					}
				}, 300);
*/
//				Handler handlerTimer = new Handler();
//				handlerTimer.postDelayed(new Runnable() {
//					public void run() {
//						barcode = txtBarra.getText().toString();
//						if (barcode.length()>=10)
//						{
//							txtBarra.requestFocus();
//							if (!mu.emptystr(barcode)) scanCallBack.run();
//						}
//					}
//				}, 300);
			}
		});

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			barcode = txtBarra.getText().toString().trim();

			Handler handlerTimer = new Handler();
			handlerTimer.postDelayed(new Runnable() {
				public void run() {
					txtBarra.setText("");
				}
			}, 20);

			lblTit.setText(barcode);

			if (!isDialogBarraShowed)	{
				if (!mu.emptystr(barcode)) 	scanCallBack.run();
			}

			Handler handlerTimer2 = new Handler();
			handlerTimer2.postDelayed(new Runnable() {
				public void run() {
					txtBarra.requestFocus();
				}
			}, 100);
		}
		return super.dispatchKeyEvent(e);
	}

	//endregion

	//region Main

	public void listItems() {
		Cursor DT;
		clsVenta item;
		double tt;
		int ii;

		items.clear();tot=0;ttimp=0;ttperc=0;selidx=-1;ii=0;

		try {
			sql="SELECT T_VENTA.PRODUCTO, P_PRODUCTO.DESCCORTA, T_VENTA.TOTAL, T_VENTA.CANT, "+
				"T_VENTA.PRECIODOC, T_VENTA.DES, T_VENTA.IMP, T_VENTA.PERCEP, T_VENTA.UM, T_VENTA.PESO, T_VENTA.UMSTOCK, T_VENTA.PRECIO  " +
				 "FROM T_VENTA INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_VENTA.PRODUCTO "+
				 "ORDER BY T_VENTA.VAL1 DESC";

			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					tt=DT.getDouble(2);

					item = clsCls.new clsVenta();

					item.Cod=DT.getString(0);if (item.Cod.equalsIgnoreCase(prodid)) selidx=ii;
					item.Nombre=DT.getString(1);
					item.Cant=DT.getDouble(3);
					item.Prec=DT.getDouble(4);
					item.Desc=DT.getDouble(5);
					item.sdesc=mu.frmdec(item.Desc)+" %";
					item.imp=DT.getDouble(6);
					item.percep=DT.getDouble(7);
					if (prodPorPeso(item.Cod)) 	{
						item.um=DT.getString(10);
					} else {
						//item.um=DT.getString(8);
						item.um=app.umVenta(item.Cod);
					}
					item.Peso=DT.getDouble(9);
					item.precio=DT.getDouble(11);

					item.val=mu.frmdecimal(item.Cant,gl.peDecImp)+" "+ltrim(item.um,6);
					if (gl.usarpeso) {
						item.valp=mu.frmdecimal(item.Peso,gl.peDecImp)+" "+ltrim(gl.umpeso,6);
					} else {
						item.valp=".";
					}

					if (sinimp) {
						ttsin=tt-item.imp-item.percep;
						item.Total=ttsin;
					} else {
						item.Total=tt;
					}

					items.add(item);

					tot+=tt;
					ttimp+=item.imp;
					ttperc+=item.percep;

					DT.moveToNext();ii++;
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }

		adapter=new ListAdaptVenta(this, items);adapter.cursym=gl.peMon;
		listView.setAdapter(adapter);

		if (sinimp) {
			ttsin=tot-ttimp-ttperc;
			ttsin=mu.round(ttsin,2);
			lblTot.setText(mu.frmcur(ttsin));
		} else {
			tot=mu.round(tot,2);
			lblTot.setText(mu.frmcur(tot));
		}


		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.smoothScrollToPosition(selidx);
		}

	}

	private void processItem(){
		try{

			String pid;

			pid=gl.gstr;
			if (mu.emptystr(pid)) {return;}

			prodid=pid;
			gl.bonprodid=prodid;
			um=gl.um;

			setCant();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setCant(){
		try{
			browse=2;

			gl.prod=prodid;
			Intent intent = new Intent(this,ProdCant.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void processCant(){
		clsDescuento clsDesc;
		clsBonif clsBonif;
		Cursor DT = null;
		double cnt,vv;
		String s;

		cnt = gl.dval;

		if (cnt < 0) return;

		try {
			try {
				sql = "SELECT CODIGO,DESCCORTA FROM P_PRODUCTO WHERE CODIGO='" + prodid + "'";
				DT = Con.OpenDT(sql);
				DT.moveToFirst();
				lblProd.setText(DT.getString(0) +" - "+ DT.getString(1));

			} catch (Exception e) {
				mu.msgbox(e.getMessage());
				addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			}finally {
				if(DT!=null) DT.close();
			}

			cant = cnt;
			um = gl.umpres;

			lblCant.setText(mu.frmdecimal(cant, gl.peDecImp) + " " + ltrim(um, 6));
			lblPres.setText("");


			// Bonificacion

			// Borra la anterior, si existe
			sql="DELETE FROM T_BONITEM WHERE Prodid='"+prodid+"'";
			db.execSQL(sql);

			desc = 0;
			prodPrecio();

			prec = mu.round(prec, 2);
			gl.bonprodcant = cant;
			gl.bonus.clear();

			vv = cant * prec;vv = mu.round(vv, 2);

			clsBonif = new clsBonif(this, prodid, cant, vv);
			if (clsBonif.tieneBonif()) {
				for (int i = 0; i < clsBonif.items.size(); i++) {
					gl.bonus.add(clsBonif.items.get(i));
				}
			}

			// Descuento

			clsDesc = new clsDescuento(this, prodid, cant);
			desc = clsDesc.getDesc();
			mdesc = clsDesc.monto;

			if (desc + mdesc > 0) {

				browse = 3;
				gl.promprod = prodid;
				gl.promcant = cant;

				if (desc > 0) {
					gl.prommodo = 0;
					gl.promdesc = desc;
				} else {
					gl.prommodo = 1;
					gl.promdesc = mdesc;
				}

				startActivity(new Intent(this, DescBon.class));

			} else {
				if (gl.bonus.size() > 0) {
					Intent intent = new Intent(this, BonList.class);
					startActivity(intent);
				}
			}

			//prodPrecio();

			if (prodPorPeso(prodid)) {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, gl.dpeso,um, gl.nuevoprecio);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			} else {
				prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, 0,um, gl.nuevoprecio);
				if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
					if (prc.precioespecial>0) prec=prc.precioespecial;
				}
			}

			precsin = prc.precsin;
			imp = prc.imp;
			impval = prc.impval;
			descmon = prc.descmon;
			tot = prc.tot;
			prodtot = tot;
			totsin = prc.totsin;
			percep = 0;

			//Toast.makeText(this,"Impval : "+impval+" , prec sin : "+precsin+" tot sin  "+totsin, Toast.LENGTH_LONG).show();

			if (sinimp) lblPrec.setText(mu.frmcur(precsin));
			else lblPrec.setText(mu.frmcur(prec));

			addItem();
		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void updDesc(){
		try{
			desc=gl.promdesc;
			prodPrecio();
			updItem();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void prodPrecio() {
		try{
			prec=prc.precio(prodid,cant,nivel,um,gl.umpeso,gl.dpeso,um, gl.nuevoprecio);

            if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
                if (prc.precioespecial>0) prec=prc.precioespecial;
            }

			prec=mu.round(prec,2);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void addItem(){
		Cursor dt,DT;
		double precdoc,fact,cantbas,peso;
		String umb;

		try {
			//sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"') AND (UM='"+um+"')";
			sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="SELECT UNIDADMINIMA,FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			umb=dt.getString(0);
			fact=dt.getDouble(1);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			umb=um;fact=1;
		}

		cantbas=cant*fact;
		//peso=mu.round(cant*fact,gl.peDec);

		porpeso=prodPorPeso(prodid);
		if (porpeso) {
			peso=mu.round(gl.dpeso,gl.peDec);
		} else {
			peso=mu.round(gl.dpeso*gl.umfactor,gl.peDec);
		}

		if (porpeso) {
			prodtot=mu.round(gl.prectemp*peso,2);
		} else {
			prodtot=mu.round(prec*cant,2);
		}

		gl.umstock=app.umStock(prodid);

		try {

            if (sinimp) precdoc=precsin; else precdoc=prec;

			ins.init("T_VENTA");

			ins.add("PRODUCTO",prodid);
			ins.add("EMPRESA",emp);
			if (porpeso) ins.add("UM",gl.umpeso);else ins.add("UM",gl.umpres);
			ins.add("CANT",cant);
			if (rutatipo.equalsIgnoreCase("V")) {
				ins.add("UMSTOCK",gl.umstock);
			}else {
				ins.add("UMSTOCK",gl.um);
			}
			if ((rutatipo.equalsIgnoreCase("P")) && (gl.umfactor==0)) gl.umfactor=1;
			ins.add("FACTOR",gl.umfactor);
			if (rutatipo.equalsIgnoreCase("P")) {
				ins.add("PRECIO",precdoc);
			}else{
				if (porpeso) ins.add("PRECIO",gl.prectemp); else ins.add("PRECIO",prec);
			}
			ins.add("IMP",impval);
			ins.add("DES",desc);
			ins.add("DESMON",descmon);
			ins.add("TOTAL",prodtot);

            if (rutatipo.equalsIgnoreCase("P")) {
				ins.add("PRECIODOC",precdoc);
			}else{
				if (porpeso) ins.add("PRECIODOC",gl.prectemp); else ins.add("PRECIODOC",precdoc);
			}

			ins.add("PESO",peso);

			try {
				sql="SELECT MAX(VAL1) FROM T_VENTA";
				DT=Con.OpenDT(sql);

				if(DT.getCount()==0) val=0; else val = DT.getInt(0) + 1;

				DT.moveToFirst();

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			}

			ins.add("VAL1",val);
			ins.add("VAL2","");
			ins.add("VAL3",0);
			ins.add("VAL4","");
			ins.add("PERCEP",percep);

			db.execSQL(ins.sql());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_VENTA WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		listItems();

	}

	private void updItem(){

		try {

			upd.init("T_VENTA");

			upd.add("PRECIO",prec);
			upd.add("IMP",imp);
			upd.add("DES",desc);
			upd.add("DESMON",descmon);
			upd.add("TOTAL",tot);
			upd.add("PRECIODOC",prec);

			upd.Where("PRODUCTO='"+prodid+"'");

	    	db.execSQL(upd.SQL());

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

    	listItems();

	}

	private void delItem(){
		try {
	    	db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+prodid+"'");
	    	listItems();
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}
	}

	//endregion

	//region Barras

	private void addBarcode() 	{
		int bbolsa;

		if (!isDialogBarraShowed) 	{

			if (barcode.length()>gl.pLongitudBarra){

			    if (gl.pPrefijoBarra.length()>0)
			        barcode=gl.pPrefijoBarra+barcode;

				gl.barra=barcode.substring(1,18);
				barcode=gl.barra;

			}else{
				gl.barra=barcode;
			}

			try {

				opendb();

				//db.beginTransaction();

				if (barraBonif()) {
					toastlong("¡La barra es parte de bonificacion!");
					//db.setTransactionSuccessful();
					//db.endTransaction();
					txtBarra.setText("");return;
				}else{
					//db.endTransaction();
				}

				bbolsa=barraBolsa();
				if (bbolsa==1) {

					txtBarra.setText("");
					listItems();

					return;
				} else if (bbolsa==-1) {
					toast("Barra vendida");
					return;
				}


				//db.beginTransaction();

				if (barraProducto()) {
					txtBarra.setText("");
					//db.beginTransaction();
					return;
				}else{
					//db.beginTransaction();
				}

				toast("¡La barra "+barcode+" no existe!");

			} catch (Exception e) {
				msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
				Log.d("VENTA","trans fail "+e.getMessage());
			}

			txtBarra.setText("");
			txtBarra.requestFocus();
		} else {
			toastlong("¡Conteste la pregunta por favor!");
			txtBarra.setText("");
			txtBarra.requestFocus();
		}

	}

	private void addBarcodeTrans() 	{
		int bbolsa;

		if (!isDialogBarraShowed) 	{

			if (barcode.length()>18){
				gl.barra=barcode.substring(1,18);
				barcode=gl.barra;
			}else{
				gl.barra=barcode;
			}

			try {

				opendb();

				db.beginTransaction();

				if (barraBonif()) {
					toastlong("¡La barra es parte de bonificacion!");
					db.setTransactionSuccessful();
					db.endTransaction();
					txtBarra.setText("");return;
				}else{
					db.endTransaction();
				}

				bbolsa=barraBolsaTrans();
				if (bbolsa==1) {

					txtBarra.setText("");
					listItems();

					return;
				} else if (bbolsa==-1) {
					toast("Barra vendida");
					return;
				}

				db.beginTransaction();

				if (barraProducto()) {
					txtBarra.setText("");
					db.beginTransaction();
					return;
				}else{
					db.beginTransaction();
				}

				toast("¡La barra "+barcode+" no existe!");

			} catch (Exception e) {
				msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
				Log.d("VENTA","trans fail "+e.getMessage());
			}

			txtBarra.setText("");
			txtBarra.requestFocus();
		} else {
			toastlong("¡Conteste la pregunta por favor!");
			txtBarra.setText("");
			txtBarra.requestFocus();
		}

	}

	private int barraBolsa() {
		Cursor dt, DT;
		double ppeso=0,pprecdoc=0,factbolsa;
		String uum,umven,uunistock;
		boolean isnew=true;

		porpeso=true;

			try {

				//db.beginTransaction();

				sql="SELECT CODIGO,CANT,PESO,UNIDADMEDIDA " +
						"FROM P_STOCKB WHERE (BARRA='"+barcode+"') ";
				dt=Con.OpenDT(sql);

				if (dt.getCount()==0) {
					sql="SELECT Barra FROM D_FACTURA_BARRA  WHERE (BARRA='"+barcode+"') ";
					dt=Con.OpenDT(sql);
					//db.endTransaction();
					if (dt.getCount()==0) {
						if(dt!=null) dt.close();
						return 0;
					}else{
						if(dt!=null) dt.close();
						return -1;
					}
				}

				dt.moveToFirst();

				prodid = dt.getString(0);
				cant = dt.getInt(1);
				ppeso = dt.getDouble(2);
				uum = dt.getString(3);

				if(dt!=null) dt.close();

				um=uum;
				umven=app.umVenta(prodid);
				factbolsa=app.factorPres(prodid,umven,um);
				cant=cant*factbolsa;

				//if (sinimp) precdoc=precsin; else precdoc=prec;
				/*
				if (prodPorPeso(prodid)) {
					prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, ppeso,umven);
					if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,ppeso)) {
						if (prctr.precioespecial>0) prec=prctr.precioespecial;
					}
				} else {
					prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, 0,umven);
					if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,0)) {
						if (prctr.precioespecial>0) prec=prctr.precioespecial;
					}
				}   */

				if (prodPorPeso(prodid)) {
					prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, ppeso,umven, gl.nuevoprecio);
					if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,ppeso)) {
						if (prc.precioespecial>0) prec=prc.precioespecial;
					}
				} else {
					prec = prc.precio(prodid, cant, nivel, um, gl.umpeso, 0,umven, gl.nuevoprecio);
					if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,0)) {
						if (prc.precioespecial>0) prec=prc.precioespecial;
					}
				}

				//if (prodPorPeso(prodid)) prec=mu.round2(prec/ppeso);
				if (prodPorPeso(prodid)) prec=mu.round2(prec);

				if (prec==0){
					msgbox("El producto no tiene precio definido para nivel de precio " + gl.nivel);
					return 0;
				}

				pprecdoc = prec;

				prodtot = prec;

				if (factbolsa>1) prodtot = cant*prec;
				if (prodPorPeso(prodid)) prodtot=mu.round2(prec*ppeso);

				try {

					ins.init("T_BARRA");
					ins.add("BARRA",barcode);
					ins.add("CODIGO",prodid);
					ins.add("PRECIO",prodtot);
					ins.add("PESO",ppeso);
					ins.add("PESOORIG",ppeso);
					ins.add("CANTIDAD",cant);
					db.execSQL(ins.sql());
					//toast(barcode);

				} catch (Exception e) 	{

					Log.d("Err_AF20190702",e.getMessage());

					isnew=false;

					if (chkBorrar.isChecked()) {
						borraBarra();
						//db.setTransactionSuccessful();
						//db.endTransaction();
						return 1;
					} else 	{
						if (!isDialogBarraShowed)	{

							txtBarra.setText("");

							isDialogBarraShowed = true;

							dialogBarra.setTitle(R.string.app_name);
							dialogBarra.setMessage("Borrar la barra \n"+ barcode  + "\n ?");
							dialogBarra.setIcon(R.drawable.ic_quest);

							dialogBarra.setPositiveButton("Si", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									isDialogBarraShowed = false;
									borraBarra();
									try {
										//db.setTransactionSuccessful();
									} catch (Exception ee) {
										String er=ee.getMessage();
									}
								}
							});

							dialogBarra.setNegativeButton("No", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									txtBarra.setText("");
									txtBarra.requestFocus();
									isDialogBarraShowed = false;
								}
							});

							dialogBarra.show();
							txtBarra.requestFocus();

						} else {
							Log.d("CerrarDialog","vos");
							isDialogBarraShowed=false;
						}

//						msgAskBarra("Borrar la barra "+barcode);
						try {
							//db.endTransaction();
						} catch (Exception e1) {
						}
						return 1;
					}
				}

				prec=mu.round(prec,2);
				prodtot=mu.round(prodtot,2);

				ins.init("T_VENTA");
				ins.add("PRODUCTO",prodid);
				ins.add("EMPRESA",emp);

				if (prodPorPeso(prodid)) {
					ins.add("UM",gl.umpeso);//ins.add("UM",gl.umpeso);
				} else {
					if (factbolsa==1) ins.add("UM",umven);else ins.add("UM",umven);
				}

				ins.add("CANT",cant);

				if (prodPorPeso(prodid)) uunistock=um; else uunistock=umven;
				if (prodid.equalsIgnoreCase("0006") || prodid.equalsIgnoreCase("0629") || prodid.equalsIgnoreCase("0747") ) {
					Double stfact;

					uunistock=um;
					umven=app.umVenta(prodid);
					stfact=app.factorPres(prodid,uunistock,umven);
					ins.add("FACTOR",stfact);
				} else {
					ins.add("FACTOR",gl.umfactor);
				}

				ins.add("UMSTOCK",uunistock);

				if (prodPorPeso(prodid)) {
					//ins.add("PRECIO",gl.prectemp);
					ins.add("PRECIO",prec);
				} else {
					ins.add("PRECIO",prec);
				}

				ins.add("IMP",0);
				ins.add("DES",0);
				ins.add("DESMON",0);
				ins.add("TOTAL",prodtot);

				if (prodPorPeso(prodid)) {
					//ins.add("PRECIODOC",gl.prectemp);
					ins.add("PRECIODOC",pprecdoc);
				} else {
					ins.add("PRECIODOC",pprecdoc);
				}

				ins.add("PESO",ppeso);

				try {
					sql="SELECT MAX(VAL1) FROM T_VENTA";
					DT=Con.OpenDT(sql);

					if(DT.getCount()==0) val=0; else val = DT.getInt(0) + 1;

					DT.moveToFirst();

				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				}

				ins.add("VAL1",0);
				ins.add("VAL2","");
				ins.add("VAL3",0);
				ins.add("VAL4","");
				ins.add("PERCEP",percep);

				try {
					db.execSQL(ins.sql());
				} catch (SQLException e) {
					Log.d(e.getMessage(),"");
				}

				actualizaTotalesBarra();

				if (isnew) validaBarraBon();

				//db.setTransactionSuccessful();
				//db.endTransaction();

				return 1;

			} catch (Exception e) {
			//	msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			//	addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				Log.d("Err_On_Insert",e.getMessage());
				//db.endTransaction();
				return 0;
			}

	}

	private int barraBolsaTrans() {
		Cursor dt, DT;
		double ppeso=0,pprecdoc=0,factbolsa;
		String uum,umven,uunistock;
		boolean isnew=true;

		porpeso=true;

		try {

			db.beginTransaction();

			sql="SELECT CODIGO,CANT,PESO,UNIDADMEDIDA " +
					"FROM P_STOCKB WHERE (BARRA='"+barcode+"') ";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) {
				sql="SELECT Barra FROM D_FACTURA_BARRA  WHERE (BARRA='"+barcode+"') ";
				dt=Con.OpenDT(sql);

				db.endTransaction();

				if (dt.getCount()==0) {
					return 0;
				}else{
					return -1;
				}
			}

			dt.moveToFirst();

			prodid = dt.getString(0);
			cant = dt.getInt(1);
			ppeso = dt.getDouble(2);
			uum = dt.getString(3);

			if(dt!=null) dt.close();

			um=uum;
			umven=app.umVenta(prodid);
			factbolsa=app.factorPres(prodid,umven,um);
			cant=cant*factbolsa;

			//if (sinimp) precdoc=precsin; else precdoc=prec;

			if (prodPorPeso(prodid)) {
				prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, ppeso,umven);
				if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,ppeso)) {
					if (prctr.precioespecial>0) prec=prctr.precioespecial;
				}
			} else {
				prec = prctr.precio(prodid, cant, nivel, um, gl.umpeso, 0,umven);
				if (prctr.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,uum,gl.umpeso,0)) {
					if (prctr.precioespecial>0) prec=prctr.precioespecial;
				}
			}

			if (prodPorPeso(prodid)) prec=mu.round2(prec/ppeso);
			pprecdoc = prec;

			prodtot = prec;

			if (factbolsa>1) prodtot = cant*prec;
			if (prodPorPeso(prodid)) prodtot=mu.round2(prec*ppeso);

			try {

				ins.init("T_BARRA");
				ins.add("BARRA",barcode);
				ins.add("CODIGO",prodid);
				ins.add("PRECIO",prodtot);
				ins.add("PESO",ppeso);
				ins.add("PESOORIG",ppeso);
				ins.add("CANTIDAD",cant);
				db.execSQL(ins.sql());
				//toast(barcode);

			} catch (Exception e) 	{

				Log.d("Err_AF20190702",e.getMessage());

				isnew=false;

				if (chkBorrar.isChecked()) {
					borraBarra();
					db.setTransactionSuccessful();
					db.endTransaction();
					return 1;
				} else 	{
					if (!isDialogBarraShowed)	{

						txtBarra.setText("");

						isDialogBarraShowed = true;

						dialogBarra.setTitle(R.string.app_name);
						dialogBarra.setMessage("Borrar la barra \n"+ barcode  + "\n ?");
						dialogBarra.setIcon(R.drawable.ic_quest);

						dialogBarra.setPositiveButton("Si", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								isDialogBarraShowed = false;
								borraBarra();
								try {
									db.setTransactionSuccessful();
								} catch (Exception ee) {
									String er=ee.getMessage();
								}
							}
						});

						dialogBarra.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								txtBarra.setText("");
								txtBarra.requestFocus();
								isDialogBarraShowed = false;
							}
						});

						dialogBarra.show();
						txtBarra.requestFocus();

					} else {
						Log.d("CerrarDialog","vos");
						isDialogBarraShowed=false;
					}

//						msgAskBarra("Borrar la barra "+barcode);
					try {
						db.endTransaction();
					} catch (Exception e1) {
					}
					return 1;
				}
			}

			prec=mu.round(prec,2);
			prodtot=mu.round(prodtot,2);

			ins.init("T_VENTA");
			ins.add("PRODUCTO",prodid);
			ins.add("EMPRESA",emp);

			if (prodPorPeso(prodid)) {
				ins.add("UM",gl.umpeso);//ins.add("UM",gl.umpeso);
			} else {
				if (factbolsa==1) ins.add("UM",umven);else ins.add("UM",umven);
			}

			ins.add("CANT",cant);

			if (prodPorPeso(prodid)) uunistock=um; else uunistock=umven;
			if (prodid.equalsIgnoreCase("0006") || prodid.equalsIgnoreCase("0629") || prodid.equalsIgnoreCase("0747") ) {
				Double stfact;

				uunistock=um;
				umven=app.umVenta(prodid);
				stfact=app.factorPres(prodid,uunistock,umven);
				ins.add("FACTOR",stfact);
			} else {
				ins.add("FACTOR",gl.umfactor);
			}

			ins.add("UMSTOCK",uunistock);

			if (prodPorPeso(prodid)) {
				//ins.add("PRECIO",gl.prectemp);
				ins.add("PRECIO",prec);
			} else {
				ins.add("PRECIO",prec);
			}

			ins.add("IMP",0);
			ins.add("DES",0);
			ins.add("DESMON",0);
			ins.add("TOTAL",prodtot);

			if (prodPorPeso(prodid)) {
				//ins.add("PRECIODOC",gl.prectemp);
				ins.add("PRECIODOC",pprecdoc);
			} else {
				ins.add("PRECIODOC",pprecdoc);
			}

			ins.add("PESO",ppeso);

			try {
				sql="SELECT MAX(VAL1) FROM T_VENTA";
				DT=Con.OpenDT(sql);

				if(DT.getCount()==0) val=0; else val = DT.getInt(0) + 1;

				DT.moveToFirst();

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			}

			ins.add("VAL1",0);
			ins.add("VAL2","");
			ins.add("VAL3",0);
			ins.add("VAL4","");
			ins.add("PERCEP",percep);

			try {
				db.execSQL(ins.sql());
			} catch (SQLException e) {
				Log.d(e.getMessage(),"");
			}

			actualizaTotalesBarra();

			if (isnew) validaBarraBon();

			db.setTransactionSuccessful();
			db.endTransaction();

			return 1;

		} catch (Exception e) {
			//	msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			//	addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			Log.d("Err_On_Insert",e.getMessage());
			//db.endTransaction();
			return 0;
		}

	}

	private void actualizaTotalesBarra() {
		Cursor dt;
		int ccant;
		double ppeso,pprecio,unfactor,stfact;

		try {

			sql="SELECT Factor FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			unfactor=dt.getDouble(0);

			//sql="SELECT COUNT(BARRA),SUM(PESO),SUM(PRECIO) FROM T_BARRA WHERE CODIGO='"+prodid+"'";
			//#CKFK 20190410 se modificó esta consulta para sumar la cantidad y no contar las barras
			sql="SELECT SUM(CANTIDAD),SUM(PESO),SUM(PRECIO) FROM T_BARRA WHERE CODIGO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			ccant=0;ppeso=0;pprecio=0;

			if (dt.getCount()>0) {
				dt.moveToFirst();

				ccant=dt.getInt(0);
				ppeso=dt.getDouble(1);
				pprecio=dt.getDouble(2);
			}

			if (prodid.equalsIgnoreCase("0006") || prodid.equalsIgnoreCase("0629") || prodid.equalsIgnoreCase("0747") ) {
				stfact=ccant;
				stfact=stfact/unfactor;
				ccant=(int) stfact;
			}

			if(dt!=null) dt.close();

			sql="UPDATE T_VENTA SET Cant="+ccant+",Peso="+ppeso+",Total="+pprecio+" WHERE PRODUCTO='"+prodid+"'";
			db.execSQL(sql);

			sql="DELETE FROM T_VENTA WHERE Cant=0";
			db.execSQL(sql);

			listItems();
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private boolean barraProducto() {
		Cursor dt;

		try {

			sql="SELECT P_STOCK.CODIGO " +
				"FROM P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO	" +
				"WHERE (P_PRODUCTO.CODBARRA='"+barcode+"') ";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();

				gl.gstr=dt.getString(0);gl.um="UN";
				processItem();
				if(dt!=null) dt.close();
				return true;
			}

		} catch (Exception e) {
            Log.d("Error en barraBonif",e.getMessage());
			//msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return false;
	}

	private void borraBarra() {
		//clsBonifTran clsBoniftr;
		clsBonif clsBoniftr;
		int bcant,bontotal,boncant,bfaltcant,bon;
		String bprod="";

		try {
			db.execSQL("DELETE FROM T_BARRA WHERE BARRA='"+gl.barra+"' AND CODIGO='"+prodid+"'");
			Log.d("BARRA","Borrar barra");
			actualizaTotalesBarra();

			gl.bonbarprod=prodid;

			bcant=cantBolsa();
			boncant=cantBonif();
			bfaltcant=cantFalt();

			//clsBoniftr = new clsBonifTran(this, prodid, bcant, 0,Con,db);
			clsBoniftr = new clsBonif(this, prodid, bcant, 0);
			if (clsBoniftr.tieneBonif()) {
				bon=(int) clsBoniftr.items.get(0).valor;
				bprod=clsBoniftr.items.get(0).lista;
				gl.bonbarid=clsBoniftr.items.get(0).lista;
			} else {
				bon=0;gl.bonbarid="";
			}

			bontotal=boncant+bfaltcant;

			//toast("Bolsas : "+bcant+" bon : "+bon+"  / "+bontotal);
			if (bon<bontotal) {
				removerBonif(bprod,(bontotal-bon));
			}
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private boolean barraBonif() {
		Cursor dt;

		try {

			sql="SELECT PRODUCTO FROM T_BARRA_BONIF WHERE (BARRA='"+barcode+"')";
			dt=Con.OpenDT(sql);

			//return dt.getCount()>0;

			if (dt!=null)return dt.getCount()>0;
			else return false;

		} catch (Exception e) {
			Log.d("Error en barraBonif",e.getMessage());
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			return false;
		}
		//return true;
	}

	private void validaBarraBon() {
		clsBonif clsBonif;
		int bcant,bontotal,boncant,bfaltcant,bon;

		gl.bonbarprod=prodid;

		bcant=cantBolsa();
		boncant=cantBonif();
		bfaltcant=cantFalt();

		clsBonif = new clsBonif(this, prodid, bcant, 0);
		if (clsBonif.tieneBonif()) {
			bon=(int) clsBonif.items.get(0).valor;
			gl.bonbarid=clsBonif.items.get(0).lista;
		} else {
			bon=0;gl.bonbarid="";
		}

		bontotal=boncant+bfaltcant;

		//toast("Bolsas : "+bcant+" bon : "+bon+"  / "+bontotal);
		if (bon>bontotal) startActivity(new Intent(this,BonBarra.class));

	}

	private int cantBolsa() {
		try {
			sql="SELECT BARRA FROM T_BARRA WHERE CODIGO='"+prodid+"'";
			Cursor dt=Con.OpenDT(sql);
			return dt.getCount();
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			return 0;
		}
	}

	private int cantBonif() {
		try {
			sql="SELECT BARRA FROM T_BARRA_BONIF WHERE PRODUCTO='"+prodid+"'";
			Cursor dt=Con.OpenDT(sql);
			return dt.getCount();
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return 0;
		}
	}

	private int cantFalt() {
		try {
			sql="SELECT PRODID FROM T_BONIFFALT WHERE PRODUCTO='"+prodid+"'";
			Cursor dt=Con.OpenDT(sql);
			return dt.getCount();
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return 0;
		}
	}

	private void removerBonif(String bprod,int bcant) {
		Cursor dt;
		String barra,sbarra="";
		int bc=0;

		try {
			for (int i = 1; i == bcant; i++) {

				sql = "SELECT CANT FROM T_BONIFFALT WHERE (PRODID='"+prodid+"') ";
				dt = Con.OpenDT(sql);

				if (dt.getCount() > 0) {
					dt.moveToFirst();

					sql="UPDATE T_BONIFFALT SET CANT=CANT-1 WHERE (PRODID='"+prodid+"') ";
					db.execSQL(sql);

					sql="DELETE FROM T_BONIFFALT WHERE CANT=0";
					db.execSQL(sql);

				} else {

					sql = "SELECT BARRA FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"') ";
					dt = Con.OpenDT(sql);

					if (dt.getCount() > 0) {
						dt.moveToLast();
						barra=dt.getString(0);sbarra+=barra+"\n";bc++;

						sql = "DELETE FROM T_BARRA_BONIF WHERE (PRODUCTO='"+prodid+"') AND (BARRA='"+barra+"') ";
						db.execSQL(sql);
					}
				}
			}

			reportBonif();

			if (bc>0) msgbox("Las barra devueltas : \n"+sbarra);

		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}
	}

	private void reportBonif() {
		int bont,bon,bonf;

		bon=cantBonif();
		bonf=cantFalt();
		bont=bon+bonf;

		if (bonf==0) {
			toast("Bonificación actual : "+bon);
		} else {
			toast("Bonificación actual : "+bon+" / "+bont);
		}

	}

	//endregion

	//region No atencion

	private void listAten(){
		Cursor DT;
		String code,name;

		lcode.clear();lname.clear();

		try {

			sql="SELECT Codigo,Nombre FROM P_CODATEN ORDER BY Nombre";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				try {
					code=String.valueOf(DT.getInt(0));
					name=DT.getString(1);

					lcode.add(code);
					lname.add(name);
				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
				DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());return;
	    }

		showAtenDialog();

	}

	public void showAtenDialog() {
		try{
			final AlertDialog Dialog;

			final String[] selitems = new String[lname.size()];
			for (int i = 0; i < lname.size(); i++) {
				selitems[i] = lname.get(i);
			}

			mMenuDlg = new AlertDialog.Builder(this);
			mMenuDlg.setTitle("Razón de no atencion");

			mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					try {
						String s=lcode.get(item);
						setNoAtt(s);
						doExit();
					} catch (Exception e) {
					}
				}
			});

			mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			Dialog = mMenuDlg.create();
			Dialog.show();

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setNoAtt(String scna){
		int cna;

		try {
			cna=Integer.parseInt(scna);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return;
		}

		String cliid=gl.cliente;

		try
		{
			upd.init("P_CLIRUTA");
			upd.add("BANDERA",cna);
			upd.Where("CLIENTE='"+cliid+"'");// AND DIA="+dweek #CKFK 20191209 Quité la condición del día

			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		saveAtten(""+cna);
	}

	private void saveAtten(String codnoate) {
		long ti,tf,td;

		ti=gl.atentini;tf=du.getActDateTime();
		td=du.timeDiff(tf,ti);if (td<1) td=1;

		try {
			ins.init("D_ATENCION");

			ins.add("RUTA",gl.ruta);
			ins.add("FECHA",ti);
			ins.add("HORALLEG",du.shora(ti)+":00");
			ins.add("HORASAL",du.shora(tf)+":00");
			ins.add("TIEMPO",td);

			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("DIAACT",du.dayofweek(ti));
			ins.add("DIA",du.dayofweek(ti));
			ins.add("DIAFLAG","S");

			ins.add("SECUENCIA",1);
			ins.add("SECUENACT",1);
			ins.add("CODATEN",codnoate);
			ins.add("KILOMET",0);

			ins.add("VALORVENTA",0);
			ins.add("VALORNEXT",0);
			ins.add("CLIPORDIA",clidia);
			ins.add("CODOPER","X");
			ins.add("COREL","");

			if (gl.gpspass) ins.add("SCANNED","G");else ins.add("SCANNED",gl.escaneo);
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
			//mu.msgbox("Error : " + e.getMessage());
		}

	}

	//endregion

	//region Location

	private void setGPS() {

		try{
			locationListener = new LocationListener() {

				@Override
				public void onLocationChanged(Location arg0) {
				}

				@Override
				public void onProviderDisabled(String arg0) {}

				@Override
				public void onProviderEnabled(String arg0)  {}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

			};

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setGPSPos();
				}
			}, 500);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setGPSPos() {

		Cursor DT = null;
		int idist;

		latitude=0;longitude=0;cpx=0;cpy=0;cdist=-1.0;

		try {

			sql="SELECT COORX,COORY FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";

			opendb();

           	DT=Con.OpenDT(sql);

           	if (DT.getCount()>0)
           	{
				DT.moveToFirst();

				cpx=DT.getDouble(0);
				cpy=DT.getDouble(1);
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }

		try {
			getLocation();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			latitude=0;longitude=0;
		}

		px=latitude;py=longitude;

		if ((px+py!=0) && (cpx+cpy!=0)) {
			float[] results = new float[1];
			Location.distanceBetween(cpx,cpy,latitude,longitude, results);
			cdist=results[0];
		}

		idist=(int) cdist;cdist=idist;

		gl.gpspx=px;
		gl.gpspy=px;
		gl.gpscpx=cpx;
		gl.gpscpy=cpy;
		gl.gpscdist=cdist;

	}

	@SuppressLint("MissingPermission")
	public Location getLocation() {

		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGPSEnabled) {
			} else {
				Toast.makeText(this,"GPS Deshabilitado !", Toast.LENGTH_SHORT).show();
			}

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {

			} else {

				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return null;
		}

		return location;
	}

	//endregion

	//region Messages

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(gl.dvbrowse!=0){
						gl.dvbrowse =0;
					}
					if (rutapos) doExit();else listAten();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskDel(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");
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

	private void msgAskBarra(String msg)
	{
		try
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg  + " ?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					borraBarra();
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

	//endregion

	//region Aux

	private void showItemMenu() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Repesaje","Borrar"};

			AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
			menudlg.setTitle("Producto venta");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
						case 0:
							browse=4;
							startActivity(new Intent(Venta.this,RepesajeLista.class));break;
						case 1:
							msgAskDel("Borrar producto");break;
					}

					dialog.cancel();
				}
			});

			menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			Dialog = menudlg.create();
			Dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setControls(){

		try{
			
			listView = (ListView) findViewById(R.id.listView1);
			lblProd= (TextView) findViewById(R.id.lblProd);
			lblPres= (TextView) findViewById(R.id.lblPres);
			lblCant= (TextView) findViewById(R.id.lblCant);
			lblPrec= (TextView) findViewById(R.id.lblPNum);
			lblTot= (TextView) findViewById(R.id.lblTot);
			lblTit= (TextView) findViewById(R.id.txtRoadTit);

			imgroad= (ImageView) findViewById(R.id.imgRoadTit);
			imgscan= (ImageView) findViewById(R.id.imageView13);imgscan.setVisibility(View.INVISIBLE);

			chkBorrar= (CheckBox) findViewById(R.id.checkBox1);chkBorrar.setVisibility(View.INVISIBLE);

			txtBarra=(EditText) findViewById(R.id.editText6);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void initValues(){
		Cursor DT;
		String contrib;

		tiposcan="*";

		try {
			sql="SELECT TIPO_HH FROM P_ARCHIVOCONF WHERE RUTA='"+gl.ruta+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();

				tiposcan=DT.getString(0);
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tiposcan="*";msgbox(e.getMessage());
		}

		usarscan=false;softscanexist=false;
		if (!mu.emptystr(tiposcan)) {
			if (tiposcan.equalsIgnoreCase("SOFTWARE")) {
				softscanexist=detectBarcodeScanner();
				usarscan=true;
			}
			if (!tiposcan.equalsIgnoreCase("SIN ESCANER")) usarscan=true;
		}

		if (usarscan) {
			imgscan.setVisibility(View.VISIBLE);
			chkBorrar.setVisibility(View.VISIBLE);
		} else {
			imgscan.setVisibility(View.INVISIBLE);
		}

		try {
			sql="SELECT INITPATH FROM P_EMPRESA WHERE EMPRESA='"+emp+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			String sim=DT.getString(0);
			sinimp=sim.equalsIgnoreCase("S");

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sinimp=false;
		}

		contrib=gl.contrib;
		if (contrib.equalsIgnoreCase("C")) sinimp=true;
		if (contrib.equalsIgnoreCase("F")) sinimp=false;

		gl.sinimp=sinimp;

		lblProd.setText("");
		lblPres.setText("");

		try {
			sql="DELETE FROM T_VENTA";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA";
			db.execSQL(sql);

			sql="DELETE FROM T_BARRA_BONIF";
			db.execSQL(sql);

			sql="DELETE FROM T_BONIFFALT";
			db.execSQL(sql);

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_BONIFFALT";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		try {
			sql="DELETE FROM T_BONITEM";
			db.execSQL(sql);
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}

		gl.ref1="";
		gl.ref2="";
		gl.ref3="";

		clsDescFiltro clsDFilt=new clsDescFiltro(this,gl.ruta,gl.cliente);

		clsBonFiltro  clsBFilt=new clsBonFiltro(this,gl.ruta,gl.cliente);

		dweek=mu.dayofweek();

		lblPrec.setText(mu.frmcur(0));
		lblTot.setText(mu.frmcur(0));

		System.gc();
	}

	private boolean hasProducts(){
		Cursor DT;

		try {
			sql="SELECT PRODUCTO FROM T_VENTA";
			DT=Con.OpenDT(sql);

			return DT.getCount()>0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
	}

	private void showCredit(){
		try{
			if (hasCredits()){
				Intent intent = new Intent(this,Cobro.class);
				startActivity(intent);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private boolean hasCredits(){
		Cursor DT;
		
		try {
			sql="SELECT SALDO FROM P_COBRO WHERE CLIENTE='"+cliid+"'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()>0) return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
	    }			
		
		return false;
	}
	
	private void validaNivelPrecio(){
		Cursor DT;
		int np;
		
		np=gl.nivel;
		
		try {
			sql="SELECT CODIGO FROM P_PRODPRECIO WHERE NIVEL="+np;
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) msgbox("No existen los precios para nivel de precio del cliente");
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
	    }			
	}
	
	private void doExit(){
		try{
			gl.closeCliDet=true;
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void cliPorDia() {
		Cursor DT;
		
		int dweek=mu.dayofweek();
		
		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);
			clidia=DT.getCount();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			clidia=0;
		}
			
	}
	
	public String ltrim(String ss,int sw) {
		try{
			int l=ss.length();
			if (l>sw) {
				ss=ss.substring(0,sw);
			} else {
				String frmstr="%-"+sw+"s";
				ss=String.format(frmstr,ss);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return ss;
	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

	private boolean prodBarra(String prodid) {
		try {
			return app.prodBarra(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

	private boolean prodRepesaje(String prodid) {
		try {
			return app.ventaRepesaje(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

	private boolean detectBarcodeScanner() {

		String packagename="com.google.zxing.client.android";
		PackageManager pm = this.getPackageManager();

		try {
			pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			toast("Aplicacion ZXing Barcode Scanner no esta instalada");return false;
		}

	}

	//endregion

	//region Activity Events
	
	@Override
	protected void onResume() {
		try{
			super.onResume();
			try {
				txtBarra.requestFocus();
			} catch (Exception e) {

			}

			if (gl.closeVenta) super.finish();

			if (browse==1) {
				browse=0;processItem();return;
			}

			if (browse==2) {
				browse=0;processCant();return;
			}

			if (browse==3) {
				browse=0;if (gl.promapl) updDesc();return;
			}

			if (browse==4) {
				browse=0;listItems();return;
			}

			if (browse==5) {
				browse=0;addBarcode();return;
			}

		}catch (Exception e)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	@Override
	public void onBackPressed() {
		try{
			msgAskExit("Salir sin terminar venta");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

}
