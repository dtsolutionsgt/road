package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocCobro extends clsDocument {

	private ArrayList<itemData> items= new ArrayList<itemData>();
	
	private double tot,desc,imp,stot,percep;
	private boolean sinimp;
	private String 	contrib,recfact;
	
	public clsDocCobro(Context context,int printwidth,String cursymbol,int decimpres) {
		super(context, printwidth,cursymbol,decimpres);
		docfactura=false;
		docrecibo=true;
	}
	
	protected boolean buildDetail() {
		itemData item;
		
		rep.line();
		//rep.empty();
		
		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);
			rep.addp("Factura "+ item.cod,"");
		}
		
		rep.line();
		
		return true;
	}
	
	protected boolean buildFooter() {
		rep.addtot("TOTAL PAGO", tot);
		
		return super.buildFooter();
	}	
		
	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String cli,vend,val;
				
		super.loadHeadData(corel);
		
		nombre="RECIBO";
		
		try {
			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,FECHA FROM D_COBRO WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			serie=DT.getString(0);
			numero=""+DT.getInt(1);
			ruta=DT.getString(2);
			
			vend=DT.getString(3);
			cli=DT.getString(4);
			
			tot=DT.getDouble(5);
			ffecha=DT.getInt(6);fsfecha=sfecha(ffecha);
					
		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
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
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
            percep=DT.getDouble(1);
            
            contrib=""+DT.getString(2);
			if (contrib.equalsIgnoreCase("C")) sinimp=true;
			if (contrib.equalsIgnoreCase("F")) sinimp=false;
			
			clicod=cli;
			clidir=DT.getString(3);
			
		} catch (Exception e) {
			val=cli;
	    }	
		
		//Toast.makeText(cont,"Percep "+percep+"  Sinimp "+sinimp, Toast.LENGTH_SHORT).show();
		
		cliente=val;
		
		return true;
		
	}
	
	protected boolean loadDocData(String corel) {
		Cursor DT;
		itemData item;
		
		loadHeadData(corel);
		
		items.clear();
		
		try {
			sql="SELECT DOCUMENTO FROM D_COBROD WHERE COREL='"+corel+"'";	
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {

				item =new itemData();
				item.cod="SIN REFERENCIA";
				items.add(item);

				return true;
			}

			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
		
				item =new itemData();		  	
				item.cod=DT.getString(0);				
				items.add(item);	
				
				DT.moveToNext();					
			}				
			
		} catch (Exception e) {
	    }		
		
		return true;
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
		public String cod,nombre;
		public double cant,prec,imp,descper,desc,tot;
	}
	
}
