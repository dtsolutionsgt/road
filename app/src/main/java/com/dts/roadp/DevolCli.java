package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class DevolCli extends PBase {

	private ListView listView;
	private TextView lblCantProds,lblCantUnd,lblCantKgs,lblCantTotal;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;

	private double cntprd=0.0,cntunis=0.0,cntkgs=0.0,cntotl=0.0;

	private printer prn;
	private clsDocDevolucion fdevol;
	public Runnable printclose;

	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos;

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
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDateTime();
		gl.devrazon="0";
		
		clearData();

		printclose= new Runnable() {
			public void run() {
				limpiavariables_devol();
				DevolCli.super.finish();
			}
		};


		prn=new printer(this,printclose);
		fdevol=new clsDocDevolucion(this,prn.prw,gl.peMon,gl.peDecImp, "printnc.txt");
		fdevol.deviceid =gl.deviceId;
	}


	// Events
	
	public void showProd(View view) {
		try{

			browse=1;
			itempos=-1;
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
			  
			  vItem.Cod=DT.getString(0);
			  vItem.Desc=DT.getString(3);
			  vItem.Valor=DT.getString(2);
			  s=mu.frmdec(DT.getDouble(1));
			  vItem.Fecha=s;
			  vItem.id=DT.getInt(4);

            cntprd = cntprd+1;
            cntunis = cntunis + Double.parseDouble(s);
            cntkgs = mu.round(cntkgs + DT.getDouble(5),gl.peDec);
            cntotl = mu.round(cntotl + DT.getDouble(6),gl.peDec);

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
		}catch (Exception e){
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
		
		gl.dvcorreld = obtienecorrel("D");
		gl.dvcorrelnc = obtienecorrel("NC");

		try {

		    if (gl.tiponcredito==1){

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

                        if (estado.equalsIgnoreCase("M")) {
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

                db.setTransactionSuccessful();

                db.endTransaction();

                Toast.makeText(this,"Devolución guardada", Toast.LENGTH_SHORT).show();

				sql="DELETE FROM T_CxCD";
				db.execSQL(sql);

                gl.closeCliDet = true;
                gl.closeVenta = true;


				createDoc();
				//msgAskSave("Aplicar pago y crear un recibo");

            }else{

                try{

                    Intent i = new Intent(this, CliDet.class);
                    gl.dvbrowse=3;
                    gl.dvdispventa = cntotl;
                    gl.dvestado = estado;
                    startActivity(i);

                }catch (Exception e){
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                }

            }

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox(e.getMessage());
		}
	}

/*	private void msgAskSave(String msg) {

		try{

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setCancelable(false);

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

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}*/

	private void createDoc(){

		try{

			if (prn.isEnabled()) {
				fdevol.buildPrint(gl.dvcorreld,0);
				//#CKFK 20190401 09:47AM Agregué la funcionalidad de enviar el nombre del archivo a imprimir
				prn.printask(printclose, "printnc.txt");
			}

		}catch (Exception e){
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

    private String obtienecorrel(String tipo){
	    String correl="";
        Cursor DT;

	    try{
            sql="SELECT SERIE,ACTUAL+1,FINAL,INICIAL FROM P_CORREL_OTROS WHERE RUTA='"+gl.ruta+"' AND TIPO='"+tipo+"'";
            DT=Con.OpenDT(sql);

            if(DT.getCount()>0){

                DT.moveToFirst();

                correl=DT.getString(0) + StringUtils.right("00000" + Integer.toString(DT.getInt(1)), 5);

                if (tipo.equals("D")){
                    gl.dvactuald = String.valueOf(DT.getInt(1));
                }else{
                    gl.dvactualnc = String.valueOf(DT.getInt(1));
                }

            }else{

                correl=gl.ruta+"_"+mu.getCorelBase();

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
