package com.dts.roadp;

import java.util.Currency;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AppMethods {

	private Context cont;
	private appGlobals gl;
	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	
	public AppMethods(Context context,appGlobals global,BaseDatos dbconnection, SQLiteDatabase database) {
		cont=context; 
		gl=global;
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}
	
	public void reconnect(BaseDatos dbconnection, SQLiteDatabase database) {
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}
	
	
	// Public
	
	public void parametrosExtra() {
		Cursor dt;
		String sql,val="";
		int ival;

		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);

		} catch (Exception e) {
			val="N";
		}

		if (val.equalsIgnoreCase("S"))gl.peStockItf=true; else gl.peStockItf=false;

		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=3";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peModal=dt.getString(0).toUpperCase();

		} catch (Exception e) {
			gl.peModal="-";
		}	
		
		//gl.peModal="APR";
		
		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=4";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
		} catch (Exception e) {
			val="N";
		}	

		if (val.equalsIgnoreCase("S"))gl.peSolicInv=true; else gl.peSolicInv=false;


		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=5";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peAceptarCarga=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peAceptarCarga=false;
		}	

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=6";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotInv=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotInv=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=7";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotPrec=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotPrec=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=8";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			gl.peBotStock=dt.getString(0).equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peBotStock=false;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=9";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<2)  ival=2;
			if (ival>10) ival=-1;
			gl.peDec=ival;
		} catch (Exception e) {
			gl.peDec=-1;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=10";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<0)  ival=0;
			if (ival>10) ival=10;
			gl.peDecImp=ival;
		} catch (Exception e) {
			gl.peDecImp=0;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=11";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			ival=Integer.parseInt(val);
			if (ival<1) ival=0;
			gl.peDecCant=ival;
		} catch (Exception e) {
			gl.peDecCant=0;
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=12";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peMon=val;
		} catch (Exception e) {
			Locale defaultLocale = Locale.getDefault();
			Currency currency = Currency.getInstance(defaultLocale);
			gl.peMon=currency.getSymbol();		
		}

		try {
			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=13";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			val=dt.getString(0);
			if (emptystr(val)) throw new Exception();

			gl.peVehAyud=val.equalsIgnoreCase("S");
		} catch (Exception e) {
			gl.peVehAyud=false;
		}

        try {
            sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=14";
            dt=Con.OpenDT(sql);
            dt.moveToFirst();

            val=dt.getString(0);
            if (emptystr(val)) throw new Exception();

            gl.peEscCliente=val.equalsIgnoreCase("S");
        } catch (Exception e) {
            gl.peEscCliente=false;
        }

	}

	
	// Common
	
	protected void toast(String msg) {
		Toast toast= Toast.makeText(cont,msg, Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}

	private boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}
	

}
