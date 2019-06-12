package com.dts.roadp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;

import uk.co.senab.photoview.PhotoViewAttacher;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class CliDet extends PBase {

	private TextView lblNom,lblRep,lblDir,lblAten,lblTel,lblGPS;
	private TextView lblCLim,lblCUsed,lblCDisp,lblCobro,lblDevol,lblCantDias,lblClientePago;
	private RelativeLayout relV,relP,relD,relCamara;//#HS_20181213 relCamara
	private ImageView imgCobro,imgDevol,imgRoadTit;
	private RadioButton chknc,chkncv;

	private PhotoViewAttacher zoomFoto;
	private AppMethods app;

	private Exist Existencia = new Exist();
	private String cod,tel, Nombre, NIT, sgp1, sgp2;
	private String imagenbase64,path,fechav;
	private Boolean imgPath, imgDB, ventaGPS,flagGPS=true,permiteVenta=true;
	private double gpx,gpy,credito,clim,cused,cdisp,cred;
	private int nivel,browse,merc,rangoGPS,modoGPS;
	private boolean porcentaje = false;
	private byte[] imagenBit;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_det);
		
		super.InitBase();
		addlog("CliDet",""+du.getActDateTime(),gl.vend);
		
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
		lblCantDias = (TextView) findViewById(R.id.lblCantDias);
		lblClientePago = (TextView) findViewById(R.id.lblClientePago);

		chknc = new RadioButton(this,null);
		chkncv = new RadioButton(this,null);

	//	relMain=(RelativeLayout) findViewById(R.id.relclimain);
		relV=(RelativeLayout) findViewById(R.id.relVenta);
		relP=(RelativeLayout) findViewById(R.id.relPreventa);
		relD=(RelativeLayout) findViewById(R.id.relDespacho);
		relCamara=(RelativeLayout) findViewById(R.id.relCamara);
		
		imgCobro= (ImageView) findViewById(R.id.imageView2);
		imgDevol= (ImageView) findViewById(R.id.imageView1);
		imgRoadTit = (ImageView) findViewById(R.id.imgRoadTit);

		app = new AppMethods(this, gl, Con, db);
		credito=gl.credito;

		cod=gl.cliente;

		ventaGPS=gl.peVentaGps!=0;
		rangoGPS=gl.peLimiteGPS+gl.peMargenGPS;
		flagGPS=ventaGPS;
		if (rangoGPS<=1) flagGPS=false;
		if (gl.gpsdist<1) flagGPS=false;

		if (flagGPS) {
			permiteVenta=gl.gpsdist<=rangoGPS;
		} else {
			permiteVenta=true;
		}

		gl.gpspass=false;

		sgp2 =" ( "+mu.frmint(rangoGPS)+"m ) ";
		sgp1 =" ( "+mu.frmint(gl.gpsdist)+"m ) ";

		/*if (!gl.devol) {
			lblDevol.setVisibility(View.INVISIBLE);
			imgDevol.setVisibility(View.INVISIBLE);
		}*/

		showData();
		calcCredit();

		browse=0;
		merc=1;

		habilitaOpciones();

		miniFachada();

		setHandlers();

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try{
			super.onSaveInstanceState(savedInstanceState);
			savedInstanceState.putString("CLIID", cod);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		try{
			super.onRestoreInstanceState(savedInstanceState);
			String cliid=savedInstanceState.getString("CLIID");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}


	//region  Events

	public void showVenta(View view){
		if (!permiteVenta) {
			if (gl.peVentaGps==1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 +"!\nPara realizar la venta debe asercarse más al cliente.");
				return;
			} else {
				modoGPS=1;
				msgAskGPSVenta();
			}
		} else {
			doVenta();
		}
	}

	public void showPreventa(View view) {
		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\nPara realizar la venta debe asercarse más al cliente.");
				return;
			} else {
				modoGPS = 2;
				msgAskGPSVenta();
			}
		} else {
			doPreventa();
		}
	}

	public void showDespacho(View view) {

		if (!permiteVenta) {
			msgbox("¡Distancia del cliente mayor que permitida!\nPara realizar la venta debe asercarse más al cliente.");return;
		}

		try {
			mu.msgbox("La funcionalidad no esta implementada.");

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		//gl.rutatipo="D";
		//runVenta();
	}

	public void showCredit(View viev){
		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\nPara realizar el cobro debe asercarse más al cliente.");
				return;
			} else {
				modoGPS = 3;
				msgAskGPSVenta();
			}
		} else {
			doCredit();
		}
	}

	public void showDevol(View view){
		if (!permiteVenta) {
			if (gl.peVentaGps == 1) {
				msgbox("¡Distancia del cliente "+ sgp1 +" es mayor que permitida "+ sgp2 + "!\nPara realizar la devolución debe asercarse más al cliente.");
				return;
			} else {
				modoGPS = 4;
				msgAskGPSVenta();
			}
		} else {
			msgAskTipoDev();
		}
	}

	public void tomarFoto(View view){
		int codResult = 1;
		try{
			if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
				msgbox("El dispositivo no soporta toma de foto");return;
			}

			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());

		//	try {

				Intent intento1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File URLfoto = new File(Environment.getExternalStorageDirectory() + "/RoadFotos/" + cod + ".jpg");
				intento1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(URLfoto));
				startActivityForResult(intento1,codResult);

		/*	}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				//mu.msgbox("tomarFoto: "+ e.getMessage());
				mu.msgbox("No se puede activar la camara. ");
			}*/
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox(e.getMessage());
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

			if (archivo.exists()) {
				imgPath = true;
				inputFachada();
			}else if(imgDB == true){
				inputFachada();
			}else{
				Toast.makeText(this,"Fachada no disponible",Toast.LENGTH_LONG).show();
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("inputFachada: " + e.getMessage());
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

				File archivo = new File(path);
				imgRoadTit.setImageURI(Uri.fromFile(archivo));
			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				//mu.msgbox("onActivityResult: " + e.getMessage());
			}

		}

	}

	private void setHandlers(){

		try {

			chknc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					if (chknc.isChecked()==true){
						chkncv.setChecked(false);
						gl.tiponcredito = 1;
					}

				}
			});

			chkncv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					if (chkncv.isChecked()==true){
						chknc.setChecked(false);
						gl.tiponcredito = 2;

						VerificaCantidad();

					}

				}
			});

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region Main
	
	private void showData() {
		Cursor DT;
		int uvis;
		String contr,sgps="0.00000000 , 0.00000000";
		
		lblNom.setText("");lblRep.setText("");
		lblDir.setText("");lblAten.setText("");lblTel.setText("");
		tel="";
		
		try {

			sql="SELECT NOMBRE,NOMBRE_PROPIETARIO,DIRECCION,ULTVISITA,TELEFONO,LIMITECREDITO,NIVELPRECIO,PERCEPCION,TIPO_CONTRIBUYENTE, " +
				"COORX,COORY,MEDIAPAGO,NIT,VALIDACREDITO,BODEGA,CHEQUEPOST,TIPO,DIACREDITO "+
				"FROM P_CLIENTE WHERE CODIGO='"+cod+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			lblNom.setText(cod + " - " + DT.getString(0));
			lblRep.setText(DT.getString(12));
			lblDir.setText(DT.getString(2));
			lblCantDias.setText(DT.getString(17));

			tel=DT.getString(4);
			lblTel.setText(DT.getString(4));
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

			sgps=mu.frmgps(DT.getDouble(9))+" , "+mu.frmgps(DT.getDouble(10));
			lblGPS.setText(sgps);
			
			gl.media=DT.getInt(11);

			if(gl.media != 4){
				lblClientePago.setText("CONTADO");
			}else if(gl.media == 4){
				lblClientePago.setText("CRÉDITO");
			}

			clim=DT.getDouble(5);
						
			gl.fnombre=DT.getString(0);
			gl.fnit=DT.getString(12);
			gl.fdir=DT.getString(2);
			gl.vcredito = DT.getString(13).equalsIgnoreCase("S");
			gl.vcheque = DT.getString(14).equalsIgnoreCase("S");
			gl.vchequepost = DT.getString(15).equalsIgnoreCase("S");
			gl.clitipo = DT.getString(16);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox(e.getMessage());
	    }
		
	}
		
	private void calcCredit() {


		try{
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

			if (gl.peModal.equalsIgnoreCase("TOL")) {
				lblCobro.setVisibility(View.VISIBLE);
				imgCobro.setVisibility(View.VISIBLE);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	cu=0;mu.msgbox(e.getMessage());
	    }	
		return cu;
	}
	
	private void initVenta(){

		try{
			if (gl.peModal.equalsIgnoreCase("APR")) {
				startActivity(new Intent(this,Aprofam1.class));
			} else {
				startActivity(new Intent(this,Venta.class));
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private boolean validaVenta() {
		Cursor DT;
		int ci,cf,ca1,ca2;
		long fecha_vigencia, diferencia;
		double dd;
		boolean resguardo=false;

		
		try {
			sql="SELECT SERIE,CORELULT,CORELINI,CORELFIN,FECHAVIG,RESGUARDO FROM P_COREL ";
			DT=Con.OpenDT(sql);
				
			DT.moveToFirst();
			
			ca1=DT.getInt(1);
			ci=DT.getInt(2);
			cf=DT.getInt(3);
			fecha_vigencia=DT.getLong(4);
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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("No esta definido correlativo de factura. No se puede continuar con la venta.\n"); //+e.getMessage());
			return false;
		}	
					
		return true;
		
	}

	private void miniFachada(){
		Cursor DT;
		imgDB = false;
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
				imgRoadTit.setImageURI(Uri.fromFile(archivo));
			}else if(imgDB == true){
				byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
				Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
				imgRoadTit.setImageBitmap(redimensionarImagen(bitm,200,200));
			}else{
				imgRoadTit.setImageResource(R.drawable.cliente);
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("inputFachada: " + e.getMessage());
		}
	}

	//endregion

	//region Fachada

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

		try{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			final ImageView imgFachada = new ImageView(this);
			imgFachada.setScaleType(CENTER_CROP);


			if(imgPath == true) {
				Bitmap bitmap1 = BitmapFactory.decodeFile(path);
				imgFachada.setImageBitmap(redimensionarImagen(bitmap1, 640, 360));
			}else if(imgDB == true) {
				byte[] btImagen = Base64.decode(imagenbase64, Base64.DEFAULT);
				Bitmap bitm = BitmapFactory.decodeByteArray(btImagen,0,btImagen.length);
				imgFachada.setImageBitmap(redimensionarImagen(bitm,640,360));
			}

			alert.setView(imgFachada);

			alert.show();

			imgFachada.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					zoomFoto = new PhotoViewAttacher(imgFachada);
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//endregion

	//region  Misc

	private void doVenta(){

		try{
			gl.banderaCobro = false;

			if (!validaVenta()) return;//Se valida si hay correlativos de factura para la venta

			if(porcentaje == false) VerificaCantidad();

		} catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			mu.msgbox("doVenta: " + e.getMessage());
		}

	}

	private void doPreventa() {
		try{
			gl.rutatipo="P";
			runVenta();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void doCredit(){
		try{
			gl.validarCred=2;
			gl.banderaCobro = true;
			Intent intent = new Intent(this,Cobro.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	public void VerificaCantidad(){
		Float cantidad;
		gl.rutatipo="V";
		//Asigna conexión actual a la forma de Existencias.
		Existencia.Con = Con;
		cantidad = Float.valueOf(Existencia.CantExistencias());

		try{
			if(cantidad == 0){
				mu.msgbox("No hay existencias disponibles.");
			}else{
				if(gl.tiponcredito == 2){
					return;
				}else {
					runVenta();
				}
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void runVenta() {

		try{
			if (merc==1) {
				browse=1;
				Intent intent = new Intent(this,MercLista.class);
				startActivity(intent);
			} else {
				initVenta();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showDir(View view) {
		try{
			//mu.msgbox(lblDir.getText().toString() + "\n" + lblRep.getText().toString());
			msgAskEditCliente();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void inputCliente() {

		try{
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
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void ActualizarCliente(String corel, String NombreEdit, String NitEdit){
		Cursor DT;
		try {
			db.execSQL("INSERT INTO D_FACTURAF(COREL, NOMBRE, NIT, DIRECCION) VALUES('"+corel+"','"+NombreEdit+"','"+NitEdit+"','')");
			mu.msgbox("Registro actualizado");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("ActualizarCliente: "+e.getMessage());
		}
	}

	public void setGPS(View view) {
		try{
			browse=2;
			startActivity(new Intent(this,CliGPS.class));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void sendSMS(View view){
		String to=tel;

		//to="59393195";

		if (to.length()==0) {
			msgbox("Número incorrecto ");return;
		}

		try {
			Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
			startActivity(launchIntent);
			//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+to)));
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			msgbox("No pudo enviar mensaje verifique que esté instalada la aplicación");
		}

		//try {
		//    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
		//     i.setType("vnd.android-dir/mms-sms");
		//     startActivity(i);
		//} catch (Exception e) {
		//     mu.msgbox("E","No se puede enviar mensaje");
		//}
	}

	@SuppressLint("MissingPermission")
	public void callPhone(View view){
		String to=tel;


		//to="42161467";

		try {

			if (to.length()==0) {
				msgbox("Número incorrecto ");return;
			}

			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+to));
			startActivity(callIntent);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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

			String sgps = "";

			sgps=lblGPS.getText().toString();

			if (!sgps.equalsIgnoreCase("0.0000000 , 0.0000000")){
				String url = "waze://?ll="+sgps;
				//"waze://?ll=14.6017278,-90.5236343";
				//"waze://?ll=14.586997,-90.513685";
						Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
				startActivity( intent );
			}else{
				msgbox("El cliente no tiene definidas las coordenadas GPS");
			}

		} catch ( ActivityNotFoundException ex )	{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			Intent intent =
					new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
			startActivity(intent);
		}

	}

	private void setDevType(String tdev) {
		try{
			gl.devtipo=tdev;
			Intent intent = new Intent(this,DevolCli.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void habilitaOpciones() {
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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),ex.getMessage(),"");
			Log.d("habilitaOpciones_err", ex.getMessage());
		}
	}

	protected void toastcent(String msg) {

		try{
			Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void doExit(){
		try{
			gl.closeCliDet=true;
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region Dialogs

	private void msgAskGPSVenta() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¡Distancia del cliente "+ sgp1 +" es mayor que la permitida "+ sgp2 + "!\n¿Está seguro de continuar?");
			dialog.setIcon(R.drawable.ic_quest);
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gl.gpspass=true;
					switch (modoGPS) {
						case 1:
							doVenta();break;
						case 2:
							doPreventa();break;
						case 3:
							doCredit();break;
						case 4:
							msgAskTipoDev();break;
					}
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

	private void msgAskExit(String msg) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿" + msg + "?");

			dialog.setIcon(R.drawable.ic_quest);
			dialog.setCancelable(false);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					doExit();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					;
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void msgAskTipoEstadoDev(String msg) {
		try{
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

			dialog.setCancelable(false);
			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	private void msgAskTipoDev() {

		try{

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Devolución");

			final LinearLayout layout   = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			if(chknc.getParent()!= null){
				((ViewGroup) chknc.getParent()).removeView(chknc);
			}

			if(chkncv.getParent()!= null){
				((ViewGroup) chknc.getParent()).removeView(chknc);
			}

			chknc.setText("Nota de crédito");
			chkncv.setText("Nota de crédito con venta");

			layout.addView(chknc);
			layout.addView(chkncv);

			alert.setView(layout);

			showkeyb();
			alert.setCancelable(false);
			alert.create();

			alert.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(chkncv.isChecked() || chknc.isChecked()){
						msgAskTipoEstadoDev("Devolución de producto en estado...");
						layout.removeAllViews();
					}else{
						//toast("Seleccione accion a realizar");
						closekeyb();
						layout.removeAllViews();
						msgAskTipoDev();
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

	private void msgAskVenta() {

		try{
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
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	private void msgAskEditCliente() {
		try{
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
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	//endregion



	//region Activity Events

	@Override
	protected void onResume() {

		try{
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

			if (gl.dvbrowse!=0){

				gl.rutatipo = "V";

				browse =3;

				if (browse==3){//Se utiliza para la devolución de cliente.
					initVenta();return;
				}return;

			}


		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}

	@Override
	public void onBackPressed() {
		try{
			//msgAskExit("Salir");
			super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

}
