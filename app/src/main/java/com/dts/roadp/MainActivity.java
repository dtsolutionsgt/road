package com.dts.roadp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;

public class MainActivity extends PBase {

	private EditText txtUser,txtPass;
	private TextView lblRuta,lblRTit;
	private ImageView imgLogo;
		
	private clsLicence lic;
	private BaseDatosVersion dbVers;
	
	private boolean rutapos,scanning=false;
	private int fecha;
	private String cs1,cs2,cs3;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            grantPermissions();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
	}

	// Grant permissions

	private void startApplication() {

        try {
            super.InitBase();

            this.setTitle("ROAD");

            txtUser= (EditText) findViewById(R.id.txtUser);
            txtPass= (EditText) findViewById(R.id.txtMonto);
            lblRuta= (TextView) findViewById(R.id.lblCDisp);
            lblRTit= (TextView) findViewById(R.id.lblCUsed);
            imgLogo= (ImageView) findViewById(R.id.imgNext);

            // DB VERSION
            dbVers=new BaseDatosVersion(this,db,Con);
            dbVers.checkVersion(1);

            setHandlers();

            txtUser.requestFocus();

            initSession();

            //#HS_20181206 Obtiene el supervisor de la ruta
            supervisorRuta();

            txtUser.setText("00100993");txtPass.setText("2613");

            gl.contlic=false;
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}

	private void grantPermissions() {
		try {
			if (Build.VERSION.SDK_INT >= 23) {

				if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
			        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
					&& checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED
					&& checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
				{
					startApplication();
				} else {
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
									     Manifest.permission.ACCESS_FINE_LOCATION,
									     Manifest.permission.CALL_PHONE,
									     Manifest.permission.CAMERA}, 1);
				}
			}

	    } catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		Toast.makeText(this,"req : "+requestCode, Toast.LENGTH_SHORT).show();

		//switch (requestCode) {

			//case 0:
				if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
						&& checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
						&& checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED
						&& checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, "GRANTED : " + requestCode, Toast.LENGTH_SHORT).show();
					startApplication();
				} else {
					Toast.makeText(this, "failed " + requestCode, Toast.LENGTH_SHORT).show();
					super.finish();
				}
				//break;
		//}
	}


	// Events
	
	public void showMenu(View view) {
		
		/*
		gl.vendnom="DTS Test";
		gl.vend="1";
		gl.vnivel=1;
		gl.vnivprec=1;
		
		gotoMenu();
		*/
	}
	
	public void comMan(View view) {
		entraComunicacion();			
	}
	
	public void gotoMenu(){
		
		txtUser.setText("");txtPass.setText("");txtUser.requestFocus();
		
		Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
}

	public void doLogin(View view) {
		processLogIn();
	}
	
	public void doRegister(View view) {
	    startActivity(new Intent(this, LicRegis.class));		
	}

	private void setHandlers() {

		txtUser.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					switch (arg1) {
						case KeyEvent.KEYCODE_ENTER:
							//toast("key ");
							txtPass.requestFocus();
							return true;
					}
				}
				return false;
			}
		});

		txtPass.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					switch (arg1) {
						case KeyEvent.KEYCODE_ENTER:
							processLogIn();
							return true;
					}
				}
				return false;
			}
		});

		/*
		txtUser.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

			public void onTextChanged(CharSequence s, int start,int before, int count) {
				//mu.msgbox("start "+start+" before "+before+" count "+count);

				final CharSequence ss=s;

				if (!scanning) {
					scanning=true;
					Handler handlerTimer = new Handler();
					handlerTimer.postDelayed(new Runnable(){
						public void run() {
							compareSC(ss);
						}}, 50);
				}


			}
		});
		*/
	}

	private void compareSC(CharSequence s) {
		String os,bc;

		bc=txtUser.getText().toString();
		if (mu.emptystr(bc) || bc.length()<2) {
			txtUser.setText("");
			scanning=false;
			return;
		}
		os=s.toString();

		if (bc.equalsIgnoreCase(os)) {
			//Toast.makeText(this,"Codigo barra : " +bc, Toast.LENGTH_SHORT).show();
			msgbox("Barra: "+bc);
		}

		txtUser.setText("");
		scanning=false;
	}


	// Main

	private void initSession() {
		Cursor DT;
		String s, rn = "";

		try {
			//#HS_20181122_1505 Se agrego el campo Impresion.
			sql = "SELECT CODIGO,NOMBRE,VENDEDOR,VENTA,WLFOLD,IMPRESION,SUCURSAL FROM P_RUTA";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			gl.ruta = DT.getString(0);
			gl.vend = DT.getString(2);
			gl.rutatipog = DT.getString(3);
			s = DT.getString(3);
			gl.wsURL = DT.getString(4);
			gl.impresora = DT.getString(5);
			gl.sucur = DT.getString(6);

			rutapos = s.equalsIgnoreCase("R");

		} catch (Exception e) {
			gl.ruta = "";
			gl.vend = "0";
			gl.rutatipog = "V";
			gl.wsURL = "http://192.168.1.1/wsAndr/wsAndr.asmx";
		}

		if (rutapos) {
			lblRTit.setText("POS No. " + gl.ruta);
			imgLogo.setImageResource(R.drawable.retail_logo);
		} else {
			lblRTit.setText("Ruta No. " + gl.ruta);

		}

		try {
			//#HS_20181120_1616 Se agrego el campo UNIDAD_MEDIDA_PESO.//campo INCIDENCIA_NO_LECTURA
			sql = "SELECT EMPRESA,NOMBRE,DEVOLUCION_MERCANCIA,USARPESO,FIN_DIA,DEPOSITO_PARCIAL,UNIDAD_MEDIDA_PESO,INCIDENCIA_NO_LECTURA FROM P_EMPRESA";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			gl.emp = DT.getString(0);
			lblRuta.setText(DT.getString(1));
			gl.devol = DT.getInt(2) == 1;
			s = DT.getString(3);
			gl.usarpeso = s.equalsIgnoreCase("S");
			gl.banderafindia=DT.getInt(4) == 1;
			gl.umpeso = DT.getString(6);
			gl.incNoLectura = DT.getInt(7)==1; //#HS_20181211 Agregue campo incNoLectura para validacion en cliente.
			gl.depparc = DT.getInt(5)==1;
		} catch (Exception e) {
			gl.emp = "0";
			lblRuta.setText("");
			gl.devol = false;
		}

		gl.vendnom = "Vendedor 1";

		try {
			File directory = new File(Environment.getExternalStorageDirectory() + "/SyncFold");
			directory.mkdirs();
		} catch (Exception e) {
		}

		try {
			File directory = new File(Environment.getExternalStorageDirectory() + "/RoadFotos");
			directory.mkdirs();
		} catch (Exception e) {
		}


		try {
			AppMethods app = new AppMethods(this, gl, Con, db);
			app.parametrosExtra();
		} catch (Exception e) {
			msgbox(e.getMessage());
		}

	}

	private void processLogIn() {
		if (gl.contlic) {
			if (!validaLicencia()) {
				mu.msgbox("¡Licencia invalida!");
				return;
			}
		}

		if (checkUser()) gotoMenu();
	}

	private boolean checkUser() {
		Cursor DT;
		String usr, pwd, dpwd;

		usr = txtUser.getText().toString().trim();
		pwd = txtPass.getText().toString().trim();

		if (mu.emptystr(usr)) {
			mu.msgbox("Usuario incorrecto.");
			return false;
		}
		if (mu.emptystr(pwd)) {
			mu.msgbox("Contraseña incorrecta.");
			return false;
		}

		if (usr.equalsIgnoreCase("DTS") && pwd.equalsIgnoreCase("DTS")) {

			gl.vendnom = "DTS";
			gl.vend = "DTS";
			gl.vnivel = 1;
			gl.vnivprec = 1;

			return true;
		}

		if (usr.equalsIgnoreCase("Venta") && pwd.equalsIgnoreCase("Venta")) {
			showDemoMenu();
			return false;
		}

		try {

			sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + usr + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() == 0) {
				mu.msgbox("Usuario incorrecto !");
				return false;
			}

			DT.moveToFirst();
			dpwd = DT.getString(1);
			if (!pwd.equalsIgnoreCase(dpwd)) {
				mu.msgbox("Contraseña incorrecta !");
				return false;
			}

			gl.vendnom = DT.getString(0);
			gl.vend = usr;
			gl.vnivel = DT.getInt(2);
			gl.vnivprec = DT.getInt(3);

			return true;

		} catch (Exception e) {
			mu.msgbox(e.getMessage());
			return false;
		}

	}

	public void supervisorRuta(){
		Cursor DT;

		try{

			sql = "SELECT CODIGO FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 2";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			if(DT.getCount()>0) {
				gl.codSupervisor = DT.getString(0);
			}

		}catch(Exception e){
			Log.d("supervisorRuta error: ", e.getMessage());
		}

	}

	// Licencia

	private boolean validaLicencia() {
		Cursor dt;
		String mac, lickey, idkey, binkey;
		int fval, ff, lkey;

		try {
			mac = lic.getMac();
			lkey = lic.getLicKey(mac);
			lickey = lic.encodeLicence(lkey);

			sql = "SELECT IDKEY,BINKEY FROM LIC_CLIENTE WHERE ID='" + mac + "'";
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
			mu.msgbox(e.getMessage());return false;
		}
	
	}


	
	// Ventas Demo
	
	public void showDemoMenu() {
		final AlertDialog Dialog;
		final String[] selitems = {"Datos de cliente","Base de datos original","Borrar datos de venta"};
	 		    
		AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
		menudlg.setTitle("Datos de demo");
					
		menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				switch (item) {
				case 0: 
					Intent intent = new Intent(MainActivity.this,DemoData.class);
					startActivity(intent);
					break;
				case 1:  
					copyRawFile();break;
				case 2:
					borrarDatos(1);break;
				}

				dialog.cancel();	
			}
		});
				
		menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
		@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
			
		Dialog = menudlg.create();
		Dialog.show();
	}	
	
	private void copyRawFile() {
		String fn;
		int rid,rslt;

		Field[] fields = R.raw.class.getFields();
		for(Field f : fields)
			try {
				fn=f.getName();
				if (fn.equalsIgnoreCase("rd_param")) {
					rid=f.getInt(null);	
					rslt=copyRawFile(rid);
					if (rslt==1) {
						Intent intent = new Intent(this,ComDrop.class);
						startActivity(intent);	
					}
				}
			} catch (Exception e) { }
	}
	
	private int copyRawFile(int rawid) {

		try {
			InputStream in = getResources().openRawResource(rawid);
			File file = new File(Environment.getExternalStorageDirectory(), "/SyncFold/rd_param.txt");
			FileOutputStream out = new FileOutputStream(file);

			byte[] buff = new byte[1024];
			int read = 0;

			try {
				while ((read = in.read(buff)) > 0) {
					out.write(buff, 0, read);
				}
			} finally {
				in.close();out.close();
			}
			return 1;
		} catch (Exception e) {
			mu.msgbox("Error : "+e.getMessage());return 0;
		}	
	}
	
	private void borrarDatos(int showmsg) {
		String sql;
		
		try {
			
			db.beginTransaction();
				
			sql="DELETE FROM D_FACTURA";db.execSQL(sql);
			sql="DELETE FROM D_FACTURAD";db.execSQL(sql);
			sql="DELETE FROM D_FACTURAP";db.execSQL(sql);
			sql="DELETE FROM D_FACTURAD_LOTES";db.execSQL(sql);
			
			sql="DELETE FROM D_PEDIDO";db.execSQL(sql);
			sql="DELETE FROM D_PEDIDOD";db.execSQL(sql);
			
			sql="DELETE FROM D_BONIF";db.execSQL(sql);
			sql="DELETE FROM D_BONIF_LOTES";db.execSQL(sql);
			sql="DELETE FROM D_REL_PROD_BON";db.execSQL(sql);
			sql="DELETE FROM D_BONIFFALT";db.execSQL(sql);
			
			sql="DELETE FROM D_DEPOS";db.execSQL(sql);
			sql="DELETE FROM D_DEPOSD";db.execSQL(sql);
	
			sql="DELETE FROM D_MOV";db.execSQL(sql);
			sql="DELETE FROM D_MOVD";db.execSQL(sql);

			sql="DELETE FROM D_ATENCION";db.execSQL(sql);
		
			
			db.setTransactionSuccessful();			
			db.endTransaction();
					
			if (showmsg==1) Toast.makeText(this,"Datos de venta borrados", Toast.LENGTH_SHORT).show();
			
		} catch (SQLException e) {
			db.endTransaction();
			mu.msgbox("Error : " + e.getMessage());
		}		
		
	}
		
	
	// Aux
	
	private void entraComunicacion() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Contraseña de administrador");//	alert.setMessage("Serial");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		input.setText("");
		input.requestFocus();
		
		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String s;
				
				try {
			    	s=input.getText().toString();
			    			    	
			    	if (s.equalsIgnoreCase("1965")) {
			    		gl.modoadmin=true;
			    		gl.autocom=0;
						startActivity( new Intent(MainActivity.this,ComWS.class));
			    	} else {	
			    		mu.msgbox("Contraseña incorrecta");return;
			    	}
			    	
				} catch (Exception e) {
					
			    }
		  	}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		alert.show();
	}

	// Activity Events
	
	protected void onResume() {
	    super.onResume();
	    initSession();
		txtUser.requestFocus();
	}

}



