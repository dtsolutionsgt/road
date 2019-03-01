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
import android.provider.Settings;
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
	private String cs1,cs2,cs3,barcode;


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
/*
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
									Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.CALL_PHONE,
									Manifest.permission.CAMERA}, 1);
*/
				}
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}

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

            supervisorRuta();
            txtUser.setText("00100993");txtPass.setText("2613");

            gl.contlic=false;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
	    try{
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
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}

	//region Events

	public void comMan(View view) {
	    try{
            entraComunicacion();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}
	
	public void gotoMenu(){
		try{
            txtUser.setText("");txtPass.setText("");txtUser.requestFocus();

            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

	public void doLogin(View view) {
	    try{
            processLogIn();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}
	
	public void doRegister(View view) {
	    try{
            startActivity(new Intent(this, LicRegis.class));
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

	}

	private void setHandlers() {

	    try{
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
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


	}

	private void compareSC(CharSequence s) {
	    try{
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
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

	}

	//endregion

	//region Main

	private void initSession() {
		Cursor DT;
		String s, rn = "";
		String vCellCom = "";

		if (dbVacia()) {
			gl.modoadmin=true;
			gl.autocom=0;
			toastcent("¡La base de datos está vacia!");
			startActivity( new Intent(MainActivity.this,ComWS.class));
		}


		try {
			//#HS_20181122_1505 Se agrego el campo Impresion.
			sql = "SELECT CODIGO,NOMBRE,VENDEDOR,VENTA,WLFOLD,IMPRESION,SUCURSAL,CELULAR FROM P_RUTA";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			gl.ruta = DT.getString(0);
			gl.vend = DT.getString(2);
			gl.rutatipog = DT.getString(3);
			s = DT.getString(3);
			gl.wsURL = DT.getString(4);
			gl.impresora = DT.getString(5);
			gl.sucur = DT.getString(6);

			if (!mu.emptystr(DT.getString(7))){
				vCellCom = DT.getString(7);
			}
			gl.CellCom = (vCellCom.equalsIgnoreCase("S"));

			rutapos = s.equalsIgnoreCase("R");

		} catch (Exception e) {
			gl.ruta = "";
			gl.vend = "0";
			gl.rutatipog = "V";
			gl.wsURL = "http://192.168.1.1/wsAndr/wsAndr.asmx";
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		if (rutapos) {
			lblRTit.setText("POS No. " + gl.ruta);
			imgLogo.setImageResource(R.drawable.retail_logo);
		} else {
			lblRTit.setText("Ruta No. " + gl.ruta);

		}

		try {
			//#HS_20181120_1616 Se agrego el campo UNIDAD_MEDIDA_PESO.//campo INCIDENCIA_NO_LECTURA
			sql = " SELECT EMPRESA,NOMBRE,DEVOLUCION_MERCANCIA,USARPESO,FIN_DIA,DEPOSITO_PARCIAL,UNIDAD_MEDIDA_PESO," +
				  " INCIDENCIA_NO_LECTURA FROM P_EMPRESA";
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

            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		gl.vendnom = "Vendedor 1";

		try {
			File directory = new File(Environment.getExternalStorageDirectory() + "/SyncFold");
			directory.mkdirs();
		} catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		try {
			File directory = new File(Environment.getExternalStorageDirectory() + "/RoadFotos");
			directory.mkdirs();
		} catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


		//Id de Dispositivo
		gl.deviceId = androidid();

		try {
			AppMethods app = new AppMethods(this, gl, Con, db);
			app.parametrosExtra();
		} catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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

		try {

		if (fecha>1903310000) {
			msgAskLic("¡Su licencia expiró!");return false;
		}

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
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
                mu.msgbox(e.getMessage());
                return false;
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			Log.d("supervisorRuta error: ", e.getMessage());
		}

	}

	//endregion

	//region Ventas Demo
	
	public void showDemoMenu() {

	    try{
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
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

	}	
	
	private void copyRawFile() {
		String fn;
		int rid, rslt;
        try{
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
                }catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("Error : "+e.getMessage());return 0;
		}	
	}
	
	private void borrarDatos(int showmsg) {

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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			db.endTransaction();
			mu.msgbox("Error : " + e.getMessage());
		}		
		
	}

	//endregion
	
	//region Aux
	
	private void entraComunicacion() {

	    try{
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
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    }
                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }catch (Exception  e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


	}

	private boolean dbVacia() {
		Cursor dt;

		try {
			sql = "SELECT CODIGO FROM P_RUTA";
			dt = Con.OpenDT(sql);

			return dt.getCount()==0;
		} catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return true;
		}
	}

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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());return false;
		}

	}

	private void msgAskLic(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  );
		dialog.setIcon(R.drawable.ic_quest);

		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		dialog.show();

	}

	private String androidid() {
		String uniqueID="";
		try {
			uniqueID = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			uniqueID="0000000000";
		}

		return uniqueID;
	}
	//endregion

	//region Activity Events
	
	protected void onResume() {
	    try{
            super.onResume();
            initSession();
            txtUser.requestFocus();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
	}

	//endregion

}



