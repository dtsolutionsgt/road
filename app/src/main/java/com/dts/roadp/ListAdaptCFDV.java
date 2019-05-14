package com.dts.roadp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Currency;
import java.util.Locale;

public class ListAdaptCFDV {

	private Context cont;
	private appGlobals gl;
	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	private String sql;
	private String sp;

	public ListAdaptCFDV(Context context, appGlobals global, BaseDatos dbconnection, SQLiteDatabase database) {

		cont = context;
		gl = global;
		Con = dbconnection;
		db = database;

		ins = Con.Ins;
		upd = Con.Upd;
	}

	public void reconnect(BaseDatos dbconnection, SQLiteDatabase database) {
		Con = dbconnection;
		db = database;

		ins = Con.Ins;
		upd = Con.Upd;
	}

	//Función para saber la cantidad de registros en una tabla
	public int getDocCount(String ss, String pps) {

		Cursor DT;
		int cnt = 0;
		String st;

		try {
			sql = ss;
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				cnt = DT.getCount();
				st = pps + " " + cnt;
				sp = sp + st + "\n";
			}

			return cnt;
		} catch (Exception e) {
			//mu.msgbox(sql+"\n"+e.getMessage());
			return 0;
		}
	}

	//Función para saber la cantidad de registros en una tabla específica
	public int getDocCountTipo(String tipo, boolean sinEnviar) {

		Cursor DT;
		int cnt = 0;
		String st, ss;
		String pps = "";

		try {

			switch (tipo) {
				case "Facturas":

					sql = "SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_FACTURA";
					sql += (sinEnviar ? " WHERE STATCOM = 'N'" : "");
					break;

				case "Pedidos":

					sql = "SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_PEDIDO";
					sql += (sinEnviar ? " WHERE STATCOM = 'N'" : "");
					break;

				case "Cobros":

					sql = "SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_COBRO";
					sql += (sinEnviar ? " WHERE STATCOM = 'N'" : "");
					break;

				case "Devolucion":

					sql = "SELECT IFNULL(COUNT(COREL),0) AS CANT FROM D_NOTACRED";
					sql += (sinEnviar ? " WHERE STATCOM = 'N'" : "");
					break;

				case "Inventario":

					sql = "";

			}
		} catch (Exception e) {

		}
		return 1;
	}
}