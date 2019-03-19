package com.dts.roadp;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ComWSExist extends PBase {

	private TextView lblInfo,lblParam;
	private ProgressBar barInfo;
	private EditText txtRuta,txtWS,txtEmp;
	
	private int isbusy,reccnt;
	private String ruta;
	
	private SQLiteDatabase dbT;
	private BaseDatos ConT;
	private BaseDatos.Insert insT;

	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayList<String> results=new ArrayList<String>();


	// Web Service

	public AsyncCallRec wsRtask;

	private static String sstr,fstr,fprog,ferr,idbg,dbg;
	private int scon;
	private String gEmpresa;
	
	private final String NAMESPACE ="http://tempuri.org/";
	private String METHOD_NAME,URL;

	private clsDataBuilder dbld;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_wsexist);
		
		super.InitBase();
		addlog("ComWSExist",""+du.getActDateTime(),gl.vend);

		System.setProperty("line.separator","\r\n");
			
		lblInfo= (TextView) findViewById(R.id.lblETipo);
		lblParam= (TextView) findViewById(R.id.lblProd);
		barInfo= (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta= (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
		txtWS= (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
		txtEmp= (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);	
		
		isbusy=0;
		
		lblInfo.setText("");lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);
		
		ruta=gl.ruta;
	    gEmpresa=gl.emp;
			
		getWSURL();

		dbld=new clsDataBuilder(this);

		Handler mtimer = new Handler();	
		Runnable mrunner=new Runnable() {
	        @Override
	        public void run() {
	        	runRecep();
		    }
	    };
		mtimer.postDelayed(mrunner,1000); 	
		
	}

	// Events
	
	public void askRec(View view)
	{

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
	
	
	// Main
	
	private void runRecep() {

		try{
			if (isbusy==1) return;

			if (!setComParams()) return;

			isbusy=1;

			barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
			lblInfo.setText("Conectando ...");

			wsRtask = new AsyncCallRec();
			wsRtask.execute();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}
		
	
	// Web Service Methods
	
	public int fillTable(String value,String delcmd) {
		int rc;
		String s,ss;
	
		METHOD_NAME = "getIns";
		sstr="OK";
		
		try {
			
			idbg=idbg+" filltable ";
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(value);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        idbg=idbg+" rec " +rc +"  ";
	           
	        s="";
	         
	        // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
	        // 	idbg=idbg+" RC ="+rc+"---";
	        //}
	        
	        for (int i = 0; i < rc; i++) {
	        	
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	//s=s+str+"\n";
	        	
	        	if (i==0) {
	        		
	        		 idbg=idbg+" ret " +str +"  ";
	        		
	        		if (str.equalsIgnoreCase("#")) {
	        			listItems.add(delcmd);
	        		} else {
	        			idbg=idbg+str;
	        			sstr=str;return 0;
	        		}
	        	} else {
	        		try {
	    			    sql=str;	
	    			    listItems.add(sql);
	    			    sstr=str;
		    		} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		    		   	sstr=e.getMessage();
		    	    }	
	        	}
	        }
	        
	        return 1;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    	idbg=idbg+" ERR "+e.getMessage();
	    	return 0;
	    }
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
			param.setName("SQL");param.setValue(sql);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        
	        for (int i = 0; i < rc+1; i++)
	        {
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	
	        	if (i==0) {
	        		sstr=str;
	        		if (!str.equalsIgnoreCase("#")) {
	        			sstr=str;
	        			return 0;
	        		}
	        	} else {	
	        		results.add(str);
	        	}
	        }
	 
	        return 1;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    	sstr=e.getMessage(); 
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
			param.setName("Value");param.setValue("OK");
	    
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
			
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
	        
	        sstr = response.toString()+"..";
	        	        
	        return 1;
	    } catch (Exception e) {
			 sstr=e.getMessage();
	    }
		
		return 0;
	}	
		
	
	// WEB SERVICE - RECEPCION

	private boolean getData()
	{
		Cursor DT;
		int rc,prn,jj;
		String s,val="";
		boolean TieneRuta = false;
		boolean TieneClientes = false;
		boolean TieneProd = false;

		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";	
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				val=DT.getString(0);
			}

			DT.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			val="N";
		}	
		
		if (val.equalsIgnoreCase("S"))gl.peStockItf=true; else gl.peStockItf=false;
				
		
		listItems.clear();
		idbg="";
		
		try {

			sql = "SELECT CODIGO FROM P_RUTA";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				TieneRuta = DT.getCount() > 0;
			}

			DT.close();

			sql = "SELECT CODIGO FROM P_CLIENTE";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				TieneClientes = DT.getCount() > 0;
			}

			DT.close();


			sql = "SELECT CODIGO FROM P_PRODUCTO";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				TieneProd = DT.getCount() > 0;
			}

			DT.close();

			if(TieneRuta && TieneProd && TieneClientes){
				if (!AddTable("P_STOCKINV")) return false;
				if (!AddTable("TMP_PRECESPEC")) return false;
				if (!AddTable("P_PRODPRECIO")) return false;
				if (!AddTable("P_STOCK")) return false;
				if (!AddTable("P_STOCK_PALLET")) return false;
				if (!AddTable("P_STOCKB")) return false;
				if (!AddTable("P_FACTORCONV")) return false;
			}else{
				msgbox("No tiene datos de la ruta, clientes y productos, debe hacer una carga de datos completa");
				return false;
			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
		
		ferr="";
		
		try {
			
			rc=listItems.size();reccnt=rc;
			if (rc==0) return true;
			
			fprog="Procesando ...";
			wsRtask.onProgressUpdate();
			
			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
		 	ConT.vDatabase =dbT;
		    insT=ConT.Ins;

			dbT.beginTransaction();

			prn=0;jj=0;

		    for (int i = 0; i < rc; i++)
		    {
		    	sql=listItems.get(i);

				try
				{
					dbT = ConT.getWritableDatabase();
					dbT.execSQL(sql);
				}
				catch (Exception e)
				{
					Log.d("M","Something happend there " + e.getMessage());
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage() + "EJC","Yo fui " + sql);
					Log.e("z", e.getMessage());
				}


				try {
					if (i % 10==0)
					{
						fprog = "Procesando: " + i + " de: " + (rc-1);
						wsRtask.onProgressUpdate();
						SystemClock.sleep(20);
					}
				} catch (Exception e)
				{
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					Log.e("z", e.getMessage());
				}
		    }

			dbT.setTransactionSuccessful();
			dbT.endTransaction();

			fprog="Registrando el documento recibido de inventario en BOF...";
			wsRtask.onProgressUpdate();

			Actualiza_Documentos();

			fprog="Fin de actualización";
			wsRtask.onProgressUpdate();

		} catch (Exception e) {

			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			
			try {
				ConT.close();  
			} catch (Exception ee) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			}
			
			sstr=e.getMessage();
			ferr=sstr+"\n"+sql;
			
			return false;
		}
		
		try {
			ConT.close();  
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}
		
		return true;
	}

	private String fterr;

	//#EJC20181120: Inserta los documentos que bajaron a la HH
	private boolean Actualiza_Documentos()
	{

		DateUtils DU = new DateUtils();
		long Now=du.getFechaActual();

		String ruta = txtRuta.getText().toString().trim();

		try{

			String SQL = " INSERT INTO P_DOC_ENVIADOS_HH " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK WHERE FECHA = '" +  Now + "' AND RUTA = '" + ruta + "' " +
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

			if (commitSQL()==1)
			{
				return  true;
			} else
			{
				fterr+="\n"+sstr;
				dbg=sstr;
				return  false;
			}

		}catch (Exception e)
		{
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			Log.e("Error",e.getMessage());
			return  false;
		}

	}

	public int commitSQL() {
		int rc;
		String s,ss;

		METHOD_NAME = "Commit";
		sstr="OK";

		if (dbld.size()==0) return 1;

		s="";
		for (int i = 0; i < dbld.size(); i++) {
			ss=dbld.items.get(i);
			s=s+ss+"\n";
		}

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(s);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE+METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			sstr=e.getMessage();
		}

		return 0;
	}

	private boolean AddTable(String TN) {
		String SQL;
		
		try {
			
			fprog=TN;idbg=TN;
			wsRtask.onProgressUpdate();

			SQL=getTableSQL(TN);
			if (fillTable(SQL,"DELETE FROM "+TN)==1) {
				idbg=idbg +SQL+"#"+"PASS OK";
				return true;	
			} else {	
				idbg=idbg +SQL+"#"+" PASS FAIL  ";
				fstr=sstr;
				return false;	
			}
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
			return false;
		}
	}
	
	private String getTableSQL(String TN) {

       String SQL="";
       String fsql,fsqli,fsqlf;

		fsql=du.univfechasql(du.getActDate());
		fsqli=du.univfechasql(du.ffecha00(du.getActDate()))+" 00:00:00";
		fsqlf=du.univfechasql(du.ffecha24(du.getActDate()))+" 23:59:59";

		try{
		  if (TN.equalsIgnoreCase("P_STOCKINV")) {
			  SQL = "SELECT * FROM P_STOCKINV";
			  return SQL;
		  }
		  if (TN.equalsIgnoreCase("P_STOCK")) {

			  if (gl.peModal.equalsIgnoreCase("TOL")) {
				  SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						  "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') " +
						  "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) AND (ENVIADO = 0)";
			  } else if (gl.peModal.equalsIgnoreCase("APR")) {
				  SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						  "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
			  } else {
				  SQL = "SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
						  "FROM P_STOCK WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsql + "') ";
			  }

			  return SQL;
		  }

		  //CKFK 20190222 Agregué a la consulta el AND (ENVIADO = 0)
		  if (TN.equalsIgnoreCase("P_STOCKB")) {
			  SQL = "SELECT RUTA, BARRA, CODIGO, CANT, COREL, PRECIO, PESO, DOCUMENTO,dbo.AndrDate(FECHA), ANULADO, CENTRO, " +
					  "STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA, DOC_ENTREGA " +
					  "FROM P_STOCKB WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
					  "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			  return SQL;
		  }

		  //CKFK 20190304 Agregué la consulta para obtener los datos de P_STOCK_PALLET
		  if (TN.equalsIgnoreCase("P_STOCK_PALLET")) {
			  SQL = "SELECT DOCUMENTO, RUTA, BARRAPALLET, CODIGO, BARRAPRODUCTO, LOTEPRODUCTO, CANT, COREL, PRECIO, PESO, " +
					  "UNIDADMEDIDA,dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, DOC_ENTREGA  " +
					  "FROM P_STOCK_PALLET WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') " +
					  "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";
			  return SQL;
		  }


		  if (TN.equalsIgnoreCase("P_FACTORCONV")) {
			  //#EJC20181112
			  //SQL = "SELECT PRODUCTO,UNIDADSUPERIOR,FACTORCONVERSION,UNIDADMINIMA FROM P_FACTORCONV ";
			  SQL = " SELECT * FROM P_FACTORCONV WHERE PRODUCTO IN (SELECT CODIGO " +
					  " FROM P_PRODUCTO WHERE LINEA IN (SELECT DISTINCT LINEA FROM P_LINEARUTA " +
					  " WHERE RUTA = '" + gl.ruta + "')) " +
					  " OR ((PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + gl.ruta + "') " +
					  " OR PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + gl.ruta + "')))";

			  return SQL;
		  }


		  if (TN.equalsIgnoreCase("P_PRODPRECIO")) {

			  SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO ";
			  SQL += " WHERE ( (CODIGO IN ( SELECT CODIGO FROM P_PRODUCTO WHERE (LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE RUTA='" + gl.ruta + "')) ) ) ";
			  SQL += " OR  (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + gl.ruta + "')) ) ";
			  SQL += " AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + gl.ruta + "'))) ";
			  return SQL;
		  }

		  if (TN.equalsIgnoreCase("TMP_PRECESPEC")) {
			  SQL = "SELECT CODIGO,VALOR,PRODUCTO,PRECIO,UNIDADMEDIDA FROM TMP_PRECESPEC ";
			  SQL += " WHERE RUTA='" + gl.ruta + "' AND (FECHA>='" + fsqli + "') AND (FECHA<='" + fsqlf + "') ";
			  return SQL;
		  }


	  }catch (Exception e){
		  addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	  }
       return SQL;
	}   
			
	
	// Web Service handling Methods
	
	public void wsExecute(){
				
		fstr="No connect";scon=0;
					
		try {
					
			if (getTest()==1) {
				scon=1;
			} else {
			}
				
			idbg=idbg + sstr;
			
			if (scon==1) {
				fstr="Sync OK";
				if (!getData()) fstr="Recepción incompleta : "+sstr;
			} else {	
				fstr="No se puede conectar al web service : "+sstr;
			}
				
		} catch (Exception e) {
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
			Log.d("E",fstr+sstr);
		}
		
	}
			
	public void wsFinished(){
			
		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);
		 try{
			 if (fstr.equalsIgnoreCase("Sync OK")) {
				 lblInfo.setText(" ");
				 s="Actualización completa.";
				 toastcent(s);
				 super.finish();
			 } else {
				 lblInfo.setText(fstr);
				 mu.msgbox("Ocurrio error : \n"+fstr+" ("+reccnt+") " + ferr);
			 }

			 isbusy=0;
			 //mu.msgbox("::"+dbg);
		 }catch (Exception e){
			 addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		 }

		
	}
	
	private class AsyncCallRec extends AsyncTask<String, Void, Void> {

		@Override
        protected Void doInBackground(String... params) {
			try {
				wsExecute();
			} catch (Exception e) {
				if (scon==0){
					fstr="No se puede conectar al web service : "+sstr;
					lblInfo.setText(fstr);
				}
				msgbox(fstr);
			}
            
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
        	try{
				wsFinished();
			}catch (Exception e){
				Log.d("onPostExecute",e.getMessage());
			}
        }
 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {
				Log.d("onPreExecute",e.getMessage());
    		}
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(fprog);
    		} catch (Exception e) {
				Log.d("onProgressUpdate",e.getMessage());
    		}
        }
 
    }	
	
		
	// Aux
	
	public void comManual(View view) {
		try{
			Intent intent = new Intent(this,ComDrop.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
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
			DT.moveToFirst();
					
			wsurl=DT.getString(0);
			
			URL=wsurl;
			txtWS.setText(URL);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			//MU.msgbox(e.getMessage());
			URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
			//txtWS.setText("");
			return;
		}
		
	}
	
	private boolean setComParams() {
		String ss;
		
		ss=txtRuta.getText().toString().trim();

		try{
			if (mu.emptystr(ss)) {
				mu.msgbox("La ruta no está definida.");return false;
			}

			ss=txtEmp.getText().toString().trim();
			if (mu.emptystr(ss)) {
				mu.msgbox("La empresa no está definida.");return false;
			}
			gEmpresa=ss;

			ss=txtWS.getText().toString().trim();
			if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
				mu.msgbox("La dirección de Web service no está definida.");return false;
			}
			URL=ss;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		
		return true;
	}
	
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		try {
			if (isbusy==0) {
				super.onBackPressed();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}	
		
}
