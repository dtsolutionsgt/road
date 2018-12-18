package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocument {

	public String nombre,numero,serie,ruta,vendedor,cliente,nit;	
	public String resol, resfecha,resvence,resrango,fsfecha;
	public String tf1="",tf2="",tf3="",tf4="",tf5="",add1="",add2="";
	public clsRepBuilder rep;
	public boolean docfactura,docrecibo=false,docanul=false,docpedido=false;
	public int ffecha,pendiente;
	
	protected android.database.sqlite.SQLiteDatabase db;
	protected BaseDatos Con;
	protected String sql;
	
	protected ArrayList<String> lines= new ArrayList<String>();
	
	protected Context cont;
	protected DateUtils DU;
	protected DecimalFormat decfrm;
	
	protected String clicod,clidir,pemodo;
	
	private int prw,residx;
	
	public clsDocument(Context context,int printwidth,String cursym,int decimpres) {
		cont=context;
		prw=printwidth;
		
		rep=new clsRepBuilder(cont,prw,true,cursym,decimpres);
		DU=new DateUtils();
		decfrm = new DecimalFormat("#,##0.00");

	}
	
	public boolean buildPrint(String corel,int reimpres) {
		
		rep.clear();
				
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrint(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb ) {
		
		rep.clear();
				
		if (!buildHeader(corel,reimpres,pCon,pdb)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintExt(String corel,int reimpres,String modo) {
		
		pemodo=modo;
		
		rep.clear();
		
		if (!buildHeader(corel,0)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintSimple(String corel,int reimpres) {
		
		rep.clear();
		
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}

	
	// Methods Prototypes
	
	protected boolean buildDetail() {
		return true;
	}
	
	protected boolean buildFooter() {
		
		return true;
	}	
	
	protected boolean loadDocData(String corel) {
		return true;
	}
	
	protected boolean loadHeadData(String corel) {
		nombre="";numero="";serie="";ruta="";vendedor="";cliente="";
		
		return true;
	}
	
	// Private
	
	private boolean buildHeader(String corel,int reimpres) {
		
		lines.clear();
		
		try {
			Con = new BaseDatos(cont);
			opendb();
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);	
			}
			loadHeadLines();
			
			try {
				Con.close();   
			} catch (Exception e1) { }
			
		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}		
		
		saveHeadLines(reimpres);
		
		return true;
	}
	
	private boolean buildHeader(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb) {
		
		lines.clear();
		
		try {
				
			Con=pCon;db=pdb;
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);	
			}
			loadHeadLines();
						
		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}		
		
		saveHeadLines(reimpres);
		
		return true;
	}
	
	
	private void saveHeadLines(int reimpres)
	{

		String s;
		
		rep.empty();rep.empty();
		
		for (int i = 0; i <lines.size(); i++)
		{
			
			s=lines.get(i);
			s=encabezado(s);
			
			if (docpedido) {
				s=s.replace("Factura serie","Pedido");
				s=s.replace("numero : 0","");
			}
			
			if (docrecibo) {
				s=s.replace("Factura","Recibo");
			}
			
			if (residx==1) {
				if (docfactura) {
					rep.add(resol);
					rep.add(resfecha);
					rep.add(resvence);
					rep.add(resrango);
					rep.add("Fecha de Emision : "+fsfecha);	
				}			
				residx=0;
			}
			if (!s.equalsIgnoreCase("@@")) rep.add(s);
		}

		if (!emptystr(nit)) rep.add("NIT : "+nit);
		if (!emptystr(clidir)) rep.add("Dir : "+clidir);
		if (!emptystr(clicod)) rep.add("Codigo: "+clicod);

		
		if (!emptystr(add1)) {
			rep.add("");
			rep.add(add1);
			if (!emptystr(add2)) rep.add(add2);
			rep.add("");
		}
		
		if (docfactura && (reimpres==1)) rep.add("-------  R E I M P R E S I O N  -------");
		//if (docfactura && (reimpres==2)) rep.add("------  C O N T A B I L I T A D  ------");
		if (docfactura && (reimpres==2)) rep.add("------  C O P I A  ------");
		if (docfactura && (reimpres==3)) rep.add("------       A N U L A D O      ------");
		//#HS_20181212 condición para factura pendiente de pago
		if(docfactura && (reimpres==4)) {
			rep.add("- P E N D I E N T E  D E  P A G O -");
			pendiente = reimpres;
		}
		
	}
	
	private String encabezado(String l) {
		String s,lu;
		int idx;
		
		residx=0;
		
		//lu=l.toUpperCase().trim();
		lu=l.trim();
			
		if (lu.length()==1 && lu.equalsIgnoreCase("N")) {
			s=nombre;s=rep.ctrim(s);return s;
		}
		
		if (l.indexOf("dd-MM-yyyy")>=0) {
			s=DU.sfecha(DU.getActDateTime());
			l=l.replace("dd-MM-yyyy",s);return l;
		}
		
		if (l.indexOf("HH:mm:ss")>=0) {
			s=DU.shora(DU.getActDateTime());
			l=l.replace("HH:mm:ss",s);return l;
		}
		
		idx=lu.indexOf("SS");
		if (idx>=0) {
			if (emptystr(serie)) return "@@";
			if (emptystr(numero)) return "@@";
		
			s=lu.substring(0,idx);
			s=s+serie+" numero : "+numero;
			residx=1;
			return s;
		}
		
		idx=lu.indexOf("VV");
		if (idx>=0) {
			if (emptystr(vendedor)) return "@@";
			l=l.replace("VV",vendedor);return l;
		}
		
		idx=lu.indexOf("RR");
		if (idx>=0) {
			if (emptystr(ruta)) return "@@";
			l=l.replace("RR",ruta);return l;
		}
		
		idx=lu.indexOf("CC");
		if (idx>=0) {
			if (emptystr(cliente)) return "@@";
			l=l.replace("CC",cliente);return l;
		}		
		
		return l;
	}
		
	
	// Aux
	
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
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}		
		
	}
	
	public boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}
	
	public String sfecha(int f) {
		int vy,vm,vd;
		String s;
		
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		
		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"-";} else {s=s+"0"+String.valueOf(vd)+"-";}  
		if (vm>9) { s=s+String.valueOf(vm)+"-20";} else {s=s+"0"+String.valueOf(vm)+"-20";}  
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);} 
		
		return s;
	}
	
	public String frmdecimal(double val,int ndec) {
		String ss="",ff="#,##0.";
		DecimalFormat ffrmint = new DecimalFormat("#,##0"); 
		
		if (ndec<=0) {		
			ss=ffrmint.format((int) val);return ss;
		}
		
		for (int i = 1; i <ndec+1; i++) {
			ff=ff+"0";
		}
		
		DecimalFormat decim = new DecimalFormat(ff);
		ss=decim.format(val);
		
		return ss;
	}

	public void toast(String msg) {
		Toast.makeText(cont,msg, Toast.LENGTH_SHORT).show();	
	}
	
	private void opendb() {
		
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
	    } catch (Exception e) {
	    	Toast.makeText(cont,"Opendb "+e.getMessage(), Toast.LENGTH_LONG).show();
	    }
	}	
		
}
