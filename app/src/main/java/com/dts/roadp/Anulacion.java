package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class Anulacion extends PBase {

	private ListView listView;
	private TextView lblTipo;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	private Runnable printotrodoc,printclose;
	private printer prn;
    private printer prn_nc;
	public  clsRepBuilder rep;
	private clsDocAnul doc;
	private clsDocFactura fdoc;

	private clsClasses.clsCFDV sitem;
	private AppMethods app;
	
	private int tipo,depparc,fcorel;	
	private String selid,itemid,fserie,fres,scor;
	private boolean modoapr=false;

	// impresion nota credito
	
	private ArrayList<String> lines= new ArrayList<String>();
	private String pserie,pnumero,pruta,pvend,pcli,presol,presfecha,pfser,pfcor;
	private String presvence,presrango,pvendedor,pcliente,pclicod,pclidir;
	private double ptot;
	private int residx;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anulacion);
		
		super.InitBase();
		addlog("Anulacion",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		lblTipo= (TextView) findViewById(R.id.lblDescrip);

		app = new AppMethods(this, gl, Con, db);
		gl.validimp=app.validaImpresora();
		if (!gl.validimp) msgbox("¡La impresora no está autorizada!");

		tipo=gl.tipo;
		if (gl.peModal.equalsIgnoreCase("APR")) modoapr=true;
		
		if (tipo==0) lblTipo.setText("Pedido");
		if (tipo==1) lblTipo.setText("Recibo");
		if (tipo==2) lblTipo.setText("Depósito");
		if (tipo==3) lblTipo.setText("Factura");
		if (tipo==4) lblTipo.setText("Recarga");
		if (tipo==5) lblTipo.setText("Devolución a bodega");
		if (tipo==6) lblTipo.setText("Nota de crédito");

		itemid="*";
				
		printotrodoc = new Runnable() {
		    public void run() {

				askPrint();
		    }
		};
		
		printclose= new Runnable() {
		    public void run() {
			}
		};
		
		prn=new printer(this,printclose,gl.validimp);
        prn_nc=new printer(this,printclose,gl.validimp);

		setHandlers();
		listItems();
				
		doc=new clsDocAnul(this,prn.prw,"");

		fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp,"",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
		fdoc.medidapeso=gl.umpeso;
	}

	//region Events
	
	public void anulDoc(View view){
		try{
			if (itemid.equalsIgnoreCase("*")) {
				mu.msgbox("Debe seleccionar un documento.");return;
			}

			if (tipo==3) {
				inputValor();
			} else {
				msgAsk("Anular documento");
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setHandlers(){
		try{

			listView.setOnTouchListener(new SwipeListener(this) {
				public void onSwipeRight() {
					onBackPressed();
				}
				public void onSwipeLeft() {}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						itemid=vItem.Cod;
						adapter.setSelectedIndex(position);

						sitem=vItem;
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
						clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;

						itemid=vItem.Cod;
						adapter.setSelectedIndex(position);
						sitem=vItem;

						anulDoc(view);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox( e.getMessage());
					}
					return true;
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region Main

	public void listItems() {
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		int vP,f;
		double val;
		String id,sf,sval;
			
		items.clear();
		selidx=-1;vP=0;
		
		try {
			
			if (tipo==0) {
				sql="SELECT D_PEDIDO.COREL,P_CLIENTE.NOMBRE,D_PEDIDO.FECHA,D_PEDIDO.TOTAL "+
					 "FROM D_PEDIDO INNER JOIN P_CLIENTE ON D_PEDIDO.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') ORDER BY D_PEDIDO.COREL DESC ";	
			}
			
			if (tipo==1) {
				sql="SELECT D_COBRO.COREL,P_CLIENTE.NOMBRE,D_COBRO.FECHA,D_COBRO.TOTAL "+
					 "FROM D_COBRO INNER JOIN P_CLIENTE ON D_COBRO.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_COBRO.ANULADO='N') AND (D_COBRO.DEPOS<>'S') ORDER BY D_COBRO.COREL DESC ";	
			}
			
			if (tipo==2) {
				sql="SELECT D_DEPOS.COREL,P_BANCO.NOMBRE,D_DEPOS.FECHA,D_DEPOS.TOTAL,D_DEPOS.CUENTA "+
					 "FROM D_DEPOS INNER JOIN P_BANCO ON D_DEPOS.BANCO=P_BANCO.CODIGO "+
					 "WHERE (D_DEPOS.ANULADO='N') ORDER BY D_DEPOS.COREL DESC ";	
			}
			
			if (tipo==3) {
				sql="SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO "+
					 "FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') " +
					 "ORDER BY D_FACTURA.COREL DESC ";
			}
			
			if (tipo==4) {
				sql="SELECT COREL,REFERENCIA,FECHA,0 "+
					 "FROM D_MOV WHERE (TIPO='R') AND (ANULADO='N') AND (STATCOM='N') ORDER BY FECHA DESC ";	
			}
			
			if (tipo==5) {
				sql="SELECT COREL,REFERENCIA,FECHA,0 "+
					 "FROM D_MOV WHERE (TIPO='D') AND (ANULADO='N') AND (STATCOM='N') ORDER BY FECHA DESC ";	
			}

			if (tipo==6) {
				sql="SELECT D_NOTACRED.COREL,P_CLIENTE.CODIGO || ' - ' || P_CLIENTE.NOMBRE AS DESC,FECHA,D_NOTACRED.TOTAL "+
					"FROM D_NOTACRED INNER JOIN P_CLIENTE ON D_NOTACRED.CLIENTE=P_CLIENTE.CODIGO "+
					"WHERE (D_NOTACRED.ANULADO='N') AND (D_NOTACRED.STATCOM='N') ORDER BY D_NOTACRED.COREL DESC ";
			}
			    		
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					id=DT.getString(0);
					
					vItem =clsCls.new clsCFDV();
			  	
					vItem.Cod=DT.getString(0);
					vItem.Desc=DT.getString(1);
					if (tipo==2) vItem.Desc+=" - "+DT.getString(4);
					
					if (tipo==3) {
						vItem.flag=DT.getInt(4);
						sf=DT.getString(2)+ StringUtils.right("000000" + Integer.toString(DT.getInt(4)), 6);;
					}else if(tipo==1||tipo==6){
						sf=DT.getString(0);
					}else{
						f=DT.getInt(2);sf=du.sfecha(f)+" "+du.shora(f);
					}
					
					vItem.Fecha=sf;
					val=DT.getDouble(3);
					try {
						sval=mu.frmcur(val);
					} catch (Exception e) {
						sval=""+val;
					}					
					
					vItem.Valor=sval;	  
					
					if (tipo==4 || tipo==5) vItem.Valor="";
					
					items.add(vItem);	
			 
					if (id.equalsIgnoreCase(selid)) selidx=vP;
					vP+=1;
			  
					DT.moveToNext();
				}	
			}

			if(DT!=null) DT.close();


		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox(e.getMessage());
	    }
			 
		adapter=new ListAdaptCFDV(this, items);
		listView.setAdapter(adapter);
		
		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}
	    
		listView.setVisibility(View.VISIBLE);
	}
	
	private void anulDocument() {
		
		try {
			
			db.beginTransaction();
			
			if (tipo==0) anulPedido(itemid);
			
			if (tipo==1) anulRecib(itemid);
			
			if (tipo==2) {
				getDepTipo();
				if (depparc==0) anulDepos(itemid); else anulDeposParc(itemid);
			}
			
			if (tipo==3) {
				if (checkFactDepos()) return;
				anulFactura(itemid);
			}
			
			if (tipo==4) anulRecarga(itemid);
			
			if (tipo==5) if (!anulDevol(itemid)) return;

			if (tipo==6) anulNotaCredito(itemid);

			db.setTransactionSuccessful();
			db.endTransaction();
			
			mu.msgbox("El documento ha sido anulado.");

			if(tipo==3) {

				clsDocFactura fdoc;

				fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
				fdoc.deviceid =gl.numSerie;
				fdoc.medidapeso=gl.umpeso;
				fdoc.buildPrint(itemid, 3, "TOL");

				String corelNotaCred=tieneNotaCredFactura(itemid);

				if (!corelNotaCred.isEmpty()){
					prn.printask(printotrodoc);
				}else {
					prn.printask(printclose);
				}


			}else if (tipo==6){

				clsDocDevolucion fdev;

				fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
				fdev.deviceid =gl.numSerie;

				fdev.buildPrint(itemid, 3, "TOL");

				String corelFactura=tieneFacturaNC(itemid);

				if (!corelFactura.isEmpty()){
					prn_nc.printask(printotrodoc, "printnc.txt");
				}else {
					prn_nc.printask(printclose, "printnc.txt");
				}

			}

			sql="DELETE FROM P_STOCK WHERE CANT=0 AND CANTM=0";
			db.execSQL(sql);

			listItems();
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
		   	mu.msgbox(e.getMessage());
		}
	}

	//endregion

	//region Documents
	
	private void anulPedido(String itemid) {

		try{
			sql="UPDATE D_PEDIDO SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_PEDIDOD SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			//anulBonif(itemid);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}	
	
	private boolean anulFactura(String itemid) {
		Cursor dt;
		String prod,um,ncred;

		boolean vAnulFactura=false;

		try{

			sql="SELECT PRODUCTO,UMSTOCK FROM D_FACTURAD WHERE Corel='"+itemid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){

				dt.moveToFirst();

				while (!dt.isAfterLast()) {

					prod=dt.getString(0);
					um=dt.getString(1);

					if (valexist(prod)) {
						revertStock(itemid,prod,um);
					}

					dt.moveToNext();
				}

			}

			if(dt!=null) dt.close();

			sql="UPDATE D_FACTURA  SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_FACTURAD SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE D_FACTURAP SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			//  Barras

			sql="INSERT INTO P_STOCKB SELECT * FROM D_FACTURA_BARRA WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="UPDATE P_STOCKB SET Corel='' WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_FACTURA_BARRA WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_STOCKB_DEV WHERE Corel='"+itemid+"'";
			db.execSQL(sql);

            sql = "UPDATE D_NOTACRED SET ANULADO ='S' WHERE FACTURA ='" + itemid + "'";
            db.execSQL(sql);

            sql = "UPDATE D_CXC SET ANULADO ='S' WHERE REFERENCIA ='" + itemid + "' AND TIPO  = 'N' ";
            db.execSQL(sql);

			anulBonif(itemid);

			// Nota credito

			sql="SELECT COREL FROM D_NOTACRED WHERE FACTURA='"+itemid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();ncred=dt.getString(0);

				sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + ncred + "' ";
				db.execSQL(sql);

				sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + ncred + "'";
				db.execSQL(sql);
			}

			//ImpresionFactura();

			if(dt!=null) dt.close();

			vAnulFactura=true;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			vAnulFactura=false;
		}

		return vAnulFactura;

	}
	
	private void anulBonif(String itemid) {
		Cursor dt;
		String prod,um;

		try{

			sql = "SELECT CODIGO,UNIDADMEDIDA FROM D_BONIF_STOCK WHERE Corel='" + itemid + "'";
			dt = Con.OpenDT(sql);

			if (dt.getCount() > 0) {

				dt.moveToFirst();
				while (!dt.isAfterLast()) {

					prod = dt.getString(0);
					um = dt.getString(1);

					revertStockBonif(itemid, prod, um);

					dt.moveToNext();
				}

				if(dt!=null) dt.close();
			}

			sql = "UPDATE D_BONIF SET Anulado='S' WHERE COREL='" + itemid + "'";
			db.execSQL(sql);

			sql = "UPDATE D_BONIFFALT SET Anulado='S' WHERE COREL='" + itemid + "'";
			db.execSQL(sql);

			sql="DELETE FROM D_BONIF_STOCK WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="DELETE FROM D_BONIF_LOTES WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			//sql="DELETE FROM D_REL_PROD_BON WHERE COREL='"+itemid+"'";
			//db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}
	}
	
	private void revertStock(String corel) {
		Cursor dt;
		String doc,stat,lot,cod,um;
		double cant,ppeso;
		
		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV,CODIGO,UNIDADMEDIDA FROM D_FACTURA_STOCK " +
					"WHERE (COREL='" + corel + "') ";
			dt = Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);
				cod=dt.getString(13);
				um=dt.getString(14);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", cod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + cod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void revertStock(String corel,String pcod,String um) {
		Cursor dt;
		String doc,stat,lot;
		double cant,ppeso;

		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV FROM D_FACTURA_STOCK " +
					"WHERE (COREL='" + corel + "') AND (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "')";
			dt = Con.OpenDT(sql);

			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", pcod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void revertStockBonif(String corel,String pcod,String um) {
		Cursor dt;
		String doc,stat,lot;
		double cant,ppeso;

		doc="";stat="";lot="";

		try{
			sql = "SELECT CANT,CANTM,PESO,plibra,LOTE,DOCUMENTO,FECHA,ANULADO,CENTRO,STATUS,ENVIADO,CODIGOLIQUIDACION,COREL_D_MOV FROM D_BONIF_STOCK " +
					"WHERE (COREL='" + corel + "') AND (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "')";
			dt = Con.OpenDT(sql);
			if (dt.getCount()==0) return;

			dt.moveToFirst();

			while (!dt.isAfterLast()) {

				cant = dt.getInt(0);
				ppeso = dt.getDouble(2);
				lot = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(9);

				try {

					ins.init("P_STOCK");

					ins.add("CODIGO", pcod);
					ins.add("CANT", 0);
					ins.add("CANTM", dt.getDouble(1));
					ins.add("PESO", 0);
					ins.add("plibra", dt.getDouble(3));
					ins.add("LOTE", lot);
					ins.add("DOCUMENTO", doc);

					ins.add("FECHA", dt.getInt(6));
					ins.add("ANULADO", dt.getInt(7));
					ins.add("CENTRO", dt.getString(8));
					ins.add("STATUS", stat);
					ins.add("ENVIADO", dt.getInt(10));
					ins.add("CODIGOLIQUIDACION", dt.getInt(11));
					ins.add("COREL_D_MOV", dt.getString(12));
					ins.add("UNIDADMEDIDA", um);

					db.execSQL(ins.sql());

				} catch (Exception e) {
					//#CKFK 20190308 Este addlog lo quité porque da error porque el registro ya existe y en ese caso solo se va a hacer el update.
					//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					//mu.msgbox(e.getMessage());
				}

				sql = "UPDATE P_STOCK SET CANT=CANT+"+cant+",PESO=PESO+"+ppeso+"  WHERE (CODIGO='" + pcod + "') AND (UNIDADMEDIDA='" + um + "') AND (LOTE='" + lot + "') AND (DOCUMENTO='" + doc + "') AND (STATUS='" + stat + "')";
				db.execSQL(sql);

				dt.moveToNext();
			}

			if(dt!=null) dt.close();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void anulDepos(String itemid) {
		Cursor DT;
		String tdoc;

		try{

			db.beginTransaction();

			if (gl.depparc){
				sql="UPDATE D_DEPOS SET Anulado='S' WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
			}

			sql="SELECT DISTINCT DOCCOREL,TIPODOC FROM D_DEPOSD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				tdoc=DT.getString(1);

				if (tdoc.equalsIgnoreCase("F")) {
					sql="UPDATE D_FACTURA SET DEPOS='N' WHERE (COREL='"+DT.getString(0)+"')";
				} else {
					sql="UPDATE D_COBRO SET DEPOS='N' WHERE (COREL='"+DT.getString(0)+"')";
				}

				db.execSQL(sql);

				DT.moveToNext();
			}
			if(DT!=null) DT.close();

			if (!gl.depparc){
				sql="DELETE FROM D_DEPOS WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
				sql="DELETE FROM D_DEPOSD WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
				sql="DELETE FROM D_DEPOSB WHERE COREL='"+itemid+"'";
				db.execSQL(sql);
			}

			sql="UPDATE FinDia SET val3 = 0, val4=0";
			db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

		}catch (Exception e){

			db.endTransaction();
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}
	
	private void anulDeposParc(String itemid) {
		try{
			sql="UPDATE D_DEPOS SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}	
	
	private void anulRecarga(String itemid) {
		Cursor DT;
		String prod;
		double cant,cantm;

		try{
			sql="UPDATE D_MOV SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="SELECT PRODUCTO,CANT,CANTM FROM D_MOVD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				prod=DT.getString(0);
				cant=DT.getDouble(1);
				cantm=DT.getDouble(2);

				try {
					sql="UPDATE P_STOCK SET CANT=CANT-"+cant+", CANTM=CANTM-"+cantm+" WHERE CODIGO='"+prod+"'";
					db.execSQL(sql);
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					mu.msgbox(e.getMessage()+"\n"+sql);
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}

	private boolean anulDevol(String itemid) {
		Cursor DT;
		String prod;
		double cant,cantm;

		boolean vAnulDevol=false;

		try{

			db.beginTransaction();

			sql="UPDATE D_MOV SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);

			sql="SELECT PRODUCTO,CANT,CANTM, UNIDADMEDIDA FROM D_MOVD WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				sql="INSERT INTO P_STOCK SELECT PRODUCTO, CANT, CANTM, PESO, 0, LOTE, '',0,'N', '','',0,0,'', UNIDADMEDIDA " +
						"FROM D_MOVD WHERE (COREL='"+itemid+"')";
				db.execSQL(sql);
			}

			sql="SELECT PRODUCTO,UNIDADMEDIDA FROM D_MOVDB WHERE (COREL='"+itemid+"')";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				sql="INSERT INTO P_STOCKB " +
					"SELECT M.RUTA, D.BARRA, D.PRODUCTO, 1, '' AS COREL, 0 AS PRECIO, D.PESO, '' AS DOCUMENTO, " +
					" M.FECHA, 0 AS ANULADO, '' AS CENTRO, 'A' AS ESTATUS, " +
					"0 AS ENVIADO, 0 AS CODIGOLIQUIDACION, '' AS COREL_D_MOV, D.UNIDADMEDIDA, '' AS DOCENTREGA " +
					"FROM D_MOV M INNER JOIN D_MOVDB D ON M.COREL = D.COREL WHERE (M.COREL='"+itemid+"')";
				db.execSQL(sql);
			}

			if(DT!=null) DT.close();

			sql="UPDATE FinDia SET val5 = 0";
			db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

			vAnulDevol=true;

		}catch (Exception e){

			db.endTransaction();
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return vAnulDevol;

	}

	private void anulRecib(String itemid) {
		try{
			sql="UPDATE D_COBRO  SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
			sql="UPDATE D_COBROD SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
			sql="UPDATE D_COBROP SET Anulado='S' WHERE COREL='"+itemid+"'";
			db.execSQL(sql);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
	}

	private boolean anulNotaCredito(String itemid) {

		Cursor DT2;
		String vCorelFactura = "";
		String vCorelDevol="";
		boolean vAnulNotaCredito=false;
		String vCorelNotaC = itemid;

		try{

			sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" + itemid + "'";
			DT2=Con.OpenDT(sql);

			if (DT2.getCount()>0){
				DT2.moveToFirst();
				vCorelFactura = DT2.getString(0);
			}

			DT2.close();

			itemid = vCorelFactura;//En la variable vCorelFactura se guarda el corel de la Factura si es una NC con venta y sino el corel de D_CXC

			if (ExisteFactura(itemid)){
				vAnulNotaCredito = (anulFactura(itemid)?true:false);
			}else{
				vCorelDevol = itemid;

				sql = "UPDATE D_CXC SET ANULADO='S' WHERE COREL='" + vCorelDevol + "' ";
				db.execSQL(sql);

				sql = "UPDATE D_NOTACRED SET ANULADO='S' WHERE COREL='" + vCorelNotaC + "'";
				db.execSQL(sql);

				vAnulNotaCredito=true;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			vAnulNotaCredito=false;
		}

		return vAnulNotaCredito;
	}

	private String tieneFacturaNC(String vCorel){

		Cursor DT;
		String vtieneFacturaNC= "";

		try{

			sql = "SELECT FACTURA FROM D_NOTACRED WHERE COREL = '" + vCorel + "' AND FACTURA IN (SELECT COREL FROM D_FACTURA)";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				vtieneFacturaNC = DT.getString(0);
			}

		}catch (Exception ex){
		    mu.msgbox("Ocurrió un error "+ex.getMessage());
		}

		return vtieneFacturaNC;
	}

	private String tieneNotaCredFactura(String vCorel){
	Cursor DT;
	String vtieneNotaCredFactura= "";

	try{

		sql = "SELECT COREL FROM D_NOTACRED WHERE FACTURA = '" + vCorel + "' AND FACTURA IN (SELECT COREL FROM D_FACTURA)";
		DT=Con.OpenDT(sql);

		if (DT.getCount()>0){
			DT.moveToFirst();
			vtieneNotaCredFactura = DT.getString(0);
		}

	}catch (Exception ex){
		mu.msgbox("Ocurrió un error "+ex.getMessage());
	}

	return vtieneNotaCredFactura;
}

    private boolean ExisteFactura(String vCorel){

        Cursor DT;
        boolean vExisteFactura = false;

        try{

            sql = "SELECT COREL FROM D_FACTURA WHERE COREL = '" + vCorel + "'";
            DT=Con.OpenDT(sql);

            vExisteFactura = (DT.getCount()>0?true:false);

        }catch (Exception ex){

        }

        return vExisteFactura;
    }

    //endregion
	
	//region Impresion
	
	private void ImpresionFactura() {
		try{
			if (fdoc.buildPrint(itemid,3,gl.peFormatoFactura)) prn.printask();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	private void Impresion() {
		try{
			if (doc.buildPrintSimple("0",0)) prn.printask();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void ImprimeNC_Fact(){

		try{

			if (tipo==3){

				clsDocDevolucion fdev;
				String corelNotaCred=tieneNotaCredFactura(itemid);

				if (!corelNotaCred.isEmpty()){

					fdev=new clsDocDevolucion(this,prn_nc.prw,gl.peMon,gl.peDecImp, "printnc.txt");
					fdev.deviceid =gl.numSerie;

					fdev.buildPrint(corelNotaCred, 3, "TOL"); prn_nc.printnoask(printclose, "printnc.txt");

				}
			}else if (tipo==6){

				String corelFactura=tieneFacturaNC(itemid);

				if (!corelFactura.isEmpty()){
					clsDocFactura fdoc;

					fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp, "",app.esClienteNuevo(pclicod),gl.codCliNuevo,gl.peModal);
					fdoc.deviceid =gl.numSerie;
					fdoc.medidapeso=gl.umpeso;
					fdoc.buildPrint(corelFactura, 3, "TOL"); prn.printnoask(printclose,"print.txt");
				}

			}

		}catch(Exception ex){

		}
	}

	private void askPrint() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Impresión correcta?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (tipo==3 || tipo==6){
						ImprimeNC_Fact();
					}

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.show();
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private class clsDocAnul extends clsDocument {

		public clsDocAnul(Context context, int printwidth, String archivo) {
			super(context, printwidth ,gl.peMon,gl.peDecImp, archivo);

			nombre="Existencias";
			numero="";
			serie="";
			ruta=gl.ruta;
			vendedor=gl.vendnom;
			cliente="";

		}

		protected boolean buildDetail() {


			return true;
		}

		protected boolean buildFooter() {

			try {
				rep.add("");rep.add("");
				rep.addc("ANULACION");
				rep.add("");
				rep.add("Ruta : "+ruta);
				rep.add("Vendedor : "+vendedor);
				rep.add("");

				rep.add("");

				//if (tipo==1) lblTipo.setText("Recibo");
				if (gl.tipo==2) {
					rep.add("Deposito");
					rep.add("Fecha : "+sitem.Fecha);
					rep.add("Cuenta : "+sitem.Desc);
					rep.add("Total : "+sitem.Valor);
				}
				if (gl.tipo==3) {
					rep.add("Factura");
					rep.add("Numero : "+sitem.Fecha);
					rep.add("Total : "+sitem.Valor);
					rep.add("Cliente : "+sitem.Desc);
				}
				if (gl.tipo==4) {
					rep.add("Recarga");
					rep.add("Fecha : "+sitem.Fecha);
				}
				//if (tipo==5) lblTipo.setText("Devoluci�n a bodega");

				rep.add("");
				rep.add("");
				rep.add("");
				rep.add("");

				return true;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				return false;
			}

		}
		
	}

	//endregion
	
	//region Aprofam
	
	private boolean aprValidaNC() {
		Cursor DT;
		int ci,cf,ca1,ca2;
		double dd;
		
		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN FROM P_CORELNC ";	
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
	
			if (ca1>=cf) {
				msgbox("Se ha acabado el talonario de notas de crédito. No se puede continuar con la anulación de factura.");
				return false;
			}
			
			dd=cf-ci;dd=0.75*dd;
			ca2=ci+((int) dd);
			
			if (ca1>ca2) {
				toastcent("Queda menos que 25% de talonario de notas de crédito.");
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("No esta definido correlativo de notas de crédito. No se puede continuar con la anulación de factura.\n"+e.getMessage());
			return false;
		}	
					
		return true;
		
	}
	
	private void aprNuevaNC() {
		Cursor DT;
		String corel,factser,cli;
		int factcor;
		double facttot;
		
		aprAssignCorel();		
		if (fcorel==0) return;		
		
		try {	
			
			corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
			fecha=du.getActDateTime();
			
			sql="SELECT TOTAL,SERIE,CORELATIVO,CLIENTE FROM D_FACTURA WHERE Corel='"+itemid+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			factser=DT.getString(1);
			factcor=DT.getInt(2);
			facttot=DT.getDouble(0);
			cli=DT.getString(3);
					
			try {
				
				db.beginTransaction();
				    			
				ins.init("D_NOTACRED");
				
				ins.add("COREL",corel);
				ins.add("ANULADO","N");
				ins.add("FECHA",fecha);
				ins.add("RUTA",gl.ruta);
				ins.add("VENDEDOR",gl.vend);
				ins.add("CLIENTE",cli);
				ins.add("TOTAL",facttot);
				ins.add("FACTURA",itemid);
				ins.add("SERIE",fserie);
				ins.add("CORELATIVO",fcorel);
				ins.add("STATCOM","N");				
				ins.add("CODIGOLIQUIDACION",0);
				ins.add("RESOLNC",fres);
				ins.add("SERIEFACT",factser);pfser=factser;
				ins.add("CORELFACT",factcor);pfcor=""+factcor;
				ins.add("IMPRES",1);
					      	
				db.execSQL(ins.sql());
								
				// Actualizacion de ultimo correlativo				
				sql="UPDATE P_CORELNC SET CORELULT="+fcorel+"  WHERE RUTA='"+gl.ruta+"'";	
				db.execSQL(sql);
							
				db.setTransactionSuccessful();					
				db.endTransaction();
			
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				db.endTransaction();
				mu.msgbox("Error (nota credito) " + e.getMessage());return;
			}

			if (prn.isEnabled()) aprNotePrn(corel);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
			
	}	
	
	private void aprNotePrn(String corel) {
		
		aprLoadHeadData(corel);
		
		try {
			
			rep=new clsRepBuilder(this,prn.prw,true,gl.peMon,gl.peDecImp, "");
			
			buildHeader(corel,0);
			
			rep.line();
			rep.empty();
			rep.addc("NOTA CREDITO");
			rep.empty();
			rep.line();
			rep.empty();
				
			rep.add("Factura serie : "+pfser+" numero : "+pfcor);
			rep.add("Monto total : "+mu.frmdec(ptot));			
			rep.empty();
			rep.line();
			rep.empty();
			rep.empty();
			rep.empty();
			rep.empty();
			rep.empty();
			
			buildHeader(corel,2);
			
			rep.line();
			rep.empty();
			rep.addc("NOTA CREDITO");
			rep.empty();
			rep.line();
			rep.empty();
				
			rep.add("Factura serie : "+pfser+" numero : "+pfcor);
			rep.add("Monto total : "+mu.frmdec(ptot));			
			rep.empty();
			rep.line();
			rep.empty();
			rep.empty();
			rep.empty();
			
			rep.save();
			
			prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private boolean aprLoadHeadData(String corel) {
		Cursor DT;
		int ff;
					
		try {
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL FROM D_NOTACRED WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pserie=DT.getString(0);
			pnumero=""+DT.getInt(1);
			pruta=DT.getString(2);
			
			pvend=DT.getString(3);
			pcli=DT.getString(4);		
			ptot=DT.getDouble(5);

			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			presol="Resolucion No. : "+DT.getString(0);
			ff=DT.getInt(1);presfecha="De Fecha : "+du.sfecha(ff);
			ff=DT.getInt(2);presvence="Resolucion vence : "+du.sfecha(ff);		
			presrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);

			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+pvend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pvendedor=DT.getString(0);

			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+pcli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pcliente=DT.getString(0);       		
			pclicod=pcli;
			pclidir=DT.getString(3);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());
			pcliente=pcli;
			pvendedor=pvend;
	    }

		return true;
		
	}

	private boolean buildHeader(String corel,int reimpres) {

		lines.clear();

		try {	
			loadHeadLines();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());return false;
		}		

		saveHeadLines(reimpres);

		return true;
	}

	private void saveHeadLines(int reimpres) {
		String s;

		rep.empty();rep.empty();

		try{
			for (int i = 0; i <lines.size(); i++) {
				s=lines.get(i);
				s=encabezado(s);
				if (residx==1) {
					rep.add(presol);
					rep.add(presfecha);
					rep.add(presvence);
					rep.add(presrango);
					residx=0;
				}
				if (!s.equalsIgnoreCase("@@")) rep.add(s);
			}

			if (!mu.emptystr(pclicod)) rep.add(pclicod);
			if (!mu.emptystr(pclidir)) rep.add(pclidir);

			if (reimpres==3) rep.add("--------  A N U L A C I O N  --------");
			if (reimpres==1) rep.add("------  R E I M P R E S I O N  ------");
			if (reimpres==2) rep.add("-----  C O N T A B I L I D A D  -----");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private String encabezado(String l) {
		String s,lu;
		int idx;

		residx=0;

		//lu=l.toUpperCase().trim();
		lu=l.trim();
		try{
			if (lu.length()==1 && lu.equalsIgnoreCase("N")) {
				s="NOTA CREDITO";s=rep.ctrim(s);return s;
			}

			if (l.indexOf("dd-MM-yyyy")>=0) {
				s=du.sfecha(du.getActDateTime());
				l=l.replace("dd-MM-yyyy",s);return l;
			}

			if (l.indexOf("HH:mm:ss")>=0) {
				s=du.shora(du.getActDateTime());
				l=l.replace("HH:mm:ss",s);return l;
			}

			idx=lu.indexOf("SS");
			if (idx>=0) {
				if (mu.emptystr(pserie)) return "@@";
				if (mu.emptystr(pnumero)) return "@@";

				s=lu.substring(0,idx);
				s="Nota credito serie : ";
				s=s+pserie+" numero : "+pnumero;
				residx=1;
				return s;
			}

			idx=lu.indexOf("VV");
			if (idx>=0) {
				if (mu.emptystr(pvendedor)) return "@@";
				l=l.replace("VV",pvendedor);return l;
			}

			idx=lu.indexOf("RR");
			if (idx>=0) {
				if (mu.emptystr(pruta)) return "@@";
				l=l.replace("RR",pruta);return l;
			}

			idx=lu.indexOf("CC");
			if (idx>=0) {
				if (mu.emptystr(pcliente)) return "@@";
				l=l.replace("CC",pcliente);return l;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return l;
	}
	
	private boolean loadHeadLines() {
		Cursor DT;	
		String s;
		
		try {
			sql="SELECT TEXTO FROM P_ENCABEZADO_REPORTESHH ORDER BY CODIGO";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return false;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				s=DT.getString(0);	
				lines.add(s);	
				DT.moveToNext();
			}

			return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());return false;
		}				
	}

	private void aprAssignCorel(){
		Cursor DT;
		int cf,ca1;
		
		fcorel=0;fserie="";fres="";
		try{
			try {
				sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN,RESOL FROM P_CORELNC WHERE RUTA='"+gl.ruta+"'";
				DT=Con.OpenDT(sql);

				DT.moveToFirst();

				fserie=DT.getString(0);
				ca1=DT.getInt(1);
				cf=DT.getInt(3);
				fres=DT.getString(4);

				fcorel=ca1+1;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				fcorel=0;fserie="";
				msgbox("No existe correlativo disponible, no se puede emitir la nota de crédito");
				return;
			}

			if (fcorel>cf) {
				msgbox("Se ha acabado el talonario de notas de crédito. No se puede continuar con la anulación de factura.");
				fcorel=0;return;
			}

			if (fcorel==cf) mu.msgbox("Esta es la última nota de crédito.");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

				
	}

	//endregion
	
	//region Aux
	
	private void msgAsk(String msg) {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("ROAD");
			dialog.setMessage("¿" + msg  + "?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					anulDocument();
				}
			});
			dialog.setNegativeButton("No", null);
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
			
	}

	private void getDepTipo() {
		Cursor DT;
		
		try {
			sql="SELECT BOLETA_DEPOSITO,DEPOSITO_PARCIAL FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			gl.boldep=DT.getInt(0);
			depparc=DT.getInt(1);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			gl.boldep=0;
			depparc=0;
		}
		
	}	
	
	private boolean checkFactDepos() {
		Cursor dt;

		try {

			sql="SELECT D_DEPOSD.DOCCOREL,D_DEPOS.ANULADO "+
				"FROM D_DEPOS INNER JOIN D_DEPOSD ON D_DEPOS.COREL=D_DEPOSD.COREL " +
				"WHERE D_DEPOSD.DOCCOREL='"+itemid+"' AND D_DEPOS.ANULADO='N'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) return false;
			msgbox("La factura está depositada, no se puede anular");
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private boolean valexist(String prcodd) {
		Cursor DT;
		
		try {
			sql="SELECT TIPO FROM P_PRODUCTO WHERE CODIGO='"+prcodd+"'";
           	DT=Con.OpenDT(sql);
           	if (DT.getCount()==0) return false;

           	DT.moveToFirst();
			
           	return DT.getString(0).equalsIgnoreCase("P");
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
	    }
	}


	private void inputValor() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		int cor;

		cor=sitem.flag;
		cor=cor % 1000;
		scor=""+cor;
		if (cor<10) {
			scor="00"+scor;
		} else if (cor<10) {
			scor="0"+scor;
		}

		alert.setTitle("Ingrese valor : "+scor);

		final EditText input = new EditText(this);
		alert.setView(input);

		input.setInputType(InputType.TYPE_CLASS_NUMBER );
		input.setText("");
		input.requestFocus();

		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					String s=input.getText().toString();
					if (s.equalsIgnoreCase(scor)) {
						msgAsk("Anular factura");
					} else {
						msgbox("Valor incorrecto");
					}
				} catch (Exception e) {
					mu.msgbox("Valor incorrecto");return;
				}
			}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});

		alert.show();
	}


	//endregion

}
