package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;

public class Cobro extends PBase {

	private ListView listView;
	private TextView lblSel,lblPag,lblPend;
	
	private ArrayList<clsClasses.clsCobro> items= new ArrayList<clsClasses.clsCobro>();
	private ListAdaptCobro adapter;
	private clsClasses.clsCobro selitem;	
	
	private Runnable printcallback,printclose;
	private printer prn;
	private clsDocCobro fdoc;
	
	private String cliid,itemid,prodid,sefect,corel,fserie,tipo;
	private double ttot,tsel,tpag,tpend,vefect,plim;
	private boolean peexit;
	private int fflag=1,fcorel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cobro);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
		lblSel = (TextView) findViewById(R.id.textView7);
		lblPag = (TextView) findViewById(R.id.lblCEmit);
		lblPend = (TextView) findViewById(R.id.TextView02);
		
		cliid=gl.cliente;
		
		setHandlers();
		
		initSession();

		listItems();
		clearAll();

		showTotals();

		gl.pagomodo=0;
		
		printcallback= new Runnable() {
		    public void run() {
		    	askPrint();
		    }
		};
		
		printclose= new Runnable() {
		    public void run() {
		    	Cobro.super.finish();
		    }
		};
		
		prn=new printer(this,printclose);
		fdoc=new clsDocCobro(this,prn.prw,gl.peMon,gl.peDecImp);
					
	}


	// Events
	
	public void paySelect(View view) {

		calcSelected();

		if (tsel==0) {
			mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
		}

		gl.pagomodo=0;
		gl.pagoval=tsel;
		gl.pagolim=plim;
		gl.pagocobro=true;
		browse=1;
		
		Intent intent = new Intent(this,Pago.class);
		startActivity(intent);	
	}
	
	public void payCash(View view) {

		calcSelected();

		if (tsel==0) {
			mu.msgbox("Total a pagar = 0, debe seleccionar un documento");return;
		}
		
		inputEfectivo();  
	}
	
	public void doExit(View view){
		exit();
	}

	public void checkAll(View view) {
        for (int i = 0; i <items.size(); i++) {
            items.get(i).flag=1;
        }

        adapter.refreshItems();

        calcSelected();
        showTotals();
	}

	public void checkNone(View view) {

		for (int i = 0; i <items.size(); i++) {
			items.get(i).flag=0;
		}

		adapter.refreshItems();

		calcSelected();
		showTotals();
	}

	public void sinRef(View view) {
		gl.pagomodo=1;
		gl.pagoval=0;
		gl.pagolim=0;
		gl.pagocobro=true;
		browse=1;

		Intent intent = new Intent(this,Pago.class);
		startActivity(intent);

	}

	private void setHandlers(){

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int flag;
				try {
					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCobro vItem = (clsClasses.clsCobro) lvObj;

					adapter.setSelectedIndex(position);

					tipo=vItem.Tipo;
					if (tipo.equalsIgnoreCase("R")) clearAll();

					flag = vItem.flag;
					if (flag == 0) flag = 1;
					else flag = 0;
					vItem.flag = flag;

					adapter.refreshItems();

					calcSelected();
					showTotals();

				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
			}

			;
		});
	}


	// Main

	private void listItems(){
		Cursor DT;
		clsClasses.clsCobro vItem;	
		double pg,sal,ssal;
				
		items.clear();ttot=0;tpag=0;

		try {
			sql="SELECT DOCUMENTO,TIPODOC,VALORORIG,SALDO,FECHAEMIT,FECHAV " +
				 "FROM P_COBRO WHERE CLIENTE='"+cliid+"' ORDER BY FECHAV";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
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
				vItem.fini=du.sfecha(DT.getInt(4));
				vItem.ffin=du.sfecha(DT.getInt(5));
			 
				ttot=ttot+DT.getDouble(3);
				tpag+=pg;
				
				items.add(vItem);	
			 
				DT.moveToNext();
			}
			
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());
	    }
			 
		adapter=new ListAdaptCobro(this,items);adapter.cursym=gl.peMon;
		listView.setAdapter(adapter);
		
		calcSelected();
		showTotals();
	}	
	
	private void createDoc(){
		if (gl.pagomodo==0) {
			docList();
			if (!applyPay()) return;
		}


	if (saveCobro()) {
			listItems();
			if (prn.isEnabled()) {
				fdoc.buildPrint(corel,0);
				prn.printask(printclose);
			}
		}

	}
	
	private boolean saveCobro(){
		Cursor DT;
		double tpago;
		String doc="";
		
		if (!assignCorel()) return false;

		corel=correlativo_factura();
		
		try {
			
			db.beginTransaction();
			
			// Encabezado
			
			sql="SELECT SUM(VALOR) FROM T_PAGO";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();		
			tpago=DT.getDouble(0);
			
			ins.init("D_COBRO");
			ins.add("COREL",corel);
			ins.add("ANULADO","N");
			ins.add("FECHA",du.getActDate());
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

				sql="SELECT DOCUMENTO,TIPODOC,MONTO,PAGO FROM T_PAGOD";
				DT=Con.OpenDT(sql);

				DT.moveToFirst();
				while (!DT.isAfterLast()) {

					ins.init("D_COBROD");

					ins.add("COREL",corel);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",gl.emp);
					ins.add("DOCUMENTO",DT.getString(0));doc=DT.getString(0);
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
		
			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				if (mu.emptystr(tipo)) {
					ins.init("D_COBROP");
					ins.add("COREL",corel);
				} else {
					if (tipo.equalsIgnoreCase("R")) {
						ins.init("D_FACTURAP");
						ins.add("COREL",doc);
					} else {
						ins.init("D_COBROP");
						ins.add("COREL",corel);
					}

				}



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
			   
			db.setTransactionSuccessful();
			
			db.endTransaction();

			if (tipo.equalsIgnoreCase("R")){

				db.beginTransaction();

				sql="DELETE FROM D_COBRO WHERE COREL='"+corel+"'";
				db.execSQL(sql);

				sql="DELETE FROM D_COBROD WHERE COREL='"+corel+"'";
				db.execSQL(sql);

				sql="DELETE FROM D_COBROP WHERE COREL='"+corel+"'";
				db.execSQL(sql);

				sql="DELETE FROM P_COBRO WHERE DOCUMENTO='"+doc+"'";
				db.execSQL(sql);

				db.setTransactionSuccessful();

				db.endTransaction();

			}

		} catch (Exception e) {
			db.endTransaction();
		   	mu.msgbox(e.getMessage());return false;
		}
		
		return true;
	}

	private String correlativo_factura(){

		Cursor DT;

		try{

			sql="SELECT SERIE,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'" ;
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			int cor=DT.getInt(1)+1;
			return DT.getString(0)+cor;

		} catch (Exception e) {
			return gl.ruta+"_"+mu.getCorelBase();
		}
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
		   	mu.msgbox(e.getMessage());
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
			
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());return false;
	    }
		
	}
	
	
	// Pago Efectivo
	
	private void inputEfectivo() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Pago Efectivo");
		alert.setMessage("Valor a pagar");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText(""+mu.round2(tsel));
		input.requestFocus();
		
		showkeyb();
		
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
	}
	
	private void checkCash() {
		double epago;
		
		try {
			epago=Double.parseDouble(sefect);
			if (epago==0) return;
			
			if (epago<0) throw new Exception();
			
			if (epago>plim) {
				mu.msgbox("Total de pago mayor que total de saldo.");return;
			}
			
			if (epago>tsel) {
				msgAskOverPayd("Total de pago mayor que saldo\nContinuar");return;
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
			
			msgAskSave("Aplicar pago y crear un recibo");
			
		} catch (Exception e) {
			inputEfectivo(); 
			mu.msgbox("Pago incorrecto"+e.getMessage());	   	
	    }
		
	}
	
	
	// Impresion
	
	private void singlePrint() {
 		prn.printask(printcallback);
 	}
	
	
	
	// Aux
	
	private void showTotals(){
		lblSel.setText(mu.frmcur(tsel));
		lblPag.setText(mu.frmcur(tpag));
		
		tpend=tsel-tpag;
		plim=ttot-tpag;
		
		if (tpend>=0) {
			lblPend.setText(mu.frmcur(tpend));
		} else {	
			lblPend.setText(mu.frmcur(0));
		}

	}
	
	private void calcSelected() {
		clsClasses.clsCobro vItem;
		Object lvObj;
		int flag,dc;
		double val;
		
		tsel=0;
		
		try {
			dc=adapter.getCount();
		} catch (Exception e) {
		   return;
		}
			
		for (int i = 0; i < dc; i++ ) {
			lvObj = listView.getItemAtPosition(i);
			vItem = (clsClasses.clsCobro)lvObj;
				
			flag=vItem.flag;
			if (flag==1) {
				val=vItem.Saldo;
				tsel+=val;
			}
		}		
		
	}

	private void clearAll() {
		for (int i = 0; i < items.size(); i++ ) {
			items.get(i).flag=0;
		}
	}

	private double getDocPago(String doc,String tipo){
		Cursor DT;
		double tp;
				
		try {
			sql="SELECT SUM(PAGO) FROM D_COBROD "+
		         "WHERE (ANULADO='N') AND (DOCUMENTO='"+doc+"') AND (TIPODOC='"+tipo+"') ";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			tp=DT.getDouble(0);
		} catch (Exception e) {
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
		   	mu.msgbox(e.getMessage());
	    }
	}
	
	private boolean assignCorel(){
		Cursor DT;
		int ca,ci,cf,ca1,ca2;
		
		fcorel=0;fserie="";
			
		try {
			sql="SELECT SERIE,INICIAL,FINAL,ACTUAL FROM P_CORRELREC WHERE RUTA='"+gl.ruta+"'";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			fserie=DT.getString(0);
			ci=DT.getInt(1);
			cf=DT.getInt(2);
			ca=DT.getInt(3);
			
		} catch (Exception e) {
			fcorel=0;fserie="";
			mu.msgbox("No esta definido correlativo de recibos.");return false;
		}	
					
		fcorel=ca+1;		
		if (fcorel>cf) toast("Se ha acabado el talonario de los recibos.");
		
		return true;
	}
	
	private void askPrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Road");
		dialog.setMessage("Impresión correcta ?");
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {	
					Cobro.super.finish();		
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	singlePrint();
		    }
		});
		
		
		dialog.show();
			
	}

	private void exit() {
		showTotals();
		if(tpend>0) {
			msgAskExit("Tiene documentos pendientes de pago. Salir");
		} else {
			finish();
		}
	}

	
	// MsgDialogs
	
	private void msgAskOverPayd(String msg) {
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
			
	}	
	
	private void msgAskSave(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿" + msg + "?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	createDoc();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	closekeyb();
		    }
		});
		
		dialog.show();
			
	}

	private void msgAskExit(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿" + msg + "?");

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

	}


	// Activity Events
	
	@Override
 	protected void onResume() {
	    super.onResume();
	    
	    if (browse==1) {
	    	browse=0;
	    	//closekeyb();
	    	
	    	if (gl.pagado) createDoc();
	    }
	    
	}

	@Override
	public void onBackPressed() {
		exit();
	}
	
}
