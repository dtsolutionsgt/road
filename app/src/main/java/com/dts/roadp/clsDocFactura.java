package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import java.util.ArrayList;

public class clsDocFactura extends clsDocument {

	private ArrayList<itemData> items= new ArrayList<itemData>();
	private ArrayList<itemData> bons= new ArrayList<itemData>();

	private double tot,desc,imp,stot,percep,totNotaC;
	private boolean sinimp;
	private String 	contrib,corelNotaC,asignacion,ccorel,corelF;
	private int decimp,diacred,totitems;


	public clsDocFactura(Context context,int printwidth,String cursymbol,int decimpres, String archivo) {
		super(context, printwidth,cursymbol,decimpres, archivo);

		docfactura=true;
		docdevolucion=false;
		docpedido=false;
		docrecibo=false;
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

			vendcod=vend;

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
		itemData item,bon;
		String serie,corNota;
		int corrl;

		ccorel=corel;

		loadHeadData(corel);
		
		items.clear();bons.clear();
		
		try {

            sql="SELECT N.COREL, F.COREL, N.TOTAL, N.FACTURA " +
                "FROM D_FACTURA F INNER JOIN D_NOTACRED N ON F.COREL = N.FACTURA " +
                "WHERE F.COREL = '"+corel+"'";

            DT=Con.OpenDT(sql);
            DT.moveToFirst();

            if(DT.getCount() != 0){

				corelNotaC = DT.getString(0);
				corelF = DT.getString(1);
				totNotaC = DT.getDouble(2);
				asignacion = DT.getString(3);

			}else{

            	corelNotaC = "";
            	asignacion = "*";
            	totNotaC = 0;
				corelF = "";

            }

			sql="SELECT D_FACTURAD.PRODUCTO,P_PRODUCTO.DESCLARGA,D_FACTURAD.CANT,D_FACTURAD.PRECIODOC,D_FACTURAD.IMP, D_FACTURAD.DES,D_FACTURAD.DESMON, D_FACTURAD.TOTAL, D_FACTURAD.UMVENTA, D_FACTURAD.UMPESO, D_FACTURAD.PESO " +
				"FROM D_FACTURAD INNER JOIN P_PRODUCTO ON D_FACTURAD.PRODUCTO = P_PRODUCTO.CODIGO " +
				"WHERE (D_FACTURAD.COREL='"+corel+"')";	
			
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			totitems=DT.getCount();

			while (!DT.isAfterLast()) {

                item = new itemData();

                item.cod = DT.getString(0);
                item.nombre = DT.getString(1);

                item.cant = DT.getDouble(2);
                item.prec = DT.getDouble(3);
                item.imp = DT.getDouble(4);
                item.descper = DT.getDouble(5);
                item.desc = DT.getDouble(6);
                item.tot = DT.getDouble(7);
                item.um = DT.getString(8);
                item.ump = DT.getString(9);
                item.peso = DT.getDouble(10);

                if (sinimp) item.tot = item.tot - item.imp;

                items.add(item);
                DT.moveToNext();
            }


			try {
				sql = "SELECT D_BONIF.PRODUCTO,P_PRODUCTO.DESCLARGA AS NOMBRE,D_BONIF.CANT, D_BONIF.UMVENTA, D_BONIF.CANT*D_BONIF.FACTOR AS TPESO " +
						"FROM D_BONIF INNER JOIN P_PRODUCTO ON D_BONIF.PRODUCTO = P_PRODUCTO.CODIGO " +
						"WHERE (D_BONIF.COREL='" + ccorel + "')";
				sql += "UNION ";
				sql += "SELECT D_BONIF_BARRA.PRODUCTO,P_PRODUCTO.DESCLARGA AS NOMBRE,COUNT(D_BONIF_BARRA.BARRA) AS CANT, D_BONIF_BARRA.UMSTOCK, SUM(D_BONIF_BARRA.PESO) AS TPESO " +
						"FROM D_BONIF_BARRA INNER JOIN P_PRODUCTO ON D_BONIF_BARRA.PRODUCTO = P_PRODUCTO.CODIGO " +
						"WHERE (D_BONIF_BARRA.COREL='" + ccorel + "') " +
						"GROUP BY D_BONIF_BARRA.PRODUCTO,P_PRODUCTO.DESCLARGA,D_BONIF_BARRA.UMVENTA ";
				sql += "ORDER BY NOMBRE ";

				DT=Con.OpenDT(sql);
				if (DT.getCount()>0) DT.moveToFirst();

				while (!DT.isAfterLast()) {

					bon = new itemData();

					bon.cod = DT.getString(0);
					bon.nombre = DT.getString(1);
					bon.cant = DT.getDouble(2);
					bon.um = DT.getString(3);
					bon.peso = DT.getDouble(4);

					bons.add(bon);
					DT.moveToNext();
				}

			} catch (Exception e) {
				Toast.makeText(cont,"Impresion bonif : "+e.getMessage(), Toast.LENGTH_LONG).show();
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

	// Bonificaciones

	private void bonificaciones() {
		itemData item;
		String ss;

		if (bons.size()==0) return;

		rep.line();
		rep.add("----   B O N I F I C A C I O N  ----");
		rep.line();
		rep.add("CODIGO   DESCRIPCION        UM  CANT");
		rep.add("       KGS    ");
		rep.line();

		for (int i = 0; i <bons.size(); i++) {

			item=bons.get(i);

			ss=rep.ltrim(item.cod+" "+item.nombre,prw-10);
			ss=ss+rep.rtrim(item.um,4)+" "+rep.rtrim(frmdecimal(item.cant,2),5);
			rep.add(ss);
			ss=rep.rtrim(frmdecimal(item.peso,2),10);
			ss=rep.ltrim(ss,prw-10);
			rep.add(ss);

		}

		rep.line();

		rep.add("");
		rep.add("");
		rep.add("");
		rep.add("");
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
		double totimp, totperc,totalNotaC;

		stot = stot - imp;
		totperc = stot * (percep / 100);
		totperc = round2(totperc);
		totimp = imp - totperc;
		totalNotaC =   tot - totNotaC;

		rep.addtotsp("Subtotal", stot);

		if (corelF.equals(asignacion)) {

			rep.addtotsp("Nota de Credito", totNotaC);
			rep.addtotsp("ITBM", totimp);
			rep.addtotsp("Total", totalNotaC);
			rep.add("");
			rep.add("");
			rep.add("Total de items: "+totitems);
			rep.add("");
			bonificaciones();
			rep.add("");
			rep.line();
			rep.addc("Firma Cliente");
			rep.add("");
			rep.addc("Se aplico nota de credito: "+corelNotaC);
			rep.add("");
			rep.addc("DE SER UNA VENTA AL CREDITO, SOLAMEN");
			rep.addc("TE NUESTRO CORRESPONDIENTE RECIBO SE");
			rep.addc("CONSIDERARA COMO EVIDENCIA  DE  PAGO");
			rep.add("");

			rep.add("Serial : "+deviceid);
			rep.add(resol);
			rep.add(resfecha);
			rep.add("");

		} else {

			rep.addtotsp("ITBM", totimp);
			rep.addtotsp("Total", tot);
			rep.add("");
			rep.add("");
			rep.add("Total de items: "+totitems);
			rep.add("");
			bonificaciones();
			rep.add("");
			rep.line();
			rep.addc("Firma Cliente");
			rep.add("");

			if (pendiente!=4){
				rep.addc("DE SER UNA VENTA AL CREDITO, SOLAMEN");
				rep.addc("TE NUESTRO CORRESPONDIENTE RECIBO SE");
				rep.addc("CONSIDERARA COMO EVIDENCIA  DE  PAGO");
				rep.add("");
			}

			rep.add("Serial : "+deviceid);
			rep.add(resol);
			rep.add(resfecha);
			rep.add("");

		}

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
