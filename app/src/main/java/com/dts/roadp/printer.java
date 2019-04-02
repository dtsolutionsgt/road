package com.dts.roadp;

import com.epson.eposdevice.Device;
import com.epson.eposdevice.printer.Printer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;

public class printer {

	public int prw;
	
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String sql;
	
	private Context cont;

	private Runnable printclose;
	private appGlobals gl;
	
	private printBase prn;
	private int prid;
	private String prpar;


	public printer(Context context, Runnable printclosecall) {

		String prsid;

		cont = context;

		prid = 0;
		prpar = "";
		prw = 32;

		printclose = printclosecall;

		gl = ((appGlobals) (((Activity) cont).getApplication()));

		try {

			//if (gl.mPrinterSet) prsid="SET";else prsid="void";
			//Toast.makeText(cont, "Printer : "+prsid, Toast.LENGTH_SHORT).show();
		} catch (Exception e2) {
			Log.d("Printer_Init_Err", e2.getMessage());
		}

		try {

			Con = new BaseDatos(cont);
			opendb();

			setPrinterType();

			try {
				Con.close();
			} catch (Exception e1) {
			}

		} catch (Exception e) {
			Toast.makeText(cont, "Printer : " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}

	public void printask(Runnable callBackHook)    {
		if (emptyparam()) return;
		if (prid>0) prn.printask(callBackHook);
	}
	
	public void printask() {
		if (emptyparam()) return;
		if (prid>0) prn.printask();
	}

	public void printask(Runnable callBackHook, String fName)    {
		if (emptyparam()) return;
		if(prid>0) prn.printask(callBackHook,fName);
	}

	public boolean print() {
		if (emptyparam()) return false;
		if (prid>0) return prn.print();else return true;
	}
		
	public void printask(String fileName) {	
		if (emptyparam()) return;
		if (prid>0) prn.printask(fileName);
	}
			
	public boolean print(String fileName) {
		if (emptyparam()) return false;
		if (prid>0) return prn.print(fileName);else return true;
	}
	
	public boolean isEnabled() {
		return prid!=0;
	}
	
	public void setPrinterType(String ptipo,String pparam,Device device,Printer printer) {
		
		prpar=pparam;
		
		prid=0;	
		if (ptipo.equalsIgnoreCase("DATAMAX")) prid=1;
		if (ptipo.equalsIgnoreCase("EPSON")) prid=2;
		if (ptipo.equalsIgnoreCase("ZEBRA CPCL")) prid=3;
			
		setPrinterClass();
			
	}
	
	
	// Aux
	
	private void setPrinterType() 	{
		Cursor DT;
		String prtipo;
		
		prid=0;prpar="";
		
		try {
			sql="SELECT COL_IMP FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			prw=DT.getInt(0);
		} catch (Exception e) {
			prw=32;
		}		
		
		try {
			sql="SELECT TIPO_IMPRESORA,PUERTO_IMPRESION FROM P_ARCHIVOCONF";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			prtipo=DT.getString(0);
			prpar=DT.getString(1);
			
			if (prtipo.equalsIgnoreCase("DATAMAX")) prid=1;
			if (prtipo.equalsIgnoreCase("EPSON")) prid=2;
			if (prtipo.equalsIgnoreCase("ZEBRA CPCL")) prid=3;
				
			setPrinterClass();
				
		} catch (Exception e) {
			prid=0;prpar="";
			//Toast.makeText(cont, "Printer 2222 : "+e.getMessage(), Toast.LENGTH_SHORT).show();
			Toast.makeText(cont, "Falta definir impresora ", Toast.LENGTH_SHORT).show();
	    }
				
	}
	
	private void setPrinterClass() {
		switch (prid) {
			case 1:  
				prn=new printDMax(cont,prpar);
				prn.printclose=printclose;
				break;	
			case 2:
				break;		
			case 3:  
				prn=new printZebraCPCL(cont,prpar);
				prn.printclose=printclose;
				prn.prwidth=prw;
				break;		
		}	
	}
	
	public boolean emptyparam(){
		if (prpar==null || prpar.isEmpty()) {
			return true;
		} else {
			return prpar.equalsIgnoreCase(" ");
		}
	}
	
	private void opendb() {
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
	    } catch (Exception e) {
	    }
	}	
	
	
}
