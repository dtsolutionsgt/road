package com.dts.roadp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PedidoRes extends PBase {

	private static final String CERO = "0";
	private static final String BARRA = "/";
	public final Calendar c = Calendar.getInstance();
	final int mes = c.get(Calendar.MONTH);
	final int dia = c.get(Calendar.DAY_OF_MONTH);
	final int anio = c.get(Calendar.YEAR);

	private ListView listView;
	private EditText txtDir;
	private EditText txtReferencia;
	private TextView lblFecha;
	private ImageView imgBon;
	private ImageView imgSave;
	private Spinner spinList;
	private ProgressBar pgSave;
	
	private List<String> spname = new ArrayList<String>();
	private ArrayList<clsClasses.clsCDB> items= new ArrayList<clsClasses.clsCDB>();
	private ListAdaptTotals adapter;
	
	private Runnable printcallback,printclose,printexit;
	
	private clsDescGlob clsDesc;
	private printer prn;
	private clsDocPedido pdoc;
	
	private long fecha,fechae;
	private String itemid,cliid,corel;
	private int cyear, cmonth, cday,dweek,impres;
	
	private double dmax,dfinmon,descpmon,descg,descgmon,tot,stot0,stot,descmon,totimp,totperc;
	private boolean acum,cleandprod;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pedido_res);
		
		super.InitBase();
		addlog("PedidoRes",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		txtDir = (EditText) findViewById(R.id.txtMonto);
		txtReferencia = (EditText) findViewById(R.id.txtReferencia);
		lblFecha = (TextView) findViewById(R.id.lblpSaldo);
		imgBon = (ImageView) findViewById(R.id.imgDevol);
		imgSave = (ImageView) findViewById(R.id.imgWhatsApp);
		pgSave = (ProgressBar) findViewById(R.id.pgSave);

		pgSave.setVisibility(View.INVISIBLE);
		imgSave.setVisibility(View.VISIBLE);

		spinList = (Spinner) findViewById(R.id.spinner1);
		
		cliid=gl.cliente;
		gl.pedsend = false;
		
		setActDate();
		fechae=fecha;
		lblFecha.setText(du.sfecha_yyyy(fechae));
		dweek=mu.dayofweek();
		
		clsDesc=new clsDescGlob(this);
		
		adjustSpinner();
		fillSpinner();
		
		descpmon=totalDescProd();
		
		dmax=clsDesc.dmax;
		acum=clsDesc.acum;

		processFinalPromo();
		
	    processFinalPromo();
		
		printcallback= new Runnable() {
		    public void run() {
		    	askPrint();
		    }
		};

		printclose= new Runnable() {
			public void run() {

			}
		};

		printexit= new Runnable() {
			public void run() {
				gl.pedsend=true;
				PedidoRes.super.finish();
			}
		};

		prn=new printer(this,printexit,gl.validimp);
		pdoc=new clsDocPedido(this,prn.prw,gl.peMon,gl.peDecImp, "");
		pdoc.deviceid =gl.numSerie;
	}
		
	
	// Events
	
	public void showBon(View view) {
		Intent intent = new Intent(this,BonVenta.class);
		startActivity(intent);	
	}
	
	
	// Main
	
	private void processFinalPromo(){
		
		descg=gl.descglob;
		
		descgmon=(double) (stot0*descg/100);
		totalOrder();
		
		if (descg>0) {
			try{
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						showPromo();
					}
				}, 300);

			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
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
			descgmon=(double) (stot0*descg/100);
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
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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
					item.Cod="Percepcion";item.Desc=mu.frmcur(totperc);item.Bandera=0;
					items.add(item);
				}
				
				item = clsCls.new clsCDB();
				item.Cod="Descuento";item.Desc=mu.frmcur(-descmon);item.Bandera=0;
				items.add(item);
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);					
				
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
				
				item = clsCls.new clsCDB();
				item.Cod="TOTAL";item.Desc=mu.frmcur(tot);item.Bandera=1;
				items.add(item);			
				
			}
					
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		adapter=new ListAdaptTotals(this,items);
		listView.setAdapter(adapter);
	}
	
 	private void finishOrder(){

		try{

			pgSave.setVisibility(View.VISIBLE);
			imgSave.setVisibility(View.INVISIBLE);

            fecha=du.cfechaSinHora(Integer.parseInt(lblFecha.getText().toString().substring(8,10)),
                    Integer.parseInt(lblFecha.getText().toString().substring(3,5)),
                    Integer.parseInt(lblFecha.getText().toString().substring(0,2)));

            if (!fechaValida()){
                mu.msgbox("La fecha de entrega ingresada no es válida, no se puede guardar el pedido");
                return;
            }

			if (!saveOrder()) return;

			clsBonifSave bonsave=new clsBonifSave(this,corel,"P");

			bonsave.ruta=gl.ruta;
			bonsave.cliente=gl.cliente;

			fecha = du.getActDate();

			bonsave.fecha=fecha;
			bonsave.emp=gl.emp;

			bonsave.save();

			if (gl.impresora.equalsIgnoreCase("S")) {
				String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
				pdoc.buildPrint(corel,0, vModo);
				prn.printask(printcallback);
			}

			gl.closeCliDet=true;
			gl.closeVenta=true;

			if (!gl.impresora.equalsIgnoreCase("S")) {
				gl.pedsend = true;
				super.finish();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
 	
	private void singlePrint() {
 		prn.printask(printcallback);
 	}
	
	private boolean saveOrder(){
		Cursor DT;
		double tot,desc,imp,peso;
		
		corel=gl.ruta+"_"+mu.getCorelBase();
		
		try {
			
			sql="SELECT SUM(TOTAL),SUM(DESMON),SUM(IMP),SUM(PESO) FROM T_VENTA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			tot=DT.getDouble(0);
			desc=DT.getDouble(1);
			imp=DT.getDouble(2);
			peso=DT.getDouble(3);
			
			db.beginTransaction();
			
			ins.init("D_PEDIDO");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",du.getActDateTime());
			ins.add("EMPRESA",gl.emp);
			ins.add("RUTA",gl.ruta);
			ins.add("VENDEDOR",gl.vend);
			ins.add("CLIENTE",gl.cliente);
			ins.add("KILOMETRAJE",0);
			ins.add("FECHAENTR",fechae);
			ins.add("DIRENTREGA",getCodigoSAP(txtDir.getText().toString()));
			ins.add("TOTAL",tot);
			ins.add("DESMONTO",descmon);
			ins.add("IMPMONTO",imp);
			ins.add("PESO",peso);
			ins.add("BANDERA","");
			ins.add("STATCOM","N");
			ins.add("CALCOBJ","N");
			ins.add("IMPRES",0);
			ins.add("ADD1",gl.media);//Se está guardando aquí la media de pago del cliente ya que La gran Fortuna lo necesita
			ins.add("ADD2","");
			ins.add("ADD3",txtReferencia.getText().toString());
			ins.add("STATPROC","");
			ins.add("RECHAZADO",0);
			ins.add("RAZON_RECHAZADO","");  // valor de percepcion 0 o xxx.xx
			ins.add("INFORMADO",0);
			ins.add("SUCURSAL",gl.sucur);//Se está guardando aquí la sucursal
			ins.add("ID_DESPACHO",0);
			ins.add("ID_FACTURACION",0);
		
			db.execSQL(ins.sql());
          		
			sql="SELECT PRODUCTO,CANT,PRECIO,IMP,DES,DESMON,TOTAL,PRECIODOC,PESO,VAL1,VAL2,UM,FACTOR,UMSTOCK FROM T_VENTA";
			DT=Con.OpenDT(sql);
	
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
			
			  	ins.init("D_PEDIDOD");
				ins.add("COREL",corel);
				ins.add("PRODUCTO",DT.getString(0));
				ins.add("EMPRESA",gl.emp);
				ins.add("ANULADO","N");
				ins.add("CANT",DT.getDouble(1));
				ins.add("PRECIO",DT.getDouble(7));//#CKFK 20190719 Guardé el precio sin impuesto en ambos campos DT.getDouble(2)
				ins.add("IMP",DT.getDouble(3));
				ins.add("DES",DT.getDouble(4));
				ins.add("DESMON",DT.getDouble(5));
				ins.add("TOTAL",DT.getDouble(6));
				ins.add("PRECIODOC",DT.getDouble(7));
				ins.add("PESO",DT.getDouble(8));
				ins.add("VAL1",DT.getDouble(9));
				ins.add("VAL2",DT.getString(10));
				ins.add("CANTPROC",0);
				ins.add("UMVENTA",DT.getString(11));
				ins.add("FACTOR",DT.getDouble(12));
				ins.add("UMSTOCK",DT.getString(13));
				ins.add("UMPESO","");
			
			    db.execSQL(ins.sql());
				
			    DT.moveToNext();
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			 
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox( e.getMessage());
		   	return false;
		}
		
		try {
			upd.init("P_CLIRUTA");
			upd.add("BANDERA",0);
			upd.Where("CLIENTE='"+cliid+"'");//AND DIA="+dweek  #CKFK 20191209 quité la condición del día para que siempre se actualice
	
			db.execSQL(upd.SQL());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		saveAtten();
		
		return true;
	}
	
	private double totalDescProd(){
		Cursor DT;
		
		try {
			sql="SELECT SUM(DESMON),SUM(TOTAL),SUM(IMP) FROM T_VENTA";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			tot=DT.getDouble(1);
			stot0=tot+DT.getDouble(0);
			
			totimp=DT.getDouble(2);
			
			return DT.getDouble(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			tot=0;
			mu.msgbox( e.getMessage());return 0;
		}	
		
	}
 	
	private void saveAtten() {
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
			ins.add("CLIPORDIA",0);
			ins.add("CODOPER","X");
			ins.add("COREL",corel);
			
			ins.add("SCANNED",gl.escaneo);
			ins.add("STATCOM","N");
			ins.add("LLEGO_COMPETENCIA_ANTES",0);
	
			db.execSQL(ins.sql());
		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//MU.msgbox("Error : " + e.getMessage());
		}	
		
	}

	private String getCodigoSAP(String vDireccion){
        String vSQL = "", vCodigoSAP = vDireccion;
		Cursor DT;

		try {

				vSQL = "SELECT COD_SAP FROM P_CLIDIR WHERE DIRECCION_ENTREGA = '" + vDireccion + "'";
				DT=Con.OpenDT(vSQL);
				DT.moveToFirst();

				if (DT.getCount()>0){
					vCodigoSAP = DT.getString(0);
				}

				DT.close();

		}catch (Exception ex){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),sql);
			mu.msgbox( ex.getMessage());
		}

		return vCodigoSAP;
	}
	// Date

	public void showDateDialog(View view) {
		try{
		/*DialogFragment newFragment = new DatePickerFragment();
	    //newFragment.show(getSupportFragmentManager(), "datePicker");*/
			obtenerFecha();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void obtenerFecha(){
		try{
			DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
					final int mesActual = month + 1;
					String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
					String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);
					lblFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);
				}
			},anio, mes, dia);

			recogerFecha.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setActDate(){
		try{
			final Calendar c = Calendar.getInstance();
			cyear = c.get(Calendar.YEAR);
			cmonth = c.get(Calendar.MONTH)+1;
			cday = c.get(Calendar.DAY_OF_MONTH);
			fecha=du.cfecha(cyear,cmonth,cday);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
	// Aux
	
	private void adjustSpinner(){

		try{
			spinList.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;
					String scod;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

						scod=spname.get(position);
						txtDir.setText(scod);

					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}

				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}	

	private void fillSpinner(){
		Cursor DT;
		
		spname.clear();
			
		try {
			sql="SELECT DIRECCION FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			spname.add(DT.getString(0));
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			spname.add("");txtDir.setText("");
			mu.msgbox( e.getMessage());
	    }		
		
		
		try {
			
			sql="SELECT DIRECCION_ENTREGA FROM P_CLIDIR WHERE CODIGO_CLIENTE='"+cliid+"' ORDER BY DIRECCION_ENTREGA";
			DT=Con.OpenDT(sql);
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
			  spname.add(DT.getString(0));
			  DT.moveToNext();
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spname);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		try {
			spinList.setAdapter(dataAdapter);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	
	}
	
	public void askSave(View view) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Guardar pedido ?");

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
	
	public void setDate(){
		lblFecha.setText(du.sfecha(fecha));
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
	
	private void askPrint() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Impresion correcta ?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					impres++;toast("Impres "+impres);

					try {
						sql="UPDATE D_PEDIDO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
						db.execSQL(sql);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					}

					if (impres>1) {

						try {
							sql="UPDATE D_PEDIDO SET IMPRES=IMPRES+1 WHERE COREL='"+corel+"'";
							db.execSQL(sql);
						} catch (Exception e) {
							addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						}

						gl.brw=0;
						gl.pedsend=true;
						finish();

					} else {
						String vModo=(gl.peModal.equalsIgnoreCase("TOL")?"TOL":"*");
						pdoc.buildPrint(corel,1,vModo);

						prn.printask(printclose);

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
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}

    public  boolean fechaValida(){
        boolean vFechaValida = false;

        try{

            if (fecha<du.getFechaActual()){
                vFechaValida= false;
            }else{
                fechae=fecha * 10000;
                vFechaValida= true;
            }

        }catch (Exception e){
            mu.msgbox("Ocurrió un error " + e.getMessage());
        }

        return  vFechaValida;
    }


	// Activity Events
	
	@Override
	protected void onResume() {
		try{
			super.onResume();

			checkPromo();

			if (browse==1) {
				browse=0;
				if (gl.promapl) updDesc();
				return;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	
	}	
	
	
}
