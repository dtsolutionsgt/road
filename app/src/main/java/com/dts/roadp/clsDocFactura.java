package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

public class clsDocFactura extends clsDocument {

	private ArrayList<itemData> items= new ArrayList<itemData>();
	
	private double tot,desc,imp,stot,percep;
	private boolean sinimp;
	private String 	contrib;
	private int decimp,diacred,totitems;
			
	public clsDocFactura(Context context,int printwidth,String cursymbol,int decimpres) {
		super(context, printwidth,cursymbol,decimpres);
		docfactura=true;
		decimp=decimpres;
	}


	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String cli="",vend="",val,empp="";
		int ff;
				
		super.loadHeadData(corel);
		
		nombre="FACTURA";
		
		try {
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,DESMONTO,IMPMONTO,EMPRESA,FECHA,ADD1,ADD2 FROM D_FACTURA WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			serie=DT.getString(0);
			numero=""+DT.getInt(1);
			ruta=DT.getString(2);
			
			vend=DT.getString(3);
			cli=DT.getString(4);
			
			tot=DT.getDouble(5);
			desc=DT.getDouble(6);
			imp=DT.getDouble(7);
			stot=tot+desc;
			
			empp=DT.getString(8);
			ffecha=DT.getInt(9);fsfecha=sfecha(ffecha);
			
			add1=DT.getString(10);
			add2=DT.getString(11);
			
		} catch (Exception e) {
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		try {
			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			resol="Resolucion No. : "+DT.getString(0);
			ff=DT.getInt(1);resfecha="De Fecha: "+sfecha(ff);
			ff=DT.getInt(2);resvence="Vigente hasta: "+sfecha(ff);
			//#EJC20181130: Se cambió el mensaje por revisión de auditor de SAT.
//			ff=DT.getInt(2);resvence="Resolucion vence : "+sfecha(ff);
			resrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);
			
		} catch (Exception e) {
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		try {
			sql="SELECT INITPATH FROM P_EMPRESA WHERE EMPRESA='"+empp+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
		
			String sim=DT.getString(0);
			sinimp=sim.equalsIgnoreCase("S");
			
		} catch (Exception e) {
			sinimp=false;
	    }	
				
		try {
			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+vend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
		} catch (Exception e) {
			val=vend;
	    }	
		
		vendedor=val;
		
		try {
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION,NIT,DIACREDITO FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
            percep=DT.getDouble(1);
            
            contrib=""+DT.getString(2);
			if (contrib.equalsIgnoreCase("C")) sinimp=true;
			if (contrib.equalsIgnoreCase("F")) sinimp=false;
			
			clicod=cli;
			clidir=DT.getString(3);
			nit=DT.getString(4);
			diacred=DT.getInt(5);
			
		} catch (Exception e) {
			val=cli;
	    }	
		
		try {
			sql="SELECT NOMBRE,NIT,DIRECCION FROM D_FACTURAF WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			cliente=DT.getString(0);
		    nit=DT.getString(1);
          	clidir=DT.getString(2);
					
		} catch (Exception e) {
	    }	
		
		
		try {
			sql="SELECT NOMBRE FROM P_REF1  WHERE CODIGO='"+add1+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			add1=add1+" - "+DT.getString(0);
		} catch (Exception e) {
	    }
				
		try {
			sql="SELECT NOMBRE FROM P_REF2  WHERE CODIGO='"+add2+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			add2=add2+" - "+DT.getString(0);
		} catch (Exception e) {
	    }
		
		//Toast.makeText(cont,"Percep "+percep+"  Sinimp "+sinimp, Toast.LENGTH_SHORT).show();
		
		//cliente=val;
		
		return true;
		
	}
	
	protected boolean loadDocData(String corel) {
		Cursor DT;
		itemData item;
		
		loadHeadData(corel);
		
		items.clear();
		
		try {
			sql="SELECT D_FACTURAD.PRODUCTO,P_PRODUCTO.DESCLARGA,D_FACTURAD.CANT,D_FACTURAD.PRECIODOC,D_FACTURAD.IMP, D_FACTURAD.DES,D_FACTURAD.DESMON, D_FACTURAD.TOTAL, D_FACTURAD.UMVENTA, D_FACTURAD.UMPESO, D_FACTURAD.PESO " +
				"FROM D_FACTURAD INNER JOIN P_PRODUCTO ON D_FACTURAD.PRODUCTO = P_PRODUCTO.CODIGO " +
				"WHERE (D_FACTURAD.COREL='"+corel+"')";	
			
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			totitems=DT.getCount();

			while (!DT.isAfterLast()) {
		
				item =new itemData();
		  	
				item.cod=DT.getString(0);
				item.nombre=DT.getString(1);
				
				item.cant=DT.getDouble(2);
				item.prec=DT.getDouble(3);
				item.imp=DT.getDouble(4);
				item.descper=DT.getDouble(5);
				item.desc=DT.getDouble(6);
				item.tot=DT.getDouble(7);				
				item.um=DT.getString(8);
				item.ump=DT.getString(9);
				item.peso=DT.getDouble(10);

				if (sinimp) item.tot=item.tot-item.imp;
				
				//Toast.makeText(cont,item.cod+" "+item.imp+"   "+item.tot, Toast.LENGTH_SHORT).show();
				
				items.add(item);	
				DT.moveToNext();					
			}				
			
		} catch (Exception e) {
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();
	    }		
		
		return true;
	}


	// Detalle por empresa

	protected boolean buildDetail() {
		if (modofact.equalsIgnoreCase("*")) return detailBase();
		if (modofact.equalsIgnoreCase("TOL")) return detailToledano();

		return false;
	}

	protected boolean detailToledano() {
		itemData item;
		String ss;

		rep.line();
		rep.add("CODIGO   DESCRIPCION        UM  CANT");
		rep.add("       KGS    PRECIO           VALOR");
		rep.line();

		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);

			ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
			ss=ss+rep.rtrim(item.um,4)+" "+rep.rtrim(frmdecimal(item.cant,2),5);
			rep.add(ss);
			ss=rep.rtrim(frmdecimal(item.peso,2),10)+" "+rep.rtrim(frmdecimal(item.prec,2),8);
			ss=rep.ltrim(ss,prw-10);
			ss=ss+" "+rep.rtrim(frmdecimal(item.tot,2),9);
			rep.add(ss);

		}

		rep.line();

		return true;
	}

	protected boolean detailBase() {
		itemData item;
		String cu,cp;

		rep.line();
		rep.add3fact("Cantidad      Peso","  Precio","Total");
		rep.line();

		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);
			rep.add(item.nombre);
			//rep.add3lrr(rep.rtrim(""+item.cant,5),item.prec,item.tot);

			cu=frmdecimal(item.cant,decimp)+" "+rep.ltrim(item.um,6);
			cp=frmdecimal(0,decimp)+" "+rep.ltrim(item.ump,3);

			rep.add3fact(cu+" "+cp,item.prec,item.tot);
		}

		rep.line();

		return true;
	}


	// Encabezado por empresa

	@Override
	protected void saveHeadLines(int reimpres) {


		if (modofact.equalsIgnoreCase("*")) super.saveHeadLines(reimpres);
		if (modofact.equalsIgnoreCase("TOL")) headerToledano(reimpres);
	}

	private void headerToledano(int reimpres) {
		String s,sc,ss;

		rep.empty();rep.empty();

		for (int i = 0; i <lines.size(); i++) 		{
			s=lines.get(i);
			s=encabezado(s);

			ss=s.replace(" ","");
			if (ss.equalsIgnoreCase("FACTURA")) {
				if (reimpres==4) s="- FACTURA PENDIENTE  DE  PAGO -";
			}

			if (!s.equalsIgnoreCase("@@")) rep.add(s);
		}

		sc="CONDICIONES DE PAGO: ";
		if (diacred==0) sc+="CONTADO"; else sc+="CREDITO "+diacred+" DIAS";
		rep.add(sc);
		rep.empty();
		rep.add("Fecha: "+sfecha(ffecha)+" Hora: "+shora(ffecha));
		rep.empty();

		if (!emptystr(add1)) {
			rep.add("");
			rep.add(add1);
			if (!emptystr(add2)) rep.add(add2);
			rep.add("");
		}

		if (docfactura && (reimpres==1)) rep.add("------  R E I M P R E S I O N  ------");
		if (docfactura && (reimpres==2)) rep.add("------       C O P I A         ------");
		if (docfactura && (reimpres==3)) rep.add("------      A N U L A D O      ------");
		if(docfactura && (reimpres==4)) {
			rep.add("- FACTURA PENDIENTE  DE  PAGO -");
			pendiente = reimpres;
		}

	}


	// Pie por empresa

	protected boolean buildFooter() {
		if (modofact.equalsIgnoreCase("*")) return footerBase();
		if (modofact.equalsIgnoreCase("TOL")) return footerToledano();

		return false;
	}

	private boolean footerBase() {
		double totimp,totperc;

		if (sinimp) {
			stot=stot-imp;
			totperc=stot*(percep/100);totperc=round2(totperc);
			totimp=imp-totperc;

			rep.addtotsp("Subtotal", stot);
			rep.addtotsp("Impuesto", totimp);
			if (contrib.equalsIgnoreCase("C")) rep.addtotsp("Percepcion", totperc);
			rep.addtotsp("Descuento", -desc);
			rep.addtotsp("TOTAL", tot);
		} else {
			if (desc!=0) {
				rep.addtotsp("Subtotal", stot);
				rep.addtotsp("Descuento", -desc);
			}
			rep.addtotsp("TOTAL A PAGAR ", tot);
		}

		rep.add("");
		rep.add("Sujeto a Pagos Trimestrales");
		rep.add("");

		//#HS_20181212 Validación para factura pendiente de pago
		if(pendiente == 4){
			rep.add("");
			rep.add("ESTE NO ES UN DOCUMENTO LEGAL");
			rep.add("EXIJA SU FACTURA ORIGINAL");
			rep.add("");
		}

		return super.buildFooter();
	}

	private boolean footerToledano() {
		double totimp, totperc;

		stot = stot - imp;
		totperc = stot * (percep / 100);
		totperc = round2(totperc);
		totimp = imp - totperc;

		rep.addtotsp("Subtotal", stot);
		rep.addtotsp("ITBM", totimp);
		rep.addtotsp("Total", tot);
		rep.add("");
		rep.add("Total de items: "+totitems);
		rep.add("");
		rep.add("");
		rep.line();
		rep.addc("Firma Cliente");
		rep.add("");
		rep.addc("DE SER UNA VENTA AL CREDITO, SOLAMEN");
		rep.addc("TE NUESTRO CORRESPONDIENTE RECIBO SE");
		rep.addc("CONSIDERARA COMO EVIDENCIA  DE  PAGO");
		rep.add("");

		rep.add("Serial : "+deviceid);
		rep.add(resol);
		rep.add(resfecha);
		rep.add("");

		return super.buildFooter();
	}


	// Aux
	
	public double round2(double val){
		int ival;
		
		val=(double) (100*val);
		double rslt=Math.round(val);
		rslt=Math.floor(rslt);
		
		ival=(int) rslt;
		rslt=(double) ival;
		
		return (double) (rslt/100);
	}
	
	private class itemData {
		public String cod,nombre,um,ump;
		public double cant,peso,prec,imp,descper,desc,tot;
	}
	
	
}
