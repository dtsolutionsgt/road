package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCD;
import com.dts.roadp.clsClasses.clsVenta;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

public class Venta extends PBase {

	private ListView listView;
	private TextView lblProd,lblPres,lblCant,lblPrec,lblTot,lblTit;
	private ImageView imgroad;
	
	private ArrayList<clsVenta> items;
	private ListAdaptVenta adapter;
	private clsVenta selitem;
	private Precio prc;

	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();
	
	private int browse;
	private String prodid,um;
	
	private double cant,desc,mdesc,prec,precsin,imp,impval;
	private double descmon,tot,totsin,percep,ttimp,ttperc,ttsin;
	private double px,py,cpx,cpy,cdist;

	private String emp,cliid,rutatipo;
	private int nivel,dweek,clidia;
	private boolean sinimp,rutapos;
	
	private DecimalFormat ffrmprec;
	
	// Location
	private LocationManager locationManager;
	private Location location;

	private LocationListener locationListener;

	private boolean isGPSEnabled,isNetworkEnabled,canGetLocation;
	private double  latitude,longitude;
	private String  cod;

	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long  MIN_TIME_BW_UPDATES = 1000; // in Milliseconds
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_venta);
		
		super.InitBase();
		
		setControls();
						
		emp=gl.emp;
		nivel=gl.nivel;
		cliid=gl.cliente;
		rutatipo=gl.rutatipo;
		rutapos=gl.rutapos;
		
		gl.atentini=du.getActDateTime();
		gl.ateninistr=du.geActTimeStr();
		
		
		if (rutatipo.equalsIgnoreCase("V")) {
			lblTit.setText("Venta");
			imgroad.setImageResource(R.drawable.pedidos_1_gray);
		} else {
			lblTit.setText("Preventa");
			imgroad.setImageResource(R.drawable.pedidos_3_gray);
		}
		if (rutapos) imgroad.setImageResource(R.drawable.pedidos_3_gray);
			
		prc=new Precio(this,mu,gl.peDec);
		ffrmprec = new DecimalFormat("#0.0000");
		
		setHandlers();
		
		items = new ArrayList<clsVenta>();
		
		initValues();
		
		browse=0;
		
		showCredit();
		
		setGPS();
		cliPorDia();

		validaNivelPrecio();

	}

	
	// Events
	
	public void showProd(View view) {
		gl.gstr="";
		browse=1;
		
		if (rutatipo.equalsIgnoreCase("P")) gl.prodtipo=0;else gl.prodtipo=1;
		
		
		Intent intent = new Intent(this,Producto.class);
		startActivity(intent);
	}	
	
	public void finishOrder(View view) {
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
				//Toast.makeText(this,"Desc    "+s, Toast.LENGTH_SHORT).show();
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
		
		
	}	
	
	public void showPromo(View view){
		gl.gstr="*";
		browse=3;
		
		Intent intent = new Intent(this,ListaPromo.class);
		startActivity(intent);
	}
		
	
	// Main
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				try {
					Object lvObj = listView.getItemAtPosition(position);
		           	clsVenta vItem = (clsVenta)lvObj;
		           	
					prodid=vItem.Cod;
					adapter.setSelectedIndex(position);
		    		
					setCant();
					
		        } catch (Exception e) {
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
					adapter.setSelectedIndex(position);
		    		
					msgAskDel("Borrar producto");
		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
		    	return true;
		      }
		});
		
	    
	}
	
	public void listItems() {
		Cursor DT;
		clsVenta item;	
		double tt;
			
		items.clear();tot=0;ttimp=0;ttperc=0;
		
		try {
			sql="SELECT T_VENTA.PRODUCTO, P_PRODUCTO.DESCCORTA, T_VENTA.TOTAL, T_VENTA.CANT, T_VENTA.PRECIODOC, T_VENTA.DES, T_VENTA.IMP, T_VENTA.PERCEP, T_VENTA.UM " +
				 "FROM T_VENTA INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_VENTA.PRODUCTO "+
				 "ORDER BY P_PRODUCTO.DESCCORTA ";	
			
			DT=Con.OpenDT(sql);
				
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					
					tt=DT.getDouble(2);
					
					item = clsCls.new clsVenta();
			  	
					item.Cod=DT.getString(0);
					item.Nombre=DT.getString(1);
					item.Cant=DT.getDouble(3);
					item.Prec=DT.getDouble(4);
					item.Desc=DT.getDouble(5);
					item.sdesc=mu.frmdec(item.Desc)+" %";
					item.imp=DT.getDouble(6);
					item.percep=DT.getDouble(7);
					item.um=DT.getString(8);
					
					item.val=mu.frmdecimal(item.Cant,gl.peDecImp)+" "+ltrim(item.um,6);
					
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
					
					DT.moveToNext();
				}	
			}
			
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptVenta(this, items);adapter.cursym=gl.peMon;
		listView.setAdapter(adapter);
		
		if (sinimp) {
			ttsin=tot-ttimp-ttperc;
			ttsin=mu.round(ttsin,gl.peDec);
			lblTot.setText(mu.frmcur(ttsin));
		} else {	
			tot=mu.round(tot,gl.peDec);
			lblTot.setText(mu.frmcur(tot));	
		}
		   	    
	}
	
	private void processItem(){
		Cursor DT;
		String pid;
		
		pid=gl.gstr;
		if (mu.emptystr(pid)) {return;}
		
		prodid=pid;
		um=gl.um;
		
		setCant();
	}
	
	private void setCant(){
		browse=2;
		
		gl.prod=prodid;
		Intent intent = new Intent(this,ProdCant.class);
		startActivity(intent);
	}
	
	private void processCant(){
		clsDescuento clsDesc;
		clsBonif clsBonif;
		Cursor DT;
		double cnt,vv;
		String s;
		
		cnt=gl.dval;
		
		if (cnt<0) return;
		
		try {
			sql="SELECT DESCCORTA FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
		
			lblProd.setText(DT.getString(0));
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }		

		cant=cnt;
		um=gl.um;
		
		lblCant.setText(mu.frmdecimal(cant,gl.peDecImp)+" "+ltrim(um,6));
		lblPres.setText("");
		
		
		// Bonificacion
		
		desc=0;
		prodPrecio();
	
		prec=mu.round(prec,gl.peDec);
		
		gl.bonprodid=prodid;
		gl.bonprodcant=cant;
		
		gl.bonus.clear();
		
		vv=cant*prec;vv=mu.round(vv,gl.peDec);
		
		clsBonif=new clsBonif(this,prodid,cant,vv);
		if (clsBonif.tieneBonif()) {
			//  lista de prod bonif
			for (int i = 0; i <clsBonif.items.size(); i++) {
				
				gl.bonus.add(clsBonif.items.get(i));
				
				//Toast.makeText(this,"Val tipolista  : "+clsBonif.items.get(i).tipolista, Toast.LENGTH_SHORT).show();
				//s=clsBonif.items.get(i).valor+"  "+clsBonif.items.get(i).lista;
				//Toast.makeText(this,s, Toast.LENGTH_SHORT).show();
			}
		}
		
		
		// Descuento por producto
		
		clsDesc=new clsDescuento(this,prodid,cant);
		
		desc=clsDesc.getDesc();
		mdesc=clsDesc.monto;
		
		//mu.msgbox(desc+"  ,  "+mdesc);
		
		if (desc+mdesc>0) {
			
			browse=3;
			gl.promprod=prodid;
			gl.promcant=cant;
			
			if (desc>0) {
				gl.prommodo=0;
				gl.promdesc=desc;
			} else {
				gl.prommodo=1;	
				gl.promdesc=mdesc;
			}
			
			Intent intent = new Intent(this,DescBon.class);
			startActivity(intent);
			
		} else {	
			
			if (gl.bonus.size()>0) {
				Intent intent = new Intent(this,BonList.class);
				startActivity(intent);
			}
			
		}
		
		//prodPrecio();
		
		prec=prc.precio(prodid,cant,nivel,um);
		
		precsin=prc.precsin;
		imp=prc.imp;
		impval=prc.impval;
		descmon=prc.descmon;
		tot=prc.tot;
		totsin=prc.totsin;
		percep=0;
		
		//Toast.makeText(this,"Impval : "+impval+" , prec sin : "+precsin+" tot sin  "+totsin, Toast.LENGTH_LONG).show();
		
		if (sinimp) lblPrec.setText(mu.frmcur(precsin));else lblPrec.setText(mu.frmcur(prec));
				
		addItem();
		
	}
	
	private void updDesc(){
		desc=gl.promdesc;
		prodPrecio();
		updItem();
	}
	
	private void prodPrecio() {		
		prec=prc.precio(prodid,cant,nivel,um);
		prec=mu.round(prec,gl.peDec);	
	}
	
	private void addItem(){
		Cursor dt;
		double precdoc,fact,cantbas;
		String umb;

		try {
			//sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"') AND (UM='"+um+"')";
			sql="DELETE FROM T_VENTA WHERE (PRODUCTO='"+prodid+"')";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
		
		try {
			sql="SELECT UNIDADMINIMA,FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			umb=dt.getString(0);
			fact=dt.getDouble(1);
		} catch (Exception e) {
			umb=um;fact=1;
		}
		
		cantbas=cant*fact;

		try {

			if (sinimp) precdoc=precsin; else precdoc=prec;

			ins.init("T_VENTA");

			ins.add("PRODUCTO",prodid);
			ins.add("EMPRESA",emp);
			ins.add("UM",gl.um);
			ins.add("CANT",cant);		
			if (rutatipo.equalsIgnoreCase("V")) ins.add("UMSTOCK",gl.umstock);else ins.add("UMSTOCK",gl.um);		
			if ((rutatipo.equalsIgnoreCase("P")) && (gl.umfactor==0)) gl.umfactor=1;
			ins.add("FACTOR",gl.umfactor);
			ins.add("PRECIO",prec);
			ins.add("IMP",impval);
			ins.add("DES",desc);
			ins.add("DESMON",descmon);
			ins.add("TOTAL",tot);
			ins.add("PRECIODOC",precdoc);
			ins.add("PESO",0);
			ins.add("VAL1",0);   
			ins.add("VAL2","");
			ins.add("VAL3",0);   
			ins.add("VAL4","");
			ins.add("PERCEP",percep);   

			db.execSQL(ins.sql());

		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	

		try {
			sql="DELETE FROM T_VENTA WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
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
			mu.msgbox("Error : " + e.getMessage());
		}	
		
    	listItems();
		
	}
	
	private void delItem(){	
		try {
	    	db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+prodid+"'");
	    	listItems();
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
	}
	
	
	// Aux
	
	private void setControls(){
		
		listView = (ListView) findViewById(R.id.listView1);
		
		lblProd= (TextView) findViewById(R.id.lblProd);
		lblPres= (TextView) findViewById(R.id.lblPres);
		lblCant= (TextView) findViewById(R.id.lblCant);
		lblPrec= (TextView) findViewById(R.id.lblPNum);
		lblTot= (TextView) findViewById(R.id.lblTot);	
		lblTit= (TextView) findViewById(R.id.txtRoadTit);	
		
		imgroad= (ImageView) findViewById(R.id.imgRoadTit);
	}
	
	private void initValues(){
		Cursor DT;
		String contrib;
		
		try {
			sql="SELECT INITPATH FROM P_EMPRESA WHERE EMPRESA='"+emp+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
		
			String sim=DT.getString(0);
			sinimp=sim.equalsIgnoreCase("S");
			
		} catch (Exception e) {
			sinimp=false;
	    }	
		
		contrib=gl.contrib;
		if (contrib.equalsIgnoreCase("C")) sinimp=true;
		if (contrib.equalsIgnoreCase("F")) sinimp=false;
		
		gl.sinimp=sinimp;
		
		//mu.msgbox(contrib+" // SINIMP "+sinimp);
		
		lblProd.setText("");
		lblPres.setText("");	
		
		try {
			sql="DELETE FROM T_VENTA";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
		
		try {
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
		
		try {
			sql="DELETE FROM T_BONIFFALT";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}		
		
		try {
			sql="DELETE FROM T_BONITEM";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		gl.ref1="";
		gl.ref2="";
		gl.ref3="";
				
		clsDescFiltro clsDFilt=new clsDescFiltro(this,gl.ruta,gl.cliente);
	
		clsBonFiltro  clsBFilt=new clsBonFiltro(this,gl.ruta,gl.cliente);
		
		//mu.msgbox("Filtro desc "+clsDFilt.estr);
		
		dweek=mu.dayofweek();
		
		lblPrec.setText(mu.frmcur(0));
		lblTot.setText(mu.frmcur(0));
		
	}
	
	private boolean hasProducts(){
		Cursor DT;
		
		try {
			sql="SELECT PRODUCTO FROM T_VENTA";	
			DT=Con.OpenDT(sql);
				
			return DT.getCount()>0;
		} catch (Exception e) {
			return false;
		}	
	}
	
	
	// No atencion
	
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
		   	mu.msgbox( e.getMessage());return;
	    }
			
		showAtenDialog();
		
	}
	
	public void showAtenDialog() {
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
	}
	
	private void setNoAtt(String scna){
		int cna;
		
		try {
			cna=Integer.parseInt(scna);
		} catch (SQLException e) {
			return;
		}
		
		String cliid=gl.cliente;
		
		try
		{
			upd.init("P_CLIRUTA");
			upd.add("BANDERA",cna);
			upd.Where("CLIENTE='"+cliid+"' AND DIA="+dweek);
			
			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		saveAtten(""+cna);
	}
	
	private void saveAtten(String codnoate) {
		int ti,tf,td;
		
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
			//mu.msgbox("Error : " + e.getMessage());
		}	
		
	}
	
	
	// Location
	
	private void setGPS() {

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
	}

	private void setGPSPos() {
		Cursor DT;
		int idist;

		latitude=0;longitude=0;cpx=0;cpy=0;cdist=-1.0;
		
		try {
			sql="SELECT COORX,COORY FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
			
           	DT=Con.OpenDT(sql);
			if (DT.getCount()>0) {
				DT.moveToFirst();
				
				cpx=DT.getDouble(0);
				cpy=DT.getDouble(1);
			}
		} catch (Exception e) {
	    }	

		try {
			getLocation();
		} catch (Exception e) {
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
				this.canGetLocation = false;
			} else {
				this.canGetLocation = true;
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
			return null;
		}

		return location;
	}

	
	
	// Aux
	
	private void msgAskExit(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	if (rutapos) doExit();else listAten();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}
	
	private void msgAskDel(String msg) {
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
			
	}
	
	private void showCredit(){
		if (hasCredits()){
			Intent intent = new Intent(this,Cobro.class);
			startActivity(intent);	
		}
	}
	
	private boolean hasCredits(){
		Cursor DT;
		
		try {
			sql="SELECT SALDO FROM P_COBRO WHERE CLIENTE='"+cliid+"'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()>0) return true;
		} catch (Exception e) {
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
			mu.msgbox(e.getMessage());
	    }			
	}
	
	private void doExit(){
		gl.closeCliDet=true;
		super.finish();
	}
	
	private void cliPorDia() {
		Cursor DT;
		
		int dweek=mu.dayofweek();
		
		try {
			sql="SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE (P_CLIRUTA.DIA ="+dweek+") ";
			DT=Con.OpenDT(sql);
			clidia=DT.getCount();
		} catch (Exception e) {
			clidia=0;
		}
			
	}
	
	public String ltrim(String ss,int sw) {
		int l=ss.length();
		if (l>sw) {
			ss=ss.substring(0,sw);	
		} else {
			String frmstr="%-"+sw+"s";	
			ss=String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	
	// Activity Events
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    if (gl.closeVenta) super.finish();
	    
	    if (browse==1) {
	    	browse=0;
	    	processItem();return;
	    }
	    
	    if (browse==2) {
	    	browse=0;
	    	processCant();return;
	    }
	    
	    if (browse==3) {
	    	browse=0;
	    	if (gl.promapl) updDesc();
	    	return;
	    }
	
	}
	
	@Override
	public void onBackPressed() {
		msgAskExit("Salir sin terminar venta");
	}			

}
