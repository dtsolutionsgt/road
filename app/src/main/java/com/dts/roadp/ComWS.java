package com.dts.roadp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.InputType;
import com.dts.roadp.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class ComWS extends PBase {

	private TextView lblInfo, lblParam, lblRec, lblEnv, lblExis,lblEnvM;
	private ProgressBar barInfo;
	private EditText txtRuta, txtWS, txtEmp;
	private ImageView imgRec, imgEnv, imgExis,imgEnvM;
	private RelativeLayout ralBack, relExist, relPrecio, relStock;
    private TextView lblUser,lblPassword,txtVersion;
    private EditText txtUser, txtPassword;

	private int isbusy, fecha, lin, reccnt, ultcor, ultcor_ant, licResult, licResultRuta;
	private String err, ruta, rutatipo, sp, docstock, ultSerie, ultSerie_ant,rrs;
	private String licSerial,licRuta,licSerialEnc,licRutaEnc,parImprID;
	private boolean fFlag, showprogress, pendientes, envioparcial, findiaactivo, errflag,esEnvioManual=false;

	private SQLiteDatabase dbT;
	private DatabaseErrorHandler er;
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
    private String jsonWS,updCxC;
	private CryptUtil cu=new CryptUtil();

	protected PowerManager.WakeLock wakeLock;

	// Web Service -

	public AsyncCallRec wsRtask;
	public AsyncCallSend wsStask;
	public AsyncCallConfirm wsCtask;

	private static String sstr, fstr, fprog, finf, ferr, fterr, idbg, dbg, ftmsg, esql, ffpos;
	private int scon, running, pflag, stockflag, conflag;
	private String ftext, slsync, senv, gEmpresa, ActRuta, mac,rootdir;
	private String fsql, fsqli, fsqlf, strliqid;
	private boolean rutapos, ftflag, esvacio, liqid;

	private final String NAMESPACE = "http://tempuri.org/";
	private String METHOD_NAME, URL, URL_Remota;

	protected PowerManager.WakeLock wakelock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_ws);

		super.InitBase();
		addlog("ComWS", "" + du.getActDateTime(), gl.vend);

		System.setProperty("line.separator", "\r\n");
        rootdir=Environment.getExternalStorageDirectory()+"/RoadFotos/";

		dbld = new clsDataBuilder(this);
		claseFindia = new clsFinDia(this);
		clsAppM = new AppMethods(this, gl, Con, db);

		lblInfo = (TextView) findViewById(R.id.lblETipo);
		lblParam = (TextView) findViewById(R.id.lblProd);
		txtVersion = (TextView) findViewById(R.id.txtVersion);
		barInfo = (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta = (EditText) findViewById(R.id.txtRuta);
		txtRuta.setEnabled(false);
		txtWS = (EditText) findViewById(R.id.txtWS);
		txtWS.setEnabled(false);
		txtEmp = (EditText) findViewById(R.id.txtEmp);
		txtEmp.setEnabled(false);

		lblRec = (TextView) findViewById(R.id.btnRec);
		lblEnv = (TextView) findViewById(R.id.btnSend);
        lblEnvM = (TextView) findViewById(R.id.btnSenHand);

		imgEnv = (ImageView) findViewById(R.id.imageView6);
        imgEnvM = (ImageView) findViewById(R.id.imageView23);
		imgRec = (ImageView) findViewById(R.id.imgWaze);

		ralBack = (RelativeLayout) findViewById(R.id.relwsmail);
		relExist = (RelativeLayout) findViewById(R.id.relExist);
		relPrecio = (RelativeLayout) findViewById(R.id.relPrecio);
		relStock = (RelativeLayout) findViewById(R.id.relStock);

		resizeButtons();

		isbusy = 0;

		lblInfo.setText("");
		lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);

        lblUser = new TextView(this,null);
        lblPassword = new TextView(this,null);
		//txtVersion=new TextView(this, null);

        txtUser = new EditText(this,null);
        txtPassword = new EditText(this,null);

        txtVersion.setText(gl.parFechaVer);

		//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
		if (gl.debug) {
			if (mu.emptystr(txtRuta.getText().toString())) {
				txtRuta.setText("1");
				txtEmp.setText("009");
				txtWS.setText("http://190.34.215.189/wsandr/wsandr.asmx");
			}
		}

		if(gl.ruta.isEmpty()){
			ruta = txtRuta.getText().toString();
			gl.ruta=ruta;
		}else {
			ruta = gl.ruta;
		}

		if(gl.emp.isEmpty()){
			gEmpresa = txtEmp.getText().toString();
			gl.emp= gEmpresa;
		}else {
			gEmpresa = gl.emp;
		}

		ActRuta = ruta;
		rutatipo = gl.rutatipog;
		rutapos = gl.rutapos;

		if (gl.tipo == 0) {
			this.setTitle("Comunicación");
		} else {
			this.setTitle("Comunicación Local");
		}

		licSerial=gl.deviceId;
		licRuta=ruta;

		try {
			licSerialEnc=cu.encrypt(licSerial);
			licRutaEnc=cu.encrypt(licRuta);
		} catch (Exception e) {
			licSerialEnc="";
			licRutaEnc="";
		}

		gl.isOnWifi = clsAppM.isOnWifi();

		getWSURL();

		//#CKFK 20190319 Para facilidades de desarrollo se debe colocar la variable debug en true
		if (gl.debug) {
			if (mu.emptystr(txtRuta.getText().toString())) {
				txtRuta.setText("1");
				txtEmp.setText("009");
			}

			txtWS.setText("http://190.34.215.189/wsandr/wsandr.asmx");

			//txtRuta.setText("8001-1");
			//txtEmp.setText("03");
			//txtWS.setText("http://192.168.1.137/wsAndr/wsandr.asmx");
		}



		mac = getMac();
		fsql = du.univfechasql(du.getActDate());
		fsqli = du.univfechasql(du.ffecha00(du.getActDate())) + " 00:00:00";
		fsqlf = du.univfechasql(du.ffecha24(du.getActDate())) + " 23:59:59";

		parImprID="0";

		lic = new clsLicence(this);

		pendientes = validaPendientes();

		envioparcial = gl.peEnvioParcial;

		visibilidadBotones();

		//if (gl.autocom==1) runSend();

		//relExist.setVisibility(View.VISIBLE);

		if (esvacio) txtWS.setEnabled(true);

		setHandlers();

		try {
			final PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
			this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:mywakelocktag");
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakeLock");
		}
	}

	//region Events

	public void askRec(View view) {

		if (isbusy == 1) {
			toastcent("Por favor, espere que se termine la tarea actual.");
			return;
		}

		if(gl.ruta.isEmpty()){
			ruta = txtRuta.getText().toString();
			gl.ruta=ruta;
		}else {
			ruta = gl.ruta;
		}

		licSerial=gl.deviceId;
		licRuta=ruta;

		try {
			licSerialEnc=cu.encrypt(licSerial);
			licRutaEnc=cu.encrypt(licRuta);
		} catch (Exception e) {
			licSerialEnc="";
			licRutaEnc="";
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

			if (ExistenDatosSinEnviar()) {
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

			/*
			if (!validaLicencia()) {
				mu.msgbox("Licencia inválida!");
				return;
			}
			 */

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

    public void askSendManual(View view) {

        try {
            if (isbusy == 1) {
                toastcent("Por favor, espere que se termine la tarea actual.");
                return;
            }

            if (!validaLicencia()) {
                mu.msgbox("Licencia inválida!");
                return;
            }

            if (gl.banderafindia) {
                if (!puedeComunicar()) {
                    mu.msgbox("No ha hecho fin de dia, no puede comunicar datos");
                    return;
                }
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Envio");
            dialog.setMessage("¿Va a realizar la COMUNICACIÓN MANUAL, está seguro?");

            dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DatosSupervisor();
                }
            });

            dialog.setNegativeButton("Cancelar", null);

            dialog.show();

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

	public void askSendContinue() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("La COMUNICACIÓN MANUAL no envía los datos directamente a liquidación,¿está seguro?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (sendDataManual()){
						askOk();
					}
				}
			});

			dialog.setNegativeButton("No", null);

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	public void askSendCorrect() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Comunicación correcta?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					askOkCom();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					askIncorrect();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	public void askIncorrect() {

		try {

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Envio");
			dialog.setMessage("¿Está seguro de que la comunicacion NO fue correcta?");

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Eliminatablas();
				}
			});

			dialog.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}

	}

	private void askOk() {

		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Envio");//	alert.setMessage("Serial");

			final TextView input = new TextView(this);
			alert.setView(input);

			input.setText("Archivo de datos creado conecte el dispotivo al ordenador");
			input.requestFocus();

			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					askSendCorrect();
				}
			});

			alert.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

	private void askOkCom() {

		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Envio");//	alert.setMessage("Serial");

			final TextView input = new TextView(this);
			alert.setView(input);

			input.setText("Está seguro de que la comunicación fue correcta");
			input.requestFocus();

			alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Eliminatablas();
				}
			});

			alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					return;
				}
			});

			alert.show();
		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
		}


	}

    private void DatosSupervisor() {

        try{

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Envío");

            final LinearLayout layout   = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            if(lblUser.getParent()!= null){
                ((ViewGroup) lblUser.getParent()).removeView(lblUser);
            }

            if(lblPassword.getParent()!= null){
                ((ViewGroup) lblPassword.getParent()).removeView(lblPassword);
            }

            if(txtUser.getParent()!= null){
                ((ViewGroup) txtUser.getParent()).removeView(txtUser);
            }

            if(txtPassword.getParent()!= null){
                ((ViewGroup) txtPassword.getParent()).removeView(txtPassword);
            }

            lblUser.setText("Usuario: ");
            lblPassword.setText("Contraseña: ");
			txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

			txtUser.setText("");
			txtPassword.setText("");

            layout.addView(lblUser);
            layout.addView(txtUser);
			layout.addView(lblPassword);
            layout.addView(txtPassword);

            alert.setView(layout);

            alert.setCancelable(false);
            alert.create();

            alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    String usr, pwd;
					boolean dtCorrectos;

                    usr = txtUser.getText().toString().trim();
                    pwd = txtPassword.getText().toString().trim();

                    if (mu.emptystr(usr)) {
                        toast("Usuario incorrecto.");
                        return;
                    }

                    if (mu.emptystr(pwd)) {
                        toast("Contraseña incorrecta.");
						return;
                    }

					dtCorrectos = validaDatos(usr,pwd);

                    if (dtCorrectos){
						askSendContinue();
					}else{
						layout.removeAllViews();
						return;
					}

                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    layout.removeAllViews();
                }
            });

            alert.show();

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private boolean validaDatos(String user,String psw){

        Cursor DT;
	    boolean correctos=false;
        String dpsw;
	    try{


            sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + user + "' AND NIVEL=1";
            DT = Con.OpenDT(sql);

            if (DT.getCount() == 0) {
               mu.msgbox("Usuario incorrecto !");
				return false;
            }

            DT.moveToFirst();
            dpsw = DT.getString(1);
            if (!psw.equalsIgnoreCase(dpsw)) {
				mu.msgbox("Contraseña incorrecta !");
                return false;
            }


			correctos = true;

         }catch (Exception e){
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return false;
        }

        return  correctos;

    }

	private boolean sendDataManual() {

		errflag = false;

		senv = "Envío terminado \n \n";

		items.clear();
		dbld.clearlog();

		try {

			esEnvioManual = true;

			envioFacturas();

			envioPedidos();

			envioNotasCredito();

			envioNotasCreditoPP();

			envioNotasDevolucion();

			envioCobros();

			envioDepositos();

			envio_D_MOV();

			envioCli();

			envioAtten();

			envioCoord();

			envioSolicitud();

            envioRating();

			envioModifCliente();

			updateAcumulados();

			updateInventario();

			update_Corel_GrandTotal();

			envioFinDia();

			dbld.saveArchivo(du.getActDateStr());

			esEnvioManual = false;

			errflag = true;

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			esEnvioManual = false;
		}

		return errflag;
	}

	private void Eliminatablas(){
        boolean Eliminadas = false;
		try{

			claseFindia.updateFinDia(du.getActDate());
			claseFindia.updateComunicacion(2);

			ActualizaStatcom();
			Eliminadas = claseFindia.eliminarTablasD();

			if (Eliminadas){
			    mu.msgbox("Envío de datos correcto");
            }

			visibilidadBotones();

		}catch (Exception e){

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

	public void doFotos(View view) {
		startActivity(new Intent(this,ComWSFotos.class));
	}

	public void doFotosCli(View view) {
		startActivity(new Intent(this,comWSFotosCli.class));
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

			try{
				//#CKFK 20190313 Agregué esto para ocultar el teclado durante la carga de los datos
				View view = this.getCurrentFocus();
				view.clearFocus();
				if (view != null) {
					keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}catch (Exception e){
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
			dbld.saveArchivo(du.getActDateStr());
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
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_NOTACRED WHERE STATCOM<>'S'", "Dev : ");
			pend = pend + getDocCount("SELECT IFNULL(COUNT(COREL),0) FROM D_NOTACRED_PP WHERE STATCOM<>'S'", "PP : ");

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return pend > 0;
	}

	//endregion

	//region Common Web Service Methods

	public int fillTable(String value, String delcmd) {

		int rc;
		String s, ss, tabla;

		int retFillTable = 0;

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

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
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

            tabla=delcmd.substring(12);
            switch (tabla){

				case "P_RUTA":
					if (rc==1){
						borraDatos();
						throw new Exception("La ruta ingresada no es válida, ruta: " + ruta + ", no se puede continuar la carga de datos");
					}
					break;

				case "P_CLIENTE":
                    if (rc==1){
						borraDatos();
                        throw new Exception("No hay clientes definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
                    }
                break;

                case "P_PRODUCTO":
                    if (rc==1){
						borraDatos();
                        throw new Exception("No hay productos definidos para esta ruta: " + ruta + ", no se puede continuar la carga de datos");
                    }
                break;
                case "P_PRODPRECIO":
                    if (rc==1){
						borraDatos();
                        throw new Exception("No hay precios definidos para los productos de esta ruta:" + ruta + ", no se puede continuar la carga de datos");
                    }
                    break;
                case "P_COREL":
                    if (rc==1 && gl.rutatipo.equals("V")){
						borraDatos();
                        throw new Exception("No hay correlativos definidos para esta ruta:" + ruta + ", no se puede continuar la carga de datos");
                    }
                    break;
            }

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
						if (str.contains("D_PEDIDO")){
							sql = sql.replace("D_PEDIDO", "P_PEDIDO_RECHAZADO");
						}
						listItems.add(sql);
						sstr = str;
					} catch (Exception e) {
						addlog(new Object() {
						}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
						sstr = e.getMessage();
					}
				}
			}

			retFillTable= 1;

		} catch (Exception e) {

			//#EJC20190226: Evitar que se muestre OK después del nombre de la tabla cuando da error de timeOut.
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			retFillTable= 0;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, value);

		}

		return  retFillTable;
	}

	public int fillTableImpresora() {

		int rc;
		String s, ss, delcmd="DELETE FROM P_IMPRESORA";

		METHOD_NAME = "getInsImpresora";

		sstr = "OK";

		try {

			idbg = idbg + " filltableImpresora ";

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			/*
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");
			param.setValue(value);
			request.addProperty(param);
			*/
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapObject resSoap = (SoapObject) envelope.getResponse();
			SoapObject result = (SoapObject) envelope.bodyIn;

			rc = resSoap.getPropertyCount() - 1;
			idbg = idbg + " rec " + rc + "  ";

			s = "";

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
			sstr = e.getMessage();
			idbg = idbg + " ERR " + e.getMessage();
			return 0;
		}
	}

	public int commitSQL() {
		int rc;
		String s, ss;
		//#CKFK 20190429 Creé esta variable para retornar si la comunicación fue correcta o no
		//e hice modificaciones en la función para garantizar esta funcionalidad
		int vCommit=0;

		METHOD_NAME = "Commit";
		sstr = "OK";

		if (dbld.size() == 0) vCommit =1;//return 1

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

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) vCommit =1;// return 1;

			sstr = s;
			//return 0;

		} catch (Exception e) {
            vCommit=0;
            sstr = e.getMessage();

            addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
		}

		return vCommit;
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

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
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

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);

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

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
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

    public int sendFoto(String transid,String firmaid) {
        String fname,resstr="";

        fname=firmaid;

        METHOD_NAME = "saveImageROAD";

        try {

            Bitmap bmp = BitmapFactory.decodeFile(fname);

            if (bmp != null){

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG,75, out);
				byte[] imagebyte = out.toByteArray();
				String strBase64 = Base64.encodeBytes(imagebyte);

				int iv1=strBase64.length();

				SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;

				PropertyInfo param = new PropertyInfo();
				param.setType(String.class);
				param.setName("transid");
				param.setValue(transid);
				request.addProperty(param);

				PropertyInfo param2 = new PropertyInfo();
				param2.setType(String.class);
				param2.setName("imgdata");
				param2.setValue(strBase64);
				request.addProperty(param2);

				envelope.setOutputSoapObject(request);

				HttpTransportSE transport = new HttpTransportSE(URL,60000);
				transport.call(NAMESPACE + METHOD_NAME, envelope);

				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

				resstr = response.toString();

				if (resstr.equalsIgnoreCase("#")) {
					return 1;
				} else {
					throw new Exception(resstr);
				}
			}

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(),"");
            errflag=true;
        }

        return 0;
    }

	public int checkLicence(String serial) {
		int rc;
		String s, ss;

		METHOD_NAME = "checkLicence";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Serial");
			param.setValue(serial);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();
			if (s.equalsIgnoreCase("1")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			sstr = e.getMessage();
		}

		return 0;
	}

	public int checkLicenceRuta(String ruta) {
		int rc;
		String s, ss;

		METHOD_NAME = "checkLicenceRuta";
		sstr = "OK";

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Ruta");
			param.setValue(ruta);
			request.addProperty(param);

			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL, 60000);
			transport.call(NAMESPACE + METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();
			if (s.equalsIgnoreCase("1")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
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
		int ejecutarhh = 0;
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

			DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			val = "N";
		}

		if (val.equalsIgnoreCase("S")) gl.peStockItf = true;
		else gl.peStockItf = false;

		listItems.clear();
		scomp = 0;idbg = "";stockflag = 0;ftmsg = "";ftflag = false;rrs="...";

		try {

			if (!AddTable("P_PARAMEXT")) return false;
			procesaParamsExt();

			listItems.clear();
			if (!AddTable("P_RUTA")) return false;
			procesaRuta();

			listItems.clear();
			if (!AddTable("P_COREL")) return false;
			if (!AddTable("P_CLIENTE")) return false;
			if (!AddTable("P_PRODUCTO")) return false;
			if (!AddTable("P_PRODPRECIO")) return false;
			if (!AddTable("P_NIVELPRECIO")) return false;
			if (!AddTable("P_TIPONEG")) return false;
			if (!AddTable("P_CLIRUTA")) return false;
			if (!AddTable("P_CLIDIR")) return false;
			if (!AddTable("P_FACTORCONV")) return false;
			if (!AddTable("P_LINEA")) return false;
			if (!AddTable("TMP_PRECESPEC")) return false;
			if (!AddTable("P_DESCUENTO")) return false;
			if (!AddTable("P_EMPRESA")) return false;
			if (!AddTable("P_SUCURSAL")) return false;
			if (!AddTable("P_BANCO")) return false;
			if (!AddTable("P_STOCKINV")) return false;

			if (!AddTable("P_CODATEN")) return false;
			if (!AddTable("P_CODDEV")) return false;
			if (!AddTable("P_CODNOLEC")) return false;
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
            if (!AddTable("P_TRANSERROR")) return false;
			if (!AddTable("P_CATALOGO_PRODUCTO")) return false;
			if (!AddTable("P_GLOBPARAM")) return false;
			if (!AddTable("P_CONFIGBARRA")) return false;

			licResult=checkLicence(licSerial);
			licResultRuta=checkLicenceRuta(licRuta);

			fillTableImpresora();

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

			//Pedido Sugerido
			dbld.clear();
			dbld.add("EXEC SP_GENERA_PEDIDO_SUGERIDO_POR_RUTA '" + ruta + "'");
			dbld.add("EXEC SP_ULTIMOSPRECIOS '" + ruta + "'");
			commitSQL();

			if (!AddTable("P_PEDSUG")) return false;
			if (!AddTable("P_ULTIMOPRECIO")) return false;

			if (!AddTable("P_PEDIDO_RECHAZADO")) return false;
			if (!AddTable("DS_PEDIDO")) return false;
			if (!AddTable("DS_PEDIDOD")) return false;

		} catch (Exception e) {
			addlog(new Object() {}.getClass().getEnclosingMethod().getName(),idbg, fstr);
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

			prn = 0;jj = 0;
			Log.d("M", "So far so good");

			try{
                for (int i = 0; i < rc; i++) {

                    sql = listItems.get(i);
                    esql = sql;
                    sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
                    sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
                    sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

                    try {
                        writer.write(sql);
                        writer.write("\r\n");
                    } catch (Exception e) {
                        Log.d("M", "Write Something happend there " + e.getMessage());
                    }
                }

                writer.close();

            }catch (Exception ex){
                Log.d("M", "Write Something happend there " + ex.getMessage());
            }

            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {

				sql = listItems.get(i);esql = sql;
				sql = sql.replace("INTO VENDEDORES", "INTO P_VENDEDOR");
				sql = sql.replace("INTO P_RAZONNOSCAN", "INTO P_CODNOLEC");
                sql = sql.replace("INTO P_ENCABEZADO_REPORTESHH_II", "INTO P_ENCABEZADO_REPORTESHH");

				/*try {
					writer.write(sql);writer.write("\r\n");
				} catch (Exception e) {
					Log.d("M", "Write Something happend there " + e.getMessage());
				}*/

				try {
					dbT = ConT.getWritableDatabase();
					dbT.execSQL(sql);
				} catch (Exception e) {
					ferr += " " +e.getMessage();
					Log.d("M", "Something happend there " + e.getMessage() + "EJC" + " Yo fui " + sql);
					Log.e("z", e.getMessage());
				}

				try {

					if (i % 10 == 0) {
						fprog = "Procesando: " + i + " de: " + (rc - 1);
						wsRtask.onProgressUpdate();
						SystemClock.sleep(20);
					}
				} catch (Exception e) {
					Log.e("z", e.getMessage());
					ferr += " " +e.getMessage();
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				}
			}

			fprog = "Procesando: " + (rc - 1) + " de: " + (rc - 1);
			wsRtask.onProgressUpdate();

			Actualiza_FinDia();
			encodePrinters();
			encodeLicence();
			encodeLicenceRuta();

            SetStatusRecToTrans("1");

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			fprog = "Documento de inventario recibido en BOF...";
			wsRtask.onProgressUpdate();

			fechaCarga();
			Actualiza_Documentos();

			fprog = "Fin de actualización";
			wsRtask.onProgressUpdate();

			scomp = 1;

			try {
				ConT.close();
			} catch (Exception e) {
				//addlog(new Object() {	}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			}

			return true;

		} catch (Exception e) {
			fprog = "Actualización incompleta";
			wsRtask.onProgressUpdate();

			Log.e("Error", e.getMessage());
			try {
				ConT.close();
			} catch (Exception ee) {
				sstr = e.getMessage();
				ferr += " " + sstr;
			}

			sstr = e.getMessage();
			ferr += " " + sstr + "\n" + sql;
			esql = sql;

			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), ferr, esql);

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

	private void procesaRuta() {
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
				sql = "SELECT VENTA FROM P_RUTA";
				dt = Con.OpenDT(sql);
				dt.moveToFirst();
				val = dt.getString(0);
			} catch (Exception e) {
				val = "V";
			}
			gl.rutatipo = val;

		} catch (Exception e) {
			try {
				ConT.close();
			} catch (Exception ee) {
			}
		}

	}

	private boolean borraDatos(){

		try {

			db.beginTransaction();

			sql="DELETE FROM P_RUTA";db.execSQL(sql);
			sql="DELETE FROM P_PRODUCTO";db.execSQL(sql);
			sql="DELETE FROM P_COREL";db.execSQL(sql);
			sql="DELETE FROM P_PARAMEXT";db.execSQL(sql);
			sql="DELETE FROM P_PRODPRECIO";db.execSQL(sql);
			sql="DELETE FROM P_CLIENTE";db.execSQL(sql);

			db.setTransactionSuccessful();
			db.endTransaction();

		} catch (SQLException e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			//mu.msgbox("Error : " + e.getMessage());
			return false;
		}

		return true;
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

				sql = "UPDATE FINDIA SET COREL = " + vCorelZ + ", VAL1=0, VAL2=0, VAL3=0, VAL4=0,VAL5=0, VAL6=0, VAL7=0, VAL8 = " + vGrandTotal;
				dbT.execSQL(sql);
			}

			DT1.close();

		} catch (Exception ex) {
			vActualizaFD = false;
		}

		return vActualizaFD;

	}

	private boolean update_Corel_GrandTotal() {

		Cursor DT1;
		int vCorelZ = 0;
		float vGrandTotal = 0;
        String vNumPlaca;

		boolean vActualizaFD = true;

		try {

			sql = "SELECT NUMPLACA, CORELZ, GRANDTOTAL FROM P_HANDHELD";
			DT1 = Con.OpenDT(sql);

			if (DT1.getCount() > 0) {

				DT1.moveToFirst();

                vNumPlaca = DT1.getString(0);
				vCorelZ = DT1.getInt(1);
				vGrandTotal = DT1.getFloat(2);

				sql = "UPDATE P_HANDHELD SET CORELZ = " + vCorelZ + ", GRANDTOTAL = " + vGrandTotal + " WHERE NUMPLACA = '" + vNumPlaca + "'";
				dbld.add(sql);
			}

			DT1.close();

		} catch (Exception ex) {
			vActualizaFD = false;
			fstr=ex.getMessage();
		}

		return vActualizaFD;

	}

	private boolean AddTable(String TN) {
		String SQL;

		try {

			fprog = TN;idbg = TN;
			wsRtask.onProgressUpdate();
			SQL = getTableSQL(TN);

			if (fillTable(SQL, "DELETE FROM " + TN) == 1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " ok ";
				idbg = idbg + SQL + "#" + "PASS OK";
				return true;
			} else {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg = dbg + " fail " + sstr;
				idbg = idbg + SQL + "#" + " PASS FAIL  ";
				fstr = "Tabla:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tabla:" + TN;
			idbg = idbg;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(),idbg, fstr);
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
				fstr = "Tabla:" + TN + " " + sstr;
				return false;
			}

		} catch (Exception e) {
			fstr = "Tabla:" + TN + ", " + e.getMessage();
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
			SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, ISNULL(DESCUENTO,'N') AS DESCUENTO,ISNULL(BONIFICACION,'N') AS BONIFICACION, ";
			SQL += "IMP1, IMP2, IMP3, VENCOMP, ISNULL(DEVOL,'S') AS DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,	MODIF_PRECIO,IMAGEN, ";
			SQL += "VIDEO,VENTA_POR_PESO,ES_PROD_BARRA,UNID_INV,VENTA_POR_PAQUETE,VENTA_POR_FACTOR_CONV,ES_SERIALIZADO,PARAM_CADUCIDAD, ";
			SQL += "PRODUCTO_PADRE,FACTOR_PADRE,TIENE_INV,TIENE_VINETA_O_TUBO,PRECIO_VINETA_O_TUBO,ES_VENDIBLE,	UNIGRASAP,UM_SALIDA ";
			SQL += "FROM P_PRODUCTO WHERE (ES_VENDIBLE=1) AND ((CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
					" OR CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "'))" +
					" OR LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "') AND EMPRESA = '" + gEmpresa + "') OR UNIDMED='CAN' )" +
					" AND CODIGO IN ( " +
					" SELECT CODIGO FROM P_PRODPRECIO WHERE (NIVEL IN ( SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE " +
					" WHERE (CODIGO IN ( SELECT DISTINCT CLIENTE FROM DS_PEDIDO WHERE (RUTA ='" + ActRuta + "') AND (BANDERA='D')))))OR " +
					" NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE  " +
					" WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "')))" +
					" OR TIENE_VINETA_O_TUBO = 1 ";

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
			SQL += " AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "')))";
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
			SQL = "SELECT CODIGO, NOMBRE, ACTIVO, VENDEDOR, VENTA, FORANIA, SUCURSAL, TIPO, SUBTIPO, " +
				  "BODEGA, SUBBODEGA, DESCUENTO, BONIF, KILOMETRAJE, IMPRESION, RECIBOPROPIO, CELULAR, RENTABIL, OFERTA, " +
				  "PERCRENT, PASARCREDITO, TECLADO, EDITDEVPREC, EDITDESC, PARAMS, SEMANA, OBJANO, OBJMES, SYNCFOLD, WLFOLD, " +
				  "FTPFOLD, EMAIL, LASTIMP, LASTCOM, LASTEXP, IMPSTAT, EXPSTAT, COMSTAT, PARAM1, " +
			 	  "PARAM2, PESOLIM, INTERVALO_MAX, LECTURAS_VALID, INTENTOS_LECT, HORA_INI, HORA_FIN, " +
				  "APLICACION_USA, PUERTO_GPS, ES_RUTA_OFICINA, DILUIR_BON, PREIMPRESION_FACTURA, MODIFICAR_MEDIA_PAGO, " +
				  "IDIMPRESORA, NUMVERSION,0 AS FECHAVERSION, ARQUITECTURA " +
				  "FROM P_RUTA WHERE CODIGO = '" + ActRuta + "'";
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

		if (TN.equalsIgnoreCase("P_TIPONEG")) {
			SQL = "SELECT CODIGO, NOMBRE FROM P_TIPONEG ";
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
			SQL = "SELECT CODIGO,TEXTO,SUCURSAL FROM P_ENCABEZADO_REPORTESHH_II WHERE SUCURSAL IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '"+ActRuta+"')";
			return SQL;
		}


		if (TN.equalsIgnoreCase("P_BONLIST")) {
			SQL = "SELECT CODIGO,PRODUCTO,CANT,CANTMIN,NOMBRE FROM P_BONLIST "+
				  "	WHERE CODIGO IN (SELECT LISTA FROM P_BONIF WHERE TIPOLISTA in (1,2) "+
				  "	AND DATEDIFF(D, FECHAINI,GETDATE()) >=0 AND DATEDIFF(D,GETDATE(), FECHAFIN) >=0  AND EMP = '" + gEmpresa +"')";
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
                    " ISNULL(MODIFICADA,'') AS MODIFICADA, " +
                    "0 AS FECHA_CREADA,0 AS FECHA_MODIFICADA,MACADDRESS FROM P_IMPRESORA";
			return SQL;
		}

        //#CKFK_20190522 Agregué tabla P_TRANSERROR
        if (TN.equalsIgnoreCase("P_TRANSERROR")) {
            SQL = " SELECT IDTRANSERROR, TRANSERROR FROM P_TRANSERROR";
            return SQL;
        }

        //#CKFK_20190707 Agregué tabla P_GLOBPARAM
		if (TN.equalsIgnoreCase("P_GLOBPARAM")) {
			SQL = " SELECT EMPID, COMSERVER, FTPSERVER, VERFACTURA, VERPEDIDO, VERCOBRO, VALORN1 FROM P_GLOBPARAM";
			return SQL;
		}

		//#CKFK_20190522 Agregué tabla P_CONFIGBARRA
		if (TN.equalsIgnoreCase("P_CONFIGBARRA")) {
			SQL = " SELECT IDCONFIGBARRA, LONGITUDBARRA, PREFIJO FROM P_CONFIGBARRA";
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
			SQL = " SELECT CODIGO, EMPRESA, DESCRIPCION, NOMBRE, DIRECCION, TELEFONO, NIT, TEXTO, COLGRID " +
					" FROM P_SUCURSAL WHERE CODIGO IN (SELECT SUCURSAL FROM P_RUTA WHERE CODIGO = '" + ActRuta + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_CATALOGO_PRODUCTO")) {
			SQL = " SELECT CODIGO_PRODUCTO, ORDEN FROM P_CATALOGO_PRODUCTO " +
				  "	WHERE CODIGO_PRODUCTO IN (SELECT CODIGO ";
			SQL += "FROM P_PRODUCTO WHERE ((CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
					" OR CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "'))" +
					" OR LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')) OR UNIDMED='CAN' )" +
					" AND CODIGO IN ( " +
					" SELECT CODIGO FROM P_PRODPRECIO WHERE (NIVEL IN ( SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE " +
					" WHERE (CODIGO IN ( SELECT DISTINCT CLIENTE FROM DS_PEDIDO WHERE (RUTA ='" + ActRuta + "') AND (BANDERA='D')))))OR " +
					" NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE  " +
					" WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "')))" +
					" OR TIENE_VINETA_O_TUBO = 1 )";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PEDSUG")) {
			SQL = " SELECT IDPEDIDOSUG, RUTA, VENDEDOR, CLIENTE, PRODUCTO, CANTIDAD, UNIDADMEDIDA, PESO, UNIDADMEDIDAPESO, PRECIO, " +
				  " TOTAL, ORDEN, ACIERTO, FECHAGENERACION, FECHASISTEMA, USERAGR, FECHAAGR, USERMOD, FECHAMOD "+
				  " FROM P_PEDSUG " +
		          " WHERE RUTA='" + ActRuta + "' AND (FECHAGENERACION>='" + fsqli + "') AND (FECHAGENERACION<='" + fsqlf + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_ULTIMOPRECIO")) {
			SQL = " SELECT IDULTIMOPRECIO, RUTA, CLIENTE, PRODUCTO, PRECIO, FECHAGENERACION "+
					" FROM P_ULTIMOPRECIO " +
					" WHERE RUTA='" + ActRuta + "' AND (FECHAGENERACION>='" + fsqli + "') AND (FECHAGENERACION<='" + fsqlf + "')";
			return SQL;
		}

		if (TN.equalsIgnoreCase("P_PEDIDO_RECHAZADO")) {
			SQL = " SELECT COREL, FECHA, EMPRESA, SUCURSAL, RUTA, VENDEDOR, CLIENTE, TOTAL, PESO, RAZON_RECHAZADO "+
				  " FROM D_PEDIDO  " +
				  " WHERE RUTA='" + ActRuta + "' AND INFORMADO = 0 AND RECHAZADO = 1";
			return SQL;
		}

		if (TN.equalsIgnoreCase("DS_PEDIDO")) {
			SQL = " SELECT COREL, ANULADO, FECHA, EMPRESA, RUTA, VENDEDOR, CLIENTE, KILOMETRAJE, FECHAENTR, DIRENTREGA, " +
				  " TOTAL, DESMONTO, IMPMONTO, PESO, BANDERA, STATCOM, CALCOBJ, IMPRES, ADD1, ADD2, ADD3 "+
				  " FROM DS_PEDIDO " +
				  " WHERE RUTA='" + ActRuta + "' ";
			return SQL;
		}

		if (TN.equalsIgnoreCase("DS_PEDIDOD")) {
			SQL = " SELECT COREL, PRODUCTO, EMPRESA, ANULADO, CANT, PRECIO, IMP, DES, DESMON, TOTAL, PRECIODOC, " +
					"PESO, VAL1, VAL2, RUTA "+
				  " FROM DS_PEDIDOD " +
				  " WHERE COREL IN (SELECT COREL FROM DS_PEDIDO WHERE RUTA = '" + ActRuta + "' )";
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
				db.execSQL("UPDATE D_NOTACRED_PP SET STATCOM='S'");
				db.execSQL("UPDATE D_CXC SET STATCOM='S'");
				db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
				db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
				db.execSQL("UPDATE D_MOV SET STATCOM='S'");
				db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
				db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
				db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
				db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
                db.execSQL("UPDATE D_RATING SET STATCOM='S'");
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
			db.execSQL("UPDATE D_NOTACRED_PP SET STATCOM='S'");
			db.execSQL("UPDATE D_CXC SET STATCOM='S'");
			db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
			db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
			db.execSQL("UPDATE D_MOV SET STATCOM='S'");
			db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
			db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
			db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
			db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
			db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
            db.execSQL("UPDATE D_RATING SET STATCOM='S'");
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

	private void encodePrinters() {
		Cursor dt;
		String prid,ser,mac,se,sm;

		try {
			sql="SELECT IDIMPRESORA,NUMSERIE,MACADDRESS FROM P_IMPRESORA";
			dt=ConT.OpenDT(sql);

			if (dt.getCount() > 0) dt.moveToFirst();
			while (!dt.isAfterLast()) {

				prid=dt.getString(0);
				ser=dt.getString(1);
				mac=dt.getString(2);

				se=cu.encrypt(ser);
				sm=cu.encrypt(mac);

				sql="UPDATE P_IMPRESORA SET NUMSERIE='"+se+"',MACADDRESS='"+sm+"' WHERE IDIMPRESORA='"+prid+"'";
				dbT.execSQL(sql);

				dt.moveToNext();
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	private void encodeLicence() {
		String lic;

		try {
			if (licResult==1) lic=licSerialEnc; else lic="";
			sql = "UPDATE Params SET lic='" + lic + "'";
			dbT.execSQL(sql);
		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}

	}

	private void encodeLicenceRuta() {
		String lic;

		try {
			if (licResultRuta==1) lic=licRutaEnc; else lic="";
			sql = "UPDATE Params SET licparam='" + lic + "'";
			dbT.execSQL(sql);
		} catch (Exception e) {
			msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
		}

	}

	private boolean validaLicencia() {
		CryptUtil cu=new CryptUtil();
		Cursor dt;
		String lic,lickey,licruta,rutaencrypt;
		Integer msgLic=0;

		try {
			lickey=cu.encrypt(gl.deviceId);
			rutaencrypt=cu.encrypt(gl.ruta);

			sql="SELECT lic, licparam FROM Params";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			lic=dt.getString(0);
			licruta=dt.getString(1);
			String ss=cu.decrypt(licruta);

			if (lic.equalsIgnoreCase(lickey) && licruta.equalsIgnoreCase(rutaencrypt)) return true;
			else if (!lic.equalsIgnoreCase(lickey) && !licruta.equalsIgnoreCase(rutaencrypt)){msgLic=1;}
			else if(!lic.equalsIgnoreCase(lickey)){msgLic=2;}
			else if(!licruta.equalsIgnoreCase(rutaencrypt)){msgLic=3;}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" : "+e.getMessage());
		}

		if(msgLic==1)toastlong("El dispositivo no tiene licencia válida de handheld, ni de ruta");
		else if(msgLic==2){toastlong("El dispositivo no tiene licencia valida de handheld");}
		else if(msgLic==3){toastlong("El dispositivo no tiene licencia valida de ruta");}

		return false;
	}

	private void fechaCarga() {

		try {
			dbT.beginTransaction();

			dbT.execSQL("DELETE FROM P_FECHA");

			sql="INSERT INTO P_FECHA VALUES('"+gl.ruta+"',"+du.getActDate()+")";
			dbT.execSQL(sql);

			dbT.setTransactionSuccessful();
			dbT.endTransaction();
		} catch (Exception e) {
			dbT.endTransaction();
		}

	}

	//endregion

	//region WS Recepcion Handling Methods

	public void wsExecute() {

		running = 1;
		fstr = "No connect";
		scon = 0;

		try {

			if (getTest() == 1) {
				scon = 1;
			} else {
				URL = URL_Remota;
				if (getTest() == 1) {
					scon = 1;
				}
			}

			idbg = idbg + sstr;

			if (scon == 1) {
				fstr = "Sync OK";
				if (!getData()) fstr = "Recepcion incompleta : " + fstr;
			} else {
				fstr = "No se puede conectar al web service : " + sstr;
			}

		} catch (Exception e) {
			scon = 0;
			fstr = "Error importando los datos: " + fstr;
			Log.d("E", fstr + " " + sstr);
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
				if (stockflag == 1) s = s + "\nSe actualizó inventario.";

				clsAppM.estandartInventario();
				validaDatos(true);

				if (stockflag == 1) sendConfirm();

				msgAskExit(s);

			} else {
				lblInfo.setText(fstr);
				mu.msgbox("Ocurrió error : \n" + fstr + " " + idbg + " (Registro: " + reccnt + ") ");
				//mu.msgbox("::" + esql);
				isbusy = 0;
				barInfo.setVisibility(View.INVISIBLE);
				addlog("Recepcion", fstr, idbg);
				return;
			}

			pendientes = validaPendientes();
			visibilidadBotones();

			isbusy = 0;
			comparaCorrel();

			otrosParametros();

			if (ftflag) msgbox(ftmsg);
		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		if (!validaLicencia()) restartApp();

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
		int rslv;

		errflag = false;

		if (getTest() == 0) {

			URL = URL_Remota;

			if (getTest() == 0) {
				errflag = true;
				return false;
			}
		}

		senv = "Envío terminado \n \n";

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			rslv=validaLiquidacion();
			if (rslv!=1) {
				liqid = false;
				if (rslv==0) {
					senv = "La liquidación no está cerrada, no se puede enviar datos";
				} else {
					senv = "No se puede determinar estado de la liquidación.";
				}
				return false;
			} else {
				liqid = true;
			}
		} else {
			liqid = true;
		}

		items.clear();

		generaArchivoBck();

		dbld.clearlog();

		try {

			envioFacturas();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}
			envioPedidos();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}
			envioNotasCredito();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}
			envioNotasCreditoPP();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}
			envioNotasDevolucion();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioCobros();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioDepositos();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envio_D_MOV();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioCli();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioAtten();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioCoord();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioSolicitud();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}
            envioRating();
            if (!fstr.equals("Sync OK")){
                dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
            }

			envioModifCliente();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			updateAcumulados();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			updateInventario();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			if (!update_Corel_GrandTotal()){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			envioFinDia();
			if (!fstr.equals("Sync OK")){
				dbld.savelog();
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),fstr,"");
				return false;
			}

			dbld.savelog();
			dbld.saveArchivo(du.getActDateStr());

            listaFachada();

			//#CKFK 20190429 Saqué esto de envioFinDia para que se guarde bien el log y luego se realice el envío.
			if (!envioparcial){

				if (getTest() == 1) {
					scon = 1;
				} else {
					URL = URL_Remota;
					if (getTest() == 1) {
						scon = 1;
					}
				}

				if (scon==1) {
					if (commitSQL() == 1) {
						errflag=false;
						return true;
					} else {
						errflag=true;
						fterr += "\n" + sstr;
						return false;
					}
				} else {
					errflag=true;
					fstr="No se puede conectar al web service : "+sstr;
					fterr += "\n" + fstr;
					return false;
				}
			} else {
				return true;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return true;
	}

	private boolean generaArchivoBck() {

		errflag = false;

		items.clear();
		dbld.clearlog();

		try {

			esEnvioManual = true;

			envioFacturas();

			envioPedidos();

			envioNotasCredito();

			envioNotasCreditoPP();

			envioNotasDevolucion();

			envioCobros();

			envioDepositos();

			envio_D_MOV();

			envioCli();

			envioAtten();

			envioCoord();

			envioSolicitud();

			envioRating();

			updateAcumulados();

			updateInventario();

			update_Corel_GrandTotal();

			envioFinDia();

			dbld.saveArchivo_bck(du.getActDateStr());

            esEnvioManual = false;

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			esEnvioManual = false;
		}

		return errflag;
	}

	private int validaLiquidacion() {
		Cursor DT;
		String ss;

		try {

			listItems.clear();

			db.execSQL("DELETE FROM P_LIQUIDACION");

			AddTableVL("P_LIQUIDACION");

			if (listItems.size() < 2) return 1;

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
					return 1;
				} else {
					return 0;
				}
			} else {
				return 1;
			}
		} catch (Exception e) {
			strliqid = e.getMessage();
			return -1;
		}
	}

	public void envioFacturas() {
		Cursor DT;
		String cor, fruta, tt;
		int i, pc = 0, pcc = 0, ccorel;

		fterr = "";
		err="";

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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

					if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_FACTURA", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAP", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAD_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_FACTURAF", "WHERE COREL='" + cor + "'");

					dbld.insert("D_STOCKB_DEV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_BARRA", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_COREL SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "' " +
							"AND CORELULT<" + ccorel);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_FACTURA SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\nFactura : " + sstr;
							dbg = sstr;
						}
					} else pc += 1;

				} catch (Exception e) {
					errflag = true;

					dbld.savelog("facturas.txt");

					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
					dbg = e.getMessage();
				}

				DT.moveToNext();
			}

			DT.close();

			if (envioparcial && (fterr.isEmpty()) && !esEnvioManual) {
				sql = "DELETE FROM D_FACTURA_BARRA";
				db.execSQL(sql);
				sql = "DELETE FROM D_STOCKB_DEV";
				db.execSQL(sql);
			}

		} catch (Exception e) {
			errflag = true;

			dbld.savelog("facturas.txt");

			addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}


                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_PEDIDO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_PEDIDOD", "WHERE COREL='" + cor + "'");

					dbld.insert("D_BONIF", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIF_LOTES", "WHERE COREL='" + cor + "'");
					dbld.insert("D_REL_PROD_BON", "WHERE COREL='" + cor + "'");
					dbld.insert("D_BONIFFALT", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
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
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}
				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
            if(gl.pCobrosDepositados){
				sql = "SELECT COREL,CORELATIVO,RUTA FROM D_COBRO WHERE STATCOM='N' AND DEPOS = 'S' ORDER BY COREL";
			}else{
				sql = "SELECT COREL,CORELATIVO,RUTA FROM D_COBRO WHERE STATCOM='N' ORDER BY COREL";
			}

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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_COBRO", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROD_SR", "WHERE COREL='" + cor + "'");
					dbld.insert("D_COBROP", "WHERE COREL='" + cor + "'");

					dbld.add("UPDATE P_CORRELREC SET Actual=" + corult + "  WHERE RUTA='" + fruta + "' " +
							"AND Actual<"+corult);

					if (envioparcial && !esEnvioManual){
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
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
		String cor, fruta="";
		int i, pc = 0, pcc = 0, ccorel;

		try {

			sql = "SELECT COREL,RUTA,CORELATIVO FROM D_NOTACRED WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas crédito : " + pc + "\n";
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_NOTACRED", "WHERE COREL='" + cor + "'");
					dbld.insert("D_NOTACREDD", "WHERE COREL='" + cor + "'");

					//dbld.add("UPDATE P_CORELNC SET CORELULT=" + ccorel + "  WHERE RUTA='" + fruta + "'");
					dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'NC' " +
							"AND ACTUAL<" + ccorel);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_NOTACRED SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag=true;
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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

	public void envioNotasCreditoPP() {
		Cursor DT;
		String cor, fruta="";
		int i, pc = 0, pcc = 0, ccorel;

		try {

			sql = "SELECT COREL,RUTA FROM D_NOTACRED_PP WHERE STATCOM='N' ORDER BY COREL";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas crédito pronto pago : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);

				try {

					i += 1;
					fprog = "Nota crédito pronto pago" + i;
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

					if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_NOTACRED_PP", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_NOTACRED_PP SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag=true;
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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

	public void envioNotasDevolucion() {
		Cursor DT;
		String cor, fruta = "", serie="";
		int i, pc = 0, pcc = 0, ccorel=0;

		try {

			sql = "SELECT COREL,RUTA FROM D_CXC WHERE STATCOM='N' ORDER BY COREL";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) {
				senv += "Notas de devolución : " + pc + "\n";
				return;
			}

			pcc = DT.getCount();
			pc = 0;
			i = 0;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				cor = DT.getString(0);
				fruta = DT.getString(1);

				try {
					i += 1;
					fprog = "Nota de devolución " + i;
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_CXC", "WHERE COREL='" + cor + "'");
					dbld.insert("D_CXCD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CXC SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

			sql = "SELECT ACTUAL, SERIE FROM P_CORREL_OTROS WHERE TIPO = 'D'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();
				ccorel = DT.getInt(0);
				serie = DT.getString(1);

				//dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND SERIE = '"+serie +"' AND TIPO = 'D' " +
				//		" AND ACTUAL =< "+ccorel+1);

				dbld.add("UPDATE P_CORREL_OTROS SET ACTUAL=" + ccorel + "  WHERE RUTA='" + fruta + "' AND TIPO = 'D' " +
						" AND ACTUAL < "+ccorel);
				//updCxC="UPDATE P_CORREL_OTROS SET ACTUAL="+ccorel+ "  WHERE RUTA='"+fruta+"' AND TIPO='D' " ;

				//ss = "exec actualizaCorelDev '" + gl.ruta + "'," + ccorel;
				//dbld.add(ss);

			}

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		//#CKFK 20190325 Sea el envío parcial o no se deben mostrar las facturas comunicadas
		//if (envioparcial) {
		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Notas devolución : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Notas devolución : " + pc + "\n";
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_DEPOS", "WHERE COREL='" + cor + "'");
					dbld.insert("D_DEPOSD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_DEPOS SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}else pc += 1;

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_MOV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVD", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDB", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDCAN", "WHERE COREL='" + cor + "'");
					dbld.insert("D_MOVDPALLET", "WHERE COREL='" + cor + "'");

					//#CKFK 20190412 Corregido error
					dbld.add("INSERT INTO P_DEVOLUCIONES_SAP " +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,'', D.LOTE, 'N', GETDATE(), D.CANT, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVD D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'" +
							" UNION" +
							" SELECT D.COREL, E.COREL, 0, E.RUTA, E.FECHA, D.PRODUCTO,D.BARRA, '', 'N', GETDATE(), 1, 'N'" +
							" FROM D_MOV E INNER JOIN D_MOVDB D ON E.COREL = D.COREL" +
							" WHERE E.COREL = '" + cor + "'");

					pc += 1;

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							pc += 1;

						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

					if (envioparcial) {
						dbld.clear();
					}

					dbld.insert("D_CLINUEVO", "WHERE CODIGO='" + cor + "'");
					if (gl.peModal.equalsIgnoreCase("APR")) {
						dbld.insert("D_CLINUEVO_APR", "WHERE CODIGO='" + cor + "'");
					}

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {

							sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
							db.execSQL(sql);
							if (gl.peModal.equalsIgnoreCase("APR")) {
								sql = "UPDATE D_CLINUEVO_APR SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							}

							pc += 1;
						} else {
							errflag = true;
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
		if (!esEnvioManual){
			wsStask.onProgressUpdate();
		}

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

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_ATENCION", "WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_ATENCION SET STATCOM='S' WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ";
							db.execSQL(sql);
						} else {
							errflag = true;
							fterr+="\n"+sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					//fterr+="\n"+e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
		if (!esEnvioManual){
			wsStask.onProgressUpdate();
		}

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

                    if (envioparcial && !esEnvioManual) dbld.clear();

					ss = "UPDATE P_CLIENTE SET COORX=" + px + ",COORY=" + py + " WHERE (CODIGO='" + cod + "')";
					dbld.add(ss);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CLICOORD SET STATCOM='S' WHERE (CODIGO='" + cod + "') AND (STAMP=" + stp + ") ";
							db.execSQL(sql);
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					fterr += "\n" + e.getMessage();
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

	}

	public void envioModifCliente() {
		Cursor DT;
		String ruta, ss, cadena;
		int stp;
		double px, py;
		long fechaM;
		fprog = " ";

		if (!esEnvioManual){
			wsStask.onProgressUpdate();
		}

		try {
			sql = "SELECT RUTA, MODIFICACION,FECHA FROM D_MODIFICACIONES WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			if (DT.getCount() == 0) return;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				ruta = DT.getString(0);
				cadena = DT.getString(1);
				fechaM = DT.getLong(2);

				try {

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.add(cadena);
					cadena = cadena.replace("'","''");
					ss = "INSERT INTO D_MODIFICACIONES VALUES('" + ruta + "','" + cadena + "','" + fechaM + "')";
					dbld.add(ss);

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_MODIFICACIONES SET STATCOM='S' WHERE (ruta='" + ruta + "') AND (fecha='" + fechaM + "') ";
							db.execSQL(sql);
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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
					if (!esEnvioManual){
						wsStask.onProgressUpdate();
					}

                    if (envioparcial && !esEnvioManual) dbld.clear();

					dbld.insert("D_SOLICINV", "WHERE COREL='" + cor + "'");
					dbld.insert("D_SOLICINVD", "WHERE COREL='" + cor + "'");

					if (envioparcial && !esEnvioManual) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_SOLICINV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							errflag = true;
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					errflag = true;
					addlog(new Object() {
					}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
					fterr += "\n" + e.getMessage();
				}

				DT.moveToNext();
			}

		} catch (Exception e) {
			errflag = true;
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

		int pc = 0, pcc=1;

		fprog = " ";
		if (!esEnvioManual){
			wsStask.onProgressUpdate();
		}

		try {

			pc = 0;

            if (envioparcial && !esEnvioManual) dbld.clear();

			ss = "UPDATE P_RUTA SET IDIMPRESORA='"+parImprID+"',NUMVERSION='"+gl.parNumVer +"',ARQUITECTURA='ANDR' WHERE CODIGO='" + gl.ruta + "'";
            dbld.add(ss);

            dbld.add("DELETE FROM D_REPFINDIA WHERE RUTA='" + gl.ruta + "'");
			dbld.insert("D_REPFINDIA", "WHERE (LINEA>=0)");

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 1) {
					pc = 1;
				} else {
					errflag = true;
					fterr += "\nFinDia : " + sstr;
					dbg = sstr;
				}
			}else pc = 0;

		} catch (Exception e) {
			errflag = true;
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			fstr = e.getMessage();
		}

		if (pc != pcc) {
			int pf = pcc - pc;
			senv += "Envío Fin día : " + pc + " , NO ENVIADO : " + pf + "\n";
		} else {
			senv += "Envío Fin día : " + pc + "\n";
		}
	}

	//#CKFK 20190522 Función creada para enviar los rating
    public void envioRating() {
        Cursor DT;
        String ruta, vendedor, comentario, fecha;
        int id, idtranserror;
        float rating;

        if (!esEnvioManual){
            wsStask.onProgressUpdate();
        }

        try {
            sql = " SELECT IDRATING, RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM " +
                  " FROM D_RATING WHERE STATCOM='N'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) return;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {

                id = DT.getInt(0);
                ruta = DT.getString(1);
                vendedor = DT.getString(2);
                rating = DT.getFloat(3);
                comentario = DT.getString(4);
                idtranserror=DT.getInt(5);
                fecha=DT.getString(6);

                try {

                    if (envioparcial && !esEnvioManual) dbld.clear();

                    ss = "INSERT INTO D_RATING (RUTA, VENDEDOR, RATING, COMENTARIO, IDTRANSERROR, FECHA, STATCOM)"+
                         " VALUES('" + ruta +"','" + vendedor +"'," + rating +",'" + comentario +"'," +
                         "" + idtranserror +",'" + fecha +"','N')";
                    dbld.add(ss);

                    if (envioparcial && !esEnvioManual) {
                        if (commitSQL() == 1) {
                            sql = "UPDATE D_RATING SET STATCOM='S' WHERE IDRATING='" + id + "'";
                            db.execSQL(sql);
                        }else{
                          fterr += "\n" + sstr;
                        }
                    }

                } catch (Exception e) {
					errflag = true;
                    addlog(new Object() {
                    }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    fterr += "\n" + e.getMessage();
                }

                DT.moveToNext();
            }

        } catch (Exception e) {
			errflag = true;
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            fstr = e.getMessage();
        }

    }

	public void updateInventario() {
		DU = new DateUtils();
		String sFecha;
		int rslt;
		long vfecha = clsAppM.fechaFactTol(du.getActDate());
		sFecha = DU.univfechasql(vfecha);
		String corel_d_mov = Get_Corel_D_Mov();

		try {

            if (envioparcial && !esEnvioManual) dbld.clear();

			ss = " UPDATE P_STOCK SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND (FECHA ='" + sFecha + "') AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' )";
			dbld.add(ss);

			ss = " UPDATE P_STOCKB SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "')";
			dbld.add(ss);

			ss = " UPDATE P_STOCK_PALLET SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + sFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + sFecha + "')";
			dbld.add(ss);

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 0) {
					fterr += "\n" + sstr;
					errflag = true;
				}
			}

		} catch (Exception e) {
			errflag = true;
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

            if (envioparcial && !esEnvioManual) dbld.clear();

			ss = "exec AcumuladoObjetivos '" + gl.ruta + "'," + oyear + "," + omonth;
			dbld.add(ss);

			if (envioparcial && !esEnvioManual) {
				if (commitSQL() == 0) {
					fterr += "\n" + sstr;
					errflag = true;
				}
			}

		} catch (Exception e) {
			errflag = true;
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
			fstr = "Tabla:" + TN + ", " + e.getMessage();
			idbg = idbg + " " + e.getMessage();
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), idbg, fstr);
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

		System.setProperty("line.separator","\r\n");

		try {
			sql = "SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA = '"+gl.ruta+"'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {

				DT.moveToFirst();

				while (!DT.isAfterLast()){

					codigo = DT.getString(0);

					String paht = (Environment.getExternalStorageDirectory() + "/RoadFachadas/" + codigo + ".jpg");
					File archivo = new File(paht);

					if(archivo.exists()){

						if (sendFoto(codigo, paht)==1) {

							String pathNew = (Environment.getExternalStorageDirectory() + "/RoadClientes/" + codigo + ".jpg");
							File archivoNew = new File(pathNew);

							if(!archivoNew.exists()) {
								archivo.renameTo(archivoNew);
							}

							archivo.delete();
						}
					}

					DT.moveToNext();
				}
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

		running=1;scon=0;
		fstr="Envio incompleto  ";
        errflag=false;

		try {

			if (getTest() == 1) {
				scon = 1;
			} else {
				URL = URL_Remota;
				if (getTest() == 1) {
					scon = 1;
				}
			}

			if (scon==1) {
				fstr="Sync OK";

				if (!sendData()) {
					fstr="Envio incompleto : "+sstr;
				}
			} else {
				fstr="No se puede conectar al web service : "+sstr;
			}
					
		} catch (Exception e) {
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
			Log.d("E", fstr + sstr);
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
				lblInfo.setText(fstr);
				isbusy = 0;
				barInfo.setVisibility(View.INVISIBLE);
				addlog("Envío", fterr + " " + fstr, esql);
				return;
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
				}

				fstr="Envio completo ";
				msgResultEnvio(senv);

			} else {
				lblInfo.setText(fterr);
                isbusy = 0;
                barInfo.setVisibility(View.INVISIBLE);
				mu.msgbox("Ocurrió error : \n" + fterr );
				addlog("Envío", fterr, esql);
				return;
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
			//if (!errflag) ComWS.super.finish();

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
			} catch (Exception e) {
				if (scon == 0) {
					fstr = "No se puede conectar al web service : " + sstr;
					//lblInfo.setText(fstr);
				}
			}

	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
			try {
				wsSendFinished();
				Looper.loop();
			}catch (Exception e) {
				Log.d("onPostExecute", e.getMessage());
			}
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
			dbld.add("UPDATE P_RUTA SET IDIMPRESORA='"+parImprID+"',NUMVERSION='"+gl.parNumVer +"',ARQUITECTURA='ANDR', FECHAVERSION='"+gl.parFechaVer +"' WHERE CODIGO='" + gl.ruta + "'");
			dbld.add("INSERT INTO P_BITACORA_VERSIONHH (RUTA,FECHA,NUMVERSION,ARQUITECTURA) " +
					"VALUES('"+gl.ruta+"','"+ du.univfechaseg() +"','"+gl.parNumVer +"','ANDR')");

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

	private void resizeButtons(){
		try {

			Display screensize = getWindowManager().getDefaultDisplay();

			Point size = new Point();
			screensize.getSize(size);

			int width = (int)(size.x/5);

			ViewGroup.LayoutParams layoutParams = imgEnv.getLayoutParams();
			layoutParams.width = width;
			layoutParams.height = width;
			imgEnv.setLayoutParams(layoutParams);

			ViewGroup.LayoutParams layoutParams1 = imgEnvM.getLayoutParams();
			layoutParams1.width = width;
			layoutParams1.height = width;
			imgEnvM.setLayoutParams(layoutParams1);

			ViewGroup.LayoutParams layoutParams2 = imgRec.getLayoutParams();
			layoutParams2.width = width;
			layoutParams2.height = width;
			imgRec.setLayoutParams(layoutParams2);

			ViewGroup.LayoutParams layoutParams3 = relPrecio.getLayoutParams();
			layoutParams3.width = width ;
			layoutParams3.height = width+30;
			relPrecio.setLayoutParams(layoutParams3);

			ViewGroup.LayoutParams layoutParams4 = relExist.getLayoutParams();
			layoutParams4.width = width;
			layoutParams4.height = width+30;
			relExist.setLayoutParams(layoutParams4);

			ViewGroup.LayoutParams layoutParams5 = relStock.getLayoutParams();
			layoutParams5.width = width;
			layoutParams5.height = width+30;
			relStock.setLayoutParams(layoutParams5);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

		if (!gl.debug) {
			txtRuta.setText(ruta);
			txtEmp.setText(gEmpresa);
		}

		try {

			sql = "SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='" + ruta + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();

				URL = DT.getString(0);
				URL_Remota = DT.getString(1);

				gl.URLtemp = URL;

				if (!URL.isEmpty()) {
					txtWS.setText(URL);
				} else if (!URL_Remota.isEmpty()) {
					txtWS.setText(URL);
				} else {
					toast("No hay configurada URL para transferencia de datos");
				}

			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			URL = "*";
			txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");

		}

	}

//
//	public void getWSURL() {
//		Cursor DT;
//		String wsurl;
//
//		if(!gl.debug){
//			txtRuta.setText(ruta);
//			txtEmp.setText(gEmpresa);
//		}
//
//		try {
//
//			sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
//			DT=Con.OpenDT(sql);
//
//			if (DT.getCount()>0){
//				DT.moveToFirst();
//
//				if (gl.isOnWifi==1) {
//					URL = DT.getString(0);
//				}else if(gl.isOnWifi==2){
//					URL = DT.getString(1);
//				}
//
//				//URL=wsurl;
//
//				if (URL!=null && !URL.equalsIgnoreCase("")){
//					txtWS.setText(URL);
//				}else{
//					toast("No hay configurada ruta para transferencia de datos");
//				}
//
//			}
//
//		} catch (Exception e) {
//			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
//			//MU.msgbox(e.getMessage());
//			//URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
//			URL="*";txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");
//			//URL="*";txtWS.setText("http://192.168.1.142/wsimagen/baktun1.asmx");
//			//txtWS.setText("");
//
//		}
//
//	}
//
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
				if (gl.modoadmin || gl.debug) {

					txtRuta.setEnabled(true);
					txtWS.setEnabled(true);
					txtEmp.setEnabled(true);

					if (esvacio) {
						lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
                        lblEnvM.setVisibility(View.INVISIBLE);imgEnvM.setVisibility(View.INVISIBLE);
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

                //Invisible botón y texto de envío manual
                lblEnvM.setVisibility(View.INVISIBLE);
                imgEnvM.setVisibility(View.INVISIBLE);

				//Invisible botón y texto de recepción
				lblRec.setVisibility(View.INVISIBLE);
				imgRec.setVisibility(View.INVISIBLE);

				//Tiene documentos
				boolean TieneFact,TienePedidos,TieneCobros,TieneDevol,YaComunico, TieneInventario, TieneOtros, TieneNCPP;

                if (!envioparcial){
                    TieneFact = (clsAppM.getDocCountTipo("Facturas",false)>0?true:false);
                    TienePedidos = (clsAppM.getDocCountTipo("Pedidos",false)>0?true:false);
                    TieneCobros = (clsAppM.getDocCountTipo("Cobros",false)>0?true:false);
                    TieneDevol = (clsAppM.getDocCountTipo("Devoluciones",false)>0?true:false);
                    YaComunico=(claseFindia.getComunicacion() == 2?true:false);
					TieneNCPP=(clsAppM.getDocCountTipo("NotaCredPP",false)>0?true:false);
					TieneInventario=(clsAppM.getDocCountTipo("Inventario",false)>0?true:false);
                }else{
                    TieneFact = (clsAppM.getDocCountTipo("Facturas",true)>0?true:false);
                    TienePedidos = (clsAppM.getDocCountTipo("Pedidos",true)>0?true:false);
                    TieneCobros = (clsAppM.getDocCountTipo("Cobros",true)>0?true:false);
                    TieneDevol = (clsAppM.getDocCountTipo("Devoluciones",true)>0?true:false);
                    YaComunico=(claseFindia.getComunicacion() == 2?true:false);
					TieneNCPP=(clsAppM.getDocCountTipo("NotaCredPP",false)>0?true:false);
                    TieneInventario=(clsAppM.getDocCountTipo("Inventario",true)>0?true:false);
                }

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
                                            lblEnvM.setVisibility(View.INVISIBLE);imgEnvM.setVisibility(View.INVISIBLE);
											if (StringUtils.equals(GetStatusRec(),"1"))
												{
													relExist.setVisibility(gl.peBotInv && !TieneFact?View.VISIBLE:View.INVISIBLE);
													relStock.setVisibility(gl.peBotStock && TieneFact?View.VISIBLE:View.INVISIBLE);
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
                                            (TieneFact || TieneCobros || TieneDevol || TienePedidos || TieneNCPP)) ||
                                            (!rutatipo.equalsIgnoreCase("V")))
                                        {
                                            lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
                                            lblEnv.setVisibility(View.VISIBLE); imgEnv.setVisibility(View.VISIBLE);
                                            lblEnvM.setVisibility(View.VISIBLE);imgEnvM.setVisibility(View.VISIBLE);
                                            relPrecio.setVisibility(gl.peBotPrec?View.VISIBLE:View.INVISIBLE);
                                            relExist.setVisibility(View.INVISIBLE);
                                            relStock.setVisibility(gl.peBotStock?View.VISIBLE:View.INVISIBLE);
                                        }
                                }
                            }
                        else
                            {
                                if ((!YaComunico) &&  !(TieneFact || TienePedidos) && !TieneCobros && !TieneDevol && !TieneNCPP)
                                    {
                                        lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
                                        lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
                                        lblEnvM.setVisibility(View.INVISIBLE);imgEnvM.setVisibility(View.INVISIBLE);
										if (StringUtils.equals(GetStatusRec(),"1"))
											{
												relExist.setVisibility(gl.peBotInv && !TieneFact?View.VISIBLE:View.INVISIBLE);
												relStock.setVisibility(gl.peBotStock && TieneFact?View.VISIBLE:View.INVISIBLE);
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
                                        if (YaComunico &&  !(TieneFact || TienePedidos) && !TieneCobros && !TieneDevol && !TieneNCPP)
                                            {
                                                lblRec.setVisibility(View.VISIBLE);	imgRec.setVisibility(View.VISIBLE);
                                                lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
                                                lblEnvM.setVisibility(View.INVISIBLE);imgEnvM.setVisibility(View.INVISIBLE);

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
                                                lblEnvM.setVisibility(View.VISIBLE);imgEnvM.setVisibility(View.VISIBLE);
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
                                lblEnvM.setVisibility(View.INVISIBLE);imgEnvM.setVisibility(View.INVISIBLE);

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
                                && (TieneFact || TieneCobros || TienePedidos) || TieneDevol || TieneNCPP)) ||((!rutatipo.equalsIgnoreCase("V"))
                                        && (!rutatipo.equalsIgnoreCase("D"))))
                                    {
                                        lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
										lblEnv.setVisibility(View.VISIBLE);imgEnv.setVisibility(View.VISIBLE);
                                        lblEnvM.setVisibility(View.VISIBLE);imgEnvM.setVisibility(View.VISIBLE);
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
	
	private void otrosParametros() {
		try {
			AppMethods app=new AppMethods(this,gl,Con,db);
			app.parametrosExtra();
			app.parametrosGlobales();
			app.parametrosBarras();
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

	private void restartApp(){
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

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which)
				{
					startActivity(new Intent(ComWS.this,rating.class));
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
						restartApp();
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

	private void msgAskSinLicencia(){

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Licencia");
			dialog.setMessage("El dispositivo no tiene licencia válida");

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					restartApp();
				}
			});

			dialog.show();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	public void SetStatusRecTo(String estado) 	{
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

    public void SetStatusRecToTrans(String estado)    {
        try         {
            sql = "UPDATE P_RUTA SET PARAM2='" + StringUtils.trim(estado) + "'";
            dbT.execSQL(sql);
        } catch (Exception ex)  {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+ " " + ex.getMessage());
        }
    }

	public String GetStatusRec() 	{
		Cursor DT;
		String vGetStatusRec = "";

		try 			{
				sql = "SELECT PARAM2 FROM P_RUTA ";
				DT = Con.OpenDT(sql);

				if (DT.getCount()> 0) 				{
					DT.moveToFirst();
					vGetStatusRec = DT.getString(0);
				}
			}

		catch (Exception ex) {
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
				} else {
					super.onBackPressed();
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	/*@Override
	protected void onResume() {
		super.onResume();
		try {
			this.wakeLock.acquire();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
	}

	@Override
	protected void onPause() {
		try {
			this.wakeLock.release();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"wakelock");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		this.wakelock.release();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		this.wakelock.release();
	}
	*/

	//endregion

}
