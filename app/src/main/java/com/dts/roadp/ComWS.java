package com.dts.roadp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class ComWS extends PBase {

	private TextView lblInfo, lblParam, lblRec, lblEnv, lblExis;
	private ProgressBar barInfo;
	private EditText txtRuta, txtWS, txtEmp;
	private ImageView imgRec, imgEnv, imgExis;
	private RelativeLayout ralBack, relExist, relPrecio, relStock;

	private int isbusy, fecha, lin, reccnt, ultcor, ultcor_ant;
	private String err, ruta, rutatipo, sp, docstock, ultSerie, ultSerie_ant;
	private boolean fFlag, showprogress, pendientes, envioparcial, findiaactivo, errflag;

	private SQLiteDatabase dbT;
	private BaseDatos ConT;
	private BaseDatos.Insert insT;
	private AppMethods clsAppM;

	private ArrayList<String> listItems = new ArrayList<String>();
	private ArrayList<String> results = new ArrayList<String>();

	private ArrayList<clsClasses.clsEnvio> items = new ArrayList<clsClasses.clsEnvio>();
	private ListAdaptEnvio adapter;

	private clsDataBuilder dbld;
	private clsLicence lic;
	private clsFinDia claseFindia;
	private DateUtils DU;
	private String jsonWS;

	// Web Service -

	public AsyncCallRec wsRtask;
	public AsyncCallSend wsStask;
	public AsyncCallConfirm wsCtask;

	private static String sstr, fstr, fprog, finf, ferr, fterr, idbg, dbg, ftmsg, esql, ffpos;
	private int scon, running, pflag, stockflag, conflag;
	private String ftext, slsync, senv, gEmpresa, ActRuta, mac, fsql, fsqli, fsqlf, strliqid;
	private boolean rutapos, ftflag, esvacio, liqid;

	private final String NAMESPACE = "http://tempuri.org/";
	private String METHOD_NAME, URL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_ws);

		super.InitBase();
		addlog("ComWS", "" + du.getActDateTime(), gl.vend);

		System.setProperty("line.separator", "\r\n");

		dbld = new clsDataBuilder(this);
		claseFindia = new clsFinDia(this);
		clsAppM = new AppMethods(this, gl, Con, db);

		lblInfo = (TextView) findViewById(R.id.lblETipo);
		lblParam = (TextView) findViewById(R.id.lblProd);
		barInfo = (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta = (EditText) findViewById(R.id.txtRuta);
		txtRuta.setEnabled(false);
		txtWS = (EditText) findViewById(R.id.txtWS);
		txtWS.setEnabled(false);
		txtEmp = (EditText) findViewById(R.id.txtEmp);
		txtEmp.setEnabled(false);

		lblRec = (TextView) findViewById(R.id.btnRec);
		lblEnv = (TextView) findViewById(R.id.btnSend);

		imgEnv = (ImageView) findViewById(R.id.imageView6);
		imgRec = (ImageView) findViewById(R.id.imageView5);

		ralBack = (RelativeLayout) findViewById(R.id.relwsmail);
		relExist = (RelativeLayout) findViewById(R.id.relExist);
		relPrecio = (RelativeLayout) findViewById(R.id.relPrecio);
		relStock = (RelativeLayout) findViewById(R.id.relStock);

		isbusy = 0;

		lblInfo.setText("");
		lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);

		ruta = gl.ruta;
		ActRuta = ruta;
		gEmpresa = gl.emp;
		rutatipo = gl.rutatipog;
		rutapos = gl.rutapos;

		if (gl.tipo == 0) {
			this.setTitle("Comunicación");
		} else {
			this.setTitle("Comunicación Local");
		}

		getWSURL();

		mac = getMac();
		fsql = du.univfechasql(du.getActDate());
		fsqli = du.univfechasql(du.ffecha00(du.getActDate())) + " 00:00:00";
		fsqlf = du.univfechasql(du.ffecha24(du.getActDate())) + " 23:59:59";

		lic = new clsLicence(this);

		pendientes = validaPendientes();

		visibilidadBotones();

		//if (gl.autocom==1) runSend();

		//relExist.setVisibility(View.VISIBLE);

		envioparcial = gl.peEnvioParcial;

		if (esvacio) txtWS.setEnabled(true);

		//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
		if (gl.debug) {
			if (mu.emptystr(txtRuta.getText().toString())) {
				txtRuta.setText("8001-1");
				txtEmp.setText("03");
				txtWS.setText("http://192.168.1./wsAndr/wsandr.asmx");
			}
		}


		//txtRuta.setText("0005-1");
		//txtWS.setText("http://192.168.1.112/wsAndr/wsandr.asmx");
		//txtEmp.setText("03");

		setHandlers();

	}


	//region Events

	public void askRec(View view) {

		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

		//CKFK 20190222 Se agregó esta validación para no sobreescribir los datos si ya se importaron
		if (!gl.modoadmin) {

			if (gl.banderafindia) {

				int fechaUltimoCierre;

				fechaUltimoCierre = claseFindia.ultimoCierreFecha();

				if ((du.getActDate() == fechaUltimoCierre) && ExistenDatos()) {
					//claseFindia.
					claseFindia.eliminarTablasD();
				}

			}

			if (ExistenDatos()) {
				BorraDatosAnteriores("¿Tiene facturas, pedidos, cobros, devoluciones o inventario, está seguro de borrar los datos?");
			} else {
				msgAskConfirmaRecibido();
			}
		} else {
			msgAskConfirmaRecibido();
		}

	}

	public void askSend(View view) {

		try {
			if (isbusy == 1) {
				toastcent("Por favor, espere que se termine la tarea actual.");
				return;
			}

			if (gl.contlic) {
				if (!validaLicencia()) {
					mu.msgbox("Licencia inválida!");
					return;
				}
			}

			if (gl.banderafindia) {
				if (!puedeComunicar()) {
					mu.msgbox("No ha hecho fin de dia, no puede comunicar datos");
					return;
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Enviar datos?");

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runSend();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private boolean puedeComunicar() {

		boolean vPuedeCom = false;

		try {

			//#CKFK 20190304 Agregué validación para verificar si ya se realizó la comunicación de los datos.
			if (gl.banderafindia) {

				return ((claseFindia.getImprimioCierreZ() == 7));

			} else {
				return true;
			}

		} catch (Exception ex) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ex.getMessage(), "");
		}

		return vPuedeCom;
	}

	public void askExist(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Existencias bodega");
			dialog.setMessage("¿Actualizar existencias?");

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runExist();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

	}

	public void askPrecios(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Precios");
			dialog.setMessage("¿Actualizar precios?");

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runPrecios();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

	}

	public void askRecarga(View view) {

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Recarga de inventario");
			dialog.setMessage("¿Recargar inventario?");

			dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runRecarga();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}
		if (isbusy == 1) {
			toastcent("Por favor, espere hasta que se termine la tarea actual.");
			return;
		}

	}

	private void setHandlers() {
		ralBack.setOnTouchListener(new SwipeListener(this) {
			public void onSwipeRight() {
				onBackPressed();
			}

			public void onSwipeLeft() {
			}
		});
	}

	//endregion

	//region Main

	private void runRecep() {

		try {
			if (isbusy == 1) return;

			if (!setComParams()) return;

			//#CKFK 20190313 Agregué esto para ocultar el teclado durante la carga de los datos
			View view = this.getCurrentFocus();
			view.clearFocus();
			if (view != null) {
				keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			isbusy = 1;

			if (!esvacio) {
				ultcor_ant = ultCorel();
				ultSerie_ant = ultSerie();
			}

			barInfo.setVisibility(View.VISIBLE);
			barInfo.invalidate();
			lblInfo.setText("Iniciando proceso de carga..");

			lblInfo.setText("Conectando ...");

			wsRtask = new AsyncCallRec();
			wsRtask.execute();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void runSend() {

		try {
			if (isbusy == 1) return;

			if (!setComParams()) return;

			isbusy = 1;

			barInfo.setVisibility(View.VISIBLE);
			barInfo.invalidate();
			lblInfo.setText("Conectando ...");

			showprogress = true;
			wsStask = new AsyncCallSend();
			wsStask.execute();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void runExist() {
		try {
			super.finish();
			startActivity(new Intent(this, ComWSExist.class));
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void runPrecios() {
		try {
			super.finish();
			startActivity(new Intent(this, ComWSPrec.class));
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void runRecarga() {
		try {
			super.finish();
			startActivity(new Intent(this, ComWSRec.class));
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void writeData(View view) {

		try {
			dbld.clear();
			dbld.insert("D_PEDIDO", "WHERE 1=1");
			dbld.insert("D_PEDIDOD", "WHERE 1=1");
			dbld.save();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}


	}

	private boolean validaPendientes() {
		int pend = 0;

		sp = "";

		try {

			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_FACTURA WHERE STATCOM<>'S'", "Fact: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_PEDIDO WHERE STATCOM<>'S'", "Ped: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_COBRO WHERE STATCOM<>'S'", "Rec: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_DEPOS WHERE STATCOM<>'S'", "Dep: ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_MOV WHERE STATCOM<>'S'", "Inv : ");

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return pend > 0;
	}

	//endregion

	//region Web Service Methods

	public int fillTable(String value, String delcmd) {

		int rc;
		String s, ss;

		METHOD_NAME = "getIns";

		sstr = "OK";

		try {

			idbg = idbg + " filltable ";

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(value);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;
			idbg = idbg + " rec " + rc + "  ";

			s = "";
			if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
				if (rc == 1) {
					stockflag = 0;//return 1;
				} else {
					stockflag = 1;
				}
			}

			// if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
			// 	idbg=idbg+" RC ="+rc+"---";
			//}


			for (int i = 0; i < rc; i++) {
				String str = "";
				try {
					str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);
					//s=s+str+"\n";
				} catch (Exception e) {
					mu.msgbox("error: " + e.getMessage());
				}

				if (i == 0) {

					idbg = idbg + " ret " + str + "  ";

					if (str.equalsIgnoreCase("#")) {
						listItems.add(delcmd);
					} else {
						idbg = idbg + str;
						ftmsg = ftmsg + "\n" + str;
						ftflag = true;
						sstr = str;
						return 0;
					}
				} else {
					try {
						sql = str;
						listItems.add(sql);
						sstr = str;
					} catch (Exception e) {
						addlog(new Object() {
						}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
						sstr = e.getMessage();
					}
				}
			}

			return 1;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			//#EJC20190226: Evitar que se muestre OK después del nombre de la tabla cuando da error de timeOut.
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			return 0;
		}
	}

	public int commitSQL() {
		int rc;
		String s, ss;

		METHOD_NAME = "Commit";
		sstr = "OK";

		if (dbld.size() == 0) return 1;

		s = "";
		for (int i = 0; i < dbld.size(); i++) {
			ss = dbld.items.get(i);
			s = s + ss + "\n";
		}
		if (showprogress) {
			fprog = "Enviando ...";
			wsStask.onProgressUpdate();
		}

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(s);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			sstr = e.getMessage();
		}

		return 0;
	}

	public int OpenDTt(String sql) {
		int rc;

		METHOD_NAME = "OpenDT";

		results.clear();

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(sql);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;

			for (int i = 0; i < rc + 1; i++) {
				String str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

				if (i == 0) {
					sstr = str;
					if (!str.equalsIgnoreCase("#")) {
						sstr = str;
						return 0;
					}
				} else {
					results.add(str);
				}
			}

			return 1;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			sstr = e.getMessage();
		}

		return 0;

	}

	public int getTest() {

		METHOD_NAME = "TestWS";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Value");
			param.setValue("OK");

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);

			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			sstr = response.toString() + "..";

			return 1;
		} catch (Exception e) {
			sstr = e.getMessage();
		}

		return 0;
	}

	//#HS_20181219 Funcion para enviar JSON al Web Service.
	public int envioFachada() {
		String METHOD_NAME = "GuardaFachada";
		s = "";

		fprog = "Enviando ...";
		wsStask.onProgressUpdate();

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("JSONFachadas");
			param.setValue(jsonWS);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE + METHOD_NAME, envelope);
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
			sstr = e.getMessage();
		}

		return 0;

	}

	//endregion

	//region WS Recepcion Methods

	private boolean getData() {
		Cursor DT;
		BufferedWriter writer = null;
		FileWriter wfile;
		int rc, scomp, prn, jj;
		String s, val = "";


		try {

			String fname = Environment.getExternalStorageDirectory() + "/roadcarga.txt";
			wfile = new FileWriter(fname, false);
			writer = new BufferedWriter(wfile);

			db.execSQL("DELETE FROM P_LIQUIDACION");

			sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				val = DT.getString(0);

			} else {
				val = "N";
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			val = "N";
		}

		if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
		else gl.peStockItf = false;


		listItems.clear();
		scomp = 0;
		idbg = "";
		stockflag = 0;

		ftmsg = "";
		ftflag = false;

		try {

			if (!AddTable("P_PARAMEXT")) return false;
			procesaParamsExt();

			if (!AddTable("P_NIVELPRECIO")) return false;

			if (!AddTable("P_RUTA")) return false;
			if (!AddTable("P_CLIENTE")) return false;
			if (!AddTable("P_CLIENTE_FACHADA")) return false;
			if (!AddTable("P_CLIRUTA")) return false;
			if (!AddTable("P_CLIDIR")) return false;
			if (!AddTable("P_PRODUCTO")) return false;
			if (!AddTable("P_FACTORCONV")) return false;
			if (!AddTable("P_LINEA")) return false;
			if (!AddTable("P_PRODPRECIO")) return false;
			if (!AddTable("TMP_PRECESPEC")) return false;
			if (!AddTable("P_DESCUENTO")) return false;
			if (!AddTable("P_EMPRESA")) return false;
			if (!AddTable("P_SUCURSAL")) return false;
			if (!AddTable("P_BANCO")) return false;
			if (!AddTable("P_STOCKINV")) return false;

			if (!AddTable("P_CODATEN")) return false;
			if (!AddTable("P_CODDEV")) return false;
			if (!AddTable("P_CODNOLEC")) return false;
			if (!AddTable("P_NIVELPRECIO")) return false;
			if (!AddTable("P_COREL")) return false;
			if (!AddTable("P_CORELNC")) return false;
			if (!AddTable("P_CORRELREC")) return false;
			if (!AddTable("P_CORREL_OTROS")) return false;
			if (!AddTable("P_STOCK_APR")) return false;
			if (!AddTable("P_STOCK")) return false;
			if (!AddTable("P_STOCKB")) return false;
			if (!AddTable("P_STOCK_PALLET"))
				return false;//#CKFK 20190304 10:48 Se agregó esta tabla para poder importar los pallets
			if (!AddTable("P_COBRO")) return false;
			if (!AddTable("P_CLIGRUPO")) return false;
			if (!AddTable("P_MEDIAPAGO")) return false;
			if (!AddTable("P_BONIF")) return false;
			if (!AddTable("P_BONLIST")) return false;
			if (!AddTable("P_PRODGRUP")) return false;
			if (!AddTable("P_IMPUESTO")) return false;
			if (!AddTable("P_VENDEDOR")) return false;
			if (!AddTable("P_MUNI")) return false;
			if (!AddTable("P_VEHICULO")) return false;
			if (!AddTable("P_HANDHELD")) return false;
			if (!AddTable("P_IMPRESORA")) return false;

			if (!AddTable("P_REF1")) return false;
			if (!AddTable("P_REF2")) return false;
			if (!AddTable("P_REF3")) return false;

			if (!AddTable("P_ARCHIVOCONF")) return false;
			if (!AddTable("P_ENCABEZADO_REPORTESHH")) return false;
			if (!AddTable("P_PORCMERMA")) return false;


			// Objetivos

			if (!AddTable("O_RUTA")) return false;
			if (!AddTable("O_COBRO")) return false;
			if (!AddTable("O_PROD")) return false;
			if (!AddTable("O_LINEA")) return false;


			// Mercadeo

			if (!AddTable("P_MEREQTIPO")) return false;
			if (!AddTable("P_MEREQUIPO")) return false;
			if (!AddTable("P_MERESTADO")) return false;
			if (!AddTable("P_MERPREGUNTA")) return false;
			if (!AddTable("P_MERRESP")) return false;
			if (!AddTable("P_MERMARCACOMP")) return false;
			if (!AddTable("P_MERPRODCOMP")) return false;

			//if (gl.contlic) {
			//	if (!AddTable("LIC_CLIENTE")) return false;
			//}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			return false;
		}

		ferr = "";

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return true;

			fprog = "Procesando ...";
			wsRtask.onProgressUpdate();

			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			prn = 0;
			jj = 0;

			Log.d("M", "So far we are good");

			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);
				esql = sql;
				sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
				sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");

				try {
					writer.write(sql);
					writer.write("\r\n");
				} catch (Exception e) {
					Log.d("M", "Something happend here " + e.getMessage());
				}

				try {
					dbT = ConT.getWritableDatabase();
					dbT.execSQL(sql);
				} catch (Exception e) {
					Log.d("M", "Something happend there " + e.getMessage());
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage() + "EJC", "Yo fui " + sql);
					Log.e("z", e.getMessage());
				}

				try {
					if (i % 10 == 0) {
						fprog = "Procesando: " + i + " de: " + (rc - 1);
						wsRtask.onProgressUpdate();
						SystemClock.sleep(20);
					}
				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					Log.e("z", e.getMessage());
				}
			}

			fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
			wsRtask.onProgressUpdate();

			Actualiza_FinDia();

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			Log.d("M", "We are ok");

			fprog = "Documento de inventario recibido en BOF...";
			wsRtask.onProgressUpdate();

			Actualiza_Documentos();

			fprog = "Fin de la actualización";
			wsRtask.onProgressUpdate();

			scomp = 1;

			try {
				ConT.close();
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			}

			try {
				writer.close();
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
				msgbox(new Object() {
				}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			}

			return true;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fprog = "Actualización incompleta";
			wsRtask.onProgressUpdate();

			Log.e("Error", e.getMessage());
			try {
				ConT.close();
			} catch (Exception ee) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			}

			sstr = e.getMessage();
			ferr = sstr + "\n" + sql;
			esql = sql;
			return false;
		}

	}

	private void procesaParamsExt() {
		Cursor dt;
		String sql, val = "";
		int ival, rc;

		try {

			rc = listItems.size();
			reccnt = rc;
			if (rc == 0) return;

			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
			ConT.vDatabase = dbT;
			insT = ConT.Ins;

			dbT.beginTransaction();

			for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);
				esql = sql;
				dbT.execSQL(sql);

				try {
					if (i % 10 == 0) {

						SystemClock.sleep(20);
					}
				} catch (Exception e) {
					Log.e("z", e.getMessage());
				}
			}

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			try {
				sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=2";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				val = dt.getString(0);
			} catch (Exception e) {
				val = "N";
			}
			if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
			else gl.peStockItf = false;

			try {
				sql = "SELECT VALOR FROM P_PARAMEXT WHERE ID=3";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				gl.peModal = dt.getString(0).toUpperCase();
			} catch (Exception e) {
				gl.peModal = "-";
			}

			try {
				ConT.close();
			} catch (Exception e) {
			}

		} catch (Exception e) {
			try {
				ConT.close();
			} catch (Exception ee) {
			}
		}

	}

	//#EJC20181120: Inserta los documentos que bajaron a la HH
	private boolean Actualiza_Documentos() {
		DateUtils DU = new DateUtils();
		long Now = du.getFechaActual();

		String ruta = txtRuta.getText().toString().trim();

		try {

			String SQL = " INSERT INTO P_DOC_ENVIADOS_HH " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCKB WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK_PALLET WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)";

			dbld.clear();
			dbld.add(SQL);

			if (commitSQL() == 1) {
				return true;
			} else {
				fterr += "\n" + sstr;
				dbg = sstr;
				return false;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			Log.e("Error", e.getMessage());
			return false;
		}

	}

	private boolean Actualiza_FinDia() {

		Cursor DT1;
		int vCorelZ = 0;
		float vGrandTotal = 0;

		boolean vActualizaFD = true;

		try {

			sql = "SELECT CORELZ, GRANDTOTAL FROM P_HANDHELD";
			DT1 = ConT.OpenDT(sql);

			if (DT1.getCount() > 0) {

				DT1.moveToFirst();

				vCorelZ = DT1.getInt(0);
				vGrandTotal = DT1.getFloat(1);

				sql = "UPDATE FINDIA SET COREL = " + vCorelZ + ", VAL1=0, VAL2=0, VAL3=0, VAL4=0,VAL5=0, VAL6=0, VAL7=0";
				dbT.execSQL(sql);
			}

			DT1.close();

		} catch (Exception ex) {
			vActualizaFD = false;
		}

		return vActualizaFD;

	}

	private boolean AddTable(String TN) {
		String SQL;

		try {

			fprog = TN;
			idbg = TN;
			wsRtask.onProgressUpdate();
			SQL = getTableSQL(TN);

			if (fillTable(SQL, "DELETE FROM " + TN) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tab:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = "Tab:" + TN + ", " + e.getMessage();
			idbg = idbg + e.getMessage();
			return false;
		}
	}

	private boolean AddTableVL(String TN) {
		String SQL;

		try {

			fprog = TN;
			idbg = TN;
			//wsRtask.onProgressUpdate();
			SQL = getTableSQL(TN);

			if (fillTable(SQL, "DELETE FROM " + TN) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tab:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tab:" + TN + ", " + e.getMessage();
			idbg = idbg + e.getMessage();
			return false;
		}
	}

	private String getTableSQL(String TN) {
		String SQL = "";
		long fi, ff;

		fi = du.ffecha00(du.getActDate());
		ff = du.ffecha24(du.getActDate());
		long ObjAno = du.getyear(du.getActDate());
		long ObjMes = du.getmonth(du.getActDate());

		if (TN.equalsIgnoreCase("P_STOCK")) {

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						"FROM P_STOCK WHERE RUTA='" + ActRuta + "'  AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
						"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)";
			} else if (gl.peModal.equalsIgnoreCase("APR")) {
				SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						"FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsql + "') ";
			} else {
				SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						"FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsql + "') ";
			}

			esql = SQL;
			return SQL;
		}

		//CKFK 20190222 Agregué a la consulta el AND (ENVIADO = 0)
		if (TN.equalsIgnoreCase("P_STOCKB")) {
			SQL = "SELECT RUTA, BARRA, CODIGO, CANT, COREL, PRECIO, PESO, DOCUMENTO,dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
					"STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, DOC_ENTREGA " +
					"FROM P_STOCKB WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
					"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			return SQL;
		}

		//CKFK 20190304 Agregué la consulta para obtener los datos de P_STOCK_PALLET
		if (TN.equalsIgnoreCase("P_STOCK_PALLET")) {
			SQL = "SELECT DOCUMENTO, RUTA, BARRAPALLET, CODIGO, BARRAPRODUCTO, LOTEPRODUCTO, CANT, COREL, PRECIO, PESO, " +
					"UNIDADMEDIDA,dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, DOC_ENTREGA  " +
					"FROM P_STOCK_PALLET WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
					"AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_BONIF")) {
			//SQL = "SELECT  CLIENTE, CTIPO, PRODUCTO, PTIPO, TIPORUTA, TIPOBON, RANGOINI, RANGOFIN, TIPOLISTA, TIPOCANT, VALOR," +
			//"LISTA, CANTEXACT, GLOBBON, PORCANT, dbo.AndrDate(FECHAINI), dbo.AndrDate(FECHAFIN), CODDESC, NOMBRE, EMP, UMPRODUCTO , UMBONIFICACION " +
			//"FROM P_BONIF WHERE ((dbo.AndrDate(FECHAINI)<=" + ff + ") AND (dbo.AndrDate(FECHAFIN)>=" + fi + "))";

			SQL = "SELECT  CLIENTE, CTIPO, PRODUCTO, PTIPO, TIPORUTA, TIPOBON, RANGOINI, RANGOFIN, TIPOLISTA, TIPOCANT, VALOR," +
					"LISTA, CANTEXACT, GLOBBON, PORCANT, dbo.AndrDate(FECHAINI), dbo.AndrDate(FECHAFIN), CODDESC, NOMBRE, EMP, UMPRODUCTO , UMBONIFICACION " +
					"FROM P_BONIF WHERE ((FECHAINI<='" + fsqlf + "') AND (FECHAFIN>='" + fsqli + "'))";
			esql = SQL;
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIRUTA")) {
			SQL = "SELECT RUTA,CLIENTE,SEMANA,DIA,SECUENCIA,-1 AS BANDERA FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIENTE")) {
			SQL = " SELECT CODIGO,NOMBRE,BLOQUEADO,TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL, ";
			SQL += "NIVELPRECIO,MEDIAPAGO,LIMITECREDITO,DIACREDITO,DESCUENTO,BONIFICACION, ";
			SQL += "dbo.AndrDate(ULTVISITA),IMPSPEC,INVTIPO,INVEQUIPO,INV1,INV2,INV3, NIT, MENSAJE, ";
			SQL += "TELEFONO,DIRTIPO, DIRECCION,SUCURSAL,COORX, COORY, FIRMADIG, CODBARRA, VALIDACREDITO, ";
			SQL += "PRECIO_ESTRATEGICO, NOMBRE_PROPIETARIO, NOMBRE_REPRESENTANTE, ";
			SQL += "BODEGA, COD_PAIS, FACT_VS_FACT, CHEQUEPOST, PERCEPCION, TIPO_CONTRIBUYENTE, ID_DESPACHO, ID_FACTURACION,MODIF_PRECIO ";
			SQL += "FROM P_CLIENTE ";
			SQL += "WHERE (CODIGO IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') )) ";
			return SQL;
		}

		//#HS_20181220 Tabla de Fachadas cliente.
		if (TN.equalsIgnoreCase("P_CLIENTE_FACHADA")) {
			SQL = " SELECT * FROM P_CLIENTE_FACHADA ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIDIR")) {
			SQL = " SELECT * FROM P_CLIDIR ";
			SQL += " WHERE (P_CLIDIR.CODIGO_CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') ))";
			return SQL;
		}
       /*       if (TN.equalsIgnoreCase("P_PRODUCTO")) {
           SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
           SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, DESCUENTO,BONIFICACION, ";
           SQL += "IMP1, IMP2, IMP3, VENCOMP, DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN ";
           SQL += "FROM P_PRODUCTO ";
           return SQL;
       }
	   */

		if (TN.equalsIgnoreCase("P_PRODUCTO")) {
			SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
			SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, ISNULL(DESCUENTO,'N') AS DESCUENTO, ISNULL(BONIFICACION,'N') AS BONIFICACION, ";
			SQL += "IMP1, IMP2, IMP3, VENCOMP, ISNULL(DEVOL,'S') AS DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN, ";
			SQL += "VIDEO,VENTA_POR_PESO,ES_PROD_BARRA,UNID_INV,VENTA_POR_PAQUETE,VENTA_POR_FACTOR_CONV,ES_SERIALIZADO,PARAM_CADUCIDAD, ";
			SQL += "PRODUCTO_PADRE,FACTOR_PADRE,TIENE_INV,TIENE_VINETA_O_TUBO,PRECIO_VINETA_O_TUBO,ES_VENDIBLE,UNIGRASAP,UM_SALIDA ";
			SQL += "FROM P_PRODUCTO WHERE (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "')) ";
			SQL += "OR LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')) ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_FACTORCONV")) {
			//#EJC20181112
			//SQL = "SELECT PRODUCTO,UNIDADSUPERIOR,FACTORCONVERSION,UNIDADMINIMA FROM P_FACTORCONV ";
			SQL = " SELECT * FROM P_FACTORCONV WHERE PRODUCTO IN (SELECT CODIGO " +
					" FROM P_PRODUCTO WHERE LINEA IN (SELECT DISTINCT LINEA FROM P_LINEARUTA " +
					" WHERE RUTA = '" + ActRuta + "')) " +
					" OR ((PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
					" OR PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "')))";

			return SQL;
		}

		if (TN.equalsIgnoreCase("P_LINEA")) {
			SQL = "SELECT CODIGO,MARCA,NOMBRE FROM P_LINEA ";
			SQL += "WHERE (CODIGO IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PRODPRECIO")) {

			SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO ";
			SQL += " WHERE ( (CODIGO IN ( SELECT CODIGO FROM P_PRODUCTO WHERE (LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE RUTA='" + ActRuta + "')) ) ) ";
			SQL += " OR  (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "')) ) ";
			SQL += " AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'))) ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("TMP_PRECESPEC")) {
			SQL = "SELECT CODIGO,VALOR,PRODUCTO,PRECIO,UNIDADMEDIDA FROM TMP_PRECESPEC ";
			SQL += " WHERE RUTA='" + ActRuta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
			return SQL;
		}


		if (TN.equalsIgnoreCase("P_DESCUENTO")) {
			SQL = "SELECT  CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,DESCTIPO,VALOR,GLOBDESC,PORCANT,dbo.AndrDateIni(FECHAINI),dbo.AndrDateFin(FECHAFIN),CODDESC,NOMBRE ";
			SQL += "FROM P_DESCUENTO WHERE DATEDIFF(D, FECHAINI,GETDATE()) >=0 AND DATEDIFF(D,GETDATE(), FECHAFIN) >=0";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_EMPRESA")) {
			SQL = "SELECT * FROM P_EMPRESA WHERE EMPRESA = '" + gEmpresa + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_RUTA")) {
			SQL = "SELECT * FROM P_RUTA WHERE CODIGO = '" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_BANCO")) {
			SQL = "SELECT * FROM P_BANCO WHERE EMPRESA = '" + gEmpresa + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_STOCKINV")) {
			SQL = "SELECT * FROM P_STOCKINV";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODATEN")) {
			SQL = "SELECT * FROM P_CODATEN";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODNOLEC")) {
			SQL = "SELECT CODIGO, DESCRIPCION AS NOMBRE  FROM P_RAZONNOSCAN";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CODDEV")) {
			SQL = "SELECT * FROM P_CODDEV";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_NIVELPRECIO")) {
			SQL = "SELECT CODIGO, NOMBRE, ISNULL(DECIMALES,0) AS DECIMALES FROM P_NIVELPRECIO ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CLIGRUPO")) {
			SQL = "SELECT CODIGO,CLIENTE FROM P_CLIGRUPO WHERE (CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_STOCK_APR")) {
			SQL = "SELECT CODIGO, CANT, PESO " +
					"FROM P_STOCK_APR WHERE RUTA='" + ActRuta + "' ";
			//SQL = "SELECT CODIGO,CANT,0 AS CANTM,PESO FROM P_STOCK WHERE RUTA='" + ActRuta + "'";
			//idbg=SQL;
			return SQL;
		}

		//#HS_20181212 Agregue campos ID_TRANSACCION, REFERENCIA, ASIGNACION.
		if (TN.equalsIgnoreCase("P_COBRO")) {
			SQL = "SELECT  DOCUMENTO, EMPRESA, RUTA, CLIENTE, TIPODOC, VALORORIG, SALDO, CANCELADO, dbo.AndrDate(FECHAEMIT),dbo.AndrDate(FECHAV),'' AS CONTRASENA, ID_TRANSACCION, REFERENCIA, ASIGNACION ";
			SQL += "FROM P_COBRO WHERE (RUTA='" + ActRuta + "') AND CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "')) ";
			//idbg=SQL;
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_COREL")) {
			SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_COREL WHERE RUTA='" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORELNC")) {
			SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_CORELNC WHERE RUTA='" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORRELREC")) {
			SQL = "SELECT RUTA,SERIE,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORRELREC WHERE RUTA='" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CORREL_OTROS")) {
			SQL = "SELECT RUTA,SERIE,TIPO,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORREL_OTROS WHERE RUTA='" + ActRuta + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MEDIAPAGO")) {
			SQL = "SELECT CODIGO,NOMBRE,ACTIVO,NIVEL,PORCOBRO FROM P_MEDIAPAGO WHERE ACTIVO='S'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_ARCHIVOCONF")) {
			SQL = "SELECT RUTA,TIPO_HH,IDIOMA,TIPO_IMPRESORA,SERIAL_HH,MODIF_PESO,PUERTO_IMPRESION,LBS_O_KGS,NOTA_CREDITO FROM P_ARCHIVOCONF WHERE (RUTA='" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_ENCABEZADO_REPORTESHH")) {
			SQL = "SELECT CODIGO,TEXTO,SUCURSAL FROM P_ENCABEZADO_REPORTESHH";
			return SQL;
		}


		if (TN.equalsIgnoreCase("P_BONLIST")) {
			SQL = "SELECT CODIGO,PRODUCTO,CANT,CANTMIN,NOMBRE FROM P_BONLIST";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PRODGRUP")) {
			SQL = "SELECT CODIGO,PRODUCTO,NOMBRE FROM P_PRODGRUP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_IMPUESTO")) {
			SQL = "SELECT CODIGO,VALOR FROM P_IMPUESTO";
			return SQL;
		}

		//#HS_20181206 Agregue Ruta.
		if (TN.equalsIgnoreCase("P_VENDEDOR")) {

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				SQL = "SELECT CODIGO,NOMBRE,CLAVE,RUTA,NIVEL,NIVELPRECIO,ISNULL(BODEGA,0) AS BODEGA,ISNULL(SUBBODEGA,0) AS SUBBODEGA,'' AS COD_VEHICULO,0 AS LIQUIDANDO,0 AS BLOQUEADO,0 AS DEVOLUCION_SAP  " +
						"FROM VENDEDORES  WHERE (RUTA='" + ActRuta + "') ";
			} else {
				SQL = "SELECT CODIGO,NOMBRE,CLAVE,RUTA,NIVEL,NIVELPRECIO,ISNULL(BODEGA,0) AS BODEGA,ISNULL(SUBBODEGA,0) AS SUBBODEGA,COD_VEHICULO,LIQUIDANDO,BLOQUEADO,DEVOLUCION_SAP  " +
						"FROM P_VENDEDOR  WHERE (RUTA='" + ActRuta + "') OR (NIVEL=1) ";
			}

			return SQL;
		}

		//#HS_20181207 Agregue campos de P_VEHICULO.
		if (TN.equalsIgnoreCase("P_VEHICULO")) {
			SQL = "SELECT CODIGO,MARCA,PLACA,PESO,KM_MILLAS,TIPO FROM P_VEHICULO";
			return SQL;
		}

		//#CKFK_20190319 Agregué tabla P_HANDHELD
		if (TN.equalsIgnoreCase("P_HANDHELD")) {
			SQL = " SELECT NUMPLACA, NUMSERIE, TIPO, ISNULL(CREADA,'') AS CREADA, " +
					" ISNULL(MODIFICADA,'') AS MODIFICADA, ISNULL(FECHA_CREADA, GETDATE()) AS FECHA_CREADA," +
					"	ISNULL(FECHA_MODIFICADA, GETDATE()) AS FECHA_MODIFICADA, CORELZ, GRANDTOTAL FROM P_HANDHELD" +
					" WHERE NUMPLACA IN (SELECT HANDHELD FROM P_COREL WHERE RUTA = '" + ActRuta + "' AND ACTIVA = 'S')";
			return SQL;
		}

		//#CKFK_20190319 Agregué tabla P_IMPRESORA
		if (TN.equalsIgnoreCase("P_IMPRESORA")) {
			SQL = " SELECT IDIMPRESORA, NUMSERIE, MARCA, ISNULL(CREADA,'') AS CREADA, " +
					" ISNULL(MODIFICADA,'') AS MODIFICADA, ISNULL(FECHA_CREADA, GETDATE()) AS FECHA_CREADA," +
					" ISNULL(FECHA_MODIFICADA, GETDATE()) AS FECHA_MODIFICADA FROM P_IMPRESORA";
			return SQL;
		}
		if (TN.equalsIgnoreCase("P_MUNI")) {
			SQL = "SELECT * FROM P_MUNI";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF1")) {
			SQL = "SELECT * FROM P_REF1";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF2")) {
			SQL = "SELECT * FROM P_REF2";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_REF3")) {
			SQL = "SELECT * FROM P_REF3";
			return SQL;
		}


		// Objetivos

		if (TN.equalsIgnoreCase("O_PROD")) {
			SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_PROD WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_LINEA")) {
			SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_LINEA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_RUTA")) {
			SQL = "SELECT * FROM O_RUTA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}

		if (TN.equalsIgnoreCase("O_COBRO")) {
			SQL = "SELECT * FROM O_COBRO WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
			return SQL;
		}


		// Mercadeo

		if (TN.equalsIgnoreCase("P_MEREQTIPO")) {
			SQL = "SELECT * FROM P_MEREQTIPO";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MEREQUIPO")) {
			SQL = "SELECT * FROM P_MEREQUIPO ";
			SQL = SQL + "WHERE (CLIENTE IN  (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "' ) )";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERESTADO")) {
			SQL = "SELECT * FROM P_MERESTADO";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERPREGUNTA")) {
			SQL = "SELECT * FROM P_MERPREGUNTA";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERRESP")) {
			SQL = "SELECT * FROM P_MERRESP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERMARCACOMP")) {
			SQL = "SELECT * FROM P_MERMARCACOMP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_MERPRODCOMP")) {
			SQL = "SELECT * FROM P_MERPRODCOMP";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PORCMERMA")) {
			SQL = "SELECT EMPRESA,SUCURSAL,RUTA,PRODUCTO,PORCENTAJEMERMA,PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE (RUTA='" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("LIC_CLIENTE")) {
			SQL = "SELECT * FROM LIC_CLIENTE WHERE ID='" + mac + "'";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PARAMEXT")) {
			SQL = "SELECT ID,Nombre,Valor FROM P_PARAMEXT WHERE ((idRuta='" + ActRuta + "') OR (ISNULL(idRuta,'')=''))";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_LIQUIDACION")) {
			SQL = "SELECT RUTA,ESTADO FROM P_LIQUIDACION WHERE (RUTA='" + ActRuta + "') AND (FECHA>='" + fsql + "') ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_SUCURSAL")) {
			SQL = " SELECT CODIGO, EMPRESA, DESCRIPCION, NOMBRE, DIRECCION, TELEFONO, NIT, TEXTO " +
					" FROM P_SUCURSAL WHERE CODIGO IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '" + ActRuta + "')";
			return SQL;
		}

		return SQL;
	}

	private void comparaCorrel() {
		Cursor DT;
		String ss;
		try {
			try {
				sql = "SELECT VENTA FROM P_RUTA";
				DT = Con.OpenDT(sql);

				if (DT.getCount() == 0) {
					msgbox("La ruta no existe. Por favor informe su supervisor !");
				}

				DT.moveToFirst();
				ss = DT.getString(0);
				if (ss.equalsIgnoreCase("T")) ss = "V";
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				ss = "X";
			}

			if (!ss.equalsIgnoreCase("V")) return;

			ultcor = ultCorel();
			if (ultcor == 0) {
				//msgbox("No está definido correlativo de las facturas!\n Por favor informe a su supervisor.");
			}

			ultSerie = ultSerie(); //#HS_20181129_1005 Agregue ultSerie.
			if (ultcor_ant != ultcor) {
				//#HS_20181129_1005 Agregue comparacion para las series.
				if (ultSerie_ant != ultSerie) {
					msgbox("Nueva serie de facturación");
				} else if (ultcor_ant > 0) {
					msgbox("El último correlativo actualizado ( " + ultcor + " ) no coincide con último emitido ( " + ultcor_ant + " )!\n Por favor infore a su supervisor.");
					return;
				}
			}
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

	}

	private boolean validaDatos(boolean completo) {

		Cursor dt;

		try {

			if (!rutatipo.equalsIgnoreCase("P")) {
				sql = "SELECT RESOL FROM P_COREL";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
					msgbox("No está definido correlativo de facturas");
					return false;
				}
			}

			sql = "SELECT Codigo FROM P_CLIENTE";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
				msgbox("Lista de clientes está vacia");
				return false;
			}

			sql = "SELECT Ruta FROM P_CLIRUTA";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
				msgbox("Lista de clientes por ruta está vacia");
				return false;
			}

			sql = "SELECT Codigo FROM P_PRODUCTO";
			dt = Con.OpenDT(sql);
			if (dt.getCount() == 0) {
				msgbox("Lista de productos está vacia");
				return false;
			}

			if (completo) {

				sql = "SELECT Nivel FROM P_PRODPRECIO ";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
					msgbox("Lista de precios está vacia");
					return false;
				}

				sql = "SELECT Producto FROM P_FACTORCONV ";
				dt = Con.OpenDT(sql);
				if (dt.getCount() == 0) {
					msgbox("Lista de conversiones está vacia");
					return false;
				}

				if (gl.peStockItf) {
					sql = "SELECT Codigo FROM P_STOCK ";
					dt = Con.OpenDT(sql);
					if (dt.getCount() == 0) {
						msgbox("La carga de productos está vacia");
						return false;
					}
				}

			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			Log.d("ValidaDatos", e.getMessage());
		}

		return true;
	}

	//#HS_20181123_1623 Agregue funcion FinDia para el commit y update de tablas.
	private boolean FinDia() {

		try {

			if (commitSQL() == 1) {

				db.beginTransaction();
				db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
				db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
				db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
				db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
				db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
				db.execSQL("UPDATE D_MOV SET STATCOM='S'");
				db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
				db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
				db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
				db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
				db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
				db.execSQL("UPDATE FINDIA SET VAL5=0, VAL4=0,VAL3=0, VAL2=0");
				db.setTransactionSuccessful();
				db.endTransaction();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox("FinDia(): " + e.getMessage());
			return false;
		}
		return true;
	}


	//#CKFK_20190325 Agregué funcion ActualizaStatcom que es una copia de FinDia pero sin el CommitSQL
	private boolean ActualizaStatcom() {

		try {

			db.beginTransaction();
			db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
			db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
			db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
			db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
			db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
			db.execSQL("UPDATE D_MOV SET STATCOM='S'");
			db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
			db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
			db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
			db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
			db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
			db.execSQL("UPDATE P_RUTA SET PARAM2 = ''");
			db.setTransactionSuccessful();
			db.endTransaction();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox("ActualizaStatcom(): " + e.getMessage());
			return false;
		}
		return true;
	}

	//endregion

	//region WS Recepcion Handling Methods

	public void wsExecute() {

		running = 1;
		fstr = "No connect";
		scon = 0;

		try {

			if (getTest() == 1) scon = 1;

			idbg = idbg + sstr;

			if (scon == 1) {
				fstr = "Sync OK";
				if (!getData()) fstr = "Recepcion incompleta : " + fstr;
			} else {
				fstr = "No se puede conectar al web service : " + sstr;
			}

		} catch (Exception e) {
			scon = 0;
			fstr = "No se puede conectar al web service. " + e.getMessage();
			Log.d("E", fstr + sstr);
		}

	}

	public void wsFinished() {

		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);
		running = 0;
		try {
			if (fstr.equalsIgnoreCase("Sync OK")) {

				lblInfo.setText(" ");
				s = "Recepción completa.";

				if (stockflag == 1) {
					s = s + "\nSe actualizó inventario.";
				}

				clsAppM.estandartInventario();
				validaDatos(true);
				if (stockflag == 1) sendConfirm();

				SetStatusRecTo("1");

				msgAskExit(s);

			} else {
				lblInfo.setText(fstr);
				mu.msgbox("Ocurrió error : \n" + fstr + " (" + reccnt + ") ");
				mu.msgbox("::" + esql);
				isbusy = 0;
				barInfo.setVisibility(View.INVISIBLE);
				addlog("Recepcion", fstr, esql);
				return;
			}

			pendientes = validaPendientes();
			visibilidadBotones();

			isbusy = 0;
			comparaCorrel();

			paramsExtra();
			//mu.msgbox("::"+esql);

			if (ftflag) msgbox(ftmsg);
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}


	}

	private class AsyncCallRec extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsExecute();
			} catch (Exception e) {
				if (scon == 0) {
					fstr = "No se puede conectar al web service : " + sstr;
					//lblInfo.setText(fstr);
				}
				//msgbox(fstr);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				wsFinished();
			} catch (Exception e) {
				Log.d("onPostExecute", e.getMessage());
			}

		}

		@Override
		protected void onPreExecute() {
			try {
				SetStatusRecTo("");
			} catch (Exception e) {
				Log.d("onPreExecute", e.getMessage());
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
				synchronized (this) {
					if (!lblInfo.getText().toString().matches("")) lblInfo.setText(fprog);
				}
			} catch (Exception e) {
				Log.d("onProgressUpdate", e.getMessage());
			}
		}

	}

	//endregion

	//region WS Envio Methods

	private boolean sendData() {

		errflag = false;

		senv = "Envío terminado \n \n";

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			if (!validaLiquidacion()) {
				liqid = false;
				senv = "La liquidación no está cerrada, no se puede enviar datos";
				return false;
			} else {
				liqid = true;
			}
		} else {
			liqid = true;
		}

		items.clear();
		dbld.clearlog();

		try {
			envioFacturas();
			envioPedidos();
			envioNotasCredito();

			envioCobros();

			envioDepositos();
			envio_D_MOV();
			envioCli();

			envioAtten();
			envioCoord();
			envioSolicitud();

			updateCorrelCXC();
			updateAcumulados();
			updateInventario();

			//updateLicence();

			envioFinDia();

			dbld.savelog();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return true;
	}

	private boolean validaLiquidacion() {
		Cursor DT;
		String ss;

		try {

			db.execSQL("DELETE FROM P_LIQUIDACION");

			AddTableVL("P_LIQUIDACION");

			if (listItems.size() < 2) {
				return true;
			}

			sql = listItems.get(0);
			db.execSQL(sql);

			sql = listItems.get(1);
			db.execSQL(sql);

			sql = "SELECT ESTADO FROM P_LIQUIDACION";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				ss = DT.getString(0);
				if (ss.equalsIgnoreCase("Cerrada")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			strliqid = e.getMessage();
			return false;
		}
	}

	public void envioFacturas() {
		Cursor DT;
		String cor, fruta, tt;
		int i, pc = 0, pcc = 0, ccorel;

		fterr = "";

		try {

			sql = "SELECT COREL,RUTA,CORELATIVO FROM D_FACTURA WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Facturas : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();

			dbld.clear();

			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);
				ccorel = DT.getInt(2);

				dbg = "::";

				try {

					i += 1;
					fprog = "Factura " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_FACTURA", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAP", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAF", "WHERE COREL='" + cor + "'");

					dbld.insert("D_STOCKB_DEV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_COREL SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_FACTURA SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\nFactura : " + sstr;
							dbg = sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
					dbg = e.getMessage();
				}

				DT.moveToNext();
			}

			sql = "DELETE FROM D_FACTURA_BARRA";
			db.execSQL(sql);
			sql = "DELETE FROM D_STOCKB_DEV";
			db.execSQL(sql);

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			dbg = fstr;
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Facturas : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Facturas : " + pc + "\n";
			}
		//}

	}

	public void envioPedidos() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			sql = "SELECT COREL FROM D_PEDIDO WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Pedidos : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Pedido " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_PEDIDO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_PEDIDOD", "WHERE COREL='" + cor + "'");

					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_PEDIDO SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							Toast.makeText(this, "Envio correcto", Toast.LENGTH_SHORT).show();
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}
				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Pedidos : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Pedidos : " + pc + "\n";
			}
		//}
	}

	public void envioCobros() {
		Cursor DT;
		String cor, fruta;
		int i, pc = 0, pcc = 0, corult;

		try {
			sql = "SELECT COREL,CORELATIVO,RUTA FROM D_COBRO WHERE STATCOM='N' ORDER BY COREL";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Cobros : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				corult = DT.getInt(1);
				fruta = DT.getString(2);

				try {

					i += 1;
					fprog = "Cobro " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_COBRO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD_SR", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROP", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_CORRELREC SET Actual=" + corult + "  WHERE RUTA='" + fruta + "'");

					if (envioparcial){
						if (commitSQL() == 1) {
							sql = "UPDATE D_COBRO SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\nCobro: " + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Cobros : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Cobros : " + pc + "\n";
			}
		//}
	}

	public void envioNotasCredito() {
		Cursor DT;
		String cor, fruta;
		int i, pc = 0, pcc = 0, ccorel;

		try {
			sql = "SELECT COREL,RUTA,CORELATIVO FROM D_NOTACRED WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas credito : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);
				ccorel = DT.getInt(2);

				try {

					i += 1;
					fprog = "Nota crédito " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_NOTACRED", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_CORELNC SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "'");
					dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'NC'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_NOTACRED SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Notas crédito : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Notas crédito : " + pc + "\n";
			}
		//}
	}

	public void envioDepositos() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			sql = "SELECT COREL FROM D_DEPOS WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Depósitos : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Depósito " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_DEPOS", "WHERE COREL='" + cor + "'");
					dbld.insert("D_DEPOSD", "WHERE COREL='" + cor + "'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_DEPOS SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Depósitos : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Depósitos : " + pc + "\n";
			}
		//}
	}

	public String Get_Corel_D_Mov() {
		Cursor DT;
		String cor = "";

		try {

			sql = "SELECT COREL FROM D_MOV WHERE (TIPO='D') ORDER BY COREL DESC ";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				cor = DT.getString(0);
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			msgbox(e.getMessage());
		}
		return cor;
	}

	public void envio_D_MOV() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			sql = "SELECT COREL FROM D_MOV WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				senv += "Inventario : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();

			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Inventario " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_MOV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVD", "WHERE COREL='" + cor + "'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							pc += 1;

						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Inventario : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Inventario : " + pc + "\n";
			}
		}
	}

	public void envioCli() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {
			sql = "SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Inventario : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Inventario " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) {
						dbld.clear();
					}

					dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");
					if (gl.peModal.equalsIgnoreCase("APR")) {
						dbld.insert("D_CLINUEVO_APR", "WHERE CODIGO='" + cor + "'");
					}

					if (envioparcial) {
						if (commitSQL() == 1) {

							sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
							db.execSQL(sql);
							if (gl.peModal.equalsIgnoreCase("APR")) {
								sql = "UPDATE D_CLINUEVO_APR SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							}

							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Cli. nuevos : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Cli. nuevos : " + pc + "\n";
			}
		}
	}

	public void envioAtten() {
		Cursor DT;
		String cor, hora;
		int fecha;

		fprog = " ";
		wsStask.onProgressUpdate();

		try {
			sql = "SELECT RUTA,FECHA,HORALLEG FROM D_ATENCION WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fecha = DT.getInt(1);
				hora = DT.getString(2);

				try {

					if (envioparcial) dbld.clear();

					dbld.insert("D_ATENCION", "WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_ATENCION SET STATCOM='S' WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ";
							db.execSQL(sql);
						} else {
							//fterr+="\n"+sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void envioCoord() {
		Cursor DT;
		String cod, ss;
		int stp;
		double px, py;
		fprog = " ";
		wsStask.onProgressUpdate();

		try {
			sql = "SELECT CODIGO,COORX,COORY,STAMP FROM D_CLICOORD WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cod = DT.getString(0);
				px = DT.getDouble(1);
				py = DT.getDouble(2);
				stp = DT.getInt(3);

				try {

					if (envioparcial) dbld.clear();

					ss = "UPDATE P_CLIENTE SET COORX=" + px + ",COORY=" + py + " WHERE (CODIGO='" + cod + "')";
					dbld.add(ss);

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CLICOORD SET STATCOM='S' WHERE (CODIGO='" + cod + "') AND (STAMP=" + stp + ") ";
							db.execSQL(sql);
						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void envioSolicitud() {
		Cursor DT;
		String cor;
		int i, pc = 0, pcc = 0;

		try {

			sql = "SELECT * FROM D_SOLICINVD";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			sql = "SELECT COREL FROM D_SOLICINV WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);

				try {

					i += 1;
					fprog = "Solicitud " + i;
					wsStask.onProgressUpdate();

					if (envioparcial) dbld.clear();

					dbld.insert("D_SOLICINV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_SOLICINVD", "WHERE COREL='" + cor + "'");

					if (envioparcial) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_SOLICINV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (envioparcial) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Solicitud : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Solicitud : " + pc + "\n";
			}
		}
	}

	public void envioFinDia() {

		fprog = " ";
		wsStask.onProgressUpdate();

		try {

			if (envioparcial) dbld.clear();

			dbld.add("DELETE FROM D_REPFINDIA WHERE RUTA='" + gl.ruta + "'");
			dbld.insert("D_REPFINDIA", "WHERE (LINEA>=0)");

			//if (envioparcial) commitSQL();

			commitSQL();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}
	}

	public void updateInventario() {
		DU = new DateUtils();
		String vFecha;
		int rslt;
		int vfecha = Get_Fecha_Inventario();
		//#HS_20181203_1000 Agregue DU.univfechaext(vfecha) para convertir la fecha a formato de yymmdd hhmm
		vFecha = DU.univfechasql(vfecha) + " 00:00:00";
		String corel_d_mov = Get_Corel_D_Mov();

		try {

			if (envioparcial) dbld.clear();

			ss = " UPDATE P_STOCK SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' )";
			dbld.add(ss);

			ss = " UPDATE P_STOCKB SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "')";
			dbld.add(ss);

			ss = " UPDATE P_STOCK_PALLET SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "')";
			dbld.add(ss);

			if (envioparcial) {
				//fterr=ss+"\n";
				rslt = commitSQL();
				//fterr=fterr+rslt+"\n";
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			//fterr=fterr+fstr;
		}

	}

	public void updateAcumulados() {
		long ff;
		int oyear, omonth, rslt;

		ff = du.getActDate();
		oyear = du.getyear(ff);
		omonth = du.getmonth(ff);

		try {

			if (envioparcial) dbld.clear();

			ss = "exec AcumuladoObjetivos '" + gl.ruta + "'," + oyear + "," + omonth;
			dbld.add(ss);

			if (envioparcial) {
				rslt = commitSQL();
				fterr = fterr + rslt + "\n";
			}

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
			fterr = fterr + fstr;
		}
	}

	public void updateLicence() {
		String SQL;
		String TN = "LIC_CLIENTE";

		try {

			fprog = TN;
			idbg = TN;
			listItems.clear();


			SQL = "SELECT * FROM LIC_CLIENTE WHERE ID='" + mac + "'";
			if (fillTable(SQL, "DELETE FROM LIC_CLIENTE") == 1) {
				idbg = idbg + SQL + "#" + "PASS OK";
			} else {
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = sstr;
			}
			idbg = idbg + " :: " + listItems.size();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = "Tab:" + TN + ", " + e.getMessage();
			idbg = idbg + e.getMessage();
		}
	}

	private  void updateCorrelCXC(){

	Cursor DT;
	int maximo;
	String serie;

	try{

			sql =" SELECT SERIE, ACTUAL FROM P_CORREL_OTROS WHERE TIPO = 'D'";
        	DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
					maximo = DT.getInt(1);
					serie =DT.getString(0);

					sql =" UPDATE P_CORREL_OTROS SET ACTUAL = "+maximo +
						 " WHERE RUTA = '"+ gl.ruta +"' AND SERIE = '"+serie +"' AND TIPO = 'D' " +
						 " AND ACTUAL < "+maximo;
					dbld.add(sql);
			}

		}catch ( Exception ex){
			msgbox("Ocurrió un error en ActCorrelDev " + ex.getMessage());

		}

	}

	public void addItem(String nombre,int env,int pend) {
		clsClasses.clsEnvio item;
		
		try {
			item=clsCls.new clsEnvio();
			
			item.Nombre=nombre;
			item.env=env;
			item.pend=pend;
			
			items.add(item);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
	}
	
	public void updateLicencePush() {
		String ss;
		
		try {
			ss=listItems.get(1);
			if (mu.emptystr(ss)) return;
			db.execSQL(ss);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//msgbox(e.getMessage());
		}
	}

	//#HS_20181219 funcion para crear JSON de fotos fachada.
	public void listaFachada(){

		Cursor DT;
		String codigo, imagen64,strImagen;
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		JSONArray json_Array = new JSONArray();

		System.setProperty("line.separator","\r\n");

		try {
			sql = "SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA = '"+gl.ruta+"'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				while (!DT.isAfterLast()){

					codigo = DT.getString(0);

					String paht = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + codigo + ".jpg");
					File archivo = new File(paht);

					if(archivo.exists()){

						/*LO CONVIERTE A BASE64*/
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						Bitmap bitmap = BitmapFactory.decodeFile(paht);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						byte[] imageBytes = baos.toByteArray();
						imagen64 = Base64.encodeToString(imageBytes,Base64.NO_PADDING);

						json = new JSONObject();
						json.put("CODIGO",codigo);
						json.put("IMAGEN",imagen64);
						json_Array.put(json);

					}

					DT.moveToNext();

				}

				json2.put("P_CLIENTE_FACHADA",json_Array);

			}

			jsonWS = json2.toString();

			//#HS_20181221 Se envian las fotos.
			if(envioFachada() == 1){
				String paht = (Environment.getExternalStorageDirectory() + "/RoadFotos");
				File archivo = new File(paht);
				EliminarArchivos(archivo);
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("listaFachada: " + e.getMessage());
		}
	}

	//#HS_20181221 Elimina las fotos de ROADFOTOS
	public void EliminarArchivos(File ArchivoDirectorio) {
		try{
			if (ArchivoDirectorio.isDirectory()) {
				for (File hijo : ArchivoDirectorio.listFiles())
					EliminarArchivos(hijo);
			} else {
				ArchivoDirectorio.delete();

			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region WS Envio Handling Methods
	
	public void wsSendExecute(){

		running=1;fstr="No connect";scon=0;
        errflag=false;

		try {

			if (getTest()==1) scon=1;

			if (scon==1) {
				fstr="Sync OK";

				if (!sendData()) {
					fstr="Envio incompleto : "+sstr;
				} else {
				}
			} else {
				fstr="No se puede conectar al web service : "+sstr;
			}
					
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
		}
	}
			
	public void wsSendFinished(){
				
		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);
		running=0;
		
		//senv="Envio completo\n";

		try{
            if (scon==0) {
                lblInfo.setText(fstr);writeErrLog(fstr);
                mu.msgbox(fstr);
            }

			if (!errflag) {
				lblInfo.setText(" ");

				if (!envioparcial){

					claseFindia.updateComunicacion(2);
					claseFindia.updateFinDia(du.getActDate());

					findiaactivo=gl.findiaactivo;
					if (ultimoCierreFecha()==du.getActDate()) findiaactivo=true;

					if (findiaactivo) {
						ActualizaStatcom();
						claseFindia.eliminarTablasD();
					}

					msgResultEnvio(senv);

				}

			} else {
				lblInfo.setText(fstr);writeErrLog(fterr);
				mu.msgbox(fterr);
			}

			if(envioparcial){

                findiaactivo=gl.findiaactivo;
                if (ultimoCierreFecha()==du.getActDate()) findiaactivo=true;

                if (findiaactivo) {
                    FinDia();
                    claseFindia.eliminarTablasD();
                }

            }

			visibilidadBotones();

			isbusy=0;

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
			
	private class AsyncCallSend extends AsyncTask<String, Void, Void> {

		@Override
	    protected Void doInBackground(String... params) {

			try {
				Looper.prepare();
				wsSendExecute();
			} catch (Exception e) {}

	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
			try {
				wsSendFinished();
				Looper.loop();
			}catch (Exception e) {}
		}
	 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(fprog);
    		} catch (Exception e) { }
        }
	 
    }
	
	//endregion

	//region WS Confirm Methods

	private void sendConfirm() {
		Cursor dt;

		try {
			try {
				sql = "SELECT DOCUMENTO FROM P_STOCK";
				dt = Con.OpenDT(sql);

				if (dt.getCount() > 0) {
					dt.moveToFirst();
					docstock = dt.getString(0);
				} else {
					docstock = "";
				}
			} catch (Exception e) {
				addlog(new Object() {
				}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				msgbox(new Object() {
				}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
			}

			//#HS_20181126_1603 Cambie el getActDate por getFechaActual
			sql = "UPDATE P_RUTA SET EMAIL='" + du.getActDate() + "'";
			db.execSQL(sql);

			Handler mtimer = new Handler();
			Runnable mrunner = new Runnable() {
				@Override
				public void run() {
					showprogress = false;
					wsCtask = new AsyncCallConfirm();
					wsCtask.execute();
				}
			};
			mtimer.postDelayed(mrunner, 500);
		}catch (Exception  e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
		}
	}

	//endregion
	
	//region WS Confirm Handling Methods

	public void wsConfirmExecute(){
		String univdate=du.univfecha(du.getActDate());
		isbusy=1;
		
		try {
			conflag=0;
					
			dbld.clear();
			dbld.add("DELETE FROM P_DOC_ENVIADOS_HH WHERE DOCUMENTO='"+docstock+"'");
			dbld.add("INSERT INTO P_DOC_ENVIADOS_HH VALUES ('"+docstock+"','"+ActRuta+"','"+univdate+"',1)");
						
			if (commitSQL()==1) conflag=1; else conflag=0;
					
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			fterr+="\n"+e.getMessage();
			dbg=e.getMessage();
		}
	}

	public void wsConfirmFinished(){
		try {
			isbusy = 0;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private class AsyncCallConfirm extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsConfirmExecute();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try{
				wsConfirmFinished();
			}catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), "");
			}

		}

		@Override
		protected void onPreExecute() {
			try {
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
			} catch (Exception e) {}
		}

	}	

	//endregion
	
	//region Aux
	
	public void comManual(View view) {
		try{
			Intent intent = new Intent(this,ComDrop.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void getWSURL() {
		Cursor DT;
		String wsurl;
		
		txtRuta.setText(ruta);
		txtEmp.setText(gEmpresa);
		
		try {

			sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();

				//if (gl.tipo==0) {
				//	wsurl=DT.getString(1);
				//} else {
				//	wsurl=DT.getString(0);
				//}

				wsurl=DT.getString(0);

				URL=wsurl;
				txtWS.setText(URL);
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//MU.msgbox(e.getMessage());
			//URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
			URL="*";txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");
			//URL="*";txtWS.setText("http://192.168.1.142/wsimagen/baktun1.asmx");
			//txtWS.setText("");
			return;

		}
		
	}
	
	private boolean setComParams() {
		String ss;
		
		ss=txtRuta.getText().toString().trim();

		try{
			if (mu.emptystr(ss)) {
				mu.msgbox("La ruta no esta definida.");return false;
			}
			ActRuta=ss;

			ss=txtEmp.getText().toString().trim();
			if (mu.emptystr(ss)) {
				mu.msgbox("La empresa no esta definida.");return false;
			}
			gEmpresa=ss;

			ss=txtWS.getText().toString().trim();
			//ss="http://192.168.1.142/wsAndr/wsandr.asmx";
			if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
				mu.msgbox("La dirección de Web service no esta definida.");return false;
			}
			URL=ss;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return true;
	}

	private int getDocCount(String ss,String pps) {

	    Cursor DT;
		int cnt = 0;
		String st;

		try {

			sql=ss;
			DT=Con.OpenDT(sql);

            if (DT.getCount()>0){
				DT.moveToFirst();
            	cnt=DT.getInt(0);
            }

            if (cnt>0) {
				st=pps+" "+cnt;
				sp=sp+st+", ";
			}

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//mu.msgbox(sql+"\n"+e.getMessage());
		}

        return cnt;

    }

	private boolean validaLicencia() {
		Cursor dt;
		String mac,lickey,idkey,binkey;
		int fval,lkey;
		long ff;

		try {
			mac=lic.getMac();
			lkey=lic.getLicKey(mac);
			lickey=lic.encodeLicence(lkey);

			sql="SELECT IDKEY,BINKEY FROM LIC_CLIENTE WHERE ID='"+mac+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

			dt.moveToFirst();
			idkey=dt.getString(0);
			binkey=dt.getString(1);

			if (!idkey.equalsIgnoreCase(lickey)) return false;

			ff=du.getActDate();
			fval=lic.decodeValue(binkey);
			fval=fval-lkey;

			//Toast.makeText(this,""+fval, Toast.LENGTH_SHORT).show();

			if (fval==999999) return true;
			fval=fval*10000;

			if (fval>=ff) return true; else return false;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());return false;
		}

	}

	private String getMac() {
		WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}
	
	private int ultCorel() {
		Cursor DT;
		int crl = 0;
		
		try {
			sql="SELECT CORELULT FROM P_COREL";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				crl=DT.getInt(0);
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}	
		
		return crl;
	}
	//#HS_20181129_1006 Agregue funcion para obtener la serie.
	private String ultSerie(){
		Cursor DT;
		String serie="";

		try{
			sql="SELECT SERIE FROM P_COREL";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0) {
				DT.moveToFirst();
				serie = DT.getString(0);
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("ultSerie(): "+e.getMessage());
		}

		return serie;
	}
	//#HS_20181121_1048 Se creo la funcion Get_Fecha_Inventario().
	private int Get_Fecha_Inventario() 	{
		Cursor DT;
		int fecha = 0;

		try {

			sql="SELECT IFNULL(EMAIL,0) AS FECHA FROM P_RUTA";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
                DT.moveToFirst();
				fecha=DT.getInt(0);
				if (fecha==0) 				{
					fecha = 1001010000 ;//#HS_20181129_0945 Cambie los valores de fecha porque deben se yymmdd hhmm
				}
			}

		} catch (Exception e) {
			//addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
		return fecha;
	}

    private int ultimoCierreFecha() {
        Cursor DT;
        int rslt=0;

        try {
            sql="SELECT val1 FROM FinDia";
            DT=Con.OpenDT(sql);
            DT.moveToFirst();
            rslt=DT.getInt(0);
        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            rslt=0;
        }

        return rslt;
    }

	private void visibilidadBotones() {
		Cursor dt;

		boolean recep=false;
		
		esvacio=false;

			try{
				try {
					sql="SELECT * FROM P_RUTA";
					dt=Con.OpenDT(sql);
					esvacio=dt.getCount()==0;
				} catch (Exception e) {
					//msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
					esvacio=true;
				}

				//Inicializa estos layout en invisible
				relExist.setVisibility(View.INVISIBLE);
				relPrecio.setVisibility(View.INVISIBLE);
				relStock.setVisibility(View.INVISIBLE);

				//Si entra en modo administración, habilita los botones y se va
				if (gl.modoadmin) {

					txtRuta.setEnabled(true);
					txtWS.setEnabled(true);
					txtEmp.setEnabled(true);

					if (esvacio) {
						lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
						lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
					}

					return;
				}

				//#HS_20181121_0910 Se creó la funcion Get_Fecha_Inventario().
				if (!esvacio){
					int fc=Get_Fecha_Inventario();
					recep=fc==du.getActDate();
				}

				//Invisible botón y texto de envío
				lblEnv.setVisibility(View.INVISIBLE);
				imgEnv.setVisibility(View.INVISIBLE);

				//Invisible botón y texto de recepción
				lblRec.setVisibility(View.INVISIBLE);
				imgRec.setVisibility(View.INVISIBLE);

				//Tiene documentos
				boolean TieneFact,TienePedidos,TieneCobros,TieneDevol,YaComunico, TieneInventario, TieneOtros;

				TieneFact = (clsAppM.getDocCountTipo("Facturas",false)>0?true:false);
				TienePedidos = (clsAppM.getDocCountTipo("Pedidos",false)>0?true:false);
				TieneCobros = (clsAppM.getDocCountTipo("Cobros",false)>0?true:false);
				TieneDevol = (clsAppM.getDocCountTipo("Devoluciones",false)>0?true:false);
				YaComunico=(claseFindia.getComunicacion() == 4?true:false);
				TieneInventario=(clsAppM.getDocCountTipo("Inventario",false)>0?true:false);

				if (gl.peModal.equalsIgnoreCase("TOL"))
                    {
                        if(claseFindia.yaHizoFindeDia())
                            {
                                if (YaComunico)
                                {
                                    if ((rutatipo.equalsIgnoreCase("V") && !TieneInventario) || (!rutatipo.equalsIgnoreCase("V")))
                                        {
                                            lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
                                            lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
											if (StringUtils.equals(GetStatusRec(),"1"))
												{
													relExist.setVisibility(gl.peBotInv?View.VISIBLE:View.INVISIBLE);
													relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
													relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
												}
											else
												{
													relExist.setVisibility(View.INVISIBLE);
													relStock.setVisibility(View.INVISIBLE);
													relPrecio.setVisibility(View.INVISIBLE);
												}
                                        }
                                    else if ((rutatipo.equalsIgnoreCase("V") && TieneInventario &&
                                            (TieneFact || TieneCobros || TieneDevol || TienePedidos)) ||
                                            (!rutatipo.equalsIgnoreCase("V")))
                                        {
                                            lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
                                            lblEnv.setVisibility(View.VISIBLE); imgEnv.setVisibility(View.VISIBLE);
                                            relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
                                            relExist.setVisibility(View.INVISIBLE);
                                            relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
                                        }
                                }
                            }
                        else
                            {
                                if ((!YaComunico) &&  !(TieneFact || TienePedidos) && !TieneCobros && !TieneDevol)
                                    {
                                        lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
                                        lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
										if (StringUtils.equals(GetStatusRec(),"1"))
											{
												relExist.setVisibility(gl.peBotInv?View.VISIBLE:View.INVISIBLE);
												relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
												relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
											}
										else
											{
												relExist.setVisibility(View.INVISIBLE);
												relStock.setVisibility(View.INVISIBLE);
												relPrecio.setVisibility(View.INVISIBLE);
											}
                                    }
                                else
                                    {
                                        if (YaComunico)
                                            {
                                                lblRec.setVisibility(View.VISIBLE);	imgRec.setVisibility(View.VISIBLE);
                                                lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);

												if (StringUtils.equals(GetStatusRec(),"1"))
													{
														relExist.setVisibility(gl.peBotInv?View.VISIBLE:View.INVISIBLE);
														relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
														relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
													}
												else
													{
														relExist.setVisibility(View.INVISIBLE);
														relStock.setVisibility(View.INVISIBLE);
														relPrecio.setVisibility(View.INVISIBLE);
													}
                                            }
                                        else
                                            {
                                                lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
                                                lblEnv.setVisibility(View.VISIBLE);imgEnv.setVisibility(View.VISIBLE);
												relExist.setVisibility(View.INVISIBLE);
												relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
												relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
                                            }
                                    }
                            }
                    }
				else
                    {
                        if(((rutatipo.equalsIgnoreCase("V")) || (rutatipo.equalsIgnoreCase("D")) && !TieneInventario)
                                ||((!rutatipo.equalsIgnoreCase("V")) && (!rutatipo.equalsIgnoreCase("D"))))
                            {

                            	lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
								lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);

								if (StringUtils.equals(GetStatusRec(),"1"))
									{
										relExist.setVisibility(gl.peBotInv?View.VISIBLE:View.INVISIBLE);
										relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
										relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
									}
                                else
									{
										relExist.setVisibility(View.INVISIBLE);
										relStock.setVisibility(View.INVISIBLE);
										relPrecio.setVisibility(View.INVISIBLE);
									}

                            }
                        else
                            {
                                if (((((rutatipo.equalsIgnoreCase("V")) || (rutatipo.equalsIgnoreCase("D")))&& TieneInventario
                                && (TieneFact || TieneCobros || TienePedidos) || TieneDevol)) ||((!rutatipo.equalsIgnoreCase("V"))
                                        && (!rutatipo.equalsIgnoreCase("D"))))
                                    {
                                        lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
										lblEnv.setVisibility(View.VISIBLE);imgEnv.setVisibility(View.VISIBLE);
                                        relExist.setVisibility(View.INVISIBLE);
										relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
                                        relPrecio.setVisibility(View.INVISIBLE);
                                    }
                            }
                    }

			}catch (Exception e) {
			    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(), sql);
		    }
	}
	
	private void paramsExtra() {
		try {
			AppMethods app=new AppMethods(this,gl,Con,db);
			app.parametrosExtra();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());
		}
	}

	private void writeErrLog(String errstr) {
		BufferedWriter writer = null;
		FileWriter wfile;

		try {
			String fname = Environment.getExternalStorageDirectory()+"/roaderror.txt";

			wfile=new FileWriter(fname,false);
			writer = new BufferedWriter(wfile);
			writer.write(errstr);writer.write("\r\n");
			writer.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	private void restarApp(){
		try{
			PackageManager packageManager = this.getPackageManager();
			Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
			ComponentName componentName = intent.getComponent();
			Intent mainIntent =Intent.makeRestartActivityTask(componentName);
			//Intent mainIntent = IntentCompat..makeRestartActivityTask(componentName);
			this.startActivity(mainIntent);
			System.exit(0);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
    public boolean ExistenDatosSinEnviar(){

        try {

            int cantFact,CantPedidos,CantCobros,CantDevol,CantInventario;

            clsAppM = new AppMethods(this, gl, Con, db);

            cantFact = clsAppM.getDocCountTipo("Facturas",true);
            CantPedidos = clsAppM.getDocCountTipo("Pedidos",true);
            CantCobros = clsAppM.getDocCountTipo("Cobros",true);
            CantDevol = clsAppM.getDocCountTipo("Devoluciones",true);
            CantInventario = clsAppM.getDocCountTipo("Inventario",true);

           return  ((cantFact>0) || (CantCobros>0) || (CantDevol>0) || (CantPedidos>0) || (CantInventario>0));

        } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());
            return false;
        }

    };

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
	public boolean ExisteInventario(){

		try {

			int CantInventario;

			clsAppM = new AppMethods(this, gl, Con, db);

			CantInventario = clsAppM.getDocCountTipo("Inventario",false);

			return  ((CantInventario>0));

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());
			return false;
		}

	};

	//CKFK 20190222 Se creó esta función para saber si existen datos en la base de datos
	public boolean ExistenDatos(){

		try {

			int cantFact,CantPedidos,CantCobros,CantDevol,CantInventario;

			clsAppM = new AppMethods(this, gl, Con, db);

			cantFact = clsAppM.getDocCountTipo("Facturas",false);
			CantPedidos = clsAppM.getDocCountTipo("Pedidos",false);
			CantCobros = clsAppM.getDocCountTipo("Cobros",false);
			CantDevol = clsAppM.getDocCountTipo("Devoluciones",false);
			CantInventario = clsAppM.getDocCountTipo("Inventario",false);

			return  ((cantFact>0) || (CantCobros>0) || (CantDevol>0) || (CantPedidos>0) || (CantInventario>0));

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(e.getMessage());
			return false;
		}

	};

	private void msgResultEnvio(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Envio correcto", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which)
				{
					ComWS.super.finish();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					if (gl.modoadmin) {
						restarApp();
					} else {
						finish();
					};
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	// #JP corregido 20190226
	private void BorraDatosAnteriores(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage(msg);
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					claseFindia.eliminarTablasD();
					msgAskConfirmaRecibido();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskExitComplete() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Está seguro de salir de la aplicación?");
			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskConfirmaRecibido(){

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Recepción");
			dialog.setMessage("¿Recibir datos nuevos?");

			dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runRecep();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	public void SetStatusRecTo(String estado)
	{
		try
		{
			sql = "UPDATE P_RUTA SET PARAM2='" + StringUtils.trim(estado) + "'";
			db.execSQL(sql);
		}
		catch (Exception ex)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+ " " + ex.getMessage());
		}

	}

	public String GetStatusRec()
	{
		Cursor DT;
		String vGetStatusRec = "";
		try
			{
				sql = "SELECT PARAM2 FROM P_RUTA ";
				DT = Con.OpenDT(sql);

				if (DT.getCount()> 0)
				{
					DT.moveToFirst();
					vGetStatusRec = DT.getString(0);
				}
			}

		catch (Exception ex)
			{
				Log.d("GetStatusRec","Something happend here " + ex.getMessage());
				msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+ " " + ex.getMessage());
			}

		return  vGetStatusRec;
	}


	//endregion

	//region Activity Events
	
	@Override
	public void onBackPressed() {
		try{
			if (isbusy==0) {
				if (gl.modoadmin) {
					msgAskExitComplete();
				}
				else{
					super.onBackPressed();
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

}
