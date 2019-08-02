package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocPedido extends clsDocument {

	private ArrayList<itemData> items= new ArrayList<itemData>();
	
	private double tot,desc,imp,stot,percep;
	private boolean sinimp;
	private String 	contrib;
	private int decimp;
	
	public clsDocPedido(Context context,int printwidth,String cursymbol,int decimpres, String archivo) {
		super(context, printwidth,cursymbol,decimpres,archivo);
		docpedido=true;
		docfactura=false;
		docrecibo=false;
		docdevolucion=false;
		decimp=decimpres;
	}
	
	protected boolean buildDetail() {
		itemData item;
		String cu,cp;

		rep.line();
		rep.add("CODIGO   DESCRIPCION                ");
		rep.add("CANT    UM   KGS   PRECIO     VALOR ");
		rep.line();
		//rep.empty();
		
		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);
			rep.add(item.cod + " " + item.nombre);
			//rep.add3lrr(rep.rtrim(""+item.cant,5),item.prec,item.tot);

			cu=frmdecimal(item.cant,decimp)+" "+rep.ltrim(item.um.substring(0,3),6);
			cp=frmdecimal(0,decimp)+" "+rep.ltrim(item.ump,3);
			
			rep.add3fact(cu+" "+cp,item.prec,item.tot);
		}
		
		rep.line();
		
		return true;
	}
	
	protected boolean buildFooter() {
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
			rep.addtotsp("TOTAL A PAGAR", tot);			
		}

		rep.empty();
		rep.add("No. Serie : "+deviceid);
		rep.empty();

		return super.buildFooter();
	}	
		
	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String cli,vend,val,empp, anulado;
		int ff;
		int impres, cantimpres;
				
		super.loadHeadData(corel);
		
		nombre="PEDIDO";
		
		try {
			sql=" SELECT COREL,'' as CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,DESMONTO,IMPMONTO,EMPRESA,FECHA,ADD1,ADD2, IMPRES, ANULADO " +
				" FROM D_PEDIDO WHERE COREL='"+corel+"'";
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

			anulado=DT.getString(13);
			impres=DT.getInt(12);
			cantimpres=0;

			if (anulado.equals("S")?true:false){
				cantimpres = -1;
			}else if (cantimpres == 0 && impres > 0){
				cantimpres = 1;
			}

			if (cantimpres>0){
				nombre = "COPIA DE PEDIDO";
			}else if (cantimpres==-1){
				nombre = "PEDIDO ANULADO";
			}else if (cantimpres==0){
				nombre = "PEDIDO";
			}

			empp=DT.getString(8);
			ffecha=DT.getInt(9);fsfecha=sfecha(ffecha);
			
			add1=DT.getString(10);
			add2=DT.getString(11);
			
		} catch (Exception e) {
			Toast.makeText(cont,"Ped head 1 : "+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		try {
			sql="SELECT RESOL,FECHARES,FECHAVIG,SERIE,CORELINI,CORELFIN FROM P_COREL";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			resol="Resolucion No. : "+DT.getString(0);
			ff=DT.getInt(1);resfecha="De Fecha : "+sfecha(ff);
			ff=DT.getInt(2);resvence="Resolucion vence : "+sfecha(ff);
			resrango="Serie : "+DT.getString(3)+" del "+DT.getInt(4)+" al "+DT.getInt(5);
			
		} catch (Exception e) {
			Toast.makeText(cont,"Ped head 2 : "+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
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

	    vendcod=vend;
		vendedor=val;

		try {
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION,NIT FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
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
			cliente=DT.getString(0);
			
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
			sql="SELECT D_PEDIDOD.PRODUCTO,P_PRODUCTO.DESCLARGA,D_PEDIDOD.CANT,D_PEDIDOD.PRECIODOC," +
				"D_PEDIDOD.IMP, D_PEDIDOD.DES,D_PEDIDOD.DESMON, D_PEDIDOD.TOTAL, D_PEDIDOD.UMVENTA, D_PEDIDOD.UMPESO " +
				"FROM D_PEDIDOD INNER JOIN P_PRODUCTO ON D_PEDIDOD.PRODUCTO = P_PRODUCTO.CODIGO " +
				"WHERE (D_PEDIDOD.COREL='"+corel+"')";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
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
				
				if (sinimp) item.tot=item.tot-item.imp;
				
				//Toast.makeText(cont,item.cod+" "+item.imp+"   "+item.tot, Toast.LENGTH_SHORT).show();
				
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
		public String cod,nombre,um,ump;
		public double cant,prec,imp,descper,desc,tot;
	}
	
	
}
