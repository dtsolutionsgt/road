package com.dts.roadp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.commons.io. IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.JsonToken;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ZoomControls;

import static android.app.Activity.RESULT_OK;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class CliDet extends PBase {

	private TextView lblNom,lblRep,lblDir,lblAten,lblTel,lblGPS;
	private TextView lblCLim,lblCUsed,lblCDisp,lblCobro,lblDevol;
	private RelativeLayout relV,relP,relD,relCamara;//#HS_20181213 relCamara
	private ImageView imgCobro,imgDevol;
	private Exist Existencia = new Exist();
	private String cod,tel, Nombre, NIT;
	//#HS_20181220 Variables para fachada;
	private String imagenbase64,path;
	private Boolean imgPath, imgDB;
	////
	private double clim,cused,cdisp;
	private int nivel,browse,merc;
	private boolean porcentaje = false;
	private byte[] imagenBit;
	
	private double gpx,gpy;
	
	// Waze
	// 14.6017278,-90.5236343,15
	// center map to Ayalon and set zoom to 10     waze://?ll=37.44469,-122.15971&z=10
	// search for address:                         waze://?q=San%20Jose%20California
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_det);
		
		super.InitBase();
		
		lblNom= (TextView) findViewById(R.id.lblNom);
		lblRep= (TextView) findViewById(R.id.lblPres);
		lblDir= (TextView) findViewById(R.id.lblDir);
		lblAten= (TextView) findViewById(R.id.lblCant);
		lblTel= (TextView) findViewById(R.id.lblTel);
		lblGPS= (TextView) findViewById(R.id.textView2);
		lblCobro= (TextView) findViewById(R.id.textView6);
		lblDevol= (TextView) findViewById(R.id.textView3);		 
		lblCLim= (TextView) findViewById(R.id.lblCLim);
		lblCUsed= (TextView) findViewById(R.id.lblCUsed);
		lblCDisp= (TextView) findViewById(R.id.lblCDisp);
		
		relV= (RelativeLayout) findViewById(R.id.relVenta);
		relP= (RelativeLayout) findViewById(R.id.relPreventa);
		relD= (RelativeLayout) findViewById(R.id.relDespacho);
		relCamara = (RelativeLayout) findViewById(R.id.relCamara);
		
		imgCobro= (ImageView) findViewById(R.id.imageView2);
		imgDevol= (ImageView) findViewById(R.id.imageView1);
		
		/*
		Con = new BaseDatos(this);
	    opendb();
	    ins=Con.Ins;upd=Con.Upd;

		vApp=this.getApplication();
		mu=new MiscUtils(this);
		du=new DateUtils();
		
		keyboard = (InputMethodManager)getSystemService(this.INPUT_METHOf_SERVICE);
		*/
		cod=gl.cliente;
		
		if (!gl.devol) {
			lblDevol.setVisibility(View.INVISIBLE);
			imgDevol.setVisibility(View.INVISIBLE);
		}	
		
		showData();
		calcCredit();
		
		browse=0;
		merc=1;
		
		habilitaOpciones();
		
		defineGeoPos();

		//Toast.makeText(this, "Create activity : ", Toast.LENGTH_SHORT).show();
	}


	// Events
	
	public void showVenta(View view) {
		//Float cantidad;
		//gl.rutatipo="V";

		if (!validaVenta()) return;

		if(porcentaje == false) {
			VerificaCantidad();
		}

		//#HS_20181129_1033 lo agregue en funcion VerificaCantidad.
		//Asigna conexión actual a la forma de Existencias.
		/*Existencia.Con = Con;
		cantidad = Float.valueOf(Existencia.CantExistencias());
		if(cantidad == 0){
			mu.msgbox("No hay existencias disponibles.");
		}else{
			runVenta();
		}*/

	}	

	public void VerificaCantidad(){
		Float cantidad;
		gl.rutatipo="V";
		//Asigna conexión actual a la forma de Existencias.
		Existencia.Con = Con;
		cantidad = Float.valueOf(Existencia.CantExistencias());
		if(cantidad == 0){
			mu.msgbox("No hay existencias disponibles.");
		}else{
			runVenta();
		}
	}

	public void showPreventa(View view) {
		gl.rutatipo="P";
		runVenta();
	}	
	
	public void showDespacho(View view) {
		mu.msgbox("La funcionalidad no esta implementada.");
		//gl.rutatipo="D";
		//runVenta();
	}	
	
	private void runVenta() {
		
		if (merc==1) {
			browse=1;
			Intent intent = new Intent(this,MercLista.class);
			startActivity(intent);	
		} else {	
			initVenta();
		}	
	}
	
	public void showDir(View view) {
		//mu.msgbox(lblDir.getText().toString() + "\n" + lblRep.getText().toString());
		msgAskEditCliente();
	}

	private void msgAskEditCliente() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Road");
		dialog.setMessage("¿Quiere editar datos del cliente?");

		dialog.setIcon(R.drawable.ic_quest);

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				inputCliente();
			}
		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				closekeyb();
			}
		});

		dialog.show();

	}

	//#HS_20181207 Cuadro de dialogo para editar datos del cliente
	private void inputCliente() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Editar Cliente");

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final TextView lblNombre = new TextView(this);
		lblNombre.setTextSize(10);
		lblNombre.setText("Nombre:");

		final TextView lblNit = new TextView(this);
		lblNit.setTextSize(10);
		lblNit.setText("NIT:");

		final EditText editNombre = new EditText(this);
		editNombre.setInputType(InputType.TYPE_CLASS_TEXT);
		editNombre.setText(lblDir.getText().toString());

		final EditText editNit = new EditText(this);
		editNit.setInputType(InputType.TYPE_CLASS_TEXT);
		editNit.setText(lblRep.getText().toString());

		layout.addView(lblNombre);
		layout.addView(editNombre);
		layout.addView(lblNit);
		layout.addView(editNit);

		alert.setView(layout);

		alert.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String corel=mu.getCorelBase();
				gl.fnombre = editNombre.getText().toString();
				gl.fnit = editNit.getText().toString();
				ActualizarCliente(corel,gl.fnombre, gl.fnit);
			}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}

	//#HS_20181211 agregue funcion para actualizar los campos de Nombre y NIT del cliente

	private void ActualizarCliente(String corel, String NombreEdit, String NitEdit){
		Cursor DT;
		try {
			db.execSQL("INSERT INTO D_FACTURAF(COREL, NOMBRE, NIT, DIRECCION) VALUES('"+corel+"','"+NombreEdit+"','"+NitEdit+"','')");
			mu.msgbox("Registro actualizado");
		}catch (Exception e){
			mu.msgbox("ActualizarCliente: "+e.getMessage());
		}
	}

	/////

	public void setGPS(View view) {
		browse=2;
		startActivity(new Intent(this,CliGPS.class));	
	}
	
	public void showCredit(View viev){
		Intent intent = new Intent(this,Cobro.class);
		startActivity(intent);	
	}
	
	public void showDevol(View view){
		msgAskTipoDev("Devolucion de producto en estado ... ");
	}
	
	public void sendSMS(View view){
		String to=tel;
		
		//to="42161467";
		
		if (to.length()==0) {
			msgbox("Numero incorrecto ");return;
		}
		
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+to)));
		} catch (Exception e) {
			msgbox("No pudo enviar mensaje : "+e.getMessage());
		} 
		
		//try {
		//    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
	   //     i.setType("vnd.android-dir/mms-sms");
       //     startActivity(i);
		//} catch (Exception e) {
       //     mu.msgbox("E","No se puede enviar mensaje");
		//}
	}
	
	public void callPhone(View view){
		String to=tel;
		
		//to="42161467";
		
		if (to.length()==0) {
			msgbox("Numero incorrecto ");return;
		}
		
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+to));
			startActivity(callIntent);
		} catch (Exception e) {
			msgbox("No pudo llamar : "+e.getMessage());
		} 
			  
	}
	
	public void callWaze(View view) {
		
		/*
		try {
			Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.example.wazetest");
			this.startActivity(intent);
		} catch ( Exception ex  )	{
		}
		*/
		
		try {
			String url = "waze://?ll=14.6017278,-90.5236343";
			Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
			startActivity( intent );
		} catch ( ActivityNotFoundException ex  )	{
			Intent intent =
			new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
			startActivity(intent);
		}
		
	}
	
	
	// Main
	
	private void showData() {
		Cursor DT;
		int uvis;
		String contr,sgps="0.00000000 , 0.00000000";
		
		lblNom.setText("");lblRep.setText("");
		lblDir.setText("");lblAten.setText("");lblTel.setText("");
		tel="";
		
		try {
			sql="SELECT NOMBRE,NOMBRE_PROPIETARIO,DIRECCION,ULTVISITA,TELEFONO,LIMITECREDITO,NIVELPRECIO,PERCEPCION,TIPO_CONTRIBUYENTE,COORX,COORY,MEDIAPAGO,NIT "+
				 "FROM P_CLIENTE WHERE CODIGO='"+cod+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			lblNom.setText(DT.getString(0));
			lblRep.setText(DT.getString(12));
			lblDir.setText(DT.getString(2));
			
			tel=DT.getString(4);
			uvis=DT.getInt(3);
			
			nivel=DT.getInt(6);
			gl.nivel=nivel;
			gl.percepcion=DT.getDouble(7);
			
			contr=""+DT.getString(8);
			gl.contrib=contr;
			
			if (uvis<=0) {
				lblAten.setText("");
			} else {	
				lblAten.setText(du.sfecha(uvis));
			}
			
			lblTel.setText("Tel : "+tel);
			sgps=mu.frmgps(DT.getDouble(9))+" , "+mu.frmgps(DT.getDouble(10));
			lblGPS.setText(sgps);
			
			gl.media=DT.getInt(11);
					
			clim=DT.getDouble(5);
						
			gl.fnombre=DT.getString(0);
			gl.fnit=DT.getString(12);
			gl.fdir=DT.getString(2);
			
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());
	    }
		
	}
		
	private void calcCredit() {
			
		NumberFormat format = NumberFormat.getInstance(); 
		format.setGroupingUsed(true);
		format.setMaximumFractionDigits(0);
		
		lblCLim.setText("-");lblCUsed.setText("-");lblCDisp.setText("-");
		
		cused=getUsedCred();
		cdisp=clim-cused;if (cdisp<0) cdisp=0;
		gl.credito=cdisp;
		
		lblCLim.setText(mu.frmcur(clim));
		lblCUsed.setText(mu.frmcur(cused));
		lblCDisp.setText(mu.frmcur(cdisp));
		
		if (cused==0) {
			lblCobro.setVisibility(View.INVISIBLE);
			imgCobro.setVisibility(View.INVISIBLE);			
		}
				
	}
	
	private double getUsedCred(){
		Cursor DT;
		double tpg,tsal,cu=0;
	
		try {
			sql="SELECT SUM(SALDO) FROM P_COBRO WHERE (CLIENTE='"+cod+"')";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			tsal=DT.getDouble(0);
	
			
			sql="SELECT SUM(TOTAL) FROM D_COBRO WHERE (ANULADO='N') AND (CLIENTE='"+cod+"')";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			tpg=DT.getDouble(0);
			
			cu=tsal-tpg;
		} catch (Exception e) {
		   	cu=0;mu.msgbox(e.getMessage());
	    }	
		return cu;
	}
	
	private void initVenta(){
		if (gl.peModal.equalsIgnoreCase("APR")) {	
			startActivity(new Intent(this,Aprofam1.class));	
		} else {	
			startActivity(new Intent(this,Venta.class));	
		}
	}
	
	private boolean validaVenta() {
		Cursor DT;
		int ci,cf,ca1,ca2,fecha_vigencia, diferencia;
		double dd;
		boolean resguardo=false;

		
		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN,FECHAVIG,RESGUARDO FROM P_COREL ";
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
			fecha_vigencia=DT.getInt(4);
			resguardo=DT.getInt(5)==1;

			if(resguardo==false){
				if(fecha_vigencia< du.getActDate()){
					//#HS_20181128_1556 Cambie el contenido del mensaje.
					mu.msgbox("La resolución esta vencida. No se puede continuar con la venta.");
					return false;
				}
			}

			if(resguardo==false){
				diferencia = fecha_vigencia - du.getActDate();
				if( diferencia <= 30){
					//#HS_20181128_1556 Cambie el contenido del mensaje.
					mu.msgbox("La resolución vence en "+diferencia+". No se puede continuar con la venta.");
					return false;
				}
			}

			if (ca1>=cf) {
				//#HS_20181128_1556 Cambie el contenido del mensaje.
				mu.msgbox("Se han terminado los correlativos de facturas. No se puede continuar con la venta.");
				return false;
			}
			
			dd=cf-ci;dd=0.75*dd;
			ca2=ci+((int) dd);
			
			if (ca1>ca2) {
				//toastcent("Queda menos que 25% de talonario de facturas.");
				//#HS_20181129_1040 agregue nuevo tipo de mensaje
				porcentaje = true;
				msgAskVenta();
			}
			
		} catch (Exception e) {
			mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n"); //+e.getMessage());
			return false;
		}	
					
		return true;
		
	}
	
	
	// Aux
	

	private void msgAskTipoDev(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Bueno", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	setDevType("B");
		    }
		});
		
		dialog.setNegativeButton("Malo", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	setDevType("M");
		    }
		});
		
		dialog.show();
			
	}

	private void msgAskVenta() {

		AlertDialog.Builder dialog1 = new  AlertDialog.Builder(this);

		dialog1.setTitle("Road");
		dialog1.setMessage("Quedan menos del 25% de correlativos disponibles.");

		dialog1.setIcon(R.drawable.ic_quest);

		dialog1.setPositiveButton("Enterado", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				porcentaje = false;
				VerificaCantidad();
			}
		});

		dialog1.show();

	}

	private void setDevType(String tdev) {
		gl.devtipo=tdev;
		Intent intent = new Intent(this,DevolCli.class);
		startActivity(intent);
	}
	
	private void habilitaOpciones()
	{
		try
		{
			String rt;
			boolean flag;

			rt=gl.rutatipog;

			flag=false;
			if (rt.equalsIgnoreCase("V") || rt.equalsIgnoreCase("T")) flag=true;
			if (flag) relV.setVisibility(View.VISIBLE);else relV.setVisibility(View.GONE);

			flag=false;
			if (rt.equalsIgnoreCase("P") || rt.equalsIgnoreCase("T")) flag=true;
			if (flag) relP.setVisibility(View.VISIBLE);else relP.setVisibility(View.GONE);

			flag=false;
			//if (rt.equalsIgnoreCase("D") || rt.equalsIgnoreCase("T")) flag=true;
			if (flag) relD.setVisibility(View.VISIBLE);else relD.setVisibility(View.GONE);

		}catch (Exception ex)
		{
			Log.d("habilitaOpciones_err", ex.getMessage());
		}
	}
	
	protected void toastcent(String msg) {
		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	//#HS_20181213 Proceso para tomar foto de la fachada
	// Camara

	public void tomarFoto(View view){
		int codResult = 1;
		try {

			Intent intento1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File URLfoto = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
			intento1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(URLfoto));
			//startActivity(intento1);
			startActivityForResult(intento1,codResult);

		}catch (Exception e){
			mu.msgbox("tomarFoto: "+ e.getMessage());
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			try {

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				String paht = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
				Bitmap bitmap1 = BitmapFactory.decodeFile(paht);

				bitmap1 = redimensionarImagen(bitmap1, 640, 360);

				FileOutputStream out = new FileOutputStream(paht);
				bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, out);
				out.flush();
				out.close();

			}catch (Exception e){
				mu.msgbox("onActivityResult: " + e.getMessage());
			}

		}

	}

	public void mostrarFachada(View view){
		Cursor DT;
		imgDB = false; imgPath=false;
		try {

            path = (Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
            File archivo = new File(path);

            sql = "SELECT IMAGEN FROM P_CLIENTE_FACHADA WHERE CODIGO ='"+ cod +"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount() > 0){
				DT.moveToFirst();
				imagenbase64 = DT.getString(0);
				imgDB = true;
			}

            if(archivo.exists()){
            	imgPath = true;
                inputFachada();
            }else if(imgDB == true){
				inputFachada();
			}else{
                Toast.makeText(this,"Fachada no disponible",Toast.LENGTH_LONG).show();
            }

		}catch (Exception e){
				mu.msgbox("inputFachada: " + e.getMessage());
		}

	}

	public Bitmap redimensionarImagen(Bitmap mBitmap, float newWidth, float newHeigth){

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeigth) / height;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);

	}

	public void inputFachada(){

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final ImageView imgFachada = new ImageView(this);
		imgFachada.setScaleType(CENTER_CROP);

        if(imgPath == true) {
			Bitmap bitmap1 = BitmapFactory.decodeFile(path);
			imgFachada.setImageBitmap(redimensionarImagen(bitmap1, 1000, 600));
		}else if(imgDB == true) {
			byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
			Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
			imgFachada.setImageBitmap(redimensionarImagen(bitm,1000,600));
		}

		alert.setView(imgFachada);

		alert.show();

	}

	//#HS_20181214 Devuelve JSON: lista de fotos en ROADFOTOS.
	public void listaFachada(View view){

	    Cursor DT;
	    String codigo,imagen64;
        JSONObject json = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONArray json_Array = new JSONArray();

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

            mu.msgbox(json2.toString());

        }catch (Exception e){
	        mu.msgbox("listaFachada: " + e.getMessage());
        }
    }

	// GPS
	
	private void defineGeoPos(){
		
	}

	
	// Activity Events
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putString("CLIID", cod);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		String cliid=savedInstanceState.getString("CLIID");
		
		//Toast.makeText(this, "Restored from a shitty crash : "+cliid, Toast.LENGTH_SHORT).show();
		
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    
	    if (gl.closeCliDet) super.finish();
	    
	    calcCredit();
	    
	    if (browse==1) {
	    	browse=0;
	    	initVenta();return;
	    }
	    
	    if (browse==2) {
	    	browse=0;
	    	showData();return;
	    }
  
	    
	}

	@Override
	protected void onPause() {
	    super.onPause();
	}	
	
	

}
