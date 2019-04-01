package com.dts.roadp;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocDepos extends clsDocument {
	
	private ArrayList<itemData> items= new ArrayList<itemData>();

	private double tot,tote,totc;
	private int numc;
	private String banco,cuenta,ref;
	
	public clsDocDepos(Context context, int printwidth,String pruta,String pvend,String cursymbol,int decimpres, String archivo) {
		super(context, printwidth,cursymbol,decimpres, archivo);
		docfactura=false;
		
		nombre="DEPOSITO";
		numero="";
		serie="";
		cliente="";	
		ruta=pruta;
		vendedor=pvend;
		
	}

	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String val;
				
		//super.loadHeadData(corel);
		
		//nombre="DEPOSITO";
			
		try {
			sql="SELECT BANCO,CUENTA,REFERENCIA,TOTAL,TOTEFEC,TOTCHEQ,NUMCHEQ FROM D_DEPOS WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
			cuenta=DT.getString(1);	
			ref=DT.getString(2);
			
			tot=DT.getDouble(3);
			tote=DT.getDouble(4);
			totc=DT.getDouble(5);
			numc=DT.getInt(6);
			
		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		try {
			sql="SELECT NOMBRE FROM P_BANCO  WHERE CODIGO='"+val+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			banco=DT.getString(0);
		} catch (Exception e) {
			banco=val;
	    }	
			
		
		return true;
		
	}	
	
	protected boolean loadDocData(String corel) {
		Cursor DT;
		itemData item;
		double val;
		String ss;
		
		items.clear();
	
		
		try {
			
			sql="SELECT D_FACTURA.SERIE,D_FACTURA.CORELATIVO,D_DEPOSD.MONTO,P_MEDIAPAGO.NOMBRE,D_DEPOSD.NUMERO,D_DEPOSD.CHEQUE " +
				"FROM D_DEPOSD INNER JOIN D_FACTURA ON D_DEPOSD.DOCCOREL = D_FACTURA.COREL INNER JOIN P_MEDIAPAGO ON D_DEPOSD.CODPAGO=P_MEDIAPAGO.CODIGO " +
				"WHERE (D_DEPOSD.TIPODOC='F') AND (D_DEPOSD.COREL='"+corel+"') ORDER BY D_FACTURA.SERIE,D_FACTURA.CORELATIVO";

			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
		
				item =new itemData();
		  	
				item.nombre=DT.getString(0)+" - "+DT.getInt(1);
				item.monto=DT.getDouble(2);
				item.tipo=DT.getString(3);
				
				ss=DT.getString(5);
				item.num=" ";
				if (ss.equalsIgnoreCase("S")) item.num=""+DT.getString(4);
			
				items.add(item);	
				DT.moveToNext();					
			}				
			
		} catch (Exception e) {
	    }		
		
		return true;
	}	
		
	protected boolean buildDetail() {
		itemData item;
		
		rep.empty();
		rep.add("Banco : "+banco);
		rep.add("Cuenta : "+cuenta);
		rep.add("Boleto : "+ref);
		rep.line();
		
		rep.addtot("Total efectivo :", rep.rtrim(rep.frmdec(tote),12));
		rep.addtot("Total cheques :", rep.rtrim(rep.frmdec(totc),12));
		rep.line();
		rep.addtot("Total  :", rep.rtrim(rep.frmdec(tot),12));
		rep.add("Cheques :"+numc);
		rep.line();
		rep.empty();
		
		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);
			
			rep.add(item.nombre);
			rep.add3lrr(item.tipo,item.num,item.monto);
		}
		
		rep.line();
		rep.empty();
		
		return true;
	}	
	
	// Aux
	
	private class itemData {
		public String cod,nombre,tipo,num;
		public double monto;
	}
		
	
	
}
