package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Reimpresion extends PBase {

	private ListView listView;
	private TextView lblTipo;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptCFDV adapter;
	private clsClasses.clsCFDV selitem;
	
	private printer prn;
	private Runnable printclose;
	public clsRepBuilder rep;
	
	private clsDocFactura fdoc;
	private clsDocMov mdoc;
	private clsDocDepos ddoc;
	private clsDocCobro cdoc;
	
	private int tipo;	
	private String selid,itemid;
	
	// impresion nota credito

	private ArrayList<String> lines= new ArrayList<String>();
	private String pserie,pnumero,pruta,pvend,pcli,presol,presfecha,pfser,pfcor;
	private String presvence,presrango,pvendedor,pcliente,pclicod,pclidir;
	private double ptot;
	private int residx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reimpresion);
	
		super.InitBase();
		addlog("Reimpresion",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		lblTipo= (TextView) findViewById(R.id.lblFecha);
				
		tipo=((appGlobals) vApp).tipo;
		itemid="*";
		
		setHandlers();
		listItems();
		
		printclose= new Runnable() {
		    public void run() {
		    	Reimpresion.super.finish();
		    }
		};
		
		prn=new printer(this,printclose);
			
		switch (tipo) {
		case 0:  
			lblTipo.setText("Pedido");break;
		case 1:  
			cdoc=new clsDocCobro(this,prn.prw,gl.peMon,gl.peDecImp);
			lblTipo.setText("Recibo");break;	
		case 2:  
			ddoc=new clsDocDepos(this,prn.prw,gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp);
			lblTipo.setText("Deposito");break;
		case 3:  
			fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp);
			lblTipo.setText("Factura");break;
		case 4:  
			mdoc=new clsDocMov(this,prn.prw,"Recarga",gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp);
			lblTipo.setText("Recarga");break;
		case 5:  
			mdoc=new clsDocMov(this,prn.prw,"Dvolucion a bodega",gl.ruta,gl.vendnom,gl.peMon,gl.peDecImp);
			lblTipo.setText("Devolución a bodega");break;
		case 6:  
			fdoc=new clsDocFactura(this,prn.prw,gl.peMon,gl.peDecImp);
			lblTipo.setText("Nota Crédito");break;
			
		case 99:  
			lblTipo.setText("Cierre de día");break;
		}		
			
	}
	
	
	// Events
	
	public void printDoc(View view){

		try{
			if (itemid.equalsIgnoreCase("*")) {
				mu.msgbox("Debe seleccionar un documento.");return;
			}

			printDocument();
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
						//printDocument();
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

						printDocument();
						//printDoc(view);
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


	// Main

	public void listItems() {
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		int vP,f;
		double val;
		String id,sf,sval,tm;
			
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

			if (tipo == 3) {
				if (gl.peModal.equalsIgnoreCase("TOL")) {
					sql = "SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO,D_FACTURA.IMPRES " +
							"FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO " +
							"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') ORDER BY D_FACTURA.COREL DESC";
				} else {
					sql = "SELECT D_FACTURA.COREL,P_CLIENTE.NOMBRE,D_FACTURA.SERIE,D_FACTURA.TOTAL,D_FACTURA.CORELATIVO,D_FACTURA.IMPRES " +
							"FROM D_FACTURA INNER JOIN P_CLIENTE ON D_FACTURA.CLIENTE=P_CLIENTE.CODIGO " +
							"WHERE (D_FACTURA.ANULADO='N') AND (D_FACTURA.STATCOM='N') ORDER BY D_FACTURA.COREL DESC LIMIT 1";
				}
			}
				
			if (tipo==4 || tipo==5) {
				tm="R";if (tipo==5) tm="D";
				sql="SELECT COREL,COREL,FECHA,0 AS TOTAL "+
					 "FROM D_MOV WHERE (TIPO='"+tm+"') AND (ANULADO='N')  ORDER BY COREL DESC ";	
			}
			
			if (tipo==6) {
				sql="SELECT D_NOTACRED.COREL,P_CLIENTE.NOMBRE,D_NOTACRED.SERIE,D_NOTACRED.TOTAL,D_NOTACRED.CORELATIVO,D_NOTACRED.IMPRES "+
					 "FROM D_NOTACRED INNER JOIN P_CLIENTE ON D_NOTACRED.CLIENTE=P_CLIENTE.CODIGO "+
					 "WHERE (D_NOTACRED.ANULADO='N') AND (D_NOTACRED.STATCOM='N') ORDER BY D_NOTACRED.COREL DESC LIMIT 1";	
			}
			
			if (tipo<99) {
				
				DT=Con.OpenDT(sql);
	
				if (DT.getCount()>0) {

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						id=DT.getString(0);

						vItem =clsCls.new clsCFDV();

						vItem.Cod=DT.getString(0);
						vItem.Desc=DT.getString(1);
						if (tipo==2) vItem.Desc+=" - "+DT.getString(4);	

						if (tipo==3 || tipo==6) {
							sf=DT.getString(2)+"-"+DT.getInt(4);						
						} else {	
							f=DT.getInt(2);sf=du.sfecha(f)+" "+du.shora(f);	
						}

						vItem.Fecha=sf;

						val=DT.getDouble(3);sval=""+val;
						vItem.Valor=sval;	  

						if (tipo==4 || tipo==5) {
							vItem.Valor="";
						} else {
							vItem.Valor=mu.frmcur(val);
						}

						if (tipo==3 || tipo==6) {
							if (DT.getInt(5)<=1) items.add(vItem);	
						} else {	
							items.add(vItem);	
						}
	
						if (id.equalsIgnoreCase(selid)) selidx=vP;
						vP+=1;

						DT.moveToNext();					

					}	
				}

			} else {	
				
				if (tipo==99) {

					vItem =clsCls.new clsCFDV();

					vItem.Cod="";
					vItem.Desc="";
					vItem.Fecha="Ultimo Cierre de dia";
					vItem.Valor="";	  

					items.add(vItem);				
				}		
			}
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptCFDV(this, items);
		listView.setAdapter(adapter);
		
		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}
	    	    
	}
	
	private void printDocument() {

		try{
			switch (tipo) {
				case 0:
					imprPedido();break;
				case 1:
					imprRecibo();break;
				case 2:
					imprDeposito();break;
				case 3:
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						imprFactura();
					} else {
						imprUltFactura();
					}
					break;
				case 4:
					imprRecarga();break;
				case 5:
					imprRecarga();break;
				case 6:
					imprUltNotaCredito();break;

				case 99:
					imprFindia();break;
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void imprPedido() {
		try{

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		
	}
	
	private void imprRecibo() {
		try {
			if (cdoc.buildPrint(itemid,1)) prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}
	
	private void imprDeposito() {
		try {
			if (ddoc.buildPrint(itemid,1)) prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}	
	}
	
	private void imprFactura() {
		fdoc.deviceid =androidid();

		try {
			if (fdoc.buildPrint(itemid,1,gl.peFormatoFactura)) prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}	
	
	private void imprRecarga() {
		try {
			if (mdoc.buildPrint(itemid,1)) prn.printask();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}	
	
	private void imprFindia() {
		try {
			prn.printask("SyncFold/findia.txt");
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
		}
	}	
	
	
	// Ultima factura + nota credito
	
	private void imprUltFactura() {
		Cursor dt;
		String id,serie;
		int corel;

		try {

			sql="SELECT COREL,IMPRES,SERIE,CORELATIVO FROM D_FACTURA WHERE COREL='"+itemid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) {
				msgbox("¡No existe ninguna factura elegible para reimpresion!");return;
			}
			
			dt.moveToFirst();
			
			id=dt.getString(0);
			serie=dt.getString(2);
			corel=dt.getInt(3);		
			
			if (dt.getInt(1)>1) {
				msgbox("¡La factura "+serie+" - "+corel+" no se puede imprimir porque ya fue reimpresa anteriormente!");return;
			}
			
			if (fdoc.buildPrint(id,1,gl.peFormatoFactura)) prn.printask();
		
			try {
				sql="UPDATE D_FACTURA SET IMPRES=2 WHERE COREL='"+itemid+"'";		
				db.execSQL(sql);
			} catch (Exception e) {
			}			
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}
	
	private void imprUltNotaCredito() {
		Cursor dt;
		String id,serie;
		int corel;

		try {

			sql="SELECT COREL,IMPRES,SERIE,CORELATIVO FROM D_NOTACRED WHERE COREL='"+itemid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()==0) {
				msgbox("¡No existe ninguna nota de credito elegible para reimpresion!");return;
			}
			
			dt.moveToFirst();
			
			id=dt.getString(0);
			serie=dt.getString(2);
			corel=dt.getInt(3);		
			
			if (dt.getInt(1)>1) {
				msgbox("�La nota de credito "+serie+" - "+corel+" no se puede imprimir porque ya fue reimpresa anteriormente!");return;
			}
				
			aprNotePrn(itemid);
			//if (fdoc.buildPrint(id,1)) prn.printask();
		
			try {
				sql="UPDATE D_NOTACRED SET IMPRES=2 WHERE COREL='"+itemid+"'";		
				//db.execSQL(sql);
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			}			
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	
	// Aprofam
	
	private void aprNotePrn(String corel) {
		
		aprLoadHeadData(corel);
		
		try {
			
			rep=new clsRepBuilder(this,prn.prw,true,gl.peMon,gl.peDecImp);
			
			buildHeader(corel,1);
			
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
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,SERIEFACT,CORELFACT FROM D_NOTACRED WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pserie=DT.getString(0);
			pnumero=""+DT.getInt(1);
			pruta=DT.getString(2);
			
			pvend=DT.getString(3);
			pcli=DT.getString(4);		
			ptot=DT.getDouble(5);
			
			pfser=DT.getString(6);
			pfcor=DT.getString(7);
	
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(e.getMessage());return false;
	    }	
		
		try {
			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			presol="Resolucion No. : "+DT.getString(0);
			ff=DT.getInt(1);presfecha="De Fecha : "+du.sfecha(ff);
			ff=DT.getInt(2);presvence="Resolucion vence : "+du.sfecha(ff);		
			presrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
			try {
			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+pvend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pvendedor=DT.getString(0);
		} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			pvendedor=pvend;
	    }	
		
		try {
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+pcli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			pcliente=DT.getString(0);       		
			pclicod=pcli;
			pclidir=DT.getString(3);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			pcliente=pcli;
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

			if (reimpres==1) rep.add("-------  R E I M P R E S I O N  -------");
			if (reimpres==2) rep.add("------  C O N T A B I L I T A D  ------");
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

	
	
	// Aux
	
	private void msgAsk(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("ROAD");
			dialog.setMessage(msg  + " ?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					printDocument();
				}
			});
			dialog.setNegativeButton("No", null);
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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


}
